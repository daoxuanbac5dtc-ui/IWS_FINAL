package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.Dto.AddToCartRequest;
import org.example.iws_websitesneaker.Dto.CartItemResponse; // THÃŠM IMPORT
import org.example.iws_websitesneaker.entity.GioHangChiTIet;

import java.util.List;

public interface GioHangService {

    /**
     * Láº¥y danh sÃ¡ch sáº£n pháº©m trong giá» hÃ ng cá»§a user
     */
    List<CartItemResponse> getCartByUserId(Integer userId); // Äá»”I RETURN TYPE

    /**
     * ThÃªm sáº£n pháº©m vÃ o giá» hÃ ng
     */
    CartItemResponse addToCart(Integer userId, AddToCartRequest request); // Äá»”I RETURN TYPE

    /**
     * Cáº­p nháº­t sá»‘ lÆ°á»£ng sáº£n pháº©m trong giá» hÃ ng
     */
    CartItemResponse updateCartItem(Integer cartItemId, Integer newQuantity); // Äá»”I RETURN TYPE

    /**
     * XÃ³a má»™t sáº£n pháº©m khá»i giá» hÃ ng
     */
    void removeCartItem(Integer cartItemId);

    /**
     * XÃ³a toÃ n bá»™ giá» hÃ ng cá»§a user
     */
    void clearCart(Integer userId);

    /**
     * Äáº¿m tá»•ng sá»‘ lÆ°á»£ng sáº£n pháº©m trong giá» hÃ ng
     */
    Integer getTotalQuantity(Integer userId);

    /**
     * TÃ­nh tá»•ng tiá»n cá»§a giá» hÃ ng
     */
    Double getTotalAmount(Integer userId);

    /**
     * Kiá»ƒm tra sáº£n pháº©m cÃ³ trong giá» hÃ ng khÃ´ng
     */
    boolean isProductInCart(Integer userId, Integer productDetailId);
}
