package se.rwatt.token.impl;

import com.auth0.jwt.interfaces.RSAKeyProvider;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class RSAKeyProviderImpl implements RSAKeyProvider {

    private static final int RSA_KEY_LENGTH = 2048;
    private final String keyId;
    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;
    private final JWK publicJwk;

    public RSAKeyProviderImpl() throws NoSuchAlgorithmException, JOSEException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(RSA_KEY_LENGTH);

        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        publicKey = (RSAPublicKey) keyPair.getPublic();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .keyIDFromThumbprint()
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .build();
        keyId = rsaKey.getKeyID();

        publicJwk = rsaKey.toPublicJWK();
    }

    @Override
    public RSAPublicKey getPublicKeyById(String keyId) {
        return keyId.equals(this.keyId) ? publicKey : null;
    }

    @Override
    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    @Override
    public String getPrivateKeyId() {
        return keyId;
    }

    public JWK getPublicJwk() {
        return publicJwk;
    }
}
