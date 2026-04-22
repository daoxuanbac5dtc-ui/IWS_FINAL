<template>
  <main class="gioithieu">
    <Nav />
    <Hero />
    <section id="about-us-content" class="relative overflow-hidden">
      <!-- Background decoration -->
      <div class="absolute inset-0 bg-gradient-to-br from-blue-50 via-white to-purple-50"></div>
      <div class="absolute top-0 left-0 w-96 h-96 bg-blue-200 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-blob"></div>
      <div class="absolute top-0 right-0 w-96 h-96 bg-purple-200 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-blob animation-delay-2000"></div>
      <div class="absolute bottom-0 left-1/2 w-96 h-96 bg-pink-200 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-blob animation-delay-4000"></div>
      
      <div class="relative z-10 py-20 px-4 md:px-8 lg:px-16">
        <div class="max-w-7xl mx-auto">
          <!-- Title with gradient -->
          <div class="text-center mb-20">
            <h1 class="text-6xl md:text-7xl font-black mb-6 animate-fade-in-down">
              <span class="bg-gradient-to-r from-blue-600 via-purple-600 to-pink-600 bg-clip-text text-transparent">
                Giới Thiệu Về Chúng Tôi
              </span>
            </h1>
            <div class="w-32 h-1 bg-gradient-to-r from-blue-600 to-purple-600 mx-auto rounded-full"></div>
          </div>

          <!-- Hero Section với hiệu ứng parallax -->
          <div class="relative mb-24 animate-fade-in-up">
            <div class="relative bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 rounded-3xl shadow-2xl overflow-hidden transform hover:scale-[1.02] transition-all duration-500">
              <div class="absolute inset-0 bg-black opacity-40"></div>
              <div class="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent"></div>
              
              <!-- Simple pattern background -->
              <div class="absolute inset-0 opacity-10">
                <div class="absolute inset-0 bg-gradient-to-br from-white/20 to-transparent"></div>
              </div>
              
              <div class="relative text-white p-12 md:p-16 lg:p-24 text-center">
                <h2 class="text-4xl md:text-5xl lg:text-6xl font-bold mb-8 leading-tight animate-slide-in-left">
                  Giày thể thao – Không chỉ là một đôi giày, 
                  <span class="block mt-2 text-transparent bg-clip-text bg-gradient-to-r from-yellow-300 to-pink-300">
                    mà là phong cách sống
                  </span>
                </h2>
                <p class="text-lg md:text-xl lg:text-2xl leading-relaxed mb-8 max-w-4xl mx-auto text-gray-100 animate-slide-in-right">
                  Trong thế giới hiện đại, giày thể thao không chỉ đơn thuần là phụ kiện thời trang hay hỗ trợ vận động mà còn thể hiện cá tính và phong cách sống năng động. Từ những đôi sneaker đậm chất đường phố đến giày chạy bộ chuyên dụng, mỗi sản phẩm đều mang trong mình sự đổi mới về công nghệ, thiết kế và chất lượng.
                </p>
                <div class="flex flex-wrap justify-center gap-4 animate-fade-in">
                  <div class="bg-white/20 backdrop-blur-sm px-6 py-3 rounded-full border border-white/30">
                    <i class="fas fa-award mr-2"></i> Chất lượng cao
                  </div>
                  <div class="bg-white/20 backdrop-blur-sm px-6 py-3 rounded-full border border-white/30">
                    <i class="fas fa-leaf mr-2"></i> Bền vững
                  </div>
                  <div class="bg-white/20 backdrop-blur-sm px-6 py-3 rounded-full border border-white/30">
                    <i class="fas fa-bolt mr-2"></i> Hiệu năng vượt trội
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Sản phẩm mới với card 3D effect -->
          <div class="mb-24 animate-fade-in-up animation-delay-200">
            <div class="bg-white rounded-3xl shadow-xl overflow-hidden transform hover:shadow-2xl transition-all duration-500 hover:-translate-y-2">
              <div class="grid lg:grid-cols-2 gap-0">
                <div class="p-12 lg:p-16 flex flex-col justify-center">
                  <div class="inline-flex items-center bg-gradient-to-r from-green-400 to-blue-500 text-white px-4 py-2 rounded-full text-sm font-semibold mb-6 self-start">
                    <i class="fas fa-star mr-2"></i> Sản phẩm mới
                  </div>
                  <h2 class="text-4xl md:text-5xl font-bold text-gray-900 mb-6 leading-tight">
                    Bứt phá mọi 
                    <span class="text-transparent bg-clip-text bg-gradient-to-r from-green-500 to-blue-600">
                      giới hạn
                    </span>
                  </h2>
                  <p class="text-lg text-gray-600 mb-8 leading-relaxed">
                    Ra mắt với thiết kế tối ưu cho hiệu suất, <span class="font-bold text-gray-900">[Tên sản phẩm]</span> là sự lựa chọn hoàn hảo cho những ai yêu thích vận động và thời trang.
                  </p>
                  <div class="space-y-4 mb-8">
                    <div class="flex items-center">
                      <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center mr-4">
                        <i class="fas fa-wind text-blue-600"></i>
                      </div>
                      <div>
                        <h4 class="font-semibold text-gray-900">Lưới thoáng khí</h4>
                        <p class="text-gray-600 text-sm">Giữ cho đôi chân luôn khô thoáng</p>
                      </div>
                    </div>
                    <div class="flex items-center">
                      <div class="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center mr-4">
                        <i class="fas fa-cloud text-purple-600"></i>
                      </div>
                      <div>
                        <h4 class="font-semibold text-gray-900">Đế êm ái</h4>
                        <p class="text-gray-600 text-sm">Cảm giác nhẹ nhàng trên từng bước chân</p>
                      </div>
                    </div>
                  </div>
                  <button class="bg-gradient-to-r from-blue-600 to-purple-600 text-white px-8 py-4 rounded-full font-semibold hover:shadow-lg transform hover:scale-105 transition-all duration-300 self-start">
                    Khám phá ngay <i class="fas fa-arrow-right ml-2"></i>
                  </button>
                </div>
                <div class="relative h-96 lg:h-auto bg-gradient-to-br from-blue-100 to-purple-100 flex items-center justify-center">
                  <div class="absolute inset-0 bg-gradient-to-br from-blue-400/20 to-purple-400/20"></div>
                  <img src="" alt="Sleek modern running shoe" class="relative z-10 w-4/5 h-4/5 object-contain transform hover:rotate-6 transition-transform duration-500" />
                  <div class="absolute bottom-8 right-8 bg-white/90 backdrop-blur-sm px-6 py-3 rounded-full shadow-lg">
                    <span class="text-2xl font-bold text-gray-900">NEW</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Brand section với glassmorphism -->
          <div class="mb-24 animate-fade-in-up animation-delay-400">
            <div class="relative bg-gradient-to-r from-gray-900 to-gray-800 rounded-3xl p-12 md:p-16 text-center overflow-hidden">
              <div class="absolute inset-0 bg-gradient-to-r from-blue-600/20 to-purple-600/20"></div>
              <div class="absolute -top-24 -right-24 w-96 h-96 bg-blue-500 rounded-full opacity-10 blur-3xl"></div>
              <div class="absolute -bottom-24 -left-24 w-96 h-96 bg-purple-500 rounded-full opacity-10 blur-3xl"></div>
              
              <div class="relative z-10">
                <div class="w-32 h-32 bg-white/10 backdrop-blur-md rounded-full mx-auto mb-8 flex items-center justify-center border border-white/20">
                  <i class="fas fa-crown text-5xl text-yellow-400"></i>
                </div>
                <h2 class="text-4xl md:text-5xl font-bold text-white mb-6">
                  Khám phá sự khác biệt cùng 
                  <span class="text-transparent bg-clip-text bg-gradient-to-r from-yellow-400 to-pink-400">
                    [Tên thương hiệu]
                  </span>
                </h2>
                <p class="text-xl text-gray-300 max-w-3xl mx-auto leading-relaxed">
                  Được thành lập từ năm <span class="font-bold text-white">[năm thành lập]</span>, chúng tôi luôn đặt mục tiêu tạo ra những đôi giày thể thao chất lượng, bền bỉ và dẫn đầu xu hướng.
                </p>
                <div class="grid grid-cols-3 gap-8 mt-12 max-w-2xl mx-auto">
                  <div class="text-center">
                    <div class="text-4xl font-bold text-yellow-400 mb-2">10K+</div>
                    <div class="text-gray-400">Khách hàng</div>
                  </div>
                  <div class="text-center">
                    <div class="text-4xl font-bold text-yellow-400 mb-2">50+</div>
                    <div class="text-gray-400">Sản phẩm</div>
                  </div>
                  <div class="text-center">
                    <div class="text-4xl font-bold text-yellow-400 mb-2">5★</div>
                    <div class="text-gray-400">Đánh giá</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Technology Grid với hover effects -->
          <div class="mb-24 animate-fade-in-up animation-delay-600">
            <div class="text-center mb-12">
              <h2 class="text-4xl md:text-5xl font-bold text-gray-900 mb-4">
                Công nghệ 
                <span class="text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-purple-600">
                  đột phá
                </span>
              </h2>
              <p class="text-xl text-gray-600">Mỗi bước chân là một trải nghiệm</p>
            </div>
            
            <div class="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
              <!-- Tech Card 1 -->
              <div class="group relative bg-white rounded-2xl p-8 shadow-lg hover:shadow-2xl transition-all duration-500 hover:-translate-y-2 overflow-hidden">
                <div class="absolute top-0 right-0 w-32 h-32 bg-blue-500 rounded-full opacity-5 -mr-16 -mt-16 group-hover:scale-150 transition-transform duration-500"></div>
                <div class="relative z-10">
                  <div class="w-16 h-16 bg-gradient-to-br from-blue-400 to-blue-600 rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
                    <i class="fas fa-tint text-white text-2xl"></i>
                  </div>
                  <h3 class="text-2xl font-bold text-gray-900 mb-3">Chống thấm nước</h3>
                  <p class="text-gray-600 leading-relaxed">Công nghệ màng chống thấm tiên tiến, giữ chân khô ráo trong mọi điều kiện thời tiết.</p>
                </div>
              </div>

              <!-- Tech Card 2 -->
              <div class="group relative bg-white rounded-2xl p-8 shadow-lg hover:shadow-2xl transition-all duration-500 hover:-translate-y-2 overflow-hidden">
                <div class="absolute top-0 right-0 w-32 h-32 bg-purple-500 rounded-full opacity-5 -mr-16 -mt-16 group-hover:scale-150 transition-transform duration-500"></div>
                <div class="relative z-10">
                  <div class="w-16 h-16 bg-gradient-to-br from-purple-400 to-purple-600 rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
                    <i class="fas fa-cloud text-white text-2xl"></i>
                  </div>
                  <h3 class="text-2xl font-bold text-gray-900 mb-3">Memory Foam</h3>
                  <p class="text-gray-600 leading-relaxed">Đế êm ái với công nghệ Memory Foam, ôm sát và nâng đỡ hoàn hảo cho bàn chân.</p>
                </div>
              </div>

              <!-- Tech Card 3 -->
              <div class="group relative bg-white rounded-2xl p-8 shadow-lg hover:shadow-2xl transition-all duration-500 hover:-translate-y-2 overflow-hidden">
                <div class="absolute top-0 right-0 w-32 h-32 bg-red-500 rounded-full opacity-5 -mr-16 -mt-16 group-hover:scale-150 transition-transform duration-500"></div>
                <div class="relative z-10">
                  <div class="w-16 h-16 bg-gradient-to-br from-red-400 to-red-600 rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
                    <i class="fas fa-shield-alt text-white text-2xl"></i>
                  </div>
                  <h3 class="text-2xl font-bold text-gray-900 mb-3">Chống sốc vượt trội</h3>
                  <p class="text-gray-600 leading-relaxed">Hệ thống giảm chấn đa lớp, bảo vệ khớp và cột sống khi vận động mạnh.</p>
                </div>
              </div>

              <!-- Tech Card 4 -->
              <div class="group relative bg-white rounded-2xl p-8 shadow-lg hover:shadow-2xl transition-all duration-500 hover:-translate-y-2 overflow-hidden">
                <div class="absolute top-0 right-0 w-32 h-32 bg-yellow-500 rounded-full opacity-5 -mr-16 -mt-16 group-hover:scale-150 transition-transform duration-500"></div>
                <div class="relative z-10">
                  <div class="w-16 h-16 bg-gradient-to-br from-yellow-400 to-yellow-600 rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
                    <i class="fas fa-wind text-white text-2xl"></i>
                  </div>
                  <h3 class="text-2xl font-bold text-gray-900 mb-3">Lưới thoáng khí</h3>
                  <p class="text-gray-600 leading-relaxed">Vật liệu lưới 3D thông minh, tối ưu luồng không khí và thoát mồ hôi.</p>
                </div>
              </div>

              <!-- Tech Card 5 -->
              <div class="group relative bg-white rounded-2xl p-8 shadow-lg hover:shadow-2xl transition-all duration-500 hover:-translate-y-2 overflow-hidden">
                <div class="absolute top-0 right-0 w-32 h-32 bg-teal-500 rounded-full opacity-5 -mr-16 -mt-16 group-hover:scale-150 transition-transform duration-500"></div>
                <div class="relative z-10">
                  <div class="w-16 h-16 bg-gradient-to-br from-teal-400 to-teal-600 rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
                    <i class="fas fa-bolt text-white text-2xl"></i>
                  </div>
                  <h3 class="text-2xl font-bold text-gray-900 mb-3">Đệm khí tiên tiến</h3>
                  <p class="text-gray-600 leading-relaxed">Công nghệ Air Cushion độc quyền, tăng cường độ nảy và phản hồi năng lượng.</p>
                </div>
              </div>

              <!-- Tech Card 6 -->
              <div class="group relative bg-white rounded-2xl p-8 shadow-lg hover:shadow-2xl transition-all duration-500 hover:-translate-y-2 overflow-hidden">
                <div class="absolute top-0 right-0 w-32 h-32 bg-orange-500 rounded-full opacity-5 -mr-16 -mt-16 group-hover:scale-150 transition-transform duration-500"></div>
                <div class="relative z-10">
                  <div class="w-16 h-16 bg-gradient-to-br from-orange-400 to-orange-600 rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
                    <i class="fas fa-microchip text-white text-2xl"></i>
                  </div>
                  <h3 class="text-2xl font-bold text-gray-900 mb-3">Smart Technology</h3>
                  <p class="text-gray-600 leading-relaxed">Tích hợp chip thông minh theo dõi hoạt động và tối ưu hiệu suất.</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Emotional Section với split design -->
          <div class="mb-24 animate-fade-in-up animation-delay-800">
            <div class="bg-gradient-to-r from-yellow-50 to-orange-50 rounded-3xl overflow-hidden shadow-xl">
              <div class="grid lg:grid-cols-2 gap-0">
                <div class="relative h-96 lg:h-auto">
                  <div class="absolute inset-0 bg-gradient-to-br from-yellow-400/20 to-orange-400/20"></div>
                  <img src="" alt="Person running on a scenic trail" class="w-full h-full object-cover" />
                  <div class="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent"></div>
                  <div class="absolute bottom-8 left-8 text-white">
                    <h3 class="text-3xl font-bold mb-2">Hành trình của bạn</h3>
                    <p class="text-lg">Bắt đầu từ đây</p>
                  </div>
                </div>
                <div class="p-12 lg:p-16 flex flex-col justify-center">
                  <div class="inline-flex items-center bg-gradient-to-r from-yellow-400 to-orange-400 text-white px-4 py-2 rounded-full text-sm font-semibold mb-6 self-start">
                    <i class="fas fa-heart mr-2"></i> Câu chuyện của bạn
                  </div>
                  <h2 class="text-4xl font-bold text-gray-900 mb-6 leading-tight">
                    Người bạn đồng hành trên mọi 
                    <span class="text-transparent bg-clip-text bg-gradient-to-r from-yellow-500 to-orange-500">
                      chặng đường
                    </span>
                  </h2>
                  <blockquote class="border-l-4 border-yellow-500 pl-6 mb-8">
                    <p class="text-lg text-gray-700 italic leading-relaxed mb-4">
                      "Bạn còn nhớ đôi giày đầu tiên bạn mang khi chạy bộ? Hay đôi sneaker khiến bạn nổi bật giữa đám đông? Giày thể thao không chỉ giúp bạn di chuyển, mà còn lưu giữ từng khoảnh khắc, từng câu chuyện của cuộc sống."
                    </p>
                  </blockquote>
                  <p class="text-lg text-gray-600 leading-relaxed">
                    Mỗi vết trầy, mỗi lần mòn đế... là minh chứng cho hành trình mà bạn đã đi qua. Đừng chỉ chọn giày – hãy chọn một người bạn đồng hành đáng tin cậy.
                  </p>
                  <div class="flex gap-4 mt-8">
                    <div class="flex items-center">
                      <i class="fas fa-route text-yellow-500 text-2xl mr-3"></i>
                      <div>
                        <div class="font-bold text-gray-900">1000+ KM</div>
                        <div class="text-sm text-gray-600">Đồng hành cùng bạn</div>
                      </div>
                    </div>
                    <div class="flex items-center">
                      <i class="fas fa-smile text-orange-500 text-2xl mr-3"></i>
                      <div>
                        <div class="font-bold text-gray-900">100%</div>
                        <div class="text-sm text-gray-600">Hài lòng</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Team Section với modern cards -->
          <div class="text-center animate-fade-in-up animation-delay-1000">
            <div class="mb-16">
              <h2 class="text-4xl md:text-5xl font-bold text-gray-900 mb-4">
                Đội ngũ 
                <span class="text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-purple-600">
                  sáng tạo
                </span>
              </h2>
              <p class="text-xl text-gray-600">Những người đứng sau thành công của chúng tôi</p>
            </div>
            
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-8">
              <!-- Team Member 1 -->
              <div class="group relative">
                <div class="relative overflow-hidden rounded-2xl bg-gradient-to-br from-blue-400 to-blue-600 p-1">
                  <div class="bg-white rounded-2xl p-6">
                    <div class="relative w-32 h-32 mx-auto mb-4 overflow-hidden rounded-full">
                      <img src="" alt="" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
                      <div class="absolute inset-0 bg-gradient-to-t from-red-600/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                    </div>
                    <h3 class="text-xl font-bold text-gray-900 mb-1">Phạm Văn Thăng</h3>
                    <p class="text-sm text-gray-600 mb-3">Developer</p>
                    <div class="flex justify-center gap-3">
                      <a href="#" class="text-gray-400 hover:text-red-600 transition-colors">
                        <i class="fab fa-linkedin"></i>
                      </a>
                      <a href="#" class="text-gray-400 hover:text-red-600 transition-colors">
                        <i class="fab fa-github"></i>
                      </a>
                    </div>
                  </div>
                </div>
              </div>
