package io.github.eduardosantiag0.kaizan_companion.infra.security.config;

import io.github.eduardosantiag0.kaizan_companion.infra.auth.GoogleAuthSuccesHandler;
import io.github.eduardosantiag0.kaizan_companion.infra.security.RateLimitingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final GoogleAuthSuccesHandler onAuthenticationSuccess;
    private final RateLimitingFilter rateLimitingFilter;

    public SecurityConfig(GoogleAuthSuccesHandler onAuthenticationSuccess, RateLimitingFilter rateLimitingFilter) {
        this.onAuthenticationSuccess = onAuthenticationSuccess;
        this.rateLimitingFilter = rateLimitingFilter;
    }

//    https://medium.com/@deepakrajs1103/oauth2-login-with-spring-boot-google-github-integration-complete-guide-66f1116f7326

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for simplicity (re-enable for production browser apps)
                .csrf(csrf -> csrf.disable())
                // Run rate limiting before authentication processing
                .addFilterBefore(rateLimitingFilter, OAuth2LoginAuthenticationFilter.class)
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

    @Bean
    public FilterRegistrationBean<RateLimitingFilter> disableAutoRegistration() {
        FilterRegistrationBean<RateLimitingFilter> registration = new FilterRegistrationBean<>(rateLimitingFilter);
        registration.setEnabled(false);
        return registration;
    }

}
