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

    @Autowired(required = false) // Optional injection để tránh lỗi khi chưa có
    private UploadImageService uploadImageService;

    @GetMapping("/by-hoa-don/{hoaDonId}")
    public ResponseEntity<Map<String, Object>> getChiTietTraHangByHoaDon(@PathVariable Integer hoaDonId) {
        try {
            List<ChiTietTraHangDTO> chiTietList = chiTietTraHangService.getChiTietTraHangByHoaDon(hoaDonId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", chiTietList,
                    "total", chiTietList.size(),
                    "message", "Lấy danh sách chi tiết trả hàng thành công"
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
                    "message", "Lấy danh sách chi tiết trả hàng theo sản phẩm thành công"
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
                    "message", "Lấy thông tin chi tiết trả hàng thành công"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // ✅ CẬP NHẬT: API tạo chi tiết trả hàng với hỗ trợ hoaDonId
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createChiTietTraHang(@RequestBody ChiTietTraHangDTO request) {
        try {
            // Kiểm tra dữ liệu đầu vào
            if (request.getChiTietSanPhamId() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lòng chọn sản phẩm"
                ));
            }

            if (request.getSoLuong() == null || request.getSoLuong() <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lòng nhập số lượng hợp lệ"
                ));
            }

            // ✅ THÊM: Validate lý do trả hàng
            if (request.getLyDo() == null || request.getLyDo().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lòng nhập lý do trả hàng"
                ));
            }

            // Validate hoaDonId nếu được cung cấp
            if (request.getHoaDonId() != null && request.getHoaDonId() <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "ID hóa đơn không hợp lệ"
                ));
            }

            ChiTietTraHangDTO created = chiTietTraHangService.createChiTietTraHang(request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", created,
                    "message", "Tạo yêu cầu trả hàng thành công"
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
                        "message", "Vui lòng chọn ảnh để upload"
                ));
            }

            // Upload ảnh mới
            String duongDanAnh = uploadImageService.saveImage(anhMinhChung, "return-images");

            // Cập nhật đường dẫn ảnh
            ChiTietTraHangDTO dto = new ChiTietTraHangDTO();
            dto.setDuongDanAnh(duongDanAnh);

            ChiTietTraHangDTO updated = chiTietTraHangService.updateChiTietTraHang(id, dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", updated,
                    "message", "Cập nhật ảnh minh chứng thành công"
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
            // Validate dữ liệu đầu vào
            if (chiTietSanPhamId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lòng chọn sản phẩm"
                ));
            }

            if (soLuong == null || soLuong <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lòng nhập số lượng hợp lệ"
                ));
            }

            if (lyDo == null || lyDo.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lòng nhập lý do trả hàng"
                ));
            }

            // Xử lý upload ảnh nếu có
            String duongDanAnh = null;
            if (anhMinhChung != null && !anhMinhChung.isEmpty()) {
                duongDanAnh = uploadImageService.saveImage(anhMinhChung, "return-images");
            }

            // Tạo DTO
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
                    "message", "Tạo yêu cầu trả hàng thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // ✅ THÊM: API tạo chi tiết trả hàng từ hóa đơn cụ thể
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
                        "message", "Thiếu thông tin bắt buộc"
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
                    "message", "Tạo yêu cầu trả hàng từ hóa đơn thành công"
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
            // Validate lý do nếu được cập nhật
            if (request.getLyDo() != null && request.getLyDo().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Lý do trả hàng không được để trống"
                ));
            }

            ChiTietTraHangDTO updated = chiTietTraHangService.updateChiTietTraHang(id, request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", updated,
                    "message", "Cập nhật chi tiết trả hàng thành công"
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
                        "message", "Vui lòng chọn trạng thái"
                ));
            }

            ChiTietTraHangDTO updated = chiTietTraHangService.updateTrangThai(id, trangThai, ghiChu);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", updated,
                    "message", "Cập nhật trạng thái thành công"
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
                    "message", "Chấp nhận trả hàng thành công (không hoàn kho)."
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
                        "message", "Vui lòng nhập lý do từ chối"
                ));
            }

            ChiTietTraHangDTO updated = chiTietTraHangService.rejectReturn(id, lyDoTuChoi);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", updated,
                    "message", "Từ chối trả hàng thành công"
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
                    "message", "Xóa chi tiết trả hàng thành công"
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

            // ✅ THÊM: Lấy thông tin tổng số đã mua
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
                            "Có thể trả hàng" :
                            String.format("Không thể trả %d sản phẩm. Đã mua: %d, đã trả: %d, có thể trả thêm: %d",
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
                    "message", "Lấy thống kê trả hàng thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // ✅ THÊM: API lấy danh sách trả hàng theo trạng thái
    @GetMapping("/by-status/{trangThai}")
    public ResponseEntity<Map<String, Object>> getByStatus(@PathVariable String trangThai) {
        try {
            // Implement this method in service if needed
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Endpoint sẽ được implement"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // ✅ THÊM: API kiểm tra trả hàng cho một hóa đơn cụ thể
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
                    "message", "Kiểm tra trả hàng thành công"
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

            // Lấy chi tiết các yêu cầu trả hàng
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
                    "message", "Chấp nhận trả hàng và hoàn lại kho thành công"
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
                    "message", "Chấp nhận trả hàng nhưng không hoàn kho"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}
