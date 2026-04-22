package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViDiemResponse {
    private Integer id;
    private Double tongDiem;
    private Double soLuongDaDung;
    private Double soLuongDaCong;
    private Double giaTriDiem;
    private Date ngayTao;
    private Date ngayCapNhat;

    // ThÃ´ng tin bá»• sung
    private Double diemKhaDung;
}

