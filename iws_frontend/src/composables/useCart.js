// composables/useCart.js - Cart management for both guest and logged in users
import axios from 'axios';
import { computed, ref } from 'vue';

const API_BASE_URL = 'http://localhost:8080';

// Helper functions
const getAuthToken = () => {
  return localStorage.getItem('auth_token');
};

const getUserId = () => {
  const userInfo = localStorage.getItem('user_info');
  if (userInfo) {
    return JSON.parse(userInfo).id;
  }
  return null;
};

const isAuthenticated = () => {
  return !!(getAuthToken() && getUserId());
};

// Build full image URL from backend path
const buildImageUrl = (imagePath) => {
  if (!imagePath) return '/placeholder-shoe.png';
  if (imagePath.startsWith('http')) return imagePath;
  return `${API_BASE_URL}${imagePath.startsWith('/') ? '' : '/'}${imagePath}`;
};

export function useCart() {
  const cartItems = ref([]);
  const isLoading = ref(false);
  const isGuestMode = ref(false);

  // Computed properties
  const subtotal = computed(() => {
    return cartItems.value.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  });

  const shippingFee = computed(() => {
    return subtotal.value >= 300000 ? 0 : 30000;
  });

  const total = computed(() => {
    return Math.max(0, subtotal.value + shippingFee.value);
  });

  const totalQuantity = computed(() => {
    return cartItems.value.reduce((sum, item) => sum + item.quantity, 0);
  });

  // Load cart (backend or localStorage)
  const loadCart = async () => {
    if (isLoading.value) return;

    try {
      isLoading.value = true;

      if (isAuthenticated()) {
        // User is logged in - load from backend
        isGuestMode.value = false;
        await loadCartFromBackend();
      } else {
        // User is not logged in - load from localStorage
        isGuestMode.value = true;
        loadCartFromLocalStorage();
      }
    } catch (error) {
      console.error('❌ Error loading cart:', error);
    } finally {
      isLoading.value = false;
    }
  };

  // Load cart from backend (for logged in users)
  const loadCartFromBackend = async () => {
    console.log('🔄 Loading cart from backend...');

    const response = await axios.get(`${API_BASE_URL}/api/gio-hang/current`, {
      headers: {
        'Authorization': `Bearer ${getAuthToken()}`,
        'Content-Type': 'application/json'
      }
    });

    console.log('✅ Cart loaded from backend:', response.data);

    cartItems.value = response.data.map(item => ({
      id: item.id,
      productDetailId: item.productDetailId,
      name: item.name,
      code: item.code,
      image: buildImageUrl(item.image),
      price: item.price,
      quantity: item.quantity,
      size: item.size,
      color: item.color,
      stock: item.stock,
      points: item.points || 0,
      totalPrice: item.totalPrice
    }));
  };

  // Load cart from localStorage (for guest users)
  const loadCartFromLocalStorage = () => {
    console.log('🔄 Loading guest cart from localStorage...');

    try {
      const guestCart = localStorage.getItem('guest_cart');
      if (guestCart) {
        cartItems.value = JSON.parse(guestCart);
        console.log('✅ Guest cart loaded:', cartItems.value);
      } else {
        cartItems.value = [];
        console.log('📦 No guest cart found, starting with empty cart');
      }
    } catch (error) {
      console.error('❌ Error loading guest cart:', error);
      cartItems.value = [];
    }
  };

  // Save cart to localStorage (for guest users)
  const saveCartToLocalStorage = () => {
    if (isGuestMode.value) {
      try {
        localStorage.setItem('guest_cart', JSON.stringify(cartItems.value));
        console.log('💾 Guest cart saved to localStorage');
      } catch (error) {
        console.error('❌ Error saving guest cart:', error);
      }
    }
  };

  // Add item to cart
  const addToCart = async (productDetailId, quantity = 1) => {
    try {
      if (isAuthenticated()) {
        // Logged in user - add to backend
        const response = await axios.post(`${API_BASE_URL}/api/gio-hang/add`, {
          chiTietSanPhamId: productDetailId,
          soLuong: quantity
        }, {
          headers: {
            'Authorization': `Bearer ${getAuthToken()}`,
            'Content-Type': 'application/json'
          }
        });

        console.log('✅ Item added to backend cart:', response.data);
        await loadCart(); // Reload cart
        window.dispatchEvent(new CustomEvent('cartUpdated'));

      } else {
        // Guest user - add to localStorage
        console.log('🔄 Adding item to guest cart:', { productDetailId, quantity });

        // Check if item already exists
        const existingItem = cartItems.value.find(item => item.productDetailId === productDetailId);

        if (existingItem) {
          // Update quantity
          existingItem.quantity += quantity;
        } else {
          // Add new item (you'll need to fetch product details)
          // For now, create a basic item structure
          const newItem = {
            id: Date.now(), // Temporary ID
            productDetailId: productDetailId,
            name: 'Sản phẩm', // You'll need to fetch this
            code: 'SP',
            image: '/placeholder-shoe.png',
            price: 0, // You'll need to fetch this
            quantity: quantity,
            size: 'M',
            color: 'Đen',
            stock: 10,
            points: 0,
            totalPrice: 0
          };
          cartItems.value.push(newItem);
        }

        saveCartToLocalStorage();
        window.dispatchEvent(new CustomEvent('cartUpdated'));
      }

      return true;
    } catch (error) {
      console.error('❌ Error adding to cart:', error);
      return false;
    }
  };

  // Update quantity
  const updateQuantity = async (cartItemId, newQuantity) => {
    if (newQuantity < 1) return false;

    const item = cartItems.value.find(item => item.id === cartItemId);
    if (item && newQuantity > item.stock) {
      console.warn(`Quantity ${newQuantity} exceeds stock ${item.stock}`);
      return false;
    }

    if (isGuestMode.value) {
      // Guest mode - update localStorage
      cartItems.value = cartItems.value.map(item =>
        item.id === cartItemId ? {
          ...item,
          quantity: newQuantity,
          totalPrice: item.price * newQuantity
        } : item
      );

      saveCartToLocalStorage();
      window.dispatchEvent(new CustomEvent('cartUpdated'));
      return true;

    } else {
      // Logged in mode - update backend
      try {
        const response = await axios.put(`${API_BASE_URL}/api/gio-hang/update/${cartItemId}`, {
          soLuong: newQuantity
        }, {
          headers: {
            'Authorization': `Bearer ${getAuthToken()}`,
            'Content-Type': 'application/json'
          }
        });

        // Update local state
        const updatedItem = response.data;
        cartItems.value = cartItems.value.map(item =>
          item.id === cartItemId ? {
            ...item,
            quantity: updatedItem.quantity,
            totalPrice: updatedItem.totalPrice
          } : item
        );

        window.dispatchEvent(new CustomEvent('cartUpdated'));
        return true;

      } catch (error) {
        console.error('❌ Error updating quantity:', error);
        return false;
      }
    }
  };

  // Remove item
  const removeItem = async (cartItemId) => {
    if (isGuestMode.value) {
      // Guest mode - remove from localStorage
      cartItems.value = cartItems.value.filter(item => item.id !== cartItemId);
      saveCartToLocalStorage();
      window.dispatchEvent(new CustomEvent('cartUpdated'));
      return true;

    } else {
      // Logged in mode - remove from backend
      try {
        await axios.delete(`${API_BASE_URL}/api/gio-hang/remove/${cartItemId}`, {
          headers: {
            'Authorization': `Bearer ${getAuthToken()}`,
            'Content-Type': 'application/json'
          }
        });

        cartItems.value = cartItems.value.filter(item => item.id !== cartItemId);
        window.dispatchEvent(new CustomEvent('cartUpdated'));
        return true;

      } catch (error) {
        console.error('❌ Error removing item:', error);
        return false;
      }
    }
  };

  // Clear cart
  const clearCart = async () => {
    if (isGuestMode.value) {
      // Guest mode - clear localStorage
      cartItems.value = [];
      saveCartToLocalStorage();
      window.dispatchEvent(new CustomEvent('cartUpdated'));
      return true;

    } else {
      // Logged in mode - clear backend
      try {
        await axios.delete(`${API_BASE_URL}/api/gio-hang/clear`, {
          headers: {
            'Authorization': `Bearer ${getAuthToken()}`,
            'Content-Type': 'application/json'
          }
        });

        cartItems.value = [];
        window.dispatchEvent(new CustomEvent('cartUpdated'));
        return true;

      } catch (error) {
        console.error('❌ Error clearing cart:', error);
        return false;
      }
    }
  };

  return {
    // State
    cartItems,
    isLoading,
    isGuestMode,

    // Computed
    subtotal,
    shippingFee,
    total,
    totalQuantity,

    // Methods
    loadCart,
    addToCart,
    updateQuantity,
    removeItem,
    clearCart,
    saveCartToLocalStorage
  };
}
