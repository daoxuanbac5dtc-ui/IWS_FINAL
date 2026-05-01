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
public class DeGiayDto {

    private Integer id;;
    @NotBlank(message = "Không được để trống mã đế giày! ")
    private String maDeGiay;
    @NotBlank(message = "Không được để trống tên đế giày! ")
    private String tenDeGiay;
    @NotNull(message = "Không được để trống trạng thái đế giày! ")
    private Integer trangThai;
}

