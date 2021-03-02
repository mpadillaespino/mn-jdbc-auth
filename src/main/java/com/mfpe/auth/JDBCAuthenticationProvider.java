package com.mfpe.auth;

import com.mfpe.auth.persistence.UserEntity;
import com.mfpe.auth.persistence.UserRepository;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.*;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class JDBCAuthenticationProvider implements AuthenticationProvider {

    final UserRepository users;

    private static final Logger LOG = LoggerFactory.getLogger(JDBCAuthenticationProvider.class);

    public JDBCAuthenticationProvider(UserRepository users) {
        this.users = users;
    }

    @Override
    public Publisher<AuthenticationResponse> authenticate(
            @Nullable HttpRequest<?> httpRequest,
            AuthenticationRequest<?, ?> authenticationRequest) {
        return Flowable.create(emitter -> {
            final String identity = (String) authenticationRequest.getIdentity();
            LOG.info("User {} tries to login...", identity);

            Optional<UserEntity> maybeUser = users.findByEmail(identity);
            if(maybeUser.isPresent()){
                LOG.info("Found user: {}", maybeUser.get().getEmail());
                String secret = (String) authenticationRequest.getSecret();
                if(secret.equals(maybeUser.get().getPassword())){
                    //pass
                    LOG.info("User logged in");
                    HashMap<String, Object> attributes = new HashMap<>();
                    attributes.put("hair_color", "brown");
                    attributes.put("language", "en");
                    emitter.onNext(new UserDetails(
                            identity,
                            Collections.singletonList("ROLE_USER"),
                            attributes));
                    emitter.onComplete();
                    return;
                } else {
                    LOG.info("Wrong password provided for user {}", identity);
                }
            } else {
                LOG.info("No user found with email: {}", identity);
            }

            emitter.onError(new AuthenticationException(new AuthenticationFailed("Wrong username or password")));
        }, BackpressureStrategy.ERROR);
    }
}
