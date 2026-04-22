package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamChiTietBanHangResponse {
    private Integer id;
    private String maChiTiet;
    private String maQR;
    private String tenSanPham;
    private Integer soLuong;
    private BigDecimal giaGoc;
    private BigDecimal giaBan;
    private BigDecimal giaKhuyenMai;
    private Integer trangThai;
    private Date ngayTao;
    private Date ngayCapNhat;
    private String maSanPham;

    // ThÃ´ng tin sáº£n pháº©m vÃ  thuá»™c tÃ­nh
    private SanPhamInfoResponse sanPham;
    private MauSacResponse mauSac;
    private KichCoResponse kichCo;
    private ThuongHieuResponse thuongHieu;
    private DanhMucResponse danhMuc;
    private ChatLieuResponse chatLieu;
    private DeGiayResponse deGiay;

    // Sá»¬A: Thay Ä‘á»•i cáº¥u trÃºc hÃ¬nh áº£nh Ä‘á»ƒ giá»‘ng quáº£n lÃ½ sáº£n pháº©m
    private List<HinhAnhResponse> danhSachHinhAnh; // THAY Äá»”I: tá»« hinhAnh thÃ nh danhSachHinhAnh
    private String hinhAnhChinh; // THÃŠM: URL cá»§a hÃ¬nh áº£nh chÃ­nh

    // ThÃ´ng tin khuyáº¿n mÃ£i
    private List<KhuyenMaiSanPhamResponse> danhSachKhuyenMai;
    private BigDecimal tongTietKiem;
    private Float phanTramGiam;
    private Boolean coKhuyenMai;

    // ThÃ´ng tin tá»“n kho
    private Boolean conHang;
    private String tinhTrangKho;
}
