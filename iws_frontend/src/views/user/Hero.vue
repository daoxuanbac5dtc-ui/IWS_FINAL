<script setup>
import arrowRight from '@/assets/icons/arrow-right.svg';
import Card from '@/components/user/Card.vue';
import UserButton from '@/components/user/UserButton.vue';
import { statistics } from '@/constants/index';
import axios from 'axios';
import 'swiper/css';
import { Keyboard } from 'swiper/modules';
import { Swiper, SwiperSlide } from 'swiper/vue';
import { onMounted, ref } from 'vue';

// State variables
const shoesCards = ref([]);
const bigImageUrl = ref('');
const currentProduct = ref(null);
const loading = ref(true);

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

// Handle main image errors
const handleMainImageError = (event) => {
    console.log('🦸 Main hero image failed:', event.target.src);
    event.target.src =
        'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAwIiBoZWlnaHQ9IjMwMCIgdmlld0Jvg9IjAiMCA0MDAgMzAwIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxyZWN0IHdpZHRoPSI0MDAiIGhlaWdodD0iMzAwIiBmaWxsPSIjZjNmNGY2Ii8+PGVsbGlwc2UgY3g9IjIwMCIgY3k9IjIwMCIgcng9IjEyMCIgcnk9IjQwIiBmaWxsPSIjZTVlN2ViIi8+PHBhdGggZD0iTTEwMCAxODAgUTEyMCAxNjAgMTUwIDE2MCBRIDE4MCAx NjAgMjAwIDE2MCBRIDI2MCAx NjAgMjgwIDE2MCBRIDI5MCAx NzAgMzAwIDE4MCBMIDI5MCAy MDAGUTE4MCAyMjAgMTUwIDIyMCBRIDEyMCAyMjAgMTAwIDIyMCBMIDEwMCAx ODAiIGZpbGw9IiNGRjY0NTIiLz48cGF0aCBkPSJNMTEwIDIyMCBRIDEzMCAyMzAgMTUwIDIzMCBRIDE4MCAyMzAgMjAwIDIzMCBRIDI2MCAyMzAgMjkwIDIzMCBMIDI4MCAyNDAgUSAyNjAgMjQ1IDE1MCAyNDUgUSAxMzAgMjQ1IDExMCAyNDAgTCAxMTAgMjIwIiBmaWxsPSIjZGQzZGQ1Ii8+PHRleHQgeD0iMjAwIiB5PSIxMDAiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGZpbGw9IiM2YjcyODAiIGZvbnQtc2l6ZT0iMTgiIGZvbnQtZmFtaWx5PSJBcmlhbCI+Tml rZSBTaG9lPC90ZXh0Pjwvc3ZnPg==';
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
        const [productsResponse, detailsResponse, imagesResponse] = await Promise.all([axios.get('http://localhost:8080/api/san-pham'), axios.get('http://localhost:8080/api/san-pham-chi-tiet'), axios.get('http://localhost:8080/hinh-anh')]);

        console.log('🦸 Hero - Products:', productsResponse.data.length);
        console.log('🦸 Hero - Details:', detailsResponse.data.length);
        console.log('🦸 Hero - Images:', imagesResponse.data.length);

        // Create image map by ID
        const imageMap = new Map();
        imagesResponse.data.forEach((image) => {
            imageMap.set(image.id, image.fullUrl || `http://localhost:8080${image.duongDan}`);
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
                    console.log(`🦸 Processing image for product ${productId}:`, detail.hinhAnh);

                    if (typeof detail.hinhAnh === 'object' && detail.hinhAnh !== null) {
                        // Trường hợp API trả về object với id
                        if (detail.hinhAnh.id) {
                            finalImageUrl = imageMap.get(detail.hinhAnh.id);
                            console.log(`✅ Found image by ID ${detail.hinhAnh.id}:`, finalImageUrl);
                        }
                        // Trường hợp API trả về object đầy đủ
                        else if (detail.hinhAnh.fullUrl) {
                            finalImageUrl = detail.hinhAnh.fullUrl;
                        } else if (detail.hinhAnh.duongDan) {
                            const duongDan = detail.hinhAnh.duongDan;
                            if (duongDan.startsWith('http')) {
                                finalImageUrl = duongDan;
                            } else if (duongDan.startsWith('/hinh-anh/')) {
                                finalImageUrl = 'http://localhost:8080' + duongDan;
                            } else {
                                finalImageUrl = 'http://localhost:8080/hinh-anh/images/' + duongDan;
                            }
                        }
                    } else if (typeof detail.hinhAnh === 'number') {
                        // Trường hợp API trả về ID number trực tiếp
                        finalImageUrl = imageMap.get(detail.hinhAnh);
                        console.log(`✅ Found image by number ID ${detail.hinhAnh}:`, finalImageUrl);
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
                    console.log(`✅ Added hero card for product ${productId}`);
                }
            }
        });

        console.log(`🦸 Processed ${processedCards.length} hero cards`);

        shoesCards.value = processedCards;

        // Set first image as main hero image
        if (processedCards.length > 0) {
            bigImageUrl.value = processedCards[0].imgUrl;
            currentProduct.value = processedCards[0];
            console.log('🦸 Set initial hero image:', processedCards[0].imgUrl);
        } else {
            // Fallback
            bigImageUrl.value =
                'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAwIiBoZWlnaHQ9IjMwMCIgdmlld0Jvg9IjAiMCA0MDAgMzAwIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxyZWN0IHdpZHRoPSI0MDAiIGhlaWdodD0iMzAwIiBmaWxsPSIjZjNmNGY2Ii8+PGVsbGlwc2UgY3g9IjIwMCIgY3k9IjIwMCIgcng9IjEyMCIgcnk9IjQwIiBmaWxsPSIjZTVlN2ViIi8+PHBhdGggZD0iTTEwMCAxODAgUTEyMCAxNjAgMTUwIDE2MCBRIDE4MCAx NjAgMjAwIDE2MCBRIDI2MCAx NjAgMjgwIDE2MCBRIDI5MCAx NzAgMzAwIDE4MCBMIDI5MCAy MDAGUTE4MCAyMjAgMTUwIDIyMCBRIDEyMCAyMjAgMTAwIDIyMCBMIDEwMCAx ODAiIGZpbGw9IiNGRjY0NTIiLz48cGF0aCBkPSJNMTEwIDIyMCBRIDEzMCAyMzAgMTUwIDIzMCBRIDE4MCAyMzAgMjAwIDIzMCBRIDI2MCAyMzAgMjkwIDIzMCBMIDI4MCAyNDAgUSAyNjAgMjQ1IDE1MCAyNDUgUSAxMzAgMjQ1IDExMCAyNDAgTCAxMTAgMjIwIiBmaWxsPSIjZGQzZGQ1Ci8+PHRleHQgeD0iMjAwIiB5PSIxMDAiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGZpbGw9IiM2YjcyODAiIGZvbnQtc2l6ZT0iMTgiIGZvbnQtZmFtaWx5PSJBcmlhbCI+Tml rZSBTaG9lPC90ZXh0Pjwvc3ZnPg==';
            console.log('🦸 No products found, using fallback');
        }
    } catch (err) {
        console.error('🦸 Error loading hero products:', err);
        // Set fallback
        bigImageUrl.value =
            'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAwIiBoZWlnaHQ9IjMwMCIgdmlld0Jvg9IjAiMCA0MDAgMzAwIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxyZWN0IHdpZHRoPSI0MDAiIGhlaWdodD0iMzAwIiBmaWxsPSIjZjNmNGY2Ii8+PGVsbGlwc2UgY3g9IjIwMCIgY3k9IjIwMCIgcng9IjEyMCIgcnk9IjQwIiBmaWxsPSIjZTVlN2ViIi8+PHBhdGggZD0iTTEwMCAxODAgUTEyMCAxNjAgMTUwIDE2MCBRIDE4MCAx NjAgMjAwIDE2MCBRIDI2MCAx NjAgMjgwIDE2MCBRIDI5MCAx NzAgMzAwIDE4MCBMIDI5MCAy MDAGUTE4MCAyMjAgMTUwIDIyMCBRIDEyMCAyMjAgMTAwIDIyMCBMIDEwMCAx ODAiIGZpbGw9IiNGRjY0NTIiLz48cGF0aCBkPSJNMTEwIDIyMCBRIDEzMCAyMzAgMTUwIDIzMCBRIDE4MCAyMzAgMjAwIDIzMCBRIDI2MCAyMzAgMjkwIDIzMCBMIDI4MCAyNDAgUSAyNjAgMjQ1IDE1MCAyNDUgUSAxMzAgMjQ1IDExMCAyNDAgTCAxMTAgMjIwIiBmaWxsPSIjZGQzZGQ1Ci8+PHRleHQgeD0iMjAwIiB5PSIxMDAiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGZpbGw9IiM2YjcyODAiIGZvbnQtc2l6ZT0iMTgiIGZvbnQtZmFtaWx5PSJBcmlhbCI+Tml rZSBTaG9lPC90ZXh0Pjwvc3ZnPg==';
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

            <UserButton :iconUrl="arrowRight">Mua ngay</UserButton>

            <div class="mt-10 flex w-full flex-wrap items-start justify-start gap-6 md:gap-16">
                <div v-for="stat in statistics" :key="stat.label">
                    <span class="font-palanquin text-3xl font-bold md:text-4xl">{{ stat.value }}</span>
                    <p class="font-palanquin text-lg text-slate-gray">{{ stat.label }}</p>
                </div>
            </div>
        </div>

        <div class="relative mt-10 flex min-h-screen flex-1 flex-col items-center justify-center bg-primary bg-hero bg-cover bg-center p-8 sm:p-0 lg:overflow-x-hidden lg:overflow-y-hidden xl:mt-0">
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
            <div v-if="shoesCards.length > 0" class="absolute bottom-3 w-full">
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
                        <Card :key="shoe.id" :imgUrl="shoe.imgUrl" :width="'140'" :isActive="bigImageUrl === shoe.imgUrl" @change-hero-img="changeHeroImg" />
                    </SwiperSlide>
                </Swiper>
            </div>

            <!-- Fallback khi không có sản phẩm -->
            <div v-else-if="!loading" class="z-40 flex items-center justify-center">
                <div class="fallback-hero">
                    <img
                        src="data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAwIiBoZWlnaHQ9IjMwMCIgdmlld0Jvg9IjAiMCA0MDAgMzAwIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxyZWN0IHdpZHRoPSI0MDAiIGhlaWdodD0iMzAwIiBmaWxsPSIjZjNmNGY2Ii8+PGVsbGlwc2UgY3g9IjIwMCIgY3k9IjIwMCIgcng9IjEyMCIgcnk9IjQwIiBmaWxsPSIjZTVlN2ViIi8+PHBhdGggZD0iTTEwMCAxODAgUTEyMCAxNjAgMTUwIDE2MCBRIDE4MCAx NjAgMjAwIDE2MCBRIDI2MCAx NjAgMjgwIDE2MCBRIDI5MCAx NzAgMzAwIDE4MCBMIDI5MCAy MDAGUTE4MCAyMjAgMTUwIDIyMCBRIDEyMCAyMjAgMTAwIDIyMCBMIDEwMCAx ODAiIGZpbGw9IiNGRjY0NTIiLz48cGF0aCBkPSJNMTEwIDIyMCBRIDEzMCAyMzAgMTUwIDIzMCBRIDE4MCAyMzAgMjAwIDIzMCBRIDI2MCAyMzAgMjkwIDIzMCBMIDI4MCAyNDAgUSAyNjAgMjQ1IDE1MCAyNDUgUSAxMzAgMjQ1IDExMCAyNDAgTCAxMTAgMjIwIiBmaWxsPSIjZGQzZGQ1Ii8+PHRleHQgeD0iMjAwIiB5PSIxMDAiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGZpbGw9IiM2YjcyODAiIGZvbnQtc2l6ZT0iMTgiIGZvbnQtZmFtaWx5PSJBcmlhbCI+Tml rZSBTaG9lPC90ZXh0Pjwvc3ZnPg=="
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
}

.swiper-slide {
    background-position: center;
    background-size: cover;
}

.hero-main-image {
    max-width: 600px;
    max-height: 500px;
}

// Loading skeleton
.hero-skeleton {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 600px;
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
    .hero-main-image {
        max-width: 400px;
        max-height: 300px;
    }

    .skeleton-shoe {
        width: 300px;
        height: 200px;
    }

    .fallback-hero img {
        max-width: 400px;
        max-height: 300px;
    }
}
</style>
