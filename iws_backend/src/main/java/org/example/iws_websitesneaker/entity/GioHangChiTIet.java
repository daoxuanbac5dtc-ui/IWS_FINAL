    package org.example.iws_websitesneaker.entity;
    import jakarta.persistence.*;
    import lombok.Data;

    import java.util.Date;

    @Data
    @Entity
    @Table(name = "gio_hang_chi_tiet")
    public class GioHangChiTIet {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @ManyToOne
        @JoinColumn(name = "id_gio_hang", nullable = false)
        private GioHang gioHang;

        @ManyToOne
        @JoinColumn(name = "id_ctsp", nullable = false)
        private ChiTietSanPham chiTietSanPham;

        @Column(name = "ma_gio_hang_chi_tiet", nullable = false, length = 25)
        private String maGioHangChiTiet;

        @Column(name = "ngay_tao")
        @Temporal(TemporalType.TIMESTAMP)
        private Date ngayTao;

        @Column(name = "ngay_cap_nhat")
        @Temporal(TemporalType.TIMESTAMP)
        private Date ngayCapNhat;

        @Column(name = "gia", nullable = false)
        private Double gia;

        @Column(name = "so_luong", nullable = false)
        private Integer soLuong;

        @Column(name = "trang_thai_hoa_don", length = 50)
        private String trangThaiHoaDon;

        public GioHangChiTIet(Integer id, GioHang gioHang, ChiTietSanPham chiTietSanPham, String maGioHangChiTiet, Date ngayTao, Date ngayCapNhat, Double gia, Integer soLuong, String trangThaiHoaDon) {
            this.id = id;
            this.gioHang = gioHang;
            this.chiTietSanPham = chiTietSanPham;
            this.maGioHangChiTiet = maGioHangChiTiet;
            this.ngayTao = ngayTao;
            this.ngayCapNhat = ngayCapNhat;
            this.gia = gia;
            this.soLuong = soLuong;
            this.trangThaiHoaDon = trangThaiHoaDon;
        }

        public GioHangChiTIet() {
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public GioHang getGioHang() {
            return gioHang;
        }

        public void setGioHang(GioHang gioHang) {
            this.gioHang = gioHang;
        }

        public ChiTietSanPham getChiTietSanPham() {
            return chiTietSanPham;
        }

        public void setChiTietSanPham(ChiTietSanPham chiTietSanPham) {
            this.chiTietSanPham = chiTietSanPham;
        }

        public String getMaGioHangChiTiet() {
            return maGioHangChiTiet;
        }

        public void setMaGioHangChiTiet(String maGioHangChiTiet) {
            this.maGioHangChiTiet = maGioHangChiTiet;
        }

        public Date getNgayTao() {
            return ngayTao;
        }

        public void setNgayTao(Date ngayTao) {
            this.ngayTao = ngayTao;
        }

        public Date getNgayCapNhat() {
            return ngayCapNhat;
        }

        public void setNgayCapNhat(Date ngayCapNhat) {
            this.ngayCapNhat = ngayCapNhat;
        }

        public Double getGia() {
            return gia;
        }

        public void setGia(Double gia) {
            this.gia = gia;
        }

        public Integer getSoLuong() {
            return soLuong;
        }

        public void setSoLuong(Integer soLuong) {
            this.soLuong = soLuong;
        }

        public String getTrangThaiHoaDon() {
            return trangThaiHoaDon;
        }

        public void setTrangThaiHoaDon(String trangThaiHoaDon) {
            this.trangThaiHoaDon = trangThaiHoaDon;
        }
    }


