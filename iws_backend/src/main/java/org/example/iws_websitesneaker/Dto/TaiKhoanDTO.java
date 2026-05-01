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

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String matKhau;

    private String vaiTroString;
    private TaiKhoan.VaiTro vaiTro;
    private Integer trangThai = 1;

    // Thông tin cá nhân
    private String hoTen;
    private String sdt;
    private LocalDate ngaySinh;
    private String chucVu;

    // FIXED: Địa chỉ flat fields
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
     * Setter cho vai trò từ JSON với nhiều format khả dụng
     */
    @JsonProperty("vaiTro")
    public void setVaiTroFromJson(String vaiTroString) {
        System.out.println("🔄 Receiving vaiTro from JSON: " + vaiTroString);

        this.vaiTroString = vaiTroString;
        if (vaiTroString != null && !vaiTroString.trim().isEmpty()) {
            try {
                this.vaiTro = parseVaiTroString(vaiTroString.trim());
                System.out.println("✅ Parsed vaiTro: " + this.vaiTro);
            } catch (IllegalArgumentException e) {
                System.err.println("❌ Invalid role string: " + vaiTroString);
                this.vaiTro = null;
            }
        }
    }

    /**
     * Setter cho vaiTroString với auto-parse
     */
    public void setVaiTroString(String vaiTroString) {
        System.out.println("🔄 Setting vaiTroString: " + vaiTroString);

        this.vaiTroString = vaiTroString;
        if (vaiTroString != null && !vaiTroString.trim().isEmpty()) {
            try {
                this.vaiTro = parseVaiTroString(vaiTroString.trim());
                System.out.println("✅ Auto-parsed vaiTro: " + this.vaiTro);
            } catch (IllegalArgumentException e) {
                System.err.println("❌ Invalid role string: " + vaiTroString);
                this.vaiTro = null;
            }
        }
    }

    /**
     * FIXED: Parse vai trò từ string với nhiều format
     */
    private TaiKhoan.VaiTro parseVaiTroString(String roleStr) {
        if (roleStr == null || roleStr.trim().isEmpty()) {
            return null;
        }

        String upperRole = roleStr.trim().toUpperCase();
        System.out.println("🔍 Parsing role: '" + roleStr + "' -> '" + upperRole + "'");

        switch (upperRole) {
            case "USER":
            case "KHACHHANG":
            case "KHÁCH HÀNG":
            case "CUSTOMER":
                return TaiKhoan.VaiTro.USER;

            case "NHANVIEN":
            case "NHÂN VIÊN":
            case "EMPLOYEE":
            case "STAFF":
                return TaiKhoan.VaiTro.NHANVIEN;

            case "ADMIN":
            case "ADMINISTRATOR":
            case "QUẢN TRỊ":
            case "QUẢN_TRỊ":
                return TaiKhoan.VaiTro.ADMIN;

            default:
                System.err.println("❌ Unknown role: " + roleStr);
                throw new IllegalArgumentException("Unknown role: " + roleStr);
        }
    }

    // ===== UTILITY METHODS =====

    /**
     * Kiểm tra có dữ liệu địa chỉ không
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
     * Lấy địa chỉ hiệu lực (ưu tiên nested object, fallback flat fields)
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
     * Kiểm tra có phải Admin không
     */
    public boolean isAdmin() {
        return this.vaiTro == TaiKhoan.VaiTro.ADMIN;
    }

    /**
     * Kiểm tra có phải User không
     */
    public boolean isUser() {
        return this.vaiTro == TaiKhoan.VaiTro.USER;
    }

    /**
     * Kiểm tra có phải Employee không
     */
    public boolean isEmployee() {
        return this.vaiTro == TaiKhoan.VaiTro.NHANVIEN;
    }

    /**
     * Kiểm tra có cần thông tin cá nhân không
     */
    public boolean needsPersonalInfo() {
        return !isAdmin();
    }

    // ===== VALIDATION METHODS =====

    public boolean isValidBasicInfo() {
        boolean valid = email != null && !email.trim().isEmpty() &&
                matKhau != null && !matKhau.trim().isEmpty() &&
                vaiTro != null;

        System.out.println("🔍 Basic info validation: " + valid);
        System.out.println("  - Email: " + (email != null ? "✓" : "✗"));
        System.out.println("  - Password: " + (matKhau != null ? "✓" : "✗"));
        System.out.println("  - Role: " + vaiTro);

        return valid;
    }

    public boolean isValidPersonalInfo() {
        if (isAdmin()) {
            return true;
        }

        boolean valid = hoTen != null && !hoTen.trim().isEmpty() &&
                sdt != null && !sdt.trim().isEmpty();

        System.out.println("🔍 Personal info validation: " + valid);
        System.out.println("  - HoTen: " + (hoTen != null ? "✓" : "✗"));
        System.out.println("  - SDT: " + (sdt != null ? "✓" : "✗"));

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

        System.out.println("🔍 Overall validation:");
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
