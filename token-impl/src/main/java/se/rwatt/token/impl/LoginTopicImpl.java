package se.rwatt.token.impl;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.pubsub.PubSubRef;
import com.lightbend.lagom.javadsl.pubsub.PubSubRegistry;
import com.lightbend.lagom.javadsl.pubsub.TopicId;
import se.rwatt.token.api.LoginEvent;

import javax.inject.Inject;

public class LoginTopicImpl implements LoginTopic {

    private PubSubRegistry pubSub;

    @Inject
    public LoginTopicImpl(PubSubRegistry pubSub) {
        this.pubSub = pubSub;
    }

    @Override
    public void publish(LoginEvent event) {
        ref().publish(event);
    }

    @Override
    public Source<LoginEvent, NotUsed> subscribe() {
        return ref().subscriber();
    }

    private PubSubRef<LoginEvent> ref() {
        return pubSub.refFor(TopicId.of(LoginEvent.class, "LoginEvent"));
    }
}
