package org.cesecore.authentication.oauth;

import org.apache.commons.lang.StringUtils;

/**
 * Utility class for OAuth Provider related operations
 * Currently only used for the CLI
 *
 */
public class OAuthKeyHelper {
        
    public static void validateProvider(final OAuthKeyInfo provider) {
        validateCommonType(provider);
        if (OAuthKeyInfo.OAuthProviderType.TYPE_AZURE.getIndex() == provider.getTypeInt()) {
            validateAzureType(provider);
        } else if (OAuthKeyInfo.OAuthProviderType.TYPE_KEYCLOAK.getIndex() == provider.getTypeInt()) {
            validateKeycloakType(provider);
        } else if (OAuthKeyInfo.OAuthProviderType.TYPE_NONE.getIndex() != provider.getTypeInt()) {
            throw new MissingOAuthKeyAttributeException("The Provider Type field is mandatory for all Trusted OAuth Providers.");
        }
    }
    
    public static void validateAzureType(final OAuthKeyInfo provider) {
        if (StringUtils.isEmpty(provider.getUrl())) {
            throw new MissingOAuthKeyAttributeException("The URL field is mandatory for Trusted OAuth Providers.");
        }
        if (StringUtils.isEmpty(provider.getClient())) {
            throw new MissingOAuthKeyAttributeException("The Tenant field (use --realm) is mandatory for Azure Trusted OAuth Providers.");
        }
        if (StringUtils.isEmpty(provider.getClientSecretAndDecrypt())) {
            throw new MissingOAuthKeyAttributeException("The Client Secret field is mandatory for Azure Trusted OAuth Providers.");
        }
    }
    
    public static void validateKeycloakType(final OAuthKeyInfo provider) {
        if (StringUtils.isEmpty(provider.getUrl())) {
            throw new MissingOAuthKeyAttributeException("The URL field is mandatory for Trusted OAuth Providers.");
        }
        if (StringUtils.isEmpty(provider.getClient())) {
            throw new MissingOAuthKeyAttributeException("The Client Name field is mandatory for Keycloak Trusted OAuth Providers.");
        }
        if (StringUtils.isEmpty(provider.getRealm())) {
            throw new MissingOAuthKeyAttributeException("The Realm field is mandatory for Keycloak Trusted OAuth Providers.");
        }
    }
    
    private static void validateCommonType(final OAuthKeyInfo provider) {
        if (StringUtils.isEmpty(provider.getLabel())) {
            throw new MissingOAuthKeyAttributeException("The Label field is mandatory for all Trusted OAuth Providers.");
        }
    }
}
