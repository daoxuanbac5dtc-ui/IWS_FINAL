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
public class DanhMucDto {
    private Integer id;;
    @NotBlank(message = "KhÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng mÃ£ danh má»¥c ")
    private String maDanhMuc;
    @NotBlank(message = "KhÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng tÃªn danh má»¥c ")
    private String tenDanhMuc;
    @NotNull(message = "KhÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng tráº¡ng thÃ¡i danh má»¥c ")
    private Integer trangThai;
}

