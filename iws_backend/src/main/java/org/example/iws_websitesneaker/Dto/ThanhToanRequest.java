package org.example.iws_websitesneaker.Dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ThanhToanRequest {
    @NotBlank(message = "PhÆ°Æ¡ng thá»©c thanh toÃ¡n khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String phuongThucThanhToan; // TIEN_MAT, CHUYEN_KHOAN

    private Double tienNhan; // Chá»‰ cáº§n thiáº¿t khi thanh toÃ¡n tiá»n máº·t

    private String ghiChu;

    @NotNull(message = "KhÃ¡ch hÃ ng ID khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Integer khachHangId;

    private Integer voucherId; // TÃ¹y chá»n

    private Integer diemSuDung; // TÃ¹y chá»n
}
