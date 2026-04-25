package org.example.iws_websitesneaker.Dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateHoaDonRequest {
    // ThÃ´ng tin khÃ¡ch hÃ ng
    private Integer khachHangId; // null cho guest
    private String tenNguoiDung;
    private String email;
    private String sdt;
    private String diaChi;
    private String ghiChu;

    // ThÃ´ng tin Ä‘Æ¡n hÃ ng
    private String maHoaDon; // Optional, sáº½ tá»± generate náº¿u null
    private String phuongThucThanhToan; // COD, VNPAY
    private String loaiHoaDon; // ONLINE, OFFLINE
    private String trangThaiHoaDon; // CHO_XAC_NHAN, etc.

    // ThÃ´ng tin tÃ i chÃ­nh
    private BigDecimal tongTien;
    private BigDecimal phiVanChuyen;
    private BigDecimal tongThanhToan;
    private Integer diemSuDung; // Äiá»ƒm tÃ­ch lÅ©y sá»­ dá»¥ng (thÆ°á»ng 0 cho guest)
    private Double giaTriDiem; // GiÃ¡ trá»‹ Ä‘iá»ƒm quy Ä‘á»•i thÃ nh tiá»n

    // ThÃ´ng tin voucher - Bá»” SUNG
    private Integer voucherId; // ID voucher Ä‘Æ°á»£c Ã¡p dá»¥ng
    private String maVoucher; // MÃ£ voucher (Ä‘á»ƒ hiá»ƒn thá»‹)
    private Double giaTriVoucher; // Sá»‘ tiá»n giáº£m tá»« voucher

    // Chi tiáº¿t sáº£n pháº©m
    private List<ChiTietHoaDonRequest> chiTietSanPham;

    // ThÃ´ng tin thá»i gian (optional - sáº½ tá»± set trong service)
    private String ngayTao;
    private String ngayXacNhan;
    private String ngayGiaoHang;
    private String ngayHoanThanh;
    private String thoiGianVanChuyen;
}
