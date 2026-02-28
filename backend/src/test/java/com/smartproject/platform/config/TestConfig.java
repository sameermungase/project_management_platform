package com.smartproject.platform.config;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;

@Configuration
@Profile("test")
@AutoConfigureWebMvc
@TestExecutionListeners(WithSecurityContextTestExecutionListener.class)
public class TestConfig {

    @Bean
    @Primary
    public TestExecutionListener testExecutionListener() {
        return new WithSecurityContextTestExecutionListener();
    }
}
