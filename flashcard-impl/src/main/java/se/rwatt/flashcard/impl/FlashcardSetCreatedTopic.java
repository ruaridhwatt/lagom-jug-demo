package se.rwatt.flashcard.impl;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.pubsub.PubSubRef;
import com.lightbend.lagom.javadsl.pubsub.PubSubRegistry;
import com.lightbend.lagom.javadsl.pubsub.TopicId;
import se.rwatt.flashcard.impl.models.FlashcardSetCreatedEvent;

import javax.inject.Inject;

public class FlashcardSetCreatedTopic {

    private final PubSubRegistry pubSub;

    @Inject
    public FlashcardSetCreatedTopic(PubSubRegistry pubSub) {
        this.pubSub = pubSub;
    }

    public void publish(FlashcardSetCreatedEvent flashcardSet) {
        getRef().publish(flashcardSet);
    }

    public Source<FlashcardSetCreatedEvent, NotUsed> subscriber() {
        return getRef().subscriber();
    }

    private PubSubRef<FlashcardSetCreatedEvent> getRef() {
        return pubSub.refFor(TopicId.of(FlashcardSetCreatedEvent.class, "flashcardSetCreated"));
    }
}
