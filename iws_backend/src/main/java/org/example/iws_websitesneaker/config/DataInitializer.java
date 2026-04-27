package org.example.iws_websitesneaker.config;

import org.example.iws_websitesneaker.entity.TaiKhoan;
import org.example.iws_websitesneaker.repository.RepoTaiKhoan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]?\\$.{56}$");

    @Autowired
    private RepoTaiKhoan repoTaiKhoan;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        List<TaiKhoan> accounts = repoTaiKhoan.findAll();

        long activeAdminCount = accounts.stream()
                .filter(account -> account.getVaiTro() == TaiKhoan.VaiTro.ADMIN)
                .filter(account -> Integer.valueOf(1).equals(account.getTrangThai()))
                .count();

        if (activeAdminCount == 0) {
            throw new IllegalStateException(
                    "Khong tim thay tai khoan admin hoat dong trong bang tai_khoan. Hay tao admin trong database truoc khi khoi dong backend.");
        }

        List<TaiKhoan> legacyPasswordAccounts = new ArrayList<>();
        for (TaiKhoan account : accounts) {
            String password = account.getMatKhau();
            if (password != null && !password.isBlank() && !isBcryptHash(password)) {
                account.setMatKhau(passwordEncoder.encode(password));
                legacyPasswordAccounts.add(account);
            }
        }

        if (!legacyPasswordAccounts.isEmpty()) {
            repoTaiKhoan.saveAll(legacyPasswordAccounts);
            log.info("Da chuyen {} tai khoan dang dung mat khau plaintext sang BCrypt trong database.",
                    legacyPasswordAccounts.size());
        }

        log.info("Da xac nhan {} tai khoan admin hoat dong trong database.", activeAdminCount);
    }

    private boolean isBcryptHash(String password) {
        return BCRYPT_PATTERN.matcher(password).matches();
    }
}
