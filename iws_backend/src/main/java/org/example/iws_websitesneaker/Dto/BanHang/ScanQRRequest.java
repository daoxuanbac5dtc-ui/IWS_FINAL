package org.example.iws_websitesneaker.Dto.BanHang;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanQRRequest {
    @NotBlank(message = "QR Code không được để trống")
    private String qrCode;
}

