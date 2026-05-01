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
            // Set ngày tạo nếu chưa có
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
            System.out.println("🔄 Bắt đầu cập nhật chi tiết sản phẩm ID: " + id);
            System.out.println("📥 Dữ liệu nhận được: " + requestData);

            // Lấy chi tiết sản phẩm hiện tại
            ChiTietSanPham existingChiTiet = sanPhamChiTietService.getById(id);
            if (existingChiTiet == null) {
                System.err.println("❌ Không tìm thấy chi tiết sản phẩm ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // Cập nhật các trường cơ bản
            if (requestData.containsKey("maChiTiet")) {
                existingChiTiet.setMaChiTiet((String) requestData.get("maChiTiet"));
                System.out.println("✅ Cập nhật maChiTiet: " + requestData.get("maChiTiet"));
            }

            if (requestData.containsKey("soLuong")) {
                existingChiTiet.setSoLuong((Integer) requestData.get("soLuong"));
                System.out.println("✅ Cập nhật soLuong: " + requestData.get("soLuong"));
            }

            if (requestData.containsKey("giaGoc")) {
                Object giaGoc = requestData.get("giaGoc");
                if (giaGoc instanceof Integer) {
                    existingChiTiet.setGiaGoc(((Integer) giaGoc).doubleValue());
                } else if (giaGoc instanceof Double) {
                    existingChiTiet.setGiaGoc((Double) giaGoc);
                }
                System.out.println("✅ Cập nhật giaGoc: " + giaGoc);
            }

            if (requestData.containsKey("giaBan")) {
                Object giaBan = requestData.get("giaBan");
                if (giaBan instanceof Integer) {
                    existingChiTiet.setGiaBan(((Integer) giaBan).doubleValue());
                } else if (giaBan instanceof Double) {
                    existingChiTiet.setGiaBan((Double) giaBan);
                }
                System.out.println("✅ Cập nhật giaBan: " + giaBan);
            }

            if (requestData.containsKey("trangThai")) {
                existingChiTiet.setTrangThai((Integer) requestData.get("trangThai"));
                System.out.println("✅ Cập nhật trangThai: " + requestData.get("trangThai"));
            }

            // XỬ LÝ CẬP NHẬT MÀU SẮC
            if (requestData.containsKey("mauSac")) {
                Map<String, Object> mauSacData = (Map<String, Object>) requestData.get("mauSac");
                if (mauSacData != null && mauSacData.containsKey("id")) {
                    Integer mauSacId = (Integer) mauSacData.get("id");
                    try {
                        // Tạo đối tượng MauSac với ID (giả sử bạn có entity MauSac)
                        MauSac mauSac = new MauSac();
                        mauSac.setId(mauSacId);
                        // Hoặc nếu cần load đầy đủ: mauSac = mauSacService.getById(mauSacId);

                        existingChiTiet.setMauSac(mauSac);
                        System.out.println("✅ Cập nhật mauSac ID: " + mauSacId);
                    } catch (Exception e) {
                        System.err.println("❌ Lỗi khi cập nhật màu sắc: " + e.getMessage());
                    }
                }
            }

            // XỬ LÝ CẬP NHẬT KÍCH CỠ
            if (requestData.containsKey("kichCo")) {
                Map<String, Object> kichCoData = (Map<String, Object>) requestData.get("kichCo");
                if (kichCoData != null && kichCoData.containsKey("id")) {
                    Integer kichCoId = (Integer) kichCoData.get("id");
                    try {
                        // Tạo đối tượng KichCo với ID (giả sử bạn có entity KichCo)
                        KichCo kichCo = new KichCo();
                        kichCo.setId(kichCoId);
                        // Hoặc nếu cần load đầy đủ: kichCo = kichCoService.getById(kichCoId);

                        existingChiTiet.setKichCo(kichCo);
                        System.out.println("✅ Cập nhật kichCo ID: " + kichCoId);
                    } catch (Exception e) {
                        System.err.println("❌ Lỗi khi cập nhật kích cỡ: " + e.getMessage());
                    }
                }
            }

            // XỬ LÝ CẬP NHẬT SẢN PHẨM (nếu có)
            if (requestData.containsKey("sanPham")) {
                Map<String, Object> sanPhamData = (Map<String, Object>) requestData.get("sanPham");
                if (sanPhamData != null && sanPhamData.containsKey("id")) {
                    Integer sanPhamId = (Integer) sanPhamData.get("id");
                    try {
                        // Tạo đối tượng SanPham với ID
                        SanPham sanPham = new SanPham();
                        sanPham.setId(sanPhamId);

                        existingChiTiet.setSanPham(sanPham);
                        System.out.println("✅ Cập nhật sanPham ID: " + sanPhamId);
                    } catch (Exception e) {
                        System.err.println("❌ Lỗi khi cập nhật sản phẩm: " + e.getMessage());
                    }
                }
            }

            // XỬ LÝ HÌNH ẢNH
            if (requestData.containsKey("hinhAnh")) {
                Map<String, Object> hinhAnhData = (Map<String, Object>) requestData.get("hinhAnh");
                if (hinhAnhData != null && hinhAnhData.containsKey("id")) {
                    Integer hinhAnhId = (Integer) hinhAnhData.get("id");
                    if (hinhAnhId != null) {
                        try {
                            HinhAnh newHinhAnh = hinhAnhService.getHinhanhById(hinhAnhId).orElse(null);
                            existingChiTiet.setHinhAnh(newHinhAnh);
                            System.out.println("✅ Cập nhật hình ảnh ID: " + hinhAnhId);
                        } catch (Exception e) {
                            System.err.println("❌ Lỗi khi tìm hình ảnh ID: " + hinhAnhId + " - " + e.getMessage());
                        }
                    }
                } else {
                    existingChiTiet.setHinhAnh(null);
                    System.out.println("✅ Đã xóa hình ảnh khỏi chi tiết sản phẩm");
                }
            }

            // Set ngày cập nhật
            existingChiTiet.setNgayCapNhat(new Date());

            // Log trước khi lưu
            System.out.println("📝 Trạng thái trước khi lưu:");
            System.out.println("   - ID: " + existingChiTiet.getId());
            System.out.println("   - MaChiTiet: " + existingChiTiet.getMaChiTiet());
            System.out.println("   - MauSac ID: " + (existingChiTiet.getMauSac() != null ? existingChiTiet.getMauSac().getId() : "null"));
            System.out.println("   - KichCo ID: " + (existingChiTiet.getKichCo() != null ? existingChiTiet.getKichCo().getId() : "null"));

            // Lưu vào database
            ChiTietSanPham updated = sanPhamChiTietService.update(existingChiTiet, id);
            if (updated != null) {
                System.out.println("🎉 Cập nhật thành công chi tiết sản phẩm ID: " + id);

                // Log sau khi lưu
                System.out.println("📝 Trạng thái sau khi lưu:");
                System.out.println("   - MauSac ID: " + (updated.getMauSac() != null ? updated.getMauSac().getId() : "null"));
                System.out.println("   - KichCo ID: " + (updated.getKichCo() != null ? updated.getKichCo().getId() : "null"));

                return ResponseEntity.ok(updated);
            }

            System.err.println("❌ Cập nhật thất bại - không tìm thấy chi tiết sản phẩm");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("💥 Lỗi khi cập nhật chi tiết sản phẩm ID: " + id + " - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        try {
            boolean deleted = sanPhamChiTietService.delete(id);
            if (deleted) {
                return ResponseEntity.ok("Xóa thành công chi tiết sản phẩm có ID: " + id);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy chi tiết sản phẩm có ID: " + id);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa chi tiết sản phẩm: " + e.getMessage());
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

            // Debug hình ảnh
            if (chiTiet.getHinhAnh() != null) {
                HinhAnh hinhAnh = chiTiet.getHinhAnh();
                Map<String, Object> hinhAnhInfo = new HashMap<>();
                hinhAnhInfo.put("id", hinhAnh.getId());
                hinhAnhInfo.put("maHinhAnh", hinhAnh.getMaHinhAnh());
                hinhAnhInfo.put("tenHinhAnh", hinhAnh.getTenHinhAnh());
                hinhAnhInfo.put("duongDan", hinhAnh.getDuongDan());
                hinhAnhInfo.put("trangThai", hinhAnh.getTrangThai());

                // Tạo URL đầy đủ
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

    // Thêm endpoint lấy tất cả hình ảnh available
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

                // Tạo URL đầy đủ để test
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
