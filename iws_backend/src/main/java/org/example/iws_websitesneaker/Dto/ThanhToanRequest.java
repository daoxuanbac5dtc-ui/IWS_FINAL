package org.example.iws_websitesneaker.Dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ThanhToanRequest {
    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String phuongThucThanhToan; // TIEN_MAT, CHUYEN_KHOAN

    private Double tienNhan; // Chỉ cần thiết khi thanh toán tiền mặt

    private String ghiChu;

    @NotNull(message = "Khách hàng ID không được để trống")
    private Integer khachHangId;

    private Integer voucherId; // Tùy chọn

    private Integer diemSuDung; // Tùy chọn
}
