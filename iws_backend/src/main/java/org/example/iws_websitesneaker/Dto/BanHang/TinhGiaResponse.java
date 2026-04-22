package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TinhGiaResponse {
    private Integer chiTietSanPhamId;
    private Integer soLuong;
    private BigDecimal giaGoc;
    private BigDecimal giaBan;
    private BigDecimal tongTienGoc;
    private BigDecimal tongTienSauGiam;
    private BigDecimal tongTietKiem;
    private List<KhuyenMaiSanPhamResponse> danhSachKhuyenMai;
    private Float phanTramGiamTongCong;
}

