<script setup>
import arrowRight from '@/assets/icons/arrow-right.svg';
import Card from '@/components/user/Card.vue';
import UserButton from '@/components/user/UserButton.vue';
import { statistics } from '@/constants/index';
import { createSvgPlaceholder, resolveProductImageUrl } from '@/utils/productMedia';
import axios from 'axios';
import 'swiper/css';
import { Keyboard } from 'swiper/modules';
import { Swiper, SwiperSlide } from 'swiper/vue';
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

// State variables
const shoesCards = ref([]);
const bigImageUrl = ref('');
const currentProduct = ref(null);
const loading = ref(true);
const heroPlaceholderImage = createSvgPlaceholder({ width: 400, height: 300, label: 'Nike Shoe' });
const route = useRoute();
const router = useRouter();

// Change main hero image
const changeHeroImg = (imgUrl) => {
    console.log('🦸 Changing hero image to:', imgUrl);
    bigImageUrl.value = imgUrl;

    // Update current product info
    const product = shoesCards.value.find((shoe) => shoe.imgUrl === imgUrl);
    if (product) {
        currentProduct.value = product;
    }
};

// Scroll to products section
const scrollToProducts = () => {
    const productsSection = document.getElementById('products');
    if (productsSection) {
        productsSection.scrollIntoView({ behavior: 'smooth' });
        return;
    }

    if (route.path !== '/products') {
        router.push('/products');
    }
};

// Handle main image errors
const handleMainImageError = (event) => {
    console.log('🦸 Main hero image failed:', event.target.src);
    event.target.src = heroPlaceholderImage;
};

// Handle main image load success
const handleMainImageLoad = (event) => {
    console.log('🦸 Main hero image loaded successfully:', event.target.src);
};

// API call khi component mount
onMounted(async () => {
    try {
        loading.value = true;
        console.log('🦸 Loading hero products...');

        // Fetch all necessary data
        const [productsResponse, detailsResponse, imagesResponse] = await Promise.all([
            axios.get('http://localhost:8080/api/san-pham'),
            axios.get('http://localhost:8080/api/san-pham-chi-tiet'),
            axios.get('http://localhost:8080/hinh-anh')
        ]);

        console.log('🦸 Hero - Products:', productsResponse.data.length);
        console.log('🦸 Hero - Details:', detailsResponse.data.length);
        console.log('🦸 Hero - Images:', imagesResponse.data.length);

        // Create image map by ID
        const imageMap = new Map();
        imagesResponse.data.forEach((image) => {
            const imageUrl = resolveProductImageUrl(image);
            if (imageUrl) {
                imageMap.set(image.id, imageUrl);
            }
        });

        // Process products to create hero cards
        const processedCards = [];
        const productMap = new Map();

        // Map products first
        productsResponse.data.forEach((product) => {
            productMap.set(product.id, product);
        });

        // Process details to extract unique products with images
        detailsResponse.data.forEach((detail) => {
            const productId = detail.sanPham?.id;

            if (productId && !processedCards.find((card) => card.productId === productId)) {
                const product = productMap.get(productId);
                if (!product) return;

                let finalImageUrl = null;

                // XỬ LÝ HÌNH ẢNH THEO ENTITY MỚI
                if (detail.hinhAnh) {
                    if (typeof detail.hinhAnh === 'object' && detail.hinhAnh !== null) {
                        if (detail.hinhAnh.id) {
                            finalImageUrl = imageMap.get(detail.hinhAnh.id);
                        } else {
                            finalImageUrl = resolveProductImageUrl(detail.hinhAnh);
                        }
                    } else if (typeof detail.hinhAnh === 'number') {
                        finalImageUrl = imageMap.get(detail.hinhAnh);
                    }
                }

                if (finalImageUrl) {
                    processedCards.push({
                        id: detail.id,
                        productId: productId,
                        imgUrl: finalImageUrl,
                        name: product.tenSanPham || 'Nike Shoe',
                        brand: product.thuongHieu?.tenThuongHieu || 'Nike',
                        price: detail.giaBan || 0
                    });
                }
            }
        });

        shoesCards.value = processedCards;

        // Set first image as main hero image
        if (processedCards.length > 0) {
            bigImageUrl.value = processedCards[0].imgUrl;
            currentProduct.value = processedCards[0];
        } else {
            bigImageUrl.value = heroPlaceholderImage;
        }
    } catch (err) {
        console.error('🦸 Error loading hero products:', err);
        bigImageUrl.value = heroPlaceholderImage;
    } finally {
        loading.value = false;
    }
});
</script>

