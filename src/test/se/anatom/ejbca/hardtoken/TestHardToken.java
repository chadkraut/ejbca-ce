/*************************************************************************
 *                                                                       *
 *  EJBCA: The OpenSource Certificate Authority                          *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/

package se.anatom.ejbca.hardtoken;

import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.NamingException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.ejbca.core.ejb.ca.store.CertificateDataBean;
import org.ejbca.core.model.SecConst;
import org.ejbca.core.model.hardtoken.HardTokenData;
import org.ejbca.core.model.hardtoken.types.SwedishEIDHardToken;
import org.ejbca.core.model.hardtoken.types.TurkishEIDHardToken;
import org.ejbca.core.model.log.Admin;
import org.ejbca.core.model.ra.raadmin.GlobalConfiguration;
import org.ejbca.util.Base64;
import org.ejbca.util.CertTools;
import org.ejbca.util.TestTools;

/**
 * Tests the hard token related entity beans.
 *
 * @version $Id: TestHardToken.java,v 1.7 2007-04-13 06:23:55 herrvendil Exp $
 */
public class TestHardToken extends TestCase {
    private static final Logger log = Logger.getLogger(TestHardToken.class);
    private static final Admin admin = new Admin(Admin.TYPE_INTERNALUSER);
    
    private static int orgEncryptCAId;

    static byte[] testcert = Base64.decode(("MIICWzCCAcSgAwIBAgIIJND6Haa3NoAwDQYJKoZIhvcNAQEFBQAwLzEPMA0GA1UE"
            + "AxMGVGVzdENBMQ8wDQYDVQQKEwZBbmFUb20xCzAJBgNVBAYTAlNFMB4XDTAyMDEw"
            + "ODA5MTE1MloXDTA0MDEwODA5MjE1MlowLzEPMA0GA1UEAxMGMjUxMzQ3MQ8wDQYD"
            + "VQQKEwZBbmFUb20xCzAJBgNVBAYTAlNFMIGdMA0GCSqGSIb3DQEBAQUAA4GLADCB"
            + "hwKBgQCQ3UA+nIHECJ79S5VwI8WFLJbAByAnn1k/JEX2/a0nsc2/K3GYzHFItPjy"
            + "Bv5zUccPLbRmkdMlCD1rOcgcR9mmmjMQrbWbWp+iRg0WyCktWb/wUS8uNNuGQYQe"
            + "ACl11SAHFX+u9JUUfSppg7SpqFhSgMlvyU/FiGLVEHDchJEdGQIBEaOBgTB/MA8G"
            + "A1UdEwEB/wQFMAMBAQAwDwYDVR0PAQH/BAUDAwegADAdBgNVHQ4EFgQUyxKILxFM"
            + "MNujjNnbeFpnPgB76UYwHwYDVR0jBBgwFoAUy5k/bKQ6TtpTWhsPWFzafOFgLmsw"
            + "GwYDVR0RBBQwEoEQMjUxMzQ3QGFuYXRvbS5zZTANBgkqhkiG9w0BAQUFAAOBgQAS"
            + "5wSOJhoVJSaEGHMPw6t3e+CbnEL9Yh5GlgxVAJCmIqhoScTMiov3QpDRHOZlZ15c"
            + "UlqugRBtORuA9xnLkrdxYNCHmX6aJTfjdIW61+o/ovP0yz6ulBkqcKzopAZLirX+"
            + "XSWf2uI9miNtxYMVnbQ1KPdEAt7Za3OQR6zcS0lGKg==").getBytes());

