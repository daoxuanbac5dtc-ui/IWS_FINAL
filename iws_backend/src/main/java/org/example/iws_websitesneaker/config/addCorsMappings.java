package org.example.iws_websitesneaker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class addCorsMappings implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Cáº¥u hÃ¬nh CORS cho táº¥t cáº£ endpoints, khÃ´ng chá»‰ /api/**
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Cho phÃ©p origins tá»« localhost vá»›i báº¥t ká»³ port nÃ o
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));

        // Cho phÃ©p táº¥t cáº£ HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        ));

        // Cho phÃ©p táº¥t cáº£ headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Expose táº¥t cáº£ headers cáº§n thiáº¿t
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // Cho phÃ©p credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache preflight response trong 1 giá»
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Ãp dá»¥ng cho táº¥t cáº£ endpoints
        source.registerCorsConfiguration("/**", configuration);

        return new CorsFilter(source);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Cho phÃ©p origins tá»« localhost vá»›i báº¥t ká»³ port nÃ o
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));

        // Cho phÃ©p táº¥t cáº£ HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        ));

        // Cho phÃ©p táº¥t cáº£ headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Expose táº¥t cáº£ headers
        configuration.setExposedHeaders(Arrays.asList("*"));

        // Cho phÃ©p credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache preflight response trong 1 giá»
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Thay Ä‘á»•i tá»« /api/** thÃ nh /** Ä‘á»ƒ cover táº¥t cáº£ endpoints
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
