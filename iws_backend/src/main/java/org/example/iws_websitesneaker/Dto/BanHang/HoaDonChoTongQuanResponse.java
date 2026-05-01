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

    // Thông tin voucher
    private VoucherResponse voucher;
    private Boolean daApDungVoucher;

    // Thông tin khách hàng
    private KhachHangResponse khachHang;

    // Danh sách sản phẩm
    private List<HoaDonChoSanPhamResponse> danhSachSanPham;

    // Thông tin tính toán
    private Date ngayCapNhat;
    private String trangThai;
}

