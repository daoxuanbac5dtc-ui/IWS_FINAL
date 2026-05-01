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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết sản phẩm với ID: " + id));

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
            // Validation cơ bản
            if (chiTietSanPham.getSanPham() == null) {
                throw new IllegalArgumentException("Sản phẩm không được để trống");
            }

            if (chiTietSanPham.getMauSac() == null) {
                throw new IllegalArgumentException("Màu sắc không được để trống");
            }

            if (chiTietSanPham.getKichCo() == null) {
                throw new IllegalArgumentException("Kích cỡ không được để trống");
            }

            if (chiTietSanPham.getSoLuong() == null || chiTietSanPham.getSoLuong() < 0) {
                throw new IllegalArgumentException("Số lượng không hợp lệ");
            }

            if (chiTietSanPham.getGiaGoc() == null || chiTietSanPham.getGiaGoc() < 0) {
                throw new IllegalArgumentException("Giá gốc không hợp lệ");
            }

            if (chiTietSanPham.getGiaBan() == null || chiTietSanPham.getGiaBan() <= 0) {
                throw new IllegalArgumentException("Giá bán phải lớn hơn 0");
            }

            // Kiểm tra logic giá bán >= giá gốc
            if (chiTietSanPham.getGiaBan() < chiTietSanPham.getGiaGoc()) {
                throw new IllegalArgumentException("Giá bán phải lớn hơn hoặc bằng giá gốc");
            }

            // Nếu là tạo mới (id = null)
            if (chiTietSanPham.getId() == null) {
                chiTietSanPham.setNgayTao(new Date());

                // Tự động tạo mã chi tiết nếu chưa có
                if (chiTietSanPham.getMaChiTiet() == null || chiTietSanPham.getMaChiTiet().trim().isEmpty()) {
                    chiTietSanPham.setMaChiTiet("CTSP" + System.currentTimeMillis());
                }

                // Set trạng thái mặc định nếu chưa có
                if (chiTietSanPham.getTrangThai() == null) {
                    chiTietSanPham.setTrangThai(1); // 1 = Hoạt động
                }
            } else {
                // Nếu là cập nhật
                chiTietSanPham.setNgayCapNhat(new Date());
            }

            // Lưu vào database
            return chiTietSanPhamRepository.save(chiTietSanPham);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu chi tiết sản phẩm: " + e.getMessage(), e);
        }
    }
    /**
     * Force load tất cả lazy associations để tránh LazyInitializationException
     */
    private void forceLoadAssociations(ChiTietSanPham product) {
        try {
            // Force load sản phẩm
            if (product.getSanPham() != null) {
                String tenSanPham = product.getSanPham().getTenSanPham();
                String maSanPham = product.getSanPham().getMaSanPham();

                // Force load thương hiệu
                if (product.getSanPham().getThuongHieu() != null) {
                    String tenThuongHieu = product.getSanPham().getThuongHieu().getTenThuongHieu();
                }

                // Force load danh mục
                if (product.getSanPham().getDanhMuc() != null) {
                    String tenDanhMuc = product.getSanPham().getDanhMuc().getTenDanhMuc();
                }
            }

            // Force load màu sắc
            if (product.getMauSac() != null) {
                String tenMauSac = product.getMauSac().getTenMauSac();
                String maMau = product.getMauSac().getMaMauSac();
            }

            // Force load kích cỡ
            if (product.getKichCo() != null) {
                String tenKichCo = product.getKichCo().getTenKichCo();
            }
        } catch (Exception e) {
            System.err.println("Error force loading associations for product " + product.getId() + ": " + e.getMessage());
        }
    }
}
