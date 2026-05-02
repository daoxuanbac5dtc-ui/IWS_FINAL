<script setup>
import axios from 'axios';
import { computed, onMounted, onUnmounted, ref } from 'vue';

// Reactive data
const cartItems = ref([]);
const isLoadingCart = ref(false);
const isGuestMode = ref(false);

// API Configuration
const API_BASE_URL = `${import.meta.env.VITE_API_BASE_URL}`;

// Confirmation Modal State
const confirmModal = ref({
    show: false,
    title: '',
    message: '',
    confirmText: 'Xác nhận',
    cancelText: 'Hủy',
    type: 'danger', // 'danger', 'warning', 'info'
    onConfirm: null,
    loading: false
});

// Auth helper
const getAuthToken = () => {
    return localStorage.getItem('auth_token');
};

// 🔎 Enrich guest items with DB data (ensure mã hàng from backend)
const enrichGuestItemsFromDB = async () => {
    try {
        const itemsNeedingCode = cartItems.value.filter(
            (it) => !it.code || it.code === 'SP' || it.code === 'UNKNOWN'
        );

        if (itemsNeedingCode.length === 0) return;

        const uniqueDetailIds = [...new Set(itemsNeedingCode.map((it) => it.productDetailId))].filter(Boolean);

        const responses = await Promise.all(
            uniqueDetailIds.map((id) =>
                axios
                    .get(`${API_BASE_URL}/api/san-pham-chi-tiet/${id}`)
                    .then((res) => ({ id, data: res.data }))
                    .catch((err) => {
                        console.warn('Failed to fetch detail for id', id, err);
                        return { id, data: null };
                    })
            )
        );

        const byId = new Map(responses.map((r) => [r.id, r.data]));

        let updated = false;
        cartItems.value = cartItems.value.map((it) => {
            if (!it.code || it.code === 'SP' || it.code === 'UNKNOWN') {
                const detail = byId.get(it.productDetailId);
                if (detail) {
                    updated = true;
                    return {
                        ...it,
                        code: detail.maChiTiet || it.code,
                        name: it.name || detail.sanPham?.tenSanPham || it.name,
                        size: it.size || detail.kichCo?.tenKichCo || it.size,
                        color: it.color || (detail.mauSac ? { id: detail.mauSac.id, name: detail.mauSac.tenMauSac } : it.color)
                    };
                }
            }
            return it;
        });

        if (updated) {
            // persist back to localStorage to keep consistency
            saveCartToLocalStorage();
        }
    } catch (e) {
        console.warn('enrichGuestItemsFromDB error:', e);
    }
};

const getUserId = () => {
    const userInfo = localStorage.getItem('user_info');
    if (userInfo) {
        return JSON.parse(userInfo).id;
    }
    return null;
};

// Helper function for color mapping
const getColorHex = (tenMau) => {
    const colorMap = {
        Trắng: '#ffffff',
        Đen: '#000000',
        Đỏ: '#ff0000',
        'Xanh Dương': '#0000ff',
        'Xanh Navy': '#001f3f',
        'Xanh Lá': '#008000',
        Vàng: '#ffff00',
        Cam: '#ffa500',
        Hồng: '#ff69b4',
        Tím: '#800080',
        Nâu: '#8b4513',
        Xám: '#808080',
        Bạc: '#c0c0c0',
        'Vàng Gold': '#ffd700',
        Be: '#f5f5dc'
    };
    return colorMap[tenMau] || '#cccccc';
};

// Build full image URL from backend path
const buildImageUrl = (imagePath) => {
    if (!imagePath) return '/placeholder-shoe.png';
    if (imagePath.startsWith('http')) return imagePath;
    return `${API_BASE_URL}${imagePath.startsWith('/') ? '' : '/'}${imagePath}`;
};

// Confirmation Modal Functions
const showConfirmModal = (title, message, onConfirm, options = {}) => {
    confirmModal.value = {
        show: true,
        title,
        message,
        confirmText: options.confirmText || 'Xác nhận',
        cancelText: options.cancelText || 'Hủy',
        type: options.type || 'danger',
        onConfirm,
        loading: false
    };
};

