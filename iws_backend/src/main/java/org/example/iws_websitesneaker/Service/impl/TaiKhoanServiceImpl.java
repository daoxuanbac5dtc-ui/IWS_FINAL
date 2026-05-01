package org.example.iws_websitesneaker.Service.impl;

import org.example.iws_websitesneaker.Dto.TaiKhoanDTO;
import org.example.iws_websitesneaker.Service.TaiKhoanService;
import org.example.iws_websitesneaker.Service.DiaChiService;
import org.example.iws_websitesneaker.Service.KhachHangService;
import org.example.iws_websitesneaker.Service.NhanVienService;
import org.example.iws_websitesneaker.entity.*;
import org.example.iws_websitesneaker.repository.RepoTaiKhoan;
import org.example.iws_websitesneaker.util.TextEncodingGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaiKhoanServiceImpl implements TaiKhoanService {

    @Autowired
    private RepoTaiKhoan taiKhoanRepository;

    @Autowired
    private DiaChiService diaChiService;

    @Autowired
    private KhachHangService khachHangService;

    @Autowired
    private NhanVienService nhanVienService;

    // ===== CONSTANTS - CÁC HẰNG SỐ =====
    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final Pattern EMAIL_REGEX = Pattern.compile(EMAIL_PATTERN);
    private static final String PHONE_PATTERN = "^0\\d{9,10}$";

    // ================== CORE CRUD OPERATIONS ==================

    @Override
    public List<TaiKhoan> findAll() {
        try {
            return taiKhoanRepository.findAll();
        } catch (Exception e) {
            System.err.println("Lỗi tìm tất cả tài khoản: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<TaiKhoan> findById(Integer id) {
        try {
            return taiKhoanRepository.findById(id);
        } catch (Exception e) {
            System.err.println("Lỗi tìm tài khoản theo ID: " + e.getMessage());
            return Optional.empty();
        }
    }
    @Override
    public boolean updatePassword(String email, String newPassword) {
        try {
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanRepository.findByEmail(email.toLowerCase().trim());

            if (taiKhoanOpt.isPresent()) {
                TaiKhoan taiKhoan = taiKhoanOpt.get();

                // Kiểm tra tài khoản có active không
                if (!taiKhoan.isActive()) {
                    System.err.println("Account is inactive: " + taiKhoan.getMaTaiKhoan());
                    return false;
                }

                // Hash mật khẩu mới bằng Spring Security BCrypt
                String hashedPassword = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(newPassword);
                taiKhoan.setMatKhau(hashedPassword);
                taiKhoan.setNgayCapNhat(new Date());

                taiKhoanRepository.save(taiKhoan);

                System.out.println("✅ Password updated successfully for account: " + taiKhoan.getMaTaiKhoan());
                return true;
            } else {
                System.err.println("❌ Account not found for email: " + email);
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Error updating password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public TaiKhoan save(TaiKhoan taiKhoan) {
        try {
            taiKhoan.setNgayCapNhat(new Date());
            if (taiKhoan.getId() == null) {
                taiKhoan.setNgayTao(new Date());
                if (taiKhoan.getMaTaiKhoan() == null) {
                    taiKhoan.setMaTaiKhoan(generateMaTaiKhoan());
                }
            }
            return taiKhoanRepository.save(taiKhoan);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Lỗi ràng buộc database: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi lưu tài khoản: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Integer id) {
        try {
            System.out.println("=== XÓA TÀI KHOẢN SERVICE ===");
            System.out.println("Đang xóa tài khoản ID: " + id);

            if (!canDeleteAccount(id)) {
                throw new IllegalStateException("Không thể xóa tài khoản này");
            }

            // Lấy thông tin tài khoản trước khi xóa
            Optional<TaiKhoan> accountOpt = findById(id);
            if (accountOpt.isEmpty()) {
                throw new IllegalArgumentException("Không tìm thấy tài khoản với ID: " + id);
            }

            TaiKhoan account = accountOpt.get();
            System.out.println("Tài khoản cần xóa: " + account.getEmail() + " (" + account.getVaiTro() + ")");

            // Bước 1: Xóa tất cả dữ liệu liên quan theo thứ tự đúng
            try {
                // 1.1: Xóa voucher của tài khoản (bảng tai_khoan_voucher)
                deleteAccountVouchers(id);

                // 1.2: Xóa các đơn hàng và dữ liệu giao dịch (nếu có)
                deleteAccountOrders(id);

                // 1.3: Xóa địa chỉ
                if (diaChiService != null) {
                    try {
                        diaChiService.deleteByTaiKhoanId(id);
                        System.out.println("✅ Đã xóa địa chỉ cho tài khoản: " + id);
                    } catch (Exception e) {
                        System.err.println("⚠️ Cảnh báo: Không thể xóa địa chỉ: " + e.getMessage());
                    }
                }

                // 1.4: Xóa entity theo vai trò
                if (account.getVaiTro() == TaiKhoan.VaiTro.USER) {
                    try {
                        if (khachHangService != null) {
                            khachHangService.deleteByTaiKhoanId(id);
                            System.out.println("✅ Đã xóa dữ liệu khách hàng cho tài khoản: " + id);
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ Cảnh báo: Không thể xóa dữ liệu khách hàng: " + e.getMessage());
                    }
                } else if (account.getVaiTro() == TaiKhoan.VaiTro.NHANVIEN) {
                    try {
                        if (nhanVienService != null) {
                            nhanVienService.deleteByTaiKhoanId(id);
                            System.out.println("✅ Đã xóa dữ liệu nhân viên cho tài khoản: " + id);
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ Cảnh báo: Không thể xóa dữ liệu nhân viên: " + e.getMessage());
                    }
                }

            } catch (Exception relatedError) {
                System.err.println("❌ Lỗi xóa entity liên quan: " + relatedError.getMessage());
                throw new RuntimeException("Không thể xóa tài khoản do có dữ liệu liên quan không thể xóa: " + relatedError.getMessage());
            }

            // Bước 2: Xóa tài khoản chính
            try {
                taiKhoanRepository.deleteById(id);
                System.out.println("✅ Xóa tài khoản thành công: " + id);

                // Ghi log việc xóa
                logAccountActivity(id, "DELETE", "Tài khoản đã được xóa thành công");

            } catch (Exception mainDeleteError) {
                System.err.println("❌ Lỗi xóa tài khoản chính: " + mainDeleteError.getMessage());

                // Kiểm tra lỗi database cụ thể
                if (mainDeleteError.getMessage().contains("constraint") ||
                        mainDeleteError.getMessage().contains("foreign key") ||
                        mainDeleteError.getMessage().contains("REFERENCE")) {
                    throw new RuntimeException("Không thể xóa tài khoản: vẫn còn dữ liệu liên quan chưa được xóa");
                }

                throw new RuntimeException("Không thể xóa tài khoản: " + mainDeleteError.getMessage());
            }

        } catch (Exception e) {
            System.err.println("❌ Xóa tài khoản thất bại: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể xóa tài khoản: " + e.getMessage(), e);
        }
    }
    private void deleteAccountVouchers(Integer accountId) {
        try {
            // Sử dụng SQL để xóa trực tiếp từ bảng tai_khoan_voucher
            taiKhoanRepository.deleteAccountVouchers(accountId);
            System.out.println("✅ Đã xóa voucher cho tài khoản: " + accountId);
        } catch (Exception e) {
            System.err.println("❌ Lỗi xóa voucher: " + e.getMessage());
            throw new RuntimeException("Không thể xóa voucher của tài khoản: " + e.getMessage());
        }
    }

    /**
     * Xóa tất cả đơn hàng và dữ liệu giao dịch của tài khoản
     */
    private void deleteAccountOrders(Integer accountId) {
        try {
            // Xóa các bảng liên quan đến đơn hàng theo thứ tự:
            // 1. Xóa chi tiết đơn hàng
            // 2. Xóa đơn hàng
            // 3. Xóa các giao dịch khác

            taiKhoanRepository.deleteAccountOrders(accountId);
            System.out.println("✅ Đã xóa đơn hàng cho tài khoản: " + accountId);
        } catch (Exception e) {
            System.err.println("❌ Lỗi xóa đơn hàng: " + e.getMessage());
            // Có thể không có đơn hàng nào, chỉ log warning
            System.err.println("⚠️ Tiếp tục xóa tài khoản...");
        }
    }
    // ================== ACCOUNT CREATION - MAIN METHODS ==================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createCompleteAccount(TaiKhoanDTO dto) {
        System.out.println("=== Tạo tài khoản hoàn chỉnh ===");

        try {
            // KIỂM TRA TRÙNG LẶP TRƯỚC KHI BẮT ĐẦU TRANSACTION
            if (!validateCreateAccountDto(dto)) {
                throw new IllegalArgumentException("Dữ liệu tài khoản không hợp lệ");
            }

            if (existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email đã tồn tại: " + dto.getEmail());
            }

            // KIỂM TRA TRÙNG LẶP SỐ ĐIỆN THOẠI
            if (dto.needsPersonalInfo() && dto.getSdt() != null) {
                String cleanPhone = dto.getSdt().trim().replaceAll("\\s+", "");

                if (dto.getVaiTro() == TaiKhoan.VaiTro.NHANVIEN || "NHANVIEN".equals(dto.getVaiTroString())) {
                    if (nhanVienService.existsBySdt(cleanPhone)) {
                        throw new IllegalArgumentException("Số điện thoại đã được sử dụng: " + cleanPhone);
                    }
                } else if (dto.getVaiTro() == TaiKhoan.VaiTro.USER || "USER".equals(dto.getVaiTroString())) {
                    if (khachHangService.existsBySdt(cleanPhone)) {
                        throw new IllegalArgumentException("Số điện thoại đã được sử dụng: " + cleanPhone);
                    }
                }
            }

            // Tạo tài khoản chính
            TaiKhoan taiKhoan = createTaiKhoan(dto);
            Map<String, Object> result = new HashMap<>();
            result.put("taiKhoan", taiKhoan);

            // Tạo entities liên quan
            try {
                if (taiKhoan.getVaiTro() == TaiKhoan.VaiTro.USER) {
                    KhachHang khachHang = createKhachHangForAccount(dto, taiKhoan);
                    result.put("khachHang", khachHang);
                } else if (taiKhoan.getVaiTro() == TaiKhoan.VaiTro.NHANVIEN) {
                    NhanVien nhanVien = createNhanVienForAccount(dto, taiKhoan);
                    result.put("nhanVien", nhanVien);
                }

                if (shouldCreateAddress(dto)) {
                    DiaChi diaChi = createAddressForAccount(dto, taiKhoan);
                    result.put("diaChi", diaChi);
                }
            } catch (Exception relatedError) {
                System.err.println("Cảnh báo: Lỗi tạo dữ liệu liên quan: " + relatedError.getMessage());
                throw new RuntimeException("Lỗi tạo " + getRoleDisplayName(taiKhoan.getVaiTro()) + ": " + relatedError.getMessage(), relatedError);
            }

            logAccountActivity(taiKhoan.getId(), "CREATE", "Tài khoản được tạo thành công");
            return result;

        } catch (Exception e) {
            System.err.println("Lỗi tạo tài khoản: " + e.getMessage());
            throw e;
        }
    }

    private String getRoleDisplayName(TaiKhoan.VaiTro vaiTro) {
        switch (vaiTro) {
            case USER: return "khách hàng";
            case NHANVIEN: return "nhân viên";
            case ADMIN: return "admin";
            default: return "người dùng";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaiKhoan createTaiKhoan(TaiKhoanDTO dto) {
        try {
            if (!validateCreateAccountDto(dto)) {
                throw new IllegalArgumentException("Dữ liệu tài khoản không hợp lệ");
            }

            if (existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email đã tồn tại");
            }

            TaiKhoan taiKhoan = new TaiKhoan();
            taiKhoan.setMaTaiKhoan(dto.getMaTaiKhoan() != null ? dto.getMaTaiKhoan() : generateMaTaiKhoan());
            taiKhoan.setEmail(normalizeEmail(dto.getEmail()));
            taiKhoan.setMatKhau(hashPassword(dto.getMatKhau()));

            // Xử lý vai trò
            if (dto.getVaiTro() != null) {
                taiKhoan.setVaiTro(dto.getVaiTro());
            } else if (dto.getVaiTroString() != null) {
                TaiKhoan.VaiTro role = parseVaiTro(dto.getVaiTroString());
                if (role == null) {
                    throw new IllegalArgumentException("Vai trò không hợp lệ: " + dto.getVaiTroString());
                }
                taiKhoan.setVaiTro(role);
            } else {
                throw new IllegalArgumentException("Vai trò là bắt buộc");
            }

            taiKhoan.setTrangThai(dto.getTrangThai() != null ? dto.getTrangThai() : 1);
            taiKhoan.setNgayTao(new Date());
            taiKhoan.setNgayCapNhat(new Date());

            return taiKhoanRepository.save(taiKhoan);

        } catch (Exception e) {
            System.err.println("Lỗi tạo TaiKhoan: " + e.getMessage());
            throw new RuntimeException("Lỗi tạo tài khoản: " + e.getMessage(), e);
        }
    }

    @Override
    public TaiKhoan createAccount(String email, String password, TaiKhoan.VaiTro vaiTro) {
        try {
            if (!isValidEmail(email)) {
                throw new IllegalArgumentException("Email không hợp lệ");
            }
            if (!isValidPassword(password)) {
                throw new IllegalArgumentException("Mật khẩu không hợp lệ");
            }
            if (existsByEmail(email)) {
                throw new IllegalArgumentException("Email đã tồn tại");
            }

            TaiKhoan taiKhoan = new TaiKhoan();
            taiKhoan.setMaTaiKhoan(generateMaTaiKhoan());
            taiKhoan.setEmail(normalizeEmail(email));
            taiKhoan.setMatKhau(hashPassword(password));
            taiKhoan.setVaiTro(vaiTro);
            taiKhoan.setTrangThai(1);
            taiKhoan.setNgayTao(new Date());
            taiKhoan.setNgayCapNhat(new Date());

            return save(taiKhoan);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo tài khoản đơn giản: " + e.getMessage(), e);
        }
    }

    // ================== AUTHENTICATION & SECURITY ==================

    @Override
    public Optional<TaiKhoan> authenticate(String email, String password) {
        try {
            if (!isValidEmail(email) || password == null || password.isEmpty()) {
                return Optional.empty();
            }

            Optional<TaiKhoan> accountOpt = findByEmail(normalizeEmail(email));
            if (accountOpt.isEmpty()) {
                return Optional.empty();
            }

            TaiKhoan account = accountOpt.get();
            if (verifyPassword(password, account.getMatKhau())) {
                return Optional.of(account);
            }

            return Optional.empty();

        } catch (Exception e) {
            System.err.println("Lỗi xác thực: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean changePassword(Integer id, String oldPassword, String newPassword) {
        try {
            Optional<TaiKhoan> accountOpt = findById(id);
            if (accountOpt.isEmpty()) {
                return false;
            }

            TaiKhoan account = accountOpt.get();
            if (!verifyPassword(oldPassword, account.getMatKhau())) {
                return false;
            }

            if (!isValidPassword(newPassword)) {
                return false;
            }

            account.setMatKhau(hashPassword(newPassword));
            save(account);

            logAccountActivity(id, "PASSWORD_CHANGE", "Đổi mật khẩu thành công");
            return true;

        } catch (Exception e) {
            System.err.println("Lỗi đổi mật khẩu: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean resetPassword(Integer id, String newPassword) {
        try {
            Optional<TaiKhoan> accountOpt = findById(id);
            if (accountOpt.isEmpty()) {
                return false;
            }

            if (!isValidPassword(newPassword)) {
                return false;
            }

            TaiKhoan account = accountOpt.get();
            account.setMatKhau(hashPassword(newPassword));
            save(account);

            logAccountActivity(id, "PASSWORD_RESET", "Reset mật khẩu bởi admin");
            return true;

        } catch (Exception e) {
            System.err.println("Lỗi reset mật khẩu: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean hasPermission(Integer accountId, String permission) {
        try {
            Optional<TaiKhoan> accountOpt = findById(accountId);
            if (accountOpt.isEmpty()) {
                return false;
            }

            TaiKhoan account = accountOpt.get();

            // Logic phân quyền cơ bản theo vai trò
            switch (account.getVaiTro()) {
                case ADMIN:
                    return true; // Admin có tất cả quyền
                case NHANVIEN:
                    return !permission.startsWith("ADMIN_"); // Nhân viên có quyền non-admin
                case USER:
                    return permission.startsWith("USER_"); // User chỉ có quyền user
                default:
                    return false;
            }

        } catch (Exception e) {
            System.err.println("Lỗi kiểm tra quyền: " + e.getMessage());
            return false;
        }
    }

    // ================== BASIC QUERIES ==================

    @Override
    public Optional<TaiKhoan> findByEmail(String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return Optional.empty();
            }
            return taiKhoanRepository.findByEmail(normalizeEmail(email));
        } catch (Exception e) {
            System.err.println("Lỗi tìm theo email: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<TaiKhoan> findByMaTaiKhoan(String maTaiKhoan) {
        try {
            if (maTaiKhoan == null || maTaiKhoan.trim().isEmpty()) {
                return Optional.empty();
            }
            TaiKhoan result = taiKhoanRepository.findByMaTaiKhoan(maTaiKhoan);
            return Optional.ofNullable(result);
        } catch (Exception e) {
            System.err.println("Lỗi tìm theo mã tài khoản: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return false;
            }
            return taiKhoanRepository.existsByEmail(normalizeEmail(email));
        } catch (Exception e) {
            System.err.println("Lỗi kiểm tra email tồn tại: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean existsByMaTaiKhoan(String maTaiKhoan) {
        try {
            if (maTaiKhoan == null || maTaiKhoan.trim().isEmpty()) {
                return false;
            }
            return taiKhoanRepository.existsByMaTaiKhoan(maTaiKhoan);
        } catch (Exception e) {
            System.err.println("Lỗi kiểm tra mã tài khoản tồn tại: " + e.getMessage());
            return false;
        }
    }

    // ================== ROLE-BASED QUERIES ==================

    @Override
    public List<TaiKhoan> findByVaiTro(TaiKhoan.VaiTro vaiTro) {
        try {
            return taiKhoanRepository.findByVaiTro(vaiTro);
        } catch (Exception e) {
            System.err.println("Lỗi tìm theo vai trò: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<TaiKhoan> findActiveCustomers() {
        try {
            return taiKhoanRepository.findByVaiTroAndTrangThai(TaiKhoan.VaiTro.USER, 1);
        } catch (Exception e) {
            System.err.println("Lỗi tìm khách hàng hoạt động: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<TaiKhoan> findActiveEmployees() {
        try {
            return taiKhoanRepository.findByVaiTroAndTrangThai(TaiKhoan.VaiTro.NHANVIEN, 1);
        } catch (Exception e) {
            System.err.println("Lỗi tìm nhân viên hoạt động: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<TaiKhoan> findActiveAdmins() {
        try {
            return taiKhoanRepository.findByVaiTroAndTrangThai(TaiKhoan.VaiTro.ADMIN, 1);
        } catch (Exception e) {
            System.err.println("Lỗi tìm admin hoạt động: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ================== STATUS MANAGEMENT ==================

    @Override
    public List<TaiKhoan> findByTrangThai(Integer trangThai) {
        try {
            return taiKhoanRepository.findByTrangThai(trangThai);
        } catch (Exception e) {
            System.err.println("Lỗi tìm theo trạng thái: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<TaiKhoan> findActiveAccounts() {
        try {
            return taiKhoanRepository.findByTrangThai(1);
        } catch (Exception e) {
            System.err.println("Lỗi tìm tài khoản hoạt động: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public boolean toggleAccountStatus(Integer id) {
        try {
            Optional<TaiKhoan> accountOpt = findById(id);
            if (accountOpt.isEmpty()) {
                return false;
            }

            TaiKhoan account = accountOpt.get();
            Integer newStatus = (account.getTrangThai() == 1) ? 0 : 1;

            // Kiểm tra admin cuối cùng
            if (account.getVaiTro() == TaiKhoan.VaiTro.ADMIN && newStatus == 0) {
                if (isLastActiveAdmin(id)) {
                    throw new IllegalStateException("Không thể vô hiệu hóa admin cuối cùng");
                }
            }

            account.setTrangThai(newStatus);
            save(account);

            String action = newStatus == 1 ? "ACTIVATE" : "DEACTIVATE";
            logAccountActivity(id, action, "Trạng thái đã được chuyển đổi");
            return true;

        } catch (Exception e) {
            System.err.println("Lỗi chuyển đổi trạng thái: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deactivateAccount(Integer id) {
        try {
            Optional<TaiKhoan> accountOpt = findById(id);
            if (accountOpt.isEmpty()) {
                return false;
            }

            TaiKhoan account = accountOpt.get();

            // Kiểm tra admin cuối cùng
            if (account.getVaiTro() == TaiKhoan.VaiTro.ADMIN && isLastActiveAdmin(id)) {
                throw new IllegalStateException("Không thể vô hiệu hóa admin cuối cùng");
            }

            account.setTrangThai(0);
            save(account);

            logAccountActivity(id, "DEACTIVATE", "Tài khoản đã bị vô hiệu hóa");
            return true;

        } catch (Exception e) {
            System.err.println("Lỗi vô hiệu hóa tài khoản: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean activateAccount(Integer id) {
        try {
            Optional<TaiKhoan> accountOpt = findById(id);
            if (accountOpt.isEmpty()) {
                return false;
            }

            TaiKhoan account = accountOpt.get();
            account.setTrangThai(1);
            save(account);

            logAccountActivity(id, "ACTIVATE", "Tài khoản đã được kích hoạt");
            return true;

        } catch (Exception e) {
            System.err.println("Lỗi kích hoạt tài khoản: " + e.getMessage());
            return false;
        }
    }

    // ================== SEARCH METHODS ==================

    @Override
    public List<TaiKhoan> searchByKeyword(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return findAll();
            }
            return taiKhoanRepository.searchByKeyword(keyword.trim());
        } catch (Exception e) {
            System.err.println("Lỗi tìm kiếm theo từ khóa: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<TaiKhoan> searchAdvanced(String email, String maTaiKhoan, TaiKhoan.VaiTro vaiTro,
                                         Integer trangThai, Date startDate, Date endDate) {
        try {
            return findAll().stream()
                    .filter(account -> {
                        // Lọc theo email
                        if (email != null && !email.trim().isEmpty()) {
                            if (account.getEmail() == null ||
                                    !account.getEmail().toLowerCase().contains(email.toLowerCase())) {
                                return false;
                            }
                        }

                        // Lọc theo mã tài khoản
                        if (maTaiKhoan != null && !maTaiKhoan.trim().isEmpty()) {
                            if (account.getMaTaiKhoan() == null ||
                                    !account.getMaTaiKhoan().toLowerCase().contains(maTaiKhoan.toLowerCase())) {
                                return false;
                            }
                        }

                        // Lọc theo vai trò
                        if (vaiTro != null && !account.getVaiTro().equals(vaiTro)) {
                            return false;
                        }

                        // Lọc theo trạng thái
                        if (trangThai != null && !account.getTrangThai().equals(trangThai)) {
                            return false;
                        }

                        // Lọc theo khoảng thời gian
                        if (startDate != null && account.getNgayTao() != null &&
                                account.getNgayTao().before(startDate)) {
                            return false;
                        }
                        if (endDate != null && account.getNgayTao() != null &&
                                account.getNgayTao().after(endDate)) {
                            return false;
                        }

                        return true;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Lỗi tìm kiếm nâng cao: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> searchWithPagination(String keyword, TaiKhoan.VaiTro vaiTro,
                                                    Integer trangThai, int page, int size,
                                                    String sortBy, String sortDir) {
        try {
            List<TaiKhoan> allResults;

            if (keyword != null && !keyword.trim().isEmpty()) {
                allResults = searchByKeyword(keyword);
            } else {
                allResults = findAll();
            }

            // Áp dụng filters
            if (vaiTro != null || trangThai != null) {
                allResults = allResults.stream()
                        .filter(account -> {
                            if (vaiTro != null && !account.getVaiTro().equals(vaiTro)) {
                                return false;
                            }
                            if (trangThai != null && !account.getTrangThai().equals(trangThai)) {
                                return false;
                            }
                            return true;
                        })
                        .collect(Collectors.toList());
            }

            // Áp dụng sorting
            sortAccountList(allResults, sortBy, sortDir);

            // Áp dụng pagination
            int totalElements = allResults.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalElements);

            List<TaiKhoan> pagedResults = startIndex < totalElements ?
                    allResults.subList(startIndex, endIndex) : new ArrayList<>();

            Map<String, Object> result = new HashMap<>();
            result.put("content", pagedResults);
            result.put("totalElements", totalElements);
            result.put("totalPages", totalPages);
            result.put("currentPage", page);
            result.put("size", size);

            return result;

        } catch (Exception e) {
            System.err.println("Lỗi tìm kiếm có phân trang: " + e.getMessage());
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("content", new ArrayList<>());
            emptyResult.put("totalElements", 0);
            emptyResult.put("totalPages", 0);
            emptyResult.put("currentPage", page);
            emptyResult.put("size", size);
            return emptyResult;
        }
    }

    // ================== STATISTICS & REPORTING ==================

    @Override
    public long countAll() {
        try {
            return taiKhoanRepository.count();
        } catch (Exception e) {
            System.err.println("Lỗi đếm tất cả tài khoản: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public long countByVaiTro(TaiKhoan.VaiTro vaiTro) {
        try {
            return taiKhoanRepository.countByVaiTro(vaiTro);
        } catch (Exception e) {
            System.err.println("Lỗi đếm theo vai trò: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public long countByTrangThai(Integer trangThai) {
        try {
            return taiKhoanRepository.countByTrangThai(trangThai);
        } catch (Exception e) {
            System.err.println("Lỗi đếm theo trạng thái: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public long countActiveByVaiTro(TaiKhoan.VaiTro vaiTro) {
        try {
            return taiKhoanRepository.countActiveByVaiTro(vaiTro);
        } catch (Exception e) {
            System.err.println("Lỗi đếm hoạt động theo vai trò: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public long countAccountsCreatedToday() {
        try {
            return taiKhoanRepository.countAccountsCreatedToday(new Date());
        } catch (Exception e) {
            System.err.println("Lỗi đếm tài khoản tạo hôm nay: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public long countAccountsCreatedThisMonth() {
        try {
            return taiKhoanRepository.countAccountsCreatedThisMonth(new Date());
        } catch (Exception e) {
            System.err.println("Lỗi đếm tài khoản tạo tháng này: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            stats.put("total", countAll());
            stats.put("active", countByTrangThai(1));
            stats.put("inactive", countByTrangThai(0));
            stats.put("customers", countByVaiTro(TaiKhoan.VaiTro.USER));
            stats.put("employees", countByVaiTro(TaiKhoan.VaiTro.NHANVIEN));
            stats.put("admins", countByVaiTro(TaiKhoan.VaiTro.ADMIN));
            stats.put("activeCustomers", countActiveByVaiTro(TaiKhoan.VaiTro.USER));
            stats.put("activeEmployees", countActiveByVaiTro(TaiKhoan.VaiTro.NHANVIEN));
            stats.put("activeAdmins", countActiveByVaiTro(TaiKhoan.VaiTro.ADMIN));
            stats.put("createdToday", countAccountsCreatedToday());
            stats.put("createdThisMonth", countAccountsCreatedThisMonth());

            return stats;

        } catch (Exception e) {
            System.err.println("Lỗi lấy thống kê dashboard: " + e.getMessage());
            return new HashMap<>();
        }
    }

    // ================== DATE RANGE QUERIES ==================

    @Override
    public List<TaiKhoan> findByDateRange(Date startDate, Date endDate) {
        try {
            return taiKhoanRepository.findByNgayTaoBetween(startDate, endDate);
        } catch (Exception e) {
            System.err.println("Lỗi tìm theo khoảng ngày: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<TaiKhoan> findLatestAccounts(int limit) {
        try {
            List<TaiKhoan> allAccounts = taiKhoanRepository.findLatestAccounts();
            return allAccounts.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Lỗi tìm tài khoản mới nhất: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<TaiKhoan> findRecentAccounts(int days) {
        try {
            Date cutoffDate = new Date(System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L));
            Date now = new Date();
            return findByDateRange(cutoffDate, now);
        } catch (Exception e) {
            System.err.println("Lỗi tìm tài khoản gần đây: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ================== VALIDATION METHODS ==================

    @Override
    public boolean validateCreateAccountDto(TaiKhoanDTO dto) {
        try {
            if (dto == null) {
                System.err.println("❌ DTO là null");
                return false;
            }

            // Validate thông tin cơ bản
            if (!dto.isValidBasicInfo()) {
                System.err.println("❌ Thông tin cơ bản không hợp lệ");
                return false;
            }

            if (!isValidEmail(dto.getEmail())) {
                System.err.println("❌ Email không hợp lệ: " + dto.getEmail());
                return false;
            }

            if (!isValidPassword(dto.getMatKhau())) {
                System.err.println("❌ Mật khẩu không hợp lệ");
                return false;
            }

            // Validate vai trò
            if (dto.getVaiTro() == null && (dto.getVaiTroString() == null || dto.getVaiTroString().trim().isEmpty())) {
                System.err.println("❌ Không có vai trò được chỉ định");
                return false;
            }

            // Validate thông tin cá nhân cho non-admin
            if (dto.needsPersonalInfo()) {
                if (TextEncodingGuard.hasEncodingIssue(dto.getHoTen())) {
                    System.err.println("Dữ liệu họ tên có dấu hiệu lỗi mã hóa: " + dto.getHoTen());
                    return false;
                }
                TaiKhoanDTO.DiaChiDto addressDto = dto.getEffectiveAddress();
                if (addressDto != null
                        && (TextEncodingGuard.hasEncodingIssue(addressDto.getTenTinh())
                        || TextEncodingGuard.hasEncodingIssue(addressDto.getTenPhuong())
                        || TextEncodingGuard.hasEncodingIssue(addressDto.getDiaChiChiTiet()))) {
                    System.err.println("Dữ liệu địa chỉ có dấu hiệu lỗi mã hóa");
                    return false;
                }
                if (!dto.isValidPersonalInfo()) {
                    System.err.println("❌ Thông tin cá nhân không hợp lệ");
                    return false;
                }
                if (!isValidPhoneNumber(dto.getSdt())) {
                    System.err.println("❌ Số điện thoại không hợp lệ: " + dto.getSdt());
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            System.err.println("Lỗi validation DTO: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validateUpdateAccountDto(TaiKhoanDTO dto, Integer accountId) {
        try {
            if (dto == null || accountId == null) return false;

            // Kiểm tra tài khoản có tồn tại
            if (findById(accountId).isEmpty()) return false;

            // Validate email nếu có
            if (dto.getEmail() != null && !isValidEmail(dto.getEmail())) return false;

            // Validate mật khẩu nếu có
            if (dto.getMatKhau() != null && !dto.getMatKhau().isEmpty() && !isValidPassword(dto.getMatKhau())) {
                return false;
            }

            // Validate số điện thoại nếu có
            if (dto.getSdt() != null && !dto.getSdt().isEmpty() && !isValidPhoneNumber(dto.getSdt())) {
                return false;
            }

            if (TextEncodingGuard.hasEncodingIssue(dto.getHoTen())) {
                return false;
            }

            return true;

        } catch (Exception e) {
            System.err.println("Lỗi validation DTO cập nhật: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return EMAIL_REGEX.matcher(email.trim()).matches();
    }

    @Override
    public boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) return false;
        return password.length() >= 6 && password.length() <= 50;
    }

    @Override
    public boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
        return phone.trim().matches(PHONE_PATTERN);
    }

    @Override
    public boolean isValidSearchParams(String email, String maTaiKhoan, String vaiTro, Integer trangThai) {
        try {
            // Validate email format nếu có
            if (email != null && !email.trim().isEmpty() && !isValidEmail(email)) {
                return false;
            }

            // Validate vai trò nếu có
            if (vaiTro != null && !vaiTro.trim().isEmpty()) {
                if (parseVaiTro(vaiTro) == null) {
                    return false;
                }
            }

            // Validate trạng thái nếu có
            if (trangThai != null && trangThai != 0 && trangThai != 1) {
                return false;
            }

            return true;

        } catch (Exception e) {
            System.err.println("Lỗi validation tham số tìm kiếm: " + e.getMessage());
            return false;
        }
    }

    // ================== BUSINESS RULES ==================

    @Override
    public boolean canDeleteAccount(Integer id) {
        try {
            Optional<TaiKhoan> accountOpt = findById(id);
            if (accountOpt.isEmpty()) {
                return false;
            }

            TaiKhoan account = accountOpt.get();

            // Quy tắc 1: Không thể xóa admin cuối cùng đang hoạt động
            if (account.getVaiTro() == TaiKhoan.VaiTro.ADMIN && account.getTrangThai() == 1) {
                long activeAdminCount = countActiveByVaiTro(TaiKhoan.VaiTro.ADMIN);
                if (activeAdminCount <= 1) {
                    System.out.println("❌ Không thể xóa admin cuối cùng đang hoạt động");
                    return false;
                }
            }

            // Quy tắc 2: Kiểm tra dữ liệu liên quan quan trọng
            try {
                int relatedDataCount = taiKhoanRepository.countRelatedData(id);
                List<String> relatedTables = taiKhoanRepository.getRelatedTables(id);

                System.out.println("Dữ liệu liên quan cho tài khoản " + id + ": " + relatedDataCount + " records");
                System.out.println("Bảng có dữ liệu: " + String.join(", ", relatedTables));

                // Cho phép xóa nếu chỉ có dữ liệu trong các bảng "an toàn"
                List<String> safeTables = Arrays.asList("dia_chi", "khach_hang", "nhan_vien", "tai_khoan_voucher");
                boolean canDelete = relatedTables.stream().allMatch(safeTables::contains);

                if (!canDelete) {
                    System.out.println("❌ Tài khoản có dữ liệu quan trọng không thể xóa");
                    return false;
                }

            } catch (Exception e) {
                System.err.println("Lỗi kiểm tra dữ liệu liên quan: " + e.getMessage());
                // Nếu không kiểm tra được, cho phép xóa nhưng cảnh báo
                System.out.println("⚠️ Không thể kiểm tra dữ liệu liên quan, tiếp tục xóa...");
            }

            return true;

        } catch (Exception e) {
            System.err.println("Lỗi kiểm tra khả năng xóa tài khoản: " + e.getMessage());
            return false;
        }
    }
    @Transactional(rollbackFor = Exception.class)
    public void safeDeleteById(Integer id) {
        try {
            System.out.println("=== XÓA AN TOÀN TÀI KHOẢN ===");

            // Kiểm tra trước khi xóa
            if (!canDeleteAccount(id)) {
                throw new IllegalStateException("Không được phép xóa tài khoản này");
            }

            Optional<TaiKhoan> accountOpt = findById(id);
            if (accountOpt.isEmpty()) {
                throw new IllegalArgumentException("Không tìm thấy tài khoản");
            }

            TaiKhoan account = accountOpt.get();

            // Thử xóa từng bước với checkpoint
            executeDeleteSteps(id, account);

            System.out.println("✅ Xóa tài khoản thành công: " + id);

        } catch (Exception e) {
            System.err.println("❌ Lỗi xóa tài khoản: " + e.getMessage());
            // Transaction sẽ tự động rollback do @Transactional(rollbackFor = Exception.class)
            throw new RuntimeException("Xóa tài khoản thất bại: " + e.getMessage(), e);
        }
    }

    private void executeDeleteSteps(Integer id, TaiKhoan account) {
        // Bước 1: Xóa voucher (quan trọng nhất - là nguyên nhân lỗi)
        try {
            taiKhoanRepository.deleteAccountVouchers(id);
            System.out.println("✅ Step 1: Đã xóa voucher");
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xóa voucher: " + e.getMessage(), e);
        }

        // Bước 2: Xóa đơn hàng (nếu có)
        try {
            taiKhoanRepository.deleteAccountOrderDetails(id);
            taiKhoanRepository.deleteAccountOrders(id);
            System.out.println("✅ Step 2: Đã xóa đơn hàng");
        } catch (Exception e) {
            System.out.println("⚠️ Step 2: Không có đơn hàng để xóa");
        }

        // Bước 3: Xóa địa chỉ
        try {
            if (diaChiService != null) {
                diaChiService.deleteByTaiKhoanId(id);
            }
            System.out.println("✅ Step 3: Đã xóa địa chỉ");
        } catch (Exception e) {
            System.out.println("⚠️ Step 3: Lỗi xóa địa chỉ: " + e.getMessage());
        }

        // Bước 4: Xóa thông tin khách hàng/nhân viên
        try {
            if (account.getVaiTro() == TaiKhoan.VaiTro.USER && khachHangService != null) {
                khachHangService.deleteByTaiKhoanId(id);
            } else if (account.getVaiTro() == TaiKhoan.VaiTro.NHANVIEN && nhanVienService != null) {
                nhanVienService.deleteByTaiKhoanId(id);
            }
            System.out.println("✅ Step 4: Đã xóa thông tin cá nhân");
        } catch (Exception e) {
            System.out.println("⚠️ Step 4: Lỗi xóa thông tin cá nhân: " + e.getMessage());
        }

        // Bước 5: Xóa tài khoản chính
        try {
            taiKhoanRepository.deleteById(id);
            System.out.println("✅ Step 5: Đã xóa tài khoản chính");
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xóa tài khoản chính: " + e.getMessage(), e);
        }
    }
    @Override
    public boolean canChangeRole(Integer accountId, TaiKhoan.VaiTro newRole) {
        try {
            Optional<TaiKhoan> accountOpt = findById(accountId);
            if (accountOpt.isEmpty()) return false;

            TaiKhoan account = accountOpt.get();

            // Không thể đổi vai trò từ ADMIN nếu là admin hoạt động cuối cùng
            if (account.getVaiTro() == TaiKhoan.VaiTro.ADMIN &&
                    newRole != TaiKhoan.VaiTro.ADMIN &&
                    account.getTrangThai() == 1) {

                if (isLastActiveAdmin(accountId)) {
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            System.err.println("Lỗi kiểm tra khả năng đổi vai trò: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isLastActiveAdmin(Integer accountId) {
        try {
            long activeAdminCount = countActiveByVaiTro(TaiKhoan.VaiTro.ADMIN);

            Optional<TaiKhoan> accountOpt = findById(accountId);
            if (accountOpt.isEmpty()) return false;

            TaiKhoan account = accountOpt.get();
            boolean isActiveAdmin = account.getVaiTro() == TaiKhoan.VaiTro.ADMIN && account.getTrangThai() == 1;

            return isActiveAdmin && activeAdminCount <= 1;

        } catch (Exception e) {
            System.err.println("Lỗi kiểm tra admin cuối cùng: " + e.getMessage());
            return false;
        }
    }

    // ================== UTILITY METHODS ==================

    @Override
    public String generateMaTaiKhoan() {
        try {
            String prefix = "TK";
            long timestamp = System.currentTimeMillis();
            int random = (int) (Math.random() * 1000);

            String maTaiKhoan;
            int attempts = 0;

            do {
                maTaiKhoan = prefix + String.format("%d%03d", (timestamp + attempts) % 100000, random);
                attempts++;
            } while (existsByMaTaiKhoan(maTaiKhoan) && attempts < 1000);

            if (attempts >= 1000) {
                throw new RuntimeException("Không thể tạo mã tài khoản unique sau 1000 lần thử");
            }

            return maTaiKhoan;

        } catch (Exception e) {
            System.err.println("Lỗi tạo mã tài khoản: " + e.getMessage());
            return "TK" + System.currentTimeMillis();
        }
    }

    @Override
    public TaiKhoan.VaiTro parseVaiTro(String vaiTroString) {
        if (vaiTroString == null || vaiTroString.trim().isEmpty()) {
            return null;
        }

        try {
            String role = vaiTroString.trim().toUpperCase();
            switch (role) {
                case "USER":
                case "KHACHHANG":
                case "KHÁCH HÀNG":
                    return TaiKhoan.VaiTro.USER;
                case "NHANVIEN":
                case "NHÂN VIÊN":
                case "EMPLOYEE":
                    return TaiKhoan.VaiTro.NHANVIEN;
                case "ADMIN":
                case "ADMINISTRATOR":
                case "QUẢN TRỊ":
                    return TaiKhoan.VaiTro.ADMIN;
                default:
                    return null;
            }
        } catch (Exception e) {
            System.err.println("Lỗi parse vai trò: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }

    @Override
    public String hashPassword(String password) {
        try {
            return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(password);
        } catch (Exception e) {
            System.err.println("Error hashing password: " + e.getMessage());
            throw new RuntimeException("Không thể hash mật khẩu");
        }
    }

    @Override
    public boolean verifyPassword(String password, String hashedPassword) {
        try {
            return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().matches(password, hashedPassword);
        } catch (Exception e) {
            System.err.println("Error verifying password: " + e.getMessage());
            return false;
        }
    }

    // ================== ADDRESS RELATED ==================

    @Override
    public boolean hasRequiredAddressData(TaiKhoanDTO dto) {
        if (dto.isAdmin()) return true; // Admin không cần địa chỉ

        TaiKhoanDTO.DiaChiDto address = dto.getEffectiveAddress();
        if (address == null) return false;

        return (address.getTenTinh() != null && !address.getTenTinh().trim().isEmpty()) ||
                (address.getTenPhuong() != null && !address.getTenPhuong().trim().isEmpty()) ||
                (address.getMaTinh() != null && !address.getMaTinh().trim().isEmpty()) ||
                (address.getMaPhuong() != null && !address.getMaPhuong().trim().isEmpty());
    }

    @Override
    public void validateAddressData(TaiKhoanDTO dto) {
        if (!dto.hasAddressData()) return;

        TaiKhoanDTO.DiaChiDto address = dto.getEffectiveAddress();
        if (address == null) {
            throw new IllegalArgumentException("Dữ liệu địa chỉ không hợp lệ");
        }

        boolean hasLocation = (address.getTenTinh() != null && !address.getTenTinh().trim().isEmpty()) ||
                (address.getTenPhuong() != null && !address.getTenPhuong().trim().isEmpty());

        if (!hasLocation) {
            throw new IllegalArgumentException("Địa chỉ phải có ít nhất tên tỉnh hoặc tên phường");
        }
    }

    // ================== ACCOUNT MAINTENANCE ==================

    @Override
    public int cleanupInactiveAccounts(int daysInactive) {
        try {
            Date cutoffDate = new Date(System.currentTimeMillis() - (daysInactive * 24 * 60 * 60 * 1000L));

            List<TaiKhoan> inactiveAccounts = findAll().stream()
                    .filter(account -> account.getTrangThai() == 0)
                    .filter(account -> account.getNgayCapNhat() != null && account.getNgayCapNhat().before(cutoffDate))
                    .filter(account -> account.getVaiTro() != TaiKhoan.VaiTro.ADMIN) // Không bao giờ dọn dẹp admin
                    .collect(Collectors.toList());

            int cleanedCount = 0;
            for (TaiKhoan account : inactiveAccounts) {
                try {
                    if (canDeleteAccount(account.getId())) {
                        deleteById(account.getId());
                        cleanedCount++;
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi dọn dẹp tài khoản " + account.getId() + ": " + e.getMessage());
                }
            }

            return cleanedCount;

        } catch (Exception e) {
            System.err.println("Lỗi dọn dẹp tài khoản không hoạt động: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public Map<String, Object> exportAccountData(Integer accountId) {
        try {
            Optional<TaiKhoan> accountOpt = findById(accountId);
            if (accountOpt.isEmpty()) {
                return new HashMap<>();
            }

            TaiKhoan account = accountOpt.get();
            Map<String, Object> exportData = new HashMap<>();

            exportData.put("id", account.getId());
            exportData.put("maTaiKhoan", account.getMaTaiKhoan());
            exportData.put("email", account.getEmail());
            exportData.put("vaiTro", account.getVaiTro().name());
            exportData.put("trangThai", account.getTrangThai());
            exportData.put("ngayTao", account.getNgayTao());
            exportData.put("ngayCapNhat", account.getNgayCapNhat());
            exportData.put("exportTime", new Date());

            return exportData;

        } catch (Exception e) {
            System.err.println("Lỗi export dữ liệu tài khoản: " + e.getMessage());
            return new HashMap<>();
        }
    }

    @Override
    public void logAccountActivity(Integer accountId, String activity, String details) {
        try {
            // TODO: Thực hiện ghi log hoạt động thực sự (database, file, etc.)
            System.out.println(String.format("[ACCOUNT_LOG] ID: %d, Hoạt động: %s, Chi tiết: %s, Thời gian: %s",
                    accountId, activity, details, new Date()));
        } catch (Exception e) {
            System.err.println("Lỗi ghi log hoạt động tài khoản: " + e.getMessage());
        }
    }

    // ================== HELPER METHODS - PHƯƠNG THỨC HỖ TRỢ ==================

    private KhachHang createKhachHangForAccount(TaiKhoanDTO dto, TaiKhoan taiKhoan) {
        try {
            KhachHang khachHang = new KhachHang();
            khachHang.setTaiKhoan(taiKhoan);
            khachHang.setMaKhachHang(generateMaKhachHang());
            khachHang.setHoTen(dto.getHoTen());
            khachHang.setSdt(dto.getSdt());
            khachHang.setTrangThai(1);
            khachHang.setNgayTao(new Date());
            khachHang.setNgayCapNhat(new Date());

            khachHangService.addKhachHang(khachHang);
            return khachHang;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo KhachHang: " + e.getMessage(), e);
        }
    }

    private NhanVien createNhanVienForAccount(TaiKhoanDTO dto, TaiKhoan taiKhoan) {
        try {
            NhanVien nhanVien = new NhanVien();
            nhanVien.setTaiKhoan(taiKhoan);
            nhanVien.setMaNhanVien(generateMaNhanVien());
            nhanVien.setHoTen(dto.getHoTen());
            nhanVien.setSdt(dto.getSdt());
            nhanVien.setTrangThai(1);
            nhanVien.setNgayTao(new Date());
            nhanVien.setNgayCapNhat(new Date());

            nhanVienService.addNhanVien(nhanVien);
            return nhanVien;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo NhanVien: " + e.getMessage(), e);
        }
    }

    private DiaChi createAddressForAccount(TaiKhoanDTO dto, TaiKhoan taiKhoan) {
        try {
            TaiKhoanDTO.DiaChiDto addressDto = dto.getEffectiveAddress();
            if (addressDto == null) {
                throw new IllegalArgumentException("Không có dữ liệu địa chỉ");
            }

            DiaChi diaChi = new DiaChi();
            diaChi.setTaiKhoan(taiKhoan);
            diaChi.setMaTinh(addressDto.getMaTinh() != null ? addressDto.getMaTinh() : "01");
            diaChi.setMaPhuong(addressDto.getMaPhuong() != null ? addressDto.getMaPhuong() : "00001");
            diaChi.setTenTinh(addressDto.getTenTinh() != null ? addressDto.getTenTinh() : ("Tỉnh " + diaChi.getMaTinh()));
            diaChi.setTenPhuong(addressDto.getTenPhuong() != null ? addressDto.getTenPhuong() : ("Phường " + diaChi.getMaPhuong()));
            diaChi.setDiaChiChiTiet(addressDto.getDiaChiChiTiet() != null ? addressDto.getDiaChiChiTiet() : "");
            diaChi.setIsDefault(true);
            diaChi.setTrangThai(1);
            diaChi.setNgayTao(new Date());
            diaChi.setNgayCapNhat(new Date());

            return diaChiService.save(diaChi);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo DiaChi: " + e.getMessage(), e);
        }
    }

    private boolean shouldCreateAddress(TaiKhoanDTO dto) {
        return !dto.isAdmin() && dto.hasAddressData() &&
                dto.getEffectiveAddress() != null && dto.getEffectiveAddress().isValid();
    }

    private String generateMaKhachHang() {
        return "KH" + System.currentTimeMillis() % 100000;
    }

    private String generateMaNhanVien() {
        return "NV" + System.currentTimeMillis() % 100000;
    }

    private void sortAccountList(List<TaiKhoan> list, String sortBy, String sortDir) {
        if (list == null || list.isEmpty() || sortBy == null) return;

        list.sort((a, b) -> {
            int result = 0;
            switch (sortBy.toLowerCase()) {
                case "id":
                    result = a.getId().compareTo(b.getId());
                    break;
                case "email":
                    result = Optional.ofNullable(a.getEmail()).orElse("")
                            .compareToIgnoreCase(Optional.ofNullable(b.getEmail()).orElse(""));
                    break;
                case "mataikhoan":
                    result = Optional.ofNullable(a.getMaTaiKhoan()).orElse("")
                            .compareToIgnoreCase(Optional.ofNullable(b.getMaTaiKhoan()).orElse(""));
                    break;
                case "vaitro":
                    result = a.getVaiTro().compareTo(b.getVaiTro());
                    break;
                case "ngaytao":
                    result = Optional.ofNullable(a.getNgayTao()).orElse(new Date(0))
                            .compareTo(Optional.ofNullable(b.getNgayTao()).orElse(new Date(0)));
                    break;
                case "trangthai":
                    result = a.getTrangThai().compareTo(b.getTrangThai());
                    break;
                default:
                    result = a.getId().compareTo(b.getId());
            }
            return "desc".equalsIgnoreCase(sortDir) ? -result : result;
        });
    }
}
