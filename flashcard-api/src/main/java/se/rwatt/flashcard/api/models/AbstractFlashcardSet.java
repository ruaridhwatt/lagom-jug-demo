package se.rwatt.flashcard.api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

@Value.Immutable
@Value.Style(typeImmutable = "*")
@JsonDeserialize(as = FlashcardSet.class)
public interface AbstractFlashcardSet {

    @Value.Parameter
    String getName();

    @Value.Default
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    default PVector<Identified<Flashcard>> getFlashcards() {
        return TreePVector.empty();
    }
}