    /**
     * Creates a new TestHardToken object.
     *
     * @param name name
     */
    public TestHardToken(String name) {
        super(name);
        CertTools.installBCProvider();
        assertTrue("Could not create TestCA.", TestTools.createTestCA());
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * adds a token to the database
     *
     * @throws Exception error
     */

    public void test01AddHardToken() throws Exception {
        log.debug(">test01AddHardToken()");
      
        GlobalConfiguration gc = TestTools.getRaAdminSession().loadGlobalConfiguration(admin);
        orgEncryptCAId = gc.getHardTokenEncryptCA();
        gc.setHardTokenEncryptCA(0);
        TestTools.getRaAdminSession().saveGlobalConfiguration(admin, gc);
        

        SwedishEIDHardToken token = new SwedishEIDHardToken("1234", "1234", "123456", "123456", 1);

        ArrayList certs = new ArrayList();

        certs.add(CertTools.getCertfromByteArray(testcert));

        TestTools.getHardTokenSession().addHardToken(admin, "1234", "TESTUSER", "CN=TEST", SecConst.TOKEN_SWEDISHEID, token, certs, null);

        TurkishEIDHardToken token2 = new TurkishEIDHardToken("1234",  "123456", 1);

        TestTools.getHardTokenSession().addHardToken(admin, "2345", "TESTUSER", "CN=TEST", SecConst.TOKEN_TURKISHEID, token2, certs, null);

        

        log.debug("<test01AddHardToken()");
    }


    /**
     * edits token
     *
     * @throws Exception error
     */
    
    public void test02EditHardToken() throws Exception {
        log.debug(">test02EditHardToken()");

        boolean ret = false;

        HardTokenData token = TestTools.getHardTokenSession().getHardToken(admin, "1234", true);

        SwedishEIDHardToken swe = (SwedishEIDHardToken) token.getHardToken();

        assertTrue("Retrieving HardToken failed", swe.getInitialAuthEncPIN().equals("1234"));

        swe.setInitialAuthEncPIN("5678");

        TestTools.getHardTokenSession().changeHardToken(admin, "1234", SecConst.TOKEN_SWEDISHEID, token.getHardToken());
        ret = true;

        assertTrue("Editing HardToken failed", ret);


        log.debug("<test02EditHardToken()");
    }  
    


    /**
     * Test that tries to find a hardtokensn from is certificate
     *
     * @throws Exception error
     */
    
    public void test03FindHardTokenByCertificate() throws Exception {
        log.debug(">test03FindHardTokenByCertificate()");

        X509Certificate cert = CertTools.getCertfromByteArray(testcert);
        // Store the dummy cert for test.  
        if(TestTools.getCertificateStoreSession().findCertificateByFingerprint(admin, CertTools.getFingerprintAsString(cert)) == null){
        	TestTools.getCertificateStoreSession().storeCertificate(admin,cert,"DUMMYUSER", CertTools.getFingerprintAsString(cert),CertificateDataBean.CERT_ACTIVE,CertificateDataBean.CERTTYPE_ENDENTITY);
        }
        String tokensn = TestTools.getHardTokenSession().findHardTokenByCertificateSNIssuerDN(admin, cert.getSerialNumber(), cert.getIssuerDN().toString());        

        assertTrue("Couldn't find right hardtokensn", tokensn.equals("1234"));

        log.debug("<test03FindHardTokenByCertificate()");
    }
    
    /**
     * edits token
     *
     * @throws Exception error
     */
    
    public void test04EncryptHardToken() throws Exception {
        log.debug(">test04EncryptHardToken()");

        GlobalConfiguration gc = TestTools.getRaAdminSession().loadGlobalConfiguration(admin);
        gc.setHardTokenEncryptCA(TestTools.getTestCAId());
        TestTools.getRaAdminSession().saveGlobalConfiguration(admin, gc);
        boolean ret = false;

        // Make sure the old data can be read
        HardTokenData token = TestTools.getHardTokenSession().getHardToken(admin, "1234", true);

        SwedishEIDHardToken swe = (SwedishEIDHardToken) token.getHardToken();

        assertTrue("Retrieving HardToken failed : " + swe.getInitialAuthEncPIN(), swe.getInitialAuthEncPIN().equals("5678"));

        swe.setInitialAuthEncPIN("5678");

        // Store the new data as encrypted
        TestTools.getHardTokenSession().changeHardToken(admin, "1234", SecConst.TOKEN_SWEDISHEID, token.getHardToken());
        ret = true;                

        assertTrue("Saving encrypted HardToken failed", ret);

        // Make sure the encrypted data can be read
        token = TestTools.getHardTokenSession().getHardToken(admin, "1234",true);

        swe = (SwedishEIDHardToken) token.getHardToken();

        assertTrue("Retrieving encrypted HardToken failed", swe.getInitialAuthEncPIN().equals("5678"));

        log.debug("<test04EncryptHardToken()");
    }
    
    /**
     * removes all profiles
     *
     * @throws Exception error
     */
   
    public void test05removeHardTokens() throws Exception {
        log.debug(">test05removeHardTokens()");
        GlobalConfiguration gc = TestTools.getRaAdminSession().loadGlobalConfiguration(admin);
        gc.setHardTokenEncryptCA(orgEncryptCAId);
        TestTools.getRaAdminSession().saveGlobalConfiguration(admin, gc);
        boolean ret = false;
        try {
            TestTools.getHardTokenSession().removeHardToken(admin, "1234");
            TestTools.getHardTokenSession().removeHardToken(admin, "2345");

            ret = true;
        } catch (Exception pee) {
        }
        assertTrue("Removing Hard Token failed", ret);

        log.debug("<test05removeHardTokens()");
    }
   
	public void test99RemoveTestCA() throws Exception {
		TestTools.removeTestCA();
	}
}
