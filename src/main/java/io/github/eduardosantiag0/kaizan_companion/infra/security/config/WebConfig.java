package io.github.eduardosantiag0.kaizan_companion.infra.security.config;

import io.github.eduardosantiag0.kaizan_companion.infra.security.TelegramTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private TelegramTokenInterceptor secretTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(secretTokenInterceptor)
                .addPathPatterns("/kaizan/api/v1");
    }
}
