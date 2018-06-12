package se.rwatt.flashcard.impl.models;

import akka.Done;
import com.lightbend.lagom.javadsl.api.transport.Forbidden;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import org.pcollections.PVector;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.rwatt.flashcard.api.models.Flashcard;
import se.rwatt.flashcard.api.models.FlashcardSet;
import se.rwatt.flashcard.api.models.Identified;
import se.rwatt.flashcard.api.models.Owned;
import se.rwatt.flashcard.impl.FlashcardSetCreatedTopic;

import javax.inject.Inject;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;


public class FlashcardSetEntity
        extends PersistentEntity
        <FlashcardSetCommand, FlashcardSetEvent, FlashcardSetState> {

    private static final Logger LOG = LoggerFactory.getLogger(FlashcardSetEntity.class);

    private static final Collector<Identified<Flashcard>, ?, PVector<Identified<Flashcard>>>
            TREE_P_VECTOR_COLLECTOR = Collectors
            .collectingAndThen(Collectors.toList(), TreePVector::from);

    private final FlashcardSetCreatedTopic topic;

    @Inject
    public FlashcardSetEntity(FlashcardSetCreatedTopic topic) {
        this.topic = topic;
    }

    @Override
    public Behavior initialBehavior(Optional<FlashcardSetState> snapshotState) {
        BehaviorBuilder b = newBehaviorBuilder(snapshotState.orElse(FlashcardSetState.builder().build()));

        b.setCommandHandler(CreateFlashcardSetCommand.class,
                this::handleCreateFlashcardSetCommand);
        b.setEventHandler(FlashcardSetCreatedEvent.class,
                this::handleFlashcardSetCreatedEvent);

        b.setReadOnlyCommandHandler(GetFlashcardSetCommand.class, this::handleGetFlashcardSetCommand);

        b.setCommandHandler(UpdateFlashcardSetCommand.class, this::handleUpdateFlashcardSetCommand);
        b.setEventHandler(FlashcardSetUpdatedEvent.class, this::handleFlashcardSetUpdatedEvent);

        b.setCommandHandler(AddFlashcardCommand.class, this::handleAddFlashcardCommand);
        b.setEventHandler(FlashcardAddedEvent.class, this::handleFlashcardAddedEvent);

        b.setCommandHandler(UpdateFlashcardCommand.class, this::handleUpdateFlashcardCommand);
        b.setEventHandler(FlashcardUpdatedEvent.class, this::handleFlashcardUpdatedEvent);

        b.setCommandHandler(DeleteFlashcardSetCommand.class, this::handleDeleteFlashcardSetCommand);
        b.setEventHandler(FlashcardSetDeletedEvent.class, this::handleFlashcardSetDeletedEvent);

        b.setCommandHandler(DeleteFlashcardCommand.class, this::handleDeleteFlashcardCommand);
        b.setEventHandler(FlashcardDeletedEvent.class, this::handleFlashcardDeletedEvent);

        return b.build();
    }

    /**
     * Create Set command handler
     *
     * @param cmd Command
     * @param ctx Context
     * @return Persist event propagation
     */
    private Persist handleCreateFlashcardSetCommand(CreateFlashcardSetCommand cmd, CommandContext<Identified<FlashcardSet>> ctx) {

        if (!state().isEmpty()) {
            ctx.commandFailed(new Forbidden("Flashcard set already exists"));
            return ctx.done();
        }

        FlashcardSet flashcardSet = cmd.getFlashcardSet();

        FlashcardSetCreatedEvent setCreatedEvent = FlashcardSetCreatedEvent.builder()
                .flashcardSet(pack(flashcardSet, entityId(), cmd.getCommander()))
                .build();

        Identified<FlashcardSet> response = pack(flashcardSet, entityId());

        return ctx.thenPersist(setCreatedEvent, event -> {
            ctx.reply(response);
            topic.publish(event);
        });
    }

    /**
     * Get Set command handler
     *
     * @param cmd Command
     * @param ctx Context
     */
    private void handleGetFlashcardSetCommand(GetFlashcardSetCommand cmd, ReadOnlyCommandContext<Identified<FlashcardSet>> ctx) {
        try {
            Identified<FlashcardSet> flashcardSet = verifyOwnership(cmd.getCommander());
            ctx.reply(flashcardSet);
        } catch (NotFound | Forbidden e) {
            ctx.commandFailed(e);
        }
    }

    /**
     * Update Set command handler
     *
     * @param cmd Command
     * @param ctx Context
     * @return Persist event propagation
     */
    private Persist handleUpdateFlashcardSetCommand(UpdateFlashcardSetCommand cmd, CommandContext<Identified<FlashcardSet>> ctx) {

        try {
            verifyOwnership(cmd.getCommander());
        } catch (NotFound | Forbidden e) {
            ctx.commandFailed(e);
            return ctx.done();
        }

        Identified<FlashcardSet> flashcardSet = pack(cmd.getFlashcardSet(), entityId());

        FlashcardSetUpdatedEvent setUpdatedEvent = FlashcardSetUpdatedEvent.builder()
                .flashcardSet(flashcardSet)
                .build();

        return ctx.thenPersist(setUpdatedEvent, event -> ctx.reply(flashcardSet));
    }

    /**
     * Add Card command handler
     *
     * @param cmd Command
     * @param ctx Context
     * @return Persist event propagation
     */
    private Persist handleAddFlashcardCommand(AddFlashcardCommand cmd, CommandContext<Identified<Flashcard>> ctx) {

        try {
            verifyOwnership(cmd.getCommander());
        } catch (NotFound | Forbidden e) {
            ctx.commandFailed(e);
            return ctx.done();
        }

        FlashcardAddedEvent flashcardAddedEvent = FlashcardAddedEvent.builder()
                .flashcard(cmd.getFlashcard())
                .build();

        Identified<Flashcard> response = pack(cmd.getFlashcard(), flashcardAddedEvent.getId());

        return ctx.thenPersist(flashcardAddedEvent, event -> ctx.reply(response));
    }

    /**
     * Update Card command handler
     *
     * @param cmd Command
     * @param ctx Context
     * @return Persist event propagation
     */
    private Persist handleUpdateFlashcardCommand(UpdateFlashcardCommand cmd, CommandContext<Identified<Flashcard>> ctx) {

        try {
            verifyOwnership(cmd.getCommander());
        } catch (NotFound | Forbidden e) {
            ctx.commandFailed(e);
            return ctx.done();
        }

        Identified<Flashcard> packedUpdate = pack(cmd.getFlashcard(), cmd.getFlashcardId());

        FlashcardUpdatedEvent flashcardUpdatedEvent = FlashcardUpdatedEvent.builder()
                .flashcard(packedUpdate)
                .build();

        return ctx.thenPersist(flashcardUpdatedEvent, event -> ctx.reply(packedUpdate));
    }


    /**
     * Delete Set command handler
     *
     * @param cmd Command
     * @param ctx Context
     * @return Persist event propagation
     */
    private Persist handleDeleteFlashcardSetCommand(DeleteFlashcardSetCommand cmd, CommandContext<Done> ctx) {

        try {
            verifyOwnership(cmd.getCommander());
        } catch (NotFound | Forbidden e) {
            ctx.commandFailed(e);
            return ctx.done();
        }

        FlashcardSetDeletedEvent flashcardSetDeletedEvent = FlashcardSetDeletedEvent.builder()
                .setId(entityId())
                .build();

        return ctx.thenPersist(flashcardSetDeletedEvent, event -> ctx.reply(Done.getInstance()));
    }

    /**
     * Delete Card command handler
     *
     * @param cmd Command
     * @param ctx Context
     * @return Persist event propagation
     */
    private Persist handleDeleteFlashcardCommand(DeleteFlashcardCommand cmd, CommandContext<Done> ctx) {

        try {
            verifyOwnership(cmd.getCommander());
        } catch (NotFound | Forbidden e) {
            ctx.commandFailed(e);
            return ctx.done();
        }

        FlashcardDeletedEvent flashcardDeletedEvent = FlashcardDeletedEvent.builder()
                .flashcardId(cmd.getFlashcardId())
                .build();

        return ctx.thenPersist(flashcardDeletedEvent, event -> ctx.reply(Done.getInstance()));
    }

    //--- Event Handlers --//

    private FlashcardSetState handleFlashcardSetCreatedEvent(FlashcardSetCreatedEvent event) {
        return state().withFlashcardSet(event.getFlashcardSet());
    }

    private FlashcardSetState handleFlashcardDeletedEvent(FlashcardDeletedEvent event) {

        Owned<Identified<FlashcardSet>> oldState = state().getFlashcardSet().get();
        FlashcardSet oldSet = unpack(oldState);

        String removeId = event.getFlashcardId();

        PVector<Identified<Flashcard>> oldFlashcards = oldSet.getFlashcards();

        PVector<Identified<Flashcard>> updatedFlashcards = oldFlashcards.stream()
                .filter(card -> !card.getId().equals(removeId))
                .collect(TREE_P_VECTOR_COLLECTOR);

        FlashcardSet updatedSet = FlashcardSet.builder()
                .from(oldSet)
                .flashcards(updatedFlashcards)
                .build();

        return state().withFlashcardSet(pack(updatedSet, entityId(), oldState.getOwnerId()));
    }

    private FlashcardSetState handleFlashcardSetDeletedEvent(FlashcardSetDeletedEvent event) {
        return FlashcardSetState.builder().build();
    }

    private FlashcardSetState handleFlashcardSetUpdatedEvent(FlashcardSetUpdatedEvent event) {

        Owned<Identified<FlashcardSet>> oldState = state().getFlashcardSet().get();
        FlashcardSet oldSet = unpack(oldState);

        FlashcardSet updatedSet = FlashcardSet.copyOf(oldSet)
                .withName(event.getFlashcardSet().getChild().getName());

        return state().withFlashcardSet(pack(updatedSet, entityId(), oldState.getOwnerId()));
    }


    private FlashcardSetState handleFlashcardUpdatedEvent(FlashcardUpdatedEvent event) {

        Owned<Identified<FlashcardSet>> oldState = state().getFlashcardSet().get();
        FlashcardSet oldSet = unpack(oldState);

        Identified<Flashcard> updatedFlashcard = event.getFlashcard();

        PVector<Identified<Flashcard>> oldFlashcards = oldSet.getFlashcards();

        PVector<Identified<Flashcard>> updatedFlashcards = oldFlashcards.stream()
                .map(card -> (card.getId().equals(updatedFlashcard.getId()) ? updatedFlashcard : card))
                .collect(TREE_P_VECTOR_COLLECTOR);

        FlashcardSet updatedSet = FlashcardSet.copyOf(oldSet).withFlashcards(updatedFlashcards);

        return state().withFlashcardSet(pack(updatedSet, entityId(), oldState.getOwnerId()));
    }


    private FlashcardSetState handleFlashcardAddedEvent(FlashcardAddedEvent event) {

        Owned<Identified<FlashcardSet>> oldState = state().getFlashcardSet().get();

        FlashcardSet oldSet = unpack(oldState);

        PVector<Identified<Flashcard>> oldFlashcards = oldSet.getFlashcards();

        Flashcard newFlashcard = event.getFlashcard();

        PVector<Identified<Flashcard>> updatedFlashcards = oldFlashcards.plus(pack(newFlashcard, event.getId()));

        FlashcardSet updatedSet = FlashcardSet.copyOf(oldSet)
                .withFlashcards(updatedFlashcards);

        return state().withFlashcardSet(pack(updatedSet, entityId(), oldState.getOwnerId()));
    }

    private Identified<FlashcardSet> verifyOwnership(String commanderId) throws NotFound, Forbidden {
        return state()
                .getFlashcardSet()
                .map(flashcardSet -> verifyOwnership(flashcardSet, commanderId))
                .orElseThrow(() -> new NotFound("Flashcard set not found"));
    }

    private Identified<FlashcardSet> verifyOwnership(Owned<Identified<FlashcardSet>> flashcardSet, String commanderId) {
        if (flashcardSet.getOwnerId().equals(commanderId)) {
            return flashcardSet.getChild();
        }
        throw new Forbidden("You are not the owner of this Flashcard set!");
    }

    private FlashcardSet unpack(Owned<Identified<FlashcardSet>> owned) {
        return owned.getChild().getChild();
    }

    private Owned<Identified<FlashcardSet>> pack(FlashcardSet set, String id, String ownerId) {
        return Owned.<Identified<FlashcardSet>>builder()
                .ownerId(ownerId)
                .child(Identified.<FlashcardSet>builder()
                        .id(id)
                        .child(set)
                        .build())
                .build();
    }

    private Identified<FlashcardSet> pack(FlashcardSet set, String id) {
        return Identified.<FlashcardSet>builder()
                .id(id)
                .child(set)
                .build();
    }

    private Identified<Flashcard> pack(Flashcard flashcard, String id) {
        return Identified.<Flashcard>builder()
                .id(id)
                .child(flashcard)
                .build();
    }
}
