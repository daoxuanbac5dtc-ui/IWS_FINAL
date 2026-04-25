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

    @NotNull(message = "PhÆ°Æ¡ng thá»©c thanh toÃ¡n khÃ´ng Ä‘Æ°á»£c trá»‘ng")
    private String phuongThucThanhToan; // TIEN_MAT, CHUYEN_KHOAN, KET_HOP

    private BigDecimal tienMat;
    private BigDecimal tienChuyenKhoan;
    private Integer diemSuDung;
    private String ghiChu;
    private String tenNguoiNhan;
    private String sdtNguoiNhan;
    private String diaChiGiaoHang;

    @NotNull(message = "Loáº¡i hÃ³a Ä‘Æ¡n khÃ´ng Ä‘Æ°á»£c trá»‘ng")
    private String loaiHoaDon = "OFFLINE"; // OFFLINE, ONLINE
}
