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
    @NotBlank(message = "KhÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng mÃ£ Ä‘áº¿ giÃ y! ")
    private String maDeGiay;
    @NotBlank(message = "KhÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng tÃªn Ä‘áº¿ giÃ y! ")
    private String tenDeGiay;
    @NotNull(message = "KhÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng tráº¡ng thÃ¡i Ä‘áº¿ giÃ y! ")
    private Integer trangThai;
}

