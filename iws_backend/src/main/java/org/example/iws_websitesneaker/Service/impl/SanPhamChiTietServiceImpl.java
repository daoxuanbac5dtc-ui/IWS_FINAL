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
        // Sá»¬A: Sá»­ dá»¥ng method cÃ³ JOIN FETCH
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

    // Sá»¬A: Method chÃ­nh vá»›i transaction vÃ  debug logging
    @Override
    @Transactional(readOnly = true)
    public List<ChiTietSanPham> getBySanPhamId(Integer sanPhamId) {
        try {
            System.out.println("ðŸ” Service: Loading chi tiáº¿t sáº£n pháº©m cho sanPhamId: " + sanPhamId);

            List<ChiTietSanPham> chiTietSanPhams = chiTietSanPhamRepository.findBySanPhamId(sanPhamId);

            System.out.println("ðŸ“¦ Repository tráº£ vá» " + chiTietSanPhams.size() + " records");

            // Force load vÃ  debug hÃ¬nh áº£nh
            for (ChiTietSanPham ctsp : chiTietSanPhams) {
                // Force load basic info
                String maChiTiet = ctsp.getMaChiTiet();
                String mauSac = ctsp.getMauSac() != null ? ctsp.getMauSac().getTenMauSac() : "NULL";
                String kichCo = ctsp.getKichCo() != null ? ctsp.getKichCo().getTenKichCo() : "NULL";

                // QUAN TRá»ŒNG: Force load hÃ¬nh áº£nh
                String hinhAnh = "NULL";
                if (ctsp.getHinhAnh() != null) {
                    try {
                        // Force load hÃ¬nh áº£nh properties
                        hinhAnh = ctsp.getHinhAnh().getTenHinhAnh();
                        String duongDan = ctsp.getHinhAnh().getDuongDan();
                        Integer trangThai = ctsp.getHinhAnh().getTrangThai();
                        System.out.println("   ðŸ–¼ï¸ HÃ¬nh áº£nh: " + hinhAnh + " | ÄÆ°á»ng dáº«n: " + duongDan + " | Tráº¡ng thÃ¡i: " + trangThai);
                    } catch (Exception e) {
                        System.err.println("   âŒ Lá»—i force load hÃ¬nh áº£nh: " + e.getMessage());
                    }
                }

                System.out.println("   âœ… " + maChiTiet + " | MÃ u: " + mauSac + " | Size: " + kichCo + " | HÃ¬nh: " + hinhAnh);
            }

            return chiTietSanPhams;

        } catch (Exception e) {
            System.err.println("âŒ Lá»—i trong getBySanPhamId service: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("KhÃ´ng thá»ƒ táº£i chi tiáº¿t sáº£n pháº©m cho ID: " + sanPhamId, e);
        }
    }
}
