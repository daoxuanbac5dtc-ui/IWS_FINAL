<script setup>
import FloatingConfigurator from '@/components/FloatingConfigurator.vue';
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const email = ref('');
const password = ref('');
const confirmPassword = ref('');
const hoTen = ref('');
const sdt = ref('');
const diaChi = ref({
    maTinh: '01',
    maHuyen: '001',
    maPhuong: '0001',
    tenTinh: '',
    tenHuyen: '',
    tenPhuong: '',
    tenKhachHang: '',
    diaChiChiTiet: ''
});
const errorMessage = ref('');
const successMessage = ref('');
const isLoading = ref(false);

const router = useRouter();

// Kiểm tra nếu user đã đăng nhập
onMounted(() => {
    const token = localStorage.getItem('auth_token');
    if (token) {
        router.push('/');
    }
});

const validateForm = () => {
    // Validate email
    if (!email.value || !email.value.trim()) {
        errorMessage.value = 'Vui lòng nhập email';
        return false;
    }

    if (!isValidEmail(email.value)) {
        errorMessage.value = 'Email không hợp lệ';
        return false;
    }

    // Validate họ tên
    if (!hoTen.value || !hoTen.value.trim()) {
        errorMessage.value = 'Vui lòng nhập họ tên';
        return false;
    }

    if (hoTen.value.length < 2) {
        errorMessage.value = 'Họ tên phải có ít nhất 2 ký tự';
        return false;
    }

    // Validate số điện thoại
    if (!sdt.value || !sdt.value.trim()) {
        errorMessage.value = 'Vui lòng nhập số điện thoại';
        return false;
    }

    if (!isValidPhoneNumber(sdt.value)) {
        errorMessage.value = 'Số điện thoại không hợp lệ';
        return false;
    }

    // Validate mật khẩu
    if (!password.value || password.value.length < 6) {
        errorMessage.value = 'Mật khẩu phải có ít nhất 6 ký tự';
        return false;
    }

    // Validate xác nhận mật khẩu
    if (!confirmPassword.value) {
        errorMessage.value = 'Vui lòng nhập xác nhận mật khẩu';
        return false;
    }

    if (password.value !== confirmPassword.value) {
        errorMessage.value = 'Mật khẩu xác nhận không khớp';
        return false;
    }

    // Validate địa chỉ
    if (!diaChi.value.tenTinh || !diaChi.value.tenTinh.trim()) {
        errorMessage.value = 'Vui lòng nhập tên tỉnh/thành phố';
        return false;
    }

    if (!diaChi.value.tenHuyen || !diaChi.value.tenHuyen.trim()) {
        errorMessage.value = 'Vui lòng nhập tên quận/huyện';
        return false;
    }

    if (!diaChi.value.tenPhuong || !diaChi.value.tenPhuong.trim()) {
        errorMessage.value = 'Vui lòng nhập tên phường/xã';
        return false;
    }

    return true;
};

const isValidEmail = (email) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.(com|net|org|edu|gov|mil|example)$/i;
    return emailRegex.test(email);
};

const isValidPhoneNumber = (phone) => {
    const phoneRegex = /^(0[3|5|7|8|9])+([0-9]{8})$/;
    return phoneRegex.test(phone);
};

