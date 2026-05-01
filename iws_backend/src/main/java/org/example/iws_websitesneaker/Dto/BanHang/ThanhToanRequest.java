package org.example.iws_websitesneaker.Dto.BanHang;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThanhToanRequest {
    private Integer khachHangId;
    private Integer voucherId;

    @NotNull(message = "Phương thức thanh toán không được trống")
    private String phuongThucThanhToan; // TIEN_MAT, CHUYEN_KHOAN, KET_HOP

    private BigDecimal tienMat;
    private BigDecimal tienChuyenKhoan;
    private Integer diemSuDung;
    private String ghiChu;
    private String tenNguoiNhan;
    private String sdtNguoiNhan;
    private String diaChiGiaoHang;

    @NotNull(message = "Loại hóa đơn không được trống")
    private String loaiHoaDon = "OFFLINE"; // OFFLINE, ONLINE
}
