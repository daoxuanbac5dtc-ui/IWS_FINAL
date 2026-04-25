package org.example.iws_websitesneaker.Dto.BanHang;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApDungVoucherRequest {
    @NotNull(message = "Voucher ID khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Integer voucherId;

    private Integer khachHangId;

    @NotNull(message = "Tá»•ng tiá»n khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Double tongTien;
}

