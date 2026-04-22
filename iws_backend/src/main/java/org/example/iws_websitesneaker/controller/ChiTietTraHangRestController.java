package org.example.iws_websitesneaker.controller;

import org.example.iws_websitesneaker.Dto.ChiTietTraHangDTO;
import org.example.iws_websitesneaker.Service.ChiTietTraHangService;
import org.example.iws_websitesneaker.Service.UploadImageService;
import org.example.iws_websitesneaker.repository.HoaDonChiTietRepository;
import org.example.iws_websitesneaker.repository.RepoChiTietTraHang;
import org.example.iws_websitesneaker.repository.RepoHoaDonChiTiet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chi-tiet-tra-hang")
@CrossOrigin(origins = "http://localhost:5173")
public class ChiTietTraHangRestController {

    @Autowired
    private ChiTietTraHangService chiTietTraHangService;

    @Autowired
    private RepoHoaDonChiTiet hoaDonChiTietRepository;

    @Autowired(required = false) // Optional injection Ä‘á»ƒ trÃ¡nh lá»—i khi chÆ°a cÃ³
    private UploadImageService uploadImageService;

    @GetMapping("/by-hoa-don/{hoaDonId}")
    public ResponseEntity<Map<String, Object>> getChiTietTraHangByHoaDon(@PathVariable Integer hoaDonId) {
        try {
            List<ChiTietTraHangDTO> chiTietList = chiTietTraHangService.getChiTietTraHangByHoaDon(hoaDonId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", chiTietList,
                    "total", chiTietList.size(),
                    "message", "Láº¥y danh sÃ¡ch chi tiáº¿t tráº£ hÃ ng thÃ nh cÃ´ng"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/by-chi-tiet-san-pham/{chiTietSanPhamId}")
    public ResponseEntity<Map<String, Object>> getChiTietTraHangBySanPham(@PathVariable Integer chiTietSanPhamId) {
        try {
            List<ChiTietTraHangDTO> chiTietList = chiTietTraHangService.getChiTietTraHangByChiTietSanPham(chiTietSanPhamId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", chiTietList,
                    "total", chiTietList.size(),
                    "message", "Láº¥y danh sÃ¡ch chi tiáº¿t tráº£ hÃ ng theo sáº£n pháº©m thÃ nh cÃ´ng"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Integer id) {
        try {
            ChiTietTraHangDTO chiTiet = chiTietTraHangService.getById(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", chiTiet,
                    "message", "Láº¥y thÃ´ng tin chi tiáº¿t tráº£ hÃ ng thÃ nh cÃ´ng"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // âœ… Cáº¬P NHáº¬T: API táº¡o chi tiáº¿t tráº£ hÃ ng vá»›i há»— trá»£ hoaDonId
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createChiTietTraHang(@RequestBody ChiTietTraHangDTO request) {
        try {
            // Kiá»ƒm tra dá»¯ liá»‡u Ä‘áº§u vÃ o
            if (request.getChiTietSanPhamId() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lÃ²ng chá»n sáº£n pháº©m"
                ));
            }

            if (request.getSoLuong() == null || request.getSoLuong() <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lÃ²ng nháº­p sá»‘ lÆ°á»£ng há»£p lá»‡"
                ));
            }

            // âœ… THÃŠM: Validate lÃ½ do tráº£ hÃ ng
            if (request.getLyDo() == null || request.getLyDo().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lÃ²ng nháº­p lÃ½ do tráº£ hÃ ng"
                ));
            }

            // Validate hoaDonId náº¿u Ä‘Æ°á»£c cung cáº¥p
            if (request.getHoaDonId() != null && request.getHoaDonId() <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "ID hÃ³a Ä‘Æ¡n khÃ´ng há»£p lá»‡"
                ));
            }

            ChiTietTraHangDTO created = chiTietTraHangService.createChiTietTraHang(request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", created,
                    "message", "Táº¡o yÃªu cáº§u tráº£ hÃ ng thÃ nh cÃ´ng"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/update-image")
    public ResponseEntity<Map<String, Object>> updateReturnImage(
            @PathVariable Integer id,
            @RequestParam("anhMinhChung") MultipartFile anhMinhChung) {
        try {
            if (anhMinhChung == null || anhMinhChung.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lÃ²ng chá»n áº£nh Ä‘á»ƒ upload"
                ));
            }

            // Upload áº£nh má»›i
            String duongDanAnh = uploadImageService.saveImage(anhMinhChung, "return-images");

            // Cáº­p nháº­t Ä‘Æ°á»ng dáº«n áº£nh
            ChiTietTraHangDTO dto = new ChiTietTraHangDTO();
            dto.setDuongDanAnh(duongDanAnh);

            ChiTietTraHangDTO updated = chiTietTraHangService.updateChiTietTraHang(id, dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", updated,
                    "message", "Cáº­p nháº­t áº£nh minh chá»©ng thÃ nh cÃ´ng"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/create-with-image")
    public ResponseEntity<Map<String, Object>> createChiTietTraHangWithImage(
            @RequestParam("chiTietSanPhamId") Integer chiTietSanPhamId,
            @RequestParam("soLuong") Integer soLuong,
            @RequestParam("lyDo") String lyDo,
            @RequestParam(value = "hoaDonId", required = false) Integer hoaDonId,
            @RequestParam(value = "anhMinhChung", required = false) MultipartFile anhMinhChung) {
        try {
            // Validate dá»¯ liá»‡u Ä‘áº§u vÃ o
            if (chiTietSanPhamId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lÃ²ng chá»n sáº£n pháº©m"
                ));
            }

            if (soLuong == null || soLuong <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lÃ²ng nháº­p sá»‘ lÆ°á»£ng há»£p lá»‡"
                ));
            }

            if (lyDo == null || lyDo.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lÃ²ng nháº­p lÃ½ do tráº£ hÃ ng"
                ));
            }