<div class="group relative">
                <div class="relative overflow-hidden rounded-2xl bg-gradient-to-br from-blue-400 to-blue-600 p-1">
                  <div class="bg-white rounded-2xl p-6">
                    <div class="relative w-32 h-32 mx-auto mb-4 overflow-hidden rounded-full">
                      <img src="" alt="" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
                      <div class="absolute inset-0 bg-gradient-to-t from-red-600/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                    </div>
                    <h3 class="text-xl font-bold text-gray-900 mb-1">Nguyễn Thị Diệu Linh</h3>
                    <p class="text-sm text-gray-600 mb-3">Developer</p>
                    <div class="flex justify-center gap-3">
                      <a href="#" class="text-gray-400 hover:text-red-600 transition-colors">
                        <i class="fab fa-linkedin"></i>
                      </a>
                      <a href="#" class="text-gray-400 hover:text-red-600 transition-colors">
                        <i class="fab fa-github"></i>
                      </a>
                    </div>
                  </div>
                </div>
              </div>
              <div class="group relative">
                <div class="relative overflow-hidden rounded-2xl bg-gradient-to-br from-blue-400 to-blue-600 p-1">
                  <div class="bg-white rounded-2xl p-6">
                    <div class="relative w-32 h-32 mx-auto mb-4 overflow-hidden rounded-full">
                      <img src="" alt="" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
                      <div class="absolute inset-0 bg-gradient-to-t from-red-600/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                    </div>
                    <h3 class="text-xl font-bold text-gray-900 mb-1">Lê Anh Tú</h3>
                    <p class="text-sm text-gray-600 mb-3">Developer</p>
                    <div class="flex justify-center gap-3">
                      <a href="#" class="text-gray-400 hover:text-red-600 transition-colors">
                        <i class="fab fa-linkedin"></i>
                      </a>
                      <a href="#" class="text-gray-400 hover:text-red-600 transition-colors">
                        <i class="fab fa-github"></i>
                      </a>
                    </div>
                  </div>
                </div>
              </div>
              <div class="group relative">
                <div class="relative overflow-hidden rounded-2xl bg-gradient-to-br from-blue-400 to-blue-600 p-1">
                  <div class="bg-white rounded-2xl p-6">
                    <div class="relative w-32 h-32 mx-auto mb-4 overflow-hidden rounded-full">
                      <img src="" alt="" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
                      <div class="absolute inset-0 bg-gradient-to-t from-red-600/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                    </div>
                    <h3 class="text-xl font-bold text-gray-900 mb-1">Nguyễn Đình Đức Lương</h3>
                    <p class="text-sm text-gray-600 mb-3">Developer</p>
                    <div class="flex justify-center gap-3">
                      <a href="#" class="text-gray-400 hover:text-red-600 transition-colors">
                        <i class="fab fa-linkedin"></i>
                      </a>
                      <a href="#" class="text-gray-400 hover:text-red-600 transition-colors">
                        <i class="fab fa-github"></i>
                      </a>
                    </div>
                  </div>
                </div>
              </div>
              <!-- Team Member 5 -->
              <div class="group relative">
                <div class="relative overflow-hidden rounded-2xl bg-gradient-to-br from-yellow-400 to-yellow-600 p-1">
                  <div class="bg-white rounded-2xl p-6">
                    <div class="relative w-32 h-32 mx-auto mb-4 overflow-hidden rounded-full">
                      <img src="" alt="Phùng Trọng Anh Quân" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
                      <div class="absolute inset-0 bg-gradient-to-t from-yellow-600/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                    </div>
                    <h3 class="text-xl font-bold text-gray-900 mb-1">Phùng Trọng Anh Quân</h3>
                    <p class="text-sm text-gray-600 mb-3">Analyst</p>
                    <div class="flex justify-center gap-3">
                      <a href="#" class="text-gray-400 hover:text-yellow-600 transition-colors">
                        <i class="fab fa-linkedin"></i>
                      </a>
                      <a href="#" class="text-gray-400 hover:text-yellow-600 transition-colors">
                        <i class="fab fa-github"></i>
                      </a>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
    <section id="footer-section" class="padding-x padding-t bg-black pb-8">
      <Footer />
    </section>
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
  name: 'Gioithieu',
  components: {
    Nav,
    Footer,
    Hero,
    ChatBot,
  },
};
</script>

