package se.rwatt.flashcard.api.models;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;


@Value.Immutable
@Value.Style(typeImmutable = "*")
@JsonDeserialize(as = Owned.class)
public interface AbstractOwned<T> {

    @Value.Parameter
    String getOwnerId();

    @Value.Parameter
    @JsonUnwrapped
    T getChild();
}
