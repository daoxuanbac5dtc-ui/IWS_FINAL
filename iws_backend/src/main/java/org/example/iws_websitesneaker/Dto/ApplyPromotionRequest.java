package org.example.iws_websitesneaker.Dto;

import lombok.Data;

import java.util.List;

@Data
public class ApplyPromotionRequest {
    private Integer khuyenMaiId;
    private List<Integer> chiTietSanPhamIds;

    public ApplyPromotionRequest(Integer khuyenMaiId, List<Integer> chiTietSanPhamIds) {
        this.khuyenMaiId = khuyenMaiId;
        this.chiTietSanPhamIds = chiTietSanPhamIds;
    }

    public ApplyPromotionRequest() {
    }

    public Integer getKhuyenMaiId() {
        return khuyenMaiId;
    }

    public void setKhuyenMaiId(Integer khuyenMaiId) {
        this.khuyenMaiId = khuyenMaiId;
    }

    public List<Integer> getChiTietSanPhamIds() {
        return chiTietSanPhamIds;
    }

    public void setChiTietSanPhamIds(List<Integer> chiTietSanPhamIds) {
        this.chiTietSanPhamIds = chiTietSanPhamIds;
    }
}
