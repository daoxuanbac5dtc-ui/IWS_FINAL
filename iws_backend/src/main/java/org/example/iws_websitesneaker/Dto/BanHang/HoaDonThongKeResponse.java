package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonThongKeResponse {
    private BigDecimal tongTienGoc;
    private BigDecimal tongTienKhuyenMai;
    private BigDecimal tongTienVoucher;
    private BigDecimal tongTietKiem;
    private Float phanTramGiamTongCong;
    private Integer tongSoSanPham;
    private Integer tongSoLuong;
}

