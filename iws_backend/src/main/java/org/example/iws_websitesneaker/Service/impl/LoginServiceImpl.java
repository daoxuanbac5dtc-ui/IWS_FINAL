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

    // Xác thực người dùng với email và password
    @Override
    public TaiKhoan authenticate(String email, String rawPassword) {
        Optional<TaiKhoan> userOptional = userRepository.findByEmail(email.toLowerCase().trim());

        if (userOptional.isPresent()) {
            TaiKhoan user = userOptional.get();

            // So sánh mật khẩu người dùng nhập với mật khẩu đã mã hóa trong DB
            if (passwordEncoder.matches(rawPassword, user.getMatKhau())) {
                return user; // đúng mật khẩu
            }
        }

        return null; // sai email hoặc mật khẩu
    }

    // Tìm tài khoản theo email
    @Override
    public TaiKhoan findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    // // Cập nhật thời gian đăng nhập gần nhất
    // public void updateLastLogin(Integer userId) {
    // Optional<TaiKhoan> userOptional = userRepository.findById(userId);
    // if (userOptional.isPresent()) {
    // TaiKhoan user = userOptional.get();
    // user.setLastLoginAt(LocalDateTime.now());
    // userRepository.save(user);
    // }
    // }

    // Kiểm tra quyền của tài khoản
    @Override
    public boolean hasPermission(TaiKhoan user, String permission) {
        if (user == null)
            return false;

        // Admin có toàn quyền
        if (user.getVaiTro() == TaiKhoan.VaiTro.ADMIN) {
            return true;
        }

        // Quyền mặc định của USER
        if (user.getVaiTro() == TaiKhoan.VaiTro.USER) {
            return hasUserPermission(permission);
        }

        // Các vai trò khác chưa có quyền cụ thể
        return false;
    }

    // Danh sách quyền của USER
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

