package org.example.iws_websitesneaker.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.iws_websitesneaker.Dto.DiaChiDto;
import org.example.iws_websitesneaker.Dto.DiaChiThanhToanDTO;
import org.example.iws_websitesneaker.Service.DiaChiService;
import org.example.iws_websitesneaker.Service.TaiKhoanService;
import org.example.iws_websitesneaker.entity.DiaChi;
import org.example.iws_websitesneaker.entity.TaiKhoan;
import org.example.iws_websitesneaker.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dia-chi")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class DiaChiRestController {

    @Autowired
    private DiaChiService diaChiService;

    @Autowired
    private TaiKhoanService taiKhoanService;

    @Autowired
    private JwtUtil  jwtUtil;

    @GetMapping("/default")
    public ResponseEntity<?> getDefaultAddress(HttpServletRequest request) {
        try {
            // 1. Láº¥y JWT token tá»« header
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token khÃ´ng tá»“n táº¡i"));
            }

            // 2. Láº¥y email tá»« token
            String email = jwtUtil.extractEmail(token);
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token khÃ´ng há»£p lá»‡"));
            }

            // 3. TÃ¬m tÃ i khoáº£n theo email
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findByEmail(email);
            if (taiKhoanOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "KhÃ´ng tÃ¬m tháº¥y tÃ i khoáº£n"));
            }

            // 4. TÃ¬m Ä‘á»‹a chá»‰ máº·c Ä‘á»‹nh cá»§a tÃ i khoáº£n
            Optional<DiaChi> defaultAddressOpt = diaChiService.findDefaultByTaiKhoanId(taiKhoanOpt.get().getId());

            if (defaultAddressOpt.isEmpty()) {
                // KhÃ´ng cÃ³ Ä‘á»‹a chá»‰ máº·c Ä‘á»‹nh - khÃ´ng pháº£i lá»—i, chá»‰ tráº£ vá» null
                return ResponseEntity.ok().build(); // 200 OK vá»›i body rá»—ng
            }

            // 5. Convert sang DTO vÃ  tráº£ vá»
            DiaChiThanhToanDTO addressDto = convertToDto(defaultAddressOpt.get());
            return ResponseEntity.ok(addressDto);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lá»—i server: " + e.getMessage()));
        }
    }

    // GET - Láº¥y táº¥t cáº£ Ä‘á»‹a chá»‰
    @GetMapping
    public ResponseEntity<?> getAllDiaChi() {
        try {
            List<DiaChi> diaChiList = diaChiService.findAll();
            List<Map<String, Object>> response = diaChiList.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lá»—i khi láº¥y danh sÃ¡ch Ä‘á»‹a chá»‰: " + e.getMessage()));
        }
    }

    // GET - Láº¥y Ä‘á»‹a chá»‰ theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getDiaChiById(@PathVariable Integer id) {
        try {
            Optional<DiaChi> diaChiOpt = diaChiService.findById(id);

            if (diaChiOpt.isPresent()) {
                return ResponseEntity.ok(convertToResponse(diaChiOpt.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "KhÃ´ng tÃ¬m tháº¥y Ä‘á»‹a chá»‰ vá»›i ID: " + id));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lá»—i khi láº¥y thÃ´ng tin Ä‘á»‹a chá»‰: " + e.getMessage()));
        }
    }

    // POST - Táº¡o Ä‘á»‹a chá»‰ má»›i (Cáº¬P NHáº¬T - Bá»Ž HUYá»†N)
    @PostMapping
    public ResponseEntity<?> createDiaChi(@RequestBody DiaChiDto diaChiDto) {
        try {
            // Validate dá»¯ liá»‡u Ä‘áº§u vÃ o
            Map<String, String> errors = validateDiaChiDto(diaChiDto);
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("errors", errors));
            }

            // Kiá»ƒm tra tÃ i khoáº£n cÃ³ tá»“n táº¡i
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findById(diaChiDto.getIdTaiKhoan());
            if (!taiKhoanOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "KhÃ´ng tÃ¬m tháº¥y tÃ i khoáº£n vá»›i ID: " + diaChiDto.getIdTaiKhoan()));
            }

            // Táº¡o entity DiaChi tá»« DTO
            DiaChi diaChi = new DiaChi();
            diaChi.setTaiKhoan(taiKhoanOpt.get());
            diaChi.setMaTinh(diaChiDto.getMaTinh() != null ? diaChiDto.getMaTinh() : "01");
            diaChi.setMaPhuong(diaChiDto.getMaPhuong() != null ? diaChiDto.getMaPhuong() : "0001");
            diaChi.setTenTinh(diaChiDto.getTenTinh());
            diaChi.setTenPhuong(diaChiDto.getTenPhuong());
            diaChi.setDiaChiChiTiet(diaChiDto.getDiaChiChiTiet() != null ? diaChiDto.getDiaChiChiTiet() : "");
            diaChi.setTrangThai(diaChiDto.getTrangThai() != null ? diaChiDto.getTrangThai() : 1);
            diaChi.setNgayTao(new Date());
            diaChi.setNgayCapNhat(new Date());

            // Kiá»ƒm tra xem Ä‘Ã¢y cÃ³ pháº£i lÃ  Ä‘á»‹a chá»‰ Ä‘áº§u tiÃªn khÃ´ng
            List<DiaChi> existingAddresses = diaChiService.findByTaiKhoanId(diaChiDto.getIdTaiKhoan());
            if (existingAddresses.isEmpty()) {
                diaChi.setIsDefault(true);
            } else {
                diaChi.setIsDefault(false);
            }

            // LÆ°u Ä‘á»‹a chá»‰
            DiaChi savedDiaChi = diaChiService.save(diaChi);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "Táº¡o Ä‘á»‹a chá»‰ thÃ nh cÃ´ng",
                            "data", convertToResponse(savedDiaChi)
                    ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lá»—i khi táº¡o Ä‘á»‹a chá»‰: " + e.getMessage()));
        }
    }

    // PUT - Cáº­p nháº­t Ä‘á»‹a chá»‰ (Cáº¬P NHáº¬T - Bá»Ž HUYá»†N)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDiaChi(@PathVariable Integer id, @RequestBody DiaChiDto diaChiDto) {
        try {
            Optional<DiaChi> existingDiaChiOpt = diaChiService.findById(id);

            if (!existingDiaChiOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "KhÃ´ng tÃ¬m tháº¥y Ä‘á»‹a chá»‰ vá»›i ID: " + id));
            }

            // Validate dá»¯ liá»‡u Ä‘áº§u vÃ o
            Map<String, String> errors = validateDiaChiDto(diaChiDto);
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("errors", errors));
            }

            DiaChi existingDiaChi = existingDiaChiOpt.get();

            // Cáº­p nháº­t thÃ´ng tin (Bá»Ž cÃ¡c trÆ°á»ng liÃªn quan Ä‘áº¿n huyá»‡n)
            if (diaChiDto.getMaTinh() != null) {
                existingDiaChi.setMaTinh(diaChiDto.getMaTinh());
            }
            if (diaChiDto.getMaPhuong() != null) {
                existingDiaChi.setMaPhuong(diaChiDto.getMaPhuong());
            }
            if (diaChiDto.getTenTinh() != null) {
                existingDiaChi.setTenTinh(diaChiDto.getTenTinh());
            }
            if (diaChiDto.getTenPhuong() != null) {
                existingDiaChi.setTenPhuong(diaChiDto.getTenPhuong());
            }
            if (diaChiDto.getDiaChiChiTiet() != null) {
                existingDiaChi.setDiaChiChiTiet(diaChiDto.getDiaChiChiTiet());
            }
            if (diaChiDto.getTrangThai() != null) {
                existingDiaChi.setTrangThai(diaChiDto.getTrangThai());
            }

            existingDiaChi.setNgayCapNhat(new Date());

            DiaChi updatedDiaChi = diaChiService.save(existingDiaChi);

            return ResponseEntity.ok(Map.of(
                    "message", "Cáº­p nháº­t Ä‘á»‹a chá»‰ thÃ nh cÃ´ng",
                    "data", convertToResponse(updatedDiaChi)
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lá»—i khi cáº­p nháº­t Ä‘á»‹a chá»‰: " + e.getMessage()));
        }
    }

    // DELETE - XÃ³a Ä‘á»‹a chá»‰ (GIá»® NGUYÃŠN)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDiaChi(@PathVariable Integer id) {
        try {
            Optional<DiaChi> diaChiOpt = diaChiService.findById(id);

            if (!diaChiOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "KhÃ´ng tÃ¬m tháº¥y Ä‘á»‹a chá»‰ vá»›i ID: " + id));
            }

            DiaChi diaChi = diaChiOpt.get();

            // KhÃ´ng cho xÃ³a Ä‘á»‹a chá»‰ máº·c Ä‘á»‹nh náº¿u cÃ²n Ä‘á»‹a chá»‰ khÃ¡c
            if (diaChi.getIsDefault() != null && diaChi.getIsDefault()) {
                List<DiaChi> otherAddresses = diaChiService.findByTaiKhoanId(diaChi.getTaiKhoan().getId())
                        .stream()
                        .filter(dc -> !dc.getId().equals(id))
                        .collect(Collectors.toList());

                if (!otherAddresses.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "KhÃ´ng thá»ƒ xÃ³a Ä‘á»‹a chá»‰ máº·c Ä‘á»‹nh. Vui lÃ²ng Ä‘áº·t Ä‘á»‹a chá»‰ khÃ¡c lÃ m máº·c Ä‘á»‹nh trÆ°á»›c"));
                }
            }

            diaChiService.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "XÃ³a Ä‘á»‹a chá»‰ thÃ nh cÃ´ng"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lá»—i khi xÃ³a Ä‘á»‹a chá»‰: " + e.getMessage()));
        }
    }

    // GET - Láº¥y Ä‘á»‹a chá»‰ theo ID tÃ i khoáº£n (GIá»® NGUYÃŠN)
    @GetMapping("/tai-khoan/{idTaiKhoan}")
    public ResponseEntity<?> getDiaChiByTaiKhoan(@PathVariable Integer idTaiKhoan) {
        try {
            List<DiaChi> diaChiList = diaChiService.findByTaiKhoanId(idTaiKhoan);
            List<Map<String, Object>> response = diaChiList.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lá»—i khi láº¥y Ä‘á»‹a chá»‰ theo tÃ i khoáº£n: " + e.getMessage()));
        }
    }

    // PATCH - Äáº·t Ä‘á»‹a chá»‰ máº·c Ä‘á»‹nh (GIá»® NGUYÃŠN)
    @PatchMapping("/{id}/set-default")
    public ResponseEntity<?> setDefaultAddress(@PathVariable Integer id) {
        try {
            Optional<DiaChi> diaChiOpt = diaChiService.findById(id);

            if (!diaChiOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "KhÃ´ng tÃ¬m tháº¥y Ä‘á»‹a chá»‰ vá»›i ID: " + id));
            }

            DiaChi diaChi = diaChiOpt.get();
            Integer taiKhoanId = diaChi.getTaiKhoan().getId();

            // Bá» máº·c Ä‘á»‹nh cho táº¥t cáº£ Ä‘á»‹a chá»‰ cá»§a tÃ i khoáº£n nÃ y
            List<DiaChi> allAddresses = diaChiService.findByTaiKhoanId(taiKhoanId);
            for (DiaChi addr : allAddresses) {
                if (addr.getIsDefault() != null && addr.getIsDefault()) {
                    addr.setIsDefault(false);
                    addr.setNgayCapNhat(new Date());
                    diaChiService.save(addr);
                }
            }

            // Äáº·t Ä‘á»‹a chá»‰ nÃ y lÃ m máº·c Ä‘á»‹nh
            diaChi.setIsDefault(true);
            diaChi.setNgayCapNhat(new Date());
            DiaChi updatedDiaChi = diaChiService.save(diaChi);

            return ResponseEntity.ok(Map.of(
                    "message", "ÄÃ£ Ä‘áº·t Ä‘á»‹a chá»‰ lÃ m máº·c Ä‘á»‹nh",
                    "data", convertToResponse(updatedDiaChi)
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lá»—i khi Ä‘áº·t Ä‘á»‹a chá»‰ máº·c Ä‘á»‹nh: " + e.getMessage()));
        }
    }

    // API kiá»ƒm tra tráº¡ng thÃ¡i service
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "service", "DiaChiService",
                "timestamp", System.currentTimeMillis()
        ));
    }

    // === Helper Methods ===
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    // === Helper Methods (Cáº¬P NHáº¬T - Bá»Ž HUYá»†N) ===


    private DiaChiThanhToanDTO convertToDto(DiaChi entity) {
        DiaChiThanhToanDTO dto = new DiaChiThanhToanDTO();
        dto.setId(entity.getId());
        dto.setMaTinh(entity.getMaTinh());
        dto.setMaPhuong(entity.getMaPhuong());
        dto.setTenTinh(entity.getTenTinh());
        dto.setTenPhuong(entity.getTenPhuong());
        dto.setDiaChiChiTiet(entity.getDiaChiChiTiet());
        dto.setIsDefault(entity.getIsDefault());
        dto.setTrangThai(entity.getTrangThai());
        dto.setIdTaiKhoan(entity.getTaiKhoan() != null ? entity.getTaiKhoan().getId() : null);
        return dto;
    }
    private Map<String, String> validateDiaChiDto(DiaChiDto dto) {
        Map<String, String> errors = new HashMap<>();

        if (dto.getIdTaiKhoan() == null) {
            errors.put("idTaiKhoan", "ID tÃ i khoáº£n khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }

        if (dto.getTenTinh() == null || dto.getTenTinh().trim().isEmpty()) {
            errors.put("tenTinh", "TÃªn tá»‰nh/thÃ nh phá»‘ khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }

        if (dto.getTenPhuong() == null || dto.getTenPhuong().trim().isEmpty()) {
            errors.put("tenPhuong", "TÃªn xÃ£/phÆ°á»ng khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }

        return errors;
    }

    private Map<String, Object> convertToResponse(DiaChi diaChi) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", diaChi.getId());
        response.put("idTaiKhoan", diaChi.getTaiKhoan() != null ? diaChi.getTaiKhoan().getId() : null);
        response.put("emailTaiKhoan", diaChi.getTaiKhoan() != null ? diaChi.getTaiKhoan().getEmail() : null);
        response.put("vaiTro", diaChi.getTaiKhoan() != null ? diaChi.getTaiKhoan().getVaiTro() : null);
        response.put("diaChiChiTiet", diaChi.getDiaChiChiTiet());
        response.put("tenPhuong", diaChi.getTenPhuong());
        response.put("tenTinh", diaChi.getTenTinh());
        response.put("maPhuong", diaChi.getMaPhuong());
        response.put("maTinh", diaChi.getMaTinh());
        response.put("isDefault", diaChi.getIsDefault());
        response.put("trangThai", diaChi.getTrangThai());
        response.put("ngayTao", diaChi.getNgayTao());
        response.put("ngayCapNhat", diaChi.getNgayCapNhat());

        // ThÃªm thÃ´ng tin ngÆ°á»i dÃ¹ng
        String hoTen = "";
        if (diaChi.getTaiKhoan() != null) {
            // TODO: Implement logic Ä‘á»ƒ láº¥y há» tÃªn tá»« KhachHang/NhanVien dá»±a vÃ o TaiKhoan
        }
        response.put("hoTen", hoTen);

        return response;
    }
}
