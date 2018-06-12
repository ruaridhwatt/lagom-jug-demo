package se.rwatt.flashcard.impl.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;
import se.rwatt.flashcard.api.models.FlashcardSet;
import se.rwatt.flashcard.api.models.Identified;
import se.rwatt.flashcard.api.models.Owned;

import java.util.Optional;


@Value.Immutable
@Value.Style(typeImmutable = "*")
@JsonDeserialize(as = FlashcardSetState.class)
public abstract class AbstractFlashcardSetState implements Jsonable {

    @Value.Parameter
    public abstract Optional<Owned<Identified<FlashcardSet>>> getFlashcardSet();

    @Value.Derived
    public boolean isEmpty() {
        return !getFlashcardSet().isPresent();
    }
}
