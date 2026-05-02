package org.example.iws_websitesneaker.Service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.iws_websitesneaker.Dto.BanHang.*;
import org.example.iws_websitesneaker.Dto.ChiTietVoucherDTO;
import org.example.iws_websitesneaker.Service.BanHangService;
import org.example.iws_websitesneaker.Service.ChiTietVoucherService;
import org.example.iws_websitesneaker.entity.*;
import org.example.iws_websitesneaker.repository.BanHang.*;
import org.example.iws_websitesneaker.specification.ChiTietSanPhamSpecification;
import org.example.iws_websitesneaker.util.TextEncodingGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
@Slf4j
public class BanHangServiceImpl implements BanHangService {

    // ===== REPOSITORIES =====
    private static final Logger logger = LoggerFactory.getLogger(BanHangServiceImpl.class);
    @Autowired private HoaDonBHRepository hoaDonRepository;
    @Autowired private HoaDonChiTietBHRepository hoaDonChiTietRepository;
    @Autowired private ChiTietSanPhamBHRepository chiTietSanPhamRepository;
    @Autowired private KhachHangBHRepository khachHangRepository;
    @Autowired private NhanVienBHRepository nhanVienRepository;
    @Autowired private VoucherBHRepository voucherRepository;
    @Autowired private LichSuHoaDonBHRepository lichSuHoaDonRepository;
    @Autowired private DiaChiBHRepository diaChiRepository;
    @Autowired private ViDiemBHRepository viDiemRepository;
    @Autowired private TaiKhoanBHRepository taiKhoanRepository;
    @Autowired private HinhAnhBHRepository hinhAnhRepository;
    @Autowired private KhuyenMaiBHRepository khuyenMaiRepository;
    @Autowired private KhuyenMaiChiTietBHRepository khuyenMaiChiTietRepository;
    @Autowired private DanhMucBHRepository danhMucRepository;
    @Autowired private ThuongHieuBHRepository thuongHieuRepository;
    @Autowired private MauSacBHRepository mauSacRepository;
    @Autowired private KichCoBHRepository kichCoRepository;
    @Autowired private ChatLieuBHRepository chatLieuRepository;
    @Autowired private DeGiayBHRepository deGiayRepository;
    @Autowired private SanPhamBHRepository sanPhamRepository;
    @Autowired private ChiTietVoucherService chiTietVoucherService;

    // ===== QUẢN LÝ HÓA ĐƠN CHỜ =====

