
<template>
  <div class="product-detail-container">
    <!-- Navigation -->
    <Nav />

    <HeroSection />

    <!-- Main Product Content -->
    <div id="product-info" class="product-main">
      <div class="container" v-if="product && !loading">
        <div class="product-nav-actions">
          <button class="back-to-products-btn" @click="goBack" type="button">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
            </svg>
            <span>Tất cả sản phẩm</span>
          </button>
        </div>
        <div class="product-wrapper">
          <!-- Left Side - Product Images -->
          <div class="product-gallery">
            <!-- Main Image with Auto Slide -->
            <div class="main-image-container"
                 @mouseenter="pauseAutoSlide"
                 @mouseleave="resumeAutoSlide">
              <!-- Image Slider -->
              <div class="image-slider">
                <transition name="fade" mode="out-in">
                  <img
                    :key="selectedImageIndex"
                    :src="selectedImage || '/placeholder-shoe.png'"
                    :alt="`${productName} - Image ${selectedImageIndex + 1}`"
                    class="main-image"
                    @error="handleImageError"
                  />
                </transition>
              </div>

              <!-- Slide Progress Bar -->
              <div class="slide-progress">
                <div class="progress-bar" :style="{ width: progressWidth + '%' }"></div>
              </div>

              <!-- Slide Indicators -->
              <div class="slide-indicators">
                <span
                  v-for="(image, index) in productImages"
                  :key="index"
                  class="indicator"
                  :class="{ active: selectedImageIndex === index }"
                  @click="selectImage(index)"
                ></span>
              </div>

              <!-- Navigation Arrows -->
              <button class="nav-arrow nav-prev" @click="previousImage">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/>
                </svg>
              </button>
              <button class="nav-arrow nav-next" @click="nextImage">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/>
                </svg>
              </button>

              <!-- Zoom Button -->
              <button class="zoom-button" @click="openZoom">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
                </svg>
              </button>
            </div>

            <!-- Thumbnail Gallery -->
            <div class="thumbnail-gallery">
              <div class="thumbnail-track">
                <div
                  v-for="(image, index) in productImages"
                  :key="index"
                  class="thumbnail-item"
                  :class="{ active: selectedImageIndex === index }"
                  @click="selectImage(index)"
                  @mouseenter="hoverThumbnail(index)"
                >
                  <img :src="image" :alt="`Thumbnail ${index + 1}`" />
                  <div class="thumbnail-overlay"></div>
                </div>
              </div>
            </div>
          </div>

          <!-- Right Side - Product Info -->
          <div class="product-info">
            <!-- Product Header -->
            <div class="product-header">
              <div class="product-badges">
                <span class="badge badge-new">Mới</span>
                <span class="badge badge-hot">Hot</span>
              </div>
              <h1 class="product-title">{{ productName }}</h1>
              <div class="product-subtitle">
                <span class="brand-name" v-if="currentProduct.sanPham?.thuongHieu">
                  {{ currentProduct.sanPham.thuongHieu.tenThuongHieu }}
                </span>
                <span class="product-code">Mã: {{ currentProduct.maChiTiet || 'N/A' }}</span>
              </div>
            </div>

            <!-- Rating and Reviews -->
            <div class="rating-section">
              <div class="rating-stars">
                <div class="stars">
                  <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= 5 }">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
                    </svg>
                  </span>
                </div>
                <span class="rating-score">4.8</span>
              </div>
              <div class="rating-info">
                <span class="review-count">156 đánh giá</span>
                <span class="separator">•</span>
                <span class="sold-count">2.5k đã bán</span>
              </div>
            </div>

            <!-- Price Section -->
            <div class="price-section">
              <div class="price-wrapper">
                <div class="price-label">Giá bán</div>
                <div class="price-content">
                  <span class="current-price">{{ formatPrice(currentPrice) }}₫</span>
                  <span class="original-price" v-if="originalPrice && originalPrice !== currentPrice">
                    {{ formatPrice(originalPrice) }}₫
                  </span>
                  <span class="discount-percentage" v-if="discountPercent > 0">
                    -{{ discountPercent }}%
                  </span>
                </div>
              </div>
              <div class="price-note">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/>
                </svg>
                <span>Giá đã bao gồm VAT</span>
              </div>
            </div>

            <!-- Product Options -->
            <div class="product-options">
           <!-- Color Option -->
<div class="option-group">
<div class="option-header">
  <span class="option-label">Màu sắc:</span>
  <span class="option-value" :class="{ 'text-muted': !selectedColorName }">
    {{ selectedColorName || 'Chưa chọn màu' }}
  </span>
</div>
<div class="color-options" v-if="availableColors.length > 0">
  <div
    v-for="color in availableColors"
    :key="color.id"
    class="color-item"
    :class="{ selected: selectedColor === color.id }"
    @click="selectColor(color.id)"
  >
    <div
      class="color-circle"
      :style="{ backgroundColor: getColorHex(color.name) }"
      :title="color.name"
    ></div>
    <span class="color-name">{{ color.name }}</span>
  </div>
</div>
<div v-else class="color-options">
  <div class="color-item disabled">
    <div class="color-circle" style="background-color: #cccccc;"></div>
    <span class="color-name text-muted">Không có màu</span>
  </div>
</div>
</div>
<!-- Size Option -->
<div class="option-group">
<div class="option-header">
  <span class="option-label">Kích cỡ:</span>
  <span class="option-value" :class="{ 'text-muted': !selectedSizeName }">
    {{ selectedSizeName || 'Không có size' }}
  </span>
</div>
<div class="size-options" v-if="availableSizes.length > 0">
  <div
    v-for="size in availableSizes"
    :key="size.id"
    class="size-item"
    :class="{
      selected: selectedSize === size.id,
      disabled: !isVariantAvailable(selectedColor, size.id)
    }"
    @click="selectSize(size.id)"
  >
    <span class="size-name">{{ size.name }}</span>
  </div>
</div>
<div v-else class="size-options">
  <div class="size-item disabled">
    <span class="size-name text-muted">Không có size</span>
  </div>
</div>
<div class="size-guide" v-if="availableSizes.length > 0">
  <button class="size-guide-btn">
    <svg viewBox="0 0 24 24" fill="currentColor">
      <path d="M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z"/>
    </svg>
    <span>Hướng dẫn chọn size</span>
  </button>
