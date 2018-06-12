package se.rwatt.token.impl;

import akka.Done;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Singleton
public class PublicKeyRepository {

    private final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS publicKeys (kid text PRIMARY KEY, jwk text)";

    private final String INSERT_PUBLIC_KEY = "INSERT INTO publicKeys (kid, jwk) VALUES (?, ?)";
    private PreparedStatement preparedInsert;

    private final String SELECT_PUBLIC_KEYS = "SELECT * FROM publicKeys";

    private final CassandraSession uninitialisedSession;

    private volatile CompletableFuture<CassandraSession> initialisedSession;

    @Inject
    public PublicKeyRepository(CassandraSession uninitialisedSession) {
        this.uninitialisedSession = uninitialisedSession;
        session();
    }

    private CompletionStage<CassandraSession> session() {
        if (initialisedSession == null || initialisedSession.isCompletedExceptionally()) {
            initialisedSession = uninitialisedSession
                    .executeCreateTable(CREATE_TABLE)
                    .thenCompose(done -> uninitialisedSession.prepare(INSERT_PUBLIC_KEY))
                    .thenApply(preparedStatement -> {
                        this.preparedInsert = preparedStatement;
                        return Done.getInstance();
                    })
                    .thenApply(done -> uninitialisedSession)
                    .toCompletableFuture();
        }
        return initialisedSession;
    }

    public CompletionStage<Done> addPublicKey(JWK publicKey) {
        return session().thenCompose(sess -> {
            BoundStatement boundInsert = preparedInsert.bind(publicKey.getKeyID(), publicKey.toJSONString());
            return sess.executeWrite(boundInsert);
        });
    }

    public CompletionStage<JWKSet> getPublicKeys() {
        return session().thenCompose(sess -> sess.selectAll(SELECT_PUBLIC_KEYS))
                .thenApply(this::mapToJwkSet);
    }

    private JWKSet mapToJwkSet(List<Row> rows) {
        return rows
                .stream()
                .map(this::mapToJwk)
                .collect(Collectors.collectingAndThen(Collectors.toList(), JWKSet::new));
    }

    private JWK mapToJwk(Row row) {
        try {
            String jwkJson = row.getString(1);
            return JWK.parse(jwkJson);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
