package org.up.reactorj.model;

import org.up.coroutines.model.User;
import org.up.reactorj.controller.UserJController;

public class UserBuilder {
    private User newUser;

    private UserBuilder(User user) {
        this.newUser = user;
    }

    public static UserBuilder from(User user) {
        User newUser = new User(user.getId(), user.getUserName(), user.getEmail(), user.getEmailVerified(), user.getAvatarUrl());
        return new UserBuilder(newUser);
    }

    public UserBuilder withAvatarUrl(String url) {
        newUser = new User(newUser.getId(), newUser.getUserName(), newUser.getEmail(), newUser.getEmailVerified(), url);
        return this;
    }

    public User build() {
        return newUser;
    }
}
