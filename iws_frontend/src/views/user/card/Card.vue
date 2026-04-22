<template>
    <div class="nike-complete-layout">
      <!-- Navigation Component -->
      <Nav />

      <!-- Breadcrumb -->
      <div class="breadcrumb-section bg-gray-50 py-4">
        <div class="w-full mx-auto px-6">
          <nav class="flex items-center space-x-2 text-sm">
            <router-link to="/" class="text-gray-600 hover:text-orange-500 transition-colors">

            </router-link>
            <span class="text-gray-400"></span>

          </nav>
        </div>
      </div>

      <!-- Main Content -->
      <main class="nike-layout w-full bg-gray-50 pt-8 pb-0">
        <div class="w-full mx-auto px-6">
          <!-- Cart Component -->
          <CardUser
            ref="cartComponent"
            @cart-updated="handleCartUpdate"
            @proceed-checkout="handleCheckout"
            @continue-shopping="handleContinueShopping"
          />

          <!-- Trust Badges - Hiển thị khi có sản phẩm trong giỏ -->
          <div v-if="hasCartItems" class="mt-0">
            <div class="bg-white rounded-lg shadow-lg p-8">
              <h3 class="text-2xl font-bold text-center text-gray-800 mb-8">🔒 Mua sắm an toàn cùng MyShoes</h3>
              <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
                <div class="text-center">
                  <div class="bg-blue-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                    <svg class="w-8 h-8 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M2.166 4.999A11.954 11.954 0 0010 1.944 11.954 11.954 0 0017.834 5c.11.65.166 1.32.166 2.001 0 5.225-3.34 9.67-8 11.317C5.34 16.67 2 12.225 2 7c0-.682.057-1.35.166-2.001zm11.541 3.708a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"></path>
                    </svg>
                  </div>
                  <h4 class="font-semibold text-gray-800 mb-2">Bảo mật thanh toán</h4>
                  <p class="text-sm text-gray-600">SSL 256-bit encryption</p>
                </div>

                <div class="text-center">
                  <div class="bg-green-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                    <svg class="w-8 h-8 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                    </svg>
                  </div>
                  <h4 class="font-semibold text-gray-800 mb-2">Hàng chính hãng</h4>
                  <p class="text-sm text-gray-600">100% authentic</p>
                </div>

                <div class="text-center">
                  <div class="bg-purple-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                    <svg class="w-8 h-8 text-purple-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M4 2a1 1 0 011 1v2.101a7.002 7.002 0 0111.601 2.566 1 1 0 11-1.885.666A5.002 5.002 0 005.999 7H9a1 1 0 010 2H4a1 1 0 01-1-1V3a1 1 0 011-1zm.008 9.057a1 1 0 011.276.61A5.002 5.002 0 0014.001 13H11a1 1 0 110-2h5a1 1 0 011 1v5a1 1 0 11-2 0v-2.101a7.002 7.002 0 01-11.601-2.566 1 1 0 01.61-1.276z"></path>
                    </svg>
                  </div>
                  <h4 class="font-semibold text-gray-800 mb-2">Đổi trả dễ dàng</h4>
                  <p class="text-sm text-gray-600">30 ngày đổi trả</p>
                </div>

                <div class="text-center">
                  <div class="bg-orange-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                    <svg class="w-8 h-8 text-orange-600" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zM3 10a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1v-6zM14 9a1 1 0 00-1 1v6a1 1 0 001 1h2a1 1 0 001-1v-6a1 1 0 00-1-1h-2z"></path>
                    </svg>
                  </div>
                  <h4 class="font-semibold text-gray-800 mb-2">Giao hàng nhanh</h4>
                  <p class="text-sm text-gray-600">2-3 ngày toàn quốc</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Recommended Products - Hiển thị khi giỏ hàng trống -->
          <div v-if="!hasCartItems" class="mt-8">
            <div class="bg-white rounded-lg shadow-lg p-8">
              <h3 class="text-2xl font-bold text-center text-gray-800 mb-8">💡 Gợi ý sản phẩm cho bạn</h3>
              <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
                <div v-for="product in recommendedProducts" :key="product.id"
                     class="bg-gray-50 rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer"
                     @click="goToProduct(product.id)">
                  <div class="aspect-square bg-white rounded-lg mb-4 flex items-center justify-center">
                    <img :src="product.image" :alt="product.name" class="w-full h-full object-cover rounded-lg">
                  </div>
                  <h4 class="font-semibold text-gray-800 mb-2">{{ product.name }}</h4>
                  <p class="text-orange-600 font-bold">{{ formatPrice(product.price) }}đ</p>
                  <button class="w-full mt-3 bg-orange-500 text-white py-2 rounded-lg hover:bg-orange-600 transition-colors">
                    Xem chi tiết
                  </button>
                </div>
              </div>
              <div class="text-center mt-8">
                <button @click="goToProductList"
                        class="bg-gray-800 text-white px-8 py-3 rounded-lg hover:bg-gray-900 transition-colors">
                  Xem tất cả sản phẩm
                </button>
              </div>
            </div>
          </div>

          <!-- Customer Support Info -->
        </div>
      </main>

      <!-- Footer Component -->
      <section id="#" class="padding-x padding-t bg-black pb-8">
        <Footer />
      </section>

      <ScrollToggler />
    </div>
  </template>

  <script>
  import Nav from '@/components/user/Nav.vue';
  import CardUser from '@/components/user/card/CardUser.vue';
  import Footer from '@/views/user/Footer.vue';
  import ScrollToggler from '@/components/user/ScrollToggler.vue';
  import axios from 'axios';

  export default {
    name: 'CartPage',

    components: {
      Nav,
      CardUser,
      Footer,
      ScrollToggler
    },

    data() {
      return {
        hasCartItems: false,
        cartItemCount: 0,
        recommendedProducts: [
          {
            id: 1,
            name: 'Nike Air Force 1',
            price: 2590000,
            image: '/api/placeholder/300/300'
          },
          {
            id: 2,
            name: 'Adidas Ultraboost',
            price: 3200000,
            image: '/api/placeholder/300/300'
          },
          {
            id: 3,
            name: 'Converse Chuck Taylor',
            price: 1800000,
            image: '/api/placeholder/300/300'
          },
          {
            id: 4,
            name: 'Vans Old Skool',
            price: 2100000,
            image: '/api/placeholder/300/300'
          }
        ]
      }
    },

    mounted() {
      this.checkCartStatus();
      window.addEventListener('storage', this.handleStorageChange);
    },

    beforeUnmount() {
      window.removeEventListener('storage', this.handleStorageChange);
    },

    methods: {
      checkCartStatus() {
        try {
          const cart = JSON.parse(localStorage.getItem('cart') || '[]');
          this.hasCartItems = cart.length > 0;
          this.cartItemCount = cart.reduce((total, item) => total + item.quantity, 0);
        } catch (error) {
          console.error('Error checking cart status:', error);
          this.hasCartItems = false;
          this.cartItemCount = 0;
        }
      },

      handleStorageChange(e) {
        if (e.key === 'cart') {
          this.checkCartStatus();
        }
      },

      handleCartUpdate(cartData) {
        this.hasCartItems = cartData.items && cartData.items.length > 0;
        this.cartItemCount = cartData.totalQuantity || 0;
      },

      handleCheckout() {
        this.$router.push('/checkout');
      },

      handleContinueShopping() {
        this.$router.push('/products');
      },

      goToProduct(productId) {
        this.$router.push(`/product/${productId}`);
      },

      goToProductList() {
        this.$router.push('/products');
      },

      formatPrice(price) {
        return new Intl.NumberFormat('vi-VN').format(price);
      }
    }
  }
  </script>

  <style scoped>
  .nike-complete-layout {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    width: 100%;
  }

  .nike-layout {
    flex: 1;
    width: 100%;
  }


  /* Full width styling */
  .w-full {
    width: 100% !important;
  }

  /* Hover effects */
  .hover\:shadow-md:hover {
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  }

  /* Smooth transitions */
  .transition-colors {
    transition-property: color, background-color, border-color;
    transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
    transition-duration: 150ms;
  }

  .transition-shadow {
    transition-property: box-shadow;
    transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
    transition-duration: 150ms;
  }

  /* Responsive adjustments */
  @media (max-width: 768px) {
    .grid-cols-1.md\:grid-cols-4 {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .grid-cols-1.md\:grid-cols-3 {
      grid-template-columns: 1fr;
    }
  }

  /* Custom styling for better UX */
  .aspect-square {
    aspect-ratio: 1 / 1;
  }

  /* Loading animation */
  @keyframes pulse {
    0%, 100% {
      opacity: 1;
    }
    50% {
      opacity: .5;
    }
  }

  .animate-pulse {
    animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
  }

  /* Remove any container max-width restrictions */
  * {
    box-sizing: border-box;
  }

  /* Ensure full width layout */
  main, .nike-layout {
    max-width: none !important;
  }
  </style>
