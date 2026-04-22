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
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t tráº£ hÃ ng vá»›i ID: " + id));
        return convertToDTO(chiTiet);
    }

    @Override
    public List<ChiTietTraHangDTO> getChiTietTraHangByHoaDon(Integer hoaDonId) {
        try {
            return chiTietTraHangRepository.findByHoaDonIdWithFullInfo(hoaDonId).stream()
                    .map(this::convertToDTO)
                    .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao())) // âœ… Sá»¬A
                    .collect(Collectors.toList());
        } catch (Exception e) {
            try {
                return chiTietTraHangRepository.findByHoaDonIdWithFullInfoFallback(hoaDonId).stream()
                        .map(this::convertToDTO)
                        .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao())) // âœ… Sá»¬A
                        .collect(Collectors.toList());
            } catch (Exception e2) {
                List<HoaDonChiTiet> hoaDonChiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
                return hoaDonChiTiets.stream()
                        .flatMap(hdct -> chiTietTraHangRepository.findByChiTietSanPhamId(hdct.getChiTietSanPham().getId()).stream())
                        .map(this::convertToDTO)
                        .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao())) // âœ… Sá»¬A
                        .collect(Collectors.toList());
            }
        }
    }

    @Override
    public ChiTietTraHangDTO createChiTietTraHang(ChiTietTraHangDTO dto) {
        // Validate input cÆ¡ báº£n
        if (dto.getChiTietSanPhamId() == null) {
            throw new RuntimeException("ID chi tiáº¿t sáº£n pháº©m khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }

        if (dto.getSoLuong() == null || dto.getSoLuong() <= 0) {
            throw new RuntimeException("Sá»‘ lÆ°á»£ng pháº£i lá»›n hÆ¡n 0");
        }

        if (dto.getLyDo() == null || dto.getLyDo().trim().isEmpty()) {
            throw new RuntimeException("LÃ½ do tráº£ hÃ ng khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }

        // Kiá»ƒm tra chi tiáº¿t sáº£n pháº©m tá»“n táº¡i
        ChiTietSanPham ctsp = chiTietSanPhamRepository.findById(dto.getChiTietSanPhamId())
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t sáº£n pháº©m vá»›i ID: " + dto.getChiTietSanPhamId()));

        // Xá»­ lÃ½ hÃ³a Ä‘Æ¡n
        HoaDon hoaDon = null;
        if (dto.getHoaDonId() != null) {
            hoaDon = hoaDonRepository.findById(dto.getHoaDonId())
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n vá»›i ID: " + dto.getHoaDonId()));
        } else {
            // Tá»± Ä‘á»™ng tÃ¬m hÃ³a Ä‘Æ¡n tá»« chi tiáº¿t sáº£n pháº©m
            List<HoaDonChiTiet> hoaDonChiTiets = hoaDonChiTietRepository.findByChiTietSanPhamId(dto.getChiTietSanPhamId());
            if (!hoaDonChiTiets.isEmpty()) {
                hoaDonChiTiets.sort((a, b) -> b.getNgayTao().compareTo(a.getNgayTao()));
                hoaDon = hoaDonChiTiets.get(0).getHoaDon();
                dto.setHoaDonId(hoaDon.getId());
            }
        }

        // âœ… QUAN TRá»ŒNG: Kiá»ƒm tra cÃ³ thá»ƒ tráº£ hÃ ng khÃ´ng vá»›i logic má»›i
        if (!canReturn(dto.getChiTietSanPhamId(), dto.getSoLuong())) {
            // ThÃªm thÃ´ng tin chi tiáº¿t vÃ o error message
            Integer tongDaMua = hoaDonChiTietRepository.getTotalSoldQuantity(dto.getChiTietSanPhamId());
            Integer tongDaTra = getTotalReturnedQuantity(dto.getChiTietSanPhamId());

            String errorMsg = String.format(
                    "KhÃ´ng thá»ƒ tráº£ %d sáº£n pháº©m. ÄÃ£ mua: %d, Ä‘Ã£ tráº£: %d, cÃ³ thá»ƒ tráº£ thÃªm: %d",
                    dto.getSoLuong(),
                    tongDaMua != null ? tongDaMua : 0,
                    tongDaTra != null ? tongDaTra : 0,
                    (tongDaMua != null && tongDaTra != null) ? (tongDaMua - tongDaTra) : 0
            );

            throw new RuntimeException(errorMsg);
        }

        // Kiá»ƒm tra tráº¡ng thÃ¡i hÃ³a Ä‘Æ¡n - CHá»ˆ check náº¿u cÃ³ hÃ³a Ä‘Æ¡n
        if (hoaDon != null) {
            // âœ… Sá»¬A: Cho phÃ©p tráº£ vá»›i nhiá»u tráº¡ng thÃ¡i hÆ¡n
            Set<String> allowedStatuses = Set.of("COMPLETED", "DELIVERED", "DA_GIAO", "HOAN_THANH");
            if (!allowedStatuses.contains(hoaDon.getTrangThaiHoaDon())) {
                throw new RuntimeException("Chá»‰ cÃ³ thá»ƒ táº¡o yÃªu cáº§u tráº£ hÃ ng cho Ä‘Æ¡n hÃ ng Ä‘Ã£ giao thÃ nh cÃ´ng. Tráº¡ng thÃ¡i hiá»‡n táº¡i: " + hoaDon.getTrangThaiHoaDon());
            }
        }

        // Táº¡o chi tiáº¿t tráº£ hÃ ng
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

        System.out.println("âœ… Táº¡o thÃ nh cÃ´ng chi tiáº¿t tráº£ hÃ ng: " + saved.getMaChiTietTraHang());

        return convertToDTO(saved);
    }

    @Override
    public ChiTietTraHangDTO updateChiTietTraHang(Integer id, ChiTietTraHangDTO dto) {
        ChiTietTraHang chiTiet = chiTietTraHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t tráº£ hÃ ng vá»›i ID: " + id));

        // Chá»‰ cho phÃ©p cáº­p nháº­t khi tráº¡ng thÃ¡i lÃ  PENDING
        if (!"PENDING".equals(chiTiet.getTrangThaiHoaDon())) {
            throw new RuntimeException("KhÃ´ng thá»ƒ cáº­p nháº­t chi tiáº¿t tráº£ hÃ ng á»Ÿ tráº¡ng thÃ¡i: " + chiTiet.getTrangThaiHoaDon());
        }

        boolean hasChanges = false;

        // Cáº­p nháº­t sá»‘ lÆ°á»£ng náº¿u cÃ³
        if (dto.getSoLuong() != null && dto.getSoLuong() > 0 && !dto.getSoLuong().equals(chiTiet.getSoLuong())) {
            // Kiá»ƒm tra sá»‘ lÆ°á»£ng cÃ³ há»£p lá»‡ khÃ´ng
            if (!canReturn(chiTiet.getChiTietSanPham().getId(), dto.getSoLuong())) {
                throw new RuntimeException("KhÃ´ng thá»ƒ tráº£ hÃ ng vá»›i sá»‘ lÆ°á»£ng nÃ y");
            }
            chiTiet.setSoLuong(dto.getSoLuong());
            hasChanges = true;
        }

        // âœ… THÃŠM: Cáº­p nháº­t lÃ½ do náº¿u cÃ³
        if (dto.getLyDo() != null && !dto.getLyDo().trim().isEmpty() && !dto.getLyDo().equals(chiTiet.getLyDo())) {
            chiTiet.setLyDo(dto.getLyDo());
            hasChanges = true;
        }

        // âœ… THÃŠM: Cáº­p nháº­t áº£nh náº¿u cÃ³
        if (dto.getDuongDanAnh() != null && !dto.getDuongDanAnh().equals(chiTiet.getDuongDanAnh())) {
            chiTiet.setDuongDanAnh(dto.getDuongDanAnh());
            hasChanges = true;
        }

        // Cáº­p nháº­t tráº¡ng thÃ¡i náº¿u cÃ³
        if (dto.getTrangThaiHoaDon() != null && !dto.getTrangThaiHoaDon().equals(chiTiet.getTrangThaiHoaDon())) {
            if (!isValidStatusTransition(chiTiet.getTrangThaiHoaDon(), dto.getTrangThaiHoaDon())) {
                throw new RuntimeException("KhÃ´ng thá»ƒ chuyá»ƒn tá»« tráº¡ng thÃ¡i " + chiTiet.getTrangThaiHoaDon() + " sang " + dto.getTrangThaiHoaDon());
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
            throw new RuntimeException("Tráº¡ng thÃ¡i khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }

        ChiTietTraHang chiTiet = chiTietTraHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t tráº£ hÃ ng vá»›i ID: " + id));

        String oldStatus = chiTiet.getTrangThaiHoaDon();

        // Kiá»ƒm tra logic chuyá»ƒn tráº¡ng thÃ¡i
        if (!isValidStatusTransition(oldStatus, trangThai)) {
            throw new RuntimeException("KhÃ´ng thá»ƒ chuyá»ƒn tá»« tráº¡ng thÃ¡i " + oldStatus + " sang " + trangThai);
        }

        chiTiet.setTrangThaiHoaDon(trangThai);
        chiTiet.setNgayCapNhat(new Date());

        // Xá»­ lÃ½ logic theo tráº¡ng thÃ¡i má»›i
        handleStatusChange(chiTiet, oldStatus, trangThai, ghiChu);

        ChiTietTraHang saved = chiTietTraHangRepository.save(chiTiet);
        return convertToDTO(saved);
    }

    @Override
    public void deleteChiTietTraHang(Integer id) {
        ChiTietTraHang chiTiet = chiTietTraHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t tráº£ hÃ ng vá»›i ID: " + id));

        // Chá»‰ cho phÃ©p xÃ³a khi tráº¡ng thÃ¡i lÃ  PENDING
        if (!"PENDING".equals(chiTiet.getTrangThaiHoaDon())) {
            throw new RuntimeException("KhÃ´ng thá»ƒ xÃ³a chi tiáº¿t tráº£ hÃ ng á»Ÿ tráº¡ng thÃ¡i: " + chiTiet.getTrangThaiHoaDon());
        }

        chiTietTraHangRepository.delete(chiTiet);
    }

    @Override
    public Integer getTotalReturnedQuantity(Integer chiTietSanPhamId) {
        try {
            // âœ… Sá»¬A: Äáº£m báº£o query nÃ y Ä‘Ãºng logic
            Integer total = chiTietTraHangRepository.getTotalReturnedQuantityExcludeRejected(chiTietSanPhamId);

            System.out.println("DEBUG getTotalReturnedQuantity for product " + chiTietSanPhamId + ": " + total);

            return total != null ? total : 0;
        } catch (Exception e) {
            System.err.println("Lá»—i getTotalReturnedQuantity: " + e.getMessage());
            e.printStackTrace(); // âœ… THÃŠM: In stack trace Ä‘á»ƒ debug
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
            System.out.println("âŒ canReturn: Invalid input - chiTietSanPhamId=" + chiTietSanPhamId + ", soLuong=" + soLuong);
            return false;
        }

        try {
            // Láº¥y tá»•ng sá»‘ lÆ°á»£ng Ä‘Ã£ mua cá»§a chi tiáº¿t sáº£n pháº©m nÃ y
            Integer tongSoLuongDaMua = hoaDonChiTietRepository.getTotalSoldQuantity(chiTietSanPhamId);
            if (tongSoLuongDaMua == null || tongSoLuongDaMua <= 0) {
                System.out.println("âŒ canReturn: No purchase found for product " + chiTietSanPhamId);
                return false;
            }

            // Láº¥y tá»•ng sá»‘ lÆ°á»£ng Ä‘Ã£ tráº£ (chá»‰ tÃ­nh nhá»¯ng cÃ¡i KHÃ”NG bá»‹ reject)
            Integer tongSoLuongDaTra = getTotalReturnedQuantity(chiTietSanPhamId);
            if (tongSoLuongDaTra == null) {
                tongSoLuongDaTra = 0;
            }

            // âœ… LOGIC CHO PHÃ‰P TRáº¢ 100%
            int coTheTraThem = tongSoLuongDaMua - tongSoLuongDaTra;
            boolean result = soLuong <= coTheTraThem;

            System.out.println("=== CAN RETURN CHECK ===");
            System.out.println("Product ID: " + chiTietSanPhamId);
            System.out.println("ÄÃ£ mua: " + tongSoLuongDaMua);
            System.out.println("ÄÃ£ tráº£: " + tongSoLuongDaTra);
            System.out.println("CÃ³ thá»ƒ tráº£ thÃªm: " + coTheTraThem);
            System.out.println("YÃªu cáº§u tráº£: " + soLuong);
            System.out.println("Káº¿t quáº£: " + result);
            System.out.println("========================");

            return result;

        } catch (Exception e) {
            System.err.println("âŒ Lá»—i kiá»ƒm tra canReturn: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public ChiTietTraHangDTO approveReturn(Integer id, String ghiChu) {
        ChiTietTraHang chiTiet = chiTietTraHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t tráº£ hÃ ng vá»›i ID: " + id));

        if (!"PENDING".equals(chiTiet.getTrangThaiHoaDon())) {
            throw new RuntimeException("Chá»‰ cÃ³ thá»ƒ cháº¥p nháº­n tráº£ hÃ ng á»Ÿ tráº¡ng thÃ¡i PENDING");
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
                    .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao())) // âœ… Sá»¬A
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

        // ThÃ´ng tin cÆ¡ báº£n
        dto.setId(chiTiet.getId());
        dto.setMaChiTietTraHang(chiTiet.getMaChiTietTraHang());
        dto.setSoLuong(chiTiet.getSoLuong());
        dto.setTrangThaiHoaDon(chiTiet.getTrangThaiHoaDon());
        dto.setNgayTao(chiTiet.getNgayTao());
        dto.setNgayTaoTraHang(chiTiet.getNgayTaoTraHang());
        dto.setNgayCapNhat(chiTiet.getNgayCapNhat());

        // âœ… THÃŠM: LÃ½ do vÃ  áº£nh
        dto.setLyDo(chiTiet.getLyDo());
        dto.setDuongDanAnh(chiTiet.getDuongDanAnh());

        // ThÃ´ng tin hÃ³a Ä‘Æ¡n
        if (chiTiet.getHoaDon() != null) {
            dto.setHoaDonId(chiTiet.getHoaDon().getId());
            dto.setMaHoaDon(chiTiet.getHoaDon().getMaHoaDon());
        }

        // ThÃ´ng tin chi tiáº¿t sáº£n pháº©m
        if (chiTiet.getChiTietSanPham() != null) {
            ChiTietSanPham ctsp = chiTiet.getChiTietSanPham();
            dto.setChiTietSanPhamId(ctsp.getId());
            dto.setMaChiTiet(ctsp.getMaChiTiet());
            dto.setGiaGoc(ctsp.getGiaGoc());
            dto.setGiaBan(ctsp.getGiaGoc()); // Sá»­ dá»¥ng giÃ¡ gá»‘c lÃ m giÃ¡ bÃ¡n cho tráº£ hÃ ng
            dto.setHinhAnh(ctsp.getHinhAnh() != null ? ctsp.getHinhAnh().getDuongDan() : null);

            // ThÃ´ng tin sáº£n pháº©m
            if (ctsp.getSanPham() != null) {
                SanPham sp = ctsp.getSanPham();
                dto.setTenSanPham(sp.getTenSanPham());
                dto.setMaSanPham(sp.getMaSanPham());
                dto.setThuongHieu(sp.getThuongHieu() != null ? sp.getThuongHieu().getTenThuongHieu() : "N/A");
                dto.setDanhMuc(sp.getDanhMuc() != null ? sp.getDanhMuc().getTenDanhMuc() : "N/A");
            } else {
                dto.setTenSanPham("Sáº£n pháº©m khÃ´ng xÃ¡c Ä‘á»‹nh");
                dto.setMaSanPham("N/A");
                dto.setThuongHieu("N/A");
                dto.setDanhMuc("N/A");
            }

            // ThÃ´ng tin mÃ u sáº¯c vÃ  kÃ­ch thÆ°á»›c
            dto.setMauSac(ctsp.getMauSac() != null ? ctsp.getMauSac().getTenMauSac() : "N/A");
            dto.setKichThuoc(ctsp.getKichCo() != null ? ctsp.getKichCo().getTenKichCo() : "N/A");
        } else {
            // Fallback náº¿u khÃ´ng cÃ³ thÃ´ng tin chi tiáº¿t sáº£n pháº©m
            dto.setTenSanPham("Sáº£n pháº©m khÃ´ng xÃ¡c Ä‘á»‹nh");
            dto.setMaSanPham("N/A");
            dto.setMauSac("N/A");
            dto.setKichThuoc("N/A");
            dto.setThuongHieu("N/A");
            dto.setDanhMuc("N/A");
        }

        // TÃ­nh toÃ¡n cÃ¡c giÃ¡ trá»‹
        dto.calculateValues();

        return dto;
    }

    private String generateMaChiTietTraHang() {
        // Format: TH + nÄƒm + thÃ¡ng + ngÃ y + sá»‘ random
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        String date = sdf.format(new Date());
        int random = (int) (Math.random() * 9999) + 1;
        return String.format("TH%s%04d", date, random);
    }

    private boolean isValidStatusTransition(String fromStatus, String toStatus) {
        if (fromStatus == null || toStatus == null) {
            return false;
        }

        // Äá»‹nh nghÄ©a cÃ¡c chuyá»ƒn Ä‘á»•i tráº¡ng thÃ¡i há»£p lá»‡
        Map<String, Set<String>> validTransitions = Map.of(
                "PENDING", Set.of("APPROVED", "REJECTED"),
                "APPROVED", Set.of(), // KhÃ´ng thá»ƒ chuyá»ƒn tá»« APPROVED
                "REJECTED", Set.of()  // KhÃ´ng thá»ƒ chuyá»ƒn tá»« REJECTED
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
                // KhÃ´ng cáº§n xá»­ lÃ½ gÃ¬ Ä‘áº·c biá»‡t
                break;
        }
    }

    private void handleApproveReturn(ChiTietTraHang chiTiet, String ghiChu) {
        try {
            // Chá»‰ ghi log thÃ´ng tin, khÃ´ng cáº­p nháº­t kho
            // Logic cáº­p nháº­t kho sáº½ Ä‘Æ°á»£c xá»­ lÃ½ riÃªng trong cÃ¡c API má»›i

            System.out.println("=== Xá»¬ LÃ CHáº¤P NHáº¬N TRáº¢ HÃ€NG ===");
            System.out.println("MÃ£ tráº£ hÃ ng: " + chiTiet.getMaChiTietTraHang());
            System.out.println("Sá»‘ lÆ°á»£ng tráº£: " + chiTiet.getSoLuong());
            System.out.println("Tráº¡ng thÃ¡i: APPROVED");

            // Log thÃ´ng tin chi tiáº¿t sáº£n pháº©m náº¿u cÃ³
            if (chiTiet.getChiTietSanPham() != null) {
                ChiTietSanPham ctsp = chiTiet.getChiTietSanPham();
                System.out.println("Sáº£n pháº©m: " + ctsp.getMaChiTiet());
                System.out.println("Sá»‘ lÆ°á»£ng tá»“n hiá»‡n táº¡i: " + (ctsp.getSoLuong() != null ? ctsp.getSoLuong() : 0));

                // Log thÃ´ng tin sáº£n pháº©m
                if (ctsp.getSanPham() != null) {
                    System.out.println("TÃªn sáº£n pháº©m: " + ctsp.getSanPham().getTenSanPham());
                    System.out.println("Tá»•ng sá»‘ lÆ°á»£ng SP: " + (ctsp.getSanPham().getSoLuong() != null ? ctsp.getSanPham().getSoLuong() : 0));
                }
            }

            // Log ghi chÃº náº¿u cÃ³
            if (ghiChu != null && !ghiChu.trim().isEmpty()) {
                System.out.println("Ghi chÃº: " + ghiChu);
            }

            System.out.println("LÆ°u Ã½: Kho CHÆ¯A Ä‘Æ°á»£c cáº­p nháº­t. Sá»­ dá»¥ng API riÃªng Ä‘á»ƒ hoÃ n kho náº¿u cáº§n.");
            System.out.println("- HoÃ n kho: /approve-with-inventory");
            System.out.println("- KhÃ´ng hoÃ n kho: /approve-no-inventory");
            System.out.println("================================");

        } catch (Exception e) {
            System.err.println("Lá»—i xá»­ lÃ½ cháº¥p nháº­n tráº£ hÃ ng: " + e.getMessage());
            // KhÃ´ng throw exception vÃ¬ Ä‘Ã¢y chá»‰ lÃ  logging
        }
    }

    private void handleRejectReturn(ChiTietTraHang chiTiet, String lyDo) {
        // Log viá»‡c tá»« chá»‘i tráº£ hÃ ng
        System.out.println("Tá»« chá»‘i tráº£ hÃ ng " + chiTiet.getMaChiTietTraHang() +
                " vá»›i lÃ½ do: " + (lyDo != null ? lyDo : "KhÃ´ng cÃ³ lÃ½ do"));

        // CÃ³ thá»ƒ thÃªm logic gá»­i thÃ´ng bÃ¡o cho khÃ¡ch hÃ ng á»Ÿ Ä‘Ã¢y
    }

    // Utility method Ä‘á»ƒ validate tráº¡ng thÃ¡i
    private boolean isValidStatus(String status) {
        return Set.of("PENDING", "APPROVED", "REJECTED").contains(status);
    }
    @Override
    public List<ChiTietTraHangDTO> searchChiTietTraHang(String lyDo, String trangThai, Integer hoaDonId) {
        try {
            return chiTietTraHangRepository.searchChiTietTraHang(lyDo, trangThai, hoaDonId).stream()
                    .map(this::convertToDTO)
                    .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao())) // âœ… Sá»¬A: dÃ¹ng getNgayTao()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Lá»—i tÃ¬m kiáº¿m chi tiáº¿t tráº£ hÃ ng: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<ChiTietTraHangDTO> searchChiTietTraHangAdvanced(String lyDo, String trangThai, Integer hoaDonId,
                                                                Integer chiTietSanPhamId, Boolean hasImage) {
        try {
            // Táº¡m thá»i dÃ¹ng method cÆ¡ báº£n, bá» qua 2 tham sá»‘ cuá»‘i
            return chiTietTraHangRepository.searchChiTietTraHang(lyDo, trangThai, hoaDonId).stream()
                    .map(this::convertToDTO)
                    .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao())) // âœ… Sá»¬A
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Lá»—i tÃ¬m kiáº¿m nÃ¢ng cao: " + e.getMessage());
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
                    .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao())) // âœ… Sá»¬A
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Lá»—i tÃ¬m kiáº¿m theo lÃ½ do: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    @Override
    public List<ChiTietTraHangDTO> getChiTietTraHangWithImages() {
        try {
            return chiTietTraHangRepository.findAllWithImages().stream()
                    .map(this::convertToDTO)
                    .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao())) // âœ… Sá»¬A
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Lá»—i láº¥y danh sÃ¡ch cÃ³ áº£nh: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<ChiTietTraHangDTO> getChiTietTraHangWithoutImages() {
        try {
            return chiTietTraHangRepository.findAllWithoutImages().stream()
                    .map(this::convertToDTO)
                    .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao())) // âœ… Sá»¬A
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Lá»—i láº¥y danh sÃ¡ch khÃ´ng cÃ³ áº£nh: " + e.getMessage());
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
            System.err.println("Lá»—i láº¥y thá»‘ng kÃª lÃ½ do: " + e.getMessage());
            return new HashMap<>();
        }
    }

    @Override
    public Long countReturnsWithImages() {
        try {
            return chiTietTraHangRepository.countReturnsWithImages();
        } catch (Exception e) {
            System.err.println("Lá»—i Ä‘áº¿m tráº£ hÃ ng cÃ³ áº£nh: " + e.getMessage());
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
            System.err.println("Lá»—i Ä‘áº¿m theo lÃ½ do: " + e.getMessage());
            return 0L;
        }
    }

    @Override
    public ChiTietTraHangDTO updateImage(Integer id, String duongDanAnh) {
        try {
            ChiTietTraHang chiTiet = chiTietTraHangRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t tráº£ hÃ ng vá»›i ID: " + id));

            // Chá»‰ cho phÃ©p cáº­p nháº­t áº£nh khi tráº¡ng thÃ¡i lÃ  PENDING hoáº·c APPROVED
            if (!Set.of("PENDING", "APPROVED").contains(chiTiet.getTrangThaiHoaDon())) {
                throw new RuntimeException("KhÃ´ng thá»ƒ cáº­p nháº­t áº£nh á»Ÿ tráº¡ng thÃ¡i: " + chiTiet.getTrangThaiHoaDon());
            }

            chiTiet.setDuongDanAnh(duongDanAnh);
            chiTiet.setNgayCapNhat(new Date());

            ChiTietTraHang saved = chiTietTraHangRepository.save(chiTiet);
            return convertToDTO(saved);
        } catch (Exception e) {
            throw new RuntimeException("Lá»—i cáº­p nháº­t áº£nh: " + e.getMessage());
        }
    }

    @Override
    public ChiTietTraHangDTO removeImage(Integer id) {
        try {
            ChiTietTraHang chiTiet = chiTietTraHangRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t tráº£ hÃ ng vá»›i ID: " + id));

            // Chá»‰ cho phÃ©p xÃ³a áº£nh khi tráº¡ng thÃ¡i lÃ  PENDING
            if (!"PENDING".equals(chiTiet.getTrangThaiHoaDon())) {
                throw new RuntimeException("KhÃ´ng thá»ƒ xÃ³a áº£nh á»Ÿ tráº¡ng thÃ¡i: " + chiTiet.getTrangThaiHoaDon());
            }

            chiTiet.setDuongDanAnh(null);
            chiTiet.setNgayCapNhat(new Date());

            ChiTietTraHang saved = chiTietTraHangRepository.save(chiTiet);
            return convertToDTO(saved);
        } catch (Exception e) {
            throw new RuntimeException("Lá»—i xÃ³a áº£nh: " + e.getMessage());
        }
    }

    // âœ… Cáº¬P NHáº¬T: Method rejectReturn Ä‘á»ƒ lÆ°u lÃ½ do tá»« chá»‘i
    @Override
    public ChiTietTraHangDTO rejectReturn(Integer id, String lyDoTuChoi) {
        ChiTietTraHang chiTiet = chiTietTraHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t tráº£ hÃ ng vá»›i ID: " + id));

        if (!"PENDING".equals(chiTiet.getTrangThaiHoaDon())) {
            throw new RuntimeException("Chá»‰ cÃ³ thá»ƒ tá»« chá»‘i tráº£ hÃ ng á»Ÿ tráº¡ng thÃ¡i PENDING");
        }

        if (lyDoTuChoi == null || lyDoTuChoi.trim().isEmpty()) {
            throw new RuntimeException("LÃ½ do tá»« chá»‘i khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }

        // Cáº­p nháº­t tráº¡ng thÃ¡i vÃ  lÃ½ do tá»« chá»‘i
        chiTiet.setTrangThaiHoaDon("REJECTED");
        // CÃ³ thá»ƒ lÆ°u lÃ½ do tá»« chá»‘i vÃ o má»™t trÆ°á»ng riÃªng hoáº·c ghÃ©p vá»›i lÃ½ do hiá»‡n táº¡i
        String lyDoMoi = chiTiet.getLyDo() + " [Tá»ª CHá»I: " + lyDoTuChoi + "]";
        chiTiet.setLyDo(lyDoMoi);
        chiTiet.setNgayCapNhat(new Date());

        ChiTietTraHang saved = chiTietTraHangRepository.save(chiTiet);
        return convertToDTO(saved);
    }

    // âœ… THÃŠM: Method helper Ä‘á»ƒ validate tráº¡ng thÃ¡i cÃ³ thá»ƒ cáº­p nháº­t
    private boolean canUpdateReturn(String trangThai) {
        return Set.of("PENDING").contains(trangThai);
    }

    // âœ… THÃŠM: Method helper Ä‘á»ƒ validate tráº¡ng thÃ¡i cÃ³ thá»ƒ thÃªm áº£nh
    private boolean canAddImage(String trangThai) {
        return Set.of("PENDING", "APPROVED").contains(trangThai);
    }

    // âœ… THÃŠM: Method helper Ä‘á»ƒ láº¥y cÃ¡c lÃ½ do tráº£ hÃ ng phá»• biáº¿n
    public List<String> getCommonReturnReasons() {
        try {
            List<Object[]> results = chiTietTraHangRepository.getReturnReasonStatistics();
            return results.stream()
                    .limit(10) // Láº¥y 10 lÃ½ do phá»• biáº¿n nháº¥t
                    .map(result -> (String) result[0])
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Tráº£ vá» danh sÃ¡ch lÃ½ do máº·c Ä‘á»‹nh
            return Arrays.asList(
                    "Sáº£n pháº©m bá»‹ lá»—i",
                    "KhÃ´ng Ä‘Ãºng mÃ´ táº£",
                    "KÃ­ch thÆ°á»›c khÃ´ng phÃ¹ há»£p",
                    "MÃ u sáº¯c khÃ´ng Ä‘Ãºng",
                    "Cháº¥t lÆ°á»£ng khÃ´ng tá»‘t",
                    "Thay Ä‘á»•i Ã½ kiáº¿n",
                    "Giao hÃ ng muá»™n",
                    "Bao bÃ¬ hÆ° há»ng"
            );
        }
    }

    // âœ… THÃŠM: Method validate file áº£nh tá»« service layer
    public boolean validateImageFile(String duongDanAnh) {
        if (duongDanAnh == null || duongDanAnh.trim().isEmpty()) {
            return false;
        }

        // Kiá»ƒm tra extension
        String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};
        String lowerPath = duongDanAnh.toLowerCase();

        return Arrays.stream(allowedExtensions)
                .anyMatch(lowerPath::endsWith);
    }

    // âœ… THÃŠM: Method táº¡o bÃ¡o cÃ¡o thá»‘ng kÃª tráº£ hÃ ng chi tiáº¿t
    public Map<String, Object> getDetailedReturnStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // Thá»‘ng kÃª tá»•ng quan
            Long totalReturns = chiTietTraHangRepository.count();
            Long returnsWithImages = countReturnsWithImages();
            Long returnsWithoutImages = totalReturns - returnsWithImages;

            // Thá»‘ng kÃª theo tráº¡ng thÃ¡i
            Long pendingReturns = chiTietTraHangRepository.countByTrangThaiHoaDon("PENDING");
            Long approvedReturns = chiTietTraHangRepository.countByTrangThaiHoaDon("APPROVED");
            Long rejectedReturns = chiTietTraHangRepository.countByTrangThaiHoaDon("REJECTED");

            // Thá»‘ng kÃª theo lÃ½ do
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
            System.err.println("Lá»—i táº¡o bÃ¡o cÃ¡o thá»‘ng kÃª: " + e.getMessage());
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
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t tráº£ hÃ ng vá»›i ID: " + id));

        if (!"PENDING".equals(chiTiet.getTrangThaiHoaDon())) {
            throw new RuntimeException("Chá»‰ cÃ³ thá»ƒ cháº¥p nháº­n tráº£ hÃ ng á»Ÿ tráº¡ng thÃ¡i PENDING");
        }

        // Cáº­p nháº­t tráº¡ng thÃ¡i
        chiTiet.setTrangThaiHoaDon("APPROVED");
        chiTiet.setNgayCapNhat(new Date());

        // HoÃ n láº¡i kho
        handleRestoreInventory(chiTiet, ghiChu);

        ChiTietTraHang saved = chiTietTraHangRepository.save(chiTiet);
        return convertToDTO(saved);
    }

    @Override
    public ChiTietTraHangDTO approveReturnNoInventory(Integer id, String ghiChu) {
        ChiTietTraHang chiTiet = chiTietTraHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t tráº£ hÃ ng vá»›i ID: " + id));

        if (!"PENDING".equals(chiTiet.getTrangThaiHoaDon())) {
            throw new RuntimeException("Chá»‰ cÃ³ thá»ƒ cháº¥p nháº­n tráº£ hÃ ng á»Ÿ tráº¡ng thÃ¡i PENDING");
        }

        // Chá»‰ cáº­p nháº­t tráº¡ng thÃ¡i, khÃ´ng hoÃ n kho
        chiTiet.setTrangThaiHoaDon("APPROVED");
        chiTiet.setNgayCapNhat(new Date());

        System.out.println("Cháº¥p nháº­n tráº£ hÃ ng " + chiTiet.getMaChiTietTraHang() +
                " KHÃ”NG hoÃ n kho theo yÃªu cáº§u");

        ChiTietTraHang saved = chiTietTraHangRepository.save(chiTiet);
        return convertToDTO(saved);
    }

    // Method helper Ä‘á»ƒ hoÃ n kho
    private void handleRestoreInventory(ChiTietTraHang chiTiet, String ghiChu) {
        try {
            ChiTietSanPham ctsp = chiTiet.getChiTietSanPham();
            if (ctsp != null) {
                // HoÃ n láº¡i sá»‘ lÆ°á»£ng vÃ o chi tiáº¿t sáº£n pháº©m
                Integer currentQty = ctsp.getSoLuong() != null ? ctsp.getSoLuong() : 0;
                ctsp.setSoLuong(currentQty + chiTiet.getSoLuong());
                chiTietSanPhamRepository.save(ctsp);

                // Cáº­p nháº­t tá»•ng sá»‘ lÆ°á»£ng sáº£n pháº©m
                if (ctsp.getSanPham() != null) {
                    SanPham sanPham = ctsp.getSanPham();
                    Integer currentSpQty = sanPham.getSoLuong() != null ? sanPham.getSoLuong() : 0;
                    sanPham.setSoLuong(currentSpQty + chiTiet.getSoLuong());
                    // sanPhamRepository.save(sanPham); // Uncomment náº¿u cÃ³ repository
                }

                System.out.println("ÄÃ£ hoÃ n láº¡i " + chiTiet.getSoLuong() +
                        " sáº£n pháº©m " + ctsp.getMaChiTiet() + " vÃ o kho");
            }
        } catch (Exception e) {
            System.err.println("Lá»—i hoÃ n kho: " + e.getMessage());
            throw new RuntimeException("Lá»—i hoÃ n kho: " + e.getMessage());
        }
    }
}
