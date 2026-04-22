<script setup>
import { arrowRight } from "@/assets/icons";
import Image from "@/assets/images/shoe8.svg";
import UserButton from "@/components/user/UserButton.vue";
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";

// Khởi tạo router
const router = useRouter();

// Animation counters
const totalProducts = ref(0);
const happyCustomers = ref(0);
const yearsExperience = ref(0);
const brandsPartner = ref(0);

// Animate numbers on mount
onMounted(() => {
  animateCounter(totalProducts, 1500, 2000);
  animateCounter(happyCustomers, 50000, 2500);
  animateCounter(yearsExperience, 15, 1500);
  animateCounter(brandsPartner, 200, 2200);
});

const animateCounter = (ref, target, duration) => {
  const step = target / (duration / 16);
  const timer = setInterval(() => {
    ref.value += step;
    if (ref.value >= target) {
      ref.value = target;
      clearInterval(timer);
    }
  }, 16);
};

const formatNumber = (num) => {
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K+';
  }
  return Math.floor(num).toString() + '+';
};

const viewAllProducts = () => {
  // Smooth scroll to top
  window.scrollTo({ top: 0, behavior: 'smooth' });
  router.push('/products');
};

const scrollToProducts = () => {
  // Scroll to products section (if exists)
  const productsSection = document.querySelector('#products-section');
  if (productsSection) {
    productsSection.scrollIntoView({ behavior: 'smooth' });
  } else {
    router.push('/products');
  }
};

// Features data
const features = [
  {
    icon: '🏆',
    title: 'Chất Lượng Hàng Đầu',
    description: 'Sản phẩm được kiểm định nghiêm ngặt theo tiêu chuẩn quốc tế'
  },
  {
    icon: '🚚',
    title: 'Giao Hàng Nhanh',
    description: 'Freeship toàn quốc, giao hàng trong 24h tại TP.HCM và Hà Nội'
  },
  {
    icon: '🔄',
    title: 'Đổi Trả Dễ Dàng',
    description: 'Đổi trả miễn phí trong 30 ngày, không cần lý do'
  },
  {
    icon: '💎',
    title: 'Bảo Hành Chính Hãng',
    description: 'Bảo hành 12 tháng, sửa chữa miễn phí tại hệ thống cửa hàng'
  }
];

// Stats data
const stats = [
  { number: totalProducts, label: 'Sản Phẩm', suffix: '+' },
  { number: happyCustomers, label: 'Khách Hàng Hài Lòng', suffix: '+' },
  { number: yearsExperience, label: 'Năm Kinh Nghiệm', suffix: '+' },
  { number: brandsPartner, label: 'Thương Hiệu Đối Tác', suffix: '+' }
];

// Testimonials
const testimonials = [
  {
    name: 'Nguyễn Minh Tú',
    role: 'CEO Tech Startup',
    avatar: 'https://ui-avatars.com/api/?name=Nguyen+Minh+Tu&background=FF6452&color=fff',
    comment: 'Chất lượng tuyệt vời, thiết kế sang trọng. Đây là lựa chọn hàng đầu của tôi!'
  },
  {
    name: 'Trần Thị Hương',
    role: 'Fashion Blogger',
    avatar: 'https://ui-avatars.com/api/?name=Tran+Thi+Huong&background=4F46E5&color=fff',
    comment: 'Phong cách hiện đại, thoải mái cả ngày. Rất đáng đầu tư!'
  }
];
</script>

