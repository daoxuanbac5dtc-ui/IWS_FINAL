package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;
import lombok.Builder;
// Sá»¬A: Import tá»« package BanHang thay vÃ¬ package gá»‘c
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

    // ThÃ´ng tin nhÃ¢n viÃªn
    private NhanVienResponse nhanVien;

    // ThÃ´ng tin khÃ¡ch hÃ ng
    private KhachHangResponse khachHang;

    // ThÃ´ng tin voucher
    private VoucherResponse voucher;
    private Boolean daApDungVoucher;

    // Danh sÃ¡ch sáº£n pháº©m
    private List<HoaDonChoSanPhamResponse> danhSachSanPham;

    // ThÃ´ng tin tá»•ng quan
    private HoaDonChoTongQuanResponse tongQuan;

    // Ghi chÃº
    private String ghiChu;
}