const register = async () => {
    try {
        isLoading.value = true;
        errorMessage.value = '';
        successMessage.value = '';

        // Validate form
        if (!validateForm()) {
            return;
        }

        // Chuẩn bị dữ liệu để gửi
        const registerData = {
            email: email.value,
            matKhau: password.value,
            vaiTro: 'USER', // Mặc định là USER (0)
            trangThai: 1,
            hoTen: hoTen.value,
            sdt: sdt.value,
            diaChi: {
                maTinh: diaChi.value.maTinh,
                maHuyen: diaChi.value.maHuyen,
                maPhuong: diaChi.value.maPhuong,
                tenTinh: diaChi.value.tenTinh,
                tenHuyen: diaChi.value.tenHuyen,
                tenPhuong: diaChi.value.tenPhuong,
                tenKhachHang: diaChi.value.tenKhachHang || hoTen.value,
                diaChiChiTiet: diaChi.value.diaChiChiTiet || ''
            }
        };

        // Gọi API đăng ký
        const response = await fetch('http://localhost:8080/tai-khoan', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify(registerData),
            credentials: 'include'
        });

        if (response.ok) {
            successMessage.value = 'Đăng ký thành công! Đang chuyển hướng đến trang đăng nhập...';

            // Chuyển hướng đến trang đăng nhập sau 2 giây
            setTimeout(() => {
                router.push('/auth/login');
            }, 2000);
        } else {
            const errorData = await response.json().catch(() => ({}));

            if (response.status === 409) {
                errorMessage.value = 'Email đã tồn tại trong hệ thống';
            } else if (response.status === 400) {
                errorMessage.value = 'Dữ liệu không hợp lệ. Vui lòng kiểm tra lại thông tin';
            } else {
                errorMessage.value = errorData.message || 'Đăng ký thất bại. Vui lòng thử lại';
            }
        }
    } catch (error) {
        console.error('Register error:', error);
        errorMessage.value = 'Có lỗi xảy ra. Vui lòng thử lại';
    } finally {
        isLoading.value = false;
    }
};

const goToLogin = () => {
    router.push('/auth/login');
};
</script>

