<template>
    <FloatingConfigurator />
    <div class="forgot-password-container">
        <!-- Nền -->
        <div class="background">
            <div class="bg-gradient"></div>
            <div class="bg-pattern"></div>
        </div>

        <div class="forgot-password-card">
            <!-- Bên trái -->
            <div class="brand-side">
                <div class="brand-content">
                    <div class="logo-area">
                        <div class="logo">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M12 15l2-2-2-2m-3 4l-2-2 2-2"/>
                                <circle cx="12" cy="12" r="10"/>
                            </svg>
                        </div>
                        <h1 class="brand-name">ShoesBees</h1>
                        <p class="brand-tagline">Khôi phục mật khẩu</p>
                    </div>

                    <div class="hero-section">
                        <div class="hero-icon">🔐</div>
                        <h3 class="hero-title">Bảo mật an toàn</h3>
                        <p class="hero-text">Chúng tôi sẽ giúp bạn lấy lại mật khẩu</p>
                    </div>

                    <!-- Tiến trình -->
                    <div class="steps">
                        <div class="step" :class="{ active: currentStep >= 1 }">
                            <div class="step-number">1</div>
                            <span class="step-text">Nhập Email</span>
                        </div>
                        <div class="step" :class="{ active: currentStep >= 2 }">
                            <div class="step-number">2</div>
                            <span class="step-text">OTP & Mật khẩu mới</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Bên phải -->
            <div class="form-side">
                <div class="form-content">
                    <!-- Bước 1 -->
                    <div v-if="currentStep === 1" class="step-container">
                        <div class="form-header">
                            <h2 class="form-title">Quên mật khẩu?</h2>
                            <p class="form-subtitle">Nhập email để nhận mã OTP</p>
                        </div>

                        <form @submit.prevent="sendOTP" class="auth-form">
                            <div class="field">
                                <label class="field-label">Email <span class="required">*</span></label>
                                <div class="input-wrapper">
                                    <i class="input-icon pi pi-envelope"></i>
                                    <InputText
                                        v-model="email"
                                        type="email"
                                        placeholder="Nhập email"
                                        class="field-input"
                                        :class="{ 'error': errorMessage && !email }"
                                        autocomplete="username"
                                    />
                                </div>
                            </div>

                            <Button
                                type="submit"
                                class="submit-btn"
                                :disabled="isLoading"
                                :loading="isLoading"
                            >
                                <template v-if="!isLoading">
                                    <span>Gửi OTP</span>
                                    <i class="pi pi-send ml-2"></i>
                                </template>
                                <span v-else>Đang gửi...</span>
                            </Button>
                        </form>

                        <div class="form-footer">
                            <p class="back-text">
                                Nhớ mật khẩu?
                                <button type="button" class="back-btn" @click="goToLogin">
                                    Đăng nhập
                                </button>
                            </p>
                        </div>
                    </div>

                    <!-- Bước 2 -->
                    <div v-if="currentStep === 2" class="step-container">
                        <div class="form-header">
                            <h2 class="form-title">Nhập OTP & mật khẩu mới</h2>
                            <p class="form-subtitle">Kiểm tra email để lấy mã OTP</p>
                        </div>

                        <form @submit.prevent="resetPassword" class="auth-form">
                            <div class="field">
                                <label class="field-label">Mã OTP <span class="required">*</span></label>
                                <div class="input-wrapper">
                                    <i class="input-icon pi pi-key"></i>
                                    <InputText
                                        v-model="otp"
                                        type="text"
                                        placeholder="Nhập OTP 6 số"
                                        maxlength="6"
                                        class="field-input otp-input"
                                        :class="{ 'error': errorMessage && otp.length < 6 }"
                                    />
                                </div>
                            </div>

                            <div class="field">
                                <label class="field-label">Mật khẩu mới <span class="required">*</span></label>
                                <div class="input-wrapper">
                                    <i class="input-icon pi pi-lock"></i>
                                    <Password
                                        v-model="newPassword"
                                        placeholder="Nhập mật khẩu mới"
                                        :toggleMask="true"
                                        class="field-password"
                                        :class="{ 'error': errorMessage && newPassword.length < 6 }"
                                        :feedback="true"
                                    />
                                </div>
                            </div>

                            <div class="field">
                                <label class="field-label">Xác nhận mật khẩu <span class="required">*</span></label>
                                <div class="input-wrapper">
                                    <i class="input-icon pi pi-lock"></i>
                                    <Password
                                        v-model="confirmPassword"
                                        placeholder="Nhập lại mật khẩu"
                                        :toggleMask="true"
                                        class="field-password"
                                        :class="{ 'error': errorMessage && (confirmPassword.length < 6 || newPassword !== confirmPassword) }"
                                        :feedback="false"
                                    />
                                </div>
                            </div>

                            <Button
                                type="submit"
                                class="submit-btn"
                                :disabled="isLoading || otp.length < 6 || newPassword.length < 6 || newPassword !== confirmPassword"
                                :loading="isLoading"
                            >
                                <template v-if="!isLoading">
                                    <span>Đổi mật khẩu</span>
                                    <i class="pi pi-check ml-2"></i>
                                </template>
                                <span v-else>Đang xử lý...</span>
                            </Button>
                        </form>

                        <div class="form-footer">
                            <p class="back-text">
                                <button type="button" class="back-btn" @click="currentStep = 1">
                                    Quay lại nhập Email
                                </button>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Thông báo -->
        <div class="toast-area">
            <transition name="toast">
                <div v-if="successMessage" class="toast toast-success">
                    <i class="pi pi-check-circle"></i>
                    <div class="toast-content">
                        <strong>Thành công!</strong>
                        <span>{{ successMessage }}</span>
                    </div>
                    <button @click="successMessage = ''" class="toast-close">
                        <i class="pi pi-times"></i>
                    </button>
                </div>
            </transition>

            <transition name="toast">
                <div v-if="errorMessage" class="toast toast-error">
                    <i class="pi pi-exclamation-triangle"></i>
                    <div class="toast-content">
                        <strong>Lỗi!</strong>
                        <span>{{ errorMessage }}</span>
                    </div>
                    <button @click="errorMessage = ''" class="toast-close">
                        <i class="pi pi-times"></i>
                    </button>
                </div>
            </transition>

            <transition name="toast">
                <div v-if="isLoading" class="toast toast-loading">
                    <div class="loading-spinner"></div>
                    <div class="toast-content">
                        <strong>Vui lòng chờ...</strong>
                        <span>{{ loadingMessage }}</span>
                    </div>
                </div>
            </transition>
        </div>
    </div>
