package se.rwatt.flashcard.impl.models;

import akka.japi.Pair;
import com.lightbend.lagom.javadsl.api.transport.MessageProtocol;
import com.lightbend.lagom.javadsl.api.transport.ResponseHeader;
import org.pcollections.HashTreePMap;

import java.net.HttpURLConnection;

public class CreatedResponse<T> extends Pair<ResponseHeader, T> {

    private static final ResponseHeader createdHeader = new ResponseHeader(
            HttpURLConnection.HTTP_CREATED,
            new MessageProtocol(),
            HashTreePMap.empty());

    private CreatedResponse(T createdResource) {
        super(createdHeader, createdResource);
    }

    public static <R> CreatedResponse<R> of(R resource) {
        return new CreatedResponse<>(resource);
    }
}
