package org.example.iws_websitesneaker.Dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ThemSanPhamRequest {
    @NotNull(message = "Chi tiết sản phẩm ID không được để trống")
    private Integer chiTietSanPhamId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer soLuong;

    private Double donGia; // Nếu muốn override giá
}

