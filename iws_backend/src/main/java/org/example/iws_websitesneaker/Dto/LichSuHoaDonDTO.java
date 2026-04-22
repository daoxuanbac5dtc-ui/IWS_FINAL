package org.example.iws_websitesneaker.Dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class LichSuHoaDonDTO {
    private Integer id;
    private String moTaHanhDong;
    private String trangThaiHoaDon;
    private String tenNhanVien;
    private Integer nhanVienId;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private Date ngayTao;

    public LichSuHoaDonDTO() {}

    // Getters vÃ  Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getMoTaHanhDong() { return moTaHanhDong; }
    public void setMoTaHanhDong(String moTaHanhDong) { this.moTaHanhDong = moTaHanhDong; }

    public String getTrangThaiHoaDon() { return trangThaiHoaDon; }
    public void setTrangThaiHoaDon(String trangThaiHoaDon) { this.trangThaiHoaDon = trangThaiHoaDon; }

    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }

    public Integer getNhanVienId() { return nhanVienId; }
    public void setNhanVienId(Integer nhanVienId) { this.nhanVienId = nhanVienId; }

    public Date getNgayTao() { return ngayTao; }
    public void setNgayTao(Date ngayTao) { this.ngayTao = ngayTao; }
}
