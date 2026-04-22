<template>
    <main class="relative">
        <Nav />
        <section id="home" class="xl:padding-l wide:padding-r padding-b overflow-hidden">
            <Hero />
        </section>
        <section id="products" class="padding">
            <PopularProducts />
        </section>
        <section id="about-us" class="padding">
            <SuperQuality />
        </section>
        <section class="padding-x py-10">
            <Services />
        </section>
        <!-- <section class="padding"><SpecialOffers /></section> -->
        <section class="padding bg-pale-blue">
            <CustomerReviews />
        </section>
        <!-- <section id="contact-us" class="padding-x sm:py-32 py-16">
            <Subscribe />
        </section> -->
        <section id="#" class="padding-x padding-t bg-black pb-8">
            <Footer />
        </section>
        <ScrollToggler />
        
        <!-- ===== ĐÃ THÊM CHATBOT COMPONENT ===== -->
        <ChatBot />
    </main>
</template>

<script setup>
import Hero from '@/views/user/Hero.vue';
import Services from '@/views/user/Services.vue';
//import SpecialOffers from '@/views/user/SpecialOffers.vue';
import Nav from '@/components/user/Nav.vue';
import ScrollToggler from '@/components/user/ScrollToggler.vue';
import CustomerReviews from '@/views/user/CustomerReviews.vue';
import Footer from '@/views/user/Footer.vue';
import PopularProducts from '@/views/user/PopularProducts.vue';
import SuperQuality from '@/views/user/SuperQuality.vue';
// ===== ĐÃ THÊM CHATBOT IMPORT =====
import ChatBot from '@/components/ChatBotAndReview/ChatBot.vue';

// Log để debug
console.log('userHome component loaded');
</script>

<style scoped>
/* ===== THÊM CSS ĐỂ ĐẢM BẢO CHATBOT HIỂN THỊ ĐÚNG ===== */
/* Đảm bảo chatbot hiển thị trên cùng */
:deep(.chatbot-container) {
  z-index: 9999 !important;
}

/* Đảm bảo không conflict với ScrollToggler */
:deep(.scroll-toggler) {
  z-index: 9998;
}

/* Responsive cho mobile */
@media (max-width: 768px) {
  :deep(.chatbot-container) {
    bottom: 1rem;
    right: 1rem;
  }
  
  :deep(.chat-window) {
    width: calc(100vw - 2rem);
    height: 70vh;
  }
}
</style>