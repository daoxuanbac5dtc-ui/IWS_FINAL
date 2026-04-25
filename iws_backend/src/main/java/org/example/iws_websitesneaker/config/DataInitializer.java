package org.example.iws_websitesneaker.config;

import org.example.iws_websitesneaker.entity.TaiKhoan;
import org.example.iws_websitesneaker.repository.RepoTaiKhoan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RepoTaiKhoan repoTaiKhoan;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        TaiKhoan admin = repoTaiKhoan.findByEmail("admin@gmail.com").orElse(null);
        
        if (admin == null) {
            System.out.println("Không tìm thấy Admin admin@gmail.com. Đang tạo Admin mặc định...");
            
            admin = TaiKhoan.builder()
                    .maTaiKhoan("ADMIN01")
                    .email("admin@gmail.com")
                    .matKhau(passwordEncoder.encode("admin123")) 
                    .vaiTro(TaiKhoan.VaiTro.ADMIN)
                    .trangThai(1)
                    .ngayTao(new Date())
                    .ngayCapNhat(new Date())
                    .build();
            
            repoTaiKhoan.save(admin);
            System.out.println("Đã tạo Admin mặc định thành công! Email: admin@gmail.com | Password: admin123");
        } else {
            System.out.println("Đã tìm thấy Admin admin@gmail.com. Đang cập nhật lại mật khẩu thành admin123...");
            admin.setMatKhau(passwordEncoder.encode("admin123"));
            admin.setVaiTro(TaiKhoan.VaiTro.ADMIN);
            admin.setTrangThai(1);
            repoTaiKhoan.save(admin);
            System.out.println("Đã cập nhật mật khẩu Admin thành công! Email: admin@gmail.com | Password: admin123");
        }
    }
}
