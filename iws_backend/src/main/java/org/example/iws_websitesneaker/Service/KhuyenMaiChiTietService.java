package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.Dto.ApplyPromotionRequest;
import org.example.iws_websitesneaker.entity.KhuyenMaiChiTiet;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface KhuyenMaiChiTietService {
    List<KhuyenMaiChiTiet> getByKhuyenMaiId(Integer khuyenMaiId);
    List<KhuyenMaiChiTiet> applyPromotionToProducts(ApplyPromotionRequest request);
    void removePromotionFromProduct(Integer khuyenMaiId, Integer chiTietSanPhamId);
    void removeAllPromotionsFromKhuyenMai(Integer khuyenMaiId);
    KhuyenMaiChiTiet getDetailById(Integer id);
    void recalculatePricesForPromotion(Integer khuyenMaiId);
    void resetPricesForInactivePromotion(Integer promotionId);
    void resetAllInactivePrices();
}

