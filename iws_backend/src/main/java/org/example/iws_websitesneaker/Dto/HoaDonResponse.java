package org.example.iws_websitesneaker.Dto;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
public class HoaDonResponse {
    private Integer id;
    private String maHoaDon;
    private String tenKhach;
    private String sdt;
    private String email;
    private String diaChi;
    private String phuongThucThanhToan;
    private String trangThaiHoaDon;
    private String loaiHoaDon;
    private BigDecimal tongTien;
    private BigDecimal tongThanhToan;
    private Double tienGiamGia;
    private Integer diemSuDung;
    private Date ngayTao;
    private Date ngayHoanThanh;
    private String ghiChu;
    private NhanVienResponse nhanVien;
    private KhachHangResponse khachHang;
    private List<HoaDonChiTietResponse> chiTiets;
    private VoucherResponse voucher;
}
