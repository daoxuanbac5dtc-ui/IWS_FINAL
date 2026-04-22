package org.example.iws_websitesneaker.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KhuyenMaiDto {
    private Integer id;
    @NotBlank(message = "KhÃ´ng Ä‘á»ƒ trá»‘ng mÃ£ khuyáº¿n mÃ£i")
    private String maKhuyenMai;
    @NotBlank(message = "KhÃ´ng Ä‘á»ƒ trá»‘ng tÃªn khuyáº¿n mÃ£i")
    private String tenKhuyenMai;
    @NotNull(message = "KhÃ´ng Ä‘á»ƒ trá»‘ng ngÃ y báº¯t Ä‘áº§u khuyáº¿n mÃ£i")
    private Date ngayBatDau;
    @NotNull(message = "KhÃ´ng Ä‘á»ƒ trá»‘ng ngÃ y káº¿t thÃºc khuyáº¿n mÃ£i")
    private Date ngayKetThuc;
    @NotNull(message = "KhÃ´ng Ä‘á»ƒ trá»‘ng tráº¡ng thÃ¡i khuyáº¿n mÃ£i")
    private Integer trangThai;
    @NotNull(message = "KhÃ´ng Ä‘á»ƒ trá»‘ng giÃ¡ trá»‹ giáº£m khuyáº¿n mÃ£i")
    private Float giaTri;
}

