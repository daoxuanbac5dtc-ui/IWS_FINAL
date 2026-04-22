// utils/api.js - Centralized API configuration with proper permission handling
import axios from 'axios'

// ===== API CONFIGURATION =====
const API_CONFIG = {
    // Environment-based URLs
    BASE_URL: process.env.NODE_ENV === 'production' 
        ? process.env.VUE_APP_API_URL || 'https://your-production-domain.com'
        : process.env.VUE_APP_API_URL || 'http://localhost:8080',
    
    // API endpoints
    ENDPOINTS: {
        // Authentication
        AUTH: {
            LOGIN: '/api/auth/login',
            LOGOUT: '/api/auth/logout',
            VERIFY: '/api/auth/verify',
            REFRESH: '/api/auth/refresh',
            ME: '/api/auth/me'
        },
        
        // Account management
        ACCOUNTS: '/api/tai-khoan',
        
        // Customer management
        CUSTOMERS: {
            BASE: '/api/khach-hang',
            SEARCH: '/api/khach-hang/search',
            BATCH_DELETE: '/api/khach-hang/batch',
            STATUS: (id) => `/api/khach-hang/${id}/status`
        },
        
        // Employee management
        EMPLOYEES: {
            BASE: '/api/nhan-vien',
            SEARCH: '/api/nhan-vien/search',
            BATCH_DELETE: '/api/nhan-vien/batch',
            STATUS: (id) => `/api/nhan-vien/${id}/status`
        },
        
        // Address services
        ADDRESS: {
            PROVINCES: '/api/vietnam-address/provinces',
            WARDS: (provinceCode) => `/api/vietnam-address/wards/${provinceCode}`
        },
        
        // Dashboard
        DASHBOARD: {
            STATS: '/api/dashboard/stats',
            HEALTH: '/api/dashboard/health',
            TEST: '/api/dashboard/test'
        }
    },
    
    // Request configuration
    TIMEOUT: 30000, // 30 seconds
    
    // Default headers
    DEFAULT_HEADERS: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
}

// ===== ROLE-BASED PERMISSIONS =====
const PERMISSIONS = {
    // View permissions
    VIEW_DASHBOARD: ['ADMIN', 'NHANVIEN'],
    VIEW_CUSTOMERS: ['ADMIN', 'NHANVIEN'],
    VIEW_EMPLOYEES: ['ADMIN', 'NHANVIEN'],
    VIEW_ACCOUNTS: ['ADMIN'],
    VIEW_REPORTS: ['ADMIN', 'NHANVIEN'],
    
    // Management permissions
    MANAGE_CUSTOMERS: ['ADMIN', 'NHANVIEN'],
    MANAGE_EMPLOYEES: ['ADMIN'], // Only ADMIN can manage employees
    MANAGE_ACCOUNTS: ['ADMIN'], // Only ADMIN can manage accounts
    MANAGE_PRODUCTS: ['ADMIN', 'NHANVIEN'],
    MANAGE_ORDERS: ['ADMIN', 'NHANVIEN'],
    
    // System permissions
    SYSTEM_SETTINGS: ['ADMIN'],
    DELETE_DATA: ['ADMIN'], // Only ADMIN can delete
    EXPORT_DATA: ['ADMIN', 'NHANVIEN'],
    
    // User permissions
    VIEW_PUBLIC: ['USER', 'ADMIN', 'NHANVIEN'], // Public content
    MANAGE_PROFILE: ['USER', 'ADMIN', 'NHANVIEN'] // Own profile
}

// ===== UTILITY FUNCTIONS =====

/**
 * Get current user from localStorage
 */
const getCurrentUser = () => {
    try {
        const userInfo = localStorage.getItem('user_info') || localStorage.getItem('user')
        return userInfo ? JSON.parse(userInfo) : null
    } catch (error) {
        console.error('Error parsing user info:', error)
        return null
    }
}

/**
 * Get current auth token
 */
const getCurrentToken = () => {
    return localStorage.getItem('auth_token') || localStorage.getItem('token')
}

