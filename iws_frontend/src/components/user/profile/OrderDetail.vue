<script setup>
import axios from 'axios';
import { useToast } from 'primevue/usetoast';
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();
const toast = useToast();

// State
const orders = ref([]);
const isLoadingOrders = ref(false);
const orderDetails = reactive({});
const loadingOrderDetails = reactive({});
const showAllProducts = reactive({});
const showAllOrders = ref(false);

// State để cache URL ảnh
const imageCache = ref({});
const loadingImages = ref(new Set());

// API Configuration
const API_BASE_URL = `${import.meta.env.VITE_API_BASE_URL}`;

// Detail mode based on route param
const isDetailMode = computed(() => !!route.params.id);
const currentOrderId = computed(() => (route.params.id ? String(route.params.id) : null));
const currentOrder = computed(() => {
    if (!isDetailMode.value) return null;
    return orders.value.find((o) => String(o.id) === currentOrderId.value) || null;
});

// Auth helpers
const getAuthToken = () => localStorage.getItem('auth_token');

// Load orders và tự động load chi tiết
const loadOrders = async () => {
    isLoadingOrders.value = true;
    try {
        const response = await axios.get(`${API_BASE_URL}/hoa-don/my-orders`, {
            headers: {
                Authorization: `Bearer ${getAuthToken()}`,
                'Content-Type': 'application/json'
            }
        });

        const allOrders = response.data || [];
        orders.value = allOrders.filter((order) => {
            return order.loaiHoaDon === 'ONLINE' || order.phuongThucDatHang === 'ONLINE' || order.isOnline === true || order.type === 'ONLINE';
        });

        // Khởi tạo state showAllProducts cho mỗi order
        orders.value.forEach((order) => {
            showAllProducts[order.id] = false;
        });

        // Tự động load chi tiết cho tất cả đơn hàng
        for (const order of orders.value) {
            await loadOrderDetails(order.id);
        }

        console.log(`Loaded ${orders.value.length} online orders with details`);

        // Debug dữ liệu thực tế
        if (orders.value.length > 0) {
            const firstOrder = orders.value[0];
            console.log('=== ORDER DEBUG ===');
            console.log('First Order:', firstOrder);

            const firstOrderId = firstOrder?.id;
            const firstItem = orderDetails[firstOrderId]?.chiTietSanPham?.[0];
            console.log('First Item:', firstItem);

            console.log('=== TOTAL CALCULATION ===');
            console.log('tongThanhToan:', firstOrder?.tongThanhToan);
            console.log('tongTien:', firstOrder?.tongTien);
            console.log('giaTriDiem:', firstOrder?.giaTriDiem);
            console.log('phiVanChuyen:', firstOrder?.phiVanChuyen);
            console.log('soTienThanhToan:', firstOrder?.soTienThanhToan);
            console.log('Computed total:', getTotalAmount(firstOrder));
        }
        } catch (error) {
        console.error('Error loading orders:', error);
        if (error.response?.status === 401) {
            toast.add({
                severity: 'error',
                summary: 'Lỗi',
                detail: 'Phiên đăng nhập hết hạn',
                life: 3000
            });
        }
        orders.value = [];
    } finally {
        isLoadingOrders.value = false;
    }
};

// Load single order summary when in detail mode
const loadOrderSummary = async (orderId) => {
    if (!orderId) return;
    isLoadingOrders.value = true;
    try {
        const response = await axios.get(`${API_BASE_URL}/hoa-don/${orderId}`, {
            headers: {
                Authorization: `Bearer ${getAuthToken()}`,
                'Content-Type': 'application/json'
            }
        });
        const ord = response.data?.data || response.data;
        orders.value = ord ? [ord] : [];
        if (orders.value.length > 0) {
            await loadOrderDetails(orders.value[0].id);
        }
    } catch (error) {
        console.error('Error loading order summary:', error);
        orders.value = [];
    } finally {
        isLoadingOrders.value = false;
    }
};