</div>
</div>

              <!-- Stock Status -->
              <div class="stock-status" :class="{ 'in-stock': currentStock > 0 }">
                <div class="stock-icon">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                  </svg>
                </div>
                <div class="stock-info">
                  <span class="stock-label">Tình trạng:</span>
                  <span class="stock-value">
                    {{ currentStock > 0 ? `Còn hàng (${currentStock} sản phẩm)` : 'Hết hàng' }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Quantity and Actions -->
            <div class="purchase-section">
              <div class="quantity-section">
                <label class="quantity-label">Số lượng:</label>
                <div class="quantity-control">
                  <button class="qty-btn minus" @click="decreaseQuantity" :disabled="quantity <= 1">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M19 13H5v-2h14v2z"/>
                    </svg>
                  </button>
                  <input
                    type="number"
                    v-model.number="quantity"
                    @input="validateQuantity"
                    class="qty-input"
                    min="1"
                    :max="currentStock"
                  />
                  <button class="qty-btn plus" @click="increaseQuantity" :disabled="quantity >= currentStock">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
                    </svg>
                  </button>
                </div>
              </div>

              <div class="action-buttons">
                <button
                  class="btn-add-cart"
                  @click="addToCart"
                  :disabled="currentStock === 0 || !selectedColor || !selectedSize"
                >
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M7 18c-1.1 0-1.99.9-1.99 2S5.9 22 7 22s2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42c-.14 0-.25-.11-.25-.25l.03-.12.9-1.63h7.45c.75 0 1.41-.41 1.75-1.03l3.58-6.49c.08-.14.12-.31.12-.48 0-.55-.45-1-1-1H5.21l-.94-2H1zm16 16c-1.1 0-1.99.9-1.99 2s.89 2 1.99 2 2-.9 2-2-.9-2-2-2z"/>
                  </svg>
                  <span>Thêm vào giỏ hàng</span>
                </button>
                <button
                  class="btn-buy-now"
                  @click="buyNow"
                  :disabled="currentStock === 0 || !selectedColor || !selectedSize"
                >
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zM10 17l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                  </svg>
                  <span>Mua ngay</span>
                </button>
              </div>
            </div>

            <!-- Benefits -->
            <div class="benefits-section">
              <div class="benefit-item">
                <div class="benefit-icon">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M18 7l-1.41-1.41-6.34 6.34 1.41 1.41L18 7zm4.24-1.41L11.66 16.17 7.48 12l-1.41 1.41L11.66 19l12-12-1.42-1.41zM.41 13.41L6 19l1.41-1.41L1.83 12 .41 13.41z"/>
                  </svg>
                </div>
                <div class="benefit-content">
                  <h4>Cam kết chính hãng</h4>
                  <p>100% Authentic</p>
                </div>
              </div>
              <div class="benefit-item">
                <div class="benefit-icon">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M18 11v-1a1 1 0 00-1-1 1 1 0 00-1 1v1a2 2 0 01-2 2h-2a2 2 0 012 2v4a1 1 0 001 1 1 1 0 001-1v-4h2a2 2 0 002-2v-2z"></path>
                    <path d="M11.05 12h-2l-.95 2H6l4-8h2l4 8h-2.1z"></path>
                  </svg>
                </div>
                <div class="benefit-content">
                  <h4>Giao hàng toàn quốc</h4>
                  <p>Freeship đơn từ 500k</p>
                </div>
              </div>
              <div class="benefit-item">
                <div class="benefit-icon">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z"/>
                  </svg>
                </div>
                <div class="benefit-content">
                  <h4>Bảo hành 12 tháng</h4>
                  <p>Đổi trả trong 7 ngày</p>
                </div>
              </div>
            </div>

            <!-- Contact Support -->
            <div class="support-section">
              <div class="support-header">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M20 15.5c-1.25 0-2.45-.2-3.57-.57-.35-.11-.74-.03-1.02.24l-2.2 2.2c-2.83-1.44-5.15-3.75-6.59-6.59l2.2-2.21c.28-.26.36-.65.25-1C8.7 6.45 8.5 5.25 8.5 4c0-.55-.45-1-1-1H4c-.55 0-1 .45-1 1 0 9.39 7.61 17 17 17 .55 0 1-.45 1-1v-3.5c0-.55-.45-1-1-1z"/>
                </svg>
                <span>Cần hỗ trợ?</span>
              </div>
              <div class="support-content">
                <a href="tel:0973711868" class="support-phone">0973 711 868</a>
                <span class="support-time">(8:00 - 22:00)</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Product Information Tabs -->
        <div class="product-tabs">
          <div class="tabs-header">
            <button
              v-for="tab in tabs"
              :key="tab.id"
              class="tab-button"
              :class="{ active: activeTab === tab.id }"
              @click="activeTab = tab.id"
            >
              {{ tab.label }}
            </button>
          </div>

          <div class="tabs-content">
            <!-- Description Tab -->
            <div v-if="activeTab === 'description'" class="tab-panel">
              <h3>Mô tả sản phẩm</h3>
              <div class="product-description">
                <p>{{ productName }} - Sản phẩm chất lượng cao từ thương hiệu {{ currentProduct.sanPham?.thuongHieu?.tenThuongHieu || 'uy tín' }}.</p>
                <ul class="feature-list">
                  <li v-if="currentProduct.sanPham?.chatLieu">
                    <strong>Chất liệu:</strong> {{ currentProduct.sanPham.chatLieu.tenChatLieu }}
                  </li>
                  <li v-if="currentProduct.sanPham?.deGiay">
                    <strong>Đế giày:</strong> {{ currentProduct.sanPham.deGiay.loaiDe }}
                  </li>
                  <li v-if="selectedColorName">
                    <strong>Màu sắc:</strong> {{ selectedColorName }}
                  </li>
                  <li v-if="selectedSizeName">
                    <strong>Size:</strong> {{ selectedSizeName }}
                  </li>
                </ul>
              </div>
            </div>

            <!-- Specifications Tab -->
            <div v-if="activeTab === 'specifications'" class="tab-panel">
              <h3>Thông số kỹ thuật</h3>
              <table class="specs-table">
                <tbody>
                  <tr v-if="currentProduct.sanPham?.thuongHieu">
                    <td>Thương hiệu</td>
                    <td>{{ currentProduct.sanPham.thuongHieu.tenThuongHieu }}</td>
                  </tr>
                  <tr v-if="currentProduct.sanPham?.danhMuc">
                    <td>Danh mục</td>
                    <td>{{ currentProduct.sanPham.danhMuc.tenDanhMuc }}</td>
                  </tr>
                  <tr v-if="currentProduct.sanPham?.chatLieu">
                    <td>Chất liệu</td>
                    <td>{{ currentProduct.sanPham.chatLieu.tenChatLieu }}</td>
                  </tr>
                  <tr v-if="currentProduct.sanPham?.deGiay">
                    <td>Loại đế</td>
                    <td>{{ currentProduct.sanPham.deGiay.loaiDe }}</td>
                  </tr>
                  <tr>
                    <td>Mã sản phẩm</td>
                    <td>{{ currentProduct.maChiTiet || 'N/A' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Reviews Tab - ĐÃ THAY THẾ BẰNG REVIEW SYSTEM -->
            <div v-if="activeTab === 'reviews'" class="tab-panel">
              <ReviewProducts
                :product-id="currentProduct?.id || productId"
                @review-submitted="handleReviewSubmittedSafe"
              />
            </div>
          </div>
        </div>

        <!-- Similar Products Section -->
        <div class="similar-products-section">
          <div class="section-header">
            <h2 class="section-title">Sản phẩm tương tự</h2>
            <a href="/products#products" class="view-all-link">
              Xem tất cả
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/>
              </svg>
            </a>
          </div>

          <div class="similar-products-slider">
            <div class="similar-products-grid">
              <div
                 v-for="similarProduct in similarProducts"
                :key="similarProduct.id"
                @click="goToProduct(similarProduct.firstDetailId)"
                class="similar-product-card"
              >
                <div class="product-image-wrapper">
                  <img
                    :src="similarProduct.imgUrl || '/placeholder-shoe.png'"
                    :alt="similarProduct.label"
                    @error="handleSimilarImageError"
                  />
                  <div class="product-overlay">
                    <button class="quick-view">Xem nhanh</button>
                  </div>
                </div>
                <div class="product-content">
                  <h4 class="product-name">{{ similarProduct.label }}</h4>
                  <div class="product-price">{{ formatPrice(similarProduct.price) }}₫</div>
                  <div class="product-rating">
                    <div class="stars-mini">
                      <span v-for="i in 5" :key="i" class="star filled">★</span>
                    </div>
                    <span class="rating-text">(4.5)</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-else-if="loading" class="loading-container">
        <div class="loading-content">
          <div class="loading-spinner">
            <div class="spinner-ring"></div>
            <div class="spinner-ring"></div>
            <div class="spinner-ring"></div>
            <div class="spinner-ring"></div>
          </div>
          <p class="loading-text">Đang tải thông tin sản phẩm...</p>
        </div>
      </div>

      <!-- Error State -->
      <div v-else class="error-container">
        <div class="error-content">
          <svg class="error-icon" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
          </svg>
          <h3 class="error-title">Không tìm thấy sản phẩm</h3>
          <p class="error-text">Sản phẩm bạn đang tìm kiếm không tồn tại hoặc đã bị xóa.</p>
          <button @click="goBack" class="btn-back">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
            </svg>
            <span>Quay lại danh sách</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <section id="#" class="padding-x padding-t bg-black pb-8">
      <Footer />
    </section>

    <!-- ChatBot Component - ĐÃ THÊM -->
    <ChatBot />
  </div>
</template>
<script>
import ChatBot from '@/components/ChatBotAndReview/ChatBot.vue';
import ReviewProducts from '@/components/ChatBotAndReview/ReviewProducts.vue';
import Nav from '@/components/user/Nav.vue';
import Footer from '@/views/user/Footer.vue';
import { createSvgPlaceholder, resolveProductImageUrl } from '@/utils/productMedia';
import axios from 'axios';
import HeroSection from '../HeroSection.vue';

// Helper functions - đặt ngoài export default
const API_BASE_URL = `${import.meta.env.VITE_API_BASE_URL}`;
const productPlaceholderImage = createSvgPlaceholder({ width: 240, height: 160, label: 'Shoe Image' });

const getAuthToken = () => {
  return localStorage.getItem('auth_token');
};

const getUserId = () => {
  const userInfo = localStorage.getItem('user_info');
  if (userInfo) {
    return JSON.parse(userInfo).id;
  }
  return null;
};

export default {
  name: 'Product',
  components: {
    Nav,
    Footer,
    HeroSection,
    ChatBot,
    ReviewProducts
  },
  data() {
    return {
      product: null,
      currentProduct: null,
      allProductDetails: [],
      productImages: [],
      selectedImageIndex: 0,
      quantity: 1,
      loading: true,
      similarProducts: [],
      availableColors: [],
      availableSizes: [],
      selectedColor: null,
      selectedSize: null,
      autoSlideInterval: null,
      isPaused: false,
      progressWidth: 0,
      progressInterval: null,
      activeTab: 'description',
      tabs: [
        { id: 'description', label: 'Mô tả' },
        { id: 'specifications', label: 'Thông số' },
        { id: 'reviews', label: 'Đánh giá' }
      ]
    };
  },
  computed: {
    productId() {
      return this.$route.params.id;
    },
    selectedImage() {
      return this.productImages[this.selectedImageIndex];
    },
    productName() {
      return this.product?.sanPham?.tenSanPham || 'Sản phẩm không tên';
    },
    selectedColorName() {
      if (!this.selectedColor) return 'Chưa chọn màu';
      const color = this.availableColors.find(c => c.id === this.selectedColor);
      return color ? color.name : 'Chưa chọn màu';
    },
    selectedSizeName() {
      if (!this.selectedSize) return 'Chưa chọn size';
      const size = this.availableSizes.find(s => s.id === this.selectedSize);
      return size ? size.name : 'Chưa chọn size';
    },
    currentPrice() {
      return this.currentProduct?.giaBan || 0;
    },
    originalPrice() {
      return this.currentProduct?.giaGoc || 0;
    },
    currentStock() {
      return this.currentProduct?.soLuong || 0;
    },
    discountPercent() {
      if (!this.originalPrice || !this.currentPrice) return 0;
      return Math.round((1 - this.currentPrice / this.originalPrice) * 100);
    }
  },
  methods: {
    handleReviewSubmittedSafe(review) {
      console.log('New review submitted:', review);
    },

    resolveDetailImage(detail, imageMap) {
      const image = detail?.hinhAnh;
      if (!image) return null;

      if (typeof image === 'object') {
        const imageById = image.id !== undefined && image.id !== null
          ? imageMap.get(String(image.id)) || imageMap.get(image.id)
          : null;
        return imageById || resolveProductImageUrl(image);
      }

      if (typeof image === 'number' || typeof image === 'string') {
        return imageMap.get(String(image)) || imageMap.get(image) || resolveProductImageUrl(image);
      }

      return null;
    },

    handleReviewSubmitted(review) {
      console.log('New review submitted:', review);
      this.$toast?.success('Cảm ơn bạn đã đánh giá sản phẩm!') ||
      alert('Cảm ơn bạn đã đánh giá sản phẩm!');
    },

    async saveReviewToAPI(review) {
      try {
        const payload = {
          san_pham_chi_tiet_id: this.currentProduct?.id,
          ten_khach_hang: review.name,
          diem_danh_gia: review.rating,
          tieu_de: review.title,
          noi_dung: review.content
        };
      } catch (error) {
        console.error('Error saving review:', error);
      }
    },

    startAutoSlide() {
      if (!this.isPaused && this.productImages.length > 1) {
        this.progressWidth = 0;
        this.autoSlideInterval = setInterval(() => {
          this.nextImage();
        }, 4000);

        this.progressInterval = setInterval(() => {
          if (this.progressWidth < 100) {
            this.progressWidth += 2.5;
          } else {
            this.progressWidth = 0;
          }
        }, 100);
      }
    },

    stopAutoSlide() {
      if (this.autoSlideInterval) {
        clearInterval(this.autoSlideInterval);
        this.autoSlideInterval = null;
      }
      if (this.progressInterval) {
        clearInterval(this.progressInterval);
        this.progressInterval = null;
      }
      this.progressWidth = 0;
    },

    pauseAutoSlide() {
      this.isPaused = true;
      this.stopAutoSlide();
    },

    resumeAutoSlide() {
      this.isPaused = false;
      this.startAutoSlide();
    },

    nextImage() {
      this.selectedImageIndex = (this.selectedImageIndex + 1) % this.productImages.length;
      this.progressWidth = 0;
    },

    previousImage() {
      this.selectedImageIndex = this.selectedImageIndex === 0
        ? this.productImages.length - 1
        : this.selectedImageIndex - 1;
      this.progressWidth = 0;
    },

    selectImage(index) {
      this.selectedImageIndex = index;
      this.progressWidth = 0;
      this.stopAutoSlide();
      this.startAutoSlide();
    },

    hoverThumbnail(index) {
      // Preview on hover without changing selection
    },

    openZoom() {
      console.log('Open zoom modal');
    },

    selectColor(colorId) {
      console.log('🎨 Color selected:', colorId);
      this.selectedColor = colorId;

      // Reset size selection when color changes
      this.selectedSize = null;

      // Update current product and images
      this.updateCurrentProduct();
      this.updateImages();
    },

    selectSize(sizeId) {
      console.log('📏 Size selected:', sizeId);

      if (!this.isVariantAvailable(this.selectedColor, sizeId)) {
        console.warn('⚠️ Selected size not available for current color');
        return;
      }

      this.selectedSize = sizeId;
      this.updateCurrentProduct();
    },

    isVariantAvailable(colorId, sizeId) {
      if (!colorId || !sizeId) return false;

      const available = this.allProductDetails.some(detail =>
        detail.mauSac?.id === colorId &&
        detail.kichCo?.id === sizeId &&
        detail.soLuong > 0
      );

      console.log(`🔍 Variant availability check: color=${colorId}, size=${sizeId}, available=${available}`);
      return available;
    },

    updateCurrentProduct() {
      if (!this.selectedColor || !this.selectedSize) {
        console.log('⚠️ Cannot update product - missing color or size selection');
        return;
      }

      console.log(`🔍 Looking for variant: color=${this.selectedColor}, size=${this.selectedSize}`);

      const variant = this.allProductDetails.find(detail =>
        detail.mauSac?.id === this.selectedColor &&
        detail.kichCo?.id === this.selectedSize
      );

      if (variant) {
        this.currentProduct = variant;
        console.log('✅ Updated current product:', {
          id: variant.id,
          maChiTiet: variant.maChiTiet,
          giaBan: variant.giaBan,
          soLuong: variant.soLuong,
          color: variant.mauSac?.tenMauSac,
          size: variant.kichCo?.tenKichCo
        });
      } else {
        console.warn('⚠️ No variant found for selected color/size combination');
        // Fallback to first available variant with selected color
        const colorVariant = this.allProductDetails.find(detail => 
          detail.mauSac?.id === this.selectedColor
        );
        if (colorVariant) {
          this.currentProduct = colorVariant;
          this.selectedSize = colorVariant.kichCo?.id;
          console.log('🔄 Fallback to first available size for selected color');
        }
      }
    },

    // XỬ LÝ HÌNH ẢNH ĐÃ SỬA CHO ENTITY MỚI - SỬA HOÀN TOÀN
    async updateImages() {
      if (!this.selectedColor) return;

      try {
        // Lấy danh sách hình ảnh từ API
        const imagesResponse = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/hinh-anh`);
        const imageMap = new Map();
        imagesResponse.data.forEach(image => {
          const imageUrl = resolveProductImageUrl(image);
          if (imageUrl) {
            imageMap.set(String(image.id), imageUrl);
          }
        });

        const colorVariants = this.allProductDetails.filter(detail =>
          detail.mauSac?.id === this.selectedColor
        );

        const newImages = [];

        colorVariants.forEach(detail => {
          if (detail.hinhAnh) {
            const imageUrl = this.resolveDetailImage(detail, imageMap);

            if (imageUrl && imageUrl.trim() !== '') {
              newImages.push(imageUrl);
            }
          }
        });

        if (newImages.length > 0) {
          this.productImages = [...new Set(newImages)];
          this.selectedImageIndex = 0;
        } else {
          this.productImages = [productPlaceholderImage];
        }
      } catch (error) {
        console.error('Error updating images:', error);
        this.productImages = [productPlaceholderImage];
      }
    },

    // FETCHPRODUCT ĐÃ SỬA HOÀN TOÀN
    async fetchProduct() {
      try {
        this.loading = true;
        console.log('Fetching product with ID:', this.productId);

        // Lấy thông tin sản phẩm chi tiết
        const detailResponse = await axios.get(`http://localhost:8080/api/san-pham-chi-tiet/${this.productId}`);
        if (!detailResponse.data) {
          throw new Error('Không tìm thấy sản phẩm');
        }

        const currentDetail = detailResponse.data;
        console.log('Current detail:', currentDetail);

        // Lấy tất cả chi tiết sản phẩm liên quan
        const allDetailsResponse = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/san-pham-chi-tiet`);
        const relatedDetails = allDetailsResponse.data.filter(d =>
          d.sanPham?.id === currentDetail.sanPham?.id
        );

        console.log('Related details:', relatedDetails.length);

        // Lấy danh sách hình ảnh
        const imagesResponse = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/hinh-anh`);
        const imageMap = new Map();
        imagesResponse.data.forEach(image => {
          const imageUrl = resolveProductImageUrl(image);
          if (imageUrl) {
            imageMap.set(String(image.id), imageUrl);
          }
        });

        // THÊM: Lấy dữ liệu màu sắc và kích cỡ
        const [colorsResponse, sizesResponse] = await Promise.all([
          axios.get(`${import.meta.env.VITE_API_BASE_URL}/mau-sac`),
          axios.get(`${import.meta.env.VITE_API_BASE_URL}/kich-co`)
        ]);

        // Tạo map cho màu sắc và kích cỡ
        const colorMap = new Map();
        colorsResponse.data.forEach(color => {
          colorMap.set(color.id, color.tenMauSac);
        });

        const sizeMap = new Map();
        sizesResponse.data.forEach(size => {
          sizeMap.set(size.id, size.tenKichCo);
        });

        // ENRICH DETAILS với tên màu sắc và kích cỡ đúng
        const enrichedDetails = relatedDetails.map(detail => {
          const colorInfo = detail.mauSac ? {
            id: detail.mauSac.id,
            tenMauSac: colorMap.get(detail.mauSac.id) || `Màu ${detail.mauSac.id}`
          } : null;

          const sizeInfo = detail.kichCo ? {
            id: detail.kichCo.id,
            tenKichCo: sizeMap.get(detail.kichCo.id) || `Size ${detail.kichCo.id}`
          } : null;

          return {
            ...detail,
            mauSac: colorInfo,
            kichCo: sizeInfo
          };
        });

        this.product = currentDetail;
        this.allProductDetails = enrichedDetails;

        // Extract available colors and sizes từ enriched data
        this.availableColors = [...new Map(enrichedDetails
          .filter(d => d.mauSac && d.mauSac.id)
          .map(d => [d.mauSac.id, {
            id: d.mauSac.id,
            name: d.mauSac.tenMauSac
          }])
        ).values()];

        this.availableSizes = [...new Map(enrichedDetails
          .filter(d => d.kichCo && d.kichCo.id)
          .map(d => [d.kichCo.id, {
            id: d.kichCo.id,
            name: d.kichCo.tenKichCo
          }])
        ).values()];

        console.log('🔴 Available colors:', this.availableColors);
        console.log('🔵 Available sizes:', this.availableSizes);

        // Set initial selections với fallback
        this.selectedColor = currentDetail.mauSac?.id || (this.availableColors[0]?.id) || null;
        this.selectedSize = currentDetail.kichCo?.id || (this.availableSizes[0]?.id) || null;
        
        // Tìm current product trong enriched details
        this.currentProduct = enrichedDetails.find(d => d.id === parseInt(this.productId)) || currentDetail;
        
        console.log('🎯 Selected color:', this.selectedColor);
        console.log('📏 Selected size:', this.selectedSize);
        console.log('📦 Current product:', this.currentProduct);

        // XỬ LÝ HÌNH ẢNH
        const currentImages = [];
        enrichedDetails.forEach(detail => {
          if (detail.hinhAnh) {
            const imageUrl = this.resolveDetailImage(detail, imageMap);

            if (imageUrl && imageUrl.trim() !== '') {
              currentImages.push(imageUrl);
            }
          }
        });

        if (currentImages.length > 0) {
          this.productImages = [...new Set(currentImages)];
          console.log('🖼️ Product images loaded:', this.productImages);
        } else {
          this.productImages = [productPlaceholderImage];
          console.log('🖼️ No images found, using placeholder');
        }

        await this.fetchSimilarProducts();

        console.log('✅ Product loaded successfully');

      } catch (error) {
        console.error('❌ Error fetching product:', error);
        console.error('Error details:', {
          message: error.message,
          status: error.response?.status,
          data: error.response?.data
        });
        
        this.product = null;
        this.loading = false;
      } finally {
        this.loading = false;
      }
    },

    // FETCHSIMILARPRODUCTS ĐÃ SỬA
    async fetchSimilarProducts() {
      try {
        const [productsResponse, detailsResponse, imagesResponse] = await Promise.all([
          axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/san-pham`),
          axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/san-pham-chi-tiet`),
          axios.get(`${import.meta.env.VITE_API_BASE_URL}/hinh-anh`)
        ]);

        // Tạo map hình ảnh theo ID
        const imageMap = new Map();
        imagesResponse.data.forEach(image => {
          const imageUrl = resolveProductImageUrl(image);
          if (imageUrl) {
            imageMap.set(String(image.id), imageUrl);
          }
        });

        const firstDetailMap = new Map();
        const productImageMap = new Map();
        const priceMap = new Map();

        detailsResponse.data.forEach(detail => {
          const productId = detail.sanPham?.id;
          if (productId) {
            if (!firstDetailMap.has(productId)) {
              firstDetailMap.set(productId, detail.id);
            }

            if (!priceMap.has(productId) || detail.giaBan < priceMap.get(productId)) {
              priceMap.set(productId, detail.giaBan);
            }

            // XỬ LÝ HÌNH ẢNH SIMILAR PRODUCTS - SỬA MỚI
            if (!productImageMap.has(productId) && detail.hinhAnh) {
              const imageUrl = this.resolveDetailImage(detail, imageMap);

              if (imageUrl && imageUrl.trim() !== '') {
                productImageMap.set(productId, imageUrl);
              }
            }
          }
        });

        const allProducts = productsResponse.data
          .filter(p => p.id !== this.product?.sanPham?.id)
          .map(p => ({
            id: p.id,
            firstDetailId: firstDetailMap.get(p.id),
            imgUrl: productImageMap.get(p.id),
            label: p.tenSanPham || 'Sản phẩm',
            price: priceMap.get(p.id) || 0,
            categoryId: p.danhMuc?.id
          }));

        const currentCategoryId = this.product?.sanPham?.danhMuc?.id;
        let sameCategoryProducts = allProducts.filter(p => p.categoryId === currentCategoryId);

        if (sameCategoryProducts.length < 4) {
          const otherProducts = allProducts.filter(p => p.categoryId !== currentCategoryId);
          sameCategoryProducts = [...sameCategoryProducts, ...otherProducts];
        }
        const shuffled = sameCategoryProducts.sort(() => 0.5 - Math.random());
        this.similarProducts = shuffled.slice(0, 4);

      } catch (error) {
        console.error('Error fetching similar products:', error);
        this.similarProducts = [];
      }
    },

    // Utility methods
    formatPrice(price) {
      if (!price) return '0';
      return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.');
    },

    getColorHex(colorName) {
      const colorMap = {
        'Trắng': '#ffffff',
        'Đen': '#000000',
        'Đỏ': '#ff0000',
        'Xanh Dương': '#0000ff',
        'Xanh Navy': '#001f3f',
        'Xanh Lá': '#008000',
        'Vàng': '#ffff00',
        'Cam': '#ffa500',
        'Hồng': '#ff69b4',
        'Tím': '#800080',
        'Nâu': '#8b4513',
        'Xám': '#808080',
        'Bạc': '#c0c0c0',
        'Vàng Gold': '#ffd700',
        'Be': '#f5f5dc'
      };
      return colorMap[colorName] || '#cccccc';
    },

    // Quantity methods
    decreaseQuantity() {
      if (this.quantity > 1) {
        this.quantity--;
      }
    },

    increaseQuantity() {
      if (this.quantity < this.currentStock) {
        this.quantity++;
      }
    },

    validateQuantity() {
      if (this.quantity < 1) {
        this.quantity = 1;
      } else if (this.quantity > this.currentStock) {
        this.quantity = this.currentStock;
      }
    },

    // Cart methods với backend integration - FIXED TOAST VERSION
    async addToCart(options = {}) {
      const actionOptions = options && options.silent !== undefined ? options : {};
      const { silent = false, resetQuantity = true } = actionOptions;
      console.log('=== ADD TO CART DEBUG START ===');

      // Check if user is authenticated
      const isAuthenticated = !!(getAuthToken() && getUserId());
      console.log('🔐 User authenticated:', isAuthenticated);

      // Kiểm tra validations
      if (!this.selectedColor || !this.selectedSize) {
        console.log('❌ Color or size not selected');
        console.log('Selected color:', this.selectedColor);
        console.log('Selected size:', this.selectedSize);
        alert('Vui lòng chọn màu sắc và kích cỡ!');
        return false;
      }
      console.log('✅ Color and size selected');

      if (!this.currentProduct) {
        console.log('❌ No current product');
        alert('Vui lòng chọn sản phẩm!');
        return false;
      }
      console.log('✅ Current product exists:', this.currentProduct);

      if (this.currentStock === 0) {
        console.log('❌ Out of stock');
        alert('Sản phẩm đã hết hàng!');
        return false;
      }
      console.log('✅ Stock available:', this.currentStock);

      if (this.quantity > this.currentStock) {
        console.log('❌ Quantity exceeds stock');
        alert(`Chỉ còn ${this.currentStock} sản phẩm trong kho!`);
        return false;
      }
      console.log('✅ Quantity valid:', this.quantity);

      try {
        if (isAuthenticated) {
          // Logged in user - add to backend
          const requestData = {
            productDetailId: this.currentProduct.id,
            soLuong: this.quantity
          };

          console.log('📤 Sending request data to backend:', requestData);
          console.log('🔍 Current product full data:', this.currentProduct);
          console.log('🔐 Auth token present:', !!getAuthToken());
          console.log('👤 User ID:', getUserId());

          // Gọi API để thêm vào giỏ hàng backend
          const response = await axios.post(
            `${API_BASE_URL}/api/gio-hang/add`,
            requestData,
            {
              headers: {
                'Authorization': `Bearer ${getAuthToken()}`,
                'Content-Type': 'application/json'
              }
            }
          );

          console.log('✅ SUCCESS - Added to backend cart:', response.data);
        } else {
          // Guest user - add to localStorage
          console.log('🛒 Adding to guest cart');

          // Get current guest cart
          let guestCart = [];
          try {
            const existingCart = localStorage.getItem('guest_cart');
            if (existingCart) {
              guestCart = JSON.parse(existingCart);
            }
          } catch (error) {
            console.error('Error reading guest cart:', error);
            guestCart = [];
          }

          // Check if item already exists
          const existingItemIndex = guestCart.findIndex(item =>
            item.productDetailId === this.currentProduct.id
          );

          if (existingItemIndex >= 0) {
            // Update quantity
            guestCart[existingItemIndex].quantity += this.quantity;
            console.log('📝 Updated existing item quantity');
          } else {
            // Add new item
            const newItem = {
              id: Date.now(), // Temporary ID
              productDetailId: this.currentProduct.id,
              name: this.currentProduct.sanPham?.tenSanPham || 'Sản phẩm',
              code: this.currentProduct.maChiTiet || 'SP',
              image: this.currentProduct.hinhAnh?.duongDan || this.productImages[0] || '/placeholder-shoe.png',
              price: this.currentProduct.giaBan || 0,
              quantity: this.quantity,
              size: this.selectedSize,
              color: this.selectedColor,
              stock: this.currentStock,
              points: 0,
              totalPrice: (this.currentProduct.giaBan || 0) * this.quantity
            };
            guestCart.push(newItem);
            console.log('➕ Added new item to guest cart');
          }

          // Save to localStorage
          localStorage.setItem('guest_cart', JSON.stringify(guestCart));
          console.log('✅ SUCCESS - Added to guest cart:', guestCart);
        }

        // Dispatch event để update cart counter ở Nav
        window.dispatchEvent(new CustomEvent('cartUpdated'));

        if (!silent) {
          // FIXED: Hiển thị thông báo thành công với fallback
          const successMessage = `Đã thêm ${this.quantity} sản phẩm vào giỏ hàng!`;

          try {
            // Thử dùng toast trước
            if (this.$toast && typeof this.$toast.success === 'function') {
              this.$toast.success(successMessage);
            } else {
              // Fallback về alert nếu toast không có
              alert(successMessage);
            }
          } catch (toastError) {
            // Nếu toast bị lỗi, dùng alert
            console.warn('Toast error, using alert:', toastError);
            alert(successMessage);
          }
        }

        // Reset số lượng về 1
        if (resetQuantity) {
          this.quantity = 1;
        }
        console.log('=== ADD TO CART DEBUG END - SUCCESS ===');
        return true;

      } catch (error) {
        console.error('❌ ERROR in addToCart:', error);

        // Log chi tiết lỗi để debug
        if (error.response) {
          console.error('📋 Response data:', error.response.data);
          console.error('📊 Response status:', error.response.status);
          console.error('📋 Response headers:', error.response.headers);
        } else if (error.request) {
          console.error('📡 Request made but no response:', error.request);
        } else {
          console.error('⚙️ Error setting up request:', error.message);
        }

        let errorMessage = 'Có lỗi xảy ra khi thêm vào giỏ hàng. Vui lòng thử lại!';

        if (error.response?.status === 401) {
          errorMessage = 'Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại!';
          alert(errorMessage);
          this.$router.push('/auth/login');
        } else if (error.response?.status === 400) {
          // Hiển thị message cụ thể từ backend
          errorMessage = error.response.data?.message ||
                        error.response.data?.error ||
                        error.response.data ||
                        'Không thể thêm sản phẩm vào giỏ hàng!';
          console.error('🚫 Backend error message:', errorMessage);
          alert(errorMessage);
        } else {
          alert(errorMessage);
        }

        console.log('=== ADD TO CART DEBUG END - FAILED ===');
        return false;
      }
    },

    async addSimilarToCart(similarProduct) {
      console.log('=== ADD SIMILAR TO CART DEBUG START ===');
      console.log('Similar product:', similarProduct);

      // Check if user is authenticated
      const isAuthenticated = !!(getAuthToken() && getUserId());
      console.log('🔐 User authenticated:', isAuthenticated);

      if (!similarProduct.firstDetailId) {
        console.log('❌ No firstDetailId in similar product');
        alert('Không tìm thấy thông tin sản phẩm!');
        return;
      }

      try {
        if (isAuthenticated) {
          // Logged in user - add to backend
          const requestData = {
            productDetailId: similarProduct.firstDetailId,
            soLuong: 1
          };

          console.log('📤 Sending similar product request to backend:', requestData);

          // Gọi API để thêm vào giỏ hàng backend với số lượng mặc định là 1
          const response = await axios.post(
            `${API_BASE_URL}/api/gio-hang/add`,
            requestData,
            {
              headers: {
                'Authorization': `Bearer ${getAuthToken()}`,
                'Content-Type': 'application/json'
              }
            }
          );

          console.log('✅ Similar product added to backend cart:', response.data);
        } else {
          // Guest user - add to localStorage
          console.log('🛒 Adding similar product to guest cart');

          // Get current guest cart
          let guestCart = [];
          try {
            const existingCart = localStorage.getItem('guest_cart');
            if (existingCart) {
              guestCart = JSON.parse(existingCart);
            }
          } catch (error) {
            console.error('Error reading guest cart:', error);
            guestCart = [];
          }

          // Check if item already exists
          const existingItemIndex = guestCart.findIndex(item =>
            item.productDetailId === similarProduct.firstDetailId
          );

          if (existingItemIndex >= 0) {
            // Update quantity
            guestCart[existingItemIndex].quantity += 1;
            console.log('📝 Updated existing similar item quantity');
          } else {
            // Add new item
            const newItem = {
              id: Date.now(), // Temporary ID
              productDetailId: similarProduct.firstDetailId,
              name: similarProduct.label || 'Sản phẩm tương tự',
              code: similarProduct.maSanPham || 'SP',
              image: similarProduct.imgUrl || '/placeholder-shoe.png',
              price: similarProduct.price || 0,
              quantity: 1,
              size: 'M',
              color: 'Đen',
              stock: 10,
              points: 0,
              totalPrice: similarProduct.price || 0
            };
            guestCart.push(newItem);
            console.log('➕ Added new similar item to guest cart');
          }

          // Save to localStorage
          localStorage.setItem('guest_cart', JSON.stringify(guestCart));
          console.log('✅ Similar product added to guest cart:', guestCart);
        }

        // Dispatch event để update cart counter
        window.dispatchEvent(new CustomEvent('cartUpdated'));

        // Hiển thị thông báo với fallback
        const successMessage = `Đã thêm "${similarProduct.label}" vào giỏ hàng!`;
        try {
          if (this.$toast && typeof this.$toast.success === 'function') {
            this.$toast.success(successMessage);
          } else {
            alert(successMessage);
          }
        } catch (toastError) {
          alert(successMessage);
        }

        console.log('=== ADD SIMILAR TO CART DEBUG END - SUCCESS ===');

      } catch (error) {
        console.error('❌ Error adding similar product to cart:', error);

        if (error.response?.status === 401) {
          alert('Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại!');
          this.$router.push('/auth/login');
        } else if (error.response?.status === 400) {
          console.error('Backend error:', error.response.data);
          alert(error.response.data.message || 'Không thể thêm sản phẩm vào giỏ hàng!');
        } else {
          alert('Có lỗi xảy ra. Vui lòng thử lại!');
        }

        console.log('=== ADD SIMILAR TO CART DEBUG END - FAILED ===');
      }
    },

    async buyNow() {
      try {
        const addedToCart = await this.addToCart({ silent: true, resetQuantity: false });
        if (!addedToCart) {
          return;
        }

        this.quantity = 1;
        await this.$router.push('/checkout');
      } catch (error) {
        // addToCart đã handle lỗi
        console.error('Buy now failed:', error);
      }
    },

    // Navigation methods
    goToProduct(productDetailId) {
      this.selectedImageIndex = 0;
      this.quantity = 1;
      this.progressWidth = 0;
      this.$router.push({
        path: `/product/${productDetailId}`,
        hash: '#product-info'
      });
    },

    goBack() {
      this.$router.push('/products#products');
    },

    // XỬ LÝ LỖI HÌNH ẢNH ĐÃ SỬA
    handleImageError(event) {
      console.log('Product image load failed for:', event.target.src);

      // Chỉ set placeholder nếu chưa phải placeholder
      if (!event.target.src.startsWith('data:image/svg+xml')) {
        console.log('Setting placeholder');
        event.target.src = productPlaceholderImage;
      }
    },

    handleSimilarImageError(event) {
      console.log('Similar product image load failed for:', event.target.src);

      // Chỉ set SVG placeholder nếu chưa phải SVG
      if (!event.target.src.startsWith('data:image/svg+xml')) {
        event.target.src = productPlaceholderImage;
      }
    }
  },

  mounted() {
    console.log('🚀 === PRODUCT COMPONENT MOUNTED ===');
    console.log('📍 Route ID:', this.productId);

    this.fetchProduct().then(() => {
      console.log('✅ === PRODUCT LOADED SUCCESSFULLY ===');
      console.log('📦 Product:', this.product);
      console.log('🎯 Current Product:', this.currentProduct);
      console.log('🔴 Colors:', this.availableColors);
      console.log('🔵 Sizes:', this.availableSizes);
      console.log('🖼️ Images:', this.productImages);
      console.log('🎨 Selected Color:', this.selectedColor);
      console.log('📏 Selected Size:', this.selectedSize);
      
      this.startAutoSlide();
    }).catch(error => {
      console.error('❌ === PRODUCT LOAD FAILED ===', error);
    });
  },

  beforeUnmount() {
    this.stopAutoSlide();
  },

  watch: {
    '$route.params.id': {
      handler(newId) {
        if (newId) {
          this.selectedImageIndex = 0;
          this.quantity = 1;
          this.progressWidth = 0;
          this.selectedColor = null;
          this.selectedSize = null;

          this.stopAutoSlide();

          this.fetchProduct().then(() => {
            this.startAutoSlide();
          });
        }
      },
      immediate: true
    }
  }
};
</script>

<style scoped>

/* Container Styles */
.product-detail-container {
min-height: 100vh;
background-color: #f8f9fa;
}

#product-info {
scroll-margin-top: 96px;
}

