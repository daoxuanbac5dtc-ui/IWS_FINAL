package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.Dto.SanPhamSearchResponse;
import org.example.iws_websitesneaker.entity.SanPham;

import java.util.List;
import java.util.Optional;

public interface SanPhamService {
    List<SanPham> getFiltered(String tenSanPham, Integer danhMucId, Integer thuongHieuId, Integer trangThai);
    Optional<SanPham> getById(Integer id);
    SanPham save(SanPham sanPham);
    void delete(Integer id);
    List<SanPhamSearchResponse> searchProducts(String keyword);
    List<SanPhamSearchResponse> getAllAvailableProducts();
    SanPhamSearchResponse findByQRCode(String qrCode);
}
