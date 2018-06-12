package se.rwatt.flashcard.impl.models;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;
import se.rwatt.flashcard.api.models.Flashcard;
import se.rwatt.flashcard.api.models.FlashcardSet;
import se.rwatt.flashcard.api.models.Identified;
import se.rwatt.flashcard.api.models.Owned;

public interface FlashcardSetEvent extends Jsonable, AggregateEvent<FlashcardSetEvent> {
    int NUM_SHARDS = 5;

    AggregateEventShards<FlashcardSetEvent> TAG =
            AggregateEventTag.sharded(FlashcardSetEvent.class, NUM_SHARDS);

    @Override
    default AggregateEventShards<FlashcardSetEvent> aggregateTag() {
        return TAG;
    }

    @Value.Immutable
    @Value.Style(typeImmutable = "*")
    @JsonDeserialize(as = FlashcardSetCreatedEvent.class)
    interface AbstractFlashcardSetCreatedEvent extends FlashcardSetEvent {
        Owned<Identified<FlashcardSet>> getFlashcardSet();
    }

    @Value.Immutable
    @Value.Style(typeImmutable = "*")
    @JsonDeserialize(as = FlashcardAddedEvent.class)
    abstract class AbstractFlashcardAddedEvent implements FlashcardSetEvent {
        @Value.Default
        public String getId() {
            return UUIDs.timeBased().toString();
        }
        public abstract Flashcard getFlashcard();

    }

    @Value.Immutable
    @Value.Style(typeImmutable = "*")
    @JsonDeserialize(as = FlashcardUpdatedEvent.class)
    interface AbstractFlashcardUpdatedEvent extends FlashcardSetEvent {
        Identified<Flashcard> getFlashcard();
    }

    @Value.Immutable
    @Value.Style(typeImmutable = "*")
    @JsonDeserialize(as = FlashcardSetUpdatedEvent.class)
    interface AbstractFlashcardSetUpdatedEvent extends FlashcardSetEvent {
        Identified<FlashcardSet> getFlashcardSet();
    }

    @Value.Immutable
    @Value.Style(typeImmutable = "*")
    @JsonDeserialize(as = FlashcardSetDeletedEvent.class)
    interface AbstractFlashcardSetDeletedEvent extends FlashcardSetEvent {
        String getSetId();
    }

    @Value.Immutable
    @Value.Style(typeImmutable = "*")
    @JsonDeserialize(as = FlashcardDeletedEvent.class)
    interface AbstractFlashcardDeletedEvent extends FlashcardSetEvent {
        String getFlashcardId();
    }
}
