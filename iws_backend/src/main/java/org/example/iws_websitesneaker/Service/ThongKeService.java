package org.example.iws_websitesneaker.Service;


import org.example.iws_websitesneaker.Dto.DoanhThuThangDTO;
import org.example.iws_websitesneaker.Dto.SoLuongBanDTO;
import org.example.iws_websitesneaker.Dto.TopSanPhamDTO;

import java.util.List;
import java.util.Map;

public interface ThongKeService {

    Integer getTongDoanhThu();

    Integer getTongDonHang();

    Integer getTongKhachHang();

    Integer getTongSanPham();

    List<DoanhThuThangDTO> getDoanhThuTheoThang();
    
    List<TopSanPhamDTO> getTopSanPhamBanChay(int top);

    List<Map<String, Object>> getThongKeDonHangTheoTrangThaiHoaDon();
}
