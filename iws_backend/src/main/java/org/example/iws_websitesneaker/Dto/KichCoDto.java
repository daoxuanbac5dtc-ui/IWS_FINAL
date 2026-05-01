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
public class KichCoDto {

    private Integer id;
    @NotBlank(message = "Không được để trống mã kích cỡ!")
    private String maKichCo;
    @NotBlank(message = "Không được để trống tên kích cỡ!")
    private String tenKichCo;
    @NotNull(message = "Không được để trống trạng thái kích cỡ!")
    private Integer trangThai;
}

