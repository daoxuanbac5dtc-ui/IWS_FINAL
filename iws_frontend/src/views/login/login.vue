<template>
    <FloatingConfigurator />
    <div class="login-container">
        <!-- Background với gradient và pattern -->
        <div class="background-overlay"></div>
        
        <!-- Floating sneakers animation -->
        <div class="floating-sneakers">
            <div class="sneaker-icon sneaker-1">👟</div>
            <div class="sneaker-icon sneaker-2">👟</div>
            <div class="sneaker-icon sneaker-3">🥾</div>
            <div class="sneaker-icon sneaker-4">👠</div>
            <div class="sneaker-icon sneaker-5">🥿</div>
        </div>

        <div class="login-wrapper">
            <!-- Left side - Branding & Image -->
            <div class="branding-section">
                <div class="brand-content">
                    <div class="logo-container">
                        <div class="logo-circle">
                            <svg viewBox="0 0 24 24" fill="none" class="logo-svg">
                                <path d="M2 18h20v2H2v-2zm1.5-6.5L12 5l8.5 6.5v8h-17v-8z" fill="currentColor"/>
                                <path d="M12 5l-2 1.5v2l2-1.5 2 1.5v-2L12 5z" fill="white"/>
                            </svg>
                        </div>
                        <h1 class="brand-name">ShoesBees</h1>
                        <p class="brand-tagline">Premium Sneakers Collection</p>
                    </div>
                    
                    <!-- Hero sneaker image placeholder -->
                    <div class="hero-sneaker">
                        <div class="sneaker-showcase">
                            <div class="sneaker-main">👟</div>
                            <div class="sneaker-shadow"></div>
                        </div>
                        <div class="sneaker-details">
                            <h3>Latest Collection</h3>
                            <p>Discover premium sneakers from top brands</p>
                        </div>
                    </div>

                    <!-- Features -->
                    <div class="features">
                        <div class="feature-item">
                            <div class="feature-icon">🚚</div>
                            <span>Free Shipping</span>
                        </div>
                        <div class="feature-item">
                            <div class="feature-icon">✨</div>
                            <span>Authentic Products</span>
                        </div>
                        <div class="feature-item">
                            <div class="feature-icon">🔒</div>
                            <span>Secure Payment</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Right side - Login Form -->
            <div class="form-section">
                <div class="form-container">
                    <div class="form-header">
                        <h2 class="form-title">Welcome Back!</h2>
                        <p class="form-subtitle">Sign in to continue your sneaker journey</p>
                    </div>

                    <!-- Toast Notifications -->
                    <div class="toast-container">
                        <!-- Success Toast -->
                        <transition name="toast" appear>
                            <div v-if="successMessage" class="toast toast-success">
                                <div class="toast-icon">
                                    <i class="pi pi-check-circle"></i>
                                </div>
                                <div class="toast-content">
                                    <h4 class="toast-title">Success!</h4>
                                    <p class="toast-message">{{ successMessage }}</p>
                                </div>
                                <button @click="successMessage = ''" class="toast-close">
                                    <i class="pi pi-times"></i>
                                </button>
                            </div>
                        </transition>

                        <!-- Error Toast -->
                        <transition name="toast" appear>
                            <div v-if="errorMessage" class="toast toast-error">
                                <div class="toast-icon">
                                    <i class="pi pi-exclamation-triangle"></i>
                                </div>
                                <div class="toast-content">
                                    <h4 class="toast-title">Error!</h4>
                                    <p class="toast-message">{{ errorMessage }}</p>
                                </div>
                                <button @click="errorMessage = ''" class="toast-close">
                                    <i class="pi pi-times"></i>
                                </button>
                            </div>
                        </transition>

                        <!-- Loading Toast -->
                        <transition name="toast" appear>
                            <div v-if="isLoading" class="toast toast-loading">
                                <div class="toast-icon">
                                    <div class="loading-spinner"></div>
                                </div>
                                <div class="toast-content">
                                    <h4 class="toast-title">Please wait...</h4>
                                    <p class="toast-message">Signing you in...</p>
                                </div>
                            </div>
                        </transition>
                    </div>

                    <form @submit.prevent="login" class="login-form">
                        <div class="input-group">
                            <label for="email1" class="input-label">
                                Email Address <span class="required">*</span>
                            </label>
                            <div class="input-wrapper">
                                <i class="pi pi-envelope input-icon"></i>
                                <InputText 
                                    id="email1" 
                                    type="text" 
                                    placeholder="Enter your email" 
                                    class="custom-input"
                                    v-model="email" 
                                    :class="{ 'input-error': errorMessage && !email }" 
                                    autocomplete="username" 
                                />
                            </div>
                        </div>

                        <div class="input-group">
                            <label for="password1" class="input-label">
                                Password <span class="required">*</span>
                            </label>
                            <div class="input-wrapper">
                                <i class="pi pi-lock input-icon"></i>
                                <Password
                                    id="password1"
                                    v-model="password"
                                    placeholder="Enter your password"
                                    :toggleMask="true"
                                    class="custom-password"
                                    :pt="{
                                        root: { class: 'w-full' },
                                        input: { class: 'w-full' }
                                    }"
                                    :feedback="false"
                                    :class="{ 'input-error': errorMessage && password.length < 6 }"
                                    autocomplete="current-password"
                                />
                            </div>
                        </div>

                        <div class="form-options">
                            <div class="checkbox-wrapper">
                                <Checkbox v-model="checked" id="rememberme1" binary class="custom-checkbox" />
                                <label for="rememberme1" class="checkbox-label">Remember me</label>
                            </div>
                            <span class="forgot-password" @click="forgotPassword">
                                Forgot password?
                            </span>
                        </div>

                        <Button 
                            type="submit" 
                            :label="isLoading ? 'Signing in...' : 'Sign In'" 
                            class="login-button"
                            :disabled="isLoading" 
                            :loading="isLoading" 
                        />
                    </form>

                    <!-- Register Link -->
                    <div class="register-section">
                        <span class="register-text">Don't have an account? </span>
                        <span class="register-link" @click="goToRegister">
                            Sign up now
                        </span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import FloatingConfigurator from '@/components/FloatingConfigurator.vue';
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

