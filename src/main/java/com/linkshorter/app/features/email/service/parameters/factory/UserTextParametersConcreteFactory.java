package com.linkshorter.app.features.email.service.parameters.factory;

import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.core.security.model.UserPasswordResetToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class UserTextParametersConcreteFactory implements TextParametersAbstractFactory {

    @Override
    public Map<String, String> createTextParametersMap(Object object) {
        Map<String, String> parameters = new HashMap<>();
        Objects.requireNonNull(object, "Given object cannot be null");
        User user = (User) object;
        buildTextParameters(parameters, user);
        return parameters;
    }

    private void buildTextParameters(Map<String, String> parameters, User user) {
        parameters.put("@user.fullname@", user.getFullName());
        parameters.put("@user.email@", user.getEmail());
        parameters.put("@user.id@", user.getId().toString());

        UserPasswordResetToken userPasswordResetToken = user.getUserPasswordResetToken();
        if (userPasswordResetToken != null) {
            parameters.put("@user.user-password-reset-token@", userPasswordResetToken.getId().toString());
        }
    }
}
