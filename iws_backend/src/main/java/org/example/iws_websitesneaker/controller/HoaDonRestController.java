package org.example.iws_websitesneaker.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.iws_websitesneaker.Dto.*;
import org.example.iws_websitesneaker.Service.*;
import org.example.iws_websitesneaker.Service.impl.HoaDonChiTietServiceImpl;
import org.example.iws_websitesneaker.entity.KhachHang;
import org.example.iws_websitesneaker.entity.TaiKhoan;
import org.example.iws_websitesneaker.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/hoa-don")
@CrossOrigin(origins = "http://localhost:5173")
public class HoaDonRestController {

    @Autowired
    private ChiTietTraHangService chiTietTraHangService;
    @Autowired
    private HoaDonService hoaDonService;
    @Autowired
    private KhachHangService khachHangService;
    @Autowired
    private TaiKhoanService taiKhoanService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private HoaDonChiTietServiceImpl hoaDonChiTietServiceImpl;
    @Autowired(required = false)
    private UploadImageService uploadImageService;
    @Autowired
    private ChiTietVoucherService chiTietVoucherService;

    @GetMapping
    public ResponseEntity<List<HoaDonDTO>> getAllHoaDons() {
        return ResponseEntity.ok(hoaDonService.getAllHoaDons());
    }

    @GetMapping("/pos")
    public ResponseEntity<List<HoaDonDTO>> getPOSInvoices() {
        return ResponseEntity.ok(hoaDonService.getPOSInvoices());
    }