// Load order details
const loadOrderDetails = async (orderId) => {
    if (orderDetails[orderId]) return; // Đã load rồi

    loadingOrderDetails[orderId] = true;
    try {
        const response = await axios.get(`${API_BASE_URL}/hoa-don/${orderId}/chi-tiet`, {
            headers: {
                Authorization: `Bearer ${getAuthToken()}`,
                'Content-Type': 'application/json'
            }
        });

        orderDetails[orderId] = response.data.data;

        // Load ảnh cho từng sản phẩm trong order
        const products = orderDetails[orderId]?.chiTietSanPham || [];
        for (const product of products) {
            if (product.idSanPham) {
                await loadProductImage(product.idSanPham, product);
            }
        }
    } catch (error) {
        console.error('Error loading order details:', error);
    } finally {
        loadingOrderDetails[orderId] = false;
    }
};

// Hàm mới để load ảnh sản phẩm
const loadProductImage = async (productId, item) => {
    console.log('🖼️ Loading image for product:', productId);
    console.log('📝 Item data:', item);

    const cacheKey = `product_${productId}`;

    if (imageCache.value[cacheKey] || loadingImages.value.has(cacheKey)) {
        return;
    }

    loadingImages.value.add(cacheKey);

    try {
        let imageUrl = null;

        // Phương án 1: Nếu có idHinhAnh, gọi API lấy thông tin hình ảnh
        if (item.idHinhAnh) {
            try {
                console.log(`🔍 Trying image API: ${API_BASE_URL}/hinh-anh/${item.idHinhAnh}`);

                const imageResponse = await axios.get(`${API_BASE_URL}/hinh-anh/${item.idHinhAnh}`, {
                headers: {
                    Authorization: `Bearer ${getAuthToken()}`,
                    'Content-Type': 'application/json'
                }
            });

                console.log('📦 Image API response:', imageResponse.data);

                if (imageResponse.data && imageResponse.data.duongDan) {
                    imageUrl = imageResponse.data.duongDan;
                    console.log('✅ Found image from duongDan:', imageUrl);
                }

            } catch (error) {
                console.log(`❌ Image API failed:`, error.response?.status || error.message);
            }
        }

        // Phương án 2: Nếu có tenHinhAnh, gọi API lấy đường dẫn theo tên
        if (!imageUrl && item.tenHinhAnh) {
            try {
                console.log(`🔍 Trying duong-dan API: ${API_BASE_URL}/hinh-anh/duong-dan/${item.tenHinhAnh}`);

                const pathResponse = await axios.get(`${API_BASE_URL}/hinh-anh/duong-dan/${item.tenHinhAnh}`, {
                    headers: {
                        Authorization: `Bearer ${getAuthToken()}`,
                        'Content-Type': 'application/json'
                    }
                });

                console.log('📦 Path API response:', pathResponse.data);

                if (pathResponse.data) {
                    imageUrl = pathResponse.data; // API trả về URL đầy đủ
                    console.log('✅ Found image from duong-dan API:', imageUrl);
                }

    } catch (error) {
                console.log(`❌ Path API failed:`, error.response?.status || error.message);
            }
        }

        // Phương án 3: Thử các endpoint sản phẩm để lấy thông tin ảnh
        if (!imageUrl) {
            const endpoints = [
                `/api/san-pham/${productId}`,
                `/san-pham/${productId}`,
                `/api/products/${productId}`,
                `/products/${productId}`,
                `/api/san-pham/detail/${productId}`
            ];

            for (const endpoint of endpoints) {
                try {
                    console.log(`🔍 Trying product endpoint: ${API_BASE_URL}${endpoint}`);

                    const productResponse = await axios.get(`${API_BASE_URL}${endpoint}`, {
                        headers: {
                            Authorization: `Bearer ${getAuthToken()}`,
                            'Content-Type': 'application/json'
                        }
                    });

                    console.log('📦 Product API response:', productResponse.data);
                    const productData = productResponse.data;

                    // Thử các field khả thi cho URL ảnh
                    const possibleImageFields = [
                        'hinhAnhChinh', 'anhChinh', 'image', 'imageUrl', 'hinhAnh',
                        'duongDan', 'url', 'anh', 'anhDaiDien', 'imagePath'
                    ];

                    for (const field of possibleImageFields) {
                        if (productData[field]) {
                            imageUrl = productData[field];
                            console.log(`✅ Found image field '${field}':`, imageUrl);
                            break;
                        }
                    }

                    // Nếu có nested object hình ảnh
                    if (!imageUrl && productData.hinhAnh) {
                        imageUrl = productData.hinhAnh.duongDan || productData.hinhAnh.url || productData.hinhAnh.path;
                        console.log('✅ Found nested image:', imageUrl);
                    }

                    // Nếu tìm thấy ảnh thì thoát khỏi loop
                    if (imageUrl) break;

                } catch (error) {
                    console.log(`❌ Endpoint ${endpoint} failed:`, error.response?.status || error.message);
                    continue;
                }
            }
        }

        if (imageUrl) {
            // Chuẩn hóa URL - nếu đã có http thì giữ nguyên, nếu không thì thêm base URL
            const finalImageUrl = imageUrl.startsWith('http')
                ? imageUrl
                : `${API_BASE_URL}${imageUrl.startsWith('/') ? '' : '/'}${imageUrl}`;

            imageCache.value[cacheKey] = finalImageUrl;
            // Trigger reactivity
            imageCache.value = { ...imageCache.value };

            console.log(`✅ Successfully loaded image for product ${productId}:`, finalImageUrl);
        } else {
            console.log(`❌ No image found for product ${productId} after trying all methods`);
        }

    } catch (error) {
        console.error('❌ Critical error loading product image for', productId, ':', error);
    } finally {
        loadingImages.value.delete(cacheKey);
    }
};

