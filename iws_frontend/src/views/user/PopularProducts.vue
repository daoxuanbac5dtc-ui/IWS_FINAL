<template>
  <section class="flex flex-col max-container popular-products-section">
    <div class="flex flex-col gap-5 justify-start">
      <h2 class="text-4xl font-bold font-montserrat section-title">
        Sản Phẩm <span class="text-coral-red highlight-text">Phổ Biến</span>
      </h2>
      <div class="flex justify-between items-end">
        <p class="text-slate-gray text-lg max-w-lg section-description">
          Trải nghiệm chất lượng và phong cách hàng đầu với những lựa chọn được săn đón của chúng tôi. 
          Khám phá thế giới của sự thoải mái, thiết kế và giá trị
        </p>
        <button 
          @click="viewAllProducts"
          class="view-all-btn hidden md:flex"
        >
          Xem tất cả
          <svg viewBox="0 0 24 24" fill="currentColor" class="w-5 h-5 ml-2">
            <path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="mt-10">
      <div class="flex gap-6 overflow-hidden">
        <div 
          v-for="n in 4" 
          :key="n" 
          class="product-skeleton"
        >
          <div class="skeleton-image"></div>
          <div class="skeleton-content">
            <div class="skeleton-line skeleton-title"></div>
            <div class="skeleton-line skeleton-price"></div>
            <div class="skeleton-line skeleton-rating"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Products Swiper -->
    <div
      v-else-if="products.length > 0"
      v-motion
      :initial="{
        opacity: 0,
        y: 200,
      }"
      :visibleOnce="{
        opacity: 1,
        y: 0,
      }"
      class="mt-10 swiper-container"
    >
      <Swiper
        :slides-per-view="slidesPerViewVar"
        :space-between="30"
        :loop="products.length > slidesPerViewVar"
        class="popular-products-swiper"
        :keyboard="{
          enabled: true,
        }"
        :modules="[Keyboard]"
      >
        <SwiperSlide 
          v-for="product in products" 
          :key="product.id"
          class="product-slide"
        >
          <div 
            class="product-card-wrapper" 
            @click="handleProductClick(product)"
          >
            <ProductCard 
              class="popular-product-card" 
              :product="product" 
              :width="'280'" 
            />
          </div>
        </SwiperSlide>
      </Swiper>

      <!-- Mobile View All Button -->
      <div class="flex justify-center mt-8 md:hidden">
        <button 
          @click="viewAllProducts"
          class="view-all-btn-mobile"
        >
          Xem tất cả sản phẩm
          <svg viewBox="0 0 24 24" fill="currentColor" class="w-5 h-5 ml-2">
            <path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="empty-state mt-10">
      <div class="flex flex-col items-center justify-center py-16">
        <svg class="w-16 h-16 text-gray-300 mb-4" viewBox="0 0 24 24" fill="currentColor">
          <path d="M19,2H5A3,3 0 0,0 2,5V19A3,3 0 0,0 5,22H19A3,3 0 0,0 22,19V5A3,3 0 0,0 19,2M19,19H5V5H19V19M13.96,12.29L11.21,15.83L9.25,13.47L6.5,17H17.5L13.96,12.29Z"/>
        </svg>
        <h3 class="text-lg font-semibold text-gray-600 mb-2">Không có sản phẩm nào</h3>
        <p class="text-gray-500 text-center mb-4">Hiện tại chưa có sản phẩm phổ biến nào để hiển thị</p>
        <button 
          @click="fetchPopularProducts"
          class="refresh-btn"
        >
          Tải lại
        </button>
      </div>
    </div>
  </section>
</template>

<script setup>
import ProductCard from "@/components/user/PopularProductsCard.vue";
import { createSvgPlaceholder, resolveProductImageUrl } from '@/utils/productMedia';
import axios from 'axios';
import "swiper/css";
import { Keyboard } from "swiper/modules";
import { Swiper, SwiperSlide } from "swiper/vue";
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const slidesPerViewVar = ref(4);
const products = ref([]);
const loading = ref(true);
const productPlaceholderImage = createSvgPlaceholder({ width: 240, height: 160, label: 'Shoe Image' });

// Responsive slides
const updateSlidesPerView = () => {
  const width = window.innerWidth;
  slidesPerViewVar.value = width >= 1440 ? 4 : width >= 1024 ? 3 : width >= 500 ? 2 : 1;
};

window.addEventListener("resize", updateSlidesPerView);

// Method để lấy URL hình ảnh với fallback
const getImageUrl = (product) => {
  const imageUrl = resolveProductImageUrl(product.imgURL);
  if (imageUrl) {
    return imageUrl;
  }

  return productPlaceholderImage;
};