/**
 * Check if user has specific permission
 */
const hasPermission = (permission) => {
    const user = getCurrentUser()
    if (!user || !user.vaiTro) return false
    
    const allowedRoles = PERMISSIONS[permission] || []
    return allowedRoles.includes(user.vaiTro)
}

/**
 * Check if user has any of the specified permissions
 */
const hasAnyPermission = (permissions) => {
    return permissions.some(permission => hasPermission(permission))
}

/**
 * Check if user is admin
 */
const isAdmin = () => {
    const user = getCurrentUser()
    return user?.vaiTro === 'ADMIN'
}

/**
 * Check if user is employee (staff)
 */
const isEmployee = () => {
    const user = getCurrentUser()
    return user?.vaiTro === 'NHANVIEN'
}

/**
 * Check if user is customer
 */
const isCustomer = () => {
    const user = getCurrentUser()
    return user?.vaiTro === 'USER'
}

/**
 * Clear all authentication data
 */
const clearAuthData = () => {
    const keysToRemove = [
        'auth_token', 'token', 'user_info', 'user',
        'rememberMe', 'savedEmail'
    ]
    
    keysToRemove.forEach(key => {
        localStorage.removeItem(key)
    })
}

/**
 * Redirect to login page
 */
const redirectToLogin = () => {
    const currentPath = window.location.pathname
    if (currentPath !== '/auth/login') {
        clearAuthData()
        window.location.href = `/auth/login?redirect=${encodeURIComponent(currentPath)}`
    }
}

/**
 * Redirect to access denied page
 */
const redirectToAccessDenied = () => {
    if (window.location.pathname !== '/auth/access') {
        window.location.href = '/auth/access'
    }
}

/**
 * Handle API errors with proper user feedback
 */
const handleApiError = (error) => {
    let errorInfo = {
        title: 'Lỗi API',
        message: 'Có lỗi xảy ra',
        code: null,
        details: null
    }
    
    if (error.response) {
        const { status, data } = error.response
        errorInfo.code = status
        
        switch (status) {
            case 400:
                errorInfo.title = 'Dữ liệu không hợp lệ'
                errorInfo.message = data.message || 'Vui lòng kiểm tra lại thông tin nhập vào'
                errorInfo.details = data.errors || data.details
                break
                
            case 401:
                errorInfo.title = 'Chưa đăng nhập'
                errorInfo.message = 'Vui lòng đăng nhập để tiếp tục'
                redirectToLogin()
                break
                
            case 403:
                errorInfo.title = 'Không có quyền truy cập'
                errorInfo.message = 'Bạn không có quyền thực hiện hành động này'
                break
                
            case 404:
                errorInfo.title = 'Không tìm thấy'
                errorInfo.message = 'Dữ liệu không tồn tại hoặc đã bị xóa'
                break
                
            case 409:
                errorInfo.title = 'Dữ liệu trùng lặp'
                errorInfo.message = data.message || 'Dữ liệu đã tồn tại trong hệ thống'
                break
                
            case 422:
                errorInfo.title = 'Dữ liệu không hợp lệ'
                errorInfo.message = 'Dữ liệu không đáp ứng yêu cầu của hệ thống'
                errorInfo.details = data.errors
                break
                
            case 429:
                errorInfo.title = 'Quá nhiều yêu cầu'
                errorInfo.message = 'Vui lòng thử lại sau ít phút'
                break
                
            case 500:
                errorInfo.title = 'Lỗi server'
                errorInfo.message = 'Có lỗi xảy ra trên máy chủ. Vui lòng thử lại sau.'
                break
                
            case 502:
            case 503:
            case 504:
                errorInfo.title = 'Dịch vụ tạm ngưng'
                errorInfo.message = 'Dịch vụ hiện không khả dụng. Vui lòng thử lại sau.'
                break
                
            default:
                errorInfo.message = data.message || data.error || `Lỗi HTTP ${status}`
        }
    } else if (error.code) {
        switch (error.code) {
            case 'ECONNREFUSED':
                errorInfo.title = 'Lỗi kết nối'
                errorInfo.message = 'Không thể kết nối đến máy chủ'
                break
                
            case 'NETWORK_ERROR':
                errorInfo.title = 'Lỗi mạng'
                errorInfo.message = 'Vui lòng kiểm tra kết nối internet'
                break
                
            case 'TIMEOUT':
                errorInfo.title = 'Hết thời gian chờ'
                errorInfo.message = 'Yêu cầu mất quá nhiều thời gian. Vui lòng thử lại.'
                break
                
            default:
                errorInfo.message = error.message || 'Lỗi không xác định'
        }
    } else {
        errorInfo.message = error.message || 'Lỗi không xác định'
    }
    
    return errorInfo
}

