package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamChiTietFilterRequest {
    private String keyword;
    private Integer danhMucId;
    private Integer thuongHieuId;
    private Integer mauSacId;
    private Integer kichCoId;
    private Integer chatLieuId;
    private Integer deGiayId;
    private Double minPrice;
    private Double maxPrice;
}
