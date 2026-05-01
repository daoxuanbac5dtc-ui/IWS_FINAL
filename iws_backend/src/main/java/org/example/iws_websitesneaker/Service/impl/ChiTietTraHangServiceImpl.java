package org.example.iws_websitesneaker.Service.impl;

import org.example.iws_websitesneaker.Dto.ChiTietTraHangDTO;
import org.example.iws_websitesneaker.Service.ChiTietTraHangService;
import org.example.iws_websitesneaker.entity.*;
import org.example.iws_websitesneaker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChiTietTraHangServiceImpl implements ChiTietTraHangService {

    @Autowired
    private RepoChiTietTraHang chiTietTraHangRepository;

    @Autowired
    private RepoChiTietSanPham chiTietSanPhamRepository;

    @Autowired
    private RepoHoaDonChiTiet hoaDonChiTietRepository;

    @Autowired
    private RepoHoaDon hoaDonRepository;

    @Override
    public List<ChiTietTraHangDTO> getChiTietTraHangByChiTietSanPham(Integer chiTietSanPhamId) {
        try {
            return chiTietTraHangRepository.findByChiTietSanPhamIdWithFullInfo(chiTietSanPhamId).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Fallback to basic query without full info
            return chiTietTraHangRepository.findByChiTietSanPhamId(chiTietSanPhamId).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public ChiTietTraHangDTO getById(Integer id) {
        ChiTietTraHang chiTiet = chiTietTraHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết trả hàng với ID: " + id));
        return convertToDTO(chiTiet);
    }

    @Override
    public List<ChiTietTraHangDTO> getChiTietTraHangByHoaDon(Integer hoaDonId) {
        try {
            return chiTietTraHangRepository.findByHoaDonIdWithFullInfo(hoaDonId).stream()
                    .map(this::convertToDTO)
                    .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao())) // ✅ SỬA
                    .collect(Collectors.toList());
        } catch (Exception e) {
            try {
                return chiTietTraHangRepository.findByHoaDonIdWithFullInfoFallback(hoaDonId).stream()
                        .map(this::convertToDTO)
                        .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao())) // ✅ SỬA
                        .collect(Collectors.toList());
            } catch (Exception e2) {
                List<HoaDonChiTiet> hoaDonChiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
                return hoaDonChiTiets.stream()
                        .flatMap(hdct -> chiTietTraHangRepository.findByChiTietSanPhamId(hdct.getChiTietSanPham().getId()).stream())
                        .map(this::convertToDTO)
                        .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao())) // ✅ SỬA
                        .collect(Collectors.toList());
            }
        }
    }

    @Override
    public ChiTietTraHangDTO createChiTietTraHang(ChiTietTraHangDTO dto) {
        // Validate input cơ bản
        if (dto.getChiTietSanPhamId() == null) {
            throw new RuntimeException("ID chi tiết sản phẩm không được để trống");
        }

        if (dto.getSoLuong() == null || dto.getSoLuong() <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        if (dto.getLyDo() == null || dto.getLyDo().trim().isEmpty()) {
            throw new RuntimeException("Lý do trả hàng không được để trống");
        }

        // Kiểm tra chi tiết sản phẩm tồn tại
        ChiTietSanPham ctsp = chiTietSanPhamRepository.findById(dto.getChiTietSanPhamId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết sản phẩm với ID: " + dto.getChiTietSanPhamId()));

        // Xử lý hóa đơn
        HoaDon hoaDon = null;
        if (dto.getHoaDonId() != null) {
            hoaDon = hoaDonRepository.findById(dto.getHoaDonId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + dto.getHoaDonId()));
        } else {
            // Tự động tìm hóa đơn từ chi tiết sản phẩm
            List<HoaDonChiTiet> hoaDonChiTiets = hoaDonChiTietRepository.findByChiTietSanPhamId(dto.getChiTietSanPhamId());
            if (!hoaDonChiTiets.isEmpty()) {
                hoaDonChiTiets.sort((a, b) -> b.getNgayTao().compareTo(a.getNgayTao()));
                hoaDon = hoaDonChiTiets.get(0).getHoaDon();
                dto.setHoaDonId(hoaDon.getId());
            }
        }

        // ✅ QUAN TRỌNG: Kiểm tra có thể trả hàng không với logic mới
        if (!canReturn(dto.getChiTietSanPhamId(), dto.getSoLuong())) {
            // Thêm thông tin chi tiết vào error message
            Integer tongDaMua = hoaDonChiTietRepository.getTotalSoldQuantity(dto.getChiTietSanPhamId());
            Integer tongDaTra = getTotalReturnedQuantity(dto.getChiTietSanPhamId());

            String errorMsg = String.format(
                    "Không thể trả %d sản phẩm. Đã mua: %d, đã trả: %d, có thể trả thêm: %d",
                    dto.getSoLuong(),
                    tongDaMua != null ? tongDaMua : 0,
                    tongDaTra != null ? tongDaTra : 0,
                    (tongDaMua != null && tongDaTra != null) ? (tongDaMua - tongDaTra) : 0
            );

            throw new RuntimeException(errorMsg);
        }

        // Kiểm tra trạng thái hóa đơn - CHỈ check nếu có hóa đơn
        if (hoaDon != null) {
            // ✅ SỬA: Cho phép trả với nhiều trạng thái hơn
            Set<String> allowedStatuses = Set.of("COMPLETED", "DA_THANH_TOAN", "DELIVERED", "DA_GIAO", "HOAN_THANH");
            if (!allowedStatuses.contains(hoaDon.getTrangThaiHoaDon())) {
                throw new RuntimeException("Chỉ có thể tạo yêu cầu trả hàng cho đơn hàng đã giao thành công. Trạng thái hiện tại: " + hoaDon.getTrangThaiHoaDon());
            }
        }

        // Tạo chi tiết trả hàng
        ChiTietTraHang chiTiet = new ChiTietTraHang();
        chiTiet.setMaChiTietTraHang(generateMaChiTietTraHang());
        chiTiet.setSoLuong(dto.getSoLuong());
        chiTiet.setTrangThaiHoaDon(dto.getTrangThaiHoaDon() != null ? dto.getTrangThaiHoaDon() : "PENDING");
        chiTiet.setChiTietSanPham(ctsp);
        chiTiet.setLyDo(dto.getLyDo());
        chiTiet.setDuongDanAnh(dto.getDuongDanAnh());

        if (hoaDon != null) {
            chiTiet.setHoaDon(hoaDon);
        }

        Date now = new Date();
        chiTiet.setNgayTao(now);
        chiTiet.setNgayTaoTraHang(now);
        chiTiet.setNgayCapNhat(now);

        ChiTietTraHang saved = chiTietTraHangRepository.save(chiTiet);

        System.out.println("✅ Tạo thành công chi tiết trả hàng: " + saved.getMaChiTietTraHang());

        return convertToDTO(saved);
    }

    @Override
    public ChiTietTraHangDTO updateChiTietTraHang(Integer id, ChiTietTraHangDTO dto) {
        ChiTietTraHang chiTiet = chiTietTraHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết trả hàng với ID: " + id));

        // Chỉ cho phép cập nhật khi trạng thái là PENDING
        if (!"PENDING".equals(chiTiet.getTrangThaiHoaDon())) {
            throw new RuntimeException("Không thể cập nhật chi tiết trả hàng ở trạng thái: " + chiTiet.getTrangThaiHoaDon());
        }

        boolean hasChanges = false;

        // Cập nhật số lượng nếu có
        if (dto.getSoLuong() != null && dto.getSoLuong() > 0 && !dto.getSoLuong().equals(chiTiet.getSoLuong())) {
            // Kiểm tra số lượng có hợp lệ không
            if (!canReturn(chiTiet.getChiTietSanPham().getId(), dto.getSoLuong())) {
                throw new RuntimeException("Không thể trả hàng với số lượng này");
            }
            chiTiet.setSoLuong(dto.getSoLuong());
            hasChanges = true;
        }

        // ✅ THÊM: Cập nhật lý do nếu có
        if (dto.getLyDo() != null && !dto.getLyDo().trim().isEmpty() && !dto.getLyDo().equals(chiTiet.getLyDo())) {
            chiTiet.setLyDo(dto.getLyDo());
            hasChanges = true;
        }

        // ✅ THÊM: Cập nhật ảnh nếu có
        if (dto.getDuongDanAnh() != null && !dto.getDuongDanAnh().equals(chiTiet.getDuongDanAnh())) {
            chiTiet.setDuongDanAnh(dto.getDuongDanAnh());
            hasChanges = true;
        }

        // Cập nhật trạng thái nếu có
        if (dto.getTrangThaiHoaDon() != null && !dto.getTrangThaiHoaDon().equals(chiTiet.getTrangThaiHoaDon())) {
            if (!isValidStatusTransition(chiTiet.getTrangThaiHoaDon(), dto.getTrangThaiHoaDon())) {
                throw new RuntimeException("Không thể chuyển từ trạng thái " + chiTiet.getTrangThaiHoaDon() + " sang " + dto.getTrangThaiHoaDon());
            }
            chiTiet.setTrangThaiHoaDon(dto.getTrangThaiHoaDon());
            hasChanges = true;
        }

        if (hasChanges) {
            chiTiet.setNgayCapNhat(new Date());
            ChiTietTraHang saved = chiTietTraHangRepository.save(chiTiet);
            return convertToDTO(saved);
        }

        return convertToDTO(chiTiet);
    }

    @Override
    public ChiTietTraHangDTO updateTrangThai(Integer id, String trangThai, String ghiChu) {
        if (trangThai == null || trangThai.trim().isEmpty()) {
            throw new RuntimeException("Trạng thái không được để trống");
        }

        ChiTietTraHang chiTiet = chiTietTraHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết trả hàng với ID: " + id));

        String oldStatus = chiTiet.getTrangThaiHoaDon();

        // Kiểm tra logic chuyển trạng thái
        if (!isValidStatusTransition(oldStatus, trangThai)) {
            throw new RuntimeException("Không thể chuyển từ trạng thái " + oldStatus + " sang " + trangThai);
        }

        chiTiet.setTrangThaiHoaDon(trangThai);
        chiTiet.setNgayCapNhat(new Date());

        // Xử lý logic theo trạng thái mới
        handleStatusChange(chiTiet, oldStatus, trangThai, ghiChu);

        ChiTietTraHang saved = chiTietTraHangRepository.save(chiTiet);
        return convertToDTO(saved);
    }

    @Override
    public void deleteChiTietTraHang(Integer id) {
        ChiTietTraHang chiTiet = chiTietTraHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết trả hàng với ID: " + id));

        // Chỉ cho phép xóa khi trạng thái là PENDING
        if (!"PENDING".equals(chiTiet.getTrangThaiHoaDon())) {
            throw new RuntimeException("Không thể xóa chi tiết trả hàng ở trạng thái: " + chiTiet.getTrangThaiHoaDon());
        }

        chiTietTraHangRepository.delete(chiTiet);
    }

    @Override
    public Integer getTotalReturnedQuantity(Integer chiTietSanPhamId) {
        try {
            // ✅ SỬA: Đảm bảo query này đúng logic
            Integer total = chiTietTraHangRepository.getTotalReturnedQuantityExcludeRejected(chiTietSanPhamId);

            System.out.println("DEBUG getTotalReturnedQuantity for product " + chiTietSanPhamId + ": " + total);

            return total != null ? total : 0;
        } catch (Exception e) {
            System.err.println("Lỗi getTotalReturnedQuantity: " + e.getMessage());
            e.printStackTrace(); // ✅ THÊM: In stack trace để debug
            return 0;
        }
    }

    @Override
    public Map<String, Object> getReturnStatistics(Integer hoaDonId) {
        List<ChiTietTraHangDTO> chiTietList = getChiTietTraHangByHoaDon(hoaDonId);

        if (chiTietList.isEmpty()) {
            return Map.of(
                    "soLoaiSanPhamTraHang", 0,
                    "tongSoLuongTraHang", 0,
                    "tongTienHoan", 0.0,
                    "soLuongChoXuLy", 0L,
                    "soLuongDaXuLy", 0L,
                    "soLuongTuChoi", 0L
            );
        }

        int tongSoLuong = chiTietList.stream().mapToInt(ChiTietTraHangDTO::getSoLuong).sum();

        double tongTienHoan = chiTietList.stream()
                .filter(ct -> "APPROVED".equals(ct.getTrangThaiHoaDon()))
                .mapToDouble(ct -> ct.getTienHoan() != null ? ct.getTienHoan().doubleValue() : 0)
                .sum();

        long soLuongChoXuLy = chiTietList.stream()
                .filter(ct -> "PENDING".equals(ct.getTrangThaiHoaDon()))
                .count();

        long soLuongDaXuLy = chiTietList.stream()
                .filter(ct -> "APPROVED".equals(ct.getTrangThaiHoaDon()))
                .count();

        long soLuongTuChoi = chiTietList.stream()
                .filter(ct -> "REJECTED".equals(ct.getTrangThaiHoaDon()))
                .count();

        return Map.of(
                "soLoaiSanPhamTraHang", chiTietList.size(),
                "tongSoLuongTraHang", tongSoLuong,
                "tongTienHoan", tongTienHoan,
                "soLuongChoXuLy", soLuongChoXuLy,
                "soLuongDaXuLy", soLuongDaXuLy,
                "soLuongTuChoi", soLuongTuChoi
        );
    }

    @Override
    public boolean canReturn(Integer chiTietSanPhamId, Integer soLuong) {
        if (chiTietSanPhamId == null || soLuong == null || soLuong <= 0) {
            System.out.println("❌ canReturn: Invalid input - chiTietSanPhamId=" + chiTietSanPhamId + ", soLuong=" + soLuong);
            return false;
        }

        try {
            // Lấy tổng số lượng đã mua của chi tiết sản phẩm này
            Integer tongSoLuongDaMua = hoaDonChiTietRepository.getTotalSoldQuantity(chiTietSanPhamId);
            if (tongSoLuongDaMua == null || tongSoLuongDaMua <= 0) {
                System.out.println("❌ canReturn: No purchase found for product " + chiTietSanPhamId);
                return false;
            }

            // Lấy tổng số lượng đã trả (chỉ tính những cái KHÔNG bị reject)
            Integer tongSoLuongDaTra = getTotalReturnedQuantity(chiTietSanPhamId);
            if (tongSoLuongDaTra == null) {
                tongSoLuongDaTra = 0;
            }

            // ✅ LOGIC CHO PHÉP TRẢ 100%
            int coTheTraThem = tongSoLuongDaMua - tongSoLuongDaTra;
            boolean result = soLuong <= coTheTraThem;

            System.out.println("=== CAN RETURN CHECK ===");
            System.out.println("Product ID: " + chiTietSanPhamId);
            System.out.println("Đã mua: " + tongSoLuongDaMua);
            System.out.println("Đã trả: " + tongSoLuongDaTra);
            System.out.println("Có thể trả thêm: " + coTheTraThem);
            System.out.println("Yêu cầu trả: " + soLuong);
            System.out.println("Kết quả: " + result);
            System.out.println("========================");

            return result;

        } catch (Exception e) {
            System.err.println("❌ Lỗi kiểm tra canReturn: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public ChiTietTraHangDTO approveReturn(Integer id, String ghiChu) {
        ChiTietTraHang chiTiet = chiTietTraHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết trả hàng với ID: " + id));

        if (!"PENDING".equals(chiTiet.getTrangThaiHoaDon())) {
            throw new RuntimeException("Chỉ có thể chấp nhận trả hàng ở trạng thái PENDING");
        }

        return updateTrangThai(id, "APPROVED", ghiChu);
    }

    @Override
    public List<ChiTietTraHangDTO> getChiTietTraHangByHoaDonAndStatus(Integer hoaDonId, String trangThai) {
        if (hoaDonId == null || trangThai == null || trangThai.trim().isEmpty()) {
            return List.of();
        }

        try {
            return chiTietTraHangRepository.findByHoaDonIdAndTrangThaiHoaDon(hoaDonId, trangThai).stream()
                    .map(this::convertToDTO)
                    .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao())) // ✅ SỬA
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public Integer getTotalReturnedQuantityByInvoice(Integer hoaDonId) {
        if (hoaDonId == null) {
            return 0;
        }

        try {
            Integer total = chiTietTraHangRepository.getTotalReturnedQuantityByInvoice(hoaDonId);
            return total != null ? total : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public Double getTotalReturnValueByInvoice(Integer hoaDonId) {
        if (hoaDonId == null) {
            return 0.0;
        }

        try {
            Double total = chiTietTraHangRepository.getTotalReturnValueByInvoice(hoaDonId);
            return total != null ? total : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public Boolean hasReturnsForInvoice(Integer hoaDonId) {
        if (hoaDonId == null) {
            return false;
        }

        try {
            return chiTietTraHangRepository.existsByHoaDonId(hoaDonId);
        } catch (Exception e) {
            return false;
        }
    }

    // =================== PRIVATE HELPER METHODS ===================

    private ChiTietTraHangDTO convertToDTO(ChiTietTraHang chiTiet) {
        ChiTietTraHangDTO dto = new ChiTietTraHangDTO();

        // Thông tin cơ bản
        dto.setId(chiTiet.getId());
        dto.setMaChiTietTraHang(chiTiet.getMaChiTietTraHang());
        dto.setSoLuong(chiTiet.getSoLuong());
        dto.setTrangThaiHoaDon(chiTiet.getTrangThaiHoaDon());
        dto.setNgayTao(chiTiet.getNgayTao());
        dto.setNgayTaoTraHang(chiTiet.getNgayTaoTraHang());
        dto.setNgayCapNhat(chiTiet.getNgayCapNhat());

        // ✅ THÊM: Lý do và ảnh
        dto.setLyDo(chiTiet.getLyDo());
        dto.setDuongDanAnh(chiTiet.getDuongDanAnh());

        // Thông tin hóa đơn
        if (chiTiet.getHoaDon() != null) {
            dto.setHoaDonId(chiTiet.getHoaDon().getId());
            dto.setMaHoaDon(chiTiet.getHoaDon().getMaHoaDon());
        }

        // Thông tin chi tiết sản phẩm
        if (chiTiet.getChiTietSanPham() != null) {
            ChiTietSanPham ctsp = chiTiet.getChiTietSanPham();
            dto.setChiTietSanPhamId(ctsp.getId());
            dto.setMaChiTiet(ctsp.getMaChiTiet());
            dto.setGiaGoc(ctsp.getGiaGoc());
            dto.setGiaBan(ctsp.getGiaGoc()); // Sử dụng giá gốc làm giá bán cho trả hàng
            dto.setHinhAnh(ctsp.getHinhAnh() != null ? ctsp.getHinhAnh().getDuongDan() : null);

            // Thông tin sản phẩm
            if (ctsp.getSanPham() != null) {
                SanPham sp = ctsp.getSanPham();
                dto.setTenSanPham(sp.getTenSanPham());
                dto.setMaSanPham(sp.getMaSanPham());
                dto.setThuongHieu(sp.getThuongHieu() != null ? sp.getThuongHieu().getTenThuongHieu() : "N/A");
                dto.setDanhMuc(sp.getDanhMuc() != null ? sp.getDanhMuc().getTenDanhMuc() : "N/A");
            } else {
                dto.setTenSanPham("Sản phẩm không xác định");
                dto.setMaSanPham("N/A");
                dto.setThuongHieu("N/A");
                dto.setDanhMuc("N/A");
            }

            // Thông tin màu sắc và kích thước
            dto.setMauSac(ctsp.getMauSac() != null ? ctsp.getMauSac().getTenMauSac() : "N/A");
            dto.setKichThuoc(ctsp.getKichCo() != null ? ctsp.getKichCo().getTenKichCo() : "N/A");
        } else {
            // Fallback nếu không có thông tin chi tiết sản phẩm
            dto.setTenSanPham("Sản phẩm không xác định");
            dto.setMaSanPham("N/A");
            dto.setMauSac("N/A");
            dto.setKichThuoc("N/A");
            dto.setThuongHieu("N/A");
            dto.setDanhMuc("N/A");
        }

        // Tính toán các giá trị
        dto.calculateValues();

        return dto;
    }

    private String generateMaChiTietTraHang() {
        // Format: TH + năm + tháng + ngày + số random
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        String date = sdf.format(new Date());
        int random = (int) (Math.random() * 9999) + 1;
        return String.format("TH%s%04d", date, random);
    }

    private boolean isValidStatusTransition(String fromStatus, String toStatus) {
        if (fromStatus == null || toStatus == null) {
            return false;
        }

        // Định nghĩa các chuyển đổi trạng thái hợp lệ
        Map<String, Set<String>> validTransitions = Map.of(
                "PENDING", Set.of("APPROVED", "REJECTED"),
                "APPROVED", Set.of(), // Không thể chuyển từ APPROVED
                "REJECTED", Set.of()  // Không thể chuyển từ REJECTED
        );

        return validTransitions.getOrDefault(fromStatus, Collections.emptySet()).contains(toStatus);
    }

    private void handleStatusChange(ChiTietTraHang chiTiet, String oldStatus, String newStatus, String ghiChu) {
        switch (newStatus) {
            case "APPROVED":
                handleApproveReturn(chiTiet, ghiChu);
                break;
            case "REJECTED":
                handleRejectReturn(chiTiet, ghiChu);
                break;
            default:
                // Không cần xử lý gì đặc biệt
                break;
        }
    }

    private void handleApproveReturn(ChiTietTraHang chiTiet, String ghiChu) {
        try {
            // Chỉ ghi log thông tin, không cập nhật kho
            // Logic cập nhật kho sẽ được xử lý riêng trong các API mới

            System.out.println("=== XỬ LÝ CHẤP NHẬN TRẢ HÀNG ===");
            System.out.println("Mã trả hàng: " + chiTiet.getMaChiTietTraHang());
            System.out.println("Số lượng trả: " + chiTiet.getSoLuong());
            System.out.println("Trạng thái: APPROVED");

            // Log thông tin chi tiết sản phẩm nếu có
            if (chiTiet.getChiTietSanPham() != null) {
                ChiTietSanPham ctsp = chiTiet.getChiTietSanPham();
                System.out.println("Sản phẩm: " + ctsp.getMaChiTiet());
                System.out.println("Số lượng tồn hiện tại: " + (ctsp.getSoLuong() != null ? ctsp.getSoLuong() : 0));

                // Log thông tin sản phẩm
                if (ctsp.getSanPham() != null) {
                    System.out.println("Tên sản phẩm: " + ctsp.getSanPham().getTenSanPham());
                    System.out.println("Tổng số lượng SP: " + (ctsp.getSanPham().getSoLuong() != null ? ctsp.getSanPham().getSoLuong() : 0));
                }
            }

            // Log ghi chú nếu có
            if (ghiChu != null && !ghiChu.trim().isEmpty()) {
                System.out.println("Ghi chú: " + ghiChu);
            }

            System.out.println("Lưu ý: Kho CHƯA được cập nhật. Sử dụng API riêng để hoàn kho nếu cần.");
            System.out.println("- Hoàn kho: /approve-with-inventory");
            System.out.println("- Không hoàn kho: /approve-no-inventory");
            System.out.println("================================");

        } catch (Exception e) {
            System.err.println("Lỗi xử lý chấp nhận trả hàng: " + e.getMessage());
            // Không throw exception vì đây chỉ là logging
        }
    }

    private void handleRejectReturn(ChiTietTraHang chiTiet, String lyDo) {
        // Log việc từ chối trả hàng
        System.out.println("Từ chối trả hàng " + chiTiet.getMaChiTietTraHang() +
                " với lý do: " + (lyDo != null ? lyDo : "Không có lý do"));

        // Có thể thêm logic gửi thông báo cho khách hàng ở đây
    }

    // Utility method để validate trạng thái
    private boolean isValidStatus(String status) {
        return Set.of("PENDING", "APPROVED", "REJECTED").contains(status);
    }
    @Override
    public List<ChiTietTraHangDTO> searchChiTietTraHang(String lyDo, String trangThai, Integer hoaDonId) {
        try {
            return chiTietTraHangRepository.searchChiTietTraHang(lyDo, trangThai, hoaDonId).stream()
                    .map(this::convertToDTO)
                    .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao())) // ✅ SỬA: dùng getNgayTao()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Lỗi tìm kiếm chi tiết trả hàng: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<ChiTietTraHangDTO> searchChiTietTraHangAdvanced(String lyDo, String trangThai, Integer hoaDonId,
                                                                Integer chiTietSanPhamId, Boolean hasImage) {
        try {
            // Tạm thời dùng method cơ bản, bỏ qua 2 tham số cuối
            return chiTietTraHangRepository.searchChiTietTraHang(lyDo, trangThai, hoaDonId).stream()
                    .map(this::convertToDTO)
                    .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao())) // ✅ SỬA
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Lỗi tìm kiếm nâng cao: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<ChiTietTraHangDTO> getChiTietTraHangByLyDo(String lyDo) {
        try {
            if (lyDo == null || lyDo.trim().isEmpty()) {
                return new ArrayList<>();
            }

            return chiTietTraHangRepository.findByLyDoContaining(lyDo).stream()
                    .map(this::convertToDTO)
                    .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao())) // ✅ SỬA
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Lỗi tìm kiếm theo lý do: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    @Override
    public List<ChiTietTraHangDTO> getChiTietTraHangWithImages() {
        try {
            return chiTietTraHangRepository.findAllWithImages().stream()
                    .map(this::convertToDTO)
                    .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao())) // ✅ SỬA
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Lỗi lấy danh sách có ảnh: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<ChiTietTraHangDTO> getChiTietTraHangWithoutImages() {
        try {
            return chiTietTraHangRepository.findAllWithoutImages().stream()
                    .map(this::convertToDTO)
                    .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao())) // ✅ SỬA
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Lỗi lấy danh sách không có ảnh: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Long> getReturnReasonStatistics() {
        try {
            List<Object[]> results = chiTietTraHangRepository.getReturnReasonStatistics();
            Map<String, Long> statistics = new LinkedHashMap<>();

            for (Object[] result : results) {
                String lyDo = (String) result[0];
                Long soLuong = ((Number) result[1]).longValue();
                statistics.put(lyDo, soLuong);
            }

            return statistics;
        } catch (Exception e) {
            System.err.println("Lỗi lấy thống kê lý do: " + e.getMessage());
            return new HashMap<>();
        }
    }

    @Override
    public Long countReturnsWithImages() {
        try {
            return chiTietTraHangRepository.countReturnsWithImages();
        } catch (Exception e) {
            System.err.println("Lỗi đếm trả hàng có ảnh: " + e.getMessage());
            return 0L;
        }
    }

    @Override
    public Long countByLyDo(String lyDo) {
        try {
            if (lyDo == null || lyDo.trim().isEmpty()) {
                return 0L;
            }
            return chiTietTraHangRepository.countByLyDo(lyDo);
        } catch (Exception e) {
            System.err.println("Lỗi đếm theo lý do: " + e.getMessage());
            return 0L;
        }
    }

    @Override
    public ChiTietTraHangDTO updateImage(Integer id, String duongDanAnh) {
        try {
            ChiTietTraHang chiTiet = chiTietTraHangRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết trả hàng với ID: " + id));

            // Chỉ cho phép cập nhật ảnh khi trạng thái là PENDING hoặc APPROVED
            if (!Set.of("PENDING", "APPROVED").contains(chiTiet.getTrangThaiHoaDon())) {
                throw new RuntimeException("Không thể cập nhật ảnh ở trạng thái: " + chiTiet.getTrangThaiHoaDon());
            }

            chiTiet.setDuongDanAnh(duongDanAnh);
            chiTiet.setNgayCapNhat(new Date());

            ChiTietTraHang saved = chiTietTraHangRepository.save(chiTiet);
            return convertToDTO(saved);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi cập nhật ảnh: " + e.getMessage());
        }
    }

    @Override
    public ChiTietTraHangDTO removeImage(Integer id) {
        try {
            ChiTietTraHang chiTiet = chiTietTraHangRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết trả hàng với ID: " + id));

            // Chỉ cho phép xóa ảnh khi trạng thái là PENDING
            if (!"PENDING".equals(chiTiet.getTrangThaiHoaDon())) {
                throw new RuntimeException("Không thể xóa ảnh ở trạng thái: " + chiTiet.getTrangThaiHoaDon());
            }

            chiTiet.setDuongDanAnh(null);
            chiTiet.setNgayCapNhat(new Date());

            ChiTietTraHang saved = chiTietTraHangRepository.save(chiTiet);
            return convertToDTO(saved);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xóa ảnh: " + e.getMessage());
        }
    }

    // ✅ CẬP NHẬT: Method rejectReturn để lưu lý do từ chối
    @Override
    public ChiTietTraHangDTO rejectReturn(Integer id, String lyDoTuChoi) {
        ChiTietTraHang chiTiet = chiTietTraHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết trả hàng với ID: " + id));

        if (!"PENDING".equals(chiTiet.getTrangThaiHoaDon())) {
            throw new RuntimeException("Chỉ có thể từ chối trả hàng ở trạng thái PENDING");
        }

        if (lyDoTuChoi == null || lyDoTuChoi.trim().isEmpty()) {
            throw new RuntimeException("Lý do từ chối không được để trống");
        }

        // Cập nhật trạng thái và lý do từ chối
        chiTiet.setTrangThaiHoaDon("REJECTED");
        // Có thể lưu lý do từ chối vào một trường riêng hoặc ghép với lý do hiện tại
        String lyDoMoi = chiTiet.getLyDo() + " [TỪ CHỐI: " + lyDoTuChoi + "]";
        chiTiet.setLyDo(lyDoMoi);
        chiTiet.setNgayCapNhat(new Date());

        ChiTietTraHang saved = chiTietTraHangRepository.save(chiTiet);
        return convertToDTO(saved);
    }

    // ✅ THÊM: Method helper để validate trạng thái có thể cập nhật
    private boolean canUpdateReturn(String trangThai) {
        return Set.of("PENDING").contains(trangThai);
    }

    // ✅ THÊM: Method helper để validate trạng thái có thể thêm ảnh
    private boolean canAddImage(String trangThai) {
        return Set.of("PENDING", "APPROVED").contains(trangThai);
    }

    // ✅ THÊM: Method helper để lấy các lý do trả hàng phổ biến
    public List<String> getCommonReturnReasons() {
        try {
            List<Object[]> results = chiTietTraHangRepository.getReturnReasonStatistics();
            return results.stream()
                    .limit(10) // Lấy 10 lý do phổ biến nhất
                    .map(result -> (String) result[0])
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Trả về danh sách lý do mặc định
            return Arrays.asList(
                    "Sản phẩm bị lỗi",
                    "Không đúng mô tả",
                    "Kích thước không phù hợp",
                    "Màu sắc không đúng",
                    "Chất lượng không tốt",
                    "Thay đổi ý kiến",
                    "Giao hàng muộn",
                    "Bao bì hư hỏng"
            );
        }
    }

    // ✅ THÊM: Method validate file ảnh từ service layer
    public boolean validateImageFile(String duongDanAnh) {
        if (duongDanAnh == null || duongDanAnh.trim().isEmpty()) {
            return false;
        }

        // Kiểm tra extension
        String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};
        String lowerPath = duongDanAnh.toLowerCase();

        return Arrays.stream(allowedExtensions)
                .anyMatch(lowerPath::endsWith);
    }

    // ✅ THÊM: Method tạo báo cáo thống kê trả hàng chi tiết
    public Map<String, Object> getDetailedReturnStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // Thống kê tổng quan
            Long totalReturns = chiTietTraHangRepository.count();
            Long returnsWithImages = countReturnsWithImages();
            Long returnsWithoutImages = totalReturns - returnsWithImages;

            // Thống kê theo trạng thái
            Long pendingReturns = chiTietTraHangRepository.countByTrangThaiHoaDon("PENDING");
            Long approvedReturns = chiTietTraHangRepository.countByTrangThaiHoaDon("APPROVED");
            Long rejectedReturns = chiTietTraHangRepository.countByTrangThaiHoaDon("REJECTED");

            // Thống kê theo lý do
            Map<String, Long> reasonStats = getReturnReasonStatistics();

            stats.put("tongSoTraHang", totalReturns);
            stats.put("coMinhChung", returnsWithImages);
            stats.put("khongCoMinhChung", returnsWithoutImages);
            stats.put("choXuLy", pendingReturns);
            stats.put("daChapNhan", approvedReturns);
            stats.put("daTuChoi", rejectedReturns);
            stats.put("thongKeLyDo", reasonStats);
            stats.put("tyLeCoAnh", totalReturns > 0 ? (returnsWithImages * 100.0 / totalReturns) : 0);
            stats.put("tyLeChapNhan", totalReturns > 0 ? (approvedReturns * 100.0 / totalReturns) : 0);

            return stats;
        } catch (Exception e) {
            System.err.println("Lỗi tạo báo cáo thống kê: " + e.getMessage());
            return new HashMap<>();
        }
    }
    private void logReturnAttempt(ChiTietTraHangDTO dto) {
        try {
            Integer totalSold = hoaDonChiTietRepository.getTotalSoldQuantity(dto.getChiTietSanPhamId());
            Integer totalReturned = getTotalReturnedQuantity(dto.getChiTietSanPhamId());

            System.out.println("=== RETURN ATTEMPT LOG ===");
            System.out.println("Product ID: " + dto.getChiTietSanPhamId());
            System.out.println("Requested quantity: " + dto.getSoLuong());
            System.out.println("Total sold: " + (totalSold != null ? totalSold : 0));
            System.out.println("Total returned: " + (totalReturned != null ? totalReturned : 0));
            System.out.println("Can return more: " + (totalSold != null && totalReturned != null ? (totalSold - totalReturned) : 0));
            System.out.println("Reason: " + dto.getLyDo());
            System.out.println("Has image: " + (dto.getDuongDanAnh() != null));
            System.out.println("==========================");
        } catch (Exception e) {
            System.err.println("Error logging return attempt: " + e.getMessage());
        }
    }
    @Override
    public ChiTietTraHangDTO approveReturnWithInventory(Integer id, String ghiChu) {
        ChiTietTraHang chiTiet = chiTietTraHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết trả hàng với ID: " + id));

        if (!"PENDING".equals(chiTiet.getTrangThaiHoaDon())) {
            throw new RuntimeException("Chỉ có thể chấp nhận trả hàng ở trạng thái PENDING");
        }

        // Cập nhật trạng thái
        chiTiet.setTrangThaiHoaDon("APPROVED");
        chiTiet.setNgayCapNhat(new Date());

        // Hoàn lại kho
        handleRestoreInventory(chiTiet, ghiChu);

        ChiTietTraHang saved = chiTietTraHangRepository.save(chiTiet);
        return convertToDTO(saved);
    }

    @Override
    public ChiTietTraHangDTO approveReturnNoInventory(Integer id, String ghiChu) {
        ChiTietTraHang chiTiet = chiTietTraHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết trả hàng với ID: " + id));

        if (!"PENDING".equals(chiTiet.getTrangThaiHoaDon())) {
            throw new RuntimeException("Chỉ có thể chấp nhận trả hàng ở trạng thái PENDING");
        }

        // Chỉ cập nhật trạng thái, không hoàn kho
        chiTiet.setTrangThaiHoaDon("APPROVED");
        chiTiet.setNgayCapNhat(new Date());

        System.out.println("Chấp nhận trả hàng " + chiTiet.getMaChiTietTraHang() +
                " KHÔNG hoàn kho theo yêu cầu");

        ChiTietTraHang saved = chiTietTraHangRepository.save(chiTiet);
        return convertToDTO(saved);
    }

    // Method helper để hoàn kho
    private void handleRestoreInventory(ChiTietTraHang chiTiet, String ghiChu) {
        try {
            ChiTietSanPham ctsp = chiTiet.getChiTietSanPham();
            if (ctsp != null) {
                // Hoàn lại số lượng vào chi tiết sản phẩm
                Integer currentQty = ctsp.getSoLuong() != null ? ctsp.getSoLuong() : 0;
                ctsp.setSoLuong(currentQty + chiTiet.getSoLuong());
                chiTietSanPhamRepository.save(ctsp);

                // Cập nhật tổng số lượng sản phẩm
                if (ctsp.getSanPham() != null) {
                    SanPham sanPham = ctsp.getSanPham();
                    Integer currentSpQty = sanPham.getSoLuong() != null ? sanPham.getSoLuong() : 0;
                    sanPham.setSoLuong(currentSpQty + chiTiet.getSoLuong());
                    // sanPhamRepository.save(sanPham); // Uncomment nếu có repository
                }

                System.out.println("Đã hoàn lại " + chiTiet.getSoLuong() +
                        " sản phẩm " + ctsp.getMaChiTiet() + " vào kho");
            }
        } catch (Exception e) {
            System.err.println("Lỗi hoàn kho: " + e.getMessage());
            throw new RuntimeException("Lỗi hoàn kho: " + e.getMessage());
        }
    }
}
