<script setup>
import { navLinks } from '@/constants/index.js';
import headerLogo from '@/layout/composables/logo.png';
import axios from 'axios';
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const router = useRouter();
const route = useRoute();
const API_BASE_URL = 'http://localhost:8080';
// State
const cartItemCount = ref(0);
const secondNavOpen = ref(false);
const user = ref(null);
const isUserDropdownOpen = ref(false);
const isScrolled = ref(false);
const searchQuery = ref('');
const isSearchFocused = ref(false);
const searchSuggestions = ref([]);
const showSuggestions = ref(false);
const searchHistory = ref([]);
const isSearching = ref(false);
const searchResults = ref([]);
// Toggle to show/hide search bar in navbar
const showSearchInNav = ref(false);

// Computed
const isLoggedIn = computed(() => !!user.value);
const userName = computed(() => {
    if (!user.value) return '';
    return user.value.ten || user.value.email?.split('@')[0] || 'User';
});

const userInitial = computed(() => {
    return userName.value.charAt(0).toUpperCase();
});

const userRole = computed(() => {
    if (!user.value) return '';
    const roles = {
        ADMIN: 'Quản trị viên',
        NHANVIEN: 'Nhân viên',
        USER: 'Khách hàng'
    };
    return roles[user.value.vaiTro] || user.value.vaiTro;
});

const getPathFromHref = (href) => href.split('#')[0] || '/';
const getHashFromHref = (href) => {
    const hash = href.split('#')[1];
    return hash ? `#${hash}` : '';
};

const isActiveRoute = (href) => {
    return route.path === getPathFromHref(href);
};

// Methods
const navToggler = () => {
    secondNavOpen.value = !secondNavOpen.value;
    document.body.style.overflow = '';
};

const handleResize = () => {
    if (window.innerWidth > 1024) {
        secondNavOpen.value = false;
        document.body.style.overflow = '';
    }
};

const handleScroll = () => {
    isScrolled.value = window.scrollY > 20;
};

const scrollToNavigationTarget = (hash) => {
    if (!hash) {
        window.scrollTo({ top: 0, left: 0, behavior: 'smooth' });
        return;
    }

    window.setTimeout(() => {
        const target = document.querySelector(hash);
        if (!target) return;

        const headerOffset = 96;
        const targetTop = target.getBoundingClientRect().top + window.scrollY - headerOffset;
        window.scrollTo({ top: Math.max(targetTop, 0), left: 0, behavior: 'smooth' });
    }, 0);
};

const handleNavClick = (navLink) => {
    secondNavOpen.value = false;
    document.body.style.overflow = '';

    const targetPath = getPathFromHref(navLink.href);
    const targetHash = getHashFromHref(navLink.href);

    if (route.path === targetPath) {
        scrollToNavigationTarget(targetHash);
    }
};

const loadUserData = () => {
    const userData = localStorage.getItem('user_info');
    if (userData) {
        try {
            user.value = JSON.parse(userData);
        } catch (e) {
            console.error('Error parsing user data:', e);
            localStorage.removeItem('user_info');
            localStorage.removeItem('auth_token');
        }
    }
};

