package org.example.iws_websitesneaker.Dto.BanHang;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonRequest {
    @NotBlank(message = "TÃªn khÃ¡ch khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String tenKhach;

    @NotBlank(message = "Sá»‘ Ä‘iá»‡n thoáº¡i khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Pattern(regexp = "^[0-9]{10}$", message = "Sá»‘ Ä‘iá»‡n thoáº¡i khÃ´ng há»£p lá»‡")
    private String sdt;

    @Email(message = "Email khÃ´ng há»£p lá»‡")
    private String email;

    private String diaChi;
    private String ghiChu;

    @NotNull(message = "Tá»•ng tiá»n khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @DecimalMin(value = "0.0", inclusive = false, message = "Tá»•ng tiá»n pháº£i lá»›n hÆ¡n 0")
    private Double tongTien;

    private Integer khachHangId;
    private Integer nhanVienId;
    private Integer voucherId;

    @NotEmpty(message = "Danh sÃ¡ch chi tiáº¿t khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private List<HoaDonChiTietRequest> chiTiets;
}

