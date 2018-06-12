package se.rwatt.flashcard.impl.models;

import akka.Done;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;
import se.rwatt.flashcard.api.models.Flashcard;
import se.rwatt.flashcard.api.models.FlashcardSet;
import se.rwatt.flashcard.api.models.Identified;

public interface FlashcardSetCommand extends Jsonable {
    String getCommander();

    @Value.Immutable
    @Value.Style(typeImmutable = "*")
    @JsonDeserialize(as = CreateFlashcardSetCommand.class)
    interface AbstractCreateFlashcardSetCommand extends FlashcardSetCommand, PersistentEntity.ReplyType<Identified<FlashcardSet>> {
        FlashcardSet getFlashcardSet();
    }

    @Value.Immutable
    @Value.Style(typeImmutable = "*")
    @JsonDeserialize(as = GetFlashcardSetCommand.class)
    interface AbstractGetFlashcardSetCommand extends FlashcardSetCommand, PersistentEntity.ReplyType<Identified<FlashcardSet>> {
    }

    @Value.Immutable
    @Value.Style(typeImmutable = "*")
    @JsonDeserialize(as = AddFlashcardCommand.class)
    interface AbstractAddFlashcardCommand extends FlashcardSetCommand, PersistentEntity.ReplyType<Identified<Flashcard>> {
        Flashcard getFlashcard();
    }

    @Value.Immutable
    @Value.Style(typeImmutable = "*")
    @JsonDeserialize(as = UpdateFlashcardSetCommand.class)
    interface AbstractUpdateFlashcardSetCommand extends FlashcardSetCommand, PersistentEntity.ReplyType<Identified<FlashcardSet>> {
        FlashcardSet getFlashcardSet();
    }

    @Value.Immutable
    @Value.Style(typeImmutable = "*")
    @JsonDeserialize(as = DeleteFlashcardSetCommand.class)
    interface AbstractDeleteFlashcardSetCommand extends FlashcardSetCommand, PersistentEntity.ReplyType<Done> {
    }

    @Value.Immutable
    @Value.Style(typeImmutable = "*")
    @JsonDeserialize(as = DeleteFlashcardCommand.class)
    interface AbstractDeleteFlashcardCommand extends FlashcardSetCommand, PersistentEntity.ReplyType<Done> {
        String getFlashcardId();
    }

    @Value.Immutable
    @Value.Style(typeImmutable = "*")
    @JsonDeserialize(as = UpdateFlashcardCommand.class)
    interface AbstractUpdateFlashcardCommand extends FlashcardSetCommand, PersistentEntity.ReplyType<Identified<Flashcard>> {
        String getFlashcardId();
        Flashcard getFlashcard();
    }
}
