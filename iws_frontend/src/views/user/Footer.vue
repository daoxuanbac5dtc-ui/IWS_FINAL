<script setup>
import { copyrightSign } from '@/assets/icons';
// import { footerLogo } from '@/assets/images';
import { footerLinks, socialMedia } from '@/constants';
import { useRouter } from 'vue-router';

const router = useRouter();

// Navigation helpers
const navigateToLink = (link) => {
    if (link.external || link.link?.startsWith('http')) {
        window.open(link.link, '_blank', 'noopener,noreferrer');
    } else if (link.route) {
        router.push(link.route);
    } else if (link.link) {
        router.push(link.link);
    }
};

const handleSocialClick = (social) => {
    if (social.url) {
        window.open(social.url, '_blank', 'noopener,noreferrer');
    }
};

// Current year for copyright
const currentYear = new Date().getFullYear();
</script>

<template>
    <footer class="bg-gradient-to-br from-gray-900 via-gray-800 to-black text-white">
        <!-- Newsletter Section -->
        <!-- <div class="bg-gradient-to-r from-blue-600 to-purple-600 py-12">
            <div class="max-container">
                <div class="mx-auto max-w-2xl text-center">
                    <h3 class="mb-4 text-2xl font-bold md:text-3xl">Đăng ký nhận tin khuyến mãi</h3>
                    <p class="mb-6 text-lg text-blue-100">Nhận thông tin sản phẩm mới và ưu đãi độc quyền ngay trong email của bạn</p>
                    <div class="mx-auto flex max-w-md flex-col gap-3 sm:flex-row">
                        <input type="email" placeholder="Nhập email của bạn..." class="flex-1 rounded-lg border-0 px-4 py-3 text-gray-900 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-white" />
                        <button class="whitespace-nowrap rounded-lg bg-white px-6 py-3 font-semibold text-blue-600 transition-colors duration-300 hover:bg-gray-100">Đăng ký ngay</button>
                    </div>
                </div>
            </div>
        </div> -->

        <!-- Main Footer Content -->
        <div class="max-container py-16">
            <div class="grid grid-cols-1 gap-8 lg:grid-cols-5">
                <!-- Brand Section -->
                <div class="lg:col-span-2">
                    <div class="space-y-6">
                        <!-- Logo -->
                        <router-link to="/" class="inline-block">
                            <!-- <img :src="footerLogo" width="180" alt="Logo" class="transition-opacity duration-300 hover:opacity-80" /> -->
                            <span class="text-2xl font-bold">BEE SHOES</span>
                        </router-link>

                        <!-- Brand Description -->
                        <p class="text-base leading-relaxed text-gray-300">Khám phá bộ sưu tập giày thể thao cao cấp với thiết kế hiện đại và chất lượng tuyệt vời. Tìm kiếm phong cách hoàn hảo cho bạn.</p>

                        <!-- Contact Info -->
                        <div class="space-y-2">
                            <div class="flex items-center gap-3 text-gray-300 transition-colors hover:text-white">
                                <i class="pi pi-map-marker text-sm text-blue-400"></i>
                                <span class="text-sm">123 Đường ABC, Quận 1, TP.HCM</span>
                            </div>
                            <div class="flex items-center gap-3 text-gray-300 transition-colors hover:text-white">
                                <i class="pi pi-phone text-sm text-green-400"></i>
                                <a href="tel:+84123456789" class="text-sm hover:underline">+84 123 456 789</a>
                            </div>
                            <div class="flex items-center gap-3 text-gray-300 transition-colors hover:text-white">
                                <i class="pi pi-envelope text-sm text-red-400"></i>
                                <a href="mailto:support@example.com" class="text-sm hover:underline">support@example.com</a>
                            </div>
                        </div>

                        <!-- Social Media -->
                        <div class="pt-2">
                            <h4 class="mb-3 text-base font-semibold text-gray-200">Kết nối với chúng tôi</h4>
                            <div class="flex gap-2">
                                <div
                                    v-for="social in socialMedia"
                                    :key="social.alt"
                                    @click="handleSocialClick(social)"
                                    class="group flex h-9 w-9 cursor-pointer items-center justify-center rounded-full bg-gray-700 transition-all duration-300 hover:scale-110 hover:bg-blue-600"
                                    :title="`Follow us on ${social.name || social.alt}`"
                                >
                                    <img :src="social.src" :alt="social.alt" class="h-4 w-4 brightness-0 invert filter transition-all duration-300 group-hover:brightness-100 group-hover:invert-0" />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Footer Links - 3 Equal Columns -->
                <div class="lg:col-span-3">
                    <div class="grid h-full grid-cols-1 gap-8 md:grid-cols-3">
                        <div v-for="section in footerLinks" :key="section.title" class="space-y-4">
                            <h3 class="mb-4 border-b border-gray-700 pb-2 text-lg font-bold text-white">
                                {{ section.title }}
                            </h3>
                            <ul class="space-y-2">
                                <li v-for="link in section.links.slice(0, 7)" :key="link.name">
                                    <a :href="link.link" @click.prevent="navigateToLink(link)" class="group flex cursor-pointer items-center gap-2 text-sm text-gray-300 transition-colors duration-300 hover:text-blue-400">
                                        <i v-if="link.icon" :class="link.icon" class="text-xs text-blue-400 group-hover:text-blue-300"></i>
                                        <span class="transition-transform duration-300 group-hover:translate-x-1">
                                            {{ link.name }}
                                        </span>
                                        <i v-if="link.isNew" class="pi pi-star-fill ml-auto animate-pulse text-xs text-yellow-400"></i>
                                        <i v-if="link.external" class="pi pi-external-link ml-auto text-xs opacity-0 transition-opacity group-hover:opacity-100"></i>
                                    </a>
                                </li>
                                <!-- Show more link if there are more than 7 items -->
                                <li v-if="section.links.length > 7" class="pt-2">
                                    <a href="#" class="flex items-center gap-1 text-sm font-medium text-blue-400 hover:text-blue-300">
                                        Xem thêm
                                        <i class="pi pi-chevron-down text-xs"></i>
                                    </a>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Trust Badges Section -->
        <div class="border-t border-gray-700 py-8">
            <div class="max-container">
                <div class="grid grid-cols-2 gap-6 md:grid-cols-4">
                    <div class="flex flex-col items-center space-y-3 text-center">
                        <div class="flex h-12 w-12 items-center justify-center rounded-full bg-green-600">
                            <i class="pi pi-shield text-xl text-white"></i>
                        </div>
                        <div>
                            <h4 class="text-sm font-semibold text-white">Bảo mật thanh toán</h4>
                            <p class="text-xs text-gray-400">100% an toàn</p>
                        </div>
                    </div>

                    <div class="flex flex-col items-center space-y-3 text-center">
                        <div class="flex h-12 w-12 items-center justify-center rounded-full bg-blue-600">
                            <i class="pi pi-truck text-xl text-white"></i>
                        </div>
                        <div>
                            <h4 class="text-sm font-semibold text-white">Giao hàng nhanh</h4>
                            <p class="text-xs text-gray-400">Miễn phí ship từ 500k</p>
                        </div>
                    </div>

                    <div class="flex flex-col items-center space-y-3 text-center">
                        <div class="flex h-12 w-12 items-center justify-center rounded-full bg-purple-600">
                            <i class="pi pi-refresh text-xl text-white"></i>
                        </div>
                        <div>
                            <h4 class="text-sm font-semibold text-white">Đổi trả dễ dàng</h4>
                            <p class="text-xs text-gray-400">30 ngày đổi trả</p>
                        </div>
                    </div>

                    <div class="flex flex-col items-center space-y-3 text-center">
                        <div class="flex h-12 w-12 items-center justify-center rounded-full bg-red-600">
                            <i class="pi pi-headphones text-xl text-white"></i>
                        </div>
                        <div>
                            <h4 class="text-sm font-semibold text-white">Hỗ trợ 24/7</h4>
                            <p class="text-xs text-gray-400">Tư vấn chuyên nghiệp</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Payment Methods -->
        <div class="border-t border-gray-700 py-6">
            <div class="max-container">
                <div class="flex flex-col items-center justify-between gap-6 md:flex-row">
                    <div class="text-center md:text-left">
                        <h4 class="mb-3 text-sm font-semibold text-white">Phương thức thanh toán</h4>
                        <div class="flex flex-wrap items-center justify-center gap-2 md:justify-start">
                            <div class="flex h-6 w-10 items-center justify-center rounded bg-white">
                                <span class="text-xs font-bold text-blue-600">VISA</span>
                            </div>
                            <div class="flex h-6 w-10 items-center justify-center rounded bg-white">
                                <span class="text-xs font-bold text-red-600">MC</span>
                            </div>
                            <div class="flex h-6 w-10 items-center justify-center rounded bg-white">
                                <span class="text-xs font-bold text-pink-500">MOMO</span>
                            </div>
                            <div class="flex h-6 w-10 items-center justify-center rounded bg-white">
                                <span class="text-xs font-bold text-blue-500">ZALO</span>
                            </div>
                            <div class="flex h-6 w-10 items-center justify-center rounded bg-white">
                                <span class="text-xs font-bold text-blue-600">VNPAY</span>
                            </div>
                        </div>
                    </div>

                    <div class="text-center md:text-right">
                        <h4 class="mb-3 text-sm font-semibold text-white">Chứng nhận</h4>
                        <div class="flex items-center justify-center gap-2 md:justify-end">
                            <div class="rounded bg-green-600 px-3 py-1 text-xs font-semibold">SSL Secured</div>
                            <div class="rounded bg-blue-600 px-3 py-1 text-xs font-semibold">Verified Store</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Bottom Footer -->
        <div class="border-t border-gray-700 py-4">
            <div class="max-container">
                <div class="flex flex-col items-center justify-between gap-3 md:flex-row">
                    <div class="flex items-center gap-2 text-gray-400">
                        <img :src="copyrightSign" alt="copyright" class="h-4 w-4" />
                        <p class="text-sm">Copyright {{ currentYear }} - Tất cả quyền được bảo lưu bởi <span class="font-semibold text-white">YourBrand</span></p>
                    </div>

                    <div class="flex items-center gap-4 text-xs">
                        <router-link to="/privacy-policy" class="text-gray-400 transition-colors hover:text-white"> Chính sách bảo mật </router-link>
                        <router-link to="/terms-conditions" class="text-gray-400 transition-colors hover:text-white"> Điều khoản sử dụng </router-link>
                        <router-link to="/sitemap" class="text-gray-400 transition-colors hover:text-white"> Sơ đồ trang web </router-link>
                    </div>
                </div>
            </div>
        </div>
    </footer>
