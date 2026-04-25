package org.example.iws_websitesneaker.Dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class DiaChiResponse {
    private Integer id;
    private String tenTinh;
    private String tenHuyen;
    private String tenPhuong;
    private String hoTen;
    private String diaChiChiTiet;
    private String diaChiDayDu;
}
