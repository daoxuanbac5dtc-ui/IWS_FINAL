    package org.example.iws_websitesneaker.controller;

    import lombok.extern.slf4j.Slf4j;
    import org.example.iws_websitesneaker.repository.BanHang.HoaDonBHRepository;
    import org.springframework.dao.DataAccessException;
    import org.springframework.transaction.UnexpectedRollbackException;
    import org.springframework.transaction.annotation.Transactional;
    import org.example.iws_websitesneaker.Dto.BanHang.*;
    import org.example.iws_websitesneaker.Service.BanHangService;
    import org.example.iws_websitesneaker.entity.NhanVien;
    import org.example.iws_websitesneaker.repository.BanHang.NhanVienBHRepository;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.domain.Sort;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import jakarta.validation.Valid;

    import java.math.BigDecimal;
    import java.util.*;
    import java.util.stream.Collectors;

    @RestController
    @RequestMapping("/api/ban-hang")
    @CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
    @Slf4j
    public class BanHangController {

        private static final Logger logger = LoggerFactory.getLogger(BanHangController.class);

        @Autowired
        private BanHangService banHangService;
        @Autowired
        private NhanVienBHRepository nhanVienBHRepository;
        @Autowired
        private HoaDonBHRepository hoaDonRepository;

        // ===== HÃ“A ÄÆ N CHá»œ =====

        /**
         * Láº¥y danh sÃ¡ch hÃ³a Ä‘Æ¡n chá»
         */

        @Transactional(readOnly = true)
        @GetMapping("/hoa-don-cho")
        public ResponseEntity<Map<String, Object>> layDanhSachHoaDonCho() {
            try {
                logger.info("ðŸ” [API] Láº¥y danh sÃ¡ch hÃ³a Ä‘Æ¡n chá»");
                List<HoaDonChoResponse> hoaDonCho = banHangService.layDanhSachHoaDonCho();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Láº¥y danh sÃ¡ch hÃ³a Ä‘Æ¡n chá» thÃ nh cÃ´ng");
                response.put("data", hoaDonCho);
                response.put("total", hoaDonCho.size());

                logger.info("âœ… [API] Láº¥y {} hÃ³a Ä‘Æ¡n chá» thÃ nh cÃ´ng", hoaDonCho.size());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i láº¥y hÃ³a Ä‘Æ¡n chá»", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Táº¡o hÃ³a Ä‘Æ¡n chá» má»›i
         */
    //    @PostMapping("/hoa-don-cho/tao-moi")
    //    public ResponseEntity<Map<String, Object>> taoHoaDonCho(@RequestParam Integer nhanVienId) {
    //        try {
    //            logger.info("ðŸ†• [API] Táº¡o hÃ³a Ä‘Æ¡n chá» má»›i cho nhÃ¢n viÃªn ID: {}", nhanVienId);
    //            HoaDonChoResponse response = banHangService.taoHoaDonCho(nhanVienId);
    //
    //            Map<String, Object> result = new HashMap<>();
    //            result.put("success", true);
    //            result.put("message", "Táº¡o hÃ³a Ä‘Æ¡n chá» thÃ nh cÃ´ng");
    //            result.put("data", response);
    //
    //            logger.info("âœ… [API] Táº¡o hÃ³a Ä‘Æ¡n chá» thÃ nh cÃ´ng: {}", response.getMaHoaDon());
    //            return ResponseEntity.ok(result);
    //
    //        } catch (Exception e) {
    //            logger.error("âŒ [API] Lá»—i táº¡o hÃ³a Ä‘Æ¡n chá»", e);
    //            Map<String, Object> errorResponse = new HashMap<>();
    //            errorResponse.put("success", false);
    //            errorResponse.put("message", "Lá»—i: " + e.getMessage());
    //            return ResponseEntity.badRequest().body(errorResponse);
    //        }
    //    }

        /**
         * XÃ³a hÃ³a Ä‘Æ¡n chá»
         */
        @Transactional
        @DeleteMapping("/hoa-don-cho/{id}")
        public ResponseEntity<Map<String, Object>> xoaHoaDonCho(@PathVariable Integer id) {
            try {
                logger.info("ðŸ—‘ï¸ [API] XÃ³a hÃ³a Ä‘Æ¡n chá»: ID {}", id);
                banHangService.xoaHoaDonCho(id);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "XÃ³a hÃ³a Ä‘Æ¡n chá» thÃ nh cÃ´ng");

                logger.info("âœ… [API] XÃ³a hÃ³a Ä‘Æ¡n chá» thÃ nh cÃ´ng: ID {}", id);
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i xÃ³a hÃ³a Ä‘Æ¡n chá»: ID {}", id, e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * Láº¥y chi tiáº¿t hÃ³a Ä‘Æ¡n chá»
         */
        @Transactional
        @GetMapping("/hoa-don-cho/{id}")
        public ResponseEntity<Map<String, Object>> layChiTietHoaDonCho(@PathVariable Integer id) {
            try {
                logger.info("ðŸ” [API] Láº¥y chi tiáº¿t hÃ³a Ä‘Æ¡n chá»: ID {}", id);
                HoaDonChoDetailResponse response = banHangService.layChiTietHoaDonCho(id);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Láº¥y chi tiáº¿t hÃ³a Ä‘Æ¡n chá» thÃ nh cÃ´ng");
                result.put("data", response);

                logger.info("âœ… [API] Láº¥y chi tiáº¿t hÃ³a Ä‘Æ¡n chá» thÃ nh cÃ´ng: {}", response.getMaHoaDon());
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i láº¥y chi tiáº¿t hÃ³a Ä‘Æ¡n chá»: ID {}", id, e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Láº¥y tá»•ng quan hÃ³a Ä‘Æ¡n chá»
         */
        @Transactional
        @GetMapping("/hoa-don-cho/{id}/tong-quan")
        public ResponseEntity<Map<String, Object>> layTongQuanHoaDonCho(@PathVariable Integer id) {
            try {
                logger.info("ðŸ“Š [API] Láº¥y tá»•ng quan hÃ³a Ä‘Æ¡n chá»: ID {}", id);
                HoaDonChoTongQuanResponse response = banHangService.layTongQuanHoaDonCho(id);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Láº¥y tá»•ng quan hÃ³a Ä‘Æ¡n chá» thÃ nh cÃ´ng");
                result.put("data", response);

                logger.info("âœ… [API] Láº¥y tá»•ng quan hÃ³a Ä‘Æ¡n chá» thÃ nh cÃ´ng");
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i láº¥y tá»•ng quan hÃ³a Ä‘Æ¡n chá»: ID {}", id, e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        // ===== QUáº¢N LÃ Sáº¢N PHáº¨M =====

        /**
         * TÃ¬m kiáº¿m sáº£n pháº©m cho bÃ¡n hÃ ng
         */
        @Transactional(readOnly = true)
        @GetMapping("/san-pham")
        public ResponseEntity<Map<String, Object>> timKiemSanPham(
                @RequestParam(defaultValue = "") String keyword,
                @RequestParam(required = false) Integer danhMucId,
                @RequestParam(required = false) Integer thuongHieuId,
                @RequestParam(required = false) Integer mauSacId,
                @RequestParam(required = false) Integer kichCoId,
                @RequestParam(required = false) Integer chatLieuId,
                @RequestParam(required = false) Integer deGiayId,
                @RequestParam(required = false) Double minPrice,
                @RequestParam(required = false) Double maxPrice,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "20") int size,
                @RequestParam(defaultValue = "ngayTao") String sortBy,
                @RequestParam(defaultValue = "desc") String sortDir) {
            try {
                logger.info("ðŸ” [API] TÃ¬m kiáº¿m sáº£n pháº©m: keyword='{}', danhMucId={}", keyword, danhMucId);

                Sort sort = sortDir.equalsIgnoreCase("desc") ?
                        Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
                Pageable pageable = PageRequest.of(page, size, sort);

                // Táº¡o filter object
                SanPhamChiTietFilterRequest filter = SanPhamChiTietFilterRequest.builder()
                        .keyword(keyword)
                        .danhMucId(danhMucId)
                        .thuongHieuId(thuongHieuId)
                        .mauSacId(mauSacId)
                        .kichCoId(kichCoId)
                        .chatLieuId(chatLieuId)
                        .deGiayId(deGiayId)
                        .minPrice(minPrice)
                        .maxPrice(maxPrice)
                        .build();

                Page<SanPhamChiTietBanHangResponse> sanPhamPage = banHangService.timKiemSanPham(filter, pageable);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "TÃ¬m kiáº¿m sáº£n pháº©m thÃ nh cÃ´ng");
                response.put("data", sanPhamPage.getContent());
                response.put("currentPage", sanPhamPage.getNumber());
                response.put("totalElements", sanPhamPage.getTotalElements());
                response.put("totalPages", sanPhamPage.getTotalPages());
                response.put("size", sanPhamPage.getSize());

                logger.info("âœ… [API] TÃ¬m kiáº¿m {} sáº£n pháº©m thÃ nh cÃ´ng", sanPhamPage.getTotalElements());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i tÃ¬m kiáº¿m sáº£n pháº©m", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Láº¥y chi tiáº¿t sáº£n pháº©m
         */
        @Transactional
        @GetMapping("/san-pham/{id}")
        public ResponseEntity<Map<String, Object>> layChiTietSanPham(@PathVariable Integer id) {
            try {
                logger.info("ðŸ” [API] Láº¥y chi tiáº¿t sáº£n pháº©m: ID {}", id);
                SanPhamChiTietBanHangResponse sanPham = banHangService.layChiTietSanPham(id);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Láº¥y chi tiáº¿t sáº£n pháº©m thÃ nh cÃ´ng");
                response.put("data", sanPham);

                logger.info("âœ… [API] Láº¥y chi tiáº¿t sáº£n pháº©m thÃ nh cÃ´ng: {}", sanPham.getTenSanPham());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i láº¥y chi tiáº¿t sáº£n pháº©m: ID {}", id, e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Scan QR code sáº£n pháº©m
         */
        @Transactional
        @PostMapping("/san-pham/scan-qr")
        public ResponseEntity<Map<String, Object>> scanQRSanPham(@RequestBody @Valid ScanQRRequest request) {
            try {
                logger.info("ðŸ“± [API] Scan QR code: {}", request.getQrCode());
                ScanQRResponse response = banHangService.scanQRSanPham(request.getQrCode());

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Scan QR code thÃ nh cÃ´ng");
                result.put("data", response);

                logger.info("âœ… [API] Scan QR code thÃ nh cÃ´ng");
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i scan QR code", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * Láº¥y sáº£n pháº©m tÆ°Æ¡ng tá»±
         */
        @Transactional
        @GetMapping("/san-pham/{id}/tuong-tu")
        public ResponseEntity<Map<String, Object>> laySanPhamTuongTu(@PathVariable Integer id) {
            try {
                logger.info("ðŸ” [API] Láº¥y sáº£n pháº©m tÆ°Æ¡ng tá»±: ID {}", id);
                List<SanPhamChiTietBanHangResponse> sanPhamList = banHangService.laySanPhamTuongTu(id);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Láº¥y sáº£n pháº©m tÆ°Æ¡ng tá»± thÃ nh cÃ´ng");
                response.put("data", sanPhamList);
                response.put("total", sanPhamList.size());

                logger.info("âœ… [API] Láº¥y {} sáº£n pháº©m tÆ°Æ¡ng tá»± thÃ nh cÃ´ng", sanPhamList.size());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i láº¥y sáº£n pháº©m tÆ°Æ¡ng tá»±", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        // ===== QUáº¢N LÃ Sáº¢N PHáº¨M TRONG HÃ“A ÄÆ N =====

        /**
         * ThÃªm sáº£n pháº©m vÃ o hÃ³a Ä‘Æ¡n chá»
         */
        @Transactional
        @PostMapping("/hoa-don-cho/{id}/them-san-pham")
        public ResponseEntity<Map<String, Object>> themSanPhamVaoHoaDon(
                @PathVariable Integer id,
                @RequestBody @Valid ThemSanPhamRequest request) {
            try {
                logger.info("âž• [API] ThÃªm sáº£n pháº©m vÃ o hÃ³a Ä‘Æ¡n chá»: HD {} - SP {}",
                        id, request.getChiTietSanPhamId());

                HoaDonChoTongQuanResponse response = banHangService.themSanPhamVaoHoaDon(id, request);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "ThÃªm sáº£n pháº©m thÃ nh cÃ´ng");
                result.put("data", response);

                logger.info("âœ… [API] ThÃªm sáº£n pháº©m vÃ o hÃ³a Ä‘Æ¡n chá» thÃ nh cÃ´ng");
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i thÃªm sáº£n pháº©m vÃ o hÃ³a Ä‘Æ¡n chá»", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * Cáº­p nháº­t sáº£n pháº©m trong hÃ³a Ä‘Æ¡n chá»
         */
        @Transactional
        @PutMapping("/hoa-don-cho/{hoaDonId}/cap-nhat-san-pham/{chiTietId}")
        public ResponseEntity<Map<String, Object>> capNhatSanPhamTrongHoaDon(
                @PathVariable Integer hoaDonId,
                @PathVariable Integer chiTietId,
                @RequestBody @Valid CapNhatSanPhamRequest request) {
            try {
                logger.info("âœï¸ [API] Cáº­p nháº­t sáº£n pháº©m trong hÃ³a Ä‘Æ¡n chá»: HD {} - CT {}",
                        hoaDonId, chiTietId);

                HoaDonChoTongQuanResponse response = banHangService.capNhatSanPhamTrongHoaDon(hoaDonId, chiTietId, request);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Cáº­p nháº­t sáº£n pháº©m thÃ nh cÃ´ng");
                result.put("data", response);

                logger.info("âœ… [API] Cáº­p nháº­t sáº£n pháº©m trong hÃ³a Ä‘Æ¡n chá» thÃ nh cÃ´ng");
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i cáº­p nháº­t sáº£n pháº©m trong hÃ³a Ä‘Æ¡n chá»", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * XÃ³a sáº£n pháº©m khá»i hÃ³a Ä‘Æ¡n chá»
         */
        @Transactional
        @DeleteMapping("/hoa-don-cho/{hoaDonId}/xoa-san-pham/{chiTietId}")
        public ResponseEntity<Map<String, Object>> xoaSanPhamKhoiHoaDon(
                @PathVariable Integer hoaDonId,
                @PathVariable Integer chiTietId) {
            try {
                logger.info("ðŸ—‘ï¸ [API] XÃ³a sáº£n pháº©m khá»i hÃ³a Ä‘Æ¡n chá»: HD {} - CT {}",
                        hoaDonId, chiTietId);

                HoaDonChoTongQuanResponse response = banHangService.xoaSanPhamKhoiHoaDon(hoaDonId, chiTietId);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "XÃ³a sáº£n pháº©m thÃ nh cÃ´ng");
                result.put("data", response);

                logger.info("âœ… [API] XÃ³a sáº£n pháº©m khá»i hÃ³a Ä‘Æ¡n chá» thÃ nh cÃ´ng");
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i xÃ³a sáº£n pháº©m khá»i hÃ³a Ä‘Æ¡n chá»", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * TÃ­nh giÃ¡ sáº£n pháº©m
         */
        @Transactional
        @PostMapping("/san-pham/tinh-gia")
        public ResponseEntity<Map<String, Object>> tinhGiaSanPham(@RequestBody @Valid TinhGiaRequest request) {
            try {
                logger.info("ðŸ’° [API] TÃ­nh giÃ¡ sáº£n pháº©m");
                TinhGiaResponse response = banHangService.tinhGiaSanPham(request);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "TÃ­nh giÃ¡ sáº£n pháº©m thÃ nh cÃ´ng");
                result.put("data", response);

                logger.info("âœ… [API] TÃ­nh giÃ¡ sáº£n pháº©m thÃ nh cÃ´ng");
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i tÃ­nh giÃ¡ sáº£n pháº©m", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        // ===== QUáº¢N LÃ KHÃCH HÃ€NG =====

        /**
         * TÃ¬m kiáº¿m khÃ¡ch hÃ ng
         */
        @GetMapping("/khach-hang/search")
        public ResponseEntity<Map<String, Object>> timKiemKhachHang(
                @RequestParam(defaultValue = "") String keyword,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size,
                @RequestParam(defaultValue = "ngayTao") String sortBy,
                @RequestParam(defaultValue = "desc") String sortDir) {
            try {
                logger.info("ðŸ” [API] TÃ¬m kiáº¿m khÃ¡ch hÃ ng: keyword='{}', page={}, size={}", keyword, page, size);

                Sort sort = sortDir.equalsIgnoreCase("desc") ?
                        Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
                Pageable pageable = PageRequest.of(page, size, sort);

                Page<KhachHangResponse> khachHangPage = banHangService.timKiemKhachHang(keyword, pageable);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "TÃ¬m kiáº¿m khÃ¡ch hÃ ng thÃ nh cÃ´ng");
                response.put("data", khachHangPage.getContent());
                response.put("currentPage", khachHangPage.getNumber());
                response.put("totalElements", khachHangPage.getTotalElements());
                response.put("totalPages", khachHangPage.getTotalPages());
                response.put("size", khachHangPage.getSize());

                logger.info("âœ… [API] TÃ¬m kiáº¿m khÃ¡ch hÃ ng thÃ nh cÃ´ng: {} káº¿t quáº£", khachHangPage.getTotalElements());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i tÃ¬m kiáº¿m khÃ¡ch hÃ ng", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Táº¡o khÃ¡ch hÃ ng nhanh
         */
        @Transactional
        @PostMapping("/khach-hang/tao-nhanh")
        public ResponseEntity<Map<String, Object>> taoKhachHangNhanh(@RequestBody @Valid TaoKhachHangNhanhRequest request) {
            try {
                logger.info("ðŸ‘¤ [API] Táº¡o khÃ¡ch hÃ ng nhanh: {}", request.getHoTen());
                KhachHangResponse response = banHangService.taoKhachHangNhanh(request);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Táº¡o khÃ¡ch hÃ ng thÃ nh cÃ´ng");
                result.put("data", response);

                logger.info("âœ… [API] Táº¡o khÃ¡ch hÃ ng nhanh thÃ nh cÃ´ng: {}", response.getHoTen());
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i táº¡o khÃ¡ch hÃ ng nhanh", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * Láº¥y thÃ´ng tin khÃ¡ch hÃ ng
         */
        @Transactional
        @GetMapping("/khach-hang/{id}")
        public ResponseEntity<Map<String, Object>> layThongTinKhachHang(@PathVariable Integer id) {
            try {
                logger.info("ðŸ” [API] Láº¥y thÃ´ng tin khÃ¡ch hÃ ng: ID {}", id);
                KhachHangDetailResponse response = banHangService.layThongTinKhachHang(id);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Láº¥y thÃ´ng tin khÃ¡ch hÃ ng thÃ nh cÃ´ng");
                result.put("data", response);

                logger.info("âœ… [API] Láº¥y thÃ´ng tin khÃ¡ch hÃ ng thÃ nh cÃ´ng: {}", response.getHoTen());
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i láº¥y thÃ´ng tin khÃ¡ch hÃ ng: ID {}", id, e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Ãp dá»¥ng khÃ¡ch hÃ ng cho hÃ³a Ä‘Æ¡n
         */
        @Transactional
        @PostMapping("/hoa-don-cho/{hoaDonId}/ap-dung-khach-hang/{khachHangId}")
        public ResponseEntity<Map<String, Object>> apDungKhachHang(
                @PathVariable Integer hoaDonId,
                @PathVariable Integer khachHangId) {
            try {
                logger.info("ðŸ‘¤ [API] Ãp dá»¥ng khÃ¡ch hÃ ng ID {} cho hÃ³a Ä‘Æ¡n ID {}", khachHangId, hoaDonId);

                HoaDonChoTongQuanResponse response = banHangService.apDungKhachHang(hoaDonId, khachHangId);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Ãp dá»¥ng khÃ¡ch hÃ ng thÃ nh cÃ´ng");
                result.put("data", response);

                logger.info("âœ… [API] Ãp dá»¥ng khÃ¡ch hÃ ng thÃ nh cÃ´ng");
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i Ã¡p dá»¥ng khÃ¡ch hÃ ng", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * Bá» khÃ¡ch hÃ ng khá»i hÃ³a Ä‘Æ¡n
         */
        @Transactional
        @DeleteMapping("/hoa-don-cho/{hoaDonId}/bo-khach-hang")
        public ResponseEntity<Map<String, Object>> boKhachHang(@PathVariable Integer hoaDonId) {
            try {
                logger.info("ðŸ—‘ï¸ [API] Bá» khÃ¡ch hÃ ng khá»i hÃ³a Ä‘Æ¡n ID {}", hoaDonId);

                HoaDonChoTongQuanResponse response = banHangService.boKhachHang(hoaDonId);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Bá» khÃ¡ch hÃ ng thÃ nh cÃ´ng");
                result.put("data", response);

                logger.info("âœ… [API] Bá» khÃ¡ch hÃ ng thÃ nh cÃ´ng");
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i bá» khÃ¡ch hÃ ng", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        // ===== QUáº¢N LÃ VOUCHER =====

        /**
         * Láº¥y danh sÃ¡ch voucher kháº£ dá»¥ng
         */
        @Transactional
        @GetMapping("/voucher/kha-dung")
        public ResponseEntity<Map<String, Object>> layDanhSachVoucherKhaDung(
                @RequestParam(required = false) Integer khachHangId,
                @RequestParam(required = false) Double tongTien) {
            try {
                logger.info("ðŸŽ« [API] Láº¥y voucher kháº£ dá»¥ng: khachHangId={}, tongTien={}", khachHangId, tongTien);
                List<VoucherResponse> vouchers = banHangService.layDanhSachVoucherKhaDung(khachHangId, tongTien);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Láº¥y voucher kháº£ dá»¥ng thÃ nh cÃ´ng");
                response.put("data", vouchers);
                response.put("total", vouchers.size());

                logger.info("âœ… [API] Láº¥y {} voucher kháº£ dá»¥ng thÃ nh cÃ´ng", vouchers.size());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i láº¥y voucher kháº£ dá»¥ng", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Kiá»ƒm tra voucher
         */
        @Transactional
        @PostMapping("/voucher/kiem-tra")
        public ResponseEntity<VoucherValidationResponse> kiemTraVoucher(@RequestBody @Valid ValidateVoucherRequest request) {
            try {
                logger.info("âœ… [API] Kiá»ƒm tra voucher: {}", request.getMaVoucher());
                VoucherValidationResponse response = banHangService.kiemTraVoucher(request);

                logger.info("âœ… [API] Kiá»ƒm tra voucher thÃ nh cÃ´ng: valid={}", response.getValid());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i kiá»ƒm tra voucher", e);
                return ResponseEntity.badRequest().body(
                        VoucherValidationResponse.builder()
                                .valid(false)
                                .message("Lá»—i há»‡ thá»‘ng: " + e.getMessage())
                                .build()
                );
            }
        }

        /**
         * Ãp dá»¥ng voucher vÃ o hÃ³a Ä‘Æ¡n
         */
        @Transactional
        @PostMapping("/hoa-don-cho/{hoaDonId}/ap-dung-voucher/{voucherId}")
        public ResponseEntity<Map<String, Object>> apDungVoucher(
                @PathVariable Integer hoaDonId,
                @PathVariable Integer voucherId) {
            try {
                logger.info("ðŸŽ« [API] Ãp dá»¥ng voucher ID {} cho hÃ³a Ä‘Æ¡n ID {}", voucherId, hoaDonId);

                HoaDonChoTongQuanResponse response = banHangService.apDungVoucher(hoaDonId, voucherId);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Ãp dá»¥ng voucher thÃ nh cÃ´ng");
                result.put("data", response);

                logger.info("âœ… [API] Ãp dá»¥ng voucher thÃ nh cÃ´ng");
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i Ã¡p dá»¥ng voucher", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * Bá» voucher khá»i hÃ³a Ä‘Æ¡n
         */
        @Transactional
        @DeleteMapping("/hoa-don-cho/{hoaDonId}/bo-voucher")
        public ResponseEntity<Map<String, Object>> boVoucher(@PathVariable Integer hoaDonId) {
            try {
                logger.info("ðŸ—‘ï¸ [API] Bá» voucher khá»i hÃ³a Ä‘Æ¡n ID {}", hoaDonId);

                HoaDonChoTongQuanResponse response = banHangService.boVoucher(hoaDonId);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Bá» voucher thÃ nh cÃ´ng");
                result.put("data", response);

                logger.info("âœ… [API] Bá» voucher thÃ nh cÃ´ng");
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i bá» voucher", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        // ===== THANH TOÃN =====

        /**
         * Thanh toÃ¡n hÃ³a Ä‘Æ¡n chá»
         */
        @Transactional
        @PostMapping("/hoa-don-cho/{id}/thanh-toan")
        public ResponseEntity<Map<String, Object>> thanhToanHoaDon(
                @PathVariable Integer id,
                @RequestBody @Valid ThanhToanRequest request) {
            try {
                logger.info("ðŸ’° [API] Thanh toÃ¡n hÃ³a Ä‘Æ¡n chá»: ID {} - PhÆ°Æ¡ng thá»©c: {}", id, request.getPhuongThucThanhToan());
                HoaDonResponse response = banHangService.thanhToanHoaDon(id, request);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Thanh toÃ¡n thÃ nh cÃ´ng");
                result.put("data", response);

                logger.info("âœ… [API] Thanh toÃ¡n hÃ³a Ä‘Æ¡n thÃ nh cÃ´ng: {}", response.getMaHoaDon());
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i thanh toÃ¡n hÃ³a Ä‘Æ¡n: ID {}", id, e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * Kiá»ƒm tra tá»“n kho trÆ°á»›c khi thanh toÃ¡n
         */
//        @GetMapping("/hoa-don-cho/{hoaDonId}/kiem-tra-ton-kho")
//        public ResponseEntity<Map<String, Object>> kiemTraTonKho(@PathVariable Integer hoaDonId) {
//            try {
//                logger.info("ðŸ“¦ [API] Kiá»ƒm tra tá»“n kho hÃ³a Ä‘Æ¡n: ID {}", hoaDonId);
//
//                // Validate input
//                if (hoaDonId == null || hoaDonId <= 0) {
//                    Map<String, Object> errorResponse = new HashMap<>();
//                    errorResponse.put("success", false);
//                    errorResponse.put("message", "ID hÃ³a Ä‘Æ¡n khÃ´ng há»£p lá»‡");
//                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//                }
//
//                // Kiá»ƒm tra hÃ³a Ä‘Æ¡n tá»“n táº¡i
//                boolean hoaDonExists = hoaDonRepository.existsById(hoaDonId);
//                if (!hoaDonExists) {
//                    Map<String, Object> errorResponse = new HashMap<>();
//                    errorResponse.put("success", false);
//                    errorResponse.put("message", "KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n vá»›i ID: " + hoaDonId);
//                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
//                }
//
//                // Gá»i service vá»›i tÃªn method má»›i
//                List<InventoryCheckResponse> responses = banHangService.kiemTraTonKhoTruocThanhToan(hoaDonId);
//
//                Map<String, Object> result = new HashMap<>();
//                result.put("success", true);
//                result.put("message", "Kiá»ƒm tra tá»“n kho thÃ nh cÃ´ng");
//                result.put("data", responses);
//
//                // ThÃªm thÃ´ng tin tá»•ng quan
//                long totalItems = responses.size();
//                long availableItems = responses.stream().filter(InventoryCheckResponse::getCoTheban).count();
//                long unavailableItems = totalItems - availableItems;
//
//                result.put("summary", Map.of(
//                        "tongSoSanPham", totalItems,
//                        "sanPhamDuHang", availableItems,
//                        "sanPhamThieuHang", unavailableItems,
//                        "coTheThanhToan", unavailableItems == 0
//                ));
//
//                logger.info("âœ… [API] Kiá»ƒm tra tá»“n kho thÃ nh cÃ´ng: {}/{} sáº£n pháº©m Ä‘á»§ hÃ ng",
//                        availableItems, totalItems);
//                return ResponseEntity.ok(result);
//
//            } catch (RuntimeException e) {
//                logger.error("âŒ [API] Lá»—i nghiá»‡p vá»¥ khi kiá»ƒm tra tá»“n kho: ID {}, Error: {}", hoaDonId, e.getMessage());
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("message", e.getMessage());
//                errorResponse.put("hoaDonId", hoaDonId);
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//
//            } catch (Exception e) {
//                logger.error("âŒ [API] Lá»—i há»‡ thá»‘ng khi kiá»ƒm tra tá»“n kho: ID {}", hoaDonId, e);
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("message", "Lá»—i há»‡ thá»‘ng: KhÃ´ng thá»ƒ kiá»ƒm tra tá»“n kho");
//                errorResponse.put("error", "SYSTEM_ERROR");
//                errorResponse.put("hoaDonId", hoaDonId);
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//            }
//        }

        // ===== THá»NG KÃŠ =====

        /**
         * Láº¥y thá»‘ng kÃª bÃ¡n hÃ ng trong ngÃ y
         */
        @Transactional
        @GetMapping("/thong-ke/ban-hang-trong-ngay")
        public ResponseEntity<Map<String, Object>> layThongKeBanHangTrongNgay() {
            try {
                logger.info("ðŸ“Š [API] Láº¥y thá»‘ng kÃª bÃ¡n hÃ ng trong ngÃ y");
                Map<String, Object> thongKe = banHangService.layThongKeBanHangTrongNgay();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Láº¥y thá»‘ng kÃª thÃ nh cÃ´ng");
                response.put("data", thongKe);

                logger.info("âœ… [API] Láº¥y thá»‘ng kÃª bÃ¡n hÃ ng trong ngÃ y thÃ nh cÃ´ng");
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i láº¥y thá»‘ng kÃª bÃ¡n hÃ ng", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Láº¥y sáº£n pháº©m bÃ¡n cháº¡y
         */
        @Transactional
        @GetMapping("/thong-ke/san-pham-ban-chay")
        public ResponseEntity<Map<String, Object>> laySanPhamBanChay(
                @RequestParam(defaultValue = "10") int limit) {
            try {
                logger.info("ðŸ“Š [API] Láº¥y sáº£n pháº©m bÃ¡n cháº¡y: limit={}", limit);
                List<Map<String, Object>> sanPhamBanChay = banHangService.laySanPhamBanChay(limit);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Láº¥y sáº£n pháº©m bÃ¡n cháº¡y thÃ nh cÃ´ng");
                response.put("data", sanPhamBanChay);
                response.put("total", sanPhamBanChay.size());

                logger.info("âœ… [API] Láº¥y {} sáº£n pháº©m bÃ¡n cháº¡y thÃ nh cÃ´ng", sanPhamBanChay.size());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i láº¥y sáº£n pháº©m bÃ¡n cháº¡y", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Láº¥y thá»‘ng kÃª doanh thu
         */
        @Transactional
        @GetMapping("/thong-ke/doanh-thu")
        public ResponseEntity<Map<String, Object>> layThongKeDoanhThu(
                @RequestParam String tuNgay,
                @RequestParam String denNgay) {
            try {
                logger.info("ðŸ“Š [API] Láº¥y thá»‘ng kÃª doanh thu: tá»« {} Ä‘áº¿n {}", tuNgay, denNgay);
                Map<String, Object> thongKe = banHangService.layThongKeDoanhThu(tuNgay, denNgay);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Láº¥y thá»‘ng kÃª doanh thu thÃ nh cÃ´ng");
                response.put("data", thongKe);

                logger.info("âœ… [API] Láº¥y thá»‘ng kÃª doanh thu thÃ nh cÃ´ng");
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i láº¥y thá»‘ng kÃª doanh thu", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        // ===== UTILITY ENDPOINTS =====

        /**
         * Health check endpoint
         */
        @Transactional
        @GetMapping("/health")
        public ResponseEntity<Map<String, Object>> healthCheck() {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "OK");
            response.put("service", "BanHang API");
            response.put("timestamp", new java.util.Date());
            return ResponseEntity.ok(response);
        }

        // ===== EXCEPTION HANDLERS =====

        // Helper method

        /**
         * Exception handler cho cÃ¡c lá»—i chung
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
            logger.error("âŒ Lá»—i chung: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", ex.getMessage()));
        }

        // ThÃªm cÃ¡c endpoint sau vÃ o BanHangController

        @GetMapping("/san-pham/{id}/debug-hinh-anh")
        public ResponseEntity<Map<String, Object>> debugHinhAnhSanPham(@PathVariable Integer id) {
            try {
                logger.info("ðŸ–¼ï¸ [API] Debug hÃ¬nh áº£nh sáº£n pháº©m: ID {}", id);

                // Láº¥y thÃ´ng tin sáº£n pháº©m chi tiáº¿t
                SanPhamChiTietBanHangResponse sanPham = banHangService.layChiTietSanPham(id);

                Map<String, Object> debugInfo = new HashMap<>();
                debugInfo.put("id", sanPham.getId());
                debugInfo.put("tenSanPham", sanPham.getTenSanPham());

                // Debug hÃ¬nh áº£nh giá»‘ng quáº£n lÃ½ sáº£n pháº©m
                if (sanPham.getDanhSachHinhAnh() != null && !sanPham.getDanhSachHinhAnh().isEmpty()) {
                    List<Map<String, Object>> danhSachHinhAnh = new ArrayList<>();

                    for (HinhAnhResponse hinhAnh : sanPham.getDanhSachHinhAnh()) {
                        Map<String, Object> hinhAnhInfo = new HashMap<>();
                        hinhAnhInfo.put("duongDanGoc", hinhAnh.getDuongDan());
                        hinhAnhInfo.put("urlDayDu", hinhAnh.getUrlHinhAnh());
                        hinhAnhInfo.put("maHinhAnh", hinhAnh.getMaHinhAnh());
                        hinhAnhInfo.put("tenHinhAnh", hinhAnh.getTenHinhAnh());
                        hinhAnhInfo.put("trangThai", hinhAnh.getTrangThai());
                        hinhAnhInfo.put("laHinhChinh", hinhAnh.getLaHinhChinh());
                        danhSachHinhAnh.add(hinhAnhInfo);
                    }

                    debugInfo.put("danhSachHinhAnh", danhSachHinhAnh);
                    debugInfo.put("soLuongHinhAnh", sanPham.getDanhSachHinhAnh().size());
                    debugInfo.put("hinhAnhChinh", sanPham.getHinhAnhChinh());
                } else {
                    debugInfo.put("danhSachHinhAnh", new ArrayList<>());
                    debugInfo.put("soLuongHinhAnh", 0);
                    debugInfo.put("hinhAnhChinh", null);
                }

                logger.info("âœ… [API] Debug hÃ¬nh áº£nh sáº£n pháº©m thÃ nh cÃ´ng");
                return ResponseEntity.ok(debugInfo);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i debug hÃ¬nh áº£nh sáº£n pháº©m: ID {}", id, e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Láº¥y hÃ¬nh áº£nh Ä‘Ã£ format URL cho frontend
         */
        @GetMapping("/san-pham/{id}/hinh-anh")
        public ResponseEntity<Map<String, Object>> layHinhAnhSanPham(@PathVariable Integer id) {
            try {
                logger.info("ðŸ–¼ï¸ [API] Láº¥y hÃ¬nh áº£nh sáº£n pháº©m: ID {}", id);

                SanPhamChiTietBanHangResponse sanPham = banHangService.layChiTietSanPham(id);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("sanPhamId", id);

                // Sá»­ dá»¥ng cáº¥u trÃºc giá»‘ng quáº£n lÃ½ sáº£n pháº©m
                if (sanPham.getDanhSachHinhAnh() != null && !sanPham.getDanhSachHinhAnh().isEmpty()) {
                    response.put("danhSachHinhAnh", sanPham.getDanhSachHinhAnh());
                    response.put("hinhAnhChinh", sanPham.getHinhAnhChinh());
                    response.put("soLuongHinhAnh", sanPham.getDanhSachHinhAnh().size());
                    response.put("message", "Láº¥y hÃ¬nh áº£nh thÃ nh cÃ´ng");
                } else {
                    response.put("danhSachHinhAnh", new ArrayList<>());
                    response.put("hinhAnhChinh", null);
                    response.put("soLuongHinhAnh", 0);
                    response.put("message", "Sáº£n pháº©m chÆ°a cÃ³ hÃ¬nh áº£nh");
                }

                logger.info("âœ… [API] Láº¥y hÃ¬nh áº£nh sáº£n pháº©m thÃ nh cÃ´ng");
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i láº¥y hÃ¬nh áº£nh sáº£n pháº©m: ID {}", id, e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Kiá»ƒm tra URL hÃ¬nh áº£nh cÃ³ accessible khÃ´ng
         */
        @Transactional
        @GetMapping("/san-pham/{id}/kiem-tra-hinh-anh")
        public ResponseEntity<Map<String, Object>> kiemTraHinhAnh(@PathVariable Integer id) {
            try {
                logger.info("ðŸ” [API] Kiá»ƒm tra hÃ¬nh áº£nh sáº£n pháº©m: ID {}", id);

                SanPhamChiTietBanHangResponse sanPham = banHangService.layChiTietSanPham(id);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("sanPhamId", id);

                // Sá»­ dá»¥ng cáº¥u trÃºc giá»‘ng quáº£n lÃ½ sáº£n pháº©m
                if (sanPham.getDanhSachHinhAnh() != null && !sanPham.getDanhSachHinhAnh().isEmpty()) {
                    HinhAnhResponse hinhAnh = sanPham.getDanhSachHinhAnh().get(0);

                    // PhÃ¢n tÃ­ch URL
                    String duongDanGoc = hinhAnh.getDuongDan();
                    String cleanPath = duongDanGoc != null ? duongDanGoc
                            .replace("/images/", "")
                            .replace("/hinh-anh/images/", "") : "";
                    String urlDayDu = hinhAnh.getUrlHinhAnh();

                    response.put("duongDanGoc", duongDanGoc);
                    response.put("duongDanSauChinh", cleanPath);
                    response.put("urlDayDu", urlDayDu);
                    response.put("coHinhAnh", true);
                    response.put("message", "Sáº£n pháº©m cÃ³ hÃ¬nh áº£nh");
                } else {
                    response.put("coHinhAnh", false);
                    response.put("message", "Sáº£n pháº©m chÆ°a cÃ³ hÃ¬nh áº£nh");
                }

                logger.info("âœ… [API] Kiá»ƒm tra hÃ¬nh áº£nh sáº£n pháº©m thÃ nh cÃ´ng");
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i kiá»ƒm tra hÃ¬nh áº£nh sáº£n pháº©m: ID {}", id, e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }
        // ThÃªm cÃ¡c endpoint nÃ y vÃ o BanHangController

    // ===== MASTER DATA ENDPOINTS =====

        /**
         * Láº¥y danh sÃ¡ch danh má»¥c
         */
        @Transactional
        @GetMapping("/master-data/danh-muc")
        public ResponseEntity<Map<String, Object>> layDanhSachDanhMuc() {
            try {
                logger.info("ðŸ“‚ [API] Láº¥y danh sÃ¡ch danh má»¥c");
                List<DanhMucResponse> danhMucList = banHangService.layDanhSachDanhMuc();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Láº¥y danh sÃ¡ch danh má»¥c thÃ nh cÃ´ng");
                response.put("data", danhMucList);
                response.put("total", danhMucList.size());

                logger.info("âœ… [API] Láº¥y {} danh má»¥c thÃ nh cÃ´ng", danhMucList.size());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i láº¥y danh sÃ¡ch danh má»¥c", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Láº¥y danh sÃ¡ch thÆ°Æ¡ng hiá»‡u
         */
        @Transactional
        @GetMapping("/master-data/thuong-hieu")
        public ResponseEntity<Map<String, Object>> layDanhSachThuongHieu() {
            try {
                logger.info("ðŸ·ï¸ [API] Láº¥y danh sÃ¡ch thÆ°Æ¡ng hiá»‡u");
                List<ThuongHieuResponse> thuongHieuList = banHangService.layDanhSachThuongHieu();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Láº¥y danh sÃ¡ch thÆ°Æ¡ng hiá»‡u thÃ nh cÃ´ng");
                response.put("data", thuongHieuList);
                response.put("total", thuongHieuList.size());

                logger.info("âœ… [API] Láº¥y {} thÆ°Æ¡ng hiá»‡u thÃ nh cÃ´ng", thuongHieuList.size());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i láº¥y danh sÃ¡ch thÆ°Æ¡ng hiá»‡u", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Láº¥y danh sÃ¡ch mÃ u sáº¯c
         */
        @Transactional
        @GetMapping("/master-data/mau-sac")
        public ResponseEntity<Map<String, Object>> layDanhSachMauSac() {
            try {
                logger.info("ðŸŽ¨ [API] Láº¥y danh sÃ¡ch mÃ u sáº¯c");
                List<MauSacResponse> mauSacList = banHangService.layDanhSachMauSac();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Láº¥y danh sÃ¡ch mÃ u sáº¯c thÃ nh cÃ´ng");
                response.put("data", mauSacList);
                response.put("total", mauSacList.size());

                logger.info("âœ… [API] Láº¥y {} mÃ u sáº¯c thÃ nh cÃ´ng", mauSacList.size());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i láº¥y danh sÃ¡ch mÃ u sáº¯c", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Láº¥y danh sÃ¡ch kÃ­ch cá»¡
         */
        @Transactional
        @GetMapping("/master-data/kich-co")
        public ResponseEntity<Map<String, Object>> layDanhSachKichCo() {
            try {
                logger.info("ðŸ“ [API] Láº¥y danh sÃ¡ch kÃ­ch cá»¡");
                List<KichCoResponse> kichCoList = banHangService.layDanhSachKichCo();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Láº¥y danh sÃ¡ch kÃ­ch cá»¡ thÃ nh cÃ´ng");
                response.put("data", kichCoList);
                response.put("total", kichCoList.size());

                logger.info("âœ… [API] Láº¥y {} kÃ­ch cá»¡ thÃ nh cÃ´ng", kichCoList.size());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i láº¥y danh sÃ¡ch kÃ­ch cá»¡", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Láº¥y danh sÃ¡ch cháº¥t liá»‡u
         */
        @Transactional
        @GetMapping("/master-data/chat-lieu")
        public ResponseEntity<Map<String, Object>> layDanhSachChatLieu() {
            try {
                logger.info("ðŸ§µ [API] Láº¥y danh sÃ¡ch cháº¥t liá»‡u");
                List<ChatLieuResponse> chatLieuList = banHangService.layDanhSachChatLieu();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Láº¥y danh sÃ¡ch cháº¥t liá»‡u thÃ nh cÃ´ng");
                response.put("data", chatLieuList);
                response.put("total", chatLieuList.size());

                logger.info("âœ… [API] Láº¥y {} cháº¥t liá»‡u thÃ nh cÃ´ng", chatLieuList.size());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i láº¥y danh sÃ¡ch cháº¥t liá»‡u", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Láº¥y danh sÃ¡ch Ä‘áº¿ giÃ y
         */
        @Transactional
        @GetMapping("/master-data/de-giay")
        public ResponseEntity<Map<String, Object>> layDanhSachDeGiay() {
            try {
                logger.info("ðŸ‘Ÿ [API] Láº¥y danh sÃ¡ch Ä‘áº¿ giÃ y");
                List<DeGiayResponse> deGiayList = banHangService.layDanhSachDeGiay();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Láº¥y danh sÃ¡ch Ä‘áº¿ giÃ y thÃ nh cÃ´ng");
                response.put("data", deGiayList);
                response.put("total", deGiayList.size());

                logger.info("âœ… [API] Láº¥y {} Ä‘áº¿ giÃ y thÃ nh cÃ´ng", deGiayList.size());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i láº¥y danh sÃ¡ch Ä‘áº¿ giÃ y", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Láº¥y táº¥t cáº£ master data trong má»™t láº§n gá»i
         */
        @Transactional
        @GetMapping("/master-data/all")
        public ResponseEntity<Map<String, Object>> layTatCaMasterData() {
            try {
                logger.info("ðŸ“¦ [API] Láº¥y táº¥t cáº£ master data");

                Map<String, Object> allMasterData = new HashMap<>();
                allMasterData.put("danhMuc", banHangService.layDanhSachDanhMuc());
                allMasterData.put("thuongHieu", banHangService.layDanhSachThuongHieu());
                allMasterData.put("mauSac", banHangService.layDanhSachMauSac());
                allMasterData.put("kichCo", banHangService.layDanhSachKichCo());
                allMasterData.put("chatLieu", banHangService.layDanhSachChatLieu());
                allMasterData.put("deGiay", banHangService.layDanhSachDeGiay());

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Láº¥y táº¥t cáº£ master data thÃ nh cÃ´ng");
                response.put("data", allMasterData);

                logger.info("âœ… [API] Láº¥y táº¥t cáº£ master data thÃ nh cÃ´ng");
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i láº¥y táº¥t cáº£ master data", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }
        @Transactional
        @PostMapping("/hoa-don-cho/tao-moi")
        public ResponseEntity<Map<String, Object>> taoHoaDonCho(@RequestParam("nhanVienId") String nhanVienIdStr) {
            try {
                logger.info("ðŸ†• [API] Táº¡o hÃ³a Ä‘Æ¡n chá» má»›i cho nhÃ¢n viÃªn: '{}'", nhanVienIdStr);

                Integer nhanVienId = null;

                // Kiá»ƒm tra xem cÃ³ pháº£i lÃ  sá»‘ khÃ´ng
                try {
                    nhanVienId = Integer.parseInt(nhanVienIdStr);
                    logger.info("âœ… Sá»­ dá»¥ng ID nhÃ¢n viÃªn trá»±c tiáº¿p: {}", nhanVienId);

                } catch (NumberFormatException e) {
                    // Náº¿u khÃ´ng pháº£i sá»‘, sá»­ dá»¥ng method linh hoáº¡t
                    logger.info("ðŸ”„ TÃ¬m ID nhÃ¢n viÃªn tá»« mÃ£: '{}'", nhanVienIdStr);

                    // âœ… Sá»¬A: Sá»­ dá»¥ng method linh hoáº¡t má»›i
                    nhanVienId = banHangService.timNhanVienIdLinhHoat(nhanVienIdStr);

                    if (nhanVienId == null) {
                        // Debug thÃ´ng tin
                        String maNhanVien = banHangService.chuyenDoiMaTaiKhoanSangMaNhanVien(nhanVienIdStr);

                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("success", false);
                        errorResponse.put("message", "KhÃ´ng tÃ¬m tháº¥y nhÃ¢n viÃªn vá»›i mÃ£: " + nhanVienIdStr);
                        errorResponse.put("debug", Map.of(
                                "inputMa", nhanVienIdStr,
                                "maNhanVienTuongUng", maNhanVien != null ? maNhanVien : "null",
                                "loaiTimKiem", "TÃ¬m theo mÃ£ tÃ i khoáº£n vÃ  mÃ£ nhÃ¢n viÃªn"
                        ));

                        logger.warn("âŒ KhÃ´ng tÃ¬m tháº¥y nhÃ¢n viÃªn: input='{}', maNV='{}'",
                                nhanVienIdStr, maNhanVien);

                        return ResponseEntity.badRequest().body(errorResponse);
                    } else {
                        logger.info("âœ… TÃ¬m tháº¥y nhÃ¢n viÃªn ID: {}", nhanVienId);
                    }
                }

                // Táº¡o hÃ³a Ä‘Æ¡n vá»›i ID nhÃ¢n viÃªn Ä‘Ã£ tÃ¬m Ä‘Æ°á»£c
                HoaDonChoResponse response = banHangService.taoHoaDonCho(nhanVienId);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Táº¡o hÃ³a Ä‘Æ¡n chá» thÃ nh cÃ´ng");
                result.put("data", response);
                result.put("debug", Map.of(
                        "inputMa", nhanVienIdStr,
                        "nhanVienId", nhanVienId,
                        "maHoaDon", response.getMaHoaDon()
                ));

                logger.info("âœ… [API] Táº¡o hÃ³a Ä‘Æ¡n chá» thÃ nh cÃ´ng: {}", response.getMaHoaDon());
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i táº¡o hÃ³a Ä‘Æ¡n chá»", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                errorResponse.put("inputMa", nhanVienIdStr);
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }
        @Transactional
        @GetMapping("/debug/chuyen-doi-ma/{ma}")
        public ResponseEntity<Map<String, Object>> debugChuyenDoiMa(@PathVariable String ma) {
            try {
                logger.info("ðŸ” [DEBUG] Test chuyá»ƒn Ä‘á»•i mÃ£: '{}'", ma);

                Map<String, Object> result = new HashMap<>();
                result.put("inputMa", ma);

                // Test chuyá»ƒn Ä‘á»•i mÃ£ tÃ i khoáº£n -> mÃ£ nhÃ¢n viÃªn
                String maNhanVien = banHangService.chuyenDoiMaTaiKhoanSangMaNhanVien(ma);
                result.put("maNhanVienFromMaTaiKhoan", maNhanVien);

                // Test tÃ¬m ID nhÃ¢n viÃªn linh hoáº¡t
                Integer nhanVienId = banHangService.timNhanVienIdLinhHoat(ma);
                result.put("nhanVienIdLinhHoat", nhanVienId);

                // Test tÃ¬m theo mÃ£ nhÃ¢n viÃªn trá»±c tiáº¿p
                Integer nhanVienIdByMaNV = banHangService.timNhanVienIdTheoMa(ma);
                result.put("nhanVienIdByMaNV", nhanVienIdByMaNV);

                // Láº¥y thÃ´ng tin nhÃ¢n viÃªn náº¿u tÃ¬m tháº¥y
                if (nhanVienId != null) {
                    // CÃ³ thá»ƒ thÃªm thÃ´ng tin chi tiáº¿t nhÃ¢n viÃªn á»Ÿ Ä‘Ã¢y
                    result.put("timThay", true);
                } else {
                    result.put("timThay", false);
                }

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Debug chuyá»ƒn Ä‘á»•i mÃ£");
                response.put("data", result);

                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [DEBUG] Lá»—i debug chuyá»ƒn Ä‘á»•i mÃ£", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }
        /**
         * Táº¡o hÃ³a Ä‘Æ¡n chá» theo ID nhÃ¢n viÃªn
         */
        @Transactional
        @PostMapping("/hoa-don-cho/tao-moi-by-id")
        public ResponseEntity<Map<String, Object>> taoHoaDonChoById(@RequestParam Integer nhanVienId) {
            try {
                logger.info("ðŸ†• [API] Táº¡o hÃ³a Ä‘Æ¡n chá» má»›i cho nhÃ¢n viÃªn ID: {}", nhanVienId);
                HoaDonChoResponse response = banHangService.taoHoaDonCho(nhanVienId);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Táº¡o hÃ³a Ä‘Æ¡n chá» thÃ nh cÃ´ng");
                result.put("data", response);

                logger.info("âœ… [API] Táº¡o hÃ³a Ä‘Æ¡n chá» thÃ nh cÃ´ng: {}", response.getMaHoaDon());
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i táº¡o hÃ³a Ä‘Æ¡n chá»", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * Táº¡o hÃ³a Ä‘Æ¡n chá» theo mÃ£ nhÃ¢n viÃªn
         */
        @Transactional
        @PostMapping("/hoa-don-cho/tao-moi-by-ma")
        public ResponseEntity<Map<String, Object>> taoHoaDonChoByMa(@RequestParam String maNhanVien) {
            try {
                logger.info("ðŸ†• [API] Táº¡o hÃ³a Ä‘Æ¡n chá» má»›i cho nhÃ¢n viÃªn mÃ£: {}", maNhanVien);

                Integer nhanVienId = banHangService.timNhanVienIdTheoMa(maNhanVien);
                if (nhanVienId == null) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "KhÃ´ng tÃ¬m tháº¥y nhÃ¢n viÃªn vá»›i mÃ£: " + maNhanVien);
                    return ResponseEntity.badRequest().body(errorResponse);
                }

                HoaDonChoResponse response = banHangService.taoHoaDonCho(nhanVienId);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Táº¡o hÃ³a Ä‘Æ¡n chá» thÃ nh cÃ´ng");
                result.put("data", response);

                logger.info("âœ… [API] Táº¡o hÃ³a Ä‘Æ¡n chá» thÃ nh cÃ´ng: {}", response.getMaHoaDon());
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i táº¡o hÃ³a Ä‘Æ¡n chá»", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }
        @Transactional
        @GetMapping("/debug/nhan-vien")
        public ResponseEntity<Map<String, Object>> debugNhanVien() {
            try {
                logger.info("ðŸ” [DEBUG] Kiá»ƒm tra dá»¯ liá»‡u nhÃ¢n viÃªn");

                List<NhanVien> allNhanVien = nhanVienBHRepository.findAll();
                List<NhanVien> activeNhanVien = nhanVienBHRepository.findByTrangThai(1);

                Map<String, Object> debugInfo = new HashMap<>();
                debugInfo.put("tongSoNhanVien", allNhanVien.size());
                debugInfo.put("soNhanVienHoatDong", activeNhanVien.size());

                List<Map<String, Object>> danhSachNhanVien = allNhanVien.stream()
                        .map(nv -> {
                            Map<String, Object> info = new HashMap<>();
                            info.put("id", nv.getId());
                            info.put("maNhanVien", nv.getMaNhanVien());
                            info.put("hoTen", nv.getHoTen());
                            info.put("trangThai", nv.getTrangThai());
                            info.put("trangThaiText", nv.getTrangThai() == 1 ? "Hoáº¡t Ä‘á»™ng" : "KhÃ´ng hoáº¡t Ä‘á»™ng");
                            return info;
                        })
                        .collect(Collectors.toList());

                debugInfo.put("danhSachNhanVien", danhSachNhanVien);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Debug thÃ´ng tin nhÃ¢n viÃªn");
                response.put("data", debugInfo);

                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [DEBUG] Lá»—i debug nhÃ¢n viÃªn", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        // âœ… THÃŠM: Endpoint test tÃ¬m kiáº¿m cá»¥ thá»ƒ
        @Transactional
        @GetMapping("/debug/nhan-vien/tim-kiem/{ma}")
        public ResponseEntity<Map<String, Object>> debugTimKiemNhanVien(@PathVariable String ma) {
            try {
                logger.info("ðŸ” [DEBUG] Test tÃ¬m kiáº¿m nhÃ¢n viÃªn vá»›i mÃ£: '{}'", ma);

                Map<String, Object> ketQua = new HashMap<>();
                ketQua.put("maTimKiem", ma);

                // Test cÃ¡c cÃ¡ch tÃ¬m kiáº¿m khÃ¡c nhau
                Optional<NhanVien> byMaNhanVien = nhanVienBHRepository.findByMaNhanVienAndTrangThai(ma, 1);
                Optional<NhanVien> byMaTaiKhoan = nhanVienBHRepository.findByTaiKhoan_MaTaiKhoanAndTrangThai(ma, 1);
                Optional<NhanVien> byMaLinhHoat = nhanVienBHRepository.findByMaNhanVienOrMaTaiKhoanAndTrangThai(ma, 1);

                ketQua.put("timTheoMaNhanVien", byMaNhanVien.isPresent() ?
                        mapNhanVienToDebugInfo(byMaNhanVien.get()) : "KhÃ´ng tÃ¬m tháº¥y");

                ketQua.put("timTheoMaTaiKhoan", byMaTaiKhoan.isPresent() ?
                        mapNhanVienToDebugInfo(byMaTaiKhoan.get()) : "KhÃ´ng tÃ¬m tháº¥y");

                ketQua.put("timLinhHoat", byMaLinhHoat.isPresent() ?
                        mapNhanVienToDebugInfo(byMaLinhHoat.get()) : "KhÃ´ng tÃ¬m tháº¥y");

                // Test service method
                Integer nhanVienId = banHangService.timNhanVienIdTheoMa(ma);
                ketQua.put("timBangService", nhanVienId != null ? nhanVienId : "KhÃ´ng tÃ¬m tháº¥y");

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Test tÃ¬m kiáº¿m nhÃ¢n viÃªn");
                response.put("data", ketQua);

                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [DEBUG] Lá»—i test tÃ¬m kiáº¿m", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        // Helper method
        private Map<String, Object> mapNhanVienToDebugInfo(NhanVien nv) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", nv.getId());
            info.put("maNhanVien", nv.getMaNhanVien());
            info.put("hoTen", nv.getHoTen());
            info.put("maTaiKhoan", nv.getTaiKhoan() != null ? nv.getTaiKhoan().getMaTaiKhoan() : null);
            return info;
        }
        @GetMapping("/debug/voucher/test")
        public ResponseEntity<Map<String, Object>> testVoucherWithAmount(
                @RequestParam(required = false) Double tongTien) {
            try {
                logger.info("ðŸ§ª [DEBUG] Test voucher vá»›i tongTien: {}", tongTien);

                List<VoucherResponse> vouchers = banHangService.layDanhSachVoucherKhaDung(null, tongTien);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("tongTien", tongTien);
                response.put("soLuongVoucher", vouchers.size());
                response.put("vouchers", vouchers);

                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [DEBUG] Lá»—i test voucher", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }
        @ControllerAdvice
        public class GlobalExceptionHandler {

            private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

            @ExceptionHandler(UnexpectedRollbackException.class)
            public ResponseEntity<Map<String, Object>> handleUnexpectedRollback(UnexpectedRollbackException ex) {
                logger.error("âŒ Transaction rollback error", ex);

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i xá»­ lÃ½ giao dá»‹ch");
                errorResponse.put("error", "TRANSACTION_ROLLBACK");

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            @ExceptionHandler(DataAccessException.class)
            public ResponseEntity<Map<String, Object>> handleDataAccessException(DataAccessException ex) {
                logger.error("âŒ Database access error", ex);

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i truy cáº­p dá»¯ liá»‡u");

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }
//        // ===== GIAO HÃ€NG ENDPOINTS =====
//
//        @PostMapping("/hoa-don-cho/{id}/chuyen-giao-hang")
//        public ResponseEntity<Map<String, Object>> chuyenSangGiaoHang(
//                @PathVariable Integer id,
//                @RequestBody @Valid GiaoHangRequest request) {
//            try {
//                logger.info("ðŸšš [API] Chuyá»ƒn hÃ³a Ä‘Æ¡n {} sang giao hÃ ng", id);
//
//                HoaDonChoTongQuanResponse response = banHangService.chuyenSangGiaoHang(id, request);
//
//                Map<String, Object> result = new HashMap<>();
//                result.put("success", true);
//                result.put("message", "Chuyá»ƒn sang giao hÃ ng thÃ nh cÃ´ng");
//                result.put("data", response);
//
//                return ResponseEntity.ok(result);
//
//            } catch (Exception e) {
//                logger.error("âŒ [API] Lá»—i chuyá»ƒn giao hÃ ng", e);
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("message", "Lá»—i: " + e.getMessage());
//                return ResponseEntity.badRequest().body(errorResponse);
//            }
//        }
//
//        @PostMapping("/tinh-phi-ship")
//        public ResponseEntity<Map<String, Object>> tinhPhiShip(@RequestBody @Valid TinhPhiShipRequest request) {
//            try {
//                logger.info("ðŸ’° [API] TÃ­nh phÃ­ ship");
//
//                TinhPhiShipResponse response = banHangService.tinhPhiShip(request);
//
//                Map<String, Object> result = new HashMap<>();
//                result.put("success", true);
//                result.put("message", "TÃ­nh phÃ­ ship thÃ nh cÃ´ng");
//                result.put("data", response);
//
//                return ResponseEntity.ok(result);
//
//            } catch (Exception e) {
//                logger.error("âŒ [API] Lá»—i tÃ­nh phÃ­ ship", e);
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("message", "Lá»—i: " + e.getMessage());
//                return ResponseEntity.badRequest().body(errorResponse);
//            }
//        }
//
//        @PutMapping("/hoa-don-cho/{id}/cap-nhat-giao-hang")
//        public ResponseEntity<Map<String, Object>> capNhatThongTinGiaoHang(
//                @PathVariable Integer id,
//                @RequestBody CapNhatGiaoHangRequest request) {
//            try {
//                logger.info("âœï¸ [API] Cáº­p nháº­t thÃ´ng tin giao hÃ ng hÃ³a Ä‘Æ¡n {}", id);
//
//                HoaDonChoTongQuanResponse response = banHangService.capNhatThongTinGiaoHang(id, request);
//
//                Map<String, Object> result = new HashMap<>();
//                result.put("success", true);
//                result.put("message", "Cáº­p nháº­t thÃ´ng tin giao hÃ ng thÃ nh cÃ´ng");
//                result.put("data", response);
//
//                return ResponseEntity.ok(result);
//
//            } catch (Exception e) {
//                logger.error("âŒ [API] Lá»—i cáº­p nháº­t giao hÃ ng", e);
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("message", "Lá»—i: " + e.getMessage());
//                return ResponseEntity.badRequest().body(errorResponse);
//            }
//        }
//
//        @PostMapping("/hoa-don-cho/{id}/xac-nhan-giao-hang")
//        public ResponseEntity<Map<String, Object>> xacNhanGiaoHang(@PathVariable Integer id) {
//            try {
//                logger.info("âœ… [API] XÃ¡c nháº­n giao hÃ ng hÃ³a Ä‘Æ¡n {}", id);
//
//                HoaDonResponse response = banHangService.xacNhanGiaoHang(id);
//
//                Map<String, Object> result = new HashMap<>();
//                result.put("success", true);
//                result.put("message", "XÃ¡c nháº­n giao hÃ ng thÃ nh cÃ´ng");
//                result.put("data", response);
//
//                return ResponseEntity.ok(result);
//
//            } catch (Exception e) {
//                logger.error("âŒ [API] Lá»—i xÃ¡c nháº­n giao hÃ ng", e);
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("message", "Lá»—i: " + e.getMessage());
//                return ResponseEntity.badRequest().body(errorResponse);
//            }
//        }
//
//        @DeleteMapping("/hoa-don-cho/{id}/huy-giao-hang")
//        public ResponseEntity<Map<String, Object>> huyGiaoHang(@PathVariable Integer id) {
//            try {
//                logger.info("ðŸš« [API] Há»§y giao hÃ ng hÃ³a Ä‘Æ¡n {}", id);
//
//                HoaDonChoTongQuanResponse response = banHangService.huyGiaoHang(id);
//
//                Map<String, Object> result = new HashMap<>();
//                result.put("success", true);
//                result.put("message", "Há»§y giao hÃ ng thÃ nh cÃ´ng");
//                result.put("data", response);
//
//                return ResponseEntity.ok(result);
//
//            } catch (Exception e) {
//                logger.error("âŒ [API] Lá»—i há»§y giao hÃ ng", e);
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("message", "Lá»—i: " + e.getMessage());
//                return ResponseEntity.badRequest().body(errorResponse);
//            }
//        }
//
//        @PutMapping("/hoa-don/{id}/dang-giao")
//        public ResponseEntity<Map<String, Object>> capNhatDangGiao(@PathVariable Integer id) {
//            try {
//                logger.info("ðŸšš [API] Cáº­p nháº­t Ä‘ang giao hÃ³a Ä‘Æ¡n {}", id);
//
//                HoaDonResponse response = banHangService.capNhatDangGiao(id);
//
//                Map<String, Object> result = new HashMap<>();
//                result.put("success", true);
//                result.put("message", "Cáº­p nháº­t Ä‘ang giao thÃ nh cÃ´ng");
//                result.put("data", response);
//
//                return ResponseEntity.ok(result);
//
//            } catch (Exception e) {
//                logger.error("âŒ [API] Lá»—i cáº­p nháº­t Ä‘ang giao", e);
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("message", "Lá»—i: " + e.getMessage());
//                return ResponseEntity.badRequest().body(errorResponse);
//            }
//        }
//
//        @PutMapping("/hoa-don/{id}/da-giao")
//        public ResponseEntity<Map<String, Object>> xacNhanDaGiao(@PathVariable Integer id) {
//            try {
//                logger.info("âœ… [API] XÃ¡c nháº­n Ä‘Ã£ giao hÃ³a Ä‘Æ¡n {}", id);
//
//                HoaDonResponse response = banHangService.xacNhanDaGiao(id);
//
//                Map<String, Object> result = new HashMap<>();
//                result.put("success", true);
//                result.put("message", "XÃ¡c nháº­n Ä‘Ã£ giao thÃ nh cÃ´ng");
//                result.put("data", response);
//
//                return ResponseEntity.ok(result);
//
//            } catch (Exception e) {
//                logger.error("âŒ [API] Lá»—i xÃ¡c nháº­n Ä‘Ã£ giao", e);
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("message", "Lá»—i: " + e.getMessage());
//                return ResponseEntity.badRequest().body(errorResponse);
//            }
//        }
//
//        @GetMapping("/hoa-don/dang-giao")
//        public ResponseEntity<Map<String, Object>> layDanhSachDangGiao(
//                @RequestParam(defaultValue = "0") int page,
//                @RequestParam(defaultValue = "10") int size) {
//            try {
//                logger.info("ðŸ“‹ [API] Láº¥y danh sÃ¡ch Ä‘Æ¡n Ä‘ang giao");
//
//                List<HoaDon> hoaDons = hoaDonRepository.findByTrangThaiHoaDonIn(
//                        Arrays.asList("ÄÃ£ xÃ¡c nháº­n", "Äang giao")
//                );
//
//                List<HoaDonResponse> responses = hoaDons.stream()
//                        .map(this::mapToHoaDonResponse)
//                        .collect(Collectors.toList());
//
//                Map<String, Object> result = new HashMap<>();
//                result.put("success", true);
//                result.put("message", "Láº¥y danh sÃ¡ch thÃ nh cÃ´ng");
//                result.put("data", responses);
//                result.put("total", responses.size());
//
//                return ResponseEntity.ok(result);
//
//            } catch (Exception e) {
//                logger.error("âŒ [API] Lá»—i láº¥y danh sÃ¡ch Ä‘ang giao", e);
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("message", "Lá»—i: " + e.getMessage());
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//            }
//        }

        @Transactional
        @PostMapping("/hoa-don-cho/{id}/thanh-toan-chi-tiet")
        public ResponseEntity<Map<String, Object>> thanhToanHoaDonChiTiet(
                @PathVariable Integer id,
                @RequestBody @Valid ThanhToanRequest request) {
            try {
                logger.info("ðŸ’° [API] Thanh toÃ¡n chi tiáº¿t hÃ³a Ä‘Æ¡n: ID {} - PhÆ°Æ¡ng thá»©c: {}", id, request.getPhuongThucThanhToan());

                // Validate phÆ°Æ¡ng thá»©c thanh toÃ¡n
                if (!Arrays.asList("TIEN_MAT", "CHUYEN_KHOAN", "KET_HOP").contains(request.getPhuongThucThanhToan())) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "PhÆ°Æ¡ng thá»©c thanh toÃ¡n khÃ´ng há»£p lá»‡");
                    return ResponseEntity.badRequest().body(errorResponse);
                }

                ThanhToanResponse response = banHangService.thanhToanHoaDonChiTiet(id, request);

                Map<String, Object> result = new HashMap<>();
                result.put("success", response.getThanhCong());
                result.put("message", response.getThanhCong() ? "Thanh toÃ¡n thÃ nh cÃ´ng" : "Thanh toÃ¡n tháº¥t báº¡i");
                result.put("data", response);

                // ThÃªm thÃ´ng tin bá»• sung cho frontend
                if (response.getThanhCong()) {
                    result.put("thongBao", response.getThongBaoThanhToan());
                    result.put("phuongThucThanhToan", response.getPhuongThucThanhToan());

                    // ThÃ´ng tin tiá»n thá»«a cho tiá»n máº·t
                    if ("TIEN_MAT".equals(response.getPhuongThucThanhToan()) || "KET_HOP".equals(response.getPhuongThucThanhToan())) {
                        if (response.getTienThua() != null && response.getTienThua().compareTo(BigDecimal.ZERO) > 0) {
                            result.put("coTienThua", true);
                            result.put("tienThua", response.getTienThua());
                        }
                    }
                }

                logger.info("âœ… [API] Thanh toÃ¡n chi tiáº¿t thÃ nh cÃ´ng: {}", response.getMaHoaDon());
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i thanh toÃ¡n chi tiáº¿t: ID {}", id, e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i thanh toÃ¡n: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * Kiá»ƒm tra phÆ°Æ¡ng thá»©c thanh toÃ¡n há»£p lá»‡
         */
        @GetMapping("/phuong-thuc-thanh-toan")
        public ResponseEntity<Map<String, Object>> layPhuongThucThanhToan() {
            try {
                List<Map<String, String>> phuongThucList = Arrays.asList(
                        Map.of("ma", "TIEN_MAT", "ten", "Tiá»n máº·t", "moTa", "Thanh toÃ¡n báº±ng tiá»n máº·t"),
                        Map.of("ma", "CHUYEN_KHOAN", "ten", "Chuyá»ƒn khoáº£n", "moTa", "Thanh toÃ¡n báº±ng chuyá»ƒn khoáº£n ngÃ¢n hÃ ng"),
                        Map.of("ma", "KET_HOP", "ten", "Káº¿t há»£p", "moTa", "Thanh toÃ¡n káº¿t há»£p tiá»n máº·t vÃ  chuyá»ƒn khoáº£n")
                );

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Láº¥y phÆ°Æ¡ng thá»©c thanh toÃ¡n thÃ nh cÃ´ng");
                response.put("data", phuongThucList);

                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i láº¥y phÆ°Æ¡ng thá»©c thanh toÃ¡n", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * TÃ­nh tiá»n thá»«a khi thanh toÃ¡n tiá»n máº·t
         */
        @PostMapping("/tinh-tien-thua")
        public ResponseEntity<Map<String, Object>> tinhTienThua(@RequestBody Map<String, Object> request) {
            try {
                BigDecimal tongTien = new BigDecimal(request.get("tongTien").toString());
                BigDecimal tienKhachDua = new BigDecimal(request.get("tienKhachDua").toString());

                if (tienKhachDua.compareTo(tongTien) < 0) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "Sá»‘ tiá»n khÃ¡ch Ä‘Æ°a khÃ´ng Ä‘á»§");
                    return ResponseEntity.badRequest().body(errorResponse);
                }

                BigDecimal tienThua = tienKhachDua.subtract(tongTien);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("tongTien", tongTien);
                result.put("tienKhachDua", tienKhachDua);
                result.put("tienThua", tienThua);
                result.put("coTienThua", tienThua.compareTo(BigDecimal.ZERO) > 0);

                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i tÃ­nh tiá»n thá»«a", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i tÃ­nh toÃ¡n: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * Validate thÃ´ng tin thanh toÃ¡n trÆ°á»›c khi thá»±c hiá»‡n
         */
        @PostMapping("/validate-thanh-toan")
        public ResponseEntity<Map<String, Object>> validateThanhToan(@RequestBody @Valid ThanhToanRequest request) {
            try {
                logger.info("ðŸ” [API] Validate thÃ´ng tin thanh toÃ¡n");

                List<String> errors = new ArrayList<>();

                // Validate phÆ°Æ¡ng thá»©c thanh toÃ¡n
                if (!Arrays.asList("TIEN_MAT", "CHUYEN_KHOAN", "KET_HOP").contains(request.getPhuongThucThanhToan())) {
                    errors.add("PhÆ°Æ¡ng thá»©c thanh toÃ¡n khÃ´ng há»£p lá»‡");
                }

                // Validate theo tá»«ng phÆ°Æ¡ng thá»©c
                switch (request.getPhuongThucThanhToan()) {
                    case "TIEN_MAT":
                        if (request.getTienMat() == null || request.getTienMat().compareTo(BigDecimal.ZERO) <= 0) {
                            errors.add("Sá»‘ tiá»n máº·t pháº£i lá»›n hÆ¡n 0");
                        }
                        break;

                    case "CHUYEN_KHOAN":
                        if (request.getTienChuyenKhoan() == null || request.getTienChuyenKhoan().compareTo(BigDecimal.ZERO) <= 0) {
                            errors.add("Sá»‘ tiá»n chuyá»ƒn khoáº£n pháº£i lá»›n hÆ¡n 0");
                        }
                        break;

                    case "KET_HOP":
                        BigDecimal tienMat = request.getTienMat() != null ? request.getTienMat() : BigDecimal.ZERO;
                        BigDecimal tienCK = request.getTienChuyenKhoan() != null ? request.getTienChuyenKhoan() : BigDecimal.ZERO;

                        if (tienMat.compareTo(BigDecimal.ZERO) <= 0 && tienCK.compareTo(BigDecimal.ZERO) <= 0) {
                            errors.add("Pháº£i cÃ³ Ã­t nháº¥t má»™t phÆ°Æ¡ng thá»©c thanh toÃ¡n cÃ³ giÃ¡ trá»‹ > 0");
                        }
                        break;
                }

                Map<String, Object> response = new HashMap<>();
                response.put("success", errors.isEmpty());
                response.put("message", errors.isEmpty() ? "ThÃ´ng tin thanh toÃ¡n há»£p lá»‡" : "CÃ³ lá»—i trong thÃ´ng tin thanh toÃ¡n");
                response.put("errors", errors);

                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("âŒ [API] Lá»—i validate thanh toÃ¡n", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lá»—i validation: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }
        @GetMapping("/hoa-don-cho/{hoaDonId}/kiem-tra-ton-kho")
        public ResponseEntity<List<InventoryCheckResponse>> kiemTraTonKhoTruocThanhToan(@PathVariable Integer hoaDonId) {
            try {
                List<InventoryCheckResponse> result = banHangService.kiemTraTonKhoTruocThanhToan(hoaDonId);
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                log.error("Lá»—i kiá»ƒm tra tá»“n kho trÆ°á»›c thanh toÃ¡n: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }
