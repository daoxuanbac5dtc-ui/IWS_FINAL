<template>
    <FloatingConfigurator />
    <div class="login-container">
        <!-- Background -->
        <div class="background">
            <div class="bg-gradient"></div>
            <div class="bg-pattern"></div>
        </div>

        <!-- Main Card -->
        <div class="login-card">
            <!-- Left Side - Branding -->
            <div class="brand-side">
                <div class="brand-content">
                    <!-- Logo -->
                    <div class="logo-area">
                        <div class="logo">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                                <polyline points="9,22 9,12 15,12 15,22"/>
                            </svg>
                        </div>
                        <h1 class="brand-name">ShoesBees</h1>
                        <p class="brand-tagline">Premium Sneaker Experience</p>
                    </div>

                    <!-- Hero Image -->
                    <div class="hero-section">
                        <div class="shoe-display">
                            <div class="shoe-icon">👟</div>
                        </div>
                        <h3 class="hero-title">New Arrivals</h3>
                        <p class="hero-text">Discover the latest trends in premium footwear</p>
                    </div>

                    <!-- Features -->
                    <div class="features">
                        <div class="feature">
                            <span class="feature-icon">🚀</span>
                            <span class="feature-text">Fast Delivery</span>
                        </div>
                        <div class="feature">
                            <span class="feature-icon">💎</span>
                            <span class="feature-text">Premium Quality</span>
                        </div>
                        <div class="feature">
                            <span class="feature-icon">🛡️</span>
                            <span class="feature-text">Secure Shopping</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Right Side - Form -->
            <div class="form-side">
                <div class="form-content">
                    <!-- Header -->
                    <div class="form-header">
                        <div class="header-line"></div>
                        <h2 class="form-title">Welcome Back</h2>
                        <p class="form-subtitle">Please sign in to your account to continue</p>
                    </div>

                    <!-- Form -->
                    <form @submit.prevent="login" class="auth-form">
                        <!-- Email -->
                        <div class="field">
                            <label class="field-label">
                                Email Address <span class="required">*</span>
                            </label>
                            <div class="input-wrapper">
                                <i class="input-icon pi pi-envelope"></i>
                                <InputText
                                    v-model="email"
                                    type="email"
                                    placeholder="Enter your email address"
                                    class="field-input"
                                    :class="{ 'error': errorMessage && !email }"
                                    autocomplete="username"
                                />
                            </div>
                        </div>

                        <!-- Password -->
                        <div class="field">
                            <label class="field-label">
                                Password <span class="required">*</span>
                            </label>
                            <div class="input-wrapper">
                                <i class="input-icon pi pi-lock"></i>
                                <Password
                                    v-model="password"
                                    placeholder="Enter your password"
                                    :toggleMask="true"
                                    class="field-password"
                                    :class="{ 'error': errorMessage && password.length < 6 }"
                                    :feedback="false"
                                    autocomplete="current-password"
                                    :pt="{
                                        root: { class: 'w-full' },
                                        input: { class: 'w-full pl-10 pr-10' }
                                    }"
                                />
                            </div>
                        </div>

                        <!-- Options -->
                        <div class="form-options">
                            <label class="checkbox-wrapper">
                                <Checkbox
                                    v-model="checked"
                                    binary
                                    class="checkbox"
                                />
                                <span class="checkbox-text">Remember me</span>
                            </label>
                            <button type="button" class="forgot-btn" @click="forgotPassword">
                                Forgot Password?
                            </button>
                        </div>

                        <!-- Submit -->
                        <Button
                            type="submit"
                            class="submit-btn"
                            :disabled="isLoading"
                            :loading="isLoading"
                        >
                            <template v-if="!isLoading">
                                <span>Log In</span>
                                <i class="pi pi-arrow-right ml-2"></i>
                            </template>
                            <span v-else>Logging in...</span>
                        </Button>
                    </form>

                    <!-- Footer -->
                    <div class="form-footer">
                        <div class="divider">
                            <span>or</span>
                        </div>
                        <p class="signup-text">
                            Don't have an account?
                            <button type="button" class="signup-btn" @click="goToRegister">
                                Create one now
                            </button>
                        </p>
                    </div>
                </div>
            </div>
        </div>

        <!-- Notifications -->
        <div class="toast-area">
            <!-- Success -->
            <transition name="toast">
                <div v-if="successMessage" class="toast toast-success">
                    <i class="pi pi-check-circle"></i>
                    <div class="toast-content">
                        <strong>Success!</strong>
                        <span>{{ successMessage }}</span>
                    </div>
                    <button @click="successMessage = ''" class="toast-close">
                        <i class="pi pi-times"></i>
                    </button>
                </div>
            </transition>

            <!-- Error -->
            <transition name="toast">
                <div v-if="errorMessage" class="toast toast-error">
                    <i class="pi pi-exclamation-triangle"></i>
                    <div class="toast-content">
                        <strong>Error!</strong>
                        <span>{{ errorMessage }}</span>
                    </div>
                    <button @click="errorMessage = ''" class="toast-close">
                        <i class="pi pi-times"></i>
                    </button>
                </div>
            </transition>

            <!-- Loading -->
            <transition name="toast">
                <div v-if="isLoading" class="toast toast-loading">
                    <div class="loading-spinner"></div>
                    <div class="toast-content">
                        <strong>Please wait...</strong>
                        <span>Logging you in...</span>
                    </div>
                </div>
            </transition>
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

            // Chuyển hướng ngay lập tức sau khi lưu thành công
            setTimeout(() => {
                successMessage.value = '';
                if (data.data.user.vaiTro === 'ADMIN') {
                    router.push('/admin');
                } else if (data.data.user.vaiTro === 'NHANVIEN') {
                    router.push('/admin');
                } else {
                    router.push('/');
                }
            }, 300); // Giảm từ 1000ms xuống 300ms

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
    router.push('/auth/forgot-password');
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
/* Base Styles */
.login-container {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    padding: 1rem;
    font-family: 'Inter', system-ui, sans-serif;
    /* Fallback background */
    background: #4f46e5;
    /* Primary gradient background */
    background: linear-gradient(135deg,
        #4f46e5 0%,
        #7c3aed 25%,
        #ec4899 50%,
        #f59e0b 75%,
        #10b981 100%);
    background-size: 400% 400%;
    animation: gradientMove 15s ease infinite;
}

/* Fallback for older browsers */
@supports not (backdrop-filter: blur(25px)) {
    .login-card {
        background: rgba(255, 255, 255, 0.95) !important;
    }

    .brand-side {
        background: linear-gradient(135deg,
            #4f46e5 0%,
            #7c3aed 25%,
            #ec4899 50%) !important;
    }

    .form-side {
        background: rgba(255, 255, 255, 0.98) !important;
    }
}

/* Background */
.background {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 1;
}

.bg-gradient {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg,
        #4f46e5 0%,
        #7c3aed 25%,
        #ec4899 50%,
        #f59e0b 75%,
        #10b981 100%);
    background-size: 400% 400%;
    animation: gradientMove 15s ease infinite;
}

@keyframes gradientMove {
    0% { background-position: 0% 50%; }
    50% { background-position: 100% 50%; }
    100% { background-position: 0% 50%; }
}

.bg-pattern {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url("data:image/svg+xml,%3Csvg width='40' height='40' viewBox='0 0 40 40' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%23ffffff' fill-opacity='0.1'%3E%3Ccircle cx='20' cy='20' r='2'/%3E%3C/g%3E%3C/svg%3E") repeat;
    animation: patternFloat 20s ease-in-out infinite;
}

@keyframes patternFloat {
    0%, 100% { transform: translateY(0px); }
    50% { transform: translateY(-10px); }
}

/* Main Card */
.login-card {
    position: relative;
    z-index: 10;
    display: grid;
    grid-template-columns: 1fr 1fr;
    max-width: 1100px;
    width: 100%;
    min-height: 650px;
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(25px);
    border-radius: 24px;
    border: 1px solid rgba(255, 255, 255, 0.3);
    box-shadow:
        0 25px 50px rgba(0, 0, 0, 0.25),
        inset 0 1px 0 rgba(255, 255, 255, 0.3),
        0 0 0 1px rgba(255, 255, 255, 0.1);
    overflow: hidden;
}

/* Brand Side */
.brand-side {
    background: linear-gradient(135deg,
        rgba(79, 70, 229, 0.9) 0%,
        rgba(124, 58, 237, 0.9) 25%,
        rgba(236, 72, 153, 0.9) 50%,
        rgba(245, 158, 11, 0.9) 75%,
        rgba(16, 185, 129, 0.9) 100%);
    backdrop-filter: blur(20px);
    padding: 3rem;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    text-align: center;
    position: relative;
    overflow: hidden;
}

.brand-side::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(
        circle,
        rgba(255, 255, 255, 0.1) 0%,
        transparent 50%
    );
    animation: rotate 20s linear infinite;
    pointer-events: none;
}

@keyframes rotate {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

.brand-content {
    max-width: 300px;
}

.logo-area {
    margin-bottom: 3rem;
}

.logo {
    width: 80px;
    height: 80px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 1.5rem;
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.3);
}

.logo svg {
    width: 40px;
    height: 40px;
    color: white;
}

.brand-name {
    font-size: 2.5rem;
    font-weight: 700;
    margin: 0 0 0.5rem;
    background: linear-gradient(135deg, #ffffff 0%, #f1f5f9 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.brand-tagline {
    font-size: 1rem;
    opacity: 0.9;
    margin: 0;
}

.hero-section {
    margin: 2rem 0;
}

.shoe-display {
    margin-bottom: 1.5rem;
}

.shoe-icon {
    font-size: 4rem;
    animation: float 3s ease-in-out infinite;
    filter: drop-shadow(0 10px 20px rgba(0, 0, 0, 0.2));
}

@keyframes float {
    0%, 100% { transform: translateY(0px); }
    50% { transform: translateY(-10px); }
}

.hero-title {
    font-size: 1.5rem;
    font-weight: 600;
    margin: 0 0 0.5rem;
}

.hero-text {
    font-size: 1rem;
    opacity: 0.8;
    margin: 0;
    line-height: 1.5;
}

.features {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    margin-top: 2rem;
}

.feature {
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 0.75rem 1rem;
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(10px);
    border-radius: 12px;
    border: 1px solid rgba(255, 255, 255, 0.2);
    transition: all 0.3s ease;
}

.feature:hover {
    background: rgba(255, 255, 255, 0.15);
    transform: translateX(5px);
}

.feature-icon {
    font-size: 1.25rem;
}

.feature-text {
    font-size: 0.9rem;
}

/* Form Side */
.form-side {
    background: rgba(255, 255, 255, 0.98);
    backdrop-filter: blur(25px);
    padding: 3rem;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
}

.form-side::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg,
        rgba(255, 255, 255, 0.9) 0%,
        rgba(248, 250, 252, 0.95) 100%);
    z-index: 1;
}

