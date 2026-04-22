package org.example.iws_websitesneaker.Service.impl;

import org.example.iws_websitesneaker.Service.LoginService;
import org.example.iws_websitesneaker.entity.TaiKhoan;
import org.example.iws_websitesneaker.repository.RepoTaiKhoan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private RepoTaiKhoan userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // XÃ¡c thá»±c ngÆ°á»i dÃ¹ng vá»›i email vÃ  password
    @Override
    public TaiKhoan authenticate(String email, String rawPassword) {
        Optional<TaiKhoan> userOptional = userRepository.findByEmail(email.toLowerCase().trim());

        if (userOptional.isPresent()) {
            TaiKhoan user = userOptional.get();

            // So sÃ¡nh máº­t kháº©u ngÆ°á»i dÃ¹ng nháº­p vá»›i máº­t kháº©u Ä‘Ã£ mÃ£ hÃ³a trong DB
            if (passwordEncoder.matches(rawPassword, user.getMatKhau())) {
                return user; // Ä‘Ãºng máº­t kháº©u
            }
        }

        return null; // sai email hoáº·c máº­t kháº©u
    }

    // TÃ¬m tÃ i khoáº£n theo email
    @Override
    public TaiKhoan findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    // // Cáº­p nháº­t thá»i gian Ä‘Äƒng nháº­p gáº§n nháº¥t
    // public void updateLastLogin(Integer userId) {
    // Optional<TaiKhoan> userOptional = userRepository.findById(userId);
    // if (userOptional.isPresent()) {
    // TaiKhoan user = userOptional.get();
    // user.setLastLoginAt(LocalDateTime.now());
    // userRepository.save(user);
    // }
    // }

    // Kiá»ƒm tra quyá»n cá»§a tÃ i khoáº£n
    @Override
    public boolean hasPermission(TaiKhoan user, String permission) {
        if (user == null)
            return false;

        // Admin cÃ³ toÃ n quyá»n
        if (user.getVaiTro() == TaiKhoan.VaiTro.ADMIN) {
            return true;
        }

        // Quyá»n máº·c Ä‘á»‹nh cá»§a USER
        if (user.getVaiTro() == TaiKhoan.VaiTro.USER) {
            return hasUserPermission(permission);
        }

        // CÃ¡c vai trÃ² khÃ¡c chÆ°a cÃ³ quyá»n cá»¥ thá»ƒ
        return false;
    }

    // Danh sÃ¡ch quyá»n cá»§a USER
    private boolean hasUserPermission(String permission) {
        String[] userPermissions = {
                "VIEW_PRODUCTS",
                "VIEW_OWN_ORDERS",
                "CREATE_ORDER",
                "CANCEL_OWN_ORDER",
                "VIEW_OWN_PROFILE",
                "UPDATE_OWN_PROFILE"
        };

        for (String userPermission : userPermissions) {
            if (userPermission.equalsIgnoreCase(permission)) {
                return true;
            }
        }

        return false;
    }
}