// FETCH POPULAR PRODUCTS ĐÃ SỬA HOÀN TOÀN
const fetchPopularProducts = async () => {
  try {
    loading.value = true;
    console.log('🔄 Starting fetchPopularProducts...');
    
    // Lấy danh sách từ API
    const [productsResponse, detailsResponse, imagesResponse] = await Promise.all([
      axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/san-pham`),
      axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/san-pham-chi-tiet`),
      axios.get(`${import.meta.env.VITE_API_BASE_URL}/hinh-anh`)
    ]);
    
    console.log('📦 Products API Response:', productsResponse.data.length, 'products');
    console.log('📋 Details API Response:', detailsResponse.data.length, 'details');
    console.log('🖼️ Images API Response:', imagesResponse.data.length, 'images');
    
    if (!productsResponse.data || productsResponse.data.length === 0) {
      console.warn('⚠️ No products data received from API');
      products.value = [];
      return;
    }
    
    // Tạo map hình ảnh theo ID
    const imageMap = new Map();
    imagesResponse.data.forEach(image => {
      const imageUrl = resolveProductImageUrl(image);
      if (imageUrl) {
        imageMap.set(image.id, imageUrl);
      }
    });
    
    const firstDetailMap = new Map();
    const priceMap = new Map();
    const productImageMap = new Map();
    
    // Xử lý từng detail để extract thông tin
    detailsResponse.data.forEach((detail) => {
      if (detail.sanPham?.id) {
        const productId = detail.sanPham.id;
        
        // Map first detail ID
        if (!firstDetailMap.has(productId)) {
          firstDetailMap.set(productId, detail.id);
        }
        
        // Map price info
        if (detail.giaBan && (!priceMap.has(productId) || detail.giaBan < priceMap.get(productId).giaBan)) {
          priceMap.set(productId, {
            giaBan: detail.giaBan,
            giaGoc: detail.giaGoc
          });
        }
        
        // XỬ LÝ HÌNH ẢNH THEO ENTITY MỚI
        if (!productImageMap.has(productId) && detail.hinhAnh) {
          console.log(`🎨 Processing image for detail ${detail.id}:`, detail.hinhAnh);
          
          let finalImageUrl = null;
          
          if (typeof detail.hinhAnh === 'object' && detail.hinhAnh !== null) {
            // Trường hợp API trả về object với id
            if (detail.hinhAnh.id) {
              finalImageUrl = imageMap.get(detail.hinhAnh.id);
              console.log(`✅ Found image by ID ${detail.hinhAnh.id}:`, finalImageUrl);
            }
            // Trường hợp API trả về object đầy đủ
            else {
              finalImageUrl = resolveProductImageUrl(detail.hinhAnh);
              console.log(`✅ Using resolved image URL:`, finalImageUrl);
            }
          } else if (typeof detail.hinhAnh === 'number') {
            // Trường hợp API trả về ID number trực tiếp
            finalImageUrl = imageMap.get(detail.hinhAnh);
            console.log(`✅ Found image by number ID ${detail.hinhAnh}:`, finalImageUrl);
          }
          
          if (finalImageUrl) {
            productImageMap.set(productId, finalImageUrl);
            console.log(`🎯 Final image URL for product ${productId}:`, finalImageUrl);
          } else {
            console.log(`❌ No valid image URL found for detail ${detail.id}`);
          }
        }
      }
    });
    
    // Map sản phẩm với thông tin từ chi tiết
    const allProducts = productsResponse.data.map((p) => {
      const priceInfo = priceMap.get(p.id) || { giaBan: 0, giaGoc: 0 };
      const imageUrl = productImageMap.get(p.id);
      const firstDetailId = firstDetailMap.get(p.id);
      
      return {
        id: p.id,
        firstDetailId: firstDetailId,
        imgURL: imageUrl, // SỬ DỤNG imgURL để match với template
        name: p.tenSanPham || 'Sản phẩm không tên',
        price: priceInfo.giaBan,
        originalPrice: priceInfo.giaGoc,
        rating: 4.5 + (Math.random() * 0.5),
        brand: p.thuongHieu?.tenThuongHieu || '',
        category: p.danhMuc?.tenDanhMuc || ''
      };
    });
    
    // Lọc sản phẩm có giá > 0 và shuffle
    const validProducts = allProducts.filter(p => p.price > 0);
    console.log('✅ Valid products (price > 0):', validProducts.length);
    
    if (validProducts.length === 0) {
      console.warn('⚠️ No valid products found');
      products.value = [];
      return;
    }
    
    // Shuffle ngẫu nhiên để tạo cảm giác "phổ biến"
    const shuffledProducts = validProducts.sort(() => 0.5 - Math.random());
    
    // Lấy 4 sản phẩm đầu tiên cho swiper
    products.value = shuffledProducts.slice(0, 4);
    
    console.log('🎯 Popular products loaded:', products.value.length);
    console.log('📊 Products with images:', products.value.filter(p => p.imgURL).length);
    console.log('📋 Products data:', products.value);
    
  } catch (error) {
    console.error('❌ Error fetching popular products:', error);
    console.error('📊 Error details:', {
      message: error.message,
      stack: error.stack
    });
    products.value = [];
  } finally {
    loading.value = false;
  }
};

