package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.entity.HinhAnh;

import java.util.List;
import java.util.Optional;

public interface HinhAnhService {
    // Láº¥y táº¥t cáº£ cÃ¡c hÃ¬nh áº£nh
    List<HinhAnh> getAllHinhanh();
    // Detail
    Optional<HinhAnh> getHinhanhById(int id);
    //ThÃªm
    void addHinhAnh(HinhAnh hinhAnh);
    // Sá»­a
    void updateHinhAnh(HinhAnh hinhAnh);
    //XÃ³a
    void deleteHinhAnh(int id);

    Optional<HinhAnh> findByTenHinhAnh(String tenHinhAnh);

    // TÃ¬m hÃ¬nh áº£nh theo ID chi tiáº¿t sáº£n pháº©m
//    List<HinhAnh> findByChiTietSanPhamId(Integer chiTietSanPhamId);
}