// Hàm đếm số lượng sản phẩm trong giỏ hàng
const updateCartCount = async () => {
    const token = localStorage.getItem('auth_token');
    const userId = localStorage.getItem('user_info');

    if (token && userId) {
        // User is logged in - get count from backend
        try {
            console.log('📦 Fetching cart count from backend...');

            const response = await axios.get(`${API_BASE_URL}/api/gio-hang/current`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            // Response.data là array của CartItemResponse
            if (Array.isArray(response.data)) {
                // Tính tổng số lượng từ backend response
                const totalQuantity = response.data.reduce((total, item) => total + (item.quantity || 0), 0);
                cartItemCount.value = totalQuantity;
                console.log('✅ Cart count from backend:', totalQuantity);
            } else {
                cartItemCount.value = 0;
            }
        } catch (error) {
            console.error('❌ Error fetching cart count:', error);

            // Nếu lỗi 401 (unauthorized), clear token và set count = 0
            if (error.response?.status === 401) {
                localStorage.removeItem('auth_token');
                localStorage.removeItem('user_info');
                cartItemCount.value = 0;
                user.value = null;
            } else {
                // Các lỗi khác, giữ nguyên count hiện tại
                console.log('Keeping current cart count due to error');
            }
        }
    } else {
        // User is not logged in - get count from localStorage (guest cart)
        try {
            const guestCart = localStorage.getItem('guest_cart');
            if (guestCart) {
                const cartItems = JSON.parse(guestCart);
                const totalQuantity = cartItems.reduce((total, item) => total + (item.quantity || 0), 0);
                cartItemCount.value = totalQuantity;
                console.log('✅ Guest cart count from localStorage:', totalQuantity);
            } else {
                cartItemCount.value = 0;
                console.log('📦 No guest cart found');
            }
        } catch (error) {
            console.error('❌ Error reading guest cart:', error);
            cartItemCount.value = 0;
        }
    }
};

const handleCartUpdated = async () => {
    console.log('📦 Cart updated event received');
    await updateCartCount();
};

watch(
    () => user.value,
    async (newUser) => {
        if (newUser) {
            // User vừa đăng nhập, load cart count
            await updateCartCount();
        } else {
            // User vừa đăng xuất, reset cart count
            cartItemCount.value = 0;
        }
    }
);

// Tìm kiếm sản phẩm nâng cao
const searchProducts = async (query, filters = {}) => {
    if (!query.trim()) return [];
    
    try {
        isSearching.value = true;
        console.log('🔍 Searching products with query:', query);
        console.log('🔍 Filters:', filters);
        
        const params = new URLSearchParams({
            q: query.trim(),
            page: 0,
            size: 20,
            ...filters
        });
        
        const response = await axios.get(`${API_BASE_URL}/api/san-pham/search?${params}`, {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            timeout: 10000
        });
        
        console.log('✅ Search results:', response.data);
        return response.data.content || response.data || [];
    } catch (error) {
        console.error('❌ Search error:', error);
        return [];
    } finally {
        isSearching.value = false;
    }
};

// Lấy gợi ý tìm kiếm với hình ảnh và giá cả
const getSearchSuggestions = async (query) => {
    if (!query.trim() || query.length < 2) {
        searchSuggestions.value = [];
        return;
    }
    
    try {
        console.log('🔍 Getting search suggestions for:', query);
        
        // Lấy danh sách sản phẩm
        const [productsResponse, detailsResponse, imagesResponse] = await Promise.all([
            axios.get(`${API_BASE_URL}/api/san-pham`),
            axios.get(`${API_BASE_URL}/api/san-pham-chi-tiet`),
            axios.get(`${API_BASE_URL}/hinh-anh`)
        ]);

        // Tạo map hình ảnh
        const imageMap = new Map();
        imagesResponse.data.forEach(image => {
            imageMap.set(image.id, image.fullUrl || `${API_BASE_URL}${image.duongDan}`);
        });

        // Tạo map chi tiết sản phẩm
        const detailsMap = new Map();
        detailsResponse.data.forEach(detail => {
            detailsMap.set(detail.id, detail);
        });

        // Tìm kiếm sản phẩm phù hợp
        const queryLower = query.toLowerCase();
        const scoredProducts = productsResponse.data
            .map(product => {
                let score = 0;
                
                // Điểm cho tên sản phẩm
                if (product.tenSanPham?.toLowerCase().includes(queryLower)) {
                    score += product.tenSanPham.toLowerCase().startsWith(queryLower) ? 100 : 80;
                }
                
                // Điểm cho thương hiệu
                if (product.thuongHieu?.tenThuongHieu?.toLowerCase().includes(queryLower)) {
                    score += product.thuongHieu.tenThuongHieu.toLowerCase().startsWith(queryLower) ? 60 : 40;
                }
                
                // Điểm cho danh mục
                if (product.danhMuc?.tenDanhMuc?.toLowerCase().includes(queryLower)) {
                    score += product.danhMuc.tenDanhMuc.toLowerCase().startsWith(queryLower) ? 50 : 30;
                }
                
                // Điểm cho chất liệu
                if (product.chatLieu?.tenChatLieu?.toLowerCase().includes(queryLower)) {
                    score += product.chatLieu.tenChatLieu.toLowerCase().startsWith(queryLower) ? 40 : 20;
                }
                
                return { product, score };
            })
            .filter(({ score }) => score > 0)
            .sort((a, b) => b.score - a.score)
            .slice(0, 8)
            .map(({ product, score }) => {
                const detail = detailsMap.get(product.id);
                let finalScore = score;

                // Bonus điểm cho sản phẩm có giá tốt
                if (detail?.giaBan && detail.giaBan < 1000000) finalScore += 10;

                // Bonus điểm cho sản phẩm có hình ảnh
                if (detail?.hinhAnh) finalScore += 5;

                let imageUrl = '';
                if (detail?.hinhAnh) {
                    if (typeof detail.hinhAnh === 'object' && detail.hinhAnh !== null) {
                        if (detail.hinhAnh.id) {
                            imageUrl = imageMap.get(detail.hinhAnh.id);
                        } else if (detail.hinhAnh.fullUrl) {
                            imageUrl = detail.hinhAnh.fullUrl;
                        } else if (detail.hinhAnh.duongDan) {
                            const duongDan = detail.hinhAnh.duongDan;
                            if (duongDan.startsWith('http')) {
                                imageUrl = duongDan;
                            } else if (duongDan.startsWith('/hinh-anh/')) {
                                imageUrl = `${API_BASE_URL}${duongDan}`;
                            } else {
                                imageUrl = `${API_BASE_URL}/hinh-anh/images/${duongDan}`;
                            }
                        }
                    } else if (typeof detail.hinhAnh === 'number') {
                        imageUrl = imageMap.get(detail.hinhAnh);
                    }
                }

                return {
                    id: product.id,
                    name: product.tenSanPham,
                    brand: product.thuongHieu?.tenThuongHieu,
                    category: product.danhMuc?.tenDanhMuc,
                    displayText: product.tenSanPham,
                    price: detail?.giaBan || 0,
                    image: imageUrl,
                    productDetailId: detail?.id || null,
                    score: finalScore
                };
            });

        searchSuggestions.value = scoredProducts;
        console.log('✅ Search suggestions loaded:', scoredProducts.length);
        
    } catch (error) {
        console.error('❌ Suggestions error:', error);
        searchSuggestions.value = [];
    }
};

// Lưu lịch sử tìm kiếm chi tiết
const saveSearchHistory = (query, results = []) => {
    if (!query.trim()) return;
    
    const history = JSON.parse(localStorage.getItem('searchHistory') || '[]');
    
    // Tạo entry mới với thông tin chi tiết
    const newEntry = {
        query: query.trim(),
        timestamp: new Date().toISOString(),
        resultCount: results.length,
        topResults: results.slice(0, 3).map(result => ({
            name: result.tenSanPham || result.name || 'Sản phẩm',
            price: result.giaBan || result.price || 0,
            image: result.hinhAnh || result.image || ''
        }))
    };
    
    // Loại bỏ entry cũ nếu có
    const filteredHistory = history.filter(item => 
        typeof item === 'string' ? item !== query.trim() : item.query !== query.trim()
    );
    
    // Thêm entry mới vào đầu
    const newHistory = [newEntry, ...filteredHistory].slice(0, 10);
    localStorage.setItem('searchHistory', JSON.stringify(newHistory));
    searchHistory.value = newHistory;
};

// Load lịch sử tìm kiếm
const loadSearchHistory = () => {
    const history = JSON.parse(localStorage.getItem('searchHistory') || '[]');
    searchHistory.value = history;
};

// Xử lý tìm kiếm chính
const handleSearch = async (query = null) => {
    const searchTerm = query || searchQuery.value.trim();
    
    if (!searchTerm) {
        showSuggestions.value = false;
        return;
    }
    
    console.log('🔍 Performing search for:', searchTerm);
    
    // Tìm kiếm sản phẩm
    const results = await searchProducts(searchTerm);
    searchResults.value = results;
    
    // Lưu vào lịch sử với kết quả
    saveSearchHistory(searchTerm, results);
    
    // Chuyển đến trang sản phẩm với kết quả tìm kiếm
    router.push({
        path: '/products',
        query: { 
            search: searchTerm,
            results: results.length
        }
    });
    
    // Reset UI
    searchQuery.value = '';
    showSuggestions.value = false;
    isSearchFocused.value = false;
    
    // Hiển thị thông báo kết quả
    if (results.length > 0) {
        console.log(`✅ Found ${results.length} products for "${searchTerm}"`);
    } else {
        console.log(`❌ No products found for "${searchTerm}"`);
    }
};

// Xử lý khi gõ trong ô tìm kiếm
const handleSearchInput = async () => {
    const query = searchQuery.value.trim();
    
    if (query.length >= 2) {
        showSuggestions.value = true;
        await getSearchSuggestions(query);
    } else {
        showSuggestions.value = false;
        searchSuggestions.value = [];
    }
};

// Chọn gợi ý
const selectSuggestion = (suggestion) => {
    if (typeof suggestion === 'string') {
        // Lịch sử tìm kiếm
        searchQuery.value = suggestion;
        showSuggestions.value = false;
        handleSearch(suggestion);
    } else {
        // Gợi ý sản phẩm
        console.log('🎯 Selected product suggestion:', suggestion);
        
        // Lưu vào lịch sử
        saveSearchHistory(suggestion.displayText);
        
        // Chuyển đến trang chi tiết sản phẩm
        if (suggestion.productDetailId) {
            router.push(`/product/${suggestion.productDetailId}`);
        } else {
            // Fallback: tìm kiếm sản phẩm
            handleSearch(suggestion.displayText);
        }
        
        // Reset UI
        searchQuery.value = '';
        showSuggestions.value = false;
        isSearchFocused.value = false;
    }
};

// Xem tất cả sản phẩm
const viewAllProducts = () => {
    router.push('/products');
    searchQuery.value = '';
    showSuggestions.value = false;
    isSearchFocused.value = false;
};

// Format giá tiền
const formatPrice = (price) => {
    if (!price) return '0';
    return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.');
};

// Xử lý lỗi hình ảnh
const handleImageError = (event) => {
    console.log('Image load failed:', event.target.src);
    event.target.style.display = 'none';
};

// Helper functions cho lịch sử tìm kiếm
const getSearchHistoryDetails = (historyItem) => {
    if (typeof historyItem === 'string') {
        return { query: historyItem, resultCount: null, timestamp: null };
    }
    return historyItem;
};

const formatSearchTime = (timestamp) => {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);
    
    if (diffMins < 1) return 'Vừa xong';
    if (diffMins < 60) return `${diffMins} phút trước`;
    if (diffHours < 24) return `${diffHours} giờ trước`;
    if (diffDays < 7) return `${diffDays} ngày trước`;
    return date.toLocaleDateString('vi-VN');
};

