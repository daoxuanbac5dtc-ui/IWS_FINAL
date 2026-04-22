// Debug utility for login issues
export const debugLogin = {
    /**
     * Test login with debug information
     */
    async testLogin(email, password) {
        console.log('🔍 DEBUG LOGIN TEST');
        console.log('📧 Email:', email);
        console.log('🔐 Password:', password);
        console.log('🌐 API Endpoint: http://localhost:8080/auth/login');
        
        try {
            const response = await fetch('http://localhost:8080/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: JSON.stringify({
                    email: email,
                    matKhau: password
                }),
                credentials: 'include'
            });
            
            console.log('📡 Response Status:', response.status);
            console.log('📡 Response Headers:', Object.fromEntries(response.headers.entries()));
            
            const data = await response.json();
            console.log('📝 Response Data:', data);
            
            if (response.ok && data.success) {
                console.log('✅ Login successful!');
                return { success: true, data: data.data };
            } else {
                console.log('❌ Login failed:', data.message);
                return { success: false, error: data.message };
            }
        } catch (error) {
            console.error('💥 Network error:', error);
            return { success: false, error: error.message };
        }
    },
    
    /**
     * Check if backend is running
     */
    async checkBackendHealth() {
        try {
            const response = await fetch('http://localhost:8080/auth/login', {
                method: 'OPTIONS',
                headers: {
                    'Access-Control-Request-Method': 'POST',
                    'Access-Control-Request-Headers': 'Content-Type'
                }
            });
            
            console.log('🏥 Backend health check:', response.status === 200 ? 'OK' : 'FAILED');
            return response.status === 200;
        } catch (error) {
            console.error('💥 Backend not reachable:', error);
            return false;
        }
    },
    
    /**
     * Clear all auth data
     */
    clearAuthData() {
        localStorage.removeItem('auth_token');
        localStorage.removeItem('user_info');
        localStorage.removeItem('rememberMe');
        localStorage.removeItem('savedEmail');
        console.log('🧹 Cleared all auth data');
    }
};

