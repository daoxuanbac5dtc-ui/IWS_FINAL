package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonChoSanPhamResponse {
    private Integer id;
    private Integer chiTietSanPhamId;
    private String tenSanPham;
    private String maSanPham;
    private String mauSac;
    private String kichCo;
    private Integer soLuong;
    private BigDecimal giaGoc;
    private BigDecimal giaBan;
    private BigDecimal tongTienGoc;
    private BigDecimal tongTienSauGiam;
    private BigDecimal soTienTietKiem;
    private List<KhuyenMaiSanPhamResponse> khuyenMaiSanPham;
    private String hinhAnhChinh;
    private Float phanTramGiam;
}
