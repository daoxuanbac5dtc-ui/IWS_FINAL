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
        // Đường dẫn uploads với absolute path
        String uploadPath = Paths.get(uploadDir).toAbsolutePath().toUri().toString();

        // Cấu hình serve static files từ thư mục uploads (đã có)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/", uploadPath)
                .setCachePeriod(3600); // Cache 1 giờ

        // ✅ THÊM: Cấu hình riêng cho return images
        registry.addResourceHandler("/api/return-images/**")
                .addResourceLocations("file:uploads/return-images/", uploadPath + "return-images/")
                .setCachePeriod(3600);

        // ✅ THÊM: Cấu hình alternative path cho return images
        registry.addResourceHandler("/images/returns/**")
                .addResourceLocations("file:uploads/return-images/", uploadPath + "return-images/")
                .setCachePeriod(3600);

        // Backup configuration cho voucher images (đã có)
        registry.addResourceHandler("/voucher/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(3600);

        // Legacy support cho ảnh cũ (đã có)
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:uploads/", uploadPath, "classpath:/static/images/")
                .setCachePeriod(3600);

        // ✅ THÊM: Cấu hình cho các loại ảnh khác (nếu cần)
        registry.addResourceHandler("/product-images/**")
                .addResourceLocations("file:uploads/products/", uploadPath + "products/")
                .setCachePeriod(3600);

        // ✅ THÊM: Cấu hình serve tất cả static content
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
    }

    // ✅ THÊM: Cấu hình CORS nếu cần (có thể bỏ nếu đã có ở chỗ khác)
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/uploads/**")
                .allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:3000")
                .allowedMethods("GET")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);

        registry.addMapping("/api/return-images/**")
                .allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:3000")
                .allowedMethods("GET")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);

        registry.addMapping("/images/**")
                .allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:3000")
                .allowedMethods("GET")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
