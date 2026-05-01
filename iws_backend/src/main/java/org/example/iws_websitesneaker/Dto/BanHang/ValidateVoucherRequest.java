package org.example.iws_websitesneaker.Dto.BanHang;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateVoucherRequest {
    @NotBlank(message = "Mã voucher không được để trống")
    private String maVoucher;

    private Integer khachHangId;

    @NotNull(message = "Tổng tiền không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Tổng tiền phải lớn hơn 0")
    private Double tongTien;
}

