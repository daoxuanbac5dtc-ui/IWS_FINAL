package org.example.iws_websitesneaker.Dto.BanHang;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiaoHangRequest {

    @NotBlank(message = "TÃªn ngÆ°á»i nháº­n khÃ´ng Ä‘Æ°á»£c trá»‘ng")
    private String tenNguoiNhan;

    @NotBlank(message = "Sá»‘ Ä‘iá»‡n thoáº¡i khÃ´ng Ä‘Æ°á»£c trá»‘ng")
    @Pattern(regexp = "^[0-9]{10}$", message = "Sá»‘ Ä‘iá»‡n thoáº¡i pháº£i cÃ³ 10 chá»¯ sá»‘")
    private String sdt;

    private String email;

    @NotBlank(message = "Äá»‹a chá»‰ giao hÃ ng khÃ´ng Ä‘Æ°á»£c trá»‘ng")
    private String diaChi;

    private BigDecimal phiVanChuyen;

    private String ghiChu;

    // PhÆ°Æ¡ng thá»©c thanh toÃ¡n: COD, CHUYEN_KHOAN
    private String phuongThucThanhToan;
}
