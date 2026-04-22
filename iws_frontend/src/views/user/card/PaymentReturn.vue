<script setup>
import axios from 'axios';
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const router = useRouter();
const route = useRoute();

const isLoading = ref(true);
const paymentResult = ref(null);

const API_BASE_URL = 'http://localhost:8080';

const processPaymentResult = async () => {
    try {
        console.log('=== Processing Payment Return ===');
        console.log('Route query:', route.query);

        // Lấy tất cả query parameters từ URL
        const params = { ...route.query };

        console.log('Sending params to backend:', params);

        // Gọi API xử lý kết quả
        const response = await axios.post(`${API_BASE_URL}/api/vnpay/payment-return`, params);

        console.log('Backend response:', response.data);

        paymentResult.value = response.data;

        if (response.data.success) {
            // 1) Re-check latest stock before creating invoice
            const stockOk = await validateLatestStockBeforeCreate();
            if (!stockOk) {
                paymentResult.value = {
                    success: false,
                    message: 'Rất tiếc, sản phẩm đã hết hoặc không đủ tồn kho trong quá trình thanh toán. Vui lòng thử lại.'
                };
                return;
            }

            // 2) Create invoice AFTER successful payment
            const raw = sessionStorage.getItem('pending_order_data');
            if (!raw) {
                paymentResult.value = {
                    success: false,
                    message: 'Không tìm thấy dữ liệu đơn hàng để tạo hóa đơn sau thanh toán.'
                };
                return;
            }
            const orderData = JSON.parse(raw);

            try {
                const token = localStorage.getItem('auth_token');
                const createRes = await axios.post(`${API_BASE_URL}/hoa-don/create`, orderData, {
                    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }
                });

                // 3) Clear cart and cleanup
                await clearCart();
                sessionStorage.removeItem('pending_order_data');

                // Attach created order id/code for navigation
                const createdId = createRes.data?.id || createRes.data?.data?.id || createRes.data?.maHoaDon;
                paymentResult.value.orderCode = createdId || paymentResult.value.orderCode;
            } catch (e) {
                console.error('Create invoice after payment failed:', e);
                paymentResult.value = {
                    success: false,
                    message: 'Thanh toán thành công nhưng tạo hóa đơn thất bại. Vui lòng liên hệ hỗ trợ.'
                };
            }
        }
    } catch (error) {
        console.error('Error processing payment result:', error);
        paymentResult.value = {
            success: false,
            message: 'Không thể xử lý kết quả thanh toán: ' + (error.response?.data?.message || error.message)
        };
    } finally {
        isLoading.value = false;
    }
};

// Validate latest stock again using items from pending_order_data
const validateLatestStockBeforeCreate = async () => {
    try {
        const raw = sessionStorage.getItem('pending_order_data');
        if (!raw) return false;
        const orderData = JSON.parse(raw);
        const token = localStorage.getItem('auth_token');

        for (const line of orderData.chiTietSanPham || []) {
            const res = await axios.get(`${API_BASE_URL}/api/san-pham-chi-tiet/${line.idChiTietSanPham}`, {
                headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }
            });
            const currentStock = Number(res.data?.soLuong || 0);
            if (currentStock < Number(line.soLuong)) {
                return false;
            }
        }
        return true;
    } catch (e) {
        console.error('validateLatestStockBeforeCreate error:', e);
        return false;
    }
};

const clearCart = async () => {
    try {
        const token = localStorage.getItem('auth_token');
        if (token) {
            await axios.delete(`${API_BASE_URL}/api/gio-hang/clear`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            console.log('Cart cleared successfully');
        }
        localStorage.removeItem('cart');

        // Dispatch events để update UI
        window.dispatchEvent(new StorageEvent('storage', { key: 'cart', newValue: null }));
        window.dispatchEvent(new CustomEvent('cartUpdated'));
    } catch (error) {
        console.error('Error clearing cart:', error);
    }
};

const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN').format(amount);
};

const goToOrderDetail = () => {
    if (paymentResult.value?.orderCode) {
        router.push(`/order-success/${paymentResult.value.orderCode}`);
    } else {
        goHome();
    }
};

