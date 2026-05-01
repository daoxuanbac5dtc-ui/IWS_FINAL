package org.example.iws_websitesneaker.Dto;

import jakarta.validation.constraints.*;
import lombok.Data;


@Data
public class CapNhatSanPhamRequest {
    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer soLuong;

    private Double donGia; // Nếu muốn update giá
}

