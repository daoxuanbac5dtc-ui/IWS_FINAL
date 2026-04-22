package org.example.iws_websitesneaker.Dto.BanHang;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapNhatSanPhamRequest {
    @NotNull(message = "Sá»‘ lÆ°á»£ng khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Min(value = 1, message = "Sá»‘ lÆ°á»£ng pháº£i lá»›n hÆ¡n 0")
    private Integer soLuong;

    private BigDecimal donGia;
    private String ghiChu;
}

