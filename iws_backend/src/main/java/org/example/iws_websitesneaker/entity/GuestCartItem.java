package org.example.iws_websitesneaker.entity;

import java.util.Date;

public class GuestCartItem {
    private Integer productDetailId;
    private Integer quantity;
    private Date addedAt;

    public GuestCartItem(Integer productDetailId, Integer quantity) {
        this.productDetailId = productDetailId;
        this.quantity = quantity;
        this.addedAt = new Date();
    }

    // Getters and setters
    public Integer getProductDetailId() { return productDetailId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Date getAddedAt() { return addedAt; }
}