</template>

<style lang="scss" scoped>
.max-container {
    @apply mx-auto max-w-7xl px-4 sm:px-6 lg:px-8;
}

// Smooth scroll behavior
html {
    scroll-behavior: smooth;
}

// Custom scrollbar for better UX
::-webkit-scrollbar {
    width: 8px;
}

::-webkit-scrollbar-track {
    @apply bg-gray-100;
}

::-webkit-scrollbar-thumb {
    @apply rounded-full bg-gray-400;
}

::-webkit-scrollbar-thumb:hover {
    @apply bg-gray-600;
}

// Animation keyframes
@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

// Hover effects for links
a,
button {
    @apply transition-all duration-300 ease-in-out;
}

// Grid responsive adjustments
@media (max-width: 768px) {
    .grid {
        @apply gap-6;
    }
}

// Focus states for accessibility
button:focus,
input:focus,
a:focus {
    @apply outline-none ring-2 ring-blue-500 ring-offset-2 ring-offset-gray-800;
}

// Loading skeleton animation (if needed)
@keyframes pulse {
    0%,
    100% {
        opacity: 1;
    }
    50% {
        opacity: 0.5;
    }
}

.animate-pulse {
    animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

// Interactive elements
.group:hover .group-hover\:translate-x-1 {
    transform: translateX(0.25rem);
}

.group:hover .group-hover\:opacity-100 {
    opacity: 1;
}

// Mobile-first responsive design
@media (min-width: 640px) {
    .sm\:flex-row {
        flex-direction: row;
    }
}

@media (min-width: 768px) {
    .md\:grid-cols-2 {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .md\:grid-cols-4 {
        grid-template-columns: repeat(4, minmax(0, 1fr));
    }
}

@media (min-width: 1024px) {
    .lg\:grid-cols-3 {
        grid-template-columns: repeat(3, minmax(0, 1fr));
    }

    .lg\:grid-cols-12 {
        grid-template-columns: repeat(12, minmax(0, 1fr));
    }

    .lg\:col-span-4 {
        grid-column: span 4 / span 4;
    }

    .lg\:col-span-8 {
        grid-column: span 8 / span 8;
    }
}

// Enhanced typography
h1,
h2,
h3,
h4,
h5,
h6 {
    @apply font-montserrat;
}

p,
a,
span,
li {
    @apply font-montserrat;
}

// Dark mode support (if needed)
@media (prefers-color-scheme: dark) {
    .footer-theme-auto {
        @apply bg-gray-900 text-white;
    }
}
</style>
