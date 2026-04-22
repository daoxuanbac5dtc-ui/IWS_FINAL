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

    // ThÆ° má»¥c lÆ°u file upload (cÃ³ thá»ƒ config trong application.properties)
    @Value("${app.upload.dir:images}")
    private String uploadDir;

    @GetMapping("/duong-dan/{tenHinhAnh}")
    public ResponseEntity<String> getDuongDanByTen(@PathVariable String tenHinhAnh) {
        try {
            Optional<HinhAnh> hinhAnh = hinhAnhService.findByTenHinhAnh(tenHinhAnh);
            if (hinhAnh.isPresent()) {
                String duongDan = hinhAnh.get().getDuongDan();
                // Tráº£ vá» URL Ä‘áº§y Ä‘á»§ vá»›i domain
                System.out.println(duongDan);
                String fullUrl = "http://localhost:8080" + duongDan;
                return ResponseEntity.ok(fullUrl);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lá»—i: " + e.getMessage());
        }
    }

    /**
     * API láº¥y Ä‘Æ°á»ng dáº«n áº£nh theo ID - tráº£ vá» URL Ä‘áº§y Ä‘á»§
     * GET /hinh-anh/duong-dan/id/{idHinhAnh}
     */
    // ========== API CÅ¨ GIá»® NGUYÃŠN ==========

    // Láº¥y táº¥t cáº£ dá»¯ liá»‡u báº£ng hÃ¬nh áº£nh
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
        return ResponseEntity.ok("ThÃªm thÃ nh cÃ´ng hÃ¬nh áº£nh");
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
        return ResponseEntity.ok("ÄÃ£ xÃ³a thÃ nh cÃ´ng hÃ¬nh áº£nh vá»›i id: " + id);
    }

    /**
     * API upload file hÃ¬nh áº£nh
     * POST /hinh-anh/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // ... existing validation code ...

            // Táº¡o thÆ° má»¥c náº¿u chÆ°a cÃ³
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Táº¡o tÃªn file unique
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = "img_" + System.currentTimeMillis() + fileExtension;

            // LÆ°u file
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Tráº£ vá» Ä‘Æ°á»ng dáº«n Ä‘Ãºng vá»›i endpoint serve
            String relativePath = "/hinh-anh/images/" + newFilename;  // â† Thay Ä‘á»•i á»Ÿ Ä‘Ã¢y

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
     * API serve hÃ¬nh áº£nh static
     * Truy cáº­p: GET /hinh-anh/images/{filename}
     */
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            // ÄÆ°á»ng dáº«n Ä‘áº¿n file
            Path imagePath = Paths.get(uploadDir).resolve(filename);
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

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=3600") // Cache 1 giá»
                        .body(resource);
            } else {
                // Tráº£ vá» 404 náº¿u khÃ´ng tÃ¬m tháº¥y
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== API CHO CHI TIáº¾T Sáº¢N PHáº¨M ==========

    /**
     * API mÃ  frontend Ä‘ang gá»i - Láº¥y hÃ¬nh áº£nh cá»§a chi tiáº¿t sáº£n pháº©m
     * Frontend gá»i: GET /hinh-anh/chi-tiet-san-pham/{chiTietSanPhamId}
     */
    @GetMapping("/chi-tiet-san-pham/{chiTietSanPhamId}")
    public ResponseEntity<List<HinhAnh>> getHinhAnhByChiTietSanPham(@PathVariable Integer chiTietSanPhamId) {
        try {
            // TÃ¬m chi tiáº¿t sáº£n pháº©m
            Optional<ChiTietSanPham> chiTietOpt = repoChiTietSanPham.findById(chiTietSanPhamId);

            List<HinhAnh> result = new ArrayList<>();
            if (chiTietOpt.isPresent() && chiTietOpt.get().getHinhAnh() != null) {
                // Náº¿u cÃ³ hÃ¬nh áº£nh, thÃªm vÃ o list
                result.add(chiTietOpt.get().getHinhAnh());
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    /**
     * API mÃ  frontend Ä‘ang gá»i - XÃ³a hÃ¬nh áº£nh khá»i chi tiáº¿t sáº£n pháº©m
     * Frontend gá»i: DELETE /hinh-anh/chi-tiet-san-pham/{chiTietSanPhamId}/clear
     */
    @DeleteMapping("/chi-tiet-san-pham/{chiTietSanPhamId}/clear")
    public ResponseEntity<String> clearHinhAnhFromChiTietSanPham(@PathVariable Integer chiTietSanPhamId) {
        try {
            // TÃ¬m chi tiáº¿t sáº£n pháº©m vÃ  set hÃ¬nh áº£nh = null
            Optional<ChiTietSanPham> chiTietOpt = repoChiTietSanPham.findById(chiTietSanPhamId);

            if (chiTietOpt.isPresent()) {
                ChiTietSanPham chiTiet = chiTietOpt.get();
                chiTiet.setHinhAnh(null);
                chiTiet.setNgayCapNhat(new Date());
                repoChiTietSanPham.save(chiTiet);
                return ResponseEntity.ok("ÄÃ£ xÃ³a hÃ¬nh áº£nh khá»i chi tiáº¿t sáº£n pháº©m");
            }

            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("OK");
        }
    }

    /**
     * API mÃ  frontend Ä‘ang gá»i - ThÃªm hÃ¬nh áº£nh vÃ o chi tiáº¿t sáº£n pháº©m
     * Frontend gá»i: POST /hinh-anh/chi-tiet-san-pham
     * Body: { "chiTietSanPhamId": 1, "hinhAnhId": 5 }
     */
    @PostMapping("/chi-tiet-san-pham")
    public ResponseEntity<String> addHinhAnhToChiTietSanPham(@RequestBody Map<String, Object> request) {
        try {
            Integer chiTietSanPhamId = (Integer) request.get("chiTietSanPhamId");
            Integer hinhAnhId = (Integer) request.get("hinhAnhId");

            // TÃ¬m chi tiáº¿t sáº£n pháº©m
            Optional<ChiTietSanPham> chiTietOpt = repoChiTietSanPham.findById(chiTietSanPhamId);
            if (chiTietOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Chi tiáº¿t sáº£n pháº©m khÃ´ng tá»“n táº¡i");
            }

            // TÃ¬m hÃ¬nh áº£nh
            Optional<HinhAnh> hinhAnhOpt = hinhAnhService.getHinhanhById(hinhAnhId);
            if (hinhAnhOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("HÃ¬nh áº£nh khÃ´ng tá»“n táº¡i");
            }

            // GÃ¡n hÃ¬nh áº£nh cho chi tiáº¿t sáº£n pháº©m
            ChiTietSanPham chiTiet = chiTietOpt.get();
            chiTiet.setHinhAnh(hinhAnhOpt.get());
            chiTiet.setNgayCapNhat(new Date());
            repoChiTietSanPham.save(chiTiet);

            return ResponseEntity.ok("ThÃªm hÃ¬nh áº£nh vÃ o chi tiáº¿t sáº£n pháº©m thÃ nh cÃ´ng");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("OK");
        }
    }
}
