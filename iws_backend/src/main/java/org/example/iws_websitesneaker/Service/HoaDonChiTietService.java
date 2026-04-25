package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.Dto.HoaDonChiTietDTO;
import org.example.iws_websitesneaker.entity.HoaDon;
import org.example.iws_websitesneaker.entity.HoaDonChiTiet;

import java.util.List;

public interface HoaDonChiTietService {
    List<HoaDonChiTietDTO> getChiTietByHoaDonId(Integer hoaDonId);
    HoaDonChiTietDTO updateQuantity(Integer id, Integer soLuong);
    void removeProduct(Integer id);
    HoaDonChiTietDTO getById(Integer id);
    HoaDonChiTietDTO updateProduct(Integer id, HoaDonChiTietDTO dto);
    HoaDonChiTiet save(HoaDonChiTiet hoaDonChiTiet);
}
