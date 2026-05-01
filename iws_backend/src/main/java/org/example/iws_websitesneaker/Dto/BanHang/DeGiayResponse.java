package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeGiayResponse {
    private Integer id;
    private String maDeGiay;
    private String tenDeGiay;
    private Integer trangThai;
    private Date ngayTao;           // ← THÊM field này
    private Date ngayCapNhat;
}

