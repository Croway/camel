/* Generated by camel build tools - do NOT edit this file! */
package org.apache.camel.component.crypto;

import javax.annotation.processing.Generated;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.ExtendedPropertyConfigurerGetter;
import org.apache.camel.spi.PropertyConfigurerGetter;
import org.apache.camel.spi.ConfigurerStrategy;
import org.apache.camel.spi.GeneratedPropertyConfigurer;
import org.apache.camel.util.CaseInsensitiveMap;
import org.apache.camel.support.component.PropertyConfigurerSupport;

/**
 * Generated by camel build tools - do NOT edit this file!
 */
@Generated("org.apache.camel.maven.packaging.EndpointSchemaGeneratorMojo")
@SuppressWarnings("unchecked")
public class DigitalSignatureComponentConfigurer extends PropertyConfigurerSupport implements GeneratedPropertyConfigurer, PropertyConfigurerGetter {

    private org.apache.camel.component.crypto.DigitalSignatureConfiguration getOrCreateConfiguration(DigitalSignatureComponent target) {
        if (target.getConfiguration() == null) {
            target.setConfiguration(new org.apache.camel.component.crypto.DigitalSignatureConfiguration());
        }
        return target.getConfiguration();
    }

    @Override
    public boolean configure(CamelContext camelContext, Object obj, String name, Object value, boolean ignoreCase) {
        DigitalSignatureComponent target = (DigitalSignatureComponent) obj;
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "algorithm": getOrCreateConfiguration(target).setAlgorithm(property(camelContext, java.lang.String.class, value)); return true;
        case "alias": getOrCreateConfiguration(target).setAlias(property(camelContext, java.lang.String.class, value)); return true;
        case "autowiredenabled":
        case "autowiredEnabled": target.setAutowiredEnabled(property(camelContext, boolean.class, value)); return true;
        case "buffersize":
        case "bufferSize": getOrCreateConfiguration(target).setBufferSize(property(camelContext, java.lang.Integer.class, value)); return true;
        case "certificate": getOrCreateConfiguration(target).setCertificate(property(camelContext, java.security.cert.Certificate.class, value)); return true;
        case "certificatename":
        case "certificateName": getOrCreateConfiguration(target).setCertificateName(property(camelContext, java.lang.String.class, value)); return true;
        case "clearheaders":
        case "clearHeaders": getOrCreateConfiguration(target).setClearHeaders(property(camelContext, boolean.class, value)); return true;
        case "configuration": target.setConfiguration(property(camelContext, org.apache.camel.component.crypto.DigitalSignatureConfiguration.class, value)); return true;
        case "keystoreparameters":
        case "keyStoreParameters": getOrCreateConfiguration(target).setKeyStoreParameters(property(camelContext, org.apache.camel.support.jsse.KeyStoreParameters.class, value)); return true;
        case "keystore": getOrCreateConfiguration(target).setKeystore(property(camelContext, java.security.KeyStore.class, value)); return true;
        case "keystorename":
        case "keystoreName": getOrCreateConfiguration(target).setKeystoreName(property(camelContext, java.lang.String.class, value)); return true;
        case "lazystartproducer":
        case "lazyStartProducer": target.setLazyStartProducer(property(camelContext, boolean.class, value)); return true;
        case "password": getOrCreateConfiguration(target).setPassword(property(camelContext, java.lang.String.class, value)); return true;
        case "privatekey":
        case "privateKey": getOrCreateConfiguration(target).setPrivateKey(property(camelContext, java.security.PrivateKey.class, value)); return true;
        case "privatekeyname":
        case "privateKeyName": getOrCreateConfiguration(target).setPrivateKeyName(property(camelContext, java.lang.String.class, value)); return true;
        case "provider": getOrCreateConfiguration(target).setProvider(property(camelContext, java.lang.String.class, value)); return true;
        case "publickey":
        case "publicKey": getOrCreateConfiguration(target).setPublicKey(property(camelContext, java.security.PublicKey.class, value)); return true;
        case "publickeyname":
        case "publicKeyName": getOrCreateConfiguration(target).setPublicKeyName(property(camelContext, java.lang.String.class, value)); return true;
        case "securerandom":
        case "secureRandom": getOrCreateConfiguration(target).setSecureRandom(property(camelContext, java.security.SecureRandom.class, value)); return true;
        case "securerandomname":
        case "secureRandomName": getOrCreateConfiguration(target).setSecureRandomName(property(camelContext, java.lang.String.class, value)); return true;
        case "signatureheadername":
        case "signatureHeaderName": getOrCreateConfiguration(target).setSignatureHeaderName(property(camelContext, java.lang.String.class, value)); return true;
        default: return false;
        }
    }

    @Override
    public Class<?> getOptionType(String name, boolean ignoreCase) {
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "algorithm": return java.lang.String.class;
        case "alias": return java.lang.String.class;
        case "autowiredenabled":
        case "autowiredEnabled": return boolean.class;
        case "buffersize":
        case "bufferSize": return java.lang.Integer.class;
        case "certificate": return java.security.cert.Certificate.class;
        case "certificatename":
        case "certificateName": return java.lang.String.class;
        case "clearheaders":
        case "clearHeaders": return boolean.class;
        case "configuration": return org.apache.camel.component.crypto.DigitalSignatureConfiguration.class;
        case "keystoreparameters":
        case "keyStoreParameters": return org.apache.camel.support.jsse.KeyStoreParameters.class;
        case "keystore": return java.security.KeyStore.class;
        case "keystorename":
        case "keystoreName": return java.lang.String.class;
        case "lazystartproducer":
        case "lazyStartProducer": return boolean.class;
        case "password": return java.lang.String.class;
        case "privatekey":
        case "privateKey": return java.security.PrivateKey.class;
        case "privatekeyname":
        case "privateKeyName": return java.lang.String.class;
        case "provider": return java.lang.String.class;
        case "publickey":
        case "publicKey": return java.security.PublicKey.class;
        case "publickeyname":
        case "publicKeyName": return java.lang.String.class;
        case "securerandom":
        case "secureRandom": return java.security.SecureRandom.class;
        case "securerandomname":
        case "secureRandomName": return java.lang.String.class;
        case "signatureheadername":
        case "signatureHeaderName": return java.lang.String.class;
        default: return null;
        }
    }

    @Override
    public Object getOptionValue(Object obj, String name, boolean ignoreCase) {
        DigitalSignatureComponent target = (DigitalSignatureComponent) obj;
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "algorithm": return getOrCreateConfiguration(target).getAlgorithm();
        case "alias": return getOrCreateConfiguration(target).getAlias();
        case "autowiredenabled":
        case "autowiredEnabled": return target.isAutowiredEnabled();
        case "buffersize":
        case "bufferSize": return getOrCreateConfiguration(target).getBufferSize();
        case "certificate": return getOrCreateConfiguration(target).getCertificate();
        case "certificatename":
        case "certificateName": return getOrCreateConfiguration(target).getCertificateName();
        case "clearheaders":
        case "clearHeaders": return getOrCreateConfiguration(target).isClearHeaders();
        case "configuration": return target.getConfiguration();
        case "keystoreparameters":
        case "keyStoreParameters": return getOrCreateConfiguration(target).getKeyStoreParameters();
        case "keystore": return getOrCreateConfiguration(target).getKeystore();
        case "keystorename":
        case "keystoreName": return getOrCreateConfiguration(target).getKeystoreName();
        case "lazystartproducer":
        case "lazyStartProducer": return target.isLazyStartProducer();
        case "password": return getOrCreateConfiguration(target).getPassword();
        case "privatekey":
        case "privateKey": return getOrCreateConfiguration(target).getPrivateKey();
        case "privatekeyname":
        case "privateKeyName": return getOrCreateConfiguration(target).getPrivateKeyName();
        case "provider": return getOrCreateConfiguration(target).getProvider();
        case "publickey":
        case "publicKey": return getOrCreateConfiguration(target).getPublicKey();
        case "publickeyname":
        case "publicKeyName": return getOrCreateConfiguration(target).getPublicKeyName();
        case "securerandom":
        case "secureRandom": return getOrCreateConfiguration(target).getSecureRandom();
        case "securerandomname":
        case "secureRandomName": return getOrCreateConfiguration(target).getSecureRandomName();
        case "signatureheadername":
        case "signatureHeaderName": return getOrCreateConfiguration(target).getSignatureHeaderName();
        default: return null;
        }
    }
}