// Lấy danh sách hóa đơn hiển thị (3 đầu tiên hoặc tất cả)
const getDisplayedOrders = () => {
    if (showAllOrders.value || orders.value.length <= 3) {
        return orders.value;
    }
    return orders.value.slice(0, 3);
};

// Đếm số hóa đơn còn lại chưa hiển thị
const getRemainingOrdersCount = () => {
    return Math.max(0, orders.value.length - 3);
};

// Toggle hiển thị tất cả hóa đơn
const toggleShowAllOrders = () => {
    showAllOrders.value = !showAllOrders.value;
};

// Lấy danh sách sản phẩm hiển thị (2 sản phẩm đầu hoặc tất cả)
const getDisplayedProducts = (orderId) => {
    const products = orderDetails[orderId]?.chiTietSanPham || [];
    if (showAllProducts[orderId] || products.length <= 2) {
        return products;
    }
    return products.slice(0, 2);
};

// Đếm số sản phẩm còn lại chưa hiển thị
const getRemainingProductsCount = (orderId) => {
    const products = orderDetails[orderId]?.chiTietSanPham || [];
    return Math.max(0, products.length - 2);
};

// Toggle hiển thị tất cả sản phẩm
const toggleShowAllProducts = (orderId) => {
    showAllProducts[orderId] = !showAllProducts[orderId];
};

// ========== SỬA HÀM LẤY ẢNH SẢN PHẨM ==========
const getProductImage = (item) => {
    // Kiểm tra các field ảnh có sẵn từ backend trước
    const directImageFields = [
        'duongDan', 'hinhAnhUrl', 'imageUrl', 'image', 'url',
        'anhChinh', 'hinhAnhChinh', 'path', 'imagePath'
    ];

    // Thử lấy ảnh trực tiếp từ item
    for (const field of directImageFields) {
        if (item[field]) {
            const imageUrl = item[field].startsWith('http')
                ? item[field]
                : `${API_BASE_URL}${item[field].startsWith('/') ? '' : '/'}${item[field]}`;
            console.log(`✅ Found direct image from field '${field}':`, imageUrl);
            return imageUrl;
        }
    }

    // Thử lấy từ nested object
    const nestedPaths = [
        'hinhAnh.duongDan', 'hinhAnh.url', 'hinhAnh.path',
        'chiTietSanPham.hinhAnh.duongDan',
        'image.url', 'image.path'
    ];

    for (const path of nestedPaths) {
        const value = getNestedValue(item, path);
        if (value) {
            const imageUrl = value.startsWith('http')
                ? value
                : `${API_BASE_URL}${value.startsWith('/') ? '' : '/'}${value}`;
            console.log(`✅ Found nested image from path '${path}':`, imageUrl);
            return imageUrl;
        }
    }

    // Nếu có idHinhAnh và tenHinhAnh, dùng cache hoặc load
    const productId = item.idSanPham || item.sanPhamId;
    const cacheKey = `product_${productId}`;

    if (imageCache.value[cacheKey]) {
        return imageCache.value[cacheKey];
    }

    // Trigger load ảnh nếu có thông tin
    if ((item.idHinhAnh || item.tenHinhAnh) && productId) {
        loadProductImage(productId, item);
    }

    // Trả về placeholder nếu chưa có ảnh
    return getPlaceholderImage(item);
};

