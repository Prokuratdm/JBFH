package com.par.jbfh;

import com.par.jbfh.config.JwtAuthenticationFilter;
import com.par.jbfh.config.SecurityConfig;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Специализированная аннотация для WebMvc-тестов контроллеров.
 * <p>
 * Исключает {@link JwtAuthenticationFilter} и {@link SecurityConfig} из контекста,
 * чтобы @WithMockUser работал корректно с @Secured аннотациями.
 * Подключает {@link TestSecurityConfig} с фильтр-чейном без JWT.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@WebMvcTest(
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
        }
)
@Import(TestSecurityConfig.class)
public @interface ControllerTest {

    @AliasFor(annotation = WebMvcTest.class, attribute = "value")
    Class<?>[] value() default {};
}