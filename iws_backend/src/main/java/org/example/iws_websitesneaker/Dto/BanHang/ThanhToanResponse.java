package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThanhToanResponse {
    private Integer hoaDonId;
    private String maHoaDon;
    private String trangThaiHoaDon;

    // ThÃ´ng tin thanh toÃ¡n
    private String phuongThucThanhToan;
    private BigDecimal tongTienCanThanhToan;
    private BigDecimal tienMat;
    private BigDecimal tienChuyenKhoan;
    private BigDecimal tienThua; // Tiá»n thá»«a khi thanh toÃ¡n tiá»n máº·t

    // ThÃ´ng tin voucher vÃ  Ä‘iá»ƒm
    private Integer voucherId;
    private String tenVoucher;
    private BigDecimal giaTriGiamVoucher;
    private Integer diemSuDung;
    private BigDecimal giaTriDiem; // GiÃ¡ trá»‹ tiá»n cá»§a Ä‘iá»ƒm Ä‘Ã£ sá»­ dá»¥ng

    // ThÃ´ng tin khÃ¡ch hÃ ng
    private Integer khachHangId;
    private String tenKhachHang;
    private String sdtKhachHang;

    // ThÃ´ng tin thá»i gian
    private Date ngayThanhToan;
    private Date ngayHoanThanh;

    // ThÃ´ng tin nhÃ¢n viÃªn
    private Integer nhanVienId;
    private String tenNhanVien;

    private String ghiChu;
    private String thongBaoThanhToan;
    private Boolean thanhCong;

    // THÃŠM FIELD Má»šI
    private List<InventoryCheckResponse> kiemTraTonKho; // Káº¿t quáº£ kiá»ƒm tra tá»“n kho
}
