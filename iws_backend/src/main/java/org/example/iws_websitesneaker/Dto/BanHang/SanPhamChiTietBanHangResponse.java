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

    // Thông tin sản phẩm và thuộc tính
    private SanPhamInfoResponse sanPham;
    private MauSacResponse mauSac;
    private KichCoResponse kichCo;
    private ThuongHieuResponse thuongHieu;
    private DanhMucResponse danhMuc;
    private ChatLieuResponse chatLieu;
    private DeGiayResponse deGiay;

    // SỬA: Thay đổi cấu trúc hình ảnh để giống quản lý sản phẩm
    private List<HinhAnhResponse> danhSachHinhAnh; // THAY ĐỔI: từ hinhAnh thành danhSachHinhAnh
    private String hinhAnhChinh; // THÊM: URL của hình ảnh chính

    // Thông tin khuyến mãi
    private List<KhuyenMaiSanPhamResponse> danhSachKhuyenMai;
    private BigDecimal tongTietKiem;
    private Float phanTramGiam;
    private Boolean coKhuyenMai;

    // Thông tin tồn kho
    private Boolean conHang;
    private String tinhTrangKho;
}
