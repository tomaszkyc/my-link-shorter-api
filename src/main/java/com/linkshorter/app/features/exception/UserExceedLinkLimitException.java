package com.linkshorter.app.features.exception;

import com.linkshorter.app.core.security.model.User;

public class UserExceedLinkLimitException extends Exception {

    private static final String EXCEPTION_MESSAGE_FORMAT = "Given user %s exceeded link limit for his account: %s";

    public UserExceedLinkLimitException(String s) {
        super(s);
    }

    public UserExceedLinkLimitException(User user, Long userLinkLimit) {
        super(String.format(EXCEPTION_MESSAGE_FORMAT, user.getUsername(), userLinkLimit));
    }
}
