// src/plugins/axios.js
import axios from 'axios';

// Set base URL
axios.defaults.baseURL = 'http://localhost:8080';

// Request interceptor - tự động thêm token vào header
axios.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('auth_token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor - xử lý lỗi 401
axios.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        if (error.response?.status === 401) {
            // Token hết hạn hoặc không hợp lệ
            localStorage.removeItem('auth_token');
            localStorage.removeItem('user_info');

            // Chỉ redirect về login nếu không phải trang chủ hoặc trang không cần đăng nhập
            const currentPath = window.location.pathname;
            const publicPaths = ['/', '/products', '/product/', '/card', '/checkout', '/order-success/', '/payment-return', '/gioithieu', '/lienhe', '/profile', '/profileInfo', '/profileOrders', '/profileAddresses'];

            const isPublicPath = publicPaths.some(path => {
                if (path.endsWith('/')) {
                    return currentPath.startsWith(path);
                }
                return currentPath === path;
            });

            if (!isPublicPath) {
                // Redirect về login
                window.location.href = '/auth/login';
            }
        }
        return Promise.reject(error);
    }
);

export default axios;
