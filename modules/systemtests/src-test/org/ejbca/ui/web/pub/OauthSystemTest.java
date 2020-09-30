/*************************************************************************
 *                                                                       *
 *  EJBCA Community: The OpenSource Certificate Authority                *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/

package org.ejbca.ui.web.pub;

import com.nimbusds.jose.util.Base64URL;
import org.bouncycastle.jce.X509KeyUsage;
import org.bouncycastle.operator.OperatorCreationException;
import org.cesecore.CaTestUtils;
import org.cesecore.SystemTestsConfiguration;
import org.cesecore.authentication.oauth.OAuthKeyInfo;
import org.cesecore.authentication.tokens.AuthenticationToken;
import org.cesecore.authentication.tokens.OAuth2AuthenticationTokenMetaData;
import org.cesecore.authentication.tokens.UsernamePrincipal;
import org.cesecore.authorization.AuthorizationDeniedException;
import org.cesecore.authorization.control.StandardRules;
import org.cesecore.authorization.user.AccessMatchType;
import org.cesecore.authorization.user.matchvalues.OAuth2AccessMatchValue;
import org.cesecore.certificates.ca.CA;
import org.cesecore.certificates.util.AlgorithmConstants;
import org.cesecore.configuration.GlobalConfigurationSessionRemote;
import org.cesecore.keys.token.CryptoTokenOfflineException;
import org.cesecore.keys.util.KeyTools;
import org.cesecore.mock.authentication.tokens.TestAlwaysAllowLocalAuthenticationToken;
import org.cesecore.roles.Role;
import org.cesecore.roles.RoleExistsException;
import org.cesecore.roles.management.RoleSessionRemote;
import org.cesecore.roles.member.RoleMember;
import org.cesecore.roles.member.RoleMemberSessionRemote;
import org.cesecore.util.CertTools;
import org.cesecore.util.CryptoProviderTools;
import org.cesecore.util.EjbRemoteHelper;
import org.ejbca.config.GlobalConfiguration;
import org.ejbca.config.WebConfiguration;
import org.ejbca.core.ejb.config.ConfigurationSessionRemote;
import org.ejbca.core.model.authorization.AccessRulesConstants;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.CertificateParsingException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Tests http requests with bearer token (oauth)
 */
public class OauthSystemTest {

    private static final String OAUTH_SUB = "OauthSystemTestSub";
    private static final String CA = "OauthSystemTestCA";
    private static final String OAUTH_KEY = "OauthSystemTestKey";
    private static final String ROLENAME = "OauthSystemTestRole";

    private static final String PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyiRvfMhXb1nLE+bQ8Dtg\n" +
            "P/YFPm6nesE+hNeSlxXQbdRI/Vd6djyynnptBVxZIvRmuax/zQRNqdK+FsoZKQGJ\n" +
            "978PuBhFoLsgCyccrqCEfO2kZp9atXFYoctgXW339Kj2bF5zRhYlSqCD/vBKcjCd\n" +
            "d6q0myEseplcPUzZXWbKHsdP4irjNRS3SwjKjetDBZ6FquAb5jXlSFH9JUx8iRYF\n" +
            "Bv4F3TDWC1NHFp3fpLovUjcZama6nrY7VQfnsLFY2YKPahQqikd4NSny2wmnonnw\n" +
            "Vyos88Ylt//DlzVgijMOvDE4TKF81g4qbd7x8B/JpPxdBk3gXdgJk8+S+scOqfPX\n" +
            "swIDAQAB\n" +
            "-----END PUBLIC KEY-----\n";

