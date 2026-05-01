package org.example.iws_websitesneaker.controller;

import org.example.iws_websitesneaker.Dto.HoaDonChiTietDTO;
import org.example.iws_websitesneaker.Service.HoaDonChiTietService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hoa-don-chi-tiet")
@CrossOrigin(origins = "http://localhost:5173")
public class HoaDonChiTietRestController {

    @Autowired
    private HoaDonChiTietService hoaDonChiTietService;

    @GetMapping("/by-hoa-don/{hoaDonId}")
    public ResponseEntity<Map<String, Object>> getChiTietByHoaDonId(@PathVariable Integer hoaDonId) {
        try {
            List<HoaDonChiTietDTO> chiTietList = hoaDonChiTietService.getChiTietByHoaDonId(hoaDonId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", chiTietList,
                    "total", chiTietList.size(),
                    "message", "Lấy chi tiết hóa đơn thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Integer id) {
        try {
            HoaDonChiTietDTO chiTiet = hoaDonChiTietService.getById(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", chiTiet,
                    "message", "Lấy thông tin chi tiết thành công"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/quantity")
    public ResponseEntity<Map<String, Object>> updateQuantity(
            @PathVariable Integer id,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer soLuong = request.get("soLuong");
            if (soLuong == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lòng nhập số lượng"
                ));
            }

            HoaDonChiTietDTO updated = hoaDonChiTietService.updateQuantity(id, soLuong);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", updated,
                    "message", "Cập nhật số lượng thành công"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable Integer id,
            @RequestBody HoaDonChiTietDTO request) {
        try {
            HoaDonChiTietDTO updated = hoaDonChiTietService.updateProduct(id, request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", updated,
                    "message", "Cập nhật sản phẩm thành công"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/remove-product/{id}")
    public ResponseEntity<Map<String, Object>> removeProduct(@PathVariable Integer id) {
        try {
            hoaDonChiTietService.removeProduct(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xóa sản phẩm thành công. Số lượng đã được hoàn lại kho."
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // API bổ sung
    @GetMapping("/thong-ke/{hoaDonId}")
    public ResponseEntity<Map<String, Object>> getStatistics(@PathVariable Integer hoaDonId) {
        try {
            List<HoaDonChiTietDTO> chiTietList = hoaDonChiTietService.getChiTietByHoaDonId(hoaDonId);

            int tongSoLuong = chiTietList.stream().mapToInt(HoaDonChiTietDTO::getSoLuong).sum();
            double tongThanhTien = chiTietList.stream()
                    .mapToDouble(ct -> ct.getThanhTien() != null ? ct.getThanhTien().doubleValue() : 0)
                    .sum();
            double tongTietKiem = chiTietList.stream()
                    .mapToDouble(ct -> ct.getTienTietKiem() != null ? ct.getTienTietKiem().doubleValue() : 0)
                    .sum();

            Map<String, Object> stats = Map.of(
                    "soLoaiSanPham", chiTietList.size(),
                    "tongSoLuong", tongSoLuong,
                    "tongThanhTien", tongThanhTien,
                    "tongTietKiem", tongTietKiem,
                    "tyLeGiamGia", tongThanhTien > 0 ? (tongTietKiem / (tongThanhTien + tongTietKiem) * 100) : 0
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", stats
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

}
