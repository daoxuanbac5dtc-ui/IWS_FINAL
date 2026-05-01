package org.example.iws_websitesneaker.controller;

import jakarta.validation.Valid;
import org.example.iws_websitesneaker.Service.VoucherService;
import org.example.iws_websitesneaker.entity.Voucher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/voucher")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class VoucherRestController {

    @Autowired
    private VoucherService voucherService;

    private static final String DEFAULT_VOUCHER_IMAGE = "voucher-200000.jpg";

    // Thư mục lưu file upload voucher (đổi tên cho rõ ràng)
    @Value("${app.upload.voucher.dir:voucher-images}")
    private String uploadDir;

    // ===== VOUCHER CRUD ENDPOINTS =====

    @GetMapping
    public List<Voucher> getAllVouchers() {
        return voucherService.getVouchers();
    }

    @GetMapping("/{id:\\d+}")
    public Voucher getVoucherById(@PathVariable int id) {
        return voucherService.getVoucherById(id).orElse(null);
    }

    // ===== UPLOAD FILE VOUCHER (THEO PATTERN HINHANH) =====

    /**
     * API upload file hình ảnh voucher
     * POST /voucher/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Validation file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Vui lòng chọn file để upload"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Chỉ được upload file hình ảnh (JPG, PNG, GIF, WEBP)"));
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Kích thước file không được vượt quá 5MB"));
            }

            // Tạo thư mục nếu chưa có
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("📁 Created voucher directory: " + uploadPath.toAbsolutePath());
            }

            // Tạo tên file unique (giống pattern hình ảnh)
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = "voucher_" + System.currentTimeMillis() + fileExtension;

            // Lưu file
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("✅ Voucher file saved: " + filePath.toAbsolutePath());

            // Trả về đường dẫn đúng với endpoint serve (THEO PATTERN HINHANH)
            String relativePath = "/voucher/images/" + newFilename;

            return ResponseEntity.ok(Map.of(
                    "success", "Upload thành công",
                    "path", relativePath,
                    "filename", newFilename
            ));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi lưu file: " + e.getMessage()));
        }
    }

    /**
     * API serve hình ảnh voucher static (THEO PATTERN HINHANH)
     * Truy cập: GET /voucher/images/{filename}
     */
    @GetMapping({
            "/images/{filename:.+}",
            "/{filename:.+\\.jpg}",
            "/{filename:.+\\.jpeg}",
            "/{filename:.+\\.png}",
            "/{filename:.+\\.gif}",
            "/{filename:.+\\.webp}"
    })
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            System.out.println("========== VOUCHER IMAGE REQUEST ==========");
            System.out.println("🔍 Requested filename: " + filename);
            System.out.println("🔍 Upload directory config: " + uploadDir);

            // Đường dẫn đến file
            Path imagePath = resolveVoucherImagePath(filename);
            System.out.println("🔍 Full file path: " + imagePath.toAbsolutePath());
            System.out.println("🔍 File exists: " + Files.exists(imagePath));

            if (Files.exists(imagePath)) {
                System.out.println("🔍 File size: " + Files.size(imagePath) + " bytes");
                System.out.println("🔍 File readable: " + Files.isReadable(imagePath));
            } else {
                System.out.println("❌ FILE NOT FOUND!");
                // List directory contents
                Path uploadPath = Paths.get(uploadDir);
                if (Files.exists(uploadPath)) {
                    System.out.println("📁 Directory contents:");
                    Files.list(uploadPath).forEach(path ->
                            System.out.println("  - " + path.getFileName())
                    );
                } else {
                    System.out.println("❌ UPLOAD DIRECTORY DOES NOT EXIST: " + uploadPath.toAbsolutePath());
                }
            }

            Resource resource = new UrlResource(imagePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Xác định content type
                String contentType;
                try {
                    contentType = Files.probeContentType(imagePath);
                } catch (IOException ex) {
                    contentType = "application/octet-stream";
                }

                if (contentType == null) {
                    contentType = filename.toLowerCase(Locale.ROOT).endsWith(".svg")
                            ? "image/svg+xml"
                            : "application/octet-stream";
                }

                System.out.println("✅ Serving voucher image: " + filename + " with type: " + contentType);
                System.out.println("==========================================");

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                        .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                        .body(resource);
            } else {
                System.out.println("❌ Voucher image not found: " + filename);
                System.out.println("==========================================");
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("💥 Error serving voucher image: " + e.getMessage());
            System.out.println("==========================================");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // THÊM METHOD DEBUG MỚI (không trùng tên)
    private Path resolveVoucherImagePath(String filename) {
        String safeFilename = Paths.get(filename).getFileName().toString();

        List<Path> candidates = Arrays.asList(
                Paths.get(uploadDir).resolve(safeFilename),
                Paths.get("images").resolve(safeFilename),
                Paths.get("uploads").resolve(safeFilename)
        );

        for (Path candidate : candidates) {
            if (Files.exists(candidate) && Files.isRegularFile(candidate)) {
                return candidate;
            }
        }

        Path defaultImage = Paths.get("images").resolve(DEFAULT_VOUCHER_IMAGE);
        if (isSeedVoucherImage(safeFilename) && Files.exists(defaultImage) && Files.isRegularFile(defaultImage)) {
            return defaultImage;
        }

        return candidates.get(0);
    }

    private boolean isSeedVoucherImage(String filename) {
        return filename != null && filename.toLowerCase(Locale.ROOT).matches("vc\\d+\\.(jpg|jpeg|png|gif|webp|svg)");
    }

    @GetMapping("/debug/check-file/{filename}")
    public ResponseEntity<Map<String, Object>> checkFile(@PathVariable String filename) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Thông tin cấu hình
            result.put("uploadDir", uploadDir);
            result.put("requestedFile", filename);

            // Kiểm tra thư mục
            Path uploadPath = Paths.get(uploadDir);
            result.put("uploadDirAbsolute", uploadPath.toAbsolutePath().toString());
            result.put("uploadDirExists", Files.exists(uploadPath));

            // Kiểm tra file cụ thể
            Path filePath = uploadPath.resolve(filename);
            result.put("fileAbsolutePath", filePath.toAbsolutePath().toString());
            result.put("fileExists", Files.exists(filePath));

            if (Files.exists(filePath)) {
                result.put("fileSize", Files.size(filePath));
                result.put("fileReadable", Files.isReadable(filePath));
                result.put("lastModified", Files.getLastModifiedTime(filePath).toString());
            }

            // List tất cả files trong thư mục
            if (Files.exists(uploadPath)) {
                List<String> files = Files.list(uploadPath)
                        .map(path -> path.getFileName().toString())
                        .collect(Collectors.toList());
                result.put("allFiles", files);
                result.put("totalFiles", files.size());
            } else {
                result.put("allFiles", Arrays.asList());
                result.put("totalFiles", 0);
            }

            // Thông tin system
            result.put("workingDirectory", System.getProperty("user.dir"));
            result.put("javaVersion", System.getProperty("java.version"));

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("stackTrace", Arrays.toString(e.getStackTrace()));
            return ResponseEntity.ok(result);
        }
    }
    @GetMapping("/debug/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();

        config.put("uploadDir", uploadDir);
        config.put("uploadDirAbsolute", Paths.get(uploadDir).toAbsolutePath().toString());
        config.put("workingDirectory", System.getProperty("user.dir"));

        // Kiểm tra các thư mục có thể
        String[] possibleDirs = {"voucher-images", "./voucher-images", "../voucher-images"};
        Map<String, Boolean> dirCheck = new HashMap<>();

        for (String dir : possibleDirs) {
            Path path = Paths.get(dir);
            dirCheck.put(dir, Files.exists(path));
        }

        config.put("directoryCheck", dirCheck);

        return ResponseEntity.ok(config);
    }

    // ===== VOUCHER CRUD WITH VALIDATION =====

    @PostMapping
    public ResponseEntity<String> addVoucher(@Valid @RequestBody Voucher voucher) {
        try {
            // Validation cơ bản
            if (voucher.getGiaTriGiamToiThieu() == null || voucher.getGiaTriGiamToiThieu() < 0) {
                return ResponseEntity.badRequest().body("Giá trị đơn hàng tối thiểu phải >= 0");
            }

            if (voucher.getGiaTriGiamToiDa() == null || voucher.getGiaTriGiamToiDa() <= 0) {
                return ResponseEntity.badRequest().body("Giá trị giảm tối đa phải > 0");
            }

            // Validation theo loại giảm giá
            if ("PHAN_TRAM".equals(voucher.getLoaiGiamGia())) {
                if (voucher.getGiaTriGiam() == null || voucher.getGiaTriGiam() <= 0 || voucher.getGiaTriGiam() > 100) {
                    return ResponseEntity.badRequest().body("Giá trị giảm theo phần trăm phải từ 1% đến 100%");
                }
            } else if ("SO_TIEN_CO_DINH".equals(voucher.getLoaiGiamGia())) {
                if (voucher.getGiaTriGiamToiThieu() > 0 && voucher.getGiaTriGiamToiDa() > voucher.getGiaTriGiamToiThieu()) {
                    return ResponseEntity.badRequest().body("Số tiền giảm cố định không được lớn hơn giá trị đơn hàng tối thiểu");
                }
                voucher.setGiaTriGiam(voucher.getGiaTriGiamToiDa());
            }

            voucher.setNgayTao(new Date());
            voucherService.addVoucher(voucher);
            System.out.println("✅ Added voucher with image: " + voucher.getDuongDanHinhAnh());
            return ResponseEntity.ok("Thêm thành công voucher");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi thêm voucher: " + e.getMessage());
        }
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<String> updateVoucher(@PathVariable int id, @Valid @RequestBody Voucher voucher) {
        try {
            Optional<Voucher> optional = voucherService.getVoucherById(id);
            if (optional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Validation tương tự như thêm mới
            if (voucher.getGiaTriGiamToiThieu() == null || voucher.getGiaTriGiamToiThieu() < 0) {
                return ResponseEntity.badRequest().body("Giá trị đơn hàng tối thiểu phải >= 0");
            }

            if (voucher.getGiaTriGiamToiDa() == null || voucher.getGiaTriGiamToiDa() <= 0) {
                return ResponseEntity.badRequest().body("Giá trị giảm tối đa phải > 0");
            }

            if ("PHAN_TRAM".equals(voucher.getLoaiGiamGia())) {
                if (voucher.getGiaTriGiam() == null || voucher.getGiaTriGiam() <= 0 || voucher.getGiaTriGiam() > 100) {
                    return ResponseEntity.badRequest().body("Giá trị giảm theo phần trăm phải từ 1% đến 100%");
                }
            } else if ("SO_TIEN_CO_DINH".equals(voucher.getLoaiGiamGia())) {
                if (voucher.getGiaTriGiamToiThieu() > 0 && voucher.getGiaTriGiamToiDa() > voucher.getGiaTriGiamToiThieu()) {
                    return ResponseEntity.badRequest().body("Số tiền giảm cố định không được lớn hơn giá trị đơn hàng tối thiểu");
                }
                voucher.setGiaTriGiam(voucher.getGiaTriGiamToiDa());
            }

            voucher.setId(id);
            voucher.setNgayCapNhat(new Date());
            voucherService.updateVoucher(voucher);
            System.out.println("✅ Updated voucher with image: " + voucher.getDuongDanHinhAnh());
            return ResponseEntity.ok("Đã sửa thành công voucher với id: " + id);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật voucher: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<String> deleteVoucher(@PathVariable int id) {
        try {
            Optional<Voucher> optional = voucherService.getVoucherById(id);
            if (optional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Xóa file ảnh nếu có (theo pattern mới)
            Voucher existingVoucher = optional.get();
            if (existingVoucher.getDuongDanHinhAnh() != null &&
                    existingVoucher.getDuongDanHinhAnh().startsWith("/voucher/images/")) {
                try {
                    String fileName = existingVoucher.getDuongDanHinhAnh()
                            .replace("/voucher/images/", "");
                    Path filePath = Paths.get(uploadDir).resolve(fileName);
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                        System.out.println("🗑️ Deleted voucher image file: " + fileName);
                    }
                } catch (IOException e) {
                    System.out.println("⚠️ Could not delete voucher image file: " + e.getMessage());
                }
            }

            voucherService.deleteVoucher(id);
            return ResponseEntity.ok("Đã xóa thành công voucher với id: " + id);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa voucher: " + e.getMessage());
        }
    }

    @GetMapping("/public")
    public ResponseEntity<List<Voucher>> getPublicVouchers() {
        try {
            System.out.println("=== GET PUBLIC VOUCHERS FOR GUEST ===");

            // Lấy tất cả vouchers và filter ở server-side
            List<Voucher> allVouchers = voucherService.getVouchers();
            Date now = new Date();

            // Filter vouchers khả dụng cho guest
            List<Voucher> publicVouchers = allVouchers.stream()
                    .filter(voucher -> {
                        // Chỉ lấy voucher active
                        if (voucher.getTrangThai() != 1) return false;

                        // Kiểm tra thời gian hiệu lực
                        if (voucher.getNgayBatDau().after(now)) return false;
                        if (voucher.getNgayKetThuc().before(now)) return false;

                        // Kiểm tra còn số lượng
                        if (voucher.getSoLuong() <= 0) return false;

                        return true;
                    })
                    .collect(Collectors.toList());

            System.out.println("Found " + publicVouchers.size() + " public vouchers");
            return ResponseEntity.ok(publicVouchers);

        } catch (Exception e) {
            System.err.println("Error getting public vouchers: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ArrayList<>());
        }
    }

    /**
     * API validate voucher cho guest (không cần authentication)
     * POST /voucher/validate-guest
     */
    @PostMapping("/validate-guest")
    public ResponseEntity<Map<String, Object>> validateVoucherForGuest(@RequestBody Map<String, Object> request) {
        try {
            String maVoucher = (String) request.get("maVoucher");
            Double tongTien = ((Number) request.get("tongTien")).doubleValue();

            System.out.println("=== VALIDATE VOUCHER FOR GUEST ===");
            System.out.println("Voucher code: " + maVoucher);
            System.out.println("Order total: " + tongTien);

            if (maVoucher == null || maVoucher.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("valid", false, "message", "Mã voucher không được để trống"));
            }

            // Tìm voucher theo mã
            List<Voucher> allVouchers = voucherService.getVouchers();
            Optional<Voucher> voucherOpt = allVouchers.stream()
                    .filter(v -> v.getMaVoucher().equalsIgnoreCase(maVoucher))
                    .findFirst();

            if (voucherOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Mã voucher không tồn tại"
                ));
            }

            Voucher voucher = voucherOpt.get();
            Date now = new Date();

            // Validate voucher
            if (voucher.getTrangThai() != 1) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Voucher không khả dụng"
                ));
            }

            if (voucher.getNgayBatDau().after(now)) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Voucher chưa có hiệu lực"
                ));
            }

            if (voucher.getNgayKetThuc().before(now)) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Voucher đã hết hạn"
                ));
            }

            if (voucher.getSoLuong() <= 0) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Voucher đã hết lượt sử dụng"
                ));
            }

            if (tongTien < voucher.getGiaTriGiamToiThieu()) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Đơn hàng chưa đạt giá trị tối thiểu: " + voucher.getGiaTriGiamToiThieu()
                ));
            }

            // Tính giá trị giảm
            double discountValue = 0;
            if ("PHAN_TRAM".equals(voucher.getLoaiGiamGia())) {
                discountValue = (tongTien * voucher.getGiaTriGiam()) / 100;
                if (voucher.getGiaTriGiamToiDa() != null && discountValue > voucher.getGiaTriGiamToiDa()) {
                    discountValue = voucher.getGiaTriGiamToiDa();
                }
            } else if ("SO_TIEN_CO_DINH".equals(voucher.getLoaiGiamGia())) {
                discountValue = voucher.getGiaTriGiam();
            }

            System.out.println("Voucher validation successful. Discount: " + discountValue);

            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "voucher", voucher,
                    "discountValue", Math.floor(discountValue),
                    "message", "Áp dụng voucher thành công"
            ));

        } catch (Exception e) {
            System.err.println("Error validating voucher for guest: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("valid", false, "message", "Lỗi hệ thống"));
        }
    }
}
