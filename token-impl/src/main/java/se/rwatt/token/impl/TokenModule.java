package se.rwatt.token.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import se.rwatt.token.api.TokenService;

public class TokenModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindService(TokenService.class, TokenServiceImpl.class);
        bind(LoginTopic.class).to(LoginTopicImpl.class);
    }
}