const email = ref('');
const password = ref('');
const checked = ref(false);
const errorMessage = ref('');
const successMessage = ref('');
const isLoading = ref(false);

const router = useRouter();

// Kiểm tra nếu user đã đăng nhập (từ localStorage hoặc session)
onMounted(() => {
    const token = localStorage.getItem('auth_token');
    if (token) {
        // User đã đăng nhập, chuyển hướng
        router.push('/san-pham');
    }
});

const validateForm = () => {
    if (!email.value || !email.value.trim()) {
        errorMessage.value = 'Không để trống email';
        return false;
    }

    if (!isValidEmail(email.value)) {
        errorMessage.value = 'Email không hợp lệ';
        return false;
    }

    if (!password.value || password.value.length < 6) {
        errorMessage.value = 'Mật khẩu phải có ít nhất 6 ký tự';
        return false;
    }

    return true;
};

const isValidEmail = (email) => {
    // Regex cho phép cả .example domain cho demo
    const emailRegex = /^[^\s@]+@[^\s@]+\.(com|net|org|edu|gov|mil|example)$/i;
    return emailRegex.test(email);
};

const login = async () => {
    try {
        isLoading.value = true;
        errorMessage.value = '';
        successMessage.value = '';

        // Validate form
        if (!validateForm()) {
            isLoading.value = false;
            setTimeout(() => {
                errorMessage.value = '';
            }, 4000);
            return;
        }

        console.log('🔄 Attempting login...');

        // Gọi API backend để đăng nhập
        const response = await fetch('http://localhost:8080/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify({
                email: email.value,
                matKhau: password.value
            }),
            credentials: 'include'
        });

        const data = await response.json();
        console.log('📝 Login response:', data);

        if (response.ok && data.success) {
            successMessage.value = 'Login successful! Redirecting...';

            // Auto hide success message and redirect
            setTimeout(() => {
                successMessage.value = '';
            }, 3000);

            // Lưu token
            localStorage.setItem('auth_token', data.data.token);

            // Tạo object user info đầy đủ với thông tin nhân viên
            const userInfo = {
                id: data.data.user.id,
                email: data.data.user.email,
                vaiTro: data.data.user.vaiTro,
                maTaiKhoan: data.data.user.maTaiKhoan,

                // Thêm thông tin nhân viên với nhiều fallback
                tenNhanVien: data.data.user.tenNhanVien || data.data.user.hoTen || data.data.user.hoVaTen || data.data.user.fullName || data.data.user.name || data.data.user.email?.split('@')[0] || 'Nhân viên',

                maNhanVien: data.data.user.maNhanVien || data.data.user.maNV || data.data.user.employeeCode || data.data.user.staffCode || data.data.user.maTaiKhoan || `NV${data.data.user.id}` || 'NV001',

                // Backup fields
                hoTen: data.data.user.hoTen || data.data.user.tenNhanVien || data.data.user.email?.split('@')[0] || 'Nhân viên'
            };

            // Log để debug
            console.log('💾 Saving user info:', userInfo);

            // Lưu thông tin user với key chính xác
            localStorage.setItem('user_info', JSON.stringify(userInfo));

            // Lưu thông tin "Remember me"
            if (checked.value) {
                localStorage.setItem('rememberMe', 'true');
                localStorage.setItem('savedEmail', email.value);
            } else {
                localStorage.removeItem('rememberMe');
                localStorage.removeItem('savedEmail');
            }

            // Verify data đã được lưu
            const savedUserInfo = localStorage.getItem('user_info');
            console.log('✅ Verified saved user info:', JSON.parse(savedUserInfo));

            // Chuyển hướng theo role từ backend
            setTimeout(() => {
                if (data.data.user.vaiTro === 'ADMIN') {
                    router.push('/admin');
                } else if (data.data.user.vaiTro === 'NHANVIEN') {
                    router.push('/admin');
                } else {
                    router.push('/');
                }
            }, 1000);
        } else {
            errorMessage.value = data.message || 'Login failed. Please try again.';
            setTimeout(() => {
                errorMessage.value = '';
            }, 4000);
        }
    } catch (error) {
        console.error('❌ Login error:', error);
        errorMessage.value = 'An error occurred. Please try again.';
        setTimeout(() => {
            errorMessage.value = '';
        }, 4000);
    } finally {
        isLoading.value = false;
    }
};