    private static final String PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEpAIBAAKCAQEAyiRvfMhXb1nLE+bQ8DtgP/YFPm6nesE+hNeSlxXQbdRI/Vd6\n" +
            "djyynnptBVxZIvRmuax/zQRNqdK+FsoZKQGJ978PuBhFoLsgCyccrqCEfO2kZp9a\n" +
            "tXFYoctgXW339Kj2bF5zRhYlSqCD/vBKcjCdd6q0myEseplcPUzZXWbKHsdP4irj\n" +
            "NRS3SwjKjetDBZ6FquAb5jXlSFH9JUx8iRYFBv4F3TDWC1NHFp3fpLovUjcZama6\n" +
            "nrY7VQfnsLFY2YKPahQqikd4NSny2wmnonnwVyos88Ylt//DlzVgijMOvDE4TKF8\n" +
            "1g4qbd7x8B/JpPxdBk3gXdgJk8+S+scOqfPXswIDAQABAoIBAQCeUCDcqo8Hz1xj\n" +
            "7s7OhsIf9c8vkTwrwLL1GVxeZaBClBLCD0QC3BDMW3eMzkGlRaI6YqYI7AjjKwDj\n" +
            "Gk7QNbtXQ9TMyn2ln0g+U9h7z41Txk6ObNl+5xGSTZTgN2MNw1KTlvlS978nDkWy\n" +
            "YYD8o6R/9zrRkA6kyf1aqRhHtVww82WFbB5DV5yqIxz8wLU7ugzs/2iiV/aqq5cJ\n" +
            "WRHFhiqmtA+88fAdrCTq0DWif6Chf5SYrY2pirTBHFpqVWs/3cq86eoVFpMYlMCY\n" +
            "AxroxHjLmJM0sSB6wCDfJEMgBtMgIm6Boh4xSM6KcOZitcj51dvM3Deh5BhJnNdq\n" +
            "oAtod/ThAoGBAO+9vXFYUBa9Yn+d1PBXc4X0iNMEh7jdGdqD6QRrBOwb2sYEishz\n" +
            "kaIZSM0U8yoApS5vDxGTYSdUm53rX5/d4tfW2BNjomV2dD6u3RyhXBz84aSHeWX8\n" +
            "p02ZzDjHUxR7CZ2ZmbsN1Ite+AKb/zwDt5KaiVSCyNwlhxwKJ454PecDAoGBANfZ\n" +
            "7JN07SBtaSD6Q3N7V7WvhrQzm/GU477+LYhLWtGPp/KylvSKuPK3jL3/cvk+Mm3l\n" +
            "UkCF5sZ3A2fbkymP7koHQPGDqSrKH0qAN4+g5zuy0R6+bKdpqkUVuESLG8YCYfOM\n" +
            "cqhc+JvD4ECYEBNgBsBcwUHLOtu0eSss76bbEFWRAoGAZtj8M2rSeN7oKZ05I54w\n" +
            "pg/gvr4bx3e6xp5+UXHj27KbaQW70ACcQnEcZTaOlr9OHZxxV3XlYO0QEXBPRpL2\n" +
            "5Od7LN46ZdKqTdXQb57dmGX4GxAvSUxZLZZEITuJbajW2DBz3eYx/1RPizcHCOUD\n" +
            "VLZNId81chP7YVEN5TW6QKcCgYEAjeF69foXnAcO4VRfXdsnbg9wVabOzF73zKU6\n" +
            "vKn7imAJHyhwvVEp/LDV3FW690YA0+e2xx688JtuK6hS9TDciuB1ucq3OZ8eLlRV\n" +
            "MR2soLsLZk/5D5oPB9YdB0EBAoiyZepdu3lRGOIJ16ucdX/bMDpH9b1mdOAN/WlO\n" +
            "Jbk85WECgYBMV1RqyFI7eCg5h6F934mU2h/cq9HdYLFX+vvEG+CwYviJF6p5R44u\n" +
            "NAoG0QxYULgcHIscLySYau4lHRgv7hAOrhY+UsJ3MnI97Gea4Gvvu4e5F13fzjlp\n" +
            "GmQnXm8ydcaDNPM6Xp7nMMjNAwXB0H9z9DFKejPFT1aDmnHY+1X+SA==\n" +
            "-----END PRIVATE KEY-----\n";

    private static final AuthenticationToken authenticationToken = new TestAlwaysAllowLocalAuthenticationToken(new UsernamePrincipal("OauthSystemTest"));

    private static GlobalConfigurationSessionRemote globalConfigSession = EjbRemoteHelper.INSTANCE.getRemoteSession(GlobalConfigurationSessionRemote.class);
    private static final ConfigurationSessionRemote configurationSession = EjbRemoteHelper.INSTANCE.getRemoteSession(ConfigurationSessionRemote.class, EjbRemoteHelper.MODULE_TEST);
    private static final RoleSessionRemote roleSession = EjbRemoteHelper.INSTANCE.getRemoteSession(RoleSessionRemote.class);
    private static final RoleMemberSessionRemote roleMemberSession = EjbRemoteHelper.INSTANCE.getRemoteSession(RoleMemberSessionRemote.class);