// ===== AXIOS INSTANCE CONFIGURATION =====
const api = axios.create({
    baseURL: API_CONFIG.BASE_URL,
    timeout: API_CONFIG.TIMEOUT,
    headers: API_CONFIG.DEFAULT_HEADERS,
    withCredentials: false // Set to true if using cookies for auth
})

// ===== REQUEST INTERCEPTOR =====
api.interceptors.request.use(
    (config) => {
        // Add authentication token
        const token = getCurrentToken()
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        
        // Add request ID for tracking
        config.metadata = { 
            startTime: new Date(),
            requestId: Math.random().toString(36).substring(7)
        }
        
        // Log request in development
        if (process.env.NODE_ENV === 'development') {
            console.group(`🚀 API Request [${config.metadata.requestId}]`)
            console.log('Method:', config.method?.toUpperCase())
            console.log('URL:', config.url)
            console.log('Data:', config.data)
            console.log('Params:', config.params)
            console.groupEnd()
        }
        
        return config
    },
    (error) => {
        console.error('Request interceptor error:', error)
        return Promise.reject(error)
    }
)

// ===== RESPONSE INTERCEPTOR =====
api.interceptors.response.use(
    (response) => {
        const duration = new Date() - response.config.metadata?.startTime
        
        // Log successful response in development
        if (process.env.NODE_ENV === 'development') {
            console.group(`✅ API Response [${response.config.metadata?.requestId}] - ${duration}ms`)
            console.log('Status:', response.status)
            console.log('URL:', response.config.url)
            console.log('Data:', response.data)
            console.groupEnd()
        }
        
        return response
    },
    (error) => {
        const duration = error.config?.metadata ? new Date() - error.config.metadata.startTime : 0
        
        // Log error in development
        if (process.env.NODE_ENV === 'development') {
            console.group(`❌ API Error [${error.config?.metadata?.requestId}] - ${duration}ms`)
            console.log('Status:', error.response?.status)
            console.log('URL:', error.config?.url)
            console.log('Message:', error.message)
            console.log('Response:', error.response?.data)
            console.groupEnd()
        }
        
        // Handle authentication errors
        if (error.response?.status === 401) {
            clearAuthData()
            redirectToLogin()
        }
        
        // Handle permission errors
        if (error.response?.status === 403) {
            redirectToAccessDenied()
        }
        
        return Promise.reject(error)
    }
)

// ===== API METHODS =====

/**
 * Safe API request with permission check
 */
const safeRequest = async (config, requiredPermission = null) => {
    // Check permission if specified
    if (requiredPermission && !hasPermission(requiredPermission)) {
        throw new Error(`Permission denied: ${requiredPermission}`)
    }
    
    try {
        const response = await api(config)
        return response
    } catch (error) {
        throw handleApiError(error)
    }
}

/**
 * Authentication API methods
 */
const auth = {
    login: (credentials) => api.post(API_CONFIG.ENDPOINTS.AUTH.LOGIN, credentials),
    logout: () => api.post(API_CONFIG.ENDPOINTS.AUTH.LOGOUT),
    verify: () => api.post(API_CONFIG.ENDPOINTS.AUTH.VERIFY),
    refresh: () => api.post(API_CONFIG.ENDPOINTS.AUTH.REFRESH),
    getProfile: () => api.get(API_CONFIG.ENDPOINTS.AUTH.ME)
}

