package se.rwatt.token.impl;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.withServer;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import se.rwatt.token.api.TokenService;
import se.rwatt.token.api.models.AuthenticationResponse;
import se.rwatt.token.api.models.UnverifiedUserIdentity;

public class TokenServiceTest {

  @Test
  public void shouldReturnAnAccessToken() {
    withServer(defaultSetup().withCassandra(), server -> {
      TokenService service = server.client(TokenService.class);

        UnverifiedUserIdentity unverifiedUserIdentity = UnverifiedUserIdentity.builder().idToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJub25lIn0." +
                "eyJ1c2VySWQiOiIxMjM0IiwidXNlcm5hbWUiOiJSdWFyaWRoIFdhdHQifQ.").build();
        AuthenticationResponse authenticationResponse = service.login().invoke(unverifiedUserIdentity).toCompletableFuture().get(5, SECONDS);
        assertTrue(authenticationResponse.getAccessToken().length() > 0);
    });
  }

}
