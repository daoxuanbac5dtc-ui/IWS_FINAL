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
    @NotBlank(message = "KhÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng mÃ£ thÆ°Æ¡ng hiá»‡u")
    private  String maThuongHieu ;
    @NotBlank(message = "KhÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng tÃªn thÆ°Æ¡ng hiá»‡u")
    private  String tenThuongHieu ;
    // NotNull
    @NotNull(message = "KhÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng tráº¡ng thÃ¡i thÆ°Æ¡ng hiá»‡u")
    private  Integer trangThai ;
}

