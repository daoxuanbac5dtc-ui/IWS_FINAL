package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;
import lombok.Builder;
// SỬA: Import từ package BanHang thay vì package gốc
import org.example.iws_websitesneaker.Dto.BanHang.KhachHangResponse;
import org.example.iws_websitesneaker.Dto.BanHang.NhanVienResponse;
import org.example.iws_websitesneaker.Dto.BanHang.VoucherResponse;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonChoDetailResponse {
    private Integer id;
    private String maHoaDon;
    private String trangThai;
    private Date ngayTao;
    private Date ngayCapNhat;

    // Thông tin nhân viên
    private NhanVienResponse nhanVien;

    // Thông tin khách hàng
    private KhachHangResponse khachHang;

    // Thông tin voucher
    private VoucherResponse voucher;
    private Boolean daApDungVoucher;

    // Danh sách sản phẩm
    private List<HoaDonChoSanPhamResponse> danhSachSanPham;

    // Thông tin tổng quan
    private HoaDonChoTongQuanResponse tongQuan;

    // Ghi chú
    private String ghiChu;
}
