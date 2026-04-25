package org.example.iws_websitesneaker.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "tai_khoan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TaiKhoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_tai_khoan", length = 25, nullable = false, unique = true)
    private String maTaiKhoan;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "mat_khau", length = 255, nullable = false)
    private String matKhau;

    // FIXED: Sá»­ dá»¥ng ORDINAL Ä‘á»ƒ map vá»›i int trong database
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "vai_tro", nullable = false)
    private VaiTro vaiTro;

    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @Column(name = "ngay_tao")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayTao;

    @Column(name = "ngay_cap_nhat")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayCapNhat;

    // ===== VAI TRO ENUM - FIXED ORDER =====
    public enum VaiTro {
        USER(0, "KhÃ¡ch hÃ ng"),      // index 0 -> int 0 trong DB
        NHANVIEN(1, "NhÃ¢n viÃªn"),   // index 1 -> int 1 trong DB
        ADMIN(2, "Quáº£n trá»‹ viÃªn");  // index 2 -> int 2 trong DB

        private final int value;
        private final String displayName;

        VaiTro(int value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }

        public int getValue() {
            return value;
        }

        public String getDisplayName() {
            return displayName;
        }

        // Convert tá»« int trong database
        public static VaiTro fromValue(int value) {
            for (VaiTro role : VaiTro.values()) {
                if (role.getValue() == value) {
                    return role;
                }
            }
            throw new IllegalArgumentException("Invalid role value: " + value);
        }

        // Convert tá»« string
        public static VaiTro fromString(String str) {
            if (str == null || str.trim().isEmpty()) {
                return null;
            }

            String upperStr = str.trim().toUpperCase();
            switch (upperStr) {
                case "USER":
                case "KHACHHANG":
                case "KHÃCH HÃ€NG":
                case "CUSTOMER":
                    return USER;
                case "NHANVIEN":
                case "NHÃ‚N VIÃŠN":
                case "EMPLOYEE":
                case "STAFF":
                    return NHANVIEN;
                case "ADMIN":
                case "ADMINISTRATOR":
                case "QUáº¢N TRá»Š":
                case "QUáº¢N_TRá»Š":
                    return ADMIN;
                default:
                    throw new IllegalArgumentException("Invalid role: " + str);
            }
        }
    }

    // ===== LIFECYCLE CALLBACKS =====
    @PrePersist
    protected void onCreate() {
        Date now = new Date();
        if (this.ngayTao == null) {
            this.ngayTao = now;
        }
        if (this.ngayCapNhat == null) {
            this.ngayCapNhat = now;
        }
        if (this.trangThai == null) {
            this.trangThai = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.ngayCapNhat = new Date();
    }

    // ===== BUSINESS METHODS =====
    public boolean isActive() {
        return this.trangThai != null && this.trangThai == 1;
    }

    public boolean isAdmin() {
        return this.vaiTro == VaiTro.ADMIN;
    }

    public boolean isUser() {
        return this.vaiTro == VaiTro.USER;
    }

    public boolean isEmployee() {
        return this.vaiTro == VaiTro.NHANVIEN;
    }

    public String getRoleDisplayName() {
        return this.vaiTro != null ? this.vaiTro.getDisplayName() : "KhÃ´ng xÃ¡c Ä‘á»‹nh";
    }

    public String getStatusDisplayName() {
        return this.isActive() ? "Hoáº¡t Ä‘á»™ng" : "NgÆ°ng hoáº¡t Ä‘á»™ng";
    }

    // ===== STATIC FACTORY METHODS =====
    public static TaiKhoan createNew(String maTaiKhoan, String email, String matKhau, VaiTro vaiTro) {
        return TaiKhoan.builder()
                .maTaiKhoan(maTaiKhoan)
                .email(email.toLowerCase().trim())
                .matKhau(matKhau)
                .vaiTro(vaiTro)
                .trangThai(1)
                .ngayTao(new Date())
                .ngayCapNhat(new Date())
                .build();
    }

    public static TaiKhoan createAdmin(String maTaiKhoan, String email, String matKhau) {
        return createNew(maTaiKhoan, email, matKhau, VaiTro.ADMIN);
    }

    public static TaiKhoan createUser(String maTaiKhoan, String email, String matKhau) {
        return createNew(maTaiKhoan, email, matKhau, VaiTro.USER);
    }

    public static TaiKhoan createEmployee(String maTaiKhoan, String email, String matKhau) {
        return createNew(maTaiKhoan, email, matKhau, VaiTro.NHANVIEN);
    }

    // ===== VALIDATION METHODS =====
    public boolean hasValidEmail() {
        return this.email != null &&
                this.email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    public boolean isValidForSave() {
        return this.email != null && !this.email.trim().isEmpty() &&
                this.matKhau != null && !this.matKhau.trim().isEmpty() &&
                this.vaiTro != null &&
                this.trangThai != null &&
                this.maTaiKhoan != null && !this.maTaiKhoan.trim().isEmpty();
    }

    // ===== EQUALS & HASHCODE =====
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        TaiKhoan other = (TaiKhoan) obj;

        if (this.id != null && other.id != null) {
            return this.id.equals(other.id);
        }

        if (this.email != null && other.email != null) {
            return this.email.equals(other.email);
        }

        if (this.maTaiKhoan != null && other.maTaiKhoan != null) {
            return this.maTaiKhoan.equals(other.maTaiKhoan);
        }

        return false;
    }

    @Override
    public int hashCode() {
        if (this.id != null) {
            return this.id.hashCode();
        }
        if (this.email != null) {
            return this.email.hashCode();
        }
        if (this.maTaiKhoan != null) {
            return this.maTaiKhoan.hashCode();
        }
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "TaiKhoan{" +
                "id=" + id +
                ", maTaiKhoan='" + maTaiKhoan + '\'' +
                ", email='" + email + '\'' +
                ", vaiTro=" + vaiTro +
                ", trangThai=" + trangThai +
                ", ngayTao=" + ngayTao +
                ", ngayCapNhat=" + ngayCapNhat +
                '}';
    }
}
