package se.rwatt.event.streamer.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import se.rwatt.event.streamer.api.EventStreamerService;
import se.rwatt.flashcard.api.FlashcardService;

public class EventStreamerModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindService(EventStreamerService.class, EventStreamerServiceImpl.class);
        bindClient(FlashcardService.class);
    }
}