.form-content {
    width: 100%;
    max-width: 400px;
    position: relative;
    z-index: 2;
}

.form-header {
    text-align: center;
    margin-bottom: 2rem;
}

.header-line {
    width: 50px;
    height: 4px;
    background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 50%, #ec4899 100%);
    border-radius: 2px;
    margin: 0 auto 1.5rem;
}

.form-title {
    font-size: 2rem;
    font-weight: 700;
    color: #1a202c;
    margin: 0 0 0.5rem;
}

.form-subtitle {
    color: #64748b;
    font-size: 1rem;
    margin: 0;
}

/* Form */
.auth-form {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.field {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.field-label {
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
    z-index: 2;
}

.field-input,
.field-password {
    width: 100% !important;
    padding: 0.875rem 1rem 0.875rem 2.75rem !important;
    border: 2px solid #e5e7eb !important;
    border-radius: 12px !important;
    font-size: 1rem !important;
    background: rgba(255, 255, 255, 0.8) !important;
    backdrop-filter: blur(10px) !important;
    transition: all 0.2s ease !important;
    height: 50px !important;
    box-sizing: border-box !important;
}

.field-password input {
    padding-left: 2.75rem !important;
    padding-right: 2.75rem !important;
}

.field-input:focus,
.field-password:focus-within {
    border-color: #4f46e5 !important;
    box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.15) !important;
    outline: none !important;
    background: rgba(255, 255, 255, 0.98) !important;
}

.field-input.error,
.field-password.error {
    border-color: #ef4444 !important;
    box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1) !important;
}

.form-options {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 0.5rem 0;
}

.checkbox-wrapper {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    cursor: pointer;
}

.checkbox-text {
    font-size: 0.9rem;
    color: #374151;
}

.forgot-btn {
    background: none;
    border: none;
    color: #667eea;
    font-size: 0.9rem;
    font-weight: 500;
    cursor: pointer;
    padding: 0;
    transition: color 0.2s ease;
}

.forgot-btn:hover {
    color: #5a67d8;
    text-decoration: underline;
}

.submit-btn {
    background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 50%, #ec4899 100%) !important;
    border: none !important;
    border-radius: 12px !important;
    padding: 0.875rem 1.5rem !important;
    font-size: 1rem !important;
    font-weight: 600 !important;
    color: white !important;
    transition: all 0.3s ease !important;
    height: 50px !important;
    box-shadow: 0 8px 32px rgba(79, 70, 229, 0.4) !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    gap: 0.5rem !important;
    position: relative !important;
    overflow: hidden !important;
}

.submit-btn::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    transition: left 0.5s ease;
}

.submit-btn:hover {
    transform: translateY(-3px) !important;
    box-shadow: 0 12px 40px rgba(79, 70, 229, 0.5) !important;
}

.submit-btn:hover::before {
    left: 100%;
}

.submit-btn:active {
    transform: translateY(-1px) !important;
}

.form-footer {
    margin-top: 2rem;
    text-align: center;
}

.divider {
    position: relative;
    margin: 1.5rem 0;
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
    background: rgba(255, 255, 255, 0.95);
    padding: 0 1rem;
    color: #64748b;
    font-size: 0.9rem;
    position: relative;
}

.signup-text {
    color: #64748b;
    font-size: 0.9rem;
    margin: 0;
}

