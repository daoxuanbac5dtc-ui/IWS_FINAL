package org.example.iws_websitesneaker.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HinhAnhDto {
    private Integer id;
    @NotBlank(message = "Không được để trống mã chi tiết sản phẩm! ")
    private String maChiTiet;
    @NotBlank(message = "Không được để trống mã hình ảnh!")
    private String maHinhAnh;
    @NotBlank(message = "Không được để trống tên hình ảnh!")
    private String tenHinhAnh;
    @NotNull(message = "Không được để trống trạng thái  hình ảnh!")
    private Integer trangThai;

}

