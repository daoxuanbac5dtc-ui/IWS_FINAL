package org.example.iws_websitesneaker.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ThuongHieuDto {

    private  Integer id ;
    @NotBlank(message = "Không được để trống mã thương hiệu")
    private  String maThuongHieu ;
    @NotBlank(message = "Không được để trống tên thương hiệu")
    private  String tenThuongHieu ;
    // NotNull
    @NotNull(message = "Không được để trống trạng thái thương hiệu")
    private  Integer trangThai ;
}

