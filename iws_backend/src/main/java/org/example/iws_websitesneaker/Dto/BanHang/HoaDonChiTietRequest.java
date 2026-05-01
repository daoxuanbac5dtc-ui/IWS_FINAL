package org.example.iws_websitesneaker.Dto.BanHang;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonChiTietRequest {
    @NotNull(message = "ID chi tiết sản phẩm không được để trống")
    private Integer chiTietSanPhamId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer soLuong;

    @NotNull(message = "Đơn giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Đơn giá phải lớn hơn 0")
    private Double donGia;

    private String ghiChu;
}