const forgotPassword = () => {
    router.push('/forgot-password');
};

const goToRegister = () => {
    router.push('/auth/register');
};

// Load saved email nếu có "Remember me"
onMounted(() => {
    if (localStorage.getItem('rememberMe') === 'true') {
        const savedEmail = localStorage.getItem('savedEmail');
        if (savedEmail) {
            email.value = savedEmail;
            checked.value = true;
        }
    }
});
</script>

<style scoped>
.login-container {
    min-height: 100vh;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    overflow: hidden;
}

.background-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
        radial-gradient(circle at 20% 80%, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
        radial-gradient(circle at 80% 20%, rgba(255, 119, 198, 0.3) 0%, transparent 50%);
    z-index: 1;
}

.floating-sneakers {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    pointer-events: none;
    z-index: 2;
}

.sneaker-icon {
    position: absolute;
    font-size: 2rem;
    opacity: 0.1;
    animation: float 6s ease-in-out infinite;
}

.sneaker-1 { top: 10%; left: 10%; animation-delay: 0s; }
.sneaker-2 { top: 20%; right: 10%; animation-delay: 1s; }
.sneaker-3 { top: 60%; left: 5%; animation-delay: 2s; }
.sneaker-4 { top: 70%; right: 20%; animation-delay: 3s; }
.sneaker-5 { top: 30%; left: 80%; animation-delay: 4s; }

@keyframes float {
    0%, 100% { transform: translateY(0px) rotate(0deg); }
    50% { transform: translateY(-20px) rotate(10deg); }
}

.login-wrapper {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(20px);
    border-radius: 24px;
    box-shadow: 0 32px 64px rgba(0, 0, 0, 0.1);
    display: grid;
    grid-template-columns: 1fr 1fr;
    max-width: 1000px;
    width: 90vw;
    min-height: 600px;
    position: relative;
    z-index: 3;
    overflow: hidden;
}

.branding-section {
    background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
    color: white;
    padding: 3rem;
    display: flex;
    flex-direction: column;
    justify-content: center;
    position: relative;
}

.branding-section::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E") repeat;
    opacity: 0.1;
}

.brand-content {
    position: relative;
    z-index: 1;
}

.logo-container {
    text-align: center;
    margin-bottom: 2rem;
}

.logo-circle {
    width: 80px;
    height: 80px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 1rem;
    backdrop-filter: blur(10px);
}

.logo-svg {
    width: 40px;
    height: 40px;
    color: white;
}

.brand-name {
    font-size: 2rem;
    font-weight: 700;
    margin: 0 0 0.5rem;
    background: linear-gradient(45deg, #fff, #e0e7ff);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.brand-tagline {
    font-size: 1rem;
    opacity: 0.9;
    margin: 0;
}

.hero-sneaker {
    text-align: center;
    margin: 2rem 0;
}

.sneaker-showcase {
    position: relative;
    margin-bottom: 1rem;
}

.sneaker-main {
    font-size: 4rem;
    display: block;
    animation: bounce 2s ease-in-out infinite;
}

.sneaker-shadow {
    width: 60px;
    height: 20px;
    background: rgba(0, 0, 0, 0.2);
    border-radius: 50%;
    margin: -10px auto 0;
    transform: scaleX(1.5);
    animation: shadowPulse 2s ease-in-out infinite;
}

@keyframes bounce {
    0%, 100% { transform: translateY(0); }
    50% { transform: translateY(-10px); }
}

@keyframes shadowPulse {
    0%, 100% { transform: scaleX(1.5) scaleY(1); opacity: 0.2; }
    50% { transform: scaleX(1.8) scaleY(0.8); opacity: 0.3; }
}

.sneaker-details h3 {
    font-size: 1.25rem;
    margin: 0 0 0.5rem;
    font-weight: 600;
}

.sneaker-details p {
    font-size: 0.9rem;
    opacity: 0.8;
    margin: 0;
}

.features {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    margin-top: 2rem;
}

.feature-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    font-size: 0.9rem;
}

