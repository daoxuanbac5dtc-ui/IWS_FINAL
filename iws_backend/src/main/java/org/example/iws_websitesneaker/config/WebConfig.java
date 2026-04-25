package org.example.iws_websitesneaker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ÄÆ°á»ng dáº«n uploads vá»›i absolute path
        String uploadPath = Paths.get(uploadDir).toAbsolutePath().toUri().toString();

        // Cáº¥u hÃ¬nh serve static files tá»« thÆ° má»¥c uploads (Ä‘Ã£ cÃ³)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/", uploadPath)
                .setCachePeriod(3600); // Cache 1 giá»

        // âœ… THÃŠM: Cáº¥u hÃ¬nh riÃªng cho return images
        registry.addResourceHandler("/api/return-images/**")
                .addResourceLocations("file:uploads/return-images/", uploadPath + "return-images/")
                .setCachePeriod(3600);

        // âœ… THÃŠM: Cáº¥u hÃ¬nh alternative path cho return images
        registry.addResourceHandler("/images/returns/**")
                .addResourceLocations("file:uploads/return-images/", uploadPath + "return-images/")
                .setCachePeriod(3600);

        // Backup configuration cho voucher images (Ä‘Ã£ cÃ³)
        registry.addResourceHandler("/voucher/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(3600);

        // Legacy support cho áº£nh cÅ© (Ä‘Ã£ cÃ³)
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(3600);

        // âœ… THÃŠM: Cáº¥u hÃ¬nh cho cÃ¡c loáº¡i áº£nh khÃ¡c (náº¿u cáº§n)
        registry.addResourceHandler("/product-images/**")
                .addResourceLocations("file:uploads/products/", uploadPath + "products/")
                .setCachePeriod(3600);

        // âœ… THÃŠM: Cáº¥u hÃ¬nh serve táº¥t cáº£ static content
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
    }

    // âœ… THÃŠM: Cáº¥u hÃ¬nh CORS náº¿u cáº§n (cÃ³ thá»ƒ bá» náº¿u Ä‘Ã£ cÃ³ á»Ÿ chá»— khÃ¡c)
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/uploads/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                .allowedMethods("GET")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);

        registry.addMapping("/api/return-images/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                .allowedMethods("GET")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
