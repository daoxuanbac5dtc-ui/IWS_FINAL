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
            // 1. Lấy JWT token từ header
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token không tồn tại"));
            }

            // 2. Lấy email từ token
            String email = jwtUtil.extractEmail(token);
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token không hợp lệ"));
            }

            // 3. Tìm tài khoản theo email
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findByEmail(email);
            if (taiKhoanOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy tài khoản"));
            }

            // 4. Tìm địa chỉ mặc định của tài khoản
            Optional<DiaChi> defaultAddressOpt = diaChiService.findDefaultByTaiKhoanId(taiKhoanOpt.get().getId());

            if (defaultAddressOpt.isEmpty()) {
                // Không có địa chỉ mặc định - không phải lỗi, chỉ trả về null
                return ResponseEntity.ok().build(); // 200 OK với body rỗng
            }

            // 5. Convert sang DTO và trả về
            DiaChiThanhToanDTO addressDto = convertToDto(defaultAddressOpt.get());
            return ResponseEntity.ok(addressDto);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi server: " + e.getMessage()));
        }
    }

    // GET - Lấy tất cả địa chỉ
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
                    .body(Map.of("error", "Lỗi khi lấy danh sách địa chỉ: " + e.getMessage()));
        }
    }

    // GET - Lấy địa chỉ theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getDiaChiById(@PathVariable Integer id) {
        try {
            Optional<DiaChi> diaChiOpt = diaChiService.findById(id);

            if (diaChiOpt.isPresent()) {
                return ResponseEntity.ok(convertToResponse(diaChiOpt.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy địa chỉ với ID: " + id));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi lấy thông tin địa chỉ: " + e.getMessage()));
        }
    }

    // POST - Tạo địa chỉ mới (CẬP NHẬT - BỎ HUYỆN)
    @PostMapping
    public ResponseEntity<?> createDiaChi(@RequestBody DiaChiDto diaChiDto) {
        try {
            // Validate dữ liệu đầu vào
            Map<String, String> errors = validateDiaChiDto(diaChiDto);
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("errors", errors));
            }

            // Kiểm tra tài khoản có tồn tại
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findById(diaChiDto.getIdTaiKhoan());
            if (!taiKhoanOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Không tìm thấy tài khoản với ID: " + diaChiDto.getIdTaiKhoan()));
            }

            // Tạo entity DiaChi từ DTO
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

            // Kiểm tra xem đây có phải là địa chỉ đầu tiên không
            List<DiaChi> existingAddresses = diaChiService.findByTaiKhoanId(diaChiDto.getIdTaiKhoan());
            if (existingAddresses.isEmpty()) {
                diaChi.setIsDefault(true);
            } else {
                diaChi.setIsDefault(false);
            }

            // Lưu địa chỉ
            DiaChi savedDiaChi = diaChiService.save(diaChi);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "Tạo địa chỉ thành công",
                            "data", convertToResponse(savedDiaChi)
                    ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi tạo địa chỉ: " + e.getMessage()));
        }
    }

    // PUT - Cập nhật địa chỉ (CẬP NHẬT - BỎ HUYỆN)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDiaChi(@PathVariable Integer id, @RequestBody DiaChiDto diaChiDto) {
        try {
            Optional<DiaChi> existingDiaChiOpt = diaChiService.findById(id);

            if (!existingDiaChiOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy địa chỉ với ID: " + id));
            }

            // Validate dữ liệu đầu vào
            Map<String, String> errors = validateDiaChiDto(diaChiDto);
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("errors", errors));
            }

            DiaChi existingDiaChi = existingDiaChiOpt.get();

            // Cập nhật thông tin (BỎ các trường liên quan đến huyện)
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
                    "message", "Cập nhật địa chỉ thành công",
                    "data", convertToResponse(updatedDiaChi)
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi cập nhật địa chỉ: " + e.getMessage()));
        }
    }

    // DELETE - Xóa địa chỉ (GIỮ NGUYÊN)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDiaChi(@PathVariable Integer id) {
        try {
            Optional<DiaChi> diaChiOpt = diaChiService.findById(id);

            if (!diaChiOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy địa chỉ với ID: " + id));
            }

            DiaChi diaChi = diaChiOpt.get();

            // Không cho xóa địa chỉ mặc định nếu còn địa chỉ khác
            if (diaChi.getIsDefault() != null && diaChi.getIsDefault()) {
                List<DiaChi> otherAddresses = diaChiService.findByTaiKhoanId(diaChi.getTaiKhoan().getId())
                        .stream()
                        .filter(dc -> !dc.getId().equals(id))
                        .collect(Collectors.toList());

                if (!otherAddresses.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Không thể xóa địa chỉ mặc định. Vui lòng đặt địa chỉ khác làm mặc định trước"));
                }
            }

            diaChiService.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Xóa địa chỉ thành công"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi xóa địa chỉ: " + e.getMessage()));
        }
    }

    // GET - Lấy địa chỉ theo ID tài khoản (GIỮ NGUYÊN)
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
                    .body(Map.of("error", "Lỗi khi lấy địa chỉ theo tài khoản: " + e.getMessage()));
        }
    }

    // PATCH - Đặt địa chỉ mặc định (GIỮ NGUYÊN)
    @PatchMapping("/{id}/set-default")
    public ResponseEntity<?> setDefaultAddress(@PathVariable Integer id) {
        try {
            Optional<DiaChi> diaChiOpt = diaChiService.findById(id);

            if (!diaChiOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy địa chỉ với ID: " + id));
            }

            DiaChi diaChi = diaChiOpt.get();
            Integer taiKhoanId = diaChi.getTaiKhoan().getId();

            // Bỏ mặc định cho tất cả địa chỉ của tài khoản này
            List<DiaChi> allAddresses = diaChiService.findByTaiKhoanId(taiKhoanId);
            for (DiaChi addr : allAddresses) {
                if (addr.getIsDefault() != null && addr.getIsDefault()) {
                    addr.setIsDefault(false);
                    addr.setNgayCapNhat(new Date());
                    diaChiService.save(addr);
                }
            }

            // Đặt địa chỉ này làm mặc định
            diaChi.setIsDefault(true);
            diaChi.setNgayCapNhat(new Date());
            DiaChi updatedDiaChi = diaChiService.save(diaChi);

            return ResponseEntity.ok(Map.of(
                    "message", "Đã đặt địa chỉ làm mặc định",
                    "data", convertToResponse(updatedDiaChi)
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi đặt địa chỉ mặc định: " + e.getMessage()));
        }
    }

    // API kiểm tra trạng thái service
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
    // === Helper Methods (CẬP NHẬT - BỎ HUYỆN) ===


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
            errors.put("idTaiKhoan", "ID tài khoản không được để trống");
        }

        if (dto.getTenTinh() == null || dto.getTenTinh().trim().isEmpty()) {
            errors.put("tenTinh", "Tên tỉnh/thành phố không được để trống");
        }

        if (dto.getTenPhuong() == null || dto.getTenPhuong().trim().isEmpty()) {
            errors.put("tenPhuong", "Tên xã/phường không được để trống");
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

        // Thêm thông tin người dùng
        String hoTen = "";
        if (diaChi.getTaiKhoan() != null) {
            // TODO: Implement logic để lấy họ tên từ KhachHang/NhanVien dựa vào TaiKhoan
        }
        response.put("hoTen", hoTen);

        return response;
    }
}
