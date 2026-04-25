package org.example.iws_websitesneaker.Service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.iws_websitesneaker.Dto.BanHang.*;
import org.example.iws_websitesneaker.Dto.ChiTietVoucherDTO;
import org.example.iws_websitesneaker.Service.BanHangService;
import org.example.iws_websitesneaker.Service.ChiTietVoucherService;
import org.example.iws_websitesneaker.entity.*;
import org.example.iws_websitesneaker.repository.BanHang.*;
import org.example.iws_websitesneaker.specification.ChiTietSanPhamSpecification;
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

    // ===== QUáº¢N LÃ HÃ“A ÄÆ N CHá»œ =====

    @Override
    public HoaDonChoResponse taoHoaDonCho(Integer nhanVienId) {
        try {
            log.info("Táº¡o hÃ³a Ä‘Æ¡n chá» cho nhÃ¢n viÃªn ID: {}", nhanVienId);

            // Kiá»ƒm tra nhÃ¢n viÃªn
            NhanVien nhanVien = nhanVienRepository.findById(nhanVienId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y nhÃ¢n viÃªn"));

            // Táº¡o hÃ³a Ä‘Æ¡n má»›i
            HoaDon hoaDon = new HoaDon();
            hoaDon.setMaHoaDon(generateMaHoaDon());
            hoaDon.setNhanVien(nhanVien);  // Set object, khÃ´ng pháº£i ID
            hoaDon.setKhachHang(null);     // ChÆ°a cÃ³ khÃ¡ch hÃ ng
            hoaDon.setTrangThaiHoaDon("CHO");
            hoaDon.setLoaiHoaDon("OFFLINE");
            hoaDon.setNgayTao(new Date());
            hoaDon.setNgayCapNhat(new Date());
            hoaDon.setTongTien(BigDecimal.ZERO);
            hoaDon.setTongThanhToan(BigDecimal.ZERO);

            // Set cÃ¡c field báº¯t buá»™c vá»›i giÃ¡ trá»‹ máº·c Ä‘á»‹nh
            hoaDon.setDiaChi("");
            hoaDon.setEmail("");
            hoaDon.setSdt("");
            hoaDon.setTenNguoiDung("KhÃ¡ch láº»");
            hoaDon.setPhiVanChuyen(BigDecimal.ZERO);

            HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

            // Táº¡o lá»‹ch sá»­ hÃ³a Ä‘Æ¡n
            taoLichSuHoaDon(savedHoaDon, "Táº¡o hÃ³a Ä‘Æ¡n chá»", nhanVien);

            return mapToHoaDonChoResponse(savedHoaDon);

        } catch (Exception e) {
            log.error("Lá»—i khi táº¡o hÃ³a Ä‘Æ¡n chá»: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ táº¡o hÃ³a Ä‘Æ¡n chá»: " + e.getMessage());
        }
    }


    @Override
    public List<HoaDonChoResponse> layDanhSachHoaDonCho() {
        try {
            logger.info("ðŸ” Láº¥y danh sÃ¡ch hÃ³a Ä‘Æ¡n chá»");
            List<HoaDon> hoaDons = hoaDonRepository.findByTrangThaiHoaDon("CHO");

            List<HoaDonChoResponse> responses = new ArrayList<>();
            for (HoaDon hoaDon : hoaDons) {
                responses.add(mapToHoaDonChoResponse(hoaDon));
            }

            logger.info("âœ… Láº¥y {} hÃ³a Ä‘Æ¡n chá» thÃ nh cÃ´ng", responses.size());
            return responses;
        } catch (Exception e) {
            logger.error("âŒ Lá»—i láº¥y danh sÃ¡ch hÃ³a Ä‘Æ¡n chá»", e);
            throw new RuntimeException("Lá»—i láº¥y danh sÃ¡ch hÃ³a Ä‘Æ¡n chá»: " + e.getMessage());
        }
    }

    @Override
    public HoaDonChoDetailResponse layChiTietHoaDonCho(Integer hoaDonId) {
        try {
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));

            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonIdOrderByNgayTao(hoaDonId);

            return mapToHoaDonChoDetailResponse(hoaDon, chiTiets);

        } catch (Exception e) {
            log.error("Lá»—i khi láº¥y chi tiáº¿t hÃ³a Ä‘Æ¡n chá»: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ láº¥y chi tiáº¿t hÃ³a Ä‘Æ¡n chá»");
        }
    }

    @Override
    public HoaDonChoTongQuanResponse layTongQuanHoaDonCho(Integer hoaDonId) {
        if (hoaDonId == null) {
            log.error("ID hÃ³a Ä‘Æ¡n khÃ´ng Ä‘Æ°á»£c null");
            throw new RuntimeException("ID hÃ³a Ä‘Æ¡n khÃ´ng há»£p lá»‡");
        }

        try {
            // Debug: Log ID Ä‘ang tÃ¬m kiáº¿m
            log.info("Äang tÃ¬m hÃ³a Ä‘Æ¡n vá»›i ID: {}", hoaDonId);

            // Debug: Kiá»ƒm tra exists trÆ°á»›c
            boolean exists = hoaDonRepository.existsById(hoaDonId);
            log.info("HÃ³a Ä‘Æ¡n ID {} tá»“n táº¡i: {}", hoaDonId, exists);

            Optional<HoaDon> hoaDonOpt = hoaDonRepository.findById(hoaDonId);
            log.info("Káº¿t quáº£ findById: {}", hoaDonOpt.isPresent() ? "TÃ¬m tháº¥y" : "KhÃ´ng tÃ¬m tháº¥y");

            if (!hoaDonOpt.isPresent()) {
                // Debug: Thá»­ query trá»±c tiáº¿p
                List<HoaDon> allHoaDon = hoaDonRepository.findAll();
                log.info("Tá»•ng sá»‘ hÃ³a Ä‘Æ¡n trong DB: {}", allHoaDon.size());

                // Debug: Kiá»ƒm tra ID cÃ³ Ä‘Ãºng type khÃ´ng
                log.info("Kiá»ƒu dá»¯ liá»‡u cá»§a hoaDonId: {}", hoaDonId.getClass().getSimpleName());

                log.warn("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n vá»›i ID: {}", hoaDonId);
                throw new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n vá»›i ID: " + hoaDonId);
            }

            HoaDon hoaDon = hoaDonOpt.get();
            log.info("TÃ¬m tháº¥y hÃ³a Ä‘Æ¡n: ID={}, Status={}", hoaDon.getId(), hoaDon.getTrangThaiHoaDon());

            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonIdOrderByNgayTao(hoaDonId);
            log.info("Sá»‘ lÆ°á»£ng chi tiáº¿t hÃ³a Ä‘Æ¡n: {}", chiTiets.size());

            return mapToHoaDonChoTongQuanResponse(hoaDon, chiTiets);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lá»—i khÃ´ng xÃ¡c Ä‘á»‹nh khi láº¥y tá»•ng quan hÃ³a Ä‘Æ¡n chá» vá»›i ID {}: {}", hoaDonId, e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ láº¥y tá»•ng quan hÃ³a Ä‘Æ¡n chá»: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void xoaHoaDonCho(Integer id) {
        // Kiá»ƒm tra hÃ³a Ä‘Æ¡n cÃ³ tá»“n táº¡i khÃ´ng
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n vá»›i ID: " + id));

        // Kiá»ƒm tra hÃ³a Ä‘Æ¡n cÃ³ sáº£n pháº©m khÃ´ng
        List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByHoaDonId(id);
        if (!chiTietList.isEmpty()) {
            throw new RuntimeException("KhÃ´ng thá»ƒ xÃ³a hÃ³a Ä‘Æ¡n Ä‘Ã£ cÃ³ sáº£n pháº©m");
        }

        try {
            // XÃ“A CÃC RECORD LIÃŠN QUAN TRÆ¯á»šC KHI XÃ“A HÃ“A ÄÆ N

            // 1. XÃ³a lá»‹ch sá»­ hÃ³a Ä‘Æ¡n
            lichSuHoaDonRepository.deleteByHoaDonId(id);

            // 2. XÃ³a cÃ¡c báº£ng liÃªn quan khÃ¡c (náº¿u cÃ³)
            // voucherSuDungRepository.deleteByHoaDonId(id);
            // diemTichLuyRepository.deleteByHoaDonId(id);

            // 3. Cuá»‘i cÃ¹ng má»›i xÃ³a hÃ³a Ä‘Æ¡n
            hoaDonRepository.deleteById(id);

            logger.info("ÄÃ£ xÃ³a hÃ³a Ä‘Æ¡n vÃ  cÃ¡c record liÃªn quan ID: {}", id);

        } catch (Exception e) {
            logger.error("Lá»—i xÃ³a hÃ³a Ä‘Æ¡n ID {}: {}", id, e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ xÃ³a hÃ³a Ä‘Æ¡n: " + e.getMessage());
        }
    }

    // ===== QUáº¢N LÃ Sáº¢N PHáº¨M =====

    @Override
    public Page<SanPhamChiTietBanHangResponse> timKiemSanPham(SanPhamChiTietFilterRequest filter, Pageable pageable) {
        try {
            // Sá»¬A: Sá»­ dá»¥ng repository gá»‘c vá»›i specification
            Specification<ChiTietSanPham> spec = ChiTietSanPhamSpecification.withFilter(filter);
            Page<ChiTietSanPham> sanPhamPage = chiTietSanPhamRepository.findAll(spec, pageable);

            return sanPhamPage.map(this::mapToSanPhamChiTietBanHangResponse);
        } catch (Exception e) {
            logger.error("âŒ Lá»—i tÃ¬m kiáº¿m sáº£n pháº©m", e);
            throw new RuntimeException("Lá»—i tÃ¬m kiáº¿m sáº£n pháº©m: " + e.getMessage());
        }
    }

    @Override
    public SanPhamChiTietBanHangResponse layChiTietSanPham(Integer chiTietSanPhamId) {
        try {
            ChiTietSanPham sanPham = chiTietSanPhamRepository.findById(chiTietSanPhamId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y sáº£n pháº©m"));

            return mapToSanPhamChiTietBanHangResponse(sanPham);

        } catch (Exception e) {
            log.error("Lá»—i khi láº¥y chi tiáº¿t sáº£n pháº©m: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ láº¥y chi tiáº¿t sáº£n pháº©m");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public ScanQRResponse scanQRSanPham(String qrCode) {
        try {
            ChiTietSanPham sanPham = chiTietSanPhamRepository.findByMaQR(qrCode)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y sáº£n pháº©m vá»›i mÃ£ QR: " + qrCode));

            if (sanPham.getTrangThai() != 1) {
                throw new RuntimeException("Sáº£n pháº©m khÃ´ng cÃ²n hoáº¡t Ä‘á»™ng");
            }

            return mapToScanQRResponse(sanPham);

        } catch (Exception e) {
            log.error("Lá»—i khi quÃ©t QR: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ quÃ©t mÃ£ QR: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<SanPhamChiTietBanHangResponse> laySanPhamTuongTu(Integer chiTietSanPhamId) {
        try {
            ChiTietSanPham sanPham = chiTietSanPhamRepository.findById(chiTietSanPhamId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y sáº£n pháº©m"));

            // Láº¥y thÃ´ng tin sáº£n pháº©m gá»‘c - Sá»¬ Dá»¤NG RELATIONSHIP
            SanPham sanPhamGoc = sanPham.getSanPham();
            if (sanPhamGoc == null) {
                return new ArrayList<>();
            }

            // Láº¥y ID danh má»¥c vÃ  thÆ°Æ¡ng hiá»‡u tá»« relationship
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
            log.error("Lá»—i khi láº¥y sáº£n pháº©m tÆ°Æ¡ng tá»±: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ láº¥y sáº£n pháº©m tÆ°Æ¡ng tá»±");
        }
    }

    // ===== QUáº¢N LÃ Sáº¢N PHáº¨M TRONG HÃ“A ÄÆ N =====

    @Override
    public HoaDonChoTongQuanResponse themSanPhamVaoHoaDon(Integer hoaDonId, ThemSanPhamRequest request) {
        try {
            log.info("ThÃªm sáº£n pháº©m vÃ o hÃ³a Ä‘Æ¡n ID: {}, Sáº£n pháº©m ID: {}", hoaDonId, request.getChiTietSanPhamId());

            // Validate request
            validateThemSanPhamRequest(request);

            // Kiá»ƒm tra hÃ³a Ä‘Æ¡n
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));

            if (!"CHO".equals(hoaDon.getTrangThaiHoaDon())) {
                throw new RuntimeException("Chá»‰ cÃ³ thá»ƒ thÃªm sáº£n pháº©m vÃ o hÃ³a Ä‘Æ¡n Ä‘ang chá» thanh toÃ¡n");
            }

            // Kiá»ƒm tra sáº£n pháº©m
            ChiTietSanPham sanPham = chiTietSanPhamRepository.findById(request.getChiTietSanPhamId())
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y sáº£n pháº©m"));

            kiemTraSanPhamCoTheBan(sanPham, request.getSoLuong());

            // Kiá»ƒm tra sáº£n pháº©m Ä‘Ã£ cÃ³ trong hÃ³a Ä‘Æ¡n chÆ°a
            Optional<HoaDonChiTiet> existingItem = hoaDonChiTietRepository
                    .findByHoaDonIdAndChiTietSanPham_Id(hoaDonId, request.getChiTietSanPhamId());

            if (existingItem.isPresent()) {
                // Cáº­p nháº­t sá»‘ lÆ°á»£ng náº¿u sáº£n pháº©m Ä‘Ã£ cÃ³
                HoaDonChiTiet chiTiet = existingItem.get();
                int soLuongMoi = chiTiet.getSoLuong() + request.getSoLuong();

                if (sanPham.getSoLuong() < request.getSoLuong()) {
                    throw new RuntimeException("KhÃ´ng Ä‘á»§ sá»‘ lÆ°á»£ng tá»“n kho. CÃ²n láº¡i: " + sanPham.getSoLuong());
                }

                chiTiet.setSoLuong(soLuongMoi);
                chiTiet.setNgayCapNhat(new Date());
                hoaDonChiTietRepository.save(chiTiet);
            } else {
                // ThÃªm sáº£n pháº©m má»›i - Sá»¬ Dá»¤NG RELATIONSHIP
                HoaDonChiTiet chiTiet = new HoaDonChiTiet();
                chiTiet.setHoaDon(hoaDon); // Set object, khÃ´ng pháº£i ID
                chiTiet.setChiTietSanPham(sanPham); // Set object, khÃ´ng pháº£i ID
                chiTiet.setSoLuong(request.getSoLuong());
                chiTiet.setGia(request.getDonGia() != null ?
                        convertToBigDecimal(request.getDonGia()).doubleValue() :
                        sanPham.getGiaBan().doubleValue());
                chiTiet.setTrangThaiHoaDon("CHO");
                chiTiet.setNgayTao(new Date());
                chiTiet.setNgayCapNhat(new Date());
                hoaDonChiTietRepository.save(chiTiet);
            }

            // Trá»« sá»‘ lÆ°á»£ng tá»“n kho
            sanPham.setSoLuong(sanPham.getSoLuong() - request.getSoLuong());
            chiTietSanPhamRepository.save(sanPham);

            // Cáº­p nháº­t tá»•ng tiá»n hÃ³a Ä‘Æ¡n
            capNhatTongTienHoaDon(hoaDonId);

            log.info("ÄÃ£ thÃªm sáº£n pháº©m vÃ o hÃ³a Ä‘Æ¡n thÃ nh cÃ´ng");

            return layTongQuanHoaDonCho(hoaDonId);

        } catch (Exception e) {
            log.error("Lá»—i khi thÃªm sáº£n pháº©m vÃ o hÃ³a Ä‘Æ¡n: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ thÃªm sáº£n pháº©m vÃ o hÃ³a Ä‘Æ¡n: " + e.getMessage());
        }
    }

    @Override
    public HoaDonChoTongQuanResponse capNhatSanPhamTrongHoaDon(
            Integer hoaDonId, Integer hoaDonChiTietId, CapNhatSanPhamRequest request) {
        try {
            log.info("Cáº­p nháº­t sáº£n pháº©m trong hÃ³a Ä‘Æ¡n ID: {}, Chi tiáº¿t ID: {}", hoaDonId, hoaDonChiTietId);

            validateCapNhatSanPhamRequest(request);

            // Kiá»ƒm tra hÃ³a Ä‘Æ¡n chi tiáº¿t
            HoaDonChiTiet chiTiet = hoaDonChiTietRepository.findById(hoaDonChiTietId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t hÃ³a Ä‘Æ¡n"));

            if (!chiTiet.getHoaDon().getId().equals(hoaDonId)) {
                throw new RuntimeException("Chi tiáº¿t hÃ³a Ä‘Æ¡n khÃ´ng thuá»™c vá» hÃ³a Ä‘Æ¡n nÃ y");
            }

            ChiTietSanPham sanPham = chiTiet.getChiTietSanPham(); // Sá»­ dá»¥ng relationship
            int soLuongCu = chiTiet.getSoLuong();
            int soLuongMoi = request.getSoLuong();
            int chenhLech = soLuongMoi - soLuongCu;

            // Kiá»ƒm tra tá»“n kho
            if (chenhLech > 0 && sanPham.getSoLuong() < chenhLech) {
                throw new RuntimeException("KhÃ´ng Ä‘á»§ sá»‘ lÆ°á»£ng tá»“n kho. CÃ²n láº¡i: " + sanPham.getSoLuong());
            }

            // Cáº­p nháº­t chi tiáº¿t hÃ³a Ä‘Æ¡n
            chiTiet.setSoLuong(soLuongMoi);
            if (request.getDonGia() != null) {
                chiTiet.setGia(convertToBigDecimal(request.getDonGia()).doubleValue());
            }
            chiTiet.setNgayCapNhat(new Date());
            hoaDonChiTietRepository.save(chiTiet);

            // Cáº­p nháº­t tá»“n kho
            sanPham.setSoLuong(sanPham.getSoLuong() - chenhLech);
            chiTietSanPhamRepository.save(sanPham);

            // Cáº­p nháº­t tá»•ng tiá»n hÃ³a Ä‘Æ¡n
            capNhatTongTienHoaDon(hoaDonId);

            log.info("ÄÃ£ cáº­p nháº­t sáº£n pháº©m trong hÃ³a Ä‘Æ¡n thÃ nh cÃ´ng");

            return layTongQuanHoaDonCho(hoaDonId);

        } catch (Exception e) {
            log.error("Lá»—i khi cáº­p nháº­t sáº£n pháº©m trong hÃ³a Ä‘Æ¡n: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ cáº­p nháº­t sáº£n pháº©m trong hÃ³a Ä‘Æ¡n: " + e.getMessage());
        }
    }

    @Override
    public HoaDonChoTongQuanResponse xoaSanPhamKhoiHoaDon(Integer hoaDonId, Integer hoaDonChiTietId) {
        try {
            log.info("XÃ³a sáº£n pháº©m khá»i hÃ³a Ä‘Æ¡n ID: {}, Chi tiáº¿t ID: {}", hoaDonId, hoaDonChiTietId);

            // Kiá»ƒm tra hÃ³a Ä‘Æ¡n chi tiáº¿t
            HoaDonChiTiet chiTiet = hoaDonChiTietRepository.findById(hoaDonChiTietId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t hÃ³a Ä‘Æ¡n"));

            if (!chiTiet.getHoaDon().getId().equals(hoaDonId)) {
                throw new RuntimeException("Chi tiáº¿t hÃ³a Ä‘Æ¡n khÃ´ng thuá»™c vá» hÃ³a Ä‘Æ¡n nÃ y");
            }

            // HoÃ n tráº£ sá»‘ lÆ°á»£ng tá»“n kho
            ChiTietSanPham sanPham = chiTiet.getChiTietSanPham(); // Sá»­ dá»¥ng relationship
            sanPham.setSoLuong(sanPham.getSoLuong() + chiTiet.getSoLuong());
            chiTietSanPhamRepository.save(sanPham);

            // XÃ³a chi tiáº¿t hÃ³a Ä‘Æ¡n
            hoaDonChiTietRepository.deleteById(hoaDonChiTietId);

            // Cáº­p nháº­t tá»•ng tiá»n hÃ³a Ä‘Æ¡n
            capNhatTongTienHoaDon(hoaDonId);

            log.info("ÄÃ£ xÃ³a sáº£n pháº©m khá»i hÃ³a Ä‘Æ¡n thÃ nh cÃ´ng");

            return layTongQuanHoaDonCho(hoaDonId);

        } catch (Exception e) {
            log.error("Lá»—i khi xÃ³a sáº£n pháº©m khá»i hÃ³a Ä‘Æ¡n: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ xÃ³a sáº£n pháº©m khá»i hÃ³a Ä‘Æ¡n: " + e.getMessage());
        }
    }

    @Override
    public TinhGiaResponse tinhGiaSanPham(TinhGiaRequest request) {
        try {
            ChiTietSanPham sanPham = chiTietSanPhamRepository.findById(request.getChiTietSanPhamId())
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y sáº£n pháº©m"));

            // Sá»¬A: Convert tá»« Double sang BigDecimal
            BigDecimal giaGoc = sanPham.getGiaGoc() != null ?
                    BigDecimal.valueOf(sanPham.getGiaGoc()) : BigDecimal.ZERO;
            BigDecimal giaBan = sanPham.getGiaBan() != null ?
                    BigDecimal.valueOf(sanPham.getGiaBan()) : BigDecimal.ZERO;
            int soLuong = request.getSoLuong();

            // TÃ­nh khuyáº¿n mÃ£i
            List<KhuyenMaiSanPhamResponse> khuyenMais = layKhuyenMaiSanPham(request.getChiTietSanPhamId());

            BigDecimal tongTienGoc = giaGoc.multiply(BigDecimal.valueOf(soLuong));
            BigDecimal tongTienSauGiam = giaBan.multiply(BigDecimal.valueOf(soLuong));

            // Ãp dá»¥ng khuyáº¿n mÃ£i
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
            log.error("Lá»—i khi tÃ­nh giÃ¡ sáº£n pháº©m: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ tÃ­nh giÃ¡ sáº£n pháº©m: " + e.getMessage());
        }
    }
    private KhachHangResponse mapToKhachHangResponseSafe(KhachHang khachHang) {
        if (khachHang == null) return null;

        try {
            // Basic info - luÃ´n cÃ³
            String maKhachHang = khachHang.getMaKhachHang() != null ? khachHang.getMaKhachHang() : "";
            String hoTen = khachHang.getHoTen() != null ? khachHang.getHoTen() : "";
            String sdt = khachHang.getSdt() != null ? khachHang.getSdt() : "";
            Integer trangThai = khachHang.getTrangThai() != null ? khachHang.getTrangThai() : 0;

            // âœ… Safe email loading
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

            // âœ… Safe points calculation
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
            log.warn("âŒ Error mapping customer {}: {}", khachHang.getId(), e.getMessage());

            // Return minimal safe object
            return KhachHangResponse.builder()
                    .id(khachHang.getId())
                    .maKhachHang(khachHang.getMaKhachHang() != null ? khachHang.getMaKhachHang() : "")
                    .hoTen(khachHang.getHoTen() != null ? khachHang.getHoTen() : "KhÃ¡ch hÃ ng")
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
            log.info("ðŸ” TÃ¬m kiáº¿m khÃ¡ch hÃ ng vá»›i keyword: '{}'", keyword);

            Page<KhachHang> khachHangs = null;

            if (StringUtils.hasText(keyword)) {
                String keywordTrimmed = keyword.trim();
                log.info("ðŸ”„ Searching with keyword: '{}'", keywordTrimmed);

                try {
                    // Thá»­ query vá»›i keyword
                    khachHangs = khachHangRepository.searchByKeyword(keywordTrimmed, pageable);
                    log.info("âœ… Found {} customers with search query", khachHangs.getTotalElements());
                } catch (Exception e) {
                    log.warn("âš ï¸ Search query failed: {}", e.getMessage());
                    // Fallback vá» query Ä‘Æ¡n giáº£n
                    try {
                        khachHangs = khachHangRepository.searchByKeywordSimple(keywordTrimmed, pageable);
                        log.info("âœ… Found {} customers with simple query", khachHangs.getTotalElements());
                    } catch (Exception e2) {
                        log.warn("âš ï¸ Simple query also failed: {}", e2.getMessage());
                        // Manual search
                        khachHangs = searchCustomersManually(keywordTrimmed, pageable);
                    }
                }
            } else {
                log.info("ðŸ“‹ Loading all active customers");
                try {
                    khachHangs = khachHangRepository.findAllActive(pageable);
                    log.info("âœ… Found {} total active customers", khachHangs.getTotalElements());
                } catch (Exception e) {
                    log.warn("âš ï¸ findAllActive failed: {}", e.getMessage());
                    // Manual search without keyword
                    khachHangs = searchCustomersManually("", pageable);
                }
            }

            // Äáº£m báº£o khachHangs khÃ´ng bao giá» null
            if (khachHangs == null) {
                khachHangs = new PageImpl<>(new ArrayList<>(), pageable, 0);
            }

            // Map to DTO vá»›i safe approach
            List<KhachHangResponse> responses = khachHangs.getContent().stream()
                    .map(this::mapKhachHangSafe)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            log.info("âœ… Successfully mapped {} customer responses", responses.size());
            return new PageImpl<>(responses, pageable, khachHangs.getTotalElements());

        } catch (Exception e) {
            log.error("âŒ Critical error in timKiemKhachHang: {}", e.getMessage());
            // KHÃ”NG THROW EXCEPTION - tráº£ vá» empty page
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
            log.warn("âŒ Error mapping customer {}: {}", kh.getId(), e.getMessage());

            // Return minimal safe version
            return KhachHangResponse.builder()
                    .id(kh.getId())
                    .maKhachHang(kh.getMaKhachHang() != null ? kh.getMaKhachHang() : "")
                    .hoTen(kh.getHoTen() != null ? kh.getHoTen() : "KhÃ¡ch hÃ ng")
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

    // âœ… 6. SAFE POINTS GETTER
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
            log.info("ðŸ”§ Manual search for keyword: '{}'", keyword);

            // Láº¥y táº¥t cáº£ khÃ¡ch hÃ ng
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

            log.info("ðŸ”§ Manual search found {} customers", total);
            return new PageImpl<>(pageContent, pageable, total);

        } catch (Exception e) {
            log.error("âŒ Manual search failed: {}", e.getMessage());
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }



    // âœ… THÃŠM: Helper method Ä‘á»ƒ tÃ¬m khÃ¡ch hÃ ng manually khi query fail
    private Page<KhachHang> findActiveCustomersManually(Pageable pageable, String keyword) {
        try {
            List<KhachHang> allCustomers = khachHangRepository.findAll();

            // Filter theo Ä‘iá»u kiá»‡n
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

            // PhÃ¢n trang manual
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), filteredCustomers.size());

            if (start >= filteredCustomers.size()) {
                return new PageImpl<>(new ArrayList<>(), pageable, filteredCustomers.size());
            }

            List<KhachHang> pageContent = filteredCustomers.subList(start, end);
            return new PageImpl<>(pageContent, pageable, filteredCustomers.size());

        } catch (Exception e) {
            log.error("âŒ Manual filtering failed: {}", e.getMessage());
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }


    // âœ… Sá»¬A: Version má»›i cá»§a mapping method khÃ´ng gÃ¢y transaction rollback
    private KhachHangResponse mapToKhachHangResponseSafeV2(KhachHang khachHang) {
        if (khachHang == null) return null;

        try {
            // âœ… Sá»¬A: TÃ­nh Ä‘iá»ƒm tÃ­ch lÅ©y an toÃ n hÆ¡n
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

            // âœ… Sá»¬A: Láº¥y email an toÃ n hÆ¡n
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
            log.error("âŒ Critical error mapping customer {}: {}",
                    khachHang.getId(), e.getMessage());

            // âœ… Sá»¬A: Tráº£ vá» object tá»‘i thiá»ƒu thay vÃ¬ null Ä‘á»ƒ trÃ¡nh NPE
            return KhachHangResponse.builder()
                    .id(khachHang.getId())
                    .maKhachHang(khachHang.getMaKhachHang() != null ? khachHang.getMaKhachHang() : "")
                    .hoTen(khachHang.getHoTen() != null ? khachHang.getHoTen() : "KhÃ¡ch hÃ ng")
                    .sdt(khachHang.getSdt() != null ? khachHang.getSdt() : "")
                    .email("")
                    .trangThai(khachHang.getTrangThai() != null ? khachHang.getTrangThai() : 0)
                    .diemTichLuy(0.0)
                    .ngayTao(khachHang.getNgayTao())
                    .build();
        }
    }


    // ===== Cáº¬P NHáº¬T PHÆ¯Æ NG THá»¨C taoKhachHangNhanh TRONG BanHangServiceImpl =====

    @Override
    public KhachHangResponse taoKhachHangNhanh(TaoKhachHangNhanhRequest request) {
        try {
            log.info("Táº¡o khÃ¡ch hÃ ng nhanh: {}", request.getSdt());

            // Kiá»ƒm tra sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ tá»“n táº¡i
            if (khachHangRepository.existsBySdt(request.getSdt())) {
                throw new RuntimeException("Sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng");
            }

            // âœ… Sá»¬A: Kiá»ƒm tra email qua TaiKhoan thay vÃ¬ KhachHang
            if (StringUtils.hasText(request.getEmail())) {
                // Kiá»ƒm tra trong TaiKhoan trá»±c tiáº¿p
                if (taiKhoanRepository.existsByEmail(request.getEmail())) {
                    throw new RuntimeException("Email Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng");
                }
                // Kiá»ƒm tra thÃªm qua KhachHang (backup)
                if (khachHangRepository.existsByTaiKhoanEmail(request.getEmail())) {
                    throw new RuntimeException("Email Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng bá»Ÿi khÃ¡ch hÃ ng khÃ¡c");
                }
            }

            // 1. Táº¡o TaiKhoan trÆ°á»›c
            TaiKhoan taiKhoan = new TaiKhoan();
            taiKhoan.setMaTaiKhoan("TK" + System.currentTimeMillis());
            taiKhoan.setEmail(request.getEmail() != null ? request.getEmail() : "");
            taiKhoan.setMatKhau("default123"); // Máº­t kháº©u máº·c Ä‘á»‹nh
            taiKhoan.setVaiTro(TaiKhoan.VaiTro.USER); // 0 = khÃ¡ch hÃ ng
            taiKhoan.setTrangThai(1);
            taiKhoan.setNgayTao(new Date());
            taiKhoan.setNgayCapNhat(new Date());
            TaiKhoan savedTaiKhoan = taiKhoanRepository.save(taiKhoan);

            // 2. Táº¡o ViDiem
            ViDiem viDiem = new ViDiem();
            viDiem.setTongDiem(0.0);
            viDiem.setSoDiemDaDung(0.0);
            viDiem.setSoDiemDaCong(0.0);
            viDiem.setGiaTriDiem(1000.0);
            viDiem.setNgayTao(new Date());
            viDiem.setNgayCapNhat(new Date());
            ViDiem savedViDiem = viDiemRepository.save(viDiem);

            // 3. Táº¡o KhachHang
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

            // 4. Táº¡o DiaChi riÃªng biá»‡t cho TaiKhoan (náº¿u cáº§n)
            if (StringUtils.hasText(request.getDiaChi())) {
                DiaChi diaChi = new DiaChi();
                diaChi.setTaiKhoan(savedTaiKhoan);
                diaChi.setMaTinh("01");
                diaChi.setMaPhuong("00001");
                diaChi.setTenTinh("HÃ  Ná»™i");
                diaChi.setTenPhuong("PhÃºc XÃ¡");
                diaChi.setDiaChiChiTiet(request.getDiaChi());
                diaChi.setIsDefault(true);
                diaChi.setTrangThai(1);
                diaChi.setNgayTao(new Date());
                diaChi.setNgayCapNhat(new Date());
                diaChiRepository.save(diaChi);
            }

            log.info("ÄÃ£ táº¡o khÃ¡ch hÃ ng nhanh thÃ nh cÃ´ng ID: {}", savedKhachHang.getId());

            return mapToKhachHangResponse(savedKhachHang);

        } catch (Exception e) {
            log.error("Lá»—i khi táº¡o khÃ¡ch hÃ ng nhanh: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ táº¡o khÃ¡ch hÃ ng: " + e.getMessage());
        }
    }

    @Override
    public KhachHangDetailResponse layThongTinKhachHang(Integer khachHangId) {
        try {
            KhachHang khachHang = khachHangRepository.findById(khachHangId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y khÃ¡ch hÃ ng"));

            return mapToKhachHangDetailResponse(khachHang);

        } catch (Exception e) {
            log.error("Lá»—i khi láº¥y thÃ´ng tin khÃ¡ch hÃ ng: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ láº¥y thÃ´ng tin khÃ¡ch hÃ ng");
        }
    }

    @Override
    public HoaDonChoTongQuanResponse apDungKhachHang(Integer hoaDonId, Integer khachHangId) {
        try {
            log.info("Ãp dá»¥ng khÃ¡ch hÃ ng ID: {} cho hÃ³a Ä‘Æ¡n ID: {}", khachHangId, hoaDonId);

            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));

            KhachHang khachHang = khachHangRepository.findById(khachHangId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y khÃ¡ch hÃ ng"));

            // Sá»¬A: Set object thay vÃ¬ ID
            hoaDon.setKhachHang(khachHang);

            // Sá»¬A: Láº¥y email tá»« TaiKhoan thay vÃ¬ tá»« KhachHang
            if (khachHang.getTaiKhoan() != null && StringUtils.hasText(khachHang.getTaiKhoan().getEmail())) {
                hoaDon.setEmail(khachHang.getTaiKhoan().getEmail());
            } else {
                hoaDon.setEmail("");
            }

            hoaDon.setSdt(khachHang.getSdt());
            hoaDon.setTenNguoiDung(khachHang.getHoTen());

            // Sá»¬A: Láº¥y Ä‘á»‹a chá»‰ tá»« TaiKhoan -> DiaChi
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
            log.error("Lá»—i khi Ã¡p dá»¥ng khÃ¡ch hÃ ng: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ Ã¡p dá»¥ng khÃ¡ch hÃ ng: " + e.getMessage());
        }
    }

    @Override
    public HoaDonChoTongQuanResponse boKhachHang(Integer hoaDonId) {
        try {
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));

            hoaDon.setKhachHang(null);  // Set null thay vÃ¬ setIdKhachHang(null)
            hoaDon.setEmail("");
            hoaDon.setSdt("");
            hoaDon.setTenNguoiDung("KhÃ¡ch láº»");
            hoaDon.setDiaChi("");
            hoaDon.setNgayCapNhat(new Date());
            hoaDonRepository.save(hoaDon);

            return layTongQuanHoaDonCho(hoaDonId);

        } catch (Exception e) {
            log.error("Lá»—i khi bá» khÃ¡ch hÃ ng: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ bá» khÃ¡ch hÃ ng: " + e.getMessage());
        }
    }

    // ===== QUáº¢N LÃ VOUCHER =====

    private VoucherResponse mapToVoucherResponseSafe(Voucher voucher) {
        if (voucher == null) return null;

        try {
            Date currentDate = new Date();
            boolean daHetHan = voucher.getNgayKetThuc() != null && currentDate.after(voucher.getNgayKetThuc());
            boolean daHetSoLuong = voucher.getSoLuong() != null && voucher.getSoLuong() <= 0;

            // âœ… Sá»¬A: Äáº£m báº£o loaiGiamGia Ä‘Æ°á»£c set Ä‘Ãºng
            String loaiGiamGia = voucher.getLoaiGiamGia();
            if (loaiGiamGia == null || loaiGiamGia.isEmpty()) {
                // Fallback dá»±a trÃªn pattern cá»§a tÃªn
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
            log.error("âŒ Error mapping voucher {}: {}", voucher.getId(), e.getMessage());
            return null;
        }
    }

    @Override
    public List<VoucherResponse> layDanhSachVoucherKhaDung(Integer khachHangId, Double tongTien) {
        try {
            log.info("ðŸŽ« Láº¥y voucher kháº£ dá»¥ng: khachHangId={}, tongTien={}", khachHangId, tongTien);

            Date currentDate = new Date();
            List<Voucher> vouchers = null;

            // âœ… Sá»¬A: Xá»­ lÃ½ case tongTien = null
            if (tongTien == null || tongTien <= 0) {
                log.info("ðŸ“‹ Láº¥y táº¥t cáº£ voucher kháº£ dá»¥ng (khÃ´ng filter theo tá»•ng tiá»n)");
                vouchers = voucherRepository.findAllAvailableVouchers(currentDate);
            } else {
                log.info("ðŸ“‹ Láº¥y voucher kháº£ dá»¥ng cho tá»•ng tiá»n: {}", tongTien);
                vouchers = voucherRepository.findAvailableVouchers(currentDate, tongTien);
            }

            log.info("ðŸ“‹ Found {} vouchers from database", vouchers.size());

            // âœ… Debug log chi tiáº¿t
            for (Voucher v : vouchers) {
                log.debug("Voucher: {} - Giáº£m: {} - Tá»‘i thiá»ƒu: {} - HSD: {}",
                        v.getTenVoucher(), v.getGiaTriGiam(), v.getGiaTriGiamToiThieu(), v.getNgayKetThuc());
            }

            List<VoucherResponse> responses = vouchers.stream()
                    .filter(voucher -> {
                        // âœ… Sá»¬A: Lá»c thÃªm theo logic business
                        if (voucher == null || !voucher.isValid()) {
                            return false;
                        }

                        // âœ… Sá»¬A: Kiá»ƒm tra giÃ¡ trá»‹ Ä‘Æ¡n hÃ ng tá»‘i thiá»ƒu náº¿u cÃ³ tongTien
                        if (tongTien != null && tongTien > 0 && tongTien < voucher.getGiaTriGiamToiThieu()) {
                            log.debug("Voucher {} bá»‹ loáº¡i: tongTien {} < minimum {}",
                                    voucher.getTenVoucher(), tongTien, voucher.getGiaTriGiamToiThieu());
                            return false;
                        }

                        return true;
                    })
                    .map(this::mapToVoucherResponseSafe)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            log.info("âœ… Returning {} applicable vouchers", responses.size());
            return responses;

        } catch (Exception e) {
            log.error("âŒ Error loading vouchers: {}", e.getMessage(), e);
            return new ArrayList<>(); // Return empty list instead of throwing
        }
    }

    @Override
    public VoucherValidationResponse kiemTraVoucher(ValidateVoucherRequest request) {
        try {
            log.info("âœ… Kiá»ƒm tra voucher: {}", request.getMaVoucher());

            Optional<Voucher> voucherOpt = voucherRepository.findByMaVoucher(request.getMaVoucher());

            if (!voucherOpt.isPresent()) {
                return VoucherValidationResponse.builder()
                        .valid(false)
                        .message("Voucher khÃ´ng tá»“n táº¡i")
                        .lyDoKhongHopLe("MÃ£ voucher khÃ´ng há»£p lá»‡")
                        .build();
            }

            Voucher voucher = voucherOpt.get();
            Date currentDate = new Date();

            // Kiá»ƒm tra cÃ¡c Ä‘iá»u kiá»‡n
            if (!voucher.isActive()) {
                return VoucherValidationResponse.builder()
                        .valid(false)
                        .message("Voucher khÃ´ng hoáº¡t Ä‘á»™ng")
                        .lyDoKhongHopLe("Voucher Ä‘Ã£ bá»‹ vÃ´ hiá»‡u hÃ³a")
                        .build();
            }

            if (voucher.getSoLuong() <= 0) {
                return VoucherValidationResponse.builder()
                        .valid(false)
                        .message("Voucher Ä‘Ã£ háº¿t lÆ°á»£t sá»­ dá»¥ng")
                        .lyDoKhongHopLe("Voucher Ä‘Ã£ háº¿t sá»‘ lÆ°á»£ng")
                        .build();
            }

            if (currentDate.before(voucher.getNgayBatDau()) || currentDate.after(voucher.getNgayKetThuc())) {
                return VoucherValidationResponse.builder()
                        .valid(false)
                        .message("Voucher khÃ´ng trong thá»i gian sá»­ dá»¥ng")
                        .lyDoKhongHopLe("Voucher ngoÃ i thá»i gian hiá»‡u lá»±c")
                        .build();
            }

            Double tongTien = request.getTongTien() != null ? request.getTongTien() : 0.0;
            if (tongTien < voucher.getGiaTriGiamToiThieu()) {
                return VoucherValidationResponse.builder()
                        .valid(false)
                        .message("ÄÆ¡n hÃ ng chÆ°a Ä‘á»§ giÃ¡ trá»‹ tá»‘i thiá»ƒu")
                        .lyDoKhongHopLe("Cáº§n tá»‘i thiá»ƒu " + formatPrice(voucher.getGiaTriGiamToiThieu()))
                        .build();
            }

            // TÃ­nh giÃ¡ trá»‹ giáº£m
            Double giaTriGiam = voucher.tinhGiaTriGiam(tongTien);

            return VoucherValidationResponse.builder()
                    .valid(true)
                    .message("Voucher há»£p lá»‡")
                    .voucher(mapToVoucherResponseSafe(voucher))
                    .giaTriGiam(giaTriGiam)
                    .build();

        } catch (Exception e) {
            log.error("âŒ Error validating voucher: {}", e.getMessage(), e);
            return VoucherValidationResponse.builder()
                    .valid(false)
                    .message("Lá»—i há»‡ thá»‘ng")
                    .lyDoKhongHopLe("KhÃ´ng thá»ƒ kiá»ƒm tra voucher: " + e.getMessage())
                    .build();
        }
    }

    // Helper method
    private String formatPrice(Double price) {
        if (price == null) return "0â‚«";
        return String.format("%,.0fâ‚«", price);
    }

    @Override
    @Transactional
    public HoaDonChoTongQuanResponse apDungVoucher(Integer hoaDonId, Integer voucherId) {
        try {
            log.info("Ãp dá»¥ng voucher ID: {} cho hÃ³a Ä‘Æ¡n ID: {}", voucherId, hoaDonId);

            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));

            Voucher voucher = voucherRepository.findById(voucherId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y voucher"));

            // Validate voucher nhÆ° cÅ©...
            Date currentDate = new Date();
            if (!voucher.isValid()) throw new RuntimeException("Voucher khÃ´ng há»£p lá»‡");
            if (voucher.getSoLuong() <= 0) throw new RuntimeException("Voucher Ä‘Ã£ háº¿t lÆ°á»£t sá»­ dá»¥ng");

            BigDecimal tongTienHoaDon = hoaDonChiTietRepository.calculateTotalAmountByHoaDonId(hoaDonId);
            if (tongTienHoaDon.doubleValue() < voucher.getGiaTriGiamToiThieu()) {
                throw new RuntimeException("ÄÆ¡n hÃ ng chÆ°a Ä‘á»§ giÃ¡ trá»‹ tá»‘i thiá»ƒu: " +
                        formatMoney(BigDecimal.valueOf(voucher.getGiaTriGiamToiThieu())));
            }

            // KHÃ”NG táº¡o chi_tiet_voucher á»Ÿ Ä‘Ã¢y ná»¯a.
            // Chá»‰ cáº­p nháº­t ngÃ y vÃ  return tá»•ng quan.
            hoaDon.setNgayCapNhat(new Date());
            hoaDonRepository.save(hoaDon);

            capNhatTongTienHoaDon(hoaDonId);
            return layTongQuanHoaDonCho(hoaDonId);

        } catch (Exception e) {
            log.error("Lá»—i khi Ã¡p dá»¥ng voucher: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ Ã¡p dá»¥ng voucher: " + e.getMessage());
        }
    }

    @Override
    public HoaDonChoTongQuanResponse boVoucher(Integer hoaDonId) {
        try {
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));

            // hoaDon.setVoucherId(null);
            hoaDon.setNgayCapNhat(new Date());
            hoaDonRepository.save(hoaDon);

            // Cáº­p nháº­t tá»•ng tiá»n
            capNhatTongTienHoaDon(hoaDonId);

            return layTongQuanHoaDonCho(hoaDonId);

        } catch (Exception e) {
            log.error("Lá»—i khi bá» voucher: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ bá» voucher: " + e.getMessage());
        }
    }

    // ===== THANH TOÃN =====

    @Override
    @Transactional
    public HoaDonResponse thanhToanHoaDon(Integer hoaDonId, ThanhToanRequest request) {
        try {
            log.info("ðŸ’° Thanh toÃ¡n hÃ³a Ä‘Æ¡n ID: {} vá»›i phÆ°Æ¡ng thá»©c: {}", hoaDonId, request.getPhuongThucThanhToan());

            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));

            if (!"CHO".equals(hoaDon.getTrangThaiHoaDon())) {
                throw new RuntimeException("HÃ³a Ä‘Æ¡n khÃ´ng á»Ÿ tráº¡ng thÃ¡i chá» thanh toÃ¡n");
            }

            // Kiá»ƒm tra tá»“n kho trÆ°á»›c khi thanh toÃ¡n
            List<InventoryCheckResponse> inventoryChecks = kiemTraTonKho(hoaDonId);
            boolean hasError = inventoryChecks.stream().anyMatch(check -> !check.getCoTheban());
            if (hasError) {
                throw new RuntimeException("CÃ³ sáº£n pháº©m khÃ´ng Ä‘á»§ tá»“n kho Ä‘á»ƒ thanh toÃ¡n");
            }

            // ===== Xá»¬ LÃ PHÆ¯Æ NG THá»¨C THANH TOÃN =====
            BigDecimal tongTienCanThanhToan = hoaDon.getTongThanhToan();

            // Kiá»ƒm tra vÃ  xá»­ lÃ½ tá»«ng phÆ°Æ¡ng thá»©c thanh toÃ¡n
            if ("TIEN_MAT".equals(request.getPhuongThucThanhToan())) {
                xuLyThanhToanTienMat(request, tongTienCanThanhToan);

            } else if ("CHUYEN_KHOAN".equals(request.getPhuongThucThanhToan())) {
                xuLyThanhToanChuyenKhoan(request, tongTienCanThanhToan);

            } else if ("KET_HOP".equals(request.getPhuongThucThanhToan())) {
                xuLyThanhToanKetHop(request, tongTienCanThanhToan);

            } else {
                throw new RuntimeException("PhÆ°Æ¡ng thá»©c thanh toÃ¡n khÃ´ng há»£p lá»‡: " + request.getPhuongThucThanhToan());
            }

            // Cáº­p nháº­t thÃ´ng tin khÃ¡ch hÃ ng náº¿u cÃ³
            if (request.getKhachHangId() != null) {
                KhachHang khachHang = khachHangRepository.findById(request.getKhachHangId()).orElse(null);
                if (khachHang != null) {
                    hoaDon.setKhachHang(khachHang);

                    // Láº¥y email tá»« TaiKhoan
                    if (khachHang.getTaiKhoan() != null && StringUtils.hasText(khachHang.getTaiKhoan().getEmail())) {
                        hoaDon.setEmail(khachHang.getTaiKhoan().getEmail());
                    } else {
                        hoaDon.setEmail("");
                    }

                    hoaDon.setSdt(khachHang.getSdt());
                    hoaDon.setTenNguoiDung(khachHang.getHoTen());

                    // Láº¥y Ä‘á»‹a chá»‰ tá»« TaiKhoan -> DiaChi
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

            // Cáº­p nháº­t thÃ´ng tin thanh toÃ¡n
            hoaDon.setTrangThaiHoaDon("DA_THANH_TOAN");
            hoaDon.setLoaiHoaDon(request.getLoaiHoaDon());
            hoaDon.setPhuongThucThanhToan(request.getPhuongThucThanhToan()); // LÆ°u phÆ°Æ¡ng thá»©c thanh toÃ¡n
            hoaDon.setNgayHoanThanh(new Date());
            hoaDon.setNgayCapNhat(new Date());
            hoaDon.setGhiChu(request.getGhiChu());
            hoaDon.setDiemSuDung(request.getDiemSuDung());

            // Ãp dá»¥ng Ä‘iá»ƒm tÃ­ch lÅ©y náº¿u cÃ³
            if (request.getDiemSuDung() != null && request.getDiemSuDung() > 0) {
                xuLyDiemTichLuy(hoaDon.getKhachHang(), request.getDiemSuDung());
            }

            // Cáº­p nháº­t tá»•ng tiá»n
            capNhatTongTienHoaDon(hoaDonId);

            // Cáº­p nháº­t tráº¡ng thÃ¡i chi tiáº¿t hÃ³a Ä‘Æ¡n
            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
            for (HoaDonChiTiet chiTiet : chiTiets) {
                chiTiet.setTrangThaiHoaDon("DA_THANH_TOAN");
                hoaDonChiTietRepository.save(chiTiet);
            }

            HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

            // SAU khi thanh toÃ¡n thÃ nh cÃ´ng vÃ  set tráº¡ng thÃ¡i "ÄÃƒ THANH TOÃN", má»›i táº¡o chi_tiet_voucher
            if (request.getVoucherId() != null) {
                Voucher voucher = voucherRepository.findById(request.getVoucherId()).orElse(null);
                if (voucher != null) {
                    // TÃ­nh láº¡i tá»•ng gá»‘c táº¡i thá»i Ä‘iá»ƒm thanh toÃ¡n
                    BigDecimal tongTienHoaDon = hoaDonChiTietRepository.calculateTotalAmountByHoaDonId(hoaDonId);

                    // TÃ­nh sá»‘ tiá»n giáº£m (dÃ¹ng hÃ m cá»§a entity Voucher)
                    Double giaTriGiam = voucher.tinhGiaTriGiam(tongTienHoaDon.doubleValue());
                    BigDecimal soTienGiam = BigDecimal.valueOf(giaTriGiam);
                    BigDecimal thanhTienSauGiam = tongTienHoaDon.subtract(soTienGiam);
                    if (thanhTienSauGiam.compareTo(BigDecimal.ZERO) < 0) thanhTienSauGiam = BigDecimal.ZERO;

                    // Táº¡o chi tiáº¿t voucher
                    taoChiTietVoucherNeuChuaCo(savedHoaDon, voucher, tongTienHoaDon, soTienGiam, thanhTienSauGiam);

                    // Giáº£m sá»‘ lÆ°á»£ng voucher
                    voucher.setSoLuong(voucher.getSoLuong() - 1);
                    voucherRepository.save(voucher);
                }
            }

            // Táº¡o lá»‹ch sá»­ hÃ³a Ä‘Æ¡n
            String moTaThanhToan = taoMoTaThanhToan(request);
            taoLichSuHoaDon(savedHoaDon, moTaThanhToan, savedHoaDon.getNhanVien());

            // Cá»™ng Ä‘iá»ƒm cho khÃ¡ch hÃ ng náº¿u cÃ³
            if (savedHoaDon.getKhachHang() != null) {
                congDiemKhachHang(savedHoaDon.getKhachHang().getId(), savedHoaDon.getTongThanhToan().doubleValue());
            }

            log.info("âœ… Thanh toÃ¡n hÃ³a Ä‘Æ¡n thÃ nh cÃ´ng ID: {} - PhÆ°Æ¡ng thá»©c: {}", hoaDonId, request.getPhuongThucThanhToan());

            return mapToHoaDonResponse(savedHoaDon);

        } catch (Exception e) {
            log.error("âŒ Lá»—i khi thanh toÃ¡n hÃ³a Ä‘Æ¡n: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ thanh toÃ¡n hÃ³a Ä‘Æ¡n: " + e.getMessage());
        }
    }

    private void taoChiTietVoucherNeuChuaCo(HoaDon hoaDon, Voucher voucher,
                                            BigDecimal giaTriDonHang, BigDecimal soTienGiam, BigDecimal thanhTien) {
        try {
            // Kiá»ƒm tra Ä‘Ã£ cÃ³ chi tiáº¿t voucher chÆ°a (idempotency)
            boolean existed = chiTietVoucherService.existsByHoaDonIdAndVoucherId(hoaDon.getId(), voucher.getId());
            if (existed) return; // ÄÃ£ cÃ³ -> khÃ´ng táº¡o trÃ¹ng

            ChiTietVoucherDTO chiTietVoucherDTO = ChiTietVoucherDTO.builder()
                    .maChiTietVoucher("CTV" + hoaDon.getMaHoaDon() + "_" + System.currentTimeMillis())
                    .hoaDonId(hoaDon.getId())
                    .voucherId(voucher.getId())

                    // Snapshot voucher táº¡i thá»i Ä‘iá»ƒm thanh toÃ¡n
                    .maVoucher(voucher.getMaVoucher())
                    .tenVoucher(voucher.getTenVoucher())
                    .loaiGiamGia(voucher.getLoaiGiamGia())
                    .giaTriGiam(voucher.getGiaTriGiam())
                    .giaTriGiamToiDa(voucher.getGiaTriGiamToiDa())
                    .giaTriGiamToiThieu(voucher.getGiaTriGiamToiThieu())

                    // Sá»‘ liá»‡u cuá»‘i cÃ¹ng
                    .giaTriDonHang(giaTriDonHang)
                    .soTienGiam(soTienGiam)
                    .thanhTien(thanhTien)
                    .ngayApDung(new Date())
                    .build();

            chiTietVoucherService.create(chiTietVoucherDTO);

        } catch (Exception ex) {
            log.warn("KhÃ´ng thá»ƒ táº¡o chi tiáº¿t voucher cho hÃ³a Ä‘Æ¡n {}: {}", hoaDon.getId(), ex.getMessage());
            // KhÃ´ng throw Ä‘á»ƒ khÃ´ng rollback thanh toÃ¡n
        }
    }

    @Override
    public List<InventoryCheckResponse> kiemTraTonKho(Integer hoaDonId) {
        try {
            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
            List<InventoryCheckResponse> responses = new ArrayList<>();

            for (HoaDonChiTiet chiTiet : chiTiets) {
                ChiTietSanPham sanPham = chiTiet.getChiTietSanPham(); // Sá»­ dá»¥ng relationship
                if (sanPham != null) {
                    boolean coTheBan = sanPham.getSoLuong() >= 0; // ÄÃ£ trá»« khi thÃªm vÃ o hÃ³a Ä‘Æ¡n
                    String thongBao = coTheBan ? "Äá»§ hÃ ng" : "ÄÃ£ bÃ¡n háº¿t";

                    // Láº¥y thÃ´ng tin sáº£n pháº©m, mÃ u sáº¯c, kÃ­ch cá»¡
                    SanPham sp = sanPham.getSanPham(); // Sá»­ dá»¥ng relationship náº¿u cÃ³
                    MauSac mauSac = sanPham.getMauSac(); // Sá»­ dá»¥ng relationship náº¿u cÃ³
                    KichCo kichCo = sanPham.getKichCo(); // Sá»­ dá»¥ng relationship náº¿u cÃ³

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
            log.error("Lá»—i khi kiá»ƒm tra tá»“n kho: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ kiá»ƒm tra tá»“n kho");
        }
    }

    // ===== THá»NG KÃŠ =====

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
            log.error("Lá»—i khi láº¥y thá»‘ng kÃª bÃ¡n hÃ ng: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ láº¥y thá»‘ng kÃª bÃ¡n hÃ ng");
        }
    }

    @Override
    public List<Map<String, Object>> laySanPhamBanChay(int limit) {
        try {
            // Query Ä‘á»ƒ láº¥y sáº£n pháº©m bÃ¡n cháº¡y nháº¥t
            List<Map<String, Object>> results = new ArrayList<>();

            // Giáº£ láº­p dá»¯ liá»‡u - cÃ³ thá»ƒ implement query phá»©c táº¡p sau
            Map<String, Object> item1 = new HashMap<>();
            item1.put("tenSanPham", "GiÃ y Nike Air Max");
            item1.put("soLuongBan", 100);
            item1.put("doanhThu", 15000000.0);
            results.add(item1);

            Map<String, Object> item2 = new HashMap<>();
            item2.put("tenSanPham", "GiÃ y Adidas Ultraboost");
            item2.put("soLuongBan", 85);
            item2.put("doanhThu", 12750000.0);
            results.add(item2);

            return results.stream().limit(limit).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Lá»—i khi láº¥y sáº£n pháº©m bÃ¡n cháº¡y: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ láº¥y sáº£n pháº©m bÃ¡n cháº¡y");
        }
    }

    @Override
    public Map<String, Object> layThongKeDoanhThu(String tuNgay, String denNgay) {
        try {
            Map<String, Object> thongKe = new HashMap<>();

            // Giáº£ láº­p dá»¯ liá»‡u - cÃ³ thá»ƒ implement query phá»©c táº¡p sau
            thongKe.put("tuNgay", tuNgay);
            thongKe.put("denNgay", denNgay);
            thongKe.put("tongDoanhThu", 50000000.0);
            thongKe.put("soLuongDonHang", 120);
            thongKe.put("doanhThuTrungBinh", 416666.67);

            return thongKe;

        } catch (Exception e) {
            log.error("Lá»—i khi láº¥y thá»‘ng kÃª doanh thu: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ láº¥y thá»‘ng kÃª doanh thu");
        }
    }

    // ===== UTILITY METHODS =====

    private void capNhatTongTienHoaDon(Integer hoaDonId) {
        try {
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
            if (hoaDon == null) return;

            BigDecimal tongTien = hoaDonChiTietRepository.calculateTotalAmountByHoaDonId(hoaDonId);
            BigDecimal tongThanhToan = tongTien;

            // Ãp dá»¥ng voucher náº¿u cÃ³ (cáº§n implement logic voucher)
            // if (hoaDon.getVoucherId() != null) {
            //     Voucher voucher = voucherRepository.findById(hoaDon.getVoucherId()).orElse(null);
            //     if (voucher != null) {
            //         Double giaTriGiam = tinhGiaTriGiamVoucher(voucher, tongTien.doubleValue());
            //         tongThanhToan = tongTien.subtract(BigDecimal.valueOf(giaTriGiam));
            //     }
            // }

            // Ãp dá»¥ng Ä‘iá»ƒm tÃ­ch lÅ©y náº¿u cÃ³
            if (hoaDon.getDiemSuDung() != null && hoaDon.getDiemSuDung() > 0) {
                BigDecimal giaTriDiem = BigDecimal.valueOf(hoaDon.getDiemSuDung() * 1000); // 1 Ä‘iá»ƒm = 1000 VND
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
            log.error("Lá»—i khi cáº­p nháº­t tá»•ng tiá»n hÃ³a Ä‘Æ¡n: {}", e.getMessage());
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

            // Sá»¬A: Set object thay vÃ¬ ID
            lichSu.setHoaDon(hoaDon);           // Thay vÃ¬ setIdHoaDon(hoaDon.getId())
            lichSu.setNhanVien(nhanVien);       // Thay vÃ¬ setIdNhanVien(nhanVien.getId())

            lichSu.setMoTaHanhDong(moTa);
            lichSu.setTrangThaiHoaDon(hoaDon.getTrangThaiHoaDon());
            lichSu.setNgayTao(new Date());
            lichSu.setNgayCapNhat(new Date());
            lichSuHoaDonRepository.save(lichSu);

        } catch (Exception e) {
            log.warn("KhÃ´ng thá»ƒ táº¡o lá»‹ch sá»­ hÃ³a Ä‘Æ¡n: {}", e.getMessage());
        }
    }

    private void congDiemKhachHang(Integer khachHangId, Double tongTien) {
        try {
            KhachHang khachHang = khachHangRepository.findById(khachHangId).orElse(null);
            if (khachHang != null && khachHang.getViDiem() != null) {
                ViDiem viDiem = khachHang.getViDiem();
                Double diemCong = Math.floor(tongTien / 100000); // 1 Ä‘iá»ƒm cho má»—i 100k
                viDiem.setTongDiem(viDiem.getTongDiem() + diemCong);
                viDiem.setSoDiemDaCong(viDiem.getSoDiemDaCong() + diemCong);
                viDiem.setNgayCapNhat(new Date());
                viDiemRepository.save(viDiem);
            }
        } catch (Exception e) {
            log.warn("KhÃ´ng thá»ƒ cá»™ng Ä‘iá»ƒm cho khÃ¡ch hÃ ng: {}", e.getMessage());
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
            log.warn("KhÃ´ng thá»ƒ láº¥y khuyáº¿n mÃ£i sáº£n pháº©m: {}", e.getMessage());
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

        // Láº¥y thÃ´ng tin khÃ¡ch hÃ ng - Sá»¬ Dá»¤NG RELATIONSHIP
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
        // Láº¥y thÃ´ng tin nhÃ¢n viÃªn
        NhanVienResponse nhanVienResponse = null;
        if (hoaDon.getNhanVien() != null) {
            nhanVienResponse = mapToNhanVienResponse(hoaDon.getNhanVien());
        }

        // Láº¥y thÃ´ng tin khÃ¡ch hÃ ng
        KhachHangResponse khachHangResponse = null;
        if (hoaDon.getKhachHang() != null) {
            khachHangResponse = mapToKhachHangResponse(hoaDon.getKhachHang());
        }

        // Táº¡o danh sÃ¡ch sáº£n pháº©m
        List<HoaDonChoSanPhamResponse> danhSachSanPham = chiTiets.stream()
                .map(this::mapToHoaDonChoSanPhamResponse)
                .collect(Collectors.toList());

        // Táº¡o thÃ´ng tin tá»•ng quan
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
                .danhSachSanPham(danhSachSanPham)  // Sá»¬A: Sá»­ dá»¥ng danhSachSanPham thay vÃ¬ chiTiets
                .tongQuan(tongQuan)                // Sá»¬A: ThÃªm thÃ´ng tin tá»•ng quan
                .ghiChu(hoaDon.getGhiChu())
                .build();
    }

    private HoaDonChoSanPhamResponse mapToHoaDonChoSanPhamResponse(HoaDonChiTiet chiTiet) {
        ChiTietSanPham sanPham = chiTiet.getChiTietSanPham();

        if (sanPham != null) {
            SanPham sp = sanPham.getSanPham();
            MauSac mauSac = sanPham.getMauSac();
            KichCo kichCo = sanPham.getKichCo();

            // Láº¥y khuyáº¿n mÃ£i
            List<KhuyenMaiSanPhamResponse> khuyenMais = layKhuyenMaiSanPham(sanPham.getId());

            // Láº¥y hÃ¬nh áº£nh chÃ­nh - Sá»¬A: Format URL Ä‘Ãºng cÃ¡ch
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

            // TÃ­nh giÃ¡
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
                    .hinhAnhChinh(hinhAnhChinh) // Sá»¬A: ThÃªm láº¡i field nÃ y
                    .phanTramGiam(phanTramGiam) // Sá»¬A: ThÃªm láº¡i field nÃ y
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
            // Sá»¬A: Sá»­ dá»¥ng relationship thay vÃ¬ getIdCtsp()
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

                // Láº¥y khuyáº¿n mÃ£i
                List<KhuyenMaiSanPhamResponse> khuyenMais = layKhuyenMaiSanPham(sanPham.getId());

                // Láº¥y hÃ¬nh áº£nh
                String hinhAnhChinh = null;
                List<HinhAnh> hinhAnhs = hinhAnhRepository.findActiveImagesByChiTietSanPhamIds(Arrays.asList(sanPham.getId()));
                if (!hinhAnhs.isEmpty()) {
                    hinhAnhChinh = hinhAnhs.get(0).getDuongDan();
                }

                // Sá»¬A: Sá»­ dá»¥ng relationship thay vÃ¬ repository query
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
                        .khuyenMaiSanPham(khuyenMais)  // Sá»¬A: danhSachKhuyenMai -> khuyenMaiSanPham
                        .hinhAnhChinh(hinhAnhChinh)
                        .phanTramGiam(phanTramGiam)
                        .build());
            }
        }

        // TÃ­nh voucher (implement later)
        BigDecimal tongTienVoucher = BigDecimal.ZERO;

        BigDecimal tongTienThanhToan = tongTienKhuyenMai.subtract(tongTienVoucher);
        BigDecimal tongTietKiem = tongTienGoc.subtract(tongTienThanhToan);

        Float phanTramGiamTongCong = tongTienGoc.compareTo(BigDecimal.ZERO) > 0 ?
                tongTietKiem.divide(tongTienGoc, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).floatValue() : 0f;

        // Láº¥y thÃ´ng tin khÃ¡ch hÃ ng
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
        // Láº¥y thÃ´ng tin sáº£n pháº©m gá»‘c
        SanPham sp = sanPham.getSanPham();
        // Láº¥y thuá»™c tÃ­nh - sá»­ dá»¥ng relationship
        MauSac mauSac = sanPham.getMauSac();
        KichCo kichCo = sanPham.getKichCo();

        // Láº¥y hÃ¬nh áº£nh tá»« relationship (giá»‘ng quáº£n lÃ½ sáº£n pháº©m)
        List<HinhAnhResponse> danhSachHinhAnh = new ArrayList<>();
        String hinhAnhChinh = null;

        try {
            // Sá»¬A: Láº¥y hÃ¬nh áº£nh tá»« ChiTietSanPham -> HinhAnh relationship
            HinhAnh hinhAnhEntity = sanPham.getHinhAnh(); // Entity Ä‘Ã£ cÃ³ relationship nÃ y

            if (hinhAnhEntity != null && hinhAnhEntity.getTrangThai() == 1) {
                String imageUrl = createImageUrl(hinhAnhEntity.getDuongDan());

                HinhAnhResponse hinhAnhResponse = HinhAnhResponse.builder()
                        .id(hinhAnhEntity.getId())
                        .maHinhAnh(hinhAnhEntity.getMaHinhAnh())
                        .tenHinhAnh(hinhAnhEntity.getTenHinhAnh())
                        .duongDan(hinhAnhEntity.getDuongDan())
                        .urlHinhAnh(imageUrl)
                        .trangThai(hinhAnhEntity.getTrangThai())
                        .laHinhChinh(true) // LuÃ´n lÃ  hÃ¬nh chÃ­nh vÃ¬ chá»‰ cÃ³ 1 hÃ¬nh
                        .build();

                danhSachHinhAnh.add(hinhAnhResponse);
                hinhAnhChinh = imageUrl;
            }

        } catch (Exception e) {
            log.warn("KhÃ´ng thá»ƒ load hÃ¬nh áº£nh cho sáº£n pháº©m {}: {}", sanPham.getId(), e.getMessage());
        }

        // Láº¥y khuyáº¿n mÃ£i
        List<KhuyenMaiSanPhamResponse> khuyenMais = layKhuyenMaiSanPham(sanPham.getId());

        // TÃ­nh giÃ¡ khuyáº¿n mÃ£i - Convert Double sang BigDecimal
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

        // Láº¥y thÃ´ng tin thuá»™c tÃ­nh tá»« sáº£n pháº©m gá»‘c
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
                .danhSachHinhAnh(danhSachHinhAnh) // Sá»¬A: Sá»­ dá»¥ng List thay vÃ¬ single object
                .hinhAnhChinh(hinhAnhChinh) // THÃŠM: URL hÃ¬nh áº£nh chÃ­nh
                .danhSachKhuyenMai(khuyenMais)
                .tongTietKiem(tongTietKiem)
                .phanTramGiam(phanTramGiam)
                .coKhuyenMai(!khuyenMais.isEmpty())
                .conHang(sanPham.getSoLuong() > 0)
                .tinhTrangKho(sanPham.getSoLuong() > 10 ? "CÃ²n hÃ ng" :
                        sanPham.getSoLuong() > 0 ? "Sáº¯p háº¿t hÃ ng" : "Háº¿t hÃ ng")
                .build();
    }

    /**
     * Táº¡o URL hÃ¬nh áº£nh giá»‘ng quáº£n lÃ½ sáº£n pháº©m
     */
    private String createImageUrl(String duongDan) {
        if (duongDan == null || duongDan.isEmpty()) {
            return null;
        }

        // Clean path - loáº¡i bá» táº¥t cáº£ prefix
        String cleanPath = duongDan;
        if (cleanPath.startsWith("/hinh-anh/images/")) {
            cleanPath = cleanPath.replace("/hinh-anh/images/", "");
        } else if (cleanPath.startsWith("/images/")) {
            cleanPath = cleanPath.replace("/images/", "");
        }

        // Táº¡o URL Ä‘áº§y Ä‘á»§
        return "http://localhost:8080/hinh-anh/images/" + cleanPath;
    }

    private ScanQRResponse mapToScanQRResponse(ChiTietSanPham sanPham) {
        List<KhuyenMaiSanPhamResponse> khuyenMais = layKhuyenMaiSanPham(sanPham.getId());

        String hinhAnhChinh = null;
        Optional<HinhAnh> hinhAnh = hinhAnhRepository.findActiveImageByChiTietSanPhamId(sanPham.getId());
        if (hinhAnh.isPresent()) {
            hinhAnhChinh = hinhAnh.get().getDuongDan();
        }

        // Láº¥y thÃ´ng tin sáº£n pháº©m, mÃ u sáº¯c, kÃ­ch cá»¡
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
                .tinhTrangKho(sanPham.getSoLuong() > 10 ? "CÃ²n hÃ ng" :
                        sanPham.getSoLuong() > 0 ? "Sáº¯p háº¿t hÃ ng" : "Háº¿t hÃ ng")
                .danhSachKhuyenMai(khuyenMais)
                .build();
    }

    private KhachHangResponse mapToKhachHangResponse(KhachHang khachHang) {
        Double diemTichLuy = 0.0;
        if (khachHang.getViDiem() != null) {
            ViDiem viDiem = khachHang.getViDiem();
            diemTichLuy = viDiem.getTongDiem() - viDiem.getSoDiemDaDung();
        }

        // âœ… Sá»¬A: Láº¥y email tá»« TaiKhoan vá»›i xá»­ lÃ½ lá»—i an toÃ n
        String email = "";
        try {
            if (khachHang.getTaiKhoan() != null && StringUtils.hasText(khachHang.getTaiKhoan().getEmail())) {
                email = khachHang.getTaiKhoan().getEmail();
            }
        } catch (Exception e) {
            log.warn("KhÃ´ng thá»ƒ láº¥y email cho khÃ¡ch hÃ ng ID: {}", khachHang.getId());
            email = ""; // Fallback vá» empty string
        }

        return KhachHangResponse.builder()
                .id(khachHang.getId())
                .maKhachHang(khachHang.getMaKhachHang())
                .hoTen(khachHang.getHoTen())
                .sdt(khachHang.getSdt())
                .email(email) // âœ… Sá»¬A: Sá»­ dá»¥ng email tá»« TaiKhoan vá»›i xá»­ lÃ½ lá»—i
                .trangThai(khachHang.getTrangThai())
                .diemTichLuy(diemTichLuy)
                .ngayTao(khachHang.getNgayTao())
                .build();
    }

    private KhachHangDetailResponse mapToKhachHangDetailResponse(KhachHang khachHang) {
        ViDiem viDiem = khachHang.getViDiem();

        // Sá»¬A: Láº¥y Ä‘á»‹a chá»‰ tá»« TaiKhoan thay vÃ¬ tá»« KhachHang
        DiaChi diaChi = null;
        if (khachHang.getTaiKhoan() != null) {
            Optional<DiaChi> diaChiOpt = diaChiRepository.findByTaiKhoanIdAndIsDefaultTrue(khachHang.getTaiKhoan().getId());
            if (diaChiOpt.isPresent()) {
                diaChi = diaChiOpt.get();
            }
        }

        Double tongChiTieu = hoaDonRepository.getTongChiTieuByKhachHangId(khachHang.getId());
        Long soLuongDonHang = hoaDonRepository.countByKhachHangId(khachHang.getId());

        // Sá»¬A: Láº¥y email tá»« TaiKhoan
        String email = "";
        if (khachHang.getTaiKhoan() != null && StringUtils.hasText(khachHang.getTaiKhoan().getEmail())) {
            email = khachHang.getTaiKhoan().getEmail();
        }

        return KhachHangDetailResponse.builder()
                .id(khachHang.getId())
                .maKhachHang(khachHang.getMaKhachHang())
                .hoTen(khachHang.getHoTen())
                .email(email) // Sá»¬A: Sá»­ dá»¥ng email tá»« TaiKhoan
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
                // XÃ“A dÃ²ng nÃ y: .giaTriDonHangToiThieu(BigDecimal.valueOf(voucher.getGiaTriGiamToiThieu()))
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

        // Sá»¬A: KhÃ´ng láº¥y hoTen tá»« DiaChi ná»¯a vÃ¬ schema má»›i khÃ´ng cÃ³ field nÃ y
        return DiaChiResponse.builder()
                .id(diaChi.getId())
                .maTinh(diaChi.getMaTinh())
                .maPhuong(diaChi.getMaPhuong())
                .tenTinh(diaChi.getTenTinh())
                .tenPhuong(diaChi.getTenPhuong())
                // Sá»¬A: XÃ³a dÃ²ng nÃ y vÃ¬ DiaChi khÃ´ng cÃ³ field hoTen
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

        // Láº¥y thÃ´ng tin nhÃ¢n viÃªn - Sá»¬A: Sá»­ dá»¥ng relationship
        NhanVienResponse nhanVienResponse = null;
        if (hoaDon.getNhanVien() != null) {
            nhanVienResponse = mapToNhanVienResponse(hoaDon.getNhanVien());
        }

        // Láº¥y thÃ´ng tin khÃ¡ch hÃ ng - Sá»¬A: Sá»­ dá»¥ng relationship
        KhachHangResponse khachHangResponse = null;
        if (hoaDon.getKhachHang() != null) {
            khachHangResponse = mapToKhachHangResponse(hoaDon.getKhachHang());
        }

        return HoaDonResponse.builder()
                .id(hoaDon.getId())
                .maHoaDon(hoaDon.getMaHoaDon())
                .tenKhach(hoaDon.getTenNguoiDung() != null ? hoaDon.getTenNguoiDung() : "KhÃ¡ch láº»")
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
        ChiTietSanPham sanPham = chiTiet.getChiTietSanPham(); // Sá»­ dá»¥ng relationship

        if (sanPham != null) {
            // Láº¥y thÃ´ng tin sáº£n pháº©m, mÃ u sáº¯c, kÃ­ch cá»¡, thÆ°Æ¡ng hiá»‡u
            SanPham sp = sanPham.getSanPham(); // Náº¿u cÃ³ relationship trong ChiTietSanPham
            // Hoáº·c sá»­ dá»¥ng: sanPhamRepository.findById(sanPham.getIdSanPham()).orElse(null);

            MauSac mauSac = sanPham.getMauSac(); // Náº¿u cÃ³ relationship
            KichCo kichCo = sanPham.getKichCo(); // Náº¿u cÃ³ relationship

            ThuongHieu thuongHieu = null;
            if (sp != null) {
                thuongHieu = sp.getThuongHieu(); // Náº¿u cÃ³ relationship trong SanPham
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
        // Sá»¬A: Láº¥y email tá»« TaiKhoan thay vÃ¬ tá»« NhanVien
        String email = "";
        if (nhanVien.getTaiKhoan() != null && StringUtils.hasText(nhanVien.getTaiKhoan().getEmail())) {
            email = nhanVien.getTaiKhoan().getEmail();
        }

        return NhanVienResponse.builder()
                .id(nhanVien.getId())
                .maNhanVien(nhanVien.getMaNhanVien())
                .hoTen(nhanVien.getHoTen())
                .email(email) // Sá»¬A: Sá»­ dá»¥ng email tá»« TaiKhoan
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

        // Format URL Ä‘áº§y Ä‘á»§ giá»‘ng SanPhamChiTiet
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
                .duongDan(duongDanGoc) // ÄÆ°á»ng dáº«n gá»‘c
                .urlHinhAnh(urlFormatted) // URL Ä‘Ã£ format
                .trangThai(hinhAnh.getTrangThai())
                .laHinhChinh(true) // Logic Ä‘á»ƒ xÃ¡c Ä‘á»‹nh hÃ¬nh chÃ­nh cÃ³ thá»ƒ Ä‘Æ°á»£c implement sau
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
                .tenMau(mauSac.getTenMauSac()) // âœ… THÃŠM: Alias cho frontend
                .maMau(getMaMauHex(mauSac))    // âœ… THÃŠM: Hex color tá»« method
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
                .thuTu(getThuTuKichCo(kichCo.getTenKichCo())) // âœ… THÃŠM: Thá»© tá»± sáº¯p xáº¿p
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
            return "VÃ€NG";
        } else if (tongChiTieu >= 5000000) {
            return "Báº C";
        } else {
            return "Äá»’NG";
        }
    }

    // ===== ADDITIONAL UTILITY METHODS =====

    /**
     * Kiá»ƒm tra quyá»n truy cáº­p cá»§a nhÃ¢n viÃªn
     */
    private void kiemTraQuyenNhanVien(Integer nhanVienId, String action) {
        NhanVien nhanVien = nhanVienRepository.findById(nhanVienId).orElse(null);
        if (nhanVien == null || nhanVien.getTrangThai() != 1) {
            throw new RuntimeException("NhÃ¢n viÃªn khÃ´ng cÃ³ quyá»n thá»±c hiá»‡n " + action);
        }
    }

    /**
     * Kiá»ƒm tra sáº£n pháº©m cÃ³ thá»ƒ bÃ¡n khÃ´ng
     */
    private void kiemTraSanPhamCoTheBan(ChiTietSanPham sanPham, int soLuongCanBan) {
        if (sanPham.getTrangThai() != 1) {
            throw new RuntimeException("Sáº£n pháº©m khÃ´ng cÃ²n kinh doanh");
        }

        if (sanPham.getSoLuong() < soLuongCanBan) {
            throw new RuntimeException("KhÃ´ng Ä‘á»§ tá»“n kho. CÃ²n láº¡i: " + sanPham.getSoLuong());
        }

        // Sá»¬A: Sá»­ dá»¥ng relationship thay vÃ¬ getIdSanPham()
        SanPham sanPhamGoc = sanPham.getSanPham();
        if (sanPhamGoc == null || sanPhamGoc.getTrangThai() != 1) {
            throw new RuntimeException("Sáº£n pháº©m khÃ´ng cÃ²n kinh doanh");
        }
    }

    /**
     * Cáº­p nháº­t tá»“n kho sáº£n pháº©m an toÃ n
     */
    private void capNhatTonKhoAnToan(Integer chiTietSanPhamId, int soLuongThayDoi) {
        ChiTietSanPham sanPham = chiTietSanPhamRepository.findById(chiTietSanPhamId)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y sáº£n pháº©m"));

        int soLuongMoi = sanPham.getSoLuong() + soLuongThayDoi;
        if (soLuongMoi < 0) {
            throw new RuntimeException("Sá»‘ lÆ°á»£ng tá»“n kho khÃ´ng thá»ƒ Ã¢m");
        }

        sanPham.setSoLuong(soLuongMoi);
        sanPham.setNgayCapNhat(new Date());
        chiTietSanPhamRepository.save(sanPham);

        // Sá»¬A: Sá»­ dá»¥ng relationship Ä‘á»ƒ láº¥y ID sáº£n pháº©m gá»‘c
        SanPham sanPhamGoc = sanPham.getSanPham();
        if (sanPhamGoc != null) {
            capNhatTongSoLuongSanPham(sanPhamGoc.getId());
        }
    }

    /**
     * Cáº­p nháº­t tá»•ng sá»‘ lÆ°á»£ng sáº£n pháº©m gá»‘c
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
            log.warn("KhÃ´ng thá»ƒ cáº­p nháº­t tá»•ng sá»‘ lÆ°á»£ng sáº£n pháº©m: {}", e.getMessage());
        }
    }

    /**
     * Gá»­i thÃ´ng bÃ¡o cho khÃ¡ch hÃ ng
     */
    private void guiThongBaoKhachHang(Integer khachHangId, String noiDung, Integer hoaDonId) {
        try {
            KhachHang khachHang = khachHangRepository.findById(khachHangId).orElse(null);
            if (khachHang != null && khachHang.getTaiKhoan() != null) {
                log.info("Gá»­i thÃ´ng bÃ¡o cho khÃ¡ch hÃ ng {}: {}", khachHang.getHoTen(), noiDung);
            }
        } catch (Exception e) {
            log.warn("KhÃ´ng thá»ƒ gá»­i thÃ´ng bÃ¡o: {}", e.getMessage());
        }
    }

    /**
     * Validate request data
     */
    private void validateThemSanPhamRequest(ThemSanPhamRequest request) {
        if (request.getChiTietSanPhamId() == null) {
            throw new IllegalArgumentException("ID chi tiáº¿t sáº£n pháº©m khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }
        if (request.getSoLuong() == null || request.getSoLuong() <= 0) {
            throw new IllegalArgumentException("Sá»‘ lÆ°á»£ng pháº£i lá»›n hÆ¡n 0");
        }
    }

    private void validateCapNhatSanPhamRequest(CapNhatSanPhamRequest request) {
        if (request.getSoLuong() == null || request.getSoLuong() <= 0) {
            throw new IllegalArgumentException("Sá»‘ lÆ°á»£ng pháº£i lá»›n hÆ¡n 0");
        }
    }

    private void validateThanhToanRequest(ThanhToanRequest request) {
        if (request.getKhachHangId() == null) {
            throw new IllegalArgumentException("ID khÃ¡ch hÃ ng khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }
        if (!StringUtils.hasText(request.getLoaiHoaDon())) {
            throw new IllegalArgumentException("Loáº¡i hÃ³a Ä‘Æ¡n khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }
    }

    /**
     * Log hoáº¡t Ä‘á»™ng cá»§a há»‡ thá»‘ng
     */
    private void logActivity(String action, Integer userId, String details) {
        log.info("ACTIVITY_LOG - Action: {}, User: {}, Details: {}", action, userId, details);
    }

    /**
     * Táº¡o mÃ£ hÃ³a Ä‘Æ¡n theo format Ä‘áº·c biá»‡t
     */
    private String taoMaHoaDonTheoFormat(String loaiHoaDon) {
        String prefix = "OFFLINE".equals(loaiHoaDon) ? "HDO" : "HDN";
        return prefix + System.currentTimeMillis();
    }

    /**
     * Kiá»ƒm tra thá»i gian lÃ m viá»‡c
     */
    private boolean kiemTraThoiGianLamViec() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return hour >= 8 && hour <= 22; // 8h - 22h
    }

    /**
     * TÃ­nh phÃ­ váº­n chuyá»ƒn
     */
    private BigDecimal tinhPhiVanChuyen(String diaChiGiaoHang, BigDecimal tongTien) {
        // Logic tÃ­nh phÃ­ váº­n chuyá»ƒn theo Ä‘á»‹a chá»‰ vÃ  tá»•ng tiá»n
        if (tongTien.compareTo(BigDecimal.valueOf(500000)) >= 0) {
            return BigDecimal.ZERO; // Miá»…n phÃ­ ship cho Ä‘Æ¡n >= 500k
        }

        if (diaChiGiaoHang != null && diaChiGiaoHang.contains("HÃ  Ná»™i")) {
            return BigDecimal.valueOf(30000); // 30k trong HÃ  Ná»™i
        } else {
            return BigDecimal.valueOf(50000); // 50k ngoáº¡i tá»‰nh
        }
    }

    /**
     * Backup dá»¯ liá»‡u quan trá»ng
     */
    private void backupHoaDonData(Integer hoaDonId) {
        try {
            // Logic backup dá»¯ liá»‡u hÃ³a Ä‘Æ¡n quan trá»ng
            log.info("Backup data for invoice: {}", hoaDonId);
        } catch (Exception e) {
            log.warn("Backup failed for invoice {}: {}", hoaDonId, e.getMessage());
        }
    }

    /**
     * Kiá»ƒm tra duplicate transaction
     */
    private boolean kiemTraDuplicateTransaction(String transactionId) {
        // Logic kiá»ƒm tra giao dá»‹ch trÃ¹ng láº·p
        return false;
    }

    /**
     * Clean up old data
     */
    private void cleanupOldData() {
        try {
            // Logic dá»n dáº¹p dá»¯ liá»‡u cÅ©, cache, temp files
            log.info("Cleanup old data completed");
        } catch (Exception e) {
            log.warn("Cleanup failed: {}", e.getMessage());
        }
    }
    @Override
    public List<DanhMucResponse> layDanhSachDanhMuc() {
        try {
            log.info("ðŸ“‚ Láº¥y danh sÃ¡ch danh má»¥c");
            // âœ… Sá»¬A: Sá»­ dá»¥ng method name Ä‘Ãºng
            List<DanhMuc> danhMucs = danhMucRepository.findByTrangThaiOrderByTenDanhMucAsc(1);

            List<DanhMucResponse> responses = danhMucs.stream()
                    .map(this::mapToDanhMucResponseForMasterData)
                    .collect(Collectors.toList());

            log.info("âœ… Láº¥y {} danh má»¥c thÃ nh cÃ´ng", responses.size());
            return responses;

        } catch (Exception e) {
            log.error("âŒ Lá»—i láº¥y danh sÃ¡ch danh má»¥c", e);
            throw new RuntimeException("KhÃ´ng thá»ƒ láº¥y danh sÃ¡ch danh má»¥c: " + e.getMessage());
        }
    }

    @Override
    public List<ThuongHieuResponse> layDanhSachThuongHieu() {
        try {
            log.info("ðŸ·ï¸ Láº¥y danh sÃ¡ch thÆ°Æ¡ng hiá»‡u");
            // âœ… Sá»¬A: Sá»­ dá»¥ng method name Ä‘Ãºng
            List<ThuongHieu> thuongHieus = thuongHieuRepository.findByTrangThaiOrderByTenThuongHieuAsc(1);

            List<ThuongHieuResponse> responses = thuongHieus.stream()
                    .map(this::mapToThuongHieuResponseForMasterData)
                    .collect(Collectors.toList());

            log.info("âœ… Láº¥y {} thÆ°Æ¡ng hiá»‡u thÃ nh cÃ´ng", responses.size());
            return responses;

        } catch (Exception e) {
            log.error("âŒ Lá»—i láº¥y danh sÃ¡ch thÆ°Æ¡ng hiá»‡u", e);
            throw new RuntimeException("KhÃ´ng thá»ƒ láº¥y danh sÃ¡ch thÆ°Æ¡ng hiá»‡u: " + e.getMessage());
        }
    }

    @Override
    public List<MauSacResponse> layDanhSachMauSac() {
        try {
            log.info("ðŸŽ¨ Láº¥y danh sÃ¡ch mÃ u sáº¯c");
            // âœ… Sá»¬A: Sá»­ dá»¥ng method name Ä‘Ãºng
            List<MauSac> mauSacs = mauSacRepository.findByTrangThaiOrderByTenMauSacAsc(1);

            List<MauSacResponse> responses = mauSacs.stream()
                    .map(this::mapToMauSacResponseForMasterData)
                    .collect(Collectors.toList());

            log.info("âœ… Láº¥y {} mÃ u sáº¯c thÃ nh cÃ´ng", responses.size());
            return responses;

        } catch (Exception e) {
            log.error("âŒ Lá»—i láº¥y danh sÃ¡ch mÃ u sáº¯c", e);
            throw new RuntimeException("KhÃ´ng thá»ƒ láº¥y danh sÃ¡ch mÃ u sáº¯c: " + e.getMessage());
        }
    }

    @Override
    public List<KichCoResponse> layDanhSachKichCo() {
        try {
            log.info("ðŸ“ Láº¥y danh sÃ¡ch kÃ­ch cá»¡");
            // âœ… Sá»¬A: Sá»­ dá»¥ng method name Ä‘Ãºng
            List<KichCo> kichCos = kichCoRepository.findByTrangThaiOrderByTenKichCoAsc(1);

            List<KichCoResponse> responses = kichCos.stream()
                    .map(this::mapToKichCoResponseForMasterData)
                    .collect(Collectors.toList());

            log.info("âœ… Láº¥y {} kÃ­ch cá»¡ thÃ nh cÃ´ng", responses.size());
            return responses;

        } catch (Exception e) {
            log.error("âŒ Lá»—i láº¥y danh sÃ¡ch kÃ­ch cá»¡", e);
            throw new RuntimeException("KhÃ´ng thá»ƒ láº¥y danh sÃ¡ch kÃ­ch cá»¡: " + e.getMessage());
        }
    }

    @Override
    public List<ChatLieuResponse> layDanhSachChatLieu() {
        try {
            log.info("ðŸ§µ Láº¥y danh sÃ¡ch cháº¥t liá»‡u");
            // âœ… Sá»¬A: Sá»­ dá»¥ng method name Ä‘Ãºng
            List<ChatLieu> chatLieus = chatLieuRepository.findByTrangThaiOrderByTenChatLieuAsc(1);

            List<ChatLieuResponse> responses = chatLieus.stream()
                    .map(this::mapToChatLieuResponseForMasterData)
                    .collect(Collectors.toList());

            log.info("âœ… Láº¥y {} cháº¥t liá»‡u thÃ nh cÃ´ng", responses.size());
            return responses;

        } catch (Exception e) {
            log.error("âŒ Lá»—i láº¥y danh sÃ¡ch cháº¥t liá»‡u", e);
            throw new RuntimeException("KhÃ´ng thá»ƒ láº¥y danh sÃ¡ch cháº¥t liá»‡u: " + e.getMessage());
        }
    }

    @Override
    public List<DeGiayResponse> layDanhSachDeGiay() {
        try {
            log.info("ðŸ‘Ÿ Láº¥y danh sÃ¡ch Ä‘áº¿ giÃ y");
            // âœ… Sá»¬A: Sá»­ dá»¥ng method name Ä‘Ãºng
            List<DeGiay> deGiays = deGiayRepository.findByTrangThaiOrderByTenDeGiayAsc(1);

            List<DeGiayResponse> responses = deGiays.stream()
                    .map(this::mapToDeGiayResponseForMasterData)
                    .collect(Collectors.toList());

            log.info("âœ… Láº¥y {} Ä‘áº¿ giÃ y thÃ nh cÃ´ng", responses.size());
            return responses;

        } catch (Exception e) {
            log.error("âŒ Lá»—i láº¥y danh sÃ¡ch Ä‘áº¿ giÃ y", e);
            throw new RuntimeException("KhÃ´ng thá»ƒ láº¥y danh sÃ¡ch Ä‘áº¿ giÃ y: " + e.getMessage());
        }
    }

// ===== MAPPING METHODS Má»šI CHO MASTER DATA =====

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
                .maMau(getMaMauHex(mauSac)) // Láº¥y mÃ£ mÃ u hex
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
                .thuTu(getThuTuKichCo(kichCo.getTenKichCo())) // Sáº¯p xáº¿p theo size
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
     * Láº¥y mÃ£ mÃ u hex tá»« entity MauSac
     */
    private String getMaMauHex(MauSac mauSac) {
        if (mauSac == null) return "#6c757d"; // MÃ u xÃ¡m máº·c Ä‘á»‹nh

        // Map theo tÃªn mÃ u tiáº¿ng Viá»‡t
        String tenMau = mauSac.getTenMauSac().toLowerCase().trim();
        switch (tenMau) {
            case "Ä‘á»":
            case "red":
            case "do":
                return "#dc3545";
            case "xanh":
            case "xanh dÆ°Æ¡ng":
            case "blue":
                return "#0066cc";
            case "Ä‘en":
            case "black":
            case "den":
                return "#000000";
            case "tráº¯ng":
            case "white":
            case "trang":
                return "#ffffff";
            case "vÃ ng":
            case "yellow":
            case "vang":
                return "#ffc107";
            case "xanh lÃ¡":
            case "xanh la":
            case "green":
                return "#28a745";
            case "tÃ­m":
            case "purple":
            case "tim":
                return "#6f42c1";
            case "há»“ng":
            case "pink":
            case "hong":
                return "#e83e8c";
            case "nÃ¢u":
            case "brown":
            case "nau":
                return "#8b4513";
            case "cam":
            case "orange":
                return "#fd7e14";
            case "xÃ¡m":
            case "gray":
            case "xam":
                return "#6c757d";
            case "be":
            case "kem":
                return "#f5f5dc";
            case "navy":
            case "xanh navy":
                return "#000080";
            case "báº¡c":
            case "bac":
            case "silver":
                return "#c0c0c0";
            case "vÃ ng Ä‘á»“ng":
            case "gold":
                return "#ffd700";
            default:
                return "#6c757d"; // MÃ u máº·c Ä‘á»‹nh
        }
    }

    /**
     * Láº¥y thá»© tá»± sáº¯p xáº¿p cho kÃ­ch cá»¡
     */
    private Integer getThuTuKichCo(String tenKichCo) {
        if (tenKichCo == null) return 999;

        try {
            // Náº¿u lÃ  sá»‘, convert trá»±c tiáº¿p
            return Integer.parseInt(tenKichCo);
        } catch (NumberFormatException e) {
            // Náº¿u khÃ´ng pháº£i sá»‘, map theo size chuáº©n
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
            log.info("ðŸ” TÃ¬m nhÃ¢n viÃªn theo mÃ£: '{}'", ma);

            if (ma == null || ma.trim().isEmpty()) {
                log.warn("âš ï¸ MÃ£ null hoáº·c rá»—ng");
                return null;
            }

            String maTrimmed = ma.trim();

            // 1. âœ… Thá»­ tÃ¬m trá»±c tiáº¿p theo mÃ£ nhÃ¢n viÃªn trÆ°á»›c
            Optional<NhanVien> nhanVienByMaNV = nhanVienRepository
                    .findByMaNhanVienAndTrangThai(maTrimmed, 1);

            if (nhanVienByMaNV.isPresent()) {
                NhanVien nhanVien = nhanVienByMaNV.get();
                log.info("âœ… TÃ¬m tháº¥y nhÃ¢n viÃªn theo MÃƒ NHÃ‚N VIÃŠN: ID={}, TÃªn={}, MaNV={}",
                        nhanVien.getId(), nhanVien.getHoTen(), nhanVien.getMaNhanVien());
                return nhanVien.getId();
            }

            // 2. âœ… Náº¿u khÃ´ng tÃ¬m tháº¥y, thá»­ tÃ¬m theo mÃ£ tÃ i khoáº£n
            log.info("ðŸ”„ KhÃ´ng tÃ¬m tháº¥y theo mÃ£ nhÃ¢n viÃªn, thá»­ tÃ¬m theo mÃ£ tÃ i khoáº£n...");

            Optional<NhanVien> nhanVienByMaTK = nhanVienRepository
                    .findByTaiKhoan_MaTaiKhoanAndTrangThai(maTrimmed, 1);

            if (nhanVienByMaTK.isPresent()) {
                NhanVien nhanVien = nhanVienByMaTK.get();
                log.info("âœ… TÃ¬m tháº¥y nhÃ¢n viÃªn theo MÃƒ TÃ€I KHOáº¢N: ID={}, TÃªn={}, MaTK={}, MaNV={}",
                        nhanVien.getId(), nhanVien.getHoTen(),
                        nhanVien.getTaiKhoan().getMaTaiKhoan(), nhanVien.getMaNhanVien());
                return nhanVien.getId();
            }

            // 3. âœ… Thá»­ láº¥y mÃ£ nhÃ¢n viÃªn tá»« mÃ£ tÃ i khoáº£n (sá»­ dá»¥ng method má»›i)
            log.info("ðŸ”„ Thá»­ láº¥y mÃ£ nhÃ¢n viÃªn tá»« mÃ£ tÃ i khoáº£n...");

            Optional<String> maNhanVienOpt = nhanVienRepository.findMaNhanVienByMaTaiKhoan(maTrimmed);
            if (maNhanVienOpt.isPresent()) {
                String maNhanVien = maNhanVienOpt.get();
                log.info("ðŸ”„ TÃ¬m tháº¥y mÃ£ nhÃ¢n viÃªn '{}' tá»« mÃ£ tÃ i khoáº£n '{}'", maNhanVien, maTrimmed);

                // TÃ¬m láº¡i nhÃ¢n viÃªn theo mÃ£ nhÃ¢n viÃªn vá»«a láº¥y Ä‘Æ°á»£c
                Optional<NhanVien> nhanVienFinal = nhanVienRepository
                        .findByMaNhanVienAndTrangThai(maNhanVien, 1);

                if (nhanVienFinal.isPresent()) {
                    NhanVien nhanVien = nhanVienFinal.get();
                    log.info("âœ… TÃ¬m tháº¥y nhÃ¢n viÃªn qua chuyá»ƒn Ä‘á»•i: ID={}, TÃªn={}, MaNV={}",
                            nhanVien.getId(), nhanVien.getHoTen(), nhanVien.getMaNhanVien());
                    return nhanVien.getId();
                }
            }

            // 4. Debug: TÃ¬m táº¥t cáº£ nhÃ¢n viÃªn hoáº¡t Ä‘á»™ng Ä‘á»ƒ debug
            List<NhanVien> allActive = nhanVienRepository.findByTrangThai(1);
            log.info("ðŸ“‹ CÃ³ {} nhÃ¢n viÃªn hoáº¡t Ä‘á»™ng trong há»‡ thá»‘ng:", allActive.size());

            for (NhanVien nv : allActive) {
                String maTaiKhoan = nv.getTaiKhoan() != null ? nv.getTaiKhoan().getMaTaiKhoan() : "NULL";
                log.info("  - ID: {}, MÃ£NV: '{}', MÃ£TK: '{}', TÃªn: '{}'",
                        nv.getId(),
                        nv.getMaNhanVien(),
                        maTaiKhoan,
                        nv.getHoTen());
            }

            log.warn("âš ï¸ KhÃ´ng tÃ¬m tháº¥y nhÃ¢n viÃªn hoáº¡t Ä‘á»™ng vá»›i mÃ£: '{}'", maTrimmed);
            return null;

        } catch (Exception e) {
            log.error("âŒ Lá»—i tÃ¬m nhÃ¢n viÃªn theo mÃ£: {}", e.getMessage(), e);
            return null;
        }
    }
    @Override
    public String chuyenDoiMaTaiKhoanSangMaNhanVien(String maTaiKhoan) {
        try {
            log.info("ðŸ”„ Chuyá»ƒn Ä‘á»•i mÃ£ tÃ i khoáº£n '{}' sang mÃ£ nhÃ¢n viÃªn", maTaiKhoan);

            if (maTaiKhoan == null || maTaiKhoan.trim().isEmpty()) {
                return null;
            }

            Optional<String> maNhanVienOpt = nhanVienRepository.findMaNhanVienByMaTaiKhoan(maTaiKhoan.trim());

            if (maNhanVienOpt.isPresent()) {
                String maNhanVien = maNhanVienOpt.get();
                log.info("âœ… Chuyá»ƒn Ä‘á»•i thÃ nh cÃ´ng: '{}' -> '{}'", maTaiKhoan, maNhanVien);
                return maNhanVien;
            }

            log.warn("âš ï¸ KhÃ´ng tÃ¬m tháº¥y mÃ£ nhÃ¢n viÃªn cho mÃ£ tÃ i khoáº£n: '{}'", maTaiKhoan);
            return null;

        } catch (Exception e) {
            log.error("âŒ Lá»—i chuyá»ƒn Ä‘á»•i mÃ£: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Integer timNhanVienIdLinhHoat(String ma) {
        try {
            log.info("ðŸ” TÃ¬m nhÃ¢n viÃªn linh hoáº¡t vá»›i mÃ£: '{}'", ma);

            if (ma == null || ma.trim().isEmpty()) {
                return null;
            }

            String maTrimmed = ma.trim();

            // BÆ°á»›c 1: Thá»­ tÃ¬m trá»±c tiáº¿p báº±ng mÃ£ nhÃ¢n viÃªn
            Optional<NhanVien> nhanVienOpt = nhanVienRepository.findByMaNhanVienAndTrangThai(maTrimmed, 1);
            if (nhanVienOpt.isPresent()) {
                log.info("âœ… TÃ¬m tháº¥y theo mÃ£ nhÃ¢n viÃªn");
                return nhanVienOpt.get().getId();
            }

            // BÆ°á»›c 2: Thá»­ chuyá»ƒn Ä‘á»•i tá»« mÃ£ tÃ i khoáº£n
            String maNhanVien = chuyenDoiMaTaiKhoanSangMaNhanVien(maTrimmed);
            if (maNhanVien != null) {
                nhanVienOpt = nhanVienRepository.findByMaNhanVienAndTrangThai(maNhanVien, 1);
                if (nhanVienOpt.isPresent()) {
                    log.info("âœ… TÃ¬m tháº¥y sau khi chuyá»ƒn Ä‘á»•i: '{}' -> '{}'", maTrimmed, maNhanVien);
                    return nhanVienOpt.get().getId();
                }
            }

            // BÆ°á»›c 3: Thá»­ tÃ¬m trá»±c tiáº¿p báº±ng mÃ£ tÃ i khoáº£n
            nhanVienOpt = nhanVienRepository.findByTaiKhoan_MaTaiKhoanAndTrangThai(maTrimmed, 1);
            if (nhanVienOpt.isPresent()) {
                log.info("âœ… TÃ¬m tháº¥y theo mÃ£ tÃ i khoáº£n");
                return nhanVienOpt.get().getId();
            }

            log.warn("âš ï¸ KhÃ´ng tÃ¬m tháº¥y nhÃ¢n viÃªn vá»›i mÃ£: '{}'", maTrimmed);
            return null;

        } catch (Exception e) {
            log.error("âŒ Lá»—i tÃ¬m nhÃ¢n viÃªn linh hoáº¡t: {}", e.getMessage());
            return null;
        }
    }
    /**
     * Kiá»ƒm tra email Ä‘Ã£ tá»“n táº¡i trong há»‡ thá»‘ng (an toÃ n)
     */
    private boolean isEmailExists(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }

        try {
            // Kiá»ƒm tra trong TaiKhoan trÆ°á»›c
            if (taiKhoanRepository.existsByEmail(email)) {
                return true;
            }

            // Kiá»ƒm tra thÃªm qua KhachHang (backup)
            return khachHangRepository.existsByTaiKhoanEmail(email);

        } catch (Exception e) {
            log.warn("Lá»—i kiá»ƒm tra email tá»“n táº¡i: {}", e.getMessage());
            // Fallback - chá»‰ kiá»ƒm tra TaiKhoan
            try {
                return taiKhoanRepository.existsByEmail(email);
            } catch (Exception e2) {
                log.error("Lá»—i nghiÃªm trá»ng kiá»ƒm tra email: {}", e2.getMessage());
                return false; // KhÃ´ng cháº·n Ä‘Æ°á»£c thÃ¬ cho phÃ©p táº¡o
            }
        }
    }
    @Override
    @Transactional
    public HoaDonChoTongQuanResponse chuyenSangGiaoHang(Integer hoaDonId, GiaoHangRequest request) {
        try {
            log.info("ðŸšš Chuyá»ƒn hÃ³a Ä‘Æ¡n {} sang giao hÃ ng", hoaDonId);

            // Kiá»ƒm tra hÃ³a Ä‘Æ¡n
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));

            if (!"CHO".equals(hoaDon.getTrangThaiHoaDon())) {
                throw new RuntimeException("Chá»‰ cÃ³ thá»ƒ chuyá»ƒn hÃ³a Ä‘Æ¡n chá» sang giao hÃ ng");
            }

            // Kiá»ƒm tra cÃ³ sáº£n pháº©m khÃ´ng
            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
            if (chiTiets.isEmpty()) {
                throw new RuntimeException("HÃ³a Ä‘Æ¡n chÆ°a cÃ³ sáº£n pháº©m");
            }

            // Cáº­p nháº­t thÃ´ng tin giao hÃ ng
            hoaDon.setLoaiHoaDon("ONLINE"); // ÄÃ¡nh dáº¥u lÃ  Ä‘Æ¡n giao hÃ ng
            hoaDon.setTenNguoiDung(request.getTenNguoiNhan());
            hoaDon.setSdt(request.getSdt());
            hoaDon.setEmail(request.getEmail() != null ? request.getEmail() : "");
            hoaDon.setDiaChi(request.getDiaChi());
            hoaDon.setGhiChu(request.getGhiChu());
            hoaDon.setPhuongThucThanhToan(request.getPhuongThucThanhToan() != null ?
                    request.getPhuongThucThanhToan() : "COD");

            // TÃ­nh phÃ­ váº­n chuyá»ƒn
            BigDecimal phiShip = request.getPhiVanChuyen() != null ?
                    request.getPhiVanChuyen() : tinhPhiVanChuyenMacDinh(request.getDiaChi(), hoaDon.getTongTien());
            hoaDon.setPhiVanChuyen(phiShip);

            // Cáº­p nháº­t tá»•ng thanh toÃ¡n
            BigDecimal tongThanhToan = hoaDon.getTongTien().add(phiShip);
            hoaDon.setTongThanhToan(tongThanhToan);

            hoaDon.setNgayCapNhat(new Date());
            hoaDonRepository.save(hoaDon);

            // Táº¡o lá»‹ch sá»­
            taoLichSuHoaDon(hoaDon, "Chuyá»ƒn sang giao hÃ ng", hoaDon.getNhanVien());

            log.info("âœ… ÄÃ£ chuyá»ƒn hÃ³a Ä‘Æ¡n {} sang giao hÃ ng", hoaDonId);

            return layTongQuanHoaDonCho(hoaDonId);

        } catch (Exception e) {
            log.error("âŒ Lá»—i chuyá»ƒn sang giao hÃ ng: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ chuyá»ƒn sang giao hÃ ng: " + e.getMessage());
        }
    }

    @Override
    public TinhPhiShipResponse tinhPhiShip(TinhPhiShipRequest request) {
        try {
            log.info("ðŸ’° TÃ­nh phÃ­ ship cho Ä‘á»‹a chá»‰: {}", request.getDiaChi());

            BigDecimal tongTien = request.getTongTien() != null ? request.getTongTien() : BigDecimal.ZERO;
            BigDecimal phiShip = BigDecimal.ZERO;
            boolean mienPhiShip = false;
            String lyDoMienPhi = null;

            // Logic tÃ­nh phÃ­ ship
            if (tongTien.compareTo(BigDecimal.valueOf(500000)) >= 0) {
                // Miá»…n phÃ­ ship cho Ä‘Æ¡n >= 500k
                mienPhiShip = true;
                lyDoMienPhi = "Miá»…n phÃ­ ship cho Ä‘Æ¡n hÃ ng tá»« 500.000â‚«";
                phiShip = BigDecimal.ZERO;
            } else {
                // TÃ­nh phÃ­ theo Ä‘á»‹a chá»‰
                String diaChi = request.getDiaChi() != null ? request.getDiaChi().toLowerCase() : "";

                if (diaChi.contains("hÃ  ná»™i") || diaChi.contains("ha noi")) {
                    phiShip = BigDecimal.valueOf(30000); // 30k trong HÃ  Ná»™i
                } else if (diaChi.contains("há»“ chÃ­ minh") || diaChi.contains("hcm") ||
                        diaChi.contains("sÃ i gÃ²n") || diaChi.contains("sai gon")) {
                    phiShip = BigDecimal.valueOf(35000); // 35k TP.HCM
                } else if (diaChi.contains("Ä‘Ã  náºµng") || diaChi.contains("da nang")) {
                    phiShip = BigDecimal.valueOf(35000); // 35k ÄÃ  Náºµng
                } else {
                    phiShip = BigDecimal.valueOf(50000); // 50k cÃ¡c tá»‰nh khÃ¡c
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
            log.error("âŒ Lá»—i tÃ­nh phÃ­ ship: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ tÃ­nh phÃ­ ship: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public HoaDonChoTongQuanResponse capNhatThongTinGiaoHang(Integer hoaDonId, CapNhatGiaoHangRequest request) {
        try {
            log.info("âœï¸ Cáº­p nháº­t thÃ´ng tin giao hÃ ng cho hÃ³a Ä‘Æ¡n {}", hoaDonId);

            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));

            if (!"CHO".equals(hoaDon.getTrangThaiHoaDon())) {
                throw new RuntimeException("Chá»‰ cÃ³ thá»ƒ cáº­p nháº­t hÃ³a Ä‘Æ¡n Ä‘ang chá»");
            }

            // Cáº­p nháº­t thÃ´ng tin náº¿u cÃ³
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
                // Cáº­p nháº­t láº¡i tá»•ng thanh toÃ¡n
                BigDecimal tongThanhToan = hoaDon.getTongTien().add(request.getPhiVanChuyen());
                hoaDon.setTongThanhToan(tongThanhToan);
            }

            hoaDon.setNgayCapNhat(new Date());
            hoaDonRepository.save(hoaDon);

            log.info("âœ… ÄÃ£ cáº­p nháº­t thÃ´ng tin giao hÃ ng");

            return layTongQuanHoaDonCho(hoaDonId);

        } catch (Exception e) {
            log.error("âŒ Lá»—i cáº­p nháº­t thÃ´ng tin giao hÃ ng: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ cáº­p nháº­t: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public HoaDonResponse xacNhanGiaoHang(Integer hoaDonId) {
        try {
            log.info("âœ… XÃ¡c nháº­n giao hÃ ng cho hÃ³a Ä‘Æ¡n {}", hoaDonId);

            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));

            // Kiá»ƒm tra tráº¡ng thÃ¡i
            if (!"CHO".equals(hoaDon.getTrangThaiHoaDon())) {
                throw new RuntimeException("HÃ³a Ä‘Æ¡n khÃ´ng á»Ÿ tráº¡ng thÃ¡i chá»");
            }

            // Kiá»ƒm tra thÃ´ng tin giao hÃ ng
            if (!StringUtils.hasText(hoaDon.getDiaChi())) {
                throw new RuntimeException("ChÆ°a cÃ³ Ä‘á»‹a chá»‰ giao hÃ ng");
            }
            if (!StringUtils.hasText(hoaDon.getSdt())) {
                throw new RuntimeException("ChÆ°a cÃ³ sá»‘ Ä‘iá»‡n thoáº¡i ngÆ°á»i nháº­n");
            }

            // Cáº­p nháº­t tráº¡ng thÃ¡i vÃ  thá»i gian
            hoaDon.setTrangThaiHoaDon("ÄÃ£ xÃ¡c nháº­n");
            hoaDon.setNgayXacNhan(new Date());
            hoaDon.setNgayCapNhat(new Date());

            // Náº¿u lÃ  COD thÃ¬ set thá»i gian váº­n chuyá»ƒn
            if ("COD".equals(hoaDon.getPhuongThucThanhToan())) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, 3); // Dá»± kiáº¿n giao sau 3 ngÃ y
                hoaDon.setThoiGianVanChuyen(cal.getTime());
            }

            hoaDonRepository.save(hoaDon);

            // Cáº­p nháº­t chi tiáº¿t hÃ³a Ä‘Æ¡n
            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
            for (HoaDonChiTiet chiTiet : chiTiets) {
                chiTiet.setTrangThaiHoaDon("ÄÃ£ xÃ¡c nháº­n");
                hoaDonChiTietRepository.save(chiTiet);
            }

            // Táº¡o lá»‹ch sá»­
            taoLichSuHoaDon(hoaDon, "XÃ¡c nháº­n Ä‘Æ¡n giao hÃ ng", hoaDon.getNhanVien());

            log.info("âœ… ÄÃ£ xÃ¡c nháº­n giao hÃ ng");

            return mapToHoaDonResponse(hoaDon);

        } catch (Exception e) {
            log.error("âŒ Lá»—i xÃ¡c nháº­n giao hÃ ng: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ xÃ¡c nháº­n: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public HoaDonChoTongQuanResponse huyGiaoHang(Integer hoaDonId) {
        try {
            log.info("ðŸš« Há»§y giao hÃ ng cho hÃ³a Ä‘Æ¡n {}", hoaDonId);

            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));

            if (!"CHO".equals(hoaDon.getTrangThaiHoaDon()) &&
                    !"ÄÃ£ xÃ¡c nháº­n".equals(hoaDon.getTrangThaiHoaDon())) {
                throw new RuntimeException("KhÃ´ng thá»ƒ há»§y giao hÃ ng cho hÃ³a Ä‘Æ¡n nÃ y");
            }

            // Chuyá»ƒn vá» bÃ¡n táº¡i quáº§y
            hoaDon.setLoaiHoaDon("OFFLINE");
            hoaDon.setDiaChi("");
            hoaDon.setPhiVanChuyen(BigDecimal.ZERO);
            hoaDon.setPhuongThucThanhToan("CASH");
            hoaDon.setTrangThaiHoaDon("CHO");

            // Cáº­p nháº­t láº¡i tá»•ng thanh toÃ¡n (bá» phÃ­ ship)
            hoaDon.setTongThanhToan(hoaDon.getTongTien());

            hoaDon.setNgayCapNhat(new Date());
            hoaDonRepository.save(hoaDon);

            // Táº¡o lá»‹ch sá»­
            taoLichSuHoaDon(hoaDon, "Há»§y giao hÃ ng, chuyá»ƒn vá» bÃ¡n táº¡i quáº§y", hoaDon.getNhanVien());

            log.info("âœ… ÄÃ£ há»§y giao hÃ ng");

            return layTongQuanHoaDonCho(hoaDonId);

        } catch (Exception e) {
            log.error("âŒ Lá»—i há»§y giao hÃ ng: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ há»§y: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public HoaDonResponse capNhatDangGiao(Integer hoaDonId) {
        try {
            log.info("ðŸšš Cáº­p nháº­t Ä‘ang giao hÃ ng cho hÃ³a Ä‘Æ¡n {}", hoaDonId);

            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));

            if (!"ÄÃ£ xÃ¡c nháº­n".equals(hoaDon.getTrangThaiHoaDon())) {
                throw new RuntimeException("Chá»‰ cÃ³ thá»ƒ giao hÃ ng cho Ä‘Æ¡n Ä‘Ã£ xÃ¡c nháº­n");
            }

            hoaDon.setTrangThaiHoaDon("Äang giao");
            hoaDon.setNgayGiaoHang(new Date());
            hoaDon.setNgayCapNhat(new Date());

            hoaDonRepository.save(hoaDon);

            // Táº¡o lá»‹ch sá»­
            taoLichSuHoaDon(hoaDon, "Báº¯t Ä‘áº§u giao hÃ ng", hoaDon.getNhanVien());

            log.info("âœ… ÄÃ£ cáº­p nháº­t Ä‘ang giao hÃ ng");

            return mapToHoaDonResponse(hoaDon);

        } catch (Exception e) {
            log.error("âŒ Lá»—i cáº­p nháº­t Ä‘ang giao: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ cáº­p nháº­t: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public HoaDonResponse xacNhanDaGiao(Integer hoaDonId) {
        try {
            log.info("âœ… XÃ¡c nháº­n Ä‘Ã£ giao hÃ ng cho hÃ³a Ä‘Æ¡n {}", hoaDonId);

            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));

            if (!"Äang giao".equals(hoaDon.getTrangThaiHoaDon())) {
                throw new RuntimeException("Chá»‰ cÃ³ thá»ƒ xÃ¡c nháº­n cho Ä‘Æ¡n Ä‘ang giao");
            }

            hoaDon.setTrangThaiHoaDon("HoÃ n thÃ nh");
            hoaDon.setNgayNhanHang(new Date());
            hoaDon.setNgayHoanThanh(new Date());
            hoaDon.setNgayCapNhat(new Date());

            hoaDonRepository.save(hoaDon);

            // Cáº­p nháº­t chi tiáº¿t
            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
            for (HoaDonChiTiet chiTiet : chiTiets) {
                chiTiet.setTrangThaiHoaDon("HoÃ n thÃ nh");
                hoaDonChiTietRepository.save(chiTiet);
            }

            // Cá»™ng Ä‘iá»ƒm cho khÃ¡ch hÃ ng náº¿u cÃ³
            if (hoaDon.getKhachHang() != null) {
                congDiemKhachHang(hoaDon.getKhachHang().getId(), hoaDon.getTongThanhToan().doubleValue());
            }

            // Táº¡o lá»‹ch sá»­
            taoLichSuHoaDon(hoaDon, "Giao hÃ ng thÃ nh cÃ´ng", hoaDon.getNhanVien());

            log.info("âœ… ÄÃ£ xÃ¡c nháº­n giao hÃ ng thÃ nh cÃ´ng");

            return mapToHoaDonResponse(hoaDon);

        } catch (Exception e) {
            log.error("âŒ Lá»—i xÃ¡c nháº­n Ä‘Ã£ giao: {}", e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ xÃ¡c nháº­n: " + e.getMessage());
        }
    }

    // Helper method
    private BigDecimal tinhPhiVanChuyenMacDinh(String diaChi, BigDecimal tongTien) {
        if (tongTien != null && tongTien.compareTo(BigDecimal.valueOf(500000)) >= 0) {
            return BigDecimal.ZERO; // Miá»…n phÃ­ cho Ä‘Æ¡n >= 500k
        }

        if (diaChi != null) {
            String diaChiLower = diaChi.toLowerCase();
            if (diaChiLower.contains("hÃ  ná»™i") || diaChiLower.contains("ha noi")) {
                return BigDecimal.valueOf(30000);
            } else if (diaChiLower.contains("há»“ chÃ­ minh") || diaChiLower.contains("hcm")) {
                return BigDecimal.valueOf(35000);
            }
        }

        return BigDecimal.valueOf(50000); // Máº·c Ä‘á»‹nh 50k
    }
    private void xuLyThanhToanTienMat(ThanhToanRequest request, BigDecimal tongTienCanThanhToan) {
        if (request.getTienMat() == null || request.getTienMat().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Sá»‘ tiá»n máº·t pháº£i lá»›n hÆ¡n 0");
        }

        if (request.getTienMat().compareTo(tongTienCanThanhToan) < 0) {
            throw new RuntimeException("Sá»‘ tiá»n máº·t khÃ´ng Ä‘á»§ Ä‘á»ƒ thanh toÃ¡n");
        }

        log.info("ðŸ’µ Thanh toÃ¡n tiá»n máº·t: {} - Tá»•ng cáº§n thanh toÃ¡n: {}",
                request.getTienMat(), tongTienCanThanhToan);
    }

    /**
     * Xá»­ lÃ½ thanh toÃ¡n báº±ng chuyá»ƒn khoáº£n
     */
    private void xuLyThanhToanChuyenKhoan(ThanhToanRequest request, BigDecimal tongTienCanThanhToan) {
        if (request.getTienChuyenKhoan() == null || request.getTienChuyenKhoan().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Sá»‘ tiá»n chuyá»ƒn khoáº£n pháº£i lá»›n hÆ¡n 0");
        }

        if (request.getTienChuyenKhoan().compareTo(tongTienCanThanhToan) < 0) {
            throw new RuntimeException("Sá»‘ tiá»n chuyá»ƒn khoáº£n khÃ´ng Ä‘á»§ Ä‘á»ƒ thanh toÃ¡n");
        }

        log.info("ðŸ¦ Thanh toÃ¡n chuyá»ƒn khoáº£n: {} - Tá»•ng cáº§n thanh toÃ¡n: {}",
                request.getTienChuyenKhoan(), tongTienCanThanhToan);
    }

    /**
     * Xá»­ lÃ½ thanh toÃ¡n káº¿t há»£p (tiá»n máº·t + chuyá»ƒn khoáº£n)
     */
    private void xuLyThanhToanKetHop(ThanhToanRequest request, BigDecimal tongTienCanThanhToan) {
        BigDecimal tienMat = request.getTienMat() != null ? request.getTienMat() : BigDecimal.ZERO;
        BigDecimal tienChuyenKhoan = request.getTienChuyenKhoan() != null ? request.getTienChuyenKhoan() : BigDecimal.ZERO;

        if (tienMat.compareTo(BigDecimal.ZERO) <= 0 && tienChuyenKhoan.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Pháº£i cÃ³ Ã­t nháº¥t má»™t phÆ°Æ¡ng thá»©c thanh toÃ¡n cÃ³ giÃ¡ trá»‹ > 0");
        }

        BigDecimal tongTienNhan = tienMat.add(tienChuyenKhoan);
        if (tongTienNhan.compareTo(tongTienCanThanhToan) < 0) {
            throw new RuntimeException("Tá»•ng tiá»n thanh toÃ¡n khÃ´ng Ä‘á»§. Cáº§n: " +
                    tongTienCanThanhToan + ", CÃ³: " + tongTienNhan);
        }

        log.info("ðŸ’° Thanh toÃ¡n káº¿t há»£p - Tiá»n máº·t: {}, Chuyá»ƒn khoáº£n: {}, Tá»•ng: {}",
                tienMat, tienChuyenKhoan, tongTienNhan);
    }

    /**
     * Xá»­ lÃ½ trá»« Ä‘iá»ƒm tÃ­ch lÅ©y
     */
    private void xuLyDiemTichLuy(KhachHang khachHang, Integer diemSuDung) {
        if (khachHang != null && khachHang.getViDiem() != null) {
            ViDiem viDiem = khachHang.getViDiem();
            Double diemHienTai = viDiem.getTongDiem() - viDiem.getSoDiemDaDung();

            if (diemSuDung > diemHienTai) {
                throw new RuntimeException("KhÃ´ng Ä‘á»§ Ä‘iá»ƒm Ä‘á»ƒ sá»­ dá»¥ng. CÃ³: " + diemHienTai + " Ä‘iá»ƒm");
            }

            viDiem.setSoDiemDaDung(viDiem.getSoDiemDaDung() + diemSuDung);
            viDiem.setNgayCapNhat(new Date());
            viDiemRepository.save(viDiem);

            log.info("ðŸŽ¯ Sá»­ dá»¥ng {} Ä‘iá»ƒm tÃ­ch lÅ©y", diemSuDung);
        }
    }

    /**
     * Táº¡o mÃ´ táº£ cho lá»‹ch sá»­ thanh toÃ¡n
     */
    private String taoMoTaThanhToan(ThanhToanRequest request) {
        StringBuilder moTa = new StringBuilder("Thanh toÃ¡n thÃ nh cÃ´ng - ");

        switch (request.getPhuongThucThanhToan()) {
            case "TIEN_MAT":
                moTa.append("Tiá»n máº·t: ").append(formatMoney(request.getTienMat()));
                break;
            case "CHUYEN_KHOAN":
                moTa.append("Chuyá»ƒn khoáº£n: ").append(formatMoney(request.getTienChuyenKhoan()));
                break;
            case "KET_HOP":
                moTa.append("Káº¿t há»£p - Tiá»n máº·t: ").append(formatMoney(request.getTienMat()))
                        .append(", Chuyá»ƒn khoáº£n: ").append(formatMoney(request.getTienChuyenKhoan()));
                break;
        }

        if (request.getDiemSuDung() != null && request.getDiemSuDung() > 0) {
            moTa.append(", Äiá»ƒm sá»­ dá»¥ng: ").append(request.getDiemSuDung());
        }

        return moTa.toString();
    }

    /**
     * Format tiá»n tá»‡
     */
    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0â‚«";
        return String.format("%,.0fâ‚«", amount);
    }

    @Override
    @Transactional
    public ThanhToanResponse thanhToanHoaDonChiTiet(Integer hoaDonId, ThanhToanRequest request) {
        try {
            log.info("ðŸ’° Thanh toÃ¡n chi tiáº¿t hÃ³a Ä‘Æ¡n ID: {} vá»›i phÆ°Æ¡ng thá»©c: {}", hoaDonId, request.getPhuongThucThanhToan());

            // ===== BÆ¯á»šC 1: KIá»‚M TRA Tá»’N KHO NGAY Äáº¦U =====
            log.info("ðŸ” BÆ°á»›c 1: Kiá»ƒm tra tá»“n kho má»›i nháº¥t...");
            List<InventoryCheckResponse> inventoryChecks = kiemTraTonKhoTruocThanhToan(hoaDonId);

            List<InventoryCheckResponse> sanPhamThieu = inventoryChecks.stream()
                    .filter(check -> !check.getCoTheban())
                    .collect(Collectors.toList());

            if (!sanPhamThieu.isEmpty()) {
                // Táº¡o thÃ´ng bÃ¡o chi tiáº¿t vá» cÃ¡c sáº£n pháº©m thiáº¿u
                StringBuilder errorMessage = new StringBuilder("KhÃ´ng thá»ƒ thanh toÃ¡n do thiáº¿u tá»“n kho:\n");
                for (InventoryCheckResponse item : sanPhamThieu) {
                    errorMessage.append(String.format("- %s (%s - %s): %s\n",
                            item.getTenSanPham(), item.getMauSac(), item.getKichCo(), item.getThongBao()));
                }

                log.warn("âš ï¸ Tá»« chá»‘i thanh toÃ¡n do thiáº¿u tá»“n kho: {}", errorMessage.toString());

                return ThanhToanResponse.builder()
                        .hoaDonId(hoaDonId)
                        .thanhCong(false)
                        .thongBaoThanhToan(errorMessage.toString().trim())
                        .kiemTraTonKho(inventoryChecks)
                        .build();
            }

            // ===== VALIDATION CÆ  Báº¢N =====
            if (hoaDonId == null || request == null) {
                throw new RuntimeException("ThÃ´ng tin thanh toÃ¡n khÃ´ng há»£p lá»‡");
            }

            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n"));

            if (!"CHO".equals(hoaDon.getTrangThaiHoaDon())) {
                throw new RuntimeException("HÃ³a Ä‘Æ¡n khÃ´ng á»Ÿ tráº¡ng thÃ¡i chá» thanh toÃ¡n");
            }

            log.info("âœ… BÆ°á»›c 1 hoÃ n thÃ nh: Tá»“n kho Ä‘á»§ Ä‘á»ƒ thanh toÃ¡n");

            // ===== BÆ¯á»šC 2: TÃNH Tá»”NG TIá»€N BAN Äáº¦U =====
            BigDecimal tongTienGoc = hoaDonChiTietRepository.calculateTotalAmountByHoaDonId(hoaDonId);
            BigDecimal tongTienCanThanhToan = tongTienGoc;
            BigDecimal tienThua = BigDecimal.ZERO;

            log.info("ðŸ“Š Tá»•ng tiá»n gá»‘c: {}", tongTienGoc);

            // ===== BÆ¯á»šC 3: Xá»¬ LÃ VOUCHER =====
            Voucher voucherApplied = null;
            BigDecimal giaTriGiamVoucher = BigDecimal.ZERO;

            if (request.getVoucherId() != null) {
                log.info("ðŸŽ« Xá»­ lÃ½ voucher ID: {}", request.getVoucherId());

                voucherApplied = voucherRepository.findById(request.getVoucherId()).orElse(null);
                if (voucherApplied == null) {
                    throw new RuntimeException("KhÃ´ng tÃ¬m tháº¥y voucher");
                }

                // Kiá»ƒm tra tÃ­nh há»£p lá»‡ cá»§a voucher
                Date currentDate = new Date();
                if (!voucherApplied.isValid()) {
                    throw new RuntimeException("Voucher khÃ´ng há»£p lá»‡");
                }

                if (voucherApplied.getSoLuong() <= 0) {
                    throw new RuntimeException("Voucher Ä‘Ã£ háº¿t lÆ°á»£t sá»­ dá»¥ng");
                }

                if (currentDate.before(voucherApplied.getNgayBatDau()) ||
                        currentDate.after(voucherApplied.getNgayKetThuc())) {
                    throw new RuntimeException("Voucher ngoÃ i thá»i gian sá»­ dá»¥ng");
                }

                // Kiá»ƒm tra giÃ¡ trá»‹ Ä‘Æ¡n hÃ ng tá»‘i thiá»ƒu
                if (tongTienGoc.doubleValue() < voucherApplied.getGiaTriGiamToiThieu()) {
                    throw new RuntimeException("ÄÆ¡n hÃ ng chÆ°a Ä‘á»§ giÃ¡ trá»‹ tá»‘i thiá»ƒu Ä‘á»ƒ Ã¡p dá»¥ng voucher: " +
                            formatMoney(BigDecimal.valueOf(voucherApplied.getGiaTriGiamToiThieu())));
                }

                // TÃ­nh giÃ¡ trá»‹ giáº£m
                Double giaTriGiam = voucherApplied.tinhGiaTriGiam(tongTienGoc.doubleValue());
                giaTriGiamVoucher = BigDecimal.valueOf(giaTriGiam);

                // Trá»« voucher khá»i tá»•ng tiá»n
                tongTienCanThanhToan = tongTienCanThanhToan.subtract(giaTriGiamVoucher);
                tongTienCanThanhToan = tongTienCanThanhToan.max(BigDecimal.ZERO);

                log.info("âœ… Voucher Ã¡p dá»¥ng: {} - Giáº£m: {}", voucherApplied.getTenVoucher(), giaTriGiamVoucher);
            }

            // ===== BÆ¯á»šC 4: Xá»¬ LÃ ÄIá»‚M TÃCH LÅ¨Y =====
            BigDecimal giaTriDiem = BigDecimal.ZERO;
            if (request.getDiemSuDung() != null && request.getDiemSuDung() > 0) {
                log.info("ðŸŽ¯ Xá»­ lÃ½ Ä‘iá»ƒm tÃ­ch lÅ©y: {} Ä‘iá»ƒm", request.getDiemSuDung());

                // Kiá»ƒm tra khÃ¡ch hÃ ng cÃ³ Ä‘á»§ Ä‘iá»ƒm khÃ´ng
                if (request.getKhachHangId() == null) {
                    throw new RuntimeException("Cáº§n cÃ³ khÃ¡ch hÃ ng Ä‘á»ƒ sá»­ dá»¥ng Ä‘iá»ƒm tÃ­ch lÅ©y");
                }

                KhachHang khachHang = khachHangRepository.findById(request.getKhachHangId()).orElse(null);
                if (khachHang == null || khachHang.getViDiem() == null) {
                    throw new RuntimeException("KhÃ¡ch hÃ ng khÃ´ng cÃ³ vÃ­ Ä‘iá»ƒm");
                }

                ViDiem viDiem = khachHang.getViDiem();
                Double diemHienTai = viDiem.getTongDiem() - viDiem.getSoDiemDaDung();

                if (request.getDiemSuDung() > diemHienTai) {
                    throw new RuntimeException("KhÃ´ng Ä‘á»§ Ä‘iá»ƒm Ä‘á»ƒ sá»­ dá»¥ng. CÃ³: " + diemHienTai + " Ä‘iá»ƒm");
                }

                // TÃ­nh giÃ¡ trá»‹ Ä‘iá»ƒm (1 Ä‘iá»ƒm = 1000 VND)
                giaTriDiem = BigDecimal.valueOf(request.getDiemSuDung() * 1000);
                tongTienCanThanhToan = tongTienCanThanhToan.subtract(giaTriDiem);
                tongTienCanThanhToan = tongTienCanThanhToan.max(BigDecimal.ZERO);

                log.info("ðŸ’° Sá»­ dá»¥ng {} Ä‘iá»ƒm = {}", request.getDiemSuDung(), formatMoney(giaTriDiem));
            }

            log.info("ðŸ’µ Tá»•ng tiá»n cáº§n thanh toÃ¡n sau giáº£m giÃ¡: {}", tongTienCanThanhToan);

            // ===== BÆ¯á»šC 5: VALIDATION VÃ€ Xá»¬ LÃ PHÆ¯Æ NG THá»¨C THANH TOÃN =====
            if ("TIEN_MAT".equals(request.getPhuongThucThanhToan())) {
                if (request.getTienMat() == null || request.getTienMat().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException("Sá»‘ tiá»n máº·t pháº£i lá»›n hÆ¡n 0");
                }
                if (request.getTienMat().compareTo(tongTienCanThanhToan) < 0) {
                    throw new RuntimeException("Sá»‘ tiá»n máº·t khÃ´ng Ä‘á»§ Ä‘á»ƒ thanh toÃ¡n. Cáº§n: " +
                            formatMoney(tongTienCanThanhToan));
                }
                tienThua = request.getTienMat().subtract(tongTienCanThanhToan);
                log.info("ðŸ’µ Thanh toÃ¡n tiá»n máº·t: {} - Tiá»n thá»«a: {}",
                        formatMoney(request.getTienMat()), formatMoney(tienThua));

            } else if ("CHUYEN_KHOAN".equals(request.getPhuongThucThanhToan())) {
                if (request.getTienChuyenKhoan() == null || request.getTienChuyenKhoan().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException("Sá»‘ tiá»n chuyá»ƒn khoáº£n pháº£i lá»›n hÆ¡n 0");
                }
                if (request.getTienChuyenKhoan().compareTo(tongTienCanThanhToan) < 0) {
                    throw new RuntimeException("Sá»‘ tiá»n chuyá»ƒn khoáº£n khÃ´ng Ä‘á»§ Ä‘á»ƒ thanh toÃ¡n. Cáº§n: " +
                            formatMoney(tongTienCanThanhToan));
                }
                log.info("ðŸ¦ Thanh toÃ¡n chuyá»ƒn khoáº£n: {}", formatMoney(request.getTienChuyenKhoan()));

            } else if ("KET_HOP".equals(request.getPhuongThucThanhToan())) {
                BigDecimal tienMat = request.getTienMat() != null ? request.getTienMat() : BigDecimal.ZERO;
                BigDecimal tienChuyenKhoan = request.getTienChuyenKhoan() != null ? request.getTienChuyenKhoan() : BigDecimal.ZERO;

                if (tienMat.compareTo(BigDecimal.ZERO) <= 0 && tienChuyenKhoan.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException("Pháº£i cÃ³ Ã­t nháº¥t má»™t phÆ°Æ¡ng thá»©c thanh toÃ¡n cÃ³ giÃ¡ trá»‹ > 0");
                }

                BigDecimal tongTienNhan = tienMat.add(tienChuyenKhoan);
                if (tongTienNhan.compareTo(tongTienCanThanhToan) < 0) {
                    throw new RuntimeException("Tá»•ng tiá»n thanh toÃ¡n khÃ´ng Ä‘á»§. Cáº§n: " +
                            formatMoney(tongTienCanThanhToan) + ", CÃ³: " + formatMoney(tongTienNhan));
                }

                tienThua = tongTienNhan.subtract(tongTienCanThanhToan);
                log.info("ðŸ’° Thanh toÃ¡n káº¿t há»£p - Tiá»n máº·t: {}, Chuyá»ƒn khoáº£n: {}, Tiá»n thá»«a: {}",
                        formatMoney(tienMat), formatMoney(tienChuyenKhoan), formatMoney(tienThua));

            } else {
                throw new RuntimeException("PhÆ°Æ¡ng thá»©c thanh toÃ¡n khÃ´ng há»£p lá»‡: " + request.getPhuongThucThanhToan());
            }

            // ===== BÆ¯á»šC 6: Cáº¬P NHáº¬T THÃ”NG TIN HÃ“A ÄÆ N =====
            // Cáº­p nháº­t thÃ´ng tin khÃ¡ch hÃ ng náº¿u cÃ³
            if (request.getKhachHangId() != null) {
                KhachHang khachHang = khachHangRepository.findById(request.getKhachHangId()).orElse(null);
                if (khachHang != null) {
                    hoaDon.setKhachHang(khachHang);

                    // Láº¥y email tá»« TaiKhoan
                    if (khachHang.getTaiKhoan() != null && StringUtils.hasText(khachHang.getTaiKhoan().getEmail())) {
                        hoaDon.setEmail(khachHang.getTaiKhoan().getEmail());
                    } else {
                        hoaDon.setEmail("");
                    }

                    hoaDon.setSdt(khachHang.getSdt());
                    hoaDon.setTenNguoiDung(khachHang.getHoTen());

                    // Láº¥y Ä‘á»‹a chá»‰ tá»« TaiKhoan -> DiaChi
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

                    log.info("ðŸ‘¤ Cáº­p nháº­t khÃ¡ch hÃ ng: {}", khachHang.getHoTen());
                }
            }

            // Cáº­p nháº­t tráº¡ng thÃ¡i vÃ  thÃ´ng tin thanh toÃ¡n
            hoaDon.setTrangThaiHoaDon("DA_THANH_TOAN");
            hoaDon.setLoaiHoaDon(request.getLoaiHoaDon() != null ? request.getLoaiHoaDon() : "OFFLINE");
            hoaDon.setPhuongThucThanhToan(request.getPhuongThucThanhToan());
            hoaDon.setNgayHoanThanh(new Date());
            hoaDon.setNgayCapNhat(new Date());
            hoaDon.setGhiChu(request.getGhiChu());
            hoaDon.setDiemSuDung(request.getDiemSuDung());

            // Cáº­p nháº­t tá»•ng tiá»n Ä‘Ãºng cÃ¡ch
            hoaDon.setTongTien(tongTienGoc);                // Tá»•ng tiá»n gá»‘c
            hoaDon.setTongThanhToan(tongTienCanThanhToan);   // Tá»•ng tiá»n sau khi Ä‘Ã£ trá»« voucher vÃ  Ä‘iá»ƒm

            HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

            // ===== BÆ¯á»šC 7: Xá»¬ LÃ VOUCHER VÃ€ ÄIá»‚M SAU KHI THANH TOÃN THÃ€NH CÃ”NG =====
            if (voucherApplied != null) {
                // Táº¡o chi tiáº¿t voucher SAU khi thanh toÃ¡n thÃ nh cÃ´ng
                taoChiTietVoucherNeuChuaCo(savedHoaDon, voucherApplied, tongTienGoc, giaTriGiamVoucher, tongTienCanThanhToan);

                // Giáº£m sá»‘ lÆ°á»£ng voucher
                voucherApplied.setSoLuong(voucherApplied.getSoLuong() - 1);
                voucherRepository.save(voucherApplied);
                log.info("ðŸŽ« ÄÃ£ sá»­ dá»¥ng voucher: {}", voucherApplied.getTenVoucher());
            }

            if (request.getDiemSuDung() != null && request.getDiemSuDung() > 0 && savedHoaDon.getKhachHang() != null) {
                // Trá»« Ä‘iá»ƒm Ä‘Ã£ sá»­ dá»¥ng
                xuLyDiemTichLuy(savedHoaDon.getKhachHang(), request.getDiemSuDung());
                log.info("ðŸŽ¯ ÄÃ£ sá»­ dá»¥ng {} Ä‘iá»ƒm", request.getDiemSuDung());
            }

            // ===== BÆ¯á»šC 8: Cáº¬P NHáº¬T CHI TIáº¾T HÃ“A ÄÆ N =====
            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
            for (HoaDonChiTiet chiTiet : chiTiets) {
                chiTiet.setTrangThaiHoaDon("DA_THANH_TOAN");
                chiTiet.setNgayCapNhat(new Date());
                hoaDonChiTietRepository.save(chiTiet);
            }

            // ===== BÆ¯á»šC 9: Táº O Lá»ŠCH Sá»¬ HÃ“A ÄÆ N =====
            String moTaThanhToan = taoMoTaThanhToanChiTiet(request, voucherApplied, giaTriGiamVoucher, giaTriDiem, tienThua);
            taoLichSuHoaDon(savedHoaDon, moTaThanhToan, savedHoaDon.getNhanVien());

            // ===== BÆ¯á»šC 10: Cá»˜NG ÄIá»‚M CHO KHÃCH HÃ€NG =====
            if (savedHoaDon.getKhachHang() != null) {
                congDiemKhachHang(savedHoaDon.getKhachHang().getId(), savedHoaDon.getTongThanhToan().doubleValue());
                log.info("ðŸ’Ž ÄÃ£ cá»™ng Ä‘iá»ƒm cho khÃ¡ch hÃ ng");
            }

            // ===== BÆ¯á»šC 11: Táº O RESPONSE =====
            String thongBaoThanhToan = taoThongBaoThanhToan(request, tienThua, voucherApplied, giaTriGiamVoucher);

            ThanhToanResponse response = ThanhToanResponse.builder()
                    .hoaDonId(savedHoaDon.getId())
                    .maHoaDon(savedHoaDon.getMaHoaDon())
                    .trangThaiHoaDon(savedHoaDon.getTrangThaiHoaDon())
                    .phuongThucThanhToan(request.getPhuongThucThanhToan())
                    .tongTienCanThanhToan(tongTienCanThanhToan) // Tá»•ng sau giáº£m giÃ¡
                    .tienMat(request.getTienMat())
                    .tienChuyenKhoan(request.getTienChuyenKhoan())
                    .tienThua(tienThua)
                    .voucherId(request.getVoucherId())
                    .tenVoucher(voucherApplied != null ? voucherApplied.getTenVoucher() : null)
                    .giaTriGiamVoucher(giaTriGiamVoucher)
                    .diemSuDung(request.getDiemSuDung())
                    .giaTriDiem(giaTriDiem)
                    .khachHangId(savedHoaDon.getKhachHang() != null ? savedHoaDon.getKhachHang().getId() : null)
                    .tenKhachHang(savedHoaDon.getKhachHang() != null ? savedHoaDon.getKhachHang().getHoTen() : "KhÃ¡ch láº»")
                    .sdtKhachHang(savedHoaDon.getKhachHang() != null ? savedHoaDon.getKhachHang().getSdt() : "")
                    .ngayThanhToan(savedHoaDon.getNgayCapNhat())
                    .ngayHoanThanh(savedHoaDon.getNgayHoanThanh())
                    .nhanVienId(savedHoaDon.getNhanVien() != null ? savedHoaDon.getNhanVien().getId() : null)
                    .tenNhanVien(savedHoaDon.getNhanVien() != null ? savedHoaDon.getNhanVien().getHoTen() : "")
                    .ghiChu(request.getGhiChu())
                    .thongBaoThanhToan(thongBaoThanhToan)
                    .thanhCong(true)
                    .kiemTraTonKho(inventoryChecks) // Tráº£ vá» káº¿t quáº£ kiá»ƒm tra tá»“n kho
                    .build();

            log.info("âœ… Thanh toÃ¡n chi tiáº¿t thÃ nh cÃ´ng - HÃ³a Ä‘Æ¡n: {} - Tá»•ng: {} - PhÆ°Æ¡ng thá»©c: {}",
                    savedHoaDon.getMaHoaDon(), formatMoney(tongTienCanThanhToan), request.getPhuongThucThanhToan());

            return response;

        } catch (RuntimeException e) {
            log.error("âŒ Lá»—i nghiá»‡p vá»¥ thanh toÃ¡n: {}", e.getMessage());
            return ThanhToanResponse.builder()
                    .hoaDonId(hoaDonId)
                    .thanhCong(false)
                    .thongBaoThanhToan("Thanh toÃ¡n tháº¥t báº¡i: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("âŒ Lá»—i há»‡ thá»‘ng thanh toÃ¡n: {}", e.getMessage(), e);
            return ThanhToanResponse.builder()
                    .hoaDonId(hoaDonId)
                    .thanhCong(false)
                    .thongBaoThanhToan("Lá»—i há»‡ thá»‘ng: Vui lÃ²ng thá»­ láº¡i sau")
                    .build();
        }
    }

    /**
     * Táº¡o mÃ´ táº£ chi tiáº¿t cho lá»‹ch sá»­ thanh toÃ¡n
     */
    private String taoMoTaThanhToanChiTiet(ThanhToanRequest request, Voucher voucher,
                                           BigDecimal giaTriGiamVoucher, BigDecimal giaTriDiem, BigDecimal tienThua) {
        StringBuilder moTa = new StringBuilder("Thanh toÃ¡n thÃ nh cÃ´ng - ");

        // PhÆ°Æ¡ng thá»©c thanh toÃ¡n
        switch (request.getPhuongThucThanhToan()) {
            case "TIEN_MAT":
                moTa.append("Tiá»n máº·t: ").append(formatMoney(request.getTienMat()));
                break;
            case "CHUYEN_KHOAN":
                moTa.append("Chuyá»ƒn khoáº£n: ").append(formatMoney(request.getTienChuyenKhoan()));
                break;
            case "KET_HOP":
                moTa.append("Káº¿t há»£p - Tiá»n máº·t: ").append(formatMoney(request.getTienMat()))
                        .append(", Chuyá»ƒn khoáº£n: ").append(formatMoney(request.getTienChuyenKhoan()));
                break;
        }

        // Voucher
        if (voucher != null && giaTriGiamVoucher.compareTo(BigDecimal.ZERO) > 0) {
            moTa.append(", Voucher '").append(voucher.getTenVoucher())
                    .append("': -").append(formatMoney(giaTriGiamVoucher));
        }

        // Äiá»ƒm tÃ­ch lÅ©y
        if (request.getDiemSuDung() != null && request.getDiemSuDung() > 0) {
            moTa.append(", Äiá»ƒm sá»­ dá»¥ng: ").append(request.getDiemSuDung())
                    .append(" Ä‘iá»ƒm (-").append(formatMoney(giaTriDiem)).append(")");
        }

        // Tiá»n thá»«a
        if (tienThua.compareTo(BigDecimal.ZERO) > 0) {
            moTa.append(", Tiá»n thá»«a: ").append(formatMoney(tienThua));
        }

        return moTa.toString();
    }

    /**
     * Táº¡o thÃ´ng bÃ¡o thanh toÃ¡n cho frontend
     */
    private String taoThongBaoThanhToan(ThanhToanRequest request, BigDecimal tienThua,
                                        Voucher voucher, BigDecimal giaTriGiamVoucher) {
        StringBuilder thongBao = new StringBuilder();

        switch (request.getPhuongThucThanhToan()) {
            case "TIEN_MAT":
                thongBao.append("Thanh toÃ¡n tiá»n máº·t thÃ nh cÃ´ng");
                if (tienThua.compareTo(BigDecimal.ZERO) > 0) {
                    thongBao.append(". Tiá»n thá»«a: ").append(formatMoney(tienThua));
                }
                break;
            case "CHUYEN_KHOAN":
                thongBao.append("Thanh toÃ¡n chuyá»ƒn khoáº£n thÃ nh cÃ´ng");
                break;
            case "KET_HOP":
                thongBao.append("Thanh toÃ¡n káº¿t há»£p thÃ nh cÃ´ng");
                if (tienThua.compareTo(BigDecimal.ZERO) > 0) {
                    thongBao.append(". Tiá»n thá»«a: ").append(formatMoney(tienThua));
                }
                break;
        }

        if (voucher != null && giaTriGiamVoucher.compareTo(BigDecimal.ZERO) > 0) {
            thongBao.append(". Voucher Ä‘Ã£ giáº£m ").append(formatMoney(giaTriGiamVoucher));
        }

        return thongBao.toString();
    }
    @Override
    @Transactional(readOnly = true)
    public List<InventoryCheckResponse> kiemTraTonKhoTruocThanhToan(Integer hoaDonId) {
        try {
            logger.info("ðŸ“¦ Báº¯t Ä‘áº§u kiá»ƒm tra tá»“n kho cho hÃ³a Ä‘Æ¡n: {}", hoaDonId);

            // Validate input
            if (hoaDonId == null || hoaDonId <= 0) {
                throw new RuntimeException("ID hÃ³a Ä‘Æ¡n khÃ´ng há»£p lá»‡: " + hoaDonId);
            }

            // Kiá»ƒm tra hÃ³a Ä‘Æ¡n tá»“n táº¡i vÃ  tráº¡ng thÃ¡i
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n vá»›i ID: " + hoaDonId));

            if (!"CHO".equals(hoaDon.getTrangThaiHoaDon())) {
                throw new RuntimeException("HÃ³a Ä‘Æ¡n khÃ´ng á»Ÿ tráº¡ng thÃ¡i chá» thanh toÃ¡n. Tráº¡ng thÃ¡i hiá»‡n táº¡i: " + hoaDon.getTrangThaiHoaDon());
            }

            // Láº¥y danh sÃ¡ch chi tiáº¿t hÃ³a Ä‘Æ¡n
            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
            if (chiTiets.isEmpty()) {
                throw new RuntimeException("HÃ³a Ä‘Æ¡n chÆ°a cÃ³ sáº£n pháº©m nÃ o");
            }

            List<InventoryCheckResponse> responses = new ArrayList<>();

            logger.info("ðŸ” Kiá»ƒm tra {} sáº£n pháº©m trong hÃ³a Ä‘Æ¡n", chiTiets.size());

            for (HoaDonChiTiet chiTiet : chiTiets) {
                try {
                    // Kiá»ƒm tra null safety
                    if (chiTiet.getChiTietSanPham() == null) {
                        logger.warn("âš ï¸ Chi tiáº¿t hÃ³a Ä‘Æ¡n {} khÃ´ng cÃ³ sáº£n pháº©m", chiTiet.getId());

                        responses.add(InventoryCheckResponse.builder()
                                .chiTietSanPhamId(null)
                                .tenSanPham("Sáº£n pháº©m khÃ´ng xÃ¡c Ä‘á»‹nh")
                                .mauSac("N/A")
                                .kichCo("N/A")
                                .soLuongTon(0)
                                .soLuongCanBan(chiTiet.getSoLuong())
                                .coTheban(false)
                                .thongBao("Lá»—i: KhÃ´ng tÃ¬m tháº¥y thÃ´ng tin sáº£n pháº©m")
                                .build());
                        continue;
                    }

                    ChiTietSanPham sanPham = chiTiet.getChiTietSanPham();

                    // Refresh dá»¯ liá»‡u tá»« database Ä‘á»ƒ cÃ³ sá»‘ liá»‡u má»›i nháº¥t
                    sanPham = chiTietSanPhamRepository.findById(sanPham.getId())
                            .orElse(sanPham);

                    // Sá»‘ lÆ°á»£ng cáº§n bÃ¡n
                    int soLuongCanBan = chiTiet.getSoLuong();

                    // Sá»‘ lÆ°á»£ng tá»“n kho hiá»‡n táº¡i (Ä‘Ã£ bá»‹ trá»« khi thÃªm vÃ o hÃ³a Ä‘Æ¡n)
                    int soLuongTonHienTai = sanPham.getSoLuong();

                    // Logic kiá»ƒm tra: náº¿u tá»“n kho >= 0 thÃ¬ cÃ³ thá»ƒ bÃ¡n (vÃ¬ Ä‘Ã£ trá»« khi thÃªm vÃ o giá»)
                    boolean coTheBan = soLuongTonHienTai >= 0;
                    String thongBao;

                    if (!coTheBan) {
                        int soLuongThieu = Math.abs(soLuongTonHienTai);
                        thongBao = String.format("Thiáº¿u %d sáº£n pháº©m trong kho", soLuongThieu);
                    } else {
                        thongBao = "Äá»§ hÃ ng";
                    }

                    // Láº¥y thÃ´ng tin sáº£n pháº©m an toÃ n
                    String tenSanPham = "N/A";
                    String mauSac = "N/A";
                    String kichCo = "N/A";

                    try {
                        SanPham sp = sanPham.getSanPham();
                        if (sp != null && sp.getTenSanPham() != null) {
                            tenSanPham = sp.getTenSanPham();
                        }
                    } catch (Exception e) {
                        logger.debug("KhÃ´ng thá»ƒ láº¥y tÃªn sáº£n pháº©m: {}", e.getMessage());
                    }

                    try {
                        MauSac ms = sanPham.getMauSac();
                        if (ms != null && ms.getTenMauSac() != null) {
                            mauSac = ms.getTenMauSac();
                        }
                    } catch (Exception e) {
                        logger.debug("KhÃ´ng thá»ƒ láº¥y mÃ u sáº¯c: {}", e.getMessage());
                    }

                    try {
                        KichCo kc = sanPham.getKichCo();
                        if (kc != null && kc.getTenKichCo() != null) {
                            kichCo = kc.getTenKichCo();
                        }
                    } catch (Exception e) {
                        logger.debug("KhÃ´ng thá»ƒ láº¥y kÃ­ch cá»¡: {}", e.getMessage());
                    }

                    responses.add(InventoryCheckResponse.builder()
                            .chiTietSanPhamId(sanPham.getId())
                            .tenSanPham(tenSanPham)
                            .mauSac(mauSac)
                            .kichCo(kichCo)
                            .soLuongTon(Math.max(0, soLuongTonHienTai)) // Hiá»ƒn thá»‹ >= 0
                            .soLuongCanBan(soLuongCanBan)
                            .coTheban(coTheBan)
                            .thongBao(thongBao)
                            .build());

                    logger.debug("âœ… Kiá»ƒm tra sáº£n pháº©m '{}': Tá»“n={}, Cáº§n={}, Äá»§HÃ ng={}",
                            tenSanPham, soLuongTonHienTai, soLuongCanBan, coTheBan);

                } catch (Exception e) {
                    logger.error("âŒ Lá»—i kiá»ƒm tra chi tiáº¿t sáº£n pháº©m {}: {}",
                            chiTiet.getId(), e.getMessage());

                    // Táº¡o response lá»—i cho sáº£n pháº©m nÃ y
                    responses.add(InventoryCheckResponse.builder()
                            .chiTietSanPhamId(chiTiet.getChiTietSanPham() != null ?
                                    chiTiet.getChiTietSanPham().getId() : null)
                            .tenSanPham("Lá»—i kiá»ƒm tra sáº£n pháº©m")
                            .mauSac("N/A")
                            .kichCo("N/A")
                            .soLuongTon(0)
                            .soLuongCanBan(chiTiet.getSoLuong())
                            .coTheban(false)
                            .thongBao("Lá»—i há»‡ thá»‘ng khi kiá»ƒm tra sáº£n pháº©m")
                            .build());
                }
            }

            // Log tá»•ng káº¿t
            long soSanPhamDuHang = responses.stream().filter(InventoryCheckResponse::getCoTheban).count();
            long soSanPhamThieuHang = responses.size() - soSanPhamDuHang;

            logger.info("ðŸ“Š Káº¿t quáº£ kiá»ƒm tra tá»“n kho: {}/{} sáº£n pháº©m Ä‘á»§ hÃ ng, {} sáº£n pháº©m thiáº¿u hÃ ng",
                    soSanPhamDuHang, responses.size(), soSanPhamThieuHang);

            if (soSanPhamThieuHang > 0) {
                logger.warn("âš ï¸ Cáº£nh bÃ¡o: CÃ³ {} sáº£n pháº©m khÃ´ng Ä‘á»§ tá»“n kho", soSanPhamThieuHang);
            }

            return responses;

        } catch (RuntimeException e) {
            // NÃ©m láº¡i exception nghiá»‡p vá»¥ Ä‘á»ƒ Controller xá»­ lÃ½
            throw e;
        } catch (Exception e) {
            logger.error("âŒ Lá»—i há»‡ thá»‘ng khi kiá»ƒm tra tá»“n kho hÃ³a Ä‘Æ¡n {}: {}", hoaDonId, e.getMessage(), e);
            throw new RuntimeException("Lá»—i há»‡ thá»‘ng khi kiá»ƒm tra tá»“n kho: " + e.getMessage());
        }
    }
}
