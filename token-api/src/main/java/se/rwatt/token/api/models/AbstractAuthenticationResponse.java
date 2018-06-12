package se.rwatt.token.api.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(typeImmutable = "*")
@JsonDeserialize(as = AuthenticationResponse.class)
public interface AbstractAuthenticationResponse {

    @Value.Parameter
    String getAccessToken();
}