/* Breadcrumb */
.breadcrumb-container {
background: white;
border-bottom: 1px solid #e5e7eb;
position: sticky;
top: 0;
z-index: 100;
}

.breadcrumb {
max-width: 1200px;
margin: 0 auto;
padding: 1rem 20px;
display: flex;
align-items: center;
gap: 0.5rem;
font-size: 0.875rem;
}

.breadcrumb-item {
color: #6b7280;
text-decoration: none;
transition: color 0.2s;
}

.breadcrumb-item:hover {
color: #FF6452;
}

.breadcrumb-separator {
color: #d1d5db;
}

.breadcrumb-current {
color: #1f2937;
font-weight: 500;
}

/* Main Content */
.product-main {
padding: 2rem 0 4rem;
}

.container {
max-width: 1200px;
margin: 0 auto;
padding: 0 20px;
}

.product-nav-actions {
display: flex;
justify-content: flex-start;
margin-bottom: 1rem;
}

.back-to-products-btn {
display: inline-flex;
align-items: center;
gap: 0.5rem;
height: 44px;
padding: 0 1rem;
border: 1px solid #e5e7eb;
border-radius: 999px;
background: #ffffff;
color: #1f2937;
font-weight: 700;
cursor: pointer;
box-shadow: 0 4px 14px rgba(0, 0, 0, 0.06);
transition: all 0.2s ease;
}

