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

    // ThÆ° má»¥c lÆ°u file upload voucher (Ä‘á»•i tÃªn cho rÃµ rÃ ng)
    @Value("${app.upload.voucher.dir:voucher-images}")
    private String uploadDir;

    // ===== VOUCHER CRUD ENDPOINTS =====

    @GetMapping
    public List<Voucher> getAllVouchers() {
        return voucherService.getVouchers();
    }

    @GetMapping("/{id}")
    public Voucher getVoucherById(@PathVariable int id) {
        return voucherService.getVoucherById(id).orElse(null);
    }

    // ===== UPLOAD FILE VOUCHER (THEO PATTERN HINHANH) =====

    /**
     * API upload file hÃ¬nh áº£nh voucher
     * POST /voucher/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Validation file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Vui lÃ²ng chá»n file Ä‘á»ƒ upload"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Chá»‰ Ä‘Æ°á»£c upload file hÃ¬nh áº£nh (JPG, PNG, GIF, WEBP)"));
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "KÃ­ch thÆ°á»›c file khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 5MB"));
            }

            // Táº¡o thÆ° má»¥c náº¿u chÆ°a cÃ³
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("ðŸ“ Created voucher directory: " + uploadPath.toAbsolutePath());
            }

            // Táº¡o tÃªn file unique (giá»‘ng pattern hÃ¬nh áº£nh)
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = "voucher_" + System.currentTimeMillis() + fileExtension;

            // LÆ°u file
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("âœ… Voucher file saved: " + filePath.toAbsolutePath());

            // Tráº£ vá» Ä‘Æ°á»ng dáº«n Ä‘Ãºng vá»›i endpoint serve (THEO PATTERN HINHANH)
            String relativePath = "/voucher/images/" + newFilename;

            return ResponseEntity.ok(Map.of(
                    "success", "Upload thÃ nh cÃ´ng",
                    "path", relativePath,
                    "filename", newFilename
            ));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lá»—i khi lÆ°u file: " + e.getMessage()));
        }
    }

    /**
     * API serve hÃ¬nh áº£nh voucher static (THEO PATTERN HINHANH)
     * Truy cáº­p: GET /voucher/images/{filename}
     */
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            System.out.println("========== VOUCHER IMAGE REQUEST ==========");
            System.out.println("ðŸ” Requested filename: " + filename);
            System.out.println("ðŸ” Upload directory config: " + uploadDir);

            // ÄÆ°á»ng dáº«n Ä‘áº¿n file
            Path imagePath = Paths.get(uploadDir).resolve(filename);
            System.out.println("ðŸ” Full file path: " + imagePath.toAbsolutePath());
            System.out.println("ðŸ” File exists: " + Files.exists(imagePath));

            if (Files.exists(imagePath)) {
                System.out.println("ðŸ” File size: " + Files.size(imagePath) + " bytes");
                System.out.println("ðŸ” File readable: " + Files.isReadable(imagePath));
            } else {
                System.out.println("âŒ FILE NOT FOUND!");
                // List directory contents
                Path uploadPath = Paths.get(uploadDir);
                if (Files.exists(uploadPath)) {
                    System.out.println("ðŸ“ Directory contents:");
                    Files.list(uploadPath).forEach(path ->
                            System.out.println("  - " + path.getFileName())
                    );
                } else {
                    System.out.println("âŒ UPLOAD DIRECTORY DOES NOT EXIST: " + uploadPath.toAbsolutePath());
                }
            }

            Resource resource = new UrlResource(imagePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // XÃ¡c Ä‘á»‹nh content type
                String contentType;
                try {
                    contentType = Files.probeContentType(imagePath);
                } catch (IOException ex) {
                    contentType = "application/octet-stream";
                }

                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                System.out.println("âœ… Serving voucher image: " + filename + " with type: " + contentType);
                System.out.println("==========================================");

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                        .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                        .body(resource);
            } else {
                System.out.println("âŒ Voucher image not found: " + filename);
                System.out.println("==========================================");
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ðŸ’¥ Error serving voucher image: " + e.getMessage());
            System.out.println("==========================================");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // THÃŠM METHOD DEBUG Má»šI (khÃ´ng trÃ¹ng tÃªn)
    @GetMapping("/debug/check-file/{filename}")
    public ResponseEntity<Map<String, Object>> checkFile(@PathVariable String filename) {
        Map<String, Object> result = new HashMap<>();

        try {
            // ThÃ´ng tin cáº¥u hÃ¬nh
            result.put("uploadDir", uploadDir);
            result.put("requestedFile", filename);

            // Kiá»ƒm tra thÆ° má»¥c
            Path uploadPath = Paths.get(uploadDir);
            result.put("uploadDirAbsolute", uploadPath.toAbsolutePath().toString());
            result.put("uploadDirExists", Files.exists(uploadPath));

            // Kiá»ƒm tra file cá»¥ thá»ƒ
            Path filePath = uploadPath.resolve(filename);
            result.put("fileAbsolutePath", filePath.toAbsolutePath().toString());
            result.put("fileExists", Files.exists(filePath));

            if (Files.exists(filePath)) {
                result.put("fileSize", Files.size(filePath));
                result.put("fileReadable", Files.isReadable(filePath));
                result.put("lastModified", Files.getLastModifiedTime(filePath).toString());
            }

            // List táº¥t cáº£ files trong thÆ° má»¥c
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

            // ThÃ´ng tin system
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

        // Kiá»ƒm tra cÃ¡c thÆ° má»¥c cÃ³ thá»ƒ
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
            // Validation cÆ¡ báº£n
            if (voucher.getGiaTriGiamToiThieu() == null || voucher.getGiaTriGiamToiThieu() < 0) {
                return ResponseEntity.badRequest().body("GiÃ¡ trá»‹ Ä‘Æ¡n hÃ ng tá»‘i thiá»ƒu pháº£i >= 0");
            }

            if (voucher.getGiaTriGiamToiDa() == null || voucher.getGiaTriGiamToiDa() <= 0) {
                return ResponseEntity.badRequest().body("GiÃ¡ trá»‹ giáº£m tá»‘i Ä‘a pháº£i > 0");
            }

            // Validation theo loáº¡i giáº£m giÃ¡
            if ("PHAN_TRAM".equals(voucher.getLoaiGiamGia())) {
                if (voucher.getGiaTriGiam() == null || voucher.getGiaTriGiam() <= 0 || voucher.getGiaTriGiam() > 100) {
                    return ResponseEntity.badRequest().body("GiÃ¡ trá»‹ giáº£m theo pháº§n trÄƒm pháº£i tá»« 1% Ä‘áº¿n 100%");
                }
            } else if ("SO_TIEN_CO_DINH".equals(voucher.getLoaiGiamGia())) {
                if (voucher.getGiaTriGiamToiThieu() > 0 && voucher.getGiaTriGiamToiDa() > voucher.getGiaTriGiamToiThieu()) {
                    return ResponseEntity.badRequest().body("Sá»‘ tiá»n giáº£m cá»‘ Ä‘á»‹nh khÃ´ng Ä‘Æ°á»£c lá»›n hÆ¡n giÃ¡ trá»‹ Ä‘Æ¡n hÃ ng tá»‘i thiá»ƒu");
                }
                voucher.setGiaTriGiam(voucher.getGiaTriGiamToiDa());
            }

            voucher.setNgayTao(new Date());
            voucherService.addVoucher(voucher);
            System.out.println("âœ… Added voucher with image: " + voucher.getDuongDanHinhAnh());
            return ResponseEntity.ok("ThÃªm thÃ nh cÃ´ng voucher");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lá»—i khi thÃªm voucher: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateVoucher(@PathVariable int id, @Valid @RequestBody Voucher voucher) {
        try {
            Optional<Voucher> optional = voucherService.getVoucherById(id);
            if (optional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Validation tÆ°Æ¡ng tá»± nhÆ° thÃªm má»›i
            if (voucher.getGiaTriGiamToiThieu() == null || voucher.getGiaTriGiamToiThieu() < 0) {
                return ResponseEntity.badRequest().body("GiÃ¡ trá»‹ Ä‘Æ¡n hÃ ng tá»‘i thiá»ƒu pháº£i >= 0");
            }

            if (voucher.getGiaTriGiamToiDa() == null || voucher.getGiaTriGiamToiDa() <= 0) {
                return ResponseEntity.badRequest().body("GiÃ¡ trá»‹ giáº£m tá»‘i Ä‘a pháº£i > 0");
            }

            if ("PHAN_TRAM".equals(voucher.getLoaiGiamGia())) {
                if (voucher.getGiaTriGiam() == null || voucher.getGiaTriGiam() <= 0 || voucher.getGiaTriGiam() > 100) {
                    return ResponseEntity.badRequest().body("GiÃ¡ trá»‹ giáº£m theo pháº§n trÄƒm pháº£i tá»« 1% Ä‘áº¿n 100%");
                }
            } else if ("SO_TIEN_CO_DINH".equals(voucher.getLoaiGiamGia())) {
                if (voucher.getGiaTriGiamToiThieu() > 0 && voucher.getGiaTriGiamToiDa() > voucher.getGiaTriGiamToiThieu()) {
                    return ResponseEntity.badRequest().body("Sá»‘ tiá»n giáº£m cá»‘ Ä‘á»‹nh khÃ´ng Ä‘Æ°á»£c lá»›n hÆ¡n giÃ¡ trá»‹ Ä‘Æ¡n hÃ ng tá»‘i thiá»ƒu");
                }
                voucher.setGiaTriGiam(voucher.getGiaTriGiamToiDa());
            }

            voucher.setId(id);
            voucher.setNgayCapNhat(new Date());
            voucherService.updateVoucher(voucher);
            System.out.println("âœ… Updated voucher with image: " + voucher.getDuongDanHinhAnh());
            return ResponseEntity.ok("ÄÃ£ sá»­a thÃ nh cÃ´ng voucher vá»›i id: " + id);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lá»—i khi cáº­p nháº­t voucher: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteVoucher(@PathVariable int id) {
        try {
            Optional<Voucher> optional = voucherService.getVoucherById(id);
            if (optional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // XÃ³a file áº£nh náº¿u cÃ³ (theo pattern má»›i)
            Voucher existingVoucher = optional.get();
            if (existingVoucher.getDuongDanHinhAnh() != null &&
                    existingVoucher.getDuongDanHinhAnh().startsWith("/voucher/images/")) {
                try {
                    String fileName = existingVoucher.getDuongDanHinhAnh()
                            .replace("/voucher/images/", "");
                    Path filePath = Paths.get(uploadDir).resolve(fileName);
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                        System.out.println("ðŸ—‘ï¸ Deleted voucher image file: " + fileName);
                    }
                } catch (IOException e) {
                    System.out.println("âš ï¸ Could not delete voucher image file: " + e.getMessage());
                }
            }

            voucherService.deleteVoucher(id);
            return ResponseEntity.ok("ÄÃ£ xÃ³a thÃ nh cÃ´ng voucher vá»›i id: " + id);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lá»—i khi xÃ³a voucher: " + e.getMessage());
        }
    }

    @GetMapping("/public")
    public ResponseEntity<List<Voucher>> getPublicVouchers() {
        try {
            System.out.println("=== GET PUBLIC VOUCHERS FOR GUEST ===");

            // Láº¥y táº¥t cáº£ vouchers vÃ  filter á»Ÿ server-side
            List<Voucher> allVouchers = voucherService.getVouchers();
            Date now = new Date();

            // Filter vouchers kháº£ dá»¥ng cho guest
            List<Voucher> publicVouchers = allVouchers.stream()
                    .filter(voucher -> {
                        // Chá»‰ láº¥y voucher active
                        if (voucher.getTrangThai() != 1) return false;

                        // Kiá»ƒm tra thá»i gian hiá»‡u lá»±c
                        if (voucher.getNgayBatDau().after(now)) return false;
                        if (voucher.getNgayKetThuc().before(now)) return false;

                        // Kiá»ƒm tra cÃ²n sá»‘ lÆ°á»£ng
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
     * API validate voucher cho guest (khÃ´ng cáº§n authentication)
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
                        .body(Map.of("valid", false, "message", "MÃ£ voucher khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng"));
            }

            // TÃ¬m voucher theo mÃ£
            List<Voucher> allVouchers = voucherService.getVouchers();
            Optional<Voucher> voucherOpt = allVouchers.stream()
                    .filter(v -> v.getMaVoucher().equalsIgnoreCase(maVoucher))
                    .findFirst();

            if (voucherOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "MÃ£ voucher khÃ´ng tá»“n táº¡i"
                ));
            }

            Voucher voucher = voucherOpt.get();
            Date now = new Date();

            // Validate voucher
            if (voucher.getTrangThai() != 1) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Voucher khÃ´ng kháº£ dá»¥ng"
                ));
            }

            if (voucher.getNgayBatDau().after(now)) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Voucher chÆ°a cÃ³ hiá»‡u lá»±c"
                ));
            }

            if (voucher.getNgayKetThuc().before(now)) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Voucher Ä‘Ã£ háº¿t háº¡n"
                ));
            }

            if (voucher.getSoLuong() <= 0) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Voucher Ä‘Ã£ háº¿t lÆ°á»£t sá»­ dá»¥ng"
                ));
            }

            if (tongTien < voucher.getGiaTriGiamToiThieu()) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "ÄÆ¡n hÃ ng chÆ°a Ä‘áº¡t giÃ¡ trá»‹ tá»‘i thiá»ƒu: " + voucher.getGiaTriGiamToiThieu()
                ));
            }

            // TÃ­nh giÃ¡ trá»‹ giáº£m
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
                    "message", "Ãp dá»¥ng voucher thÃ nh cÃ´ng"
            ));

        } catch (Exception e) {
            System.err.println("Error validating voucher for guest: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("valid", false, "message", "Lá»—i há»‡ thá»‘ng"));
        }
    }
}
