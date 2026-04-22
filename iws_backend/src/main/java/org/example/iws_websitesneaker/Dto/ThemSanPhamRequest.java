package org.example.iws_websitesneaker.Dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ThemSanPhamRequest {
    @NotNull(message = "Chi tiáº¿t sáº£n pháº©m ID khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Integer chiTietSanPhamId;

    @NotNull(message = "Sá»‘ lÆ°á»£ng khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Min(value = 1, message = "Sá»‘ lÆ°á»£ng pháº£i lá»›n hÆ¡n 0")
    private Integer soLuong;

    private Double donGia; // Náº¿u muá»‘n override giÃ¡
}

