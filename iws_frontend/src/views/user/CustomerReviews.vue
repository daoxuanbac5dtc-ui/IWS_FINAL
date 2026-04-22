<script setup>
import { customers } from "@/constants";
import { onMounted, onUnmounted, ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

// States
const activeIndex = ref(0);
const isAutoPlay = ref(true);
let autoPlayInterval = null;

// Thống kê
const stats = ref({
  totalReviews: 2847,
  averageRating: 4.9,
  satisfaction: 98
});

// Auto-play carousel
const startAutoPlay = () => {
  autoPlayInterval = setInterval(() => {
    if (isAutoPlay.value) {
      activeIndex.value = (activeIndex.value + 1) % customers.length;
    }
  }, 5000);
};

const stopAutoPlay = () => {
  if (autoPlayInterval) {
    clearInterval(autoPlayInterval);
    autoPlayInterval = null;
  }
};

// Lifecycle
onMounted(() => {
  startAutoPlay();
});

onUnmounted(() => {
  stopAutoPlay();
});

// Methods
const selectTestimonial = (index) => {
  activeIndex.value = index;
  isAutoPlay.value = false;
  
  // Khởi động lại auto-play sau 10 giây
  setTimeout(() => {
    isAutoPlay.value = true;
  }, 10000);
};

const navigateToAbout = () => {
  router.push('/gioithieu');
};

const viewAllReviews = () => {
  router.push('/gioithieu#reviews');
};

// Computed
const getStarRating = (rating) => {
  const numRating = parseFloat(rating);
  const fullStars = Math.floor(numRating);
  const hasHalfStar = numRating % 1 !== 0;
  return { fullStars, hasHalfStar };
};
</script>

<template>
  <section class="customer-reviews-section">
    <div class="max-container">
      <!-- Header Section -->
      <div 
        v-motion
        :initial="{ opacity: 0, y: 50 }"
        :visibleOnce="{ opacity: 1, y: 0 }"
        class="section-header"
      >
        <div class="header-badge">
          <span>⭐ Được khách hàng yêu thích</span>
        </div>
        
        <h2 class="section-title">
          <span class="highlight">Khách Hàng</span> Nói Gì Về Chúng Tôi?
        </h2>
        
        <p class="section-description">
          Hãy lắng nghe những câu chuyện chân thực từ khách hàng hài lòng về trải nghiệm 
          tuyệt vời của họ khi hợp tác với chúng tôi.
        </p>
      </div>

      <!-- Stats Cards -->
      <div 
        v-motion
        :initial="{ opacity: 0, y: 30 }"
        :visibleOnce="{ opacity: 1, y: 0 }"
        :delay="200"
        class="stats-container"
      >
        <div class="stat-card">
          <div class="stat-icon">📝</div>
          <div class="stat-value">{{ stats.totalReviews.toLocaleString() }}</div>
          <div class="stat-label">Đánh giá</div>
        </div>
        
        <div class="stat-card featured">
          <div class="stat-icon">⭐</div>
          <div class="stat-value">{{ stats.averageRating }}/5</div>
          <div class="stat-label">Điểm trung bình</div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon">😊</div>
          <div class="stat-value">{{ stats.satisfaction }}%</div>
          <div class="stat-label">Hài lòng</div>
        </div>
      </div>

      <!-- Testimonials Carousel -->
      <div 
        v-motion
        :initial="{ opacity: 0, y: 50 }"
        :visibleOnce="{ opacity: 1, y: 0 }"
        :delay="400"
        class="testimonials-wrapper"
      >
        <!-- Main Testimonial Display -->
        <div class="testimonial-main">
          <TransitionGroup name="fade" mode="out-in">
            <div 
              v-for="(customer, index) in customers" 
              :key="customer.name"
              v-show="index === activeIndex"
              class="testimonial-card"
            >
              <div class="quote-icon">"</div>
              
              <div class="testimonial-content">
                <!-- Rating Stars -->
                <div class="rating-stars">
                  <span 
                    v-for="i in getStarRating(customer.rating).fullStars" 
                    :key="'full-' + i"
                    class="star filled"
                  >★</span>
                  <span 
                    v-if="getStarRating(customer.rating).hasHalfStar"
                    class="star half"
                  >★</span>
                  <span class="rating-number">({{ customer.rating }})</span>
                </div>
                
                <!-- Comment -->
                <p class="testimonial-text">
                  {{ customer.comment }}
                </p>
                
                <!-- Author -->
                <div class="testimonial-author">
                  <img 
                    :src="customer.profileImg" 
                    :alt="customer.name"
                    class="author-avatar"
                    @error="(e) => e.target.src = `https://ui-avatars.com/api/?name=${customer.name}&background=FF6452&color=fff`"
                  />
                  <div class="author-info">
                    <h4 class="author-name">{{ customer.name }}</h4>
                    <p class="author-title">Khách hàng thân thiết</p>
                  </div>
                </div>
              </div>
            </div>
          </TransitionGroup>
        </div>

        <!-- Testimonial Indicators -->
        <div class="testimonial-indicators">
          <button
            v-for="(customer, index) in customers"
            :key="'indicator-' + index"
            :class="['indicator', { active: index === activeIndex }]"
            @click="selectTestimonial(index)"
            :aria-label="`Xem đánh giá của ${customer.name}`"
          >
            <span class="indicator-dot"></span>
          </button>
        </div>

        <!-- Mini Testimonials Grid -->
        <div class="testimonials-grid">
          <div 
            v-for="(customer, index) in customers"
            :key="'grid-' + customer.name"
            :class="['testimonial-mini', { active: index === activeIndex }]"
            @click="selectTestimonial(index)"
          >
            <div class="mini-header">
              <img 
                :src="customer.profileImg" 
                :alt="customer.name"
                class="mini-avatar"
              />
              <div class="mini-info">
                <h5>{{ customer.name }}</h5>
                <div class="mini-rating">
                  <span class="star">★</span>
                  <span>{{ customer.rating }}</span>
                </div>
              </div>
            </div>
            <p class="mini-comment">
              {{ customer.comment.substring(0, 60) }}...
            </p>
          </div>
        </div>
      </div>

      <!-- CTA Section -->
      <div 
        v-motion
        :initial="{ opacity: 0, y: 30 }"
        :visibleOnce="{ opacity: 1, y: 0 }"
        :delay="600"
        class="cta-section"
      >
        <div class="cta-content">
          <h3>Muốn tìm hiểu thêm về chúng tôi?</h3>
          <p>Khám phá câu chuyện của chúng tôi và lý do hàng nghìn khách hàng tin tưởng lựa chọn</p>
        </div>
        
        <div class="cta-buttons">
          <button @click="navigateToAbout" class="btn-primary">
            <span>Xem Giới Thiệu</span>
            <svg class="btn-icon" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8z"/>
            </svg>
          </button>
          
          <button @click="viewAllReviews" class="btn-secondary">
            <span>Xem Tất Cả Đánh Giá</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Background Decoration -->
    <div class="bg-decoration">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-circle circle-3"></div>
    </div>
  </section>
</template>

<style lang="scss" scoped>
.customer-reviews-section {
  position: relative;
  padding: 5rem 0;
  background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
  overflow: hidden;
}

// Header Section
.section-header {
  text-align: center;
  margin-bottom: 3rem;
}

.header-badge {
  display: inline-flex;
  margin-bottom: 1rem;
  
  span {
    background: linear-gradient(135deg, #FF6452, #ff8a80);
    color: white;
    padding: 0.5rem 1.5rem;
    border-radius: 50px;
    font-size: 0.875rem;
    font-weight: 600;
    box-shadow: 0 4px 15px rgba(255, 100, 82, 0.3);
  }
}

.section-title {
  font-size: 3rem;
  font-weight: 800;
  color: #1a202c;
  margin-bottom: 1rem;
  
  .highlight {
    background: linear-gradient(135deg, #FF6452, #ff8a80);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.section-description {
  font-size: 1.125rem;
  color: #4a5568;
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.8;
}

// Stats Container
.stats-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2rem;
  max-width: 600px;
  margin: 0 auto 4rem;
}

.stat-card {
  background: white;
  padding: 2rem;
  border-radius: 20px;
  text-align: center;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.12);
  }
  
  &.featured {
    background: linear-gradient(135deg, #FF6452, #ff8a80);
    color: white;
    transform: scale(1.1);
    
    .stat-label {
      color: rgba(255, 255, 255, 0.9);
    }
  }
  
  .stat-icon {
    font-size: 2.5rem;
    margin-bottom: 0.5rem;
  }
  
  .stat-value {
    font-size: 2rem;
    font-weight: 800;
    margin-bottom: 0.25rem;
  }
  
  .stat-label {
    font-size: 0.875rem;
    color: #718096;
    font-weight: 600;
  }
}

// Testimonials Wrapper
.testimonials-wrapper {
  max-width: 1000px;
  margin: 0 auto;
}

// Main Testimonial
.testimonial-main {
  position: relative;
  margin-bottom: 3rem;
  min-height: 300px;
}

.testimonial-card {
  background: white;
  border-radius: 24px;
  padding: 3rem;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  position: relative;
  
  .quote-icon {
    position: absolute;
    top: -20px;
    left: 40px;
    font-size: 5rem;
    color: #FF6452;
    opacity: 0.2;
    font-family: Georgia, serif;
  }
}

.testimonial-content {
  position: relative;
  z-index: 1;
}

.rating-stars {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  margin-bottom: 1.5rem;
  
  .star {
    color: #fbbf24;
    font-size: 1.25rem;
    
    &.half {
      position: relative;
      &::after {
        content: '★';
        position: absolute;
        left: 0;
        width: 50%;
        overflow: hidden;
        color: #e5e7eb;
      }
    }
  }
  
  .rating-number {
    margin-left: 0.5rem;
    color: #718096;
    font-size: 0.875rem;
  }
}

.testimonial-text {
  font-size: 1.25rem;
  line-height: 1.8;
  color: #4a5568;
  margin-bottom: 2rem;
  font-style: italic;
}

.testimonial-author {
  display: flex;
  align-items: center;
  gap: 1rem;
  
  .author-avatar {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    object-fit: cover;
    border: 3px solid #FF6452;
  }
  
  .author-info {
    .author-name {
      font-size: 1.125rem;
      font-weight: 700;
      color: #1a202c;
      margin-bottom: 0.25rem;
    }
    
    .author-title {
      font-size: 0.875rem;
      color: #718096;
    }
  }
}

// Indicators
.testimonial-indicators {
  display: flex;
  justify-content: center;
  gap: 0.75rem;
  margin-bottom: 3rem;
}

.indicator {
  background: none;
  border: none;
  padding: 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  
  .indicator-dot {
    display: block;
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: #e2e8f0;
    transition: all 0.3s ease;
  }
  
  &.active .indicator-dot {
    width: 30px;
    border-radius: 5px;
    background: #FF6452;
  }
  
  &:hover .indicator-dot {
    background: #cbd5e0;
  }
}

// Mini Testimonials Grid
.testimonials-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  margin-bottom: 4rem;
}

.testimonial-mini {
  background: white;
  padding: 1.5rem;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  
  &.active {
    border-color: #FF6452;
    box-shadow: 0 5px 20px rgba(255, 100, 82, 0.2);
  }
  
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
  }
}

