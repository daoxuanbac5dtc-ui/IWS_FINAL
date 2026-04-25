package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonResponse {
    private Integer id;
    private String maHoaDon;
    private String tenKhach;
    private String sdt;
    private String email;
    private String diaChi;
    private String trangThaiHoaDon;
    private String loaiHoaDon;
    private BigDecimal tongTien;
    private BigDecimal tongThanhToan;
    private Integer diemSuDung;
    private Date ngayTao;
    private Date ngayHoanThanh;
    private String ghiChu;

    // ThÃ´ng tin liÃªn quan
    private NhanVienResponse nhanVien;
    private KhachHangResponse khachHang;
    private List<HoaDonChiTietResponse> chiTiets;
}

