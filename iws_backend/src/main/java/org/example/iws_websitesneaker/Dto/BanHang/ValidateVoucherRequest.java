package org.example.iws_websitesneaker.Dto.BanHang;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateVoucherRequest {
    @NotBlank(message = "MÃ£ voucher khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String maVoucher;

    private Integer khachHangId;

    @NotNull(message = "Tá»•ng tiá»n khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @DecimalMin(value = "0.0", inclusive = false, message = "Tá»•ng tiá»n pháº£i lá»›n hÆ¡n 0")
    private Double tongTien;
}