    private static final String HTTP_HOST = SystemTestsConfiguration.getRemoteHost(configurationSession.getProperty(WebConfiguration.CONFIG_HTTPSSERVERHOSTNAME));
    private static final String HTTP_PORT = SystemTestsConfiguration.getRemotePortHttp(configurationSession.getProperty(WebConfiguration.CONFIG_HTTPSERVERPUBHTTPS));
    private static final String HTTP_REQ_PATH = "https://" + HTTP_HOST + ":" + HTTP_PORT + "/ejbca";
    private static int oAuthKeyInfoInternalId;
    private static CA adminca;
    private static RoleMember roleMember;
    private static String token;
    private static String expiredToken;


    @BeforeClass
    public static void beforeClass() throws NoSuchAlgorithmException, InvalidKeySpecException, AuthorizationDeniedException, RoleExistsException, CertificateParsingException, OperatorCreationException, CryptoTokenOfflineException {
        CryptoProviderTools.installBCProviderIfNotAvailable();
        // Public key
        byte[] pubKeyBytes = KeyTools.getBytesFromPEM(PUBLIC_KEY, CertTools.BEGIN_PUBLIC_KEY, CertTools.END_PUBLIC_KEY);
        // Private key
        final PKCS8EncodedKeySpec pkKeySpec = new PKCS8EncodedKeySpec(KeyTools.getBytesFromPEM(PRIVATE_KEY, CertTools.BEGIN_PRIVATE_KEY, CertTools.END_PRIVATE_KEY));
        final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privKey = keyFactory.generatePrivate(pkKeySpec);

        GlobalConfiguration globalConfiguration = (GlobalConfiguration) globalConfigSession.getCachedConfiguration(GlobalConfiguration.GLOBAL_CONFIGURATION_ID);
        globalConfiguration.getOauthKeys();
        //add oauth key
        OAuthKeyInfo oAuthKeyInfo = new OAuthKeyInfo(OAUTH_KEY, pubKeyBytes, 6000);
        oAuthKeyInfoInternalId = oAuthKeyInfo.getInternalId();
        globalConfiguration.addOauthKey(oAuthKeyInfo);
        globalConfigSession.saveConfiguration(authenticationToken, globalConfiguration);

        final int keyusage = X509KeyUsage.digitalSignature + X509KeyUsage.keyCertSign + X509KeyUsage.cRLSign;
        adminca = CaTestUtils.createTestX509CA("CN=" + CA, "foo123".toCharArray(), false, keyusage);
        // add role
        final Role role1 = roleSession.persistRole(authenticationToken, new Role(null, ROLENAME, Arrays.asList(
                AccessRulesConstants.ROLE_ADMINISTRATOR,
                AccessRulesConstants.REGULAR_VIEWCERTIFICATE,
                StandardRules.CREATECERT.resource(),
                AccessRulesConstants.REGULAR_VIEWENDENTITY,
                AccessRulesConstants.REGULAR_CREATEENDENTITY,
                AccessRulesConstants.REGULAR_EDITENDENTITY,
                AccessRulesConstants.REGULAR_DELETEENDENTITY,
                AccessRulesConstants.REGULAR_REVOKEENDENTITY,
                AccessRulesConstants.REGULAR_VIEWENDENTITYHISTORY
        ), null));
        // Add the second RA role
        roleMember = roleMemberSession.persist(authenticationToken, new RoleMember(OAuth2AuthenticationTokenMetaData.TOKEN_TYPE,
                adminca.getCAId(), OAuth2AccessMatchValue.CLAIM_SUBJECT.getNumericValue(), AccessMatchType.TYPE_EQUALCASE.getNumericValue(),
                OAUTH_SUB, role1.getRoleId(), null));

        token = encodeToken("{\"alg\":\"RS256\",\"kid\":\"" + OAUTH_KEY + "\",\"typ\":\"JWT\"}", "{\"sub\":\"" + OAUTH_SUB + "\"}", privKey);
        final String timestamp = String.valueOf((System.currentTimeMillis() + -60 * 60 * 1000) / 1000); // 1 hour old
        expiredToken = encodeToken("{\"alg\":\"RS256\",\"kid\":\"key1\",\"typ\":\"JWT\"}", "{\"sub\":\"johndoe\",\"exp\":" + timestamp + "}", privKey);

    }


