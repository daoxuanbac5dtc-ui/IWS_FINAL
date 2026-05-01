package org.example.iws_websitesneaker.Service.impl;

import org.example.iws_websitesneaker.entity.ChiTietSanPham;
import org.example.iws_websitesneaker.repository.HoaDonChiTietRepository;
import org.example.iws_websitesneaker.repository.RepoChiTietSanPham;
import org.example.iws_websitesneaker.Service.SanPhamChiTietService;
import org.example.iws_websitesneaker.repository.RepoHinhAnh;
import org.example.iws_websitesneaker.repository.RepoHoaDonChiTiet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SanPhamChiTietServiceImpl implements SanPhamChiTietService {

    @Autowired
    private RepoChiTietSanPham chiTietSanPhamRepository;

    @Autowired
    private RepoHoaDonChiTiet repoHoaDonChiTietRepository;

    @Autowired
    private RepoHinhAnh hinhAnhRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Override
    public List<ChiTietSanPham> getAll() {
        return chiTietSanPhamRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ChiTietSanPham getById(Integer id) {
        // SỬA: Sử dụng method có JOIN FETCH
        return chiTietSanPhamRepository.findByIdWithDetails(id).orElse(null);
    }

    @Override
    @Transactional
    public ChiTietSanPham save(ChiTietSanPham chiTietSanPham) {
        if (chiTietSanPham.getGiaBan() == null) {
            chiTietSanPham.setGiaBan(0.0);
        }
        if (chiTietSanPham.getGiaGoc() == null) {
            chiTietSanPham.setGiaGoc(0.0);
        }
        chiTietSanPham.setNgayTao(new Date());
        return chiTietSanPhamRepository.save(chiTietSanPham);
    }

    @Override
    @Transactional
    public ChiTietSanPham update(ChiTietSanPham chiTietSanPham, Integer id) {
        if (chiTietSanPhamRepository.existsById(id)) {
            chiTietSanPham.setId(id);
            if (chiTietSanPham.getGiaBan() == null) {
                chiTietSanPham.setGiaBan(0.0);
            }
            if (chiTietSanPham.getGiaGoc() == null) {
                chiTietSanPham.setGiaGoc(0.0);
            }
            chiTietSanPham.setNgayCapNhat(new Date());
            return chiTietSanPhamRepository.save(chiTietSanPham);
        }
        return null;
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        if (chiTietSanPhamRepository.existsById(id)) {
            hoaDonChiTietRepository.removeChiTietSanPhamReference(id);
            chiTietSanPhamRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // SỬA: Method chính với transaction và debug logging
    @Override
    @Transactional(readOnly = true)
    public List<ChiTietSanPham> getBySanPhamId(Integer sanPhamId) {
        try {
            System.out.println("🔍 Service: Loading chi tiết sản phẩm cho sanPhamId: " + sanPhamId);

            List<ChiTietSanPham> chiTietSanPhams = chiTietSanPhamRepository.findBySanPhamId(sanPhamId);

            System.out.println("📦 Repository trả về " + chiTietSanPhams.size() + " records");

            // Force load và debug hình ảnh
            for (ChiTietSanPham ctsp : chiTietSanPhams) {
                // Force load basic info
                String maChiTiet = ctsp.getMaChiTiet();
                String mauSac = ctsp.getMauSac() != null ? ctsp.getMauSac().getTenMauSac() : "NULL";
                String kichCo = ctsp.getKichCo() != null ? ctsp.getKichCo().getTenKichCo() : "NULL";

                // QUAN TRỌNG: Force load hình ảnh
                String hinhAnh = "NULL";
                if (ctsp.getHinhAnh() != null) {
                    try {
                        // Force load hình ảnh properties
                        hinhAnh = ctsp.getHinhAnh().getTenHinhAnh();
                        String duongDan = ctsp.getHinhAnh().getDuongDan();
                        Integer trangThai = ctsp.getHinhAnh().getTrangThai();
                        System.out.println("   🖼️ Hình ảnh: " + hinhAnh + " | Đường dẫn: " + duongDan + " | Trạng thái: " + trangThai);
                    } catch (Exception e) {
                        System.err.println("   ❌ Lỗi force load hình ảnh: " + e.getMessage());
                    }
                }

                System.out.println("   ✅ " + maChiTiet + " | Màu: " + mauSac + " | Size: " + kichCo + " | Hình: " + hinhAnh);
            }

            return chiTietSanPhams;

        } catch (Exception e) {
            System.err.println("❌ Lỗi trong getBySanPhamId service: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể tải chi tiết sản phẩm cho ID: " + sanPhamId, e);
        }
    }
}