</template>

<script setup>
import FloatingConfigurator from '@/components/FloatingConfigurator.vue';
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// State
const currentStep = ref(1);
const email = ref('');
const otp = ref('');
const newPassword = ref('');
const confirmPassword = ref('');
const isLoading = ref(false);
const loadingMessage = ref('');
const successMessage = ref('');
const errorMessage = ref('');

// Step 1: Send OTP
const sendOTP = async () => {
    if (!email.value) {
        showError('Vui lòng nhập email');
        return;
    }

    if (!isValidEmail(email.value)) {
        showError('Email không hợp lệ');
        return;
    }

    isLoading.value = true;
    loadingMessage.value = 'Đang gửi OTP...';

    try {
        const response = await fetch('http://localhost:8080/auth/forgot-password/send-code', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: email.value })
        });

        const data = await response.json();

        if (data.success) {
            currentStep.value = 2;
            showSuccess('OTP đã được gửi đến email của bạn');

            // Nếu là demo mode, hiển thị OTP
            if (data.data && data.data.demo_otp) {
                console.log('Demo OTP:', data.data.demo_otp);
                showSuccess(`OTP (Demo): ${data.data.demo_otp}`);
            }
        } else {
            showError(data.message);
        }
    } catch (error) {
        showError('Lỗi kết nối server');
    } finally {
        isLoading.value = false;
        loadingMessage.value = '';
    }
};