const hideConfirmModal = () => {
    confirmModal.value.show = false;
    confirmModal.value.loading = false;
    confirmModal.value.onConfirm = null;
};

const handleConfirm = async () => {
    if (confirmModal.value.onConfirm) {
        confirmModal.value.loading = true;
        try {
            await confirmModal.value.onConfirm();
            hideConfirmModal();
        } catch (error) {
            confirmModal.value.loading = false;
            console.error('Error in confirmation action:', error);
        }
    }
};

// 🛒 Load cart (backend or localStorage)
const loadCart = async () => {
    if (isLoadingCart.value) return;

    try {
        isLoadingCart.value = true;

        // Check if user is authenticated
        const token = getAuthToken();
        const userId = getUserId();

        if (token && userId) {
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
        showNotification('error', 'Lỗi', 'Không thể tải giỏ hàng');
    } finally {
        isLoadingCart.value = false;
    }
};

// 🛒 Load cart from backend (for logged in users)
const loadCartFromBackend = async () => {
    console.log('🔄 Loading cart from backend...');
    console.log('🔐 Auth token:', getAuthToken() ? 'Present' : 'Missing');

    const response = await axios.get(`${API_BASE_URL}/api/gio-hang/current`, {
        headers: {
            Authorization: `Bearer ${getAuthToken()}`,
            'Content-Type': 'application/json'
        }
    });

    console.log('✅ Cart loaded from backend:', response.data);

    // Backend trả về array của CartItemResponse
    cartItems.value = response.data.map((item) => ({
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

    console.log('📦 Mapped cart items:', cartItems.value);
};

// 🛒 Load cart from localStorage (for guest users)
const loadCartFromLocalStorage = async () => {
    console.log('🔄 Loading guest cart from localStorage...');

    try {
        const guestCart = localStorage.getItem('guest_cart');
        if (guestCart) {
            const parsedCart = JSON.parse(guestCart);
            // Process guest cart items similar to backend cart
            cartItems.value = parsedCart.map((item) => ({
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
                points: item.points,
                totalPrice: item.totalPrice
            }));
            // Enrich missing or placeholder codes from DB
            await enrichGuestItemsFromDB();
            console.log('✅ Guest cart loaded and processed:', cartItems.value);
        } else {
            cartItems.value = [];
            console.log('📦 No guest cart found, starting with empty cart');
        }
    } catch (error) {
        console.error('❌ Error loading guest cart:', error);
        cartItems.value = [];
    }
};

// 💾 Save cart to localStorage (for guest users)
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

// ✏️ Update quantity
const updateQuantity = async (cartItemId, newQuantity) => {
    if (newQuantity < 1) return;

    const item = cartItems.value.find((item) => item.id === cartItemId);
    if (item && newQuantity > item.stock) {
        showNotification('warning', 'Vượt quá số lượng', `Số lượng không được vượt quá ${item.stock} sản phẩm có sẵn!`);
        return;
    }

    if (isGuestMode.value) {
        // Guest mode - update localStorage
        console.log(`🔄 Updating guest cart quantity for item ${cartItemId} to ${newQuantity}`);

        cartItems.value = cartItems.value.map((item) =>
            item.id === cartItemId
                ? {
                      ...item,
                      quantity: newQuantity,
                      totalPrice: item.price * newQuantity
                  }
                : item
        );

        saveCartToLocalStorage();
        showNotification('success', 'Cập nhật thành công', 'Đã cập nhật số lượng sản phẩm');
        window.dispatchEvent(new CustomEvent('cartUpdated'));
    } else {
        // Logged in mode - update backend
        try {
            console.log(`🔄 Updating quantity for item ${cartItemId} to ${newQuantity}`);

            const response = await axios.put(
                `${API_BASE_URL}/api/gio-hang/update/${cartItemId}`,
                {
                    soLuong: newQuantity
                },
                {
                    headers: {
                        Authorization: `Bearer ${getAuthToken()}`,
                        'Content-Type': 'application/json'
                    }
                }
            );

            console.log('✅ Quantity updated:', response.data);

            // Update local state
            const updatedItem = response.data;
            cartItems.value = cartItems.value.map((item) =>
                item.id === cartItemId
                    ? {
                          ...item,
                          quantity: updatedItem.quantity,
                          totalPrice: updatedItem.totalPrice
                      }
                    : item
            );

            showNotification('success', 'Cập nhật thành công', 'Đã cập nhật số lượng sản phẩm');
            window.dispatchEvent(new CustomEvent('cartUpdated'));
        } catch (error) {
            console.error('❌ Error updating quantity:', error);
            showNotification('error', 'Lỗi', 'Không thể cập nhật số lượng');
        }
    }
};

// 🗑️ Remove item with custom modal
const removeItem = (cartItemId) => {
    const item = cartItems.value.find((item) => item.id === cartItemId);

    showConfirmModal(
        '🗑️ Xóa sản phẩm',
        `Bạn có chắc chắn muốn xóa "<strong>${item?.name || 'sản phẩm này'}</strong>" khỏi giỏ hàng không?`,
        async () => {
            if (isGuestMode.value) {
                // Guest mode - remove from localStorage
                console.log(`🔄 Removing item ${cartItemId} from guest cart`);

                cartItems.value = cartItems.value.filter((item) => item.id !== cartItemId);
                saveCartToLocalStorage();

                showNotification('success', 'Đã xóa sản phẩm', `"${item?.name}" đã được xóa khỏi giỏ hàng`);
                window.dispatchEvent(new CustomEvent('cartUpdated'));
            } else {
                // Logged in mode - remove from backend
                try {
                    console.log(`🔄 Removing item ${cartItemId}`);

                    await axios.delete(`${API_BASE_URL}/api/gio-hang/remove/${cartItemId}`, {
                        headers: {
                            Authorization: `Bearer ${getAuthToken()}`,
                            'Content-Type': 'application/json'
                        }
                    });

                    console.log('✅ Item removed from backend');

                    // Update local state
                    cartItems.value = cartItems.value.filter((item) => item.id !== cartItemId);

                    showNotification('success', 'Đã xóa sản phẩm', `"${item?.name}" đã được xóa khỏi giỏ hàng`);
                    window.dispatchEvent(new CustomEvent('cartUpdated'));
                } catch (error) {
                    console.error('❌ Error removing item:', error);
                    showNotification('error', 'Lỗi', 'Không thể xóa sản phẩm');
                }
            }
        },
        {
            confirmText: 'Xóa ngay',
            cancelText: 'Hủy bỏ',
            type: 'danger'
        }
    );
};

// 🧹 Clear cart with custom modal
const clearCart = () => {
    const itemCount = cartItems.value.length;

    showConfirmModal(
        '🧹 Xóa toàn bộ giỏ hàng',
        `Bạn có chắc chắn muốn xóa <strong>tất cả ${itemCount} sản phẩm</strong> trong giỏ hàng không?<br><br><span class="text-amber-600">⚠️ Hành động này không thể hoàn tác!</span>`,
        async () => {
            if (isGuestMode.value) {
                // Guest mode - clear localStorage
                console.log('🔄 Clearing guest cart...');

                cartItems.value = [];
                saveCartToLocalStorage();

                showNotification('success', 'Đã xóa giỏ hàng', `Đã xóa ${itemCount} sản phẩm khỏi giỏ hàng`);
                window.dispatchEvent(new CustomEvent('cartUpdated'));
            } else {
                // Logged in mode - clear backend
                try {
                    console.log('🔄 Clearing cart...');

                    await axios.delete(`${API_BASE_URL}/api/gio-hang/clear`, {
                        headers: {
                            Authorization: `Bearer ${getAuthToken()}`,
                            'Content-Type': 'application/json'
                        }
                    });

                    console.log('✅ Cart cleared from backend');

                    cartItems.value = [];

                    showNotification('success', 'Đã xóa giỏ hàng', `Đã xóa ${itemCount} sản phẩm khỏi giỏ hàng`);
                    window.dispatchEvent(new CustomEvent('cartUpdated'));
                } catch (error) {
                    console.error('❌ Error clearing cart:', error);
                    showNotification('error', 'Lỗi', 'Không thể xóa giỏ hàng');
                }
            }
        },
        {
            confirmText: 'Xóa tất cả',
            cancelText: 'Giữ lại',
            type: 'danger'
        }
    );
};

// Notification system
const notification = ref({
    show: false,
    type: 'success',
    title: '',
    message: '',
    duration: 3000
});

const showNotification = (type, title, message, duration = 3000) => {
    notification.value = {
        show: true,
        type,
        title,
        message,
        duration
    };

    setTimeout(() => {
        hideNotification();
    }, duration);
};

const hideNotification = () => {
    notification.value.show = false;
};

// Navigation functions
const continueShopping = () => {
    window.location.href = '/products';
};

const proceedToCheckout = () => {
    if (cartItems.value.length === 0) {
        showNotification('warning', 'Giỏ hàng trống', 'Vui lòng thêm sản phẩm trước khi thanh toán');
        return;
    }

    // Both guest and logged in users go to the same checkout page
    window.location.href = '/checkout';
};

// Handle image loading error
const handleImageError = (event, fallbackSrc = '/placeholder-shoe.png') => {
    if (event.target.src !== fallbackSrc) {
        event.target.src = fallbackSrc;
    } else {
        event.target.style.display = 'none';
        const placeholder = event.target.parentElement;
        if (placeholder) {
            placeholder.innerHTML = `
        <div class="w-full h-full flex items-center justify-center bg-gray-100 text-gray-400">
          <svg class="w-8 h-8" fill="currentColor" viewBox="0 0 24 24">
            <path d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/>
          </svg>
        </div>
      `;
        }
    }
};

// Computed properties
const subtotal = computed(() => {
    return cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0);
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

// Utility functions
const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN').format(amount);
};

// Listen for cart updates from other components
const handleCartUpdated = () => {
    console.log('🔄 Cart updated event received, reloading...');
    loadCart();
};

// Lifecycle hooks
onMounted(() => {
    console.log('🚀 Cart component mounted');
    loadCart(); // Load cart (backend or localStorage based on auth status)

    // Listen for cart updates
    window.addEventListener('cartUpdated', handleCartUpdated);
});

onUnmounted(() => {
    window.removeEventListener('cartUpdated', handleCartUpdated);
});
</script>

<template>
    <!-- Custom Confirmation Modal -->
    <Transition name="modal">
        <div v-if="confirmModal.show" class="fixed inset-0 z-50 overflow-y-auto">
            <!-- Backdrop -->
            <div class="flex min-h-screen items-center justify-center px-4 pb-20 pt-4 text-center sm:block sm:p-0">
                <div class="fixed inset-0 transition-opacity" aria-hidden="true" @click="hideConfirmModal">
                    <div class="absolute inset-0 bg-gray-900 opacity-75"></div>
                </div>

                <!-- Modal -->
                <div class="inline-block transform overflow-hidden rounded-2xl bg-white px-4 pb-4 pt-5 text-left align-bottom shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg sm:p-6 sm:align-middle">
                    <!-- Icon -->
                    <div
                        class="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full"
                        :class="{
                            'bg-red-100': confirmModal.type === 'danger',
                            'bg-yellow-100': confirmModal.type === 'warning',
                            'bg-blue-100': confirmModal.type === 'info'
                        }"
                    >
                        <svg v-if="confirmModal.type === 'danger'" class="h-8 w-8 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                        <svg v-else-if="confirmModal.type === 'warning'" class="h-8 w-8 text-yellow-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.732 15.5c-.77.833.192 2.5 1.732 2.5z" />
                        </svg>
                        <svg v-else class="h-8 w-8 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                    </div>

                    <!-- Content -->
                    <div class="text-center">
                        <h3 class="mb-2 text-lg font-bold leading-6 text-gray-900">
                            {{ confirmModal.title }}
                        </h3>
                        <div class="mt-2">
                            <p class="text-sm text-gray-600" v-html="confirmModal.message"></p>
                        </div>
                    </div>

                    <!-- Actions -->
                    <div class="mt-6 flex flex-col-reverse gap-3 sm:flex-row sm:justify-center">
                        <button
                            type="button"
                            class="inline-flex w-full justify-center rounded-xl border border-gray-300 bg-white px-4 py-3 text-base font-medium text-gray-700 shadow-sm transition-all hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-gray-200 focus:ring-offset-2 sm:w-auto sm:text-sm"
                            @click="hideConfirmModal"
                            :disabled="confirmModal.loading"
                        >
                            {{ confirmModal.cancelText }}
                        </button>
                        <button
                            type="button"
                            class="inline-flex w-full items-center justify-center rounded-xl border border-transparent px-4 py-3 text-base font-medium text-white shadow-sm transition-all focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 sm:w-auto sm:text-sm"
                            :class="{
                                'bg-red-600 hover:bg-red-700 focus:ring-red-500': confirmModal.type === 'danger',
                                'bg-yellow-600 hover:bg-yellow-700 focus:ring-yellow-500': confirmModal.type === 'warning',
                                'bg-blue-600 hover:bg-blue-700 focus:ring-blue-500': confirmModal.type === 'info'
                            }"
                            @click="handleConfirm"
                            :disabled="confirmModal.loading"
                        >
                            <svg v-if="confirmModal.loading" class="-ml-1 mr-2 h-4 w-4 animate-spin text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                            </svg>
                            {{ confirmModal.loading ? 'Đang xử lý...' : confirmModal.confirmText }}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </Transition>

    <!-- Notification -->
    <Transition name="slide-fade">
        <div
            v-if="notification.show"
            :class="[
                'fixed right-4 top-4 z-50 max-w-sm rounded-lg p-4 shadow-lg',
                notification.type === 'success' ? 'bg-green-500 text-white' : notification.type === 'warning' ? 'bg-yellow-500 text-white' : notification.type === 'info' ? 'bg-blue-500 text-white' : 'bg-red-500 text-white'
            ]"
        >
            <h4 class="font-bold">{{ notification.title }}</h4>
            <p class="text-sm">{{ notification.message }}</p>
        </div>
    </Transition>

    <div class="mx-auto min-h-screen w-full bg-gray-50 p-4 sm:p-6">
        <div class="overflow-hidden rounded-lg bg-white shadow-lg">
            <!-- Header -->
            <div class="border-b border-gray-200 p-4 sm:p-6">
                <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
                    <div class="flex items-center gap-3">
                        <div class="h-8 w-8 text-2xl text-orange-500">🛒</div>
                        <h1 class="text-2xl font-bold text-gray-800 sm:text-3xl">Giỏ hàng của bạn</h1>
                    </div>
                    <div class="flex items-center gap-4">
                        <span class="text-gray-600">
                            <span class="font-semibold">{{ cartItems.length }}</span> sản phẩm
                            <span class="text-sm">| {{ totalQuantity }} món</span>
                        </span>
                        <button v-if="cartItems.length > 0" @click="clearCart" class="text-sm font-medium text-red-500 transition-colors hover:text-red-700">Xóa tất cả</button>
                    </div>
                </div>
            </div>

            <!-- Loading State -->
            <div v-if="isLoadingCart" class="py-12 text-center">
                <div class="mx-auto mb-4 h-12 w-12 animate-spin rounded-full border-b-2 border-orange-500"></div>
                <p class="text-gray-600">Đang tải giỏ hàng...</p>
            </div>

            <div v-else class="flex flex-col gap-6 p-4 sm:p-6 lg:flex-row">
                <!-- Cart Items -->
                <div class="flex-1">
                    <!-- Empty Cart State -->
                    <div v-if="cartItems.length === 0" class="py-12 text-center sm:py-20">
                        <div class="mb-4 text-6xl">🛒</div>
                        <h3 class="mb-2 text-xl font-medium text-gray-800">Giỏ hàng trống</h3>
                        <p class="mb-6 text-gray-600">Bạn chưa có sản phẩm nào trong giỏ hàng</p>
                        <button @click="continueShopping" class="rounded-lg bg-orange-500 px-6 py-3 font-medium text-white transition-colors hover:bg-orange-600">Tiếp tục mua sắm</button>
                    </div>

                    <!-- Cart Items Header - Desktop only -->
                    <div v-else class="mb-4 hidden rounded-lg bg-gray-50 p-4 lg:block">
                        <div class="grid-cols-13 grid gap-4 text-sm font-medium text-gray-600">
                            <div class="col-span-1">Hình ảnh</div>
                            <div class="col-span-3">Tên sản phẩm</div>
                            <div class="col-span-2">Mã hàng</div>
                            <div class="col-span-2">Số lượng</div>
                            <div class="col-span-2">Đơn giá</div>
                            <div class="col-span-2">Thành tiền</div>
                            <div class="col-span-1">Thao tác</div>
                        </div>
                    </div>

                    <!-- Cart Items List -->
                    <div class="space-y-4">
                        <div v-for="item in cartItems" :key="item.id" class="cart-item rounded-lg border border-gray-200 bg-white p-4 transition-all duration-200 hover:shadow-md">
                            <!-- Desktop Layout -->
                            <div class="grid-cols-13 hidden items-center gap-4 lg:grid">
                                <!-- Product Image -->
                                <div class="col-span-1">
                                    <div class="flex h-20 w-20 items-center justify-center overflow-hidden rounded-lg bg-gray-100">
                                        <img :src="item.image" :alt="item.name" class="h-full w-full rounded-lg object-cover" @error="handleImageError" />
                                    </div>
                                </div>

                                <!-- Product Info -->
                                <div class="col-span-3">
                                    <h3 class="mb-1 line-clamp-2 text-sm font-medium text-gray-800">{{ item.name }}</h3>
                                    <div class="mb-1 flex flex-wrap gap-1 text-xs text-gray-500">
                                        <span v-if="item.size" class="rounded bg-gray-100 px-2 py-1"> Size: {{ item.size }} </span>
                                        <span v-if="item.color?.name" class="flex items-center gap-1 rounded bg-gray-100 px-2 py-1">
                                            <div class="h-2 w-2 rounded-full border border-gray-300" :style="{ backgroundColor: getColorHex(item.color.name) }"></div>
                                            {{ item.color.name }}
                                        </span>
                                    </div>
                                    <!-- <p class="text-xs text-green-600">Điểm: +{{ item.points }}</p>
                                    <p class="text-xs text-gray-500">Kho: {{ item.stock }}</p> -->
                                </div>

                                <!-- Product Code -->
                                <div class="col-span-2">
                                    <div class="break-all font-mono text-xs text-gray-600">{{ item.code }}</div>
                                </div>

                                <!-- Quantity Controls -->
                                <div class="col-span-2">
                                    <div class="space-y-2">
                                        <div class="flex max-w-40 overflow-hidden rounded-md border bg-white">
                                            <button
                                                @click="updateQuantity(item.id, item.quantity - 1)"
                                                class="flex h-8 w-8 items-center justify-center text-lg font-bold transition-colors hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-50"
                                                :disabled="item.quantity <= 1"
                                            >
                                                −
                                            </button>
                                            <input
                                                type="number"
                                                :value="item.quantity"
                                                @input="updateQuantity(item.id, parseInt($event.target.value) || 1)"
                                                class="h-8 w-16 border-x text-center text-sm focus:bg-blue-50 focus:outline-none"
                                                min="1"
                                                :max="item.stock"
                                            />
                                            <button
                                                @click="updateQuantity(item.id, item.quantity + 1)"
                                                class="flex h-8 w-8 items-center justify-center text-lg font-bold transition-colors hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-50"
                                                :disabled="item.quantity >= item.stock"
                                            >
                                                +
                                            </button>
                                        </div>
                                        <div class="text-xs text-gray-500">{{ item.quantity }} / {{ item.stock }}</div>
                                    </div>
                                </div>

                                <!-- Unit Price -->
                                <div class="col-span-2">{{ formatCurrency(item.price) }}đ</div>

                                <!-- Total Price -->
                                <div class="col-span-2">{{ formatCurrency(item.price * item.quantity) }}đ</div>

                                <!-- Actions -->
                                <div class="col-span-1">
                                    <div class="flex flex-col items-center gap-2">
                                        <button @click="removeItem(item.id)" class="rounded bg-red-500 px-3 py-1 text-xs font-medium text-white transition-colors hover:bg-red-600" title="Xóa sản phẩm">Xóa</button>
                                    </div>
                                </div>
                            </div>

                            <!-- Mobile Layout -->
                            <div class="lg:hidden">
                                <div class="flex gap-4">
                                    <!-- Product Image -->
                                    <div class="flex h-20 w-20 flex-shrink-0 items-center justify-center overflow-hidden rounded-lg bg-gray-100">
                                        <img :src="item.image" :alt="item.name" class="h-full w-full rounded-lg object-cover" @error="handleImageError" />
                                    </div>

                                    <!-- Product Info -->
                                    <div class="flex-1">
                                        <h3 class="mb-2 font-medium text-gray-800">{{ item.name }}</h3>
                                        <div class="mb-2 flex flex-wrap gap-2 text-sm text-gray-500">
                                            <span v-if="item.size" class="rounded bg-gray-100 px-2 py-1 text-xs"> Size: {{ item.size }} </span>
                                            <span v-if="item.color?.name" class="flex items-center gap-1 rounded bg-gray-100 px-2 py-1 text-xs">
                                                <div class="h-3 w-3 rounded-full border border-gray-300" :style="{ backgroundColor: getColorHex(item.color.name) }"></div>
                                                {{ item.color.name }}
                                            </span>
                                        </div>
                                        <p class="mb-1 text-xs text-gray-500">Mã: {{ item.code }}</p>
                                        <p class="text-xs text-green-600">Điểm thưởng: +{{ item.points }}</p>
                                    </div>

                                    <!-- Actions -->
                                    <div class="flex flex-col gap-1">
                                        <button @click="removeItem(item.id)" class="rounded bg-red-500 px-2 py-1 text-xs text-white hover:bg-red-600" title="Xóa sản phẩm">Xóa</button>
                                    </div>
                                </div>

                                <!-- Mobile Quantity and Price -->
                                <div class="mt-4 space-y-3">
                                    <div class="flex items-center justify-between">
                                        <span class="text-sm text-gray-600">Số lượng:</span>
                                        <div class="flex items-center gap-3">
                                            <div class="flex overflow-hidden rounded-lg border bg-white">
                                                <button @click="updateQuantity(item.id, item.quantity - 1)" class="p-2 text-lg font-bold transition-colors hover:bg-gray-100 disabled:opacity-50" :disabled="item.quantity <= 1">−</button>
                                                <input
                                                    type="number"
                                                    :value="item.quantity"
                                                    @input="updateQuantity(item.id, parseInt($event.target.value) || 1)"
                                                    class="w-12 border-x py-2 text-center text-sm focus:bg-blue-50 focus:outline-none"
                                                    min="1"
                                                    :max="item.stock"
                                                />
                                                <button @click="updateQuantity(item.id, item.quantity + 1)" class="p-2 text-lg font-bold transition-colors hover:bg-gray-100 disabled:opacity-50" :disabled="item.quantity >= item.stock">+</button>
                                            </div>
                                            <span class="text-xs text-gray-400">/ {{ item.stock }}</span>
                                        </div>
                                    </div>

                                    <div class="flex justify-between">
                                        <div class="text-left">
                                            <div class="text-sm text-gray-600">Đơn giá:</div>
                                            <div class="font-medium text-gray-800">{{ formatCurrency(item.price) }}đ</div>
                                        </div>
                                        <div class="text-right">
                                            <div class="text-sm text-gray-600">Thành tiền:</div>
                                            <div class="text-lg font-bold text-orange-600">{{ formatCurrency(item.price * item.quantity) }}đ</div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Order Summary -->
                <div class="w-full lg:w-96" v-if="cartItems.length > 0">
                    <div class="sticky top-6 rounded-lg border bg-white p-6">
                        <h3 class="mb-4 text-lg font-bold text-gray-800">📋 Tóm tắt đơn hàng</h3>

                        <div class="mb-4 space-y-3">
                            <div class="flex justify-between">
                                <span class="text-gray-600">Tạm tính ({{ totalQuantity }} món):</span>
                                <span class="font-medium">{{ formatCurrency(subtotal) }}đ</span>
                            </div>

                            <div class="flex justify-between">
                                <span class="text-gray-600">🚚 Phí vận chuyển:</span>
                                <span class="font-medium" :class="shippingFee === 0 ? 'text-green-600' : ''">
                                    {{ shippingFee === 0 ? 'Miễn phí' : formatCurrency(shippingFee) + 'đ' }}
                                </span>
                            </div>

                            <div v-if="subtotal < 300000 && subtotal > 0" class="rounded bg-orange-50 p-3 text-xs text-orange-600">🎁 Thêm {{ formatCurrency(300000 - subtotal) }}đ để được <strong>miễn phí vận chuyển</strong></div>
                        </div>

                        <hr class="my-4" />

                        <div class="mb-6 flex justify-between text-xl font-bold">
                            <span>💳 Tổng cộng:</span>
                            <span class="text-orange-600">{{ formatCurrency(total) }}đ</span>
                        </div>

                        <div class="space-y-3">
                            <button @click="proceedToCheckout" class="w-full rounded-lg bg-red-600 py-4 text-lg font-bold text-white transition-colors hover:bg-red-700">🛒 THANH TOÁN NGAY</button>

                            <button @click="continueShopping" class="w-full rounded-lg border-2 border-gray-300 py-3 font-medium text-gray-700 transition-colors hover:border-gray-400">🛍️ TIẾP TỤC MUA SẮM</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.line-clamp-2 {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.slide-fade-enter-active {
    transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
    transition: all 0.3s ease-in;
}

.slide-fade-enter-from {
    transform: translateX(20px);
    opacity: 0;
}

.slide-fade-leave-to {
    transform: translateX(20px);
    opacity: 0;
}

/* Modal animations */
.modal-enter-active,
.modal-leave-active {
    transition: opacity 0.3s ease;
}

.modal-enter-active .modal-container,
.modal-leave-active .modal-container {
    transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
    opacity: 0;
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
    opacity: 0;
    transform: scale(0.9) translateY(-20px);
}

@keyframes slideIn {
    from {
        opacity: 0;
        transform: translateY(10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.cart-item {
    animation: slideIn 0.3s ease-out;
}

.cart-item:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* Custom scrollbar */
::-webkit-scrollbar {
    width: 6px;
}

::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 10px;
}

::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 10px;
}

::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
}

/* Fixed width for action buttons */
.cart-item button {
    flex-shrink: 0;
}

/* Quantity input styling */
.cart-item input[type='number']::-webkit-outer-spin-button,
.cart-item input[type='number']::-webkit-inner-spin-button {
    -webkit-appearance: none;
    margin: 0;
}

.cart-item input[type='number'] {
    -moz-appearance: textfield;
}

/* Better text wrapping */
.break-all {
    word-break: break-all;
}

/* Grid layout for desktop - 13 columns */
.grid-cols-13 {
    grid-template-columns: repeat(13, 1fr);
}
</style>