.signup-btn {
    background: none;
    border: none;
    color: #667eea;
    font-weight: 600;
    cursor: pointer;
    font-size: 0.9rem;
    padding: 0;
    transition: color 0.2s ease;
}

.signup-btn:hover {
    color: #5a67d8;
    text-decoration: underline;
}

/* Toast Notifications */
.toast-area {
    position: fixed;
    top: 1rem;
    right: 1rem;
    z-index: 1000;
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.toast {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 1rem 1.25rem;
    border-radius: 12px;
    background: white;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
    border: 1px solid #e5e7eb;
    min-width: 300px;
    max-width: 400px;
}

.toast-success {
    border-left: 4px solid #10b981;
    color: #065f46;
}

.toast-error {
    border-left: 4px solid #ef4444;
    color: #7f1d1d;
}

.toast-loading {
    border-left: 4px solid #3b82f6;
    color: #1e3a8a;
}

.toast-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
}

.toast-content strong {
    font-size: 0.9rem;
    font-weight: 600;
}

.toast-content span {
    font-size: 0.85rem;
    opacity: 0.8;
}

.toast-close {
    background: none;
    border: none;
    color: currentColor;
    cursor: pointer;
    padding: 0.25rem;
    border-radius: 4px;
    opacity: 0.6;
    transition: opacity 0.2s ease;
}

.toast-close:hover {
    opacity: 1;
}

.loading-spinner {
    width: 20px;
    height: 20px;
    border: 2px solid #e5e7eb;
    border-top: 2px solid #3b82f6;
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

/* Toast Transitions */
.toast-enter-active,
.toast-leave-active {
    transition: all 0.3s ease;
}

.toast-enter-from {
    opacity: 0;
    transform: translateX(100%);
}

.toast-leave-to {
    opacity: 0;
    transform: translateX(100%);
}

/* Responsive */
@media (max-width: 768px) {
    .login-card {
        grid-template-columns: 1fr;
        max-width: 500px;
        min-height: auto;
        background: rgba(255, 255, 255, 0.95);
    }

    .brand-side {
        display: none;
    }

    .form-side {
        padding: 2rem 1.5rem;
        border-radius: 24px;
        background: rgba(255, 255, 255, 0.98);
        backdrop-filter: blur(25px);
    }

    .form-side::before {
        background: linear-gradient(135deg,
            rgba(255, 255, 255, 0.95) 0%,
            rgba(248, 250, 252, 0.98) 100%);
    }

    .toast-area {
        top: 0.5rem;
        right: 0.5rem;
        left: 0.5rem;
    }

    .toast {
        min-width: auto;
        max-width: none;
    }

    /* Đảm bảo background vẫn hiển thị trên mobile */
    .login-container {
        background: linear-gradient(135deg,
            #4f46e5 0%,
            #7c3aed 25%,
            #ec4899 50%,
            #f59e0b 75%,
            #10b981 100%);
    }
}

@media (max-width: 480px) {
    .login-container {
        padding: 0.5rem;
        background: linear-gradient(135deg,
            #4f46e5 0%,
            #7c3aed 25%,
            #ec4899 50%,
            #f59e0b 75%,
            #10b981 100%) !important;
    }

    .form-side {
        padding: 1.5rem 1rem;
        background: rgba(255, 255, 255, 0.98) !important;
        backdrop-filter: blur(25px) !important;
    }

    .form-title {
        font-size: 1.75rem;
    }

    .login-card {
        background: rgba(255, 255, 255, 0.98) !important;
        backdrop-filter: blur(25px) !important;
    }
}

/* Dark Mode */
@media (prefers-color-scheme: dark) {
    .login-container {
        background: linear-gradient(135deg,
            #1e1b4b 0%,
            #581c87 25%,
            #831843 50%,
            #92400e 75%,
            #064e3b 100%) !important;
    }

    .form-side {
        background: rgba(15, 23, 42, 0.95) !important;
    }

    .form-side::before {
        background: linear-gradient(135deg,
            rgba(15, 23, 42, 0.9) 0%,
            rgba(30, 41, 59, 0.95) 100%) !important;
    }

    .form-title {
        color: #f1f5f9;
    }

    .form-subtitle {
        color: #94a3b8;
    }

    .field-label {
        color: #e2e8f0;
    }

    .checkbox-text {
        color: #cbd5e1;
    }

    .signup-text {
        color: #94a3b8;
    }

    .field-input,
    .field-password {
        background: rgba(30, 41, 59, 0.8) !important;
        border-color: #475569 !important;
        color: #f1f5f9 !important;
    }

    .field-input:focus,
    .field-password:focus-within {
        background: rgba(30, 41, 59, 0.95) !important;
        border-color: #4f46e5 !important;
    }

    .divider::before {
        background: #475569;
    }

    .divider span {
        background: rgba(15, 23, 42, 0.95);
        color: #64748b;
    }
}
</style>
