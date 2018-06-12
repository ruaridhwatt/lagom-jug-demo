package se.rwatt.flashcard.impl.models;

import akka.NotUsed;
import akka.japi.Pair;
import com.lightbend.lagom.javadsl.api.transport.ResponseHeader;

public class NoContentResponse extends Pair<ResponseHeader, NotUsed> {

    private NoContentResponse() {
        super(ResponseHeader.NO_CONTENT, NotUsed.getInstance());
    }

    public static NoContentResponse instance() {
        return new NoContentResponse();
    }
}
