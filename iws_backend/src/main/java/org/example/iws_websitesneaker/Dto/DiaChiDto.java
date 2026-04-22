    // ===== 1. Cáº¬P NHáº¬T DiaChiDto (GIá»® NGUYÃŠN TÃŠN) =====
    package org.example.iws_websitesneaker.Dto;

    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.NotBlank;

    public class DiaChiDto {
        private Integer id;

        @NotNull(message = "ID tÃ i khoáº£n khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng!")
        private Integer idTaiKhoan;


        private String maTinh;
        // Bá»Ž: private String maHuyen;
        private String maPhuong;

        @NotBlank(message = "TÃªn tá»‰nh/thÃ nh phá»‘ khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng!")
        private String tenTinh;
        // Bá»Ž: private String tenHuyen;

        @NotBlank(message = "TÃªn xÃ£/phÆ°á»ng khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng!")
        private String tenPhuong;

        private String tenKhachHang;
        private String diaChiChiTiet;
        private Integer trangThai;

        // Constructors
        public DiaChiDto() {}

        // Getters and Setters (GIá»® NGUYÃŠN TÃŠN)
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
            // Method Ä‘á»ƒ tÆ°Æ¡ng thÃ­ch
        }
    }
