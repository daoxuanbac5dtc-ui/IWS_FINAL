package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.entity.ChiTietSanPham;

import java.util.List;

public interface SanPhamChiTietService {
    List<ChiTietSanPham> getAll();
    ChiTietSanPham getById(Integer id);
    ChiTietSanPham save(ChiTietSanPham chiTietSanPham);
    ChiTietSanPham update(ChiTietSanPham chiTietSanPham, Integer id);
    boolean delete(Integer id);
    List<ChiTietSanPham> getBySanPhamId(Integer sanPhamId);
}
