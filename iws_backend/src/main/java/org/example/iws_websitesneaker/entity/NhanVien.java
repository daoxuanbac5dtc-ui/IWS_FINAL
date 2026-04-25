package org.example.iws_websitesneaker.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "nhan_vien")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// THÃŠM: Annotation Ä‘á»ƒ trÃ¡nh lá»—i lazy loading khi serialize JSON
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // GIá»® NGUYÃŠN: FetchType.LAZY vÃ¬ sáº½ dÃ¹ng JOIN FETCH trong repository
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tai_khoan", referencedColumnName = "id")
    // THÃŠM: Annotation Ä‘á»ƒ trÃ¡nh lá»—i JSON serialization
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private TaiKhoan taiKhoan;

    @Column(name = "ma_nhan_vien", length = 25, nullable = false, unique = true)
    private String maNhanVien;

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

    // GIá»® NGUYÃŠN: CÃ¡c method nÃ y
    public void setDiaChi(DiaChi diaChi) {
        // Implementation for setting address
    }

    public void setIdDiaChi(Integer diaChiId) {
        // Implementation for setting address ID
    }

    // ===== THÃŠM: LIFECYCLE CALLBACKS =====

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

    // ===== THÃŠM: SAFE METHODS - KhÃ´ng gÃ¢y LazyInitializationException =====

    /**
     * Láº¥y email má»™t cÃ¡ch an toÃ n tá»« TaiKhoan
     * QUAN TRá»ŒNG: Method nÃ y khÃ´ng gÃ¢y LazyInitializationException
     */
    public String getEmailSafe() {
        try {
            return this.taiKhoan != null ? this.taiKhoan.getEmail() : null;
        } catch (Exception e) {
            // Log warning nhÆ°ng khÃ´ng throw exception Ä‘á»ƒ trÃ¡nh crash
            System.err.println("Warning: Could not get email for employee " + this.id + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Kiá»ƒm tra nhÃ¢n viÃªn cÃ³ Ä‘ang hoáº¡t Ä‘á»™ng khÃ´ng
     */
    public boolean isActive() {
        return this.trangThai != null && this.trangThai == 1;
    }

    /**
     * Kiá»ƒm tra cÃ³ pháº£i admin khÃ´ng (thÃ´ng qua tÃ i khoáº£n)
     * SAFE METHOD - khÃ´ng gÃ¢y LazyInitializationException
     */
    public boolean isAdminSafe() {
        try {
            return this.taiKhoan != null && this.taiKhoan.isAdmin();
        } catch (Exception e) {
            System.err.println("Warning: Could not check admin status for employee " + this.id + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Láº¥y tÃªn vai trÃ² má»™t cÃ¡ch an toÃ n
     */
    public String getRoleDisplayNameSafe() {
        try {
            return this.taiKhoan != null ? this.taiKhoan.getRoleDisplayName() : "KhÃ´ng xÃ¡c Ä‘á»‹nh";
        } catch (Exception e) {
            System.err.println("Warning: Could not get role for employee " + this.id + ": " + e.getMessage());
            return "KhÃ´ng xÃ¡c Ä‘á»‹nh";
        }
    }

    /**
     * Get display name cho tráº¡ng thÃ¡i
     */
    public String getStatusDisplayName() {
        return this.isActive() ? "Äang lÃ m viá»‡c" : "Nghá»‰ viá»‡c";
    }

    // ===== THÃŠM: BUSINESS LOGIC METHODS =====

    /**
     * Cáº­p nháº­t thÃ´ng tin cÆ¡ báº£n (khÃ´ng thay Ä‘á»•i tÃ i khoáº£n)
     */
    public void updateBasicInfo(String hoTen, String sdt, String maNhanVien) {
        if (hoTen != null && !hoTen.trim().isEmpty()) {
            this.hoTen = hoTen.trim();
        }
        if (sdt != null && !sdt.trim().isEmpty()) {
            this.sdt = sdt.trim();
        }
        if (maNhanVien != null && !maNhanVien.trim().isEmpty()) {
            this.maNhanVien = maNhanVien.trim();
        }
        this.ngayCapNhat = new Date();
    }

    /**
     * Toggle tráº¡ng thÃ¡i hoáº¡t Ä‘á»™ng
     */
    public void toggleStatus() {
        this.trangThai = this.trangThai == 1 ? 0 : 1;
        this.ngayCapNhat = new Date();
    }

    /**
     * Deactivate nhÃ¢n viÃªn (soft delete)
     */
    public void deactivate() {
        this.trangThai = 0;
        this.ngayCapNhat = new Date();
    }

    /**
     * Reactivate nhÃ¢n viÃªn
     */
    public void reactivate() {
        this.trangThai = 1;
        this.ngayCapNhat = new Date();
    }

    // ===== THÃŠM: VALIDATION METHODS =====

    /**
     * Validate sá»‘ Ä‘iá»‡n thoáº¡i Viá»‡t Nam (10-11 sá»‘, báº¯t Ä‘áº§u báº±ng 0)
     */
    public boolean hasValidPhoneNumber() {
        return this.sdt != null && this.sdt.matches("^0\\d{9,10}$");
    }

    /**
     * Validate há» tÃªn (chá»‰ chá»©a chá»¯ cÃ¡i vÃ  khoáº£ng tráº¯ng, há»— trá»£ tiáº¿ng Viá»‡t)
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
                this.maNhanVien != null && !this.maNhanVien.trim().isEmpty() &&
                this.taiKhoan != null &&
                this.trangThai != null;
    }

    // ===== THÃŠM: SAFE TOSTRING =====

    /**
     * toString an toÃ n - khÃ´ng truy cáº­p lazy-loaded fields
     */
    @Override
    public String toString() {
        return "NhanVien{" +
                "id=" + id +
                ", maNhanVien='" + maNhanVien + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", sdt='" + sdt + '\'' +
                ", trangThai=" + trangThai +
                ", ngayTao=" + ngayTao +
                ", ngayCapNhat=" + ngayCapNhat +
                ", taiKhoanId=" + (taiKhoan != null ? taiKhoan.getId() : null) +
                '}';
    }

    // ===== THÃŠM: STATIC FACTORY METHODS =====

    /**
     * Táº¡o nhÃ¢n viÃªn má»›i tá»« tÃ i khoáº£n
     */
    public static NhanVien createFromTaiKhoan(TaiKhoan taiKhoan, String maNhanVien,
                                              String hoTen, String sdt) {
        if (taiKhoan == null || hoTen == null || sdt == null || maNhanVien == null) {
            throw new IllegalArgumentException("CÃ¡c tham sá»‘ báº¯t buá»™c khÃ´ng Ä‘Æ°á»£c null");
        }

        return NhanVien.builder()
                .taiKhoan(taiKhoan)
                .maNhanVien(maNhanVien)
                .hoTen(hoTen)
                .sdt(sdt)
                .trangThai(1)
                .ngayTao(new Date())
                .ngayCapNhat(new Date())
                .build();
    }

    // ===== THÃŠM: EQUALS & HASHCODE =====

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        NhanVien other = (NhanVien) obj;

        // So sÃ¡nh theo ID náº¿u cÃ³
        if (this.id != null && other.id != null) {
            return this.id.equals(other.id);
        }

        // So sÃ¡nh theo mÃ£ nhÃ¢n viÃªn
        if (this.maNhanVien != null && other.maNhanVien != null) {
            return this.maNhanVien.equals(other.maNhanVien);
        }

        return false;
    }

    @Override
    public int hashCode() {
        if (this.id != null) {
            return this.id.hashCode();
        }
        if (this.maNhanVien != null) {
            return this.maNhanVien.hashCode();
        }
        return super.hashCode();
    }
}
