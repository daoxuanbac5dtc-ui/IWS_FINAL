package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonChoResponse {
    private Integer id;
    private String maHoaDon;
    private Date ngayTao;
    private BigDecimal tongTien;
    private Integer soLuongSanPham;
    private String trangThai;
    private KhachHangResponse khachHang;
    private List<HoaDonChiTietResponse> chiTiets;
}
