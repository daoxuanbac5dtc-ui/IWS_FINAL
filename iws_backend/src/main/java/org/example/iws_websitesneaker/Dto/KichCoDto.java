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
    @NotBlank(message = "KhÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng mÃ£ kÃ­ch cá»¡!")
    private String maKichCo;
    @NotBlank(message = "KhÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng tÃªn kÃ­ch cá»¡!")
    private String tenKichCo;
    @NotNull(message = "KhÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng tráº¡ng thÃ¡i kÃ­ch cá»¡!")
    private Integer trangThai;
}

