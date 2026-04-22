<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- Header/Nav -->
    <Nav />

    <!-- Breadcrumb -->
    <div class="bg-white border-b">
      <div class="max-w-6xl mx-auto px-4 py-3 text-sm">
        <nav class="flex items-center gap-2 text-gray-600">
          <router-link to="/" class="hover:text-coral-red">Trang chủ</router-link>
          <span>/</span>
          <span class="text-gray-800 font-medium">Tra cứu đơn hàng</span>
        </nav>
      </div>
    </div>

    <!-- Main Content -->
    <main class="flex-1">
      <div class="max-w-4xl mx-auto px-4 py-8">
        <!-- Order Tracking Component -->
        <OrderTracking />
      </div>
    </main>

    <!-- Footer -->
    <Footer />
  </div>
</template>

<script>
import Nav from '@/components/user/Nav.vue';
import Footer from '@/views/user/Footer.vue';
import OrderTracking from '@/components/user/card/OrderTracking.vue';

export default {
  name: 'OrderTrackingView',
  components: {
    Nav,
    Footer,
    OrderTracking
  }
};
</script>

<style scoped>
.text-coral-red {
  color: #ff6452;
}
</style>
