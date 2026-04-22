package org.example.iws_websitesneaker.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "khach_hang")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vi_diem", referencedColumnName = "id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ViDiem viDiem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tai_khoan", referencedColumnName = "id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private TaiKhoan taiKhoan;

    @Column(name = "ma_khach_hang", length = 25, nullable = false, unique = true)
    private String maKhachHang;

    @Column(name = "ho_ten", length = 225, nullable = false)
    private String hoTen;

    @Column(name = "sdt", length = 10, nullable = false)
    private String sdt;

    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @Column(name = "ngay_tao")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayTao;

    @Column(name = "ngay_cap_nhat")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayCapNhat;

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
            this.trangThai = 1; // Máº·c Ä‘á»‹nh active
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.ngayCapNhat = new Date();
    }

    // ===== SAFE METHODS - KhÃ´ng gÃ¢y LazyInitializationException =====

    /**
     * Láº¥y email má»™t cÃ¡ch an toÃ n tá»« TaiKhoan
     */
    public String getEmailSafe() {
        try {
            return this.taiKhoan != null ? this.taiKhoan.getEmail() : null;
        } catch (Exception e) {
            System.err.println("Warning: Could not get email for customer " + this.id + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Kiá»ƒm tra khÃ¡ch hÃ ng cÃ³ Ä‘ang hoáº¡t Ä‘á»™ng khÃ´ng
     */
    public boolean isActive() {
        return this.trangThai != null && this.trangThai == 1;
    }

    /**
     * Kiá»ƒm tra profile Ä‘Ã£ hoÃ n thiá»‡n chÆ°a
     */
    public boolean isProfileCompleted() {
        return this.hoTen != null && !this.hoTen.trim().isEmpty() &&
                this.sdt != null && !this.sdt.trim().isEmpty() &&
                getEmailSafe() != null && !getEmailSafe().trim().isEmpty();
    }

    /**
     * Láº¥y tÃªn hiá»ƒn thá»‹
     */
    public String getDisplayName() {
        if (this.hoTen != null && !this.hoTen.trim().isEmpty()) {
            return this.hoTen;
        }
        String email = getEmailSafe();
        if (email != null && !email.trim().isEmpty()) {
            return email;
        }
        return this.maKhachHang != null ? this.maKhachHang : "KhÃ¡ch hÃ ng";
    }

    /**
     * Get display name cho tráº¡ng thÃ¡i
     */
    public String getStatusDisplayName() {
        return this.isActive() ? "Äang hoáº¡t Ä‘á»™ng" : "NgÆ°ng hoáº¡t Ä‘á»™ng";
    }

    // ===== BUSINESS LOGIC METHODS =====

    /**
     * Cáº­p nháº­t thÃ´ng tin cÆ¡ báº£n
     */
    public void updateBasicInfo(String hoTen, String sdt) {
        if (hoTen != null && !hoTen.trim().isEmpty()) {
            this.hoTen = hoTen.trim();
        }
        if (sdt != null && !sdt.trim().isEmpty()) {
            this.sdt = sdt.trim();
        }
        this.ngayCapNhat = new Date();
    }

    /**
     * Toggle tráº¡ng thÃ¡i
     */
    public void toggleStatus() {
        this.trangThai = this.trangThai == 1 ? 0 : 1;
        this.ngayCapNhat = new Date();
    }

    /**
     * Deactivate khÃ¡ch hÃ ng
     */
    public void deactivate() {
        this.trangThai = 0;
        this.ngayCapNhat = new Date();
    }

    /**
     * Reactivate khÃ¡ch hÃ ng
     */
    public void reactivate() {
        this.trangThai = 1;
        this.ngayCapNhat = new Date();
    }

    // ===== VALIDATION METHODS =====

    /**
     * Validate sá»‘ Ä‘iá»‡n thoáº¡i
     */
    public boolean hasValidPhoneNumber() {
        return this.sdt != null && this.sdt.matches("^0\\d{9,10}$");
    }

    /**
     * Validate há» tÃªn
     */
    public boolean hasValidName() {
        return this.hoTen != null &&
                this.hoTen.matches("^[a-zA-ZÃ€ÃÃ‚ÃƒÃˆÃ‰ÃŠÃŒÃÃ’Ã“Ã”Ã•Ã™ÃšÄ‚ÄÄ¨Å¨Æ Ã Ã¡Ã¢Ã£Ã¨Ã©ÃªÃ¬Ã­Ã²Ã³Ã´ÃµÃ¹ÃºÄƒÄ‘Ä©Å©Æ¡Æ¯Ä‚áº áº¢áº¤áº¦áº¨áºªáº¬áº®áº°áº²áº´áº¶áº¸áººáº¼á»€á»€á»‚Æ°Äƒáº¡áº£áº¥áº§áº©áº«áº­áº¯áº±áº³áºµáº·áº¹áº»áº½á»áº¿á»ƒá»„á»†á»ˆá»Šá»Œá»Žá»á»’á»”á»–á»˜á»šá»œá»žá» á»¢á»¤á»¦á»¨á»ªá»…á»‡á»‰á»‹á»á»á»‘á»“á»•á»—á»™á»›á»á»Ÿá»¡á»£á»¥á»§á»©á»«á»¬á»®á»°á»²á»´Ãá»¶á»¸á»­á»¯á»±á»³á»µÃ½á»·á»¹\\s]+$");
    }

    /**
     * Validate dá»¯ liá»‡u cÆ¡ báº£n
     */
    public boolean isValidForSave() {
        return this.hoTen != null && !this.hoTen.trim().isEmpty() &&
                this.sdt != null && !this.sdt.trim().isEmpty() &&
                this.maKhachHang != null && !this.maKhachHang.trim().isEmpty() &&
                this.taiKhoan != null &&
                this.trangThai != null;
    }

    // Legacy method - Ä‘á»ƒ compatibility
    public void setIdDiaChi(Integer diaChiId) {
        // Empty implementation for compatibility
        // Äá»‹a chá»‰ giá» Ä‘Æ°á»£c quáº£n lÃ½ qua TaiKhoan
    }

    // ===== SAFE TOSTRING =====
    @Override
    public String toString() {
        return "KhachHang{" +
                "id=" + id +
                ", maKhachHang='" + maKhachHang + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", sdt='" + sdt + '\'' +
                ", trangThai=" + trangThai +
                ", ngayTao=" + ngayTao +
                ", ngayCapNhat=" + ngayCapNhat +
                ", taiKhoanId=" + (taiKhoan != null ? taiKhoan.getId() : null) +
                '}';
    }

    // ===== EQUALS & HASHCODE =====
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        KhachHang other = (KhachHang) obj;

        if (this.id != null && other.id != null) {
            return this.id.equals(other.id);
        }

        if (this.maKhachHang != null && other.maKhachHang != null) {
            return this.maKhachHang.equals(other.maKhachHang);
        }

        return false;
    }

    @Override
    public int hashCode() {
        if (this.id != null) {
            return this.id.hashCode();
        }
        if (this.maKhachHang != null) {
            return this.maKhachHang.hashCode();
        }
        return super.hashCode();
    }
}
