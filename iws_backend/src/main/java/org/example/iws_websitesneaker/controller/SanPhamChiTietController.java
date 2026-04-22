package org.example.iws_websitesneaker.controller;

import org.example.iws_websitesneaker.Service.HinhAnhService;
import org.example.iws_websitesneaker.Service.SanPhamChiTietService;
import org.example.iws_websitesneaker.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/san-pham-chi-tiet")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SanPhamChiTietController {

    @Autowired
    private SanPhamChiTietService sanPhamChiTietService;

    @Autowired
    private HinhAnhService hinhAnhService;

    @GetMapping("")
    public ResponseEntity<List<ChiTietSanPham>> getAll() {
        try {
            List<ChiTietSanPham> chiTietSanPhams = sanPhamChiTietService.getAll();
            return ResponseEntity.ok(chiTietSanPhams);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChiTietSanPham> getById(@PathVariable Integer id) {
        try {
            ChiTietSanPham chiTietSanPham = sanPhamChiTietService.getById(id);
            if (chiTietSanPham != null) {
                return ResponseEntity.ok(chiTietSanPham);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/save")
    public ResponseEntity<ChiTietSanPham> save(@RequestBody ChiTietSanPham chiTietSanPham) {
        try {
            // Set ngÃ y táº¡o náº¿u chÆ°a cÃ³
            if (chiTietSanPham.getNgayTao() == null) {
                chiTietSanPham.setNgayTao(new Date());
            }
            chiTietSanPham.setNgayCapNhat(new Date());

            ChiTietSanPham saved = sanPhamChiTietService.save(chiTietSanPham);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ChiTietSanPham> update(@RequestBody Map<String, Object> requestData, @PathVariable Integer id) {
        try {
            System.out.println("ðŸ”„ Báº¯t Ä‘áº§u cáº­p nháº­t chi tiáº¿t sáº£n pháº©m ID: " + id);
            System.out.println("ðŸ“¥ Dá»¯ liá»‡u nháº­n Ä‘Æ°á»£c: " + requestData);

            // Láº¥y chi tiáº¿t sáº£n pháº©m hiá»‡n táº¡i
            ChiTietSanPham existingChiTiet = sanPhamChiTietService.getById(id);
            if (existingChiTiet == null) {
                System.err.println("âŒ KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t sáº£n pháº©m ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // Cáº­p nháº­t cÃ¡c trÆ°á»ng cÆ¡ báº£n
            if (requestData.containsKey("maChiTiet")) {
                existingChiTiet.setMaChiTiet((String) requestData.get("maChiTiet"));
                System.out.println("âœ… Cáº­p nháº­t maChiTiet: " + requestData.get("maChiTiet"));
            }

            if (requestData.containsKey("soLuong")) {
                existingChiTiet.setSoLuong((Integer) requestData.get("soLuong"));
                System.out.println("âœ… Cáº­p nháº­t soLuong: " + requestData.get("soLuong"));
            }

            if (requestData.containsKey("giaGoc")) {
                Object giaGoc = requestData.get("giaGoc");
                if (giaGoc instanceof Integer) {
                    existingChiTiet.setGiaGoc(((Integer) giaGoc).doubleValue());
                } else if (giaGoc instanceof Double) {
                    existingChiTiet.setGiaGoc((Double) giaGoc);
                }
                System.out.println("âœ… Cáº­p nháº­t giaGoc: " + giaGoc);
            }

            if (requestData.containsKey("giaBan")) {
                Object giaBan = requestData.get("giaBan");
                if (giaBan instanceof Integer) {
                    existingChiTiet.setGiaBan(((Integer) giaBan).doubleValue());
                } else if (giaBan instanceof Double) {
                    existingChiTiet.setGiaBan((Double) giaBan);
                }
                System.out.println("âœ… Cáº­p nháº­t giaBan: " + giaBan);
            }

            if (requestData.containsKey("trangThai")) {
                existingChiTiet.setTrangThai((Integer) requestData.get("trangThai"));
                System.out.println("âœ… Cáº­p nháº­t trangThai: " + requestData.get("trangThai"));
            }

            // Xá»¬ LÃ Cáº¬P NHáº¬T MÃ€U Sáº®C
            if (requestData.containsKey("mauSac")) {
                Map<String, Object> mauSacData = (Map<String, Object>) requestData.get("mauSac");
                if (mauSacData != null && mauSacData.containsKey("id")) {
                    Integer mauSacId = (Integer) mauSacData.get("id");
                    try {
                        // Táº¡o Ä‘á»‘i tÆ°á»£ng MauSac vá»›i ID (giáº£ sá»­ báº¡n cÃ³ entity MauSac)
                        MauSac mauSac = new MauSac();
                        mauSac.setId(mauSacId);
                        // Hoáº·c náº¿u cáº§n load Ä‘áº§y Ä‘á»§: mauSac = mauSacService.getById(mauSacId);

                        existingChiTiet.setMauSac(mauSac);
                        System.out.println("âœ… Cáº­p nháº­t mauSac ID: " + mauSacId);
                    } catch (Exception e) {
                        System.err.println("âŒ Lá»—i khi cáº­p nháº­t mÃ u sáº¯c: " + e.getMessage());
                    }
                }
            }

            // Xá»¬ LÃ Cáº¬P NHáº¬T KÃCH Cá» 
            if (requestData.containsKey("kichCo")) {
                Map<String, Object> kichCoData = (Map<String, Object>) requestData.get("kichCo");
                if (kichCoData != null && kichCoData.containsKey("id")) {
                    Integer kichCoId = (Integer) kichCoData.get("id");
                    try {
                        // Táº¡o Ä‘á»‘i tÆ°á»£ng KichCo vá»›i ID (giáº£ sá»­ báº¡n cÃ³ entity KichCo)
                        KichCo kichCo = new KichCo();
                        kichCo.setId(kichCoId);
                        // Hoáº·c náº¿u cáº§n load Ä‘áº§y Ä‘á»§: kichCo = kichCoService.getById(kichCoId);

                        existingChiTiet.setKichCo(kichCo);
                        System.out.println("âœ… Cáº­p nháº­t kichCo ID: " + kichCoId);
                    } catch (Exception e) {
                        System.err.println("âŒ Lá»—i khi cáº­p nháº­t kÃ­ch cá»¡: " + e.getMessage());
                    }
                }
            }

            // Xá»¬ LÃ Cáº¬P NHáº¬T Sáº¢N PHáº¨M (náº¿u cÃ³)
            if (requestData.containsKey("sanPham")) {
                Map<String, Object> sanPhamData = (Map<String, Object>) requestData.get("sanPham");
                if (sanPhamData != null && sanPhamData.containsKey("id")) {
                    Integer sanPhamId = (Integer) sanPhamData.get("id");
                    try {
                        // Táº¡o Ä‘á»‘i tÆ°á»£ng SanPham vá»›i ID
                        SanPham sanPham = new SanPham();
                        sanPham.setId(sanPhamId);

                        existingChiTiet.setSanPham(sanPham);
                        System.out.println("âœ… Cáº­p nháº­t sanPham ID: " + sanPhamId);
                    } catch (Exception e) {
                        System.err.println("âŒ Lá»—i khi cáº­p nháº­t sáº£n pháº©m: " + e.getMessage());
                    }
                }
            }

            // Xá»¬ LÃ HÃŒNH áº¢NH
            if (requestData.containsKey("hinhAnh")) {
                Map<String, Object> hinhAnhData = (Map<String, Object>) requestData.get("hinhAnh");
                if (hinhAnhData != null && hinhAnhData.containsKey("id")) {
                    Integer hinhAnhId = (Integer) hinhAnhData.get("id");
                    if (hinhAnhId != null) {
                        try {
                            HinhAnh newHinhAnh = hinhAnhService.getHinhanhById(hinhAnhId).orElse(null);
                            existingChiTiet.setHinhAnh(newHinhAnh);
                            System.out.println("âœ… Cáº­p nháº­t hÃ¬nh áº£nh ID: " + hinhAnhId);
                        } catch (Exception e) {
                            System.err.println("âŒ Lá»—i khi tÃ¬m hÃ¬nh áº£nh ID: " + hinhAnhId + " - " + e.getMessage());
                        }
                    }
                } else {
                    existingChiTiet.setHinhAnh(null);
                    System.out.println("âœ… ÄÃ£ xÃ³a hÃ¬nh áº£nh khá»i chi tiáº¿t sáº£n pháº©m");
                }
            }

            // Set ngÃ y cáº­p nháº­t
            existingChiTiet.setNgayCapNhat(new Date());

            // Log trÆ°á»›c khi lÆ°u
            System.out.println("ðŸ“ Tráº¡ng thÃ¡i trÆ°á»›c khi lÆ°u:");
            System.out.println("   - ID: " + existingChiTiet.getId());
            System.out.println("   - MaChiTiet: " + existingChiTiet.getMaChiTiet());
            System.out.println("   - MauSac ID: " + (existingChiTiet.getMauSac() != null ? existingChiTiet.getMauSac().getId() : "null"));
            System.out.println("   - KichCo ID: " + (existingChiTiet.getKichCo() != null ? existingChiTiet.getKichCo().getId() : "null"));

            // LÆ°u vÃ o database
            ChiTietSanPham updated = sanPhamChiTietService.update(existingChiTiet, id);
            if (updated != null) {
                System.out.println("ðŸŽ‰ Cáº­p nháº­t thÃ nh cÃ´ng chi tiáº¿t sáº£n pháº©m ID: " + id);

                // Log sau khi lÆ°u
                System.out.println("ðŸ“ Tráº¡ng thÃ¡i sau khi lÆ°u:");
                System.out.println("   - MauSac ID: " + (updated.getMauSac() != null ? updated.getMauSac().getId() : "null"));
                System.out.println("   - KichCo ID: " + (updated.getKichCo() != null ? updated.getKichCo().getId() : "null"));

                return ResponseEntity.ok(updated);
            }

            System.err.println("âŒ Cáº­p nháº­t tháº¥t báº¡i - khÃ´ng tÃ¬m tháº¥y chi tiáº¿t sáº£n pháº©m");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ðŸ’¥ Lá»—i khi cáº­p nháº­t chi tiáº¿t sáº£n pháº©m ID: " + id + " - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        try {
            boolean deleted = sanPhamChiTietService.delete(id);
            if (deleted) {
                return ResponseEntity.ok("XÃ³a thÃ nh cÃ´ng chi tiáº¿t sáº£n pháº©m cÃ³ ID: " + id);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t sáº£n pháº©m cÃ³ ID: " + id);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lá»—i khi xÃ³a chi tiáº¿t sáº£n pháº©m: " + e.getMessage());
        }
    }

    @GetMapping("/san-pham/{sanPhamId}")
    public ResponseEntity<List<ChiTietSanPham>> getBySanPhamId(@PathVariable Integer sanPhamId) {
        try {
            List<ChiTietSanPham> chiTietSanPhams = sanPhamChiTietService.getBySanPhamId(sanPhamId);
            return ResponseEntity.ok(chiTietSanPhams);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("SanPhamChiTiet Service is running");
    }

    @GetMapping("/debug/{id}")
    public ResponseEntity<Map<String, Object>> debugChiTietSanPham(@PathVariable Integer id) {
        try {
            ChiTietSanPham chiTiet = sanPhamChiTietService.getById(id);

            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("id", chiTiet.getId());
            debugInfo.put("maChiTiet", chiTiet.getMaChiTiet());

            // Debug hÃ¬nh áº£nh
            if (chiTiet.getHinhAnh() != null) {
                HinhAnh hinhAnh = chiTiet.getHinhAnh();
                Map<String, Object> hinhAnhInfo = new HashMap<>();
                hinhAnhInfo.put("id", hinhAnh.getId());
                hinhAnhInfo.put("maHinhAnh", hinhAnh.getMaHinhAnh());
                hinhAnhInfo.put("tenHinhAnh", hinhAnh.getTenHinhAnh());
                hinhAnhInfo.put("duongDan", hinhAnh.getDuongDan());
                hinhAnhInfo.put("trangThai", hinhAnh.getTrangThai());

                // Táº¡o URL Ä‘áº§y Ä‘á»§
                String fullImageUrl = "http://localhost:8080/hinh-anh/images/" +
                        hinhAnh.getDuongDan().replace("/images/", "").replace("/hinh-anh/images/", "");
                hinhAnhInfo.put("fullImageUrl", fullImageUrl);

                debugInfo.put("hinhAnh", hinhAnhInfo);
            } else {
                debugInfo.put("hinhAnh", null);
            }

            return ResponseEntity.ok(debugInfo);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ThÃªm endpoint láº¥y táº¥t cáº£ hÃ¬nh áº£nh available
    @GetMapping("/available-images")
    public ResponseEntity<List<Map<String, Object>>> getAvailableImages() {
        try {
            List<HinhAnh> allImages = hinhAnhService.getAllHinhanh();

            List<Map<String, Object>> result = allImages.stream().map(img -> {
                Map<String, Object> imageInfo = new HashMap<>();
                imageInfo.put("id", img.getId());
                imageInfo.put("maHinhAnh", img.getMaHinhAnh());
                imageInfo.put("tenHinhAnh", img.getTenHinhAnh());
                imageInfo.put("duongDan", img.getDuongDan());
                imageInfo.put("trangThai", img.getTrangThai());

                // Táº¡o URL Ä‘áº§y Ä‘á»§ Ä‘á»ƒ test
                if (img.getDuongDan() != null) {
                    String cleanPath = img.getDuongDan()
                            .replace("/images/", "")
                            .replace("/hinh-anh/images/", "");
                    String fullUrl = "http://localhost:8080/hinh-anh/images/" + cleanPath;
                    imageInfo.put("fullImageUrl", fullUrl);
                }

                return imageInfo;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }


    }
}
