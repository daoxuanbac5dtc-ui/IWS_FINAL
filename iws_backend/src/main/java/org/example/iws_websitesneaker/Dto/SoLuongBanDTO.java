package org.example.iws_websitesneaker.Dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SoLuongBanDTO {
    private Integer thoiGian;   // có thể là tháng hoặc tuần
    private Integer soLuong;    // tổng số lượng sản phẩm bán
}
