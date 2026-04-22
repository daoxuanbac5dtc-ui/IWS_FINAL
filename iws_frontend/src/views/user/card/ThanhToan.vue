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
            <router-link to="/" class="text-gray-600 hover:text-orange-500 transition-colors">

            </router-link>
            <span class="text-gray-400"></span>
            <span class="text-gray-900 font-medium"></span>
          </nav>
        </div>
      </div>

      <!-- Main Content -->
      <main class="nike-layout w-full bg-gray-50 py-8">
        <div class="w-full mx-auto px-6">
          <!-- Checkout Component -->
          <CheckoutForm
            ref="checkoutComponent"
            @order-success="handleOrderSuccess"
            @go-back="handleGoBack"
          />
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
  import Footer from '@/views/user/Footer.vue';
  import ScrollToggler from '@/components/user/ScrollToggler.vue';
  import CheckoutForm from '@/components/user/card/ThanhToanUser.vue';

  export default {
    name: 'CheckoutPage',

    components: {
      Nav,
      Footer,
      ScrollToggler,
      CheckoutForm
    },

    methods: {
      handleOrderSuccess(orderData) {
        // Handle successful order
        console.log('Order placed successfully:', orderData);
        // The CheckoutForm component will handle the redirect
      },

      handleGoBack() {
        this.$router.push('/card');
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

  /* Ensure full width layout */
  main, .nike-layout {
    max-width: none !important;
  }

  /* Smooth transitions */
  .transition-colors {
    transition-property: color, background-color, border-color;
    transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
    transition-duration: 150ms;
  }

  /* Remove any container max-width restrictions */
  * {
    box-sizing: border-box;
  }

  /* Padding classes for footer */
  .padding-x {
    padding-left: 1.5rem;
    padding-right: 1.5rem;
  }

  .padding-t {
    padding-top: 3rem;
  }

  @media (min-width: 640px) {
    .padding-x {
      padding-left: 2rem;
      padding-right: 2rem;
    }
  }

  @media (min-width: 1024px) {
    .padding-x {
      padding-left: 3rem;
      padding-right: 3rem;
    }
  }
  </style>
