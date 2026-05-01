    // ===== 1. CẬP NHẬT DiaChiDto (GIỮ NGUYÊN TÊN) =====
    package org.example.iws_websitesneaker.Dto;

    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.NotBlank;

    public class DiaChiDto {
        private Integer id;

        @NotNull(message = "ID tài khoản không được để trống!")
        private Integer idTaiKhoan;


        private String maTinh;
        // BỎ: private String maHuyen;
        private String maPhuong;

        @NotBlank(message = "Tên tỉnh/thành phố không được để trống!")
        private String tenTinh;
        // BỎ: private String tenHuyen;

        @NotBlank(message = "Tên xã/phường không được để trống!")
        private String tenPhuong;

        private String tenKhachHang;
        private String diaChiChiTiet;
        private Integer trangThai;

        // Constructors
        public DiaChiDto() {}

        // Getters and Setters (GIỮ NGUYÊN TÊN)
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public Integer getIdTaiKhoan() { return idTaiKhoan; }
        public void setIdTaiKhoan(Integer idTaiKhoan) { this.idTaiKhoan = idTaiKhoan; }


        public String getMaTinh() { return maTinh; }
        public void setMaTinh(String maTinh) { this.maTinh = maTinh; }


        public String getMaPhuong() { return maPhuong; }
        public void setMaPhuong(String maPhuong) { this.maPhuong = maPhuong; }

        public String getTenTinh() { return tenTinh; }
        public void setTenTinh(String tenTinh) { this.tenTinh = tenTinh; }


        public String getTenPhuong() { return tenPhuong; }
        public void setTenPhuong(String tenPhuong) { this.tenPhuong = tenPhuong; }

        public String getTenKhachHang() { return tenKhachHang; }
        public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

        public String getDiaChiChiTiet() { return diaChiChiTiet; }
        public void setDiaChiChiTiet(String diaChiChiTiet) { this.diaChiChiTiet = diaChiChiTiet; }

        public Integer getTrangThai() { return trangThai; }
        public void setTrangThai(Integer trangThai) { this.trangThai = trangThai; }

        public void setIsDefault(boolean b) {
            // Method để tương thích
        }
    }
