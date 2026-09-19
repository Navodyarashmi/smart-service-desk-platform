package com.smartdesk.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Provides password hashing components used by the identity module.
 */
@Configuration(proxyBeanMethods = false)
public class PasswordConfiguration {

    /**
     * Creates an upgrade-friendly password encoder.
     *
     * New passwords are currently encoded with BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}