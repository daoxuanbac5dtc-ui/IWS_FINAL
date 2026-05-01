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

            // Force load tất cả lazy associations
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi với ID: " + request.getKhuyenMaiId()));

        List<KhuyenMaiChiTiet> results = new ArrayList<>();

        for (Integer chiTietSanPhamId : request.getChiTietSanPhamIds()) {
            ChiTietSanPham chiTietSanPham = chiTietSanPhamService.getById(chiTietSanPhamId);

            // Kiểm tra xem sản phẩm đã có khuyến mãi active chưa
            List<KhuyenMaiChiTiet> existingPromotions =
                    repository.findActivePromotionsByProductDetail(chiTietSanPhamId);

            if (!existingPromotions.isEmpty()) {
                throw new RuntimeException("Sản phẩm " + chiTietSanPham.getMaChiTiet() +
                        " đã có khuyến mãi đang áp dụng");
            }

            // Kiểm tra xem đã tồn tại bản ghi này chưa
            if (repository.findByKhuyenMaiAndChiTietSanPham(khuyenMai, chiTietSanPham).isPresent()) {
                continue;
            }

            // Xử lý giá với Double
            Double giaGoc = chiTietSanPham.getGiaGoc();

            // Nếu chưa có giá gốc, lấy giá bán hiện tại làm giá gốc
            if (giaGoc == null || giaGoc <= 0) {
                giaGoc = chiTietSanPham.getGiaBan();
                if (giaGoc == null || giaGoc <= 0) {
                    throw new RuntimeException("Sản phẩm " + chiTietSanPham.getMaChiTiet() +
                            " không có giá hợp lệ");
                }
                chiTietSanPham.setGiaGoc(giaGoc);
            }

            // Tính giá bán mới: giaBan = giaGoc * (1 - discount)
            Double discountRate = khuyenMai.getGiaTri().doubleValue() / 100.0;
            Double multiplier = 1.0 - discountRate;
            Double giaBanMoi = giaGoc * multiplier;

            // Làm tròn đến 2 chữ số thập phân
            giaBanMoi = Math.round(giaBanMoi * 100.0) / 100.0;

            // Cập nhật giá bán
            chiTietSanPham.setGiaBan(giaBanMoi);
            chiTietSanPham.setNgayCapNhat(new Date());
            chiTietSanPhamRepository.save(chiTietSanPham);

            // Tạo bản ghi khuyến mãi chi tiết
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi với ID: " + khuyenMaiId));
        ChiTietSanPham chiTietSanPham = chiTietSanPhamService.getById(chiTietSanPhamId);

        repository.findByKhuyenMaiAndChiTietSanPham(khuyenMai, chiTietSanPham)
                .ifPresent(kmct -> {
                    // Khôi phục giá bán về giá gốc
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết khuyến mãi với ID: " + id));

        // Force load associations
        forceLoadPromotionDetailAssociations(detail);

        return detail;
    }

    @Override
    public void recalculatePricesForPromotion(Integer khuyenMaiId) {
        KhuyenMai khuyenMai = khuyenMaiRepository.findById(khuyenMaiId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi với ID: " + khuyenMaiId));

        List<KhuyenMaiChiTiet> promotionDetails = repository.findByKhuyenMaiId(khuyenMaiId);

        for (KhuyenMaiChiTiet detail : promotionDetails) {
            ChiTietSanPham chiTietSanPham = detail.getChiTietSanPham();
            Double giaGoc = chiTietSanPham.getGiaGoc();

            if (giaGoc != null && giaGoc > 0) {
                Double discountRate = khuyenMai.getGiaTri().doubleValue() / 100.0;
                Double multiplier = 1.0 - discountRate;
                Double giaBanMoi = giaGoc * multiplier;

                // Làm tròn đến 2 chữ số thập phân
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
     * Force load tất cả lazy associations cho KhuyenMaiChiTiet
     */
    private void forceLoadPromotionDetailAssociations(KhuyenMaiChiTiet detail) {
        try {
            // Force load khuyến mãi
            if (detail.getKhuyenMai() != null) {
                String maKhuyenMai = detail.getKhuyenMai().getMaKhuyenMai();
                String tenKhuyenMai = detail.getKhuyenMai().getTenKhuyenMai();
                Float giaTri = detail.getKhuyenMai().getGiaTri();
            }

            // Force load chi tiết sản phẩm và associations
            if (detail.getChiTietSanPham() != null) {
                ChiTietSanPham ctsp = detail.getChiTietSanPham();
                String maChiTiet = ctsp.getMaChiTiet();

                // Force load sản phẩm
                if (ctsp.getSanPham() != null) {
                    String tenSanPham = ctsp.getSanPham().getTenSanPham();
                    String maSanPham = ctsp.getSanPham().getMaSanPham();

                    // Force load thương hiệu
                    if (ctsp.getSanPham().getThuongHieu() != null) {
                        String tenThuongHieu = ctsp.getSanPham().getThuongHieu().getTenThuongHieu();
                    }

                    // Force load danh mục
                    if (ctsp.getSanPham().getDanhMuc() != null) {
                        String tenDanhMuc = ctsp.getSanPham().getDanhMuc().getTenDanhMuc();
                    }
                }

                // Force load màu sắc
                if (ctsp.getMauSac() != null) {
                    String tenMauSac = ctsp.getMauSac().getTenMauSac();
                    String maMau = ctsp.getMauSac().getMaMauSac();
                }

                // Force load kích cỡ
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
            // Lấy thông tin khuyến mãi
            KhuyenMai khuyenMai = khuyenMaiRepository.findById(promotionId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi"));

            // Kiểm tra trạng thái khuyến mãi
            Date now = new Date();
            boolean isActive = khuyenMai.getTrangThai() == 1 &&
                    khuyenMai.getNgayBatDau().before(now) &&
                    khuyenMai.getNgayKetThuc().after(now);

            if (!isActive) {
                // Lấy tất cả sản phẩm của khuyến mãi này
                List<KhuyenMaiChiTiet> details = repository.findByKhuyenMaiId(promotionId);

                for (KhuyenMaiChiTiet detail : details) {
                    ChiTietSanPham chiTiet = detail.getChiTietSanPham();
                    if (chiTiet != null) {
                        // Reset giá bán về giá gốc
                        chiTiet.setGiaBan(chiTiet.getGiaGoc());
                        chiTietSanPhamRepository.save(chiTiet);
                    }
                }

                System.out.println("Reset prices for " + details.size() + " products of promotion " + promotionId);
            }
        } catch (Exception e) {
            System.err.println("Error resetting prices for promotion " + promotionId + ": " + e.getMessage());
            throw new RuntimeException("Lỗi khi reset giá cho khuyến mãi: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void resetAllInactivePrices() {
        try {
            // Lấy tất cả khuyến mãi
            List<KhuyenMai> allPromotions = khuyenMaiRepository.findAll();
            Date now = new Date();
            int resetCount = 0;

            for (KhuyenMai khuyenMai : allPromotions) {
                // Kiểm tra trạng thái khuyến mãi
                boolean isActive = khuyenMai.getTrangThai() == 1 &&
                        khuyenMai.getNgayBatDau().before(now) &&
                        khuyenMai.getNgayKetThuc().after(now);

                if (!isActive) {
                    // Lấy tất cả sản phẩm của khuyến mãi này
                    List<KhuyenMaiChiTiet> details = repository.findByKhuyenMaiId(khuyenMai.getId());

                    for (KhuyenMaiChiTiet detail : details) {
                        ChiTietSanPham chiTiet = detail.getChiTietSanPham();
                        if (chiTiet != null && !chiTiet.getGiaBan().equals(chiTiet.getGiaGoc())) {
                            // Reset giá bán về giá gốc
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
            throw new RuntimeException("Lỗi khi reset tất cả giá không hoạt động: " + e.getMessage());
        }
    }
}
