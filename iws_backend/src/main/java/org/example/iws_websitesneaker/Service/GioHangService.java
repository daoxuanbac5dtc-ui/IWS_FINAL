package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.Dto.AddToCartRequest;
import org.example.iws_websitesneaker.Dto.CartItemResponse; // THÊM IMPORT
import org.example.iws_websitesneaker.entity.GioHangChiTIet;

import java.util.List;

public interface GioHangService {

    /**
     * Lấy danh sách sản phẩm trong giỏ hàng của user
     */
    List<CartItemResponse> getCartByUserId(Integer userId); // ĐỔI RETURN TYPE

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    CartItemResponse addToCart(Integer userId, AddToCartRequest request); // ĐỔI RETURN TYPE

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     */
    CartItemResponse updateCartItem(Integer cartItemId, Integer newQuantity); // ĐỔI RETURN TYPE

    /**
     * Xóa một sản phẩm khỏi giỏ hàng
     */
    void removeCartItem(Integer cartItemId);

    /**
     * Xóa toàn bộ giỏ hàng của user
     */
    void clearCart(Integer userId);

    /**
     * Đếm tổng số lượng sản phẩm trong giỏ hàng
     */
    Integer getTotalQuantity(Integer userId);

    /**
     * Tính tổng tiền của giỏ hàng
     */
    Double getTotalAmount(Integer userId);

    /**
     * Kiểm tra sản phẩm có trong giỏ hàng không
     */
    boolean isProductInCart(Integer userId, Integer productDetailId);
}
