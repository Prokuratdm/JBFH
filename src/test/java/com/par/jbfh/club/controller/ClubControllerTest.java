package com.par.jbfh.club.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class ClubControllerTest {

    @Test
    void getLogoShouldNotHaveSecuredAnnotation() throws NoSuchMethodException {
        Method getLogoMethod = ClubController.class.getMethod("getLogo", java.util.UUID.class);

        Secured securedAnnotation = getLogoMethod.getAnnotation(Secured.class);
        assertThat(securedAnnotation).isNull();
    }

    @Test
    void getLogoShouldHaveGetMappingAnnotation() throws NoSuchMethodException {
        Method getLogoMethod = ClubController.class.getMethod("getLogo", java.util.UUID.class);

        GetMapping getMapping = getLogoMethod.getAnnotation(GetMapping.class);
        assertThat(getMapping).isNotNull();
        assertThat(getMapping.value()).containsExactly("/{id}/logo");
    }
}