// Helper function để lấy nested value
const getNestedValue = (obj, path) => {
    return path.split('.').reduce((current, key) => {
        return current && current[key] !== undefined ? current[key] : null;
    }, obj);
};

const getPlaceholderImage = (item) => {
    const productName = item?.tenSanPham || 'Sản phẩm';
    return `data:image/svg+xml;base64,${btoa(`
      <svg width="64" height="64" xmlns="http://www.w3.org/2000/svg">
        <rect width="64" height="64" fill="#f3f4f6"/>
        <text x="32" y="28" text-anchor="middle" dominant-baseline="middle"
              font-family="Arial" font-size="8" fill="#9ca3af">
          ${productName.substring(0, 10)}
        </text>
        <text x="32" y="40" text-anchor="middle" dominant-baseline="middle"
              font-family="Arial" font-size="6" fill="#9ca3af">
          No Image
        </text>
      </svg>
    `)}`;
};

// View order detail - chuyển sang trang chi tiết
const viewOrderDetail = (orderId) => {
    router.push(`/profile/orders/${orderId}`);
};

// Handle image error
const handleImageError = (event) => {
    event.target.src = getPlaceholderImage();
};

// Utility functions
const formatDate = (date) => {
    if (!date && date !== 0) return '';
    // Trường hợp backend trả về mảng ngày [yyyy, MM, dd, HH, mm, ss]
    if (Array.isArray(date) && date.length >= 3) {
        const [y, M, d, hh = 0, mm = 0, ss = 0] = date.map(Number);
        const dt = new Date(y, M - 1, d, hh, mm, ss);
        return isNaN(dt.getTime()) ? '' : dt.toLocaleDateString('vi-VN');
    }
    // Nếu là số hoặc chuỗi số (epoch millis)
    if (typeof date === 'number' || /^\d+$/.test(String(date))) {
        return new Date(Number(date)).toLocaleDateString('vi-VN');
    }
    // Chuẩn ISO hoặc có dấu 'T'
    if (typeof date === 'string' && /T/.test(date)) {
    return new Date(date).toLocaleDateString('vi-VN');
    }
    // Định dạng dd/MM/yyyy hoặc dd-MM-yyyy
    if (typeof date === 'string' && /^(\d{1,2})[/-](\d{1,2})[/-](\d{4})/.test(date)) {
        const match = date.match(/(\d{1,2})[/-](\d{1,2})[/-](\d{4})/);
        const d = Number(match[1]);
        const m = Number(match[2]) - 1;
        const y = Number(match[3]);
        return new Date(y, m, d).toLocaleDateString('vi-VN');
    }
    // Fallback: thay space thành 'T' nếu có giờ
    if (typeof date === 'string' && date.includes(' ')) {
        const tryIso = new Date(date.replace(' ', 'T'));
        if (!isNaN(tryIso.getTime())) return tryIso.toLocaleDateString('vi-VN');
    }
    const dt = new Date(date);
    return isNaN(dt.getTime()) ? '' : dt.toLocaleDateString('vi-VN');
};

const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN').format(amount || 0) + 'đ';
};

