package org.example.iws_websitesneaker.controller;

import org.example.iws_websitesneaker.Dto.WardApiDto;
import org.example.iws_websitesneaker.Dto.ProvinceApiDto;
import org.example.iws_websitesneaker.Service.VietnamAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vietnam-address")
@CrossOrigin(
        origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:8081"},
        allowCredentials = "true",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}
)
public class VietnamAddressController {

    // Constants
    private static final int MIN_PROVINCE_CODE = 1;
    private static final int MAX_PROVINCE_CODE = 99;
    private static final String CACHE_CONTROL_HEADER = "public, max-age=86400";

    @Autowired
    private VietnamAddressService vietnamAddressService;

    /**
     * Lấy tất cả tỉnh/thành phố Việt Nam
     */
    @GetMapping("/provinces")
    public ResponseEntity<?> getAllProvinces() {
        try {
            List<ProvinceApiDto> provinces = vietnamAddressService.getAllProvinces();

            return ResponseEntity.ok()
                    .header("Cache-Control", CACHE_CONTROL_HEADER)
                    .body(Map.of(
                            "success", true,
                            "message", "Lấy danh sách tỉnh/thành phố thành công",
                            "data", provinces,
                            "total", provinces.size(),
                            "cached", true,
                            "timestamp", System.currentTimeMillis()
                    ));
        } catch (Exception e) {
            System.err.println("Error in getAllProvinces: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Lỗi khi lấy danh sách tỉnh/thành phố",
                            "errorCode", "PROVINCES_FETCH_ERROR",
                            "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * Lấy danh sách xã/phường theo mã tỉnh (BỎ HUYỆN - CHỈ CÓN 2 CẤP)
     * Frontend gọi: /api/vietnam-address/wards/{provinceCode}
     */
    @GetMapping("/wards/{provinceCode}")
    public ResponseEntity<?> getWardsByProvince(@PathVariable String provinceCode) {
        try {
            // Validation đầu vào
            if (provinceCode == null || provinceCode.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "Mã tỉnh không được để trống",
                                "errorCode", "EMPTY_PROVINCE_CODE",
                                "timestamp", System.currentTimeMillis()
                        ));
            }

            Integer provinceCodeInt;
            try {
                provinceCodeInt = Integer.parseInt(provinceCode.trim());

                // Validation phạm vi
                if (provinceCodeInt < MIN_PROVINCE_CODE || provinceCodeInt > MAX_PROVINCE_CODE) {
                    return ResponseEntity.badRequest()
                            .body(Map.of(
                                    "success", false,
                                    "message", String.format("Mã tỉnh phải trong khoảng %d-%d", MIN_PROVINCE_CODE, MAX_PROVINCE_CODE),
                                    "errorCode", "PROVINCE_CODE_OUT_OF_RANGE",
                                    "providedValue", provinceCode,
                                    "timestamp", System.currentTimeMillis()
                            ));
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "Mã tỉnh phải là số nguyên hợp lệ: " + provinceCode,
                                "errorCode", "INVALID_NUMBER_FORMAT",
                                "providedValue", provinceCode,
                                "timestamp", System.currentTimeMillis()
                        ));
            }

            List<WardApiDto> wards = vietnamAddressService.getWardsByProvinceCode(provinceCodeInt);

            // Kiểm tra kết quả
            if (wards == null || wards.isEmpty()) {
                return ResponseEntity.ok()
                        .header("Cache-Control", CACHE_CONTROL_HEADER)
                        .body(Map.of(
                                "success", true,
                                "message", "Không tìm thấy xã/phường cho mã tỉnh: " + provinceCodeInt,
                                "data", List.of(),
                                "total", 0,
                                "provinceCode", provinceCodeInt,
                                "timestamp", System.currentTimeMillis()
                        ));
            }

            return ResponseEntity.ok()
                    .header("Cache-Control", CACHE_CONTROL_HEADER)
                    .body(Map.of(
                            "success", true,
                            "message", "Lấy danh sách xã/phường thành công",
                            "data", wards,
                            "total", wards.size(),
                            "provinceCode", provinceCodeInt,
                            "cached", true,
                            "timestamp", System.currentTimeMillis()
                    ));
        } catch (Exception e) {
            System.err.println("Error in getWardsByProvince for code " + provinceCode + ": " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Lỗi hệ thống khi lấy danh sách xã/phường",
                            "errorCode", "WARDS_FETCH_ERROR",
                            "provinceCode", provinceCode,
                            "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * DEPRECATED: Endpoint cũ cho districts - giữ lại để tương thích ngược
     */
    @GetMapping("/districts/{provinceCode}")
    public ResponseEntity<?> getDistrictsByProvince(@PathVariable String provinceCode) {
        return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Endpoint này đã bị vô hiệu hóa. Hệ thống chỉ sử dụng 2 cấp địa chỉ: Tỉnh/TP và Xã/Phường",
                "deprecated", true,
                "newEndpoint", "/api/vietnam-address/wards/" + provinceCode,
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Health check chi tiết
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            // Test kết nối đến API bên ngoài
            List<ProvinceApiDto> provinces = vietnamAddressService.getAllProvinces();
            boolean isHealthy = provinces != null && !provinces.isEmpty();

            Map<String, Object> healthData = Map.of(
                    "status", isHealthy ? "UP" : "DOWN",
                    "timestamp", System.currentTimeMillis(),
                    "service", "Vietnam Address API",
                    "version", "2.0 (2-level addressing)",
                    "details", Map.of(
                            "external_api_connection", isHealthy ? "CONNECTED" : "DISCONNECTED",
                            "cache_status", "ACTIVE",
                            "total_provinces", provinces != null ? provinces.size() : 0,
                            "structure", "Province -> Ward (No District)"
                    )
            );

            return isHealthy ?
                    ResponseEntity.ok(healthData) :
                    ResponseEntity.status(503).body(healthData);

        } catch (Exception e) {
            System.err.println("Health check failed: " + e.getMessage());
            return ResponseEntity.status(503)
                    .body(Map.of(
                            "status", "DOWN",
                            "timestamp", System.currentTimeMillis(),
                            "error", e.getMessage(),
                            "service", "Vietnam Address API"
                    ));
        }
    }

    /**
     * API endpoint để test connection
     */
    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Vietnam Address API is working",
                "timestamp", System.currentTimeMillis(),
                "structure", "2-level: Province -> Ward",
                "endpoints", Map.of(
                        "provinces", "/api/vietnam-address/provinces",
                        "wards", "/api/vietnam-address/wards/{provinceCode}",
                        "health", "/api/vietnam-address/health",
                        "summary", "/api/vietnam-address/summary"
                )
        ));
    }

    /**
     * API để lấy thông tin summary
     */
    @GetMapping("/summary")
    public ResponseEntity<?> getSummary() {
        try {
            List<ProvinceApiDto> provinces = vietnamAddressService.getAllProvinces();

            return ResponseEntity.ok()
                    .header("Cache-Control", CACHE_CONTROL_HEADER)
                    .body(Map.of(
                            "success", true,
                            "message", "Thông tin tổng quan địa chỉ Việt Nam",
                            "timestamp", System.currentTimeMillis(),
                            "data", Map.of(
                                    "totalProvinces", provinces.size(),
                                    "structure", "2-level addressing",
                                    "levels", List.of("Tỉnh/Thành phố", "Xã/Phường"),
                                    "note", "Đã bỏ cấp Quận/Huyện để đơn giản hóa",
                                    "caching", "24 hours cache duration",
                                    "apiVersion", "2.0"
                            )
                    ));
        } catch (Exception e) {
            System.err.println("Error in getSummary: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Lỗi khi lấy thông tin tổng quan",
                            "errorCode", "SUMMARY_FETCH_ERROR",
                            "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * Clear cache - chỉ dùng cho development/testing
     * Có thể bảo vệ bằng authentication trong production
     */
    @PostMapping("/clear-cache")
    public ResponseEntity<?> clearCache() {
        try {
            vietnamAddressService.clearCache();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cache đã được xóa thành công",
                    "timestamp", System.currentTimeMillis(),
                    "note", "Dữ liệu mới sẽ được tải từ API bên ngoài trong lần gọi tiếp theo"
            ));
        } catch (Exception e) {
            System.err.println("Error clearing cache: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Lỗi khi xóa cache",
                            "errorCode", "CACHE_CLEAR_ERROR",
                            "error", e.getMessage(),
                            "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * Get cache statistics
     */
    @GetMapping("/cache-stats")
    public ResponseEntity<?> getCacheStats() {
        try {
            // Đây là method giả định - bạn có thể implement trong service
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Thông tin cache",
                    "timestamp", System.currentTimeMillis(),
                    "data", Map.of(
                            "cacheEnabled", true,
                            "cacheDuration", "24 hours",
                            "note", "Implement cache statistics in VietnamAddressService if needed"
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Lỗi khi lấy thông tin cache",
                            "timestamp", System.currentTimeMillis()
                    ));
        }
    }
}