.feature-icon {
    font-size: 1.25rem;
}

.form-section {
    padding: 3rem;
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.form-container {
    max-width: 400px;
    margin: 0 auto;
    width: 100%;
}

.form-header {
    text-align: center;
    margin-bottom: 2rem;
}

.form-title {
    font-size: 2rem;
    font-weight: 700;
    color: #1e293b;
    margin: 0 0 0.5rem;
}

.form-subtitle {
    color: #64748b;
    margin: 0;
    font-size: 0.95rem;
}

/* Toast Notifications */
.toast-container {
    position: fixed;
    top: 20px;
    right: 20px;
    z-index: 9999;
    display: flex;
    flex-direction: column;
    gap: 10px;
    pointer-events: none;
}

.toast {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 16px 20px;
    border-radius: 12px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    min-width: 320px;
    max-width: 400px;
    pointer-events: auto;
    position: relative;
    overflow: hidden;
}

.toast::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    animation: progressBar 4s linear forwards;
}

.toast-success {
    background: linear-gradient(135deg, rgba(34, 197, 94, 0.95) 0%, rgba(21, 128, 61, 0.95) 100%);
    color: white;
}

.toast-success::before {
    background: rgba(255, 255, 255, 0.3);
}

.toast-error {
    background: linear-gradient(135deg, rgba(239, 68, 68, 0.95) 0%, rgba(185, 28, 28, 0.95) 100%);
    color: white;
}

.toast-error::before {
    background: rgba(255, 255, 255, 0.3);
}

.toast-loading {
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.95) 0%, rgba(37, 99, 235, 0.95) 100%);
    color: white;
}

.toast-icon {
    flex-shrink: 0;
    font-size: 20px;
    margin-top: 2px;
}

.loading-spinner {
    width: 20px;
    height: 20px;
    border: 2px solid rgba(255, 255, 255, 0.3);
    border-top: 2px solid white;
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

.toast-content {
    flex: 1;
}

.toast-title {
    font-size: 14px;
    font-weight: 600;
    margin: 0 0 4px 0;
    line-height: 1.2;
}

.toast-message {
    font-size: 13px;
    margin: 0;
    opacity: 0.95;
    line-height: 1.3;
}

.toast-close {
    background: none;
    border: none;
    color: currentColor;
    cursor: pointer;
    font-size: 14px;
    padding: 4px;
    border-radius: 4px;
    opacity: 0.7;
    transition: opacity 0.2s ease;
    margin-top: -2px;
}

.toast-close:hover {
    opacity: 1;
    background: rgba(255, 255, 255, 0.1);
}

@keyframes progressBar {
    0% { width: 100%; }
    100% { width: 0%; }
}

/* Toast Transitions */
.toast-enter-active, .toast-leave-active {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.toast-enter-from {
    opacity: 0;
    transform: translateX(100%) scale(0.95);
}

.toast-leave-to {
    opacity: 0;
    transform: translateX(100%) scale(0.95);
}

.toast-enter-to, .toast-leave-from {
    opacity: 1;
    transform: translateX(0) scale(1);
}

.login-form {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.input-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.input-label {
    font-weight: 500;
    color: #374151;
    font-size: 0.9rem;
}

.required {
    color: #ef4444;
}

.input-wrapper {
    position: relative;
}

.input-icon {
    position: absolute;
    left: 1rem;
    top: 50%;
    transform: translateY(-50%);
    color: #9ca3af;
    z-index: 1;
}

.custom-input, .custom-password {
    width: 100% !important;
    padding: 0.875rem 1rem 0.875rem 3rem !important;
    border-radius: 12px !important;
    border: 2px solid #e5e7eb !important;
    font-size: 0.95rem !important;
    transition: all 0.2s ease !important;
    height: 50px !important;
    box-sizing: border-box !important;
}

/* Đảm bảo password input có cùng kích thước */
.custom-password input {
    width: 100% !important;
    padding: 0.875rem 3rem 0.875rem 3rem !important;
    border: none !important;
    background: transparent !important;
    font-size: 0.95rem !important;
    height: 100% !important;
    box-sizing: border-box !important;
}

/* Wrapper của password để match với email input */
.custom-password {
    display: flex !important;
    align-items: center !important;
    position: relative !important;
}

.custom-input:focus, .custom-password:focus {
    border-color: #4f46e5 !important;
    box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1) !important;
    outline: none !important;
}

.custom-password:focus-within {
    border-color: #4f46e5 !important;
    box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1) !important;
}

.custom-password input:focus {
    outline: none !important;
}

.input-error {
    border-color: #ef4444 !important;
}

.input-error:focus, .input-error:focus-within {
    border-color: #ef4444 !important;
    box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1) !important;
}