// ========== SỬA HÀM TÍNH TỔNG TIỀN THEO OrderDetail.vue ==========
const getTotalAmount = (order) => {
    // Ưu tiên dùng tổng tiền thực tế đã thanh toán từ backend (như OrderDetail.vue)
    const backendTotal = order.tongThanhToan || order.tongTien || order.totalAmount || order.total || order.amount || order.thanhTien || order.soTien;
    if (backendTotal !== undefined && backendTotal !== null) {
        console.log('Using backend total:', backendTotal);
        return backendTotal;
    }

    // Tính toán từ các thành phần (như OrderDetail.vue)
    const subtotal = order.tongTienGoc || order.tongTien || order.tamTinh || order.tongTienHang || 0;
    const discount = order.giamGia || order.giaTriDiem || order.giaTriGiam || order.voucherDiscount || order.voucherValue || order.tongTienGiamGia || order.tongTienVoucher || order.tienDiem || 0;
    const shipping = order.phiVanChuyen || order.phiShip || order.shippingFee || 0;

    // Công thức: Tạm tính - Giảm giá + Phí vận chuyển (như OrderDetail.vue)
    const computedTotal = subtotal - Math.abs(discount) + shipping;

    console.log('=== TOTAL CALCULATION LIKE OrderDetail.vue ===');
    console.log('Subtotal:', subtotal);
    console.log('Discount:', discount);
    console.log('Shipping:', shipping);
    console.log('Computed total:', computedTotal);

    return Math.max(0, computedTotal);
};

const getStatusLabel = (status) => {
    const statusMap = {
        CHO_XAC_NHAN: 'Chờ xác nhận',
        CHO_THANH_TOAN: 'Chờ thanh toán',
        DA_XAC_NHAN: 'Đã xác nhận',
        DANG_GIAO: 'Đang giao',
        DA_GIAO: 'Đã giao',
        HOAN_THANH: 'Hoàn thành',
        DA_HUY: 'Đã hủy'
    };
    return statusMap[status] || status;
};

const getStatusClass = (status) => {
    const classMap = {
        CHO_XAC_NHAN: 'bg-yellow-100 text-yellow-800',
        CHO_THANH_TOAN: 'bg-orange-100 text-orange-800',
        DA_XAC_NHAN: 'bg-blue-100 text-blue-800',
        DANG_GIAO: 'bg-purple-100 text-purple-800',
        DA_GIAO: 'bg-green-100 text-green-800',
        HOAN_THANH: 'bg-green-100 text-green-800',
        DA_HUY: 'bg-red-100 text-red-800'
    };
    return classMap[status] || 'bg-gray-100 text-gray-800';
};

// Lifecycle
onMounted(() => {
    if (isDetailMode.value) {
        loadOrderSummary(route.params.id);
    } else {
        loadOrders();
    }
});
</script>

