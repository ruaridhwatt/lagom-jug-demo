package se.rwatt.event.streamer.impl;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import se.rwatt.event.streamer.api.Event;
import se.rwatt.event.streamer.api.EventStreamerService;
import se.rwatt.flashcard.api.FlashcardService;

import javax.inject.Inject;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class EventStreamerServiceImpl implements EventStreamerService {

    private FlashcardService flashcardService;

    @Inject
    public EventStreamerServiceImpl(FlashcardService flashcardService) {

        this.flashcardService = flashcardService;
    }

    @Override
    public ServiceCall<NotUsed, Source<Event, ?>> streamEvents() {
        return request -> completedFuture(flashcardService
                .flashcardEvents()
                .subscribe()
                .atMostOnceSource()
                .map(event -> Event.builder().event(event).build())
        );
    }
}