// Xóa lịch sử tìm kiếm
const clearSearchHistory = () => {
    localStorage.removeItem('searchHistory');
    searchHistory.value = [];
};

// Tìm kiếm nâng cao với bộ lọc
const advancedSearch = async (filters = {}) => {
    const query = searchQuery.value.trim();
    if (!query) return;
    
    const results = await searchProducts(query, filters);
    searchResults.value = results;
    
    router.push({
        path: '/products',
        query: { 
            search: query,
            ...filters,
            results: results.length
        }
    });
};

const goToLogin = () => {
    router.push('/auth/login');
};

const goToCart = () => {
    router.push('/card');
};

const goToProfile = () => {
    router.push('/profileInfo');
    isUserDropdownOpen.value = false;
};

const goToOrders = () => {
    router.push('/profileOrders');
    isUserDropdownOpen.value = false;
};

const goToDashboard = () => {
    router.push('/dashboard');
    isUserDropdownOpen.value = false;
};

const logout = async () => {
    try {
        isUserDropdownOpen.value = false;

        const token = localStorage.getItem('auth_token');
        if (token) {
            try {
                await fetch('http://localhost:8080/auth/logout', {
                    method: 'POST',
                    headers: {
                        Authorization: `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    },
                    credentials: 'include'
                });
            } catch (apiError) {
                console.log('Logout API call failed:', apiError.message);
            }
        }

        localStorage.removeItem('auth_token');
        localStorage.removeItem('user_info');
        localStorage.removeItem('rememberMe');
        localStorage.removeItem('savedEmail');

        user.value = null;
        cartItemCount.value = 0; // Reset cart count khi logout
        router.push('/');
    } catch (error) {
        console.error('Logout error:', error);
        localStorage.clear();
        user.value = null;
        cartItemCount.value = 0; // Reset cart count
        router.push('/');
    }
};

const toggleUserDropdown = () => {
    isUserDropdownOpen.value = !isUserDropdownOpen.value;
};

const handleClickOutside = (event) => {
    const dropdown = document.querySelector('.user-dropdown');
    const userButton = document.querySelector('.user-button');
    const mobileMenu = document.querySelector('.mobile-menu-panel');
    const mobileMenuButton = document.querySelector('.mobile-menu-button');

    if (dropdown && !dropdown.contains(event.target) && !userButton.contains(event.target)) {
        isUserDropdownOpen.value = false;
    }

    if (secondNavOpen.value && mobileMenu && mobileMenuButton && !mobileMenu.contains(event.target) && !mobileMenuButton.contains(event.target)) {
        secondNavOpen.value = false;
    }
};

// Close mobile menu on route change
watch(
    () => route.path,
    () => {
        secondNavOpen.value = false;
        document.body.style.overflow = '';
    }
);

// Update onMounted
onMounted(async () => {
    handleResize();
    loadUserData();
    loadSearchHistory(); // Load lịch sử tìm kiếm

    // Load cart count từ backend nếu user đã đăng nhập
    if (user.value) {
        await updateCartCount();
    }

    // Lắng nghe sự kiện
    window.addEventListener('resize', handleResize);
    window.addEventListener('scroll', handleScroll);
    window.addEventListener('cartUpdated', handleCartUpdated);
    document.addEventListener('click', handleClickOutside);
});

// Update onUnmounted (remove handleStorageChange)
onUnmounted(() => {
    window.removeEventListener('resize', handleResize);
    window.removeEventListener('scroll', handleScroll);
    window.removeEventListener('cartUpdated', handleCartUpdated);
    document.removeEventListener('click', handleClickOutside);
    document.body.style.overflow = '';
});
</script>

<template>
    <header class="site-header fixed top-0 z-[1200] w-full transition-all duration-300" :class="[isScrolled ? 'bg-white shadow-lg' : 'bg-white/90 backdrop-blur-md', 'border-b border-gray-100']">
        <nav class="container mx-auto px-4 lg:px-8">
            <div class="flex h-20 items-center justify-between">
                <!-- Logo -->
                <router-link to="/" class="relative z-50 flex items-center space-x-2 transition-transform duration-300 hover:scale-105">
                    <img :src="headerLogo" alt="Logo1" class="img-fluid" style="width: 50px" />
                    <span class="site-logo-text">BEE SHOES</span>
                </router-link>

                <!-- Desktop Navigation -->
                <div class="hidden lg:flex lg:flex-1 lg:items-center lg:justify-center">
                    <ul class="site-nav-list flex items-center gap-2 xl:gap-3">
                        <li v-for="navLink in navLinks" :key="navLink.label">
                            <router-link
                                :to="navLink.href"
                                class="nav-link group relative flex h-11 items-center justify-center px-3 text-center text-sm font-medium leading-tight transition-colors xl:text-base"
                                :class="[isActiveRoute(navLink.href) ? 'text-coral-red' : 'text-gray-700 hover:text-coral-red']"
                                @click="handleNavClick(navLink)"
                            >
                                {{ navLink.label }}
                                <span class="absolute bottom-0 left-0 h-0.5 w-0 bg-coral-red transition-all duration-300 group-hover:w-full" :class="{ 'w-full': isActiveRoute(navLink.href) }"></span>
                            </router-link>
                        </li>
                    </ul>
                </div>

                <!-- Right Section -->
                <div class="flex items-center space-x-4">
                    <!-- Search Bar (Desktop) -->
                    <div v-if="showSearchInNav" class="hidden lg:block">
                        <div class="search-container relative transition-all duration-300" :class="isSearchFocused ? 'w-80' : 'w-64'">
                            <input
                                v-model="searchQuery"
                                @input="handleSearchInput"
                                @keyup.enter="handleSearch"
                                @focus="isSearchFocused = true; showSuggestions = true"
                                @blur="setTimeout(() => showSuggestions = false, 200)"
                                type="text"
                                placeholder="Tìm kiếm sản phẩm..."
                                class="w-full rounded-full border border-gray-200 bg-gray-50 px-4 py-2 pr-10 text-sm outline-none transition-all duration-300 focus:border-coral-red focus:bg-white focus:shadow-md"
                            />
                            <button @click="handleSearch" class="absolute right-0 top-0 flex h-full items-center px-3 text-gray-400 transition-colors hover:text-coral-red">
                                <svg v-if="!isSearching" class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                                </svg>
                                <svg v-else class="h-5 w-5 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                                </svg>
                            </button>
                            
                            <!-- Search Suggestions Dropdown -->
                            <Transition name="dropdown">
                                <div v-if="showSuggestions && (searchSuggestions.length > 0 || searchHistory.length > 0)" 
                                     class="absolute top-full left-0 right-0 mt-2 max-h-80 overflow-y-auto rounded-xl bg-white shadow-xl ring-1 ring-black ring-opacity-5 z-50">
                                    
                                    <!-- Search Suggestions -->
                                    <div v-if="searchSuggestions.length > 0" class="p-2">
                                        <div class="px-3 py-2 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                                            Gợi ý tìm kiếm
                                        </div>
                                        <button
                                            v-for="suggestion in searchSuggestions.slice(0, 6)"
                                            :key="suggestion.id"
                                            @click="selectSuggestion(suggestion)"
                                            class="flex w-full items-center space-x-3 rounded-lg px-3 py-3 text-left transition-colors hover:bg-gray-50"
                                        >
                                            <!-- Product Image -->
                                            <div class="flex-shrink-0 w-12 h-12 rounded-lg overflow-hidden bg-gray-100">
                                                <img
                                                    v-if="suggestion.image"
                                                    :src="suggestion.image"
                                                    :alt="suggestion.name"
                                                    class="w-full h-full object-cover"
                                                    @error="handleImageError"
                                                />
                                                <div v-else class="w-full h-full flex items-center justify-center bg-gray-200">
                                                    <svg class="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                                    </svg>
                                                </div>
                                            </div>
                                            
                                            <!-- Product Info -->
                                            <div class="flex-1 min-w-0">
                                                <div class="text-sm font-medium text-gray-900 truncate">{{ suggestion.displayText }}</div>
                                                <div class="flex items-center space-x-2 mt-1">
                                                    <div class="text-xs text-gray-500 truncate" v-if="suggestion.brand">{{ suggestion.brand }}</div>
                                                    <span class="text-xs text-gray-400" v-if="suggestion.brand && suggestion.category">•</span>
                                                    <div class="text-xs text-gray-500 truncate" v-if="suggestion.category">{{ suggestion.category }}</div>
                                                </div>
                                                <div class="text-sm font-semibold text-red-600 mt-1" v-if="suggestion.price">
                                                    {{ formatPrice(suggestion.price) }}₫
                                                </div>
                                            </div>
                                            
                                            <!-- Arrow Icon -->
                                            <svg class="h-4 w-4 text-gray-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                                            </svg>
                                        </button>
                                        
                                        <!-- View All Products Button -->
                                        <div class="px-3 py-2 border-t border-gray-100">
                                            <button
                                                @click="viewAllProducts"
                                                class="flex w-full items-center justify-center space-x-2 rounded-lg px-3 py-2 text-sm font-medium text-coral-red transition-colors hover:bg-red-50"
                                            >
                                                <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                                                </svg>
                                                <span>Xem tất cả sản phẩm</span>
                                            </button>
                                        </div>
                                    </div>
                                    
                                    <!-- Search History -->
                                    <div v-if="searchHistory.length > 0 && searchSuggestions.length === 0" class="p-2">
                                        <div class="flex items-center justify-between px-3 py-2">
                                            <div class="text-xs font-semibold text-gray-500 uppercase tracking-wider">
                                                Lịch sử tìm kiếm
                                            </div>
                                            <button @click="clearSearchHistory" class="text-xs text-gray-400 hover:text-gray-600">
                                                Xóa
                                            </button>
                                        </div>
                                        <button
                                            v-for="historyItem in searchHistory.slice(0, 3)"
                                            :key="typeof historyItem === 'string' ? historyItem : historyItem.query"
                                            @click="selectSuggestion(typeof historyItem === 'string' ? historyItem : historyItem.query)"
                                            class="flex w-full items-center space-x-3 rounded-lg px-3 py-2 text-left transition-colors hover:bg-gray-50"
                                        >
                                            <svg class="h-4 w-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                                            </svg>
                                            <div class="flex-1 min-w-0">
                                                <span class="text-sm text-gray-700 truncate">
                                                    {{ typeof historyItem === 'string' ? historyItem : historyItem.query }}
                                                </span>
                                                <div class="text-xs text-gray-500 mt-1">
                                                    {{ getSearchHistoryDetails(historyItem)?.resultCount ? `${getSearchHistoryDetails(historyItem).resultCount} kết quả` : 'Tìm kiếm gần đây' }}
                                                    <span v-if="getSearchHistoryDetails(historyItem)?.timestamp" class="ml-2">
                                                        • {{ formatSearchTime(getSearchHistoryDetails(historyItem).timestamp) }}
                                                    </span>
                                                </div>
                                            </div>
                                        </button>
                                    </div>
                                    
                                    <!-- No Results -->
                                    <div v-if="searchSuggestions.length === 0 && searchHistory.length === 0 && searchQuery.length >= 2" 
                                         class="p-4 text-center text-sm text-gray-500">
                                        Không tìm thấy gợi ý nào
                                    </div>
                                </div>
                            </Transition>
                        </div>
                    </div>

                    <!-- Cart -->
                    <button @click="goToCart" class="relative flex h-10 w-10 items-center justify-center rounded-full transition-all duration-300 hover:bg-gray-100">
                        <svg class="h-6 w-6 text-gray-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
                        </svg>
                        <span v-if="cartItemCount > 0" class="absolute -right-1 -top-1 flex h-5 w-5 items-center justify-center rounded-full bg-coral-red text-xs font-semibold text-white">
                            {{ cartItemCount }}
                        </span>
                    </button>

                    <!-- User Menu -->
                    <div class="relative">
                        <!-- Not Logged In -->
                        <button v-if="!isLoggedIn" @click="goToLogin" class="flex h-10 items-center space-x-2 rounded-full bg-coral-red px-4 py-2 text-sm font-medium text-white transition-all duration-300 hover:bg-red-600 hover:shadow-lg">
                            <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                            </svg>
                            <span class="hidden sm:inline">Đăng nhập</span>
                        </button>

                        <!-- Logged In -->
                        <div v-else>
                            <button @click="toggleUserDropdown" class="user-button flex h-10 items-center space-x-2 rounded-full border border-gray-200 bg-white px-3 py-2 transition-all duration-300 hover:border-gray-300 hover:shadow-md">
                                <div class="flex h-7 w-7 items-center justify-center rounded-full text-sm font-semibold text-white" :class="[user.vaiTro === 'ADMIN' ? 'bg-purple-500' : user.vaiTro === 'NHANVIEN' ? 'bg-blue-500' : 'bg-green-500']">
                                    {{ userInitial }}
                                </div>
                                <span class="nav-user-name hidden text-sm font-medium text-gray-700 sm:inline">
                                    {{ userName }}
                                </span>
                                <svg class="h-4 w-4 text-gray-400 transition-transform duration-200" :class="{ 'rotate-180': isUserDropdownOpen }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                                </svg>
                            </button>

                            <!-- Dropdown Menu -->
                            <Transition name="dropdown">
                                <div v-if="isUserDropdownOpen" class="user-dropdown absolute right-0 mt-2 w-72 overflow-hidden rounded-xl bg-white shadow-xl ring-1 ring-black ring-opacity-5">
                                    <!-- User Info Header -->
                                    <div class="bg-gradient-to-r from-gray-50 to-gray-100 px-4 py-4">
                                        <div class="flex items-center space-x-3">
                                            <div
                                                class="flex h-12 w-12 items-center justify-center rounded-full text-lg font-bold text-white shadow-md"
                                                :class="[user.vaiTro === 'ADMIN' ? 'bg-purple-500' : user.vaiTro === 'NHANVIEN' ? 'bg-blue-500' : 'bg-green-500']"
                                            >
                                                {{ userInitial }}
                                            </div>
                                            <div class="flex-1">
                                                <div class="font-semibold text-gray-900">{{ userName }}</div>
                                                <div class="text-sm text-gray-600">{{ user.email }}</div>
                                                <div class="mt-1">
                                                    <span
                                                        class="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium"
                                                        :class="[user.vaiTro === 'ADMIN' ? 'bg-purple-100 text-purple-700' : user.vaiTro === 'NHANVIEN' ? 'bg-blue-100 text-blue-700' : 'bg-green-100 text-green-700']"
                                                    >
                                                        {{ userRole }}
                                                    </span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Menu Items -->
                                    <div class="py-2">
                                        <button @click="goToProfile" class="flex w-full items-center space-x-3 px-4 py-2.5 text-left transition-colors hover:bg-gray-50">
                                            <div class="flex h-9 w-9 items-center justify-center rounded-lg bg-gray-100">
                                                <svg class="h-5 w-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                                                </svg>
                                            </div>
                                            <div>
                                                <div class="text-sm font-medium text-gray-900">Thông tin cá nhân</div>
                                                <div class="text-xs text-gray-500">Quản lý tài khoản của bạn</div>
                                            </div>
                                        </button>

                                        <button @click="goToOrders" class="flex w-full items-center space-x-3 px-4 py-2.5 text-left transition-colors hover:bg-gray-50">
                                            <div class="flex h-9 w-9 items-center justify-center rounded-lg bg-gray-100">
                                                <svg class="h-5 w-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path
                                                        stroke-linecap="round"
                                                        stroke-linejoin="round"
                                                        stroke-width="2"
                                                        d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
                                                    />
                                                </svg>
                                            </div>
                                            <div>
                                                <div class="text-sm font-medium text-gray-900">Đơn hàng của tôi</div>
                                                <div class="text-xs text-gray-500">Theo dõi và quản lý đơn hàng</div>
                                            </div>
                                        </button>

                                        <!-- Admin/Staff Dashboard -->
                                        <button v-if="user.vaiTro === 'ADMIN' || user.vaiTro === 'NHANVIEN'" @click="goToDashboard" class="flex w-full items-center space-x-3 px-4 py-2.5 text-left transition-colors hover:bg-gray-50">
                                            <div class="flex h-9 w-9 items-center justify-center rounded-lg bg-purple-100">
                                                <svg class="h-5 w-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path
                                                        stroke-linecap="round"
                                                        stroke-linejoin="round"
                                                        stroke-width="2"
                                                        d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
                                                    />
                                                </svg>
                                            </div>
                                            <div>
                                                <div class="text-sm font-medium text-gray-900">Trang quản trị</div>
                                                <div class="text-xs text-gray-500">Quản lý hệ thống</div>
                                            </div>
                                        </button>

                                        <div class="my-2 border-t border-gray-100"></div>

                                        <!-- Logout -->
                                        <button @click="logout" class="flex w-full items-center space-x-3 px-4 py-2.5 text-left transition-colors hover:bg-red-50">
                                            <div class="flex h-9 w-9 items-center justify-center rounded-lg bg-red-100">
                                                <svg class="h-5 w-5 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                                                </svg>
                                            </div>
                                            <div>
                                                <div class="text-sm font-medium text-red-600">Đăng xuất</div>
                                                <div class="text-xs text-red-500">Thoát khỏi tài khoản</div>
                                            </div>
                                        </button>
                                    </div>
                                </div>
                            </Transition>
                        </div>
                    </div>

                    <!-- Mobile Menu Button -->
                    <button @click="navToggler" class="mobile-menu-button relative z-50 flex h-10 w-10 flex-col items-center justify-center rounded-lg transition-colors hover:bg-gray-100 lg:hidden">
                        <span class="absolute h-0.5 w-5 transform bg-gray-700 transition-all duration-300" :class="[secondNavOpen ? 'rotate-45' : '-translate-y-1.5']"></span>
                        <span class="absolute h-0.5 w-5 bg-gray-700 transition-all duration-300" :class="[secondNavOpen ? 'opacity-0' : 'opacity-100']"></span>
                        <span class="absolute h-0.5 w-5 transform bg-gray-700 transition-all duration-300" :class="[secondNavOpen ? '-rotate-45' : 'translate-y-1.5']"></span>
                    </button>
                </div>
            </div>
        </nav>

        <!-- Mobile Navigation -->
        <Transition name="mobile-menu">
            <div v-if="secondNavOpen" class="mobile-menu-panel lg:hidden">
                <div class="mobile-menu-content">
                    <!-- Mobile Search -->
                    <div class="mb-4">
                        <div class="relative">
                            <input
                                v-model="searchQuery"
                                @input="handleSearchInput"
                                @keyup.enter="handleSearch"
                                @focus="showSuggestions = true"
                                @blur="setTimeout(() => showSuggestions = false, 200)"
                                type="text"
                                placeholder="Tìm kiếm sản phẩm..."
                                class="w-full rounded-lg border border-gray-200 bg-gray-50 px-4 py-3 pr-10 outline-none transition-all focus:border-coral-red focus:bg-white"
                            />
                            <button @click="handleSearch" class="absolute right-0 top-0 flex h-full items-center px-4 text-gray-400">
                                <svg v-if="!isSearching" class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                                </svg>
                                <svg v-else class="h-5 w-5 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                                </svg>
                            </button>
                            
                            <!-- Mobile Search Suggestions -->
                            <Transition name="dropdown">
                                <div v-if="showSuggestions && (searchSuggestions.length > 0 || searchHistory.length > 0)" 
                                     class="absolute top-full left-0 right-0 mt-2 max-h-60 overflow-y-auto rounded-lg bg-white shadow-xl ring-1 ring-black ring-opacity-5 z-50">
                                    
                                    <!-- Search Suggestions -->
                                    <div v-if="searchSuggestions.length > 0" class="p-2">
                                        <div class="px-3 py-2 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                                            Gợi ý tìm kiếm
                                        </div>
                                        <button
                                            v-for="suggestion in searchSuggestions.slice(0, 4)"
                                            :key="suggestion.id"
                                            @click="selectSuggestion(suggestion)"
                                            class="flex w-full items-center space-x-3 rounded-lg px-3 py-3 text-left transition-colors hover:bg-gray-50"
                                        >
                                            <!-- Product Image -->
                                            <div class="flex-shrink-0 w-10 h-10 rounded-lg overflow-hidden bg-gray-100">
                                                <img
                                                    v-if="suggestion.image"
                                                    :src="suggestion.image"
                                                    :alt="suggestion.name"
                                                    class="w-full h-full object-cover"
                                                    @error="handleImageError"
                                                />
                                                <div v-else class="w-full h-full flex items-center justify-center bg-gray-200">
                                                    <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                                    </svg>
                                                </div>
                                            </div>
                                            
                                            <!-- Product Info -->
                                            <div class="flex-1 min-w-0">
                                                <div class="text-sm font-medium text-gray-900 truncate">{{ suggestion.displayText }}</div>
                                                <div class="flex items-center space-x-2 mt-1">
                                                    <div class="text-xs text-gray-500 truncate" v-if="suggestion.brand">{{ suggestion.brand }}</div>
                                                    <span class="text-xs text-gray-400" v-if="suggestion.brand && suggestion.category">•</span>
                                                    <div class="text-xs text-gray-500 truncate" v-if="suggestion.category">{{ suggestion.category }}</div>
                                                </div>
                                                <div class="text-sm font-semibold text-red-600 mt-1" v-if="suggestion.price">
                                                    {{ formatPrice(suggestion.price) }}₫
                                                </div>
                                            </div>
                                            
                                            <!-- Arrow Icon -->
                                            <svg class="h-4 w-4 text-gray-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                                            </svg>
                                        </button>
                                        
                                        <!-- View All Products Button -->
                                        <div class="px-3 py-2 border-t border-gray-100">
                                            <button
                                                @click="viewAllProducts"
                                                class="flex w-full items-center justify-center space-x-2 rounded-lg px-3 py-2 text-sm font-medium text-coral-red transition-colors hover:bg-red-50"
                                            >
                                                <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                                                </svg>
                                                <span>Xem tất cả sản phẩm</span>
                                            </button>
                                        </div>
                                    </div>
                                    
                                    <!-- Search History -->
                                    <div v-if="searchHistory.length > 0 && searchSuggestions.length === 0" class="p-2">
                                        <div class="flex items-center justify-between px-3 py-2">
                                            <div class="text-xs font-semibold text-gray-500 uppercase tracking-wider">
                                                Lịch sử tìm kiếm
                                            </div>
                                            <button @click="clearSearchHistory" class="text-xs text-gray-400 hover:text-gray-600">
                                                Xóa
                                            </button>
                                        </div>
                                        <button
                                            v-for="historyItem in searchHistory.slice(0, 3)"
                                            :key="typeof historyItem === 'string' ? historyItem : historyItem.query"
                                            @click="selectSuggestion(typeof historyItem === 'string' ? historyItem : historyItem.query)"
                                            class="flex w-full items-center space-x-3 rounded-lg px-3 py-2 text-left transition-colors hover:bg-gray-50"
                                        >
                                            <svg class="h-4 w-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                                            </svg>
                                            <div class="flex-1 min-w-0">
                                                <span class="text-sm text-gray-700 truncate">
                                                    {{ typeof historyItem === 'string' ? historyItem : historyItem.query }}
                                                </span>
                                                <div class="text-xs text-gray-500 mt-1">
                                                    {{ getSearchHistoryDetails(historyItem)?.resultCount ? `${getSearchHistoryDetails(historyItem).resultCount} kết quả` : 'Tìm kiếm gần đây' }}
                                                    <span v-if="getSearchHistoryDetails(historyItem)?.timestamp" class="ml-2">
                                                        • {{ formatSearchTime(getSearchHistoryDetails(historyItem).timestamp) }}
                                                    </span>
                                                </div>
                                            </div>
                                        </button>
                                    </div>
                                </div>
                            </Transition>
                        </div>
                    </div>

                    <!-- Mobile Nav Links -->
                    <nav class="space-y-1">
                        <router-link
                            v-for="navLink in navLinks"
                            :key="navLink.label"
                            :to="navLink.href"
                            class="mobile-nav-link flex min-h-12 items-center rounded-lg px-4 py-3 text-base font-medium transition-colors"
                            :class="[isActiveRoute(navLink.href) ? 'bg-coral-red text-white' : 'text-gray-700 hover:bg-gray-100']"
                            @click="handleNavClick(navLink)"
                        >
                            {{ navLink.label }}
                        </router-link>
                    </nav>

                    <!-- Mobile User Menu (if logged in) -->
                    <div v-if="isLoggedIn" class="mt-8 border-t border-gray-200 pt-8">
                        <div class="space-y-1">
                            <router-link to="/profile" class="block rounded-lg px-4 py-3 text-base font-medium text-gray-700 transition-colors hover:bg-gray-100"> Thông tin cá nhân </router-link>
                            <router-link to="/orders" class="block rounded-lg px-4 py-3 text-base font-medium text-gray-700 transition-colors hover:bg-gray-100"> Đơn hàng của tôi </router-link>
                            <router-link v-if="user.vaiTro === 'ADMIN' || user.vaiTro === 'NHANVIEN'" to="/dashboard" class="block rounded-lg px-4 py-3 text-base font-medium text-gray-700 transition-colors hover:bg-gray-100">
                                Trang quản trị
                            </router-link>
                            <button @click="logout" class="block w-full rounded-lg px-4 py-3 text-left text-base font-medium text-red-600 transition-colors hover:bg-red-50">Đăng xuất</button>
                        </div>
                    </div>
                </div>
            </div>
        </Transition>
    </header>

    <!-- Spacer for fixed header -->
    <div class="h-20"></div>
</template>

<style scoped>
/* Transition for dropdown */
.dropdown-enter-active,
.dropdown-leave-active {
    transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
    opacity: 0;
    transform: translateY(-10px);
}

/* Transition for mobile menu */
.mobile-menu-enter-active,
.mobile-menu-leave-active {
    transition: opacity 0.2s ease, transform 0.2s ease;
}

.mobile-menu-enter-from,
.mobile-menu-leave-to {
    opacity: 0;
    transform: translateY(-0.75rem);
}

/* Smooth underline animation */
.site-header {
    font-family: 'Montserrat', Arial, sans-serif;
}

.mobile-menu-panel {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    z-index: 1201;
    padding: 0 0.75rem 0.75rem;
    background: transparent;
}

.mobile-menu-content {
    max-height: min(28rem, calc(100vh - 5.75rem));
    overflow-y: auto;
    border: 1px solid #e5e7eb;
    border-top: 0;
    border-radius: 0 0 1rem 1rem;
    background: #ffffff;
    padding: 1rem;
    box-shadow: 0 18px 40px rgba(15, 23, 42, 0.16);
}

.site-logo-text {
    font-size: 1rem;
    font-weight: 500;
    line-height: 1.25rem;
    letter-spacing: 0;
    color: #1f2937;
    white-space: nowrap;
}

.site-nav-list {
    min-width: 0;
}

.nav-link {
    width: clamp(7rem, 8.5vw, 9rem);
    letter-spacing: 0;
    white-space: normal;
}

.nav-link span {
    transform-origin: left;
}

.nav-user-name {
    max-width: 7rem;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    line-height: 1.25rem;
}

@media (min-width: 1280px) {
    .nav-link {
        width: 8.75rem;
    }
}

/* Custom scrollbar for mobile menu */
@media (max-width: 1023px) {
    .mobile-menu-content::-webkit-scrollbar {
        width: 4px;
    }

    .mobile-menu-content::-webkit-scrollbar-track {
        background: #f3f4f6;
    }

    .mobile-menu-content::-webkit-scrollbar-thumb {
        background: #d1d5db;
        border-radius: 2px;
    }
}

/* Coral red color */
.text-coral-red {
    color: #ff6452;
}

.bg-coral-red {
    background-color: #ff6452;
}

.border-coral-red {
    border-color: #ff6452;
}

.focus\:border-coral-red:focus {
    border-color: #ff6452;
}
</style>
