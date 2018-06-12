package se.rwatt.token.impl;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import se.rwatt.token.api.LoginEvent;

interface LoginTopic {
    void publish(LoginEvent event);
    Source<LoginEvent, NotUsed> subscribe();
}
