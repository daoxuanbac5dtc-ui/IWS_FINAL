package org.example.iws_websitesneaker.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatLieuDto {
    private Integer id;

    @NotBlank(message = "Không được để trống mã chất liệu! ")
    private String maChatLieu;

    @NotBlank(message = "Không được để trống tên chất liệu! ")
    private String tenChatLieu;

    @NotNull(message = "Không được để trống trạng thái của chất liệu! ")
    private Integer trangThai;
}

