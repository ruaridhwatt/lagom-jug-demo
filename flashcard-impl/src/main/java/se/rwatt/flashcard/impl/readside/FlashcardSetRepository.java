package se.rwatt.flashcard.impl.readside;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import se.rwatt.flashcard.api.models.FlashcardSet;
import se.rwatt.flashcard.api.models.Identified;
import se.rwatt.flashcard.api.models.Owned;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface FlashcardSetRepository {
    CompletionStage<List<Owned<Identified<FlashcardSet>>>> getAllFlashcardSets();

    Source<Owned<Identified<FlashcardSet>>, NotUsed> streamAllFlashcardSets();
}
