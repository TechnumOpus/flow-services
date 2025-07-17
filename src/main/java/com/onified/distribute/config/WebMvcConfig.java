package com.onified.distribute.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Only handle static resources from specific paths
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");

        // Ensure API paths are not treated as static resources
        registry.addResourceHandler("/api/**")
                .addResourceLocations("classpath:/nonexistent/");
    }
}