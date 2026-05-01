package org.example.iws_websitesneaker.Dto.BanHang;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TinhGiaRequest {
    @NotNull(message = "ID chi tiết sản phẩm không được để trống")
    private Integer chiTietSanPhamId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer soLuong;
}

