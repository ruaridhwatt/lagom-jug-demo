package se.rwatt.flashcard.impl.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Optional;

public class ForeignAccessTokenIssuer implements AccessTokenIssuer {

    private final String issuerId;
    private final URL keySetUrl;
    private JWKSet keySet;

    public ForeignAccessTokenIssuer(@NotNull String issuerId, @NotNull URL keySetUrl) throws IOException, ParseException {
        this.issuerId = issuerId;
        this.keySetUrl = keySetUrl;
    }

    @Override
    public String getIssuerId() {
        return issuerId;
    }

    @Override
    public Optional<JWK> getPublicKeyWith(String keyId) throws IOException, ParseException {
        if (keySet == null || keySet.getKeyByKeyId(keyId) == null) {
            refreshKeySet();
        }
        return Optional.ofNullable(keySet.getKeyByKeyId(keyId));
    }

    private void refreshKeySet() throws IOException, ParseException {
        this.keySet = JWKSet.load(keySetUrl);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        ForeignAccessTokenIssuer that = (ForeignAccessTokenIssuer) o;

        return issuerId.equals(that.issuerId);
    }

    @Override
    public String toString() {
        return issuerId;
    }
}
