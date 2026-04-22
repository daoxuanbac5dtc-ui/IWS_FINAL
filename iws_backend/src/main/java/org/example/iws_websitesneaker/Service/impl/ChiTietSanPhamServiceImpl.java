package org.example.iws_websitesneaker.Service.impl;

import org.example.iws_websitesneaker.Service.ChiTietSanPhamService;
import org.example.iws_websitesneaker.entity.ChiTietSanPham;
import org.example.iws_websitesneaker.repository.ChiTietSanPhamRepository;
import org.example.iws_websitesneaker.repository.HoaDonChiTietRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ChiTietSanPhamServiceImpl implements ChiTietSanPhamService {

    @Autowired
    private ChiTietSanPhamRepository repository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;
    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Override
    public List<ChiTietSanPham> getAll() {
        return repository.findAll();
    }

    @Override
    public List<ChiTietSanPham> getActiveProducts() {
        try {
            System.out.println("Loading active products with details...");
            List<ChiTietSanPham> products = repository.findActiveProductsWithDetails();

            // Force trigger lazy loading trong transaction
            for (ChiTietSanPham product : products) {
                forceLoadAssociations(product);
            }

            System.out.println("Loaded " + products.size() + " active products");
            return products;
        } catch (Exception e) {
            System.err.println("Error with detailed query, using simple query: " + e.getMessage());
            e.printStackTrace();
            return repository.findActiveProducts();
        }
    }

    @Override
    public List<ChiTietSanPham> getProductsWithoutPromotion() {
        try {
            System.out.println("Loading products without promotion...");
            List<ChiTietSanPham> products = repository.findProductsWithoutActivePromotion();

            // Force trigger lazy loading trong transaction
            for (ChiTietSanPham product : products) {
                forceLoadAssociations(product);
            }

            System.out.println("Loaded " + products.size() + " products without promotion");
            return products;
        } catch (Exception e) {
            System.err.println("Error finding products without promotion: " + e.getMessage());
            e.printStackTrace();
            // Fallback to simple query
            return repository.findActiveProducts();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ChiTietSanPham getById(Integer id) {
        ChiTietSanPham product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t sáº£n pháº©m vá»›i ID: " + id));

        // Force load associations
        forceLoadAssociations(product);

        return product;
    }

    @Override
    public List<ChiTietSanPham> searchByKeyword(String keyword) {
        try {
            System.out.println("Searching products with keyword: " + keyword);
            List<ChiTietSanPham> products = repository.searchByKeyword(keyword);

            // Force trigger lazy loading trong transaction
            for (ChiTietSanPham product : products) {
                forceLoadAssociations(product);
            }

            System.out.println("Found " + products.size() + " products for keyword: " + keyword);
            return products;
        } catch (Exception e) {
            System.err.println("Error searching products: " + e.getMessage());
            e.printStackTrace();
            return repository.findActiveProducts();
        }
    }

    @Override
    public Long countActiveProducts() {
        try {
            return repository.countActiveProducts();
        } catch (Exception e) {
            System.err.println("Error counting products: " + e.getMessage());
            return 0L;
        }
    }

    @Override
    public ChiTietSanPham save(ChiTietSanPham chiTietSanPham) {
        try {
            // Validation cÆ¡ báº£n
            if (chiTietSanPham.getSanPham() == null) {
                throw new IllegalArgumentException("Sáº£n pháº©m khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
            }

            if (chiTietSanPham.getMauSac() == null) {
                throw new IllegalArgumentException("MÃ u sáº¯c khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
            }

            if (chiTietSanPham.getKichCo() == null) {
                throw new IllegalArgumentException("KÃ­ch cá»¡ khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
            }

            if (chiTietSanPham.getSoLuong() == null || chiTietSanPham.getSoLuong() < 0) {
                throw new IllegalArgumentException("Sá»‘ lÆ°á»£ng khÃ´ng há»£p lá»‡");
            }

            if (chiTietSanPham.getGiaGoc() == null || chiTietSanPham.getGiaGoc() < 0) {
                throw new IllegalArgumentException("GiÃ¡ gá»‘c khÃ´ng há»£p lá»‡");
            }

            if (chiTietSanPham.getGiaBan() == null || chiTietSanPham.getGiaBan() <= 0) {
                throw new IllegalArgumentException("GiÃ¡ bÃ¡n pháº£i lá»›n hÆ¡n 0");
            }

            // Kiá»ƒm tra logic giÃ¡ bÃ¡n >= giÃ¡ gá»‘c
            if (chiTietSanPham.getGiaBan() < chiTietSanPham.getGiaGoc()) {
                throw new IllegalArgumentException("GiÃ¡ bÃ¡n pháº£i lá»›n hÆ¡n hoáº·c báº±ng giÃ¡ gá»‘c");
            }

            // Náº¿u lÃ  táº¡o má»›i (id = null)
            if (chiTietSanPham.getId() == null) {
                chiTietSanPham.setNgayTao(new Date());

                // Tá»± Ä‘á»™ng táº¡o mÃ£ chi tiáº¿t náº¿u chÆ°a cÃ³
                if (chiTietSanPham.getMaChiTiet() == null || chiTietSanPham.getMaChiTiet().trim().isEmpty()) {
                    chiTietSanPham.setMaChiTiet("CTSP" + System.currentTimeMillis());
                }

                // Set tráº¡ng thÃ¡i máº·c Ä‘á»‹nh náº¿u chÆ°a cÃ³
                if (chiTietSanPham.getTrangThai() == null) {
                    chiTietSanPham.setTrangThai(1); // 1 = Hoáº¡t Ä‘á»™ng
                }
            } else {
                // Náº¿u lÃ  cáº­p nháº­t
                chiTietSanPham.setNgayCapNhat(new Date());
            }

            // LÆ°u vÃ o database
            return chiTietSanPhamRepository.save(chiTietSanPham);

        } catch (Exception e) {
            throw new RuntimeException("Lá»—i khi lÆ°u chi tiáº¿t sáº£n pháº©m: " + e.getMessage(), e);
        }
    }
    /**
     * Force load táº¥t cáº£ lazy associations Ä‘á»ƒ trÃ¡nh LazyInitializationException
     */
    private void forceLoadAssociations(ChiTietSanPham product) {
        try {
            // Force load sáº£n pháº©m
            if (product.getSanPham() != null) {
                String tenSanPham = product.getSanPham().getTenSanPham();
                String maSanPham = product.getSanPham().getMaSanPham();

                // Force load thÆ°Æ¡ng hiá»‡u
                if (product.getSanPham().getThuongHieu() != null) {
                    String tenThuongHieu = product.getSanPham().getThuongHieu().getTenThuongHieu();
                }

                // Force load danh má»¥c
                if (product.getSanPham().getDanhMuc() != null) {
                    String tenDanhMuc = product.getSanPham().getDanhMuc().getTenDanhMuc();
                }
            }

            // Force load mÃ u sáº¯c
            if (product.getMauSac() != null) {
                String tenMauSac = product.getMauSac().getTenMauSac();
                String maMau = product.getMauSac().getMaMauSac();
            }

            // Force load kÃ­ch cá»¡
            if (product.getKichCo() != null) {
                String tenKichCo = product.getKichCo().getTenKichCo();
            }
        } catch (Exception e) {
            System.err.println("Error force loading associations for product " + product.getId() + ": " + e.getMessage());
        }
    }
}