const goHome = () => {
    router.push('/');
};

const retryPayment = () => {
    router.push('/checkout');
};

onMounted(() => {
    console.log('PaymentReturn component mounted');
    processPaymentResult();
});
</script>

<template>
    <div class="mx-auto mt-10 max-w-2xl p-6">
        <!-- Loading State -->
        <div v-if="isLoading" class="text-center">
            <div class="mx-auto mb-4 h-12 w-12 animate-spin rounded-full border-b-2 border-blue-500"></div>
            <p class="text-gray-600">Đang xử lý kết quả thanh toán...</p>
        </div>

        <!-- Success State -->
        <div v-else-if="paymentResult && paymentResult.success" class="text-center">
            <div class="mb-6 rounded-lg border border-green-200 bg-green-50 p-6">
                <div class="mb-4 text-6xl text-green-600">✅</div>
                <h1 class="mb-2 text-2xl font-bold text-green-800">Thanh toán thành công!</h1>
                <p class="text-green-600">{{ paymentResult.message }}</p>
            </div>

            <div class="rounded-lg border bg-white p-6 text-left">
                <h3 class="mb-4 font-semibold">Thông tin giao dịch:</h3>
                <div class="space-y-2 text-sm">
                    <div class="flex justify-between">
                        <span class="text-gray-600">Mã đơn hàng:</span>
                        <span class="font-medium">{{ paymentResult.orderCode }}</span>
                    </div>
                    <div class="flex justify-between">
                        <span class="text-gray-600">Số tiền:</span>
                        <span class="font-medium">{{ formatCurrency(paymentResult.amount) }}đ</span>
                    </div>
                    <div class="flex justify-between" v-if="paymentResult.transactionNo">
                        <span class="text-gray-600">Mã giao dịch:</span>
                        <span class="font-medium">{{ paymentResult.transactionNo }}</span>
                    </div>
                    <div class="flex justify-between" v-if="paymentResult.bankCode">
                        <span class="text-gray-600">Ngân hàng:</span>
                        <span class="font-medium">{{ paymentResult.bankCode }}</span>
                    </div>
                </div>
            </div>

            <div class="mt-6 space-y-3">
                <button @click="goToOrderDetail" class="w-full rounded-lg bg-blue-600 px-6 py-3 text-white transition hover:bg-blue-700">Xem chi tiết đơn hàng</button>
                <button @click="goHome" class="w-full rounded-lg border border-gray-300 px-6 py-3 transition hover:bg-gray-50">Về trang chủ</button>
            </div>
        </div>

        <!-- Error State -->
        <div v-else class="text-center">
            <div class="mb-6 rounded-lg border border-red-200 bg-red-50 p-6">
                <div class="mb-4 text-6xl text-red-600">❌</div>
                <h1 class="mb-2 text-2xl font-bold text-red-800">Thanh toán thất bại!</h1>
                <p class="text-red-600">{{ paymentResult?.message || 'Đã có lỗi xảy ra' }}</p>
            </div>

            <div class="rounded-lg border bg-white p-6 text-left" v-if="paymentResult?.orderCode">
                <h3 class="mb-4 font-semibold">Thông tin giao dịch:</h3>
                <div class="space-y-2 text-sm">
                    <div class="flex justify-between">
                        <span class="text-gray-600">Mã đơn hàng:</span>
                        <span class="font-medium">{{ paymentResult.orderCode }}</span>
                    </div>
                </div>
            </div>

            <div class="mt-6 space-y-3">
                <button @click="retryPayment" class="w-full rounded-lg bg-red-600 px-6 py-3 text-white transition hover:bg-red-700">Thử thanh toán lại</button>
                <button @click="goHome" class="w-full rounded-lg border border-gray-300 px-6 py-3 transition hover:bg-gray-50">Về trang chủ</button>
            </div>
        </div>
    </div>
</template>

<style scoped>
.animate-spin {
    animation: spin 1s linear infinite;
}

@keyframes spin {
    from {
        transform: rotate(0deg);
    }
    to {
        transform: rotate(360deg);
    }
}

.transition {
    transition: all 0.3s ease;
}
</style>
