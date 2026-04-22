// stores/auth.js - Improved Auth Store with proper error handling
import { computed, reactive, watch } from 'vue'

// ===== CONSTANTS =====
const AUTH_STORAGE_KEYS = {
    TOKEN: 'auth_token',
    USER: 'user_info',
    REMEMBER_ME: 'rememberMe'
}

const DEFAULT_USER_ROLES = {
    ADMIN: 'ADMIN',
    NHANVIEN: 'NHANVIEN',
    USER: 'USER'
}

// ===== REACTIVE STATE =====
const state = reactive({
    user: null,
    token: null,
    isAuthenticated: false,
    loading: false,
    error: null
})

// ===== UTILITIES =====
const isValidToken = (token) => {
    if (!token || typeof token !== 'string') return false
    
    // Add more sophisticated token validation here
    // For JWT tokens, you might decode and check expiration
    try {
        // Basic validation - minimum length
        return token.length > 10
    } catch {
        return false
    }
}

const isValidUser = (user) => {
    return user && 
           typeof user === 'object' && 
           user.id && 
           user.email && 
           user.vaiTro &&
           Object.values(DEFAULT_USER_ROLES).includes(user.vaiTro)
}

const saveToStorage = (key, value) => {
    try {
        localStorage.setItem(key, typeof value === 'object' ? JSON.stringify(value) : value)
    } catch (error) {
        console.warn(`Failed to save ${key} to localStorage:`, error)
    }
}

const removeFromStorage = (key) => {
    try {
        localStorage.removeItem(key)
    } catch (error) {
        console.warn(`Failed to remove ${key} from localStorage:`, error)
    }
}

const getFromStorage = (key, isJSON = false) => {
    try {
        const item = localStorage.getItem(key)
        return isJSON && item ? JSON.parse(item) : item
    } catch (error) {
        console.warn(`Failed to get ${key} from localStorage:`, error)
        return null
    }
}

// ===== INITIALIZATION =====
const initializeAuth = () => {
    console.log('🔐 Initializing Auth Store...')
    
    state.loading = true
    state.error = null
    
    try {
        // Try to load from localStorage
        const token = getFromStorage(AUTH_STORAGE_KEYS.TOKEN)
        const userData = getFromStorage(AUTH_STORAGE_KEYS.USER, true)
        
        if (token && isValidToken(token) && userData && isValidUser(userData)) {
            state.token = token
            state.user = userData
            state.isAuthenticated = true
            console.log('✅ User restored from localStorage:', {
                id: userData.id,
                email: userData.email,
                role: userData.vaiTro
            })
        } else {
            // Clear invalid data
            clearAuthData()
            console.log('⚠️ Invalid stored auth data, clearing...')
        }
    } catch (error) {
        console.error('❌ Error initializing auth:', error)
        state.error = 'Failed to initialize authentication'
        clearAuthData()
    } finally {
        state.loading = false
    }
}

const clearAuthData = () => {
    state.user = null
    state.token = null
    state.isAuthenticated = false
    state.error = null
    
    // Clear from localStorage
    removeFromStorage(AUTH_STORAGE_KEYS.TOKEN)
    removeFromStorage(AUTH_STORAGE_KEYS.USER)
    removeFromStorage(AUTH_STORAGE_KEYS.REMEMBER_ME)
    
    // Clear legacy keys
    removeFromStorage('token')
    removeFromStorage('user')
}

