package org.example.iws_websitesneaker.Dto.BanHang;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonRequest {
    @NotBlank(message = "Tên khách không được để trống")
    private String tenKhach;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại không hợp lệ")
    private String sdt;

    @Email(message = "Email không hợp lệ")
    private String email;

    private String diaChi;
    private String ghiChu;

    @NotNull(message = "Tổng tiền không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Tổng tiền phải lớn hơn 0")
    private Double tongTien;

    private Integer khachHangId;
    private Integer nhanVienId;
    private Integer voucherId;

    @NotEmpty(message = "Danh sách chi tiết không được để trống")
    private List<HoaDonChiTietRequest> chiTiets;
}