<template>
  <section class="hero-section">
    <!-- Main Hero Content -->
    <div
      v-motion
      :initial="{ opacity: 0, y: 200 }"
      :visibleOnce="{ opacity: 1, y: 0 }"
      class="main-hero max-container"
    >
      <!-- Left Content -->
      <div class="hero-content">
        <!-- Badge -->
        <div class="hero-badge">
          <span class="badge-text">✨ #1 Thương Hiệu Giày Chất Lượng Việt Nam</span>
        </div>

        <!-- Main Heading -->
        <h1 class="hero-heading">
          Giày Chất Lượng
          <span class="gradient-text">Dành Cho Bạn</span>
          <div class="heading-decoration">
            <div class="decoration-line"></div>
            <div class="decoration-dot"></div>
          </div>
        </h1>

        <!-- Description -->
        <div class="hero-description">
          <p class="main-description">
            Đảm bảo sự thoải mái và phong cách cao cấp, những đôi giày được chế tác tỉ mỉ 
            của chúng tôi được thiết kế để nâng cao trải nghiệm của bạn, mang đến cho bạn 
            <strong>chất lượng vô song</strong>, sự đổi mới và một chút thanh lịch.
          </p>
          <p class="sub-description">
            Sự tận tâm của chúng tôi đến từng chi tiết và sự xuất sắc đảm bảo sự hài lòng của bạn
          </p>
        </div>

        <!-- Key Features -->
        <div class="key-features">
          <div class="feature-item" v-for="(feature, index) in features.slice(0, 2)" :key="index">
            <div class="feature-icon">{{ feature.icon }}</div>
            <div class="feature-content">
              <h4>{{ feature.title }}</h4>
              <p>{{ feature.description }}</p>
            </div>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="hero-actions">
          <UserButton 
            :icon-url="arrowRight" 
            @click="viewAllProducts"
            class="primary-btn"
          >
            Mua Ngay
          </UserButton>
          <button @click="scrollToProducts" class="secondary-btn">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="currentColor">
              <path d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z"/>
            </svg>
            Xem Bộ Sưu Tập
          </button>
        </div>

        <!-- Social Proof -->
        <div class="social-proof">
          <div class="rating-section">
            <div class="stars">
              <span v-for="i in 5" :key="i" class="star">⭐</span>
            </div>
            <div class="rating-text">
              <span class="rating-score">4.9/5</span>
              <span class="rating-count">(2,847 đánh giá)</span>
            </div>
          </div>
          <div class="trust-badges">
            <div class="trust-item">🏆 Thương hiệu tin cậy</div>
            <div class="trust-item">🛡️ Bảo hành chính hãng</div>
          </div>
        </div>
      </div>

      <!-- Right Content - Product Image -->
      <div class="hero-image">
        <div class="image-container">
          <!-- Floating Elements -->
          <div class="floating-card price-card">
            <div class="card-icon">💰</div>
            <div class="card-content">
              <div class="card-title">Giá Tốt Nhất</div>
              <div class="card-value">Tiết kiệm 30%</div>
            </div>
          </div>
          
          <div class="floating-card shipping-card">
            <div class="card-icon">🚀</div>
            <div class="card-content">
              <div class="card-title">Giao Hàng</div>
              <div class="card-value">Miễn Phí</div>
            </div>
          </div>

          <!-- Main Product Image -->
          <div class="product-showcase">
            <img
              :src="Image"
              alt="Premium Shoes Collection"
              class="hero-product-image"
            />
            <div class="image-glow"></div>
          </div>

          <!-- Decorative Elements -->
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
          <div class="decoration-circle circle-3"></div>
        </div>
      </div>
    </div>

    <!-- Stats Section -->
    <div 
      v-motion
      :initial="{ opacity: 0, y: 50 }"
      :visibleOnce="{ opacity: 1, y: 0 }"
      class="stats-section max-container"
    >
      <div class="stats-grid">
        <div 
          v-for="(stat, index) in stats" 
          :key="index"
          class="stat-item"
          :style="{ animationDelay: `${index * 0.1}s` }"
        >
          <div class="stat-number">
            {{ index < 2 ? formatNumber(stat.number.value) : Math.floor(stat.number.value) + stat.suffix }}
          </div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- Features Grid -->
    <div 
      v-motion
      :initial="{ opacity: 0, y: 50 }"
      :visibleOnce="{ opacity: 1, y: 0 }"
      class="features-section max-container"
    >
      <h2 class="section-title">Tại Sao Chọn Chúng Tôi?</h2>
      <div class="features-grid">
        <div 
          v-for="(feature, index) in features" 
          :key="index"
          class="feature-card"
          :style="{ animationDelay: `${index * 0.1}s` }"
        >
          <div class="feature-card-icon">{{ feature.icon }}</div>
          <h3 class="feature-card-title">{{ feature.title }}</h3>
          <p class="feature-card-description">{{ feature.description }}</p>
        </div>
      </div>
    </div>

    <!-- Testimonials -->
    <div 
      v-motion
      :initial="{ opacity: 0, y: 50 }"
      :visibleOnce="{ opacity: 1, y: 0 }"
      class="testimonials-section max-container"
    >
      <h2 class="section-title">Khách Hàng Nói Gì Về Chúng Tôi</h2>
      <div class="testimonials-grid">
        <div 
          v-for="(testimonial, index) in testimonials" 
          :key="index"
          class="testimonial-card"
        >
          <div class="testimonial-content">
            <div class="quote-icon">"</div>
            <p class="testimonial-text">{{ testimonial.comment }}</p>
          </div>
          <div class="testimonial-author">
            <img :src="testimonial.avatar" :alt="testimonial.name" class="author-avatar" />
            <div class="author-info">
              <div class="author-name">{{ testimonial.name }}</div>
              <div class="author-role">{{ testimonial.role }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style lang="scss" scoped>
.hero-section {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #fafafa 0%, #f0f0f0 100%);
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
      radial-gradient(circle at 20% 20%, rgba(255, 100, 82, 0.1) 0%, transparent 50%),
      radial-gradient(circle at 80% 80%, rgba(79, 70, 229, 0.1) 0%, transparent 50%);
    pointer-events: none;
  }
}

