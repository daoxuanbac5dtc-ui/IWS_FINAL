package org.example.iws_websitesneaker.Dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherValidationResponse {
    private Boolean valid;
    private String message;
    private VoucherResponse voucher;
    private Double giaTriGiam;
    private String lyDoKhongHopLe;
}