/**
 * Account management API methods (Admin only)
 */
const accounts = {
    getAll: () => safeRequest({ 
        method: 'GET', 
        url: API_CONFIG.ENDPOINTS.ACCOUNTS 
    }, 'MANAGE_ACCOUNTS'),
    
    create: (data) => safeRequest({ 
        method: 'POST', 
        url: API_CONFIG.ENDPOINTS.ACCOUNTS, 
        data 
    }, 'MANAGE_ACCOUNTS'),
    
    update: (id, data) => safeRequest({ 
        method: 'PUT', 
        url: `${API_CONFIG.ENDPOINTS.ACCOUNTS}/${id}`, 
        data 
    }, 'MANAGE_ACCOUNTS'),
    
    delete: (id) => safeRequest({ 
        method: 'DELETE', 
        url: `${API_CONFIG.ENDPOINTS.ACCOUNTS}/${id}` 
    }, 'DELETE_DATA'),
    
    changeStatus: (id, status) => safeRequest({ 
        method: 'PATCH', 
        url: `${API_CONFIG.ENDPOINTS.ACCOUNTS}/${id}/trang-thai`,
        params: { trangThai: status }
    }, 'MANAGE_ACCOUNTS')
}

/**
 * Customer management API methods
 */
const customers = {
    getAll: (params = {}) => safeRequest({ 
        method: 'GET', 
        url: API_CONFIG.ENDPOINTS.CUSTOMERS.BASE, 
        params 
    }, 'VIEW_CUSTOMERS'),
    
    search: (params = {}) => safeRequest({ 
        method: 'GET', 
        url: API_CONFIG.ENDPOINTS.CUSTOMERS.SEARCH, 
        params 
    }, 'VIEW_CUSTOMERS'),
    
    getById: (id) => safeRequest({ 
        method: 'GET', 
        url: `${API_CONFIG.ENDPOINTS.CUSTOMERS.BASE}/${id}` 
    }, 'VIEW_CUSTOMERS'),
    
    create: (data) => safeRequest({ 
        method: 'POST', 
        url: API_CONFIG.ENDPOINTS.CUSTOMERS.BASE, 
        data 
    }, 'MANAGE_CUSTOMERS'),
    
    update: (id, data) => safeRequest({ 
        method: 'PUT', 
        url: `${API_CONFIG.ENDPOINTS.CUSTOMERS.BASE}/${id}`, 
        data 
    }, 'MANAGE_CUSTOMERS'),
    
    delete: (id) => safeRequest({ 
        method: 'DELETE', 
        url: `${API_CONFIG.ENDPOINTS.CUSTOMERS.BASE}/${id}` 
    }, 'DELETE_DATA'),
    
    batchDelete: (ids) => safeRequest({ 
        method: 'DELETE', 
        url: API_CONFIG.ENDPOINTS.CUSTOMERS.BATCH_DELETE, 
        data: ids 
    }, 'DELETE_DATA'),
    
    changeStatus: (id, status) => safeRequest({ 
        method: 'PATCH', 
        url: API_CONFIG.ENDPOINTS.CUSTOMERS.STATUS(id), 
        data: { trangThai: status } 
    }, 'MANAGE_CUSTOMERS')
}

/**
 * Employee management API methods (Admin only for modifications)
 */
