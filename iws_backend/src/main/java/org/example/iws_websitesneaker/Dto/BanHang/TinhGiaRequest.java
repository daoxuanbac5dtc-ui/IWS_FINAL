package org.example.iws_websitesneaker.Dto.BanHang;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TinhGiaRequest {
    @NotNull(message = "ID chi tiáº¿t sáº£n pháº©m khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Integer chiTietSanPhamId;

    @NotNull(message = "Sá»‘ lÆ°á»£ng khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Min(value = 1, message = "Sá»‘ lÆ°á»£ng pháº£i lá»›n hÆ¡n 0")
    private Integer soLuong;
}

