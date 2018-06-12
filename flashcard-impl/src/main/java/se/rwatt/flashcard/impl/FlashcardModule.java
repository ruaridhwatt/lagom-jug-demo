package se.rwatt.flashcard.impl;

import com.auth0.jwt.interfaces.RSAKeyProvider;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import se.rwatt.flashcard.api.FlashcardService;
import se.rwatt.flashcard.impl.readside.CassandraFlashcardSetRepository;
import se.rwatt.flashcard.impl.readside.FlashcardSetRepository;
import se.rwatt.flashcard.impl.security.AccessTokenIssuer;
import se.rwatt.flashcard.impl.security.TokenServiceKeyProvider;

public class FlashcardModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindService(FlashcardService.class, FlashcardServiceImpl.class);
        bind(AccessTokenIssuer.class).to(TokenServiceKeyProvider.class);
        bind(RSAKeyProvider.class).to(TokenServiceKeyProvider.class);
        bind(FlashcardSetRepository.class).to(CassandraFlashcardSetRepository.class);
    }
}