const employees = {
    getAll: (params = {}) => safeRequest({ 
        method: 'GET', 
        url: API_CONFIG.ENDPOINTS.EMPLOYEES.BASE, 
        params 
    }, 'VIEW_EMPLOYEES'),
    
    search: (params = {}) => safeRequest({ 
        method: 'GET', 
        url: API_CONFIG.ENDPOINTS.EMPLOYEES.SEARCH, 
        params 
    }, 'VIEW_EMPLOYEES'),
    
    getById: (id) => safeRequest({ 
        method: 'GET', 
        url: `${API_CONFIG.ENDPOINTS.EMPLOYEES.BASE}/${id}` 
    }, 'VIEW_EMPLOYEES'),
    
    create: (data) => safeRequest({ 
        method: 'POST', 
        url: API_CONFIG.ENDPOINTS.EMPLOYEES.BASE, 
        data 
    }, 'MANAGE_EMPLOYEES'),
    
    update: (id, data) => safeRequest({ 
        method: 'PUT', 
        url: `${API_CONFIG.ENDPOINTS.EMPLOYEES.BASE}/${id}`, 
        data 
    }, 'MANAGE_EMPLOYEES'),
    
    delete: (id) => safeRequest({ 
        method: 'DELETE', 
        url: `${API_CONFIG.ENDPOINTS.EMPLOYEES.BASE}/${id}` 
    }, 'MANAGE_EMPLOYEES'), // Note: Only ADMIN can delete employees
    
    batchDelete: (ids) => safeRequest({ 
        method: 'DELETE', 
        url: API_CONFIG.ENDPOINTS.EMPLOYEES.BATCH_DELETE, 
        data: ids 
    }, 'MANAGE_EMPLOYEES'),
    
    changeStatus: (id, status) => safeRequest({ 
        method: 'PATCH', 
        url: API_CONFIG.ENDPOINTS.EMPLOYEES.STATUS(id), 
        data: { trangThai: status } 
    }, 'MANAGE_EMPLOYEES')
}

/**
 * Address API methods (Public access)
 */
const address = {
    getProvinces: () => api.get(API_CONFIG.ENDPOINTS.ADDRESS.PROVINCES),
    getWards: (provinceCode) => api.get(API_CONFIG.ENDPOINTS.ADDRESS.WARDS(provinceCode))
}

/**
 * Dashboard API methods
 */
const dashboard = {
    getStats: (period = '30days') => safeRequest({ 
        method: 'GET', 
        url: API_CONFIG.ENDPOINTS.DASHBOARD.STATS, 
        params: { period } 
    }, 'VIEW_DASHBOARD'),
    
    getStatsByRange: (startDate, endDate) => safeRequest({ 
        method: 'GET', 
        url: API_CONFIG.ENDPOINTS.DASHBOARD.STATS, 
        params: { startDate, endDate } 
    }, 'VIEW_DASHBOARD'),
    
    healthCheck: () => api.get(API_CONFIG.ENDPOINTS.DASHBOARD.HEALTH),
    
    testConnection: () => api.get(API_CONFIG.ENDPOINTS.DASHBOARD.TEST)
}

// ===== EXPORTS =====
export default api

export {
    accounts, address, API_CONFIG,
    // API methods
    auth, clearAuthData, customers, dashboard, employees, getCurrentToken,
    // Utility functions
    getCurrentUser, handleApiError, hasAnyPermission, hasPermission, isAdmin, isCustomer, isEmployee, PERMISSIONS, safeRequest
}

// ===== ROLE-BASED COMPONENT GUARDS =====

/**
 * Higher-order component for role-based access control
 */
export const withPermission = (permission) => {
    return (component) => {
        return {
            ...component,
            beforeCreate() {
                if (!hasPermission(permission)) {
                    this.$router.push('/auth/access')
                    return
                }
                if (component.beforeCreate) {
                    component.beforeCreate.call(this)
                }
            }
        }
    }
}

/**
 * Route guard for permission checking
 */
export const requirePermission = (permission) => {
    return (to, from, next) => {
        if (hasPermission(permission)) {
            next()
        } else {
            next('/auth/access')
        }
    }
}

/**
 * Multiple permissions guard (user must have ALL permissions)
 */
export const requireAllPermissions = (permissions) => {
    return (to, from, next) => {
        const hasAll = permissions.every(permission => hasPermission(permission))
        if (hasAll) {
            next()
        } else {
            next('/auth/access')
        }
    }
}

/**
 * Multiple permissions guard (user must have ANY of the permissions)
 */
export const requireAnyPermission = (permissions) => {
    return (to, from, next) => {
        if (hasAnyPermission(permissions)) {
            next()
        } else {
            next('/auth/access')
        }
    }
}