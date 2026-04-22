package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopSanPhamResponse {
    private Integer chiTietSanPhamId;
    private String tenSanPham;
    private String mauSac;
    private String kichCo;
    private Integer soLuongBan;
    private BigDecimal doanhThu;
    private String hinhAnhChinh;
    private Integer xepHang;
}

