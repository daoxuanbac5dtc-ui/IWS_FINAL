
// composables/useAuth.js
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';

export const useAuth = () => {
    const router = useRouter();
    const user = ref(null);
    const token = ref(null);
    const isLoading = ref(false);

    // Load user from localStorage
    const loadUser = () => {
        const savedToken = localStorage.getItem('auth_token');
        const savedUser = localStorage.getItem('user_info');

        if (savedToken && savedUser) {
            try {
                token.value = savedToken;
                user.value = JSON.parse(savedUser);
            } catch (e) {
                console.error('Failed to parse user info:', e);
                clearAuth();
            }
        }
    };

    // Clear authentication data
    const clearAuth = () => {
        user.value = null;
        token.value = null;
        localStorage.removeItem('auth_token');
        localStorage.removeItem('user_info');
        localStorage.removeItem('rememberMe');
        localStorage.removeItem('savedEmail');
    };

    // Computed properties
    const isAuthenticated = computed(() => {
        return !!(token.value && user.value);
    });

    const isAdmin = computed(() => {
        return user.value?.vaiTro === 'ADMIN';
    });

    const isNhanVien = computed(() => {
        return user.value?.vaiTro === 'NHANVIEN';
    });

    const isUser = computed(() => {
        return user.value?.vaiTro === 'USER';
    });

    const canAccessAdmin = computed(() => {
        return isAdmin.value || isNhanVien.value;
    });

    // Check if user has specific role
    const hasRole = (role) => {
        return user.value?.vaiTro === role;
    };

    // Check if user has any of the specified roles
    const hasAnyRole = (roles) => {
        if (!user.value) return false;
        return roles.includes(user.value.vaiTro);
    };

    // Verify token with server
    const verifyToken = async () => {
        if (!token.value) return false;

        try {
            const response = await fetch('/api/auth/verify', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token.value}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const data = await response.json();
                if (data.success) {
                    user.value = data.data;
                    return true;
                }
            }

            clearAuth();
            return false;
        } catch (error) {
            console.error('Token verification failed:', error);
            return false;
        }
    };

    // Login function
    const login = async (email, password) => {
        isLoading.value = true;

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    email,
                    matKhau: password
                })
            });

            const data = await response.json();

            if (response.ok && data.success) {
                token.value = data.data.token;
                user.value = data.data.user;

                localStorage.setItem('auth_token', data.data.token);
                localStorage.setItem('user_info', JSON.stringify(data.data.user));

                return { success: true, message: data.message };
            } else {
                return { success: false, message: data.message || 'Đăng nhập thất bại' };
            }
        } catch (error) {
            console.error('Login error:', error);
            return { success: false, message: 'Có lỗi xảy ra. Vui lòng thử lại.' };
        } finally {
            isLoading.value = false;
        }
    };

    // Logout function
    const logout = async () => {
        try {
            if (token.value) {
                await fetch('/api/auth/logout', {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${token.value}`,
                        'Content-Type': 'application/json'
                    }
                });
            }
        } catch (error) {
            console.warn('Logout API call failed:', error);
        } finally {
            clearAuth();
            router.push('/auth/login');
        }
    };

    // Get default redirect path based on role
    const getDefaultPath = () => {
        if (!user.value) return '/';

        if (isAdmin.value || isNhanVien.value) {
            return '/dashboard';
        } else {
            return '/';
        }
    };

    // Initialize auth state
    onMounted(() => {
        loadUser();
    });

    return {
        // State
        user: computed(() => user.value),
        token: computed(() => token.value),
        isLoading: computed(() => isLoading.value),

        // Computed
        isAuthenticated,
        isAdmin,
        isNhanVien,
        isUser,
        canAccessAdmin,

        // Methods
        hasRole,
        hasAnyRole,
        verifyToken,
        login,
        logout,
        getDefaultPath,
        loadUser,
        clearAuth
    };
};

// Permission constants
export const PERMISSIONS = {
    // Product permissions
    VIEW_PRODUCTS: 'VIEW_PRODUCTS',
    MANAGE_PRODUCTS: 'MANAGE_PRODUCTS',
    CREATE_PRODUCT: 'CREATE_PRODUCT',
    EDIT_PRODUCT: 'EDIT_PRODUCT',
    DELETE_PRODUCT: 'DELETE_PRODUCT',

    // Order permissions
    VIEW_ORDERS: 'VIEW_ORDERS',
    MANAGE_ORDERS: 'MANAGE_ORDERS',
    VIEW_ALL_ORDERS: 'VIEW_ALL_ORDERS',
    EDIT_ORDER: 'EDIT_ORDER',
    CANCEL_ORDER: 'CANCEL_ORDER',

    // User permissions
    VIEW_USERS: 'VIEW_USERS',
    MANAGE_USERS: 'MANAGE_USERS',
    EDIT_USER: 'EDIT_USER',
    DELETE_USER: 'DELETE_USER',

    // System permissions
    VIEW_DASHBOARD: 'VIEW_DASHBOARD',
    MANAGE_SETTINGS: 'MANAGE_SETTINGS',
    VIEW_REPORTS: 'VIEW_REPORTS'
};

// Role-based permissions
export const ROLE_PERMISSIONS = {
    ADMIN: Object.values(PERMISSIONS),
    NHANVIEN: [
        PERMISSIONS.VIEW_PRODUCTS,
        PERMISSIONS.MANAGE_PRODUCTS,
        PERMISSIONS.CREATE_PRODUCT,
        PERMISSIONS.EDIT_PRODUCT,
        PERMISSIONS.VIEW_ORDERS,
        PERMISSIONS.MANAGE_ORDERS,
        PERMISSIONS.VIEW_ALL_ORDERS,
        PERMISSIONS.EDIT_ORDER,
        PERMISSIONS.VIEW_USERS,
        PERMISSIONS.VIEW_DASHBOARD
    ],
    USER: [
        PERMISSIONS.VIEW_PRODUCTS
    ]
};

// Check if user has permission
export const hasPermission = (userRole, permission) => {
    const rolePermissions = ROLE_PERMISSIONS[userRole] || [];
    return rolePermissions.includes(permission);
};

