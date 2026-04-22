<template>
  <main class="lienhe">
    <Nav />
    <Hero />
    
    <!-- Header Section với gradient và animation -->
    <section class="relative overflow-hidden">
      <!-- Animated background -->
      <div class="absolute inset-0 bg-gradient-to-br from-emerald-400 via-green-500 to-teal-600">
        <div class="absolute inset-0 bg-black opacity-20"></div>
        <div class="absolute top-0 left-0 w-96 h-96 bg-white rounded-full mix-blend-multiply filter blur-3xl opacity-10 animate-blob"></div>
        <div class="absolute bottom-0 right-0 w-96 h-96 bg-yellow-300 rounded-full mix-blend-multiply filter blur-3xl opacity-10 animate-blob animation-delay-4000"></div>
      </div>
      
      <!-- Content -->
      <div class="relative z-10 py-24 px-4 md:px-8 lg:px-16">
        <div class="max-w-4xl mx-auto text-center">
          <h1 class="text-5xl md:text-6xl lg:text-7xl font-black text-white mb-6 animate-fade-in-down">
            Liên Hệ Với Chúng Tôi
          </h1>
          <p class="text-xl md:text-2xl text-white/90 animate-fade-in-up animation-delay-200">
            Chúng tôi luôn sẵn sàng lắng nghe và hỗ trợ bạn
          </p>
          <div class="mt-8 animate-fade-in-up animation-delay-400">
            <a href="#contact-form" class="inline-flex items-center bg-white text-green-600 px-8 py-4 rounded-full font-semibold hover:bg-gray-100 transform hover:scale-105 transition-all duration-300 shadow-lg">
              Gửi tin nhắn ngay <i class="fas fa-arrow-down ml-2"></i>
            </a>
          </div>
        </div>
      </div>
    </section>

    <!-- Contact Information Cards với hover effects -->
    <section class="py-20 px-4 md:px-8 lg:px-16 bg-gray-50">
      <div class="max-w-7xl mx-auto">
        <div class="text-center mb-16">
          <h2 class="text-4xl md:text-5xl font-bold text-gray-900 mb-4">
            Thông Tin 
            <span class="text-transparent bg-clip-text bg-gradient-to-r from-green-600 to-teal-600">
              Liên Hệ
            </span>
          </h2>
          <div class="w-24 h-1 bg-gradient-to-r from-green-600 to-teal-600 mx-auto rounded-full"></div>
        </div>
        
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <!-- Card 1 - Địa chỉ -->
          <div class="group relative bg-white rounded-2xl p-8 shadow-lg hover:shadow-2xl transition-all duration-500 hover:-translate-y-2 overflow-hidden">
            <div class="absolute top-0 right-0 w-32 h-32 bg-green-500 rounded-full opacity-5 -mr-16 -mt-16 group-hover:scale-150 transition-transform duration-500"></div>
            <div class="relative z-10">
              <div class="w-16 h-16 bg-gradient-to-br from-green-400 to-green-600 rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
                <i class="fas fa-map-marker-alt text-white text-2xl"></i>
              </div>
              <h3 class="text-2xl font-bold text-gray-900 mb-4">Địa chỉ văn phòng</h3>
              <p class="text-gray-600 leading-relaxed">134, 15 Huỳnh Văn Nghệ, Phường 15, Quận Tân Bình, TP HCM, Việt Nam</p>
              <a href="#" class="inline-flex items-center text-green-600 hover:text-green-700 mt-4 font-semibold">
                Xem bản đồ <i class="fas fa-arrow-right ml-2"></i>
              </a>
            </div>
          </div>

          <!-- Card 2 - Liên hệ -->
          <div class="group relative bg-white rounded-2xl p-8 shadow-lg hover:shadow-2xl transition-all duration-500 hover:-translate-y-2 overflow-hidden">
            <div class="absolute top-0 right-0 w-32 h-32 bg-blue-500 rounded-full opacity-5 -mr-16 -mt-16 group-hover:scale-150 transition-transform duration-500"></div>
            <div class="relative z-10">
              <div class="w-16 h-16 bg-gradient-to-br from-blue-400 to-blue-600 rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
                <i class="fas fa-phone-alt text-white text-2xl"></i>
              </div>
              <h3 class="text-2xl font-bold text-gray-900 mb-4">Hotline – Zalo</h3>
              <p class="text-gray-600 mb-2">
                <strong>Điện thoại:</strong> 
                <a href="tel:0368971050" class="text-blue-600 hover:text-blue-700">036.897.1050</a>
              </p>
              <p class="text-gray-600">
                <strong>Email:</strong> 
                <a href="mailto:info@thietkewebchuyen.com" class="text-blue-600 hover:text-blue-700">info@thietkewebchuyen.com</a>
              </p>
              <div class="flex gap-3 mt-4">
                <a href="#" class="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center text-blue-600 hover:bg-blue-600 hover:text-white transition-all duration-300">
                  <i class="fab fa-facebook-f"></i>
                </a>
                <a href="#" class="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center text-green-600 hover:bg-green-600 hover:text-white transition-all duration-300">
                  <i class="fas fa-comment-dots"></i>
                </a>
              </div>
            </div>
          </div>

          <!-- Card 3 - Giờ làm việc -->
          <div class="group relative bg-white rounded-2xl p-8 shadow-lg hover:shadow-2xl transition-all duration-500 hover:-translate-y-2 overflow-hidden">
            <div class="absolute top-0 right-0 w-32 h-32 bg-purple-500 rounded-full opacity-5 -mr-16 -mt-16 group-hover:scale-150 transition-transform duration-500"></div>
            <div class="relative z-10">
              <div class="w-16 h-16 bg-gradient-to-br from-purple-400 to-purple-600 rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
                <i class="fas fa-clock text-white text-2xl"></i>
              </div>
              <h3 class="text-2xl font-bold text-gray-900 mb-4">Giờ làm việc</h3>
              <div class="space-y-2">
                <p class="text-gray-600">
                  <strong>Thứ 2 - Thứ 6:</strong> 8:00 - 17:30
                </p>
                <p class="text-gray-600">
                  <strong>Thứ 7:</strong> 8:00 - 12:00
                </p>
                <p class="text-gray-600">
                  <strong>Chủ nhật:</strong> Nghỉ
                </p>
              </div>
              <p class="text-sm text-gray-500 mt-4 italic">
                (Ngoài giờ vui lòng liên hệ hotline)
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Contact Form Section với modern design -->
    <section id="contact-form" class="py-20 px-4 md:px-8 lg:px-16 bg-white">
      <div class="max-w-7xl mx-auto">
        <div class="text-center mb-16">
          <p class="text-sm font-bold text-green-600 uppercase tracking-wider mb-4">LIÊN HỆ NGAY</p>
          <h2 class="text-4xl md:text-5xl font-bold text-gray-900 mb-6">
            Bạn Có Câu Hỏi Hoặc 
            <span class="text-transparent bg-clip-text bg-gradient-to-r from-green-600 to-teal-600">
              Cần Tư Vấn?
            </span>
          </h2>
          <p class="text-xl text-gray-600 max-w-3xl mx-auto">
            Vui lòng để lại thông tin, chúng tôi sẽ phản hồi trong vòng 24h làm việc
          </p>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-2 gap-16 items-center">
          <!-- Left side - Features -->
          <div class="space-y-8">
            <div class="relative">
              <img src="" alt="Contact illustration" class="w-full max-w-md mx-auto rounded-2xl shadow-xl" />
              <div class="absolute -bottom-6 -right-6 bg-gradient-to-r from-green-500 to-teal-600 text-white p-6 rounded-2xl shadow-lg max-w-xs">
                <h4 class="font-bold text-lg mb-2">Cam kết của chúng tôi</h4>
                <ul class="space-y-2 text-sm">
                  <li class="flex items-start">
                    <i class="fas fa-check-circle mr-2 mt-0.5"></i>
                    <span>Phản hồi nhanh chóng</span>
                  </li>
                  <li class="flex items-start">
                    <i class="fas fa-check-circle mr-2 mt-0.5"></i>
                    <span>Tư vấn chuyên nghiệp</span>
                  </li>
                  <li class="flex items-start">
                    <i class="fas fa-check-circle mr-2 mt-0.5"></i>
                    <span>Hỗ trợ 24/7</span>
                  </li>
                </ul>
              </div>
            </div>
            
            <!-- Contact methods -->
            <div class="grid grid-cols-2 gap-4 mt-12">
              <div class="bg-gray-50 p-6 rounded-xl text-center hover:bg-green-50 transition-colors duration-300">
                <i class="fas fa-headset text-3xl text-green-600 mb-3"></i>
                <h5 class="font-semibold text-gray-900">Tư vấn trực tiếp</h5>
                <p class="text-sm text-gray-600 mt-2">Gọi ngay hotline</p>
              </div>
              <div class="bg-gray-50 p-6 rounded-xl text-center hover:bg-blue-50 transition-colors duration-300">
                <i class="fas fa-comments text-3xl text-blue-600 mb-3"></i>
                <h5 class="font-semibold text-gray-900">Chat online</h5>
                <p class="text-sm text-gray-600 mt-2">Messenger & Zalo</p>
              </div>
            </div>
          </div>

          <!-- Right side - Form -->
          <div class="bg-gradient-to-br from-gray-50 to-white p-10 rounded-3xl shadow-xl">
            <form class="space-y-6">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label for="fullName" class="block text-sm font-semibold text-gray-700 mb-2">
                    Họ và tên <span class="text-red-500">*</span>
                  </label>
                  <input type="text" id="fullName" required
                         class="w-full px-5 py-3 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-all duration-300" />
                </div>
                <div>
                  <label for="company" class="block text-sm font-semibold text-gray-700 mb-2">
                    Công ty
                  </label>
                  <input type="text" id="company"
                         class="w-full px-5 py-3 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-all duration-300" />
                </div>
              </div>
              
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label for="email" class="block text-sm font-semibold text-gray-700 mb-2">
                    Email <span class="text-red-500">*</span>
                  </label>
                  <input type="email" id="email" required
                         class="w-full px-5 py-3 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-all duration-300" />
                </div>
                <div>
                  <label for="phone" class="block text-sm font-semibold text-gray-700 mb-2">
                    Số điện thoại <span class="text-red-500">*</span>
                  </label>
                  <input type="tel" id="phone" required
                         class="w-full px-5 py-3 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-all duration-300" />
                </div>
              </div>
              
              <div>
                <label for="subject" class="block text-sm font-semibold text-gray-700 mb-2">
                  Chủ đề
                </label>
                <select id="subject" class="w-full px-5 py-3 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-all duration-300">
                  <option value="">Chọn chủ đề</option>
                  <option value="product">Tư vấn sản phẩm</option>
                  <option value="order">Đơn hàng</option>
                  <option value="support">Hỗ trợ kỹ thuật</option>
                  <option value="other">Khác</option>
                </select>
              </div>
              
              <div>
                <label for="message" class="block text-sm font-semibold text-gray-700 mb-2">
                  Nội dung tin nhắn <span class="text-red-500">*</span>
                </label>
                <textarea id="message" rows="5" required
                          class="w-full px-5 py-3 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-all duration-300 resize-y"></textarea>
              </div>
              
              <div class="flex items-center">
                <input type="checkbox" id="privacy" class="mr-3 w-4 h-4 text-green-600 rounded focus:ring-green-500">
                <label for="privacy" class="text-sm text-gray-600">
                  Tôi đồng ý với <a href="#" class="text-green-600 hover:text-green-700 font-semibold">chính sách bảo mật</a>
                </label>
              </div>
              
              <button type="submit"
                      class="w-full bg-gradient-to-r from-green-600 to-teal-600 hover:from-green-700 hover:to-teal-700 text-white font-bold py-4 px-8 rounded-xl transition-all duration-300 transform hover:scale-[1.02] shadow-lg">
                <i class="fas fa-paper-plane mr-2"></i> Gửi tin nhắn
              </button>
            </form>
          </div>
        </div>
      </div>
    </section>

    <!-- Map Section -->
    <section class="relative h-96 bg-gray-200">
      <div class="absolute inset-0 bg-gradient-to-b from-transparent to-black/20"></div>
      <iframe 
        src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3919.0824710975044!2d106.63657131480079!3d10.805183992303403!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31752935e97b0445%3A0x7edabc7f961f6b79!2zMTM0IEh14buzbmggVsSDbiBOZ2jhu4csIFBoxrDhu51uZyAxNSwgVMOibiBCw6xuaCwgVGjDoG5oIHBo4buRIEjhu5MgQ2jDrSBNaW5oLCBWaeG7h3QgTmFt!5e0!3m2!1svi!2s!4v1635841420185!5m2!1svi!2s"
        width="100%" 
        height="100%" 
        style="border:0;" 
        allowfullscreen="" 
        loading="lazy"
        class="w-full h-full">
      </iframe>
      <div class="absolute bottom-8 left-8 bg-white p-6 rounded-xl shadow-lg max-w-xs">
        <h4 class="font-bold text-gray-900 mb-2">Ghé thăm chúng tôi</h4>
        <p class="text-sm text-gray-600">Chúng tôi luôn chào đón bạn đến văn phòng để trao đổi trực tiếp</p>
        <a href="#" class="inline-flex items-center text-green-600 hover:text-green-700 mt-3 font-semibold text-sm">
          Chỉ đường <i class="fas fa-directions ml-2"></i>
        </a>
      </div>
    </section>

    <section id="footer-section" class="padding-x padding-t bg-black pb-8">
      <Footer />
    </section>

    <!-- Modern Floating Buttons -->
    <div class="fixed bottom-6 left-6 z-50">
      <button class="group bg-gradient-to-r from-green-600 to-teal-600 hover:from-green-700 hover:to-teal-700 text-white font-bold py-4 px-6 rounded-full shadow-lg flex items-center space-x-3 transition-all duration-300 transform hover:scale-105">
        <div class="w-10 h-10 bg-white/20 rounded-full flex items-center justify-center group-hover:bg-white/30 transition-colors duration-300">
          <i class="fas fa-headset text-xl"></i>
        </div>
        <span>Hỗ trợ 24/7</span>
      </button>
    </div>

    <div class="fixed bottom-6 right-6 z-50 flex flex-col space-y-4">
      <!-- Messenger -->
      <button class="group relative w-14 h-14 bg-gradient-to-br from-blue-500 to-blue-700 hover:from-blue-600 hover:to-blue-800 text-white rounded-full shadow-lg flex items-center justify-center transition-all duration-300 transform hover:scale-110">
        <i class="fab fa-facebook-messenger text-2xl"></i>
        <span class="absolute right-full mr-3 bg-gray-900 text-white text-sm px-3 py-1 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity duration-300 whitespace-nowrap">
          Chat Messenger
        </span>
      </button>
      
      <!-- Zalo -->
      <button class="group relative w-14 h-14 bg-gradient-to-br from-blue-400 to-blue-600 hover:from-blue-500 hover:to-blue-700 text-white rounded-full shadow-lg flex items-center justify-center transition-all duration-300 transform hover:scale-110">
        <span class="font-bold text-xl">Z</span>
        <span class="absolute right-full mr-3 bg-gray-900 text-white text-sm px-3 py-1 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity duration-300 whitespace-nowrap">
          Chat Zalo
        </span>
      </button>
      
      <!-- Back to top -->
      <button onclick="window.scrollTo({top: 0, behavior: 'smooth'})" class="group relative w-14 h-14 bg-gradient-to-br from-gray-600 to-gray-800 hover:from-gray-700 hover:to-gray-900 text-white rounded-full shadow-lg flex items-center justify-center transition-all duration-300 transform hover:scale-110">
        <i class="fas fa-arrow-up text-xl"></i>
        <span class="absolute right-full mr-3 bg-gray-900 text-white text-sm px-3 py-1 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity duration-300 whitespace-nowrap">
          Lên đầu trang
        </span>
      </button>
    </div>
    <!-- ===== ĐÃ THÊM CHATBOT COMPONENT ===== -->
    <ChatBot />
  </main>
