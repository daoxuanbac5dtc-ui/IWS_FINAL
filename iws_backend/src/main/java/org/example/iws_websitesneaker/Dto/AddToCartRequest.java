package org.example.iws_websitesneaker.Dto;

import lombok.Data;

@Data
public class AddToCartRequest {
    private Integer productDetailId;
    private Integer soLuong;
    private Double gia;

    public AddToCartRequest() {}

    public AddToCartRequest(Integer productDetailId, Integer soLuong, Double gia) {
        this.productDetailId = productDetailId;
        this.soLuong = soLuong;
        this.gia = gia;
    }

    // Getters and Setters
    public Integer getProductDetailId() {
        return productDetailId;
    }

    public void setProductDetailId(Integer productDetailId) {
        this.productDetailId = productDetailId;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public Double getGia() {
        return gia;
    }

    public void setGia(Double gia) {
        this.gia = gia;
    }
}

