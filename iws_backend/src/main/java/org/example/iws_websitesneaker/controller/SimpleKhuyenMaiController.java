//package org.example.iws_websitesneaker.controller;
//
//import org.example.iws_websitesneaker.Service.KhuyenMaiService;
//import org.example.iws_websitesneaker.entity.KhuyenMai;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/khuyen-mai")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:8081"}, allowCredentials = "true")
//public class SimpleKhuyenMaiController {
//
//    @Autowired
//    private KhuyenMaiService khuyenMaiService;
//
//    // Simple test endpoint
//    @GetMapping("/test")
//    public ResponseEntity<?> test() {
//        Map<String, Object> response = new HashMap<>();
//        response.put("status", "OK");
//        response.put("message", "Controller is working");
//        response.put("timestamp", System.currentTimeMillis());
//        return ResponseEntity.ok(response);
//    }
//
//    // Step 1: Test service call only
//    @GetMapping("/raw")
//    public ResponseEntity<?> getRawKhuyenMai() {
//        try {
//            System.out.println("=== Testing raw service call ===");
//            List<KhuyenMai> khuyenMais = khuyenMaiService.getAll();
//            System.out.println("Service returned: " + khuyenMais.size() + " items");
//
//            // Return just the count first
//            Map<String, Object> response = new HashMap<>();
//            response.put("count", khuyenMais.size());
//            response.put("message", "Service call successful");
//
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            System.err.println("Error in raw call: " + e.getMessage());
//            e.printStackTrace();
//
//            Map<String, Object> error = new HashMap<>();
//            error.put("error", e.getMessage());
//            error.put("type", e.getClass().getSimpleName());
//            return ResponseEntity.badRequest().body(error);
//        }
//    }
//
//    // Step 2: Test returning entities directly
//    @GetMapping("/entities")
//    public ResponseEntity<?> getEntities() {
//        try {
//            System.out.println("=== Testing entity return ===");
//            List<KhuyenMai> khuyenMais = khuyenMaiService.getAll();
//            System.out.println("Returning " + khuyenMais.size() + " entities");
//
//            return ResponseEntity.ok(khuyenMais);
//        } catch (Exception e) {
//            System.err.println("Error returning entities: " + e.getMessage());
//            e.printStackTrace();
//
//            Map<String, Object> error = new HashMap<>();
//            error.put("error", e.getMessage());
//            error.put("type", e.getClass().getSimpleName());
//            return ResponseEntity.badRequest().body(error);
//        }
//    }
//
//    // Step 3: Test simple mapping without product count
//    @GetMapping("/simple")
//    public ResponseEntity<?> getSimpleMapping() {
//        try {
//            System.out.println("=== Testing simple mapping ===");
//            List<KhuyenMai> khuyenMais = khuyenMaiService.getAll();
//
//            List<Map<String, Object>> responses = new ArrayList<>();
//            for (KhuyenMai km : khuyenMais) {
//                Map<String, Object> item = new HashMap<>();
//                item.put("id", km.getId());
//                item.put("maKhuyenMai", km.getMaKhuyenMai());
//                item.put("tenKhuyenMai", km.getTenKhuyenMai());
//                item.put("giaTri", km.getGiaTri());
//                item.put("trangThai", km.getTrangThai());
//                item.put("ngayBatDau", km.getNgayBatDau());
//                item.put("ngayKetThuc", km.getNgayKetThuc());
//                item.put("soLuongSanPham", 0); // Fixed value for now
//                responses.add(item);
//            }
//
//            System.out.println("Returning " + responses.size() + " mapped items");
//            return ResponseEntity.ok(responses);
//
//        } catch (Exception e) {
//            System.err.println("Error in simple mapping: " + e.getMessage());
//            e.printStackTrace();
//
//            Map<String, Object> error = new HashMap<>();
//            error.put("error", e.getMessage());
//            error.put("type", e.getClass().getSimpleName());
//            error.put("cause", e.getCause() != null ? e.getCause().getMessage() : null);
//            return ResponseEntity.badRequest().body(error);
//        }
//    }
//
//    // Main endpoint - simplified version
//    @GetMapping
//    public ResponseEntity<?> getAllKhuyenMai() {
//        try {
//            System.out.println("=== Main GET /khuyen-mai called ===");
//
//            List<KhuyenMai> khuyenMais = khuyenMaiService.getAll();
//            System.out.println("Found " + khuyenMais.size() + " promotions");
//
//            // Simple mapping without complex logic
//            List<Map<String, Object>> responses = new ArrayList<>();
//            for (KhuyenMai km : khuyenMais) {
//                Map<String, Object> item = new HashMap<>();
//                item.put("id", km.getId());
//                item.put("maKhuyenMai", km.getMaKhuyenMai());
//                item.put("tenKhuyenMai", km.getTenKhuyenMai());
//                item.put("giaTri", km.getGiaTri());
//                item.put("trangThai", km.getTrangThai());
//                item.put("ngayBatDau", km.getNgayBatDau());
//                item.put("ngayKetThuc", km.getNgayKetThuc());
//                item.put("ngayTao", km.getNgayTao());
//                item.put("ngayCapNhat", km.getNgayCapNhat());
//                item.put("soLuongSanPham", 0); // We'll implement this later
//                responses.add(item);
//            }
//
//            System.out.println("Successfully mapped " + responses.size() + " items");
//            return ResponseEntity.ok(responses);
//
//        } catch (Exception e) {
//            System.err.println("Error in getAllKhuyenMai: " + e.getMessage());
//            e.printStackTrace();
//
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("error", "Internal Server Error");
//            errorResponse.put("message", e.getMessage());
//            errorResponse.put("type", e.getClass().getSimpleName());
//            errorResponse.put("timestamp", System.currentTimeMillis());
//
//            if (e.getCause() != null) {
//                errorResponse.put("rootCause", e.getCause().getMessage());
//            }
//
//            return ResponseEntity.status(500).body(errorResponse);
//        }
//    }
//}
