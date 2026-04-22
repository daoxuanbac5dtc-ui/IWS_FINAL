package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanQRResponse {
    private Integer chiTietSanPhamId;
    private String maChiTiet;
    private String maQR;
    private String tenSanPham;
    private String mauSac;
    private String kichCo;
    private Integer soLuongTon;
    private BigDecimal giaGoc;
    private BigDecimal giaBan;
    private String hinhAnhChinh;
    private Boolean conHang;
    private String tinhTrangKho;
    private List<KhuyenMaiSanPhamResponse> danhSachKhuyenMai;
}
