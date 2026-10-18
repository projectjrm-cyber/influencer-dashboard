package com.analytics.influencer_dashboard.config;

import com.analytics.influencer_dashboard.entity.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Consultation dashboard (includes payment images and uploads)
        registry.addInterceptor(new RoleInterceptor(Role.CONSULTATION))
                .addPathPatterns("/", "/dashboard", "/new-consultation",
                        "/consultation/**", "/update-status/**", "/payment-image/**");

        // E-commerce dashboard
        registry.addInterceptor(new RoleInterceptor(Role.ECOMMERCE))
                .addPathPatterns("/ecommerce/**");
    }
}