    @GetMapping("/online")
    public ResponseEntity<List<HoaDonDTO>> getOnlineInvoices() {
        return ResponseEntity.ok(hoaDonService.getOnlineInvoices());
    }
    @PostMapping("/create")
    public ResponseEntity<?> createHoaDon(@RequestBody CreateHoaDonRequest request) {
        try {
            HoaDonDTO hoaDon = hoaDonService.createHoaDonFromCheckout(request);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", hoaDon);
            response.put("message", "Tạo hóa đơn thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(HttpServletRequest request) {
        try {
            // Lấy token từ header
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token không tồn tại"));
            }

            // Lấy email từ token
            String email = jwtUtil.extractEmail(token);
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token không hợp lệ"));
            }

            // Tìm tài khoản qua email
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findByEmail(email);
            if (taiKhoanOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy tài khoản"));
            }

            // Tìm khách hàng qua tài khoản
            KhachHang khachHang = khachHangService.findByTaiKhoanId(taiKhoanOpt.get().getId());
            if (khachHang == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy khách hàng"));
            }

            // Lấy danh sách hóa đơn của khách hàng
            List<HoaDonDTO> hoaDons = hoaDonService.getHoaDonsByKhachHangId(khachHang.getId());

            return ResponseEntity.ok(hoaDons);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi server: " + e.getMessage()));
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<HoaDonDTO> getHoaDonById(@PathVariable Integer id) {
        return ResponseEntity.ok(hoaDonService.getHoaDonById(id));
    }

    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Integer id,
                                                            @RequestBody StatusUpdateRequest request) {
        try {
            HoaDonDTO updatedHoaDon = hoaDonService.updateStatus(id, request);

            // ✅ FIX: Return consistent response format
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedHoaDon);
            response.put("message", "Cập nhật trạng thái thành công");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PutMapping("/{id}/xac-nhan")
    public ResponseEntity<Map<String, Object>> confirmInvoice(@PathVariable Integer id) {
        try {
            HoaDonDTO updatedHoaDon = hoaDonService.confirmInvoice(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedHoaDon);
            response.put("message", "Xác nhận đơn hàng thành công");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PutMapping("/{id}/hoan-thanh")
    public ResponseEntity<Map<String, Object>> completeInvoice(@PathVariable Integer id) {
        try {
            HoaDonDTO updatedHoaDon = hoaDonService.completeInvoice(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedHoaDon);
            response.put("message", "Hoàn thành đơn hàng thành công");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PutMapping("/{id}/huy")
    public ResponseEntity<Map<String, Object>> cancelInvoice(@PathVariable Integer id,
                                                             @RequestBody Map<String, String> request) {
        try {
            String lyDo = request.get("lyDo");
            HoaDonDTO updatedHoaDon = hoaDonService.cancelInvoice(id, lyDo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedHoaDon);
            response.put("message", "Hủy đơn hàng thành công");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Thêm endpoint hủy đơn hàng cho khách hàng
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrderByCustomer(@PathVariable Integer id, HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token không tồn tại"));
            }

            String email = jwtUtil.extractEmail(token);
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findByEmail(email);
            if (taiKhoanOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token không hợp lệ"));
            }

            // Lấy thông tin hóa đơn
            HoaDonDTO hoaDon = hoaDonService.getHoaDonById(id);
            if (hoaDon == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy hóa đơn"));
            }

            TaiKhoan taiKhoan = taiKhoanOpt.get();

            // ADMIN -> cho phép
            if (!"ADMIN".equals(taiKhoan.getVaiTro())) {
                boolean hasAccess = false;

                // Khớp theo KhachHangId
                KhachHang khachHang = khachHangService.findByTaiKhoanId(taiKhoan.getId());
                if (khachHang != null && hoaDon.getKhachHangId() != null &&
                        khachHang.getId().equals(hoaDon.getKhachHangId())) {
                    hasAccess = true;
                }

                // Khớp theo email (đơn guest)
                if (!hasAccess && hoaDon.getEmail() != null && hoaDon.getEmail().equals(email)) {
                    hasAccess = true;
                }

                if (!hasAccess) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "Không có quyền hủy đơn hàng này"));
                }
            }

            // Trạng thái cho phép hủy
            String st = hoaDon.getTrangThaiHoaDon();
            if (!Set.of("CHO_XAC_NHAN", "PENDING").contains(st)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Không thể hủy đơn hàng ở trạng thái này"));
            }

            // Hủy đơn (ghi chú nguyên nhân)
            HoaDonDTO updatedHoaDon = hoaDonService.cancelInvoice(id, "Khách hàng yêu cầu hủy");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Hủy đơn hàng thành công");
            response.put("data", updatedHoaDon);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi server: " + e.getMessage()));
        }
    }
    @PostMapping("/validate-guest-contact")
    public ResponseEntity<?> validateGuestContact(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String sdt = request.get("sdt");

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email không được để trống"));
            }

            if (sdt == null || sdt.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Số điện thoại không được để trống"));
            }

            // Simple validation using existing repositories
            boolean emailInTaiKhoan = taiKhoanService.findByEmail(email.trim()).isPresent();
            boolean sdtInKhachHang = khachHangService.existsBySdt(sdt.trim());

            if (emailInTaiKhoan || sdtInKhachHang) {
                return ResponseEntity.status(409).body(Map.of(
                        "error", "Email hoặc số điện thoại đã tồn tại trong hệ thống",
                        "suggestion", "Bạn có thể đã có tài khoản. Vui lòng đăng nhập để được hưởng ưu đãi!"
                ));
            }

            return ResponseEntity.ok(Map.of("valid", true, "message", "Thông tin hợp lệ"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi server: " + e.getMessage()));
        }
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // Endpoint lấy chi tiết trả hàng của hóa đơn
    @GetMapping("/{id}/tra-hang")
    public ResponseEntity<?> getChiTietTraHang(@PathVariable Integer id, HttpServletRequest request) {
        try {
            // Kiểm tra quyền truy cập (tương tự như endpoint chi-tiet)
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token không tồn tại"));
            }

            String email = jwtUtil.extractEmail(token);
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token không hợp lệ"));
            }

            // Lấy thông tin hóa đơn
            HoaDonDTO hoaDon = hoaDonService.getHoaDonById(id);
            if (hoaDon == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy hóa đơn"));
            }

            // Kiểm tra quyền sở hữu
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findByEmail(email);
            if (taiKhoanOpt.isPresent()) {
                TaiKhoan taiKhoan = taiKhoanOpt.get();

                // Nếu không phải admin, kiểm tra quyền sở hữu
                if (!"ADMIN".equals(taiKhoan.getVaiTro())) {
                    KhachHang khachHang = khachHangService.findByTaiKhoanId(taiKhoan.getId());
                    if (khachHang == null || !khachHang.getId().equals(hoaDon.getKhachHangId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("error", "Không có quyền truy cập đơn hàng này"));
                    }
                }
            }

            // Lấy chi tiết trả hàng
            List<ChiTietTraHangDTO> chiTietTraHangList = chiTietTraHangService.getChiTietTraHangByHoaDon(id);
            Map<String, Object> returnStats = chiTietTraHangService.getReturnStatistics(id);

            // Tạo response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                    "hoaDon", hoaDon,
                    "chiTietTraHang", chiTietTraHangList,
                    "thongKeTraHang", returnStats
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi server: " + e.getMessage()));
        }
    }

    // Endpoint tạo yêu cầu trả hàng cho khách hàng
    @PostMapping("/{id}/tao-tra-hang")
    public ResponseEntity<?> createReturnRequest(@PathVariable Integer id,
                                                 @RequestBody Map<String, Object> request,
                                                 HttpServletRequest httpRequest) {
        try {
            // Kiểm tra quyền truy cập
            String token = extractTokenFromRequest(httpRequest);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token không tồn tại"));
            }

            String email = jwtUtil.extractEmail(token);
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findByEmail(email);
            if (taiKhoanOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token không hợp lệ"));
            }

            // Lấy thông tin hóa đơn
            HoaDonDTO hoaDon = hoaDonService.getHoaDonById(id);
            if (hoaDon == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy hóa đơn"));
            }

            // Kiểm tra quyền sở hữu
            KhachHang khachHang = khachHangService.findByTaiKhoanId(taiKhoanOpt.get().getId());
            if (khachHang == null || !khachHang.getId().equals(hoaDon.getKhachHangId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Không có quyền tạo yêu cầu trả hàng cho đơn hàng này"));
            }

            // Kiểm tra trạng thái hóa đơn (chỉ cho phép trả hàng khi COMPLETED)
            if (!isCompletedInvoiceStatus(hoaDon.getTrangThaiHoaDon())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Chỉ có thể trả hàng khi đơn hàng đã hoàn thành"));
            }

            // Lấy thông tin từ request
            Integer chiTietSanPhamId = (Integer) request.get("chiTietSanPhamId");
            Integer soLuong = (Integer) request.get("soLuong");
            String lyDo = (String) request.get("lyDo");
            String duongDanAnh = (String) request.get("duongDanAnh"); // ✅ THÊM

            if (chiTietSanPhamId == null || soLuong == null || soLuong <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Thông tin sản phẩm và số lượng không hợp lệ"));
            }

            // ✅ THÊM: Validate lý do trả hàng
            if (lyDo == null || lyDo.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Vui lòng nhập lý do trả hàng"));
            }

            // Tạo DTO cho chi tiết trả hàng
            ChiTietTraHangDTO chiTietTraHangDTO = new ChiTietTraHangDTO();
            chiTietTraHangDTO.setChiTietSanPhamId(chiTietSanPhamId);
            chiTietTraHangDTO.setSoLuong(soLuong);
            chiTietTraHangDTO.setLyDo(lyDo); // ✅ THÊM
            chiTietTraHangDTO.setDuongDanAnh(duongDanAnh); // ✅ THÊM
            chiTietTraHangDTO.setHoaDonId(id);
            chiTietTraHangDTO.setTrangThaiHoaDon("PENDING");

            // Tạo yêu cầu trả hàng
            ChiTietTraHangDTO created = chiTietTraHangService.createChiTietTraHang(chiTietTraHangDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tạo yêu cầu trả hàng thành công");
            response.put("data", created);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi server: " + e.getMessage()));
        }
    }
    @PostMapping("/{id}/tao-tra-hang-with-image")
    public ResponseEntity<?> createReturnRequestWithImage(
            @PathVariable Integer id,
            @RequestParam("chiTietSanPhamId") Integer chiTietSanPhamId,
            @RequestParam("soLuong") Integer soLuong,
            @RequestParam("lyDo") String lyDo,
            @RequestParam(value = "anhMinhChung", required = false) MultipartFile anhMinhChung,
            HttpServletRequest httpRequest) {
        try {
            // Kiểm tra quyền truy cập (tương tự như endpoint trên)
            String token = extractTokenFromRequest(httpRequest);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token không tồn tại"));
            }

            String email = jwtUtil.extractEmail(token);
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findByEmail(email);
            if (taiKhoanOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token không hợp lệ"));
            }

            // Lấy thông tin hóa đơn
            HoaDonDTO hoaDon = hoaDonService.getHoaDonById(id);
            if (hoaDon == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy hóa đơn"));
            }

            // Kiểm tra quyền sở hữu
            KhachHang khachHang = khachHangService.findByTaiKhoanId(taiKhoanOpt.get().getId());
            if (khachHang == null || !khachHang.getId().equals(hoaDon.getKhachHangId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Không có quyền tạo yêu cầu trả hàng cho đơn hàng này"));
            }

            // Kiểm tra trạng thái hóa đơn
            if (!isCompletedInvoiceStatus(hoaDon.getTrangThaiHoaDon())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Chỉ có thể trả hàng khi đơn hàng đã hoàn thành"));
            }

            // Validate dữ liệu đầu vào
            if (chiTietSanPhamId == null || soLuong == null || soLuong <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Thông tin sản phẩm và số lượng không hợp lệ"));
            }

            if (lyDo == null || lyDo.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Vui lòng nhập lý do trả hàng"));
            }

            // Xử lý upload ảnh nếu có
            String duongDanAnh = null;
            if (anhMinhChung != null && !anhMinhChung.isEmpty()) {
                // Validate file ảnh
                String contentType = anhMinhChung.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "File upload phải là ảnh"));
                }

                // Kiểm tra kích thước file (max 5MB)
                if (anhMinhChung.getSize() > 5 * 1024 * 1024) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Kích thước ảnh không được vượt quá 5MB"));
                }

                // Upload ảnh (cần implement uploadImageService)
                duongDanAnh = uploadImageService.saveImage(anhMinhChung, "return-images");
            }

            // Tạo DTO cho chi tiết trả hàng
            ChiTietTraHangDTO chiTietTraHangDTO = new ChiTietTraHangDTO();
            chiTietTraHangDTO.setChiTietSanPhamId(chiTietSanPhamId);
            chiTietTraHangDTO.setSoLuong(soLuong);
            chiTietTraHangDTO.setLyDo(lyDo);
            chiTietTraHangDTO.setDuongDanAnh(duongDanAnh);
            chiTietTraHangDTO.setHoaDonId(id);
            chiTietTraHangDTO.setTrangThaiHoaDon("PENDING");

            // Tạo yêu cầu trả hàng
            ChiTietTraHangDTO created = chiTietTraHangService.createChiTietTraHang(chiTietTraHangDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tạo yêu cầu trả hàng thành công");
            response.put("data", created);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi server: " + e.getMessage()));
        }
    }
    @GetMapping("/{id}/voucher-details")
    public ResponseEntity<?> getVoucherDetailsByHoaDon(@PathVariable Integer id, HttpServletRequest request) {
        try {
            // Kiểm tra quyền truy cập (tương tự như endpoint chi-tiet)
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token không tồn tại"));
            }

            String email = jwtUtil.extractEmail(token);
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token không hợp lệ"));
            }