<template>
    <FloatingConfigurator />
    <div class="bg-surface-50 dark:bg-surface-950 flex items-center justify-center min-h-screen min-w-[100vw] overflow-hidden">
        <div class="flex flex-col items-center justify-center">
            <div style="border-radius: 56px; padding: 0.3rem; background: linear-gradient(180deg, var(--primary-color) 10%, rgba(33, 150, 243, 0) 30%)">
                <div class="w-full bg-surface-0 dark:bg-surface-900 py-20 px-8 sm:px-20" style="border-radius: 53px">
                    <div class="text-center mb-8">
                        <svg viewBox="0 0 54 40" fill="none" xmlns="http://www.w3.org/2000/svg" class="mb-8 w-16 shrink-0 mx-auto">
                            <path d="M4 20C4 11.1634 11.1634 4 20 4H34C42.8366 4 50 11.1634 50 20V20C50 28.8366 42.8366 36 34 36H20C11.1634 36 4 28.8366 4 20V20Z" fill="var(--primary-color)"/>
                            <path d="M20 12H34V28H20V12Z" fill="white"/>
                        </svg>
                        <div class="text-surface-900 dark:text-surface-0 text-3xl font-medium mb-4">
                            Đăng ký tài khoản
                        </div>
                        <span class="text-muted-color font-medium">Tạo tài khoản mới để bắt đầu mua sắm</span>
                    </div>

                    <!-- Thông báo thành công -->
                    <div v-if="successMessage" class="mb-4 p-3 bg-green-100 dark:bg-green-900 text-green-700 dark:text-green-300 rounded-md border border-green-200 dark:border-green-800">
                        <i class="pi pi-check-circle mr-2"></i>
                        {{ successMessage }}
                    </div>

                    <!-- Thông báo lỗi -->
                    <div v-if="errorMessage" class="mb-4 p-3 bg-red-100 dark:bg-red-900 text-red-700 dark:text-red-300 rounded-md border border-red-200 dark:border-red-800">
                        <i class="pi pi-exclamation-triangle mr-2"></i>
                        {{ errorMessage }}
                    </div>

                    <form @submit.prevent="register">
                        <!-- Email -->
                        <div class="mb-4">
                            <label for="email" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2">
                                Email <span class="text-red-500">*</span>
                            </label>
                            <InputText
                                id="email"
                                type="email"
                                placeholder="Nhập địa chỉ email"
                                class="w-full"
                                v-model="email"
                                :class="{ 'p-invalid': errorMessage && !email }"
                                autocomplete="email"
                            />
                        </div>

                        <!-- Họ tên -->
                        <div class="mb-4">
                            <label for="hoTen" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2">
                                Họ và tên <span class="text-red-500">*</span>
                            </label>
                            <InputText
                                id="hoTen"
                                type="text"
                                placeholder="Nhập họ và tên"
                                class="w-full"
                                v-model="hoTen"
                                :class="{ 'p-invalid': errorMessage && !hoTen }"
                                autocomplete="name"
                            />
                        </div>

                        <!-- Số điện thoại -->
                        <div class="mb-4">
                            <label for="sdt" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2">
                                Số điện thoại <span class="text-red-500">*</span>
                            </label>
                            <InputText
                                id="sdt"
                                type="tel"
                                placeholder="Nhập số điện thoại"
                                class="w-full"
                                v-model="sdt"
                                :class="{ 'p-invalid': errorMessage && !sdt }"
                                autocomplete="tel"
                            />
                        </div>

                        <!-- Địa chỉ -->
                        <div class="mb-4">
                            <label class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2">
                                Địa chỉ <span class="text-red-500">*</span>
                            </label>
                            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                                <div>
                                    <InputText
                                        placeholder="Tỉnh/Thành phố"
                                        class="w-full"
                                        v-model="diaChi.tenTinh"
                                        :class="{ 'p-invalid': errorMessage && !diaChi.tenTinh }"
                                    />
                                </div>
                                <div>
                                    <InputText
                                        placeholder="Quận/Huyện"
                                        class="w-full"
                                        v-model="diaChi.tenHuyen"
                                        :class="{ 'p-invalid': errorMessage && !diaChi.tenHuyen }"
                                    />
                                </div>
                                <div>
                                    <InputText
                                        placeholder="Phường/Xã"
                                        class="w-full"
                                        v-model="diaChi.tenPhuong"
                                        :class="{ 'p-invalid': errorMessage && !diaChi.tenPhuong }"
                                    />
                                </div>
                            </div>
                            <div class="mt-3">
                                <InputText
                                    placeholder="Địa chỉ chi tiết (số nhà, tên đường...)"
                                    class="w-full"
                                    v-model="diaChi.diaChiChiTiet"
                                />
                            </div>
                        </div>

                        <!-- Mật khẩu -->
                        <div class="mb-4">
                            <label for="password" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-2">
                                Mật khẩu <span class="text-red-500">*</span>
                            </label>
                            <Password
                                id="password"
                                v-model="password"
                                placeholder="Nhập mật khẩu"
                                :toggleMask="true"
                                class="w-full"
                                fluid
                                :feedback="false"
                                :class="{ 'p-invalid': errorMessage && password.length < 6 }"
                                autocomplete="new-password"
                            />
                        </div>

                        <!-- Xác nhận mật khẩu -->
                        <div class="mb-6">
                            <label for="confirmPassword" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-2">
                                Xác nhận mật khẩu <span class="text-red-500">*</span>
                            </label>
                            <Password
                                id="confirmPassword"
                                v-model="confirmPassword"
                                placeholder="Nhập lại mật khẩu"
                                :toggleMask="true"
                                class="w-full"
                                fluid
                                :feedback="false"
                                :class="{ 'p-invalid': errorMessage && (password !== confirmPassword) }"
                                autocomplete="new-password"
                            />
                        </div>

                        <Button
                            type="submit"
                            :label="isLoading ? 'Đang xử lý...' : 'Đăng ký'"
                            class="w-full mb-4"
                            :disabled="isLoading"
                            :loading="isLoading"
                        />
                    </form>

                    <!-- Login link -->
                    <div class="text-center mt-8">
                        <span class="text-muted-color">Đã có tài khoản? </span>
                        <span
                            class="font-medium cursor-pointer text-primary hover:underline"
                            @click="goToLogin"
                        >
                            Đăng nhập ngay
                        </span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.pi-eye {
    transform: scale(1.6);
    margin-right: 1rem;
}

.pi-eye-slash {
    transform: scale(1.6);
    margin-right: 1rem;
}

.p-invalid {
    border-color: var(--red-500) !important;
}

.p-invalid:focus {
    border-color: var(--red-500) !important;
    box-shadow: 0 0 0 0.2rem rgba(239, 68, 68, 0.2) !important;
}
</style>
