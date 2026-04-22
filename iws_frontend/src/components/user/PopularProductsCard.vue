<script setup>
import { computed } from 'vue';

const props = defineProps({
  product: {
    type: Object,
    required: true
  },
  width: {
    type: String,
    default: '280'
  }
});

// Format giá tiền
const formatPrice = (price) => {
  if (!price) return '0';
  return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.');
};

// Tính discount percentage
const discountPercent = computed(() => {
  if (!props.product.originalPrice || !props.product.price) return 0;
  if (props.product.originalPrice <= props.product.price) return 0;
  return Math.round((1 - props.product.price / props.product.originalPrice) * 100);
});

// Xử lý lỗi hình ảnh
const handleImageError = (event) => {
  event.target.src = '/placeholder-shoe.png';
};
</script>

<template>
  <div 
    class="popular-product-card"
    :style="{ width: width + 'px' }"
  >
    <!-- Product Image -->
    <div class="product-image-container">
      <img 
        :src="product.imgURL || '/placeholder-shoe.png'"
        :alt="product.name"
        class="product-image"
        @error="handleImageError"
      />
      
      <!-- Discount Badge -->
      <div v-if="discountPercent > 0" class="discount-badge">
        -{{ discountPercent }}%
      </div>
      
      <!-- New Badge -->
      <div class="new-badge">
        Mới
      </div>
      
      <!-- Hover Overlay -->
      <div class="product-overlay">
        <div class="overlay-content">
          <button class="quick-view-btn">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
    
    <!-- Product Info -->
    <div class="product-info">
      <!-- Brand -->
      <div class="product-brand" v-if="product.brand">
        {{ product.brand }}
      </div>
      
      <!-- Product Name -->
      <h3 class="product-name">
        {{ product.name }}
      </h3>
      
      <!-- Rating -->
      <div class="product-rating">
        <div class="stars">
          <span 
            v-for="i in 5" 
            :key="i"
            class="star"
            :class="{ 'filled': i <= Math.floor(product.rating) }"
          >
            ★
          </span>
        </div>
        <span class="rating-text">
          ({{ product.rating?.toFixed(1) }})
        </span>
      </div>
      
      <!-- Price -->
      <div class="price-container">
        <div class="current-price">
          ₫{{ formatPrice(product.price) }}
        </div>
        <div 
          v-if="product.originalPrice && product.originalPrice > product.price" 
          class="original-price"
        >
          ₫{{ formatPrice(product.originalPrice) }}
        </div>
      </div>
      
      <!-- Category -->
      <div class="product-category" v-if="product.category">
        {{ product.category }}
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.popular-product-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  
  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
    
    .product-image {
      transform: scale(1.05);
    }
    
    .product-overlay {
      opacity: 1;
    }
  }
}

.product-image-container {
  position: relative;
  width: 100%;
  height: 200px;
  background: #f8f9fa;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .product-image {
    width: 90%;
    height: 90%;
    object-fit: contain;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    filter: drop-shadow(0 10px 20px rgba(0, 0, 0, 0.1));
  }
  
  .discount-badge {
    position: absolute;
    top: 12px;
    left: 12px;
    background: linear-gradient(135deg, #e74c3c, #c0392b);
    color: white;
    padding: 0.3rem 0.8rem;
    border-radius: 15px;
    font-size: 0.75rem;
    font-weight: 700;
    z-index: 2;
    box-shadow: 0 4px 12px rgba(231, 76, 60, 0.3);
  }
  
  .new-badge {
    position: absolute;
    top: 12px;
    right: 12px;
    background: linear-gradient(135deg, #FF6452, #ff8a80);
    color: white;
    padding: 0.3rem 0.8rem;
    border-radius: 15px;
    font-size: 0.75rem;
    font-weight: 700;
    z-index: 2;
    box-shadow: 0 4px 12px rgba(255, 100, 82, 0.3);
  }
  
  .product-overlay {
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, rgba(255, 100, 82, 0.9), rgba(255, 138, 128, 0.9));
    opacity: 0;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 3;
    
    .overlay-content {
      display: flex;
      gap: 1rem;
      
      .quick-view-btn {
        width: 50px;
        height: 50px;
        background: white;
        color: #FF6452;
        border: none;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.3s ease;
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
        
        svg {
          width: 20px;
          height: 20px;
        }
        
        &:hover {
          transform: scale(1.1);
          background: #FF6452;
          color: white;
        }
      }
    }
  }
}

.product-info {
  padding: 1.5rem;
  
  .product-brand {
    font-size: 0.75rem;
    color: #FF6452;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    margin-bottom: 0.5rem;
  }
  
  .product-name {
    font-size: 1.1rem;
    font-weight: 700;
    color: #1a202c;
    line-height: 1.4;
    margin-bottom: 0.75rem;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  
  .product-rating {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 1rem;
    
    .stars {
      display: flex;
      gap: 0.1rem;
      
      .star {
        color: #e2e8f0;
        font-size: 1rem;
        transition: all 0.2s ease;
        
        &.filled {
          color: #fbbf24;
          text-shadow: 0 0 8px rgba(251, 191, 36, 0.5);
        }
      }
    }
    
    .rating-text {
      font-size: 0.8rem;
      color: #64748b;
      font-weight: 500;
    }
  }
  
  .price-container {
    display: flex;
    align-items: baseline;
    gap: 0.75rem;
    margin-bottom: 0.75rem;
    
    .current-price {
      font-size: 1.4rem;
      font-weight: 900;
      background: linear-gradient(135deg, #FF6452, #ff8a80);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    
    .original-price {
      font-size: 1rem;
      color: #9ca3af;
      text-decoration: line-through;
      font-weight: 500;
    }
  }
  
  .product-category {
    font-size: 0.8rem;
    color: #64748b;
    font-weight: 500;
    padding: 0.25rem 0.75rem;
    background: #f1f5f9;
    border-radius: 12px;
    display: inline-block;
  }
}

// Responsive
@media (max-width: 1024px) {
  .popular-product-card {
    .product-image-container {
      height: 180px;
    }
    
    .product-info {
      padding: 1.25rem;
      
      .product-name {
        font-size: 1rem;
      }
      
      .current-price {
        font-size: 1.2rem;
      }
    }
  }
}

@media (max-width: 768px) {
  .popular-product-card {
    .product-image-container {
      height: 160px;
    }
    
    .product-info {
      padding: 1rem;
      
      .product-name {
        font-size: 0.95rem;
      }
      
      .current-price {
        font-size: 1.1rem;
      }
    }
  }
}

@media (max-width: 500px) {
  .popular-product-card {
    .product-image-container {
      height: 140px;
    }
    
    .product-info {
      padding: 0.875rem;
      
      .product-name {
        font-size: 0.9rem;
      }
      
      .current-price {
        font-size: 1rem;
      }
    }
  }
}
</style>