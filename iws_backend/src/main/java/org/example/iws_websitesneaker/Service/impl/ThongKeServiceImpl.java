package org.example.iws_websitesneaker.Service.impl;

import lombok.RequiredArgsConstructor;
import org.example.iws_websitesneaker.Dto.DoanhThuThangDTO;
import org.example.iws_websitesneaker.Dto.SoLuongBanDTO;
import org.example.iws_websitesneaker.Dto.TopSanPhamDTO;
import org.example.iws_websitesneaker.repository.ThongKeRepository;
import org.example.iws_websitesneaker.Service.ThongKeService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThongKeServiceImpl implements ThongKeService {

    private final ThongKeRepository thongKeRepository;

    @Override
    public Integer getTongDoanhThu() {
        Double doanhThu = thongKeRepository.getTongDoanhThu();
        return doanhThu != null ? doanhThu.intValue() : 0;
    }

    @Override
    public Integer getTongDonHang() {
        Integer donHang = thongKeRepository.getTongDonHang();
        return donHang != null ? donHang : 0;
    }

    @Override
    public Integer getTongKhachHang() {
        Integer khachHang = thongKeRepository.getTongKhachHang();
        return khachHang != null ? khachHang : 0;
    }

    @Override
    public Integer getTongSanPham() {
        Integer sanPham = thongKeRepository.getTongSanPham();
        return sanPham != null ? sanPham : 0;
    }

//    @Override
//    public List<Object[]> getDoanhThuTheoThang() {
//        return thongKeRepository.getDoanhThuTheoThang();
//    }
    @Override
    public List<DoanhThuThangDTO> getDoanhThuTheoThang() {
        List<Object[]> results = thongKeRepository.getDoanhThuTheoThang();
        Map<Integer, Double> map = new HashMap<>();

        for (Object[] row : results) {
            Integer thang = ((Number) row[0]).intValue();
            Double doanhThu = ((Number) row[1]).doubleValue();
            map.put(thang, doanhThu);
        }

        List<DoanhThuThangDTO> dtos = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            dtos.add(new DoanhThuThangDTO(i, map.getOrDefault(i, 0.0)));
        }
        return dtos;
    }


    @Override
    public List<TopSanPhamDTO> getTopSanPhamBanChay(int top) {
        List<Object[]> results = thongKeRepository.findTopSanPhamBanChay(top);
        List<TopSanPhamDTO> list = new ArrayList<>();

        for (Object[] row : results) {
            TopSanPhamDTO dto = new TopSanPhamDTO(
                    (Integer) row[0],              // idSanPham
                    (String) row[1],               // maSanPham
                    (String) row[2],               // tenSanPham
                    ((Number) row[3]).longValue(), // tongSoLuongBan
                    ((Number) row[4]).doubleValue()// tongDoanhThu
            );
            list.add(dto);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> getThongKeDonHangTheoTrangThaiHoaDon() {
        return thongKeRepository.thongKeDonHangTheoTrangThaiHoaDon();
    }
}
