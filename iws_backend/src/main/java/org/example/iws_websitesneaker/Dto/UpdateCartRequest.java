package org.example.iws_websitesneaker.Dto;

import lombok.Data;

@Data
public class UpdateCartRequest {
    private Integer soLuong;

    public UpdateCartRequest() {}

    public UpdateCartRequest(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }
}
