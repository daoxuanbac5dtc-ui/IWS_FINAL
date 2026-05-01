package org.example.iws_websitesneaker.Dto.BanHang;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiaoHangRequest {

    @NotBlank(message = "Tên người nhận không được trống")
    private String tenNguoiNhan;

    @NotBlank(message = "Số điện thoại không được trống")
    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại phải có 10 chữ số")
    private String sdt;

    private String email;

    @NotBlank(message = "Địa chỉ giao hàng không được trống")
    private String diaChi;

    private BigDecimal phiVanChuyen;

    private String ghiChu;

    // Phương thức thanh toán: COD, CHUYEN_KHOAN
    private String phuongThucThanhToan;
}
