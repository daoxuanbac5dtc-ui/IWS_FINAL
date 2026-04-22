// composables/useAuthPermissions.js - Enhanced authentication and permissions management
import {
    auth as authApi,
    clearAuthData,
    getCurrentToken,
    getCurrentUser,
    hasAnyPermission,
    hasPermission,
    isAdmin,
    isCustomer,
    isEmployee
} from '@/utils/api.js'
import { useToast } from 'primevue/usetoast'
import { computed, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'

// ===== REACTIVE AUTH STATE =====
const authState = reactive({
    user: null,
    token: null,
    isAuthenticated: false,
    isLoading: false,
    error: null,
    lastChecked: null
})

// ===== COMPOSABLE =====
export function useAuthPermissions() {
    const router = useRouter()
    const toast = useToast()

    // ===== COMPUTED PROPERTIES =====
    const user = computed(() => authState.user || getCurrentUser())
    const token = computed(() => authState.token || getCurrentToken())
    const isAuthenticated = computed(() => {
        return Boolean(user.value && token.value)
    })
    const loading = computed(() => authState.isLoading)
    const error = computed(() => authState.error)

    // Role checks
    const userRole = computed(() => user.value?.vaiTro || null)
    const isAdminUser = computed(() => isAdmin())
    const isEmployeeUser = computed(() => isEmployee())
    const isCustomerUser = computed(() => isCustomer())
    const canAccessAdmin = computed(() => isAdminUser.value || isEmployeeUser.value)

    // ===== PERMISSION METHODS =====
    
    /**
     * Check if user has specific permission
     */
    const checkPermission = (permission) => {
        return hasPermission(permission)
    }

    /**
     * Check if user has any of the specified permissions
     */
    const checkAnyPermission = (permissions) => {
        return hasAnyPermission(permissions)
    }

    /**
     * Check multiple permissions and return results
     */
    const checkPermissions = (permissions) => {
        const results = {}
        permissions.forEach(permission => {
            results[permission] = hasPermission(permission)
        })
        return results
    }

    /**
     * Guard method - throws error if permission not met
     */
    const requirePermission = (permission, customMessage = null) => {
        if (!checkPermission(permission)) {
            const message = customMessage || `Bạn không có quyền: ${permission}`
            throw new Error(message)
        }
        return true
    }

    /**
     * Safe permission check with toast notification
     */
    const checkPermissionWithNotification = (permission, errorMessage = null) => {
        const hasAccess = checkPermission(permission)
        
        if (!hasAccess && errorMessage !== false) {
            const message = errorMessage || `Bạn không có quyền thực hiện hành động này. Yêu cầu: ${permission}`
            toast.add({
                severity: 'warn',
                summary: 'Không có quyền',
                detail: message,
                life: 4000
            })
        }
        
        return hasAccess
    }

    // ===== SPECIFIC PERMISSION CHECKS =====
    
    // Customer management permissions
    const canViewCustomers = computed(() => checkPermission('VIEW_CUSTOMERS'))
    const canManageCustomers = computed(() => checkPermission('MANAGE_CUSTOMERS'))
    const canDeleteCustomers = computed(() => checkPermission('DELETE_DATA'))
    
    // Employee management permissions
    const canViewEmployees = computed(() => checkPermission('VIEW_EMPLOYEES'))
    const canManageEmployees = computed(() => checkPermission('MANAGE_EMPLOYEES'))
    const canDeleteEmployees = computed(() => checkPermission('DELETE_DATA') && isAdminUser.value)
    
    // Account management permissions
    const canViewAccounts = computed(() => checkPermission('VIEW_ACCOUNTS'))
    const canManageAccounts = computed(() => checkPermission('MANAGE_ACCOUNTS'))
    
    // Dashboard and reporting permissions
    const canViewDashboard = computed(() => checkPermission('VIEW_DASHBOARD'))
    const canViewReports = computed(() => checkPermission('VIEW_REPORTS'))
    const canExportData = computed(() => checkPermission('EXPORT_DATA'))
    
    // System permissions
    const canManageSystem = computed(() => checkPermission('SYSTEM_SETTINGS'))

    // ===== UI HELPER METHODS =====

    /**
     * Get user display name
     */
    const getUserDisplayName = () => {
        if (!user.value) return 'Người dùng'
        return user.value.hoTen || user.value.email || 'Người dùng'
    }

    /**
     * Get user role label
     */
    const getUserRoleLabel = () => {
        switch (userRole.value) {
            case 'ADMIN': return 'Quản trị viên'
            case 'NHANVIEN': return 'Nhân viên'
            case 'USER': return 'Khách hàng'
            default: return 'Không xác định'
        }
    }

    /**
     * Get role-based dashboard path
     */
    const getDefaultDashboardPath = () => {
        if (isAdminUser.value || isEmployeeUser.value) {
            return '/admin/dashboard'
        }
        return '/'
    }

    /**
     * Check if user can access specific route
     */
    const canAccessRoute = (routePath) => {
        // Define route permissions
        const routePermissions = {
            '/admin': 'VIEW_DASHBOARD',
            '/admin/dashboard': 'VIEW_DASHBOARD',
            '/admin/khach-hang': 'VIEW_CUSTOMERS',
            '/admin/nhan-vien': 'VIEW_EMPLOYEES',
            '/admin/tai-khoan': 'MANAGE_ACCOUNTS',
            '/admin/san-pham': 'MANAGE_PRODUCTS',
            '/admin/hoa-don': 'VIEW_ORDERS'
        }

        const requiredPermission = routePermissions[routePath]
        return requiredPermission ? checkPermission(requiredPermission) : true
    }

    // ===== AUTH ACTIONS =====

    /**
     * Initialize auth state from storage
     */
    const initializeAuth = () => {
        authState.isLoading = true
        authState.error = null

        try {
            const currentUser = getCurrentUser()
            const currentToken = getCurrentToken()

            if (currentUser && currentToken) {
                authState.user = currentUser
                authState.token = currentToken
                authState.isAuthenticated = true
                authState.lastChecked = new Date()
                console.log('✅ Auth initialized:', {
                    user: currentUser.email,
                    role: currentUser.vaiTro
                })
            } else {
                authState.user = null
                authState.token = null
                authState.isAuthenticated = false
            }
        } catch (error) {
            console.error('❌ Auth initialization failed:', error)
            authState.error = 'Không thể khởi tạo xác thực'
            clearAuth()
        } finally {
            authState.isLoading = false
        }
    }

    /**
     * Login user
     */
    const login = async (credentials) => {
        authState.isLoading = true
        authState.error = null

        try {
            const response = await authApi.login(credentials)
            
            if (response.data && response.data.success) {
                const { token, user } = response.data.data
                
                // Store in localStorage
                localStorage.setItem('auth_token', token)
                localStorage.setItem('user_info', JSON.stringify(user))
                
                // Update reactive state
                authState.user = user
                authState.token = token
                authState.isAuthenticated = true
                authState.lastChecked = new Date()
                
                toast.add({
                    severity: 'success',
                    summary: 'Đăng nhập thành công',
                    detail: `Chào mừng ${getUserDisplayName()}!`,
                    life: 3000
                })

                return { success: true, user, token }
            } else {
                throw new Error(response.data?.message || 'Đăng nhập thất bại')
            }
        } catch (error) {
            console.error('❌ Login failed:', error)
            authState.error = error.message || 'Đăng nhập thất bại'
            
            toast.add({
                severity: 'error',
                summary: 'Đăng nhập thất bại',
                detail: error.message || 'Vui lòng kiểm tra lại thông tin đăng nhập',
                life: 5000
            })
            
            return { success: false, error: error.message }
        } finally {
            authState.isLoading = false
        }
    }

    /**
     * Logout user
     */
    const logout = async () => {
        authState.isLoading = true
        
        try {
            // Call logout API if token exists
            if (authState.token || getCurrentToken()) {
                try {
                    await authApi.logout()
                } catch (apiError) {
                    console.warn('⚠️ Logout API failed:', apiError.message)
                }
            }
            
            clearAuth()
            
            toast.add({
                severity: 'info',
                summary: 'Đã đăng xuất',
                detail: 'Cảm ơn bạn đã sử dụng hệ thống',
                life: 3000
            })
            
            // Redirect to login page
            router.push('/auth/login')
            
        } catch (error) {
            console.error('❌ Logout error:', error)
            // Force clear even if API fails
            clearAuth()
            router.push('/auth/login')
        } finally {
            authState.isLoading = false
        }
    }

    /**
     * Clear authentication data
     */
    const clearAuth = () => {
        clearAuthData()
        authState.user = null
        authState.token = null
        authState.isAuthenticated = false
        authState.error = null
        authState.lastChecked = null
    }

    /**
     * Verify token with server
     */
    const verifyToken = async () => {
        if (!token.value) return false
        
        try {
            const response = await authApi.verify()
            
            if (response.data?.success) {
                // Update user data if changed
                if (response.data.data) {
                    authState.user = response.data.data
                    localStorage.setItem('user_info', JSON.stringify(response.data.data))
                }
                authState.lastChecked = new Date()
                return true
            }
            
            clearAuth()
            return false
        } catch (error) {
            console.error('❌ Token verification failed:', error)
            clearAuth()
            return false
        }
    }

    /**
     * Refresh user profile data
     */
    const refreshProfile = async () => {
        if (!isAuthenticated.value) return false
        
        try {
            const response = await authApi.getProfile()
            
            if (response.data) {
                authState.user = response.data
                localStorage.setItem('user_info', JSON.stringify(response.data))
                authState.lastChecked = new Date()
                return true
            }
            
            return false
        } catch (error) {
            console.error('❌ Profile refresh failed:', error)
            return false
        }
    }

    // ===== ROUTE GUARDS =====

    /**
     * Navigation guard for authentication
     */
    const requireAuth = (to, from, next) => {
        if (isAuthenticated.value) {
            next()
        } else {
            toast.add({
                severity: 'warn',
                summary: 'Yêu cầu đăng nhập',
                detail: 'Vui lòng đăng nhập để truy cập trang này',
                life: 4000
            })
            next({
                path: '/auth/login',
                query: { redirect: to.fullPath }
            })
        }
    }

    /**
     * Navigation guard for guest only routes
     */
    const requireGuest = (to, from, next) => {
        if (isAuthenticated.value) {
            next(getDefaultDashboardPath())
        } else {
            next()
        }
    }

    /**
     * Navigation guard with permission check
     */
    const requirePermissionGuard = (permission) => {
        return (to, from, next) => {
            if (!isAuthenticated.value) {
                next({
                    path: '/auth/login',
                    query: { redirect: to.fullPath }
                })
                return
            }

            if (checkPermission(permission)) {
                next()
            } else {
                toast.add({
                    severity: 'error',
                    summary: 'Không có quyền truy cập',
                    detail: `Bạn không có quyền truy cập trang này. Yêu cầu: ${permission}`,
                    life: 5000
                })
                next('/auth/access')
            }
        }
    }

    /**
     * Role-based navigation guard
     */
    const requireRole = (allowedRoles) => {
        return (to, from, next) => {
            if (!isAuthenticated.value) {
                next({
                    path: '/auth/login',
                    query: { redirect: to.fullPath }
                })
                return
            }

            if (allowedRoles.includes(userRole.value)) {
                next()
            } else {
                toast.add({
                    severity: 'error',
                    summary: 'Không có quyền truy cập',
                    detail: `Trang này chỉ dành cho: ${allowedRoles.join(', ')}`,
                    life: 5000
                })
                next('/auth/access')
            }
        }
    }

    // ===== WATCHERS =====

    // Watch for route changes and verify permissions
    watch(() => router.currentRoute.value.path, (newPath) => {
        if (isAuthenticated.value && !canAccessRoute(newPath)) {
            toast.add({
                severity: 'warn',
                summary: 'Truy cập bị từ chối',
                detail: 'Bạn không có quyền truy cập trang này',
                life: 4000
            })
            router.push('/auth/access')
        }
    })

    // Auto-refresh token periodically
    watch(isAuthenticated, (authenticated) => {
        if (authenticated) {
            // Verify token every 30 minutes
            const interval = setInterval(async () => {
                const isValid = await verifyToken()
                if (!isValid) {
                    clearInterval(interval)
                    router.push('/auth/login')
                }
            }, 30 * 60 * 1000)

            return () => clearInterval(interval)
        }
    }, { immediate: true })

    // ===== DEBUG HELPERS =====

    const debugAuth = () => {
        const debugInfo = {
            state: {
                isAuthenticated: isAuthenticated.value,
                hasUser: Boolean(user.value),
                hasToken: Boolean(token.value),
                loading: loading.value,
                error: error.value,
                lastChecked: authState.lastChecked
            },
            user: user.value ? {
                id: user.value.id,
                email: user.value.email,
                role: user.value.vaiTro,
                name: getUserDisplayName()
            } : null,
            permissions: {
                isAdmin: isAdminUser.value,
                isEmployee: isEmployeeUser.value,
                isCustomer: isCustomerUser.value,
                canAccessAdmin: canAccessAdmin.value,
                canViewCustomers: canViewCustomers.value,
                canManageCustomers: canManageCustomers.value,
                canViewEmployees: canViewEmployees.value,
                canManageEmployees: canManageEmployees.value,
                canManageAccounts: canManageAccounts.value,
                canViewDashboard: canViewDashboard.value
            },
            storage: {
                hasTokenInStorage: Boolean(getCurrentToken()),
                hasUserInStorage: Boolean(getCurrentUser())
            }
        }
        
        console.log('🔍 Auth Debug Info:', debugInfo)
        return debugInfo
    }

    // ===== INITIALIZATION =====

    // Auto-initialize on first use
    if (!authState.lastChecked) {
        initializeAuth()
    }

    // ===== RETURN API =====
    return {
        // State
        user: user,
        token: token,
        isAuthenticated: isAuthenticated,
        loading: loading,
        error: error,
        userRole: userRole,

        // Role checks
        isAdmin: isAdminUser,
        isEmployee: isEmployeeUser,
        isCustomer: isCustomerUser,
        canAccessAdmin: canAccessAdmin,

        // Permission checks
        checkPermission,
        checkAnyPermission,
        checkPermissions,
        requirePermission,
        checkPermissionWithNotification,

        // Specific permissions
        canViewCustomers,
        canManageCustomers,
        canDeleteCustomers,
        canViewEmployees,
        canManageEmployees,
        canDeleteEmployees,
        canViewAccounts,
        canManageAccounts,
        canViewDashboard,
        canViewReports,
        canExportData,
        canManageSystem,

        // UI helpers
        getUserDisplayName,
        getUserRoleLabel,
        getDefaultDashboardPath,
        canAccessRoute,

        // Auth actions
        initializeAuth,
        login,
        logout,
        clearAuth,
        verifyToken,
        refreshProfile,

        // Route guards
        requireAuth,
        requireGuest,
        requirePermissionGuard,
        requireRole,

        // Debug
        debugAuth
    }
}

// ===== GLOBAL AUTH STATE MANAGER =====

/**
 * Global auth state that can be used across the app
 */
class AuthManager {
    constructor() {
        this.initialized = false
    }

    async initialize() {
        if (this.initialized) return

        const { initializeAuth, verifyToken, isAuthenticated } = useAuthPermissions()
        
        initializeAuth()
        
        // Verify token on app startup
        if (isAuthenticated.value) {
            await verifyToken()
        }
        
        this.initialized = true
        console.log('🔐 AuthManager initialized')
    }

    getInstance() {
        return useAuthPermissions()
    }
}

export const authManager = new AuthManager()

// ===== VUE PLUGIN =====

/**
 * Vue plugin for global auth integration
 */
export const AuthPlugin = {
    install(app, options = {}) {
        // Initialize auth manager
        authManager.initialize()

        // Provide auth instance globally
        app.provide('auth', authManager.getInstance())

        // Global properties
        app.config.globalProperties.$auth = authManager.getInstance()
        
        console.log('🔌 Auth plugin installed')
    }
}

// ===== UTILITIES FOR TEMPLATES =====

/**
 * Template directive for permission-based rendering
 * Usage: v-permission="'VIEW_CUSTOMERS'"
 */
export const permissionDirective = {
    beforeMount(el, binding) {
        const { checkPermission } = useAuthPermissions()
        const permission = binding.value
        
        if (!checkPermission(permission)) {
            el.style.display = 'none'
            el.setAttribute('data-no-permission', permission)
        }
    },
    
    updated(el, binding) {
        const { checkPermission } = useAuthPermissions()
        const permission = binding.value
        
        if (!checkPermission(permission)) {
            el.style.display = 'none'
            el.setAttribute('data-no-permission', permission)
        } else {
            el.style.display = ''
            el.removeAttribute('data-no-permission')
        }
    }
}

/**
 * Template directive for role-based rendering
 * Usage: v-role="['ADMIN', 'NHANVIEN']"
 */
export const roleDirective = {
    beforeMount(el, binding) {
        const { userRole } = useAuthPermissions()
        const allowedRoles = Array.isArray(binding.value) ? binding.value : [binding.value]
        
        if (!allowedRoles.includes(userRole.value)) {
            el.style.display = 'none'
            el.setAttribute('data-no-role', allowedRoles.join(','))
        }
    },
    
    updated(el, binding) {
        const { userRole } = useAuthPermissions()
        const allowedRoles = Array.isArray(binding.value) ? binding.value : [binding.value]
        
        if (!allowedRoles.includes(userRole.value)) {
            el.style.display = 'none'
            el.setAttribute('data-no-role', allowedRoles.join(','))
        } else {
            el.style.display = ''
            el.removeAttribute('data-no-role')
        }
    }
}

export default useAuthPermissions