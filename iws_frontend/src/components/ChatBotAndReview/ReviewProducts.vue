<template>
  <div class="review-system">
    <!-- Review Summary -->
    <div class="review-summary">
      <div class="summary-left">
        <div class="overall-rating">
          <span class="rating-score">{{ overallRating }}</span>
          <div class="rating-stars">
            <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= Math.floor(overallRating) }">
              ★
            </span>
          </div>
          <p class="rating-text">{{ totalReviews }} đánh giá</p>
        </div>
      </div>
      
      <div class="summary-right">
        <div class="rating-bars">
          <div v-for="(count, rating) in ratingDistribution" :key="rating" class="rating-bar">
            <span class="rating-label">{{ rating }} ★</span>
            <div class="bar-container">
              <div class="bar-fill" :style="{ width: getPercentage(count) + '%' }"></div>
            </div>
            <span class="rating-count">{{ count }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Review Actions -->
    <div class="review-actions">
      <button class="write-review-btn" @click="showWriteReview = true">
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
        </svg>
        Viết đánh giá
      </button>
      
      <div class="filter-buttons">
        <button 
          v-for="filter in filters" 
          :key="filter"
          class="filter-btn"
          :class="{ active: activeFilter === filter }"
          @click="activeFilter = filter"
        >
          {{ filter }}
        </button>
      </div>
    </div>

    <!-- Write Review Modal -->
    <div v-if="showWriteReview" class="review-modal-overlay" @click="closeModal">
      <div class="review-modal" @click.stop>
        <div class="modal-header">
          <h3>Viết đánh giá sản phẩm</h3>
          <button @click="closeModal" class="close-modal">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </button>
        </div>
        
        <div class="modal-content">
          <div class="rating-input">
            <label>Đánh giá của bạn:</label>
            <div class="star-input">
              <button 
                v-for="i in 5" 
                :key="i"
                @click="newReview.rating = i"
                class="star-btn"
                :class="{ active: i <= newReview.rating }"
              >
                ★
              </button>
            </div>
          </div>
          
          <div class="form-group">
            <label>Tên của bạn:</label>
            <input v-model="newReview.name" placeholder="Nhập tên của bạn" />
          </div>
          
          <div class="form-group">
            <label>Tiêu đề đánh giá:</label>
            <input v-model="newReview.title" placeholder="Tóm tắt đánh giá của bạn" />
          </div>
          
          <div class="form-group">
            <label>Nội dung đánh giá:</label>
            <textarea 
              v-model="newReview.content" 
              placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm..."
              rows="4"
            ></textarea>
          </div>
          
          <div class="modal-actions">
            <button @click="closeModal" class="cancel-btn">Hủy</button>
            <button @click="submitReview" class="submit-btn" :disabled="!canSubmit">
              Gửi đánh giá
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Reviews List -->
    <div class="reviews-list">
      <div 
        v-for="review in filteredReviews" 
        :key="review.id"
        class="review-item"
      >
        <div class="review-header">
          <div class="reviewer-info">
            <div class="avatar">
              {{ review.name.charAt(0).toUpperCase() }}
            </div>
            <div class="reviewer-details">
              <h4>{{ review.name }}</h4>
              <div class="review-meta">
                <div class="stars">
                  <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= review.rating }">
                    ★
                  </span>
                </div>
                <span class="review-date">{{ formatDate(review.date) }}</span>
              </div>
            </div>
          </div>
          
          <div class="review-actions-menu">
            <button class="menu-btn" @click="toggleMenu(review.id)">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"/>
              </svg>
            </button>
            <div v-if="activeMenu === review.id" class="menu-dropdown">
              <button @click="reportReview(review.id)">Báo cáo</button>
              <button @click="likeReview(review.id)">Hữu ích</button>
            </div>
          </div>
        </div>
        
        <div class="review-content">
          <h5 v-if="review.title">{{ review.title }}</h5>
          <p>{{ review.content }}</p>
          
          <div v-if="review.images && review.images.length" class="review-images">
            <img 
              v-for="(image, index) in review.images" 
              :key="index"
              :src="image" 
              @click="openImageModal(image)"
              class="review-image"
            />
          </div>
        </div>
        
        <div class="review-footer">
          <button class="helpful-btn" :class="{ liked: review.isLiked }" @click="toggleHelpful(review.id)">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M7.5 4v7.5H4a2 2 0 0 0-2 2v4a2 2 0 0 0 2 2h2.5v1.5a.5.5 0 0 0 .5.5h1.5V4H7.5zM20 8.5h-3.5V4a2 2 0 0 0-2-2H12a2 2 0 0 0-2 2v4.5H6.5V21H10v-1.5h4a2 2 0 0 0 2-2V14a2 2 0 0 0 2-2V8.5z"/>
            </svg>
            <span>Hữu ích ({{ review.helpfulCount || 0 }})</span>
          </button>
          
          <button class="reply-btn" @click="toggleReply(review.id)">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M10 9V5l-7 7 7 7v-4.1c5 0 8.5 1.6 11 5.1-1-5-4-10-11-11z"/>
            </svg>
            Trả lời
          </button>
        </div>
        
        <!-- Reply Section -->
        <div v-if="review.showReply" class="reply-section">
          <div class="reply-input">
            <textarea 
              v-model="replyContent[review.id]" 
              placeholder="Viết phản hồi..."
              rows="2"
            ></textarea>
            <div class="reply-actions">
              <button @click="cancelReply(review.id)" class="cancel-reply">Hủy</button>
              <button @click="submitReply(review.id)" class="submit-reply">Gửi</button>
            </div>
          </div>
        </div>
        
        <!-- Replies -->
        <div v-if="review.replies && review.replies.length" class="replies-list">
          <div v-for="reply in review.replies" :key="reply.id" class="reply-item">
            <div class="reply-avatar">{{ reply.name.charAt(0).toUpperCase() }}</div>
            <div class="reply-content">
              <div class="reply-header">
                <strong>{{ reply.name }}</strong>
                <span class="reply-date">{{ formatDate(reply.date) }}</span>
              </div>
              <p>{{ reply.content }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Load More -->
    <div v-if="hasMoreReviews" class="load-more">
      <button @click="loadMoreReviews" class="load-more-btn">
        Xem thêm đánh giá
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ReviewProducts',
  props: {
    productId: {
      type: [String, Number],
      required: true
    }
  },
  
  data() {
    return {
      showWriteReview: false,
      activeFilter: 'Tất cả',
      activeMenu: null,
      filters: ['Tất cả', '5 sao', '4 sao', '3 sao', '2 sao', '1 sao', 'Có hình ảnh'],
      
      newReview: {
        rating: 0,
        name: '',
        title: '',
        content: ''
      },
      
      replyContent: {},
      
      // Sample data - replace with API call
      reviews: [
        {
          id: 1,
          name: 'Nguyễn Văn A',
          rating: 5,
          title: 'Sản phẩm tuyệt vời!',
          content: 'Giày rất đẹp và chất lượng, giao hàng nhanh. Tôi rất hài lòng với sản phẩm này. Chất liệu da mềm, đế êm chân. Sẽ mua thêm những đôi khác.',
          date: new Date('2024-01-15'),
          helpfulCount: 12,
          isLiked: false,
          showReply: false,
          images: [],
          replies: [
            {
              id: 1,
              name: 'Shop',
              content: 'Cảm ơn bạn đã tin tưởng sản phẩm của shop! 😊',
              date: new Date('2024-01-16')
            }
          ]
        },
        {
          id: 2,
          name: 'Trần Thị B',
          rating: 4,
          title: 'Chất lượng tốt',
          content: 'Giày đẹp nhưng hơi chật so với size thông thường. Nên chọn size lớn hơn 0.5 so với bình thường. Nhìn chung vẫn ok, giá cả hợp lý.',
          date: new Date('2024-01-10'),
          helpfulCount: 8,
          isLiked: true,
          showReply: false,
          images: [],
          replies: []
        },
        {
          id: 3,
          name: 'Lê Văn C',
          rating: 5,
          title: 'Xuất sắc',
          content: 'Mình đã mua 3 đôi rồi, chất lượng luôn ổn định. Recommend! Đóng gói cẩn thận, ship nhanh.',
          date: new Date('2024-01-08'),
          helpfulCount: 15,
          isLiked: false,
          showReply: false,
          images: [],
          replies: []
        },
        {
          id: 4,
          name: 'Phạm Thị D',
          rating: 4,
          title: 'Đáng tiền',
          content: 'Giày đẹp, form chuẩn. Màu sắc như hình. Chỉ tiếc là không có thêm màu khác để lựa chọn.',
          date: new Date('2024-01-05'),
          helpfulCount: 6,
          isLiked: false,
          showReply: false,
          images: [],
          replies: []
        }
      ],
      
      hasMoreReviews: true
    }
  },
  
  computed: {
    overallRating() {
      if (this.reviews.length === 0) return 0;
      const sum = this.reviews.reduce((acc, review) => acc + review.rating, 0);
      return (sum / this.reviews.length).toFixed(1);
    },
    
    totalReviews() {
      return this.reviews.length;
    },
    
    ratingDistribution() {
      const distribution = { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 };
      this.reviews.forEach(review => {
        distribution[review.rating]++;
      });
      return distribution;
    },
    
    filteredReviews() {
      if (this.activeFilter === 'Tất cả') return this.reviews;
      if (this.activeFilter === 'Có hình ảnh') {
        return this.reviews.filter(review => review.images && review.images.length > 0);
      }
      const rating = parseInt(this.activeFilter.charAt(0));
      return this.reviews.filter(review => review.rating === rating);
    },
    
    canSubmit() {
      return this.newReview.rating > 0 && 
             this.newReview.name.trim() && 
             this.newReview.content.trim();
    }
  },
  
  methods: {
    closeModal() {
      this.showWriteReview = false;
      this.resetNewReview();
    },
    
    resetNewReview() {
      this.newReview = {
        rating: 0,
        name: '',
        title: '',
        content: ''
      };
    },
    
    submitReview() {
      if (!this.canSubmit) return;
      
      const review = {
        id: Date.now(),
        name: this.newReview.name,
        rating: this.newReview.rating,
        title: this.newReview.title,
        content: this.newReview.content,
        date: new Date(),
        helpfulCount: 0,
        isLiked: false,
        showReply: false,
        images: [],
        replies: []
      };
      
      this.reviews.unshift(review);
      this.closeModal();
      
      // Show success message
      this.$emit('review-submitted', review);
      alert('Cảm ơn bạn đã đánh giá sản phẩm!');
    },
    
    getPercentage(count) {
      return this.totalReviews > 0 ? (count / this.totalReviews) * 100 : 0;
    },
    
    formatDate(date) {
      return new Intl.DateTimeFormat('vi-VN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      }).format(date);
    },
    
    toggleMenu(reviewId) {
      this.activeMenu = this.activeMenu === reviewId ? null : reviewId;
    },
    
    toggleHelpful(reviewId) {
      const review = this.reviews.find(r => r.id === reviewId);
      if (review) {
        if (review.isLiked) {
          review.helpfulCount--;
          review.isLiked = false;
        } else {
          review.helpfulCount++;
          review.isLiked = true;
        }
      }
    },
    
    toggleReply(reviewId) {
      const review = this.reviews.find(r => r.id === reviewId);
      if (review) {
        review.showReply = !review.showReply;
        if (!review.showReply) {
          this.replyContent[reviewId] = '';
        }
      }
    },
    
    submitReply(reviewId) {
      const content = this.replyContent[reviewId];
      if (!content || !content.trim()) return;
      
      const review = this.reviews.find(r => r.id === reviewId);
      if (review) {
        if (!review.replies) review.replies = [];
        
        review.replies.push({
          id: Date.now(),
          name: 'Bạn', // In real app, get from user session
          content: content.trim(),
          date: new Date()
        });
        
        this.replyContent[reviewId] = '';
        review.showReply = false;
      }
    },
    
    cancelReply(reviewId) {
      const review = this.reviews.find(r => r.id === reviewId);
      if (review) {
        review.showReply = false;
        this.replyContent[reviewId] = '';
      }
    },
    
    reportReview(reviewId) {
      alert('Đã báo cáo đánh giá. Chúng tôi sẽ xem xét trong thời gian sớm nhất.');
      this.activeMenu = null;
    },
    
    likeReview(reviewId) {
      this.toggleHelpful(reviewId);
      this.activeMenu = null;
    },
    
    loadMoreReviews() {
      // Simulate loading more reviews
      setTimeout(() => {
        this.hasMoreReviews = false;
      }, 1000);
    },
    
    openImageModal(image) {
      // Implement image modal
      console.log('Open image:', image);
    }
  }
}
</script>

<style scoped>
.review-system {
  background: white;
  border-radius: 20px;
  padding: 2rem;
  margin-top: 2rem;
}

.review-summary {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 3rem;
  margin-bottom: 2rem;
  padding-bottom: 2rem;
  border-bottom: 1px solid #e5e7eb;
}

.overall-rating {
  text-align: center;
}

.rating-score {
  font-size: 4rem;
  font-weight: 900;
  color: #FF6452;
  display: block;
  line-height: 1;
}

.rating-stars {
  display: flex;
  justify-content: center;
  gap: 0.25rem;
  margin: 1rem 0;
}

.star {
  font-size: 1.5rem;
  color: #e5e7eb;
}

.star.filled {
  color: #fbbf24;
}

.rating-text {
  color: #64748b;
  font-size: 1.1rem;
  margin: 0;
}

.rating-bars {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.rating-bar {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.rating-label {
  min-width: 50px;
  font-size: 0.9rem;
  color: #64748b;
}

.bar-container {
  flex: 1;
  height: 8px;
  background: #f1f5f9;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(135deg, #FF6452, #ff8a80);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.rating-count {
  min-width: 30px;
  text-align: right;
  font-size: 0.9rem;
  color: #64748b;
}

.review-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  gap: 1rem;
}

.write-review-btn {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background: linear-gradient(135deg, #FF6452, #ff8a80);
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.write-review-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 100, 82, 0.3);
}

.write-review-btn svg {
  width: 20px;
  height: 20px;
}

.filter-buttons {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.filter-btn {
  padding: 0.5rem 1rem;
  border: 2px solid #e5e7eb;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  color: #64748b;
}

.filter-btn:hover,
.filter-btn.active {
  border-color: #FF6452;
  color: #FF6452;
  background: #fef2f2;
}

.review-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 2rem;
}

.review-modal {
  background: white;
  border-radius: 20px;
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2rem 2rem 1rem;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h3 {
  margin: 0;
  color: #111827;
  font-size: 1.5rem;
}

.close-modal {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 50%;
  color: #64748b;
  transition: all 0.2s;
}

.close-modal:hover {
  background: #f3f4f6;
  color: #374151;
}

.close-modal svg {
  width: 24px;
  height: 24px;
}

.modal-content {
  padding: 2rem;
}

.rating-input {
  margin-bottom: 1.5rem;
}

.rating-input label {
  display: block;
  margin-bottom: 0.75rem;
  font-weight: 600;
  color: #374151;
}

.star-input {
  display: flex;
  gap: 0.5rem;
}

.star-btn {
  background: none;
  border: none;
  font-size: 2rem;
  color: #e5e7eb;
  cursor: pointer;
  transition: all 0.2s;
}

.star-btn:hover,
.star-btn.active {
  color: #fbbf24;
  transform: scale(1.1);
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #374151;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.2s;
  font-family: inherit;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #FF6452;
}

.form-group textarea {
  resize: vertical;
  min-height: 100px;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 2rem;
}

.cancel-btn,
.submit-btn {
  padding: 0.75rem 2rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn {
  background: white;
  border: 2px solid #e5e7eb;
  color: #64748b;
}

.cancel-btn:hover {
  border-color: #d1d5db;
  color: #374151;
}

.submit-btn {
  background: linear-gradient(135deg, #FF6452, #ff8a80);
  border: none;
  color: white;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 100, 82, 0.3);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.review-item {
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 2rem;
  transition: all 0.3s ease;
}

.review-item:hover {
  border-color: #d1d5db;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.reviewer-info {
  display: flex;
  gap: 1rem;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6452, #ff8a80);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 1.2rem;
}

.reviewer-details h4 {
  margin: 0 0 0.5rem 0;
  color: #111827;
  font-size: 1.1rem;
}

.review-meta {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stars {
  display: flex;
  gap: 0.1rem;
}

.stars .star {
  font-size: 1rem;
  color: #e5e7eb;
}

.stars .star.filled {
  color: #fbbf24;
}

.review-date {
  color: #64748b;
  font-size: 0.9rem;
}

.review-actions-menu {
  position: relative;
}

.menu-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 50%;
  color: #64748b;
  transition: all 0.2s;
}

.menu-btn:hover {
  background: #f3f4f6;
}

.menu-btn svg {
  width: 20px;
  height: 20px;
}

.menu-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 10;
  min-width: 120px;
}

.menu-dropdown button {
  width: 100%;
  text-align: left;
  padding: 0.75rem 1rem;
  border: none;
  background: none;
  cursor: pointer;
  transition: background 0.2s;
  font-size: 0.9rem;
  color: #374151;
}

.menu-dropdown button:hover {
  background: #f3f4f6;
}

.review-content h5 {
  margin: 0 0 0.75rem 0;
  color: #111827;
  font-size: 1.1rem;
}

.review-content p {
  color: #374151;
  line-height: 1.6;
  margin: 0 0 1rem 0;
}

.review-images {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
}

.review-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}

.review-image:hover {
  transform: scale(1.05);
}

.review-footer {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #f3f4f6;
}

.helpful-btn,
.reply-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: none;
  border: 1px solid #e5e7eb;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.9rem;
  color: #64748b;
}

.helpful-btn:hover,
.reply-btn:hover {
  border-color: #FF6452;
  color: #FF6452;
}

.helpful-btn.liked {
  border-color: #FF6452;
  color: #FF6452;
  background: #fef2f2;
}

.helpful-btn svg,
.reply-btn svg {
  width: 16px;
  height: 16px;
}

.reply-section {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #f3f4f6;
}

.reply-input textarea {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-family: inherit;
  resize: vertical;
  margin-bottom: 0.75rem;
}

.reply-input textarea:focus {
  outline: none;
  border-color: #FF6452;
}

.reply-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
}

.cancel-reply,
.submit-reply {
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-reply {
  background: white;
  border: 1px solid #e5e7eb;
  color: #64748b;
}

.cancel-reply:hover {
  border-color: #d1d5db;
  color: #374151;
}

.submit-reply {
  background: #FF6452;
  border: none;
  color: white;
}

.submit-reply:hover {
  background: #e55547;
}

.replies-list {
  margin-top: 1rem;
  padding-left: 2rem;
  border-left: 2px solid #f3f4f6;
}

.reply-item {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.reply-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f3f4f6;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.9rem;
  flex-shrink: 0;
}

.reply-content {
  flex: 1;
}

.reply-header {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  margin-bottom: 0.25rem;
}

.reply-header strong {
  color: #111827;
  font-size: 0.9rem;
}

.reply-date {
  color: #64748b;
  font-size: 0.8rem;
}

.reply-content p {
  margin: 0;
  color: #374151;
  font-size: 0.9rem;
  line-height: 1.5;
}

.load-more {
  text-align: center;
  margin-top: 2rem;
}

.load-more-btn {
  background: white;
  border: 2px solid #FF6452;
  color: #FF6452;
  padding: 1rem 2rem;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.load-more-btn:hover {
  background: #FF6452;
  color: white;
  transform: translateY(-2px);
}

/* Responsive */
@media (max-width: 768px) {
  .review-summary {
    grid-template-columns: 1fr;
    gap: 2rem;
  }
  
  .review-actions {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }
  
  .filter-buttons {
    justify-content: center;
  }
  
  .review-modal {
    margin: 1rem;
    max-height: calc(100vh - 2rem);
  }
  
  .modal-content {
    padding: 1rem;
  }
  
  .review-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .review-footer {
    flex-wrap: wrap;
  }
  
  .replies-list {
    padding-left: 1rem;
  }
}
</style>