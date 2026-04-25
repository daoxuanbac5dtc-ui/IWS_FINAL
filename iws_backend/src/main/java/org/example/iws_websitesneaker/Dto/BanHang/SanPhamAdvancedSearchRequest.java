package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamAdvancedSearchRequest {
    private String keyword;
    private List<Integer> danhMucIds;
    private List<Integer> thuongHieuIds;
    private List<Integer> mauSacIds;
    private List<Integer> kichCoIds;
    private List<Integer> chatLieuIds;
    private List<Integer> deGiayIds;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer minQuantity;
    private Integer maxQuantity;
    private Boolean coKhuyenMai;
    private Boolean conHang;
    private String sortBy;
    private String sortDirection;
}
