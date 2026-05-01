package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.entity.ChiTietSanPham;
import org.springframework.stereotype.Service;

import java.util.List;

public interface ChiTietSanPhamService {
    List<ChiTietSanPham> getAll();
    List<ChiTietSanPham> getActiveProducts();
    List<ChiTietSanPham> getProductsWithoutPromotion();
    ChiTietSanPham getById(Integer id);
    List<ChiTietSanPham> searchByKeyword(String keyword);
    Long countActiveProducts();
    ChiTietSanPham save(ChiTietSanPham chiTietSanPham);
    // Kiểm tra trùng lặp
//    ChiTietSanPham findBySanPhamAndMauSacAndKichCo(Integer sanPhamId, Integer mauSacId, Integer kichCoId);
//
//    // Validation
//    boolean existsByMaChiTiet(String maChiTiet);
//
//    // Thống kê
//    Integer getTotalQuantityBySanPhamId(Integer sanPhamId);
}