    @Override
    @Transactional
    public HoaDonChoResponse taoHoaDonCho(Integer nhanVienId) {
        try {
            log.info("Tạo hóa đơn chờ cho nhân viên ID: {}", nhanVienId);

            // Kiểm tra nhân viên
            NhanVien nhanVien = nhanVienRepository.findById(nhanVienId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

            // Tạo hóa đơn mới
            HoaDon hoaDon = new HoaDon();
            hoaDon.setMaHoaDon(generateMaHoaDon());
            hoaDon.setNhanVien(nhanVien);  // Set object, không phải ID
            hoaDon.setKhachHang(null);     // Chưa có khách hàng
            hoaDon.setTrangThaiHoaDon("CHO");
            hoaDon.setLoaiHoaDon("OFFLINE");
            hoaDon.setNgayTao(new Date());
            hoaDon.setNgayCapNhat(new Date());
            hoaDon.setTongTien(BigDecimal.ZERO);
            hoaDon.setTongThanhToan(BigDecimal.ZERO);

            // Set các field bắt buộc với giá trị mặc định
            hoaDon.setDiaChi("");
            hoaDon.setEmail("");
            hoaDon.setSdt("");
            hoaDon.setTenNguoiDung("Khách lẻ");
            hoaDon.setPhiVanChuyen(BigDecimal.ZERO);

            HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

            // Tạo lịch sử hóa đơn
            taoLichSuHoaDon(savedHoaDon, "Tạo hóa đơn chờ", nhanVien);

            return mapToHoaDonChoResponse(savedHoaDon);

        } catch (Exception e) {
            log.error("Lỗi khi tạo hóa đơn chờ: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo hóa đơn chờ: " + e.getMessage());
        }
    }


    @Override
    public List<HoaDonChoResponse> layDanhSachHoaDonCho() {
        try {
            logger.info("🔍 Lấy danh sách hóa đơn chờ");
            List<HoaDon> hoaDons = hoaDonRepository.findByTrangThaiHoaDon("CHO");

            List<HoaDonChoResponse> responses = new ArrayList<>();
            for (HoaDon hoaDon : hoaDons) {
                responses.add(mapToHoaDonChoResponse(hoaDon));
            }

            logger.info("✅ Lấy {} hóa đơn chờ thành công", responses.size());
            return responses;
        } catch (Exception e) {
            logger.error("❌ Lỗi lấy danh sách hóa đơn chờ", e);
            throw new RuntimeException("Lỗi lấy danh sách hóa đơn chờ: " + e.getMessage());
        }
    }

    @Override
    public HoaDonChoDetailResponse layChiTietHoaDonCho(Integer hoaDonId) {
        try {
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonIdOrderByNgayTao(hoaDonId);

            return mapToHoaDonChoDetailResponse(hoaDon, chiTiets);

        } catch (Exception e) {
            log.error("Lỗi khi lấy chi tiết hóa đơn chờ: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy chi tiết hóa đơn chờ");
        }
    }

    @Override
    public HoaDonChoTongQuanResponse layTongQuanHoaDonCho(Integer hoaDonId) {
        if (hoaDonId == null) {
            log.error("ID hóa đơn không được null");
            throw new RuntimeException("ID hóa đơn không hợp lệ");
        }

        try {
            // Debug: Log ID đang tìm kiếm
            log.info("Đang tìm hóa đơn với ID: {}", hoaDonId);

            // Debug: Kiểm tra exists trước
            boolean exists = hoaDonRepository.existsById(hoaDonId);
            log.info("Hóa đơn ID {} tồn tại: {}", hoaDonId, exists);

            Optional<HoaDon> hoaDonOpt = hoaDonRepository.findById(hoaDonId);
            log.info("Kết quả findById: {}", hoaDonOpt.isPresent() ? "Tìm thấy" : "Không tìm thấy");

            if (!hoaDonOpt.isPresent()) {
                // Debug: Thử query trực tiếp
                List<HoaDon> allHoaDon = hoaDonRepository.findAll();
                log.info("Tổng số hóa đơn trong DB: {}", allHoaDon.size());

                // Debug: Kiểm tra ID có đúng type không
                log.info("Kiểu dữ liệu của hoaDonId: {}", hoaDonId.getClass().getSimpleName());

                log.warn("Không tìm thấy hóa đơn với ID: {}", hoaDonId);
                throw new RuntimeException("Không tìm thấy hóa đơn với ID: " + hoaDonId);
            }

            HoaDon hoaDon = hoaDonOpt.get();
            log.info("Tìm thấy hóa đơn: ID={}, Status={}", hoaDon.getId(), hoaDon.getTrangThaiHoaDon());

            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonIdOrderByNgayTao(hoaDonId);
            log.info("Số lượng chi tiết hóa đơn: {}", chiTiets.size());

            return mapToHoaDonChoTongQuanResponse(hoaDon, chiTiets);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi không xác định khi lấy tổng quan hóa đơn chờ với ID {}: {}", hoaDonId, e.getMessage());
            throw new RuntimeException("Không thể lấy tổng quan hóa đơn chờ: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void xoaHoaDonCho(Integer id) {
        // Kiểm tra hóa đơn có tồn tại không
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + id));

        // Kiểm tra hóa đơn có sản phẩm không
        List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByHoaDonId(id);
        if (!chiTietList.isEmpty()) {
            throw new RuntimeException("Không thể xóa hóa đơn đã có sản phẩm");
        }

        try {
            // XÓA CÁC RECORD LIÊN QUAN TRƯỚC KHI XÓA HÓA ĐƠN

            // 1. Xóa lịch sử hóa đơn
            lichSuHoaDonRepository.deleteByHoaDonId(id);

            // 2. Xóa các bảng liên quan khác (nếu có)
            // voucherSuDungRepository.deleteByHoaDonId(id);
            // diemTichLuyRepository.deleteByHoaDonId(id);

            // 3. Cuối cùng mới xóa hóa đơn
            hoaDonRepository.deleteById(id);

            logger.info("Đã xóa hóa đơn và các record liên quan ID: {}", id);

        } catch (Exception e) {
            logger.error("Lỗi xóa hóa đơn ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Không thể xóa hóa đơn: " + e.getMessage());
        }
    }

    // ===== QUẢN LÝ SẢN PHẨM =====

    @Override
    public Page<SanPhamChiTietBanHangResponse> timKiemSanPham(SanPhamChiTietFilterRequest filter, Pageable pageable) {
        try {
            // SỬA: Sử dụng repository gốc với specification
            Specification<ChiTietSanPham> spec = ChiTietSanPhamSpecification.withFilter(filter);
            Page<ChiTietSanPham> sanPhamPage = chiTietSanPhamRepository.findAll(spec, pageable);

            return sanPhamPage.map(this::mapToSanPhamChiTietBanHangResponse);
        } catch (Exception e) {
            logger.error("❌ Lỗi tìm kiếm sản phẩm", e);
            throw new RuntimeException("Lỗi tìm kiếm sản phẩm: " + e.getMessage());
        }
    }

    @Override
    public SanPhamChiTietBanHangResponse layChiTietSanPham(Integer chiTietSanPhamId) {
        try {
            ChiTietSanPham sanPham = chiTietSanPhamRepository.findById(chiTietSanPhamId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            return mapToSanPhamChiTietBanHangResponse(sanPham);

        } catch (Exception e) {
            log.error("Lỗi khi lấy chi tiết sản phẩm: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy chi tiết sản phẩm");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public ScanQRResponse scanQRSanPham(String qrCode) {
        try {
            ChiTietSanPham sanPham = chiTietSanPhamRepository.findByMaQR(qrCode)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với mã QR: " + qrCode));

            if (sanPham.getTrangThai() != 1) {
                throw new RuntimeException("Sản phẩm không còn hoạt động");
            }

            return mapToScanQRResponse(sanPham);

        } catch (Exception e) {
            log.error("Lỗi khi quét QR: {}", e.getMessage());
            throw new RuntimeException("Không thể quét mã QR: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<SanPhamChiTietBanHangResponse> laySanPhamTuongTu(Integer chiTietSanPhamId) {
        try {
            ChiTietSanPham sanPham = chiTietSanPhamRepository.findById(chiTietSanPhamId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            // Lấy thông tin sản phẩm gốc - SỬ DỤNG RELATIONSHIP
            SanPham sanPhamGoc = sanPham.getSanPham();
            if (sanPhamGoc == null) {
                return new ArrayList<>();
            }

            // Lấy ID danh mục và thương hiệu từ relationship
            Integer danhMucId = null;
            Integer thuongHieuId = null;

            if (sanPhamGoc.getDanhMuc() != null) {
                danhMucId = sanPhamGoc.getDanhMuc().getId();
            }
            if (sanPhamGoc.getThuongHieu() != null) {
                thuongHieuId = sanPhamGoc.getThuongHieu().getId();
            }

            List<ChiTietSanPham> sanPhamTuongTu = chiTietSanPhamRepository.findSanPhamTuongTu(
                    danhMucId,
                    thuongHieuId,
                    chiTietSanPhamId,
                    PageRequest.of(0, 10)
            );

            return sanPhamTuongTu.stream()
                    .map(this::mapToSanPhamChiTietBanHangResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Lỗi khi lấy sản phẩm tương tự: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy sản phẩm tương tự");
        }
    }

    // ===== QUẢN LÝ SẢN PHẨM TRONG HÓA ĐƠN =====

    @Override
    @Transactional
    public HoaDonChoTongQuanResponse themSanPhamVaoHoaDon(Integer hoaDonId, ThemSanPhamRequest request) {
        try {
            log.info("Thêm sản phẩm vào hóa đơn ID: {}, Sản phẩm ID: {}", hoaDonId, request.getChiTietSanPhamId());

            // Validate request
            validateThemSanPhamRequest(request);

            // Kiểm tra hóa đơn
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

            if (!"CHO".equals(hoaDon.getTrangThaiHoaDon())) {
                throw new RuntimeException("Chỉ có thể thêm sản phẩm vào hóa đơn đang chờ thanh toán");
            }

            // Kiểm tra sản phẩm
            ChiTietSanPham sanPham = chiTietSanPhamRepository.findById(request.getChiTietSanPhamId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            kiemTraSanPhamCoTheBan(sanPham, request.getSoLuong());

            // Kiểm tra sản phẩm đã có trong hóa đơn chưa
            Optional<HoaDonChiTiet> existingItem = hoaDonChiTietRepository
                    .findByHoaDonIdAndChiTietSanPham_Id(hoaDonId, request.getChiTietSanPhamId());

            if (existingItem.isPresent()) {
                // Cập nhật số lượng nếu sản phẩm đã có
                HoaDonChiTiet chiTiet = existingItem.get();
                int soLuongMoi = chiTiet.getSoLuong() + request.getSoLuong();

                if (sanPham.getSoLuong() < request.getSoLuong()) {
                    throw new RuntimeException("Không đủ số lượng tồn kho. Còn lại: " + sanPham.getSoLuong());
                }

                chiTiet.setSoLuong(soLuongMoi);
                chiTiet.setNgayCapNhat(new Date());
                hoaDonChiTietRepository.save(chiTiet);
            } else {
                // Thêm sản phẩm mới - SỬ DỤNG RELATIONSHIP
                HoaDonChiTiet chiTiet = new HoaDonChiTiet();
                chiTiet.setHoaDon(hoaDon); // Set object, không phải ID
                chiTiet.setChiTietSanPham(sanPham); // Set object, không phải ID
                chiTiet.setSoLuong(request.getSoLuong());
                chiTiet.setGia(request.getDonGia() != null ?
                        convertToBigDecimal(request.getDonGia()).doubleValue() :
                        sanPham.getGiaBan().doubleValue());
                chiTiet.setTrangThaiHoaDon("CHO");
                chiTiet.setNgayTao(new Date());
                chiTiet.setNgayCapNhat(new Date());
                hoaDonChiTietRepository.save(chiTiet);
            }

            // Trừ số lượng tồn kho
            sanPham.setSoLuong(sanPham.getSoLuong() - request.getSoLuong());
            chiTietSanPhamRepository.save(sanPham);

            // Cập nhật tổng tiền hóa đơn
            capNhatTongTienHoaDon(hoaDonId);

            log.info("Đã thêm sản phẩm vào hóa đơn thành công");

            return layTongQuanHoaDonCho(hoaDonId);

        } catch (Exception e) {
            log.error("Lỗi khi thêm sản phẩm vào hóa đơn: {}", e.getMessage());
            throw new RuntimeException("Không thể thêm sản phẩm vào hóa đơn: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public HoaDonChoTongQuanResponse capNhatSanPhamTrongHoaDon(
            Integer hoaDonId, Integer hoaDonChiTietId, CapNhatSanPhamRequest request) {
        try {
            log.info("Cập nhật sản phẩm trong hóa đơn ID: {}, Chi tiết ID: {}", hoaDonId, hoaDonChiTietId);

            validateCapNhatSanPhamRequest(request);

            // Kiểm tra hóa đơn chi tiết
            HoaDonChiTiet chiTiet = hoaDonChiTietRepository.findById(hoaDonChiTietId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết hóa đơn"));

            if (!chiTiet.getHoaDon().getId().equals(hoaDonId)) {
                throw new RuntimeException("Chi tiết hóa đơn không thuộc về hóa đơn này");
            }

            ChiTietSanPham sanPham = chiTiet.getChiTietSanPham(); // Sử dụng relationship
            int soLuongCu = chiTiet.getSoLuong();
            int soLuongMoi = request.getSoLuong();
            int chenhLech = soLuongMoi - soLuongCu;

            // Kiểm tra tồn kho
            if (chenhLech > 0 && sanPham.getSoLuong() < chenhLech) {
                throw new RuntimeException("Không đủ số lượng tồn kho. Còn lại: " + sanPham.getSoLuong());
            }

            // Cập nhật chi tiết hóa đơn
            chiTiet.setSoLuong(soLuongMoi);
            if (request.getDonGia() != null) {
                chiTiet.setGia(convertToBigDecimal(request.getDonGia()).doubleValue());
            }
            chiTiet.setNgayCapNhat(new Date());
            hoaDonChiTietRepository.save(chiTiet);

            // Cập nhật tồn kho
            sanPham.setSoLuong(sanPham.getSoLuong() - chenhLech);
            chiTietSanPhamRepository.save(sanPham);

            // Cập nhật tổng tiền hóa đơn
            capNhatTongTienHoaDon(hoaDonId);

            log.info("Đã cập nhật sản phẩm trong hóa đơn thành công");

            return layTongQuanHoaDonCho(hoaDonId);

        } catch (Exception e) {
            log.error("Lỗi khi cập nhật sản phẩm trong hóa đơn: {}", e.getMessage());
            throw new RuntimeException("Không thể cập nhật sản phẩm trong hóa đơn: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public HoaDonChoTongQuanResponse xoaSanPhamKhoiHoaDon(Integer hoaDonId, Integer hoaDonChiTietId) {
        try {
            log.info("Xóa sản phẩm khỏi hóa đơn ID: {}, Chi tiết ID: {}", hoaDonId, hoaDonChiTietId);

            // Kiểm tra hóa đơn chi tiết
            HoaDonChiTiet chiTiet = hoaDonChiTietRepository.findById(hoaDonChiTietId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết hóa đơn"));

            if (!chiTiet.getHoaDon().getId().equals(hoaDonId)) {
                throw new RuntimeException("Chi tiết hóa đơn không thuộc về hóa đơn này");
            }

            // Hoàn trả số lượng tồn kho
            ChiTietSanPham sanPham = chiTiet.getChiTietSanPham(); // Sử dụng relationship
            sanPham.setSoLuong(sanPham.getSoLuong() + chiTiet.getSoLuong());
            chiTietSanPhamRepository.save(sanPham);

            // Xóa chi tiết hóa đơn
            hoaDonChiTietRepository.deleteById(hoaDonChiTietId);

            // Cập nhật tổng tiền hóa đơn
            capNhatTongTienHoaDon(hoaDonId);

            log.info("Đã xóa sản phẩm khỏi hóa đơn thành công");

            return layTongQuanHoaDonCho(hoaDonId);

        } catch (Exception e) {
            log.error("Lỗi khi xóa sản phẩm khỏi hóa đơn: {}", e.getMessage());
            throw new RuntimeException("Không thể xóa sản phẩm khỏi hóa đơn: " + e.getMessage());
        }
    }

    @Override
    public TinhGiaResponse tinhGiaSanPham(TinhGiaRequest request) {
        try {
            ChiTietSanPham sanPham = chiTietSanPhamRepository.findById(request.getChiTietSanPhamId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            // SỬA: Convert từ Double sang BigDecimal
            BigDecimal giaGoc = sanPham.getGiaGoc() != null ?
                    BigDecimal.valueOf(sanPham.getGiaGoc()) : BigDecimal.ZERO;
            BigDecimal giaBan = sanPham.getGiaBan() != null ?
                    BigDecimal.valueOf(sanPham.getGiaBan()) : BigDecimal.ZERO;
            int soLuong = request.getSoLuong();

            // Tính khuyến mãi
            List<KhuyenMaiSanPhamResponse> khuyenMais = layKhuyenMaiSanPham(request.getChiTietSanPhamId());

            BigDecimal tongTienGoc = giaGoc.multiply(BigDecimal.valueOf(soLuong));
            BigDecimal tongTienSauGiam = giaBan.multiply(BigDecimal.valueOf(soLuong));

            // Áp dụng khuyến mãi
            for (KhuyenMaiSanPhamResponse km : khuyenMais) {
                if (Boolean.TRUE.equals(km.getDangApDung())) {
                    if ("PHAN_TRAM".equals(km.getLoaiKhuyenMai())) {
                        BigDecimal giam = tongTienSauGiam.multiply(BigDecimal.valueOf(km.getGiaTri() / 100))
                                .setScale(0, RoundingMode.HALF_UP);
                        tongTienSauGiam = tongTienSauGiam.subtract(giam);
                    } else {
                        BigDecimal giam = BigDecimal.valueOf(km.getGiaTri()).multiply(BigDecimal.valueOf(soLuong));
                        tongTienSauGiam = tongTienSauGiam.subtract(giam);
                    }
                }
            }

            BigDecimal tongTietKiem = tongTienGoc.subtract(tongTienSauGiam);
            Float phanTramGiam = tongTienGoc.compareTo(BigDecimal.ZERO) > 0 ?
                    tongTietKiem.divide(tongTienGoc, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).floatValue() : 0f;

            return TinhGiaResponse.builder()
                    .chiTietSanPhamId(request.getChiTietSanPhamId())
                    .soLuong(soLuong)
                    .giaGoc(giaGoc)
                    .giaBan(giaBan)
                    .tongTienGoc(tongTienGoc)
                    .tongTienSauGiam(tongTienSauGiam)
                    .tongTietKiem(tongTietKiem)
                    .danhSachKhuyenMai(khuyenMais)
                    .phanTramGiamTongCong(phanTramGiam)
                    .build();

        } catch (Exception e) {
            log.error("Lỗi khi tính giá sản phẩm: {}", e.getMessage());
            throw new RuntimeException("Không thể tính giá sản phẩm: " + e.getMessage());
        }
    }
    private KhachHangResponse mapToKhachHangResponseSafe(KhachHang khachHang) {
        if (khachHang == null) return null;

        try {
            // Basic info - luôn có
            String maKhachHang = khachHang.getMaKhachHang() != null ? khachHang.getMaKhachHang() : "";
            String hoTen = khachHang.getHoTen() != null ? khachHang.getHoTen() : "";
            String sdt = khachHang.getSdt() != null ? khachHang.getSdt() : "";
            Integer trangThai = khachHang.getTrangThai() != null ? khachHang.getTrangThai() : 0;

            // ✅ Safe email loading
            String email = "";
            try {
                if (khachHang.getTaiKhoan() != null &&
                        khachHang.getTaiKhoan().getEmail() != null) {
                    email = khachHang.getTaiKhoan().getEmail().trim();
                }
            } catch (Exception e) {
                // Ignore email loading error
                email = "";
            }

            // ✅ Safe points calculation
            Double diemTichLuy = 0.0;
            try {
                if (khachHang.getViDiem() != null) {
                    ViDiem viDiem = khachHang.getViDiem();
                    Double tongDiem = viDiem.getTongDiem() != null ? viDiem.getTongDiem() : 0.0;
                    Double diemDaDung = viDiem.getSoDiemDaDung() != null ? viDiem.getSoDiemDaDung() : 0.0;
                    diemTichLuy = Math.max(0, tongDiem - diemDaDung);
                }
            } catch (Exception e) {
                // Ignore points calculation error
                diemTichLuy = 0.0;
            }

            return KhachHangResponse.builder()
                    .id(khachHang.getId())
                    .maKhachHang(maKhachHang)
                    .hoTen(hoTen)
                    .sdt(sdt)
                    .email(email)
                    .trangThai(trangThai)
                    .diemTichLuy(diemTichLuy)
                    .ngayTao(khachHang.getNgayTao())
                    .build();

        } catch (Exception e) {
            log.warn("❌ Error mapping customer {}: {}", khachHang.getId(), e.getMessage());

            // Return minimal safe object
            return KhachHangResponse.builder()
                    .id(khachHang.getId())
                    .maKhachHang(khachHang.getMaKhachHang() != null ? khachHang.getMaKhachHang() : "")
                    .hoTen(khachHang.getHoTen() != null ? khachHang.getHoTen() : "Khách hàng")
                    .sdt(khachHang.getSdt() != null ? khachHang.getSdt() : "")
                    .email("")
                    .trangThai(0)
                    .diemTichLuy(0.0)
                    .ngayTao(khachHang.getNgayTao())
                    .build();
        }
    }

    @Override
    public Page<KhachHangResponse> timKiemKhachHang(String keyword, Pageable pageable) {
        try {
            log.info("🔍 Tìm kiếm khách hàng với keyword: '{}'", keyword);

            Page<KhachHang> khachHangs = null;

            if (StringUtils.hasText(keyword)) {
                String keywordTrimmed = keyword.trim();
                log.info("🔄 Searching with keyword: '{}'", keywordTrimmed);

                try {
                    // Thử query với keyword
                    khachHangs = khachHangRepository.searchByKeyword(keywordTrimmed, pageable);
                    log.info("✅ Found {} customers with search query", khachHangs.getTotalElements());
                } catch (Exception e) {
                    log.warn("⚠️ Search query failed: {}", e.getMessage());
                    // Fallback về query đơn giản
                    try {
                        khachHangs = khachHangRepository.searchByKeywordSimple(keywordTrimmed, pageable);
                        log.info("✅ Found {} customers with simple query", khachHangs.getTotalElements());
                    } catch (Exception e2) {
                        log.warn("⚠️ Simple query also failed: {}", e2.getMessage());
                        // Manual search
                        khachHangs = searchCustomersManually(keywordTrimmed, pageable);
                    }
                }
            } else {
                log.info("📋 Loading all active customers");
                try {
                    khachHangs = khachHangRepository.findAllActive(pageable);
                    log.info("✅ Found {} total active customers", khachHangs.getTotalElements());
                } catch (Exception e) {
                    log.warn("⚠️ findAllActive failed: {}", e.getMessage());
                    // Manual search without keyword
                    khachHangs = searchCustomersManually("", pageable);
                }
            }

            // Đảm bảo khachHangs không bao giờ null
            if (khachHangs == null) {
                khachHangs = new PageImpl<>(new ArrayList<>(), pageable, 0);
            }

            // Map to DTO với safe approach
            List<KhachHangResponse> responses = khachHangs.getContent().stream()
                    .map(this::mapKhachHangSafe)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            log.info("✅ Successfully mapped {} customer responses", responses.size());
            return new PageImpl<>(responses, pageable, khachHangs.getTotalElements());

        } catch (Exception e) {
            log.error("❌ Critical error in timKiemKhachHang: {}", e.getMessage());
            // KHÔNG THROW EXCEPTION - trả về empty page
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    private KhachHangResponse mapKhachHangSafe(KhachHang kh) {
        if (kh == null) return null;

        try {
            return KhachHangResponse.builder()
                    .id(kh.getId())
                    .maKhachHang(kh.getMaKhachHang() != null ? kh.getMaKhachHang() : "")
                    .hoTen(kh.getHoTen() != null ? kh.getHoTen() : "")
                    .sdt(kh.getSdt() != null ? kh.getSdt() : "")
                    .email(getEmailSafe(kh))
                    .trangThai(kh.getTrangThai() != null ? kh.getTrangThai() : 0)
                    .diemTichLuy(getDiemTichLuySafe(kh))
                    .ngayTao(kh.getNgayTao())
                    .build();
        } catch (Exception e) {
            log.warn("❌ Error mapping customer {}: {}", kh.getId(), e.getMessage());

            // Return minimal safe version
            return KhachHangResponse.builder()
                    .id(kh.getId())
                    .maKhachHang(kh.getMaKhachHang() != null ? kh.getMaKhachHang() : "")
                    .hoTen(kh.getHoTen() != null ? kh.getHoTen() : "Khách hàng")
                    .sdt(kh.getSdt() != null ? kh.getSdt() : "")
                    .email("")
                    .trangThai(0)
                    .diemTichLuy(0.0)
                    .ngayTao(kh.getNgayTao())
                    .build();
        }
    }

    private String getEmailSafe(KhachHang kh) {
        try {
            return (kh.getTaiKhoan() != null && kh.getTaiKhoan().getEmail() != null)
                    ? kh.getTaiKhoan().getEmail().trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    // ✅ 6. SAFE POINTS GETTER
    private Double getDiemTichLuySafe(KhachHang kh) {
        try {
            if (kh.getViDiem() != null) {
                ViDiem vd = kh.getViDiem();
                Double tong = vd.getTongDiem() != null ? vd.getTongDiem() : 0.0;
                Double daDung = vd.getSoDiemDaDung() != null ? vd.getSoDiemDaDung() : 0.0;
                return Math.max(0, tong - daDung);
            }
            return 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private Page<KhachHang> searchCustomersManually(String keyword, Pageable pageable) {
        try {
            log.info("🔧 Manual search for keyword: '{}'", keyword);

            // Lấy tất cả khách hàng
            List<KhachHang> allCustomers = khachHangRepository.findAll();

            // Filter
            List<KhachHang> filtered = allCustomers.stream()
                    .filter(kh -> kh != null && kh.getTrangThai() != null && kh.getTrangThai() == 1)
                    .filter(kh -> {
                        if (!StringUtils.hasText(keyword)) return true;

                        String lowerKeyword = keyword.toLowerCase();
                        boolean matchName = kh.getHoTen() != null &&
                                kh.getHoTen().toLowerCase().contains(lowerKeyword);
                        boolean matchPhone = kh.getSdt() != null &&
                                kh.getSdt().contains(keyword);

                        return matchName || matchPhone;
                    })
                    .sorted((a, b) -> {
                        Date dateA = a.getNgayTao();
                        Date dateB = b.getNgayTao();
                        if (dateA == null && dateB == null) return 0;
                        if (dateA == null) return 1;
                        if (dateB == null) return -1;
                        return dateB.compareTo(dateA); // Desc order
                    })
                    .collect(Collectors.toList());

            // Manual pagination
            int start = (int) pageable.getOffset();
            int total = filtered.size();
            int end = Math.min(start + pageable.getPageSize(), total);

            List<KhachHang> pageContent = start >= total ?
                    new ArrayList<>() : filtered.subList(start, end);

            log.info("🔧 Manual search found {} customers", total);
            return new PageImpl<>(pageContent, pageable, total);

        } catch (Exception e) {
            log.error("❌ Manual search failed: {}", e.getMessage());
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }



    // ✅ THÊM: Helper method để tìm khách hàng manually khi query fail
    private Page<KhachHang> findActiveCustomersManually(Pageable pageable, String keyword) {
        try {
            List<KhachHang> allCustomers = khachHangRepository.findAll();

            // Filter theo điều kiện
            List<KhachHang> filteredCustomers = allCustomers.stream()
                    .filter(kh -> kh.getTrangThai() != null && kh.getTrangThai() == 1)
                    .filter(kh -> {
                        if (!StringUtils.hasText(keyword)) return true;

                        String keywordLower = keyword.toLowerCase();
                        boolean matchName = kh.getHoTen() != null &&
                                kh.getHoTen().toLowerCase().contains(keywordLower);
                        boolean matchPhone = kh.getSdt() != null &&
                                kh.getSdt().contains(keyword);

                        return matchName || matchPhone;
                    })
                    .sorted((a, b) -> {
                        if (a.getNgayTao() == null && b.getNgayTao() == null) return 0;
                        if (a.getNgayTao() == null) return 1;
                        if (b.getNgayTao() == null) return -1;
                        return b.getNgayTao().compareTo(a.getNgayTao());
                    })
                    .collect(Collectors.toList());

            // Phân trang manual
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), filteredCustomers.size());

            if (start >= filteredCustomers.size()) {
                return new PageImpl<>(new ArrayList<>(), pageable, filteredCustomers.size());
            }

            List<KhachHang> pageContent = filteredCustomers.subList(start, end);
            return new PageImpl<>(pageContent, pageable, filteredCustomers.size());

        } catch (Exception e) {
            log.error("❌ Manual filtering failed: {}", e.getMessage());
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }


    // ✅ SỬA: Version mới của mapping method không gây transaction rollback
    private KhachHangResponse mapToKhachHangResponseSafeV2(KhachHang khachHang) {
        if (khachHang == null) return null;

        try {
            // ✅ SỬA: Tính điểm tích lũy an toàn hơn
            Double diemTichLuy = 0.0;
            try {
                if (khachHang.getViDiem() != null) {
                    ViDiem viDiem = khachHang.getViDiem();
                    Double tongDiem = viDiem.getTongDiem() != null ? viDiem.getTongDiem() : 0.0;
                    Double diemDaDung = viDiem.getSoDiemDaDung() != null ? viDiem.getSoDiemDaDung() : 0.0;
                    diemTichLuy = Math.max(0, tongDiem - diemDaDung);
                }
            } catch (Exception e) {
                log.debug("Could not calculate points for customer {}: {}", khachHang.getId(), e.getMessage());
                diemTichLuy = 0.0;
            }

            // ✅ SỬA: Lấy email an toàn hơn
            String email = "";
            try {
                if (khachHang.getTaiKhoan() != null &&
                        khachHang.getTaiKhoan().getEmail() != null &&
                        !khachHang.getTaiKhoan().getEmail().trim().isEmpty()) {
                    email = khachHang.getTaiKhoan().getEmail().trim();
                }
            } catch (Exception e) {
                log.debug("Could not load email for customer {}: {}", khachHang.getId(), e.getMessage());
                email = "";
            }

            return KhachHangResponse.builder()
                    .id(khachHang.getId())
                    .maKhachHang(khachHang.getMaKhachHang() != null ? khachHang.getMaKhachHang() : "")
                    .hoTen(khachHang.getHoTen() != null ? khachHang.getHoTen() : "")
                    .sdt(khachHang.getSdt() != null ? khachHang.getSdt() : "")
                    .email(email)
                    .trangThai(khachHang.getTrangThai() != null ? khachHang.getTrangThai() : 0)
                    .diemTichLuy(diemTichLuy)
                    .ngayTao(khachHang.getNgayTao())
                    .build();

        } catch (Exception e) {
            log.error("❌ Critical error mapping customer {}: {}",
                    khachHang.getId(), e.getMessage());

            // ✅ SỬA: Trả về object tối thiểu thay vì null để tránh NPE
            return KhachHangResponse.builder()
                    .id(khachHang.getId())
                    .maKhachHang(khachHang.getMaKhachHang() != null ? khachHang.getMaKhachHang() : "")
                    .hoTen(khachHang.getHoTen() != null ? khachHang.getHoTen() : "Khách hàng")
                    .sdt(khachHang.getSdt() != null ? khachHang.getSdt() : "")
                    .email("")
                    .trangThai(khachHang.getTrangThai() != null ? khachHang.getTrangThai() : 0)
                    .diemTichLuy(0.0)
                    .ngayTao(khachHang.getNgayTao())
                    .build();
        }
    }


    // ===== CẬP NHẬT PHƯƠNG THỨC taoKhachHangNhanh TRONG BanHangServiceImpl =====

    @Override
    @Transactional
    public KhachHangResponse taoKhachHangNhanh(TaoKhachHangNhanhRequest request) {
        try {
            log.info("Tạo khách hàng nhanh: {}", request.getSdt());
            request.setHoTen(TextEncodingGuard.normalizeAndRejectCorrupted("Họ tên khách hàng", request.getHoTen()));
            request.setDiaChi(TextEncodingGuard.normalizeAndRejectCorrupted("Địa chỉ khách hàng", request.getDiaChi()));

            // Kiểm tra số điện thoại đã tồn tại
            if (khachHangRepository.existsBySdt(request.getSdt())) {
                throw new RuntimeException("Số điện thoại đã được sử dụng");
            }

            // ✅ SỬA: Kiểm tra email qua TaiKhoan thay vì KhachHang
            if (StringUtils.hasText(request.getEmail())) {
                // Kiểm tra trong TaiKhoan trực tiếp
                if (taiKhoanRepository.existsByEmail(request.getEmail())) {
                    throw new RuntimeException("Email đã được sử dụng");
                }
                // Kiểm tra thêm qua KhachHang (backup)
                if (khachHangRepository.existsByTaiKhoanEmail(request.getEmail())) {
                    throw new RuntimeException("Email đã được sử dụng bởi khách hàng khác");
                }
            }

            // 1. Tạo TaiKhoan trước
            TaiKhoan taiKhoan = new TaiKhoan();
            taiKhoan.setMaTaiKhoan("TK" + System.currentTimeMillis());
            taiKhoan.setEmail(request.getEmail() != null ? request.getEmail() : "");
            taiKhoan.setMatKhau("default123"); // Mật khẩu mặc định
            taiKhoan.setVaiTro(TaiKhoan.VaiTro.USER); // 0 = khách hàng
            taiKhoan.setTrangThai(1);
            taiKhoan.setNgayTao(new Date());
            taiKhoan.setNgayCapNhat(new Date());
            TaiKhoan savedTaiKhoan = taiKhoanRepository.save(taiKhoan);

            // 2. Tạo ViDiem
            ViDiem viDiem = new ViDiem();
            viDiem.setTongDiem(0.0);
            viDiem.setSoDiemDaDung(0.0);
            viDiem.setSoDiemDaCong(0.0);
            viDiem.setGiaTriDiem(1000.0);
            viDiem.setNgayTao(new Date());
            viDiem.setNgayCapNhat(new Date());
            ViDiem savedViDiem = viDiemRepository.save(viDiem);

            // 3. Tạo KhachHang
            KhachHang khachHang = new KhachHang();
            khachHang.setMaKhachHang(generateMaKhachHang());
            khachHang.setHoTen(request.getHoTen());
            khachHang.setSdt(request.getSdt());
            khachHang.setViDiem(savedViDiem);
            khachHang.setTaiKhoan(savedTaiKhoan);
            khachHang.setTrangThai(1);
            khachHang.setNgayTao(new Date());
            khachHang.setNgayCapNhat(new Date());

            KhachHang savedKhachHang = khachHangRepository.save(khachHang);

            // 4. Tạo DiaChi riêng biệt cho TaiKhoan (nếu cần)
            if (StringUtils.hasText(request.getDiaChi())) {
                DiaChi diaChi = new DiaChi();
                diaChi.setTaiKhoan(savedTaiKhoan);
                diaChi.setMaTinh("01");
                diaChi.setMaPhuong("00001");
                diaChi.setTenTinh("Hà Nội");
                diaChi.setTenPhuong("Phúc Xá");
                diaChi.setDiaChiChiTiet(request.getDiaChi());
                diaChi.setIsDefault(true);
                diaChi.setTrangThai(1);
                diaChi.setNgayTao(new Date());
                diaChi.setNgayCapNhat(new Date());
                diaChiRepository.save(diaChi);
            }

            log.info("Đã tạo khách hàng nhanh thành công ID: {}", savedKhachHang.getId());

            return mapToKhachHangResponse(savedKhachHang);

        } catch (Exception e) {
            log.error("Lỗi khi tạo khách hàng nhanh: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo khách hàng: " + e.getMessage());
        }
    }

    @Override
    public KhachHangDetailResponse layThongTinKhachHang(Integer khachHangId) {
        try {
            KhachHang khachHang = khachHangRepository.findById(khachHangId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

            return mapToKhachHangDetailResponse(khachHang);

        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin khách hàng: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy thông tin khách hàng");
        }
    }

    @Override
    @Transactional
    public HoaDonChoTongQuanResponse apDungKhachHang(Integer hoaDonId, Integer khachHangId) {
        try {
            log.info("Áp dụng khách hàng ID: {} cho hóa đơn ID: {}", khachHangId, hoaDonId);

            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

            KhachHang khachHang = khachHangRepository.findById(khachHangId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

            // SỬA: Set object thay vì ID
            hoaDon.setKhachHang(khachHang);

            // SỬA: Lấy email từ TaiKhoan thay vì từ KhachHang
            if (khachHang.getTaiKhoan() != null && StringUtils.hasText(khachHang.getTaiKhoan().getEmail())) {
                hoaDon.setEmail(khachHang.getTaiKhoan().getEmail());
            } else {
                hoaDon.setEmail("");
            }

            hoaDon.setSdt(khachHang.getSdt());
            hoaDon.setTenNguoiDung(khachHang.getHoTen());

            // SỬA: Lấy địa chỉ từ TaiKhoan -> DiaChi
            if (khachHang.getTaiKhoan() != null) {
                Optional<DiaChi> diaChiOpt = diaChiRepository.findByTaiKhoanIdAndIsDefaultTrue(khachHang.getTaiKhoan().getId());
                if (diaChiOpt.isPresent()) {
                    DiaChi diaChi = diaChiOpt.get();
                    String diaChiDayDu = diaChi.getDiaChiChiTiet() + ", " +
                            diaChi.getTenPhuong() + ", " +
                            diaChi.getTenTinh();
                    hoaDon.setDiaChi(diaChiDayDu);
                }
            }

            hoaDon.setNgayCapNhat(new Date());
            hoaDonRepository.save(hoaDon);

            return layTongQuanHoaDonCho(hoaDonId);

        } catch (Exception e) {
            log.error("Lỗi khi áp dụng khách hàng: {}", e.getMessage());
            throw new RuntimeException("Không thể áp dụng khách hàng: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public HoaDonChoTongQuanResponse boKhachHang(Integer hoaDonId) {
        try {
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

            hoaDon.setKhachHang(null);  // Set null thay vì setIdKhachHang(null)
            hoaDon.setEmail("");
            hoaDon.setSdt("");
            hoaDon.setTenNguoiDung("Khách lẻ");
            hoaDon.setDiaChi("");
            hoaDon.setNgayCapNhat(new Date());
            hoaDonRepository.save(hoaDon);

            return layTongQuanHoaDonCho(hoaDonId);

        } catch (Exception e) {
            log.error("Lỗi khi bỏ khách hàng: {}", e.getMessage());
            throw new RuntimeException("Không thể bỏ khách hàng: " + e.getMessage());
        }
    }

    // ===== QUẢN LÝ VOUCHER =====

    private VoucherResponse mapToVoucherResponseSafe(Voucher voucher) {
        if (voucher == null) return null;

        try {
            Date currentDate = new Date();
            boolean daHetHan = voucher.getNgayKetThuc() != null && currentDate.after(voucher.getNgayKetThuc());
            boolean daHetSoLuong = voucher.getSoLuong() != null && voucher.getSoLuong() <= 0;

            // ✅ SỬA: Đảm bảo loaiGiamGia được set đúng
            String loaiGiamGia = voucher.getLoaiGiamGia();
            if (loaiGiamGia == null || loaiGiamGia.isEmpty()) {
                // Fallback dựa trên pattern của tên
                loaiGiamGia = "PHAN_TRAM"; // Default
            }

            return VoucherResponse.builder()
                    .id(voucher.getId())
                    .maVoucher(voucher.getMaVoucher())
                    .tenVoucher(voucher.getTenVoucher())
                    .loaiGiamGia(loaiGiamGia)
                    .trangThai(voucher.getTrangThai())
                    .duongDanHinhAnh(voucher.getDuongDanHinhAnh())
                    .giaTriGiamToiDa(BigDecimal.valueOf(voucher.getGiaTriGiamToiDa() != null ? voucher.getGiaTriGiamToiDa() : 0))
                    .giaTriGiam(BigDecimal.valueOf(voucher.getGiaTriGiam() != null ? voucher.getGiaTriGiam() : 0))
                    .giaTriGiamToiThieu(BigDecimal.valueOf(voucher.getGiaTriGiamToiThieu() != null ? voucher.getGiaTriGiamToiThieu() : 0))
                    .giaTriDonHangToiThieu(BigDecimal.valueOf(voucher.getGiaTriGiamToiThieu() != null ? voucher.getGiaTriGiamToiThieu() : 0))
                    .soLuong(voucher.getSoLuong() != null ? voucher.getSoLuong() : 0)
                    .ngayBatDau(voucher.getNgayBatDau())
                    .ngayKetThuc(voucher.getNgayKetThuc())
                    .ngayTao(voucher.getNgayTao())
                    .ngayCapNhat(voucher.getNgayCapNhat())
                    .daHetHan(daHetHan)
                    .daHetSoLuong(daHetSoLuong)
                    .soLuongConLai(voucher.getSoLuong() != null ? voucher.getSoLuong() : 0)
                    .khaDung(!daHetHan && !daHetSoLuong && voucher.getTrangThai() != null && voucher.getTrangThai() == 1)
                    .build();

        } catch (Exception e) {
            log.error("❌ Error mapping voucher {}: {}", voucher.getId(), e.getMessage());
            return null;
        }
    }

    @Override
    public List<VoucherResponse> layDanhSachVoucherKhaDung(Integer khachHangId, Double tongTien) {
        try {
            log.info("🎫 Lấy voucher khả dụng: khachHangId={}, tongTien={}", khachHangId, tongTien);

            Date currentDate = new Date();
            List<Voucher> vouchers = null;

            // ✅ SỬA: Xử lý case tongTien = null
            if (tongTien == null || tongTien <= 0) {
                log.info("📋 Lấy tất cả voucher khả dụng (không filter theo tổng tiền)");
                vouchers = voucherRepository.findAllAvailableVouchers(currentDate);
            } else {
                log.info("📋 Lấy voucher khả dụng cho tổng tiền: {}", tongTien);
                vouchers = voucherRepository.findAvailableVouchers(currentDate, tongTien);
            }

            log.info("📋 Found {} vouchers from database", vouchers.size());

            // ✅ Debug log chi tiết
            for (Voucher v : vouchers) {
                log.debug("Voucher: {} - Giảm: {} - Tối thiểu: {} - HSD: {}",
                        v.getTenVoucher(), v.getGiaTriGiam(), v.getGiaTriGiamToiThieu(), v.getNgayKetThuc());
            }

            List<VoucherResponse> responses = vouchers.stream()
                    .filter(voucher -> {
                        // ✅ SỬA: Lọc thêm theo logic business
                        if (voucher == null || !voucher.isValid()) {
                            return false;
                        }

                        // ✅ SỬA: Kiểm tra giá trị đơn hàng tối thiểu nếu có tongTien
                        if (tongTien != null && tongTien > 0 && tongTien < voucher.getGiaTriGiamToiThieu()) {
                            log.debug("Voucher {} bị loại: tongTien {} < minimum {}",
                                    voucher.getTenVoucher(), tongTien, voucher.getGiaTriGiamToiThieu());
                            return false;
                        }

                        return true;
                    })
                    .map(this::mapToVoucherResponseSafe)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            log.info("✅ Returning {} applicable vouchers", responses.size());
            return responses;

        } catch (Exception e) {
            log.error("❌ Error loading vouchers: {}", e.getMessage(), e);
            return new ArrayList<>(); // Return empty list instead of throwing
        }
    }

    @Override
    public VoucherValidationResponse kiemTraVoucher(ValidateVoucherRequest request) {
        try {
            log.info("✅ Kiểm tra voucher: {}", request.getMaVoucher());

            Optional<Voucher> voucherOpt = voucherRepository.findByMaVoucher(request.getMaVoucher());

            if (!voucherOpt.isPresent()) {
                return VoucherValidationResponse.builder()
                        .valid(false)
                        .message("Voucher không tồn tại")
                        .lyDoKhongHopLe("Mã voucher không hợp lệ")
                        .build();
            }

            Voucher voucher = voucherOpt.get();
            Date currentDate = new Date();

            // Kiểm tra các điều kiện
            if (!voucher.isActive()) {
                return VoucherValidationResponse.builder()
                        .valid(false)
                        .message("Voucher không hoạt động")
                        .lyDoKhongHopLe("Voucher đã bị vô hiệu hóa")
                        .build();
            }

            if (voucher.getSoLuong() <= 0) {
                return VoucherValidationResponse.builder()
                        .valid(false)
                        .message("Voucher đã hết lượt sử dụng")
                        .lyDoKhongHopLe("Voucher đã hết số lượng")
                        .build();
            }

            if (currentDate.before(voucher.getNgayBatDau()) || currentDate.after(voucher.getNgayKetThuc())) {
                return VoucherValidationResponse.builder()
                        .valid(false)
                        .message("Voucher không trong thời gian sử dụng")
                        .lyDoKhongHopLe("Voucher ngoài thời gian hiệu lực")
                        .build();
            }

            Double tongTien = request.getTongTien() != null ? request.getTongTien() : 0.0;
            if (tongTien < voucher.getGiaTriGiamToiThieu()) {
                return VoucherValidationResponse.builder()
                        .valid(false)
                        .message("Đơn hàng chưa đủ giá trị tối thiểu")
                        .lyDoKhongHopLe("Cần tối thiểu " + formatPrice(voucher.getGiaTriGiamToiThieu()))
                        .build();
            }

            // Tính giá trị giảm
            Double giaTriGiam = voucher.tinhGiaTriGiam(tongTien);

            return VoucherValidationResponse.builder()
                    .valid(true)
                    .message("Voucher hợp lệ")
                    .voucher(mapToVoucherResponseSafe(voucher))
                    .giaTriGiam(giaTriGiam)
                    .build();

        } catch (Exception e) {
            log.error("❌ Error validating voucher: {}", e.getMessage(), e);
            return VoucherValidationResponse.builder()
                    .valid(false)
                    .message("Lỗi hệ thống")
                    .lyDoKhongHopLe("Không thể kiểm tra voucher: " + e.getMessage())
                    .build();
        }
    }

    // Helper method
    private String formatPrice(Double price) {
        if (price == null) return "0₫";
        return String.format("%,.0f₫", price);
    }

    @Override
    @Transactional
    public HoaDonChoTongQuanResponse apDungVoucher(Integer hoaDonId, Integer voucherId) {
        try {
            log.info("Áp dụng voucher ID: {} cho hóa đơn ID: {}", voucherId, hoaDonId);

            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

            Voucher voucher = voucherRepository.findById(voucherId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher"));

            // Validate voucher như cũ...
            Date currentDate = new Date();
            if (!voucher.isValid()) throw new RuntimeException("Voucher không hợp lệ");
            if (voucher.getSoLuong() <= 0) throw new RuntimeException("Voucher đã hết lượt sử dụng");

            BigDecimal tongTienHoaDon = hoaDonChiTietRepository.calculateTotalAmountByHoaDonId(hoaDonId);
            if (tongTienHoaDon.doubleValue() < voucher.getGiaTriGiamToiThieu()) {
                throw new RuntimeException("Đơn hàng chưa đủ giá trị tối thiểu: " +
                        formatMoney(BigDecimal.valueOf(voucher.getGiaTriGiamToiThieu())));
            }

            // KHÔNG tạo chi_tiet_voucher ở đây nữa.
            // Chỉ cập nhật ngày và return tổng quan.
            hoaDon.setNgayCapNhat(new Date());
            hoaDonRepository.save(hoaDon);

            capNhatTongTienHoaDon(hoaDonId);
            return layTongQuanHoaDonCho(hoaDonId);

        } catch (Exception e) {
            log.error("Lỗi khi áp dụng voucher: {}", e.getMessage());
            throw new RuntimeException("Không thể áp dụng voucher: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public HoaDonChoTongQuanResponse boVoucher(Integer hoaDonId) {
        try {
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

            // hoaDon.setVoucherId(null);
            hoaDon.setNgayCapNhat(new Date());
            hoaDonRepository.save(hoaDon);

            // Cập nhật tổng tiền
            capNhatTongTienHoaDon(hoaDonId);

            return layTongQuanHoaDonCho(hoaDonId);

        } catch (Exception e) {
            log.error("Lỗi khi bỏ voucher: {}", e.getMessage());
            throw new RuntimeException("Không thể bỏ voucher: " + e.getMessage());
        }
    }

    // ===== THANH TOÁN =====

    @Override
    @Transactional
    public HoaDonResponse thanhToanHoaDon(Integer hoaDonId, ThanhToanRequest request) {
        try {
            log.info("💰 Thanh toán hóa đơn ID: {} với phương thức: {}", hoaDonId, request.getPhuongThucThanhToan());

            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

            if (!"CHO".equals(hoaDon.getTrangThaiHoaDon())) {
                throw new RuntimeException("Hóa đơn không ở trạng thái chờ thanh toán");
            }

            // Kiểm tra tồn kho trước khi thanh toán
            List<InventoryCheckResponse> inventoryChecks = kiemTraTonKho(hoaDonId);
            boolean hasError = inventoryChecks.stream().anyMatch(check -> !check.getCoTheban());
            if (hasError) {
                throw new RuntimeException("Có sản phẩm không đủ tồn kho để thanh toán");
            }

            // ===== XỬ LÝ PHƯƠNG THỨC THANH TOÁN =====
            BigDecimal tongTienCanThanhToan = hoaDon.getTongThanhToan();

            // Kiểm tra và xử lý từng phương thức thanh toán
            if ("TIEN_MAT".equals(request.getPhuongThucThanhToan())) {
                xuLyThanhToanTienMat(request, tongTienCanThanhToan);

            } else if ("CHUYEN_KHOAN".equals(request.getPhuongThucThanhToan())) {
                xuLyThanhToanChuyenKhoan(request, tongTienCanThanhToan);

            } else if ("KET_HOP".equals(request.getPhuongThucThanhToan())) {
                xuLyThanhToanKetHop(request, tongTienCanThanhToan);

            } else {
                throw new RuntimeException("Phương thức thanh toán không hợp lệ: " + request.getPhuongThucThanhToan());
            }

            // Cập nhật thông tin khách hàng nếu có
            if (request.getKhachHangId() != null) {
                KhachHang khachHang = khachHangRepository.findById(request.getKhachHangId()).orElse(null);
                if (khachHang != null) {
                    hoaDon.setKhachHang(khachHang);

                    // Lấy email từ TaiKhoan
                    if (khachHang.getTaiKhoan() != null && StringUtils.hasText(khachHang.getTaiKhoan().getEmail())) {
                        hoaDon.setEmail(khachHang.getTaiKhoan().getEmail());
                    } else {
                        hoaDon.setEmail("");
                    }

                    hoaDon.setSdt(khachHang.getSdt());
                    hoaDon.setTenNguoiDung(khachHang.getHoTen());

                    // Lấy địa chỉ từ TaiKhoan -> DiaChi
                    if (khachHang.getTaiKhoan() != null) {
                        Optional<DiaChi> diaChiOpt = diaChiRepository.findByTaiKhoanIdAndIsDefaultTrue(khachHang.getTaiKhoan().getId());
                        if (diaChiOpt.isPresent()) {
                            DiaChi diaChi = diaChiOpt.get();
                            String diaChiDayDu = diaChi.getDiaChiChiTiet() + ", " +
                                    diaChi.getTenPhuong() + ", " +
                                    diaChi.getTenTinh();
                            hoaDon.setDiaChi(diaChiDayDu);
                        }
                    }
                }
            }

            // Cập nhật thông tin thanh toán
            hoaDon.setTrangThaiHoaDon("COMPLETED");
            hoaDon.setLoaiHoaDon(request.getLoaiHoaDon());
            hoaDon.setPhuongThucThanhToan(request.getPhuongThucThanhToan()); // Lưu phương thức thanh toán
            hoaDon.setNgayHoanThanh(new Date());
            hoaDon.setNgayCapNhat(new Date());
            hoaDon.setGhiChu(request.getGhiChu());
            hoaDon.setDiemSuDung(request.getDiemSuDung());

            // Áp dụng điểm tích lũy nếu có
            if (request.getDiemSuDung() != null && request.getDiemSuDung() > 0) {
                xuLyDiemTichLuy(hoaDon.getKhachHang(), request.getDiemSuDung());
            }

            // Cập nhật tổng tiền
            capNhatTongTienHoaDon(hoaDonId);

            // Cập nhật trạng thái chi tiết hóa đơn
            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
            for (HoaDonChiTiet chiTiet : chiTiets) {
                chiTiet.setTrangThaiHoaDon("COMPLETED");
                hoaDonChiTietRepository.save(chiTiet);
            }

            HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

            // SAU khi thanh toán thành công và set trạng thái "ĐÃ THANH TOÁN", mới tạo chi_tiet_voucher
            if (request.getVoucherId() != null) {
                Voucher voucher = voucherRepository.findById(request.getVoucherId()).orElse(null);
                if (voucher != null) {
                    // Tính lại tổng gốc tại thời điểm thanh toán
                    BigDecimal tongTienHoaDon = hoaDonChiTietRepository.calculateTotalAmountByHoaDonId(hoaDonId);

                    // Tính số tiền giảm (dùng hàm của entity Voucher)
                    Double giaTriGiam = voucher.tinhGiaTriGiam(tongTienHoaDon.doubleValue());
                    BigDecimal soTienGiam = BigDecimal.valueOf(giaTriGiam);
                    BigDecimal thanhTienSauGiam = tongTienHoaDon.subtract(soTienGiam);
                    if (thanhTienSauGiam.compareTo(BigDecimal.ZERO) < 0) thanhTienSauGiam = BigDecimal.ZERO;

                    // Tạo chi tiết voucher
                    taoChiTietVoucherNeuChuaCo(savedHoaDon, voucher, tongTienHoaDon, soTienGiam, thanhTienSauGiam);

                    // Giảm số lượng voucher
                    voucher.setSoLuong(voucher.getSoLuong() - 1);
                    voucherRepository.save(voucher);
                }
            }

            // Tạo lịch sử hóa đơn
            String moTaThanhToan = taoMoTaThanhToan(request);
            taoLichSuHoaDon(savedHoaDon, moTaThanhToan, savedHoaDon.getNhanVien());

            // Cộng điểm cho khách hàng nếu có
            if (savedHoaDon.getKhachHang() != null) {
                congDiemKhachHang(savedHoaDon.getKhachHang().getId(), savedHoaDon.getTongThanhToan().doubleValue());
            }

            log.info("✅ Thanh toán hóa đơn thành công ID: {} - Phương thức: {}", hoaDonId, request.getPhuongThucThanhToan());

            return mapToHoaDonResponse(savedHoaDon);

        } catch (Exception e) {
            log.error("❌ Lỗi khi thanh toán hóa đơn: {}", e.getMessage());
            throw new RuntimeException("Không thể thanh toán hóa đơn: " + e.getMessage());
        }
    }

    private void taoChiTietVoucherNeuChuaCo(HoaDon hoaDon, Voucher voucher,
                                            BigDecimal giaTriDonHang, BigDecimal soTienGiam, BigDecimal thanhTien) {
        try {
            // Kiểm tra đã có chi tiết voucher chưa (idempotency)
            boolean existed = chiTietVoucherService.existsByHoaDonIdAndVoucherId(hoaDon.getId(), voucher.getId());
            if (existed) return; // Đã có -> không tạo trùng

            ChiTietVoucherDTO chiTietVoucherDTO = ChiTietVoucherDTO.builder()
                    .maChiTietVoucher("CTV" + hoaDon.getMaHoaDon() + "_" + System.currentTimeMillis())
                    .hoaDonId(hoaDon.getId())
                    .voucherId(voucher.getId())

                    // Snapshot voucher tại thời điểm thanh toán
                    .maVoucher(voucher.getMaVoucher())
                    .tenVoucher(voucher.getTenVoucher())
                    .loaiGiamGia(voucher.getLoaiGiamGia())
                    .giaTriGiam(voucher.getGiaTriGiam())
                    .giaTriGiamToiDa(voucher.getGiaTriGiamToiDa())
                    .giaTriGiamToiThieu(voucher.getGiaTriGiamToiThieu())

                    // Số liệu cuối cùng
                    .giaTriDonHang(giaTriDonHang)
                    .soTienGiam(soTienGiam)
                    .thanhTien(thanhTien)
                    .ngayApDung(new Date())
                    .build();

            chiTietVoucherService.create(chiTietVoucherDTO);

        } catch (Exception ex) {
            log.warn("Không thể tạo chi tiết voucher cho hóa đơn {}: {}", hoaDon.getId(), ex.getMessage());
            // Không throw để không rollback thanh toán
        }
    }

    @Override
    public List<InventoryCheckResponse> kiemTraTonKho(Integer hoaDonId) {
        try {
            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
            List<InventoryCheckResponse> responses = new ArrayList<>();

            for (HoaDonChiTiet chiTiet : chiTiets) {
                ChiTietSanPham sanPham = chiTiet.getChiTietSanPham(); // Sử dụng relationship
                if (sanPham != null) {
                    boolean coTheBan = sanPham.getSoLuong() >= 0; // Đã trừ khi thêm vào hóa đơn
                    String thongBao = coTheBan ? "Đủ hàng" : "Đã bán hết";

                    // Lấy thông tin sản phẩm, màu sắc, kích cỡ
                    SanPham sp = sanPham.getSanPham(); // Sử dụng relationship nếu có
                    MauSac mauSac = sanPham.getMauSac(); // Sử dụng relationship nếu có
                    KichCo kichCo = sanPham.getKichCo(); // Sử dụng relationship nếu có

                    responses.add(InventoryCheckResponse.builder()
                            .chiTietSanPhamId(sanPham.getId())
                            .tenSanPham(sp != null ? sp.getTenSanPham() : "N/A")
                            .mauSac(mauSac != null ? mauSac.getTenMauSac() : "N/A")
                            .kichCo(kichCo != null ? kichCo.getTenKichCo() : "N/A")
                            .soLuongTon(sanPham.getSoLuong())
                            .coTheban(coTheBan)
                            .thongBao(thongBao)
                            .build());
                }
            }

            return responses;

        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra tồn kho: {}", e.getMessage());
            throw new RuntimeException("Không thể kiểm tra tồn kho");
        }
    }

    // ===== THỐNG KÊ =====

    @Override
    public Map<String, Object> layThongKeBanHangTrongNgay() {
        try {
            Date today = new Date();
            Date startOfDay = getStartOfDay(today);
            Date endOfDay = getEndOfDay(today);

            Long soLuongHoaDon = hoaDonRepository.countByNgayTaoBetween(startOfDay, endOfDay);
            Double doanhThu = hoaDonRepository.getTongDoanhThuByNgayTao(startOfDay, endOfDay);
            Long soLuongSanPhamBan = hoaDonChiTietRepository.getTotalQuantityByDate(startOfDay, endOfDay);
//            Long soKhachHangMoi = khachHangRepository.countByNgayTaoBetween(startOfDay, endOfDay);

            Map<String, Object> thongKe = new HashMap<>();
            thongKe.put("soLuongHoaDon", soLuongHoaDon != null ? soLuongHoaDon : 0);
            thongKe.put("doanhThu", doanhThu != null ? doanhThu : 0.0);
            thongKe.put("soLuongSanPhamBan", soLuongSanPhamBan != null ? soLuongSanPhamBan : 0);
//            thongKe.put("soKhachHangMoi", soKhachHangMoi != null ? soKhachHangMoi : 0);
            thongKe.put("ngayThongKe", today);

            return thongKe;

        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê bán hàng: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy thống kê bán hàng");
        }
    }

    @Override
    public List<Map<String, Object>> laySanPhamBanChay(int limit) {
        try {
            // Query để lấy sản phẩm bán chạy nhất
            List<Map<String, Object>> results = new ArrayList<>();

            // Giả lập dữ liệu - có thể implement query phức tạp sau
            Map<String, Object> item1 = new HashMap<>();
            item1.put("tenSanPham", "Giày Nike Air Max");
            item1.put("soLuongBan", 100);
            item1.put("doanhThu", 15000000.0);
            results.add(item1);

            Map<String, Object> item2 = new HashMap<>();
            item2.put("tenSanPham", "Giày Adidas Ultraboost");
            item2.put("soLuongBan", 85);
            item2.put("doanhThu", 12750000.0);
            results.add(item2);

            return results.stream().limit(limit).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Lỗi khi lấy sản phẩm bán chạy: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy sản phẩm bán chạy");
        }
    }

    @Override
    public Map<String, Object> layThongKeDoanhThu(String tuNgay, String denNgay) {
        try {
            Map<String, Object> thongKe = new HashMap<>();

            // Giả lập dữ liệu - có thể implement query phức tạp sau
            thongKe.put("tuNgay", tuNgay);
            thongKe.put("denNgay", denNgay);
            thongKe.put("tongDoanhThu", 50000000.0);
            thongKe.put("soLuongDonHang", 120);
            thongKe.put("doanhThuTrungBinh", 416666.67);

            return thongKe;

        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê doanh thu: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy thống kê doanh thu");
        }
    }

    // ===== UTILITY METHODS =====

    private void capNhatTongTienHoaDon(Integer hoaDonId) {
        try {
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
            if (hoaDon == null) return;

            BigDecimal tongTien = hoaDonChiTietRepository.calculateTotalAmountByHoaDonId(hoaDonId);
            BigDecimal tongThanhToan = tongTien;

            // Áp dụng voucher nếu có (cần implement logic voucher)
            // if (hoaDon.getVoucherId() != null) {
            //     Voucher voucher = voucherRepository.findById(hoaDon.getVoucherId()).orElse(null);
            //     if (voucher != null) {
            //         Double giaTriGiam = tinhGiaTriGiamVoucher(voucher, tongTien.doubleValue());
            //         tongThanhToan = tongTien.subtract(BigDecimal.valueOf(giaTriGiam));
            //     }
            // }

            // Áp dụng điểm tích lũy nếu có
            if (hoaDon.getDiemSuDung() != null && hoaDon.getDiemSuDung() > 0) {
                BigDecimal giaTriDiem = BigDecimal.valueOf(hoaDon.getDiemSuDung() * 1000); // 1 điểm = 1000 VND
                tongThanhToan = tongThanhToan.subtract(giaTriDiem);
            }

            if (tongThanhToan.compareTo(BigDecimal.ZERO) < 0) {
                tongThanhToan = BigDecimal.ZERO;
            }

            hoaDon.setTongTien(tongTien);
            hoaDon.setTongThanhToan(tongThanhToan);
            hoaDon.setNgayCapNhat(new Date());
            hoaDonRepository.save(hoaDon);

        } catch (Exception e) {
            log.error("Lỗi khi cập nhật tổng tiền hóa đơn: {}", e.getMessage());
        }
    }

    private Double tinhGiaTriGiamVoucher(Voucher voucher, Double tongTien) {
        if ("PHAN_TRAM".equals(voucher.getLoaiGiamGia())) {
            Double giam = tongTien * voucher.getGiaTriGiamToiDa() / 100;
            return Math.min(giam, voucher.getGiaTriGiamToiDa());
        } else {
            return Math.min(voucher.getGiaTriGiamToiDa(), tongTien);
        }
    }

    private void taoLichSuHoaDon(HoaDon hoaDon, String moTa, NhanVien nhanVien) {
        try {
            LichSuHoaDon lichSu = new LichSuHoaDon();

            // SỬA: Set object thay vì ID
            lichSu.setHoaDon(hoaDon);           // Thay vì setIdHoaDon(hoaDon.getId())
            lichSu.setNhanVien(nhanVien);       // Thay vì setIdNhanVien(nhanVien.getId())

            lichSu.setMoTaHanhDong(moTa);
            lichSu.setTrangThaiHoaDon(hoaDon.getTrangThaiHoaDon());
            lichSu.setNgayTao(new Date());
            lichSu.setNgayCapNhat(new Date());
            lichSuHoaDonRepository.save(lichSu);

        } catch (Exception e) {
            log.warn("Không thể tạo lịch sử hóa đơn: {}", e.getMessage());
        }
    }

    private void congDiemKhachHang(Integer khachHangId, Double tongTien) {
        try {
            KhachHang khachHang = khachHangRepository.findById(khachHangId).orElse(null);
            if (khachHang != null && khachHang.getViDiem() != null) {
                ViDiem viDiem = khachHang.getViDiem();
                Double diemCong = Math.floor(tongTien / 100000); // 1 điểm cho mỗi 100k
                viDiem.setTongDiem(viDiem.getTongDiem() + diemCong);
                viDiem.setSoDiemDaCong(viDiem.getSoDiemDaCong() + diemCong);
                viDiem.setNgayCapNhat(new Date());
                viDiemRepository.save(viDiem);
            }
        } catch (Exception e) {
            log.warn("Không thể cộng điểm cho khách hàng: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<KhuyenMaiSanPhamResponse> layKhuyenMaiSanPham(Integer chiTietSanPhamId) {
        try {
            Date currentDate = new Date();
            List<KhuyenMaiChiTiet> khuyenMaiChiTiets = khuyenMaiChiTietRepository
                    .findKhuyenMaiHienTaiByChiTietSanPham(chiTietSanPhamId, currentDate);

            return khuyenMaiChiTiets.stream()
                    .map(kmct -> {
                        KhuyenMai km = kmct.getKhuyenMai();

                        if (km != null) {
                            return KhuyenMaiSanPhamResponse.builder()
                                    .id(km.getId())
                                    .maKhuyenMai(km.getMaKhuyenMai())
                                    .tenKhuyenMai(km.getTenKhuyenMai())
                                    .ngayBatDau(km.getNgayBatDau())
                                    .ngayKetThuc(km.getNgayKetThuc())
                                    .giaTri(km.getGiaTri())
                                    .loaiKhuyenMai("PHAN_TRAM")
                                    .trangThai(km.getTrangThai())
                                    .dangApDung(true)
                                    .build();
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Không thể lấy khuyến mãi sản phẩm: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private BigDecimal convertToBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Double) return BigDecimal.valueOf((Double) value);
        if (value instanceof Integer) return BigDecimal.valueOf((Integer) value);
        if (value instanceof Float) return BigDecimal.valueOf((Float) value);
        if (value instanceof Long) return BigDecimal.valueOf((Long) value);
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        throw new IllegalArgumentException("Cannot convert " + value.getClass() + " to BigDecimal");
    }

    private String generateMaHoaDon() {
        return "HD" + System.currentTimeMillis();
    }

    private String generateMaKhachHang() {
        return "KH" + System.currentTimeMillis();
    }

    private Date getStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date getEndOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    // ===== MAPPING METHODS =====

    private HoaDonChoResponse mapToHoaDonChoResponse(HoaDon hoaDon) {
        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDon.getId());

        int soLuongSanPham = chiTiets.size();
        BigDecimal tongTien = chiTiets.stream()
                .map(ct -> BigDecimal.valueOf(ct.getGia()).multiply(BigDecimal.valueOf(ct.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Lấy thông tin khách hàng - SỬ DỤNG RELATIONSHIP
        KhachHangResponse khachHangResponse = null;
        if (hoaDon.getKhachHang() != null) {
            khachHangResponse = mapToKhachHangResponse(hoaDon.getKhachHang());
        }

        return HoaDonChoResponse.builder()
                .id(hoaDon.getId())
                .maHoaDon(hoaDon.getMaHoaDon())
                .ngayTao(hoaDon.getNgayTao())
                .tongTien(tongTien)
                .soLuongSanPham(soLuongSanPham)
                .trangThai(hoaDon.getTrangThaiHoaDon())
                .khachHang(khachHangResponse)
                .chiTiets(chiTiets.stream().map(this::mapToHoaDonChiTietResponse).collect(Collectors.toList()))
                .build();
    }

    private HoaDonChoDetailResponse mapToHoaDonChoDetailResponse(HoaDon hoaDon, List<HoaDonChiTiet> chiTiets) {
        // Lấy thông tin nhân viên
        NhanVienResponse nhanVienResponse = null;
        if (hoaDon.getNhanVien() != null) {
            nhanVienResponse = mapToNhanVienResponse(hoaDon.getNhanVien());
        }

        // Lấy thông tin khách hàng
        KhachHangResponse khachHangResponse = null;
        if (hoaDon.getKhachHang() != null) {
            khachHangResponse = mapToKhachHangResponse(hoaDon.getKhachHang());
        }

        // Tạo danh sách sản phẩm
        List<HoaDonChoSanPhamResponse> danhSachSanPham = chiTiets.stream()
                .map(this::mapToHoaDonChoSanPhamResponse)
                .collect(Collectors.toList());

        // Tạo thông tin tổng quan
        HoaDonChoTongQuanResponse tongQuan = mapToHoaDonChoTongQuanResponse(hoaDon, chiTiets);

        return HoaDonChoDetailResponse.builder()
                .id(hoaDon.getId())
                .maHoaDon(hoaDon.getMaHoaDon())
                .trangThai(hoaDon.getTrangThaiHoaDon())
                .ngayTao(hoaDon.getNgayTao())
                .ngayCapNhat(hoaDon.getNgayCapNhat())
                .nhanVien(nhanVienResponse)
                .khachHang(khachHangResponse)
                .voucher(null) // Implement voucher mapping later
                .daApDungVoucher(false) // Implement voucher logic later
                .danhSachSanPham(danhSachSanPham)  // SỬA: Sử dụng danhSachSanPham thay vì chiTiets
                .tongQuan(tongQuan)                // SỬA: Thêm thông tin tổng quan
                .ghiChu(hoaDon.getGhiChu())
                .build();
    }

    private HoaDonChoSanPhamResponse mapToHoaDonChoSanPhamResponse(HoaDonChiTiet chiTiet) {
        ChiTietSanPham sanPham = chiTiet.getChiTietSanPham();

        if (sanPham != null) {
            SanPham sp = sanPham.getSanPham();
            MauSac mauSac = sanPham.getMauSac();
            KichCo kichCo = sanPham.getKichCo();

            // Lấy khuyến mãi
            List<KhuyenMaiSanPhamResponse> khuyenMais = layKhuyenMaiSanPham(sanPham.getId());

            // Lấy hình ảnh chính - SỬA: Format URL đúng cách
            String hinhAnhChinh = null;
            Optional<HinhAnh> hinhAnhOpt = hinhAnhRepository.findActiveImageByChiTietSanPhamId(sanPham.getId());
            if (hinhAnhOpt.isPresent()) {
                HinhAnh hinhAnh = hinhAnhOpt.get();
                String duongDan = hinhAnh.getDuongDan();
                if (duongDan != null && !duongDan.isEmpty()) {
                    String cleanPath = duongDan
                            .replace("/images/", "")
                            .replace("/hinh-anh/images/", "");
                    hinhAnhChinh = "http://localhost:8080/hinh-anh/images/" + cleanPath;
                }
            }

            // Tính giá
            BigDecimal giaGoc = sanPham.getGiaGoc() != null ? BigDecimal.valueOf(sanPham.getGiaGoc()) : BigDecimal.ZERO;
            BigDecimal giaBan = BigDecimal.valueOf(chiTiet.getGia());
            int soLuong = chiTiet.getSoLuong();

            BigDecimal tongTienGoc = giaGoc.multiply(BigDecimal.valueOf(soLuong));
            BigDecimal tongTienSauGiam = giaBan.multiply(BigDecimal.valueOf(soLuong));
            BigDecimal soTienTietKiem = tongTienGoc.subtract(tongTienSauGiam);

            Float phanTramGiam = tongTienGoc.compareTo(BigDecimal.ZERO) > 0 ?
                    soTienTietKiem.divide(tongTienGoc, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).floatValue() : 0f;

            return HoaDonChoSanPhamResponse.builder()
                    .id(chiTiet.getId())
                    .chiTietSanPhamId(sanPham.getId())
                    .tenSanPham(sp != null ? sp.getTenSanPham() : "N/A")
                    .maSanPham(sp != null ? sanPham.getMaChiTiet() : "N/A")
                    .mauSac(mauSac != null ? mauSac.getTenMauSac() : "N/A")
                    .kichCo(kichCo != null ? kichCo.getTenKichCo() : "N/A")
                    .soLuong(soLuong)
                    .giaGoc(giaGoc)
                    .giaBan(giaBan)
                    .tongTienGoc(tongTienGoc)
                    .tongTienSauGiam(tongTienSauGiam)
                    .soTienTietKiem(soTienTietKiem)
                    .khuyenMaiSanPham(khuyenMais)
                    .hinhAnhChinh(hinhAnhChinh) // SỬA: Thêm lại field này
                    .phanTramGiam(phanTramGiam) // SỬA: Thêm lại field này
                    .build();
        }

        return null;
    }

    private HoaDonChoTongQuanResponse mapToHoaDonChoTongQuanResponse(HoaDon hoaDon, List<HoaDonChiTiet> chiTiets) {
        int soLuongSanPham = chiTiets.size();
        int tongSoLuong = chiTiets.stream().mapToInt(HoaDonChiTiet::getSoLuong).sum();

        BigDecimal tongTienGoc = BigDecimal.ZERO;
        BigDecimal tongTienKhuyenMai = BigDecimal.ZERO;

        List<HoaDonChoSanPhamResponse> danhSachSanPham = new ArrayList<>();

        for (HoaDonChiTiet chiTiet : chiTiets) {
            // SỬA: Sử dụng relationship thay vì getIdCtsp()
            ChiTietSanPham sanPham = chiTiet.getChiTietSanPham();
            if (sanPham != null) {
                BigDecimal giaGoc = sanPham.getGiaGoc() != null ? BigDecimal.valueOf(sanPham.getGiaGoc()) : BigDecimal.ZERO;
                BigDecimal giaBan = BigDecimal.valueOf(chiTiet.getGia());
                int soLuong = chiTiet.getSoLuong();

                BigDecimal tongTienGocSP = giaGoc.multiply(BigDecimal.valueOf(soLuong));
                BigDecimal tongTienSauGiam = giaBan.multiply(BigDecimal.valueOf(soLuong));
                BigDecimal soTienTietKiem = tongTienGocSP.subtract(tongTienSauGiam);

                tongTienGoc = tongTienGoc.add(tongTienGocSP);
                tongTienKhuyenMai = tongTienKhuyenMai.add(tongTienSauGiam);

                Float phanTramGiam = tongTienGocSP.compareTo(BigDecimal.ZERO) > 0 ?
                        soTienTietKiem.divide(tongTienGocSP, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).floatValue() : 0f;

                // Lấy khuyến mãi
                List<KhuyenMaiSanPhamResponse> khuyenMais = layKhuyenMaiSanPham(sanPham.getId());

                // Lấy hình ảnh
                String hinhAnhChinh = null;
                List<HinhAnh> hinhAnhs = hinhAnhRepository.findActiveImagesByChiTietSanPhamIds(Arrays.asList(sanPham.getId()));
                if (!hinhAnhs.isEmpty()) {
                    hinhAnhChinh = hinhAnhs.get(0).getDuongDan();
                }

                // SỬA: Sử dụng relationship thay vì repository query
                SanPham sp = sanPham.getSanPham();
                MauSac mauSac = sanPham.getMauSac();
                KichCo kichCo = sanPham.getKichCo();

                danhSachSanPham.add(HoaDonChoSanPhamResponse.builder()
                        .id(chiTiet.getId())
                        .chiTietSanPhamId(sanPham.getId())
                        .tenSanPham(sp != null ? sp.getTenSanPham() : "N/A")
                        .mauSac(mauSac != null ? mauSac.getTenMauSac() : "N/A")
                        .kichCo(kichCo != null ? kichCo.getTenKichCo() : "N/A")
                        .soLuong(soLuong)
                        .giaGoc(giaGoc)
                        .giaBan(giaBan)
                        .tongTienGoc(tongTienGocSP)
                        .tongTienSauGiam(tongTienSauGiam)
                        .soTienTietKiem(soTienTietKiem)
                        .khuyenMaiSanPham(khuyenMais)  // SỬA: danhSachKhuyenMai -> khuyenMaiSanPham
                        .hinhAnhChinh(hinhAnhChinh)
                        .phanTramGiam(phanTramGiam)
                        .build());
            }
        }

        // Tính voucher (implement later)
        BigDecimal tongTienVoucher = BigDecimal.ZERO;

        BigDecimal tongTienThanhToan = tongTienKhuyenMai.subtract(tongTienVoucher);
        BigDecimal tongTietKiem = tongTienGoc.subtract(tongTienThanhToan);

        Float phanTramGiamTongCong = tongTienGoc.compareTo(BigDecimal.ZERO) > 0 ?
                tongTietKiem.divide(tongTienGoc, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).floatValue() : 0f;

        // Lấy thông tin khách hàng
        KhachHangResponse khachHangResponse = null;
        if (hoaDon.getKhachHang() != null) {
            khachHangResponse = mapToKhachHangResponse(hoaDon.getKhachHang());
        }

        return HoaDonChoTongQuanResponse.builder()
                .id(hoaDon.getId())
                .maHoaDon(hoaDon.getMaHoaDon())
                .soLuongSanPham(soLuongSanPham)
                .tongSoLuong(tongSoLuong)
                .tongTienGoc(tongTienGoc)
                .tongTienKhuyenMai(tongTienKhuyenMai)
                .tongTienVoucher(tongTienVoucher)
                .tongTienThanhToan(tongTienThanhToan)
                .tongTietKiem(tongTietKiem)
                .phanTramGiamTongCong(phanTramGiamTongCong)
                .voucher(null)
                .daApDungVoucher(false)
                .khachHang(khachHangResponse)
                .danhSachSanPham(danhSachSanPham)
                .ngayCapNhat(hoaDon.getNgayCapNhat())
                .trangThai(hoaDon.getTrangThaiHoaDon())
                .build();
    }

    private SanPhamChiTietBanHangResponse mapToSanPhamChiTietBanHangResponse(ChiTietSanPham sanPham) {
        // Lấy thông tin sản phẩm gốc
        SanPham sp = sanPham.getSanPham();
        // Lấy thuộc tính - sử dụng relationship
        MauSac mauSac = sanPham.getMauSac();
        KichCo kichCo = sanPham.getKichCo();

        // Lấy hình ảnh từ relationship (giống quản lý sản phẩm)
        List<HinhAnhResponse> danhSachHinhAnh = new ArrayList<>();
        String hinhAnhChinh = null;

        try {
            // SỬA: Lấy hình ảnh từ ChiTietSanPham -> HinhAnh relationship
            HinhAnh hinhAnhEntity = sanPham.getHinhAnh(); // Entity đã có relationship này

            if (hinhAnhEntity != null && hinhAnhEntity.getTrangThai() == 1) {
                String imageUrl = createImageUrl(hinhAnhEntity.getDuongDan());

                HinhAnhResponse hinhAnhResponse = HinhAnhResponse.builder()
                        .id(hinhAnhEntity.getId())
                        .maHinhAnh(hinhAnhEntity.getMaHinhAnh())
                        .tenHinhAnh(hinhAnhEntity.getTenHinhAnh())
                        .duongDan(hinhAnhEntity.getDuongDan())
                        .urlHinhAnh(imageUrl)
                        .trangThai(hinhAnhEntity.getTrangThai())
                        .laHinhChinh(true) // Luôn là hình chính vì chỉ có 1 hình
                        .build();

                danhSachHinhAnh.add(hinhAnhResponse);
                hinhAnhChinh = imageUrl;
            }

        } catch (Exception e) {
            log.warn("Không thể load hình ảnh cho sản phẩm {}: {}", sanPham.getId(), e.getMessage());
        }

        // Lấy khuyến mãi
        List<KhuyenMaiSanPhamResponse> khuyenMais = layKhuyenMaiSanPham(sanPham.getId());

        // Tính giá khuyến mãi - Convert Double sang BigDecimal
        BigDecimal giaGoc = sanPham.getGiaGoc() != null ? BigDecimal.valueOf(sanPham.getGiaGoc()) : BigDecimal.ZERO;
        BigDecimal giaBan = sanPham.getGiaBan() != null ? BigDecimal.valueOf(sanPham.getGiaBan()) : BigDecimal.ZERO;
        BigDecimal giaKhuyenMai = giaBan;
        BigDecimal tongTietKiem = BigDecimal.ZERO;
        Float phanTramGiam = 0f;

        for (KhuyenMaiSanPhamResponse km : khuyenMais) {
            if (Boolean.TRUE.equals(km.getDangApDung())) {
                if ("PHAN_TRAM".equals(km.getLoaiKhuyenMai())) {
                    BigDecimal giam = giaKhuyenMai.multiply(BigDecimal.valueOf(km.getGiaTri() / 100));
                    giaKhuyenMai = giaKhuyenMai.subtract(giam);
                    tongTietKiem = tongTietKiem.add(giam);
                } else {
                    BigDecimal giam = BigDecimal.valueOf(km.getGiaTri());
                    giaKhuyenMai = giaKhuyenMai.subtract(giam);
                    tongTietKiem = tongTietKiem.add(giam);
                }
            }
        }

        if (giaGoc.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalSavings = giaGoc.subtract(giaKhuyenMai);
            phanTramGiam = totalSavings.divide(giaGoc, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).floatValue();
        }

        // Lấy thông tin thuộc tính từ sản phẩm gốc
        ThuongHieuResponse thuongHieuResponse = null;
        DanhMucResponse danhMucResponse = null;
        ChatLieuResponse chatLieuResponse = null;
        DeGiayResponse deGiayResponse = null;

        if (sp != null) {
            if (sp.getThuongHieu() != null) {
                thuongHieuResponse = mapToThuongHieuResponse(sp.getThuongHieu());
            }
            if (sp.getDanhMuc() != null) {
                danhMucResponse = mapToDanhMucResponse(sp.getDanhMuc());
            }
            if (sp.getChatLieu() != null) {
                chatLieuResponse = mapToChatLieuResponse(sp.getChatLieu());
            }
            if (sp.getDeGiay() != null) {
                deGiayResponse = mapToDeGiayResponse(sp.getDeGiay());
            }
        }

        return SanPhamChiTietBanHangResponse.builder()
                .id(sanPham.getId())
                .maChiTiet(sanPham.getMaChiTiet())
                .maQR(sanPham.getMaQR())
                .tenSanPham(sp != null ? sp.getTenSanPham() : "N/A")
                .maSanPham(sp != null ? sanPham.getMaChiTiet() : "N/A")
                .soLuong(sanPham.getSoLuong())
                .giaGoc(giaGoc)
                .giaBan(giaBan)
                .giaKhuyenMai(giaKhuyenMai)
                .trangThai(sanPham.getTrangThai())
                .ngayTao(sanPham.getNgayTao())
                .ngayCapNhat(sanPham.getNgayCapNhat())
                .maSanPham(sp != null ? sp.getMaSanPham() : "")
                .sanPham(sp != null ? mapToSanPhamInfoResponse(sp) : null)
                .mauSac(mauSac != null ? mapToMauSacResponse(mauSac) : null)
                .kichCo(kichCo != null ? mapToKichCoResponse(kichCo) : null)
                .thuongHieu(thuongHieuResponse)
                .danhMuc(danhMucResponse)
                .chatLieu(chatLieuResponse)
                .deGiay(deGiayResponse)
                .danhSachHinhAnh(danhSachHinhAnh) // SỬA: Sử dụng List thay vì single object
                .hinhAnhChinh(hinhAnhChinh) // THÊM: URL hình ảnh chính
                .danhSachKhuyenMai(khuyenMais)
                .tongTietKiem(tongTietKiem)
                .phanTramGiam(phanTramGiam)
                .coKhuyenMai(!khuyenMais.isEmpty())
                .conHang(sanPham.getSoLuong() > 0)
                .tinhTrangKho(sanPham.getSoLuong() > 10 ? "Còn hàng" :
                        sanPham.getSoLuong() > 0 ? "Sắp hết hàng" : "Hết hàng")
                .build();
    }

    /**
     * Tạo URL hình ảnh giống quản lý sản phẩm
     */
    private String createImageUrl(String duongDan) {
        if (duongDan == null || duongDan.isEmpty()) {
            return null;
        }

        // Clean path - loại bỏ tất cả prefix
        String cleanPath = duongDan;
        if (cleanPath.startsWith("/hinh-anh/images/")) {
            cleanPath = cleanPath.replace("/hinh-anh/images/", "");
        } else if (cleanPath.startsWith("/images/")) {
            cleanPath = cleanPath.replace("/images/", "");
        }

        // Tạo URL đầy đủ
        return "http://localhost:8080/hinh-anh/images/" + cleanPath;
    }

    private ScanQRResponse mapToScanQRResponse(ChiTietSanPham sanPham) {
        List<KhuyenMaiSanPhamResponse> khuyenMais = layKhuyenMaiSanPham(sanPham.getId());

        String hinhAnhChinh = null;
        Optional<HinhAnh> hinhAnh = hinhAnhRepository.findActiveImageByChiTietSanPhamId(sanPham.getId());
        if (hinhAnh.isPresent()) {
            hinhAnhChinh = hinhAnh.get().getDuongDan();
        }

        // Lấy thông tin sản phẩm, màu sắc, kích cỡ
        SanPham sp = sanPham.getSanPham();
        MauSac mauSac = sanPham.getMauSac();
        KichCo kichCo = sanPham.getKichCo();

        return ScanQRResponse.builder()
                .chiTietSanPhamId(sanPham.getId())
                .maChiTiet(sanPham.getMaChiTiet())
                .maQR(sanPham.getMaQR())
                .tenSanPham(sp != null ? sp.getTenSanPham() : "N/A")
                .mauSac(mauSac != null ? mauSac.getTenMauSac() : "N/A")
                .kichCo(kichCo != null ? kichCo.getTenKichCo() : "N/A")
                .soLuongTon(sanPham.getSoLuong())
                .giaGoc(sanPham.getGiaGoc() != null ? BigDecimal.valueOf(sanPham.getGiaGoc()) : BigDecimal.ZERO)
                .giaBan(sanPham.getGiaBan() != null ? BigDecimal.valueOf(sanPham.getGiaBan()) : BigDecimal.ZERO)
                .hinhAnhChinh(hinhAnhChinh)
                .conHang(sanPham.getSoLuong() > 0)
                .tinhTrangKho(sanPham.getSoLuong() > 10 ? "Còn hàng" :
                        sanPham.getSoLuong() > 0 ? "Sắp hết hàng" : "Hết hàng")
                .danhSachKhuyenMai(khuyenMais)
                .build();
    }

    private KhachHangResponse mapToKhachHangResponse(KhachHang khachHang) {
        Double diemTichLuy = 0.0;
        if (khachHang.getViDiem() != null) {
            ViDiem viDiem = khachHang.getViDiem();
            diemTichLuy = viDiem.getTongDiem() - viDiem.getSoDiemDaDung();
        }

        // ✅ SỬA: Lấy email từ TaiKhoan với xử lý lỗi an toàn
        String email = "";
        try {
            if (khachHang.getTaiKhoan() != null && StringUtils.hasText(khachHang.getTaiKhoan().getEmail())) {
                email = khachHang.getTaiKhoan().getEmail();
            }
        } catch (Exception e) {
            log.warn("Không thể lấy email cho khách hàng ID: {}", khachHang.getId());
            email = ""; // Fallback về empty string
        }

        return KhachHangResponse.builder()
                .id(khachHang.getId())
                .maKhachHang(khachHang.getMaKhachHang())
                .hoTen(khachHang.getHoTen())
                .sdt(khachHang.getSdt())
                .email(email) // ✅ SỬA: Sử dụng email từ TaiKhoan với xử lý lỗi
                .trangThai(khachHang.getTrangThai())
                .diemTichLuy(diemTichLuy)
                .ngayTao(khachHang.getNgayTao())
                .build();
    }

    private KhachHangDetailResponse mapToKhachHangDetailResponse(KhachHang khachHang) {
        ViDiem viDiem = khachHang.getViDiem();

        // SỬA: Lấy địa chỉ từ TaiKhoan thay vì từ KhachHang
        DiaChi diaChi = null;
        if (khachHang.getTaiKhoan() != null) {
            Optional<DiaChi> diaChiOpt = diaChiRepository.findByTaiKhoanIdAndIsDefaultTrue(khachHang.getTaiKhoan().getId());
            if (diaChiOpt.isPresent()) {
                diaChi = diaChiOpt.get();
            }
        }

        Double tongChiTieu = hoaDonRepository.getTongChiTieuByKhachHangId(khachHang.getId());
        Long soLuongDonHang = hoaDonRepository.countByKhachHangId(khachHang.getId());

        // SỬA: Lấy email từ TaiKhoan
        String email = "";
        if (khachHang.getTaiKhoan() != null && StringUtils.hasText(khachHang.getTaiKhoan().getEmail())) {
            email = khachHang.getTaiKhoan().getEmail();
        }

        return KhachHangDetailResponse.builder()
                .id(khachHang.getId())
                .maKhachHang(khachHang.getMaKhachHang())
                .hoTen(khachHang.getHoTen())
                .email(email) // SỬA: Sử dụng email từ TaiKhoan
                .sdt(khachHang.getSdt())
                .trangThai(khachHang.getTrangThai())
                .ngayTao(khachHang.getNgayTao())
                .ngayCapNhat(khachHang.getNgayCapNhat())
                .viDiem(viDiem != null ? mapToViDiemResponse(viDiem) : null)
                .diaChi(diaChi != null ? mapToDiaChiResponse(diaChi) : null)
                .diemTichLuy(viDiem != null ? viDiem.getTongDiem() - viDiem.getSoDiemDaDung() : 0.0)
                .soLuongDonHang(soLuongDonHang != null ? soLuongDonHang.doubleValue() : 0.0)
                .tongChiTieu(tongChiTieu != null ? tongChiTieu : 0.0)
                .capBacKhachHang(xacDinhCapBacKhachHang(tongChiTieu))
                .build();
    }

    private VoucherResponse mapToVoucherResponse(Voucher voucher) {
        Date currentDate = new Date();
        boolean daHetHan = currentDate.after(voucher.getNgayKetThuc());
        boolean daHetSoLuong = voucher.getSoLuong() <= 0;

        return VoucherResponse.builder()
                .id(voucher.getId())
                .maVoucher(voucher.getMaVoucher())
                .tenVoucher(voucher.getTenVoucher())
                .loaiGiamGia(voucher.getLoaiGiamGia())
                .trangThai(voucher.getTrangThai())
                .duongDanHinhAnh(voucher.getDuongDanHinhAnh())
                .giaTriGiamToiDa(BigDecimal.valueOf(voucher.getGiaTriGiamToiDa()))
                .giaTriGiamToiThieu(BigDecimal.valueOf(voucher.getGiaTriGiamToiThieu()))
                // XÓA dòng này: .giaTriDonHangToiThieu(BigDecimal.valueOf(voucher.getGiaTriGiamToiThieu()))
                .soLuong(voucher.getSoLuong())
                .ngayBatDau(voucher.getNgayBatDau())
                .ngayKetThuc(voucher.getNgayKetThuc())
                .ngayTao(voucher.getNgayTao())
                .ngayCapNhat(voucher.getNgayCapNhat())
                .daHetHan(daHetHan)
                .daHetSoLuong(daHetSoLuong)
                .soLuongConLai(voucher.getSoLuong())
//                .khaDung(!daHetHan && !daHetSoLuong && voucher.getTrangThai() == 1)
                .build();
    }

    private DiaChiResponse mapToDiaChiResponse(DiaChi diaChi) {
        if (diaChi == null) return null;

        // SỬA: Không lấy hoTen từ DiaChi nữa vì schema mới không có field này
        return DiaChiResponse.builder()
                .id(diaChi.getId())
                .maTinh(diaChi.getMaTinh())
                .maPhuong(diaChi.getMaPhuong())
                .tenTinh(diaChi.getTenTinh())
                .tenPhuong(diaChi.getTenPhuong())
                // SỬA: Xóa dòng này vì DiaChi không có field hoTen
                // .hoTen(diaChi.getHoTen())
                .diaChiChiTiet(diaChi.getDiaChiChiTiet())
                .trangThai(diaChi.getTrangThai())
                .ngayTao(diaChi.getNgayTao())
                .ngayCapNhat(diaChi.getNgayCapNhat())
                .diaChiDayDu(diaChi.getDiaChiChiTiet() + ", " +
                        diaChi.getTenPhuong() + ", " +
                        diaChi.getTenTinh())
                .build();
    }

    private HoaDonResponse mapToHoaDonResponse(HoaDon hoaDon) {
        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonIdOrderByNgayTao(hoaDon.getId());

        // Lấy thông tin nhân viên - SỬA: Sử dụng relationship
        NhanVienResponse nhanVienResponse = null;
        if (hoaDon.getNhanVien() != null) {
            nhanVienResponse = mapToNhanVienResponse(hoaDon.getNhanVien());
        }

        // Lấy thông tin khách hàng - SỬA: Sử dụng relationship
        KhachHangResponse khachHangResponse = null;
        if (hoaDon.getKhachHang() != null) {
            khachHangResponse = mapToKhachHangResponse(hoaDon.getKhachHang());
        }

        return HoaDonResponse.builder()
                .id(hoaDon.getId())
                .maHoaDon(hoaDon.getMaHoaDon())
                .tenKhach(hoaDon.getTenNguoiDung() != null ? hoaDon.getTenNguoiDung() : "Khách lẻ")
                .sdt(hoaDon.getSdt() != null ? hoaDon.getSdt() : "")
                .email(hoaDon.getEmail() != null ? hoaDon.getEmail() : "")
                .trangThaiHoaDon(hoaDon.getTrangThaiHoaDon())
                .loaiHoaDon(hoaDon.getLoaiHoaDon())
                .tongTien(hoaDon.getTongTien())
                .tongThanhToan(hoaDon.getTongThanhToan())
                .ngayTao(hoaDon.getNgayTao())
                .ngayHoanThanh(hoaDon.getNgayHoanThanh())
                .ghiChu(hoaDon.getGhiChu())
                .nhanVien(nhanVienResponse)
                .khachHang(khachHangResponse)
                .chiTiets(chiTiets.stream().map(this::mapToHoaDonChiTietResponse).collect(Collectors.toList()))
                .build();
    }

    private HoaDonChiTietResponse mapToHoaDonChiTietResponse(HoaDonChiTiet chiTiet) {
        ChiTietSanPham sanPham = chiTiet.getChiTietSanPham(); // Sử dụng relationship

        if (sanPham != null) {
            // Lấy thông tin sản phẩm, màu sắc, kích cỡ, thương hiệu
            SanPham sp = sanPham.getSanPham(); // Nếu có relationship trong ChiTietSanPham
            // Hoặc sử dụng: sanPhamRepository.findById(sanPham.getIdSanPham()).orElse(null);

            MauSac mauSac = sanPham.getMauSac(); // Nếu có relationship
            KichCo kichCo = sanPham.getKichCo(); // Nếu có relationship

            ThuongHieu thuongHieu = null;
            if (sp != null) {
                thuongHieu = sp.getThuongHieu(); // Nếu có relationship trong SanPham
            }

            return HoaDonChiTietResponse.builder()
                    .id(chiTiet.getId())
                    .chiTietSanPhamId(sanPham.getId())
                    .tenSanPham(sp != null ? sp.getTenSanPham() : "N/A")
                    .mauSac(mauSac != null ? mauSac.getTenMauSac() : "N/A")
                    .kichCo(kichCo != null ? kichCo.getTenKichCo() : "N/A")
                    .thuongHieu(thuongHieu != null ? thuongHieu.getTenThuongHieu() : "N/A")
                    .donGia(chiTiet.getGia())
                    .soLuong(chiTiet.getSoLuong())
                    .thanhTien(chiTiet.getGia() * chiTiet.getSoLuong())
                    .soLuongTon(sanPham.getSoLuong())
                    .maChiTiet(sanPham.getMaChiTiet())
                    .build();
        }

        return HoaDonChiTietResponse.builder()
                .id(chiTiet.getId())
                .chiTietSanPhamId(null)
                .tenSanPham("N/A")
                .mauSac("N/A")
                .kichCo("N/A")
                .thuongHieu("N/A")
                .donGia(chiTiet.getGia())
                .soLuong(chiTiet.getSoLuong())
                .thanhTien(chiTiet.getGia() * chiTiet.getSoLuong())
                .soLuongTon(0)
                .maChiTiet("N/A")
                .build();
    }

    private NhanVienResponse mapToNhanVienResponse(NhanVien nhanVien) {
        // SỬA: Lấy email từ TaiKhoan thay vì từ NhanVien
        String email = "";
        if (nhanVien.getTaiKhoan() != null && StringUtils.hasText(nhanVien.getTaiKhoan().getEmail())) {
            email = nhanVien.getTaiKhoan().getEmail();
        }

        return NhanVienResponse.builder()
                .id(nhanVien.getId())
                .maNhanVien(nhanVien.getMaNhanVien())
                .hoTen(nhanVien.getHoTen())
                .email(email) // SỬA: Sử dụng email từ TaiKhoan
                .sdt(nhanVien.getSdt())
                .trangThai(nhanVien.getTrangThai())
                .vaiTro("NHAN_VIEN") // Default role
                .ngayTao(nhanVien.getNgayTao())
                .ngayCapNhat(nhanVien.getNgayCapNhat())
                .build();
    }

    private ViDiemResponse mapToViDiemResponse(ViDiem viDiem) {
        return ViDiemResponse.builder()
                .id(viDiem.getId())
                .tongDiem(viDiem.getTongDiem())
                .soLuongDaDung(viDiem.getSoDiemDaDung())
                .soLuongDaCong(viDiem.getSoDiemDaCong())
                .giaTriDiem(viDiem.getGiaTriDiem())
                .ngayTao(viDiem.getNgayTao())
                .ngayCapNhat(viDiem.getNgayCapNhat())
                .diemKhaDung(viDiem.getTongDiem() - viDiem.getSoDiemDaDung())
                .build();
    }

    private HinhAnhResponse mapToHinhAnhResponse(HinhAnh hinhAnh) {
        if (hinhAnh == null) return null;

        // Format URL đầy đủ giống SanPhamChiTiet
        String duongDanGoc = hinhAnh.getDuongDan();
        String urlFormatted = duongDanGoc;

        if (duongDanGoc != null && !duongDanGoc.isEmpty()) {
            String cleanPath = duongDanGoc
                    .replace("/images/", "")
                    .replace("/hinh-anh/images/", "");
            urlFormatted = "http://localhost:8080/hinh-anh/images/" + cleanPath;
        }

        return HinhAnhResponse.builder()
                .id(hinhAnh.getId())
                .maHinhAnh(hinhAnh.getMaHinhAnh())
                .tenHinhAnh(hinhAnh.getTenHinhAnh())
                .duongDan(duongDanGoc) // Đường dẫn gốc
                .urlHinhAnh(urlFormatted) // URL đã format
                .trangThai(hinhAnh.getTrangThai())
                .laHinhChinh(true) // Logic để xác định hình chính có thể được implement sau
                .build();
    }

    private SanPhamInfoResponse mapToSanPhamInfoResponse(SanPham sanPham) {
        return SanPhamInfoResponse.builder()
                .id(sanPham.getId())
                .maSanPham(sanPham.getMaSanPham())
                .tenSanPham(sanPham.getTenSanPham())
//                .moTa(sanPham.getMoTa())
                .soLuong(sanPham.getSoLuong())
                .trangThai(sanPham.getTrangThai())
                .build();
    }

    private MauSacResponse mapToMauSacResponse(MauSac mauSac) {
        if (mauSac == null) return null;

        return MauSacResponse.builder()
                .id(mauSac.getId())
                .maMauSac(mauSac.getMaMauSac())
                .tenMauSac(mauSac.getTenMauSac())
                .tenMau(mauSac.getTenMauSac()) // ✅ THÊM: Alias cho frontend
                .maMau(getMaMauHex(mauSac))    // ✅ THÊM: Hex color từ method
                .trangThai(mauSac.getTrangThai())
                .build();
    }

    private KichCoResponse mapToKichCoResponse(KichCo kichCo) {
        if (kichCo == null) return null;

        return KichCoResponse.builder()
                .id(kichCo.getId())
                .maKichCo(kichCo.getMaKichCo())
                .tenKichCo(kichCo.getTenKichCo())
                .trangThai(kichCo.getTrangThai())
                .thuTu(getThuTuKichCo(kichCo.getTenKichCo())) // ✅ THÊM: Thứ tự sắp xếp
                .build();
    }

    private ThuongHieuResponse mapToThuongHieuResponse(ThuongHieu thuongHieu) {
        if (thuongHieu == null) return null;

        Integer soLuongSanPham = sanPhamRepository.countByThuongHieuIdAndTrangThai(thuongHieu.getId(), 1);

        return ThuongHieuResponse.builder()
                .id(thuongHieu.getId())
                .maThuongHieu(thuongHieu.getMaThuongHieu())
                .tenThuongHieu(thuongHieu.getTenThuongHieu())
                .trangThai(thuongHieu.getTrangThai())
                .soLuongSanPham(soLuongSanPham != null ? soLuongSanPham : 0)
                .build();
    }

    private DanhMucResponse mapToDanhMucResponse(DanhMuc danhMuc) {
        if (danhMuc == null) return null;

        Integer soLuongSanPham = sanPhamRepository.countByDanhMucIdAndTrangThai(danhMuc.getId(), 1);

        return DanhMucResponse.builder()
                .id(danhMuc.getId())
                .maDanhMuc(danhMuc.getMaDanhMuc())
                .tenDanhMuc(danhMuc.getTenDanhMuc())
                .trangThai(danhMuc.getTrangThai())
                .soLuongSanPham(soLuongSanPham != null ? soLuongSanPham : 0)
                .build();
    }

    private ChatLieuResponse mapToChatLieuResponse(ChatLieu chatLieu) {
        if (chatLieu == null) return null;

        return ChatLieuResponse.builder()
                .id(chatLieu.getId())
                .maChatLieu(chatLieu.getMaChatLieu())
                .tenChatLieu(chatLieu.getTenChatLieu())
                .trangThai(chatLieu.getTrangThai())
                .build();
    }

    private DeGiayResponse mapToDeGiayResponse(DeGiay deGiay) {
        if (deGiay == null) return null;

        return DeGiayResponse.builder()
                .id(deGiay.getId())
                .maDeGiay(deGiay.getMaDeGiay())
                .tenDeGiay(deGiay.getTenDeGiay())
                .trangThai(deGiay.getTrangThai())
                .build();
    }

    private String xacDinhCapBacKhachHang(Double tongChiTieu) {
        if (tongChiTieu == null) tongChiTieu = 0.0;

        if (tongChiTieu >= 50000000) {
            return "VIP";
        } else if (tongChiTieu >= 20000000) {
            return "VÀNG";
        } else if (tongChiTieu >= 5000000) {
            return "BẠC";
        } else {
            return "ĐỒNG";
        }
    }

    // ===== ADDITIONAL UTILITY METHODS =====

    /**
     * Kiểm tra quyền truy cập của nhân viên
     */
    private void kiemTraQuyenNhanVien(Integer nhanVienId, String action) {
        NhanVien nhanVien = nhanVienRepository.findById(nhanVienId).orElse(null);
        if (nhanVien == null || nhanVien.getTrangThai() != 1) {
            throw new RuntimeException("Nhân viên không có quyền thực hiện " + action);
        }
    }

    /**
     * Kiểm tra sản phẩm có thể bán không
     */
    private void kiemTraSanPhamCoTheBan(ChiTietSanPham sanPham, int soLuongCanBan) {
        if (sanPham.getTrangThai() != 1) {
            throw new RuntimeException("Sản phẩm không còn kinh doanh");
        }

        if (sanPham.getSoLuong() < soLuongCanBan) {
            throw new RuntimeException("Không đủ tồn kho. Còn lại: " + sanPham.getSoLuong());
        }

        // SỬA: Sử dụng relationship thay vì getIdSanPham()
        SanPham sanPhamGoc = sanPham.getSanPham();
        if (sanPhamGoc == null || sanPhamGoc.getTrangThai() != 1) {
            throw new RuntimeException("Sản phẩm không còn kinh doanh");
        }
    }

    /**
     * Cập nhật tồn kho sản phẩm an toàn
     */
    private void capNhatTonKhoAnToan(Integer chiTietSanPhamId, int soLuongThayDoi) {
        ChiTietSanPham sanPham = chiTietSanPhamRepository.findById(chiTietSanPhamId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        int soLuongMoi = sanPham.getSoLuong() + soLuongThayDoi;
        if (soLuongMoi < 0) {
            throw new RuntimeException("Số lượng tồn kho không thể âm");
        }

        sanPham.setSoLuong(soLuongMoi);
        sanPham.setNgayCapNhat(new Date());
        chiTietSanPhamRepository.save(sanPham);

        // SỬA: Sử dụng relationship để lấy ID sản phẩm gốc
        SanPham sanPhamGoc = sanPham.getSanPham();
        if (sanPhamGoc != null) {
            capNhatTongSoLuongSanPham(sanPhamGoc.getId());
        }
    }

    /**
     * Cập nhật tổng số lượng sản phẩm gốc
     */
    private void capNhatTongSoLuongSanPham(Integer sanPhamId) {
        try {
            List<ChiTietSanPham> chiTietSanPhams = chiTietSanPhamRepository.findBySanPhamIdAndTrangThai(sanPhamId, 1);
            int tongSoLuong = chiTietSanPhams.stream().mapToInt(ChiTietSanPham::getSoLuong).sum();

            SanPham sanPham = sanPhamRepository.findById(sanPhamId).orElse(null);
            if (sanPham != null) {
                sanPham.setSoLuong(tongSoLuong);
                sanPham.setNgayCapNhat(new Date());
                sanPhamRepository.save(sanPham);
            }
        } catch (Exception e) {
            log.warn("Không thể cập nhật tổng số lượng sản phẩm: {}", e.getMessage());
        }
    }

    /**
     * Gửi thông báo cho khách hàng
     */
    private void guiThongBaoKhachHang(Integer khachHangId, String noiDung, Integer hoaDonId) {
        try {
            KhachHang khachHang = khachHangRepository.findById(khachHangId).orElse(null);
            if (khachHang != null && khachHang.getTaiKhoan() != null) {
                log.info("Gửi thông báo cho khách hàng {}: {}", khachHang.getHoTen(), noiDung);
            }
        } catch (Exception e) {
            log.warn("Không thể gửi thông báo: {}", e.getMessage());
        }
    }

    /**
     * Validate request data
     */
    private void validateThemSanPhamRequest(ThemSanPhamRequest request) {
        if (request.getChiTietSanPhamId() == null) {
            throw new IllegalArgumentException("ID chi tiết sản phẩm không được để trống");
        }
        if (request.getSoLuong() == null || request.getSoLuong() <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
    }

    private void validateCapNhatSanPhamRequest(CapNhatSanPhamRequest request) {
        if (request.getSoLuong() == null || request.getSoLuong() <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
    }

    private void validateThanhToanRequest(ThanhToanRequest request) {
        if (request.getKhachHangId() == null) {
            throw new IllegalArgumentException("ID khách hàng không được để trống");
        }
        if (!StringUtils.hasText(request.getLoaiHoaDon())) {
            throw new IllegalArgumentException("Loại hóa đơn không được để trống");
        }
    }

    /**
     * Log hoạt động của hệ thống
     */
    private void logActivity(String action, Integer userId, String details) {
        log.info("ACTIVITY_LOG - Action: {}, User: {}, Details: {}", action, userId, details);
    }

    /**
     * Tạo mã hóa đơn theo format đặc biệt
     */
    private String taoMaHoaDonTheoFormat(String loaiHoaDon) {
        String prefix = "OFFLINE".equals(loaiHoaDon) ? "HDO" : "HDN";
        return prefix + System.currentTimeMillis();
    }

    /**
     * Kiểm tra thời gian làm việc
     */
    private boolean kiemTraThoiGianLamViec() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return hour >= 8 && hour <= 22; // 8h - 22h
    }

    /**
     * Tính phí vận chuyển
     */
    private BigDecimal tinhPhiVanChuyen(String diaChiGiaoHang, BigDecimal tongTien) {
        // Logic tính phí vận chuyển theo địa chỉ và tổng tiền
        if (tongTien.compareTo(BigDecimal.valueOf(500000)) >= 0) {
            return BigDecimal.ZERO; // Miễn phí ship cho đơn >= 500k
        }

        if (diaChiGiaoHang != null && diaChiGiaoHang.contains("Hà Nội")) {
            return BigDecimal.valueOf(30000); // 30k trong Hà Nội
        } else {
            return BigDecimal.valueOf(50000); // 50k ngoại tỉnh
        }
    }

    /**
     * Backup dữ liệu quan trọng
     */
    private void backupHoaDonData(Integer hoaDonId) {
        try {
            // Logic backup dữ liệu hóa đơn quan trọng
            log.info("Backup data for invoice: {}", hoaDonId);
        } catch (Exception e) {
            log.warn("Backup failed for invoice {}: {}", hoaDonId, e.getMessage());
        }
    }

    /**
     * Kiểm tra duplicate transaction
     */
    private boolean kiemTraDuplicateTransaction(String transactionId) {
        // Logic kiểm tra giao dịch trùng lặp
        return false;
    }

    /**
     * Clean up old data
     */
    private void cleanupOldData() {
        try {
            // Logic dọn dẹp dữ liệu cũ, cache, temp files
            log.info("Cleanup old data completed");
        } catch (Exception e) {
            log.warn("Cleanup failed: {}", e.getMessage());
        }
    }
    @Override
    public List<DanhMucResponse> layDanhSachDanhMuc() {
        try {
            log.info("📂 Lấy danh sách danh mục");
            // ✅ SỬA: Sử dụng method name đúng
            List<DanhMuc> danhMucs = danhMucRepository.findByTrangThaiOrderByTenDanhMucAsc(1);

            List<DanhMucResponse> responses = danhMucs.stream()
                    .map(this::mapToDanhMucResponseForMasterData)
                    .collect(Collectors.toList());

            log.info("✅ Lấy {} danh mục thành công", responses.size());
            return responses;

        } catch (Exception e) {
            log.error("❌ Lỗi lấy danh sách danh mục", e);
            throw new RuntimeException("Không thể lấy danh sách danh mục: " + e.getMessage());
        }
    }

    @Override
    public List<ThuongHieuResponse> layDanhSachThuongHieu() {
        try {
            log.info("🏷️ Lấy danh sách thương hiệu");
            // ✅ SỬA: Sử dụng method name đúng
            List<ThuongHieu> thuongHieus = thuongHieuRepository.findByTrangThaiOrderByTenThuongHieuAsc(1);

            List<ThuongHieuResponse> responses = thuongHieus.stream()
                    .map(this::mapToThuongHieuResponseForMasterData)
                    .collect(Collectors.toList());

            log.info("✅ Lấy {} thương hiệu thành công", responses.size());
            return responses;

        } catch (Exception e) {
            log.error("❌ Lỗi lấy danh sách thương hiệu", e);
            throw new RuntimeException("Không thể lấy danh sách thương hiệu: " + e.getMessage());
        }
    }

    @Override
    public List<MauSacResponse> layDanhSachMauSac() {
        try {
            log.info("🎨 Lấy danh sách màu sắc");
            // ✅ SỬA: Sử dụng method name đúng
            List<MauSac> mauSacs = mauSacRepository.findByTrangThaiOrderByTenMauSacAsc(1);

            List<MauSacResponse> responses = mauSacs.stream()
                    .map(this::mapToMauSacResponseForMasterData)
                    .collect(Collectors.toList());

            log.info("✅ Lấy {} màu sắc thành công", responses.size());
            return responses;

        } catch (Exception e) {
            log.error("❌ Lỗi lấy danh sách màu sắc", e);
            throw new RuntimeException("Không thể lấy danh sách màu sắc: " + e.getMessage());
        }
    }

    @Override
    public List<KichCoResponse> layDanhSachKichCo() {
        try {
            log.info("📏 Lấy danh sách kích cỡ");
            // ✅ SỬA: Sử dụng method name đúng
            List<KichCo> kichCos = kichCoRepository.findByTrangThaiOrderByTenKichCoAsc(1);

            List<KichCoResponse> responses = kichCos.stream()
                    .map(this::mapToKichCoResponseForMasterData)
                    .collect(Collectors.toList());

            log.info("✅ Lấy {} kích cỡ thành công", responses.size());
            return responses;

        } catch (Exception e) {
            log.error("❌ Lỗi lấy danh sách kích cỡ", e);
            throw new RuntimeException("Không thể lấy danh sách kích cỡ: " + e.getMessage());
        }
    }

    @Override
    public List<ChatLieuResponse> layDanhSachChatLieu() {
        try {
            log.info("🧵 Lấy danh sách chất liệu");
            // ✅ SỬA: Sử dụng method name đúng
            List<ChatLieu> chatLieus = chatLieuRepository.findByTrangThaiOrderByTenChatLieuAsc(1);

            List<ChatLieuResponse> responses = chatLieus.stream()
                    .map(this::mapToChatLieuResponseForMasterData)
                    .collect(Collectors.toList());

            log.info("✅ Lấy {} chất liệu thành công", responses.size());
            return responses;

        } catch (Exception e) {
            log.error("❌ Lỗi lấy danh sách chất liệu", e);
            throw new RuntimeException("Không thể lấy danh sách chất liệu: " + e.getMessage());
        }
    }

    @Override
    public List<DeGiayResponse> layDanhSachDeGiay() {
        try {
            log.info("👟 Lấy danh sách đế giày");
            // ✅ SỬA: Sử dụng method name đúng
            List<DeGiay> deGiays = deGiayRepository.findByTrangThaiOrderByTenDeGiayAsc(1);

            List<DeGiayResponse> responses = deGiays.stream()
                    .map(this::mapToDeGiayResponseForMasterData)
                    .collect(Collectors.toList());

            log.info("✅ Lấy {} đế giày thành công", responses.size());
            return responses;

        } catch (Exception e) {
            log.error("❌ Lỗi lấy danh sách đế giày", e);
            throw new RuntimeException("Không thể lấy danh sách đế giày: " + e.getMessage());
        }
    }

// ===== MAPPING METHODS MỚI CHO MASTER DATA =====

    private DanhMucResponse mapToDanhMucResponseForMasterData(DanhMuc danhMuc) {
        if (danhMuc == null) return null;

        Integer soLuongSanPham = sanPhamRepository.countByDanhMucIdAndTrangThai(danhMuc.getId(), 1);

        return DanhMucResponse.builder()
                .id(danhMuc.getId())
                .maDanhMuc(danhMuc.getMaDanhMuc())
                .tenDanhMuc(danhMuc.getTenDanhMuc())
                .trangThai(danhMuc.getTrangThai())
                .soLuongSanPham(soLuongSanPham != null ? soLuongSanPham : 0)
                .ngayTao(danhMuc.getNgayTao())
                .ngayCapNhat(danhMuc.getNgayCapNhat())
                .build();
    }

    private ThuongHieuResponse mapToThuongHieuResponseForMasterData(ThuongHieu thuongHieu) {
        if (thuongHieu == null) return null;

        Integer soLuongSanPham = sanPhamRepository.countByThuongHieuIdAndTrangThai(thuongHieu.getId(), 1);

        return ThuongHieuResponse.builder()
                .id(thuongHieu.getId())
                .maThuongHieu(thuongHieu.getMaThuongHieu())
                .tenThuongHieu(thuongHieu.getTenThuongHieu())
                .trangThai(thuongHieu.getTrangThai())
                .soLuongSanPham(soLuongSanPham != null ? soLuongSanPham : 0)
                .ngayTao(thuongHieu.getNgayTao())
                .ngayCapNhat(thuongHieu.getNgayCapNhat())
                .build();
    }

    private MauSacResponse mapToMauSacResponseForMasterData(MauSac mauSac) {
        return MauSacResponse.builder()
                .id(mauSac.getId())
                .maMauSac(mauSac.getMaMauSac())
                .tenMauSac(mauSac.getTenMauSac())
                .tenMau(mauSac.getTenMauSac()) // Alias cho frontend
                .maMau(getMaMauHex(mauSac)) // Lấy mã màu hex
                .trangThai(mauSac.getTrangThai())
                .ngayTao(mauSac.getNgayTao())
                .ngayCapNhat(mauSac.getNgayCapNhat())
                .build();
    }

    private KichCoResponse mapToKichCoResponseForMasterData(KichCo kichCo) {
        return KichCoResponse.builder()
                .id(kichCo.getId())
                .maKichCo(kichCo.getMaKichCo())
                .tenKichCo(kichCo.getTenKichCo())
                .trangThai(kichCo.getTrangThai())
                .thuTu(getThuTuKichCo(kichCo.getTenKichCo())) // Sắp xếp theo size
                .ngayTao(kichCo.getNgayTao())
                .ngayCapNhat(kichCo.getNgayCapNhat())
                .build();
    }

    private ChatLieuResponse mapToChatLieuResponseForMasterData(ChatLieu chatLieu) {
        if (chatLieu == null) return null;

        return ChatLieuResponse.builder()
                .id(chatLieu.getId())
                .maChatLieu(chatLieu.getMaChatLieu())
                .tenChatLieu(chatLieu.getTenChatLieu())
                .trangThai(chatLieu.getTrangThai())
                .ngayTao(chatLieu.getNgayTao())
                .ngayCapNhat(chatLieu.getNgayCapNhat())
                .build();
    }

    private DeGiayResponse mapToDeGiayResponseForMasterData(DeGiay deGiay) {
        if (deGiay == null) return null;

        return DeGiayResponse.builder()
                .id(deGiay.getId())
                .maDeGiay(deGiay.getMaDeGiay())
                .tenDeGiay(deGiay.getTenDeGiay())
                .trangThai(deGiay.getTrangThai())
                .ngayTao(deGiay.getNgayTao())
                .ngayCapNhat(deGiay.getNgayCapNhat())
                .build();
    }

// ===== UTILITY METHODS =====

    /**
     * Lấy mã màu hex từ entity MauSac
     */
    private String getMaMauHex(MauSac mauSac) {
        if (mauSac == null) return "#6c757d"; // Màu xám mặc định

        // Map theo tên màu tiếng Việt
        String tenMau = mauSac.getTenMauSac().toLowerCase().trim();
        switch (tenMau) {
            case "đỏ":
            case "red":
            case "do":
                return "#dc3545";
            case "xanh":
            case "xanh dương":
            case "blue":
                return "#0066cc";
            case "đen":
            case "black":
            case "den":
                return "#000000";
            case "trắng":
            case "white":
            case "trang":
                return "#ffffff";
            case "vàng":
            case "yellow":
            case "vang":
                return "#ffc107";
            case "xanh lá":
            case "xanh la":
            case "green":
                return "#28a745";
            case "tím":
            case "purple":
            case "tim":
                return "#6f42c1";
            case "hồng":
            case "pink":
            case "hong":
                return "#e83e8c";
            case "nâu":
            case "brown":
            case "nau":
                return "#8b4513";
            case "cam":
            case "orange":
                return "#fd7e14";
            case "xám":
            case "gray":
            case "xam":
                return "#6c757d";
            case "be":
            case "kem":
                return "#f5f5dc";
            case "navy":
            case "xanh navy":
                return "#000080";
            case "bạc":
            case "bac":
            case "silver":
                return "#c0c0c0";
            case "vàng đồng":
            case "gold":
                return "#ffd700";
            default:
                return "#6c757d"; // Màu mặc định
        }
    }

    /**
     * Lấy thứ tự sắp xếp cho kích cỡ
     */
    private Integer getThuTuKichCo(String tenKichCo) {
        if (tenKichCo == null) return 999;

        try {
            // Nếu là số, convert trực tiếp
            return Integer.parseInt(tenKichCo);
        } catch (NumberFormatException e) {
            // Nếu không phải số, map theo size chuẩn
            switch (tenKichCo.toUpperCase().trim()) {
                case "XS": return 1;
                case "S": return 2;
                case "M": return 3;
                case "L": return 4;
                case "XL": return 5;
                case "XXL": return 6;
                case "XXXL": return 7;
                default: return 999;
            }
        }
    }
    @Override
    public Integer timNhanVienIdTheoMa(String ma) {
        try {
            log.info("🔍 Tìm nhân viên theo mã: '{}'", ma);

            if (ma == null || ma.trim().isEmpty()) {
                log.warn("⚠️ Mã null hoặc rỗng");
                return null;
            }

            String maTrimmed = ma.trim();

            // 1. ✅ Thử tìm trực tiếp theo mã nhân viên trước
            Optional<NhanVien> nhanVienByMaNV = nhanVienRepository
                    .findByMaNhanVienAndTrangThai(maTrimmed, 1);

            if (nhanVienByMaNV.isPresent()) {
                NhanVien nhanVien = nhanVienByMaNV.get();
                log.info("✅ Tìm thấy nhân viên theo MÃ NHÂN VIÊN: ID={}, Tên={}, MaNV={}",
                        nhanVien.getId(), nhanVien.getHoTen(), nhanVien.getMaNhanVien());
                return nhanVien.getId();
            }

            // 2. ✅ Nếu không tìm thấy, thử tìm theo mã tài khoản
            log.info("🔄 Không tìm thấy theo mã nhân viên, thử tìm theo mã tài khoản...");

            Optional<NhanVien> nhanVienByMaTK = nhanVienRepository
                    .findByTaiKhoan_MaTaiKhoanAndTrangThai(maTrimmed, 1);

            if (nhanVienByMaTK.isPresent()) {
                NhanVien nhanVien = nhanVienByMaTK.get();
                log.info("✅ Tìm thấy nhân viên theo MÃ TÀI KHOẢN: ID={}, Tên={}, MaTK={}, MaNV={}",
                        nhanVien.getId(), nhanVien.getHoTen(),
                        nhanVien.getTaiKhoan().getMaTaiKhoan(), nhanVien.getMaNhanVien());
                return nhanVien.getId();
            }

            // 3. ✅ Thử lấy mã nhân viên từ mã tài khoản (sử dụng method mới)
            log.info("🔄 Thử lấy mã nhân viên từ mã tài khoản...");

            Optional<String> maNhanVienOpt = nhanVienRepository.findMaNhanVienByMaTaiKhoan(maTrimmed);
            if (maNhanVienOpt.isPresent()) {
                String maNhanVien = maNhanVienOpt.get();
                log.info("🔄 Tìm thấy mã nhân viên '{}' từ mã tài khoản '{}'", maNhanVien, maTrimmed);

                // Tìm lại nhân viên theo mã nhân viên vừa lấy được
                Optional<NhanVien> nhanVienFinal = nhanVienRepository
                        .findByMaNhanVienAndTrangThai(maNhanVien, 1);

                if (nhanVienFinal.isPresent()) {
                    NhanVien nhanVien = nhanVienFinal.get();
                    log.info("✅ Tìm thấy nhân viên qua chuyển đổi: ID={}, Tên={}, MaNV={}",
                            nhanVien.getId(), nhanVien.getHoTen(), nhanVien.getMaNhanVien());
                    return nhanVien.getId();
                }
            }

            // 4. Debug: Tìm tất cả nhân viên hoạt động để debug
            List<NhanVien> allActive = nhanVienRepository.findByTrangThai(1);
            log.info("📋 Có {} nhân viên hoạt động trong hệ thống:", allActive.size());

            for (NhanVien nv : allActive) {
                String maTaiKhoan = nv.getTaiKhoan() != null ? nv.getTaiKhoan().getMaTaiKhoan() : "NULL";
                log.info("  - ID: {}, MãNV: '{}', MãTK: '{}', Tên: '{}'",
                        nv.getId(),
                        nv.getMaNhanVien(),
                        maTaiKhoan,
                        nv.getHoTen());
            }

            log.warn("⚠️ Không tìm thấy nhân viên hoạt động với mã: '{}'", maTrimmed);
            return null;

        } catch (Exception e) {
            log.error("❌ Lỗi tìm nhân viên theo mã: {}", e.getMessage(), e);
            return null;
        }
    }
    @Override
    public String chuyenDoiMaTaiKhoanSangMaNhanVien(String maTaiKhoan) {
        try {
            log.info("🔄 Chuyển đổi mã tài khoản '{}' sang mã nhân viên", maTaiKhoan);

            if (maTaiKhoan == null || maTaiKhoan.trim().isEmpty()) {
                return null;
            }

            Optional<String> maNhanVienOpt = nhanVienRepository.findMaNhanVienByMaTaiKhoan(maTaiKhoan.trim());

            if (maNhanVienOpt.isPresent()) {
                String maNhanVien = maNhanVienOpt.get();
                log.info("✅ Chuyển đổi thành công: '{}' -> '{}'", maTaiKhoan, maNhanVien);
                return maNhanVien;
            }

            log.warn("⚠️ Không tìm thấy mã nhân viên cho mã tài khoản: '{}'", maTaiKhoan);
            return null;

        } catch (Exception e) {
            log.error("❌ Lỗi chuyển đổi mã: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Integer timNhanVienIdLinhHoat(String ma) {
        try {
            log.info("🔍 Tìm nhân viên linh hoạt với mã: '{}'", ma);

            if (ma == null || ma.trim().isEmpty()) {
                return null;
            }

            String maTrimmed = ma.trim();

            // Bước 1: Thử tìm trực tiếp bằng mã nhân viên
            Optional<NhanVien> nhanVienOpt = nhanVienRepository.findByMaNhanVienAndTrangThai(maTrimmed, 1);
            if (nhanVienOpt.isPresent()) {
                log.info("✅ Tìm thấy theo mã nhân viên");
                return nhanVienOpt.get().getId();
            }

            // Bước 2: Thử chuyển đổi từ mã tài khoản
            String maNhanVien = chuyenDoiMaTaiKhoanSangMaNhanVien(maTrimmed);
            if (maNhanVien != null) {
                nhanVienOpt = nhanVienRepository.findByMaNhanVienAndTrangThai(maNhanVien, 1);
                if (nhanVienOpt.isPresent()) {
                    log.info("✅ Tìm thấy sau khi chuyển đổi: '{}' -> '{}'", maTrimmed, maNhanVien);
                    return nhanVienOpt.get().getId();
                }
            }

            // Bước 3: Thử tìm trực tiếp bằng mã tài khoản
            nhanVienOpt = nhanVienRepository.findByTaiKhoan_MaTaiKhoanAndTrangThai(maTrimmed, 1);
            if (nhanVienOpt.isPresent()) {
                log.info("✅ Tìm thấy theo mã tài khoản");
                return nhanVienOpt.get().getId();
            }

            log.warn("⚠️ Không tìm thấy nhân viên với mã: '{}'", maTrimmed);
            return null;

        } catch (Exception e) {
            log.error("❌ Lỗi tìm nhân viên linh hoạt: {}", e.getMessage());
            return null;
        }
    }
    /**
     * Kiểm tra email đã tồn tại trong hệ thống (an toàn)
     */
    private boolean isEmailExists(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }

        try {
            // Kiểm tra trong TaiKhoan trước
            if (taiKhoanRepository.existsByEmail(email)) {
                return true;
            }

            // Kiểm tra thêm qua KhachHang (backup)
            return khachHangRepository.existsByTaiKhoanEmail(email);

        } catch (Exception e) {
            log.warn("Lỗi kiểm tra email tồn tại: {}", e.getMessage());
            // Fallback - chỉ kiểm tra TaiKhoan
            try {
                return taiKhoanRepository.existsByEmail(email);
            } catch (Exception e2) {
                log.error("Lỗi nghiêm trọng kiểm tra email: {}", e2.getMessage());
                return false; // Không chặn được thì cho phép tạo
            }
        }
    }
    @Override
    @Transactional
    public HoaDonChoTongQuanResponse chuyenSangGiaoHang(Integer hoaDonId, GiaoHangRequest request) {
        try {
            log.info("🚚 Chuyển hóa đơn {} sang giao hàng", hoaDonId);

            // Kiểm tra hóa đơn
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

            if (!"CHO".equals(hoaDon.getTrangThaiHoaDon())) {
                throw new RuntimeException("Chỉ có thể chuyển hóa đơn chờ sang giao hàng");
            }

            // Kiểm tra có sản phẩm không
            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
            if (chiTiets.isEmpty()) {
                throw new RuntimeException("Hóa đơn chưa có sản phẩm");
            }

            // Cập nhật thông tin giao hàng
            hoaDon.setLoaiHoaDon("ONLINE"); // Đánh dấu là đơn giao hàng
            hoaDon.setTenNguoiDung(request.getTenNguoiNhan());
            hoaDon.setSdt(request.getSdt());
            hoaDon.setEmail(request.getEmail() != null ? request.getEmail() : "");
            hoaDon.setDiaChi(request.getDiaChi());
            hoaDon.setGhiChu(request.getGhiChu());
            hoaDon.setPhuongThucThanhToan(request.getPhuongThucThanhToan() != null ?
                    request.getPhuongThucThanhToan() : "COD");

            // Tính phí vận chuyển
            BigDecimal phiShip = request.getPhiVanChuyen() != null ?
                    request.getPhiVanChuyen() : tinhPhiVanChuyenMacDinh(request.getDiaChi(), hoaDon.getTongTien());
            hoaDon.setPhiVanChuyen(phiShip);

            // Cập nhật tổng thanh toán
            BigDecimal tongThanhToan = hoaDon.getTongTien().add(phiShip);
            hoaDon.setTongThanhToan(tongThanhToan);

            hoaDon.setNgayCapNhat(new Date());
            hoaDonRepository.save(hoaDon);

            // Tạo lịch sử
            taoLichSuHoaDon(hoaDon, "Chuyển sang giao hàng", hoaDon.getNhanVien());

            log.info("✅ Đã chuyển hóa đơn {} sang giao hàng", hoaDonId);

            return layTongQuanHoaDonCho(hoaDonId);

        } catch (Exception e) {
            log.error("❌ Lỗi chuyển sang giao hàng: {}", e.getMessage());
            throw new RuntimeException("Không thể chuyển sang giao hàng: " + e.getMessage());
        }
    }

    @Override
    public TinhPhiShipResponse tinhPhiShip(TinhPhiShipRequest request) {
        try {
            log.info("💰 Tính phí ship cho địa chỉ: {}", request.getDiaChi());

            BigDecimal tongTien = request.getTongTien() != null ? request.getTongTien() : BigDecimal.ZERO;
            BigDecimal phiShip = BigDecimal.ZERO;
            boolean mienPhiShip = false;
            String lyDoMienPhi = null;

            // Logic tính phí ship
            if (tongTien.compareTo(BigDecimal.valueOf(500000)) >= 0) {
                // Miễn phí ship cho đơn >= 500k
                mienPhiShip = true;
                lyDoMienPhi = "Miễn phí ship cho đơn hàng từ 500.000₫";
                phiShip = BigDecimal.ZERO;
            } else {
                // Tính phí theo địa chỉ
                String diaChi = request.getDiaChi() != null ? request.getDiaChi().toLowerCase() : "";

                if (diaChi.contains("hà nội") || diaChi.contains("ha noi")) {
                    phiShip = BigDecimal.valueOf(30000); // 30k trong Hà Nội
                } else if (diaChi.contains("hồ chí minh") || diaChi.contains("hcm") ||
                        diaChi.contains("sài gòn") || diaChi.contains("sai gon")) {
                    phiShip = BigDecimal.valueOf(35000); // 35k TP.HCM
                } else if (diaChi.contains("đà nẵng") || diaChi.contains("da nang")) {
                    phiShip = BigDecimal.valueOf(35000); // 35k Đà Nẵng
                } else {
                    phiShip = BigDecimal.valueOf(50000); // 50k các tỉnh khác
                }
            }

            BigDecimal tongTienSauShip = tongTien.add(phiShip);

            return TinhPhiShipResponse.builder()
                    .phiShip(phiShip)
                    .mienPhiShip(mienPhiShip)
                    .lyDoMienPhi(lyDoMienPhi)
                    .tongTienSauShip(tongTienSauShip)
                    .build();

        } catch (Exception e) {
            log.error("❌ Lỗi tính phí ship: {}", e.getMessage());
            throw new RuntimeException("Không thể tính phí ship: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public HoaDonChoTongQuanResponse capNhatThongTinGiaoHang(Integer hoaDonId, CapNhatGiaoHangRequest request) {
        try {
            log.info("✏️ Cập nhật thông tin giao hàng cho hóa đơn {}", hoaDonId);

            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

            if (!"CHO".equals(hoaDon.getTrangThaiHoaDon())) {
                throw new RuntimeException("Chỉ có thể cập nhật hóa đơn đang chờ");
            }

            // Cập nhật thông tin nếu có
            if (request.getTenNguoiNhan() != null) {
                hoaDon.setTenNguoiDung(request.getTenNguoiNhan());
            }
            if (request.getSdt() != null) {
                hoaDon.setSdt(request.getSdt());
            }
            if (request.getEmail() != null) {
                hoaDon.setEmail(request.getEmail());
            }
            if (request.getDiaChi() != null) {
                hoaDon.setDiaChi(request.getDiaChi());
            }
            if (request.getGhiChu() != null) {
                hoaDon.setGhiChu(request.getGhiChu());
            }
            if (request.getPhiVanChuyen() != null) {
                hoaDon.setPhiVanChuyen(request.getPhiVanChuyen());
                // Cập nhật lại tổng thanh toán
                BigDecimal tongThanhToan = hoaDon.getTongTien().add(request.getPhiVanChuyen());
                hoaDon.setTongThanhToan(tongThanhToan);
            }

            hoaDon.setNgayCapNhat(new Date());
            hoaDonRepository.save(hoaDon);

            log.info("✅ Đã cập nhật thông tin giao hàng");

            return layTongQuanHoaDonCho(hoaDonId);

        } catch (Exception e) {
            log.error("❌ Lỗi cập nhật thông tin giao hàng: {}", e.getMessage());
            throw new RuntimeException("Không thể cập nhật: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public HoaDonResponse xacNhanGiaoHang(Integer hoaDonId) {
        try {
            log.info("✅ Xác nhận giao hàng cho hóa đơn {}", hoaDonId);

            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

            // Kiểm tra trạng thái
            if (!"CHO".equals(hoaDon.getTrangThaiHoaDon())) {
                throw new RuntimeException("Hóa đơn không ở trạng thái chờ");
            }

            // Kiểm tra thông tin giao hàng
            if (!StringUtils.hasText(hoaDon.getDiaChi())) {
                throw new RuntimeException("Chưa có địa chỉ giao hàng");
            }
            if (!StringUtils.hasText(hoaDon.getSdt())) {
                throw new RuntimeException("Chưa có số điện thoại người nhận");
            }

            // Cập nhật trạng thái và thời gian
            hoaDon.setTrangThaiHoaDon("Đã xác nhận");
            hoaDon.setNgayXacNhan(new Date());
            hoaDon.setNgayCapNhat(new Date());

            // Nếu là COD thì set thời gian vận chuyển
            if ("COD".equals(hoaDon.getPhuongThucThanhToan())) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, 3); // Dự kiến giao sau 3 ngày
                hoaDon.setThoiGianVanChuyen(cal.getTime());
            }

            hoaDonRepository.save(hoaDon);

            // Cập nhật chi tiết hóa đơn
            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
            for (HoaDonChiTiet chiTiet : chiTiets) {
                chiTiet.setTrangThaiHoaDon("Đã xác nhận");
                hoaDonChiTietRepository.save(chiTiet);
            }

            // Tạo lịch sử
            taoLichSuHoaDon(hoaDon, "Xác nhận đơn giao hàng", hoaDon.getNhanVien());

            log.info("✅ Đã xác nhận giao hàng");

            return mapToHoaDonResponse(hoaDon);

        } catch (Exception e) {
            log.error("❌ Lỗi xác nhận giao hàng: {}", e.getMessage());
            throw new RuntimeException("Không thể xác nhận: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public HoaDonChoTongQuanResponse huyGiaoHang(Integer hoaDonId) {
        try {
            log.info("🚫 Hủy giao hàng cho hóa đơn {}", hoaDonId);

            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

            if (!"CHO".equals(hoaDon.getTrangThaiHoaDon()) &&
                    !"Đã xác nhận".equals(hoaDon.getTrangThaiHoaDon())) {
                throw new RuntimeException("Không thể hủy giao hàng cho hóa đơn này");
            }

            // Chuyển về bán tại quầy
            hoaDon.setLoaiHoaDon("OFFLINE");
            hoaDon.setDiaChi("");
            hoaDon.setPhiVanChuyen(BigDecimal.ZERO);
            hoaDon.setPhuongThucThanhToan("CASH");
            hoaDon.setTrangThaiHoaDon("CHO");

            // Cập nhật lại tổng thanh toán (bỏ phí ship)
            hoaDon.setTongThanhToan(hoaDon.getTongTien());

            hoaDon.setNgayCapNhat(new Date());
            hoaDonRepository.save(hoaDon);

            // Tạo lịch sử
            taoLichSuHoaDon(hoaDon, "Hủy giao hàng, chuyển về bán tại quầy", hoaDon.getNhanVien());

            log.info("✅ Đã hủy giao hàng");

            return layTongQuanHoaDonCho(hoaDonId);

        } catch (Exception e) {
            log.error("❌ Lỗi hủy giao hàng: {}", e.getMessage());
            throw new RuntimeException("Không thể hủy: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public HoaDonResponse capNhatDangGiao(Integer hoaDonId) {
        try {
            log.info("🚚 Cập nhật đang giao hàng cho hóa đơn {}", hoaDonId);

            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

            if (!"Đã xác nhận".equals(hoaDon.getTrangThaiHoaDon())) {
                throw new RuntimeException("Chỉ có thể giao hàng cho đơn đã xác nhận");
            }

            hoaDon.setTrangThaiHoaDon("Đang giao");
            hoaDon.setNgayGiaoHang(new Date());
            hoaDon.setNgayCapNhat(new Date());

            hoaDonRepository.save(hoaDon);

            // Tạo lịch sử
            taoLichSuHoaDon(hoaDon, "Bắt đầu giao hàng", hoaDon.getNhanVien());

            log.info("✅ Đã cập nhật đang giao hàng");

            return mapToHoaDonResponse(hoaDon);

        } catch (Exception e) {
            log.error("❌ Lỗi cập nhật đang giao: {}", e.getMessage());
            throw new RuntimeException("Không thể cập nhật: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public HoaDonResponse xacNhanDaGiao(Integer hoaDonId) {
        try {
            log.info("✅ Xác nhận đã giao hàng cho hóa đơn {}", hoaDonId);

            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

            if (!"Đang giao".equals(hoaDon.getTrangThaiHoaDon())) {
                throw new RuntimeException("Chỉ có thể xác nhận cho đơn đang giao");
            }

            hoaDon.setTrangThaiHoaDon("Hoàn thành");
            hoaDon.setNgayNhanHang(new Date());
            hoaDon.setNgayHoanThanh(new Date());
            hoaDon.setNgayCapNhat(new Date());

            hoaDonRepository.save(hoaDon);

            // Cập nhật chi tiết
            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
            for (HoaDonChiTiet chiTiet : chiTiets) {
                chiTiet.setTrangThaiHoaDon("Hoàn thành");
                hoaDonChiTietRepository.save(chiTiet);
            }

            // Cộng điểm cho khách hàng nếu có
            if (hoaDon.getKhachHang() != null) {
                congDiemKhachHang(hoaDon.getKhachHang().getId(), hoaDon.getTongThanhToan().doubleValue());
            }

            // Tạo lịch sử
            taoLichSuHoaDon(hoaDon, "Giao hàng thành công", hoaDon.getNhanVien());

            log.info("✅ Đã xác nhận giao hàng thành công");

            return mapToHoaDonResponse(hoaDon);

        } catch (Exception e) {
            log.error("❌ Lỗi xác nhận đã giao: {}", e.getMessage());
            throw new RuntimeException("Không thể xác nhận: " + e.getMessage());
        }
    }

    // Helper method
    private BigDecimal tinhPhiVanChuyenMacDinh(String diaChi, BigDecimal tongTien) {
        if (tongTien != null && tongTien.compareTo(BigDecimal.valueOf(500000)) >= 0) {
            return BigDecimal.ZERO; // Miễn phí cho đơn >= 500k
        }

        if (diaChi != null) {
            String diaChiLower = diaChi.toLowerCase();
            if (diaChiLower.contains("hà nội") || diaChiLower.contains("ha noi")) {
                return BigDecimal.valueOf(30000);
            } else if (diaChiLower.contains("hồ chí minh") || diaChiLower.contains("hcm")) {
                return BigDecimal.valueOf(35000);
            }
        }

        return BigDecimal.valueOf(50000); // Mặc định 50k
    }
    private void xuLyThanhToanTienMat(ThanhToanRequest request, BigDecimal tongTienCanThanhToan) {
        if (request.getTienMat() == null || request.getTienMat().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Số tiền mặt phải lớn hơn 0");
        }

        if (request.getTienMat().compareTo(tongTienCanThanhToan) < 0) {
            throw new RuntimeException("Số tiền mặt không đủ để thanh toán");
        }

        log.info("💵 Thanh toán tiền mặt: {} - Tổng cần thanh toán: {}",
                request.getTienMat(), tongTienCanThanhToan);
    }

    /**
     * Xử lý thanh toán bằng chuyển khoản
     */
    private void xuLyThanhToanChuyenKhoan(ThanhToanRequest request, BigDecimal tongTienCanThanhToan) {
        if (request.getTienChuyenKhoan() == null || request.getTienChuyenKhoan().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Số tiền chuyển khoản phải lớn hơn 0");
        }

        if (request.getTienChuyenKhoan().compareTo(tongTienCanThanhToan) < 0) {
            throw new RuntimeException("Số tiền chuyển khoản không đủ để thanh toán");
        }

        log.info("🏦 Thanh toán chuyển khoản: {} - Tổng cần thanh toán: {}",
                request.getTienChuyenKhoan(), tongTienCanThanhToan);
    }

    /**
     * Xử lý thanh toán kết hợp (tiền mặt + chuyển khoản)
     */
    private void xuLyThanhToanKetHop(ThanhToanRequest request, BigDecimal tongTienCanThanhToan) {
        BigDecimal tienMat = request.getTienMat() != null ? request.getTienMat() : BigDecimal.ZERO;
        BigDecimal tienChuyenKhoan = request.getTienChuyenKhoan() != null ? request.getTienChuyenKhoan() : BigDecimal.ZERO;

        if (tienMat.compareTo(BigDecimal.ZERO) <= 0 && tienChuyenKhoan.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Phải có ít nhất một phương thức thanh toán có giá trị > 0");
        }

        BigDecimal tongTienNhan = tienMat.add(tienChuyenKhoan);
        if (tongTienNhan.compareTo(tongTienCanThanhToan) < 0) {
            throw new RuntimeException("Tổng tiền thanh toán không đủ. Cần: " +
                    tongTienCanThanhToan + ", Có: " + tongTienNhan);
        }

        log.info("💰 Thanh toán kết hợp - Tiền mặt: {}, Chuyển khoản: {}, Tổng: {}",
                tienMat, tienChuyenKhoan, tongTienNhan);
    }

    /**
     * Xử lý trừ điểm tích lũy
     */
    private void xuLyDiemTichLuy(KhachHang khachHang, Integer diemSuDung) {
        if (khachHang != null && khachHang.getViDiem() != null) {
            ViDiem viDiem = khachHang.getViDiem();
            Double diemHienTai = viDiem.getTongDiem() - viDiem.getSoDiemDaDung();

            if (diemSuDung > diemHienTai) {
                throw new RuntimeException("Không đủ điểm để sử dụng. Có: " + diemHienTai + " điểm");
            }

            viDiem.setSoDiemDaDung(viDiem.getSoDiemDaDung() + diemSuDung);
            viDiem.setNgayCapNhat(new Date());
            viDiemRepository.save(viDiem);

            log.info("🎯 Sử dụng {} điểm tích lũy", diemSuDung);
        }
    }

    /**
     * Tạo mô tả cho lịch sử thanh toán
     */
    private String taoMoTaThanhToan(ThanhToanRequest request) {
        StringBuilder moTa = new StringBuilder("Thanh toán thành công - ");

        switch (request.getPhuongThucThanhToan()) {
            case "TIEN_MAT":
                moTa.append("Tiền mặt: ").append(formatMoney(request.getTienMat()));
                break;
            case "CHUYEN_KHOAN":
                moTa.append("Chuyển khoản: ").append(formatMoney(request.getTienChuyenKhoan()));
                break;
            case "KET_HOP":
                moTa.append("Kết hợp - Tiền mặt: ").append(formatMoney(request.getTienMat()))
                        .append(", Chuyển khoản: ").append(formatMoney(request.getTienChuyenKhoan()));
                break;
        }

        if (request.getDiemSuDung() != null && request.getDiemSuDung() > 0) {
            moTa.append(", Điểm sử dụng: ").append(request.getDiemSuDung());
        }

        return moTa.toString();
    }

    /**
     * Format tiền tệ
     */
    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0₫";
        return String.format("%,.0f₫", amount);
    }

    @Override
    @Transactional
    public ThanhToanResponse thanhToanHoaDonChiTiet(Integer hoaDonId, ThanhToanRequest request) {
        try {
            log.info("💰 Thanh toán chi tiết hóa đơn ID: {} với phương thức: {}", hoaDonId, request.getPhuongThucThanhToan());

            // ===== BƯỚC 1: KIỂM TRA TỒN KHO NGAY ĐẦU =====
            log.info("🔍 Bước 1: Kiểm tra tồn kho mới nhất...");
            List<InventoryCheckResponse> inventoryChecks = kiemTraTonKhoTruocThanhToan(hoaDonId);

            List<InventoryCheckResponse> sanPhamThieu = inventoryChecks.stream()
                    .filter(check -> !check.getCoTheban())
                    .collect(Collectors.toList());

            if (!sanPhamThieu.isEmpty()) {
                // Tạo thông báo chi tiết về các sản phẩm thiếu
                StringBuilder errorMessage = new StringBuilder("Không thể thanh toán do thiếu tồn kho:\n");
                for (InventoryCheckResponse item : sanPhamThieu) {
                    errorMessage.append(String.format("- %s (%s - %s): %s\n",
                            item.getTenSanPham(), item.getMauSac(), item.getKichCo(), item.getThongBao()));
                }

                log.warn("⚠️ Từ chối thanh toán do thiếu tồn kho: {}", errorMessage.toString());

                return ThanhToanResponse.builder()
                        .hoaDonId(hoaDonId)
                        .thanhCong(false)
                        .thongBaoThanhToan(errorMessage.toString().trim())
                        .kiemTraTonKho(inventoryChecks)
                        .build();
            }

            // ===== VALIDATION CƠ BẢN =====
            if (hoaDonId == null || request == null) {
                throw new RuntimeException("Thông tin thanh toán không hợp lệ");
            }

            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

            if (!"CHO".equals(hoaDon.getTrangThaiHoaDon())) {
                throw new RuntimeException("Hóa đơn không ở trạng thái chờ thanh toán");
            }

            log.info("✅ Bước 1 hoàn thành: Tồn kho đủ để thanh toán");

            // ===== BƯỚC 2: TÍNH TỔNG TIỀN BAN ĐẦU =====
            BigDecimal tongTienGoc = hoaDonChiTietRepository.calculateTotalAmountByHoaDonId(hoaDonId);
            BigDecimal tongTienCanThanhToan = tongTienGoc;
            BigDecimal tienThua = BigDecimal.ZERO;

            log.info("📊 Tổng tiền gốc: {}", tongTienGoc);

            // ===== BƯỚC 3: XỬ LÝ VOUCHER =====
            Voucher voucherApplied = null;
            BigDecimal giaTriGiamVoucher = BigDecimal.ZERO;

            if (request.getVoucherId() != null) {
                log.info("🎫 Xử lý voucher ID: {}", request.getVoucherId());

                voucherApplied = voucherRepository.findById(request.getVoucherId()).orElse(null);
                if (voucherApplied == null) {
                    throw new RuntimeException("Không tìm thấy voucher");
                }

                // Kiểm tra tính hợp lệ của voucher
                Date currentDate = new Date();
                if (!voucherApplied.isValid()) {
                    throw new RuntimeException("Voucher không hợp lệ");
                }

                if (voucherApplied.getSoLuong() <= 0) {
                    throw new RuntimeException("Voucher đã hết lượt sử dụng");
                }

                if (currentDate.before(voucherApplied.getNgayBatDau()) ||
                        currentDate.after(voucherApplied.getNgayKetThuc())) {
                    throw new RuntimeException("Voucher ngoài thời gian sử dụng");
                }

                // Kiểm tra giá trị đơn hàng tối thiểu
                if (tongTienGoc.doubleValue() < voucherApplied.getGiaTriGiamToiThieu()) {
                    throw new RuntimeException("Đơn hàng chưa đủ giá trị tối thiểu để áp dụng voucher: " +
                            formatMoney(BigDecimal.valueOf(voucherApplied.getGiaTriGiamToiThieu())));
                }

                // Tính giá trị giảm
                Double giaTriGiam = voucherApplied.tinhGiaTriGiam(tongTienGoc.doubleValue());
                giaTriGiamVoucher = BigDecimal.valueOf(giaTriGiam);

                // Trừ voucher khỏi tổng tiền
                tongTienCanThanhToan = tongTienCanThanhToan.subtract(giaTriGiamVoucher);
                tongTienCanThanhToan = tongTienCanThanhToan.max(BigDecimal.ZERO);

                log.info("✅ Voucher áp dụng: {} - Giảm: {}", voucherApplied.getTenVoucher(), giaTriGiamVoucher);
            }

            // ===== BƯỚC 4: XỬ LÝ ĐIỂM TÍCH LŨY =====
            BigDecimal giaTriDiem = BigDecimal.ZERO;
            if (request.getDiemSuDung() != null && request.getDiemSuDung() > 0) {
                log.info("🎯 Xử lý điểm tích lũy: {} điểm", request.getDiemSuDung());

                // Kiểm tra khách hàng có đủ điểm không
                if (request.getKhachHangId() == null) {
                    throw new RuntimeException("Cần có khách hàng để sử dụng điểm tích lũy");
                }

                KhachHang khachHang = khachHangRepository.findById(request.getKhachHangId()).orElse(null);
                if (khachHang == null || khachHang.getViDiem() == null) {
                    throw new RuntimeException("Khách hàng không có ví điểm");
                }

                ViDiem viDiem = khachHang.getViDiem();
                Double diemHienTai = viDiem.getTongDiem() - viDiem.getSoDiemDaDung();

                if (request.getDiemSuDung() > diemHienTai) {
                    throw new RuntimeException("Không đủ điểm để sử dụng. Có: " + diemHienTai + " điểm");
                }

                // Tính giá trị điểm (1 điểm = 1000 VND)
                giaTriDiem = BigDecimal.valueOf(request.getDiemSuDung() * 1000);
                tongTienCanThanhToan = tongTienCanThanhToan.subtract(giaTriDiem);
                tongTienCanThanhToan = tongTienCanThanhToan.max(BigDecimal.ZERO);

                log.info("💰 Sử dụng {} điểm = {}", request.getDiemSuDung(), formatMoney(giaTriDiem));
            }

            log.info("💵 Tổng tiền cần thanh toán sau giảm giá: {}", tongTienCanThanhToan);

            // ===== BƯỚC 5: VALIDATION VÀ XỬ LÝ PHƯƠNG THỨC THANH TOÁN =====
            if ("TIEN_MAT".equals(request.getPhuongThucThanhToan())) {
                if (request.getTienMat() == null || request.getTienMat().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException("Số tiền mặt phải lớn hơn 0");
                }
                if (request.getTienMat().compareTo(tongTienCanThanhToan) < 0) {
                    throw new RuntimeException("Số tiền mặt không đủ để thanh toán. Cần: " +
                            formatMoney(tongTienCanThanhToan));
                }
                tienThua = request.getTienMat().subtract(tongTienCanThanhToan);
                log.info("💵 Thanh toán tiền mặt: {} - Tiền thừa: {}",
                        formatMoney(request.getTienMat()), formatMoney(tienThua));

            } else if ("CHUYEN_KHOAN".equals(request.getPhuongThucThanhToan())) {
                if (request.getTienChuyenKhoan() == null || request.getTienChuyenKhoan().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException("Số tiền chuyển khoản phải lớn hơn 0");
                }
                if (request.getTienChuyenKhoan().compareTo(tongTienCanThanhToan) < 0) {
                    throw new RuntimeException("Số tiền chuyển khoản không đủ để thanh toán. Cần: " +
                            formatMoney(tongTienCanThanhToan));
                }
                log.info("🏦 Thanh toán chuyển khoản: {}", formatMoney(request.getTienChuyenKhoan()));

            } else if ("KET_HOP".equals(request.getPhuongThucThanhToan())) {
                BigDecimal tienMat = request.getTienMat() != null ? request.getTienMat() : BigDecimal.ZERO;
                BigDecimal tienChuyenKhoan = request.getTienChuyenKhoan() != null ? request.getTienChuyenKhoan() : BigDecimal.ZERO;

                if (tienMat.compareTo(BigDecimal.ZERO) <= 0 && tienChuyenKhoan.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException("Phải có ít nhất một phương thức thanh toán có giá trị > 0");
                }

                BigDecimal tongTienNhan = tienMat.add(tienChuyenKhoan);
                if (tongTienNhan.compareTo(tongTienCanThanhToan) < 0) {
                    throw new RuntimeException("Tổng tiền thanh toán không đủ. Cần: " +
                            formatMoney(tongTienCanThanhToan) + ", Có: " + formatMoney(tongTienNhan));
                }

                tienThua = tongTienNhan.subtract(tongTienCanThanhToan);
                log.info("💰 Thanh toán kết hợp - Tiền mặt: {}, Chuyển khoản: {}, Tiền thừa: {}",
                        formatMoney(tienMat), formatMoney(tienChuyenKhoan), formatMoney(tienThua));

            } else {
                throw new RuntimeException("Phương thức thanh toán không hợp lệ: " + request.getPhuongThucThanhToan());
            }

            // ===== BƯỚC 6: CẬP NHẬT THÔNG TIN HÓA ĐƠN =====
            // Cập nhật thông tin khách hàng nếu có
            if (request.getKhachHangId() != null) {
                KhachHang khachHang = khachHangRepository.findById(request.getKhachHangId()).orElse(null);
                if (khachHang != null) {
                    hoaDon.setKhachHang(khachHang);

                    // Lấy email từ TaiKhoan
                    if (khachHang.getTaiKhoan() != null && StringUtils.hasText(khachHang.getTaiKhoan().getEmail())) {
                        hoaDon.setEmail(khachHang.getTaiKhoan().getEmail());
                    } else {
                        hoaDon.setEmail("");
                    }

                    hoaDon.setSdt(khachHang.getSdt());
                    hoaDon.setTenNguoiDung(khachHang.getHoTen());

                    // Lấy địa chỉ từ TaiKhoan -> DiaChi
                    if (khachHang.getTaiKhoan() != null) {
                        Optional<DiaChi> diaChiOpt = diaChiRepository.findByTaiKhoanIdAndIsDefaultTrue(khachHang.getTaiKhoan().getId());
                        if (diaChiOpt.isPresent()) {
                            DiaChi diaChi = diaChiOpt.get();
                            String diaChiDayDu = diaChi.getDiaChiChiTiet() + ", " +
                                    diaChi.getTenPhuong() + ", " +
                                    diaChi.getTenTinh();
                            hoaDon.setDiaChi(diaChiDayDu);
                        }
                    }

                    log.info("👤 Cập nhật khách hàng: {}", khachHang.getHoTen());
                }
            }

            // Cập nhật trạng thái và thông tin thanh toán
            hoaDon.setTrangThaiHoaDon("COMPLETED");
            hoaDon.setLoaiHoaDon(request.getLoaiHoaDon() != null ? request.getLoaiHoaDon() : "OFFLINE");
            hoaDon.setPhuongThucThanhToan(request.getPhuongThucThanhToan());
            hoaDon.setNgayHoanThanh(new Date());
            hoaDon.setNgayCapNhat(new Date());
            hoaDon.setGhiChu(request.getGhiChu());
            hoaDon.setDiemSuDung(request.getDiemSuDung());

            // Cập nhật tổng tiền đúng cách
            hoaDon.setTongTien(tongTienGoc);                // Tổng tiền gốc
            hoaDon.setTongThanhToan(tongTienCanThanhToan);   // Tổng tiền sau khi đã trừ voucher và điểm

            HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

            // ===== BƯỚC 7: XỬ LÝ VOUCHER VÀ ĐIỂM SAU KHI THANH TOÁN THÀNH CÔNG =====
            if (voucherApplied != null) {
                // Tạo chi tiết voucher SAU khi thanh toán thành công
                taoChiTietVoucherNeuChuaCo(savedHoaDon, voucherApplied, tongTienGoc, giaTriGiamVoucher, tongTienCanThanhToan);

                // Giảm số lượng voucher
                voucherApplied.setSoLuong(voucherApplied.getSoLuong() - 1);
                voucherRepository.save(voucherApplied);
                log.info("🎫 Đã sử dụng voucher: {}", voucherApplied.getTenVoucher());
            }

            if (request.getDiemSuDung() != null && request.getDiemSuDung() > 0 && savedHoaDon.getKhachHang() != null) {
                // Trừ điểm đã sử dụng
                xuLyDiemTichLuy(savedHoaDon.getKhachHang(), request.getDiemSuDung());
                log.info("🎯 Đã sử dụng {} điểm", request.getDiemSuDung());
            }

            // ===== BƯỚC 8: CẬP NHẬT CHI TIẾT HÓA ĐƠN =====
            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
            for (HoaDonChiTiet chiTiet : chiTiets) {
                chiTiet.setTrangThaiHoaDon("COMPLETED");
                chiTiet.setNgayCapNhat(new Date());
                hoaDonChiTietRepository.save(chiTiet);
            }

            // ===== BƯỚC 9: TẠO LỊCH SỬ HÓA ĐƠN =====
            String moTaThanhToan = taoMoTaThanhToanChiTiet(request, voucherApplied, giaTriGiamVoucher, giaTriDiem, tienThua);
            taoLichSuHoaDon(savedHoaDon, moTaThanhToan, savedHoaDon.getNhanVien());

            // ===== BƯỚC 10: CỘNG ĐIỂM CHO KHÁCH HÀNG =====
            if (savedHoaDon.getKhachHang() != null) {
                congDiemKhachHang(savedHoaDon.getKhachHang().getId(), savedHoaDon.getTongThanhToan().doubleValue());
                log.info("💎 Đã cộng điểm cho khách hàng");
            }

            // ===== BƯỚC 11: TẠO RESPONSE =====
            String thongBaoThanhToan = taoThongBaoThanhToan(request, tienThua, voucherApplied, giaTriGiamVoucher);

            ThanhToanResponse response = ThanhToanResponse.builder()
                    .hoaDonId(savedHoaDon.getId())
                    .maHoaDon(savedHoaDon.getMaHoaDon())
                    .trangThaiHoaDon(savedHoaDon.getTrangThaiHoaDon())
                    .phuongThucThanhToan(request.getPhuongThucThanhToan())
                    .tongTienCanThanhToan(tongTienCanThanhToan) // Tổng sau giảm giá
                    .tienMat(request.getTienMat())
                    .tienChuyenKhoan(request.getTienChuyenKhoan())
                    .tienThua(tienThua)
                    .voucherId(request.getVoucherId())
                    .tenVoucher(voucherApplied != null ? voucherApplied.getTenVoucher() : null)
                    .giaTriGiamVoucher(giaTriGiamVoucher)
                    .diemSuDung(request.getDiemSuDung())
                    .giaTriDiem(giaTriDiem)
                    .khachHangId(savedHoaDon.getKhachHang() != null ? savedHoaDon.getKhachHang().getId() : null)
                    .tenKhachHang(savedHoaDon.getKhachHang() != null ? savedHoaDon.getKhachHang().getHoTen() : "Khách lẻ")
                    .sdtKhachHang(savedHoaDon.getKhachHang() != null ? savedHoaDon.getKhachHang().getSdt() : "")
                    .ngayThanhToan(savedHoaDon.getNgayCapNhat())
                    .ngayHoanThanh(savedHoaDon.getNgayHoanThanh())
                    .nhanVienId(savedHoaDon.getNhanVien() != null ? savedHoaDon.getNhanVien().getId() : null)
                    .tenNhanVien(savedHoaDon.getNhanVien() != null ? savedHoaDon.getNhanVien().getHoTen() : "")
                    .ghiChu(request.getGhiChu())
                    .thongBaoThanhToan(thongBaoThanhToan)
                    .thanhCong(true)
                    .kiemTraTonKho(inventoryChecks) // Trả về kết quả kiểm tra tồn kho
                    .build();

            log.info("✅ Thanh toán chi tiết thành công - Hóa đơn: {} - Tổng: {} - Phương thức: {}",
                    savedHoaDon.getMaHoaDon(), formatMoney(tongTienCanThanhToan), request.getPhuongThucThanhToan());

            return response;

        } catch (RuntimeException e) {
            log.error("❌ Lỗi nghiệp vụ thanh toán: {}", e.getMessage());
            return ThanhToanResponse.builder()
                    .hoaDonId(hoaDonId)
                    .thanhCong(false)
                    .thongBaoThanhToan("Thanh toán thất bại: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("❌ Lỗi hệ thống thanh toán: {}", e.getMessage(), e);
            return ThanhToanResponse.builder()
                    .hoaDonId(hoaDonId)
                    .thanhCong(false)
                    .thongBaoThanhToan("Lỗi hệ thống: Vui lòng thử lại sau")
                    .build();
        }
    }

    /**
     * Tạo mô tả chi tiết cho lịch sử thanh toán
     */
    private String taoMoTaThanhToanChiTiet(ThanhToanRequest request, Voucher voucher,
                                           BigDecimal giaTriGiamVoucher, BigDecimal giaTriDiem, BigDecimal tienThua) {
        StringBuilder moTa = new StringBuilder("Thanh toán thành công - ");

        // Phương thức thanh toán
        switch (request.getPhuongThucThanhToan()) {
            case "TIEN_MAT":
                moTa.append("Tiền mặt: ").append(formatMoney(request.getTienMat()));
                break;
            case "CHUYEN_KHOAN":
                moTa.append("Chuyển khoản: ").append(formatMoney(request.getTienChuyenKhoan()));
                break;
            case "KET_HOP":
                moTa.append("Kết hợp - Tiền mặt: ").append(formatMoney(request.getTienMat()))
                        .append(", Chuyển khoản: ").append(formatMoney(request.getTienChuyenKhoan()));
                break;
        }

        // Voucher
        if (voucher != null && giaTriGiamVoucher.compareTo(BigDecimal.ZERO) > 0) {
            moTa.append(", Voucher '").append(voucher.getTenVoucher())
                    .append("': -").append(formatMoney(giaTriGiamVoucher));
        }

        // Điểm tích lũy
        if (request.getDiemSuDung() != null && request.getDiemSuDung() > 0) {
            moTa.append(", Điểm sử dụng: ").append(request.getDiemSuDung())
                    .append(" điểm (-").append(formatMoney(giaTriDiem)).append(")");
        }

        // Tiền thừa
        if (tienThua.compareTo(BigDecimal.ZERO) > 0) {
            moTa.append(", Tiền thừa: ").append(formatMoney(tienThua));
        }

        return moTa.toString();
    }

    /**
     * Tạo thông báo thanh toán cho frontend
     */
    private String taoThongBaoThanhToan(ThanhToanRequest request, BigDecimal tienThua,
                                        Voucher voucher, BigDecimal giaTriGiamVoucher) {
        StringBuilder thongBao = new StringBuilder();

        switch (request.getPhuongThucThanhToan()) {
            case "TIEN_MAT":
                thongBao.append("Thanh toán tiền mặt thành công");
                if (tienThua.compareTo(BigDecimal.ZERO) > 0) {
                    thongBao.append(". Tiền thừa: ").append(formatMoney(tienThua));
                }
                break;
            case "CHUYEN_KHOAN":
                thongBao.append("Thanh toán chuyển khoản thành công");
                break;
            case "KET_HOP":
                thongBao.append("Thanh toán kết hợp thành công");
                if (tienThua.compareTo(BigDecimal.ZERO) > 0) {
                    thongBao.append(". Tiền thừa: ").append(formatMoney(tienThua));
                }
                break;
        }

        if (voucher != null && giaTriGiamVoucher.compareTo(BigDecimal.ZERO) > 0) {
            thongBao.append(". Voucher đã giảm ").append(formatMoney(giaTriGiamVoucher));
        }

        return thongBao.toString();
    }
    @Override
    @Transactional(readOnly = true)
    public List<InventoryCheckResponse> kiemTraTonKhoTruocThanhToan(Integer hoaDonId) {
        try {
            logger.info("📦 Bắt đầu kiểm tra tồn kho cho hóa đơn: {}", hoaDonId);

            // Validate input
            if (hoaDonId == null || hoaDonId <= 0) {
                throw new RuntimeException("ID hóa đơn không hợp lệ: " + hoaDonId);
            }

            // Kiểm tra hóa đơn tồn tại và trạng thái
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + hoaDonId));

            if (!"CHO".equals(hoaDon.getTrangThaiHoaDon())) {
                throw new RuntimeException("Hóa đơn không ở trạng thái chờ thanh toán. Trạng thái hiện tại: " + hoaDon.getTrangThaiHoaDon());
            }

            // Lấy danh sách chi tiết hóa đơn
            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
            if (chiTiets.isEmpty()) {
                throw new RuntimeException("Hóa đơn chưa có sản phẩm nào");
            }

            List<InventoryCheckResponse> responses = new ArrayList<>();

            logger.info("🔍 Kiểm tra {} sản phẩm trong hóa đơn", chiTiets.size());

            for (HoaDonChiTiet chiTiet : chiTiets) {
                try {
                    // Kiểm tra null safety
                    if (chiTiet.getChiTietSanPham() == null) {
                        logger.warn("⚠️ Chi tiết hóa đơn {} không có sản phẩm", chiTiet.getId());

                        responses.add(InventoryCheckResponse.builder()
                                .chiTietSanPhamId(null)
                                .tenSanPham("Sản phẩm không xác định")
                                .mauSac("N/A")
                                .kichCo("N/A")
                                .soLuongTon(0)
                                .soLuongCanBan(chiTiet.getSoLuong())
                                .coTheban(false)
                                .thongBao("Lỗi: Không tìm thấy thông tin sản phẩm")
                                .build());
                        continue;
                    }

                    ChiTietSanPham sanPham = chiTiet.getChiTietSanPham();

                    // Refresh dữ liệu từ database để có số liệu mới nhất
                    sanPham = chiTietSanPhamRepository.findById(sanPham.getId())
                            .orElse(sanPham);

                    // Số lượng cần bán
                    int soLuongCanBan = chiTiet.getSoLuong();

                    // Số lượng tồn kho hiện tại (đã bị trừ khi thêm vào hóa đơn)
                    int soLuongTonHienTai = sanPham.getSoLuong();

                    // Logic kiểm tra: nếu tồn kho >= 0 thì có thể bán (vì đã trừ khi thêm vào giỏ)
                    boolean coTheBan = soLuongTonHienTai >= 0;
                    String thongBao;

                    if (!coTheBan) {
                        int soLuongThieu = Math.abs(soLuongTonHienTai);
                        thongBao = String.format("Thiếu %d sản phẩm trong kho", soLuongThieu);
                    } else {
                        thongBao = "Đủ hàng";
                    }

                    // Lấy thông tin sản phẩm an toàn
                    String tenSanPham = "N/A";
                    String mauSac = "N/A";
                    String kichCo = "N/A";

                    try {
                        SanPham sp = sanPham.getSanPham();
                        if (sp != null && sp.getTenSanPham() != null) {
                            tenSanPham = sp.getTenSanPham();
                        }
                    } catch (Exception e) {
                        logger.debug("Không thể lấy tên sản phẩm: {}", e.getMessage());
                    }

                    try {
                        MauSac ms = sanPham.getMauSac();
                        if (ms != null && ms.getTenMauSac() != null) {
                            mauSac = ms.getTenMauSac();
                        }
                    } catch (Exception e) {
                        logger.debug("Không thể lấy màu sắc: {}", e.getMessage());
                    }

                    try {
                        KichCo kc = sanPham.getKichCo();
                        if (kc != null && kc.getTenKichCo() != null) {
                            kichCo = kc.getTenKichCo();
                        }
                    } catch (Exception e) {
                        logger.debug("Không thể lấy kích cỡ: {}", e.getMessage());
                    }

                    responses.add(InventoryCheckResponse.builder()
                            .chiTietSanPhamId(sanPham.getId())
                            .tenSanPham(tenSanPham)
                            .mauSac(mauSac)
                            .kichCo(kichCo)
                            .soLuongTon(Math.max(0, soLuongTonHienTai)) // Hiển thị >= 0
                            .soLuongCanBan(soLuongCanBan)
                            .coTheban(coTheBan)
                            .thongBao(thongBao)
                            .build());

                    logger.debug("✅ Kiểm tra sản phẩm '{}': Tồn={}, Cần={}, ĐủHàng={}",
                            tenSanPham, soLuongTonHienTai, soLuongCanBan, coTheBan);

                } catch (Exception e) {
                    logger.error("❌ Lỗi kiểm tra chi tiết sản phẩm {}: {}",
                            chiTiet.getId(), e.getMessage());

                    // Tạo response lỗi cho sản phẩm này
                    responses.add(InventoryCheckResponse.builder()
                            .chiTietSanPhamId(chiTiet.getChiTietSanPham() != null ?
                                    chiTiet.getChiTietSanPham().getId() : null)
                            .tenSanPham("Lỗi kiểm tra sản phẩm")
                            .mauSac("N/A")
                            .kichCo("N/A")
                            .soLuongTon(0)
                            .soLuongCanBan(chiTiet.getSoLuong())
                            .coTheban(false)
                            .thongBao("Lỗi hệ thống khi kiểm tra sản phẩm")
                            .build());
                }
            }

            // Log tổng kết
            long soSanPhamDuHang = responses.stream().filter(InventoryCheckResponse::getCoTheban).count();
            long soSanPhamThieuHang = responses.size() - soSanPhamDuHang;

            logger.info("📊 Kết quả kiểm tra tồn kho: {}/{} sản phẩm đủ hàng, {} sản phẩm thiếu hàng",
                    soSanPhamDuHang, responses.size(), soSanPhamThieuHang);

            if (soSanPhamThieuHang > 0) {
                logger.warn("⚠️ Cảnh báo: Có {} sản phẩm không đủ tồn kho", soSanPhamThieuHang);
            }

            return responses;

        } catch (RuntimeException e) {
            // Ném lại exception nghiệp vụ để Controller xử lý
            throw e;
        } catch (Exception e) {
            logger.error("❌ Lỗi hệ thống khi kiểm tra tồn kho hóa đơn {}: {}", hoaDonId, e.getMessage(), e);
            throw new RuntimeException("Lỗi hệ thống khi kiểm tra tồn kho: " + e.getMessage());
        }
    }
}
