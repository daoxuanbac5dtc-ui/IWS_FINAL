package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.example.iws_websitesneaker.Dto.NhanVienResponse;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NhanVienLoginResponse {
    private Boolean success;
    private String message;
    private NhanVienResponse nhanVien;
}
