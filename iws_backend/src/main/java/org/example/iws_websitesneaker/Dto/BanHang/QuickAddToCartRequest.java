package org.example.iws_websitesneaker.Dto.BanHang;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickAddToCartRequest {
    @NotNull(message = "ID hÃ³a Ä‘Æ¡n chá» khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Integer hoaDonChoId;

    @NotBlank(message = "MÃ£ QR khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String qrCode;

    @NotNull(message = "Sá»‘ lÆ°á»£ng khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Min(value = 1, message = "Sá»‘ lÆ°á»£ng pháº£i lá»›n hÆ¡n 0")
    private Integer soLuong;
}

