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

        // ===== HÓA ĐƠN CHỜ =====

        /**
         * Lấy danh sách hóa đơn chờ
         */

        @Transactional(readOnly = true)
        @GetMapping("/hoa-don-cho")
        public ResponseEntity<Map<String, Object>> layDanhSachHoaDonCho() {
            try {
                logger.info("🔍 [API] Lấy danh sách hóa đơn chờ");
                List<HoaDonChoResponse> hoaDonCho = banHangService.layDanhSachHoaDonCho();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Lấy danh sách hóa đơn chờ thành công");
                response.put("data", hoaDonCho);
                response.put("total", hoaDonCho.size());

                logger.info("✅ [API] Lấy {} hóa đơn chờ thành công", hoaDonCho.size());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi lấy hóa đơn chờ", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Tạo hóa đơn chờ mới
         */
    //    @PostMapping("/hoa-don-cho/tao-moi")
    //    public ResponseEntity<Map<String, Object>> taoHoaDonCho(@RequestParam Integer nhanVienId) {
    //        try {
    //            logger.info("🆕 [API] Tạo hóa đơn chờ mới cho nhân viên ID: {}", nhanVienId);
    //            HoaDonChoResponse response = banHangService.taoHoaDonCho(nhanVienId);
    //
    //            Map<String, Object> result = new HashMap<>();
    //            result.put("success", true);
    //            result.put("message", "Tạo hóa đơn chờ thành công");
    //            result.put("data", response);
    //
    //            logger.info("✅ [API] Tạo hóa đơn chờ thành công: {}", response.getMaHoaDon());
    //            return ResponseEntity.ok(result);
    //
    //        } catch (Exception e) {
    //            logger.error("❌ [API] Lỗi tạo hóa đơn chờ", e);
    //            Map<String, Object> errorResponse = new HashMap<>();
    //            errorResponse.put("success", false);
    //            errorResponse.put("message", "Lỗi: " + e.getMessage());
    //            return ResponseEntity.badRequest().body(errorResponse);
    //        }
    //    }

        /**
         * Xóa hóa đơn chờ
         */
        @Transactional
        @DeleteMapping("/hoa-don-cho/{id}")
        public ResponseEntity<Map<String, Object>> xoaHoaDonCho(@PathVariable Integer id) {
            try {
                logger.info("🗑️ [API] Xóa hóa đơn chờ: ID {}", id);
                banHangService.xoaHoaDonCho(id);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Xóa hóa đơn chờ thành công");

                logger.info("✅ [API] Xóa hóa đơn chờ thành công: ID {}", id);
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi xóa hóa đơn chờ: ID {}", id, e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * Lấy chi tiết hóa đơn chờ
         */
        @Transactional
        @GetMapping("/hoa-don-cho/{id}")
        public ResponseEntity<Map<String, Object>> layChiTietHoaDonCho(@PathVariable Integer id) {
            try {
                logger.info("🔍 [API] Lấy chi tiết hóa đơn chờ: ID {}", id);
                HoaDonChoDetailResponse response = banHangService.layChiTietHoaDonCho(id);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Lấy chi tiết hóa đơn chờ thành công");
                result.put("data", response);

                logger.info("✅ [API] Lấy chi tiết hóa đơn chờ thành công: {}", response.getMaHoaDon());
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi lấy chi tiết hóa đơn chờ: ID {}", id, e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Lấy tổng quan hóa đơn chờ
         */
        @Transactional
        @GetMapping("/hoa-don-cho/{id}/tong-quan")
        public ResponseEntity<Map<String, Object>> layTongQuanHoaDonCho(@PathVariable Integer id) {
            try {
                logger.info("📊 [API] Lấy tổng quan hóa đơn chờ: ID {}", id);
                HoaDonChoTongQuanResponse response = banHangService.layTongQuanHoaDonCho(id);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Lấy tổng quan hóa đơn chờ thành công");
                result.put("data", response);

                logger.info("✅ [API] Lấy tổng quan hóa đơn chờ thành công");
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi lấy tổng quan hóa đơn chờ: ID {}", id, e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        // ===== QUẢN LÝ SẢN PHẨM =====

        /**
         * Tìm kiếm sản phẩm cho bán hàng
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
                logger.info("🔍 [API] Tìm kiếm sản phẩm: keyword='{}', danhMucId={}", keyword, danhMucId);

                Sort sort = sortDir.equalsIgnoreCase("desc") ?
                        Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
                Pageable pageable = PageRequest.of(page, size, sort);

                // Tạo filter object
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
                response.put("message", "Tìm kiếm sản phẩm thành công");
                response.put("data", sanPhamPage.getContent());
                response.put("currentPage", sanPhamPage.getNumber());
                response.put("totalElements", sanPhamPage.getTotalElements());
                response.put("totalPages", sanPhamPage.getTotalPages());
                response.put("size", sanPhamPage.getSize());

                logger.info("✅ [API] Tìm kiếm {} sản phẩm thành công", sanPhamPage.getTotalElements());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi tìm kiếm sản phẩm", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Lấy chi tiết sản phẩm
         */
        @Transactional
        @GetMapping("/san-pham/{id}")
        public ResponseEntity<Map<String, Object>> layChiTietSanPham(@PathVariable Integer id) {
            try {
                logger.info("🔍 [API] Lấy chi tiết sản phẩm: ID {}", id);
                SanPhamChiTietBanHangResponse sanPham = banHangService.layChiTietSanPham(id);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Lấy chi tiết sản phẩm thành công");
                response.put("data", sanPham);

                logger.info("✅ [API] Lấy chi tiết sản phẩm thành công: {}", sanPham.getTenSanPham());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi lấy chi tiết sản phẩm: ID {}", id, e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Scan QR code sản phẩm
         */
        @Transactional
        @PostMapping("/san-pham/scan-qr")
        public ResponseEntity<Map<String, Object>> scanQRSanPham(@RequestBody @Valid ScanQRRequest request) {
            try {
                logger.info("📱 [API] Scan QR code: {}", request.getQrCode());
                ScanQRResponse response = banHangService.scanQRSanPham(request.getQrCode());

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Scan QR code thành công");
                result.put("data", response);

                logger.info("✅ [API] Scan QR code thành công");
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi scan QR code", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * Lấy sản phẩm tương tự
         */
        @Transactional
        @GetMapping("/san-pham/{id}/tuong-tu")
        public ResponseEntity<Map<String, Object>> laySanPhamTuongTu(@PathVariable Integer id) {
            try {
                logger.info("🔍 [API] Lấy sản phẩm tương tự: ID {}", id);
                List<SanPhamChiTietBanHangResponse> sanPhamList = banHangService.laySanPhamTuongTu(id);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Lấy sản phẩm tương tự thành công");
                response.put("data", sanPhamList);
                response.put("total", sanPhamList.size());

                logger.info("✅ [API] Lấy {} sản phẩm tương tự thành công", sanPhamList.size());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi lấy sản phẩm tương tự", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        // ===== QUẢN LÝ SẢN PHẨM TRONG HÓA ĐƠN =====

        /**
         * Thêm sản phẩm vào hóa đơn chờ
         */
        @Transactional
        @PostMapping("/hoa-don-cho/{id}/them-san-pham")
        public ResponseEntity<Map<String, Object>> themSanPhamVaoHoaDon(
                @PathVariable Integer id,
                @RequestBody @Valid ThemSanPhamRequest request) {
            try {
                logger.info("➕ [API] Thêm sản phẩm vào hóa đơn chờ: HD {} - SP {}",
                        id, request.getChiTietSanPhamId());

                HoaDonChoTongQuanResponse response = banHangService.themSanPhamVaoHoaDon(id, request);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Thêm sản phẩm thành công");
                result.put("data", response);

                logger.info("✅ [API] Thêm sản phẩm vào hóa đơn chờ thành công");
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi thêm sản phẩm vào hóa đơn chờ", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * Cập nhật sản phẩm trong hóa đơn chờ
         */
        @Transactional
        @PutMapping("/hoa-don-cho/{hoaDonId}/cap-nhat-san-pham/{chiTietId}")
        public ResponseEntity<Map<String, Object>> capNhatSanPhamTrongHoaDon(
                @PathVariable Integer hoaDonId,
                @PathVariable Integer chiTietId,
                @RequestBody @Valid CapNhatSanPhamRequest request) {
            try {
                logger.info("✏️ [API] Cập nhật sản phẩm trong hóa đơn chờ: HD {} - CT {}",
                        hoaDonId, chiTietId);

                HoaDonChoTongQuanResponse response = banHangService.capNhatSanPhamTrongHoaDon(hoaDonId, chiTietId, request);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Cập nhật sản phẩm thành công");
                result.put("data", response);

                logger.info("✅ [API] Cập nhật sản phẩm trong hóa đơn chờ thành công");
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi cập nhật sản phẩm trong hóa đơn chờ", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * Xóa sản phẩm khỏi hóa đơn chờ
         */
        @Transactional
        @DeleteMapping("/hoa-don-cho/{hoaDonId}/xoa-san-pham/{chiTietId}")
        public ResponseEntity<Map<String, Object>> xoaSanPhamKhoiHoaDon(
                @PathVariable Integer hoaDonId,
                @PathVariable Integer chiTietId) {
            try {
                logger.info("🗑️ [API] Xóa sản phẩm khỏi hóa đơn chờ: HD {} - CT {}",
                        hoaDonId, chiTietId);

                HoaDonChoTongQuanResponse response = banHangService.xoaSanPhamKhoiHoaDon(hoaDonId, chiTietId);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Xóa sản phẩm thành công");
                result.put("data", response);

                logger.info("✅ [API] Xóa sản phẩm khỏi hóa đơn chờ thành công");
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi xóa sản phẩm khỏi hóa đơn chờ", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * Tính giá sản phẩm
         */
        @Transactional
        @PostMapping("/san-pham/tinh-gia")
        public ResponseEntity<Map<String, Object>> tinhGiaSanPham(@RequestBody @Valid TinhGiaRequest request) {
            try {
                logger.info("💰 [API] Tính giá sản phẩm");
                TinhGiaResponse response = banHangService.tinhGiaSanPham(request);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Tính giá sản phẩm thành công");
                result.put("data", response);

                logger.info("✅ [API] Tính giá sản phẩm thành công");
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi tính giá sản phẩm", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        // ===== QUẢN LÝ KHÁCH HÀNG =====

        /**
         * Tìm kiếm khách hàng
         */
        @GetMapping("/khach-hang/search")
        public ResponseEntity<Map<String, Object>> timKiemKhachHang(
                @RequestParam(defaultValue = "") String keyword,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size,
                @RequestParam(defaultValue = "ngayTao") String sortBy,
                @RequestParam(defaultValue = "desc") String sortDir) {
            try {
                logger.info("🔍 [API] Tìm kiếm khách hàng: keyword='{}', page={}, size={}", keyword, page, size);

                Sort sort = sortDir.equalsIgnoreCase("desc") ?
                        Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
                Pageable pageable = PageRequest.of(page, size, sort);

                Page<KhachHangResponse> khachHangPage = banHangService.timKiemKhachHang(keyword, pageable);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Tìm kiếm khách hàng thành công");
                response.put("data", khachHangPage.getContent());
                response.put("currentPage", khachHangPage.getNumber());
                response.put("totalElements", khachHangPage.getTotalElements());
                response.put("totalPages", khachHangPage.getTotalPages());
                response.put("size", khachHangPage.getSize());

                logger.info("✅ [API] Tìm kiếm khách hàng thành công: {} kết quả", khachHangPage.getTotalElements());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi tìm kiếm khách hàng", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Tạo khách hàng nhanh
         */
        @Transactional
        @PostMapping("/khach-hang/tao-nhanh")
        public ResponseEntity<Map<String, Object>> taoKhachHangNhanh(@RequestBody @Valid TaoKhachHangNhanhRequest request) {
            try {
                logger.info("👤 [API] Tạo khách hàng nhanh: {}", request.getHoTen());
                KhachHangResponse response = banHangService.taoKhachHangNhanh(request);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Tạo khách hàng thành công");
                result.put("data", response);

                logger.info("✅ [API] Tạo khách hàng nhanh thành công: {}", response.getHoTen());
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi tạo khách hàng nhanh", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * Lấy thông tin khách hàng
         */
        @Transactional
        @GetMapping("/khach-hang/{id}")
        public ResponseEntity<Map<String, Object>> layThongTinKhachHang(@PathVariable Integer id) {
            try {
                logger.info("🔍 [API] Lấy thông tin khách hàng: ID {}", id);
                KhachHangDetailResponse response = banHangService.layThongTinKhachHang(id);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Lấy thông tin khách hàng thành công");
                result.put("data", response);

                logger.info("✅ [API] Lấy thông tin khách hàng thành công: {}", response.getHoTen());
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi lấy thông tin khách hàng: ID {}", id, e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Áp dụng khách hàng cho hóa đơn
         */
        @Transactional
        @PostMapping("/hoa-don-cho/{hoaDonId}/ap-dung-khach-hang/{khachHangId}")
        public ResponseEntity<Map<String, Object>> apDungKhachHang(
                @PathVariable Integer hoaDonId,
                @PathVariable Integer khachHangId) {
            try {
                logger.info("👤 [API] Áp dụng khách hàng ID {} cho hóa đơn ID {}", khachHangId, hoaDonId);

                HoaDonChoTongQuanResponse response = banHangService.apDungKhachHang(hoaDonId, khachHangId);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Áp dụng khách hàng thành công");
                result.put("data", response);

                logger.info("✅ [API] Áp dụng khách hàng thành công");
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi áp dụng khách hàng", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * Bỏ khách hàng khỏi hóa đơn
         */
        @Transactional
        @DeleteMapping("/hoa-don-cho/{hoaDonId}/bo-khach-hang")
        public ResponseEntity<Map<String, Object>> boKhachHang(@PathVariable Integer hoaDonId) {
            try {
                logger.info("🗑️ [API] Bỏ khách hàng khỏi hóa đơn ID {}", hoaDonId);

                HoaDonChoTongQuanResponse response = banHangService.boKhachHang(hoaDonId);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Bỏ khách hàng thành công");
                result.put("data", response);

                logger.info("✅ [API] Bỏ khách hàng thành công");
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi bỏ khách hàng", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        // ===== QUẢN LÝ VOUCHER =====

        /**
         * Lấy danh sách voucher khả dụng
         */
        @Transactional
        @GetMapping("/voucher/kha-dung")
        public ResponseEntity<Map<String, Object>> layDanhSachVoucherKhaDung(
                @RequestParam(required = false) Integer khachHangId,
                @RequestParam(required = false) Double tongTien) {
            try {
                logger.info("🎫 [API] Lấy voucher khả dụng: khachHangId={}, tongTien={}", khachHangId, tongTien);
                List<VoucherResponse> vouchers = banHangService.layDanhSachVoucherKhaDung(khachHangId, tongTien);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Lấy voucher khả dụng thành công");
                response.put("data", vouchers);
                response.put("total", vouchers.size());

                logger.info("✅ [API] Lấy {} voucher khả dụng thành công", vouchers.size());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi lấy voucher khả dụng", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Kiểm tra voucher
         */
        @Transactional
        @PostMapping("/voucher/kiem-tra")
        public ResponseEntity<VoucherValidationResponse> kiemTraVoucher(@RequestBody @Valid ValidateVoucherRequest request) {
            try {
                logger.info("✅ [API] Kiểm tra voucher: {}", request.getMaVoucher());
                VoucherValidationResponse response = banHangService.kiemTraVoucher(request);

                logger.info("✅ [API] Kiểm tra voucher thành công: valid={}", response.getValid());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi kiểm tra voucher", e);
                return ResponseEntity.badRequest().body(
                        VoucherValidationResponse.builder()
                                .valid(false)
                                .message("Lỗi hệ thống: " + e.getMessage())
                                .build()
                );
            }
        }

        /**
         * Áp dụng voucher vào hóa đơn
         */
        @Transactional
        @PostMapping("/hoa-don-cho/{hoaDonId}/ap-dung-voucher/{voucherId}")
        public ResponseEntity<Map<String, Object>> apDungVoucher(
                @PathVariable Integer hoaDonId,
                @PathVariable Integer voucherId) {
            try {
                logger.info("🎫 [API] Áp dụng voucher ID {} cho hóa đơn ID {}", voucherId, hoaDonId);

                HoaDonChoTongQuanResponse response = banHangService.apDungVoucher(hoaDonId, voucherId);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Áp dụng voucher thành công");
                result.put("data", response);

                logger.info("✅ [API] Áp dụng voucher thành công");
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi áp dụng voucher", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * Bỏ voucher khỏi hóa đơn
         */
        @Transactional
        @DeleteMapping("/hoa-don-cho/{hoaDonId}/bo-voucher")
        public ResponseEntity<Map<String, Object>> boVoucher(@PathVariable Integer hoaDonId) {
            try {
                logger.info("🗑️ [API] Bỏ voucher khỏi hóa đơn ID {}", hoaDonId);

                HoaDonChoTongQuanResponse response = banHangService.boVoucher(hoaDonId);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Bỏ voucher thành công");
                result.put("data", response);

                logger.info("✅ [API] Bỏ voucher thành công");
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi bỏ voucher", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        // ===== THANH TOÁN =====

        /**
         * Thanh toán hóa đơn chờ
         */
        @Transactional
        @PostMapping("/hoa-don-cho/{id}/thanh-toan")
        public ResponseEntity<Map<String, Object>> thanhToanHoaDon(
                @PathVariable Integer id,
                @RequestBody @Valid ThanhToanRequest request) {
            try {
                logger.info("💰 [API] Thanh toán hóa đơn chờ: ID {} - Phương thức: {}", id, request.getPhuongThucThanhToan());
                HoaDonResponse response = banHangService.thanhToanHoaDon(id, request);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Thanh toán thành công");
                result.put("data", response);

                logger.info("✅ [API] Thanh toán hóa đơn thành công: {}", response.getMaHoaDon());
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi thanh toán hóa đơn: ID {}", id, e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * Kiểm tra tồn kho trước khi thanh toán
         */
//        @GetMapping("/hoa-don-cho/{hoaDonId}/kiem-tra-ton-kho")
//        public ResponseEntity<Map<String, Object>> kiemTraTonKho(@PathVariable Integer hoaDonId) {
//            try {
//                logger.info("📦 [API] Kiểm tra tồn kho hóa đơn: ID {}", hoaDonId);
//
//                // Validate input
//                if (hoaDonId == null || hoaDonId <= 0) {
//                    Map<String, Object> errorResponse = new HashMap<>();
//                    errorResponse.put("success", false);
//                    errorResponse.put("message", "ID hóa đơn không hợp lệ");
//                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//                }
//
//                // Kiểm tra hóa đơn tồn tại
//                boolean hoaDonExists = hoaDonRepository.existsById(hoaDonId);
//                if (!hoaDonExists) {
//                    Map<String, Object> errorResponse = new HashMap<>();
//                    errorResponse.put("success", false);
//                    errorResponse.put("message", "Không tìm thấy hóa đơn với ID: " + hoaDonId);
//                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
//                }
//
//                // Gọi service với tên method mới
//                List<InventoryCheckResponse> responses = banHangService.kiemTraTonKhoTruocThanhToan(hoaDonId);
//
//                Map<String, Object> result = new HashMap<>();
//                result.put("success", true);
//                result.put("message", "Kiểm tra tồn kho thành công");
//                result.put("data", responses);
//
//                // Thêm thông tin tổng quan
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
//                logger.info("✅ [API] Kiểm tra tồn kho thành công: {}/{} sản phẩm đủ hàng",
//                        availableItems, totalItems);
//                return ResponseEntity.ok(result);
//
//            } catch (RuntimeException e) {
//                logger.error("❌ [API] Lỗi nghiệp vụ khi kiểm tra tồn kho: ID {}, Error: {}", hoaDonId, e.getMessage());
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("message", e.getMessage());
//                errorResponse.put("hoaDonId", hoaDonId);
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//
//            } catch (Exception e) {
//                logger.error("❌ [API] Lỗi hệ thống khi kiểm tra tồn kho: ID {}", hoaDonId, e);
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("message", "Lỗi hệ thống: Không thể kiểm tra tồn kho");
//                errorResponse.put("error", "SYSTEM_ERROR");
//                errorResponse.put("hoaDonId", hoaDonId);
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//            }
//        }

        // ===== THỐNG KÊ =====

        /**
         * Lấy thống kê bán hàng trong ngày
         */
        @Transactional
        @GetMapping("/thong-ke/ban-hang-trong-ngay")
        public ResponseEntity<Map<String, Object>> layThongKeBanHangTrongNgay() {
            try {
                logger.info("📊 [API] Lấy thống kê bán hàng trong ngày");
                Map<String, Object> thongKe = banHangService.layThongKeBanHangTrongNgay();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Lấy thống kê thành công");
                response.put("data", thongKe);

                logger.info("✅ [API] Lấy thống kê bán hàng trong ngày thành công");
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi lấy thống kê bán hàng", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Lấy sản phẩm bán chạy
         */
        @Transactional
        @GetMapping("/thong-ke/san-pham-ban-chay")
        public ResponseEntity<Map<String, Object>> laySanPhamBanChay(
                @RequestParam(defaultValue = "10") int limit) {
            try {
                logger.info("📊 [API] Lấy sản phẩm bán chạy: limit={}", limit);
                List<Map<String, Object>> sanPhamBanChay = banHangService.laySanPhamBanChay(limit);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Lấy sản phẩm bán chạy thành công");
                response.put("data", sanPhamBanChay);
                response.put("total", sanPhamBanChay.size());

                logger.info("✅ [API] Lấy {} sản phẩm bán chạy thành công", sanPhamBanChay.size());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi lấy sản phẩm bán chạy", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Lấy thống kê doanh thu
         */
        @Transactional
        @GetMapping("/thong-ke/doanh-thu")
        public ResponseEntity<Map<String, Object>> layThongKeDoanhThu(
                @RequestParam String tuNgay,
                @RequestParam String denNgay) {
            try {
                logger.info("📊 [API] Lấy thống kê doanh thu: từ {} đến {}", tuNgay, denNgay);
                Map<String, Object> thongKe = banHangService.layThongKeDoanhThu(tuNgay, denNgay);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Lấy thống kê doanh thu thành công");
                response.put("data", thongKe);

                logger.info("✅ [API] Lấy thống kê doanh thu thành công");
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi lấy thống kê doanh thu", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
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
         * Exception handler cho các lỗi chung
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
            logger.error("❌ Lỗi chung: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", ex.getMessage()));
        }

        // Thêm các endpoint sau vào BanHangController

        @GetMapping("/san-pham/{id}/debug-hinh-anh")
        public ResponseEntity<Map<String, Object>> debugHinhAnhSanPham(@PathVariable Integer id) {
            try {
                logger.info("🖼️ [API] Debug hình ảnh sản phẩm: ID {}", id);

                // Lấy thông tin sản phẩm chi tiết
                SanPhamChiTietBanHangResponse sanPham = banHangService.layChiTietSanPham(id);

                Map<String, Object> debugInfo = new HashMap<>();
                debugInfo.put("id", sanPham.getId());
                debugInfo.put("tenSanPham", sanPham.getTenSanPham());

                // Debug hình ảnh giống quản lý sản phẩm
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

                logger.info("✅ [API] Debug hình ảnh sản phẩm thành công");
                return ResponseEntity.ok(debugInfo);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi debug hình ảnh sản phẩm: ID {}", id, e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Lấy hình ảnh đã format URL cho frontend
         */
        @GetMapping("/san-pham/{id}/hinh-anh")
        public ResponseEntity<Map<String, Object>> layHinhAnhSanPham(@PathVariable Integer id) {
            try {
                logger.info("🖼️ [API] Lấy hình ảnh sản phẩm: ID {}", id);

                SanPhamChiTietBanHangResponse sanPham = banHangService.layChiTietSanPham(id);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("sanPhamId", id);

                // Sử dụng cấu trúc giống quản lý sản phẩm
                if (sanPham.getDanhSachHinhAnh() != null && !sanPham.getDanhSachHinhAnh().isEmpty()) {
                    response.put("danhSachHinhAnh", sanPham.getDanhSachHinhAnh());
                    response.put("hinhAnhChinh", sanPham.getHinhAnhChinh());
                    response.put("soLuongHinhAnh", sanPham.getDanhSachHinhAnh().size());
                    response.put("message", "Lấy hình ảnh thành công");
                } else {
                    response.put("danhSachHinhAnh", new ArrayList<>());
                    response.put("hinhAnhChinh", null);
                    response.put("soLuongHinhAnh", 0);
                    response.put("message", "Sản phẩm chưa có hình ảnh");
                }

                logger.info("✅ [API] Lấy hình ảnh sản phẩm thành công");
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi lấy hình ảnh sản phẩm: ID {}", id, e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Kiểm tra URL hình ảnh có accessible không
         */
        @Transactional
        @GetMapping("/san-pham/{id}/kiem-tra-hinh-anh")
        public ResponseEntity<Map<String, Object>> kiemTraHinhAnh(@PathVariable Integer id) {
            try {
                logger.info("🔍 [API] Kiểm tra hình ảnh sản phẩm: ID {}", id);

                SanPhamChiTietBanHangResponse sanPham = banHangService.layChiTietSanPham(id);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("sanPhamId", id);

                // Sử dụng cấu trúc giống quản lý sản phẩm
                if (sanPham.getDanhSachHinhAnh() != null && !sanPham.getDanhSachHinhAnh().isEmpty()) {
                    HinhAnhResponse hinhAnh = sanPham.getDanhSachHinhAnh().get(0);

                    // Phân tích URL
                    String duongDanGoc = hinhAnh.getDuongDan();
                    String cleanPath = duongDanGoc != null ? duongDanGoc
                            .replace("/images/", "")
                            .replace("/hinh-anh/images/", "") : "";
                    String urlDayDu = hinhAnh.getUrlHinhAnh();

                    response.put("duongDanGoc", duongDanGoc);
                    response.put("duongDanSauChinh", cleanPath);
                    response.put("urlDayDu", urlDayDu);
                    response.put("coHinhAnh", true);
                    response.put("message", "Sản phẩm có hình ảnh");
                } else {
                    response.put("coHinhAnh", false);
                    response.put("message", "Sản phẩm chưa có hình ảnh");
                }

                logger.info("✅ [API] Kiểm tra hình ảnh sản phẩm thành công");
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi kiểm tra hình ảnh sản phẩm: ID {}", id, e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }
        // Thêm các endpoint này vào BanHangController

    // ===== MASTER DATA ENDPOINTS =====

        /**
         * Lấy danh sách danh mục
         */
        @Transactional
        @GetMapping("/master-data/danh-muc")
        public ResponseEntity<Map<String, Object>> layDanhSachDanhMuc() {
            try {
                logger.info("📂 [API] Lấy danh sách danh mục");
                List<DanhMucResponse> danhMucList = banHangService.layDanhSachDanhMuc();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Lấy danh sách danh mục thành công");
                response.put("data", danhMucList);
                response.put("total", danhMucList.size());

                logger.info("✅ [API] Lấy {} danh mục thành công", danhMucList.size());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi lấy danh sách danh mục", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Lấy danh sách thương hiệu
         */
        @Transactional
        @GetMapping("/master-data/thuong-hieu")
        public ResponseEntity<Map<String, Object>> layDanhSachThuongHieu() {
            try {
                logger.info("🏷️ [API] Lấy danh sách thương hiệu");
                List<ThuongHieuResponse> thuongHieuList = banHangService.layDanhSachThuongHieu();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Lấy danh sách thương hiệu thành công");
                response.put("data", thuongHieuList);
                response.put("total", thuongHieuList.size());

                logger.info("✅ [API] Lấy {} thương hiệu thành công", thuongHieuList.size());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi lấy danh sách thương hiệu", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Lấy danh sách màu sắc
         */
        @Transactional
        @GetMapping("/master-data/mau-sac")
        public ResponseEntity<Map<String, Object>> layDanhSachMauSac() {
            try {
                logger.info("🎨 [API] Lấy danh sách màu sắc");
                List<MauSacResponse> mauSacList = banHangService.layDanhSachMauSac();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Lấy danh sách màu sắc thành công");
                response.put("data", mauSacList);
                response.put("total", mauSacList.size());

                logger.info("✅ [API] Lấy {} màu sắc thành công", mauSacList.size());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi lấy danh sách màu sắc", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Lấy danh sách kích cỡ
         */
        @Transactional
        @GetMapping("/master-data/kich-co")
        public ResponseEntity<Map<String, Object>> layDanhSachKichCo() {
            try {
                logger.info("📏 [API] Lấy danh sách kích cỡ");
                List<KichCoResponse> kichCoList = banHangService.layDanhSachKichCo();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Lấy danh sách kích cỡ thành công");
                response.put("data", kichCoList);
                response.put("total", kichCoList.size());

                logger.info("✅ [API] Lấy {} kích cỡ thành công", kichCoList.size());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi lấy danh sách kích cỡ", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Lấy danh sách chất liệu
         */
        @Transactional
        @GetMapping("/master-data/chat-lieu")
        public ResponseEntity<Map<String, Object>> layDanhSachChatLieu() {
            try {
                logger.info("🧵 [API] Lấy danh sách chất liệu");
                List<ChatLieuResponse> chatLieuList = banHangService.layDanhSachChatLieu();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Lấy danh sách chất liệu thành công");
                response.put("data", chatLieuList);
                response.put("total", chatLieuList.size());

                logger.info("✅ [API] Lấy {} chất liệu thành công", chatLieuList.size());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi lấy danh sách chất liệu", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Lấy danh sách đế giày
         */
        @Transactional
        @GetMapping("/master-data/de-giay")
        public ResponseEntity<Map<String, Object>> layDanhSachDeGiay() {
            try {
                logger.info("👟 [API] Lấy danh sách đế giày");
                List<DeGiayResponse> deGiayList = banHangService.layDanhSachDeGiay();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Lấy danh sách đế giày thành công");
                response.put("data", deGiayList);
                response.put("total", deGiayList.size());

                logger.info("✅ [API] Lấy {} đế giày thành công", deGiayList.size());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi lấy danh sách đế giày", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Lấy tất cả master data trong một lần gọi
         */
        @Transactional
        @GetMapping("/master-data/all")
        public ResponseEntity<Map<String, Object>> layTatCaMasterData() {
            try {
                logger.info("📦 [API] Lấy tất cả master data");

                Map<String, Object> allMasterData = new HashMap<>();
                allMasterData.put("danhMuc", banHangService.layDanhSachDanhMuc());
                allMasterData.put("thuongHieu", banHangService.layDanhSachThuongHieu());
                allMasterData.put("mauSac", banHangService.layDanhSachMauSac());
                allMasterData.put("kichCo", banHangService.layDanhSachKichCo());
                allMasterData.put("chatLieu", banHangService.layDanhSachChatLieu());
                allMasterData.put("deGiay", banHangService.layDanhSachDeGiay());

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Lấy tất cả master data thành công");
                response.put("data", allMasterData);

                logger.info("✅ [API] Lấy tất cả master data thành công");
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi lấy tất cả master data", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }
        @Transactional
        @PostMapping("/hoa-don-cho/tao-moi")
        public ResponseEntity<Map<String, Object>> taoHoaDonCho(@RequestParam("nhanVienId") String nhanVienIdStr) {
            try {
                logger.info("🆕 [API] Tạo hóa đơn chờ mới cho nhân viên: '{}'", nhanVienIdStr);

                Integer nhanVienId = null;

                // Kiểm tra xem có phải là số không
                try {
                    nhanVienId = Integer.parseInt(nhanVienIdStr);
                    logger.info("✅ Sử dụng ID nhân viên trực tiếp: {}", nhanVienId);

                } catch (NumberFormatException e) {
                    // Nếu không phải số, sử dụng method linh hoạt
                    logger.info("🔄 Tìm ID nhân viên từ mã: '{}'", nhanVienIdStr);

                    // ✅ SỬA: Sử dụng method linh hoạt mới
                    nhanVienId = banHangService.timNhanVienIdLinhHoat(nhanVienIdStr);

                    if (nhanVienId == null) {
                        // Debug thông tin
                        String maNhanVien = banHangService.chuyenDoiMaTaiKhoanSangMaNhanVien(nhanVienIdStr);

                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("success", false);
                        errorResponse.put("message", "Không tìm thấy nhân viên với mã: " + nhanVienIdStr);
                        errorResponse.put("debug", Map.of(
                                "inputMa", nhanVienIdStr,
                                "maNhanVienTuongUng", maNhanVien != null ? maNhanVien : "null",
                                "loaiTimKiem", "Tìm theo mã tài khoản và mã nhân viên"
                        ));

                        logger.warn("❌ Không tìm thấy nhân viên: input='{}', maNV='{}'",
                                nhanVienIdStr, maNhanVien);

                        return ResponseEntity.badRequest().body(errorResponse);
                    } else {
                        logger.info("✅ Tìm thấy nhân viên ID: {}", nhanVienId);
                    }
                }

                // Tạo hóa đơn với ID nhân viên đã tìm được
                HoaDonChoResponse response = banHangService.taoHoaDonCho(nhanVienId);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Tạo hóa đơn chờ thành công");
                result.put("data", response);
                result.put("debug", Map.of(
                        "inputMa", nhanVienIdStr,
                        "nhanVienId", nhanVienId,
                        "maHoaDon", response.getMaHoaDon()
                ));

                logger.info("✅ [API] Tạo hóa đơn chờ thành công: {}", response.getMaHoaDon());
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi tạo hóa đơn chờ", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                errorResponse.put("inputMa", nhanVienIdStr);
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }
        @Transactional
        @GetMapping("/debug/chuyen-doi-ma/{ma}")
        public ResponseEntity<Map<String, Object>> debugChuyenDoiMa(@PathVariable String ma) {
            try {
                logger.info("🔍 [DEBUG] Test chuyển đổi mã: '{}'", ma);

                Map<String, Object> result = new HashMap<>();
                result.put("inputMa", ma);

                // Test chuyển đổi mã tài khoản -> mã nhân viên
                String maNhanVien = banHangService.chuyenDoiMaTaiKhoanSangMaNhanVien(ma);
                result.put("maNhanVienFromMaTaiKhoan", maNhanVien);

                // Test tìm ID nhân viên linh hoạt
                Integer nhanVienId = banHangService.timNhanVienIdLinhHoat(ma);
                result.put("nhanVienIdLinhHoat", nhanVienId);

                // Test tìm theo mã nhân viên trực tiếp
                Integer nhanVienIdByMaNV = banHangService.timNhanVienIdTheoMa(ma);
                result.put("nhanVienIdByMaNV", nhanVienIdByMaNV);

                // Lấy thông tin nhân viên nếu tìm thấy
                if (nhanVienId != null) {
                    // Có thể thêm thông tin chi tiết nhân viên ở đây
                    result.put("timThay", true);
                } else {
                    result.put("timThay", false);
                }

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Debug chuyển đổi mã");
                response.put("data", result);

                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [DEBUG] Lỗi debug chuyển đổi mã", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }
        /**
         * Tạo hóa đơn chờ theo ID nhân viên
         */
        @Transactional
        @PostMapping("/hoa-don-cho/tao-moi-by-id")
        public ResponseEntity<Map<String, Object>> taoHoaDonChoById(@RequestParam Integer nhanVienId) {
            try {
                logger.info("🆕 [API] Tạo hóa đơn chờ mới cho nhân viên ID: {}", nhanVienId);
                HoaDonChoResponse response = banHangService.taoHoaDonCho(nhanVienId);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Tạo hóa đơn chờ thành công");
                result.put("data", response);

                logger.info("✅ [API] Tạo hóa đơn chờ thành công: {}", response.getMaHoaDon());
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi tạo hóa đơn chờ", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * Tạo hóa đơn chờ theo mã nhân viên
         */
        @Transactional
        @PostMapping("/hoa-don-cho/tao-moi-by-ma")
        public ResponseEntity<Map<String, Object>> taoHoaDonChoByMa(@RequestParam String maNhanVien) {
            try {
                logger.info("🆕 [API] Tạo hóa đơn chờ mới cho nhân viên mã: {}", maNhanVien);

                Integer nhanVienId = banHangService.timNhanVienIdTheoMa(maNhanVien);
                if (nhanVienId == null) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "Không tìm thấy nhân viên với mã: " + maNhanVien);
                    return ResponseEntity.badRequest().body(errorResponse);
                }

                HoaDonChoResponse response = banHangService.taoHoaDonCho(nhanVienId);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Tạo hóa đơn chờ thành công");
                result.put("data", response);

                logger.info("✅ [API] Tạo hóa đơn chờ thành công: {}", response.getMaHoaDon());
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi tạo hóa đơn chờ", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }
        @Transactional
        @GetMapping("/debug/nhan-vien")
        public ResponseEntity<Map<String, Object>> debugNhanVien() {
            try {
                logger.info("🔍 [DEBUG] Kiểm tra dữ liệu nhân viên");

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
                            info.put("trangThaiText", nv.getTrangThai() == 1 ? "Hoạt động" : "Không hoạt động");
                            return info;
                        })
                        .collect(Collectors.toList());

                debugInfo.put("danhSachNhanVien", danhSachNhanVien);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Debug thông tin nhân viên");
                response.put("data", debugInfo);

                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [DEBUG] Lỗi debug nhân viên", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        // ✅ THÊM: Endpoint test tìm kiếm cụ thể
        @Transactional
        @GetMapping("/debug/nhan-vien/tim-kiem/{ma}")
        public ResponseEntity<Map<String, Object>> debugTimKiemNhanVien(@PathVariable String ma) {
            try {
                logger.info("🔍 [DEBUG] Test tìm kiếm nhân viên với mã: '{}'", ma);

                Map<String, Object> ketQua = new HashMap<>();
                ketQua.put("maTimKiem", ma);

                // Test các cách tìm kiếm khác nhau
                Optional<NhanVien> byMaNhanVien = nhanVienBHRepository.findByMaNhanVienAndTrangThai(ma, 1);
                Optional<NhanVien> byMaTaiKhoan = nhanVienBHRepository.findByTaiKhoan_MaTaiKhoanAndTrangThai(ma, 1);
                Optional<NhanVien> byMaLinhHoat = nhanVienBHRepository.findByMaNhanVienOrMaTaiKhoanAndTrangThai(ma, 1);

                ketQua.put("timTheoMaNhanVien", byMaNhanVien.isPresent() ?
                        mapNhanVienToDebugInfo(byMaNhanVien.get()) : "Không tìm thấy");

                ketQua.put("timTheoMaTaiKhoan", byMaTaiKhoan.isPresent() ?
                        mapNhanVienToDebugInfo(byMaTaiKhoan.get()) : "Không tìm thấy");

                ketQua.put("timLinhHoat", byMaLinhHoat.isPresent() ?
                        mapNhanVienToDebugInfo(byMaLinhHoat.get()) : "Không tìm thấy");

                // Test service method
                Integer nhanVienId = banHangService.timNhanVienIdTheoMa(ma);
                ketQua.put("timBangService", nhanVienId != null ? nhanVienId : "Không tìm thấy");

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Test tìm kiếm nhân viên");
                response.put("data", ketQua);

                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [DEBUG] Lỗi test tìm kiếm", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
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
                logger.info("🧪 [DEBUG] Test voucher với tongTien: {}", tongTien);

                List<VoucherResponse> vouchers = banHangService.layDanhSachVoucherKhaDung(null, tongTien);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("tongTien", tongTien);
                response.put("soLuongVoucher", vouchers.size());
                response.put("vouchers", vouchers);

                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [DEBUG] Lỗi test voucher", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }
        @ControllerAdvice
        public class GlobalExceptionHandler {

            private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

            @ExceptionHandler(UnexpectedRollbackException.class)
            public ResponseEntity<Map<String, Object>> handleUnexpectedRollback(UnexpectedRollbackException ex) {
                logger.error("❌ Transaction rollback error", ex);

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi xử lý giao dịch");
                errorResponse.put("error", "TRANSACTION_ROLLBACK");

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            @ExceptionHandler(DataAccessException.class)
            public ResponseEntity<Map<String, Object>> handleDataAccessException(DataAccessException ex) {
                logger.error("❌ Database access error", ex);

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi truy cập dữ liệu");

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }
//        // ===== GIAO HÀNG ENDPOINTS =====
//
//        @PostMapping("/hoa-don-cho/{id}/chuyen-giao-hang")
//        public ResponseEntity<Map<String, Object>> chuyenSangGiaoHang(
//                @PathVariable Integer id,
//                @RequestBody @Valid GiaoHangRequest request) {
//            try {
//                logger.info("🚚 [API] Chuyển hóa đơn {} sang giao hàng", id);
//
//                HoaDonChoTongQuanResponse response = banHangService.chuyenSangGiaoHang(id, request);
//
//                Map<String, Object> result = new HashMap<>();
//                result.put("success", true);
//                result.put("message", "Chuyển sang giao hàng thành công");
//                result.put("data", response);
//
//                return ResponseEntity.ok(result);
//
//            } catch (Exception e) {
//                logger.error("❌ [API] Lỗi chuyển giao hàng", e);
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("message", "Lỗi: " + e.getMessage());
//                return ResponseEntity.badRequest().body(errorResponse);
//            }
//        }
//
//        @PostMapping("/tinh-phi-ship")
//        public ResponseEntity<Map<String, Object>> tinhPhiShip(@RequestBody @Valid TinhPhiShipRequest request) {
//            try {
//                logger.info("💰 [API] Tính phí ship");
//
//                TinhPhiShipResponse response = banHangService.tinhPhiShip(request);
//
//                Map<String, Object> result = new HashMap<>();
//                result.put("success", true);
//                result.put("message", "Tính phí ship thành công");
//                result.put("data", response);
//
//                return ResponseEntity.ok(result);
//
//            } catch (Exception e) {
//                logger.error("❌ [API] Lỗi tính phí ship", e);
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("message", "Lỗi: " + e.getMessage());
//                return ResponseEntity.badRequest().body(errorResponse);
//            }
//        }
//
//        @PutMapping("/hoa-don-cho/{id}/cap-nhat-giao-hang")
//        public ResponseEntity<Map<String, Object>> capNhatThongTinGiaoHang(
//                @PathVariable Integer id,
//                @RequestBody CapNhatGiaoHangRequest request) {
//            try {
//                logger.info("✏️ [API] Cập nhật thông tin giao hàng hóa đơn {}", id);
//
//                HoaDonChoTongQuanResponse response = banHangService.capNhatThongTinGiaoHang(id, request);
//
//                Map<String, Object> result = new HashMap<>();
//                result.put("success", true);
//                result.put("message", "Cập nhật thông tin giao hàng thành công");
//                result.put("data", response);
//
//                return ResponseEntity.ok(result);
//
//            } catch (Exception e) {
//                logger.error("❌ [API] Lỗi cập nhật giao hàng", e);
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("message", "Lỗi: " + e.getMessage());
//                return ResponseEntity.badRequest().body(errorResponse);
//            }
//        }
//
//        @PostMapping("/hoa-don-cho/{id}/xac-nhan-giao-hang")
//        public ResponseEntity<Map<String, Object>> xacNhanGiaoHang(@PathVariable Integer id) {
//            try {
//                logger.info("✅ [API] Xác nhận giao hàng hóa đơn {}", id);
//
//                HoaDonResponse response = banHangService.xacNhanGiaoHang(id);
//
//                Map<String, Object> result = new HashMap<>();
//                result.put("success", true);
//                result.put("message", "Xác nhận giao hàng thành công");
//                result.put("data", response);
//
//                return ResponseEntity.ok(result);
//
//            } catch (Exception e) {
//                logger.error("❌ [API] Lỗi xác nhận giao hàng", e);
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("message", "Lỗi: " + e.getMessage());
//                return ResponseEntity.badRequest().body(errorResponse);
//            }
//        }
//
//        @DeleteMapping("/hoa-don-cho/{id}/huy-giao-hang")
//        public ResponseEntity<Map<String, Object>> huyGiaoHang(@PathVariable Integer id) {
//            try {
//                logger.info("🚫 [API] Hủy giao hàng hóa đơn {}", id);
//
//                HoaDonChoTongQuanResponse response = banHangService.huyGiaoHang(id);
//
//                Map<String, Object> result = new HashMap<>();
//                result.put("success", true);
//                result.put("message", "Hủy giao hàng thành công");
//                result.put("data", response);
//
//                return ResponseEntity.ok(result);
//
//            } catch (Exception e) {
//                logger.error("❌ [API] Lỗi hủy giao hàng", e);
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("message", "Lỗi: " + e.getMessage());
//                return ResponseEntity.badRequest().body(errorResponse);
//            }
//        }
//
//        @PutMapping("/hoa-don/{id}/dang-giao")
//        public ResponseEntity<Map<String, Object>> capNhatDangGiao(@PathVariable Integer id) {
//            try {
//                logger.info("🚚 [API] Cập nhật đang giao hóa đơn {}", id);
//
//                HoaDonResponse response = banHangService.capNhatDangGiao(id);
//
//                Map<String, Object> result = new HashMap<>();
//                result.put("success", true);
//                result.put("message", "Cập nhật đang giao thành công");
//                result.put("data", response);
//
//                return ResponseEntity.ok(result);
//
//            } catch (Exception e) {
//                logger.error("❌ [API] Lỗi cập nhật đang giao", e);
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("message", "Lỗi: " + e.getMessage());
//                return ResponseEntity.badRequest().body(errorResponse);
//            }
//        }
//
//        @PutMapping("/hoa-don/{id}/da-giao")
//        public ResponseEntity<Map<String, Object>> xacNhanDaGiao(@PathVariable Integer id) {
//            try {
//                logger.info("✅ [API] Xác nhận đã giao hóa đơn {}", id);
//
//                HoaDonResponse response = banHangService.xacNhanDaGiao(id);
//
//                Map<String, Object> result = new HashMap<>();
//                result.put("success", true);
//                result.put("message", "Xác nhận đã giao thành công");
//                result.put("data", response);
//
//                return ResponseEntity.ok(result);
//
//            } catch (Exception e) {
//                logger.error("❌ [API] Lỗi xác nhận đã giao", e);
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("message", "Lỗi: " + e.getMessage());
//                return ResponseEntity.badRequest().body(errorResponse);
//            }
//        }
//
//        @GetMapping("/hoa-don/dang-giao")
//        public ResponseEntity<Map<String, Object>> layDanhSachDangGiao(
//                @RequestParam(defaultValue = "0") int page,
//                @RequestParam(defaultValue = "10") int size) {
//            try {
//                logger.info("📋 [API] Lấy danh sách đơn đang giao");
//
//                List<HoaDon> hoaDons = hoaDonRepository.findByTrangThaiHoaDonIn(
//                        Arrays.asList("Đã xác nhận", "Đang giao")
//                );
//
//                List<HoaDonResponse> responses = hoaDons.stream()
//                        .map(this::mapToHoaDonResponse)
//                        .collect(Collectors.toList());
//
//                Map<String, Object> result = new HashMap<>();
//                result.put("success", true);
//                result.put("message", "Lấy danh sách thành công");
//                result.put("data", responses);
//                result.put("total", responses.size());
//
//                return ResponseEntity.ok(result);
//
//            } catch (Exception e) {
//                logger.error("❌ [API] Lỗi lấy danh sách đang giao", e);
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("message", "Lỗi: " + e.getMessage());
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//            }
//        }

        @Transactional
        @PostMapping("/hoa-don-cho/{id}/thanh-toan-chi-tiet")
        public ResponseEntity<Map<String, Object>> thanhToanHoaDonChiTiet(
                @PathVariable Integer id,
                @RequestBody @Valid ThanhToanRequest request) {
            try {
                logger.info("💰 [API] Thanh toán chi tiết hóa đơn: ID {} - Phương thức: {}", id, request.getPhuongThucThanhToan());

                // Validate phương thức thanh toán
                if (!Arrays.asList("TIEN_MAT", "CHUYEN_KHOAN", "KET_HOP").contains(request.getPhuongThucThanhToan())) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "Phương thức thanh toán không hợp lệ");
                    return ResponseEntity.badRequest().body(errorResponse);
                }

                ThanhToanResponse response = banHangService.thanhToanHoaDonChiTiet(id, request);

                Map<String, Object> result = new HashMap<>();
                result.put("success", response.getThanhCong());
                result.put("message", response.getThanhCong() ? "Thanh toán thành công" : "Thanh toán thất bại");
                result.put("data", response);

                // Thêm thông tin bổ sung cho frontend
                if (response.getThanhCong()) {
                    result.put("thongBao", response.getThongBaoThanhToan());
                    result.put("phuongThucThanhToan", response.getPhuongThucThanhToan());

                    // Thông tin tiền thừa cho tiền mặt
                    if ("TIEN_MAT".equals(response.getPhuongThucThanhToan()) || "KET_HOP".equals(response.getPhuongThucThanhToan())) {
                        if (response.getTienThua() != null && response.getTienThua().compareTo(BigDecimal.ZERO) > 0) {
                            result.put("coTienThua", true);
                            result.put("tienThua", response.getTienThua());
                        }
                    }
                }

                logger.info("✅ [API] Thanh toán chi tiết thành công: {}", response.getMaHoaDon());
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi thanh toán chi tiết: ID {}", id, e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi thanh toán: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * Kiểm tra phương thức thanh toán hợp lệ
         */
        @GetMapping("/phuong-thuc-thanh-toan")
        public ResponseEntity<Map<String, Object>> layPhuongThucThanhToan() {
            try {
                List<Map<String, String>> phuongThucList = Arrays.asList(
                        Map.of("ma", "TIEN_MAT", "ten", "Tiền mặt", "moTa", "Thanh toán bằng tiền mặt"),
                        Map.of("ma", "CHUYEN_KHOAN", "ten", "Chuyển khoản", "moTa", "Thanh toán bằng chuyển khoản ngân hàng"),
                        Map.of("ma", "KET_HOP", "ten", "Kết hợp", "moTa", "Thanh toán kết hợp tiền mặt và chuyển khoản")
                );

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Lấy phương thức thanh toán thành công");
                response.put("data", phuongThucList);

                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi lấy phương thức thanh toán", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }

        /**
         * Tính tiền thừa khi thanh toán tiền mặt
         */
        @PostMapping("/tinh-tien-thua")
        public ResponseEntity<Map<String, Object>> tinhTienThua(@RequestBody Map<String, Object> request) {
            try {
                BigDecimal tongTien = new BigDecimal(request.get("tongTien").toString());
                BigDecimal tienKhachDua = new BigDecimal(request.get("tienKhachDua").toString());

                if (tienKhachDua.compareTo(tongTien) < 0) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "Số tiền khách đưa không đủ");
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
                logger.error("❌ [API] Lỗi tính tiền thừa", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi tính toán: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        /**
         * Validate thông tin thanh toán trước khi thực hiện
         */
        @PostMapping("/validate-thanh-toan")
        public ResponseEntity<Map<String, Object>> validateThanhToan(@RequestBody @Valid ThanhToanRequest request) {
            try {
                logger.info("🔍 [API] Validate thông tin thanh toán");

                List<String> errors = new ArrayList<>();

                // Validate phương thức thanh toán
                if (!Arrays.asList("TIEN_MAT", "CHUYEN_KHOAN", "KET_HOP").contains(request.getPhuongThucThanhToan())) {
                    errors.add("Phương thức thanh toán không hợp lệ");
                }

                // Validate theo từng phương thức
                switch (request.getPhuongThucThanhToan()) {
                    case "TIEN_MAT":
                        if (request.getTienMat() == null || request.getTienMat().compareTo(BigDecimal.ZERO) <= 0) {
                            errors.add("Số tiền mặt phải lớn hơn 0");
                        }
                        break;

                    case "CHUYEN_KHOAN":
                        if (request.getTienChuyenKhoan() == null || request.getTienChuyenKhoan().compareTo(BigDecimal.ZERO) <= 0) {
                            errors.add("Số tiền chuyển khoản phải lớn hơn 0");
                        }
                        break;

                    case "KET_HOP":
                        BigDecimal tienMat = request.getTienMat() != null ? request.getTienMat() : BigDecimal.ZERO;
                        BigDecimal tienCK = request.getTienChuyenKhoan() != null ? request.getTienChuyenKhoan() : BigDecimal.ZERO;

                        if (tienMat.compareTo(BigDecimal.ZERO) <= 0 && tienCK.compareTo(BigDecimal.ZERO) <= 0) {
                            errors.add("Phải có ít nhất một phương thức thanh toán có giá trị > 0");
                        }
                        break;
                }

                Map<String, Object> response = new HashMap<>();
                response.put("success", errors.isEmpty());
                response.put("message", errors.isEmpty() ? "Thông tin thanh toán hợp lệ" : "Có lỗi trong thông tin thanh toán");
                response.put("errors", errors);

                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("❌ [API] Lỗi validate thanh toán", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi validation: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }
        @GetMapping("/hoa-don-cho/{hoaDonId}/kiem-tra-ton-kho")
        public ResponseEntity<List<InventoryCheckResponse>> kiemTraTonKhoTruocThanhToan(@PathVariable Integer hoaDonId) {
            try {
                List<InventoryCheckResponse> result = banHangService.kiemTraTonKhoTruocThanhToan(hoaDonId);
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                log.error("Lỗi kiểm tra tồn kho trước thanh toán: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }
