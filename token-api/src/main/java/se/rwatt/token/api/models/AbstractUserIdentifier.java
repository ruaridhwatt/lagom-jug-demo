package se.rwatt.token.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(typeImmutable = "*")
@JsonDeserialize(as = UserIdentifier.class)
public abstract class AbstractUserIdentifier {

    public static final int MINIMUM_LENGTH = 3;
    public static final int MAXIMUM_LENGTH = 32;

    public static final String REGEX = String.format(
            "[\\p{IsAlphabetic}\\p{Digit}-_]{%d,%d}",
            MINIMUM_LENGTH,
            MAXIMUM_LENGTH
    );

    @Value.Parameter
    @JsonProperty("userId")
    public abstract String value();

    @Value.Check
    void check() {
        Preconditions.checkArgument(
                value().matches(REGEX),
                "Bad user AbstractIdentified: %s\n"
                        + "\tA user ID must be %s-%s alphanumeric, _ or - characters",
                value(),
                MINIMUM_LENGTH,
                MAXIMUM_LENGTH
        );
    }
}