<style scoped>
/* Font imports must come first */
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap');
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');

.gioithieu {
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
    transform: translateY(-30px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fade-in-up {
  0% {
    opacity: 0;
    transform: translateY(30px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slide-in-left {
  0% {
    opacity: 0;
    transform: translateX(-50px);
  }
  100% {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slide-in-right {
  0% {
    opacity: 0;
    transform: translateX(50px);
  }
  100% {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes fade-in {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}

.animate-blob {
  animation: blob 7s infinite;
}

.animation-delay-2000 {
  animation-delay: 2s;
}

.animation-delay-4000 {
  animation-delay: 4s;
}

.animate-fade-in-down {
  animation: fade-in-down 1s ease-out;
}

.animate-fade-in-up {
  animation: fade-in-up 1s ease-out;
}

.animate-slide-in-left {
  animation: slide-in-left 1s ease-out;
}

.animate-slide-in-right {
  animation: slide-in-right 1s ease-out;
}

.animate-fade-in {
  animation: fade-in 1s ease-out;
}

.animation-delay-200 {
  animation-delay: 0.2s;
  animation-fill-mode: both;
}

.animation-delay-400 {
  animation-delay: 0.4s;
  animation-fill-mode: both;
}

.animation-delay-600 {
  animation-delay: 0.6s;
  animation-fill-mode: both;
}

.animation-delay-800 {
  animation-delay: 0.8s;
  animation-fill-mode: both;
}

.animation-delay-1000 {
  animation-delay: 1s;
  animation-fill-mode: both;
}

/* Custom gradient text support */
.text-transparent {
  color: transparent;
}

.bg-clip-text {
  -webkit-background-clip: text;
  background-clip: text;
}

/* Padding utilities */
.xl\:padding-l {
  padding-left: 2rem;
}

@media (min-width: 1280px) {
  .xl\:padding-l {
    padding-left: 3rem;
  }
}

.wide\:padding-r {
  padding-right: 3rem;
}

@media (min-width: 1536px) {
  .wide\:padding-r {
    padding-right: 5rem;
  }
}

.padding-b {
  padding-bottom: 3rem;
}

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

/* Hover effect enhancements */
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
</style>