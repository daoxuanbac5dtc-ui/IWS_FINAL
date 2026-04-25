package org.example.iws_websitesneaker.Dto.BanHang;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanQRRequest {
    @NotBlank(message = "QR Code khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String qrCode;
}

