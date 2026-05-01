package org.example.iws_websitesneaker.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.iws_websitesneaker.Dto.AddToCartRequest;
import org.example.iws_websitesneaker.Dto.CartItemResponse;
import org.example.iws_websitesneaker.Dto.GuestCheckoutRequest;
import org.example.iws_websitesneaker.Dto.UpdateCartRequest;
import org.example.iws_websitesneaker.Service.ChiTietSanPhamService;
import org.example.iws_websitesneaker.Service.GioHangService;
import org.example.iws_websitesneaker.Service.HoaDonChiTietService;
import org.example.iws_websitesneaker.Service.HoaDonService;
import org.example.iws_websitesneaker.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/gio-hang")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class GioHangRestController {

    @Autowired
    private GioHangService gioHangService;
    private final Map<String, List<GuestCartItem>> guestCarts = new ConcurrentHashMap<>();
    @Autowired
    private ChiTietSanPhamService chiTietSanPhamService;
    @Autowired
    private HoaDonService hoaDonService;
    @Autowired
    private HoaDonChiTietService hoaDonChiTietService;

    // Lấy giỏ hàng của user hiện tại
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUserOrGuestCart(HttpServletRequest request,
                                                       @RequestParam(required = false) String sessionId) {
        try {
            System.out.println("=== GET CURRENT CART (USER OR GUEST) ===");

            // 1. Thử lấy user ID từ JWT
            Integer userId = null;
            try {
                userId = getUserIdFromRequest(request);
            } catch (Exception ex) {
                System.out.println("No JWT user found, fallback to guest.");
            }

            // 2. Nếu có user -> trả về giỏ hàng DB
            if (userId != null) {
                List<CartItemResponse> cartItems = gioHangService.getCartByUserId(userId);
                return ResponseEntity.ok(cartItems);
            }

            // 3. Nếu không có user -> dùng sessionId (guest)
            if (sessionId != null) {
                List<GuestCartItem> guestItems = guestCarts.getOrDefault(sessionId, new ArrayList<>());

                List<CartItemResponse> cartItems = new ArrayList<>();
                for (GuestCartItem item : guestItems) {
                    CartItemResponse response = convertGuestItemToResponse(item);
                    if (response != null) {
                        cartItems.add(response);
                    }
                }

                return ResponseEntity.ok(cartItems);
            }

            // 4. Không có user, không có sessionId -> giỏ trống
            return ResponseEntity.ok(Collections.emptyList());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }


    // Thêm sản phẩm vào giỏ hàng
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest request, HttpServletRequest httpRequest) {
        try {
            System.out.println("=== ADD TO CART CONTROLLER DEBUG ===");

            // ✅ SỬA: Lấy user ID từ JWT Filter attributes
            Integer userId = getUserIdFromRequest(httpRequest);
            System.out.println("User ID from JWT: " + userId);
            System.out.println("Request: " + request);

            CartItemResponse cartItem = gioHangService.addToCart(userId, request);
            return ResponseEntity.ok(cartItem);
        } catch (Exception e) {
            System.err.println("Error in addToCart: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Cập nhật số lượng
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCartItem(@PathVariable Integer id, @RequestBody UpdateCartRequest request, HttpServletRequest httpRequest) {
        try {
            System.out.println("=== UPDATE CART ITEM DEBUG ===");

            // Có thể cần verify ownership của cart item
            Integer userId = getUserIdFromRequest(httpRequest);
            System.out.println("User ID: " + userId + ", Item ID: " + id + ", New quantity: " + request.getSoLuong());

            CartItemResponse updated = gioHangService.updateCartItem(id, request.getSoLuong());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            System.err.println("Error in updateCartItem: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Xóa sản phẩm
    @DeleteMapping("/remove/{id}")
    public ResponseEntity<?> removeCartItem(@PathVariable Integer id, HttpServletRequest httpRequest) {
        try {
            System.out.println("=== REMOVE CART ITEM DEBUG ===");

            // Có thể cần verify ownership của cart item
            Integer userId = getUserIdFromRequest(httpRequest);
            System.out.println("User ID: " + userId + ", Removing item ID: " + id);

            gioHangService.removeCartItem(id);
            return ResponseEntity.ok().body("{\"message\": \"Removed successfully\"}");
        } catch (Exception e) {
            System.err.println("Error in removeCartItem: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Xóa toàn bộ giỏ hàng
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(HttpServletRequest request) {
        try {
            System.out.println("=== CLEAR CART DEBUG ===");

            // ✅ SỬA: Lấy user ID từ JWT Filter attributes
            Integer userId = getUserIdFromRequest(request);
            System.out.println("User ID: " + userId);

            gioHangService.clearCart(userId);
            return ResponseEntity.ok().body("{\"message\": \"Cart cleared\"}");
        } catch (Exception e) {
            System.err.println("Error in clearCart: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * ✅ SỬA: Lấy User ID từ JWT Filter attributes thay vì decode JWT
     * JWT Filter đã làm việc này và đặt thông tin vào request attributes
     */
    private Integer getUserIdFromRequest(HttpServletRequest request) {
        try {
            // Lấy từ attributes được set bởi JWT Filter
            Integer userId = (Integer) request.getAttribute("currentUserId");
            if (userId != null) {
                System.out.println("✅ User ID from JWT Filter attributes: " + userId);
                return userId;
            }

            // Lấy user object và extract ID
            TaiKhoan currentUser = (TaiKhoan) request.getAttribute("currentUser");
            if (currentUser != null) {
                System.out.println("✅ User ID from user object: " + currentUser.getId());
                return currentUser.getId();
            }

            // Fallback: log thông tin debug
            System.err.println("❌ No user info found in request attributes");
            System.err.println("Available attributes:");
            request.getAttributeNames().asIterator().forEachRemaining(name -> {
                System.err.println("  - " + name + ": " + request.getAttribute(name));
            });

            throw new RuntimeException("User not authenticated - no user info in request");

        } catch (Exception e) {
            System.err.println("❌ Error getting user ID from request: " + e.getMessage());
            throw new RuntimeException("Unable to get user information: " + e.getMessage());
        }
    }
    @GetMapping("/guest/{sessionId}")
    public ResponseEntity<?> getGuestCart(@PathVariable String sessionId) {
        try {
            System.out.println("=== GET GUEST CART ===");
            System.out.println("Session ID: " + sessionId);

            List<GuestCartItem> guestItems = guestCarts.getOrDefault(sessionId, new ArrayList<>());

            // Convert guest items to CartItemResponse format
            List<CartItemResponse> cartItems = new ArrayList<>();
            for (GuestCartItem item : guestItems) {
                CartItemResponse response = convertGuestItemToResponse(item);
                if (response != null) {
                    cartItems.add(response);
                }
            }

            System.out.println("Guest cart items: " + cartItems.size());
            return ResponseEntity.ok(cartItems);

        } catch (Exception e) {
            System.err.println("Error in getGuestCart: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/guest/{sessionId}/add")
    public ResponseEntity<?> addToGuestCart(@PathVariable String sessionId, @RequestBody AddToCartRequest request) {
        try {
            System.out.println("=== ADD TO GUEST CART ===");
            System.out.println("Session ID: " + sessionId);
            System.out.println("Product ID: " + request.getProductDetailId());
            System.out.println("Quantity: " + request.getSoLuong());

            List<GuestCartItem> cart = guestCarts.computeIfAbsent(sessionId, k -> new ArrayList<>());

            // Tìm xem sản phẩm đã có chưa
            Optional<GuestCartItem> existingItem = cart.stream()
                    .filter(item -> item.getProductDetailId().equals(request.getProductDetailId()))
                    .findFirst();

            if (existingItem.isPresent()) {
                // Cập nhật số lượng
                existingItem.get().setQuantity(existingItem.get().getQuantity() + request.getSoLuong());
            } else {
                // Thêm mới
                cart.add(new GuestCartItem(request.getProductDetailId(), request.getSoLuong()));
            }

            // Convert và return response
            GuestCartItem itemToReturn = existingItem.orElse(cart.get(cart.size() - 1));
            CartItemResponse response = convertGuestItemToResponse(itemToReturn);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error in addToGuestCart: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/guest/{sessionId}/update/{productDetailId}")
    public ResponseEntity<?> updateGuestCartItem(@PathVariable String sessionId,
                                                 @PathVariable Integer productDetailId,
                                                 @RequestBody UpdateCartRequest request) {
        try {
            System.out.println("=== UPDATE GUEST CART ITEM ===");

            List<GuestCartItem> cart = guestCarts.get(sessionId);
            if (cart == null) {
                return ResponseEntity.notFound().build();
            }

            Optional<GuestCartItem> item = cart.stream()
                    .filter(i -> i.getProductDetailId().equals(productDetailId))
                    .findFirst();

            if (item.isPresent()) {
                item.get().setQuantity(request.getSoLuong());
                CartItemResponse response = convertGuestItemToResponse(item.get());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            System.err.println("Error in updateGuestCartItem: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/guest/{sessionId}/remove/{productDetailId}")
    public ResponseEntity<?> removeGuestCartItem(@PathVariable String sessionId, @PathVariable Integer productDetailId) {
        try {
            System.out.println("=== REMOVE GUEST CART ITEM ===");

            List<GuestCartItem> cart = guestCarts.get(sessionId);
            if (cart != null) {
                cart.removeIf(item -> item.getProductDetailId().equals(productDetailId));
            }

            return ResponseEntity.ok().body("{\"message\": \"Removed successfully\"}");

        } catch (Exception e) {
            System.err.println("Error in removeGuestCartItem: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/guest/{sessionId}/clear")
    public ResponseEntity<?> clearGuestCart(@PathVariable String sessionId) {
        try {
            System.out.println("=== CLEAR GUEST CART ===");

            guestCarts.remove(sessionId);

            return ResponseEntity.ok().body("{\"message\": \"Cart cleared\"}");

        } catch (Exception e) {
            System.err.println("Error in clearGuestCart: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/guest/{sessionId}/checkout")
    public ResponseEntity<?> guestCheckout(@PathVariable String sessionId, @RequestBody GuestCheckoutRequest request) {
        try {
            System.out.println("=== GUEST CHECKOUT ===");

            List<GuestCartItem> cart = guestCarts.get(sessionId);
            if (cart == null || cart.isEmpty()) {
                return ResponseEntity.badRequest().body("Cart is empty");
            }

            // Tạo đơn hàng guest
            HoaDon order = createGuestOrder(request, cart);

            // Xóa guest cart
            guestCarts.remove(sessionId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "orderId", order.getId(),
                    "orderCode", order.getMaHoaDon(),
                    "message", "Đặt hàng thành công! Mã đơn hàng: " + order.getMaHoaDon()
            ));

        } catch (Exception e) {
            System.err.println("Error in guestCheckout: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    private CartItemResponse convertGuestItemToResponse(GuestCartItem guestItem) {
        try {
            // Lấy thông tin chi tiết sản phẩm từ database
            ChiTietSanPham chiTiet = chiTietSanPhamService.getById(guestItem.getProductDetailId());
            if (chiTiet == null) return null;

            CartItemResponse response = new CartItemResponse();
            response.setId(guestItem.getProductDetailId());
            response.setProductDetailId(guestItem.getProductDetailId());
            response.setQuantity(guestItem.getQuantity());

            // Giá
            response.setPrice(chiTiet.getGiaBan());
            response.setTotalPrice(chiTiet.getGiaBan() * guestItem.getQuantity());

            // Thông tin sản phẩm
            if (chiTiet.getSanPham() != null) {
                response.setName(chiTiet.getSanPham().getTenSanPham());
                response.setCode(chiTiet.getSanPham().getMaSanPham());
            }

            // Ảnh sản phẩm
            if (chiTiet.getHinhAnh() != null) {
                response.setImage(chiTiet.getHinhAnh().getDuongDan());
            }

            // Size
            if (chiTiet.getKichCo() != null) {
                response.setSize(chiTiet.getKichCo().getTenKichCo());
            }

            // Màu (String thôi)
            if (chiTiet.getMauSac() != null) {
                response.setColor(chiTiet.getMauSac().getTenMauSac());
            }

            response.setStock(chiTiet.getSoLuong());

            return response;

        } catch (Exception e) {
            System.err.println("Error converting guest item: " + e.getMessage());
            return null;
        }
    }


    private HoaDon createGuestOrder(GuestCheckoutRequest request, List<GuestCartItem> cart) {
        // Lấy khách lẻ mặc định (id = 10)
        KhachHang khachLe = new KhachHang();
        khachLe.setId(10);

        HoaDon order = new HoaDon();
        order.setKhachHang(khachLe); // Gán khách lẻ
        order.setMaHoaDon("HD" + System.currentTimeMillis());

        // Thông tin checkout (ghi đè từ form người dùng nhập)
        order.setEmail(request.getEmail());
        order.setTenNguoiDung(request.getTenNguoiDung());
        order.setSdt(request.getSdt());
        order.setDiaChi(request.getDiaChi());

        order.setTrangThaiHoaDon("CHO_XAC_NHAN");
        order.setLoaiHoaDon("ONLINE");
        order.setPhiVanChuyen(new BigDecimal(request.getPhiVanChuyen()));
        order.setNgayTao(new Date());

        // Tính tổng tiền
        BigDecimal tongTien = cart.stream()
                .map(item -> {
                    ChiTietSanPham ctsp = chiTietSanPhamService.getById(item.getProductDetailId());
                    return new BigDecimal(ctsp.getGiaGoc() * item.getQuantity());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTongTien(tongTien);
        order.setTongThanhToan(tongTien.add(order.getPhiVanChuyen()));

        // Lưu đơn hàng
        HoaDon savedOrder = hoaDonService.save(order);

        // Tạo chi tiết đơn hàng
        for (GuestCartItem item : cart) {
            ChiTietSanPham ctsp = chiTietSanPhamService.getById(item.getProductDetailId());

            HoaDonChiTiet chiTiet = new HoaDonChiTiet();
            chiTiet.setHoaDon(savedOrder);
            chiTiet.setChiTietSanPham(ctsp);
            chiTiet.setGia(ctsp.getGiaBan());
            chiTiet.setSoLuong(item.getQuantity());
            chiTiet.setTrangThaiHoaDon("CHO_XAC_NHAN");
            chiTiet.setNgayTao(new Date());

            hoaDonChiTietService.save(chiTiet);
        }

        return savedOrder;
    }

}
