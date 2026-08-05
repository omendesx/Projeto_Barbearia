package com.barbearia.sistema.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthInterceptor auth;
    public WebConfig(AuthInterceptor auth){this.auth=auth;}
    @Override public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(auth).addPathPatterns("/paineladmin.html","/api/users/**","/api/clients/**","/api/appointments/**","/api/services/**","/api/professionals/**")
                .excludePathPatterns("/api/portal/**");
    }
}