            // Xá»­ lÃ½ upload áº£nh náº¿u cÃ³
            String duongDanAnh = null;
            if (anhMinhChung != null && !anhMinhChung.isEmpty()) {
                duongDanAnh = uploadImageService.saveImage(anhMinhChung, "return-images");
            }

            // Táº¡o DTO
            ChiTietTraHangDTO dto = new ChiTietTraHangDTO();
            dto.setChiTietSanPhamId(chiTietSanPhamId);
            dto.setSoLuong(soLuong);
            dto.setLyDo(lyDo);
            dto.setDuongDanAnh(duongDanAnh);
            dto.setHoaDonId(hoaDonId);
            dto.setTrangThaiHoaDon("PENDING");

            ChiTietTraHangDTO created = chiTietTraHangService.createChiTietTraHang(dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", created,
                    "message", "Táº¡o yÃªu cáº§u tráº£ hÃ ng thÃ nh cÃ´ng"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // âœ… THÃŠM: API táº¡o chi tiáº¿t tráº£ hÃ ng tá»« hÃ³a Ä‘Æ¡n cá»¥ thá»ƒ
    @PostMapping("/create-from-invoice")
    public ResponseEntity<Map<String, Object>> createChiTietTraHangFromInvoice(@RequestBody Map<String, Object> request) {
        try {
            Integer hoaDonId = (Integer) request.get("hoaDonId");
            Integer chiTietSanPhamId = (Integer) request.get("chiTietSanPhamId");
            Integer soLuong = (Integer) request.get("soLuong");
            String lyDo = (String) request.get("lyDo");

            if (hoaDonId == null || chiTietSanPhamId == null || soLuong == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Thiáº¿u thÃ´ng tin báº¯t buá»™c"
                ));
            }

            ChiTietTraHangDTO dto = new ChiTietTraHangDTO();
            dto.setHoaDonId(hoaDonId);
            dto.setChiTietSanPhamId(chiTietSanPhamId);
            dto.setSoLuong(soLuong);
            dto.setTrangThaiHoaDon("PENDING");

            ChiTietTraHangDTO created = chiTietTraHangService.createChiTietTraHang(dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", created,
                    "message", "Táº¡o yÃªu cáº§u tráº£ hÃ ng tá»« hÃ³a Ä‘Æ¡n thÃ nh cÃ´ng"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateChiTietTraHang(
            @PathVariable Integer id,
            @RequestBody ChiTietTraHangDTO request) {
        try {
            // Validate lÃ½ do náº¿u Ä‘Æ°á»£c cáº­p nháº­t
            if (request.getLyDo() != null && request.getLyDo().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "LÃ½ do tráº£ hÃ ng khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng"
                ));
            }

            ChiTietTraHangDTO updated = chiTietTraHangService.updateChiTietTraHang(id, request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", updated,
                    "message", "Cáº­p nháº­t chi tiáº¿t tráº£ hÃ ng thÃ nh cÃ´ng"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<Map<String, Object>> updateTrangThai(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {
        try {
            String trangThai = request.get("trangThai");
            String ghiChu = request.get("ghiChu");

            if (trangThai == null || trangThai.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lÃ²ng chá»n tráº¡ng thÃ¡i"
                ));
            }

            ChiTietTraHangDTO updated = chiTietTraHangService.updateTrangThai(id, trangThai, ghiChu);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", updated,
                    "message", "Cáº­p nháº­t tráº¡ng thÃ¡i thÃ nh cÃ´ng"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveReturn(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {
        try {
            String ghiChu = request.get("ghiChu");
            ChiTietTraHangDTO updated = chiTietTraHangService.approveReturn(id, ghiChu);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", updated,
                    "message", "Cháº¥p nháº­n tráº£ hÃ ng thÃ nh cÃ´ng (khÃ´ng hoÃ n kho)."
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectReturn(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {
        try {
            String lyDoTuChoi = request.get("lyDoTuChoi");
            if (lyDoTuChoi == null || lyDoTuChoi.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lÃ²ng nháº­p lÃ½ do tá»« chá»‘i"
                ));
            }

            ChiTietTraHangDTO updated = chiTietTraHangService.rejectReturn(id, lyDoTuChoi);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", updated,
                    "message", "Tá»« chá»‘i tráº£ hÃ ng thÃ nh cÃ´ng"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteChiTietTraHang(@PathVariable Integer id) {
        try {
            chiTietTraHangService.deleteChiTietTraHang(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "XÃ³a chi tiáº¿t tráº£ hÃ ng thÃ nh cÃ´ng"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/can-return/{chiTietSanPhamId}")
    public ResponseEntity<Map<String, Object>> checkCanReturn(
            @PathVariable Integer chiTietSanPhamId,
            @RequestParam Integer soLuong) {
        try {
            boolean canReturn = chiTietTraHangService.canReturn(chiTietSanPhamId, soLuong);
            Integer totalReturned = chiTietTraHangService.getTotalReturnedQuantity(chiTietSanPhamId);

            // âœ… THÃŠM: Láº¥y thÃ´ng tin tá»•ng sá»‘ Ä‘Ã£ mua
            Integer totalSold = hoaDonChiTietRepository.getTotalSoldQuantity(chiTietSanPhamId);
            Integer canReturnMore = (totalSold != null && totalReturned != null) ? (totalSold - totalReturned) : 0;

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "canReturn", canReturn,
                    "totalSold", totalSold != null ? totalSold : 0,
                    "totalReturned", totalReturned != null ? totalReturned : 0,
                    "canReturnMore", Math.max(0, canReturnMore),
                    "requestedQuantity", soLuong,
                    "message", canReturn ?
                            "CÃ³ thá»ƒ tráº£ hÃ ng" :
                            String.format("KhÃ´ng thá»ƒ tráº£ %d sáº£n pháº©m. ÄÃ£ mua: %d, Ä‘Ã£ tráº£: %d, cÃ³ thá»ƒ tráº£ thÃªm: %d",
                                    soLuong, totalSold != null ? totalSold : 0, totalReturned != null ? totalReturned : 0, canReturnMore)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/thong-ke-tra-hang/{hoaDonId}")
    public ResponseEntity<Map<String, Object>> getReturnStatistics(@PathVariable Integer hoaDonId) {
        try {
            Map<String, Object> statistics = chiTietTraHangService.getReturnStatistics(hoaDonId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", statistics,
                    "message", "Láº¥y thá»‘ng kÃª tráº£ hÃ ng thÃ nh cÃ´ng"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // âœ… THÃŠM: API láº¥y danh sÃ¡ch tráº£ hÃ ng theo tráº¡ng thÃ¡i
    @GetMapping("/by-status/{trangThai}")
    public ResponseEntity<Map<String, Object>> getByStatus(@PathVariable String trangThai) {
        try {
            // Implement this method in service if needed
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Endpoint sáº½ Ä‘Æ°á»£c implement"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // âœ… THÃŠM: API kiá»ƒm tra tráº£ hÃ ng cho má»™t hÃ³a Ä‘Æ¡n cá»¥ thá»ƒ
    @GetMapping("/check-invoice/{hoaDonId}")
    public ResponseEntity<Map<String, Object>> checkInvoiceReturns(@PathVariable Integer hoaDonId) {
        try {
            List<ChiTietTraHangDTO> returns = chiTietTraHangService.getChiTietTraHangByHoaDon(hoaDonId);
            Map<String, Object> statistics = chiTietTraHangService.getReturnStatistics(hoaDonId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of(
                            "returns", returns,
                            "statistics", statistics,
                            "hasReturns", !returns.isEmpty()
                    ),
                    "message", "Kiá»ƒm tra tráº£ hÃ ng thÃ nh cÃ´ng"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    @GetMapping("/debug/{chiTietSanPhamId}")
    public ResponseEntity<Map<String, Object>> debugReturnInfo(@PathVariable Integer chiTietSanPhamId) {
        try {
            Integer totalSold = hoaDonChiTietRepository.getTotalSoldQuantity(chiTietSanPhamId);
            Integer totalReturned = chiTietTraHangService.getTotalReturnedQuantity(chiTietSanPhamId);

            // Láº¥y chi tiáº¿t cÃ¡c yÃªu cáº§u tráº£ hÃ ng
            List<ChiTietTraHangDTO> returns = chiTietTraHangService.getChiTietTraHangByChiTietSanPham(chiTietSanPhamId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of(
                            "chiTietSanPhamId", chiTietSanPhamId,
                            "totalSold", totalSold != null ? totalSold : 0,
                            "totalReturned", totalReturned != null ? totalReturned : 0,
                            "canReturnMore", (totalSold != null && totalReturned != null) ? (totalSold - totalReturned) : 0,
                            "returnHistory", returns
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/approve-with-inventory")
    public ResponseEntity<Map<String, Object>> approveReturnWithInventory(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {
        try {
            String ghiChu = request.get("ghiChu");
            ChiTietTraHangDTO updated = chiTietTraHangService.approveReturnWithInventory(id, ghiChu);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", updated,
                    "message", "Cháº¥p nháº­n tráº£ hÃ ng vÃ  hoÃ n láº¡i kho thÃ nh cÃ´ng"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/approve-no-inventory")
    public ResponseEntity<Map<String, Object>> approveReturnNoInventory(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {
        try {
            String ghiChu = request.get("ghiChu");
            ChiTietTraHangDTO updated = chiTietTraHangService.approveReturnNoInventory(id, ghiChu);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", updated,
                    "message", "Cháº¥p nháº­n tráº£ hÃ ng nhÆ°ng khÃ´ng hoÃ n kho"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}
