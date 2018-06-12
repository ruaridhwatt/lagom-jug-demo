package se.rwatt.flashcard.api.models;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;


@Value.Immutable
@Value.Style(typeImmutable = "*")
@JsonDeserialize(as = Identified.class)
public interface AbstractIdentified<T> {

    @Value.Parameter
    String getId();

    @Value.Parameter
    @JsonUnwrapped
    T getChild();
}
