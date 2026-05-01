package org.example.iws_websitesneaker.controller;

import org.example.iws_websitesneaker.Dto.ChiTietSanPhamDTO;
import org.example.iws_websitesneaker.entity.KhuyenMaiChiTiet;
import org.example.iws_websitesneaker.entity.ChiTietSanPham;
import org.example.iws_websitesneaker.Dto.ApplyPromotionRequest;
import org.example.iws_websitesneaker.Service.KhuyenMaiChiTietService;
import org.example.iws_websitesneaker.Service.ChiTietSanPhamService;
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
@RequestMapping("/khuyen-mai-chi-tiet")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:8081"}, allowCredentials = "true")
public class KhuyenMaiChiTietController {

    @Autowired
    private KhuyenMaiChiTietService khuyenMaiChiTietService;

    @Autowired
    private ChiTietSanPhamService chiTietSanPhamService;

    @GetMapping("/{khuyenMaiId}")
    public ResponseEntity<?> getByKhuyenMaiId(@PathVariable Integer khuyenMaiId) {
        try {
            System.out.println("Getting promotion details for ID: " + khuyenMaiId);
            List<KhuyenMaiChiTiet> details = khuyenMaiChiTietService.getByKhuyenMaiId(khuyenMaiId);
            System.out.println("Found " + details.size() + " promotion details");

            // Convert to safe DTOs để tránh lazy loading issues
            List<Map<String, Object>> safeDTOs = convertToSafeDTOs(details);

            return ResponseEntity.ok(safeDTOs);
        } catch (Exception e) {
            System.err.println("Error getting promotion details: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getDetailById(@PathVariable Integer id) {
        try {
            KhuyenMaiChiTiet detail = khuyenMaiChiTietService.getDetailById(id);
            Map<String, Object> safeDTO = convertToSafeDTO(detail);
            return ResponseEntity.ok(safeDTO);
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

    @PostMapping("/apply")
    public ResponseEntity<?> applyPromotionToProducts(@RequestBody ApplyPromotionRequest request) {
        try {
            if (request.getKhuyenMaiId() == null || request.getChiTietSanPhamIds() == null ||
                    request.getChiTietSanPhamIds().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Bad Request");
                errorResponse.put("message", "Thông tin khuyến mãi và sản phẩm là bắt buộc");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            List<KhuyenMaiChiTiet> results = khuyenMaiChiTietService.applyPromotionToProducts(request);

            // Convert to safe DTOs
            List<Map<String, Object>> safeDTOs = convertToSafeDTOs(results);

            return ResponseEntity.ok(safeDTOs);
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

    @DeleteMapping("/{khuyenMaiId}/product/{chiTietSanPhamId}")
    public ResponseEntity<?> removePromotionFromProduct(
            @PathVariable Integer khuyenMaiId,
            @PathVariable Integer chiTietSanPhamId) {
        try {
            khuyenMaiChiTietService.removePromotionFromProduct(khuyenMaiId, chiTietSanPhamId);

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("message", "Hủy áp dụng khuyến mãi thành công");
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

    @DeleteMapping("/{khuyenMaiId}/all")
    public ResponseEntity<?> removeAllPromotionsFromKhuyenMai(@PathVariable Integer khuyenMaiId) {
        try {
            khuyenMaiChiTietService.removeAllPromotionsFromKhuyenMai(khuyenMaiId);

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("message", "Hủy tất cả áp dụng khuyến mãi thành công");
            return ResponseEntity.ok(successResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/products/available")
    public ResponseEntity<?> getAvailableProducts() {
        try {
            System.out.println("Getting available products...");
            List<ChiTietSanPham> products = chiTietSanPhamService.getProductsWithoutPromotion();
            System.out.println("Found " + products.size() + " available products");

            // Convert to DTOs to avoid lazy loading issues
            List<ChiTietSanPhamDTO> productDTOs = products.stream()
                    .map(ChiTietSanPhamDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(productDTOs);
        } catch (Exception e) {
            System.err.println("Error getting available products: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/products/all")
    public ResponseEntity<?> getAllProducts() {
        try {
            System.out.println("Getting all active products...");
            List<ChiTietSanPham> products = chiTietSanPhamService.getActiveProducts();
            System.out.println("Found " + products.size() + " active products");

            // Convert to DTOs
            List<ChiTietSanPhamDTO> productDTOs = products.stream()
                    .map(ChiTietSanPhamDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(productDTOs);
        } catch (Exception e) {
            System.err.println("Error getting all products: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/products/search")
    public ResponseEntity<?> searchProducts(@RequestParam String keyword) {
        try {
            System.out.println("Searching products with keyword: " + keyword);
            List<ChiTietSanPham> products = chiTietSanPhamService.searchByKeyword(keyword);
            System.out.println("Found " + products.size() + " products for keyword: " + keyword);

            // Convert to DTOs
            List<ChiTietSanPhamDTO> productDTOs = products.stream()
                    .map(ChiTietSanPhamDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(productDTOs);
        } catch (Exception e) {
            System.err.println("Error searching products: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Debug endpoints
    @GetMapping("/debug/available-products")
    public ResponseEntity<?> debugAvailableProducts() {
        try {
            List<ChiTietSanPham> products = chiTietSanPhamService.getProductsWithoutPromotion();

            List<Map<String, Object>> debugData = new ArrayList<>();

            for (ChiTietSanPham product : products) {
                Map<String, Object> debug = new HashMap<>();
                debug.put("id", product.getId());
                debug.put("maChiTiet", product.getMaChiTiet());
                debug.put("giaGoc", product.getGiaGoc());
                debug.put("soLuong", product.getSoLuong());

                // Debug sản phẩm
                if (product.getSanPham() != null) {
                    Map<String, Object> spInfo = new HashMap<>();
                    spInfo.put("id", product.getSanPham().getId());
                    spInfo.put("tenSanPham", product.getSanPham().getTenSanPham());
                    spInfo.put("maSanPham", product.getSanPham().getMaSanPham());

                    // Debug thương hiệu
                    if (product.getSanPham().getThuongHieu() != null) {
                        spInfo.put("thuongHieu", product.getSanPham().getThuongHieu().getTenThuongHieu());
                    } else {
                        spInfo.put("thuongHieu", "NULL");
                    }

                    // Debug danh mục
                    if (product.getSanPham().getDanhMuc() != null) {
                        spInfo.put("danhMuc", product.getSanPham().getDanhMuc().getTenDanhMuc());
                    } else {
                        spInfo.put("danhMuc", "NULL");
                    }

                    debug.put("sanPham", spInfo);
                } else {
                    debug.put("sanPham", "NULL");
                }

                // Debug màu sắc
                if (product.getMauSac() != null) {
                    Map<String, Object> msInfo = new HashMap<>();
                    msInfo.put("id", product.getMauSac().getId());
                    msInfo.put("tenMauSac", product.getMauSac().getTenMauSac());
                    msInfo.put("maMau", product.getMauSac().getMaMauSac());
                    debug.put("mauSac", msInfo);
                } else {
                    debug.put("mauSac", "NULL");
                }

                // Debug kích cỡ
                if (product.getKichCo() != null) {
                    Map<String, Object> kcInfo = new HashMap<>();
                    kcInfo.put("id", product.getKichCo().getId());
                    kcInfo.put("tenKichCo", product.getKichCo().getTenKichCo());
                    debug.put("kichCo", kcInfo);
                } else {
                    debug.put("kichCo", "NULL");
                }

                debugData.add(debug);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("total", products.size());
            response.put("data", debugData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("stackTrace", e.getStackTrace());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "API is working");
            response.put("timestamp", System.currentTimeMillis());

            try {
                Long count = chiTietSanPhamService.countActiveProducts();
                response.put("active_products_count", count);
            } catch (Exception e) {
                response.put("database_error", e.getMessage());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/debug/database")
    public ResponseEntity<?> debugDatabase() {
        try {
            Map<String, Object> response = new HashMap<>();

            Long totalCount = chiTietSanPhamService.countActiveProducts();
            response.put("total_active_products", totalCount);

            List<ChiTietSanPham> sampleProducts = chiTietSanPhamService.getActiveProducts()
                    .stream().limit(3).collect(Collectors.toList());

            response.put("sample_products_count", sampleProducts.size());
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Database Debug Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Helper methods để convert entity thành safe DTO
    private List<Map<String, Object>> convertToSafeDTOs(List<KhuyenMaiChiTiet> details) {
        List<Map<String, Object>> safeDTOs = new ArrayList<>();
        for (KhuyenMaiChiTiet detail : details) {
            safeDTOs.add(convertToSafeDTO(detail));
        }
        return safeDTOs;
    }

    private Map<String, Object> convertToSafeDTO(KhuyenMaiChiTiet detail) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", detail.getId());
        dto.put("trangThai", detail.getTrangThai());
        dto.put("ngayTao", detail.getNgayTao());
        dto.put("ngayCapNhat", detail.getNgayCapNhat());

        // Safe khuyến mãi info
        if (detail.getKhuyenMai() != null) {
            Map<String, Object> kmInfo = new HashMap<>();
            kmInfo.put("id", detail.getKhuyenMai().getId());
            kmInfo.put("maKhuyenMai", detail.getKhuyenMai().getMaKhuyenMai());
            kmInfo.put("tenKhuyenMai", detail.getKhuyenMai().getTenKhuyenMai());
            kmInfo.put("giaTri", detail.getKhuyenMai().getGiaTri());
            kmInfo.put("ngayBatDau", detail.getKhuyenMai().getNgayBatDau());
            kmInfo.put("ngayKetThuc", detail.getKhuyenMai().getNgayKetThuc());
            kmInfo.put("trangThai", detail.getKhuyenMai().getTrangThai());
            dto.put("khuyenMai", kmInfo);
        }

        // Safe chi tiết sản phẩm info
        if (detail.getChiTietSanPham() != null) {
            Map<String, Object> ctspInfo = new HashMap<>();
            ChiTietSanPham ctsp = detail.getChiTietSanPham();

            ctspInfo.put("id", ctsp.getId());
            ctspInfo.put("maChiTiet", ctsp.getMaChiTiet());
            ctspInfo.put("giaGoc", ctsp.getGiaGoc());
            ctspInfo.put("giaBan", ctsp.getGiaBan());
            ctspInfo.put("soLuong", ctsp.getSoLuong());
            ctspInfo.put("trangThai", ctsp.getTrangThai());

            // Safe sản phẩm info
            if (ctsp.getSanPham() != null) {
                Map<String, Object> spInfo = new HashMap<>();
                spInfo.put("id", ctsp.getSanPham().getId());
                spInfo.put("tenSanPham", ctsp.getSanPham().getTenSanPham());
                spInfo.put("maSanPham", ctsp.getSanPham().getMaSanPham());

                // Safe thương hiệu
                if (ctsp.getSanPham().getThuongHieu() != null) {
                    Map<String, Object> thInfo = new HashMap<>();
                    thInfo.put("id", ctsp.getSanPham().getThuongHieu().getId());
                    thInfo.put("tenThuongHieu", ctsp.getSanPham().getThuongHieu().getTenThuongHieu());
                    spInfo.put("thuongHieu", thInfo);
                }

                // Safe danh mục
                if (ctsp.getSanPham().getDanhMuc() != null) {
                    Map<String, Object> dmInfo = new HashMap<>();
                    dmInfo.put("id", ctsp.getSanPham().getDanhMuc().getId());
                    dmInfo.put("tenDanhMuc", ctsp.getSanPham().getDanhMuc().getTenDanhMuc());
                    spInfo.put("danhMuc", dmInfo);
                }

                ctspInfo.put("sanPham", spInfo);
            }

            // Safe màu sắc
            if (ctsp.getMauSac() != null) {
                Map<String, Object> msInfo = new HashMap<>();
                msInfo.put("id", ctsp.getMauSac().getId());
                msInfo.put("tenMauSac", ctsp.getMauSac().getTenMauSac());
                msInfo.put("maMau", ctsp.getMauSac().getMaMauSac());
                ctspInfo.put("mauSac", msInfo);
            }

            // Safe kích cỡ
            if (ctsp.getKichCo() != null) {
                Map<String, Object> kcInfo = new HashMap<>();
                kcInfo.put("id", ctsp.getKichCo().getId());
                kcInfo.put("tenKichCo", ctsp.getKichCo().getTenKichCo());
                ctspInfo.put("kichCo", kcInfo);
            }

            dto.put("chiTietSanPham", ctspInfo);
        }

        return dto;
    }

    @PutMapping("/{promotionId}/reset-prices")
    public ResponseEntity<?> resetPricesForPromotion(@PathVariable Integer promotionId) {
        try {
            System.out.println("Resetting prices for promotion ID: " + promotionId);
            khuyenMaiChiTietService.resetPricesForInactivePromotion(promotionId);

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("message", "Đã reset giá về giá gốc cho khuyến mãi");
            return ResponseEntity.ok(successResponse);
        } catch (Exception e) {
            System.err.println("Error resetting prices for promotion: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/reset-all-inactive-prices")
    public ResponseEntity<?> resetAllInactivePrices() {
        try {
            System.out.println("Resetting prices for all inactive promotions");
            khuyenMaiChiTietService.resetAllInactivePrices();

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("message", "Đã reset giá về giá gốc cho tất cả khuyến mãi không hoạt động");
            return ResponseEntity.ok(successResponse);
        } catch (Exception e) {
            System.err.println("Error resetting all inactive prices: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
