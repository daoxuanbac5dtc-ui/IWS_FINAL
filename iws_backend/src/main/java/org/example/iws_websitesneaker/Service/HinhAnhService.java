package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.entity.HinhAnh;

import java.util.List;
import java.util.Optional;

public interface HinhAnhService {
    // Lấy tất cả các hình ảnh
    List<HinhAnh> getAllHinhanh();
    // Detail
    Optional<HinhAnh> getHinhanhById(int id);
    //Thêm
    void addHinhAnh(HinhAnh hinhAnh);
    // Sửa
    void updateHinhAnh(HinhAnh hinhAnh);
    //Xóa
    void deleteHinhAnh(int id);

    Optional<HinhAnh> findByTenHinhAnh(String tenHinhAnh);

    // Tìm hình ảnh theo ID chi tiết sản phẩm
//    List<HinhAnh> findByChiTietSanPhamId(Integer chiTietSanPhamId);
}

