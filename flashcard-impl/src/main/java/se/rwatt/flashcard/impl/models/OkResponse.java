package se.rwatt.flashcard.impl.models;

import akka.japi.Pair;
import com.lightbend.lagom.javadsl.api.transport.ResponseHeader;

public class OkResponse<T> extends Pair<ResponseHeader, T> {

    private OkResponse(T resource) {
        super(ResponseHeader.OK, resource);
    }

    public static <R> OkResponse<R> of(R resource) {
        return new OkResponse<>(resource);
    }
}
