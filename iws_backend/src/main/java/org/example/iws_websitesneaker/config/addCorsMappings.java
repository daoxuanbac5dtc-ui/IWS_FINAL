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
        // Cấu hình CORS cho tất cả endpoints, không chỉ /api/**
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

        // Cho phép origins từ localhost với bất kỳ port nào
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));

        // Cho phép tất cả HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        ));

        // Cho phép tất cả headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Expose tất cả headers cần thiết
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // Cho phép credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache preflight response trong 1 giờ
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Áp dụng cho tất cả endpoints
        source.registerCorsConfiguration("/**", configuration);

        return new CorsFilter(source);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Cho phép origins từ localhost với bất kỳ port nào
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));

        // Cho phép tất cả HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        ));

        // Cho phép tất cả headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Expose tất cả headers
        configuration.setExposedHeaders(Arrays.asList("*"));

        // Cho phép credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache preflight response trong 1 giờ
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Thay đổi từ /api/** thành /** để cover tất cả endpoints
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
