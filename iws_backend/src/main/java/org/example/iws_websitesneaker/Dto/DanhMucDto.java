package org.example.iws_websitesneaker.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DanhMucDto {
    private Integer id;;
    @NotBlank(message = "Không được để trống mã danh mục ")
    private String maDanhMuc;
    @NotBlank(message = "Không được để trống tên danh mục ")
    private String tenDanhMuc;
    @NotNull(message = "Không được để trống trạng thái danh mục ")
    private Integer trangThai;
}

