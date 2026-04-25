package org.example.iws_websitesneaker.Service.impl;

import org.example.iws_websitesneaker.Dto.AddToCartRequest;
import org.example.iws_websitesneaker.Dto.CartItemResponse; // THÃŠM IMPORT
import org.example.iws_websitesneaker.Service.GioHangService;
import org.example.iws_websitesneaker.entity.GioHang;
import org.example.iws_websitesneaker.entity.GioHangChiTIet;
import org.example.iws_websitesneaker.entity.TaiKhoan;
import org.example.iws_websitesneaker.entity.ChiTietSanPham;
import org.example.iws_websitesneaker.repository.RepoGioHang;
import org.example.iws_websitesneaker.repository.RepoGioHangChiTiet;
import org.example.iws_websitesneaker.repository.RepoTaiKhoan;
import org.example.iws_websitesneaker.repository.RepoChiTietSanPham;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GioHangServiceImpl implements GioHangService {

    @Autowired
    private RepoGioHang repoGioHang;

    @Autowired
    private RepoGioHangChiTiet gioHangChiTietRepository;

    @Autowired
    private RepoTaiKhoan repoTaiKhoan;

    @Autowired
    private RepoChiTietSanPham chiTietSanPhamRepository;

    @Override
    public List<CartItemResponse> getCartByUserId(Integer userId) {
        GioHang gioHang = findOrCreateCart(userId);
        List<GioHangChiTIet> cartItems = gioHangChiTietRepository.findByGioHang(gioHang);

        return cartItems.stream()
                .map(this::convertToCartItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CartItemResponse addToCart(Integer userId, AddToCartRequest request) {
        System.out.println("=== ADD TO CART SERVICE DEBUG ===");
        System.out.println("User ID: " + userId);
        System.out.println("Request: " + request);
        System.out.println("Product Detail ID: " + request.getProductDetailId());
        System.out.println("Quantity: " + request.getSoLuong());
        System.out.println("Price: " + request.getGia());

        // Validation Ä‘áº§u vÃ o
        if (userId == null) {
            throw new IllegalArgumentException("User ID khÃ´ng Ä‘Æ°á»£c null");
        }
        if (request.getProductDetailId() == null) {
            throw new IllegalArgumentException("Product Detail ID khÃ´ng Ä‘Æ°á»£c null");
        }
        if (request.getSoLuong() == null || request.getSoLuong() <= 0) {
            throw new IllegalArgumentException("Sá»‘ lÆ°á»£ng pháº£i lá»›n hÆ¡n 0");
        }

        try {
            // 1. TÃ¬m hoáº·c táº¡o giá» hÃ ng
            System.out.println("Finding or creating cart for user: " + userId);
            GioHang gioHang = findOrCreateCart(userId);
            System.out.println("Cart found/created: " + gioHang.getId());

            // 2. Kiá»ƒm tra chi tiáº¿t sáº£n pháº©m cÃ³ tá»“n táº¡i
            System.out.println("Checking if product detail exists: " + request.getProductDetailId());
            ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository
                    .findById(request.getProductDetailId())
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t sáº£n pháº©m vá»›i ID: " + request.getProductDetailId()));
            System.out.println("Product detail found: " + chiTietSanPham.getId());

            // 3. Kiá»ƒm tra item Ä‘Ã£ tá»“n táº¡i trong giá» hÃ ng
            System.out.println("Checking if item already exists in cart");
            Optional<GioHangChiTIet> existing = gioHangChiTietRepository
                    .findByGioHangAndChiTietSanPham_Id(gioHang, request.getProductDetailId());

            System.out.println("Existing item found: " + existing.isPresent());

            GioHangChiTIet cartItem;

            if (existing.isPresent()) {
                // Update quantity
                System.out.println("Updating existing item");
                cartItem = existing.get();
                int oldQuantity = cartItem.getSoLuong();
                cartItem.setSoLuong(oldQuantity + request.getSoLuong());
                cartItem.setNgayCapNhat(new Date());
                System.out.println("Updated quantity from " + oldQuantity + " to " + cartItem.getSoLuong());
            } else {
                // Create new item
                System.out.println("Creating new cart item");
                cartItem = new GioHangChiTIet();
                cartItem.setGioHang(gioHang);
                cartItem.setChiTietSanPham(chiTietSanPham);
                cartItem.setSoLuong(request.getSoLuong());

                // Xá»­ lÃ½ giÃ¡ - náº¿u khÃ´ng cÃ³ trong request thÃ¬ láº¥y tá»« sáº£n pháº©m
                Double price = request.getGia();
                if (price == null) {
                    price = chiTietSanPham.getGiaBan(); // Giáº£ sá»­ cÃ³ method nÃ y
                    System.out.println("Price taken from product: " + price);
                }
                cartItem.setGia(price);

                cartItem.setNgayTao(new Date());
                cartItem.setNgayCapNhat(new Date());
                cartItem.setMaGioHangChiTiet("GHCT" + System.currentTimeMillis());
                cartItem.setTrangThaiHoaDon("ACTIVE");

                System.out.println("New cart item created with:");
                System.out.println("- Quantity: " + cartItem.getSoLuong());
                System.out.println("- Price: " + cartItem.getGia());
                System.out.println("- Code: " + cartItem.getMaGioHangChiTiet());
            }

            // 4. LÆ°u vÃ o database
            System.out.println("Saving cart item to database");
            cartItem = gioHangChiTietRepository.save(cartItem);
            System.out.println("Cart item saved with ID: " + cartItem.getId());

            // 5. Convert vÃ  tráº£ vá» response
            System.out.println("Converting to response");
            CartItemResponse response = convertToCartItemResponse(cartItem);
            System.out.println("Response created successfully");

            return response;

        } catch (Exception e) {
            System.err.println("Error in addToCart: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional
    public CartItemResponse updateCartItem(Integer cartItemId, Integer newQuantity) {
        GioHangChiTIet cartItem = gioHangChiTietRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y sáº£n pháº©m trong giá» hÃ ng"));

        if (newQuantity <= 0) {
            throw new RuntimeException("Sá»‘ lÆ°á»£ng pháº£i lá»›n hÆ¡n 0");
        }

        cartItem.setSoLuong(newQuantity);
        cartItem.setNgayCapNhat(new Date());

        cartItem = gioHangChiTietRepository.save(cartItem);
        return convertToCartItemResponse(cartItem); // CONVERT TO DTO
    }

    @Override
    @Transactional
    public void removeCartItem(Integer cartItemId) {
        if (!gioHangChiTietRepository.existsById(cartItemId)) {
            throw new RuntimeException("KhÃ´ng tÃ¬m tháº¥y sáº£n pháº©m trong giá» hÃ ng");
        }
        gioHangChiTietRepository.deleteById(cartItemId);
    }

    @Override
    @Transactional
    public void clearCart(Integer userId) {
        GioHang gioHang = findOrCreateCart(userId);
        gioHangChiTietRepository.deleteByGioHang(gioHang);
    }

    @Override
    public Integer getTotalQuantity(Integer userId) {
        List<CartItemResponse> cartItems = getCartByUserId(userId);
        return cartItems.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();
    }

    @Override
    public Double getTotalAmount(Integer userId) {
        List<CartItemResponse> cartItems = getCartByUserId(userId);
        return cartItems.stream()
                .mapToDouble(CartItemResponse::getTotalPrice)
                .sum();
    }

    @Override
    public boolean isProductInCart(Integer userId, Integer productDetailId) {
        GioHang gioHang = findOrCreateCart(userId);
        Optional<GioHangChiTIet> existing = gioHangChiTietRepository
                .findByGioHangAndChiTietSanPham_Id(gioHang, productDetailId);
        return existing.isPresent();
    }

    private GioHang findOrCreateCart(Integer userId) {
        TaiKhoan taiKhoan = repoTaiKhoan.findById(userId).orElseThrow();

        // TÃ¬m giá» hÃ ng cÃ³ sáºµn
        Optional<GioHang> existingCart = repoGioHang.findByTaiKhoan(taiKhoan);

        if (existingCart.isPresent()) {
            return existingCart.get();
        } else {
            // Táº¡o giá» hÃ ng má»›i
            GioHang newCart = new GioHang();
            newCart.setTaiKhoan(taiKhoan);
            newCart.setMaGioHang("GH" + System.currentTimeMillis());
            newCart.setNgayTao(new Date());
            return repoGioHang.save(newCart);
        }
    }

    // THÃŠM METHOD CONVERT
    private CartItemResponse convertToCartItemResponse(GioHangChiTIet item) {
        ChiTietSanPham ctsp = item.getChiTietSanPham();

        CartItemResponse response = new CartItemResponse();
        response.setId(item.getId());
        response.setProductDetailId(ctsp.getId());
        response.setName(ctsp.getSanPham() != null ? ctsp.getSanPham().getTenSanPham() : "N/A");
        response.setCode("MSL" + ctsp.getId());
        response.setImage(ctsp.getHinhAnh() != null ? ctsp.getHinhAnh().getDuongDan() : null);
        response.setPrice(item.getGia());
        response.setQuantity(item.getSoLuong());
        response.setSize(ctsp.getKichCo() != null ? ctsp.getKichCo().getTenKichCo() : null);
        response.setColor(ctsp.getMauSac() != null ? ctsp.getMauSac().getTenMauSac() : null); // Náº¿u response.setColor() nháº­n String

        response.setStock(ctsp.getSoLuong());
        response.setPoints(Math.toIntExact(Math.round(item.getGia() / 100)));
        response.setTotalPrice(item.getGia() * item.getSoLuong());
        response.setCreatedDate(item.getNgayTao());
        response.setUpdatedDate(item.getNgayCapNhat());

        return response;
    }
}
