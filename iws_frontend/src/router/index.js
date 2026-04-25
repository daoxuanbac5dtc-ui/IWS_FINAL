import AppLayout from '@/layout/AppLayout.vue';

import { createRouter, createWebHistory } from 'vue-router';
const router = createRouter({
    history: createWebHistory(),
    routes: [
        // ROUTE USER - Trang chủ người dùng (KHÔNG CẦN ĐĂNG NHẬP)
        {
            path: '/',
            name: 'user-home',
            component: () => import('@/views/user/UserHome.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/products',
            name: 'products',
            component: () => import('@/views/user/product/ProductList.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/card',
            name: 'card',
            component: () => import('@/views/user/card/Card.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/checkout',
            name: 'checkout',
            component: () => import('@/views/user/card/ThanhToan.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/order-success/:orderId',
            name: 'order-success',
            component: () => import('@/views/user/card/OrderSuccess.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/track-order',
            name: 'track-order',
            component: () => import('@/views/user/card/OrderTrackingView.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/product/:id',
            name: 'product',
            component: () => import('@/views/user/product/Product.vue'),
            meta: { requiresAuth: false }
        },
        
        {
            path: '/payment-return',
            name: 'PaymentReturn',
            component: () => import('@/views/user/card/PaymentReturn.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/profile',
            component: () => import('@/layout/ProfileLayout.vue'), // Sử dụng ProfileLayout làm layout chính
            redirect: '/profileInfo', // Redirect to your existing route
            children: [
                {
                    path: '/profileInfo', // Route: /profileInfo (absolute path)
                    name: 'profileInfo',
                    component: () => import('@/components/user/profile/ProfileInfo.vue'),
                    meta: { requiresAuth: false }
                },
                {
                    path: '/profile/orders/:id',
                    name: 'OrderDetail',
                    component: () => import('@/views/user/profile/OrderDetailPage.vue'),
                    meta: { requiresAuth: false }
                },
                {
                    path: '/profileOrders', // Route: /profileOrders
                    name: 'ProfileOrders',
                    component: () => import('@/components/user/profile/ProfileOrders .vue'),
                    meta: { requiresAuth: false }
                },
                {
                    path: '/returnGoods', // Route: /profileOrders
                    name: 'ReturnGoods',
                    component: () => import('@/components/user/profile/ReturnGoods.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: '/profileAddresses', // Route: /profileAddresses
                    name: 'ProfileAddresses',
                    component: () => import('@/components/user/profile/ProfileAddresses .vue'),
                    meta: { requiresAuth: false }
                }
            ]
        },

        {
            path: '/gioithieu',
            name: 'gioi-thieu',
            component: () => import('@/views/user/gioithieu/Gioithieu.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/lienhe',
            name: 'lien-he',
            component: () => import('@/views/user/lienhe/LienHe.vue'),
            meta: { requiresAuth: false }
        },

        // ADMIN ROUTES - CẦN ĐĂNG NHẬP VÀ LÀ ADMIN/NHÂN VIÊN
        {
            path: '/adminNhanVien',
            component: AppLayout,
            meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] },
            children: [
                {
                    path: '/dashboard',
                    name: 'dashboard',
                    component: () => import('@/views/Dashboard.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    path: '/uikit/formlayout',
                    name: 'formlayout',
                    component: () => import('@/views/uikit/FormLayout.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    path: '/uikit/input',
                    name: 'input',
                    component: () => import('@/views/uikit/InputDoc.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    path: '/uikit/button',
                    name: 'button',
                    component: () => import('@/views/uikit/ButtonDoc.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    path: '/uikit/table',
                    name: 'table',
                    component: () => import('@/views/uikit/TableDoc.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    path: '/uikit/list',
                    name: 'list',
                    component: () => import('@/views/uikit/ListDoc.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    path: '/uikit/tree',
                    name: 'tree',
                    component: () => import('@/views/uikit/TreeDoc.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    path: '/uikit/panel',
                    name: 'panel',
                    component: () => import('@/views/uikit/PanelsDoc.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    path: '/uikit/overlay',
                    name: 'overlay',
                    component: () => import('@/views/uikit/OverlayDoc.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    path: '/uikit/media',
                    name: 'media',
                    component: () => import('@/views/uikit/MediaDoc.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    path: '/uikit/message',
                    name: 'message',
                    component: () => import('@/views/uikit/MessagesDoc.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    path: '/uikit/file',
                    name: 'file',
                    component: () => import('@/views/uikit/FileDoc.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    path: '/uikit/menu',
                    name: 'menu',
                    component: () => import('@/views/uikit/MenuDoc.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    path: '/uikit/charts',
                    name: 'charts',
                    component: () => import('@/views/uikit/ChartDoc.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    path: '/uikit/misc',
                    name: 'misc',
                    component: () => import('@/views/uikit/MiscDoc.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    path: '/uikit/timeline',
                    name: 'timeline',
                    component: () => import('@/views/uikit/TimelineDoc.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    path: '/pages/empty',
                    name: 'empty',
                    component: () => import('@/views/pages/Empty.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    path: '/pages/crud',
                    name: 'crud',
                    component: () => import('@/views/pages/Crud.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    path: '/documentation',
                    name: 'documentation',
                    component: () => import('@/views/pages/Documentation.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    name: 'SanPham',
                    path: '/san-pham',
                    component: () => import('@/views/sanpham/SanPhamList.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    name: 'HoaDon',
                    path: '/hoa-don',
                    component: () => import('@/views/HoaDon/ViewHoaDon.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    name: 'NhanVien',
                    path: '/nhan-vien',
                    component: () => import('@/views/TaiKhoan/NhanVien/NhanVienView.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN'] }
                },
                {
                    name: 'KhachHang',
                    path: '/khach-hang',
                    component: () => import('@/views/TaiKhoan/KhachHang/KhachHangView.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    name: 'ChatLieu',
                    path: '/chat-lieu',
                    component: () => import('@/views/ThuocTinh/ChatLieu/ChatLieuView.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    name: 'DanhMuc',
                    path: '/danh-muc',
                    component: () => import('@/views/ThuocTinh/DanhMuc/DanhMucView.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    name: 'DeGiay',
                    path: '/de-giay',
                    component: () => import('@/views/ThuocTinh/DeGiay/DeGiayView.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    name: 'ThuongHieu',
                    path: '/thuong-hieu',
                    component: () => import('@/views/ThuocTinh/ThuongHieu/ThuongHieuView.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    name: 'MauSac',
                    path: '/mau-sac',
                    component: () => import('@/views/ThuocTinh/MauSac/MauSacView.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    name: 'KichCo',
                    path: '/kich-co',
                    component: () => import('@/views/ThuocTinh/KichCo/KichCoView.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    name: 'Voucher',
                    path: '/voucher',
                    component: () => import('@/views/Voucher/VoucherList.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    name: 'KhuyenMai',
                    path: '/khuyen-mai',
                    component: () => import('@/views/KhuyenMai/KhuyenMaiList.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    name: 'HinhAnh',
                    path: '/hinh-anh',
                    component: () => import('@/views/HinhAnh/HinhAnhView.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN', 'NHANVIEN'] }
                },
                {
                    name: 'TaiKhoan',
                    path: '/tai-khoan',
                    component: () => import('@/views/TaiKhoan/TaiKhoanManagement.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN'] }
                },
                {
                    name: 'BanHang',
                    path: '/ban-hang',
                    component: () => import('@/views/BanHang/BanHangView.vue')
                },
                {
                    name: 'TraHang',
                    path: '/tra-hang',
                    component: () => import('@/views/TraHang/traHang.vue')
                }
            ]
        },

        // CÁC ROUTE KHÁC - Giữ nguyên
        {
            path: '/landing',
            name: 'landing',
            component: () => import('@/views/pages/Landing.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/pages/notfound',
            name: 'notfound',
            component: () => import('@/views/pages/NotFound.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/auth/forgot-password',
            name: 'forgot-password',
            component: () => import('@/views/pages/auth/ForgotPassword.vue'),
            meta: { requiresAuth: false }
        },
        // AUTH ROUTES
        {
            path: '/auth/login',
            name: 'login',
            component: () => import('@/views/pages/auth/Login.vue'),
            meta: {
                requiresAuth: false,
                requiresGuest: true
            }
        },
        {
            path: '/auth/access',
            name: 'accessDenied',
            component: () => import('@/views/pages/auth/Access.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/auth/error',
            name: 'error',
            component: () => import('@/views/pages/auth/Error.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/auth/register',
            name: 'register',
            component: () => import('@/views/pages/auth/Register.vue'),
            meta: { requiresAuth: false }
        },

        // Redirect
        {
            path: '/admin',
            redirect: '/dashboard'
        },

        // Catch all
        {
            path: '/:pathMatch(.*)*',
            redirect: '/pages/notfound'
        }
    ]
});

// ROUTE GUARDS - Kiểm tra quyền truy cập
router.beforeEach(async (to, from, next) => {
    console.log('=== ROUTER GUARD DEBUG ===');
    console.log('Navigating to:', to.path);
    console.log('Route meta:', to.meta);

    // Lấy thông tin user từ localStorage
    const token = localStorage.getItem('auth_token');
    const userInfo = localStorage.getItem('user_info');
    let user = null;

    console.log('Token exists:', !!token);
    console.log('User info exists:', !!userInfo);

    if (userInfo) {
        try {
            user = JSON.parse(userInfo);
            console.log('User parsed:', user);
        } catch (e) {
            console.error('Failed to parse user info:', e);
            localStorage.removeItem('user_info');
            localStorage.removeItem('auth_token');
        }
    }

    // Kiểm tra requiresGuest (chỉ cho phép user chưa đăng nhập)
    if (to.meta.requiresGuest && token && user) {
        console.log('RequiresGuest but user logged in, redirecting...');
        // User đã đăng nhập, chuyển hướng về trang chủ theo role
        if (['ADMIN', 'NHANVIEN'].includes(user.vaiTro)) {
            return next('/dashboard');
        } else {
            return next('/');
        }
    }

    // Kiểm tra requiresAuth
    if (to.meta.requiresAuth) {
        console.log('Route requires auth');
        if (!token || !user) {
            console.log('No token or user, redirecting to login');
            // Chưa đăng nhập -> chuyển đến login
            return next('/auth/login');
        }

        console.log('User authenticated, checking roles...');

        // Kiểm tra roles
        if (to.meta.roles && to.meta.roles.length > 0) {
            console.log('Required roles:', to.meta.roles);
            console.log('User role:', user.vaiTro);

            if (!to.meta.roles.includes(user.vaiTro)) {
                console.log('User role not allowed, redirecting to access denied');
                // Không có quyền truy cập
                return next('/auth/access');
            }
        }
    }

    console.log('Navigation allowed');
    // Cho phép navigation
    next();
});

// After navigation
router.afterEach((to, from) => {
    // Update page title
    const baseTitle = 'Sneaker Store Admin';
    document.title = to.meta.title ? `${to.meta.title} - ${baseTitle}` : baseTitle;
});

// Utility functions để sử dụng trong components
export const useAuthUtils = () => {
    const getUser = () => {
        const userInfo = localStorage.getItem('user_info');
        if (userInfo) {
            try {
                return JSON.parse(userInfo);
            } catch (e) {
                return null;
            }
        }
        return null;
    };

    const getToken = () => {
        return localStorage.getItem('auth_token');
    };

    const isAuthenticated = () => {
        return !!(getToken() && getUser());
    };

    const isAdmin = () => {
        const user = getUser();
        return user?.vaiTro === 'ADMIN';
    };

    const isNhanVien = () => {
        const user = getUser();
        return user?.vaiTro === 'NHANVIEN';
    };

    const isUser = () => {
        const user = getUser();
        return user?.vaiTro === 'USER';
    };

    const canAccess = (roles) => {
        const user = getUser();
        if (!user) return false;
        if (!roles || roles.length === 0) return true;
        return roles.includes(user.vaiTro);
    };

    const logout = () => {
        localStorage.removeItem('auth_token');
        localStorage.removeItem('user_info');
        localStorage.removeItem('rememberMe');
        localStorage.removeItem('savedEmail');

        // Gọi API logout
        fetch('/api/auth/logout', {
            method: 'POST',
            headers: {
                Authorization: `Bearer ${getToken()}`,
                'Content-Type': 'application/json'
            }
        }).catch((e) => console.warn('Logout API call failed:', e));

        // Chuyển hướng về login
        router.push('/auth/login');
    };

    return {
        getUser,
        getToken,
        isAuthenticated,
        isAdmin,
        isNhanVien,
        isUser,
        canAccess,
        logout
    };
};

export default router;
