package com.linkshorter.app.core.swagger.configuration;

import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@EnableSwagger2
@Configuration
public class SwaggerConfiguration {

    @Bean
    public Docket get() {
        ApiInfo apiInfo = createApiInfo();
        return new Docket(DocumentationType.SWAGGER_2)
                .ignoredParameterTypes(AuthenticationPrincipal.class)
                .select()
                .paths(PathSelectors.ant("/api/**"))
                .apis(Predicates.or(
                        RequestHandlerSelectors.basePackage("com.linkshorter.app.core.security.controller"),
                        RequestHandlerSelectors.basePackage("com.linkshorter.app.features.links.controller")
                ))
                .build().apiInfo(apiInfo);
    }

    private ApiInfo createApiInfo() {
        Contact contact = new Contact("Administrator of LinkShorter service", "http://mylinkshorter.com", "mylinkshorternoreply@gmail.com");
        return new ApiInfo("LinkShorter API",
                "LinkShorter API for services integration",
                "1.0",
                "http://mylinkshorter.com",
                contact,
                "MIT License",
                "https://pl.wikipedia.org/wiki/Licencja_MIT",
                Collections.emptyList()
        );
    }
}