<template>
    <section class="max-container flex min-h-screen w-full flex-col xl:flex-row">
        <div class="flex-start padding-l relative flex flex-col items-start justify-center gap-8 pt-28 lg:mb-28 xl:w-2/5">
            <p class="font-montserrat text-xl text-coral-red">Bộ Sưu Tập Giày Của Chúng Tôi</p>
            <h1 class="z-10 font-palanquin text-8xl font-bold max-sm:text-4xl xl:whitespace-nowrap">
                <span class="">Giày</span>
                <span class="mt-3 text-coral-red">BeeShoes</span><br />
                Mới Ra Mắt
            </h1>
            <p class="font-montserrat text-lg leading-8 text-slate-gray">
                Khám phá những mẫu giày Nike thời trang, chất lượng <br />
                thoải mái và sự đổi mới cho cuộc sống năng động của bạn.
            </p>

            <UserButton :iconUrl="arrowRight" @click="scrollToProducts">Mua ngay</UserButton>

            <div class="mt-10 flex w-full flex-wrap items-start justify-start gap-6 md:gap-16">
                <div v-for="stat in statistics" :key="stat.label">
                    <span class="font-palanquin text-3xl font-bold md:text-4xl">{{ stat.value }}</span>
                    <p class="font-palanquin text-lg text-slate-gray">{{ stat.label }}</p>
                </div>
            </div>
        </div>

        <div class="relative mt-10 flex min-h-screen flex-1 flex-col items-center justify-center overflow-hidden bg-primary bg-hero bg-cover bg-center p-4 sm:p-0 xl:mt-0">
            <!-- Loading State -->
            <div v-if="loading" class="z-40 flex items-center justify-center">
                <div class="hero-skeleton">
                    <div class="skeleton-shoe"></div>
                </div>
            </div>

            <!-- Main Hero Image with Vue transition -->
            <transition v-else name="fade" mode="out-in">
                <img
                    class="hero-main-image z-40 rotate-12 object-contain mix-blend-multiply transition-all duration-1000"
                    :key="bigImageUrl"
                    :src="bigImageUrl"
                    :alt="currentProduct?.name || 'Shoes collection'"
                    width="600"
                    @error="handleMainImageError"
                    @load="handleMainImageLoad"
                />
            </transition>

            <!-- Product Cards Swiper -->
            <div v-if="shoesCards.length > 0" class="hero-card-carousel absolute bottom-4 w-full">
                <Swiper
                    :slides-per-view="2"
                    :space-between="10"
                    :breakpoints="{
                        '560': {
                            slidesPerView: 3,
                            spaceBetween: 20
                        },
                        '768': {
                            slidesPerView: 4,
                            spaceBetween: 40
                        },
                        '1024': {
                            slidesPerView: 5,
                            spaceBetween: 50
                        }
                    }"
                    :loop="shoesCards.length > 2"
                    :keyboard="true"
                    :modules="[Keyboard]"
                >
                    <SwiperSlide v-for="shoe in shoesCards" :key="shoe.id">
                        <Card :key="shoe.id" :imgUrl="shoe.imgUrl" :isActive="bigImageUrl === shoe.imgUrl" @change-hero-img="changeHeroImg" />
                    </SwiperSlide>
                </Swiper>
            </div>

            <!-- Fallback khi không có sản phẩm -->
            <div v-else-if="!loading" class="z-40 flex items-center justify-center">
                <div class="fallback-hero">
                    <img
                        :src="heroPlaceholderImage"
                        alt="Nike Shoe Placeholder"
                        class="rotate-12 object-contain"
                        width="600"
                    />
                </div>
            </div>
        </div>
    </section>
</template>

<style lang="scss" scoped>
.fade-enter-active,
.fade-leave-active {
    transition: all 1s ease;
    rotate: 20deg;
}

.fade-enter-from,
.fade-leave-to {
    transform: translateX(800px);
}

.swiper {
    width: 90%;
    height: 100%;
    padding: 12px 0 18px;
}

.swiper-slide {
    display: flex;
    justify-content: center;
    background-position: center;
    background-size: cover;
}

.hero-card-carousel {
    z-index: 50;
}

.hero-main-image {
    width: min(600px, 100%);
    max-width: 600px;
    max-height: 500px;
}

// Loading skeleton
.hero-skeleton {
    display: flex;
    justify-content: center;
    align-items: center;
    width: min(600px, 100%);
    height: 500px;
}

.skeleton-shoe {
    width: 400px;
    height: 300px;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: shimmer 2s infinite;
    border-radius: 20px;
}

.fallback-hero {
    display: flex;
    justify-content: center;
    align-items: center;

    img {
        width: min(600px, 100%);
        max-width: 600px;
        max-height: 500px;
    }
}

@keyframes shimmer {
    0% {
        background-position: -200% 0;
    }
    100% {
        background-position: 200% 0;
    }
}

// Responsive
@media (max-width: 768px) {
    .swiper {
        width: 100%;
        overflow: hidden;
    }

    .hero-main-image {
        max-width: min(320px, 100%);
        max-height: 280px;
    }

    .hero-skeleton {
        width: min(320px, 100%);
        height: 220px;
    }

    .skeleton-shoe {
        width: 300px;
        height: 200px;
    }

    .fallback-hero img {
        max-width: min(320px, 100%);
        max-height: 280px;
    }
}
</style>