.mini-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
  
  .mini-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    object-fit: cover;
  }
  
  .mini-info {
    h5 {
      font-size: 0.875rem;
      font-weight: 600;
      color: #1a202c;
      margin-bottom: 0.25rem;
    }
    
    .mini-rating {
      display: flex;
      align-items: center;
      gap: 0.25rem;
      font-size: 0.75rem;
      color: #718096;
      
      .star {
        color: #fbbf24;
      }
    }
  }
}

.mini-comment {
  font-size: 0.875rem;
  color: #4a5568;
  line-height: 1.5;
}

// CTA Section
.cta-section {
  background: linear-gradient(135deg, #1a202c, #2d3748);
  border-radius: 24px;
  padding: 3rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255, 100, 82, 0.1) 0%, transparent 70%);
    animation: rotate 20s linear infinite;
  }
}

.cta-content {
  position: relative;
  z-index: 1;
  
  h3 {
    font-size: 1.75rem;
    font-weight: 700;
    color: white;
    margin-bottom: 0.5rem;
  }
  
  p {
    color: #cbd5e0;
    font-size: 1rem;
  }
}

.cta-buttons {
  display: flex;
  gap: 1rem;
  position: relative;
  z-index: 1;
}

.btn-primary,
.btn-secondary {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.875rem 1.75rem;
  border-radius: 50px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  
  .btn-icon {
    width: 20px;
    height: 20px;
    transition: transform 0.3s ease;
  }
}