            // Lấy thông tin hóa đơn
            HoaDonDTO hoaDon = hoaDonService.getHoaDonById(id);
            if (hoaDon == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy hóa đơn"));
            }

            // Kiểm tra quyền sở hữu
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findByEmail(email);
            if (taiKhoanOpt.isPresent()) {
                TaiKhoan taiKhoan = taiKhoanOpt.get();

                // Nếu không phải admin, kiểm tra quyền sở hữu
                if (!"ADMIN".equals(taiKhoan.getVaiTro())) {
                    KhachHang khachHang = khachHangService.findByTaiKhoanId(taiKhoan.getId());
                    if (khachHang == null || !khachHang.getId().equals(hoaDon.getKhachHangId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("error", "Không có quyền truy cập đơn hàng này"));
                    }
                }
            }

            // Lấy chi tiết voucher
            List<ChiTietVoucherDTO> chiTietVoucherList = chiTietVoucherService.findByHoaDonId(id);

            // Tính tổng tiết kiệm
            BigDecimal tongTietKiem = chiTietVoucherList.stream()
                    .map(ChiTietVoucherDTO::getSoTienGiam)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Tạo response với thông tin voucher chi tiết
            Map<String, Object> voucherInfo = new HashMap<>();
            voucherInfo.put("chiTietVoucherList", chiTietVoucherList);
            voucherInfo.put("soLuongVoucher", chiTietVoucherList.size());
            voucherInfo.put("tongTietKiem", tongTietKiem);

