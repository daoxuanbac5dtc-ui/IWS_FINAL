package org.example.iws_websitesneaker.Dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ValidateVoucherRequest {
    @NotBlank(message = "MÃ£ voucher khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String maVoucher;

    @NotNull(message = "Tá»•ng tiá»n khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Double tongTien;

    private Integer khachHangId;
}

