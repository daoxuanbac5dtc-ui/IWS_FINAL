package org.example.iws_websitesneaker.Dto;

import lombok.*;

@Data
public class InvoiceStatusUpdateRequest {
    private String trangThai;
    private String ghiChu;
    private String lyDoHuy;
    private Integer nhanVienId;

    // Constructors, getters vÃ  setters
}
