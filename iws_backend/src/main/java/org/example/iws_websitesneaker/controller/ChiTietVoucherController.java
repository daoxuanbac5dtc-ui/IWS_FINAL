package org.example.iws_websitesneaker.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.iws_websitesneaker.Service.ChiTietVoucherService;
import org.example.iws_websitesneaker.Dto.ChiTietVoucherDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("/api/chi-tiet-voucher")
@RequiredArgsConstructor
@Slf4j
public class ChiTietVoucherController {

    private final ChiTietVoucherService chiTietVoucherService;


    @PutMapping("/{id}")
    public ResponseEntity<ChiTietVoucherDTO> update(@PathVariable Integer id, @RequestBody ChiTietVoucherDTO dto) {
        ChiTietVoucherDTO updated = chiTietVoucherService.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        chiTietVoucherService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // APIs tìm kiếm
    @GetMapping("/search/by-ma-ctv")
    public ResponseEntity<List<ChiTietVoucherDTO>> searchByMaChiTietVoucher(@RequestParam String keyword) {
        return ResponseEntity.ok(chiTietVoucherService.searchByMaChiTietVoucher(keyword));
    }

    @GetMapping("/search/by-ma-voucher")
    public ResponseEntity<List<ChiTietVoucherDTO>> searchByMaVoucher(@RequestParam String keyword) {
        return ResponseEntity.ok(chiTietVoucherService.searchByMaVoucher(keyword));
    }

    @GetMapping("/search/by-ten-voucher")
    public ResponseEntity<List<ChiTietVoucherDTO>> searchByTenVoucher(@RequestParam String keyword) {
        return ResponseEntity.ok(chiTietVoucherService.searchByTenVoucher(keyword));
    }
    @GetMapping("/hoa-don/{hoaDonId}")
    public ResponseEntity<Map<String, Object>> getChiTietVoucherByHoaDon(@PathVariable Integer hoaDonId) {
        try {
            log.info("🔍 Lấy chi tiết voucher cho hóa đơn ID: {}", hoaDonId);

            List<ChiTietVoucherDTO> chiTietVoucherList = chiTietVoucherService.findByHoaDonId(hoaDonId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy chi tiết voucher thành công");
            response.put("data", chiTietVoucherList);
            response.put("count", chiTietVoucherList.size());

            // Tính tổng tiết kiệm
            BigDecimal tongTietKiem = chiTietVoucherList.stream()
                    .map(ChiTietVoucherDTO::getSoTienGiam)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            response.put("tongTietKiem", tongTietKiem);

            log.info("✅ Tìm thấy {} chi tiết voucher, tổng tiết kiệm: {}",
                    chiTietVoucherList.size(), tongTietKiem);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Lỗi lấy chi tiết voucher cho hóa đơn {}: {}", hoaDonId, e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi lấy chi tiết voucher: " + e.getMessage());
            errorResponse.put("data", Collections.emptyList());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Lấy tất cả chi tiết voucher (phân trang)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllChiTietVoucher(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            log.info("📄 Lấy danh sách chi tiết voucher - Page: {}, Size: {}", page, size);

            Pageable pageable = PageRequest.of(page, size,
                    Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy));

            Page<ChiTietVoucherDTO> chiTietVoucherPage = chiTietVoucherService.findAll(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy danh sách thành công");
            response.put("data", chiTietVoucherPage.getContent());
            response.put("currentPage", chiTietVoucherPage.getNumber());
            response.put("totalPages", chiTietVoucherPage.getTotalPages());
            response.put("totalElements", chiTietVoucherPage.getTotalElements());
            response.put("pageSize", chiTietVoucherPage.getSize());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Lỗi lấy danh sách chi tiết voucher: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi lấy danh sách: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Lấy chi tiết voucher theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getChiTietVoucherById(@PathVariable Integer id) {
        try {
            log.info("🔍 Lấy chi tiết voucher ID: {}", id);

            ChiTietVoucherDTO chiTietVoucher = chiTietVoucherService.getById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy chi tiết voucher thành công");
            response.put("data", chiTietVoucher);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Lỗi lấy chi tiết voucher ID {}: {}", id, e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Không tìm thấy chi tiết voucher: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Tạo chi tiết voucher thủ công (nếu cần)
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createChiTietVoucher(@RequestBody ChiTietVoucherDTO dto) {
        try {
            log.info("➕ Tạo chi tiết voucher mới: {}", dto.getMaChiTietVoucher());

            ChiTietVoucherDTO created = chiTietVoucherService.create(dto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tạo chi tiết voucher thành công");
            response.put("data", created);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("❌ Lỗi tạo chi tiết voucher: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi tạo chi tiết voucher: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Lấy thống kê voucher theo hóa đơn
     */
    @GetMapping("/hoa-don/{hoaDonId}/thong-ke")
    public ResponseEntity<Map<String, Object>> getThongKeVoucherByHoaDon(@PathVariable Integer hoaDonId) {
        try {
            log.info("📊 Lấy thống kê voucher cho hóa đơn ID: {}", hoaDonId);

            List<ChiTietVoucherDTO> chiTietVoucherList = chiTietVoucherService.findByHoaDonId(hoaDonId);

            // Tính toán thống kê
            BigDecimal tongTietKiem = chiTietVoucherList.stream()
                    .map(ChiTietVoucherDTO::getSoTienGiam)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal tongGiaTriDonHang = chiTietVoucherList.stream()
                    .map(ChiTietVoucherDTO::getGiaTriDonHang)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::max); // Lấy giá trị lớn nhất (tổng đơn hàng)

            BigDecimal tongThanhTien = chiTietVoucherList.stream()
                    .map(ChiTietVoucherDTO::getThanhTien)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::min); // Lấy giá trị nhỏ nhất (sau cùng)

            // Tính % tiết kiệm
            double phanTramTietKiem = 0.0;
            if (tongGiaTriDonHang.compareTo(BigDecimal.ZERO) > 0) {
                phanTramTietKiem = tongTietKiem.divide(tongGiaTriDonHang, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();
            }

            Map<String, Object> thongKe = new HashMap<>();
            thongKe.put("soLuongVoucher", chiTietVoucherList.size());
            thongKe.put("tongGiaTriDonHang", tongGiaTriDonHang);
            thongKe.put("tongTietKiem", tongTietKiem);
            thongKe.put("tongThanhTien", tongThanhTien);
            thongKe.put("phanTramTietKiem", Math.round(phanTramTietKiem * 100.0) / 100.0);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy thống kê thành công");
            response.put("data", thongKe);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Lỗi lấy thống kê voucher: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi lấy thống kê: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}

