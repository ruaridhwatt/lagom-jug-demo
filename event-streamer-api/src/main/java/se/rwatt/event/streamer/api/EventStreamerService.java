package se.rwatt.event.streamer.api;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import static com.lightbend.lagom.javadsl.api.Service.*;
import static com.lightbend.lagom.javadsl.api.Service.named;

public interface EventStreamerService extends Service {

    ServiceCall<NotUsed, Source<Event, ?>> streamEvents();

    default Descriptor descriptor() {
        return named("event-streamer")
                .withCalls(
                        pathCall("/eventstream", this::streamEvents)
                )
                .withAutoAcl(true);
    }


}
