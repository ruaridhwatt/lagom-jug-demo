package se.rwatt.token.api.models;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(typeImmutable = "*")
@JsonDeserialize(as = VerifiedUserIdentity.class)
public interface AbstractVerifiedUserIdentity {

    @Value.Parameter
    @JsonUnwrapped
    Username getUsername();

    @Value.Parameter
    @JsonUnwrapped
    UserIdentifier getUserId();
}
