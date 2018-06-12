package se.rwatt.flashcard.impl.security;

import com.auth0.jwt.interfaces.RSAKeyProvider;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;

import java.io.IOException;
import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Optional;

public class TokenServiceKeyProvider extends ForeignAccessTokenIssuer implements RSAKeyProvider {

    public static final String ISSUER_ID = "auth.rwatt.se";
    private static final String KEY_SET_URL = "http://localhost:9000/token/jwks";

    public TokenServiceKeyProvider() throws IOException, ParseException {
        super(ISSUER_ID, new URL(KEY_SET_URL));
    }

    @Override
    public RSAPublicKey getPublicKeyById(String kid) {
        try {
            Optional<JWK> maybeJwk = this.getPublicKeyWith(kid);
            if (maybeJwk.isPresent()) {
                JWK jwk = maybeJwk.get();
                if (jwk.getAlgorithm().equals(JWSAlgorithm.RS256) && !jwk.isPrivate()) {
                    return ((RSAKey) jwk).toRSAPublicKey();
                }
            }
            return null;
        } catch (JOSEException | IOException | ParseException e) {
            return null;
        }
    }

    @Override
    public RSAPrivateKey getPrivateKey() {
        return null;
    }

    @Override
    public String getPrivateKeyId() {
        return null;
    }
}
