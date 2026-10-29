package io.github.eduardosantiag0.kaizan_companion.infra.security;

import io.github.eduardosantiag0.kaizan_companion.infra.auth.GoogleAuthSuccesHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final GoogleAuthSuccesHandler onAuthenticationSuccess;

    public SecurityConfig(GoogleAuthSuccesHandler onAuthenticationSuccess) {
        this.onAuthenticationSuccess = onAuthenticationSuccess;
    }

//    https://medium.com/@deepakrajs1103/oauth2-login-with-spring-boot-google-github-integration-complete-guide-66f1116f7326

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for simplicity (re-enable for production browser apps)
                .csrf(csrf -> csrf.disable())
                // All requests require authentication
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                // Use OAuth2 login instead of form-based login
                // After successful login, redirect user to /hello
                .oauth2Login(oauth -> oauth
                        .successHandler(onAuthenticationSuccess)
                );
        return http.build();
    }


}
