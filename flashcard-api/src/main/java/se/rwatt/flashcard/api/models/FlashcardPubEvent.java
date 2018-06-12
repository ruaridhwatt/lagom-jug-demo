package se.rwatt.flashcard.api.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FlashcardSetCreatedPubEvent.class, name = "flashcardSetCreated")
})
public interface FlashcardPubEvent extends Jsonable {
    String getName();
    @Value.Immutable
    @Value.Style(typeImmutable = "*")
    @JsonDeserialize(as = FlashcardSetCreatedPubEvent.class)
    interface AbstractFlashcardSetCreatedPubEvent extends FlashcardPubEvent {
        default String getName() {
            return "FlashcardSetCreatedPubEvent";
        }
        Identified<FlashcardSet> getFlashcardSet();
    }
}
