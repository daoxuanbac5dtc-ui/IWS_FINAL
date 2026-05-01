// KhuyenMaiController.java - Fixed version
package org.example.iws_websitesneaker.controller;

import org.example.iws_websitesneaker.Dto.KhuyenMaiResponse;
import org.example.iws_websitesneaker.Service.impl.KhuyenMaiChiTietServiceImpl;
import org.example.iws_websitesneaker.entity.KhuyenMai;
import org.example.iws_websitesneaker.Dto.KhuyenMaiRequest;
import org.example.iws_websitesneaker.Service.KhuyenMaiService;
import org.example.iws_websitesneaker.Service.KhuyenMaiChiTietService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/khuyen-mai")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8081"}, allowCredentials = "true")
public class KhuyenMaiController {

    @Autowired
    private KhuyenMaiService khuyenMaiService;

    @Autowired
    private KhuyenMaiChiTietService khuyenMaiChiTietService;

    @GetMapping
    public ResponseEntity<?> getAllKhuyenMai() {
        try {
            List<KhuyenMai> khuyenMais = khuyenMaiService.getAll();
            List<KhuyenMaiResponse> responses = khuyenMais.stream()
                    .map(km -> {
                        KhuyenMaiResponse response = new KhuyenMaiResponse(km);
                        response.setSoLuongSanPham(
                                khuyenMaiChiTietService.getByKhuyenMaiId(km.getId()).size()
                        );
                        return response;
                    })
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getKhuyenMaiById(@PathVariable Integer id) {
        try {
            KhuyenMai khuyenMai = khuyenMaiService.getById(id);
            KhuyenMaiResponse response = new KhuyenMaiResponse(khuyenMai);
            response.setSoLuongSanPham(
                    khuyenMaiChiTietService.getByKhuyenMaiId(id).size()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Not Found");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping
    public ResponseEntity<?> createKhuyenMai(@RequestBody KhuyenMaiRequest request) {
        try {
            KhuyenMai khuyenMai = khuyenMaiService.create(request);
            return ResponseEntity.ok(khuyenMai);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Business Logic Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateKhuyenMai(@PathVariable Integer id, @RequestBody KhuyenMaiRequest request) {
        try {
            KhuyenMai oldKhuyenMai = khuyenMaiService.getById(id);
            Float oldGiaTri = oldKhuyenMai.getGiaTri();

            KhuyenMai khuyenMai = khuyenMaiService.update(id, request);

            // Nếu giá trị khuyến mãi thay đổi, tính lại giá cho tất cả sản phẩm
            if (!oldGiaTri.equals(request.getGiaTri())) {
                if (khuyenMaiChiTietService instanceof KhuyenMaiChiTietServiceImpl) {
                    ((KhuyenMaiChiTietServiceImpl) khuyenMaiChiTietService)
                            .recalculatePricesForPromotion(id);
                }
            }

            return ResponseEntity.ok(khuyenMai);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Business Logic Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteKhuyenMai(@PathVariable Integer id) {
        try {
            khuyenMaiChiTietService.removeAllPromotionsFromKhuyenMai(id);
            khuyenMaiService.delete(id);

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("message", "Xóa khuyến mãi thành công");
            return ResponseEntity.ok(successResponse);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Business Logic Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<?> changeStatus(@PathVariable Integer id) {
        try {
            KhuyenMai khuyenMai = khuyenMaiService.changeStatus(id);
            return ResponseEntity.ok(khuyenMai);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Business Logic Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/trang-thai/{trangThai}")
    public ResponseEntity<?> getByTrangThai(@PathVariable Integer trangThai) {
        try {
            List<KhuyenMai> khuyenMais = khuyenMaiService.getByTrangThai(trangThai);
            return ResponseEntity.ok(khuyenMais);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActivePromotions() {
        try {
            List<KhuyenMai> khuyenMais = khuyenMaiService.getActivePromotions();
            return ResponseEntity.ok(khuyenMais);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchKhuyenMai(@RequestParam String keyword) {
        try {
            List<KhuyenMai> khuyenMais = khuyenMaiService.searchByKeyword(keyword);
            return ResponseEntity.ok(khuyenMais);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
