package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TinhPhiShipResponse {
    private BigDecimal phiShip;
    private Boolean mienPhiShip;
    private String lyDoMienPhi;
    private BigDecimal tongTienSauShip;
}