.back-to-products-btn svg {
width: 18px;
height: 18px;
color: #ff6452;
}

.back-to-products-btn:hover {
border-color: #ff6452;
color: #ff6452;
transform: translateY(-1px);
}

/* Product Wrapper */
.product-wrapper {
display: grid;
grid-template-columns: 1fr 1fr;
gap: 3rem;
background: white;
border-radius: 24px;
padding: 3rem;
box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
margin-bottom: 3rem;
}

/* Gallery Styles */
.product-gallery {
display: flex;
flex-direction: column;
gap: 1.5rem;
}

.main-image-container {
position: relative;
background: #f9fafb;
border-radius: 20px;
overflow: hidden;
aspect-ratio: 1;
}

.image-slider {
width: 100%;
height: 100%;
display: flex;
align-items: center;
justify-content: center;
}

.main-image {
max-width: 90%;
max-height: 90%;
object-fit: contain;
filter: drop-shadow(0 20px 40px rgba(0, 0, 0, 0.1));
}

/* Slide Progress Bar */
.slide-progress {
position: absolute;
bottom: 0;
left: 0;
right: 0;
height: 3px;
background: rgba(0, 0, 0, 0.1);
}

.progress-bar {
height: 100%;
background: linear-gradient(90deg, #FF6452, #ff8a80);
transition: width 0.1s linear;
}

/* Slide Indicators */
.slide-indicators {
position: absolute;
bottom: 20px;
left: 50%;
transform: translateX(-50%);
display: flex;
gap: 8px;
z-index: 3;
}

.indicator {
width: 8px;
height: 8px;
border-radius: 50%;
background: rgba(255, 255, 255, 0.5);
cursor: pointer;
transition: all 0.3s ease;
}

.indicator.active {
width: 24px;
border-radius: 4px;
background: white;
}

/* Navigation Arrows */
.nav-arrow {
position: absolute;
top: 50%;
transform: translateY(-50%);
background: rgba(255, 255, 255, 0.9);
border: none;
width: 48px;
height: 48px;
border-radius: 50%;
display: flex;
align-items: center;
justify-content: center;
cursor: pointer;
transition: all 0.3s ease;
box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
z-index: 3;
}

.nav-arrow:hover {
background: white;
box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.nav-arrow svg {
width: 24px;
height: 24px;
}

.nav-prev {
left: 20px;
}

.nav-next {
right: 20px;
}

/* Zoom Button */
.zoom-button {
position: absolute;
top: 20px;
right: 20px;
background: rgba(255, 255, 255, 0.9);
border: none;
width: 40px;
height: 40px;
border-radius: 50%;
display: flex;
align-items: center;
justify-content: center;
cursor: pointer;
transition: all 0.3s ease;
box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.zoom-button:hover {
background: white;
transform: scale(1.1);
}

.zoom-button svg {
width: 20px;
height: 20px;
}

/* Thumbnail Gallery */
.thumbnail-gallery {
width: 100%;
}

.thumbnail-track {
display: flex;
gap: 0.75rem;
}

.thumbnail-item {
flex: 1;
aspect-ratio: 1;
border-radius: 12px;
overflow: hidden;
cursor: pointer;
position: relative;
border: 2px solid transparent;
transition: all 0.3s ease;
}

.thumbnail-item.active {
border-color: #FF6452;
}

.thumbnail-item:hover {
transform: scale(1.05);
}

.thumbnail-item img {
width: 100%;
height: 100%;
object-fit: cover;
}

.thumbnail-overlay {
position: absolute;
inset: 0;
background: linear-gradient(to bottom, transparent, rgba(0, 0, 0, 0.1));
opacity: 0;
transition: opacity 0.3s ease;
}

.thumbnail-item:hover .thumbnail-overlay {
opacity: 1;
}

/* Product Info Styles */
.product-info {
display: flex;
flex-direction: column;
gap: 2rem;
}

/* Product Header */
.product-header {
display: flex;
flex-direction: column;
gap: 0.75rem;
}

.product-badges {
display: flex;
gap: 0.5rem;
}

.badge {
padding: 0.25rem 0.75rem;
border-radius: 20px;
font-size: 0.75rem;
font-weight: 600;
text-transform: uppercase;
}

.badge-new {
background: linear-gradient(135deg, #FF6452, #ff8a80);
color: white;
}

.badge-hot {
background: linear-gradient(135deg, #fbbf24, #f59e0b);
color: white;
}

.product-title {
font-size: 2rem;
font-weight: 800;
color: #111827;
line-height: 1.2;
}

.product-subtitle {
display: flex;
align-items: center;
gap: 1rem;
color: #6b7280;
font-size: 0.875rem;
}

.brand-name {
font-weight: 600;
color: #FF6452;
}

/* Rating Section */
.rating-section {
display: flex;
flex-direction: column;
gap: 0.5rem;
padding: 1.5rem;
background: #f9fafb;
border-radius: 16px;
}

.rating-stars {
display: flex;
align-items: center;
gap: 0.5rem;
}

.stars {
display: flex;
gap: 0.25rem;
}

.star {
width: 20px;
height: 20px;
color: #e5e7eb;
}

.star.filled {
color: #fbbf24;
}

.star svg {
width: 100%;
height: 100%;
}

.rating-score {
font-size: 1.25rem;
font-weight: 700;
color: #111827;
}

.rating-info {
display: flex;
align-items: center;
gap: 0.75rem;
font-size: 0.875rem;
color: #6b7280;
}

.separator {
color: #d1d5db;
}

/* Price Section */
.price-section {
background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
border-radius: 20px;
padding: 1.5rem;
}

.price-wrapper {
display: flex;
flex-direction: column;
gap: 0.5rem;
}

.price-label {
font-size: 0.875rem;
color: #92400e;
font-weight: 500;
}

.price-content {
display: flex;
align-items: baseline;
gap: 1rem;
}

.current-price {
font-size: 2.5rem;
font-weight: 900;
color: #92400e;
}

.original-price {
font-size: 1.5rem;
color: #b45309;
text-decoration: line-through;
opacity: 0.7;
}

.discount-percentage {
background: #dc2626;
color: white;
padding: 0.25rem 0.75rem;
border-radius: 20px;
font-size: 0.875rem;
font-weight: 600;
}

.price-note {
display: flex;
align-items: center;
gap: 0.5rem;
margin-top: 0.75rem;
font-size: 0.75rem;
color: #92400e;
}

.price-note svg {
width: 16px;
height: 16px;
}

/* Product Options */
.product-options {
display: flex;
flex-direction: column;
gap: 1.5rem;
}

.option-group {
display: flex;
flex-direction: column;
gap: 0.75rem;
}

.option-header {
display: flex;
align-items: center;
gap: 0.5rem;
}

.option-label {
font-weight: 600;
color: #374151;
}

.option-value {
color: #111827;
font-weight: 700;
}

/* Color Options */
.color-options {
display: flex;
gap: 0.75rem;
flex-wrap: wrap;
}

.color-item {
display: flex;
align-items: center;
gap: 0.5rem;
padding: 0.5rem 1rem;
border: 2px solid #e5e7eb;
border-radius: 12px;
cursor: pointer;
transition: all 0.3s ease;
}

.color-item.selected {
border-color: #FF6452;
background: #fef2f2;
}

.color-item:hover {
border-color: #FF6452;
}

.color-circle {
width: 24px;
height: 24px;
border-radius: 50%;
border: 2px solid #e5e7eb;
}

.color-name {
font-size: 0.875rem;
font-weight: 500;
}

/* Size Options */
.size-options {
display: flex;
gap: 0.75rem;
flex-wrap: wrap;
}

.size-item {
padding: 0.75rem 1rem;
border: 2px solid #e5e7eb;
border-radius: 12px;
cursor: pointer;
transition: all 0.3s ease;
min-width: 60px;
text-align: center;
}

.size-item.selected {
border-color: #FF6452;
background: #fef2f2;
}

.size-item.disabled {
opacity: 0.5;
cursor: not-allowed;
background: #f3f4f6;
}

.size-item:hover:not(.disabled) {
border-color: #FF6452;
}

.size-name {
font-size: 0.875rem;
font-weight: 600;
}

.size-guide {
margin-top: 0.5rem;
}

.size-guide-btn {
display: flex;
align-items: center;
gap: 0.5rem;
background: none;
border: none;
color: #FF6452;
font-size: 0.875rem;
font-weight: 500;
cursor: pointer;
transition: all 0.2s ease;
}

.size-guide-btn:hover {
color: #dc2626;
}

.size-guide-btn svg {
width: 18px;
height: 18px;
}

/* Stock Status */
.stock-status {
display: flex;
align-items: center;
gap: 0.75rem;
padding: 1rem;
border-radius: 12px;
background: #fef2f2;
border: 1px solid #fecaca;
}

.stock-status.in-stock {
background: #f0fdf4;
border-color: #bbf7d0;
}

.stock-icon svg {
width: 24px;
height: 24px;
color: #dc2626;
}

.stock-status.in-stock .stock-icon svg {
color: #16a34a;
}

.stock-info {
display: flex;
align-items: center;
gap: 0.5rem;
}

.stock-label {
font-size: 0.875rem;
color: #6b7280;
}

.stock-value {
font-weight: 600;
color: #dc2626;
}

.stock-status.in-stock .stock-value {
color: #16a34a;
}

/* Purchase Section */
.purchase-section {
display: flex;
flex-direction: column;
gap: 1.5rem;
padding-top: 1.5rem;
border-top: 1px solid #e5e7eb;
}

.quantity-section {
display: flex;
align-items: center;
gap: 1rem;
}

.quantity-label {
font-weight: 600;
color: #374151;
}

.quantity-control {
display: flex;
align-items: center;
border: 2px solid #e5e7eb;
border-radius: 12px;
overflow: hidden;
}

.qty-btn {
width: 48px;
height: 48px;
background: white;
border: none;
cursor: pointer;
display: flex;
align-items: center;
justify-content: center;
transition: all 0.2s ease;
}

.qty-btn:hover:not(:disabled) {
background: #f3f4f6;
}

.qty-btn:disabled {
opacity: 0.5;
cursor: not-allowed;
}

.qty-btn svg {
width: 20px;
height: 20px;
}

.qty-input {
width: 80px;
height: 48px;
border: none;
text-align: center;
font-size: 1.125rem;
font-weight: 600;
color: #111827;
}

.qty-input:focus {
outline: none;
}

/* Action Buttons */
.action-buttons {
display: flex;
gap: 1rem;
}

.btn-add-cart,
.btn-buy-now {
flex: 1;
padding: 1rem 2rem;
border: none;
border-radius: 16px;
font-size: 1rem;
font-weight: 700;
cursor: pointer;
transition: all 0.3s ease;
display: flex;
align-items: center;
justify-content: center;
gap: 0.75rem;
}

.btn-add-cart {
background: white;
color: #FF6452;
border: 2px solid #FF6452;
}

.btn-add-cart:hover:not(:disabled) {
background: #FF6452;
color: white;
transform: translateY(-2px);
box-shadow: 0 8px 24px rgba(255, 100, 82, 0.3);
}

.btn-buy-now {
background: linear-gradient(135deg, #FF6452, #ff8a80);
color: white;
box-shadow: 0 4px 16px rgba(255, 100, 82, 0.3);
}

.btn-buy-now:hover:not(:disabled) {
transform: translateY(-2px);
box-shadow: 0 8px 24px rgba(255, 100, 82, 0.4);
}

.btn-add-cart:disabled,
.btn-buy-now:disabled {
opacity: 0.5;
cursor: not-allowed;
transform: none;
}

.btn-add-cart svg,
.btn-buy-now svg {
width: 20px;
height: 20px;
}

/* Benefits Section */
.benefits-section {
display: grid;
grid-template-columns: repeat(3, 1fr);
gap: 1rem;
padding: 1.5rem 0;
border-top: 1px solid #e5e7eb;
}

.benefit-item {
display: flex;
align-items: flex-start;
gap: 0.75rem;
}

.benefit-icon {
width: 40px;
height: 40px;
background: #fef2f2;
border-radius: 50%;
display: flex;
align-items: center;
justify-content: center;
flex-shrink: 0;
}

.benefit-icon svg {
width: 20px;
height: 20px;
color: #FF6452;
}

.benefit-content h4 {
font-size: 0.875rem;
font-weight: 600;
color: #111827;
margin-bottom: 0.25rem;
}

.benefit-content p {
font-size: 0.75rem;
color: #6b7280;
}

/* Support Section */
.support-section {
background: #f9fafb;
border-radius: 12px;
padding: 1rem;
display: flex;
align-items: center;
justify-content: space-between;
}

.support-header {
display: flex;
align-items: center;
gap: 0.5rem;
font-weight: 600;
color: #374151;
}

.support-header svg {
width: 20px;
height: 20px;
color: #FF6452;
}

.support-content {
display: flex;
align-items: center;
gap: 0.5rem;
}

.support-phone {
color: #FF6452;
font-weight: 700;
text-decoration: none;
font-size: 1.125rem;
}

.support-time {
color: #6b7280;
font-size: 0.875rem;
}

/* Product Tabs */
.product-tabs {
background: white;
border-radius: 24px;
padding: 2rem;
box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
margin-bottom: 3rem;
}

.tabs-header {
display: flex;
border-bottom: 2px solid #e5e7eb;
margin-bottom: 2rem;
}

.tab-button {
background: none;
border: none;
padding: 1rem 2rem;
font-weight: 600;
color: #6b7280;
cursor: pointer;
position: relative;
transition: all 0.3s ease;
}

.tab-button:hover {
color: #374151;
}

.tab-button.active {
color: #FF6452;
}

.tab-button.active::after {
content: '';
position: absolute;
bottom: -2px;
left: 0;
right: 0;
height: 2px;
background: #FF6452;
}

.tab-panel {
animation: fadeIn 0.3s ease;
min-height: 400px;
}

.tab-panel h3 {
font-size: 1.25rem;
font-weight: 700;
color: #111827;
margin-bottom: 1.5rem;
}

.product-description {
color: #374151;
line-height: 1.8;
}

.feature-list {
list-style: none;
padding: 0;
margin-top: 1rem;
}

.feature-list li {
padding: 0.75rem 0;
border-bottom: 1px solid #f3f4f6;
color: #374151;
}

.feature-list li strong {
color: #111827;
margin-right: 0.5rem;
}

.specs-table {
width: 100%;
border-collapse: collapse;
}

.specs-table td {
padding: 1rem;
border-bottom: 1px solid #f3f4f6;
}

.specs-table td:first-child {
font-weight: 600;
color: #6b7280;
width: 40%;
}

.specs-table td:last-child {
color: #111827;
}

.reviews-summary {
display: flex;
align-items: center;
gap: 3rem;
}

.rating-overview {
text-align: center;
}

.rating-score-large {
font-size: 3rem;
font-weight: 900;
color: #111827;
}

.rating-stars-large {
display: flex;
gap: 0.25rem;
margin: 0.5rem 0;
}

.rating-count {
color: #6b7280;
font-size: 0.875rem;
}

/* Similar Products */
.similar-products-section {
background: white;
border-radius: 24px;
padding: 3rem;
box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.section-header {
display: flex;
align-items: center;
justify-content: space-between;
margin-bottom: 2rem;
}

.section-title {
font-size: 1.75rem;
font-weight: 800;
color: #111827;
}

.view-all-link {
display: flex;
align-items: center;
gap: 0.5rem;
color: #FF6452;
text-decoration: none;
font-weight: 600;
transition: all 0.2s ease;
}

.view-all-link:hover {
gap: 0.75rem;
}

.view-all-link svg {
width: 20px;
height: 20px;
}

.similar-products-grid {
display: grid;
grid-template-columns: repeat(4, 1fr);
gap: 1.5rem;
}

.similar-product-card {
background: #f9fafb;
border-radius: 16px;
overflow: hidden;
cursor: pointer;
transition: all 0.3s ease;
}

.similar-product-card:hover {
transform: translateY(-4px);
box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
}

.product-image-wrapper {
position: relative;
aspect-ratio: 1;
overflow: hidden;
background: white;
}

.product-image-wrapper img {
width: 100%;
height: 100%;
object-fit: cover;
transition: transform 0.3s ease;
}

.similar-product-card:hover .product-image-wrapper img {
transform: scale(1.05);
}

.product-overlay {
position: absolute;
inset: 0;
background: rgba(0, 0, 0, 0.7);
display: flex;
align-items: center;
justify-content: center;
opacity: 0;
transition: opacity 0.3s ease;
}

.similar-product-card:hover .product-overlay {
opacity: 1;
}

.quick-view {
background: white;
color: #111827;
border: none;
padding: 0.75rem 1.5rem;
border-radius: 8px;
font-weight: 600;
cursor: pointer;
transform: translateY(20px);
transition: transform 0.3s ease;
}

.similar-product-card:hover .quick-view {
transform: translateY(0);
}

.product-content {
padding: 1.25rem;
}

.product-content .product-name {
font-size: 1rem;
font-weight: 600;
color: #111827;
margin-bottom: 0.5rem;
display: -webkit-box;
-webkit-line-clamp: 2;
-webkit-box-orient: vertical;
overflow: hidden;
}

.product-content .product-price {
font-size: 1.25rem;
font-weight: 700;
color: #FF6452;
margin-bottom: 0.5rem;
}

.product-content .product-rating {
display: flex;
align-items: center;
gap: 0.5rem;
}

.stars-mini {
display: flex;
gap: 0.1rem;
}

.stars-mini .star {
font-size: 0.875rem;
color: #fbbf24;
}

.rating-text {
font-size: 0.75rem;
color: #6b7280;
}

/* Loading State */
.loading-container {
min-height: 60vh;
display: flex;
align-items: center;
justify-content: center;
}

.loading-content {
text-align: center;
}

.loading-spinner {
width: 80px;
height: 80px;
margin: 0 auto;
position: relative;
}

.spinner-ring {
box-sizing: border-box;
display: block;
position: absolute;
width: 64px;
height: 64px;
margin: 8px;
border: 8px solid #fff;
border-radius: 50%;
animation: spinner-ring 1.2s cubic-bezier(0.5, 0, 0.5, 1) infinite;
border-color: #FF6452 transparent transparent transparent;
}

.spinner-ring:nth-child(1) {
animation-delay: -0.45s;
}

.spinner-ring:nth-child(2) {
animation-delay: -0.3s;
}

.spinner-ring:nth-child(3) {
animation-delay: -0.15s;
}
@keyframes spinner-ring {
0% {
  transform: rotate(0deg);
}
100% {
  transform: rotate(360deg);
}
}

/* Breadcrumb */
.breadcrumb-container {
background: white;
border-bottom: 1px solid #e5e7eb;
position: sticky;
top: 0;
z-index: 100;
}

.breadcrumb {
max-width: 1200px;
margin: 0 auto;
padding: 1rem 20px;
display: flex;
align-items: center;
gap: 0.5rem;
font-size: 0.875rem;
}

/* ===== ĐÃ THÊM CSS CHO CHATBOT & REVIEW SYSTEM ===== */
.chatbot-container {
z-index: 9999 !important;
}

.review-modal-overlay {
z-index: 9998 !important;
}


/* Đảm bảo tab reviews có đủ không gian */
.tab-panel {
min-height: 400px;
}

/* Responsive cho review system */
@media (max-width: 768px) {
.review-system {
  padding: 1rem;
  margin-top: 1rem;
}
}

/* Responsive Design */
@media (max-width: 1024px) {

.product-wrapper {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3rem;
  background: white;
  border-radius: 24px;
  padding: 3rem;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  margin-bottom: 3rem;
}

/* Gallery Styles */
.product-gallery {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.main-image-container {
  position: relative;
  background: #f9fafb;
  border-radius: 20px;
  overflow: hidden;
  aspect-ratio: 1;
}

.image-slider {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.main-image {
  max-width: 90%;
  max-height: 90%;
  object-fit: contain;
  filter: drop-shadow(0 20px 40px rgba(0, 0, 0, 0.1));
}

/* Slide Progress Bar */
.slide-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: rgba(0, 0, 0, 0.1);
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #FF6452, #ff8a80);
  transition: width 0.1s linear;
}

/* Slide Indicators */
.slide-indicators {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  z-index: 3;
}

.indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s ease;
}

.indicator.active {
  width: 24px;
  border-radius: 4px;
  background: white;
}

/* Navigation Arrows */
.nav-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(255, 255, 255, 0.9);
  border: none;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 3;
}

.nav-arrow:hover {
  background: white;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.nav-arrow svg {
  width: 24px;
  height: 24px;
}

.nav-prev {
  left: 20px;
}

.nav-next {
  right: 20px;
}

/* Zoom Button */
.zoom-button {
  position: absolute;
  top: 20px;
  right: 20px;
  background: rgba(255, 255, 255, 0.9);
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.zoom-button:hover {
  background: white;
  transform: scale(1.1);
}

.zoom-button svg {
  width: 20px;
  height: 20px;
}

/* Thumbnail Gallery */
.thumbnail-gallery {
  width: 100%;
}

.thumbnail-track {
  display: flex;
  gap: 0.75rem;
}

.thumbnail-item {
  flex: 1;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.thumbnail-item.active {
  border-color: #FF6452;
}

.thumbnail-item:hover {
  transform: scale(1.05);
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumbnail-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, transparent, rgba(0, 0, 0, 0.1));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.thumbnail-item:hover .thumbnail-overlay {
  opacity: 1;
}

/* Product Info Styles */
.product-info {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

/* Product Header */
.product-header {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.product-badges {
  display: flex;
  gap: 0.5rem;
}

.badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
}

.badge-new {
  background: linear-gradient(135deg, #FF6452, #ff8a80);
  color: white;
}

.badge-hot {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  color: white;
}

.product-title {
  font-size: 2rem;
  font-weight: 800;
  color: #111827;
  line-height: 1.2;
}

.product-subtitle {
  display: flex;
  align-items: center;
  gap: 1rem;
  color: #6b7280;
  font-size: 0.875rem;
}

.brand-name {
  font-weight: 600;
  color: #FF6452;
}

/* Rating Section */
.rating-section {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 1.5rem;
  background: #f9fafb;
  border-radius: 16px;
}

.rating-stars {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.stars {
  display: flex;
  gap: 0.25rem;
}

.star {
  width: 20px;
  height: 20px;
  color: #e5e7eb;
}

.star.filled {
  color: #fbbf24;
}

.star svg {
  width: 100%;
  height: 100%;
}

.rating-score {
  font-size: 1.25rem;
  font-weight: 700;
  color: #111827;
}

.rating-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.875rem;
  color: #6b7280;
}

.separator {
  color: #d1d5db;
}

/* Price Section */
.price-section {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 20px;
  padding: 1.5rem;
}

.price-wrapper {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.price-label {
  font-size: 0.875rem;
  color: #92400e;
  font-weight: 500;
}

.price-content {
  display: flex;
  align-items: baseline;
  gap: 1rem;
}

.current-price {
  font-size: 2.5rem;
  font-weight: 900;
  color: #92400e;
}

.original-price {
  font-size: 1.5rem;
  color: #b45309;
  text-decoration: line-through;
  opacity: 0.7;
}

.discount-percentage {
  background: #dc2626;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
}

.price-note {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.75rem;
  font-size: 0.75rem;
  color: #92400e;
}

.price-note svg {
  width: 16px;
  height: 16px;
}

/* Product Options */
.product-options {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.option-group {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.option-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.option-label {
  font-weight: 600;
  color: #374151;
}

.option-value {
  color: #111827;
  font-weight: 700;
}

/* Color Options */
.color-options {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.color-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.color-item.selected {
  border-color: #FF6452;
  background: #fef2f2;
}

.color-item:hover {
  border-color: #FF6452;
}

.color-circle {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid #e5e7eb;
}

.color-name {
  font-size: 0.875rem;
  font-weight: 500;
}

/* Size Options */
.size-options {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.size-item {
  padding: 0.75rem 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 60px;
  text-align: center;
}

.size-item.selected {
  border-color: #FF6452;
  background: #fef2f2;
}

.size-item.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f3f4f6;
}

.size-item:hover:not(.disabled) {
  border-color: #FF6452;
}

.size-name {
  font-size: 0.875rem;
  font-weight: 600;
}

.size-guide {
  margin-top: 0.5rem;
}

.size-guide-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: none;
  border: none;
  color: #FF6452;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.size-guide-btn:hover {
  color: #dc2626;
}

.size-guide-btn svg {
  width: 18px;
  height: 18px;
}

/* Stock Status */
.stock-status {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  border-radius: 12px;
  background: #fef2f2;
  border: 1px solid #fecaca;
}

.stock-status.in-stock {
  background: #f0fdf4;
  border-color: #bbf7d0;
}

.stock-icon svg {
  width: 24px;
  height: 24px;
  color: #dc2626;
}

.stock-status.in-stock .stock-icon svg {
  color: #16a34a;
}

.stock-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.stock-label {
  font-size: 0.875rem;
  color: #6b7280;
}

.stock-value {
  font-weight: 600;
  color: #dc2626;
}

.stock-status.in-stock .stock-value {
  color: #16a34a;
}

/* Purchase Section */
.purchase-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e5e7eb;
}

.quantity-section {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.quantity-label {
  font-weight: 600;
  color: #374151;
}

.quantity-control {
  display: flex;
  align-items: center;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
}

.qty-btn {
  width: 48px;
  height: 48px;
  background: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.qty-btn:hover:not(:disabled) {
  background: #f3f4f6;
}

.qty-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.qty-btn svg {
  width: 20px;
  height: 20px;
}

.qty-input {
  width: 80px;
  height: 48px;
  border: none;
  text-align: center;
  font-size: 1.125rem;
  font-weight: 600;
  color: #111827;
}

.qty-input:focus {
  outline: none;
}

/* Action Buttons */
.action-buttons {
  display: flex;
  gap: 1rem;
}

.btn-add-cart,
.btn-buy-now {
  flex: 1;
  padding: 1rem 2rem;
  border: none;
  border-radius: 16px;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
}

.btn-add-cart {
  background: white;
  color: #FF6452;
  border: 2px solid #FF6452;
}

.btn-add-cart:hover:not(:disabled) {
  background: #FF6452;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(255, 100, 82, 0.3);
}

.btn-buy-now {
  background: linear-gradient(135deg, #FF6452, #ff8a80);
  color: white;
  box-shadow: 0 4px 16px rgba(255, 100, 82, 0.3);
}

.btn-buy-now:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(255, 100, 82, 0.4);
}

.btn-add-cart:disabled,
.btn-buy-now:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.btn-add-cart svg,
.btn-buy-now svg {
  width: 20px;
  height: 20px;
}

/* Benefits Section */
.benefits-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  padding: 1.5rem 0;
  border-top: 1px solid #e5e7eb;
}

.benefit-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}

.benefit-icon {
  width: 40px;
  height: 40px;
  background: #fef2f2;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.benefit-icon svg {
  width: 20px;
  height: 20px;
  color: #FF6452;
}

.benefit-content h4 {
  font-size: 0.875rem;
  font-weight: 600;
  color: #111827;
  margin-bottom: 0.25rem;
}

.benefit-content p {
  font-size: 0.75rem;
  color: #6b7280;
}

/* Support Section */
.support-section {
  background: #f9fafb;
  border-radius: 12px;
  padding: 1rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.support-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  color: #374151;
}

.support-header svg {
  width: 20px;
  height: 20px;
  color: #FF6452;
}

.support-content {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.support-phone {
  color: #FF6452;
  font-weight: 700;
  text-decoration: none;
  font-size: 1.125rem;
}

.support-time {
  color: #6b7280;
  font-size: 0.875rem;
}

/* Product Tabs */
.product-tabs {
  background: white;
  border-radius: 24px;
  padding: 2rem;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  margin-bottom: 3rem;
}

.tabs-header {
  display: flex;
  border-bottom: 2px solid #e5e7eb;
  margin-bottom: 2rem;
}

.tab-button {
  background: none;
  border: none;
  padding: 1rem 2rem;
  font-weight: 600;
  color: #6b7280;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;
}

.tab-button:hover {
  color: #374151;
}

.tab-button.active {
  color: #FF6452;
}

.tab-button.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  background: #FF6452;
}

.tab-panel {
  animation: fadeIn 0.3s ease;
}

.tab-panel h3 {
  font-size: 1.25rem;
  font-weight: 700;
  color: #111827;
  margin-bottom: 1.5rem;
}

.product-description {
  color: #374151;
  line-height: 1.8;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin-top: 1rem;
}

.feature-list li {
  padding: 0.75rem 0;
  border-bottom: 1px solid #f3f4f6;
  color: #374151;
}

.feature-list li strong {
  color: #111827;
  margin-right: 0.5rem;
}

.specs-table {
  width: 100%;
  border-collapse: collapse;
}

.specs-table td {
  padding: 1rem;
  border-bottom: 1px solid #f3f4f6;
}

.specs-table td:first-child {
  font-weight: 600;
  color: #6b7280;
  width: 40%;
}

.specs-table td:last-child {
  color: #111827;
}

.reviews-summary {
  display: flex;
  align-items: center;
  gap: 3rem;
}

.rating-overview {
  text-align: center;
}

.rating-score-large {
  font-size: 3rem;
  font-weight: 900;
  color: #111827;
}

.rating-stars-large {
  display: flex;
  gap: 0.25rem;
  margin: 0.5rem 0;
}

.rating-count {
  color: #6b7280;
  font-size: 0.875rem;
}

/* Similar Products */
.similar-products-section {
  background: white;
  border-radius: 24px;
  padding: 3rem;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2rem;
}

.section-title {
  font-size: 1.75rem;
  font-weight: 800;
  color: #111827;
}

.view-all-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #FF6452;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.2s ease;
}

}

</style>