// Step 2: Reset Password
const resetPassword = async () => {
    if (!otp.value || otp.value.length < 6) {
        showError('Vui lòng nhập OTP 6 số');
        return;
    }

    if (!newPassword.value || newPassword.value.length < 6) {
        showError('Mật khẩu phải có ít nhất 6 ký tự');
        return;
    }

    if (newPassword.value !== confirmPassword.value) {
        showError('Mật khẩu xác nhận không khớp');
        return;
    }

    isLoading.value = true;
    loadingMessage.value = 'Đang đặt lại mật khẩu...';

    try {
        const response = await fetch('http://localhost:8080/auth/forgot-password/reset-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: email.value,
                code: otp.value,
                newPassword: newPassword.value
            })
        });

        const data = await response.json();

        if (data.success) {
            showSuccess('Đổi mật khẩu thành công! Đang chuyển hướng...');
            setTimeout(() => {
                router.push('/auth/login');
            }, 2000);
        } else {
            showError(data.message);
        }
    } catch (error) {
        showError('Lỗi kết nối server');
    } finally {
        isLoading.value = false;
        loadingMessage.value = '';
    }
};

// Utilities
const isValidEmail = (email) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.(com|net|org|edu|gov|mil|example)$/i;
    return emailRegex.test(email);
};

const showSuccess = (message) => {
    successMessage.value = message;
    errorMessage.value = '';
    setTimeout(() => successMessage.value = '', 4000);
};

const showError = (message) => {
    errorMessage.value = message;
    successMessage.value = '';
    setTimeout(() => errorMessage.value = '', 4000);
};

const goToLogin = () => {
    router.push('/auth/login');
};
</script>
<style scoped>
.forgot-password-container {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 1rem;
    font-family: 'Inter', system-ui, sans-serif;
    background: #f3f4f6;
}

.forgot-password-card {
    display: grid;
    grid-template-columns: 1fr 1fr;
    max-width: 1000px;
    width: 100%;
    min-height: 600px;
    background: white;
    border-radius: 16px;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
    overflow: hidden;
}

.brand-side {
    background: linear-gradient(135deg, #4f46e5, #7c3aed);
    color: white;
    padding: 2rem;
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.brand-name {
    font-size: 2rem;
    font-weight: 700;
}

.brand-tagline {
    opacity: 0.9;
    font-size: 1rem;
}

.hero-section {
    margin-top: 2rem;
}

.hero-icon {
    font-size: 3rem;
    margin-bottom: 1rem;
}

.steps {
    margin-top: 2rem;
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.step {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    opacity: 0.6;
    font-size: 0.9rem;
}

.step.active {
    font-weight: 600;
    opacity: 1;
}

.form-side {
    padding: 2.5rem;
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.form-title {
    font-size: 1.5rem;
    font-weight: 700;
    margin-bottom: 0.5rem;
}

.form-subtitle {
    font-size: 0.95rem;
    color: #6b7280;
    margin-bottom: 2rem;
}

.field {
    margin-bottom: 1.25rem;
}

.field-label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 500;
}

.required {
    color: red;
}

.input-wrapper {
    position: relative;
}

.input-icon {
    position: absolute;
    left: 0.75rem;
    top: 50%;
    transform: translateY(-50%);
    color: #9ca3af;
}

.field-input,
.field-password {
    width: 100%;
    padding: 0.75rem 0.75rem 0.75rem 2.5rem;
    border: 1px solid #d1d5db;
    border-radius: 8px;
    font-size: 1rem;
}

.field-input.error,
.field-password.error {
    border-color: red;
}

.submit-btn {
    width: 100%;
    margin-top: 1rem;
    border-radius: 8px;
}

.form-footer {
    margin-top: 1.5rem;
    text-align: center;
}

.back-btn {
    color: #4f46e5;
    background: none;
    border: none;
    font-weight: 500;
    cursor: pointer;
}

.otp-input {
    text-align: center;
    font-size: 1.2rem;
    font-weight: 600;
    letter-spacing: 0.25rem;
}

/* Toast */
.toast-area {
    position: fixed;
    bottom: 1rem;
    right: 1rem;
    z-index: 9999;
}

.toast {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    background: white;
    border-radius: 8px;
    padding: 0.75rem 1rem;
    margin-top: 0.5rem;
    box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

.toast-success {
    border-left: 4px solid #10b981;
}

.toast-error {
    border-left: 4px solid #ef4444;
}

.toast-loading {
    border-left: 4px solid #f59e0b;
}

.toast-content strong {
    display: block;
    font-weight: 600;
}
</style>
