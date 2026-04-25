package org.example.iws_websitesneaker.Service.impl;

import org.example.iws_websitesneaker.entity.KhuyenMaiChiTiet;
import org.example.iws_websitesneaker.entity.KhuyenMai;
import org.example.iws_websitesneaker.entity.ChiTietSanPham;
import org.example.iws_websitesneaker.Dto.ApplyPromotionRequest;
import org.example.iws_websitesneaker.Service.KhuyenMaiChiTietService;
import org.example.iws_websitesneaker.Service.KhuyenMaiService;
import org.example.iws_websitesneaker.Service.ChiTietSanPhamService;
import org.example.iws_websitesneaker.repository.KhuyenMaiChiTietRepository;
import org.example.iws_websitesneaker.repository.ChiTietSanPhamRepository;
import org.example.iws_websitesneaker.repository.KhuyenMaiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class KhuyenMaiChiTietServiceImpl implements KhuyenMaiChiTietService {

    @Autowired
    private KhuyenMaiChiTietRepository repository;

    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Autowired
    private KhuyenMaiRepository khuyenMaiRepository;

    @Autowired
    private ChiTietSanPhamService chiTietSanPhamService;

    @Override
    @Transactional(readOnly = true)
    public List<KhuyenMaiChiTiet> getByKhuyenMaiId(Integer khuyenMaiId) {
        try {
            System.out.println("Loading promotion details for ID: " + khuyenMaiId);
            List<KhuyenMaiChiTiet> details = repository.findDetailsByKhuyenMaiId(khuyenMaiId);

            // Force load táº¥t cáº£ lazy associations
            for (KhuyenMaiChiTiet detail : details) {
                forceLoadPromotionDetailAssociations(detail);
            }

            System.out.println("Loaded " + details.size() + " promotion details with full info");
            return details;
        } catch (Exception e) {
            System.err.println("Error loading promotion details: " + e.getMessage());
            e.printStackTrace();
            // Fallback to simple query
            return repository.findByKhuyenMaiId(khuyenMaiId);
        }
    }

    @Override
    public List<KhuyenMaiChiTiet> applyPromotionToProducts(ApplyPromotionRequest request) {
        KhuyenMai khuyenMai = khuyenMaiRepository.findById(request.getKhuyenMaiId())
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y khuyáº¿n mÃ£i vá»›i ID: " + request.getKhuyenMaiId()));

        List<KhuyenMaiChiTiet> results = new ArrayList<>();

        for (Integer chiTietSanPhamId : request.getChiTietSanPhamIds()) {
            ChiTietSanPham chiTietSanPham = chiTietSanPhamService.getById(chiTietSanPhamId);

            // Kiá»ƒm tra xem sáº£n pháº©m Ä‘Ã£ cÃ³ khuyáº¿n mÃ£i active chÆ°a
            List<KhuyenMaiChiTiet> existingPromotions =
                    repository.findActivePromotionsByProductDetail(chiTietSanPhamId);

            if (!existingPromotions.isEmpty()) {
                throw new RuntimeException("Sáº£n pháº©m " + chiTietSanPham.getMaChiTiet() +
                        " Ä‘Ã£ cÃ³ khuyáº¿n mÃ£i Ä‘ang Ã¡p dá»¥ng");
            }

            // Kiá»ƒm tra xem Ä‘Ã£ tá»“n táº¡i báº£n ghi nÃ y chÆ°a
            if (repository.findByKhuyenMaiAndChiTietSanPham(khuyenMai, chiTietSanPham).isPresent()) {
                continue;
            }

            // Xá»­ lÃ½ giÃ¡ vá»›i Double
            Double giaGoc = chiTietSanPham.getGiaGoc();

            // Náº¿u chÆ°a cÃ³ giÃ¡ gá»‘c, láº¥y giÃ¡ bÃ¡n hiá»‡n táº¡i lÃ m giÃ¡ gá»‘c
            if (giaGoc == null || giaGoc <= 0) {
                giaGoc = chiTietSanPham.getGiaBan();
                if (giaGoc == null || giaGoc <= 0) {
                    throw new RuntimeException("Sáº£n pháº©m " + chiTietSanPham.getMaChiTiet() +
                            " khÃ´ng cÃ³ giÃ¡ há»£p lá»‡");
                }
                chiTietSanPham.setGiaGoc(giaGoc);
            }

            // TÃ­nh giÃ¡ bÃ¡n má»›i: giaBan = giaGoc * (1 - discount)
            Double discountRate = khuyenMai.getGiaTri().doubleValue() / 100.0;
            Double multiplier = 1.0 - discountRate;
            Double giaBanMoi = giaGoc * multiplier;

            // LÃ m trÃ²n Ä‘áº¿n 2 chá»¯ sá»‘ tháº­p phÃ¢n
            giaBanMoi = Math.round(giaBanMoi * 100.0) / 100.0;

            // Cáº­p nháº­t giÃ¡ bÃ¡n
            chiTietSanPham.setGiaBan(giaBanMoi);
            chiTietSanPham.setNgayCapNhat(new Date());
            chiTietSanPhamRepository.save(chiTietSanPham);

            // Táº¡o báº£n ghi khuyáº¿n mÃ£i chi tiáº¿t
            KhuyenMaiChiTiet khuyenMaiChiTiet = new KhuyenMaiChiTiet();
            khuyenMaiChiTiet.setKhuyenMai(khuyenMai);
            khuyenMaiChiTiet.setChiTietSanPham(chiTietSanPham);
            khuyenMaiChiTiet.setTrangThai(1);
            khuyenMaiChiTiet.setNgayTao(new Date());

            results.add(repository.save(khuyenMaiChiTiet));

            System.out.println("Applied promotion to product: " + chiTietSanPham.getMaChiTiet() +
                    ", old price: " + giaGoc + ", new price: " + giaBanMoi);
        }

        return results;
    }

    @Override
    public void removePromotionFromProduct(Integer khuyenMaiId, Integer chiTietSanPhamId) {
        KhuyenMai khuyenMai = khuyenMaiRepository.findById(khuyenMaiId)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y khuyáº¿n mÃ£i vá»›i ID: " + khuyenMaiId));
        ChiTietSanPham chiTietSanPham = chiTietSanPhamService.getById(chiTietSanPhamId);

        repository.findByKhuyenMaiAndChiTietSanPham(khuyenMai, chiTietSanPham)
                .ifPresent(kmct -> {
                    // KhÃ´i phá»¥c giÃ¡ bÃ¡n vá» giÃ¡ gá»‘c
                    if (chiTietSanPham.getGiaGoc() != null && chiTietSanPham.getGiaGoc() > 0) {
                        chiTietSanPham.setGiaBan(chiTietSanPham.getGiaGoc());
                        chiTietSanPham.setNgayCapNhat(new Date());
                        chiTietSanPhamRepository.save(chiTietSanPham);

                        System.out.println("Restored price for product: " + chiTietSanPham.getMaChiTiet() +
                                " to: " + chiTietSanPham.getGiaGoc());
                    }
                    repository.delete(kmct);
                });
    }

    @Override
    public void removeAllPromotionsFromKhuyenMai(Integer khuyenMaiId) {
        List<KhuyenMaiChiTiet> promotionDetails = repository.findByKhuyenMaiId(khuyenMaiId);

        for (KhuyenMaiChiTiet detail : promotionDetails) {
            ChiTietSanPham chiTietSanPham = detail.getChiTietSanPham();
            if (chiTietSanPham.getGiaGoc() != null && chiTietSanPham.getGiaGoc() > 0) {
                chiTietSanPham.setGiaBan(chiTietSanPham.getGiaGoc());
                chiTietSanPham.setNgayCapNhat(new Date());
                chiTietSanPhamRepository.save(chiTietSanPham);

                System.out.println("Restored price for product: " + chiTietSanPham.getMaChiTiet());
            }
        }

        repository.deleteByKhuyenMaiId(khuyenMaiId);
        System.out.println("Removed all promotions for khuyenMai ID: " + khuyenMaiId);
    }

    @Override
    @Transactional(readOnly = true)
    public KhuyenMaiChiTiet getDetailById(Integer id) {
        KhuyenMaiChiTiet detail = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t khuyáº¿n mÃ£i vá»›i ID: " + id));

        // Force load associations
        forceLoadPromotionDetailAssociations(detail);

        return detail;
    }

    @Override
    public void recalculatePricesForPromotion(Integer khuyenMaiId) {
        KhuyenMai khuyenMai = khuyenMaiRepository.findById(khuyenMaiId)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y khuyáº¿n mÃ£i vá»›i ID: " + khuyenMaiId));

        List<KhuyenMaiChiTiet> promotionDetails = repository.findByKhuyenMaiId(khuyenMaiId);

        for (KhuyenMaiChiTiet detail : promotionDetails) {
            ChiTietSanPham chiTietSanPham = detail.getChiTietSanPham();
            Double giaGoc = chiTietSanPham.getGiaGoc();

            if (giaGoc != null && giaGoc > 0) {
                Double discountRate = khuyenMai.getGiaTri().doubleValue() / 100.0;
                Double multiplier = 1.0 - discountRate;
                Double giaBanMoi = giaGoc * multiplier;

                // LÃ m trÃ²n Ä‘áº¿n 2 chá»¯ sá»‘ tháº­p phÃ¢n
                giaBanMoi = Math.round(giaBanMoi * 100.0) / 100.0;

                chiTietSanPham.setGiaBan(giaBanMoi);
                chiTietSanPham.setNgayCapNhat(new Date());
                chiTietSanPhamRepository.save(chiTietSanPham);

                System.out.println("Recalculated price for product: " + chiTietSanPham.getMaChiTiet() +
                        " new price: " + giaBanMoi);
            }
        }
    }

    /**
     * Force load táº¥t cáº£ lazy associations cho KhuyenMaiChiTiet
     */
    private void forceLoadPromotionDetailAssociations(KhuyenMaiChiTiet detail) {
        try {
            // Force load khuyáº¿n mÃ£i
            if (detail.getKhuyenMai() != null) {
                String maKhuyenMai = detail.getKhuyenMai().getMaKhuyenMai();
                String tenKhuyenMai = detail.getKhuyenMai().getTenKhuyenMai();
                Float giaTri = detail.getKhuyenMai().getGiaTri();
            }

            // Force load chi tiáº¿t sáº£n pháº©m vÃ  associations
            if (detail.getChiTietSanPham() != null) {
                ChiTietSanPham ctsp = detail.getChiTietSanPham();
                String maChiTiet = ctsp.getMaChiTiet();

                // Force load sáº£n pháº©m
                if (ctsp.getSanPham() != null) {
                    String tenSanPham = ctsp.getSanPham().getTenSanPham();
                    String maSanPham = ctsp.getSanPham().getMaSanPham();

                    // Force load thÆ°Æ¡ng hiá»‡u
                    if (ctsp.getSanPham().getThuongHieu() != null) {
                        String tenThuongHieu = ctsp.getSanPham().getThuongHieu().getTenThuongHieu();
                    }

                    // Force load danh má»¥c
                    if (ctsp.getSanPham().getDanhMuc() != null) {
                        String tenDanhMuc = ctsp.getSanPham().getDanhMuc().getTenDanhMuc();
                    }
                }

                // Force load mÃ u sáº¯c
                if (ctsp.getMauSac() != null) {
                    String tenMauSac = ctsp.getMauSac().getTenMauSac();
                    String maMau = ctsp.getMauSac().getMaMauSac();
                }

                // Force load kÃ­ch cá»¡
                if (ctsp.getKichCo() != null) {
                    String tenKichCo = ctsp.getKichCo().getTenKichCo();
                }
            }
        } catch (Exception e) {
            System.err.println("Error force loading associations for promotion detail " + detail.getId() + ": " + e.getMessage());
        }
    }
    @Override
    @Transactional
    public void resetPricesForInactivePromotion(Integer promotionId) {
        try {
            // Láº¥y thÃ´ng tin khuyáº¿n mÃ£i
            KhuyenMai khuyenMai = khuyenMaiRepository.findById(promotionId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y khuyáº¿n mÃ£i"));

            // Kiá»ƒm tra tráº¡ng thÃ¡i khuyáº¿n mÃ£i
            Date now = new Date();
            boolean isActive = khuyenMai.getTrangThai() == 1 &&
                    khuyenMai.getNgayBatDau().before(now) &&
                    khuyenMai.getNgayKetThuc().after(now);

            if (!isActive) {
                // Láº¥y táº¥t cáº£ sáº£n pháº©m cá»§a khuyáº¿n mÃ£i nÃ y
                List<KhuyenMaiChiTiet> details = repository.findByKhuyenMaiId(promotionId);

                for (KhuyenMaiChiTiet detail : details) {
                    ChiTietSanPham chiTiet = detail.getChiTietSanPham();
                    if (chiTiet != null) {
                        // Reset giÃ¡ bÃ¡n vá» giÃ¡ gá»‘c
                        chiTiet.setGiaBan(chiTiet.getGiaGoc());
                        chiTietSanPhamRepository.save(chiTiet);
                    }
                }

                System.out.println("Reset prices for " + details.size() + " products of promotion " + promotionId);
            }
        } catch (Exception e) {
            System.err.println("Error resetting prices for promotion " + promotionId + ": " + e.getMessage());
            throw new RuntimeException("Lá»—i khi reset giÃ¡ cho khuyáº¿n mÃ£i: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void resetAllInactivePrices() {
        try {
            // Láº¥y táº¥t cáº£ khuyáº¿n mÃ£i
            List<KhuyenMai> allPromotions = khuyenMaiRepository.findAll();
            Date now = new Date();
            int resetCount = 0;

            for (KhuyenMai khuyenMai : allPromotions) {
                // Kiá»ƒm tra tráº¡ng thÃ¡i khuyáº¿n mÃ£i
                boolean isActive = khuyenMai.getTrangThai() == 1 &&
                        khuyenMai.getNgayBatDau().before(now) &&
                        khuyenMai.getNgayKetThuc().after(now);

                if (!isActive) {
                    // Láº¥y táº¥t cáº£ sáº£n pháº©m cá»§a khuyáº¿n mÃ£i nÃ y
                    List<KhuyenMaiChiTiet> details = repository.findByKhuyenMaiId(khuyenMai.getId());

                    for (KhuyenMaiChiTiet detail : details) {
                        ChiTietSanPham chiTiet = detail.getChiTietSanPham();
                        if (chiTiet != null && !chiTiet.getGiaBan().equals(chiTiet.getGiaGoc())) {
                            // Reset giÃ¡ bÃ¡n vá» giÃ¡ gá»‘c
                            chiTiet.setGiaBan(chiTiet.getGiaGoc());
                            chiTietSanPhamRepository.save(chiTiet);
                            resetCount++;
                        }
                    }
                }
            }

            System.out.println("Reset prices for " + resetCount + " products from inactive promotions");
        } catch (Exception e) {
            System.err.println("Error resetting all inactive prices: " + e.getMessage());
            throw new RuntimeException("Lá»—i khi reset táº¥t cáº£ giÃ¡ khÃ´ng hoáº¡t Ä‘á»™ng: " + e.getMessage());
        }
    }
}