    @AfterClass
    public static void afterClass() throws AuthorizationDeniedException {
        CaTestUtils.removeCa(authenticationToken, adminca.getCAInfo());
        GlobalConfiguration globalConfiguration = (GlobalConfiguration) globalConfigSession.getCachedConfiguration(GlobalConfiguration.GLOBAL_CONFIGURATION_ID);
        globalConfiguration.removeOauthKey(oAuthKeyInfoInternalId);
        globalConfigSession.saveConfiguration(authenticationToken, globalConfiguration);
        roleMemberSession.remove(authenticationToken, roleMember.getId());
        roleSession.deleteRoleIdempotent(authenticationToken, roleMember.getRoleId());
    }

    private static String encodeToken(final String headerJson, final String payloadJson, final PrivateKey key) {
        final StringBuilder sb = new StringBuilder();
        sb.append(Base64URL.encode(headerJson).toString());
        sb.append('.');
        sb.append(Base64URL.encode(payloadJson).toString());
        if (key != null) {
            final byte[] signature = sign(sb.toString().getBytes(StandardCharsets.US_ASCII), key);
            sb.append('.');
            sb.append(Base64URL.encode(signature).toString());
        } else {
            sb.append('.');
        }
        return sb.toString();
    }

    private static byte[] sign(final byte[] toBeSigned, final PrivateKey key) {
        try {
            return KeyTools.signData(key, AlgorithmConstants.SIGALG_SHA256_WITH_RSA, toBeSigned);
        } catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void testAdminWeb() throws IOException {
        final URL url = new URL(HTTP_REQ_PATH + "/adminweb");
        final HttpURLConnection connection = doGetRequest(url, token);
        assertEquals("Response code was not 200", 200, connection.getResponseCode());
        String response = getResponse(connection.getInputStream());
        assertTrue("EJBCA Administration should be accessible", response.contains("EJBCA Administration"));
    }

    @Test
    public void testAdminWebWithExpiredToken() throws IOException {
        final URL url = new URL(HTTP_REQ_PATH + "/adminweb");
        final HttpURLConnection connection = doGetRequest(url, expiredToken);
        assertEquals("Response code was not 200", 200, connection.getResponseCode());
        String response = getResponse(connection.getInputStream());
        assertTrue("Authentication should fail", response.contains("Authentication failed using OAuth Bearer Token"));
    }

    @Test
    public void testRaWeb() throws IOException {
        final URL url = new URL(HTTP_REQ_PATH + "/ra");
        final HttpURLConnection connection = doGetRequest(url, token);
        assertEquals("Response code was not 200", 200, connection.getResponseCode());
        String response = getResponse(connection.getInputStream());
        assertTrue("EJBCA Administration should be accessible", response.contains("Logged in as " + OAUTH_SUB));
    }

    @Test
    public void testAdminRaWithExpiredToken() throws IOException {
        final URL url = new URL(HTTP_REQ_PATH + "/ra");
        final HttpURLConnection connection = doGetRequest(url, expiredToken);
        assertEquals("Response code was not 200", 200, connection.getResponseCode());
        String response = getResponse(connection.getInputStream());
        assertTrue("Authentication should fail", response.contains("Not logged in"));
    }

    @Test
    public void testRestApiWeb() throws IOException {
        final URL url = new URL(HTTP_REQ_PATH + "/ejbca-rest-api/v1/ca");
        final HttpURLConnection connection = doGetRequest(url, token);
        assertEquals("Response code was not 200", 200, connection.getResponseCode());
        String response = getResponse(connection.getInputStream());
        assertTrue("Should return JSON with ca list", response.contains("certificate_authorities"));
    }

    @Test
    public void testAdminRestApiWithExpiredToken() throws IOException {
        final URL url = new URL(HTTP_REQ_PATH + "/ejbca-rest-api/v1/ca");
        final HttpURLConnection connection = doGetRequest(url, expiredToken);
        assertEquals("Response code was not 403", 403, connection.getResponseCode());
        String response = getResponse(connection.getErrorStream());
        assertEquals("Authentication should fail", "Forbidden", connection.getResponseMessage());
        assertTrue("Authentication should fail", response.contains("Authentication failed using OAuth Bearer Token"));
    }

    private String getResponse(InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }
        return sb.toString();
    }

    private HttpURLConnection doGetRequest(URL url, String token) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.getDoOutput();
        connection.connect();
        connection.disconnect();
        return connection;
    }

}