            // Thống kê voucher theo loại
            Map<String, Long> thongKeTheoLoai = chiTietVoucherList.stream()
                    .collect(Collectors.groupingBy(
                            ChiTietVoucherDTO::getLoaiGiamGia,
                            Collectors.counting()
                    ));
            voucherInfo.put("thongKeTheoLoai", thongKeTheoLoai);

            // Tính % tiết kiệm nếu có voucher
            if (!chiTietVoucherList.isEmpty() && hoaDon.getTongTien() != null) {
                double phanTramTietKiem = tongTietKiem.divide(hoaDon.getTongTien(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();
                voucherInfo.put("phanTramTietKiem", Math.round(phanTramTietKiem * 100.0) / 100.0);
            } else {
                voucherInfo.put("phanTramTietKiem", 0.0);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                    "hoaDon", hoaDon,
                    "voucherInfo", voucherInfo
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi server: " + e.getMessage()));
        }
    }

    /**
     * ✅ CẬP NHẬT: Endpoint chi-tiet để bao gồm thông tin voucher
     */
    @GetMapping("/{id}/chi-tiet")
    public ResponseEntity<?> getHoaDonChiTiet(@PathVariable Integer id, HttpServletRequest request) {
        try {
            System.out.println("🔍 DEBUG chi-tiet - Hóa đơn ID: " + id);

            // Kiểm tra quyền truy cập
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token không tồn tại"));
            }

            String email = jwtUtil.extractEmail(token);
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token không hợp lệ"));
            }

            System.out.println("🔑 DEBUG chi-tiet - Email: " + email);

            // Lấy thông tin hóa đơn
            HoaDonDTO hoaDon = hoaDonService.getHoaDonById(id);
            if (hoaDon == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy hóa đơn"));
            }

            // DEBUG: In ra thông tin hóa đơn
            System.out.println("📋 DEBUG chi-tiet - Hóa đơn: " + hoaDon.getMaHoaDon());
            System.out.println("📋 DEBUG chi-tiet - KhachHangId từ DTO: " + hoaDon.getKhachHangId());
            System.out.println("📋 DEBUG chi-tiet - Email từ DTO: " + hoaDon.getEmail());

            // ✅ SỬA: Kiểm tra quyền sở hữu linh hoạt hơn
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findByEmail(email);
            if (taiKhoanOpt.isPresent()) {
                TaiKhoan taiKhoan = taiKhoanOpt.get();
                System.out.println("👤 DEBUG chi-tiet - TaiKhoan ID: " + taiKhoan.getId() + ", Role: " + taiKhoan.getVaiTro());

                // Nếu không phải admin, kiểm tra quyền sở hữu
                if (!"ADMIN".equals(taiKhoan.getVaiTro())) {
                    boolean hasAccess = false;

                    KhachHang khachHang = khachHangService.findByTaiKhoanId(taiKhoan.getId());
                    if (khachHang != null) {
                        System.out.println("👤 DEBUG chi-tiet - KhachHang ID: " + khachHang.getId());

                        // Kiểm tra nhiều điều kiện
                        if (hoaDon.getKhachHangId() != null && khachHang.getId().equals(hoaDon.getKhachHangId())) {
                            hasAccess = true;
                            System.out.println("✅ DEBUG chi-tiet - Khớp qua KhachHang ID");
                        } else if (hoaDon.getEmail() != null && hoaDon.getEmail().equals(email)) {
                            hasAccess = true;
                            System.out.println("✅ DEBUG chi-tiet - Khớp qua email");
                        } else {
                            System.out.println("❌ DEBUG chi-tiet - Không khớp điều kiện nào");
                            System.out.println("   - KhachHang.ID: " + khachHang.getId());
                            System.out.println("   - HoaDon.KhachHangId: " + hoaDon.getKhachHangId());
                            System.out.println("   - HoaDon.Email: " + hoaDon.getEmail());
                        }
                    } else {
                        System.out.println("❌ DEBUG chi-tiet - Không tìm thấy KhachHang");
                    }

                    if (!hasAccess) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("error", "Không có quyền truy cập đơn hàng này"));
                    }
                } else {
                    System.out.println("✅ DEBUG chi-tiet - Admin có quyền truy cập tất cả");
                }
            }

            // ✅ GIỮ NGUYÊN: Lấy chi tiết sản phẩm của hóa đơn (giống code cũ)
            List<HoaDonChiTietDTO> chiTietList = hoaDonChiTietServiceImpl.getChiTietByHoaDonId(id);

            // ✅ THÊM LẠI: Lấy chi tiết voucher và các thông tin khác như code gốc
            List<ChiTietVoucherDTO> chiTietVoucherList = null;
            BigDecimal tongTietKiemVoucher = BigDecimal.ZERO;

            try {
                // Thử lấy voucher info nếu service có sẵn
                if (chiTietVoucherService != null) {
                    chiTietVoucherList = chiTietVoucherService.findByHoaDonId(id);
                    tongTietKiemVoucher = chiTietVoucherList.stream()
                            .map(ChiTietVoucherDTO::getSoTienGiam)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    System.out.println("🎫 DEBUG chi-tiet - Số voucher: " + chiTietVoucherList.size());
                }
            } catch (Exception e) {
                System.out.println("⚠️ DEBUG chi-tiet - Không thể lấy voucher info: " + e.getMessage());
                chiTietVoucherList = new ArrayList<>();
            }

            // ✅ GIỮ NGUYÊN: Tạo response với đầy đủ thông tin như code gốc
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("hoaDon", hoaDon);
            dataMap.put("chiTietSanPham", chiTietList);

            // Thêm voucher info nếu có
            if (chiTietVoucherList != null) {
                dataMap.put("chiTietVoucher", chiTietVoucherList);
                dataMap.put("tongTietKiemVoucher", tongTietKiemVoucher);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", dataMap);

            System.out.println("✅ DEBUG chi-tiet - Trả về thành công với " + chiTietList.size() + " sản phẩm");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("💥 DEBUG chi-tiet - Lỗi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi server: " + e.getMessage()));
        }
    }
    @PostMapping("/api/hoa-don/{id}/cancel-by-customer")
    public ResponseEntity<?> cancelByCustomer(
            @PathVariable Integer id,
            HttpServletRequest request
    ) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token không tồn tại"));
            }
            String email = jwtUtil.extractEmail(token);

            HoaDonDTO updated = hoaDonService.cancelInvoiceByCustomer(id, "Khách hàng yêu cầu hủy", email);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Hủy đơn hàng thành công",
                    "data", updated
            ));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi server: " + e.getMessage()));
        }
    }

    private boolean isCompletedInvoiceStatus(String status) {
        return "COMPLETED".equals(normalizeInvoiceStatus(status));
    }

    private String normalizeInvoiceStatus(String status) {
        if (status == null || status.isBlank()) {
            return "";
        }

        String normalized = Normalizer.normalize(status.trim().toUpperCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('\u0110', 'D')
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");

        return switch (normalized) {
            case "DA_THANH_TOAN", "HOAN_THANH" -> "COMPLETED";
            default -> normalized;
        };
    }
}
