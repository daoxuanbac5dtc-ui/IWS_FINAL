package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KhuyenMaiSanPhamResponse {
    private Integer id;
    private String maKhuyenMai;
    private String tenKhuyenMai;
    private Date ngayBatDau;
    private Date ngayKetThuc;
    private Float giaTri;
    private String loaiKhuyenMai;
    private Integer trangThai;
    private BigDecimal soTienGiam;
    private Boolean dangApDung;
}

