package org.example.iws_websitesneaker.Dto.BanHang;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickAddToCartRequest {
    @NotNull(message = "ID hóa đơn chờ không được để trống")
    private Integer hoaDonChoId;

    @NotBlank(message = "Mã QR không được để trống")
    private String qrCode;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer soLuong;
}

