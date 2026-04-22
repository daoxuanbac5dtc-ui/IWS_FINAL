package org.example.iws_websitesneaker.Dto;

public class ViDiemDto {
    private Integer id;
    private Double tongDiem;
    private Double soDiemDaDung;
    private Double soDiemDaCong;
    private Double giaTriDiem;

    // Constructors
    public ViDiemDto() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Double getTongDiem() { return tongDiem; }
    public void setTongDiem(Double tongDiem) { this.tongDiem = tongDiem; }

    public Double getSoDiemDaDung() { return soDiemDaDung; }
    public void setSoDiemDaDung(Double soDiemDaDung) { this.soDiemDaDung = soDiemDaDung; }

    public Double getSoDiemDaCong() { return soDiemDaCong; }
    public void setSoDiemDaCong(Double soDiemDaCong) { this.soDiemDaCong = soDiemDaCong; }

    public Double getGiaTriDiem() { return giaTriDiem; }
    public void setGiaTriDiem(Double giaTriDiem) { this.giaTriDiem = giaTriDiem; }
}

