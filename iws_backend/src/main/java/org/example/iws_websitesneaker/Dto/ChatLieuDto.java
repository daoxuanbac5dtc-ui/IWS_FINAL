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

    @NotBlank(message = "KhÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng mÃ£ cháº¥t liá»‡u! ")
    private String maChatLieu;

    @NotBlank(message = "KhÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng tÃªn cháº¥t liá»‡u! ")
    private String tenChatLieu;

    @NotNull(message = "KhÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng tráº¡ng thÃ¡i cá»§a cháº¥t liá»‡u! ")
    private Integer trangThai;
}

