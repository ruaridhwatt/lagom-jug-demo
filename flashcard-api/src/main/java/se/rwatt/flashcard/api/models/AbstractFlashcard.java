package se.rwatt.flashcard.api.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(typeImmutable = "*")
@JsonDeserialize(as = Flashcard.class)
public interface AbstractFlashcard {

    @Value.Parameter
    String getFront();

    @Value.Parameter
    String getBack();
}
