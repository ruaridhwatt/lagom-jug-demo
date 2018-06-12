package se.rwatt.flashcard.api;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceAcl;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.broker.kafka.KafkaProperties;
import com.lightbend.lagom.javadsl.api.transport.Method;
import se.rwatt.flashcard.api.models.*;

import java.util.List;

import static com.lightbend.lagom.javadsl.api.Service.*;

public interface FlashcardService extends Service {
    String ROOT = "/flashcards";

    String FLASHCARD_SETS_ENDPOINT = ROOT + "/sets";
    ServiceCall<FlashcardSet, Identified<FlashcardSet>> createSet();
    ServiceCall<NotUsed, List<Owned<Identified<FlashcardSet>>>> getAllSets();

    String STREAM_FLASHCARD_SETS_ENDPOINT = ROOT + "/stream/sets";
    ServiceCall<NotUsed, Source<Owned<Identified<FlashcardSet>>, NotUsed>> streamSets();

    String FLASHCARD_SET_ENDPOINT = ROOT + "/sets/:setId";
    ServiceCall<NotUsed, Identified<FlashcardSet>> getSet(String setId);
    ServiceCall<FlashcardSet, Identified<FlashcardSet>> updateSet(String setId);
    ServiceCall<Flashcard, Identified<Flashcard>> addFlashcard(String setId);
    ServiceCall<NotUsed, NotUsed> deleteSet(String setId);

    String FLASHCARD_ENDPOINT = ROOT + "/sets/:setId/:flashcardId";
    ServiceCall<Flashcard, Identified<Flashcard>> updateFlashcard(String setId, String flashcardId);
    ServiceCall<NotUsed, NotUsed> deleteFlashcard(String setId, String flashcardId);

    Topic<FlashcardPubEvent> flashcardEvents();

    default Descriptor descriptor() {
        return named("flashcard")
                .withCalls(
                        restCall(Method.POST, FLASHCARD_SETS_ENDPOINT, this::createSet),
                        restCall(Method.GET, FLASHCARD_SETS_ENDPOINT, this::getAllSets),
                        pathCall(STREAM_FLASHCARD_SETS_ENDPOINT, this::streamSets),

                        restCall(Method.GET, FLASHCARD_SET_ENDPOINT, this::getSet),
                        restCall(Method.PATCH, FLASHCARD_SET_ENDPOINT, this::updateSet),
                        restCall(Method.POST, FLASHCARD_SET_ENDPOINT, this::addFlashcard),
                        restCall(Method.DELETE, FLASHCARD_SET_ENDPOINT, this::deleteSet),

                        restCall(Method.PATCH, FLASHCARD_ENDPOINT, this::updateFlashcard),
                        restCall(Method.DELETE, FLASHCARD_ENDPOINT, this::deleteFlashcard)
                )
                .withTopics(
                        topic("FlashcardEvents", this::flashcardEvents)
                                .withProperty(KafkaProperties.partitionKeyStrategy(), FlashcardPubEvent::getName)
                )
                .withAutoAcl(true).withServiceAcls(ServiceAcl.methodAndPath(Method.OPTIONS, ROOT + "/.*"));
    }
}