// ===== COMPOSABLE FUNCTION =====
export const useAuthStore = () => {
    // Initialize if not already done
    if (!state.user && !state.loading) {
        initializeAuth()
    }

    // ===== COMPUTED GETTERS =====
    const user = computed(() => state.user)
    const token = computed(() => state.token)
    const isAuthenticated = computed(() => state.isAuthenticated && !!state.user && !!state.token)
    const loading = computed(() => state.loading)
    const error = computed(() => state.error)
    
    const getUserRole = computed(() => {
        return state.user?.vaiTro || null
    })

    const isAdmin = computed(() => {
        return getUserRole.value === DEFAULT_USER_ROLES.ADMIN
    })

    const isNhanVien = computed(() => {
        return getUserRole.value === DEFAULT_USER_ROLES.NHANVIEN
    })

    const isUser = computed(() => {
        return getUserRole.value === DEFAULT_USER_ROLES.USER
    })

    const canAccessAdminArea = computed(() => {
        return isAdmin.value || isNhanVien.value
    })

    // ===== ACTIONS =====
    const setUser = (userData) => {
        if (!isValidUser(userData)) {
            console.error('❌ Invalid user data provided to setUser:', userData)
            state.error = 'Invalid user data'
            return false
        }

        state.user = userData
        state.isAuthenticated = true
        state.error = null
        
        saveToStorage(AUTH_STORAGE_KEYS.USER, userData)
        
        console.log('✅ User set successfully:', {
            id: userData.id,
            email: userData.email,
            role: userData.vaiTro
        })
        return true
    }

    const setToken = (tokenValue) => {
        if (!isValidToken(tokenValue)) {
            console.error('❌ Invalid token provided to setToken')
            state.error = 'Invalid token'
            return false
        }

        state.token = tokenValue
        state.error = null
        
        saveToStorage(AUTH_STORAGE_KEYS.TOKEN, tokenValue)
        
        console.log('✅ Token set successfully')
        return true
    }

    const login = async (userData, tokenValue, rememberMe = false) => {
        state.loading = true
        state.error = null

        try {
            if (!isValidUser(userData) || !isValidToken(tokenValue)) {
                throw new Error('Invalid login data')
            }

            const userSet = setUser(userData)
            const tokenSet = setToken(tokenValue)

            if (!userSet || !tokenSet) {
                throw new Error('Failed to set authentication data')
            }

            if (rememberMe) {
                saveToStorage(AUTH_STORAGE_KEYS.REMEMBER_ME, 'true')
            }

            console.log('✅ Login successful for user:', userData.email)
            return true

        } catch (error) {
            console.error('❌ Login failed:', error)
            state.error = error.message || 'Login failed'
            clearAuthData()
            return false
        } finally {
            state.loading = false
        }
    }

    const logout = async () => {
        state.loading = true
        
        try {
            // Optional: Call logout API
            if (state.token) {
                try {
                    await fetch('/api/auth/logout', {
                        method: 'POST',
                        headers: {
                            'Authorization': `Bearer ${state.token}`,
                            'Content-Type': 'application/json'
                        }
                    })
                } catch (apiError) {
                    console.warn('⚠️ Logout API call failed:', apiError)
                }
            }

            clearAuthData()
            console.log('👋 Logout successful')
            
        } catch (error) {
            console.error('❌ Logout error:', error)
            // Still clear data even if API call fails
            clearAuthData()
        } finally {
            state.loading = false
        }
    }

    const refreshUser = async () => {
        if (!state.token) {
            console.warn('⚠️ No token available for refresh')
            return false
        }

        state.loading = true
        try {
            // Call API to refresh user data
            const response = await fetch('/api/auth/me', {
                headers: {
                    'Authorization': `Bearer ${state.token}`,
                    'Content-Type': 'application/json'
                }
            })

            if (!response.ok) {
                throw new Error('Failed to refresh user data')
            }

            const userData = await response.json()
            
            if (setUser(userData)) {
                console.log('✅ User data refreshed successfully')
                return true
            } else {
                throw new Error('Invalid user data received')
            }

        } catch (error) {
            console.error('❌ Failed to refresh user:', error)
            state.error = 'Failed to refresh user data'
            return false
        } finally {
            state.loading = false
        }
    }

    const checkPermission = (requiredRoles) => {
        if (!state.isAuthenticated || !state.user) {
            return false
        }

        if (!requiredRoles || requiredRoles.length === 0) {
            return true
        }

        if (typeof requiredRoles === 'string') {
            requiredRoles = [requiredRoles]
        }

        return requiredRoles.includes(state.user.vaiTro)
    }

    const clearError = () => {
        state.error = null
    }

    // ===== DEBUG HELPERS =====
    const debug = () => {
        const debugInfo = {
            state: {
                isAuthenticated: state.isAuthenticated,
                hasUser: !!state.user,
                hasToken: !!state.token,
                loading: state.loading,
                error: state.error
            },
            user: state.user ? {
                id: state.user.id,
                email: state.user.email,
                role: state.user.vaiTro
            } : null,
            localStorage: {
                token: !!getFromStorage(AUTH_STORAGE_KEYS.TOKEN),
                user: !!getFromStorage(AUTH_STORAGE_KEYS.USER),
            },
            permissions: {
                isAdmin: isAdmin.value,
                isNhanVien: isNhanVien.value,
                isUser: isUser.value,
                canAccessAdminArea: canAccessAdminArea.value
            }
        }
        
        console.log('🔍 Auth Store Debug:', debugInfo)
        return debugInfo
    }

    // ===== WATCHERS =====
    // Watch for authentication state changes
    watch(
        () => state.isAuthenticated,
        (newValue) => {
            console.log('🔄 Authentication state changed:', newValue)
        }
    )

    return {
        // State
        user,
        token,
        isAuthenticated,
        loading,
        error,
        
        // Computed
        getUserRole,
        isAdmin,
        isNhanVien,
        isUser,
        canAccessAdminArea,
        
        // Actions
        login,
        logout,
        setUser,
        setToken,
        refreshUser,
        checkPermission,
        clearError,
        
        // Utilities
        debug
    }
}

// Auto-initialize when module loads
initializeAuth()

// Export for direct access if needed
export { clearAuthData, DEFAULT_USER_ROLES, initializeAuth }

