package org.example.iws_websitesneaker.Dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class ViDiemResponse {
    private Integer id;
    private Double tongDiem;
    private Double soLuongDaDung;
    private Double soLuongDaCong;
    private Double giaTriDiem;
    private Double diemKhaDung;
}
