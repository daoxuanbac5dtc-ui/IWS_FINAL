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

    // CÃ¡c tráº¡ng thÃ¡i khÃ´ng thá»ƒ thay Ä‘á»•i
    private static final Set<String> FINAL_STATUSES = Set.of("COMPLETED", "CANCELLED", "RETURNED");
    private static final Set<String> PROCESSING_STATUSES = Set.of("PENDING", "CONFIRMED", "SHIPPING");


    @Override
    public List<HoaDonDTO> getAllHoaDons() {
        try {
            return hoaDonRepository.findAllOrderByNgayTaoDesc().stream()
                    .map(this::convertToFullDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Fallback náº¿u query custom khÃ´ng hoáº¡t Ä‘á»™ng
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
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n vá»›i ID: " + id));
        return convertToFullDTO(hoaDon);
    }

    @Override
    public List<HoaDonDTO> searchInvoices(String keyword, String trangThai, String loaiHoaDon) {
        try {
            return hoaDonRepository.searchInvoices(keyword, trangThai, loaiHoaDon).stream()
                    .map(this::convertToFullDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Fallback tÃ¬m kiáº¿m Ä‘Æ¡n giáº£n
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
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));

        // Kiá»ƒm tra logic chuyá»ƒn tráº¡ng thÃ¡i
        validateStatusTransition(hoaDon.getTrangThaiHoaDon(), request.getTrangThai());

        String oldStatus = hoaDon.getTrangThaiHoaDon();
        hoaDon.setTrangThaiHoaDon(request.getTrangThai());
        hoaDon.setGhiChu(request.getGhiChu());
        hoaDon.setNgayCapNhat(new Date());

        // Cáº­p nháº­t ngÃ y theo tráº¡ng thÃ¡i
        updateStatusDates(hoaDon, request.getTrangThai());

        HoaDon saved = hoaDonRepository.save(hoaDon);

        // LÆ°u lá»‹ch sá»­ thay Ä‘á»•i
        saveStatusHistory(saved, oldStatus, request.getTrangThai(), request.getNhanVienId(), request.getGhiChu());

        return convertToFullDTO(saved);
    }

    @Override
    public HoaDonDTO confirmInvoice(Integer id, Integer nhanVienId) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));

        if (!canConfirm(hoaDon.getTrangThaiHoaDon())) {
            throw new RuntimeException("KhÃ´ng thá»ƒ xÃ¡c nháº­n hÃ³a Ä‘Æ¡n á»Ÿ tráº¡ng thÃ¡i: " + hoaDon.getTrangThaiHoaDon());
        }

        String oldStatus = hoaDon.getTrangThaiHoaDon();
        hoaDon.setTrangThaiHoaDon("CONFIRMED");
        hoaDon.setNgayXacNhan(new Date());
        hoaDon.setNgayCapNhat(new Date());

        HoaDon saved = hoaDonRepository.save(hoaDon);
        saveStatusHistory(saved, oldStatus, "CONFIRMED", nhanVienId, "XÃ¡c nháº­n Ä‘Æ¡n hÃ ng");

        return convertToFullDTO(saved);
    }

    @Override
    public HoaDonDTO cancelInvoice(Integer id, String lyDo, Integer nhanVienId) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));

        if (!canCancel(hoaDon.getTrangThaiHoaDon())) {
            throw new RuntimeException("KhÃ´ng thá»ƒ há»§y hÃ³a Ä‘Æ¡n á»Ÿ tráº¡ng thÃ¡i: " + hoaDon.getTrangThaiHoaDon());
        }

        String oldStatus = hoaDon.getTrangThaiHoaDon();
        hoaDon.setTrangThaiHoaDon("CANCELLED");
        hoaDon.setGhiChu(lyDo);
        hoaDon.setNgayCapNhat(new Date());

        // HoÃ n láº¡i sá»‘ lÆ°á»£ng sáº£n pháº©m
        restoreProductQuantity(hoaDon.getId());

        HoaDon saved = hoaDonRepository.save(hoaDon);
        saveStatusHistory(saved, oldStatus, "CANCELLED", nhanVienId, lyDo);

        return convertToFullDTO(saved);
    }

    @Override
    public HoaDonDTO completeInvoice(Integer id, Integer nhanVienId) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));

        if (!canComplete(hoaDon.getTrangThaiHoaDon())) {
            throw new RuntimeException("KhÃ´ng thá»ƒ hoÃ n thÃ nh hÃ³a Ä‘Æ¡n á»Ÿ tráº¡ng thÃ¡i: " + hoaDon.getTrangThaiHoaDon());
        }

        String oldStatus = hoaDon.getTrangThaiHoaDon();
        hoaDon.setTrangThaiHoaDon("COMPLETED");
        hoaDon.setNgayHoanThanh(new Date());
        hoaDon.setNgayCapNhat(new Date());

        HoaDon saved = hoaDonRepository.save(hoaDon);
        saveStatusHistory(saved, oldStatus, "COMPLETED", nhanVienId, "HoÃ n thÃ nh Ä‘Æ¡n hÃ ng");

        return convertToFullDTO(saved);
    }

    // Thay tháº¿ method getInvoiceStatistics() trong HoaDonServiceImpl

    @Override
    public Map<String, Object> getInvoiceStatistics() {
        try {
            Long totalInvoices = hoaDonRepository.count();
            Long completedInvoices = hoaDonRepository.countByTrangThaiHoaDon("COMPLETED");
            Long cancelledInvoices = hoaDonRepository.countByTrangThaiHoaDon("CANCELLED");
            Long pendingInvoices = hoaDonRepository.countByTrangThaiHoaDon("PENDING");
            Long confirmedInvoices = hoaDonRepository.countByTrangThaiHoaDon("CONFIRMED");
            Long shippingInvoices = hoaDonRepository.countByTrangThaiHoaDon("SHIPPING");

            // Tá»•ng doanh thu tá»« Ä‘Æ¡n hÃ ng hoÃ n thÃ nh
            Double totalRevenue = hoaDonRepository.sumTotalAmountByStatus("COMPLETED");

            // Doanh thu thÃ¡ng nÃ y
            Calendar cal = Calendar.getInstance();
            int currentYear = cal.get(Calendar.YEAR);
            int currentMonth = cal.get(Calendar.MONTH) + 1;
            Double monthlyRevenue = hoaDonRepository.sumMonthlyRevenue(currentYear, currentMonth);

            // âœ… Sá»¬A: Doanh thu hÃ´m nay sá»­ dá»¥ng startOfDay vÃ  endOfDay
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
            // Log lá»—i Ä‘á»ƒ debug
            System.err.println("Lá»—i khi tÃ­nh thá»‘ng kÃª: " + e.getMessage());
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

    // âœ… THÃŠM method helper Ä‘á»ƒ tÃ­nh doanh thu hÃ´m nay
    private Double getDailyRevenueToday() {
        try {
            Calendar cal = Calendar.getInstance();

            // Äáº·t vá» Ä‘áº§u ngÃ y (00:00:00)
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date startOfDay = cal.getTime();

            // Äáº·t vá» cuá»‘i ngÃ y (23:59:59) hoáº·c Ä‘áº§u ngÃ y hÃ´m sau
            cal.add(Calendar.DAY_OF_MONTH, 1);
            Date endOfDay = cal.getTime();

            return hoaDonRepository.sumDailyRevenue(startOfDay, endOfDay);
        } catch (Exception e) {
            System.err.println("Lá»—i tÃ­nh doanh thu hÃ´m nay: " + e.getMessage());
            return 0.0;
        }
    }

    // âœ… THÃŠM method tiá»‡n Ã­ch Ä‘á»ƒ láº¥y doanh thu theo ngÃ y cá»¥ thá»ƒ
    public Double getDailyRevenueByDate(Date date) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            // Äáº·t vá» Ä‘áº§u ngÃ y
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date startOfDay = cal.getTime();

            // Cuá»‘i ngÃ y
            cal.add(Calendar.DAY_OF_MONTH, 1);
            Date endOfDay = cal.getTime();

            return hoaDonRepository.sumDailyRevenue(startOfDay, endOfDay);
        } catch (Exception e) {
            System.err.println("Lá»—i tÃ­nh doanh thu ngÃ y " + date + ": " + e.getMessage());
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
            // Validation cÆ¡ báº£n
            if (hoaDon.getTenNguoiDung() == null || hoaDon.getTenNguoiDung().trim().isEmpty()) {
                throw new IllegalArgumentException("TÃªn ngÆ°á»i dÃ¹ng khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
            }

            if (hoaDon.getEmail() == null || hoaDon.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Email khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
            }

            if (hoaDon.getSdt() == null || hoaDon.getSdt().trim().isEmpty()) {
                throw new IllegalArgumentException("Sá»‘ Ä‘iá»‡n thoáº¡i khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
            }

            if (hoaDon.getDiaChi() == null || hoaDon.getDiaChi().trim().isEmpty()) {
                throw new IllegalArgumentException("Äá»‹a chá»‰ khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
            }

            // Náº¿u lÃ  táº¡o má»›i (id = null), set ngÃ y táº¡o
            if (hoaDon.getId() == null) {
                hoaDon.setNgayTao(new Date());
            }

            // LÆ°u vÃ o database
            return hoaDonRepository.save(hoaDon);

        } catch (Exception e) {
            throw new RuntimeException("Lá»—i khi lÆ°u hÃ³a Ä‘Æ¡n: " + e.getMessage(), e);
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

                // Æ¯u tiÃªn láº¥y áº£nh tá»« ChiTietSanPham.hinhAnh
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
        // DB lÆ°u kiá»ƒu: /hinh-anh/images/xxx.png
        return "http://localhost:8080" + (imagePath.startsWith("/") ? "" : "/") + imagePath;
    }


    private HoaDonDTO convertToFullDTO(HoaDon hoaDon) {
        HoaDonDTO dto = new HoaDonDTO();

        // ThÃ´ng tin cÆ¡ báº£n (existing code)...
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

        // ThÃ´ng tin thá»i gian (existing code)...
        dto.setNgayTao(hoaDon.getNgayTao());
        dto.setNgayXacNhan(hoaDon.getNgayXacNhan());
        dto.setNgayGiaoHang(hoaDon.getNgayGiaoHang());
        dto.setNgayHoanThanh(hoaDon.getNgayHoanThanh());
        dto.setNgayCapNhat(hoaDon.getNgayCapNhat());

        // Láº¥y chi tiáº¿t sáº£n pháº©m (existing code)...
        List<HoaDonChiTietDTO> chiTietList = getChiTietList(hoaDon.getId());
        dto.setChiTietList(chiTietList);

        // âœ… THÃŠM: Láº¥y vÃ  set thÃ´ng tin voucher chi tiáº¿t
        loadDetailedVoucherInfo(dto, hoaDon);

        // TÃ­nh toÃ¡n tiá»n (existing code)...
        calculateInvoiceAmounts(dto, hoaDon, chiTietList);

        // Load thÃ´ng tin khÃ¡c (existing code)...
        loadReturnDetails(dto, hoaDon);

        // Thá»‘ng kÃª (existing code)...
        dto.setSoLuongSanPham(chiTietList.size());
        dto.setTongSoLuong(chiTietList.stream().mapToInt(HoaDonChiTietDTO::getSoLuong).sum());

        // Tráº¡ng thÃ¡i cÃ³ thá»ƒ thay Ä‘á»•i (existing code)...
        setStatusChangeability(dto, hoaDon.getTrangThaiHoaDon());

        return dto;
    }

    private void loadDetailedVoucherInfo(HoaDonDTO dto, HoaDon hoaDon) {
        try {
            // Láº¥y danh sÃ¡ch chi tiáº¿t voucher
            List<ChiTietVoucherDTO> chiTietVoucherList = chiTietVoucherService.findByHoaDonId(hoaDon.getId());
            dto.setChiTietVoucherList(chiTietVoucherList);

            if (!chiTietVoucherList.isEmpty()) {
                // TÃ­nh tá»•ng tiáº¿t kiá»‡m tá»« táº¥t cáº£ voucher
                BigDecimal tongTienVoucherChiTiet = chiTietVoucherList.stream()
                        .map(ChiTietVoucherDTO::getSoTienGiam)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                dto.setTongTienVoucherChiTiet(tongTienVoucherChiTiet);

                // Set thÃ´ng tin voucher chÃ­nh (voucher Ä‘áº§u tiÃªn hoáº·c cÃ³ giÃ¡ trá»‹ lá»›n nháº¥t)
                ChiTietVoucherDTO voucherChinh = chiTietVoucherList.stream()
                        .max(Comparator.comparing(v -> v.getSoTienGiam() != null ? v.getSoTienGiam() : BigDecimal.ZERO))
                        .orElse(chiTietVoucherList.get(0));

                dto.setTenVoucher(voucherChinh.getTenVoucher());
                dto.setMaVoucherDaApDung(voucherChinh.getMaVoucher());
                dto.setLoaiVoucher(voucherChinh.getLoaiGiamGia());
                dto.setGiaTriVoucher(BigDecimal.valueOf(voucherChinh.getGiaTriGiam() != null ? voucherChinh.getGiaTriGiam() : 0));

                // Thá»‘ng kÃª voucher
                dto.setSoLuongVoucherDaApDung(chiTietVoucherList.size());

                // PhÃ¢n loáº¡i voucher theo loáº¡i
                Map<String, Integer> thongKeVoucherTheoLoai = chiTietVoucherList.stream()
                        .collect(Collectors.groupingBy(
                                ChiTietVoucherDTO::getLoaiGiamGia,
                                Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                        ));
                dto.setThongKeVoucherTheoLoai(thongKeVoucherTheoLoai);

                // TÃ­nh % tiáº¿t kiá»‡m tá»« voucher
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
                // KhÃ´ng cÃ³ voucher - set giÃ¡ trá»‹ máº·c Ä‘á»‹nh
                dto.setChiTietVoucherList(new ArrayList<>());
                dto.setTongTienVoucherChiTiet(BigDecimal.ZERO);
                dto.setSoLuongVoucherDaApDung(0);
                dto.setPhanTramTietKiemVoucher(0.0);
                dto.setThongKeVoucherTheoLoai(new HashMap<>());
            }

        } catch (Exception e) {
            // Log lá»—i nhÆ°ng khÃ´ng lÃ m fail
            System.err.println("Lá»—i load thÃ´ng tin voucher chi tiáº¿t cho hÃ³a Ä‘Æ¡n " + hoaDon.getId() + ": " + e.getMessage());

            // Set giÃ¡ trá»‹ máº·c Ä‘á»‹nh
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

        // TÃ­nh toÃ¡n cÃ¡c giÃ¡ trá»‹
        dto.calculateValues();

        return dto;
    }

    private void calculateInvoiceAmounts(HoaDonDTO dto, HoaDon hoaDon, List<HoaDonChiTietDTO> chiTietList) {
        // TÃ­nh tá»•ng tiá»n gá»‘c (trÆ°á»›c khi cÃ³ báº¥t ká»³ giáº£m giÃ¡ nÃ o)
        BigDecimal tongTienGoc = chiTietList.stream()
                .map(item -> BigDecimal.valueOf(item.getGiaGoc() != null ? item.getGiaGoc() : item.getGiaBan())
                        .multiply(BigDecimal.valueOf(item.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tá»•ng tiá»n hiá»‡n táº¡i (Ä‘Ã£ tÃ­nh khuyáº¿n mÃ£i sáº£n pháº©m)
        BigDecimal tongTienHienTai = chiTietList.stream()
                .map(HoaDonChiTietDTO::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tá»•ng tiá»n giáº£m tá»« khuyáº¿n mÃ£i sáº£n pháº©m
        BigDecimal tongTienGiamGia = tongTienGoc.subtract(tongTienHienTai);

        // Set phÃ­ váº­n chuyá»ƒn
        BigDecimal phiVanChuyen = hoaDon.getPhiVanChuyen() != null ? hoaDon.getPhiVanChuyen() : BigDecimal.ZERO;

        dto.setTongTienGoc(tongTienGoc);
        dto.setTongTienGiamGia(tongTienGiamGia);
        dto.setPhiVanChuyen(phiVanChuyen);

        // TÃ­nh tá»•ng tiá»n = tiá»n sáº£n pháº©m + phÃ­ váº­n chuyá»ƒn
        BigDecimal tongTien = tongTienHienTai.add(phiVanChuyen);
        dto.setTongTien(hoaDon.getTongTien() != null ? hoaDon.getTongTien() : tongTien);

        // TÃNH Tá»”NG THANH TOÃN (Tá»•ng tiá»n - Voucher - Äiá»ƒm)
        BigDecimal tongThanhToan = dto.getTongTien();

        // Trá»« voucher
        if (dto.getTongTienVoucherChiTiet() != null && dto.getTongTienVoucherChiTiet().compareTo(BigDecimal.ZERO) > 0) {
            tongThanhToan = tongThanhToan.subtract(dto.getTongTienVoucherChiTiet());
        }

        // Trá»« Ä‘iá»ƒm
        BigDecimal giaTriDiem = BigDecimal.ZERO;
        if (hoaDon.getGiaTriDiem() != null && hoaDon.getGiaTriDiem() > 0) {
            giaTriDiem = BigDecimal.valueOf(hoaDon.getGiaTriDiem());
            tongThanhToan = tongThanhToan.subtract(giaTriDiem);
        }

        // Äáº£m báº£o tá»•ng thanh toÃ¡n khÃ´ng Ã¢m
        if (tongThanhToan.compareTo(BigDecimal.ZERO) < 0) {
            tongThanhToan = BigDecimal.ZERO;
        }

        // Set giÃ¡ trá»‹ cuá»‘i cÃ¹ng
        dto.setTongThanhToan(hoaDon.getTongThanhToan() != null ? hoaDon.getTongThanhToan() : tongThanhToan);
        dto.setTienDiem(giaTriDiem);
        dto.setGiaTriDiem(hoaDon.getGiaTriDiem());
        dto.setDiemSuDung(hoaDon.getDiemSuDung() != null ? hoaDon.getDiemSuDung() : 0);
    }

    private void loadVoucherAndPointInfo(HoaDonDTO dto, HoaDon hoaDon) {
        // ThÃ´ng tin voucher
        chiTietVoucherRepository.findByHoaDonId(hoaDon.getId()).stream().findFirst()
                .ifPresent(ctv -> {
                    dto.setTenVoucher(ctv.getVoucher().getTenVoucher());
                    dto.setLoaiVoucher(ctv.getVoucher().getLoaiGiamGia());
                    dto.setGiaTriVoucher(BigDecimal.valueOf(ctv.getVoucher().getGiaTriGiam()));
                    dto.setTongTienVoucher(ctv.getThanhTien());
                });

        // ThÃ´ng tin Ä‘iá»ƒm
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
            throw new RuntimeException("KhÃ´ng thá»ƒ thay Ä‘á»•i tráº¡ng thÃ¡i tá»« " + fromStatus);
        }

        // Kiá»ƒm tra logic chuyá»ƒn tráº¡ng thÃ¡i há»£p lá»‡
        if (!isValidTransition(fromStatus, toStatus)) {
            throw new RuntimeException("KhÃ´ng thá»ƒ chuyá»ƒn tá»« tráº¡ng thÃ¡i " + fromStatus + " sang " + toStatus);
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

            lichSu.setMoTaHanhDong(String.format("Chuyá»ƒn tá»« %s sang %s: %s",
                    oldStatus, newStatus, ghiChu != null ? ghiChu : ""));
            lichSu.setTrangThaiHoaDon(newStatus);
            lichSu.setNgayTao(new Date());
            lichSu.setNgayCapNhat(new Date());

            lichSuHoaDonRepository.save(lichSu);
        } catch (Exception e) {
            // Log error nhÆ°ng khÃ´ng lÃ m fail transaction
            System.err.println("Lá»—i lÆ°u lá»‹ch sá»­: " + e.getMessage());
        }
    }

    private void restoreProductQuantity(Integer hoaDonId) {
        try {
            List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
            for (HoaDonChiTiet chiTiet : chiTietList) {
                ChiTietSanPham ctsp = chiTiet.getChiTietSanPham();
                if (ctsp != null) {
                    ctsp.setSoLuong(ctsp.getSoLuong() + chiTiet.getSoLuong());
                    // Cáº­p nháº­t tá»•ng sá»‘ lÆ°á»£ng sáº£n pháº©m
                    SanPham sanPham = ctsp.getSanPham();
                    if (sanPham != null) {
                        sanPham.setSoLuong(sanPham.getSoLuong() + chiTiet.getSoLuong());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lá»—i hoÃ n láº¡i sá»‘ lÆ°á»£ng: " + e.getMessage());
        }
    }

    private LichSuHoaDonDTO convertLichSuToDTO(LichSuHoaDon lichSu) {
        LichSuHoaDonDTO dto = new LichSuHoaDonDTO();
        dto.setId(lichSu.getId());
        dto.setMoTaHanhDong(lichSu.getMoTaHanhDong());
        dto.setTrangThaiHoaDon(lichSu.getTrangThaiHoaDon());
        dto.setTenNhanVien(lichSu.getNhanVien() != null ? lichSu.getNhanVien().getHoTen() : "Há»‡ thá»‘ng");
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
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));

        hoaDon.setTrangThaiHoaDon(request.getTrangThai());
        hoaDon.setGhiChu(request.getGhiChu());
        hoaDon.setNgayCapNhat(new Date());

        HoaDon saved = hoaDonRepository.save(hoaDon);
        return convertToDTO(saved);
    }

    @Override
    public HoaDonDTO confirmInvoice(Integer id) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));

        hoaDon.setTrangThaiHoaDon("CONFIRMED");
        hoaDon.setNgayXacNhan(new Date());
        hoaDon.setNgayCapNhat(new Date());

        HoaDon saved = hoaDonRepository.save(hoaDon);
        return convertToDTO(saved);
    }

    @Override
    public HoaDonDTO completeInvoice(Integer id) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));

        hoaDon.setTrangThaiHoaDon("COMPLETED");
        hoaDon.setNgayHoanThanh(new Date());
        hoaDon.setNgayCapNhat(new Date());

        HoaDon saved = hoaDonRepository.save(hoaDon);
        return convertToDTO(saved);
    }

    @Override
    public HoaDonDTO cancelInvoice(Integer id, String lyDo) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));

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
                .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao())) // Sáº¯p xáº¿p má»›i nháº¥t trÆ°á»›c
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

            // 2. Kiá»ƒm tra tá»“n kho TRÆ¯á»šC KHI táº¡o Ä‘Æ¡n
            validateProductStock(request.getChiTietSanPham());

            // 3. Validate vÃ  láº¥y voucher náº¿u cÃ³
            Voucher voucher = null;
            if (request.getVoucherId() != null) {
                voucher = validateAndGetVoucher(request.getVoucherId(), request.getTongTien());
            }

            HoaDon hoaDon = new HoaDon();

            // 4. Set khÃ¡ch hÃ ng - Xá»­ lÃ½ cáº£ user vÃ  guest
            if (!isGuestOrder) {
                KhachHang khachHang = khachHangRepository.findById(request.getKhachHangId())
                        .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y khÃ¡ch hÃ ng"));
                hoaDon.setKhachHang(khachHang);
                System.out.println("Order for logged-in user: " + khachHang.getHoTen());
            } else {
                hoaDon.setKhachHang(null); // Guest order - id_khach_hang = NULL
                System.out.println("Guest order for: " + request.getTenNguoiDung());
            }

            // 5. Generate mÃ£ hÃ³a Ä‘Æ¡n (phÃ¢n biá»‡t user vs guest)
            String maHoaDon = request.getMaHoaDon();
            if (maHoaDon == null || maHoaDon.isEmpty()) {
                maHoaDon = generateMaHoaDon(!isGuestOrder);
            }
            hoaDon.setMaHoaDon(maHoaDon);

            // 6. Set thÃ´ng tin cÆ¡ báº£n theo database columns
            hoaDon.setTenNguoiDung(request.getTenNguoiDung());
            hoaDon.setEmail(request.getEmail());
            hoaDon.setSdt(request.getSdt());
            hoaDon.setDiaChi(request.getDiaChi());
            hoaDon.setGhiChu(request.getGhiChu());
            hoaDon.setPhuongThucThanhToan(request.getPhuongThucThanhToan());
            hoaDon.setLoaiHoaDon(request.getLoaiHoaDon());
            hoaDon.setTrangThaiHoaDon(request.getTrangThaiHoaDon());

            // Set Ä‘áº§y Ä‘á»§ thÃ´ng tin tiá»n
            hoaDon.setTongTien(request.getTongTien());
            hoaDon.setPhiVanChuyen(request.getPhiVanChuyen() != null ? request.getPhiVanChuyen() : BigDecimal.ZERO);
            hoaDon.setTongThanhToan(request.getTongThanhToan());
            hoaDon.setDiemSuDung(request.getDiemSuDung() != null ? request.getDiemSuDung() : 0);
            hoaDon.setGiaTriDiem(request.getGiaTriDiem() != null ? request.getGiaTriDiem() : 0.0);

            // Set ngÃ y
            Date now = new Date();
            hoaDon.setNgayTao(now);
            hoaDon.setNgayCapNhat(now);
            // CÃ¡c ngÃ y khÃ¡c Ä‘á»ƒ NULL: ngay_xac_nhan, ngay_giao_hang, etc.

            // 9. LÆ°u hÃ³a Ä‘Æ¡n
            HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
            System.out.println("HÃ³a Ä‘Æ¡n saved with ID: " + savedHoaDon.getId());

            // 10. Táº¡o chi tiáº¿t hÃ³a Ä‘Æ¡n vÃ  trá»« tá»“n kho
            if (request.getChiTietSanPham() != null && !request.getChiTietSanPham().isEmpty()) {
                createHoaDonChiTiet(savedHoaDon, request.getChiTietSanPham(), now);
            }

            // 11. Táº¡o chi_tiet_voucher náº¿u cÃ³ voucher (thay vÃ¬ set trá»±c tiáº¿p vÃ o hoa_don)
            if (voucher != null) {
                createChiTietVoucher(savedHoaDon, voucher, request);
                updateVoucherQuantity(voucher);
            }

            // 12. LÆ°u lá»‹ch sá»­
            saveInitialStatusHistory(savedHoaDon, isGuestOrder ? "Guest" : "User");

            System.out.println("Order created successfully: " + savedHoaDon.getMaHoaDon());
            return convertToDTO(savedHoaDon);
        } catch (Exception e) {
            System.err.println("Error creating order: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("KhÃ´ng thá»ƒ táº¡o Ä‘Æ¡n hÃ ng: " + e.getMessage());
        }
    }

    private void validateCreateHoaDonRequest(CreateHoaDonRequest request) {
        if (request.getTenNguoiDung() == null || request.getTenNguoiDung().trim().isEmpty()) {
            throw new IllegalArgumentException("TÃªn ngÆ°á»i dÃ¹ng khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }


        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }

        if (request.getSdt() == null || request.getSdt().trim().isEmpty()) {
            throw new IllegalArgumentException("Sá»‘ Ä‘iá»‡n thoáº¡i khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }

        if (request.getDiaChi() == null || request.getDiaChi().trim().isEmpty()) {
            throw new IllegalArgumentException("Äá»‹a chá»‰ khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }

        if (request.getChiTietSanPham() == null || request.getChiTietSanPham().isEmpty()) {
            throw new IllegalArgumentException("Giá» hÃ ng trá»‘ng");
        }
    }

    // Validate product stock
    private void validateProductStock(List<ChiTietHoaDonRequest> chiTietList) {
        for (ChiTietHoaDonRequest item : chiTietList) {
            ChiTietSanPham ctsp = chiTietSanPhamRepository.findById(item.getIdChiTietSanPham())
                    .orElseThrow(() -> new RuntimeException("Sáº£n pháº©m khÃ´ng tá»“n táº¡i: " + item.getIdChiTietSanPham()));

            if (ctsp.getSoLuong() < item.getSoLuong()) {
                String productName = ctsp.getSanPham() != null ? ctsp.getSanPham().getTenSanPham() : "Sáº£n pháº©m";
                throw new RuntimeException(productName + " chá»‰ cÃ²n " + ctsp.getSoLuong() + " sáº£n pháº©m trong kho");
            }
        }
    }

    private Voucher validateAndGetVoucher(Integer voucherId, BigDecimal tongTien) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y voucher"));

        Date now = new Date();

        // Kiá»ƒm tra thá»i gian hiá»‡u lá»±c
        if (voucher.getNgayBatDau().after(now)) {
            throw new RuntimeException("Voucher chÆ°a cÃ³ hiá»‡u lá»±c");
        }
        if (voucher.getNgayKetThuc().before(now)) {
            throw new RuntimeException("Voucher Ä‘Ã£ háº¿t háº¡n");
        }

        // Kiá»ƒm tra tráº¡ng thÃ¡i (1 = active theo database)
        if (voucher.getTrangThai() != 1) {
            throw new RuntimeException("Voucher khÃ´ng kháº£ dá»¥ng");
        }

        // Kiá»ƒm tra sá»‘ lÆ°á»£ng
        if (voucher.getSoLuong() <= 0) {
            throw new RuntimeException("Voucher Ä‘Ã£ háº¿t lÆ°á»£t sá»­ dá»¥ng");
        }

        // Kiá»ƒm tra giÃ¡ trá»‹ tá»‘i thiá»ƒu (FLOAT in database)
        if (tongTien.doubleValue() < voucher.getGiaTriGiamToiThieu()) {
            throw new RuntimeException("ÄÆ¡n hÃ ng chÆ°a Ä‘áº¡t giÃ¡ trá»‹ tá»‘i thiá»ƒu: " + voucher.getGiaTriGiamToiThieu());
        }

        System.out.println("Voucher validation passed: " + voucher.getMaVoucher());
        return voucher;
    }

    // Helper method: Táº¡o chi_tiet_voucher record
    // Simplified createChiTietVoucher method to match your actual entity

    private void createChiTietVoucher(HoaDon hoaDon, Voucher voucher, CreateHoaDonRequest request) {
        try {
            ChiTietVoucher chiTietVoucher = new ChiTietVoucher();

            // Generate mÃ£ chi tiáº¿t voucher
            String maChiTietVoucher = generateMaChiTietVoucher();
            chiTietVoucher.setMaChiTietVoucher(maChiTietVoucher);

            // LiÃªn káº¿t vá»›i hÃ³a Ä‘Æ¡n vÃ  voucher
            chiTietVoucher.setHoaDon(hoaDon);
            chiTietVoucher.setVoucher(voucher);

            // LÆ°u thÃ´ng tin voucher táº¡i thá»i Ä‘iá»ƒm Ã¡p dá»¥ng
            chiTietVoucher.setMaVoucher(voucher.getMaVoucher());
            chiTietVoucher.setTenVoucher(voucher.getTenVoucher());
            chiTietVoucher.setLoaiGiamGia(voucher.getLoaiGiamGia());
            chiTietVoucher.setGiaTriGiam(voucher.getGiaTriGiam());
            chiTietVoucher.setGiaTriGiamToiDa(voucher.getGiaTriGiamToiDa());
            chiTietVoucher.setGiaTriGiamToiThieu(voucher.getGiaTriGiamToiThieu());

            // ThÃ´ng tin tÃ­nh toÃ¡n
            BigDecimal giaTriDonHang = request.getTongTien();
            BigDecimal soTienGiam = BigDecimal.valueOf(request.getGiaTriDiem() != null ? request.getGiaTriDiem() : 0);
            BigDecimal thanhTien = request.getTongThanhToan();

            chiTietVoucher.setGiaTriDonHang(giaTriDonHang);
            chiTietVoucher.setSoTienGiam(soTienGiam);
            chiTietVoucher.setThanhTien(thanhTien);

            // Thá»i gian
            Date now = new Date();
            chiTietVoucher.setNgayApDung(now);
            chiTietVoucher.setNgayTao(now);
            chiTietVoucher.setNgayCapNhat(now);

            // LÆ°u vÃ o database
            chiTietVoucherRepository.save(chiTietVoucher);

            System.out.println("Chi tiáº¿t voucher Ä‘Ã£ Ä‘Æ°á»£c táº¡o vá»›i mÃ£: " + maChiTietVoucher);

        } catch (Exception e) {
            System.err.println("Lá»—i khi táº¡o chi tiáº¿t voucher: " + e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ táº¡o chi tiáº¿t voucher");
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
            throw new RuntimeException("KhÃ´ng thá»ƒ cáº­p nháº­t voucher");
        }
    }

    // Helper method: Generate mÃ£ hÃ³a Ä‘Æ¡n
    private String generateMaHoaDon(boolean isUserOrder) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        String date = sdf.format(new Date());
        int random = (int) (Math.random() * 9999) + 1;

        String prefix = isUserOrder ? "HD" : "GU"; // HD for user, GU for guest
        return String.format("%s%s%04d", prefix, date, random);
    }

    private void createHoaDonChiTiet(HoaDon hoaDon, List<ChiTietHoaDonRequest> chiTietList, Date now) {
        for (ChiTietHoaDonRequest chiTiet : chiTietList) {
            // Táº¡o chi tiáº¿t hÃ³a Ä‘Æ¡n
            HoaDonChiTiet hdct = new HoaDonChiTiet();
            hdct.setHoaDon(hoaDon);

            ChiTietSanPham ctsp = chiTietSanPhamRepository.findById(chiTiet.getIdChiTietSanPham())
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y sáº£n pháº©m"));
            hdct.setChiTietSanPham(ctsp);
            hdct.setGia(chiTiet.getGiaBan());
            hdct.setSoLuong(chiTiet.getSoLuong());
            hdct.setTrangThaiHoaDon(hoaDon.getTrangThaiHoaDon());
            hdct.setNgayTao(now);
            hdct.setNgayCapNhat(now);

            hoaDonChiTietRepository.save(hdct);

            // Trá»« tá»“n kho
            updateProductStock(ctsp, chiTiet.getSoLuong());

            System.out.println("âœ… Created detail for product: " + ctsp.getMaChiTiet() +
                    ", quantity: " + chiTiet.getSoLuong());
        }
    }

    private void updateVoucherUsage(Voucher voucher) {
        try {
            if (voucher.getSoLuong() > 0) {
                voucher.setSoLuong(voucher.getSoLuong() - 1);
                voucherRepository.save(voucher);
                System.out.println("âœ… Updated voucher usage: " + voucher.getMaVoucher());
            }
        } catch (Exception e) {
            System.err.println("âš ï¸ Warning: Could not update voucher usage: " + e.getMessage());
        }
    }

    private void saveInitialStatusHistory(HoaDon hoaDon, String orderType) {
        try {
            String description = "Táº¡o Ä‘Æ¡n hÃ ng " + orderType + " - " + hoaDon.getTrangThaiHoaDon();
            saveStatusHistory(hoaDon, null, hoaDon.getTrangThaiHoaDon(), null, description);
        } catch (Exception e) {
            System.err.println("Warning: Could not save initial status history: " + e.getMessage());
        }
    }

    private HoaDonDTO convertToDTO(HoaDon hoaDon) {
        HoaDonDTO dto = new HoaDonDTO();

        // ThÃ´ng tin cÆ¡ báº£n
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

        // ThÃ´ng tin thá»i gian
        dto.setNgayTao(hoaDon.getNgayTao());
        dto.setNgayXacNhan(hoaDon.getNgayXacNhan());
        dto.setNgayGiaoHang(hoaDon.getNgayGiaoHang());
        dto.setNgayHoanThanh(hoaDon.getNgayHoanThanh());
        dto.setNgayCapNhat(hoaDon.getNgayCapNhat());

        // ThÃ´ng tin liÃªn káº¿t
        dto.setTenNhanVien(hoaDon.getNhanVien() != null ? hoaDon.getNhanVien().getHoTen() : null);
        dto.setKhachHangId(hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getId() : null);
        dto.setNhanVienId(hoaDon.getNhanVien() != null ? hoaDon.getNhanVien().getId() : null);

        // ThÃ´ng tin tiá»n cÆ¡ báº£n tá»« entity
        dto.setTongTien(hoaDon.getTongTien());
        dto.setPhiVanChuyen(hoaDon.getPhiVanChuyen() != null ? hoaDon.getPhiVanChuyen() : BigDecimal.ZERO);
        dto.setTongThanhToan(hoaDon.getTongThanhToan());
        dto.setDiemSuDung(hoaDon.getDiemSuDung() != null ? hoaDon.getDiemSuDung() : 0);
        dto.setGiaTriDiem(hoaDon.getGiaTriDiem());
        dto.setTienDiem(hoaDon.getGiaTriDiem() != null ? BigDecimal.valueOf(hoaDon.getGiaTriDiem()) : BigDecimal.ZERO);

        // Láº¥y chi tiáº¿t Ä‘á»ƒ cÃ³ Ä‘áº§y Ä‘á»§ thÃ´ng tin (optional - chá»‰ khi cáº§n)
        try {
            List<HoaDonChiTietDTO> chiTietList = getChiTietList(hoaDon.getId());
            if (chiTietList != null && !chiTietList.isEmpty()) {
                dto.setChiTietList(chiTietList);

                // Load voucher info
                loadDetailedVoucherInfo(dto, hoaDon);

                // TÃ­nh toÃ¡n láº¡i Ä‘áº§y Ä‘á»§
                calculateInvoiceAmounts(dto, hoaDon, chiTietList);

                // Thá»‘ng kÃª
                dto.setSoLuongSanPham(chiTietList.size());
                dto.setTongSoLuong(chiTietList.stream().mapToInt(HoaDonChiTietDTO::getSoLuong).sum());
            }
        } catch (Exception e) {
            // Log nhÆ°ng khÃ´ng fail - dÃ¹ng thÃ´ng tin cÆ¡ báº£n
            System.err.println("KhÃ´ng thá»ƒ load chi tiáº¿t cho hÃ³a Ä‘Æ¡n " + hoaDon.getId() + ": " + e.getMessage());
        }

        return dto;
    }

    private void updateProductStock(ChiTietSanPham ctsp, Integer quantity) {
        Integer currentStock = ctsp.getSoLuong();
        if (currentStock < quantity) {
            String productName = ctsp.getSanPham() != null ? ctsp.getSanPham().getTenSanPham() : ctsp.getMaChiTiet();
            throw new RuntimeException("Sáº£n pháº©m " + productName + " khÃ´ng Ä‘á»§ sá»‘ lÆ°á»£ng trong kho");
        }

        // Trá»« tá»“n kho chi tiáº¿t sáº£n pháº©m
        ctsp.setSoLuong(currentStock - quantity);
        chiTietSanPhamRepository.save(ctsp);

        // Cáº­p nháº­t tá»•ng sá»‘ lÆ°á»£ng sáº£n pháº©m náº¿u cÃ³
        if (ctsp.getSanPham() != null) {
            SanPham sanPham = ctsp.getSanPham();
            Integer currentTotalStock = sanPham.getSoLuong();
            if (currentTotalStock != null && currentTotalStock >= quantity) {
                sanPham.setSoLuong(currentTotalStock - quantity);
                // Note: Cáº§n inject SanPhamRepository náº¿u muá»‘n save
            }
        }

        System.out.println("âœ… Updated stock for " + ctsp.getMaChiTiet() +
                ": " + currentStock + " -> " + ctsp.getSoLuong());
    }
    // Method má»›i Ä‘á»ƒ load thÃ´ng tin tráº£ hÃ ng
    private void loadReturnDetails(HoaDonDTO dto, HoaDon hoaDon) {
        try {
            // Láº¥y danh sÃ¡ch chi tiáº¿t tráº£ hÃ ng
            List<ChiTietTraHangDTO> chiTietTraHangList = chiTietTraHangService.getChiTietTraHangByHoaDon(hoaDon.getId());
            dto.setChiTietTraHangList(chiTietTraHangList);

            // TÃ­nh thá»‘ng kÃª tráº£ hÃ ng
            if (!chiTietTraHangList.isEmpty()) {
                Map<String, Object> returnStats = chiTietTraHangService.getReturnStatistics(hoaDon.getId());

                dto.setSoLoaiSanPhamTraHang((Integer) returnStats.get("soLoaiSanPhamTraHang"));
                dto.setTongSoLuongTraHang((Integer) returnStats.get("tongSoLuongTraHang"));
                dto.setTongTienTraHang(BigDecimal.valueOf((Double) returnStats.get("tongTienHoan")));
                dto.setSoLuongTraHangChoXuLy(((Long) returnStats.get("soLuongChoXuLy")).intValue());
                dto.setSoLuongTraHangDaXuLy(((Long) returnStats.get("soLuongDaXuLy")).intValue());
                dto.setSoLuongTraHangTuChoi(((Long) returnStats.get("soLuongTuChoi")).intValue());
            } else {
                // Khá»Ÿi táº¡o giÃ¡ trá»‹ máº·c Ä‘á»‹nh khi khÃ´ng cÃ³ tráº£ hÃ ng
                dto.setSoLoaiSanPhamTraHang(0);
                dto.setTongSoLuongTraHang(0);
                dto.setTongTienTraHang(BigDecimal.ZERO);
                dto.setSoLuongTraHangChoXuLy(0);
                dto.setSoLuongTraHangDaXuLy(0);
                dto.setSoLuongTraHangTuChoi(0);
            }

            // XÃ¡c Ä‘á»‹nh cÃ³ thá»ƒ tráº£ hÃ ng khÃ´ng
            dto.setCoTheTraHang(canReturnInvoice(hoaDon.getTrangThaiHoaDon()));

        } catch (Exception e) {
            // Log lá»—i nhÆ°ng khÃ´ng lÃ m fail
            System.err.println("Lá»—i load thÃ´ng tin tráº£ hÃ ng cho hÃ³a Ä‘Æ¡n " + hoaDon.getId() + ": " + e.getMessage());

            // Set giÃ¡ trá»‹ máº·c Ä‘á»‹nh
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

    // Method kiá»ƒm tra cÃ³ thá»ƒ tráº£ hÃ ng khÃ´ng dá»±a trÃªn tráº¡ng thÃ¡i hÃ³a Ä‘Æ¡n
    private boolean canReturnInvoice(String trangThai) {
        // Chá»‰ cho phÃ©p tráº£ hÃ ng khi hÃ³a Ä‘Æ¡n Ä‘Ã£ hoÃ n thÃ nh
        return "COMPLETED".equals(trangThai);
    }
    @Override
    public HoaDonDTO cancelInvoiceByCustomer(Integer id, String lyDo, String customerEmail)
            throws IllegalAccessException, IllegalStateException, NoSuchElementException {

        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));

        // Quyá»n sá»Ÿ há»¯u: KhachHangId hoáº·c email Ä‘Æ¡n
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
            throw new IllegalAccessException("KhÃ´ng cÃ³ quyá»n há»§y Ä‘Æ¡n hÃ ng nÃ y");
        }

        // Tráº¡ng thÃ¡i cho phÃ©p há»§y
        String st = hoaDon.getTrangThaiHoaDon();
        if (!Set.of("CHO_XAC_NHAN", "PENDING").contains(st)) {
            throw new IllegalStateException("KhÃ´ng thá»ƒ há»§y Ä‘Æ¡n hÃ ng á»Ÿ tráº¡ng thÃ¡i nÃ y");
        }

        hoaDon.setTrangThaiHoaDon("CANCELLED");
        hoaDon.setGhiChu(lyDo);
        hoaDon.setNgayCapNhat(new Date());

        HoaDon saved = hoaDonRepository.save(hoaDon);
        return convertToDTO(saved);
    }
}

