package se.rwatt.event.streamer.api;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import com.lightbend.lagom.serialization.Jsonable;


@Value.Immutable
@Value.Style(typeImmutable = "*")
@JsonDeserialize(as = Event.class)
public interface AbstractEvent {
    @JsonUnwrapped
    Jsonable getEvent();
}
