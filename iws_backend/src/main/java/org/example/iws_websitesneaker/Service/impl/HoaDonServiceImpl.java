package org.example.iws_websitesneaker.Service.impl;

import jakarta.persistence.EntityManager;
import org.example.iws_websitesneaker.Dto.*;
import org.example.iws_websitesneaker.Service.*;
import org.example.iws_websitesneaker.entity.*;
import org.example.iws_websitesneaker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class HoaDonServiceImpl implements HoaDonService {

    @Autowired private ChiTietTraHangService chiTietTraHangService;
    @Autowired private RepoHoaDon hoaDonRepository;
    @Autowired private RepoHoaDonChiTiet hoaDonChiTietRepository;
    @Autowired private RepoChiTietVoucher chiTietVoucherRepository;
    @Autowired private RepoLichSuHoaDon lichSuHoaDonRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private TaiKhoanService taiKhoanService;
    @Autowired private KhachHangService khachHangService;
    @Autowired
    private RepoVoucher voucherRepository;
    @Autowired
    private ChiTietVoucherService chiTietVoucherService;

    // Các trạng thái không thể thay đổi
    private static final Set<String> FINAL_STATUSES = Set.of("COMPLETED", "CANCELLED", "RETURNED");
    private static final Set<String> PROCESSING_STATUSES = Set.of("PENDING", "CONFIRMED", "SHIPPING");


    @Override
    public List<HoaDonDTO> getAllHoaDons() {
        try {
            return hoaDonRepository.findAllOrderByNgayTaoDesc().stream()
                    .map(this::convertToFullDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Fallback nếu query custom không hoạt động
            return hoaDonRepository.findAll().stream()
                    .sorted((h1, h2) -> h2.getNgayTao().compareTo(h1.getNgayTao()))
                    .map(this::convertToFullDTO)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<HoaDonDTO> getHoaDonByStatus(String trangThai) {
        try {
            return hoaDonRepository.findByTrangThaiHoaDonOrderByNgayTaoDesc(trangThai).stream()
                    .map(this::convertToFullDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return hoaDonRepository.findAll().stream()
                    .filter(h -> trangThai.equals(h.getTrangThaiHoaDon()))
                    .sorted((h1, h2) -> h2.getNgayTao().compareTo(h1.getNgayTao()))
                    .map(this::convertToFullDTO)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<HoaDonDTO> getPOSInvoices() {
        try {
            return hoaDonRepository.findPOSInvoices().stream()
                    .map(this::convertToFullDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return hoaDonRepository.findAll().stream()
                    .filter(h -> "OFFLINE".equals(h.getLoaiHoaDon()))
                    .sorted((h1, h2) -> h2.getNgayTao().compareTo(h1.getNgayTao()))
                    .map(this::convertToFullDTO)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<HoaDonDTO> getOnlineInvoices() {
        try {
            return hoaDonRepository.findOnlineInvoices().stream()
                    .map(this::convertToFullDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return hoaDonRepository.findAll().stream()
                    .filter(h -> "ONLINE".equals(h.getLoaiHoaDon()))
                    .sorted((h1, h2) -> h2.getNgayTao().compareTo(h1.getNgayTao()))
                    .map(this::convertToFullDTO)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public HoaDonDTO getHoaDonById(Integer id) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + id));
        return convertToFullDTO(hoaDon);
    }

    @Override
    public List<HoaDonDTO> searchInvoices(String keyword, String trangThai, String loaiHoaDon) {
        try {
            return hoaDonRepository.searchInvoices(keyword, trangThai, loaiHoaDon).stream()
                    .map(this::convertToFullDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Fallback tìm kiếm đơn giản
            return hoaDonRepository.findAll().stream()
                    .filter(h -> {
                        boolean keywordMatch = keyword == null || keyword.isEmpty() ||
                                h.getMaHoaDon().contains(keyword) ||
                                (h.getTenNguoiDung() != null && h.getTenNguoiDung().contains(keyword)) ||
                                (h.getSdt() != null && h.getSdt().contains(keyword)) ||
                                (h.getKhachHang() != null && h.getKhachHang().getHoTen().contains(keyword));

                        boolean statusMatch = trangThai == null || trangThai.isEmpty() ||
                                trangThai.equals(h.getTrangThaiHoaDon());

                        boolean typeMatch = loaiHoaDon == null || loaiHoaDon.isEmpty() ||
                                loaiHoaDon.equals(h.getLoaiHoaDon());

                        return keywordMatch && statusMatch && typeMatch;
                    })
                    .sorted((h1, h2) -> h2.getNgayTao().compareTo(h1.getNgayTao()))
                    .map(this::convertToFullDTO)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public HoaDonDTO updateStatus(Integer id, InvoiceStatusUpdateRequest request) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // Kiểm tra logic chuyển trạng thái
        validateStatusTransition(hoaDon.getTrangThaiHoaDon(), request.getTrangThai());

        String oldStatus = hoaDon.getTrangThaiHoaDon();
        hoaDon.setTrangThaiHoaDon(request.getTrangThai());
        hoaDon.setGhiChu(request.getGhiChu());
        hoaDon.setNgayCapNhat(new Date());

        // Cập nhật ngày theo trạng thái
        updateStatusDates(hoaDon, request.getTrangThai());

        HoaDon saved = hoaDonRepository.save(hoaDon);

        // Lưu lịch sử thay đổi
        saveStatusHistory(saved, oldStatus, request.getTrangThai(), request.getNhanVienId(), request.getGhiChu());

        return convertToFullDTO(saved);
    }

    @Override
    public HoaDonDTO confirmInvoice(Integer id, Integer nhanVienId) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        if (!canConfirm(hoaDon.getTrangThaiHoaDon())) {
            throw new RuntimeException("Không thể xác nhận hóa đơn ở trạng thái: " + hoaDon.getTrangThaiHoaDon());
        }

        String oldStatus = hoaDon.getTrangThaiHoaDon();
        hoaDon.setTrangThaiHoaDon("CONFIRMED");
        hoaDon.setNgayXacNhan(new Date());
        hoaDon.setNgayCapNhat(new Date());

        HoaDon saved = hoaDonRepository.save(hoaDon);
        saveStatusHistory(saved, oldStatus, "CONFIRMED", nhanVienId, "Xác nhận đơn hàng");

        return convertToFullDTO(saved);
    }

    @Override
    public HoaDonDTO cancelInvoice(Integer id, String lyDo, Integer nhanVienId) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        if (!canCancel(hoaDon.getTrangThaiHoaDon())) {
            throw new RuntimeException("Không thể hủy hóa đơn ở trạng thái: " + hoaDon.getTrangThaiHoaDon());
        }

        String oldStatus = hoaDon.getTrangThaiHoaDon();
        hoaDon.setTrangThaiHoaDon("CANCELLED");
        hoaDon.setGhiChu(lyDo);
        hoaDon.setNgayCapNhat(new Date());

        // Hoàn lại số lượng sản phẩm
        restoreProductQuantity(hoaDon.getId());

        HoaDon saved = hoaDonRepository.save(hoaDon);
        saveStatusHistory(saved, oldStatus, "CANCELLED", nhanVienId, lyDo);

        return convertToFullDTO(saved);
    }

    @Override
    public HoaDonDTO completeInvoice(Integer id, Integer nhanVienId) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        if (!canComplete(hoaDon.getTrangThaiHoaDon())) {
            throw new RuntimeException("Không thể hoàn thành hóa đơn ở trạng thái: " + hoaDon.getTrangThaiHoaDon());
        }

        String oldStatus = hoaDon.getTrangThaiHoaDon();
        hoaDon.setTrangThaiHoaDon("COMPLETED");
        hoaDon.setNgayHoanThanh(new Date());
        hoaDon.setNgayCapNhat(new Date());

        HoaDon saved = hoaDonRepository.save(hoaDon);
        saveStatusHistory(saved, oldStatus, "COMPLETED", nhanVienId, "Hoàn thành đơn hàng");

        return convertToFullDTO(saved);
    }

    // Thay thế method getInvoiceStatistics() trong HoaDonServiceImpl

    @Override
    public Map<String, Object> getInvoiceStatistics() {
        try {
            Long totalInvoices = hoaDonRepository.count();
            Long completedInvoices = hoaDonRepository.countCompletedInvoices();
            Long cancelledInvoices = hoaDonRepository.countByTrangThaiHoaDon("CANCELLED");
            Long pendingInvoices = hoaDonRepository.countByTrangThaiHoaDon("PENDING");
            Long confirmedInvoices = hoaDonRepository.countByTrangThaiHoaDon("CONFIRMED");
            Long shippingInvoices = hoaDonRepository.countByTrangThaiHoaDon("SHIPPING");

            // Tổng doanh thu từ đơn hàng hoàn thành
            Double totalRevenue = hoaDonRepository.sumTotalAmountByStatus("COMPLETED");

            // Doanh thu tháng này
            Calendar cal = Calendar.getInstance();
            int currentYear = cal.get(Calendar.YEAR);
            int currentMonth = cal.get(Calendar.MONTH) + 1;
            Double monthlyRevenue = hoaDonRepository.sumMonthlyRevenue(currentYear, currentMonth);

            // ✅ SỬA: Doanh thu hôm nay sử dụng startOfDay và endOfDay
            Double dailyRevenue = getDailyRevenueToday();

            return Map.of(
                    "tongDonHang", totalInvoices,
                    "donHangHoanThanh", completedInvoices,
                    "donHangHuy", cancelledInvoices,
                    "donHangDangXuLy", pendingInvoices + confirmedInvoices + shippingInvoices,
                    "tyLeHoanThanh", totalInvoices > 0 ? (completedInvoices * 100.0 / totalInvoices) : 0,
                    "tyLeHuy", totalInvoices > 0 ? (cancelledInvoices * 100.0 / totalInvoices) : 0,
                    "tongDoanhThu", totalRevenue != null ? totalRevenue : 0.0,
                    "doanhThuThangNay", monthlyRevenue != null ? monthlyRevenue : 0.0,
                    "doanhThuHomNay", dailyRevenue != null ? dailyRevenue : 0.0
            );
        } catch (Exception e) {
            // Log lỗi để debug
            System.err.println("Lỗi khi tính thống kê: " + e.getMessage());
            e.printStackTrace();

            // Fallback statistics
            List<HoaDon> allInvoices = hoaDonRepository.findAll();
            long total = allInvoices.size();
            long completed = allInvoices.stream().mapToLong(h -> "COMPLETED".equals(h.getTrangThaiHoaDon()) ? 1 : 0).sum();
            long cancelled = allInvoices.stream().mapToLong(h -> "CANCELLED".equals(h.getTrangThaiHoaDon()) ? 1 : 0).sum();
            long pending = allInvoices.stream().mapToLong(h -> "PENDING".equals(h.getTrangThaiHoaDon()) ? 1 : 0).sum();
            long confirmed = allInvoices.stream().mapToLong(h -> "CONFIRMED".equals(h.getTrangThaiHoaDon()) ? 1 : 0).sum();
            long shipping = allInvoices.stream().mapToLong(h -> "SHIPPING".equals(h.getTrangThaiHoaDon()) ? 1 : 0).sum();

            return Map.of(
                    "tongDonHang", total,
                    "donHangHoanThanh", completed,
                    "donHangHuy", cancelled,
                    "donHangDangXuLy", pending + confirmed + shipping,
                    "tyLeHoanThanh", total > 0 ? (completed * 100.0 / total) : 0,
                    "tyLeHuy", total > 0 ? (cancelled * 100.0 / total) : 0,
                    "tongDoanhThu", 0.0,
                    "doanhThuThangNay", 0.0,
                    "doanhThuHomNay", 0.0
            );
        }
    }

    // ✅ THÊM method helper để tính doanh thu hôm nay
    private Double getDailyRevenueToday() {
        try {
            Calendar cal = Calendar.getInstance();

            // Đặt về đầu ngày (00:00:00)
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date startOfDay = cal.getTime();

            // Đặt về cuối ngày (23:59:59) hoặc đầu ngày hôm sau
            cal.add(Calendar.DAY_OF_MONTH, 1);
            Date endOfDay = cal.getTime();

            return hoaDonRepository.sumDailyRevenue(startOfDay, endOfDay);
        } catch (Exception e) {
            System.err.println("Lỗi tính doanh thu hôm nay: " + e.getMessage());
            return 0.0;
        }
    }

    // ✅ THÊM method tiện ích để lấy doanh thu theo ngày cụ thể
    public Double getDailyRevenueByDate(Date date) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            // Đặt về đầu ngày
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date startOfDay = cal.getTime();

            // Cuối ngày
            cal.add(Calendar.DAY_OF_MONTH, 1);
            Date endOfDay = cal.getTime();

            return hoaDonRepository.sumDailyRevenue(startOfDay, endOfDay);
        } catch (Exception e) {
            System.err.println("Lỗi tính doanh thu ngày " + date + ": " + e.getMessage());
            return 0.0;
        }
    }

    @Override
    public List<LichSuHoaDonDTO> getInvoiceHistory(Integer hoaDonId) {
        return lichSuHoaDonRepository.findByHoaDonIdOrderByNgayTaoDesc(hoaDonId).stream()
                .map(this::convertLichSuToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<HoaDon> findByEmailAndMaHoaDon(String email, String maHoaDon) {
        return hoaDonRepository.findByEmailAndMaHoaDon(email, maHoaDon);
    }

    private String normalizeInvoiceStatus(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) {
            return "";
        }

        String normalized = Normalizer.normalize(trangThai.trim().toUpperCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('\u0110', 'D')
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");

        return switch (normalized) {
            case "DA_THANH_TOAN", "HOAN_THANH" -> "COMPLETED";
            default -> normalized;
        };
    }

    @Override
    public OrderTrackingResponse getTrackingResponse(String email, String orderCode) {
        HoaDon order = hoaDonRepository.findByEmailAndMaHoaDon(email, orderCode)
                .orElse(null);

        if (order == null) return null;

        List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByHoaDonId(order.getId());

        return convertToTrackingResponse(order, chiTietList);
    }
    @Override
    public HoaDon save(HoaDon hoaDon) {
        try {
            // Validation cơ bản
            if (hoaDon.getTenNguoiDung() == null || hoaDon.getTenNguoiDung().trim().isEmpty()) {
                throw new IllegalArgumentException("Tên người dùng không được để trống");
            }

            if (hoaDon.getEmail() == null || hoaDon.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Email không được để trống");
            }

            if (hoaDon.getSdt() == null || hoaDon.getSdt().trim().isEmpty()) {
                throw new IllegalArgumentException("Số điện thoại không được để trống");
            }

            if (hoaDon.getDiaChi() == null || hoaDon.getDiaChi().trim().isEmpty()) {
                throw new IllegalArgumentException("Địa chỉ không được để trống");
            }

            // Nếu là tạo mới (id = null), set ngày tạo
            if (hoaDon.getId() == null) {
                hoaDon.setNgayTao(new Date());
            }

            // Lưu vào database
            return hoaDonRepository.save(hoaDon);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu hóa đơn: " + e.getMessage(), e);
        }
    }
    // =================== PRIVATE HELPER METHODS ===================

    private OrderTrackingResponse convertToTrackingResponse(HoaDon order, List<HoaDonChiTiet> chiTietList) {
        OrderTrackingResponse response = new OrderTrackingResponse();

        // Basic info
        response.setOrderId(order.getId());
        response.setOrderCode(order.getMaHoaDon());
        response.setStatus(order.getTrangThaiHoaDon());
        response.setCreatedDate(order.getNgayTao());

        // Customer info
        response.setCustomerName(order.getTenNguoiDung());
        response.setEmail(order.getEmail());
        response.setPhone(order.getSdt());
        response.setAddress(order.getDiaChi());

        // Payment info
        response.setPaymentMethod(order.getPhuongThucThanhToan());
        response.setShippingFee(order.getPhiVanChuyen() != null ? order.getPhiVanChuyen().doubleValue() : 0.0);
        response.setTotalAmount(order.getTongThanhToan() != null ? order.getTongThanhToan().doubleValue() : 0.0);
        response.setSubtotal(order.getTongTien() != null ? order.getTongTien().doubleValue() : 0.0);

        // Items
        List<OrderItemResponse> items = new ArrayList<>();
        for (HoaDonChiTiet chiTiet : chiTietList) {
            OrderItemResponse item = new OrderItemResponse();
            item.setId(chiTiet.getId());
            item.setQuantity(chiTiet.getSoLuong());

            double unitPrice = chiTiet.getGia() != null ? chiTiet.getGia() : 0.0;
            item.setUnitPrice(unitPrice);
            item.setTotalPrice(unitPrice * (chiTiet.getSoLuong() != null ? chiTiet.getSoLuong() : 0));

            if (chiTiet.getChiTietSanPham() != null) {
                var ctsp = chiTiet.getChiTietSanPham();

                if (ctsp.getSanPham() != null) {
                    item.setName(ctsp.getSanPham().getTenSanPham());
                    item.setCode(ctsp.getSanPham().getMaSanPham());
                }

                // Ưu tiên lấy ảnh từ ChiTietSanPham.hinhAnh
                if (ctsp.getHinhAnh() != null) {
                    item.setImage(buildImageUrl(ctsp.getHinhAnh().getDuongDan()));
                } else {
                    item.setImage("/placeholder-shoe.png");
                }

                if (ctsp.getKichCo() != null) {
                    item.setSize(ctsp.getKichCo().getTenKichCo());
                }
                if (ctsp.getMauSac() != null) {
                    item.setColor(ctsp.getMauSac().getTenMauSac());
                }
            }

            items.add(item);
        }
        response.setItems(items);

        return response;
    }

    private String buildImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return "/placeholder-shoe.png";
        }
        if (imagePath.startsWith("http")) {
            return imagePath;
        }
        // DB lưu kiểu: /hinh-anh/images/xxx.png
        return "http://localhost:8080" + (imagePath.startsWith("/") ? "" : "/") + imagePath;
    }


    private HoaDonDTO convertToFullDTO(HoaDon hoaDon) {
        HoaDonDTO dto = new HoaDonDTO();

        // Thông tin cơ bản (existing code)...
        dto.setId(hoaDon.getId());
        dto.setMaHoaDon(hoaDon.getMaHoaDon());
        dto.setTenKhachHang(hoaDon.getKhachHang() != null ?
                hoaDon.getKhachHang().getHoTen() : hoaDon.getTenNguoiDung());
        dto.setSdt(hoaDon.getSdt());
        dto.setEmail(hoaDon.getEmail());
        dto.setDiaChi(hoaDon.getDiaChi());
        dto.setTrangThaiHoaDon(hoaDon.getTrangThaiHoaDon());
        dto.setLoaiHoaDon(hoaDon.getLoaiHoaDon());
        dto.setPhuongThucThanhToan(hoaDon.getPhuongThucThanhToan());
        dto.setGhiChu(hoaDon.getGhiChu());

        // Thông tin thời gian (existing code)...
        dto.setNgayTao(hoaDon.getNgayTao());
        dto.setNgayXacNhan(hoaDon.getNgayXacNhan());
        dto.setNgayGiaoHang(hoaDon.getNgayGiaoHang());
        dto.setNgayHoanThanh(hoaDon.getNgayHoanThanh());
        dto.setNgayCapNhat(hoaDon.getNgayCapNhat());

        // Lấy chi tiết sản phẩm (existing code)...
        List<HoaDonChiTietDTO> chiTietList = getChiTietList(hoaDon.getId());
        dto.setChiTietList(chiTietList);

        // ✅ THÊM: Lấy và set thông tin voucher chi tiết
        loadDetailedVoucherInfo(dto, hoaDon);

        // Tính toán tiền (existing code)...
        calculateInvoiceAmounts(dto, hoaDon, chiTietList);

        // Load thông tin khác (existing code)...
        loadReturnDetails(dto, hoaDon);

        // Thống kê (existing code)...
        dto.setSoLuongSanPham(chiTietList.size());
        dto.setTongSoLuong(chiTietList.stream().mapToInt(HoaDonChiTietDTO::getSoLuong).sum());

        // Trạng thái có thể thay đổi (existing code)...
        setStatusChangeability(dto, hoaDon.getTrangThaiHoaDon());

        return dto;
    }

    private void loadDetailedVoucherInfo(HoaDonDTO dto, HoaDon hoaDon) {
        try {
            // Lấy danh sách chi tiết voucher
            List<ChiTietVoucherDTO> chiTietVoucherList = chiTietVoucherService.findByHoaDonId(hoaDon.getId());
            dto.setChiTietVoucherList(chiTietVoucherList);

            if (!chiTietVoucherList.isEmpty()) {
                // Tính tổng tiết kiệm từ tất cả voucher
                BigDecimal tongTienVoucherChiTiet = chiTietVoucherList.stream()
                        .map(ChiTietVoucherDTO::getSoTienGiam)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                dto.setTongTienVoucherChiTiet(tongTienVoucherChiTiet);

                // Set thông tin voucher chính (voucher đầu tiên hoặc có giá trị lớn nhất)
                ChiTietVoucherDTO voucherChinh = chiTietVoucherList.stream()
                        .max(Comparator.comparing(v -> v.getSoTienGiam() != null ? v.getSoTienGiam() : BigDecimal.ZERO))
                        .orElse(chiTietVoucherList.get(0));

                dto.setTenVoucher(voucherChinh.getTenVoucher());
                dto.setMaVoucherDaApDung(voucherChinh.getMaVoucher());
                dto.setLoaiVoucher(voucherChinh.getLoaiGiamGia());
                dto.setGiaTriVoucher(BigDecimal.valueOf(voucherChinh.getGiaTriGiam() != null ? voucherChinh.getGiaTriGiam() : 0));

                // Thống kê voucher
                dto.setSoLuongVoucherDaApDung(chiTietVoucherList.size());

                // Phân loại voucher theo loại
                Map<String, Integer> thongKeVoucherTheoLoai = chiTietVoucherList.stream()
                        .collect(Collectors.groupingBy(
                                ChiTietVoucherDTO::getLoaiGiamGia,
                                Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                        ));
                dto.setThongKeVoucherTheoLoai(thongKeVoucherTheoLoai);

                // Tính % tiết kiệm từ voucher
                if (dto.getTongTien() != null && dto.getTongTien().compareTo(BigDecimal.ZERO) > 0) {
                    double phanTramTietKiemVoucher = tongTienVoucherChiTiet
                            .divide(dto.getTongTien(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();
                    dto.setPhanTramTietKiemVoucher(Math.round(phanTramTietKiemVoucher * 100.0) / 100.0);
                } else {
                    dto.setPhanTramTietKiemVoucher(0.0);
                }

            } else {
                // Không có voucher - set giá trị mặc định
                dto.setChiTietVoucherList(new ArrayList<>());
                dto.setTongTienVoucherChiTiet(BigDecimal.ZERO);
                dto.setSoLuongVoucherDaApDung(0);
                dto.setPhanTramTietKiemVoucher(0.0);
                dto.setThongKeVoucherTheoLoai(new HashMap<>());
            }

        } catch (Exception e) {
            // Log lỗi nhưng không làm fail
            System.err.println("Lỗi load thông tin voucher chi tiết cho hóa đơn " + hoaDon.getId() + ": " + e.getMessage());

            // Set giá trị mặc định
            dto.setChiTietVoucherList(new ArrayList<>());
            dto.setTongTienVoucherChiTiet(BigDecimal.ZERO);
            dto.setSoLuongVoucherDaApDung(0);
            dto.setPhanTramTietKiemVoucher(0.0);
            dto.setThongKeVoucherTheoLoai(new HashMap<>());
        }
    }

    private List<HoaDonChiTietDTO> getChiTietList(Integer hoaDonId) {
        try {
            return hoaDonChiTietRepository.findByHoaDonIdWithFullInfo(hoaDonId).stream()
                    .map(this::convertChiTietToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Fallback without full info
            return hoaDonChiTietRepository.findByHoaDonId(hoaDonId).stream()
                    .map(this::convertChiTietToDTO)
                    .collect(Collectors.toList());
        }
    }

    private HoaDonChiTietDTO convertChiTietToDTO(HoaDonChiTiet chiTiet) {
        HoaDonChiTietDTO dto = new HoaDonChiTietDTO();

        dto.setId(chiTiet.getId());
        dto.setHoaDonId(chiTiet.getHoaDon().getId());
        dto.setSoLuong(chiTiet.getSoLuong());
        dto.setGiaBan(chiTiet.getGia());
        dto.setTrangThai(chiTiet.getTrangThaiHoaDon());

        if (chiTiet.getChiTietSanPham() != null) {
            ChiTietSanPham ctsp = chiTiet.getChiTietSanPham();
            dto.setChiTietSanPhamId(ctsp.getId());
            dto.setMaChiTiet(ctsp.getMaChiTiet());
            dto.setGiaGoc(ctsp.getGiaGoc());
            dto.setHinhAnh(ctsp.getHinhAnh() != null ? ctsp.getHinhAnh().getDuongDan() : null);

            if (ctsp.getSanPham() != null) {
                dto.setTenSanPham(ctsp.getSanPham().getTenSanPham());
                dto.setMaSanPham(ctsp.getSanPham().getMaSanPham());
                dto.setThuongHieu(ctsp.getSanPham().getThuongHieu() != null ?
                        ctsp.getSanPham().getThuongHieu().getTenThuongHieu() : "N/A");
                dto.setDanhMuc(ctsp.getSanPham().getDanhMuc() != null ?
                        ctsp.getSanPham().getDanhMuc().getTenDanhMuc() : "N/A");
            }

            dto.setMauSac(ctsp.getMauSac() != null ? ctsp.getMauSac().getTenMauSac() : "N/A");
            dto.setKichThuoc(ctsp.getKichCo() != null ? ctsp.getKichCo().getTenKichCo() : "N/A");
        }

        // Tính toán các giá trị
        dto.calculateValues();

        return dto;
    }

    private void calculateInvoiceAmounts(HoaDonDTO dto, HoaDon hoaDon, List<HoaDonChiTietDTO> chiTietList) {
        // Tính tổng tiền gốc (trước khi có bất kỳ giảm giá nào)
        BigDecimal tongTienGoc = chiTietList.stream()
                .map(item -> BigDecimal.valueOf(item.getGiaGoc() != null ? item.getGiaGoc() : item.getGiaBan())
                        .multiply(BigDecimal.valueOf(item.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tổng tiền hiện tại (đã tính khuyến mãi sản phẩm)
        BigDecimal tongTienHienTai = chiTietList.stream()
                .map(HoaDonChiTietDTO::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tổng tiền giảm từ khuyến mãi sản phẩm
        BigDecimal tongTienGiamGia = tongTienGoc.subtract(tongTienHienTai);

        // Set phí vận chuyển
        BigDecimal phiVanChuyen = hoaDon.getPhiVanChuyen() != null ? hoaDon.getPhiVanChuyen() : BigDecimal.ZERO;

        dto.setTongTienGoc(tongTienGoc);
        dto.setTongTienGiamGia(tongTienGiamGia);
        dto.setPhiVanChuyen(phiVanChuyen);

        // Tính tổng tiền = tiền sản phẩm + phí vận chuyển
        BigDecimal tongTien = tongTienHienTai.add(phiVanChuyen);
        dto.setTongTien(hoaDon.getTongTien() != null ? hoaDon.getTongTien() : tongTien);

        // TÍNH TỔNG THANH TOÁN (Tổng tiền - Voucher - Điểm)
        BigDecimal tongThanhToan = dto.getTongTien();

        // Trừ voucher
        if (dto.getTongTienVoucherChiTiet() != null && dto.getTongTienVoucherChiTiet().compareTo(BigDecimal.ZERO) > 0) {
            tongThanhToan = tongThanhToan.subtract(dto.getTongTienVoucherChiTiet());
        }

        // Trừ điểm
        BigDecimal giaTriDiem = BigDecimal.ZERO;
        if (hoaDon.getGiaTriDiem() != null && hoaDon.getGiaTriDiem() > 0) {
            giaTriDiem = BigDecimal.valueOf(hoaDon.getGiaTriDiem());
            tongThanhToan = tongThanhToan.subtract(giaTriDiem);
        }

        // Đảm bảo tổng thanh toán không âm
        if (tongThanhToan.compareTo(BigDecimal.ZERO) < 0) {
            tongThanhToan = BigDecimal.ZERO;
        }

        // Set giá trị cuối cùng
        dto.setTongThanhToan(hoaDon.getTongThanhToan() != null ? hoaDon.getTongThanhToan() : tongThanhToan);
        dto.setTienDiem(giaTriDiem);
        dto.setGiaTriDiem(hoaDon.getGiaTriDiem());
        dto.setDiemSuDung(hoaDon.getDiemSuDung() != null ? hoaDon.getDiemSuDung() : 0);
    }

    private void loadVoucherAndPointInfo(HoaDonDTO dto, HoaDon hoaDon) {
        // Thông tin voucher
        chiTietVoucherRepository.findByHoaDonId(hoaDon.getId()).stream().findFirst()
                .ifPresent(ctv -> {
                    dto.setTenVoucher(ctv.getVoucher().getTenVoucher());
                    dto.setLoaiVoucher(ctv.getVoucher().getLoaiGiamGia());
                    dto.setGiaTriVoucher(BigDecimal.valueOf(ctv.getVoucher().getGiaTriGiam()));
                    dto.setTongTienVoucher(ctv.getThanhTien());
                });

        // Thông tin điểm
        dto.setDiemSuDung(hoaDon.getDiemSuDung() != null ? hoaDon.getDiemSuDung() : 0);
        dto.setTienDiem(hoaDon.getGiaTriDiem() != null ?
                BigDecimal.valueOf(hoaDon.getGiaTriDiem()) : BigDecimal.ZERO);
        dto.setGiaTriDiem(hoaDon.getGiaTriDiem());
    }

    private void setStatusChangeability(HoaDonDTO dto, String currentStatus) {
        dto.setCoTheHuy(canCancel(currentStatus));
        dto.setCoTheXacNhan(canConfirm(currentStatus));
        dto.setCoTheGiaoHang(canShip(currentStatus));
        dto.setCoTheHoanThanh(canComplete(currentStatus));
    }

    private void validateStatusTransition(String fromStatus, String toStatus) {
        if (FINAL_STATUSES.contains(fromStatus)) {
            throw new RuntimeException("Không thể thay đổi trạng thái từ " + fromStatus);
        }

        // Kiểm tra logic chuyển trạng thái hợp lệ
        if (!isValidTransition(fromStatus, toStatus)) {
            throw new RuntimeException("Không thể chuyển từ trạng thái " + fromStatus + " sang " + toStatus);
        }
    }

    private boolean isValidTransition(String from, String to) {
        Map<String, Set<String>> validTransitions = Map.of(
                "PENDING", Set.of("CONFIRMED", "CANCELLED"),
                "CONFIRMED", Set.of("SHIPPING", "CANCELLED"),
                "SHIPPING", Set.of("DELIVERED", "CANCELLED"),
                "DELIVERED", Set.of("COMPLETED", "RETURNED")
        );

        return validTransitions.getOrDefault(from, Collections.emptySet()).contains(to);
    }

    private void updateStatusDates(HoaDon hoaDon, String newStatus) {
        Date now = new Date();
        switch (newStatus) {
            case "CONFIRMED":
                if (hoaDon.getNgayXacNhan() == null) hoaDon.setNgayXacNhan(now);
                break;
            case "SHIPPING":
                if (hoaDon.getNgayGiaoHang() == null) hoaDon.setNgayGiaoHang(now);
                break;
            case "COMPLETED":
                if (hoaDon.getNgayHoanThanh() == null) hoaDon.setNgayHoanThanh(now);
                break;
        }
    }

    private void saveStatusHistory(HoaDon hoaDon, String oldStatus, String newStatus,
                                   Integer nhanVienId, String ghiChu) {
        try {
            LichSuHoaDon lichSu = new LichSuHoaDon();
            lichSu.setHoaDon(hoaDon);

            if (nhanVienId != null) {
                NhanVien nhanVien = entityManager.getReference(NhanVien.class, nhanVienId);
                lichSu.setNhanVien(nhanVien);
            }

            lichSu.setMoTaHanhDong(String.format("Chuyển từ %s sang %s: %s",
                    oldStatus, newStatus, ghiChu != null ? ghiChu : ""));
            lichSu.setTrangThaiHoaDon(newStatus);
            lichSu.setNgayTao(new Date());
            lichSu.setNgayCapNhat(new Date());

            lichSuHoaDonRepository.save(lichSu);
        } catch (Exception e) {
            // Log error nhưng không làm fail transaction
            System.err.println("Lỗi lưu lịch sử: " + e.getMessage());
        }
    }

    private void restoreProductQuantity(Integer hoaDonId) {
        try {
            List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
            for (HoaDonChiTiet chiTiet : chiTietList) {
                ChiTietSanPham ctsp = chiTiet.getChiTietSanPham();
                if (ctsp != null) {
                    ctsp.setSoLuong(ctsp.getSoLuong() + chiTiet.getSoLuong());
                    // Cập nhật tổng số lượng sản phẩm
                    SanPham sanPham = ctsp.getSanPham();
                    if (sanPham != null) {
                        sanPham.setSoLuong(sanPham.getSoLuong() + chiTiet.getSoLuong());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi hoàn lại số lượng: " + e.getMessage());
        }
    }

    private LichSuHoaDonDTO convertLichSuToDTO(LichSuHoaDon lichSu) {
        LichSuHoaDonDTO dto = new LichSuHoaDonDTO();
        dto.setId(lichSu.getId());
        dto.setMoTaHanhDong(lichSu.getMoTaHanhDong());
        dto.setTrangThaiHoaDon(lichSu.getTrangThaiHoaDon());
        dto.setTenNhanVien(lichSu.getNhanVien() != null ? lichSu.getNhanVien().getHoTen() : "Hệ thống");
        dto.setNhanVienId(lichSu.getNhanVien() != null ? lichSu.getNhanVien().getId() : null);
        dto.setNgayTao(lichSu.getNgayTao());
        return dto;
    }

    // Status checking methods
    private boolean canCancel(String status) {
        return Set.of("PENDING", "CONFIRMED").contains(status);
    }

    private boolean canConfirm(String status) {
        return "PENDING".equals(status);
    }

    private boolean canShip(String status) {
        return "CONFIRMED".equals(status);
    }

    private boolean canComplete(String status) {
        return Set.of("DELIVERED", "SHIPPING").contains(status);
    }
    @Autowired
    private RepoKhachHang khachHangRepository;

    @Autowired
    private RepoChiTietSanPham chiTietSanPhamRepository;


    @Override
    public HoaDonDTO updateStatus(Integer id, StatusUpdateRequest request) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        hoaDon.setTrangThaiHoaDon(request.getTrangThai());
        hoaDon.setGhiChu(request.getGhiChu());
        hoaDon.setNgayCapNhat(new Date());

        HoaDon saved = hoaDonRepository.save(hoaDon);
        return convertToDTO(saved);
    }

    @Override
    public HoaDonDTO confirmInvoice(Integer id) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        hoaDon.setTrangThaiHoaDon("CONFIRMED");
        hoaDon.setNgayXacNhan(new Date());
        hoaDon.setNgayCapNhat(new Date());

        HoaDon saved = hoaDonRepository.save(hoaDon);
        return convertToDTO(saved);
    }

    @Override
    public HoaDonDTO completeInvoice(Integer id) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        hoaDon.setTrangThaiHoaDon("COMPLETED");
        hoaDon.setNgayHoanThanh(new Date());
        hoaDon.setNgayCapNhat(new Date());

        HoaDon saved = hoaDonRepository.save(hoaDon);
        return convertToDTO(saved);
    }

    @Override
    public HoaDonDTO cancelInvoice(Integer id, String lyDo) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        hoaDon.setTrangThaiHoaDon("CANCELLED");
        hoaDon.setGhiChu(lyDo);
        hoaDon.setNgayCapNhat(new Date());

        HoaDon saved = hoaDonRepository.save(hoaDon);
        return convertToDTO(saved);
    }

    @Override
    public List<HoaDonDTO> getHoaDonsByKhachHangId(Integer khachHangId) {
        return hoaDonRepository.findByKhachHangId(khachHangId).stream()
                .map(this::convertToDTO)
                .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao())) // Sắp xếp mới nhất trước
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HoaDonDTO createHoaDonFromCheckout(CreateHoaDonRequest request) {
        try {
            System.out.println("=== CREATE HOA DON FROM CHECKOUT ===");
            System.out.println("KhachHangId: " + request.getKhachHangId());
            System.out.println("TenNguoiDung: " + request.getTenNguoiDung());
            System.out.println("Email: " + request.getEmail());

            boolean isGuestOrder = (request.getKhachHangId() == null);
            System.out.println("Order type: " + (isGuestOrder ? "GUEST" : "USER"));

            // 1. Validate request data
            validateCreateHoaDonRequest(request);

            // 2. Kiểm tra tồn kho TRƯỚC KHI tạo đơn
            validateProductStock(request.getChiTietSanPham());

            // 3. Validate và lấy voucher nếu có
            Voucher voucher = null;
            if (request.getVoucherId() != null) {
                voucher = validateAndGetVoucher(request.getVoucherId(), request.getTongTien());
            }

            HoaDon hoaDon = new HoaDon();

            // 4. Set khách hàng - Xử lý cả user và guest
            if (!isGuestOrder) {
                KhachHang khachHang = khachHangRepository.findById(request.getKhachHangId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
                hoaDon.setKhachHang(khachHang);
                System.out.println("Order for logged-in user: " + khachHang.getHoTen());
            } else {
                hoaDon.setKhachHang(null); // Guest order - id_khach_hang = NULL
                System.out.println("Guest order for: " + request.getTenNguoiDung());
            }

            // 5. Generate mã hóa đơn (phân biệt user vs guest)
            String maHoaDon = request.getMaHoaDon();
            if (maHoaDon == null || maHoaDon.isEmpty()) {
                maHoaDon = generateMaHoaDon(!isGuestOrder);
            }
            hoaDon.setMaHoaDon(maHoaDon);

            // 6. Set thông tin cơ bản theo database columns
            hoaDon.setTenNguoiDung(request.getTenNguoiDung());
            hoaDon.setEmail(request.getEmail());
            hoaDon.setSdt(request.getSdt());
            hoaDon.setDiaChi(request.getDiaChi());
            hoaDon.setGhiChu(request.getGhiChu());
            hoaDon.setPhuongThucThanhToan(request.getPhuongThucThanhToan());
            hoaDon.setLoaiHoaDon(request.getLoaiHoaDon());
            hoaDon.setTrangThaiHoaDon(request.getTrangThaiHoaDon());

            // Set đầy đủ thông tin tiền
            hoaDon.setTongTien(request.getTongTien());
            hoaDon.setPhiVanChuyen(request.getPhiVanChuyen() != null ? request.getPhiVanChuyen() : BigDecimal.ZERO);
            hoaDon.setTongThanhToan(request.getTongThanhToan());
            hoaDon.setDiemSuDung(request.getDiemSuDung() != null ? request.getDiemSuDung() : 0);
            hoaDon.setGiaTriDiem(request.getGiaTriDiem() != null ? request.getGiaTriDiem() : 0.0);

            // Set ngày
            Date now = new Date();
            hoaDon.setNgayTao(now);
            hoaDon.setNgayCapNhat(now);
            // Các ngày khác để NULL: ngay_xac_nhan, ngay_giao_hang, etc.

            // 9. Lưu hóa đơn
            HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
            System.out.println("Hóa đơn saved with ID: " + savedHoaDon.getId());

            // 10. Tạo chi tiết hóa đơn và trừ tồn kho
            if (request.getChiTietSanPham() != null && !request.getChiTietSanPham().isEmpty()) {
                createHoaDonChiTiet(savedHoaDon, request.getChiTietSanPham(), now);
            }

            // 11. Tạo chi_tiet_voucher nếu có voucher (thay vì set trực tiếp vào hoa_don)
            if (voucher != null) {
                createChiTietVoucher(savedHoaDon, voucher, request);
                updateVoucherQuantity(voucher);
            }

            // 12. Lưu lịch sử
            saveInitialStatusHistory(savedHoaDon, isGuestOrder ? "Guest" : "User");

            System.out.println("Order created successfully: " + savedHoaDon.getMaHoaDon());
            return convertToDTO(savedHoaDon);
        } catch (Exception e) {
            System.err.println("Error creating order: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể tạo đơn hàng: " + e.getMessage());
        }
    }

    private void validateCreateHoaDonRequest(CreateHoaDonRequest request) {
        if (request.getTenNguoiDung() == null || request.getTenNguoiDung().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên người dùng không được để trống");
        }


        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống");
        }

        if (request.getSdt() == null || request.getSdt().trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống");
        }

        if (request.getDiaChi() == null || request.getDiaChi().trim().isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ không được để trống");
        }

        if (request.getChiTietSanPham() == null || request.getChiTietSanPham().isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng trống");
        }
    }

    // Validate product stock
    private void validateProductStock(List<ChiTietHoaDonRequest> chiTietList) {
        for (ChiTietHoaDonRequest item : chiTietList) {
            ChiTietSanPham ctsp = chiTietSanPhamRepository.findById(item.getIdChiTietSanPham())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại: " + item.getIdChiTietSanPham()));

            if (ctsp.getSoLuong() < item.getSoLuong()) {
                String productName = ctsp.getSanPham() != null ? ctsp.getSanPham().getTenSanPham() : "Sản phẩm";
                throw new RuntimeException(productName + " chỉ còn " + ctsp.getSoLuong() + " sản phẩm trong kho");
            }
        }
    }

    private Voucher validateAndGetVoucher(Integer voucherId, BigDecimal tongTien) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher"));

        Date now = new Date();

        // Kiểm tra thời gian hiệu lực
        if (voucher.getNgayBatDau().after(now)) {
            throw new RuntimeException("Voucher chưa có hiệu lực");
        }
        if (voucher.getNgayKetThuc().before(now)) {
            throw new RuntimeException("Voucher đã hết hạn");
        }

        // Kiểm tra trạng thái (1 = active theo database)
        if (voucher.getTrangThai() != 1) {
            throw new RuntimeException("Voucher không khả dụng");
        }

        // Kiểm tra số lượng
        if (voucher.getSoLuong() <= 0) {
            throw new RuntimeException("Voucher đã hết lượt sử dụng");
        }

        // Kiểm tra giá trị tối thiểu (FLOAT in database)
        if (tongTien.doubleValue() < voucher.getGiaTriGiamToiThieu()) {
            throw new RuntimeException("Đơn hàng chưa đạt giá trị tối thiểu: " + voucher.getGiaTriGiamToiThieu());
        }

        System.out.println("Voucher validation passed: " + voucher.getMaVoucher());
        return voucher;
    }

    // Helper method: Tạo chi_tiet_voucher record
    // Simplified createChiTietVoucher method to match your actual entity

    private void createChiTietVoucher(HoaDon hoaDon, Voucher voucher, CreateHoaDonRequest request) {
        try {
            ChiTietVoucher chiTietVoucher = new ChiTietVoucher();

            // Generate mã chi tiết voucher
            String maChiTietVoucher = generateMaChiTietVoucher();
            chiTietVoucher.setMaChiTietVoucher(maChiTietVoucher);

            // Liên kết với hóa đơn và voucher
            chiTietVoucher.setHoaDon(hoaDon);
            chiTietVoucher.setVoucher(voucher);

            // Lưu thông tin voucher tại thời điểm áp dụng
            chiTietVoucher.setMaVoucher(voucher.getMaVoucher());
            chiTietVoucher.setTenVoucher(voucher.getTenVoucher());
            chiTietVoucher.setLoaiGiamGia(voucher.getLoaiGiamGia());
            chiTietVoucher.setGiaTriGiam(voucher.getGiaTriGiam());
            chiTietVoucher.setGiaTriGiamToiDa(voucher.getGiaTriGiamToiDa());
            chiTietVoucher.setGiaTriGiamToiThieu(voucher.getGiaTriGiamToiThieu());

            // Thông tin tính toán
            BigDecimal giaTriDonHang = request.getTongTien();
            BigDecimal soTienGiam = BigDecimal.valueOf(request.getGiaTriDiem() != null ? request.getGiaTriDiem() : 0);
            BigDecimal thanhTien = request.getTongThanhToan();

            chiTietVoucher.setGiaTriDonHang(giaTriDonHang);
            chiTietVoucher.setSoTienGiam(soTienGiam);
            chiTietVoucher.setThanhTien(thanhTien);

            // Thời gian
            Date now = new Date();
            chiTietVoucher.setNgayApDung(now);
            chiTietVoucher.setNgayTao(now);
            chiTietVoucher.setNgayCapNhat(now);

            // Lưu vào database
            chiTietVoucherRepository.save(chiTietVoucher);

            System.out.println("Chi tiết voucher đã được tạo với mã: " + maChiTietVoucher);

        } catch (Exception e) {
            System.err.println("Lỗi khi tạo chi tiết voucher: " + e.getMessage());
            throw new RuntimeException("Không thể tạo chi tiết voucher");
        }
    }

    private String generateMaChiTietVoucher() {
        return "CTV" + System.currentTimeMillis();
    }

    // Helper method: Update voucher quantity
    private void updateVoucherQuantity(Voucher voucher) {
        try {
            voucher.setSoLuong(voucher.getSoLuong() - 1);
            voucher.setNgayCapNhat(new Date());
            voucherRepository.save(voucher);
            System.out.println("Updated voucher quantity: " + voucher.getMaVoucher() + " -> " + voucher.getSoLuong());
        } catch (Exception e) {
            System.err.println("Error updating voucher: " + e.getMessage());
            throw new RuntimeException("Không thể cập nhật voucher");
        }
    }

    // Helper method: Generate mã hóa đơn
    private String generateMaHoaDon(boolean isUserOrder) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        String date = sdf.format(new Date());
        int random = (int) (Math.random() * 9999) + 1;

        String prefix = isUserOrder ? "HD" : "GU"; // HD for user, GU for guest
        return String.format("%s%s%04d", prefix, date, random);
    }

    private void createHoaDonChiTiet(HoaDon hoaDon, List<ChiTietHoaDonRequest> chiTietList, Date now) {
        for (ChiTietHoaDonRequest chiTiet : chiTietList) {
            // Tạo chi tiết hóa đơn
            HoaDonChiTiet hdct = new HoaDonChiTiet();
            hdct.setHoaDon(hoaDon);

            ChiTietSanPham ctsp = chiTietSanPhamRepository.findById(chiTiet.getIdChiTietSanPham())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
            hdct.setChiTietSanPham(ctsp);
            hdct.setGia(chiTiet.getGiaBan());
            hdct.setSoLuong(chiTiet.getSoLuong());
            hdct.setTrangThaiHoaDon(hoaDon.getTrangThaiHoaDon());
            hdct.setNgayTao(now);
            hdct.setNgayCapNhat(now);

            hoaDonChiTietRepository.save(hdct);

            // Trừ tồn kho
            updateProductStock(ctsp, chiTiet.getSoLuong());

            System.out.println("✅ Created detail for product: " + ctsp.getMaChiTiet() +
                    ", quantity: " + chiTiet.getSoLuong());
        }
    }

    private void updateVoucherUsage(Voucher voucher) {
        try {
            if (voucher.getSoLuong() > 0) {
                voucher.setSoLuong(voucher.getSoLuong() - 1);
                voucherRepository.save(voucher);
                System.out.println("✅ Updated voucher usage: " + voucher.getMaVoucher());
            }
        } catch (Exception e) {
            System.err.println("⚠️ Warning: Could not update voucher usage: " + e.getMessage());
        }
    }

    private void saveInitialStatusHistory(HoaDon hoaDon, String orderType) {
        try {
            String description = "Tạo đơn hàng " + orderType + " - " + hoaDon.getTrangThaiHoaDon();
            saveStatusHistory(hoaDon, null, hoaDon.getTrangThaiHoaDon(), null, description);
        } catch (Exception e) {
            System.err.println("Warning: Could not save initial status history: " + e.getMessage());
        }
    }

    private HoaDonDTO convertToDTO(HoaDon hoaDon) {
        HoaDonDTO dto = new HoaDonDTO();

        // Thông tin cơ bản
        dto.setId(hoaDon.getId());
        dto.setMaHoaDon(hoaDon.getMaHoaDon());
        dto.setTenKhachHang(hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getHoTen() : hoaDon.getTenNguoiDung());
        dto.setSdt(hoaDon.getSdt());
        dto.setEmail(hoaDon.getEmail());
        dto.setDiaChi(hoaDon.getDiaChi());
        dto.setTrangThaiHoaDon(hoaDon.getTrangThaiHoaDon());
        dto.setLoaiHoaDon(hoaDon.getLoaiHoaDon());
        dto.setPhuongThucThanhToan(hoaDon.getPhuongThucThanhToan());
        dto.setGhiChu(hoaDon.getGhiChu());

        // Thông tin thời gian
        dto.setNgayTao(hoaDon.getNgayTao());
        dto.setNgayXacNhan(hoaDon.getNgayXacNhan());
        dto.setNgayGiaoHang(hoaDon.getNgayGiaoHang());
        dto.setNgayHoanThanh(hoaDon.getNgayHoanThanh());
        dto.setNgayCapNhat(hoaDon.getNgayCapNhat());

        // Thông tin liên kết
        dto.setTenNhanVien(hoaDon.getNhanVien() != null ? hoaDon.getNhanVien().getHoTen() : null);
        dto.setKhachHangId(hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getId() : null);
        dto.setNhanVienId(hoaDon.getNhanVien() != null ? hoaDon.getNhanVien().getId() : null);

        // Thông tin tiền cơ bản từ entity
        dto.setTongTien(hoaDon.getTongTien());
        dto.setPhiVanChuyen(hoaDon.getPhiVanChuyen() != null ? hoaDon.getPhiVanChuyen() : BigDecimal.ZERO);
        dto.setTongThanhToan(hoaDon.getTongThanhToan());
        dto.setDiemSuDung(hoaDon.getDiemSuDung() != null ? hoaDon.getDiemSuDung() : 0);
        dto.setGiaTriDiem(hoaDon.getGiaTriDiem());
        dto.setTienDiem(hoaDon.getGiaTriDiem() != null ? BigDecimal.valueOf(hoaDon.getGiaTriDiem()) : BigDecimal.ZERO);

        // Lấy chi tiết để có đầy đủ thông tin (optional - chỉ khi cần)
        try {
            List<HoaDonChiTietDTO> chiTietList = getChiTietList(hoaDon.getId());
            if (chiTietList != null && !chiTietList.isEmpty()) {
                dto.setChiTietList(chiTietList);

                // Load voucher info
                loadDetailedVoucherInfo(dto, hoaDon);

                // Tính toán lại đầy đủ
                calculateInvoiceAmounts(dto, hoaDon, chiTietList);

                // Thống kê
                dto.setSoLuongSanPham(chiTietList.size());
                dto.setTongSoLuong(chiTietList.stream().mapToInt(HoaDonChiTietDTO::getSoLuong).sum());
            }
        } catch (Exception e) {
            // Log nhưng không fail - dùng thông tin cơ bản
            System.err.println("Không thể load chi tiết cho hóa đơn " + hoaDon.getId() + ": " + e.getMessage());
        }

        return dto;
    }

    private void updateProductStock(ChiTietSanPham ctsp, Integer quantity) {
        Integer currentStock = ctsp.getSoLuong();
        if (currentStock < quantity) {
            String productName = ctsp.getSanPham() != null ? ctsp.getSanPham().getTenSanPham() : ctsp.getMaChiTiet();
            throw new RuntimeException("Sản phẩm " + productName + " không đủ số lượng trong kho");
        }

        // Trừ tồn kho chi tiết sản phẩm
        ctsp.setSoLuong(currentStock - quantity);
        chiTietSanPhamRepository.save(ctsp);

        // Cập nhật tổng số lượng sản phẩm nếu có
        if (ctsp.getSanPham() != null) {
            SanPham sanPham = ctsp.getSanPham();
            Integer currentTotalStock = sanPham.getSoLuong();
            if (currentTotalStock != null && currentTotalStock >= quantity) {
                sanPham.setSoLuong(currentTotalStock - quantity);
                // Note: Cần inject SanPhamRepository nếu muốn save
            }
        }

        System.out.println("✅ Updated stock for " + ctsp.getMaChiTiet() +
                ": " + currentStock + " -> " + ctsp.getSoLuong());
    }
    // Method mới để load thông tin trả hàng
    private void loadReturnDetails(HoaDonDTO dto, HoaDon hoaDon) {
        try {
            // Lấy danh sách chi tiết trả hàng
            List<ChiTietTraHangDTO> chiTietTraHangList = chiTietTraHangService.getChiTietTraHangByHoaDon(hoaDon.getId());
            dto.setChiTietTraHangList(chiTietTraHangList);

            // Tính thống kê trả hàng
            if (!chiTietTraHangList.isEmpty()) {
                Map<String, Object> returnStats = chiTietTraHangService.getReturnStatistics(hoaDon.getId());

                dto.setSoLoaiSanPhamTraHang((Integer) returnStats.get("soLoaiSanPhamTraHang"));
                dto.setTongSoLuongTraHang((Integer) returnStats.get("tongSoLuongTraHang"));
                dto.setTongTienTraHang(BigDecimal.valueOf((Double) returnStats.get("tongTienHoan")));
                dto.setSoLuongTraHangChoXuLy(((Long) returnStats.get("soLuongChoXuLy")).intValue());
                dto.setSoLuongTraHangDaXuLy(((Long) returnStats.get("soLuongDaXuLy")).intValue());
                dto.setSoLuongTraHangTuChoi(((Long) returnStats.get("soLuongTuChoi")).intValue());
            } else {
                // Khởi tạo giá trị mặc định khi không có trả hàng
                dto.setSoLoaiSanPhamTraHang(0);
                dto.setTongSoLuongTraHang(0);
                dto.setTongTienTraHang(BigDecimal.ZERO);
                dto.setSoLuongTraHangChoXuLy(0);
                dto.setSoLuongTraHangDaXuLy(0);
                dto.setSoLuongTraHangTuChoi(0);
            }

            // Xác định có thể trả hàng không
            dto.setCoTheTraHang(canReturnInvoice(hoaDon.getTrangThaiHoaDon()));

        } catch (Exception e) {
            // Log lỗi nhưng không làm fail
            System.err.println("Lỗi load thông tin trả hàng cho hóa đơn " + hoaDon.getId() + ": " + e.getMessage());

            // Set giá trị mặc định
            dto.setChiTietTraHangList(new ArrayList<>());
            dto.setSoLoaiSanPhamTraHang(0);
            dto.setTongSoLuongTraHang(0);
            dto.setTongTienTraHang(BigDecimal.ZERO);
            dto.setSoLuongTraHangChoXuLy(0);
            dto.setSoLuongTraHangDaXuLy(0);
            dto.setSoLuongTraHangTuChoi(0);
            dto.setCoTheTraHang(false);
        }
    }

    // Method kiểm tra có thể trả hàng không dựa trên trạng thái hóa đơn
    private boolean canReturnInvoice(String trangThai) {
        // Chỉ cho phép trả hàng khi hóa đơn đã hoàn thành
        return "COMPLETED".equals(normalizeInvoiceStatus(trangThai));
    }
    @Override
    public HoaDonDTO cancelInvoiceByCustomer(Integer id, String lyDo, String customerEmail)
            throws IllegalAccessException, IllegalStateException, NoSuchElementException {

        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy hóa đơn"));

        // Quyền sở hữu: KhachHangId hoặc email đơn
        boolean hasAccess = false;

        if (hoaDon.getKhachHang() != null) {
            Optional<TaiKhoan> tkOpt = taiKhoanService.findByEmail(customerEmail);
            if (tkOpt.isPresent()) {
                KhachHang kh = khachHangService.findByTaiKhoanId(tkOpt.get().getId());
                if (kh != null && kh.getId().equals(hoaDon.getKhachHang().getId())) {
                    hasAccess = true;
                }
            }
        }

        if (!hasAccess && hoaDon.getEmail() != null && hoaDon.getEmail().equals(customerEmail)) {
            hasAccess = true;
        }

        if (!hasAccess) {
            throw new IllegalAccessException("Không có quyền hủy đơn hàng này");
        }

        // Trạng thái cho phép hủy
        String st = hoaDon.getTrangThaiHoaDon();
        if (!Set.of("CHO_XAC_NHAN", "PENDING").contains(st)) {
            throw new IllegalStateException("Không thể hủy đơn hàng ở trạng thái này");
        }

        hoaDon.setTrangThaiHoaDon("CANCELLED");
        hoaDon.setGhiChu(lyDo);
        hoaDon.setNgayCapNhat(new Date());

        HoaDon saved = hoaDonRepository.save(hoaDon);
        return convertToDTO(saved);
    }
}

