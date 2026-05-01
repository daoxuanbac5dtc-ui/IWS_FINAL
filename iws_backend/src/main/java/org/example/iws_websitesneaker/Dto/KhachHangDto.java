package org.example.iws_websitesneaker.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KhachHangDto {
    // Basic customer information
    private Integer id;
    private Integer idTaiKhoan; // Reference to TaiKhoan
    private Integer idDiaChi;   // Legacy field (có thể bỏ nếu không dùng)
    private Integer idViDiem;   // Reference to ViDiem
    private String maKhachHang;

    // Email comes from TaiKhoan - read-only
    private String email;

    // Customer personal information - can be null initially, completed later
    @Size(max = 225, message = "Họ tên không được quá 225 ký tự")
    private String hoTen;

    @Pattern(regexp = "^0\\d{9,10}$|^$", message = "Số điện thoại phải có 10-11 số và bắt đầu bằng 0")
    private String sdt;

    // Status and audit fields
    private Integer trangThai;
    private Date ngayTao;
    private Date ngayCapNhat;

    // Address information - managed through TaiKhoan
    private List<DiaChiInfo> danhSachDiaChi;
    private DiaChiInfo diaChiMacDinh;

    // Address inner class - using 2-level addressing (Province -> Ward only)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiaChiInfo {
        private Integer id;
        private String maTinh;
        private String maPhuong;        // Directly ward, no district
        private String tenTinh;
        private String tenPhuong;       // Directly ward, no district
        private String diaChiChiTiet;
        private String diaChiDayDu;     // Full concatenated address
        private Boolean isDefault;
        private Integer trangThai;
    }

    // BUSINESS LOGIC HELPER METHODS

    /**
     * Check if customer profile is completed
     */
    public boolean isProfileCompleted() {
        return hoTen != null && !hoTen.trim().isEmpty() &&
                sdt != null && !sdt.trim().isEmpty() &&
                email != null && !email.trim().isEmpty();
    }

    /**
     * Get display name for UI
     */
    public String getDisplayName() {
        if (hoTen != null && !hoTen.trim().isEmpty()) {
            return hoTen;
        }
        if (email != null && !email.trim().isEmpty()) {
            return email;
        }
        return maKhachHang != null ? maKhachHang : "Khách hàng";
    }

    /**
     * Check if customer is active
     */
    public boolean isActive() {
        return trangThai != null && trangThai == 1;
    }

    /**
     * Get default address for display
     */
    public String getDefaultAddressDisplay() {
        if (diaChiMacDinh != null && diaChiMacDinh.getDiaChiDayDu() != null) {
            return diaChiMacDinh.getDiaChiDayDu();
        }
        return "Chưa có địa chỉ";
    }

    /**
     * Format phone number for display
     */
    public String getFormattedPhone() {
        if (sdt == null || sdt.trim().isEmpty()) {
            return "Chưa cập nhật";
        }

        String phone = sdt.trim();
        if (phone.length() == 10) {
            return phone.substring(0, 4) + " " + phone.substring(4, 7) + " " + phone.substring(7);
        } else if (phone.length() == 11) {
            return phone.substring(0, 4) + " " + phone.substring(4, 8) + " " + phone.substring(8);
        }
        return phone;
    }

    // VALIDATION HELPER METHODS

    /**
     * Validate customer data for profile completion
     */
    public boolean isValidForProfileCompletion() {
        return hoTen != null && !hoTen.trim().isEmpty() &&
                sdt != null && sdt.matches("^0\\d{9,10}$");
    }

    /**
     * Get validation errors for profile completion
     */
    public List<String> getProfileCompletionErrors() {
        List<String> errors = new java.util.ArrayList<>();

        if (hoTen == null || hoTen.trim().isEmpty()) {
            errors.add("Họ tên không được để trống");
        } else if (hoTen.length() > 225) {
            errors.add("Họ tên không được quá 225 ký tự");
        }

        if (sdt == null || sdt.trim().isEmpty()) {
            errors.add("Số điện thoại không được để trống");
        } else if (!sdt.matches("^0\\d{9,10}$")) {
            errors.add("Số điện thoại không đúng định dạng");
        }

        return errors;
    }
}
