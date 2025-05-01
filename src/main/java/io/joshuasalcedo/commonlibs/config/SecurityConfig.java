package io.joshuasalcedo.commonlibs.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration that permits all requests.
 * Only activated in web applications.
 */
@Configuration
@ConditionalOnWebApplication
public class SecurityConfig {


    /**
     * Configures a SecurityFilterChain that permits all requests.
     * This is an alternative approach to disabling security.
     *
     * @param http HttpSecurity to configure
     * @return SecurityFilterChain that permits all requests
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Disable CSRF protection
        http.csrf(AbstractHttpConfigurer::disable)
                // Permit all requests without authentication
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll()
                );

        return http.build();
    }
}