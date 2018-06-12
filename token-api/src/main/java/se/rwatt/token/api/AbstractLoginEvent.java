package se.rwatt.token.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import se.rwatt.token.api.models.Username;

import java.time.ZonedDateTime;

@Value.Immutable
@Value.Style(typeImmutable = "*")
@JsonDeserialize(as = LoginEvent.class)
public interface AbstractLoginEvent {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    default ZonedDateTime getTimeStamp() {
        return ZonedDateTime.now();
    }

    @JsonUnwrapped
    Username getUsername();
}
