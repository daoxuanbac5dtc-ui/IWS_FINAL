package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.entity.TaiKhoan;

public interface LoginService {
    TaiKhoan authenticate(String email, String password);

    TaiKhoan findByEmail(String email);

    // Náº¿u anh dÃ¹ng `lastLoginAt` thÃ¬ bá» comment á»Ÿ Ä‘Ã¢y
    // void updateLastLogin(Integer userId);

    boolean hasPermission(TaiKhoan user, String permission);
}

