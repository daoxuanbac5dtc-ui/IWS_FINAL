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
public class NhanVienDto {
    // Basic employee information
    private Integer id;
    private Integer idTaiKhoan; // Reference to TaiKhoan
    private String maNhanVien;

    // Email comes from TaiKhoan - read-only
    private String email;

    // Employee personal information - can be null initially, completed later
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

    // Employee summary information
    private EmployeeSummary summary;

    // Address inner class - using 2-level addressing
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiaChiInfo {
        private Integer id;
        private String maTinh;
        private String maPhuong; // No district level
        private String tenTinh;
        private String tenPhuong; // No district level
        private String diaChiChiTiet;
        private String diaChiDayDu; // Full concatenated address
        private Boolean isDefault;
        private Integer trangThai;
    }

    // Employee summary information
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeSummary {
        private String employeeTier; // Junior, Regular, Senior, Expert
        private Long daysWorked;
        private Date lastActivity;
        private Boolean needsAdminAttention;
        private Boolean isProfileCompleted;
        private Integer soLuongDiaChi;

        // Helper methods
        public boolean isNewEmployee() {
            return daysWorked != null && daysWorked < 90;
        }

        public boolean isExperienced() {
            return daysWorked != null && daysWorked > 365;
        }
    }

    // BUSINESS LOGIC HELPER METHODS

    /**
     * Check if employee profile is completed
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
        return maNhanVien != null ? maNhanVien : "Nhân viên";
    }

    /**
     * Check if employee is active
     */
    public boolean isActive() {
        return trangThai != null && trangThai == 1;
    }

    /**
     * Get default address for display - SỬA ĐỂ HIỂN THỊ ĐỊA CHỈ
     */
    public String getDefaultAddressDisplay() {
        if (diaChiMacDinh != null && diaChiMacDinh.getDiaChiDayDu() != null && !diaChiMacDinh.getDiaChiDayDu().trim().isEmpty()) {
            return diaChiMacDinh.getDiaChiDayDu();
        }

        if (danhSachDiaChi != null && !danhSachDiaChi.isEmpty()) {
            DiaChiInfo firstAddress = danhSachDiaChi.get(0);
            if (firstAddress.getDiaChiDayDu() != null && !firstAddress.getDiaChiDayDu().trim().isEmpty()) {
                return firstAddress.getDiaChiDayDu();
            }
        }

        return "Chưa có địa chỉ";
    }

    /**
     * THÊM: Get full address for table display
     */
    public String getFullAddressForTable() {
        String address = getDefaultAddressDisplay();
        if ("Chưa có địa chỉ".equals(address)) {
            return address;
        }

        // Truncate if too long for table display
        if (address.length() > 50) {
            return address.substring(0, 47) + "...";
        }

        return address;
    }

    /**
     * THÊM: Check if has address
     */
    public boolean hasAddress() {
        return !getDefaultAddressDisplay().equals("Chưa có địa chỉ");
    }

    /**
     * THÊM: Get province name for filtering/display
     */
    public String getProvinceName() {
        if (diaChiMacDinh != null && diaChiMacDinh.getTenTinh() != null) {
            return diaChiMacDinh.getTenTinh();
        }

        if (danhSachDiaChi != null && !danhSachDiaChi.isEmpty()) {
            DiaChiInfo firstAddress = danhSachDiaChi.get(0);
            if (firstAddress.getTenTinh() != null) {
                return firstAddress.getTenTinh();
            }
        }

        return "Chưa xác định";
    }

    /**
     * THÊM: Get ward name for filtering/display
     */
    public String getWardName() {
        if (diaChiMacDinh != null && diaChiMacDinh.getTenPhuong() != null) {
            return diaChiMacDinh.getTenPhuong();
        }

        if (danhSachDiaChi != null && !danhSachDiaChi.isEmpty()) {
            DiaChiInfo firstAddress = danhSachDiaChi.get(0);
            if (firstAddress.getTenPhuong() != null) {
                return firstAddress.getTenPhuong();
            }
        }

        return "Chưa xác định";
    }

    /**
     * Get employee tier/level
     */
    public String getEmployeeTier() {
        if (summary != null && summary.getEmployeeTier() != null) {
            return summary.getEmployeeTier();
        }
        return "Junior"; // Default tier
    }

    /**
     * Check if employee needs admin attention
     */
    public boolean needsAdminAttention() {
        return summary != null &&
                summary.getNeedsAdminAttention() != null &&
                summary.getNeedsAdminAttention();
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
     * Validate employee data for profile completion
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

    /**
     * Check if this is a new employee
     */
    public boolean isNewEmployee() {
        return summary != null && summary.isNewEmployee();
    }

    /**
     * Get work duration in days
     */
    public Long getWorkDurationDays() {
        if (summary != null && summary.getDaysWorked() != null) {
            return summary.getDaysWorked();
        }

        if (ngayTao != null) {
            long diffInMillis = new Date().getTime() - ngayTao.getTime();
            return diffInMillis / (24 * 60 * 60 * 1000);
        }

        return 0L;
    }

    /**
     * Check if employee has recent activity
     */
    public boolean hasRecentActivity() {
        if (summary == null || summary.getLastActivity() == null) {
            return false;
        }

        long daysSince = (new Date().getTime() - summary.getLastActivity().getTime()) / (1000 * 60 * 60 * 24);
        return daysSince <= 7; // Within last 7 days
    }

    /**
     * THÊM: Get status display text
     */
    public String getStatusDisplayText() {
        return isActive() ? "Đang làm việc" : "Nghỉ việc";
    }

    /**
     * THÊM: Get badge class for status
     */
    public String getStatusBadgeClass() {
        return isActive() ? "badge-success" : "badge-danger";
    }

    /**
     * THÊM: Get detailed summary for admin view
     */
    public String getDetailedSummary() {
        StringBuilder summary = new StringBuilder();

        summary.append("Nhân viên ").append(getDisplayName());

        if (hasAddress()) {
            summary.append(" tại ").append(getProvinceName());
        }

        summary.append(" - ").append(getStatusDisplayText());

        if (needsAdminAttention()) {
            summary.append(" (Cần xem xét)");
        }

        return summary.toString();
    }
}