// Main Hero
.main-hero {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4rem;
  align-items: center;
  min-height: 90vh;
  padding: 4rem 2rem;
  position: relative;
  z-index: 1;
}

// Hero Content
.hero-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.hero-badge {
  display: inline-flex;
  width: fit-content;
  
  .badge-text {
    background: linear-gradient(135deg, #FF6452, #ff8a80);
    color: white;
    padding: 0.75rem 1.5rem;
    border-radius: 50px;
    font-size: 0.875rem;
    font-weight: 600;
    box-shadow: 0 4px 20px rgba(255, 100, 82, 0.3);
    animation: pulse 2s infinite;
  }
}

.hero-heading {
  position: relative;
  font-size: 4rem;
  font-weight: 900;
  line-height: 1.1;
  color: #1a202c;
  font-family: 'Montserrat', sans-serif;
  
  .gradient-text {
    background: linear-gradient(135deg, #FF6452, #ff8a80, #ffd700);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
  
  .heading-decoration {
    position: absolute;
    bottom: -10px;
    left: 0;
    display: flex;
    align-items: center;
    gap: 1rem;
    
    .decoration-line {
      width: 100px;
      height: 4px;
      background: linear-gradient(90deg, #FF6452, transparent);
      border-radius: 2px;
    }
    
    .decoration-dot {
      width: 12px;
      height: 12px;
      background: #FF6452;
      border-radius: 50%;
      animation: bounce 2s infinite;
    }
  }
}

.hero-description {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  
  .main-description {
    font-size: 1.125rem;
    line-height: 1.8;
    color: #4a5568;
    
    strong {
      color: #FF6452;
      font-weight: 700;
    }
  }
  
  .sub-description {
    font-size: 1rem;
    color: #718096;
    font-style: italic;
  }
}

.key-features {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  margin: 1rem 0;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1rem;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  }
  
  .feature-icon {
    font-size: 2rem;
    flex-shrink: 0;
  }
  
  .feature-content {
    h4 {
      font-size: 0.875rem;
      font-weight: 700;
      color: #1a202c;
      margin-bottom: 0.25rem;
    }
    
    p {
      font-size: 0.75rem;
      color: #718096;
      line-height: 1.4;
    }
  }
}

// Hero Actions
.hero-actions {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  margin: 1rem 0;
}

.primary-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 2rem;
  background: linear-gradient(135deg, #FF6452, #ff8a80);
  color: white;
  border: none;
  border-radius: 50px;
  font-weight: 700;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(255, 100, 82, 0.3);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 30px rgba(255, 100, 82, 0.4);
  }
  
  &:active {
    transform: translateY(0);
  }
}

.secondary-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem 1.5rem;
  background: transparent;
  color: #4a5568;
  border: 2px solid #e2e8f0;
  border-radius: 50px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  
  .btn-icon {
    width: 20px;
    height: 20px;
  }
  
  &:hover {
    border-color: #FF6452;
    color: #FF6452;
    transform: translateY(-2px);
  }
}

// Social Proof
.social-proof {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 1.5rem 0;
  border-top: 1px solid #e2e8f0;
}

.rating-section {
  display: flex;
  align-items: center;
  gap: 1rem;
  
  .stars {
    display: flex;
    gap: 0.25rem;
    
    .star {
      font-size: 1.25rem;
    }
  }
  
  .rating-text {
    .rating-score {
      font-weight: 700;
      color: #1a202c;
      margin-right: 0.5rem;
    }
    
    .rating-count {
      color: #718096;
      font-size: 0.875rem;
    }
  }
}

.trust-badges {
  display: flex;
  gap: 1rem;
  
  .trust-item {
    font-size: 0.875rem;
    color: #4a5568;
    font-weight: 500;
  }
}

// Hero Image
.hero-image {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 600px;
}

.image-container {
  position: relative;
  width: 100%;
  height: 100%;
}

.product-showcase {
  position: relative;
  z-index: 2;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  
  .hero-product-image {
    max-width: 500px;
    height: auto;
    object-fit: contain;
    filter: drop-shadow(0 20px 40px rgba(0, 0, 0, 0.1));
    animation: float 3s ease-in-out infinite;
  }
  
  .image-glow {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, rgba(255, 100, 82, 0.2) 0%, transparent 70%);
    border-radius: 50%;
    z-index: -1;
    animation: pulse 4s ease-in-out infinite;
  }
}

