package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.entity.TaiKhoan;

public interface LoginService {
    TaiKhoan authenticate(String email, String password);

    TaiKhoan findByEmail(String email);

    // Nếu anh dùng `lastLoginAt` thì bỏ comment ở đây
    // void updateLastLogin(Integer userId);

    boolean hasPermission(TaiKhoan user, String permission);
}

