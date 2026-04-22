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
            response.put("message", "Táº¡o hÃ³a Ä‘Æ¡n thÃ nh cÃ´ng");
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
            // Láº¥y token tá»« header
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token khÃ´ng tá»“n táº¡i"));
            }

            // Láº¥y email tá»« token
            String email = jwtUtil.extractEmail(token);
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token khÃ´ng há»£p lá»‡"));
            }

            // TÃ¬m tÃ i khoáº£n qua email
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findByEmail(email);
            if (taiKhoanOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "KhÃ´ng tÃ¬m tháº¥y tÃ i khoáº£n"));
            }

            // TÃ¬m khÃ¡ch hÃ ng qua tÃ i khoáº£n
            KhachHang khachHang = khachHangService.findByTaiKhoanId(taiKhoanOpt.get().getId());
            if (khachHang == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "KhÃ´ng tÃ¬m tháº¥y khÃ¡ch hÃ ng"));
            }

            // Láº¥y danh sÃ¡ch hÃ³a Ä‘Æ¡n cá»§a khÃ¡ch hÃ ng
            List<HoaDonDTO> hoaDons = hoaDonService.getHoaDonsByKhachHangId(khachHang.getId());

            return ResponseEntity.ok(hoaDons);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lá»—i server: " + e.getMessage()));
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

            // âœ… FIX: Return consistent response format
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedHoaDon);
            response.put("message", "Cáº­p nháº­t tráº¡ng thÃ¡i thÃ nh cÃ´ng");

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
            response.put("message", "XÃ¡c nháº­n Ä‘Æ¡n hÃ ng thÃ nh cÃ´ng");

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
            response.put("message", "HoÃ n thÃ nh Ä‘Æ¡n hÃ ng thÃ nh cÃ´ng");

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
            response.put("message", "Há»§y Ä‘Æ¡n hÃ ng thÃ nh cÃ´ng");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ThÃªm endpoint há»§y Ä‘Æ¡n hÃ ng cho khÃ¡ch hÃ ng
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrderByCustomer(@PathVariable Integer id, HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token khÃ´ng tá»“n táº¡i"));
            }

            String email = jwtUtil.extractEmail(token);
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findByEmail(email);
            if (taiKhoanOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token khÃ´ng há»£p lá»‡"));
            }

            // Láº¥y thÃ´ng tin hÃ³a Ä‘Æ¡n
            HoaDonDTO hoaDon = hoaDonService.getHoaDonById(id);
            if (hoaDon == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));
            }

            TaiKhoan taiKhoan = taiKhoanOpt.get();

            // ADMIN -> cho phÃ©p
            if (!"ADMIN".equals(taiKhoan.getVaiTro())) {
                boolean hasAccess = false;

                // Khá»›p theo KhachHangId
                KhachHang khachHang = khachHangService.findByTaiKhoanId(taiKhoan.getId());
                if (khachHang != null && hoaDon.getKhachHangId() != null &&
                        khachHang.getId().equals(hoaDon.getKhachHangId())) {
                    hasAccess = true;
                }

                // Khá»›p theo email (Ä‘Æ¡n guest)
                if (!hasAccess && hoaDon.getEmail() != null && hoaDon.getEmail().equals(email)) {
                    hasAccess = true;
                }

                if (!hasAccess) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "KhÃ´ng cÃ³ quyá»n há»§y Ä‘Æ¡n hÃ ng nÃ y"));
                }
            }

            // Tráº¡ng thÃ¡i cho phÃ©p há»§y
            String st = hoaDon.getTrangThaiHoaDon();
            if (!Set.of("CHO_XAC_NHAN", "PENDING").contains(st)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "KhÃ´ng thá»ƒ há»§y Ä‘Æ¡n hÃ ng á»Ÿ tráº¡ng thÃ¡i nÃ y"));
            }

            // Há»§y Ä‘Æ¡n (ghi chÃº nguyÃªn nhÃ¢n)
            HoaDonDTO updatedHoaDon = hoaDonService.cancelInvoice(id, "KhÃ¡ch hÃ ng yÃªu cáº§u há»§y");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Há»§y Ä‘Æ¡n hÃ ng thÃ nh cÃ´ng");
            response.put("data", updatedHoaDon);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lá»—i server: " + e.getMessage()));
        }
    }
    @PostMapping("/validate-guest-contact")
    public ResponseEntity<?> validateGuestContact(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String sdt = request.get("sdt");

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng"));
            }

            if (sdt == null || sdt.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Sá»‘ Ä‘iá»‡n thoáº¡i khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng"));
            }

            // Simple validation using existing repositories
            boolean emailInTaiKhoan = taiKhoanService.findByEmail(email.trim()).isPresent();
            boolean sdtInKhachHang = khachHangService.existsBySdt(sdt.trim());

            if (emailInTaiKhoan || sdtInKhachHang) {
                return ResponseEntity.status(409).body(Map.of(
                        "error", "Email hoáº·c sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ tá»“n táº¡i trong há»‡ thá»‘ng",
                        "suggestion", "Báº¡n cÃ³ thá»ƒ Ä‘Ã£ cÃ³ tÃ i khoáº£n. Vui lÃ²ng Ä‘Äƒng nháº­p Ä‘á»ƒ Ä‘Æ°á»£c hÆ°á»Ÿng Æ°u Ä‘Ã£i!"
                ));
            }

            return ResponseEntity.ok(Map.of("valid", true, "message", "ThÃ´ng tin há»£p lá»‡"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Lá»—i server: " + e.getMessage()));
        }
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // Endpoint láº¥y chi tiáº¿t tráº£ hÃ ng cá»§a hÃ³a Ä‘Æ¡n
    @GetMapping("/{id}/tra-hang")
    public ResponseEntity<?> getChiTietTraHang(@PathVariable Integer id, HttpServletRequest request) {
        try {
            // Kiá»ƒm tra quyá»n truy cáº­p (tÆ°Æ¡ng tá»± nhÆ° endpoint chi-tiet)
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token khÃ´ng tá»“n táº¡i"));
            }

            String email = jwtUtil.extractEmail(token);
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token khÃ´ng há»£p lá»‡"));
            }

            // Láº¥y thÃ´ng tin hÃ³a Ä‘Æ¡n
            HoaDonDTO hoaDon = hoaDonService.getHoaDonById(id);
            if (hoaDon == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));
            }

            // Kiá»ƒm tra quyá»n sá»Ÿ há»¯u
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findByEmail(email);
            if (taiKhoanOpt.isPresent()) {
                TaiKhoan taiKhoan = taiKhoanOpt.get();

                // Náº¿u khÃ´ng pháº£i admin, kiá»ƒm tra quyá»n sá»Ÿ há»¯u
                if (!"ADMIN".equals(taiKhoan.getVaiTro())) {
                    KhachHang khachHang = khachHangService.findByTaiKhoanId(taiKhoan.getId());
                    if (khachHang == null || !khachHang.getId().equals(hoaDon.getKhachHangId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("error", "KhÃ´ng cÃ³ quyá»n truy cáº­p Ä‘Æ¡n hÃ ng nÃ y"));
                    }
                }
            }

            // Láº¥y chi tiáº¿t tráº£ hÃ ng
            List<ChiTietTraHangDTO> chiTietTraHangList = chiTietTraHangService.getChiTietTraHangByHoaDon(id);
            Map<String, Object> returnStats = chiTietTraHangService.getReturnStatistics(id);

            // Táº¡o response
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
                    .body(Map.of("error", "Lá»—i server: " + e.getMessage()));
        }
    }

    // Endpoint táº¡o yÃªu cáº§u tráº£ hÃ ng cho khÃ¡ch hÃ ng
    @PostMapping("/{id}/tao-tra-hang")
    public ResponseEntity<?> createReturnRequest(@PathVariable Integer id,
                                                 @RequestBody Map<String, Object> request,
                                                 HttpServletRequest httpRequest) {
        try {
            // Kiá»ƒm tra quyá»n truy cáº­p
            String token = extractTokenFromRequest(httpRequest);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token khÃ´ng tá»“n táº¡i"));
            }

            String email = jwtUtil.extractEmail(token);
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findByEmail(email);
            if (taiKhoanOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token khÃ´ng há»£p lá»‡"));
            }

            // Láº¥y thÃ´ng tin hÃ³a Ä‘Æ¡n
            HoaDonDTO hoaDon = hoaDonService.getHoaDonById(id);
            if (hoaDon == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));
            }

            // Kiá»ƒm tra quyá»n sá»Ÿ há»¯u
            KhachHang khachHang = khachHangService.findByTaiKhoanId(taiKhoanOpt.get().getId());
            if (khachHang == null || !khachHang.getId().equals(hoaDon.getKhachHangId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "KhÃ´ng cÃ³ quyá»n táº¡o yÃªu cáº§u tráº£ hÃ ng cho Ä‘Æ¡n hÃ ng nÃ y"));
            }

            // Kiá»ƒm tra tráº¡ng thÃ¡i hÃ³a Ä‘Æ¡n (chá»‰ cho phÃ©p tráº£ hÃ ng khi COMPLETED)
            if (!"COMPLETED".equals(hoaDon.getTrangThaiHoaDon())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Chá»‰ cÃ³ thá»ƒ tráº£ hÃ ng khi Ä‘Æ¡n hÃ ng Ä‘Ã£ hoÃ n thÃ nh"));
            }

            // Láº¥y thÃ´ng tin tá»« request
            Integer chiTietSanPhamId = (Integer) request.get("chiTietSanPhamId");
            Integer soLuong = (Integer) request.get("soLuong");
            String lyDo = (String) request.get("lyDo");
            String duongDanAnh = (String) request.get("duongDanAnh"); // âœ… THÃŠM

            if (chiTietSanPhamId == null || soLuong == null || soLuong <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "ThÃ´ng tin sáº£n pháº©m vÃ  sá»‘ lÆ°á»£ng khÃ´ng há»£p lá»‡"));
            }

            // âœ… THÃŠM: Validate lÃ½ do tráº£ hÃ ng
            if (lyDo == null || lyDo.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Vui lÃ²ng nháº­p lÃ½ do tráº£ hÃ ng"));
            }

            // Táº¡o DTO cho chi tiáº¿t tráº£ hÃ ng
            ChiTietTraHangDTO chiTietTraHangDTO = new ChiTietTraHangDTO();
            chiTietTraHangDTO.setChiTietSanPhamId(chiTietSanPhamId);
            chiTietTraHangDTO.setSoLuong(soLuong);
            chiTietTraHangDTO.setLyDo(lyDo); // âœ… THÃŠM
            chiTietTraHangDTO.setDuongDanAnh(duongDanAnh); // âœ… THÃŠM
            chiTietTraHangDTO.setHoaDonId(id);
            chiTietTraHangDTO.setTrangThaiHoaDon("PENDING");

            // Táº¡o yÃªu cáº§u tráº£ hÃ ng
            ChiTietTraHangDTO created = chiTietTraHangService.createChiTietTraHang(chiTietTraHangDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Táº¡o yÃªu cáº§u tráº£ hÃ ng thÃ nh cÃ´ng");
            response.put("data", created);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lá»—i server: " + e.getMessage()));
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
            // Kiá»ƒm tra quyá»n truy cáº­p (tÆ°Æ¡ng tá»± nhÆ° endpoint trÃªn)
            String token = extractTokenFromRequest(httpRequest);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token khÃ´ng tá»“n táº¡i"));
            }

            String email = jwtUtil.extractEmail(token);
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findByEmail(email);
            if (taiKhoanOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token khÃ´ng há»£p lá»‡"));
            }

            // Láº¥y thÃ´ng tin hÃ³a Ä‘Æ¡n
            HoaDonDTO hoaDon = hoaDonService.getHoaDonById(id);
            if (hoaDon == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));
            }

            // Kiá»ƒm tra quyá»n sá»Ÿ há»¯u
            KhachHang khachHang = khachHangService.findByTaiKhoanId(taiKhoanOpt.get().getId());
            if (khachHang == null || !khachHang.getId().equals(hoaDon.getKhachHangId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "KhÃ´ng cÃ³ quyá»n táº¡o yÃªu cáº§u tráº£ hÃ ng cho Ä‘Æ¡n hÃ ng nÃ y"));
            }

            // Kiá»ƒm tra tráº¡ng thÃ¡i hÃ³a Ä‘Æ¡n
            if (!"COMPLETED".equals(hoaDon.getTrangThaiHoaDon())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Chá»‰ cÃ³ thá»ƒ tráº£ hÃ ng khi Ä‘Æ¡n hÃ ng Ä‘Ã£ hoÃ n thÃ nh"));
            }

            // Validate dá»¯ liá»‡u Ä‘áº§u vÃ o
            if (chiTietSanPhamId == null || soLuong == null || soLuong <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "ThÃ´ng tin sáº£n pháº©m vÃ  sá»‘ lÆ°á»£ng khÃ´ng há»£p lá»‡"));
            }

            if (lyDo == null || lyDo.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Vui lÃ²ng nháº­p lÃ½ do tráº£ hÃ ng"));
            }

            // Xá»­ lÃ½ upload áº£nh náº¿u cÃ³
            String duongDanAnh = null;
            if (anhMinhChung != null && !anhMinhChung.isEmpty()) {
                // Validate file áº£nh
                String contentType = anhMinhChung.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "File upload pháº£i lÃ  áº£nh"));
                }

                // Kiá»ƒm tra kÃ­ch thÆ°á»›c file (max 5MB)
                if (anhMinhChung.getSize() > 5 * 1024 * 1024) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "KÃ­ch thÆ°á»›c áº£nh khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 5MB"));
                }

                // Upload áº£nh (cáº§n implement uploadImageService)
                duongDanAnh = uploadImageService.saveImage(anhMinhChung, "return-images");
            }

            // Táº¡o DTO cho chi tiáº¿t tráº£ hÃ ng
            ChiTietTraHangDTO chiTietTraHangDTO = new ChiTietTraHangDTO();
            chiTietTraHangDTO.setChiTietSanPhamId(chiTietSanPhamId);
            chiTietTraHangDTO.setSoLuong(soLuong);
            chiTietTraHangDTO.setLyDo(lyDo);
            chiTietTraHangDTO.setDuongDanAnh(duongDanAnh);
            chiTietTraHangDTO.setHoaDonId(id);
            chiTietTraHangDTO.setTrangThaiHoaDon("PENDING");

            // Táº¡o yÃªu cáº§u tráº£ hÃ ng
            ChiTietTraHangDTO created = chiTietTraHangService.createChiTietTraHang(chiTietTraHangDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Táº¡o yÃªu cáº§u tráº£ hÃ ng thÃ nh cÃ´ng");
            response.put("data", created);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lá»—i server: " + e.getMessage()));
        }
    }
    @GetMapping("/{id}/voucher-details")
    public ResponseEntity<?> getVoucherDetailsByHoaDon(@PathVariable Integer id, HttpServletRequest request) {
        try {
            // Kiá»ƒm tra quyá»n truy cáº­p (tÆ°Æ¡ng tá»± nhÆ° endpoint chi-tiet)
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token khÃ´ng tá»“n táº¡i"));
            }

            String email = jwtUtil.extractEmail(token);
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token khÃ´ng há»£p lá»‡"));
            }

            // Láº¥y thÃ´ng tin hÃ³a Ä‘Æ¡n
            HoaDonDTO hoaDon = hoaDonService.getHoaDonById(id);
            if (hoaDon == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));
            }

            // Kiá»ƒm tra quyá»n sá»Ÿ há»¯u
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findByEmail(email);
            if (taiKhoanOpt.isPresent()) {
                TaiKhoan taiKhoan = taiKhoanOpt.get();

                // Náº¿u khÃ´ng pháº£i admin, kiá»ƒm tra quyá»n sá»Ÿ há»¯u
                if (!"ADMIN".equals(taiKhoan.getVaiTro())) {
                    KhachHang khachHang = khachHangService.findByTaiKhoanId(taiKhoan.getId());
                    if (khachHang == null || !khachHang.getId().equals(hoaDon.getKhachHangId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("error", "KhÃ´ng cÃ³ quyá»n truy cáº­p Ä‘Æ¡n hÃ ng nÃ y"));
                    }
                }
            }

            // Láº¥y chi tiáº¿t voucher
            List<ChiTietVoucherDTO> chiTietVoucherList = chiTietVoucherService.findByHoaDonId(id);

            // TÃ­nh tá»•ng tiáº¿t kiá»‡m
            BigDecimal tongTietKiem = chiTietVoucherList.stream()
                    .map(ChiTietVoucherDTO::getSoTienGiam)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Táº¡o response vá»›i thÃ´ng tin voucher chi tiáº¿t
            Map<String, Object> voucherInfo = new HashMap<>();
            voucherInfo.put("chiTietVoucherList", chiTietVoucherList);
            voucherInfo.put("soLuongVoucher", chiTietVoucherList.size());
            voucherInfo.put("tongTietKiem", tongTietKiem);

            // Thá»‘ng kÃª voucher theo loáº¡i
            Map<String, Long> thongKeTheoLoai = chiTietVoucherList.stream()
                    .collect(Collectors.groupingBy(
                            ChiTietVoucherDTO::getLoaiGiamGia,
                            Collectors.counting()
                    ));
            voucherInfo.put("thongKeTheoLoai", thongKeTheoLoai);

            // TÃ­nh % tiáº¿t kiá»‡m náº¿u cÃ³ voucher
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
                    .body(Map.of("error", "Lá»—i server: " + e.getMessage()));
        }
    }

    /**
     * âœ… Cáº¬P NHáº¬T: Endpoint chi-tiet Ä‘á»ƒ bao gá»“m thÃ´ng tin voucher
     */
    @GetMapping("/{id}/chi-tiet")
    public ResponseEntity<?> getHoaDonChiTiet(@PathVariable Integer id, HttpServletRequest request) {
        try {
            System.out.println("ðŸ” DEBUG chi-tiet - HÃ³a Ä‘Æ¡n ID: " + id);

            // Kiá»ƒm tra quyá»n truy cáº­p
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token khÃ´ng tá»“n táº¡i"));
            }

            String email = jwtUtil.extractEmail(token);
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token khÃ´ng há»£p lá»‡"));
            }

            System.out.println("ðŸ”‘ DEBUG chi-tiet - Email: " + email);

            // Láº¥y thÃ´ng tin hÃ³a Ä‘Æ¡n
            HoaDonDTO hoaDon = hoaDonService.getHoaDonById(id);
            if (hoaDon == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));
            }

            // DEBUG: In ra thÃ´ng tin hÃ³a Ä‘Æ¡n
            System.out.println("ðŸ“‹ DEBUG chi-tiet - HÃ³a Ä‘Æ¡n: " + hoaDon.getMaHoaDon());
            System.out.println("ðŸ“‹ DEBUG chi-tiet - KhachHangId tá»« DTO: " + hoaDon.getKhachHangId());
            System.out.println("ðŸ“‹ DEBUG chi-tiet - Email tá»« DTO: " + hoaDon.getEmail());

            // âœ… Sá»¬A: Kiá»ƒm tra quyá»n sá»Ÿ há»¯u linh hoáº¡t hÆ¡n
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findByEmail(email);
            if (taiKhoanOpt.isPresent()) {
                TaiKhoan taiKhoan = taiKhoanOpt.get();
                System.out.println("ðŸ‘¤ DEBUG chi-tiet - TaiKhoan ID: " + taiKhoan.getId() + ", Role: " + taiKhoan.getVaiTro());

                // Náº¿u khÃ´ng pháº£i admin, kiá»ƒm tra quyá»n sá»Ÿ há»¯u
                if (!"ADMIN".equals(taiKhoan.getVaiTro())) {
                    boolean hasAccess = false;

                    KhachHang khachHang = khachHangService.findByTaiKhoanId(taiKhoan.getId());
                    if (khachHang != null) {
                        System.out.println("ðŸ‘¤ DEBUG chi-tiet - KhachHang ID: " + khachHang.getId());

                        // Kiá»ƒm tra nhiá»u Ä‘iá»u kiá»‡n
                        if (hoaDon.getKhachHangId() != null && khachHang.getId().equals(hoaDon.getKhachHangId())) {
                            hasAccess = true;
                            System.out.println("âœ… DEBUG chi-tiet - Khá»›p qua KhachHang ID");
                        } else if (hoaDon.getEmail() != null && hoaDon.getEmail().equals(email)) {
                            hasAccess = true;
                            System.out.println("âœ… DEBUG chi-tiet - Khá»›p qua email");
                        } else {
                            System.out.println("âŒ DEBUG chi-tiet - KhÃ´ng khá»›p Ä‘iá»u kiá»‡n nÃ o");
                            System.out.println("   - KhachHang.ID: " + khachHang.getId());
                            System.out.println("   - HoaDon.KhachHangId: " + hoaDon.getKhachHangId());
                            System.out.println("   - HoaDon.Email: " + hoaDon.getEmail());
                        }
                    } else {
                        System.out.println("âŒ DEBUG chi-tiet - KhÃ´ng tÃ¬m tháº¥y KhachHang");
                    }

                    if (!hasAccess) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("error", "KhÃ´ng cÃ³ quyá»n truy cáº­p Ä‘Æ¡n hÃ ng nÃ y"));
                    }
                } else {
                    System.out.println("âœ… DEBUG chi-tiet - Admin cÃ³ quyá»n truy cáº­p táº¥t cáº£");
                }
            }

            // âœ… GIá»® NGUYÃŠN: Láº¥y chi tiáº¿t sáº£n pháº©m cá»§a hÃ³a Ä‘Æ¡n (giá»‘ng code cÅ©)
            List<HoaDonChiTietDTO> chiTietList = hoaDonChiTietServiceImpl.getChiTietByHoaDonId(id);

            // âœ… THÃŠM Láº I: Láº¥y chi tiáº¿t voucher vÃ  cÃ¡c thÃ´ng tin khÃ¡c nhÆ° code gá»‘c
            List<ChiTietVoucherDTO> chiTietVoucherList = null;
            BigDecimal tongTietKiemVoucher = BigDecimal.ZERO;

            try {
                // Thá»­ láº¥y voucher info náº¿u service cÃ³ sáºµn
                if (chiTietVoucherService != null) {
                    chiTietVoucherList = chiTietVoucherService.findByHoaDonId(id);
                    tongTietKiemVoucher = chiTietVoucherList.stream()
                            .map(ChiTietVoucherDTO::getSoTienGiam)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    System.out.println("ðŸŽ« DEBUG chi-tiet - Sá»‘ voucher: " + chiTietVoucherList.size());
                }
            } catch (Exception e) {
                System.out.println("âš ï¸ DEBUG chi-tiet - KhÃ´ng thá»ƒ láº¥y voucher info: " + e.getMessage());
                chiTietVoucherList = new ArrayList<>();
            }

            // âœ… GIá»® NGUYÃŠN: Táº¡o response vá»›i Ä‘áº§y Ä‘á»§ thÃ´ng tin nhÆ° code gá»‘c
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("hoaDon", hoaDon);
            dataMap.put("chiTietSanPham", chiTietList);

            // ThÃªm voucher info náº¿u cÃ³
            if (chiTietVoucherList != null) {
                dataMap.put("chiTietVoucher", chiTietVoucherList);
                dataMap.put("tongTietKiemVoucher", tongTietKiemVoucher);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", dataMap);

            System.out.println("âœ… DEBUG chi-tiet - Tráº£ vá» thÃ nh cÃ´ng vá»›i " + chiTietList.size() + " sáº£n pháº©m");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("ðŸ’¥ DEBUG chi-tiet - Lá»—i: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lá»—i server: " + e.getMessage()));
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
                        .body(Map.of("error", "Token khÃ´ng tá»“n táº¡i"));
            }
            String email = jwtUtil.extractEmail(token);

            HoaDonDTO updated = hoaDonService.cancelInvoiceByCustomer(id, "KhÃ¡ch hÃ ng yÃªu cáº§u há»§y", email);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Há»§y Ä‘Æ¡n hÃ ng thÃ nh cÃ´ng",
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
                    .body(Map.of("error", "Lá»—i server: " + e.getMessage()));
        }
    }
}
