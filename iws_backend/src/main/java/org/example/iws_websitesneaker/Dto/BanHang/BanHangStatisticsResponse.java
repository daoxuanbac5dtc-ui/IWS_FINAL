package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BanHangStatisticsResponse {
    private Date ngay;
    private Integer soHoaDon;
    private BigDecimal doanhThu;
    private Integer soSanPhamBan;
    private BigDecimal doanhThuTrungBinh;
    private Integer soKhachHangMoi;
    private BigDecimal tongTienKhuyenMai;
    private BigDecimal tongTienVoucher;
}

