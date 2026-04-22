package org.example.iws_websitesneaker.Dto;

import org.example.iws_websitesneaker.entity.TaiKhoan;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TaiKhoanDTO {
    private String maTaiKhoan;

    @NotBlank(message = "Email khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Email(message = "Email khÃ´ng há»£p lá»‡")
    private String email;

    @NotBlank(message = "Máº­t kháº©u khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Size(min = 6, message = "Máº­t kháº©u pháº£i cÃ³ Ã­t nháº¥t 6 kÃ½ tá»±")
    private String matKhau;

    private String vaiTroString;
    private TaiKhoan.VaiTro vaiTro;
    private Integer trangThai = 1;

    // ThÃ´ng tin cÃ¡ nhÃ¢n
    private String hoTen;
    private String sdt;
    private LocalDate ngaySinh;
    private String chucVu;

    // FIXED: Äá»‹a chá»‰ flat fields
    private String maTinh;
    private String maPhuong;
    private String tenTinh;
    private String tenPhuong;
    private String diaChiChiTiet;

    // Nested address object (optional)
    private DiaChiDto diaChi;

    // ===== CONSTRUCTORS =====
    public TaiKhoanDTO() {}

    // ===== ROLE HANDLING - FIXED =====

    /**
     * Setter cho vai trÃ² tá»« JSON vá»›i nhiá»u format kháº£ dá»¥ng
     */
    @JsonProperty("vaiTro")
    public void setVaiTroFromJson(String vaiTroString) {
        System.out.println("ðŸ”„ Receiving vaiTro from JSON: " + vaiTroString);

        this.vaiTroString = vaiTroString;
        if (vaiTroString != null && !vaiTroString.trim().isEmpty()) {
            try {
                this.vaiTro = parseVaiTroString(vaiTroString.trim());
                System.out.println("âœ… Parsed vaiTro: " + this.vaiTro);
            } catch (IllegalArgumentException e) {
                System.err.println("âŒ Invalid role string: " + vaiTroString);
                this.vaiTro = null;
            }
        }
    }

    /**
     * Setter cho vaiTroString vá»›i auto-parse
     */
    public void setVaiTroString(String vaiTroString) {
        System.out.println("ðŸ”„ Setting vaiTroString: " + vaiTroString);

        this.vaiTroString = vaiTroString;
        if (vaiTroString != null && !vaiTroString.trim().isEmpty()) {
            try {
                this.vaiTro = parseVaiTroString(vaiTroString.trim());
                System.out.println("âœ… Auto-parsed vaiTro: " + this.vaiTro);
            } catch (IllegalArgumentException e) {
                System.err.println("âŒ Invalid role string: " + vaiTroString);
                this.vaiTro = null;
            }
        }
    }

    /**
     * FIXED: Parse vai trÃ² tá»« string vá»›i nhiá»u format
     */
    private TaiKhoan.VaiTro parseVaiTroString(String roleStr) {
        if (roleStr == null || roleStr.trim().isEmpty()) {
            return null;
        }

        String upperRole = roleStr.trim().toUpperCase();
        System.out.println("ðŸ” Parsing role: '" + roleStr + "' -> '" + upperRole + "'");

        switch (upperRole) {
            case "USER":
            case "KHACHHANG":
            case "KHÃCH HÃ€NG":
            case "CUSTOMER":
                return TaiKhoan.VaiTro.USER;

            case "NHANVIEN":
            case "NHÃ‚N VIÃŠN":
            case "EMPLOYEE":
            case "STAFF":
                return TaiKhoan.VaiTro.NHANVIEN;

            case "ADMIN":
            case "ADMINISTRATOR":
            case "QUáº¢N TRá»Š":
            case "QUáº¢N_TRá»Š":
                return TaiKhoan.VaiTro.ADMIN;

            default:
                System.err.println("âŒ Unknown role: " + roleStr);
                throw new IllegalArgumentException("Unknown role: " + roleStr);
        }
    }

    // ===== UTILITY METHODS =====

    /**
     * Kiá»ƒm tra cÃ³ dá»¯ liá»‡u Ä‘á»‹a chá»‰ khÃ´ng
     */
    public boolean hasAddressData() {
        // Check flat fields
        boolean hasFlat = (maTinh != null && !maTinh.trim().isEmpty()) ||
                (maPhuong != null && !maPhuong.trim().isEmpty()) ||
                (tenTinh != null && !tenTinh.trim().isEmpty()) ||
                (tenPhuong != null && !tenPhuong.trim().isEmpty()) ||
                (diaChiChiTiet != null && !diaChiChiTiet.trim().isEmpty());

        // Check nested object
        boolean hasNested = diaChi != null && diaChi.isValid();

        return hasFlat || hasNested;
    }

    /**
     * Láº¥y Ä‘á»‹a chá»‰ hiá»‡u lá»±c (Æ°u tiÃªn nested object, fallback flat fields)
     */
    public DiaChiDto getEffectiveAddress() {
        // Priority 1: nested object
        if (diaChi != null && diaChi.isValid()) {
            return diaChi;
        }

        // Priority 2: flat fields
        if (hasAddressData()) {
            DiaChiDto effective = new DiaChiDto();
            effective.setMaTinh(this.maTinh);
            effective.setMaPhuong(this.maPhuong);
            effective.setTenTinh(this.tenTinh);
            effective.setTenPhuong(this.tenPhuong);
            effective.setDiaChiChiTiet(this.diaChiChiTiet);
            return effective;
        }

        return null;
    }

    /**
     * Kiá»ƒm tra cÃ³ pháº£i Admin khÃ´ng
     */
    public boolean isAdmin() {
        return this.vaiTro == TaiKhoan.VaiTro.ADMIN;
    }

    /**
     * Kiá»ƒm tra cÃ³ pháº£i User khÃ´ng
     */
    public boolean isUser() {
        return this.vaiTro == TaiKhoan.VaiTro.USER;
    }

    /**
     * Kiá»ƒm tra cÃ³ pháº£i Employee khÃ´ng
     */
    public boolean isEmployee() {
        return this.vaiTro == TaiKhoan.VaiTro.NHANVIEN;
    }

    /**
     * Kiá»ƒm tra cÃ³ cáº§n thÃ´ng tin cÃ¡ nhÃ¢n khÃ´ng
     */
    public boolean needsPersonalInfo() {
        return !isAdmin();
    }

    // ===== VALIDATION METHODS =====

    public boolean isValidBasicInfo() {
        boolean valid = email != null && !email.trim().isEmpty() &&
                matKhau != null && !matKhau.trim().isEmpty() &&
                vaiTro != null;

        System.out.println("ðŸ” Basic info validation: " + valid);
        System.out.println("  - Email: " + (email != null ? "âœ“" : "âœ—"));
        System.out.println("  - Password: " + (matKhau != null ? "âœ“" : "âœ—"));
        System.out.println("  - Role: " + vaiTro);

        return valid;
    }

    public boolean isValidPersonalInfo() {
        if (isAdmin()) {
            return true;
        }

        boolean valid = hoTen != null && !hoTen.trim().isEmpty() &&
                sdt != null && !sdt.trim().isEmpty();

        System.out.println("ðŸ” Personal info validation: " + valid);
        System.out.println("  - HoTen: " + (hoTen != null ? "âœ“" : "âœ—"));
        System.out.println("  - SDT: " + (sdt != null ? "âœ“" : "âœ—"));

        return valid;
    }

    public boolean isValidAddress() {
        DiaChiDto effectiveAddr = getEffectiveAddress();
        return effectiveAddr == null || effectiveAddr.isValid();
    }

    public boolean isValid() {
        boolean basicValid = isValidBasicInfo();
        boolean personalValid = isValidPersonalInfo();
        boolean addressValid = isValidAddress();

        System.out.println("ðŸ” Overall validation:");
        System.out.println("  - Basic: " + basicValid);
        System.out.println("  - Personal: " + personalValid);
        System.out.println("  - Address: " + addressValid);

        return basicValid && personalValid && addressValid;
    }

    // ===== GETTERS/SETTERS =====

    public String getMaTaiKhoan() { return maTaiKhoan; }
    public void setMaTaiKhoan(String maTaiKhoan) { this.maTaiKhoan = maTaiKhoan; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }

    public String getVaiTroString() { return vaiTroString; }

    public TaiKhoan.VaiTro getVaiTro() { return vaiTro; }
    public void setVaiTro(TaiKhoan.VaiTro vaiTro) {
        this.vaiTro = vaiTro;
        if (vaiTro != null) {
            this.vaiTroString = vaiTro.name();
        }
    }

    public Integer getTrangThai() { return trangThai; }
    public void setTrangThai(Integer trangThai) { this.trangThai = trangThai; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getChucVu() { return chucVu; }
    public void setChucVu(String chucVu) { this.chucVu = chucVu; }

    // ADDRESS GETTERS/SETTERS
    public String getMaTinh() { return maTinh; }
    public void setMaTinh(String maTinh) { this.maTinh = maTinh; }

    public String getMaPhuong() { return maPhuong; }
    public void setMaPhuong(String maPhuong) { this.maPhuong = maPhuong; }

    public String getTenTinh() { return tenTinh; }
    public void setTenTinh(String tenTinh) { this.tenTinh = tenTinh; }

    public String getTenPhuong() { return tenPhuong; }
    public void setTenPhuong(String tenPhuong) { this.tenPhuong = tenPhuong; }

    public String getDiaChiChiTiet() { return diaChiChiTiet; }
    public void setDiaChiChiTiet(String diaChiChiTiet) { this.diaChiChiTiet = diaChiChiTiet; }

    public DiaChiDto getDiaChi() { return diaChi; }
    public void setDiaChi(DiaChiDto diaChi) { this.diaChi = diaChi; }

    // ===== INNER CLASS - ADDRESS DTO =====

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DiaChiDto {
        private String maTinh;
        private String maPhuong;
        private String tenTinh;
        private String tenPhuong;
        private String diaChiChiTiet;

        public DiaChiDto() {}

        public boolean isValid() {
            return (tenTinh != null && !tenTinh.trim().isEmpty()) ||
                    (tenPhuong != null && !tenPhuong.trim().isEmpty()) ||
                    (maTinh != null && !maTinh.trim().isEmpty()) ||
                    (maPhuong != null && !maPhuong.trim().isEmpty());
        }

        // Getters/Setters
        public String getMaTinh() { return maTinh; }
        public void setMaTinh(String maTinh) { this.maTinh = maTinh; }

        public String getMaPhuong() { return maPhuong; }
        public void setMaPhuong(String maPhuong) { this.maPhuong = maPhuong; }

        public String getTenTinh() { return tenTinh; }
        public void setTenTinh(String tenTinh) { this.tenTinh = tenTinh; }

        public String getTenPhuong() { return tenPhuong; }
        public void setTenPhuong(String tenPhuong) { this.tenPhuong = tenPhuong; }

        public String getDiaChiChiTiet() { return diaChiChiTiet; }
        public void setDiaChiChiTiet(String diaChiChiTiet) { this.diaChiChiTiet = diaChiChiTiet; }

        @Override
        public String toString() {
            return "DiaChiDto{maTinh='" + maTinh + "', tenTinh='" + tenTinh +
                    "', maPhuong='" + maPhuong + "', tenPhuong='" + tenPhuong +
                    "', diaChiChiTiet='" + diaChiChiTiet + "'}";
        }
    }

    // ===== DEBUG STRING =====

    @Override
    public String toString() {
        return "TaiKhoanDTO{" +
                "email='" + email + "'" +
                ", vaiTro=" + vaiTro +
                ", vaiTroString='" + vaiTroString + "'" +
                ", hoTen='" + hoTen + "'" +
                ", sdt='" + sdt + "'" +
                ", hasAddressData=" + hasAddressData() +
                ", maTaiKhoan='" + maTaiKhoan + "'" +
                ", trangThai=" + trangThai +
                "}";
    }
}