.floating-card {
  position: absolute;
  background: white;
  border-radius: 16px;
  padding: 1rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  z-index: 3;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  animation: float 2s ease-in-out infinite;
  
  .card-icon {
    font-size: 1.5rem;
  }
  
  .card-content {
    .card-title {
      font-size: 0.75rem;
      color: #718096;
      font-weight: 500;
    }
    
    .card-value {
      font-size: 0.875rem;
      font-weight: 700;
      color: #1a202c;
    }
  }
}

.price-card {
  top: 20%;
  left: 10%;
  animation-delay: -0.5s;
}

.shipping-card {
  bottom: 20%;
  right: 10%;
  animation-delay: -1s;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  z-index: 1;
  
  &.circle-1 {
    top: 10%;
    right: 20%;
    width: 60px;
    height: 60px;
    background: linear-gradient(135deg, #FF6452, #ff8a80);
    opacity: 0.6;
    animation: bounce 3s ease-in-out infinite;
  }
  
  &.circle-2 {
    bottom: 30%;
    left: 15%;
    width: 40px;
    height: 40px;
    background: linear-gradient(135deg, #4F46E5, #7C3AED);
    opacity: 0.5;
    animation: bounce 2.5s ease-in-out infinite reverse;
  }
  
  &.circle-3 {
    top: 60%;
    right: 5%;
    width: 80px;
    height: 80px;
    background: linear-gradient(135deg, #10B981, #34D399);
    opacity: 0.3;
    animation: pulse 4s ease-in-out infinite;
  }
}

// Stats Section
.stats-section {
  padding: 4rem 2rem;
  background: white;
  border-radius: 24px;
  margin: 2rem auto;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 2rem;
}

.stat-item {
  text-align: center;
  padding: 1.5rem;
  border-radius: 16px;
  transition: all 0.3s ease;
  animation: slideUp 0.6s ease-out forwards;
  opacity: 0;
  
  &:hover {
    transform: translateY(-4px);
    background: linear-gradient(135deg, #FF6452, #ff8a80);
    color: white;
  }
  
  .stat-number {
    font-size: 3rem;
    font-weight: 900;
    background: linear-gradient(135deg, #FF6452, #ff8a80);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    margin-bottom: 0.5rem;
  }
  
  .stat-label {
    font-size: 0.875rem;
    font-weight: 600;
    color: #4a5568;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }
}

// Features Section
.features-section {
  padding: 4rem 2rem;
}

.section-title {
  text-align: center;
  font-size: 2.5rem;
  font-weight: 800;
  color: #1a202c;
  margin-bottom: 3rem;
  
  &::after {
    content: '';
    display: block;
    width: 80px;
    height: 4px;
    background: linear-gradient(90deg, #FF6452, #ff8a80);
    margin: 1rem auto;
    border-radius: 2px;
  }
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 2rem;
}

.feature-card {
  background: white;
  padding: 2rem;
  border-radius: 20px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  animation: slideUp 0.6s ease-out forwards;
  opacity: 0;
  
  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 20px 50px rgba(0, 0, 0, 0.15);
  }
  
  .feature-card-icon {
    font-size: 3rem;
    margin-bottom: 1rem;
  }
  
  .feature-card-title {
    font-size: 1.25rem;
    font-weight: 700;
    color: #1a202c;
    margin-bottom: 1rem;
  }
  
  .feature-card-description {
    color: #718096;
    line-height: 1.6;
  }
}

// Testimonials Section
.testimonials-section {
  padding: 4rem 2rem;
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  border-radius: 24px;
  margin: 2rem auto;
}

.testimonials-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.testimonial-card {
  background: white;
  padding: 2rem;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
  }
}

.testimonial-content {
  position: relative;
  margin-bottom: 1.5rem;
  
  .quote-icon {
    position: absolute;
    top: -10px;
    left: -10px;
    font-size: 3rem;
    color: #FF6452;
    opacity: 0.3;
  }
  
  .testimonial-text {
    font-style: italic;
    color: #4a5568;
    line-height: 1.6;
    margin-left: 1rem;
  }
}

.testimonial-author {
  display: flex;
  align-items: center;
  gap: 1rem;
  
  .author-avatar {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    border: 2px solid #FF6452;
  }
  
  .author-info {
    .author-name {
      font-weight: 700;
      color: #1a202c;
    }
    
    .author-role {
      font-size: 0.875rem;
      color: #718096;
    }
  }
}

// Animations
@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.05); opacity: 0.8; }
}

@keyframes bounce {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-15px); }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// Responsive Design
// Responsive Design (continued)
@media (max-width: 1024px) {
  .main-hero {
    grid-template-columns: 1fr;
    text-align: center;
    gap: 3rem;
  }
  
  .hero-heading {
    font-size: 3rem;
    
    .heading-decoration {
      left: 50%;
      transform: translateX(-50%);
    }
  }
  
  .hero-content {
    align-items: center;
  }
  
  .hero-badge {
    margin: 0 auto;
  }
  
  .hero-description {
    text-align: center;
    max-width: 600px;
  }
  
  .key-features {
    grid-template-columns: 1fr;
    max-width: 400px;
    margin: 1rem auto;
  }
  
  .hero-actions {
    justify-content: center;
  }
  
  .social-proof {
    align-items: center;
  }
  
  .trust-badges {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .hero-image {
    order: -1;
    height: 400px;
  }
  
  .product-showcase {
    .hero-product-image {
      max-width: 350px;
    }
  }
  
  .floating-card {
    padding: 0.75rem;
    
    .card-icon {
      font-size: 1.25rem;
    }
    
    .card-content {
      .card-title {
        font-size: 0.7rem;
      }
      
      .card-value {
        font-size: 0.8rem;
      }
    }
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .main-hero {
    padding: 3rem 1.5rem;
    min-height: auto;
  }
  
  .hero-heading {
    font-size: 2.5rem;
  }
  
  .hero-description {
    .main-description {
      font-size: 1rem;
    }
    
    .sub-description {
      font-size: 0.875rem;
    }
  }
  
  .hero-actions {
    flex-direction: column;
    width: 100%;
    
    .primary-btn,
    .secondary-btn {
      width: 100%;
      justify-content: center;
    }
  }
  
  .hero-image {
    height: 350px;
  }
  
  .product-showcase {
    .hero-product-image {
      max-width: 300px;
    }
    
    .image-glow {
      width: 300px;
      height: 300px;
    }
  }
  
  .price-card {
    top: 10%;
    left: 5%;
  }
  
  .shipping-card {
    bottom: 10%;
    right: 5%;
  }
  
  .decoration-circle {
    &.circle-1 {
      width: 40px;
      height: 40px;
    }
    
    &.circle-2 {
      width: 30px;
      height: 30px;
    }
    
    &.circle-3 {
      width: 60px;
      height: 60px;
    }
  }
  
  .stats-section {
    padding: 3rem 1.5rem;
  }
  
  .stat-item {
    padding: 1rem;
    
    .stat-number {
      font-size: 2rem;
    }
    
    .stat-label {
      font-size: 0.75rem;
    }
  }
  
  .features-section {
    padding: 3rem 1.5rem;
  }
  
  .section-title {
    font-size: 2rem;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .testimonials-section {
    padding: 3rem 1.5rem;
  }
  
  .testimonials-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .main-hero {
    padding: 2rem 1rem;
  }
  
  .hero-heading {
    font-size: 2rem;
  }
  
  .hero-badge {
    .badge-text {
      font-size: 0.75rem;
      padding: 0.5rem 1rem;
    }
  }
  
  .key-features {
    gap: 1rem;
  }
  
  .feature-item {
    padding: 0.75rem;
    
    .feature-icon {
      font-size: 1.5rem;
    }
    
    .feature-content {
      h4 {
        font-size: 0.8rem;
      }
      
      p {
        font-size: 0.7rem;
      }
    }
  }
  
  .social-proof {
    .rating-section {
      flex-direction: column;
      gap: 0.5rem;
      
      .stars {
        .star {
          font-size: 1rem;
        }
      }
    }
    
    .trust-badges {
      .trust-item {
        font-size: 0.75rem;
      }
    }
  }
  
  .hero-image {
    height: 300px;
  }
  
  .product-showcase {
    .hero-product-image {
      max-width: 250px;
    }
  }
  
  .floating-card {
    display: none; // Hide floating cards on mobile for cleaner look
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .stat-item {
    .stat-number {
      font-size: 1.75rem;
    }
  }
  
  .feature-card {
    padding: 1.5rem;
    
    .feature-card-icon {
      font-size: 2.5rem;
    }
    
    .feature-card-title {
      font-size: 1.1rem;
    }
    
    .feature-card-description {
      font-size: 0.875rem;
    }
  }
  
  .testimonial-card {
    padding: 1.5rem;
    
    .testimonial-content {
      .quote-icon {
        font-size: 2rem;
      }
      
      .testimonial-text {
        font-size: 0.875rem;
      }
    }
    
    .testimonial-author {
      .author-avatar {
        width: 40px;
        height: 40px;
      }
      
      .author-info {
        .author-name {
          font-size: 0.875rem;
        }
        
        .author-role {
          font-size: 0.75rem;
        }
      }
    }
  }
}

// Print styles
@media print {
  .hero-section {
    background: white;
    
    &::before {
      display: none;
    }
  }
  
  .floating-card,
  .decoration-circle,
  .image-glow {
    display: none;
  }
  
  .hero-actions,
  .social-proof {
    display: none;
  }
}

// High contrast mode
@media (prefers-contrast: high) {
  .hero-heading {
    .gradient-text {
      -webkit-text-fill-color: #FF6452;
    }
  }
  
  .stat-item {
    .stat-number {
      -webkit-text-fill-color: #FF6452;
    }
  }
}

// Reduced motion
@media (prefers-reduced-motion: reduce) {
  * {
    animation: none !important;
    transition: none !important;
  }
}

// Dark mode support (optional)
@media (prefers-color-scheme: dark) {
  .hero-section {
    background: linear-gradient(135deg, #1a202c 0%, #2d3748 100%);
  }
  
  .hero-content {
    .hero-heading {
      color: #f7fafc;
    }
    
    .hero-description {
      .main-description {
        color: #cbd5e0;
      }
      
      .sub-description {
        color: #a0aec0;
      }
    }
  }
  
  .feature-item {
    background: #2d3748;
    
    .feature-content {
      h4 {
        color: #f7fafc;
      }
      
      p {
        color: #a0aec0;
      }
    }
  }
  
  .stats-section {
    background: #2d3748;
    
    .stat-item {
      .stat-label {
        color: #a0aec0;
      }
      
      &:hover {
        color: white;
      }
    }
  }
  
  .feature-card {
    background: #2d3748;
    
    .feature-card-title {
      color: #f7fafc;
    }
    
    .feature-card-description {
      color: #a0aec0;
    }
  }
  
  .testimonials-section {
    background: linear-gradient(135deg, #2d3748 0%, #1a202c 100%);
  }
  
  .testimonial-card {
    background: #2d3748;
    
    .testimonial-content {
      .testimonial-text {
        color: #cbd5e0;
      }
    }
    
    .testimonial-author {
      .author-info {
        .author-name {
          color: #f7fafc;
        }
        
        .author-role {
          color: #a0aec0;
        }
      }
    }
  }
}
</style>