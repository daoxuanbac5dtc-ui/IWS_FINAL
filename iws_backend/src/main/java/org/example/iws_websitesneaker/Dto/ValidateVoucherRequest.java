package org.example.iws_websitesneaker.Dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ValidateVoucherRequest {
    @NotBlank(message = "Mã voucher không được để trống")
    private String maVoucher;

    @NotNull(message = "Tổng tiền không được để trống")
    private Double tongTien;

    private Integer khachHangId;
}

