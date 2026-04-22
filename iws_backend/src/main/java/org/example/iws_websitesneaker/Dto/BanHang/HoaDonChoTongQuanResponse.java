package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;
import org.example.iws_websitesneaker.Dto.BanHang.KhachHangResponse;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonChoTongQuanResponse {
    private Integer id;
    private String maHoaDon;
    private Integer soLuongSanPham;
    private Integer tongSoLuong;
    private BigDecimal tongTienGoc;
    private BigDecimal tongTienKhuyenMai;
    private BigDecimal tongTienVoucher;
    private BigDecimal tongTienThanhToan;
    private BigDecimal tongTietKiem;
    private Float phanTramGiamTongCong;

    // ThÃ´ng tin voucher
    private VoucherResponse voucher;
    private Boolean daApDungVoucher;

    // ThÃ´ng tin khÃ¡ch hÃ ng
    private KhachHangResponse khachHang;

    // Danh sÃ¡ch sáº£n pháº©m
    private List<HoaDonChoSanPhamResponse> danhSachSanPham;

    // ThÃ´ng tin tÃ­nh toÃ¡n
    private Date ngayCapNhat;
    private String trangThai;
}

