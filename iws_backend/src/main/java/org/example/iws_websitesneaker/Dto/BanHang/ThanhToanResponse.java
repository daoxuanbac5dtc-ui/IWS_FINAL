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

    // Thông tin thanh toán
    private String phuongThucThanhToan;
    private BigDecimal tongTienCanThanhToan;
    private BigDecimal tienMat;
    private BigDecimal tienChuyenKhoan;
    private BigDecimal tienThua; // Tiền thừa khi thanh toán tiền mặt

    // Thông tin voucher và điểm
    private Integer voucherId;
    private String tenVoucher;
    private BigDecimal giaTriGiamVoucher;
    private Integer diemSuDung;
    private BigDecimal giaTriDiem; // Giá trị tiền của điểm đã sử dụng

    // Thông tin khách hàng
    private Integer khachHangId;
    private String tenKhachHang;
    private String sdtKhachHang;

    // Thông tin thời gian
    private Date ngayThanhToan;
    private Date ngayHoanThanh;

    // Thông tin nhân viên
    private Integer nhanVienId;
    private String tenNhanVien;

    private String ghiChu;
    private String thongBaoThanhToan;
    private Boolean thanhCong;

    // THÊM FIELD MỚI
    private List<InventoryCheckResponse> kiemTraTonKho; // Kết quả kiểm tra tồn kho
}