// Xử lý click vào sản phẩm
const handleProductClick = (product) => {
  if (product.firstDetailId) {
    router.push(`/product/${product.firstDetailId}`);
  } else {
    console.warn('No firstDetailId found for product:', product);
    router.push('/products');
  }
};

// Xử lý click vào "Xem tất cả"
const viewAllProducts = () => {
  router.push('/products');
};

onMounted(() => {
  updateSlidesPerView();
  fetchPopularProducts();
});
</script>

<style lang="scss" scoped>
.popular-products-section {
  padding: 2rem 0;
  
  .section-title {
    position: relative;
    
    .highlight-text {
      position: relative;
      
      &::after {
        content: '';
        position: absolute;
        bottom: -2px;
        left: 0;
        right: 0;
        height: 3px;
        background: linear-gradient(90deg, #FF6452, #ff8a80);
        border-radius: 2px;
      }
    }
  }
  
  .section-description {
    line-height: 1.7;
  }
}

.view-all-btn {
  display: flex;
  align-items: center;
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #FF6452, #ff8a80);
  color: white;
  border: none;
  border-radius: 25px;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 100, 82, 0.3);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(255, 100, 82, 0.4);
  }
  
  &:active {
    transform: translateY(0);
  }
}

.view-all-btn-mobile {
  display: flex;
  align-items: center;
  padding: 1rem 2rem;
  background: linear-gradient(135deg, #FF6452, #ff8a80);
  color: white;
  border: none;
  border-radius: 30px;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 100, 82, 0.3);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(255, 100, 82, 0.4);
  }
}

.swiper-container {
  position: relative;
  
  .popular-products-swiper {
    width: 100%;
    height: 100%;
    padding: 1rem 0;
    
    .product-slide {
      height: auto;
      
      .product-card-wrapper {
        cursor: pointer;
        transition: all 0.3s ease;
        border-radius: 20px;
        overflow: hidden;
        
        &:hover {
          transform: translateY(-8px);
          box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
        }
        
        .popular-product-card {
          height: 100%;
          border-radius: 20px;
          overflow: hidden;
        }
      }
    }
  }
}

// Loading Skeleton
.product-skeleton {
  width: 280px;
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  
  .skeleton-image {
    width: 100%;
    height: 200px;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: shimmer 2s infinite;
  }
  
  .skeleton-content {
    padding: 1.5rem;
    
    .skeleton-line {
      height: 16px;
      background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
      background-size: 200% 100%;
      animation: shimmer 2s infinite;
      border-radius: 8px;
      margin-bottom: 1rem;
      
      &.skeleton-title {
        width: 80%;
        height: 20px;
      }
      
      &.skeleton-price {
        width: 60%;
        height: 18px;
      }
      
      &.skeleton-rating {
        width: 50%;
      }
    }
  }
}

.empty-state {
  .refresh-btn {
    padding: 0.75rem 1.5rem;
    background: #FF6452;
    color: white;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      background: #e55645;
      transform: translateY(-1px);
    }
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
@media (max-width: 1440px) {
  .swiper-container .popular-products-swiper {
    .product-slide .product-card-wrapper {
      .popular-product-card {
        width: 260px;
      }
    }
  }
}

@media (max-width: 1024px) {
  .popular-products-section {
    padding: 1.5rem 0;
    
    .section-title {
      font-size: 2.5rem;
    }
  }
  
  .swiper-container .popular-products-swiper {
    .product-slide .product-card-wrapper {
      .popular-product-card {
        width: 240px;
      }
    }
  }
}

@media (max-width: 768px) {
  .popular-products-section {
    .section-title {
      font-size: 2rem;
    }
    
    .section-description {
      font-size: 1rem;
    }
  }
  
  .swiper-container .popular-products-swiper {
    .product-slide .product-card-wrapper {
      .popular-product-card {
        width: 220px;
      }
    }
  }
  
  .product-skeleton {
    width: 220px;
    
    .skeleton-image {
      height: 160px;
    }
  }
}

@media (max-width: 500px) {
  .popular-products-section {
    .section-title {
      font-size: 1.75rem;
    }
  }
  
  .swiper-container .popular-products-swiper {
    .product-slide .product-card-wrapper {
      .popular-product-card {
        width: 200px;
      }
    }
  }
  
  .product-skeleton {
    width: 200px;
    
    .skeleton-image {
      height: 140px;
    }
    
    .skeleton-content {
      padding: 1rem;
    }
  }
}
</style>