</template>

<script>
import Nav from '@/components/user/Nav.vue';  
import Footer from '@/views/user/Footer.vue';
import Hero from '../Hero.vue';
import ChatBot from '@/components/ChatBotAndReview/ChatBot.vue';

export default {
  name: 'Lienhe',
  components: {
    Nav,
    Footer,
    Hero,
    ChatBot
  },
};
</script>

<style scoped>
/* Font imports */
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap');
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');

.lienhe {
  font-family: 'Inter', sans-serif;
  background-color: #ffffff;
  min-height: 100vh;
}

/* Animations */
@keyframes blob {
  0% {
    transform: translate(0px, 0px) scale(1);
  }
  33% {
    transform: translate(30px, -50px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
  100% {
    transform: translate(0px, 0px) scale(1);
  }
}

@keyframes fade-in-down {
  0% {
    opacity: 0;
    transform: translateY(-20px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fade-in-up {
  0% {
    opacity: 0;
    transform: translateY(20px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-blob {
  animation: blob 7s infinite;
}

.animate-fade-in-down {
  animation: fade-in-down 0.8s ease-out;
}

.animate-fade-in-up {
  animation: fade-in-up 0.8s ease-out;
}

.animation-delay-200 {
  animation-delay: 0.2s;
  animation-fill-mode: both;
}

.animation-delay-400 {
  animation-delay: 0.4s;
  animation-fill-mode: both;
}

.animation-delay-4000 {
  animation-delay: 4s;
}

/* Gradient text */
.text-transparent {
  color: transparent;
}

.bg-clip-text {
  -webkit-background-clip: text;
  background-clip: text;
}

/* Padding utilities */
.padding-x {
  padding-left: 1rem;
  padding-right: 1rem;
}

@media (min-width: 768px) {
  .padding-x {
    padding-left: 2rem;
    padding-right: 2rem;
  }
}

.padding-t {
  padding-top: 3rem;
}

/* Hover effects */
.group:hover .group-hover\:scale-110 {
  transform: scale(1.1);
}

.group:hover .group-hover\:scale-150 {
  transform: scale(1.5);
}

.group:hover .group-hover\:opacity-100 {
  opacity: 1;
}

/* Smooth scrolling */
html {
  scroll-behavior: smooth;
}

/* Custom form styles */
input:focus,
textarea:focus,
select:focus {
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

/* Map overlay fix */
iframe {
  filter: grayscale(20%);
}
</style>