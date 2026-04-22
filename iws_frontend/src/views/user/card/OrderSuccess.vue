<template>
    <div class="nike-complete-layout">
      <!-- Navigation Component -->
      <Nav />

      <!-- Breadcrumb -->
      <div class="breadcrumb-section bg-gray-50 py-4">
        <div class="w-full mx-auto px-6">
          <nav class="flex items-center space-x-2 text-sm">
            <router-link to="/" class="text-gray-600 hover:text-orange-500 transition-colors">
              Trang chủ
            </router-link>
            <span class="text-gray-400">></span>
            <span class="text-gray-800 font-medium">Đặt hàng thành công</span>
          </nav>
        </div>
      </div>

      <!-- Main Content -->
      <main class="nike-layout w-full bg-gray-50 pt-8 pb-0">
        <div class="w-full mx-auto px-6">

          <!-- Success Header -->
          <div class="success-header text-center mb-8">
            <div class="success-animation mb-6">
              <div class="checkmark-container">
                <svg class="checkmark" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
                  <circle class="checkmark-circle" cx="26" cy="26" r="25" fill="none"/>
                  <path class="checkmark-check" fill="none" d="m14.1 27.2l7.1 7.2 16.7-16.8"/>
                </svg>
              </div>
            </div>
            <h1 class="success-title text-4xl font-bold text-gray-800 mb-4">Đặt hàng thành công!</h1>
            <p class="success-subtitle text-xl text-gray-600">Cảm ơn bạn đã tin tưởng và mua sắm tại MyShoes</p>
          </div>

          <!-- Order Information Card -->
          <div class="max-w-4xl mx-auto mb-8">
            <div class="info-card bg-white rounded-lg shadow-lg overflow-hidden">
              <div class="card-header bg-gradient-to-r from-orange-500 to-red-500 text-white p-6">
                <div class="flex items-center gap-3">
                  <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9 2a1 1 0 000 2h2a1 1 0 100-2H9z"></path>
                    <path fill-rule="evenodd" d="M4 5a2 2 0 012-2v1a1 1 0 001 1h6a1 1 0 001-1V3a2 2 0 012 2v6a2 2 0 01-2 2H6a2 2 0 01-2-2V5zm3 4a1 1 0 000 2h.01a1 1 0 100-2H7zm3 0a1 1 0 000 2h3a1 1 0 100-2h-3z"></path>
                  </svg>
                  <h3 class="text-2xl font-semibold">Thông tin đơn hàng</h3>
                </div>
              </div>
              <div class="card-content p-8">
                <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">

                  <!-- Thông tin đơn hàng -->
                  <div class="space-y-4">
                    <h4 class="text-lg font-semibold text-gray-800 mb-4 border-b border-gray-200 pb-2">Chi tiết đơn hàng</h4>

                    <div class="info-row flex justify-between items-center py-3 border-b border-gray-100">
                      <span class="label font-medium text-gray-600">Mã đơn hàng:</span>
                      <span class="value font-bold text-orange-600 font-mono">#{{ orderId }}</span>
                    </div>

                    <div class="info-row flex justify-between items-center py-3 border-b border-gray-100">
                      <span class="label font-medium text-gray-600">Ngày đặt:</span>
                      <span class="value font-semibold text-gray-800">{{ currentDate }}</span>
                    </div>

                    <div class="info-row flex justify-between items-center py-3 border-b border-gray-100">
                      <span class="label font-medium text-gray-600">Khách hàng:</span>
                      <span class="value font-semibold text-gray-800">{{ customerName }}</span>
                    </div>

                    <div class="info-row flex justify-between items-center py-3 border-b border-gray-100">
                      <span class="label font-medium text-gray-600">Email:</span>
                      <span class="value font-semibold text-gray-800">{{ customerEmail }}</span>
                    </div>

                    <div class="info-row flex justify-between items-center py-3 border-b border-gray-100">
                      <span class="label font-medium text-gray-600">Số điện thoại:</span>
                      <span class="value font-semibold text-gray-800">{{ customerPhone }}</span>
                    </div>

                    <div class="info-row flex justify-between items-start py-3 border-b border-gray-100">
                      <span class="label font-medium text-gray-600">Địa chỉ giao hàng:</span>
                      <span class="value font-semibold text-gray-800 text-right max-w-64">{{ shippingAddress }}</span>
                    </div>

                    <div class="info-row flex justify-between items-center py-3">
                      <span class="label font-medium text-gray-600">Phương thức thanh toán:</span>
                      <span class="value font-semibold text-gray-800">{{ paymentMethodText }}</span>
                    </div>
                  </div>

                  <!-- Sản phẩm đã đặt -->
                  <div class="space-y-4">
                    <h4 class="text-lg font-semibold text-gray-800 mb-4 border-b border-gray-200 pb-2">Sản phẩm đã đặt</h4>

                    <div v-if="cartItems.length > 0" class="space-y-3 max-h-64 overflow-y-auto">
                      <div v-for="item in cartItems" :key="item.code || item.productDetailId" class="flex gap-3 pb-3 border-b border-gray-100 last:border-0">
                        <img
                          :src="item.image || '/placeholder-shoe.png'"
                          :alt="item.name"
                          class="w-16 h-16 object-cover rounded bg-gray-100"
                          @error="handleImageError"
                        />
                        <div class="flex-1">
                          <h5 class="font-medium text-gray-800 text-sm line-clamp-2">{{ item.name }}</h5>
                          <p class="text-xs text-gray-500 mt-1">
                            {{ item.code }} | Size: {{ item.size }}
                            <span v-if="item.color"> | {{ item.color.name || item.color }}</span>
                          </p>
                          <div class="flex justify-between items-center mt-2">
                            <span class="text-xs text-gray-600">SL: {{ item.quantity }}</span>
                            <span class="text-sm font-semibold text-orange-600">{{ formatMoney(item.price * item.quantity) }}</span>
                          </div>
                        </div>
                      </div>
                    </div>

                    <div v-else class="text-center py-6 text-gray-500">
                      <svg class="w-12 h-12 mx-auto mb-2 text-gray-300" fill="currentColor" viewBox="0 0 20 20">
                        <path d="M3 1a1 1 0 000 2h1.22l.305 1.222a.997.997 0 00.01.042l1.358 5.43-.893.892C3.74 11.846 4.632 14 6.414 14H15a1 1 0 000-2H6.414l1-1H14a1 1 0 00.894-.553l3-6A1 1 0 0017 3H6.28l-.31-1.243A1 1 0 005 1H3zM16 16.5a1.5 1.5 0 11-3 0 1.5 1.5 0 013 0zM6.5 18a1.5 1.5 0 100-3 1.5 1.5 0 000 3z"></path>
                      </svg>
                      <p>Không có thông tin sản phẩm</p>
                    </div>
                  </div>
                </div>

                <div class="space-y-4">
                  <!-- Tổng quan đơn hàng -->
                  <div>
                    <h4 class="text-lg font-semibold text-gray-800 mb-4 border-b border-gray-200 pb-2">Tổng quan đơn hàng</h4>

                    <div class="summary-row flex justify-between items-center py-3 border-b border-gray-100">
                      <span class="font-medium text-gray-600">Tổng sản phẩm:</span>
                      <span class="font-semibold text-gray-800">{{ totalItems }} sản phẩm</span>
                    </div>

                    <div class="summary-row flex justify-between items-center py-3 border-b border-gray-100">
                      <span class="font-medium text-gray-600">Tạm tính:</span>
                      <span class="font-semibold text-gray-800">{{ formatMoney(subtotal) }}</span>
                    </div>

                    <div v-if="discount > 0" class="summary-row flex justify-between items-center py-3 border-b border-gray-100">
                      <span class="font-medium text-gray-600">Giảm giá:</span>
                      <span class="font-semibold text-green-600">-{{ formatMoney(discount) }}</span>
                    </div>

                    <div class="summary-row flex justify-between items-center py-3 border-b border-gray-100">
                      <span class="font-medium text-gray-600">Phí vận chuyển:</span>
                      <span class="font-semibold text-gray-800">{{ shippingFee === 0 ? 'Miễn phí' : formatMoney(shippingFee) }}</span>
                    </div>

                    <div class="summary-row total flex justify-between items-center py-4 border-t-2 border-orange-500 bg-orange-50 rounded-lg px-4 mt-4">
                      <span class="font-bold text-xl text-gray-800">Tổng cộng:</span>
                      <span class="font-bold text-2xl text-orange-600">{{ formatMoney(total) }}</span>
                    </div>

                    <!-- Tiết kiệm -->
                    <div v-if="discount > 0" class="bg-green-50 border border-green-200 rounded-lg p-4 mt-4">
                      <div class="flex items-center gap-2">
                        <svg class="w-5 h-5 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                          <path fill-rule="evenodd" d="M6.267 3.455a3.066 3.066 0 001.745-.723 3.066 3.066 0 013.976 0 3.066 3.066 0 001.745.723 3.066 3.066 0 012.812 2.812c.051.643.304 1.254.723 1.745a3.066 3.066 0 010 3.976 3.066 3.066 0 00-.723 1.745 3.066 3.066 0 01-2.812 2.812 3.066 3.066 0 00-1.745.723 3.066 3.066 0 01-3.976 0 3.066 3.066 0 00-1.745-.723 3.066 3.066 0 01-2.812-2.812 3.066 3.066 0 00-.723-1.745 3.066 3.066 0 010-3.976 3.066 3.066 0 00.723-1.745 3.066 3.066 0 012.812-2.812zm7.44 5.252a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"></path>
                        </svg>
                        <span class="font-medium text-green-800">Bạn đã tiết kiệm được {{ formatMoney(discount) }}!</span>
                      </div>
                    </div>

                    <!-- Ghi chú -->
                    <div v-if="orderNote" class="bg-blue-50 border border-blue-200 rounded-lg p-4 mt-4">
                      <div class="flex items-start gap-2">
                        <svg class="w-5 h-5 text-blue-600 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                          <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"></path>
                        </svg>
                        <div>
                          <span class="font-medium text-blue-800">Ghi chú:</span>
                          <p class="text-blue-700 mt-1">{{ orderNote }}</p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="actions-section text-center mb-8">
            <div class="flex flex-col sm:flex-row gap-4 justify-center items-center">
              <router-link to="/orders"
                          class="btn btn-primary bg-orange-500 hover:bg-orange-600 text-white px-8 py-4 rounded-lg font-semibold transition-all duration-300 transform hover:scale-105 shadow-lg flex items-center gap-3 min-w-48">
                <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M9 2a1 1 0 000 2h2a1 1 0 100-2H9z"></path>
                  <path fill-rule="evenodd" d="M4 5a2 2 0 012-2v1a1 1 0 001 1h6a1 1 0 001-1V3a2 2 0 012 2v6a2 2 0 01-2 2H6a2 2 0 01-2-2V5zm3 4a1 1 0 000 2h.01a1 1 0 100-2H7zm3 0a1 1 0 000 2h3a1 1 0 100-2h-3z"></path>
                </svg>
                Theo dõi đơn hàng
              </router-link>

              <router-link to="/"
                          class="btn btn-secondary bg-white hover:bg-gray-50 text-gray-800 border-2 border-gray-300 px-8 py-4 rounded-lg font-semibold transition-all duration-300 transform hover:scale-105 shadow-lg flex items-center gap-3 min-w-48">
                <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M3 1a1 1 0 000 2h1.22l.305 1.222a.997.997 0 00.01.042l1.358 5.43-.893.892C3.74 11.846 4.632 14 6.414 14H15a1 1 0 000-2H6.414l1-1H14a1 1 0 00.894-.553l3-6A1 1 0 0017 3H6.28l-.31-1.243A1 1 0 005 1H3zM16 16.5a1.5 1.5 0 11-3 0 1.5 1.5 0 013 0zM6.5 18a1.5 1.5 0 100-3 1.5 1.5 0 000 3z"></path>
                </svg>
                Tiếp tục mua sắm
              </router-link>
            </div>
          </div>

        </div>
      </main>

      <!-- Footer Component -->
      <section id="#" class="padding-x padding-t bg-black pb-8">
        <Footer />
      </section>

      <ScrollToggler />
    </div>
  </template>

  <script setup>
  import { ref, computed, onMounted } from 'vue';
  import { useRoute } from 'vue-router';
  import Nav from '@/components/user/Nav.vue';
  import Footer from '@/views/user/Footer.vue';
  import ScrollToggler from '@/components/user/ScrollToggler.vue';

  const route = useRoute();

  // Lấy dữ liệu từ route state (được truyền từ trang checkout)
  const successPageData = ref(route.params.successPageData || {});
  const orderId = ref(route.params.orderId || 'MS' + Math.random().toString(36).substr(2, 9).toUpperCase());

  // Dữ liệu đơn hàng - sử dụng dữ liệu từ checkout hoặc localStorage
  const customerName = ref('');
  const customerEmail = ref('');
  const customerPhone = ref('');
  const shippingAddress = ref('');
  const paymentMethod = ref('cod');
  const orderNote = ref('');

  // Dữ liệu tài chính
  const totalItems = ref(0);
  const subtotal = ref(0);
  const discount = ref(0);
  const shippingFee = ref(0);
  const total = ref(0);

  // Dữ liệu sản phẩm
  const cartItems = ref([]);

  const currentDate = computed(() => {
    return new Date().toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  });

  const paymentMethodText = computed(() => {
    return paymentMethod.value === 'cod' ? 'COD - Thanh toán khi nhận hàng' : 'Chuyển khoản ngân hàng';
  });

  const formatMoney = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  };

  // Handle image error
  const handleImageError = (event) => {
    event.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjQiIGhlaWdodD0iNjQiIHZpZXdCb3g9IjAgMCA2NCA2NCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjY0IiBoZWlnaHQ9IjY0IiBmaWxsPSIjRjNGNEY2Ii8+CjxwYXRoIGQ9Ik0yMCAyMEg0NFY0NEgyMFYyMFoiIHN0cm9rZT0iIzlDQTNBRiIgc3Ryb2tlLXdpZHRoPSIyIiBzdHJva2UtbGluZWNhcD0icm91bmQiIHN0cm9rZS1saW5lam9pbj0icm91bmQiLz4KPHBhdGggZD0iTTI4IDI4TDM2IDM2TDQwIDMyIiBzdHJva2U9IiM5Q0EzQUYiIHN0cm9rZS13aWR0aD0iMiIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIiBzdHJva2UtbGluZWpvaW49InJvdW5kIi8+Cjwvc3ZnPgo=';
  };

  // Lấy dữ liệu từ localStorage hoặc sessionStorage
  const loadOrderData = () => {
    try {
      console.log('📦 Loading order data...');

      // Thử lấy từ sessionStorage trước (dữ liệu từ checkout)
      const sessionOrderData = sessionStorage.getItem('order_success_data');
      if (sessionOrderData) {
        const data = JSON.parse(sessionOrderData);
        console.log('✅ Found order data in session:', data);

        // Thông tin khách hàng
        customerName.value = data.customerName || data.shippingInfo?.fullName || '';
        customerEmail.value = data.customerEmail || data.shippingInfo?.email || '';
        customerPhone.value = data.customerPhone || data.shippingInfo?.phone || '';
        shippingAddress.value = data.shippingAddress || data.addressText || '';
        paymentMethod.value = data.paymentMethod || 'cod';
        orderNote.value = data.orderNote || data.shippingInfo?.note || '';

        // Thông tin tài chính
        totalItems.value = data.totalItems || 0;
        subtotal.value = data.subtotal || 0;
        discount.value = data.discount || data.discountValue || 0;
        shippingFee.value = data.shippingFee || 0;
        total.value = data.total || 0;

        // Thông tin sản phẩm
        cartItems.value = data.cartItems || [];

        // Xóa dữ liệu tạm thời sau khi sử dụng
        sessionStorage.removeItem('order_success_data');
        return;
      }

      console.log('⚠️ No session data found, trying localStorage...');

      // Fallback: Lấy từ localStorage
      const cartData = localStorage.getItem('cart');
      const userInfo = localStorage.getItem('user_info');

      if (cartData) {
        const cart = JSON.parse(cartData);
        console.log('📋 Found cart data:', cart);

        // Tính toán từ giỏ hàng
        totalItems.value = cart.reduce((sum, item) => sum + item.quantity, 0);
        subtotal.value = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
        shippingFee.value = subtotal.value >= 300000 ? 0 : 30000;
        total.value = subtotal.value + shippingFee.value - discount.value;

        // Map cart items để hiển thị
        cartItems.value = cart.map(item => ({
          name: item.name || item.productName || 'Sản phẩm',
          code: item.code || item.productCode || '',
          size: item.size || '',
          color: item.color || '',
          quantity: item.quantity || 1,
          price: item.price || 0,
          image: item.image || item.imageUrl || '/placeholder-shoe.png'
        }));
      }

      // Lấy thông tin khách hàng
      if (userInfo) {
        const user = JSON.parse(userInfo);
        console.log('👤 Found user info:', user);

        customerName.value = user.hoTen || user.fullName || user.name || '';
        customerEmail.value = user.email || '';
        customerPhone.value = user.sdt || user.phone || '';
      }

      // Fallback dữ liệu mẫu nếu không có gì
      if (!customerName.value) {
        console.log('🔄 Using fallback data...');
        customerName.value = 'Phạm Thị Khách Hàng';
        customerEmail.value = 'khachhang1@gmail.com';
        customerPhone.value = '0901234567';
        shippingAddress.value = '321 Hàng, Phường Phan Chu Trinh, Hoàn Kiếm, Hà Nội';

        // Dữ liệu mẫu nếu không có sản phẩm
        if (cartItems.value.length === 0) {
          cartItems.value = [
            {
              name: 'Nike Air Max 270',
              code: 'MSL1',
              size: '40',
              color: 'Đen',
              quantity: 1,
              price: 2000000,
              image: '/placeholder-shoe.png'
            }
          ];
          totalItems.value = 1;
          subtotal.value = 2000000;
          shippingFee.value = 0; // Miễn phí vì > 300k
          total.value = 2000000;
        }
      }

    } catch (error) {
      console.error('❌ Error loading order data:', error);

      // Dữ liệu khẩn cấp
      customerName.value = 'Khách hàng';
      customerEmail.value = 'customer@example.com';
      customerPhone.value = '0123456789';
      shippingAddress.value = 'Địa chỉ giao hàng';
      totalItems.value = 1;
      subtotal.value = 1000000;
      shippingFee.value = 30000;
      total.value = 1030000;
      cartItems.value = [
        {
          name: 'Sản phẩm mẫu',
          code: 'SAMPLE',
          size: '40',
          quantity: 1,
          price: 1000000,
          image: '/placeholder-shoe.png'
        }
      ];
    }
  };

  onMounted(() => {
    console.log('🚀 Order success page mounted');
    console.log('📋 Route params:', route.params);

    loadOrderData();

    console.log('✅ Order data loaded:', {
      customerName: customerName.value,
      customerEmail: customerEmail.value,
      customerPhone: customerPhone.value,
      shippingAddress: shippingAddress.value,
      totalItems: totalItems.value,
      subtotal: subtotal.value,
      discount: discount.value,
      shippingFee: shippingFee.value,
      total: total.value
    });
  });
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

  /* Checkmark Animation */
  .checkmark-container {
    display: inline-block;
  }

  .checkmark {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    display: block;
    stroke-width: 3;
    stroke: #4caf50;
    stroke-miterlimit: 10;
    margin: 0 auto;
    animation: checkmark-scale 0.3s ease-in-out 0.9s both;
  }

  .checkmark-circle {
    stroke-dasharray: 166;
    stroke-dashoffset: 166;
    stroke-width: 3;
    stroke-miterlimit: 10;
    stroke: #4caf50;
    fill: rgba(76, 175, 80, 0.1);
    animation: checkmark-stroke 0.6s cubic-bezier(0.65, 0, 0.45, 1) forwards;
  }

  .checkmark-check {
    transform-origin: 50% 50%;
    stroke-dasharray: 48;
    stroke-dashoffset: 48;
    stroke: #4caf50;
    animation: checkmark-stroke 0.3s cubic-bezier(0.65, 0, 0.45, 1) 0.8s forwards;
  }

  @keyframes checkmark-stroke {
    100% {
      stroke-dashoffset: 0;
    }
  }

  @keyframes checkmark-scale {
    0%, 100% {
      transform: none;
    }
    50% {
      transform: scale3d(1.1, 1.1, 1);
    }
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

  .transition-all {
    transition-property: all;
    transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
    transition-duration: 300ms;
  }

  /* Button hover effects */
  .btn:hover {
    transform: translateY(-2px);
  }

  /* Responsive adjustments */
  @media (max-width: 768px) {
    .grid-cols-1.md\:grid-cols-4 {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .lg\:grid-cols-2 {
      grid-template-columns: 1fr;
    }

    .flex-col.sm\:flex-row {
      flex-direction: column;
    }

    .success-title {
      font-size: 2rem;
    }

    .min-w-48 {
      min-width: 100%;
    }
  }

  @media (max-width: 480px) {
    .success-title {
      font-size: 1.75rem;
    }

    .checkmark {
      width: 60px;
      height: 60px;
    }

    .card-content,
    .card-header {
      padding: 1rem;
    }
  }

  /* Remove any container max-width restrictions */
  * {
    box-sizing: border-box;
  }

  /* Ensure full width layout */
  main, .nike-layout {
    max-width: none !important;
  }

  /* Line clamp utilities */
  .line-clamp-2 {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .line-clamp-1 {
    display: -webkit-box;
    -webkit-line-clamp: 1;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  /* Custom scrollbar for product list */
  .overflow-y-auto::-webkit-scrollbar {
    width: 6px;
  }

  .overflow-y-auto::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 10px;
  }

  .overflow-y-auto::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 10px;
  }

  .overflow-y-auto::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
  }

  /* Last child border removal */
  .last\:border-0:last-child {
    border: none !important;
  }
  </style>
