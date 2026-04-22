package org.example.iws_websitesneaker.controller;

import lombok.RequiredArgsConstructor;
import org.example.iws_websitesneaker.Dto.DoanhThuThangDTO;
import org.example.iws_websitesneaker.Dto.SoLuongBanDTO;
import org.example.iws_websitesneaker.Dto.TopSanPhamDTO;
import org.example.iws_websitesneaker.Service.ThongKeService;
import org.example.iws_websitesneaker.repository.ThongKeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/thong-ke")
@RequiredArgsConstructor
public class ThongKeController {

    private final ThongKeService thongKeService;

    @GetMapping
    public Map<String, Object> getThongKe() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalRevenue", thongKeService.getTongDoanhThu());
        result.put("totalOrders", thongKeService.getTongDonHang());
        result.put("totalCustomers", thongKeService.getTongKhachHang());
        result.put("totalProducts", thongKeService.getTongSanPham());
        return result;
    }

    @GetMapping("/doanh-thu-thang")
    public ResponseEntity<List<DoanhThuThangDTO>> getDoanhThuThang() {
        return ResponseEntity.ok(thongKeService.getDoanhThuTheoThang());
    }

    @GetMapping("/top-ban-chay")
    public List<TopSanPhamDTO> getTopSanPhamBanChay(
            @RequestParam(defaultValue = "5") int top) {
        return thongKeService.getTopSanPhamBanChay(top);
    }

    @GetMapping("/trang-thai-don-hang")
    public List<Map<String, Object>> getThongKeDonHangTheoTrangThai() {
        return thongKeService.getThongKeDonHangTheoTrangThaiHoaDon();
    }

}

