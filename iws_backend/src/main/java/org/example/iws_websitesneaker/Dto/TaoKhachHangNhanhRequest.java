package org.example.iws_websitesneaker.Dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TaoKhachHangNhanhRequest {
    @NotBlank(message = "Họ tên không được để trống")
    private String hoTen;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại phải có 10 chữ số")
    private String sdt;

    private String email;
    private String diaChi;
}

