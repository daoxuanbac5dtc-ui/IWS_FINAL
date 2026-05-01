package org.example.iws_websitesneaker.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KhuyenMaiDto {
    private Integer id;
    @NotBlank(message = "Không để trống mã khuyến mãi")
    private String maKhuyenMai;
    @NotBlank(message = "Không để trống tên khuyến mãi")
    private String tenKhuyenMai;
    @NotNull(message = "Không để trống ngày bắt đầu khuyến mãi")
    private Date ngayBatDau;
    @NotNull(message = "Không để trống ngày kết thúc khuyến mãi")
    private Date ngayKetThuc;
    @NotNull(message = "Không để trống trạng thái khuyến mãi")
    private Integer trangThai;
    @NotNull(message = "Không để trống giá trị giảm khuyến mãi")
    private Float giaTri;
}

