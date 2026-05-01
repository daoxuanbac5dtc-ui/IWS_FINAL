package org.example.iws_websitesneaker.controller;

import org.example.iws_websitesneaker.Service.HinhAnhService;
import java.net.URI;
import org.example.iws_websitesneaker.entity.ChiTietSanPham;
import org.example.iws_websitesneaker.entity.HinhAnh;
import org.example.iws_websitesneaker.repository.RepoChiTietSanPham;
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

@RestController
@RequestMapping("/hinh-anh")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class HinhAnhController {

    @Autowired
    private HinhAnhService hinhAnhService;

    @Autowired
    private RepoChiTietSanPham repoChiTietSanPham;

    // Thư mục lưu file upload (có thể config trong application.properties)
    @Value("${app.upload.dir:images}")
    private String uploadDir;

    @GetMapping("/duong-dan/{tenHinhAnh}")
    public ResponseEntity<String> getDuongDanByTen(@PathVariable String tenHinhAnh) {
        try {
            Optional<HinhAnh> hinhAnh = hinhAnhService.findByTenHinhAnh(tenHinhAnh);
            if (hinhAnh.isPresent()) {
                String duongDan = hinhAnh.get().getDuongDan();
                // Trả về URL đầy đủ với domain
                System.out.println(duongDan);
                String fullUrl = "http://localhost:8080" + duongDan;
                return ResponseEntity.ok(fullUrl);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * API lấy đường dẫn ảnh theo ID - trả về URL đầy đủ
     * GET /hinh-anh/duong-dan/id/{idHinhAnh}
     */
    // ========== API CŨ GIỮ NGUYÊN ==========

    // Lấy tất cả dữ liệu bảng hình ảnh
    @GetMapping
    public List<HinhAnh> getHinhAnh() {
        return hinhAnhService.getAllHinhanh();
    }

    @GetMapping("/{id}")
    public HinhAnh getHinhAnhDetail(@PathVariable int id){
        return hinhAnhService.getHinhanhById(id).orElse(null);
    }

    @PostMapping
    public ResponseEntity<String> addHinhAnh(@RequestBody HinhAnh hinhAnh) {
        hinhAnh.setNgayTao(new Date());
        hinhAnhService.addHinhAnh(hinhAnh);
        return ResponseEntity.ok("Thêm thành công hình ảnh");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateHinhAnh(@PathVariable int id, @RequestBody HinhAnh hinhAnh) {
        Optional<HinhAnh> optional = hinhAnhService.getHinhanhById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        hinhAnh.setId(id);
        hinhAnh.setNgayCapNhat(new Date());
        hinhAnhService.updateHinhAnh(hinhAnh);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteHinhAnh(@PathVariable int id){
        Optional<HinhAnh> optional = hinhAnhService.getHinhanhById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        hinhAnhService.deleteHinhAnh(id);
        return ResponseEntity.ok("Đã xóa thành công hình ảnh với id: " + id);
    }

    /**
     * API upload file hình ảnh
     * POST /hinh-anh/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // ... existing validation code ...

            // Tạo thư mục nếu chưa có
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Tạo tên file unique
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = "img_" + System.currentTimeMillis() + fileExtension;

            // Lưu file
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Trả về đường dẫn đúng với endpoint serve
            String relativePath = "/hinh-anh/images/" + newFilename;  // ← Thay đổi ở đây

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
     * API serve hình ảnh static
     * Truy cập: GET /hinh-anh/images/{filename}
     */
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            // Đường dẫn đến file
            Path imagePath = Paths.get(uploadDir).resolve(filename);
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
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=3600") // Cache 1 giờ
                        .body(resource);
            } else {
                // Trả về 404 nếu không tìm thấy
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== API CHO CHI TIẾT SẢN PHẨM ==========

    /**
     * API mà frontend đang gọi - Lấy hình ảnh của chi tiết sản phẩm
     * Frontend gọi: GET /hinh-anh/chi-tiet-san-pham/{chiTietSanPhamId}
     */
    @GetMapping("/chi-tiet-san-pham/{chiTietSanPhamId}")
    public ResponseEntity<List<HinhAnh>> getHinhAnhByChiTietSanPham(@PathVariable Integer chiTietSanPhamId) {
        try {
            // Tìm chi tiết sản phẩm
            Optional<ChiTietSanPham> chiTietOpt = repoChiTietSanPham.findById(chiTietSanPhamId);

            List<HinhAnh> result = new ArrayList<>();
            if (chiTietOpt.isPresent() && chiTietOpt.get().getHinhAnh() != null) {
                // Nếu có hình ảnh, thêm vào list
                result.add(chiTietOpt.get().getHinhAnh());
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    /**
     * API mà frontend đang gọi - Xóa hình ảnh khỏi chi tiết sản phẩm
     * Frontend gọi: DELETE /hinh-anh/chi-tiet-san-pham/{chiTietSanPhamId}/clear
     */
    @DeleteMapping("/chi-tiet-san-pham/{chiTietSanPhamId}/clear")
    public ResponseEntity<String> clearHinhAnhFromChiTietSanPham(@PathVariable Integer chiTietSanPhamId) {
        try {
            // Tìm chi tiết sản phẩm và set hình ảnh = null
            Optional<ChiTietSanPham> chiTietOpt = repoChiTietSanPham.findById(chiTietSanPhamId);

            if (chiTietOpt.isPresent()) {
                ChiTietSanPham chiTiet = chiTietOpt.get();
                chiTiet.setHinhAnh(null);
                chiTiet.setNgayCapNhat(new Date());
                repoChiTietSanPham.save(chiTiet);
                return ResponseEntity.ok("Đã xóa hình ảnh khỏi chi tiết sản phẩm");
            }

            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("OK");
        }
    }

    /**
     * API mà frontend đang gọi - Thêm hình ảnh vào chi tiết sản phẩm
     * Frontend gọi: POST /hinh-anh/chi-tiet-san-pham
     * Body: { "chiTietSanPhamId": 1, "hinhAnhId": 5 }
     */
    @PostMapping("/chi-tiet-san-pham")
    public ResponseEntity<String> addHinhAnhToChiTietSanPham(@RequestBody Map<String, Object> request) {
        try {
            Integer chiTietSanPhamId = (Integer) request.get("chiTietSanPhamId");
            Integer hinhAnhId = (Integer) request.get("hinhAnhId");

            // Tìm chi tiết sản phẩm
            Optional<ChiTietSanPham> chiTietOpt = repoChiTietSanPham.findById(chiTietSanPhamId);
            if (chiTietOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Chi tiết sản phẩm không tồn tại");
            }

            // Tìm hình ảnh
            Optional<HinhAnh> hinhAnhOpt = hinhAnhService.getHinhanhById(hinhAnhId);
            if (hinhAnhOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Hình ảnh không tồn tại");
            }

            // Gán hình ảnh cho chi tiết sản phẩm
            ChiTietSanPham chiTiet = chiTietOpt.get();
            chiTiet.setHinhAnh(hinhAnhOpt.get());
            chiTiet.setNgayCapNhat(new Date());
            repoChiTietSanPham.save(chiTiet);

            return ResponseEntity.ok("Thêm hình ảnh vào chi tiết sản phẩm thành công");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("OK");
        }
    }
}
