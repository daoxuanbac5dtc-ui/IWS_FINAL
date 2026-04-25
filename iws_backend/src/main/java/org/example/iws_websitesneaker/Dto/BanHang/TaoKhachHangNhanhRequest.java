package org.example.iws_websitesneaker.Dto.BanHang;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaoKhachHangNhanhRequest {
    @NotBlank(message = "Há» tÃªn khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String hoTen;

    @NotBlank(message = "Sá»‘ Ä‘iá»‡n thoáº¡i khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Pattern(regexp = "^[0-9]{10}$", message = "Sá»‘ Ä‘iá»‡n thoáº¡i pháº£i cÃ³ 10 chá»¯ sá»‘")
    private String sdt;

    @Email(message = "Email khÃ´ng há»£p lá»‡")
    private String email;

    private String diaChi;
    private String ghiChu;
}

