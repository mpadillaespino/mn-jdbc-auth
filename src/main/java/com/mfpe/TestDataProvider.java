package com.mfpe;

import com.mfpe.auth.persistence.UserEntity;
import com.mfpe.auth.persistence.UserRepository;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;

import javax.inject.Singleton;

@Singleton
public class TestDataProvider {

    private final UserRepository users;

    public TestDataProvider(UserRepository users) {
        this.users = users;
    }

    @EventListener
    public void init(StartupEvent event) {
        if(users.findByEmail("alice@example.com").isEmpty()){
            UserEntity user = new UserEntity();
            user.setEmail("alice@example.com");
            user.setPassword("secret");
            users.save(user);
        }
    }

}
