package se.rwatt.flashcard.impl;

import akka.Done;
import akka.NotUsed;
import akka.japi.Pair;
import akka.stream.javadsl.Source;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import com.datastax.driver.core.utils.UUIDs;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.transport.Forbidden;
import com.lightbend.lagom.javadsl.api.transport.RequestHeader;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.server.HeaderServiceCall;
import se.rwatt.flashcard.api.FlashcardService;
import se.rwatt.flashcard.api.models.*;
import se.rwatt.flashcard.impl.models.*;
import se.rwatt.flashcard.impl.readside.FlashcardSetRepository;
import se.rwatt.flashcard.impl.security.TokenServiceKeyProvider;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class FlashcardServiceImpl implements FlashcardService {

    private static final String REQUIRED_ACCESS_TOKEN_AUDIENCE = "api.rwatt.se";
    private static final String EXPECTED_TOKEN_TYPE = "Bearer";

    private final JWTVerifier jwtVerifier;
    private final FlashcardSetCreatedTopic topic;
    private PersistentEntityRegistry persistentEntities;
    private FlashcardSetRepository flashcardSetRepository;

    @Inject
    public FlashcardServiceImpl(
            RSAKeyProvider keyProvider,
            PersistentEntityRegistry persistentEntities,
            FlashcardSetRepository flashcardSetRepository,
            FlashcardSetCreatedTopic topic
    ) {
        Algorithm algorithm = Algorithm.RSA256(keyProvider);
        jwtVerifier = JWT.require(algorithm)
                .withIssuer(TokenServiceKeyProvider.ISSUER_ID)
                .withAudience(REQUIRED_ACCESS_TOKEN_AUDIENCE)
                .build();
        this.persistentEntities = persistentEntities;
        persistentEntities.register(FlashcardSetEntity.class);
        this.flashcardSetRepository = flashcardSetRepository;
        this.topic = topic;
    }

    @Override
    public HeaderServiceCall<FlashcardSet, Identified<FlashcardSet>> createSet() {
        return (requestHeader, flashcardSet) ->
                getVerifiedAccessToken(requestHeader)
                        .thenCompose(accessToken -> persist(flashcardSet, accessToken))
                        .thenApply(CreatedResponse::of);
    }

    @Override
    public ServiceCall<NotUsed, List<Owned<Identified<FlashcardSet>>>> getAllSets() {
        return request -> flashcardSetRepository.getAllFlashcardSets();
    }

    @Override
    public ServiceCall<NotUsed, Source<Owned<Identified<FlashcardSet>>, NotUsed>> streamSets() {
        return request -> {
            Source<Owned<Identified<FlashcardSet>>, NotUsed> source = flashcardSetRepository.streamAllFlashcardSets();
            Source<Owned<Identified<FlashcardSet>>, NotUsed> subscriber = topic.subscriber().map(FlashcardSetCreatedEvent::getFlashcardSet);
            return CompletableFuture.supplyAsync(() -> source.concat(subscriber));
        };
    }

    @Override
    public HeaderServiceCall<NotUsed, Identified<FlashcardSet>> getSet(String setId) {
        return (requestHeader, flashcardSet) ->
                getVerifiedAccessToken(requestHeader)
                        .thenCompose(accessToken -> get(setId, accessToken))
                        .thenApply(OkResponse::of);
    }

    @Override
    public HeaderServiceCall<FlashcardSet, Identified<FlashcardSet>> updateSet(String setId) {
        return (requestHeader, flashcardSet) ->
                getVerifiedAccessToken(requestHeader)
                        .thenCompose(accessToken -> update(flashcardSet, setId, accessToken))
                        .thenApply(OkResponse::of);
    }

    @Override
    public HeaderServiceCall<NotUsed, NotUsed> deleteSet(String setId) {
        return (requestHeader, request) ->
                getVerifiedAccessToken(requestHeader)
                        .thenCompose(accessToken -> deleteFlashcardSet(setId, accessToken))
                        .thenApply(done -> NoContentResponse.instance());
    }

    @Override
    public HeaderServiceCall<Flashcard, Identified<Flashcard>> addFlashcard(String setId) {
        return (requestHeader, flashcard) ->
                getVerifiedAccessToken(requestHeader)
                        .thenCompose(accessToken -> persist(flashcard, setId, accessToken))
                        .thenApply(CreatedResponse::of);
    }

    @Override
    public HeaderServiceCall<Flashcard, Identified<Flashcard>> updateFlashcard(String setId, String flashcardId) {
        return (requestHeader, flashcard) ->
                getVerifiedAccessToken(requestHeader)
                        .thenCompose(accessToken -> update(setId, flashcardId, flashcard, accessToken))
                        .thenApply(OkResponse::of);
    }

    @Override
    public HeaderServiceCall<NotUsed, NotUsed> deleteFlashcard(String setId, String flashcardId) {
        return (requestHeader, request) ->
                getVerifiedAccessToken(requestHeader)
                        .thenCompose(accessToken -> deleteFlashcard(setId, flashcardId, accessToken))
                        .thenApply(done -> NoContentResponse.instance());
    }

    @Override
    public Topic<FlashcardPubEvent> flashcardEvents() {
        return TopicProducer.taggedStreamWithOffset(
                FlashcardSetEvent.TAG.allTags(),
                this::getEventStream
        );
    }

    private Source<Pair<FlashcardPubEvent, Offset>, ?> getEventStream(
            AggregateEventTag<FlashcardSetEvent> tag,
            Offset offset
    ) {
        return persistentEntities.eventStream(tag, offset)
                .filter(eventAndOffset ->
                        eventAndOffset.first() instanceof FlashcardSetCreatedEvent)
                .map(this::toPubEventAndOffset);
    }

    private Pair<FlashcardPubEvent, Offset> toPubEventAndOffset(
            Pair<FlashcardSetEvent, Offset> eventAndOffset) {

        FlashcardSetCreatedEvent persistedEvent = (FlashcardSetCreatedEvent) eventAndOffset.first();

        Owned<Identified<FlashcardSet>> flashcardSet = persistedEvent.getFlashcardSet();
        FlashcardSetCreatedPubEvent eventToPublish = FlashcardSetCreatedPubEvent.builder()
                .flashcardSet(flashcardSet.getChild())
                .build();
        return Pair.create(eventToPublish, eventAndOffset.second());
    }

    private CompletionStage<Identified<FlashcardSet>> get(String setId, DecodedJWT accessToken) {
        GetFlashcardSetCommand getFlashcardSetCommand = GetFlashcardSetCommand.builder()
                .commander(accessToken.getSubject())
                .build();
        return persistentEntities.refFor(FlashcardSetEntity.class, setId)
                .ask(getFlashcardSetCommand);
    }

    private CompletionStage<Identified<FlashcardSet>> update(FlashcardSet flashcardSet, String setId, DecodedJWT accessToken) {
        UpdateFlashcardSetCommand updateFlashcardSetCommand = UpdateFlashcardSetCommand.builder()
                .flashcardSet(flashcardSet)
                .commander(accessToken.getSubject())
                .build();
        return persistentEntities.refFor(FlashcardSetEntity.class, setId)
                .ask(updateFlashcardSetCommand);
    }

    private CompletionStage<Identified<Flashcard>> persist(Flashcard flashcard, String setId, DecodedJWT accessToken) {
        AddFlashcardCommand addFlashcardCommand = AddFlashcardCommand.builder()
                .flashcard(flashcard)
                .commander(accessToken.getSubject())
                .build();
        return persistentEntities.refFor(FlashcardSetEntity.class, setId)
                .ask(addFlashcardCommand);
    }

    private CompletionStage<Identified<Flashcard>> update(String setId, String flashcardId, Flashcard flashcard, DecodedJWT accessToken) {
        UpdateFlashcardCommand updateFlashcardCommand = UpdateFlashcardCommand.builder()
                .commander(accessToken.getSubject())
                .flashcardId(flashcardId)
                .flashcard(flashcard)
                .build();
        return persistentEntities.refFor(FlashcardSetEntity.class, setId)
                .ask(updateFlashcardCommand);
    }

    private CompletionStage<Done> deleteFlashcard(String setId, String flashcardId, DecodedJWT accessToken) {
        DeleteFlashcardCommand deleteFlashcardCommand = DeleteFlashcardCommand.builder()
                .commander(accessToken.getSubject())
                .flashcardId(flashcardId)
                .build();
        return persistentEntities.refFor(FlashcardSetEntity.class, setId)
                .ask(deleteFlashcardCommand);
    }

    private CompletionStage<Done> deleteFlashcardSet(String setId, DecodedJWT accessToken) {
        DeleteFlashcardSetCommand deleteFlashcardSetCommand = DeleteFlashcardSetCommand.builder()
                .commander(accessToken.getSubject())
                .build();
        return persistentEntities.refFor(FlashcardSetEntity.class, setId)
                .ask(deleteFlashcardSetCommand);
    }

    private CompletionStage<Identified<FlashcardSet>> persist(
            FlashcardSet flashcardSet,
            DecodedJWT accessToken) {

        String setId = UUIDs.timeBased().toString();

        CreateFlashcardSetCommand createFlashcardSetCommand = CreateFlashcardSetCommand.builder()
                .flashcardSet(flashcardSet)
                .commander(accessToken.getSubject())
                .build();

        return persistentEntities.refFor(FlashcardSetEntity.class, setId)
                .ask(createFlashcardSetCommand);
    }

    private CompletionStage<DecodedJWT> getVerifiedAccessToken(RequestHeader requestHeader) {
        return CompletableFuture
                .supplyAsync(() -> requestHeader
                        .getHeader("Authorization")
                        .map(this::parseAuthorizationHeader)
                        .orElseThrow(() -> new Forbidden("Missing Authorization Header")))
                .thenApply(this::verifyAccessToken);
    }

    private String parseAuthorizationHeader(String authzHeader) {
        String expectedStart = EXPECTED_TOKEN_TYPE + " ";
        if (!authzHeader.startsWith(expectedStart)) {
            throw new Forbidden("Bad token type, expected: " + EXPECTED_TOKEN_TYPE);
        }
        return authzHeader.substring(expectedStart.length());
    }

    private DecodedJWT verifyAccessToken(String unverifiedAccessToken) {
        try {
            return jwtVerifier.verify(unverifiedAccessToken);
        } catch (JWTVerificationException e) {
            throw new Forbidden(e);
        }
    }
}
