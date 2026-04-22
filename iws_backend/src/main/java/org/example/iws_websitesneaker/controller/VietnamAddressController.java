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
     * Láº¥y táº¥t cáº£ tá»‰nh/thÃ nh phá»‘ Viá»‡t Nam
     */
    @GetMapping("/provinces")
    public ResponseEntity<?> getAllProvinces() {
        try {
            List<ProvinceApiDto> provinces = vietnamAddressService.getAllProvinces();

            return ResponseEntity.ok()
                    .header("Cache-Control", CACHE_CONTROL_HEADER)
                    .body(Map.of(
                            "success", true,
                            "message", "Láº¥y danh sÃ¡ch tá»‰nh/thÃ nh phá»‘ thÃ nh cÃ´ng",
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
                            "message", "Lá»—i khi láº¥y danh sÃ¡ch tá»‰nh/thÃ nh phá»‘",
                            "errorCode", "PROVINCES_FETCH_ERROR",
                            "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * Láº¥y danh sÃ¡ch xÃ£/phÆ°á»ng theo mÃ£ tá»‰nh (Bá»Ž HUYá»†N - CHá»ˆ CÃ“N 2 Cáº¤P)
     * Frontend gá»i: /api/vietnam-address/wards/{provinceCode}
     */
    @GetMapping("/wards/{provinceCode}")
    public ResponseEntity<?> getWardsByProvince(@PathVariable String provinceCode) {
        try {
            // Validation Ä‘áº§u vÃ o
            if (provinceCode == null || provinceCode.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "MÃ£ tá»‰nh khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng",
                                "errorCode", "EMPTY_PROVINCE_CODE",
                                "timestamp", System.currentTimeMillis()
                        ));
            }

            Integer provinceCodeInt;
            try {
                provinceCodeInt = Integer.parseInt(provinceCode.trim());

                // Validation pháº¡m vi
                if (provinceCodeInt < MIN_PROVINCE_CODE || provinceCodeInt > MAX_PROVINCE_CODE) {
                    return ResponseEntity.badRequest()
                            .body(Map.of(
                                    "success", false,
                                    "message", String.format("MÃ£ tá»‰nh pháº£i trong khoáº£ng %d-%d", MIN_PROVINCE_CODE, MAX_PROVINCE_CODE),
                                    "errorCode", "PROVINCE_CODE_OUT_OF_RANGE",
                                    "providedValue", provinceCode,
                                    "timestamp", System.currentTimeMillis()
                            ));
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "MÃ£ tá»‰nh pháº£i lÃ  sá»‘ nguyÃªn há»£p lá»‡: " + provinceCode,
                                "errorCode", "INVALID_NUMBER_FORMAT",
                                "providedValue", provinceCode,
                                "timestamp", System.currentTimeMillis()
                        ));
            }

            List<WardApiDto> wards = vietnamAddressService.getWardsByProvinceCode(provinceCodeInt);

            // Kiá»ƒm tra káº¿t quáº£
            if (wards == null || wards.isEmpty()) {
                return ResponseEntity.ok()
                        .header("Cache-Control", CACHE_CONTROL_HEADER)
                        .body(Map.of(
                                "success", true,
                                "message", "KhÃ´ng tÃ¬m tháº¥y xÃ£/phÆ°á»ng cho mÃ£ tá»‰nh: " + provinceCodeInt,
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
                            "message", "Láº¥y danh sÃ¡ch xÃ£/phÆ°á»ng thÃ nh cÃ´ng",
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
                            "message", "Lá»—i há»‡ thá»‘ng khi láº¥y danh sÃ¡ch xÃ£/phÆ°á»ng",
                            "errorCode", "WARDS_FETCH_ERROR",
                            "provinceCode", provinceCode,
                            "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * DEPRECATED: Endpoint cÅ© cho districts - giá»¯ láº¡i Ä‘á»ƒ tÆ°Æ¡ng thÃ­ch ngÆ°á»£c
     */
    @GetMapping("/districts/{provinceCode}")
    public ResponseEntity<?> getDistrictsByProvince(@PathVariable String provinceCode) {
        return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Endpoint nÃ y Ä‘Ã£ bá»‹ vÃ´ hiá»‡u hÃ³a. Há»‡ thá»‘ng chá»‰ sá»­ dá»¥ng 2 cáº¥p Ä‘á»‹a chá»‰: Tá»‰nh/TP vÃ  XÃ£/PhÆ°á»ng",
                "deprecated", true,
                "newEndpoint", "/api/vietnam-address/wards/" + provinceCode,
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Health check chi tiáº¿t
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            // Test káº¿t ná»‘i Ä‘áº¿n API bÃªn ngoÃ i
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
     * API endpoint Ä‘á»ƒ test connection
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
     * API Ä‘á»ƒ láº¥y thÃ´ng tin summary
     */
    @GetMapping("/summary")
    public ResponseEntity<?> getSummary() {
        try {
            List<ProvinceApiDto> provinces = vietnamAddressService.getAllProvinces();

            return ResponseEntity.ok()
                    .header("Cache-Control", CACHE_CONTROL_HEADER)
                    .body(Map.of(
                            "success", true,
                            "message", "ThÃ´ng tin tá»•ng quan Ä‘á»‹a chá»‰ Viá»‡t Nam",
                            "timestamp", System.currentTimeMillis(),
                            "data", Map.of(
                                    "totalProvinces", provinces.size(),
                                    "structure", "2-level addressing",
                                    "levels", List.of("Tá»‰nh/ThÃ nh phá»‘", "XÃ£/PhÆ°á»ng"),
                                    "note", "ÄÃ£ bá» cáº¥p Quáº­n/Huyá»‡n Ä‘á»ƒ Ä‘Æ¡n giáº£n hÃ³a",
                                    "caching", "24 hours cache duration",
                                    "apiVersion", "2.0"
                            )
                    ));
        } catch (Exception e) {
            System.err.println("Error in getSummary: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Lá»—i khi láº¥y thÃ´ng tin tá»•ng quan",
                            "errorCode", "SUMMARY_FETCH_ERROR",
                            "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * Clear cache - chá»‰ dÃ¹ng cho development/testing
     * CÃ³ thá»ƒ báº£o vá»‡ báº±ng authentication trong production
     */
    @PostMapping("/clear-cache")
    public ResponseEntity<?> clearCache() {
        try {
            vietnamAddressService.clearCache();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cache Ä‘Ã£ Ä‘Æ°á»£c xÃ³a thÃ nh cÃ´ng",
                    "timestamp", System.currentTimeMillis(),
                    "note", "Dá»¯ liá»‡u má»›i sáº½ Ä‘Æ°á»£c táº£i tá»« API bÃªn ngoÃ i trong láº§n gá»i tiáº¿p theo"
            ));
        } catch (Exception e) {
            System.err.println("Error clearing cache: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Lá»—i khi xÃ³a cache",
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
            // ÄÃ¢y lÃ  method giáº£ Ä‘á»‹nh - báº¡n cÃ³ thá»ƒ implement trong service
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "ThÃ´ng tin cache",
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
                            "message", "Lá»—i khi láº¥y thÃ´ng tin cache",
                            "timestamp", System.currentTimeMillis()
                    ));
        }
    }
}
