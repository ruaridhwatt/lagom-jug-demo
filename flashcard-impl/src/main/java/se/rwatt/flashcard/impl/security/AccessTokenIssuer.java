package se.rwatt.flashcard.impl.security;

import com.nimbusds.jose.jwk.JWK;

import java.io.IOException;
import java.text.ParseException;
import java.util.Optional;

public interface AccessTokenIssuer {
    String getIssuerId();
    Optional<JWK> getPublicKeyWith(String keyId) throws IOException, ParseException;
}
