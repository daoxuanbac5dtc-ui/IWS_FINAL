package org.example.iws_websitesneaker.Dto.BanHang;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApDungVoucherRequest {
    @NotNull(message = "Voucher ID không được để trống")
    private Integer voucherId;

    private Integer khachHangId;

    @NotNull(message = "Tổng tiền không được để trống")
    private Double tongTien;
}

