package com.linkshorter.app.features.usersettings.service;

import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.core.security.service.UserService;
import com.linkshorter.app.features.usersettings.controller.UserSettingsController;
import com.linkshorter.app.features.usersettings.model.UserSettingsDto;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserSettingsDtoService {

    private static final Logger log = LoggerFactory.getLogger(UserSettingsController.class);

    private final UserService userService;

    public UserSettingsDtoService(UserService userService) {
        this.userService = userService;
    }

    public void updateUserDetails(UserSettingsDto userSettingsDto, User user) throws Exception {
        validateInputArguments(userSettingsDto, user);
        changeUserPasswordIfNotBlank(userSettingsDto, user);
        updateUser(userSettingsDto, user);
    }

    private void validateInputArguments(UserSettingsDto userSettingsDto, User user) throws Exception {
        Objects.requireNonNull(userSettingsDto, "User settings object cannot be null");
        Objects.requireNonNull(user, "Given user object cannot be null");
    }

    private void changeUserPasswordIfNotBlank(UserSettingsDto userSettingsDto, User user) throws Exception {
        String password = userSettingsDto.getPassword();
        if (StringUtils.isNotBlank(password)) {
            userService.changeUserPassword(user, password);
        }
    }

    private void updateUser(UserSettingsDto userSettingsDto, User user) throws Exception {
        user.setFullName(userSettingsDto.getFullname());
        user.setEmail(userSettingsDto.getEmail());
        user.setUsername(userSettingsDto.getEmail());
        user.setUserAdditionalPropertiesFromMap(userSettingsDto.getUserAdditionalProperties());
        userService.updateUser(user);
    }
}
