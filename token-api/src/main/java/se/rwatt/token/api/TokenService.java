package se.rwatt.token.api;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.nimbusds.jose.jwk.JWKSet;
import se.rwatt.token.api.models.AuthenticationResponse;
import se.rwatt.token.api.models.UnverifiedUserIdentity;

import static com.lightbend.lagom.javadsl.api.Service.*;


public interface TokenService extends Service {

    String ROOT = "/token";

    String LOGIN_PATH = ROOT + "/login";
    ServiceCall<UnverifiedUserIdentity, AuthenticationResponse> login();
    ServiceCall<NotUsed, Source<LoginEvent, NotUsed>> streamLoginEvents();

    String PUBLIC_KEY_SET_PATH = ROOT + "/jwks";
    ServiceCall<NotUsed, JWKSet> getPublicKeySet();

    @Override
    default Descriptor descriptor() {
        return named("token")
                .withCalls(
                        pathCall(LOGIN_PATH, this::login),
                        pathCall(LOGIN_PATH, this::streamLoginEvents),
                        pathCall(PUBLIC_KEY_SET_PATH, this::getPublicKeySet)
                )
                .withAutoAcl(true);
    }
}
