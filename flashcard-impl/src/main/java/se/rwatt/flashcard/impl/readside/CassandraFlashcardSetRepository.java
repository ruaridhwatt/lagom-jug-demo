package se.rwatt.flashcard.impl.readside;

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import org.pcollections.PSequence;
import se.rwatt.flashcard.api.models.FlashcardSet;
import se.rwatt.flashcard.api.models.Identified;
import se.rwatt.flashcard.api.models.Owned;
import se.rwatt.flashcard.impl.models.*;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide.completedStatement;

public class CassandraFlashcardSetRepository implements FlashcardSetRepository {

    private final CassandraSession db;
    private static final String SELECT_ALL_FLASHCARD_SETS =
            "SELECT * FROM flashcardSets";

    @Inject
    CassandraFlashcardSetRepository(CassandraSession db, ReadSide readSide) {
        this.db = db;
        readSide.register(FlashcardEventReadSideProcessor.class);
    }

    @Override
    public CompletionStage<List<Owned<Identified<FlashcardSet>>>> getAllFlashcardSets() {
        return db.selectAll(SELECT_ALL_FLASHCARD_SETS)
                .thenApply(
                        rows -> rows
                                .stream()
                                .map(this::mapFlashcardSet)
                                .collect(Collectors.toList())
                );

    }

    @Override
    public Source<Owned<Identified<FlashcardSet>>, NotUsed> streamAllFlashcardSets() {
        return db.select(SELECT_ALL_FLASHCARD_SETS).map(this::mapFlashcardSet);
    }

    private Owned<Identified<FlashcardSet>> mapFlashcardSet(Row row) {
        FlashcardSet set = FlashcardSet.builder()
                .name(row.getString("name"))
                .build();
        Identified<FlashcardSet> identified = Identified.<FlashcardSet>builder()
                .id(row.getString("flashcardSetId"))
                .child(set)
                .build();
        return Owned.<Identified<FlashcardSet>>builder()
                .ownerId(row.getString("ownerId"))
                .child(identified)
                .build();
    }

    private static class FlashcardEventReadSideProcessor extends ReadSideProcessor<FlashcardSetEvent> {
        private final CassandraSession db;
        private final CassandraReadSide readSide;

        private PreparedStatement insertFlashcardSet;
        private PreparedStatement updateFlashcardSet;
        private PreparedStatement deleteFlashcardSet;

        @Inject
        private FlashcardEventReadSideProcessor(CassandraSession db, CassandraReadSide readSide) {
            this.db = db;
            this.readSide = readSide;
        }

        @Override
        public ReadSideHandler<FlashcardSetEvent> buildHandler() {
            return readSide.<FlashcardSetEvent>builder("FlashcardSetEventReadSideProcessor")
                    .setGlobalPrepare(this::createTable)
                    .setPrepare(tag -> prepareStatements())
                    .setEventHandler(FlashcardSetCreatedEvent.class, this::insertFlashcardSet)
                    .setEventHandler(FlashcardSetUpdatedEvent.class, this::updateFlashcardSet)
                    .setEventHandler(FlashcardSetDeletedEvent.class, this::deleteFlashcardSet)
                    .build();
        }

        @Override
        public PSequence<AggregateEventTag<FlashcardSetEvent>> aggregateTags() {
            return FlashcardSetEvent.TAG.allTags();
        }

        private CompletionStage<Done> createTable() {
            return db.executeCreateTable(
                    "CREATE TABLE IF NOT EXISTS flashcardSets "
                            + "(flashcardSetId text PRIMARY KEY, ownerId text, name text)");
        }

        private CompletionStage<Done> prepareStatements() {

            return db.prepare("INSERT INTO flashcardSets (flashcardSetId, ownerId, name) VALUES (?, ?, ?)")
                    .thenApply(preparedStatement -> {
                        insertFlashcardSet = preparedStatement;
                        return Done.getInstance();
                    })
                    .thenCompose(done -> db.prepare("UPDATE flashcardSets SET name=? WHERE flashcardSetId=?"))
                    .thenApply(preparedStatement -> {
                        updateFlashcardSet = preparedStatement;
                        return Done.getInstance();
                    })
                    .thenCompose(done -> db.prepare("DELETE FROM flashcardSets WHERE flashcardSetId=?"))
                    .thenApply(preparedStatement -> {
                        deleteFlashcardSet = preparedStatement;
                        return Done.getInstance();
                    });
        }

        private CompletionStage<List<BoundStatement>> insertFlashcardSet(FlashcardSetCreatedEvent event) {
            Owned<Identified<FlashcardSet>> packedSet = event.getFlashcardSet();
            FlashcardSet flashcardSet = unpack(packedSet);
            return completedStatement(
                    insertFlashcardSet.bind(packedSet.getChild().getId(), packedSet.getOwnerId(), flashcardSet.getName())
            );
        }

        private CompletionStage<List<BoundStatement>> updateFlashcardSet(FlashcardSetUpdatedEvent event) {
            Identified<FlashcardSet> flashcardSet = event.getFlashcardSet();
            return completedStatement(
                    updateFlashcardSet.bind(flashcardSet.getChild().getName(), flashcardSet.getId())
            );
        }

        private CompletionStage<List<BoundStatement>> deleteFlashcardSet(FlashcardSetDeletedEvent event) {
            return completedStatement(
                    deleteFlashcardSet.bind(event.getSetId())
            );
        }

        private FlashcardSet unpack(Owned<Identified<FlashcardSet>> owned) {
            return owned.getChild().getChild();
        }
    }
}
