package org.example.iws_websitesneaker.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MauSacDto {
    private Integer id;
    @NotBlank(message = "Không được để trống mã màu sắc ")
    private String maMauSac;
    @NotBlank(message = "Không được để trống tên màu sắc ")
    private String tenMauSac;
    @NotNull(message = "Không được để trống trạng thái màu sắc ")
    private Integer trangThai;
}

