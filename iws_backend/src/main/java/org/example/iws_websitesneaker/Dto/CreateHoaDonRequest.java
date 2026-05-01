package org.example.iws_websitesneaker.Dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateHoaDonRequest {
    // Thông tin khách hàng
    private Integer khachHangId; // null cho guest
    private String tenNguoiDung;
    private String email;
    private String sdt;
    private String diaChi;
    private String ghiChu;

    // Thông tin đơn hàng
    private String maHoaDon; // Optional, sẽ tự generate nếu null
    private String phuongThucThanhToan; // COD, VNPAY
    private String loaiHoaDon; // ONLINE, OFFLINE
    private String trangThaiHoaDon; // CHO_XAC_NHAN, etc.

    // Thông tin tài chính
    private BigDecimal tongTien;
    private BigDecimal phiVanChuyen;
    private BigDecimal tongThanhToan;
    private Integer diemSuDung; // Điểm tích lũy sử dụng (thường 0 cho guest)
    private Double giaTriDiem; // Giá trị điểm quy đổi thành tiền

    // Thông tin voucher - BỔ SUNG
    private Integer voucherId; // ID voucher được áp dụng
    private String maVoucher; // Mã voucher (để hiển thị)
    private Double giaTriVoucher; // Số tiền giảm từ voucher

    // Chi tiết sản phẩm
    private List<ChiTietHoaDonRequest> chiTietSanPham;

    // Thông tin thời gian (optional - sẽ tự set trong service)
    private String ngayTao;
    private String ngayXacNhan;
    private String ngayGiaoHang;
    private String ngayHoanThanh;
    private String thoiGianVanChuyen;
}
