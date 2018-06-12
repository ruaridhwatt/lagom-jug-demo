package se.rwatt.token.impl;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Forbidden;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.rwatt.token.api.LoginEvent;
import se.rwatt.token.api.TokenService;
import se.rwatt.token.api.models.*;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.Date;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class TokenServiceImpl implements TokenService {
    private static final Logger log = LoggerFactory.getLogger("Token Service");

    private static final String ISSUER = "auth.rwatt.se";
    private static final String AUDIENCE = "api.rwatt.se";
    private static final int EXPIRY_HOURS = 24;

    private final PublicKeyRepository publicKeyRepository;
    private final Algorithm signingAlgorithm;
    private LoginTopic loginTopic;

    // TODO [ruri] change to a verifier based on an identity provider
    private final JWTVerifier verifier = JWT.require(Algorithm.none()).build();

    @Inject
    public TokenServiceImpl(
            RSAKeyProviderImpl keyProvider,
            PublicKeyRepository publicKeyRepository,
            LoginTopic loginTopic
    ) {
        this.publicKeyRepository = publicKeyRepository;

        signingAlgorithm = Algorithm.RSA256(keyProvider);
        this.loginTopic = loginTopic;
        JWK publicJwk = keyProvider.getPublicJwk();

        publicKeyRepository.addPublicKey(publicJwk)
                .thenAccept(done -> log.info("Public key added"));
    }

    @Override
    public ServiceCall<UnverifiedUserIdentity, AuthenticationResponse> login() {
        return unverifiedUserIdentity -> completedFuture(unverifiedUserIdentity)
                .thenApplyAsync(this::verifyUserIdentity)
                .thenApplyAsync(this::publishLoginEvent)
                .thenApplyAsync(this::generateAuthenticationResponse);
    }

    @Override
    public ServiceCall<NotUsed, Source<LoginEvent, NotUsed>> streamLoginEvents() {
        return request -> completedFuture(loginTopic.subscribe());
    }

    @Override
    public ServiceCall<NotUsed, JWKSet> getPublicKeySet() {
        return req -> publicKeyRepository.getPublicKeys();
    }

    private VerifiedUserIdentity publishLoginEvent(VerifiedUserIdentity verifiedUserIdentity) {
        LoginEvent loginEvent = LoginEvent.builder()
                .username(verifiedUserIdentity.getUsername())
                .build();
        loginTopic.publish(loginEvent);
        return verifiedUserIdentity;
    }

    private AuthenticationResponse generateAuthenticationResponse(VerifiedUserIdentity verifiedUserIdentity) {
        Date expiry = Date.from(ZonedDateTime.now().plusHours(EXPIRY_HOURS).toInstant());
        String serializedAccessToken = JWT.create()
                .withIssuer(ISSUER)
                .withAudience(AUDIENCE)
                .withExpiresAt(expiry)
                .withSubject(verifiedUserIdentity.getUserId().value())
                .withClaim("username", verifiedUserIdentity.getUsername().value())
                .sign(signingAlgorithm);
        return AuthenticationResponse.builder()
                .accessToken(serializedAccessToken)
                .build();
    }

    private VerifiedUserIdentity verifyUserIdentity(UnverifiedUserIdentity unverifiedUserIdentity) {
        try {
            String serializedIdToken = unverifiedUserIdentity.getIdToken();
            DecodedJWT jwt = verifier.verify(serializedIdToken);
            UserIdentifier userId = UserIdentifier.of(jwt.getSubject());
            Username username = Username.of(jwt.getClaim("username").asString());
            return VerifiedUserIdentity.builder()
                    .userId(userId)
                    .username(username)
                    .build();
        } catch (JWTVerificationException e) {
            throw new Forbidden(e);
        }
    }
}