<template>
    <div class="rounded-lg bg-white p-6 shadow-sm">
        <h4 class="mb-6 text-xl font-semibold text-gray-800">
            {{ isDetailMode ? 'CHI TIẾT ĐƠN HÀNG' : 'HÓA ĐƠN CỦA TÔI' }}
        </h4>

        <div v-if="isLoadingOrders" class="flex justify-center py-12">
            <div class="h-12 w-12 animate-spin rounded-full border-b-2 border-orange-500"></div>
        </div>

        <div v-else-if="orders.length === 0" class="py-12 text-center">
            <i class="pi pi-file-text mb-4 text-6xl text-gray-300"></i>
            <p class="text-gray-500">Bạn chưa có hóa đơn nào</p>
        </div>

        <!-- Detail view -->
        <div v-else-if="isDetailMode && currentOrder" class="space-y-6">
            <div class="overflow-hidden rounded-lg border border-gray-200">
                <!-- Header -->
                <div class="flex items-center justify-between bg-gray-50 px-6 py-4">
                    <div class="flex items-center space-x-4">
                        <span class="text-sm font-semibold text-gray-800">{{ currentOrder.maHoaDon }}</span>
                        <span class="text-sm text-gray-500">{{ formatDate(currentOrder.ngayTao || currentOrder.ngayDat || currentOrder.ngayDatHang || currentOrder.createdAt) }}</span>
                        <span class="inline-flex items-center rounded-full bg-green-100 px-2 py-1 text-xs font-medium text-green-800">
                            <i class="pi pi-globe mr-1"></i>
                            Online
                        </span>
                    </div>
                    <div class="flex items-center space-x-3">
                        <span :class="['rounded-full px-3 py-1 text-sm font-medium', getStatusClass(currentOrder.trangThaiHoaDon)]">
                            {{ getStatusLabel(currentOrder.trangThaiHoaDon) }}
                        </span>
                    </div>
                </div>

                <!-- Products list (all) -->
                <div class="p-6">
                    <div v-if="loadingOrderDetails[currentOrder.id]" class="flex justify-center py-8">
                        <div class="h-8 w-8 animate-spin rounded-full border-b-2 border-orange-500"></div>
                    </div>

                    <div v-else-if="orderDetails[currentOrder.id]" class="mb-6 space-y-4">
                        <div v-for="item in orderDetails[currentOrder.id].chiTietSanPham" :key="item.id" class="flex items-center space-x-4">
                            <div class="h-16 w-16 flex-shrink-0 overflow-hidden rounded-lg bg-gray-200">
                                <img :src="getProductImage(item)" :alt="item.tenSanPham" class="h-full w-full object-cover" @error="handleImageError" />
                            </div>
                            <div class="flex-1">
                                <h5 class="mb-1 font-medium text-gray-900">{{ item.tenSanPham }}</h5>
                            </div>
                            <div class="text-right">
                                <p class="text-black-500 text-sm font-semibold">{{ item.soLuong }} x {{ formatCurrency(item.giaBan) }}</p>
                            </div>
                        </div>

                        <!-- Totals -->
                        <div class="mt-4 border-t border-gray-200 pt-4">
                            <p class="text-sm text-red-500">Thành tiền: {{ formatCurrency(getTotalAmount(currentOrder)) }}</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- List view -->
        <div v-else class="space-y-6">
            <!-- Hiển thị hóa đơn (3 đầu tiên hoặc tất cả) -->
            <div v-for="order in getDisplayedOrders()" :key="order.id" class="overflow-hidden rounded-lg border border-gray-200">
                <!-- Header hóa đơn -->
                <div class="flex items-center justify-between bg-gray-50 px-6 py-4">
                    <div class="flex items-center space-x-4">
                        <span class="text-sm font-semibold text-gray-800">{{ order.maHoaDon }}</span>
                        <span class="text-sm text-gray-500">{{ formatDate(order.ngayTao || order.ngayDat || order.ngayDatHang || order.createdAt) }}</span>
                        <span class="inline-flex items-center rounded-full bg-green-100 px-2 py-1 text-xs font-medium text-green-800">
                            <i class="pi pi-globe mr-1"></i>
                            Online
                            </span>
                    </div>
                    <div class="flex items-center space-x-3">
                        <span :class="['rounded-full px-3 py-1 text-sm font-medium', getStatusClass(order.trangThaiHoaDon)]">
                            {{ getStatusLabel(order.trangThaiHoaDon) }}
                                </span>
                    </div>
                </div>

                <!-- Danh sách sản phẩm -->
                <div class="p-6">
                    <div v-if="loadingOrderDetails[order.id]" class="flex justify-center py-8">
                        <div class="h-8 w-8 animate-spin rounded-full border-b-2 border-orange-500"></div>
                    </div>

                    <div v-else-if="orderDetails[order.id]" class="mb-6 space-y-4">
                        <!-- Hiển thị sản phẩm (2 đầu tiên hoặc tất cả) -->
                        <div v-for="item in getDisplayedProducts(order.id)" :key="item.id" class="flex items-center space-x-4">
                            <!-- Hình ảnh sản phẩm -->
                            <div class="h-16 w-16 flex-shrink-0 overflow-hidden rounded-lg bg-gray-200">
                                <img :src="getProductImage(item)" :alt="item.tenSanPham" class="h-full w-full object-cover" @error="handleImageError" />
                            </div>

                            <!-- Thông tin sản phẩm -->
                            <div class="flex-1">
                                <h5 class="mb-1 font-medium text-gray-900">{{ item.tenSanPham }}</h5>
                                <div class="text-sm text-gray-500">
                                    <span v-if="item.giaGiam" class="mr-2 line-through">{{ formatCurrency(item.giaGoc) }}</span>
                                </div>
                            </div>

                            <!-- Số lượng và giá -->
                            <div class="text-right">
                                <p class="text-black-500 text-sm font-semibold">{{ item.soLuong }} x {{ formatCurrency(item.giaBan) }}</p>
                    </div>
                </div>

                        <!-- Nút "Xem thêm" nếu có nhiều hơn 2 sản phẩm -->
                        <div v-if="getRemainingProductsCount(order.id) > 0 && !showAllProducts[order.id]" class="pt-2 text-center">
                            <button @click="toggleShowAllProducts(order.id)" class="flex items-center justify-center space-x-1 text-sm font-medium text-orange-600 hover:text-orange-700">
                                <span>Xem thêm {{ getRemainingProductsCount(order.id) }} sản phẩm</span>
                                <i class="pi pi-chevron-down"></i>
                            </button>
                        </div>

                        <!-- Nút "Thu gọn" nếu đang hiển thị tất cả -->
                        <div v-if="showAllProducts[order.id] && orderDetails[order.id].chiTietSanPham.length > 2" class="pt-2 text-center">
                            <button @click="toggleShowAllProducts(order.id)" class="flex items-center justify-center space-x-1 text-sm font-medium text-orange-600 hover:text-orange-700">
                                <span>Thu gọn</span>
                                <i class="pi pi-chevron-up"></i>
                            </button>
                    </div>
                </div>

                    <!-- Thành tiền và nút xem chi tiết -->
                    <div class="flex items-center justify-between border-t border-gray-200 pt-4">
                        <div class="text-right">
                            <p class="text-sm text-red-500">Thành tiền: {{ formatCurrency(getTotalAmount(order)) }}</p>
                        </div>

                        <button @click="viewOrderDetail(order.id)" class="rounded-lg bg-orange-600 px-6 py-2 text-white transition hover:bg-orange-700">Xem chi tiết</button>
                    </div>
                </div>
            </div>

            <!-- Nút "Xem thêm hóa đơn" nếu có nhiều hơn 3 hóa đơn -->
            <div v-if="getRemainingOrdersCount() > 0 && !showAllOrders" class="pt-6 text-center">
                <button @click="toggleShowAllOrders" class="mx-auto flex items-center justify-center space-x-2 rounded-lg bg-gray-100 px-6 py-3 text-gray-700 transition hover:bg-gray-200">
                    <span>Xem thêm {{ getRemainingOrdersCount() }} hóa đơn</span>
                    <i class="pi pi-chevron-down"></i>
                </button>
            </div>

            <!-- Nút "Thu gọn" nếu đang hiển thị tất cả hóa đơn -->
            <div v-if="showAllOrders && orders.length > 3" class="pt-6 text-center">
                <button @click="toggleShowAllOrders" class="mx-auto flex items-center justify-center space-x-2 rounded-lg bg-gray-100 px-6 py-3 text-gray-700 transition hover:bg-gray-200">
                    <span>Thu gọn danh sách</span>
                    <i class="pi pi-chevron-up"></i>
                </button>
            </div>
        </div>
    </div>
</template>

<style scoped>
@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}

.animate-spin {
    animation: spin 1s linear infinite;
}

.bg-yellow-100 {
    background-color: #fef3c7;
}
.text-yellow-800 {
    color: #92400e;
}
.bg-orange-100 {
    background-color: #fed7aa;
}
.text-orange-800 {
    color: #9a3412;
}
.bg-blue-100 {
    background-color: #dbeafe;
}
.text-blue-800 {
    color: #1e40af;
}
.bg-purple-100 {
    background-color: #ede9fe;
}
.text-purple-800 {
    color: #5b21b6;
}
.bg-green-100 {
    background-color: #d1fae5;
}
.text-green-800 {
    color: #065f46;
}
.bg-red-100 {
    background-color: #fee2e2;
}
.text-red-800 {
    color: #991b1b;
}
.bg-gray-100 {
    background-color: #f3f4f6;
}
.text-gray-800 {
    color: #1f2937;
}

button {
    transition: all 0.2s ease;
}

.transition {
    transition: all 0.3s ease;
}

/* Thêm style cho nút xem thêm */
button:hover {
    transform: translateY(-1px);
}

.pi-chevron-down,
.pi-chevron-up {
    font-size: 12px;
}
</style>