.btn-primary {
  background: linear-gradient(135deg, #FF6452, #ff8a80);
  color: white;
  box-shadow: 0 4px 20px rgba(255, 100, 82, 0.3);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 30px rgba(255, 100, 82, 0.4);
    
    .btn-icon {
      transform: translateX(4px);
    }
  }
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.2);
  
  &:hover {
    background: rgba(255, 255, 255, 0.2);
    border-color: rgba(255, 255, 255, 0.3);
  }
}

// Background Decoration
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  
  &.circle-1 {
    top: 10%;
    left: 5%;
    width: 200px;
    height: 200px;
    background: radial-gradient(circle, rgba(255, 100, 82, 0.1) 0%, transparent 70%);
    animation: float 15s ease-in-out infinite;
  }
  
  &.circle-2 {
    bottom: 10%;
    right: 5%;
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, rgba(79, 70, 229, 0.1) 0%, transparent 70%);
    animation: float 20s ease-in-out infinite reverse;
  }
  
  &.circle-3 {
    top: 50%;
    right: 10%;
    width: 150px;
    height: 150px;
    background: radial-gradient(circle, rgba(16, 185, 129, 0.1) 0%, transparent 70%);
    animation: float 18s ease-in-out infinite;
  }
}

// Animations
@keyframes fade-enter-active {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fade-leave-active {
  from {
    opacity: 1;
    transform: translateY(0);
  }
  to {
    opacity: 0;
    transform: translateY(-20px);
  }
}

.fade-enter-active {
  animation: fade-enter-active 0.5s ease;
}

.fade-leave-active {
  animation: fade-leave-active 0.5s ease;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

// Responsive
@media (max-width: 1024px) {
  .stats-container {
    max-width: 500px;
  }
  
  .cta-section {
    flex-direction: column;
    text-align: center;
    gap: 2rem;
  }
  
  .cta-buttons {
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .section-title {
    font-size: 2rem;
  }
  
  .section-description {
    font-size: 1rem;
  }
  
  .stats-container {
    grid-template-columns: 1fr;
    gap: 1rem;
    max-width: 300px;
  }
  
  .stat-card {
    padding: 1.5rem;
    
    &.featured {
      transform: scale(1);
    }
  }
  
  .testimonial-card {
    padding: 2rem;
  }
  
  .testimonial-text {
    font-size: 1rem;
  }
  
  .testimonials-grid {
    grid-template-columns: 1fr;
  }
  
  .cta-section {
    padding: 2rem;
  }
  
  .cta-content {
    h3 {
      font-size: 1.5rem;
    }
  }
  
  .cta-buttons {
    flex-direction: column;
    width: 100%;
    
    .btn-primary,
    .btn-secondary {
      width: 100%;
      justify-content: center;
    }
  }
}

@media (max-width: 480px) {
  .section-title {
    font-size: 1.75rem;
  }
  
  .testimonial-card {
    padding: 1.5rem;
    
    .quote-icon {
      font-size: 3rem;
      top: -10px;
      left: 20px;
    }
  }
  
  .testimonial-author {
    .author-avatar {
      width: 50px;
      height: 50px;
    }
  }
  
  .decoration-circle {
    &.circle-1 {
      width: 100px;
      height: 100px;
    }
    
    &.circle-2 {
      width: 150px;
      height: 150px;
    }
    
    &.circle-3 {
      width: 80px;
      height: 80px;
    }
  }
}
</style>