.input-error input:focus {
    outline: none !important;
}

.form-options {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: -0.5rem;
}

.checkbox-wrapper {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.checkbox-label {
    font-size: 0.9rem;
    color: #4b5563;
    cursor: pointer;
}

.forgot-password {
    font-size: 0.9rem;
    color: #4f46e5;
    cursor: pointer;
    font-weight: 500;
}

.forgot-password:hover {
    text-decoration: underline;
}

.login-button {
    background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%) !important;
    border: none !important;
    border-radius: 12px !important;
    padding: 0.875rem 1.5rem !important;
    font-weight: 600 !important;
    font-size: 1rem !important;
    transition: all 0.2s ease !important;
    box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3) !important;
}

.login-button:hover {
    transform: translateY(-2px) !important;
    box-shadow: 0 8px 24px rgba(79, 70, 229, 0.4) !important;
}

.login-button:active {
    transform: translateY(0) !important;
}

.demo-section {
    margin-top: 2rem;
}

.divider {
    text-align: center;
    margin-bottom: 1rem;
    position: relative;
}

.divider::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 0;
    right: 0;
    height: 1px;
    background: #e5e7eb;
}

.divider span {
    background: white;
    padding: 0 1rem;
    font-size: 0.85rem;
    color: #9ca3af;
}

.demo-buttons {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 0.75rem;
}

.demo-btn {
    padding: 0.75rem 1rem;
    border: 2px solid;
    border-radius: 10px;
    font-size: 0.85rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
}

.demo-admin {
    border-color: #fbbf24;
    color: #92400e;
    background: #fef3c7;
}

.demo-admin:hover {
    background: #fde68a;
    transform: translateY(-2px);
}

.demo-user {
    border-color: #34d399;
    color: #065f46;
    background: #d1fae5;
}

.demo-user:hover {
    background: #a7f3d0;
    transform: translateY(-2px);
}

.register-section {
    text-align: center;
    margin-top: 2rem;
    padding-top: 2rem;
    border-top: 1px solid #e5e7eb;
}

.register-text {
    color: #6b7280;
    font-size: 0.9rem;
}

.register-link {
    color: #4f46e5;
    font-weight: 600;
    cursor: pointer;
    font-size: 0.9rem;
}

.register-link:hover {
    text-decoration: underline;
}

/* Responsive Design */
@media (max-width: 768px) {
    .login-wrapper {
        grid-template-columns: 1fr;
        max-width: 400px;
    }
    
    .branding-section {
        display: none;
    }
    
    .form-section {
        padding: 2rem 1.5rem;
    }
    
    .floating-sneakers {
        display: none;
    }
    
    .toast-container {
        top: 10px;
        right: 10px;
        left: 10px;
    }
    
    .toast {
        min-width: auto;
        max-width: none;
    }
}

/* Dark mode support */
@media (prefers-color-scheme: dark) {
    .login-wrapper {
        background: rgba(15, 23, 42, 0.95);
    }
    
    .form-title {
        color: #f1f5f9;
    }
    
    .form-subtitle {
        color: #94a3b8;
    }
    
    .input-label {
        color: #e2e8f0;
    }
    
    .checkbox-label {
        color: #cbd5e1;
    }
    
    .register-text {
        color: #94a3b8;
    }
    
    .custom-input, .custom-password {
        background: #1e293b !important;
        border-color: #475569 !important;
        color: #f1f5f9 !important;
    }
    
    .custom-password input {
        background: transparent !important;
        color: #f1f5f9 !important;
    }
    
    .custom-input:focus, .custom-password:focus-within {
        background: #1e293b !important;
    }
    
    .divider::before {
        background: #475569;
    }
    
    .divider span {
        background: #0f172a;
        color: #64748b;
    }
}
</style>