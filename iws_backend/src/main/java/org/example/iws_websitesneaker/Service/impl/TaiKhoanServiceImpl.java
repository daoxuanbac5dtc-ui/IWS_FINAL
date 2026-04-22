package org.example.iws_websitesneaker.Service.impl;

import org.example.iws_websitesneaker.Dto.TaiKhoanDTO;
import org.example.iws_websitesneaker.Service.TaiKhoanService;
import org.example.iws_websitesneaker.Service.DiaChiService;
import org.example.iws_websitesneaker.Service.KhachHangService;
import org.example.iws_websitesneaker.Service.NhanVienService;
import org.example.iws_websitesneaker.entity.*;
import org.example.iws_websitesneaker.repository.RepoTaiKhoan;
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

    // ===== CONSTANTS - CÃC Háº°NG Sá» =====
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
            System.err.println("Lá»—i tÃ¬m táº¥t cáº£ tÃ i khoáº£n: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<TaiKhoan> findById(Integer id) {
        try {
            return taiKhoanRepository.findById(id);
        } catch (Exception e) {
            System.err.println("Lá»—i tÃ¬m tÃ i khoáº£n theo ID: " + e.getMessage());
            return Optional.empty();
        }
    }
    @Override
    public boolean updatePassword(String email, String newPassword) {
        try {
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanRepository.findByEmail(email.toLowerCase().trim());

            if (taiKhoanOpt.isPresent()) {
                TaiKhoan taiKhoan = taiKhoanOpt.get();

                // Kiá»ƒm tra tÃ i khoáº£n cÃ³ active khÃ´ng
                if (!taiKhoan.isActive()) {
                    System.err.println("Account is inactive: " + taiKhoan.getMaTaiKhoan());
                    return false;
                }

                // Hash máº­t kháº©u má»›i báº±ng Spring Security BCrypt
                String hashedPassword = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(newPassword);
                taiKhoan.setMatKhau(hashedPassword);
                taiKhoan.setNgayCapNhat(new Date());

                taiKhoanRepository.save(taiKhoan);

                System.out.println("âœ… Password updated successfully for account: " + taiKhoan.getMaTaiKhoan());
                return true;
            } else {
                System.err.println("âŒ Account not found for email: " + email);
                return false;
            }

        } catch (Exception e) {
            System.err.println("âŒ Error updating password: " + e.getMessage());
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
            throw new RuntimeException("Lá»—i rÃ ng buá»™c database: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Lá»—i lÆ°u tÃ i khoáº£n: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Integer id) {
        try {
            System.out.println("=== XÃ“A TÃ€I KHOáº¢N SERVICE ===");
            System.out.println("Äang xÃ³a tÃ i khoáº£n ID: " + id);

            if (!canDeleteAccount(id)) {
                throw new IllegalStateException("KhÃ´ng thá»ƒ xÃ³a tÃ i khoáº£n nÃ y");
            }

            // Láº¥y thÃ´ng tin tÃ i khoáº£n trÆ°á»›c khi xÃ³a
            Optional<TaiKhoan> accountOpt = findById(id);
            if (accountOpt.isEmpty()) {
                throw new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y tÃ i khoáº£n vá»›i ID: " + id);
            }

            TaiKhoan account = accountOpt.get();
            System.out.println("TÃ i khoáº£n cáº§n xÃ³a: " + account.getEmail() + " (" + account.getVaiTro() + ")");

            // BÆ°á»›c 1: XÃ³a táº¥t cáº£ dá»¯ liá»‡u liÃªn quan theo thá»© tá»± Ä‘Ãºng
            try {
                // 1.1: XÃ³a voucher cá»§a tÃ i khoáº£n (báº£ng tai_khoan_voucher)
                deleteAccountVouchers(id);

                // 1.2: XÃ³a cÃ¡c Ä‘Æ¡n hÃ ng vÃ  dá»¯ liá»‡u giao dá»‹ch (náº¿u cÃ³)
                deleteAccountOrders(id);

                // 1.3: XÃ³a Ä‘á»‹a chá»‰
                if (diaChiService != null) {
                    try {
                        diaChiService.deleteByTaiKhoanId(id);
                        System.out.println("âœ… ÄÃ£ xÃ³a Ä‘á»‹a chá»‰ cho tÃ i khoáº£n: " + id);
                    } catch (Exception e) {
                        System.err.println("âš ï¸ Cáº£nh bÃ¡o: KhÃ´ng thá»ƒ xÃ³a Ä‘á»‹a chá»‰: " + e.getMessage());
                    }
                }

                // 1.4: XÃ³a entity theo vai trÃ²
                if (account.getVaiTro() == TaiKhoan.VaiTro.USER) {
                    try {
                        if (khachHangService != null) {
                            khachHangService.deleteByTaiKhoanId(id);
                            System.out.println("âœ… ÄÃ£ xÃ³a dá»¯ liá»‡u khÃ¡ch hÃ ng cho tÃ i khoáº£n: " + id);
                        }
                    } catch (Exception e) {
                        System.err.println("âš ï¸ Cáº£nh bÃ¡o: KhÃ´ng thá»ƒ xÃ³a dá»¯ liá»‡u khÃ¡ch hÃ ng: " + e.getMessage());
                    }
                } else if (account.getVaiTro() == TaiKhoan.VaiTro.NHANVIEN) {
                    try {
                        if (nhanVienService != null) {
                            nhanVienService.deleteByTaiKhoanId(id);
                            System.out.println("âœ… ÄÃ£ xÃ³a dá»¯ liá»‡u nhÃ¢n viÃªn cho tÃ i khoáº£n: " + id);
                        }
                    } catch (Exception e) {
                        System.err.println("âš ï¸ Cáº£nh bÃ¡o: KhÃ´ng thá»ƒ xÃ³a dá»¯ liá»‡u nhÃ¢n viÃªn: " + e.getMessage());
                    }
                }

            } catch (Exception relatedError) {
                System.err.println("âŒ Lá»—i xÃ³a entity liÃªn quan: " + relatedError.getMessage());
                throw new RuntimeException("KhÃ´ng thá»ƒ xÃ³a tÃ i khoáº£n do cÃ³ dá»¯ liá»‡u liÃªn quan khÃ´ng thá»ƒ xÃ³a: " + relatedError.getMessage());
            }

            // BÆ°á»›c 2: XÃ³a tÃ i khoáº£n chÃ­nh
            try {
                taiKhoanRepository.deleteById(id);
                System.out.println("âœ… XÃ³a tÃ i khoáº£n thÃ nh cÃ´ng: " + id);

                // Ghi log viá»‡c xÃ³a
                logAccountActivity(id, "DELETE", "TÃ i khoáº£n Ä‘Ã£ Ä‘Æ°á»£c xÃ³a thÃ nh cÃ´ng");

            } catch (Exception mainDeleteError) {
                System.err.println("âŒ Lá»—i xÃ³a tÃ i khoáº£n chÃ­nh: " + mainDeleteError.getMessage());

                // Kiá»ƒm tra lá»—i database cá»¥ thá»ƒ
                if (mainDeleteError.getMessage().contains("constraint") ||
                        mainDeleteError.getMessage().contains("foreign key") ||
                        mainDeleteError.getMessage().contains("REFERENCE")) {
                    throw new RuntimeException("KhÃ´ng thá»ƒ xÃ³a tÃ i khoáº£n: váº«n cÃ²n dá»¯ liá»‡u liÃªn quan chÆ°a Ä‘Æ°á»£c xÃ³a");
                }

                throw new RuntimeException("KhÃ´ng thá»ƒ xÃ³a tÃ i khoáº£n: " + mainDeleteError.getMessage());
            }

        } catch (Exception e) {
            System.err.println("âŒ XÃ³a tÃ i khoáº£n tháº¥t báº¡i: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("KhÃ´ng thá»ƒ xÃ³a tÃ i khoáº£n: " + e.getMessage(), e);
        }
    }
    private void deleteAccountVouchers(Integer accountId) {
        try {
            // Sá»­ dá»¥ng SQL Ä‘á»ƒ xÃ³a trá»±c tiáº¿p tá»« báº£ng tai_khoan_voucher
            taiKhoanRepository.deleteAccountVouchers(accountId);
            System.out.println("âœ… ÄÃ£ xÃ³a voucher cho tÃ i khoáº£n: " + accountId);
        } catch (Exception e) {
            System.err.println("âŒ Lá»—i xÃ³a voucher: " + e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ xÃ³a voucher cá»§a tÃ i khoáº£n: " + e.getMessage());
        }
    }

    /**
     * XÃ³a táº¥t cáº£ Ä‘Æ¡n hÃ ng vÃ  dá»¯ liá»‡u giao dá»‹ch cá»§a tÃ i khoáº£n
     */
    private void deleteAccountOrders(Integer accountId) {
        try {
            // XÃ³a cÃ¡c báº£ng liÃªn quan Ä‘áº¿n Ä‘Æ¡n hÃ ng theo thá»© tá»±:
            // 1. XÃ³a chi tiáº¿t Ä‘Æ¡n hÃ ng
            // 2. XÃ³a Ä‘Æ¡n hÃ ng
            // 3. XÃ³a cÃ¡c giao dá»‹ch khÃ¡c

            taiKhoanRepository.deleteAccountOrders(accountId);
            System.out.println("âœ… ÄÃ£ xÃ³a Ä‘Æ¡n hÃ ng cho tÃ i khoáº£n: " + accountId);
        } catch (Exception e) {
            System.err.println("âŒ Lá»—i xÃ³a Ä‘Æ¡n hÃ ng: " + e.getMessage());
            // CÃ³ thá»ƒ khÃ´ng cÃ³ Ä‘Æ¡n hÃ ng nÃ o, chá»‰ log warning
            System.err.println("âš ï¸ Tiáº¿p tá»¥c xÃ³a tÃ i khoáº£n...");
        }
    }
    // ================== ACCOUNT CREATION - MAIN METHODS ==================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createCompleteAccount(TaiKhoanDTO dto) {
        System.out.println("=== Táº¡o tÃ i khoáº£n hoÃ n chá»‰nh ===");

        try {
            // KIá»‚M TRA TRÃ™NG Láº¶P TRÆ¯á»šC KHI Báº®T Äáº¦U TRANSACTION
            if (!validateCreateAccountDto(dto)) {
                throw new IllegalArgumentException("Dá»¯ liá»‡u tÃ i khoáº£n khÃ´ng há»£p lá»‡");
            }

            if (existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email Ä‘Ã£ tá»“n táº¡i: " + dto.getEmail());
            }

            // KIá»‚M TRA TRÃ™NG Láº¶P Sá» ÄIá»†N THOáº I
            if (dto.needsPersonalInfo() && dto.getSdt() != null) {
                String cleanPhone = dto.getSdt().trim().replaceAll("\\s+", "");

                if (dto.getVaiTro() == TaiKhoan.VaiTro.NHANVIEN || "NHANVIEN".equals(dto.getVaiTroString())) {
                    if (nhanVienService.existsBySdt(cleanPhone)) {
                        throw new IllegalArgumentException("Sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng: " + cleanPhone);
                    }
                } else if (dto.getVaiTro() == TaiKhoan.VaiTro.USER || "USER".equals(dto.getVaiTroString())) {
                    if (khachHangService.existsBySdt(cleanPhone)) {
                        throw new IllegalArgumentException("Sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng: " + cleanPhone);
                    }
                }
            }

            // Táº¡o tÃ i khoáº£n chÃ­nh
            TaiKhoan taiKhoan = createTaiKhoan(dto);
            Map<String, Object> result = new HashMap<>();
            result.put("taiKhoan", taiKhoan);

            // Táº¡o entities liÃªn quan
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
                System.err.println("Cáº£nh bÃ¡o: Lá»—i táº¡o dá»¯ liá»‡u liÃªn quan: " + relatedError.getMessage());
                throw new RuntimeException("Lá»—i táº¡o " + getRoleDisplayName(taiKhoan.getVaiTro()) + ": " + relatedError.getMessage(), relatedError);
            }

            logAccountActivity(taiKhoan.getId(), "CREATE", "TÃ i khoáº£n Ä‘Æ°á»£c táº¡o thÃ nh cÃ´ng");
            return result;

        } catch (Exception e) {
            System.err.println("Lá»—i táº¡o tÃ i khoáº£n: " + e.getMessage());
            throw e;
        }
    }

    private String getRoleDisplayName(TaiKhoan.VaiTro vaiTro) {
        switch (vaiTro) {
            case USER: return "khÃ¡ch hÃ ng";
            case NHANVIEN: return "nhÃ¢n viÃªn";
            case ADMIN: return "admin";
            default: return "ngÆ°á»i dÃ¹ng";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaiKhoan createTaiKhoan(TaiKhoanDTO dto) {
        try {
            if (!validateCreateAccountDto(dto)) {
                throw new IllegalArgumentException("Dá»¯ liá»‡u tÃ i khoáº£n khÃ´ng há»£p lá»‡");
            }

            if (existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email Ä‘Ã£ tá»“n táº¡i");
            }

            TaiKhoan taiKhoan = new TaiKhoan();
            taiKhoan.setMaTaiKhoan(dto.getMaTaiKhoan() != null ? dto.getMaTaiKhoan() : generateMaTaiKhoan());
            taiKhoan.setEmail(normalizeEmail(dto.getEmail()));
            taiKhoan.setMatKhau(hashPassword(dto.getMatKhau()));

            // Xá»­ lÃ½ vai trÃ²
            if (dto.getVaiTro() != null) {
                taiKhoan.setVaiTro(dto.getVaiTro());
            } else if (dto.getVaiTroString() != null) {
                TaiKhoan.VaiTro role = parseVaiTro(dto.getVaiTroString());
                if (role == null) {
                    throw new IllegalArgumentException("Vai trÃ² khÃ´ng há»£p lá»‡: " + dto.getVaiTroString());
                }
                taiKhoan.setVaiTro(role);
            } else {
                throw new IllegalArgumentException("Vai trÃ² lÃ  báº¯t buá»™c");
            }

            taiKhoan.setTrangThai(dto.getTrangThai() != null ? dto.getTrangThai() : 1);
            taiKhoan.setNgayTao(new Date());
            taiKhoan.setNgayCapNhat(new Date());

            return taiKhoanRepository.save(taiKhoan);

        } catch (Exception e) {
            System.err.println("Lá»—i táº¡o TaiKhoan: " + e.getMessage());
            throw new RuntimeException("Lá»—i táº¡o tÃ i khoáº£n: " + e.getMessage(), e);
        }
    }

    @Override
    public TaiKhoan createAccount(String email, String password, TaiKhoan.VaiTro vaiTro) {
        try {
            if (!isValidEmail(email)) {
                throw new IllegalArgumentException("Email khÃ´ng há»£p lá»‡");
            }
            if (!isValidPassword(password)) {
                throw new IllegalArgumentException("Máº­t kháº©u khÃ´ng há»£p lá»‡");
            }
            if (existsByEmail(email)) {
                throw new IllegalArgumentException("Email Ä‘Ã£ tá»“n táº¡i");
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
            throw new RuntimeException("Lá»—i táº¡o tÃ i khoáº£n Ä‘Æ¡n giáº£n: " + e.getMessage(), e);
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
            System.err.println("Lá»—i xÃ¡c thá»±c: " + e.getMessage());
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

            logAccountActivity(id, "PASSWORD_CHANGE", "Äá»•i máº­t kháº©u thÃ nh cÃ´ng");
            return true;

        } catch (Exception e) {
            System.err.println("Lá»—i Ä‘á»•i máº­t kháº©u: " + e.getMessage());
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

            logAccountActivity(id, "PASSWORD_RESET", "Reset máº­t kháº©u bá»Ÿi admin");
            return true;

        } catch (Exception e) {
            System.err.println("Lá»—i reset máº­t kháº©u: " + e.getMessage());
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

            // Logic phÃ¢n quyá»n cÆ¡ báº£n theo vai trÃ²
            switch (account.getVaiTro()) {
                case ADMIN:
                    return true; // Admin cÃ³ táº¥t cáº£ quyá»n
                case NHANVIEN:
                    return !permission.startsWith("ADMIN_"); // NhÃ¢n viÃªn cÃ³ quyá»n non-admin
                case USER:
                    return permission.startsWith("USER_"); // User chá»‰ cÃ³ quyá»n user
                default:
                    return false;
            }

        } catch (Exception e) {
            System.err.println("Lá»—i kiá»ƒm tra quyá»n: " + e.getMessage());
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
            System.err.println("Lá»—i tÃ¬m theo email: " + e.getMessage());
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
            System.err.println("Lá»—i tÃ¬m theo mÃ£ tÃ i khoáº£n: " + e.getMessage());
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
            System.err.println("Lá»—i kiá»ƒm tra email tá»“n táº¡i: " + e.getMessage());
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
            System.err.println("Lá»—i kiá»ƒm tra mÃ£ tÃ i khoáº£n tá»“n táº¡i: " + e.getMessage());
            return false;
        }
    }

    // ================== ROLE-BASED QUERIES ==================

    @Override
    public List<TaiKhoan> findByVaiTro(TaiKhoan.VaiTro vaiTro) {
        try {
            return taiKhoanRepository.findByVaiTro(vaiTro);
        } catch (Exception e) {
            System.err.println("Lá»—i tÃ¬m theo vai trÃ²: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<TaiKhoan> findActiveCustomers() {
        try {
            return taiKhoanRepository.findByVaiTroAndTrangThai(TaiKhoan.VaiTro.USER, 1);
        } catch (Exception e) {
            System.err.println("Lá»—i tÃ¬m khÃ¡ch hÃ ng hoáº¡t Ä‘á»™ng: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<TaiKhoan> findActiveEmployees() {
        try {
            return taiKhoanRepository.findByVaiTroAndTrangThai(TaiKhoan.VaiTro.NHANVIEN, 1);
        } catch (Exception e) {
            System.err.println("Lá»—i tÃ¬m nhÃ¢n viÃªn hoáº¡t Ä‘á»™ng: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<TaiKhoan> findActiveAdmins() {
        try {
            return taiKhoanRepository.findByVaiTroAndTrangThai(TaiKhoan.VaiTro.ADMIN, 1);
        } catch (Exception e) {
            System.err.println("Lá»—i tÃ¬m admin hoáº¡t Ä‘á»™ng: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ================== STATUS MANAGEMENT ==================

    @Override
    public List<TaiKhoan> findByTrangThai(Integer trangThai) {
        try {
            return taiKhoanRepository.findByTrangThai(trangThai);
        } catch (Exception e) {
            System.err.println("Lá»—i tÃ¬m theo tráº¡ng thÃ¡i: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<TaiKhoan> findActiveAccounts() {
        try {
            return taiKhoanRepository.findByTrangThai(1);
        } catch (Exception e) {
            System.err.println("Lá»—i tÃ¬m tÃ i khoáº£n hoáº¡t Ä‘á»™ng: " + e.getMessage());
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

            // Kiá»ƒm tra admin cuá»‘i cÃ¹ng
            if (account.getVaiTro() == TaiKhoan.VaiTro.ADMIN && newStatus == 0) {
                if (isLastActiveAdmin(id)) {
                    throw new IllegalStateException("KhÃ´ng thá»ƒ vÃ´ hiá»‡u hÃ³a admin cuá»‘i cÃ¹ng");
                }
            }

            account.setTrangThai(newStatus);
            save(account);

            String action = newStatus == 1 ? "ACTIVATE" : "DEACTIVATE";
            logAccountActivity(id, action, "Tráº¡ng thÃ¡i Ä‘Ã£ Ä‘Æ°á»£c chuyá»ƒn Ä‘á»•i");
            return true;

        } catch (Exception e) {
            System.err.println("Lá»—i chuyá»ƒn Ä‘á»•i tráº¡ng thÃ¡i: " + e.getMessage());
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

            // Kiá»ƒm tra admin cuá»‘i cÃ¹ng
            if (account.getVaiTro() == TaiKhoan.VaiTro.ADMIN && isLastActiveAdmin(id)) {
                throw new IllegalStateException("KhÃ´ng thá»ƒ vÃ´ hiá»‡u hÃ³a admin cuá»‘i cÃ¹ng");
            }

            account.setTrangThai(0);
            save(account);

            logAccountActivity(id, "DEACTIVATE", "TÃ i khoáº£n Ä‘Ã£ bá»‹ vÃ´ hiá»‡u hÃ³a");
            return true;

        } catch (Exception e) {
            System.err.println("Lá»—i vÃ´ hiá»‡u hÃ³a tÃ i khoáº£n: " + e.getMessage());
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

            logAccountActivity(id, "ACTIVATE", "TÃ i khoáº£n Ä‘Ã£ Ä‘Æ°á»£c kÃ­ch hoáº¡t");
            return true;

        } catch (Exception e) {
            System.err.println("Lá»—i kÃ­ch hoáº¡t tÃ i khoáº£n: " + e.getMessage());
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
            System.err.println("Lá»—i tÃ¬m kiáº¿m theo tá»« khÃ³a: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<TaiKhoan> searchAdvanced(String email, String maTaiKhoan, TaiKhoan.VaiTro vaiTro,
                                         Integer trangThai, Date startDate, Date endDate) {
        try {
            return findAll().stream()
                    .filter(account -> {
                        // Lá»c theo email
                        if (email != null && !email.trim().isEmpty()) {
                            if (account.getEmail() == null ||
                                    !account.getEmail().toLowerCase().contains(email.toLowerCase())) {
                                return false;
                            }
                        }

                        // Lá»c theo mÃ£ tÃ i khoáº£n
                        if (maTaiKhoan != null && !maTaiKhoan.trim().isEmpty()) {
                            if (account.getMaTaiKhoan() == null ||
                                    !account.getMaTaiKhoan().toLowerCase().contains(maTaiKhoan.toLowerCase())) {
                                return false;
                            }
                        }

                        // Lá»c theo vai trÃ²
                        if (vaiTro != null && !account.getVaiTro().equals(vaiTro)) {
                            return false;
                        }

                        // Lá»c theo tráº¡ng thÃ¡i
                        if (trangThai != null && !account.getTrangThai().equals(trangThai)) {
                            return false;
                        }

                        // Lá»c theo khoáº£ng thá»i gian
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
            System.err.println("Lá»—i tÃ¬m kiáº¿m nÃ¢ng cao: " + e.getMessage());
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

            // Ãp dá»¥ng filters
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

            // Ãp dá»¥ng sorting
            sortAccountList(allResults, sortBy, sortDir);

            // Ãp dá»¥ng pagination
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
            System.err.println("Lá»—i tÃ¬m kiáº¿m cÃ³ phÃ¢n trang: " + e.getMessage());
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
            System.err.println("Lá»—i Ä‘áº¿m táº¥t cáº£ tÃ i khoáº£n: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public long countByVaiTro(TaiKhoan.VaiTro vaiTro) {
        try {
            return taiKhoanRepository.countByVaiTro(vaiTro);
        } catch (Exception e) {
            System.err.println("Lá»—i Ä‘áº¿m theo vai trÃ²: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public long countByTrangThai(Integer trangThai) {
        try {
            return taiKhoanRepository.countByTrangThai(trangThai);
        } catch (Exception e) {
            System.err.println("Lá»—i Ä‘áº¿m theo tráº¡ng thÃ¡i: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public long countActiveByVaiTro(TaiKhoan.VaiTro vaiTro) {
        try {
            return taiKhoanRepository.countActiveByVaiTro(vaiTro);
        } catch (Exception e) {
            System.err.println("Lá»—i Ä‘áº¿m hoáº¡t Ä‘á»™ng theo vai trÃ²: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public long countAccountsCreatedToday() {
        try {
            return taiKhoanRepository.countAccountsCreatedToday(new Date());
        } catch (Exception e) {
            System.err.println("Lá»—i Ä‘áº¿m tÃ i khoáº£n táº¡o hÃ´m nay: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public long countAccountsCreatedThisMonth() {
        try {
            return taiKhoanRepository.countAccountsCreatedThisMonth(new Date());
        } catch (Exception e) {
            System.err.println("Lá»—i Ä‘áº¿m tÃ i khoáº£n táº¡o thÃ¡ng nÃ y: " + e.getMessage());
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
            System.err.println("Lá»—i láº¥y thá»‘ng kÃª dashboard: " + e.getMessage());
            return new HashMap<>();
        }
    }

    // ================== DATE RANGE QUERIES ==================

    @Override
    public List<TaiKhoan> findByDateRange(Date startDate, Date endDate) {
        try {
            return taiKhoanRepository.findByNgayTaoBetween(startDate, endDate);
        } catch (Exception e) {
            System.err.println("Lá»—i tÃ¬m theo khoáº£ng ngÃ y: " + e.getMessage());
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
            System.err.println("Lá»—i tÃ¬m tÃ i khoáº£n má»›i nháº¥t: " + e.getMessage());
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
            System.err.println("Lá»—i tÃ¬m tÃ i khoáº£n gáº§n Ä‘Ã¢y: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ================== VALIDATION METHODS ==================

    @Override
    public boolean validateCreateAccountDto(TaiKhoanDTO dto) {
        try {
            if (dto == null) {
                System.err.println("âŒ DTO lÃ  null");
                return false;
            }

            // Validate thÃ´ng tin cÆ¡ báº£n
            if (!dto.isValidBasicInfo()) {
                System.err.println("âŒ ThÃ´ng tin cÆ¡ báº£n khÃ´ng há»£p lá»‡");
                return false;
            }

            if (!isValidEmail(dto.getEmail())) {
                System.err.println("âŒ Email khÃ´ng há»£p lá»‡: " + dto.getEmail());
                return false;
            }

            if (!isValidPassword(dto.getMatKhau())) {
                System.err.println("âŒ Máº­t kháº©u khÃ´ng há»£p lá»‡");
                return false;
            }

            // Validate vai trÃ²
            if (dto.getVaiTro() == null && (dto.getVaiTroString() == null || dto.getVaiTroString().trim().isEmpty())) {
                System.err.println("âŒ KhÃ´ng cÃ³ vai trÃ² Ä‘Æ°á»£c chá»‰ Ä‘á»‹nh");
                return false;
            }

            // Validate thÃ´ng tin cÃ¡ nhÃ¢n cho non-admin
            if (dto.needsPersonalInfo()) {
                if (!dto.isValidPersonalInfo()) {
                    System.err.println("âŒ ThÃ´ng tin cÃ¡ nhÃ¢n khÃ´ng há»£p lá»‡");
                    return false;
                }
                if (!isValidPhoneNumber(dto.getSdt())) {
                    System.err.println("âŒ Sá»‘ Ä‘iá»‡n thoáº¡i khÃ´ng há»£p lá»‡: " + dto.getSdt());
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            System.err.println("Lá»—i validation DTO: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validateUpdateAccountDto(TaiKhoanDTO dto, Integer accountId) {
        try {
            if (dto == null || accountId == null) return false;

            // Kiá»ƒm tra tÃ i khoáº£n cÃ³ tá»“n táº¡i
            if (findById(accountId).isEmpty()) return false;

            // Validate email náº¿u cÃ³
            if (dto.getEmail() != null && !isValidEmail(dto.getEmail())) return false;

            // Validate máº­t kháº©u náº¿u cÃ³
            if (dto.getMatKhau() != null && !dto.getMatKhau().isEmpty() && !isValidPassword(dto.getMatKhau())) {
                return false;
            }

            // Validate sá»‘ Ä‘iá»‡n thoáº¡i náº¿u cÃ³
            if (dto.getSdt() != null && !dto.getSdt().isEmpty() && !isValidPhoneNumber(dto.getSdt())) {
                return false;
            }

            return true;

        } catch (Exception e) {
            System.err.println("Lá»—i validation DTO cáº­p nháº­t: " + e.getMessage());
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
            // Validate email format náº¿u cÃ³
            if (email != null && !email.trim().isEmpty() && !isValidEmail(email)) {
                return false;
            }

            // Validate vai trÃ² náº¿u cÃ³
            if (vaiTro != null && !vaiTro.trim().isEmpty()) {
                if (parseVaiTro(vaiTro) == null) {
                    return false;
                }
            }

            // Validate tráº¡ng thÃ¡i náº¿u cÃ³
            if (trangThai != null && trangThai != 0 && trangThai != 1) {
                return false;
            }

            return true;

        } catch (Exception e) {
            System.err.println("Lá»—i validation tham sá»‘ tÃ¬m kiáº¿m: " + e.getMessage());
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

            // Quy táº¯c 1: KhÃ´ng thá»ƒ xÃ³a admin cuá»‘i cÃ¹ng Ä‘ang hoáº¡t Ä‘á»™ng
            if (account.getVaiTro() == TaiKhoan.VaiTro.ADMIN && account.getTrangThai() == 1) {
                long activeAdminCount = countActiveByVaiTro(TaiKhoan.VaiTro.ADMIN);
                if (activeAdminCount <= 1) {
                    System.out.println("âŒ KhÃ´ng thá»ƒ xÃ³a admin cuá»‘i cÃ¹ng Ä‘ang hoáº¡t Ä‘á»™ng");
                    return false;
                }
            }

            // Quy táº¯c 2: Kiá»ƒm tra dá»¯ liá»‡u liÃªn quan quan trá»ng
            try {
                int relatedDataCount = taiKhoanRepository.countRelatedData(id);
                List<String> relatedTables = taiKhoanRepository.getRelatedTables(id);

                System.out.println("Dá»¯ liá»‡u liÃªn quan cho tÃ i khoáº£n " + id + ": " + relatedDataCount + " records");
                System.out.println("Báº£ng cÃ³ dá»¯ liá»‡u: " + String.join(", ", relatedTables));

                // Cho phÃ©p xÃ³a náº¿u chá»‰ cÃ³ dá»¯ liá»‡u trong cÃ¡c báº£ng "an toÃ n"
                List<String> safeTables = Arrays.asList("dia_chi", "khach_hang", "nhan_vien", "tai_khoan_voucher");
                boolean canDelete = relatedTables.stream().allMatch(safeTables::contains);

                if (!canDelete) {
                    System.out.println("âŒ TÃ i khoáº£n cÃ³ dá»¯ liá»‡u quan trá»ng khÃ´ng thá»ƒ xÃ³a");
                    return false;
                }

            } catch (Exception e) {
                System.err.println("Lá»—i kiá»ƒm tra dá»¯ liá»‡u liÃªn quan: " + e.getMessage());
                // Náº¿u khÃ´ng kiá»ƒm tra Ä‘Æ°á»£c, cho phÃ©p xÃ³a nhÆ°ng cáº£nh bÃ¡o
                System.out.println("âš ï¸ KhÃ´ng thá»ƒ kiá»ƒm tra dá»¯ liá»‡u liÃªn quan, tiáº¿p tá»¥c xÃ³a...");
            }

            return true;

        } catch (Exception e) {
            System.err.println("Lá»—i kiá»ƒm tra kháº£ nÄƒng xÃ³a tÃ i khoáº£n: " + e.getMessage());
            return false;
        }
    }
    @Transactional(rollbackFor = Exception.class)
    public void safeDeleteById(Integer id) {
        try {
            System.out.println("=== XÃ“A AN TOÃ€N TÃ€I KHOáº¢N ===");

            // Kiá»ƒm tra trÆ°á»›c khi xÃ³a
            if (!canDeleteAccount(id)) {
                throw new IllegalStateException("KhÃ´ng Ä‘Æ°á»£c phÃ©p xÃ³a tÃ i khoáº£n nÃ y");
            }

            Optional<TaiKhoan> accountOpt = findById(id);
            if (accountOpt.isEmpty()) {
                throw new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y tÃ i khoáº£n");
            }

            TaiKhoan account = accountOpt.get();

            // Thá»­ xÃ³a tá»«ng bÆ°á»›c vá»›i checkpoint
            executeDeleteSteps(id, account);

            System.out.println("âœ… XÃ³a tÃ i khoáº£n thÃ nh cÃ´ng: " + id);

        } catch (Exception e) {
            System.err.println("âŒ Lá»—i xÃ³a tÃ i khoáº£n: " + e.getMessage());
            // Transaction sáº½ tá»± Ä‘á»™ng rollback do @Transactional(rollbackFor = Exception.class)
            throw new RuntimeException("XÃ³a tÃ i khoáº£n tháº¥t báº¡i: " + e.getMessage(), e);
        }
    }

    private void executeDeleteSteps(Integer id, TaiKhoan account) {
        // BÆ°á»›c 1: XÃ³a voucher (quan trá»ng nháº¥t - lÃ  nguyÃªn nhÃ¢n lá»—i)
        try {
            taiKhoanRepository.deleteAccountVouchers(id);
            System.out.println("âœ… Step 1: ÄÃ£ xÃ³a voucher");
        } catch (Exception e) {
            throw new RuntimeException("Lá»—i xÃ³a voucher: " + e.getMessage(), e);
        }

        // BÆ°á»›c 2: XÃ³a Ä‘Æ¡n hÃ ng (náº¿u cÃ³)
        try {
            taiKhoanRepository.deleteAccountOrderDetails(id);
            taiKhoanRepository.deleteAccountOrders(id);
            System.out.println("âœ… Step 2: ÄÃ£ xÃ³a Ä‘Æ¡n hÃ ng");
        } catch (Exception e) {
            System.out.println("âš ï¸ Step 2: KhÃ´ng cÃ³ Ä‘Æ¡n hÃ ng Ä‘á»ƒ xÃ³a");
        }

        // BÆ°á»›c 3: XÃ³a Ä‘á»‹a chá»‰
        try {
            if (diaChiService != null) {
                diaChiService.deleteByTaiKhoanId(id);
            }
            System.out.println("âœ… Step 3: ÄÃ£ xÃ³a Ä‘á»‹a chá»‰");
        } catch (Exception e) {
            System.out.println("âš ï¸ Step 3: Lá»—i xÃ³a Ä‘á»‹a chá»‰: " + e.getMessage());
        }

        // BÆ°á»›c 4: XÃ³a thÃ´ng tin khÃ¡ch hÃ ng/nhÃ¢n viÃªn
        try {
            if (account.getVaiTro() == TaiKhoan.VaiTro.USER && khachHangService != null) {
                khachHangService.deleteByTaiKhoanId(id);
            } else if (account.getVaiTro() == TaiKhoan.VaiTro.NHANVIEN && nhanVienService != null) {
                nhanVienService.deleteByTaiKhoanId(id);
            }
            System.out.println("âœ… Step 4: ÄÃ£ xÃ³a thÃ´ng tin cÃ¡ nhÃ¢n");
        } catch (Exception e) {
            System.out.println("âš ï¸ Step 4: Lá»—i xÃ³a thÃ´ng tin cÃ¡ nhÃ¢n: " + e.getMessage());
        }

        // BÆ°á»›c 5: XÃ³a tÃ i khoáº£n chÃ­nh
        try {
            taiKhoanRepository.deleteById(id);
            System.out.println("âœ… Step 5: ÄÃ£ xÃ³a tÃ i khoáº£n chÃ­nh");
        } catch (Exception e) {
            throw new RuntimeException("Lá»—i xÃ³a tÃ i khoáº£n chÃ­nh: " + e.getMessage(), e);
        }
    }
    @Override
    public boolean canChangeRole(Integer accountId, TaiKhoan.VaiTro newRole) {
        try {
            Optional<TaiKhoan> accountOpt = findById(accountId);
            if (accountOpt.isEmpty()) return false;

            TaiKhoan account = accountOpt.get();

            // KhÃ´ng thá»ƒ Ä‘á»•i vai trÃ² tá»« ADMIN náº¿u lÃ  admin hoáº¡t Ä‘á»™ng cuá»‘i cÃ¹ng
            if (account.getVaiTro() == TaiKhoan.VaiTro.ADMIN &&
                    newRole != TaiKhoan.VaiTro.ADMIN &&
                    account.getTrangThai() == 1) {

                if (isLastActiveAdmin(accountId)) {
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            System.err.println("Lá»—i kiá»ƒm tra kháº£ nÄƒng Ä‘á»•i vai trÃ²: " + e.getMessage());
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
            System.err.println("Lá»—i kiá»ƒm tra admin cuá»‘i cÃ¹ng: " + e.getMessage());
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
                throw new RuntimeException("KhÃ´ng thá»ƒ táº¡o mÃ£ tÃ i khoáº£n unique sau 1000 láº§n thá»­");
            }

            return maTaiKhoan;

        } catch (Exception e) {
            System.err.println("Lá»—i táº¡o mÃ£ tÃ i khoáº£n: " + e.getMessage());
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
                case "KHÃCH HÃ€NG":
                    return TaiKhoan.VaiTro.USER;
                case "NHANVIEN":
                case "NHÃ‚N VIÃŠN":
                case "EMPLOYEE":
                    return TaiKhoan.VaiTro.NHANVIEN;
                case "ADMIN":
                case "ADMINISTRATOR":
                case "QUáº¢N TRá»Š":
                    return TaiKhoan.VaiTro.ADMIN;
                default:
                    return null;
            }
        } catch (Exception e) {
            System.err.println("Lá»—i parse vai trÃ²: " + e.getMessage());
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
            throw new RuntimeException("KhÃ´ng thá»ƒ hash máº­t kháº©u");
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
        if (dto.isAdmin()) return true; // Admin khÃ´ng cáº§n Ä‘á»‹a chá»‰

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
            throw new IllegalArgumentException("Dá»¯ liá»‡u Ä‘á»‹a chá»‰ khÃ´ng há»£p lá»‡");
        }

        boolean hasLocation = (address.getTenTinh() != null && !address.getTenTinh().trim().isEmpty()) ||
                (address.getTenPhuong() != null && !address.getTenPhuong().trim().isEmpty());

        if (!hasLocation) {
            throw new IllegalArgumentException("Äá»‹a chá»‰ pháº£i cÃ³ Ã­t nháº¥t tÃªn tá»‰nh hoáº·c tÃªn phÆ°á»ng");
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
                    .filter(account -> account.getVaiTro() != TaiKhoan.VaiTro.ADMIN) // KhÃ´ng bao giá» dá»n dáº¹p admin
                    .collect(Collectors.toList());

            int cleanedCount = 0;
            for (TaiKhoan account : inactiveAccounts) {
                try {
                    if (canDeleteAccount(account.getId())) {
                        deleteById(account.getId());
                        cleanedCount++;
                    }
                } catch (Exception e) {
                    System.err.println("Lá»—i dá»n dáº¹p tÃ i khoáº£n " + account.getId() + ": " + e.getMessage());
                }
            }

            return cleanedCount;

        } catch (Exception e) {
            System.err.println("Lá»—i dá»n dáº¹p tÃ i khoáº£n khÃ´ng hoáº¡t Ä‘á»™ng: " + e.getMessage());
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
            System.err.println("Lá»—i export dá»¯ liá»‡u tÃ i khoáº£n: " + e.getMessage());
            return new HashMap<>();
        }
    }

    @Override
    public void logAccountActivity(Integer accountId, String activity, String details) {
        try {
            // TODO: Thá»±c hiá»‡n ghi log hoáº¡t Ä‘á»™ng thá»±c sá»± (database, file, etc.)
            System.out.println(String.format("[ACCOUNT_LOG] ID: %d, Hoáº¡t Ä‘á»™ng: %s, Chi tiáº¿t: %s, Thá»i gian: %s",
                    accountId, activity, details, new Date()));
        } catch (Exception e) {
            System.err.println("Lá»—i ghi log hoáº¡t Ä‘á»™ng tÃ i khoáº£n: " + e.getMessage());
        }
    }

    // ================== HELPER METHODS - PHÆ¯Æ NG THá»¨C Há»– TRá»¢ ==================

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
            throw new RuntimeException("Lá»—i táº¡o KhachHang: " + e.getMessage(), e);
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
            throw new RuntimeException("Lá»—i táº¡o NhanVien: " + e.getMessage(), e);
        }
    }

    private DiaChi createAddressForAccount(TaiKhoanDTO dto, TaiKhoan taiKhoan) {
        try {
            TaiKhoanDTO.DiaChiDto addressDto = dto.getEffectiveAddress();
            if (addressDto == null) {
                throw new IllegalArgumentException("KhÃ´ng cÃ³ dá»¯ liá»‡u Ä‘á»‹a chá»‰");
            }

            DiaChi diaChi = new DiaChi();
            diaChi.setTaiKhoan(taiKhoan);
            diaChi.setMaTinh(addressDto.getMaTinh() != null ? addressDto.getMaTinh() : "01");
            diaChi.setMaPhuong(addressDto.getMaPhuong() != null ? addressDto.getMaPhuong() : "00001");
            diaChi.setTenTinh(addressDto.getTenTinh() != null ? addressDto.getTenTinh() : ("Tá»‰nh " + diaChi.getMaTinh()));
            diaChi.setTenPhuong(addressDto.getTenPhuong() != null ? addressDto.getTenPhuong() : ("PhÆ°á»ng " + diaChi.getMaPhuong()));
            diaChi.setDiaChiChiTiet(addressDto.getDiaChiChiTiet() != null ? addressDto.getDiaChiChiTiet() : "");
            diaChi.setIsDefault(true);
            diaChi.setTrangThai(1);
            diaChi.setNgayTao(new Date());
            diaChi.setNgayCapNhat(new Date());

            return diaChiService.save(diaChi);

        } catch (Exception e) {
            throw new RuntimeException("Lá»—i táº¡o DiaChi: " + e.getMessage(), e);
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
