package com.linkshorter.app.core.startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.TimeZone;

@Component
public class StartupInformationApplicationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupInformationApplicationRunner.class);

    @Value("${spring.profiles.active}")
    private String profile;

    @Value("${server.port}")
    private String port;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        TimeZone warsawTimeZone = TimeZone.getTimeZone("Europe/Warsaw");
        TimeZone.setDefault(warsawTimeZone);
        log.info("Application was started at: {}", new Date());
        log.info("Application profile: {}, port: {}", profile, port);
    }
}
