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

    // Láº¥y giá» hÃ ng cá»§a user hiá»‡n táº¡i
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUserOrGuestCart(HttpServletRequest request,
                                                       @RequestParam(required = false) String sessionId) {
        try {
            System.out.println("=== GET CURRENT CART (USER OR GUEST) ===");

            // 1. Thá»­ láº¥y user ID tá»« JWT
            Integer userId = null;
            try {
                userId = getUserIdFromRequest(request);
            } catch (Exception ex) {
                System.out.println("No JWT user found, fallback to guest.");
            }

            // 2. Náº¿u cÃ³ user -> tráº£ vá» giá» hÃ ng DB
            if (userId != null) {
                List<CartItemResponse> cartItems = gioHangService.getCartByUserId(userId);
                return ResponseEntity.ok(cartItems);
            }

            // 3. Náº¿u khÃ´ng cÃ³ user -> dÃ¹ng sessionId (guest)
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

            // 4. KhÃ´ng cÃ³ user, khÃ´ng cÃ³ sessionId -> giá» trá»‘ng
            return ResponseEntity.ok(Collections.emptyList());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }


    // ThÃªm sáº£n pháº©m vÃ o giá» hÃ ng
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest request, HttpServletRequest httpRequest) {
        try {
            System.out.println("=== ADD TO CART CONTROLLER DEBUG ===");

            // âœ… Sá»¬A: Láº¥y user ID tá»« JWT Filter attributes
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

    // Cáº­p nháº­t sá»‘ lÆ°á»£ng
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCartItem(@PathVariable Integer id, @RequestBody UpdateCartRequest request, HttpServletRequest httpRequest) {
        try {
            System.out.println("=== UPDATE CART ITEM DEBUG ===");

            // CÃ³ thá»ƒ cáº§n verify ownership cá»§a cart item
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

    // XÃ³a sáº£n pháº©m
    @DeleteMapping("/remove/{id}")
    public ResponseEntity<?> removeCartItem(@PathVariable Integer id, HttpServletRequest httpRequest) {
        try {
            System.out.println("=== REMOVE CART ITEM DEBUG ===");

            // CÃ³ thá»ƒ cáº§n verify ownership cá»§a cart item
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

    // XÃ³a toÃ n bá»™ giá» hÃ ng
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(HttpServletRequest request) {
        try {
            System.out.println("=== CLEAR CART DEBUG ===");

            // âœ… Sá»¬A: Láº¥y user ID tá»« JWT Filter attributes
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
     * âœ… Sá»¬A: Láº¥y User ID tá»« JWT Filter attributes thay vÃ¬ decode JWT
     * JWT Filter Ä‘Ã£ lÃ m viá»‡c nÃ y vÃ  Ä‘áº·t thÃ´ng tin vÃ o request attributes
     */
    private Integer getUserIdFromRequest(HttpServletRequest request) {
        try {
            // Láº¥y tá»« attributes Ä‘Æ°á»£c set bá»Ÿi JWT Filter
            Integer userId = (Integer) request.getAttribute("currentUserId");
            if (userId != null) {
                System.out.println("âœ… User ID from JWT Filter attributes: " + userId);
                return userId;
            }

            // Láº¥y user object vÃ  extract ID
            TaiKhoan currentUser = (TaiKhoan) request.getAttribute("currentUser");
            if (currentUser != null) {
                System.out.println("âœ… User ID from user object: " + currentUser.getId());
                return currentUser.getId();
            }

            // Fallback: log thÃ´ng tin debug
            System.err.println("âŒ No user info found in request attributes");
            System.err.println("Available attributes:");
            request.getAttributeNames().asIterator().forEachRemaining(name -> {
                System.err.println("  - " + name + ": " + request.getAttribute(name));
            });

            throw new RuntimeException("User not authenticated - no user info in request");

        } catch (Exception e) {
            System.err.println("âŒ Error getting user ID from request: " + e.getMessage());
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

            // TÃ¬m xem sáº£n pháº©m Ä‘Ã£ cÃ³ chÆ°a
            Optional<GuestCartItem> existingItem = cart.stream()
                    .filter(item -> item.getProductDetailId().equals(request.getProductDetailId()))
                    .findFirst();

            if (existingItem.isPresent()) {
                // Cáº­p nháº­t sá»‘ lÆ°á»£ng
                existingItem.get().setQuantity(existingItem.get().getQuantity() + request.getSoLuong());
            } else {
                // ThÃªm má»›i
                cart.add(new GuestCartItem(request.getProductDetailId(), request.getSoLuong()));
            }

            // Convert vÃ  return response
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

            // Táº¡o Ä‘Æ¡n hÃ ng guest
            HoaDon order = createGuestOrder(request, cart);

            // XÃ³a guest cart
            guestCarts.remove(sessionId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "orderId", order.getId(),
                    "orderCode", order.getMaHoaDon(),
                    "message", "Äáº·t hÃ ng thÃ nh cÃ´ng! MÃ£ Ä‘Æ¡n hÃ ng: " + order.getMaHoaDon()
            ));

        } catch (Exception e) {
            System.err.println("Error in guestCheckout: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    private CartItemResponse convertGuestItemToResponse(GuestCartItem guestItem) {
        try {
            // Láº¥y thÃ´ng tin chi tiáº¿t sáº£n pháº©m tá»« database
            ChiTietSanPham chiTiet = chiTietSanPhamService.getById(guestItem.getProductDetailId());
            if (chiTiet == null) return null;

            CartItemResponse response = new CartItemResponse();
            response.setId(guestItem.getProductDetailId());
            response.setProductDetailId(guestItem.getProductDetailId());
            response.setQuantity(guestItem.getQuantity());

            // GiÃ¡
            response.setPrice(chiTiet.getGiaBan());
            response.setTotalPrice(chiTiet.getGiaBan() * guestItem.getQuantity());

            // ThÃ´ng tin sáº£n pháº©m
            if (chiTiet.getSanPham() != null) {
                response.setName(chiTiet.getSanPham().getTenSanPham());
                response.setCode(chiTiet.getSanPham().getMaSanPham());
            }

            // áº¢nh sáº£n pháº©m
            if (chiTiet.getHinhAnh() != null) {
                response.setImage(chiTiet.getHinhAnh().getDuongDan());
            }

            // Size
            if (chiTiet.getKichCo() != null) {
                response.setSize(chiTiet.getKichCo().getTenKichCo());
            }

            // MÃ u (String thÃ´i)
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
        // Láº¥y khÃ¡ch láº» máº·c Ä‘á»‹nh (id = 10)
        KhachHang khachLe = new KhachHang();
        khachLe.setId(10);

        HoaDon order = new HoaDon();
        order.setKhachHang(khachLe); // GÃ¡n khÃ¡ch láº»
        order.setMaHoaDon("HD" + System.currentTimeMillis());

        // ThÃ´ng tin checkout (ghi Ä‘Ã¨ tá»« form ngÆ°á»i dÃ¹ng nháº­p)
        order.setEmail(request.getEmail());
        order.setTenNguoiDung(request.getTenNguoiDung());
        order.setSdt(request.getSdt());
        order.setDiaChi(request.getDiaChi());

        order.setTrangThaiHoaDon("CHO_XAC_NHAN");
        order.setLoaiHoaDon("ONLINE");
        order.setPhiVanChuyen(new BigDecimal(request.getPhiVanChuyen()));
        order.setNgayTao(new Date());

        // TÃ­nh tá»•ng tiá»n
        BigDecimal tongTien = cart.stream()
                .map(item -> {
                    ChiTietSanPham ctsp = chiTietSanPhamService.getById(item.getProductDetailId());
                    return new BigDecimal(ctsp.getGiaGoc() * item.getQuantity());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTongTien(tongTien);
        order.setTongThanhToan(tongTien.add(order.getPhiVanChuyen()));

        // LÆ°u Ä‘Æ¡n hÃ ng
        HoaDon savedOrder = hoaDonService.save(order);

        // Táº¡o chi tiáº¿t Ä‘Æ¡n hÃ ng
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
