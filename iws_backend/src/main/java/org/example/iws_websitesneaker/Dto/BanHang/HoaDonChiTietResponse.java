package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonChiTietResponse {
    private Integer id;
    private Integer chiTietSanPhamId;
    private String tenSanPham;
    private String mauSac;
    private String kichCo;
    private String thuongHieu;
    private Double donGia;
    private Integer soLuong;
    private Double thanhTien;
    private Integer soLuongTon;
    private String maChiTiet;
}

