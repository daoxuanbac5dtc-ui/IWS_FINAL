package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KichCoResponse {
    private Integer id;
    private String maKichCo;
    private String tenKichCo;
    private Integer trangThai;
    private Integer thuTu;          // ← THÊM field này (thứ tự sắp xếp)
    private Date ngayTao;           // ← THÊM field này
    private Date ngayCapNhat;
}
