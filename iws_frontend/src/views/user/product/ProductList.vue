<script>
import ChatBot from '@/components/ChatBotAndReview/ChatBot.vue';
import Nav from '@/components/user/Nav.vue';
import ScrollToggler from '@/components/user/ScrollToggler.vue';
import Footer from '@/views/user/Footer.vue';
import axios from 'axios';
import Hero from '../Hero.vue';

export default {
    name: 'ProductList',

    components: {
        Nav,
        Footer,
        Hero,
        ScrollToggler,
        ChatBot
    },

    data() {
        return {
            products: [],
            categories: [],
            selectedCategory: null,
            cartItems: 0,
            loading: true,
            loadingCategories: true,
            isMobileMenuOpen: false
        };
    },

    computed: {
        filteredProducts() {
            if (!this.selectedCategory) {
                return this.products;
            }
            return this.products.filter((product) => product.categoryId === this.selectedCategory);
        },

        selectedCategoryName() {
            if (!this.selectedCategory) {
                return 'Phổ Biến';
            }
            const category = this.categories.find((cat) => cat.id === this.selectedCategory);
            return category ? category.tenDanhMuc : 'Phổ Biến';
        },

        totalProducts() {
            return this.products.length;
        }
    },

    methods: {
        selectCategory(categoryId) {
            this.selectedCategory = categoryId;
            this.isMobileMenuOpen = false;
            window.scrollTo({ top: 0, behavior: 'smooth' });
        },

        scrollToProducts() {
            if (this.$refs.productsSection) {
                this.$refs.productsSection.scrollIntoView({ behavior: 'smooth' });
            }
        },

        goToProductDetail(product) {
            this.$router.push({
                name: 'product',
                params: { id: product.firstDetailId || product.id }
            });
        },

        goToProductDetail(product) {
            console.log('Navigating to product detail:', product);
            
            // Chuyển đến trang chi tiết sản phẩm
            if (product.firstDetailId) {
                this.$router.push(`/product/${product.firstDetailId}`);
            } else {
                console.warn('No firstDetailId found for product:', product);
                this.$toast?.error('Không thể xem chi tiết sản phẩm này') || alert('Không thể xem chi tiết sản phẩm này');
            }
        },

        // Method để lấy URL hình ảnh với fallback chain - ĐÃ SỬA
        getImageUrl(product) {
            // Kiểm tra nếu có imgUrl trực tiếp và hợp lệ
            if (product.imgUrl && product.imgUrl.trim() !== '' && product.imgUrl !== 'null' && product.imgUrl !== 'undefined' && !product.imgUrl.includes('null')) {
                return product.imgUrl;
            }

            // Fallback sang SVG placeholder ngay
            return 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjQwIiBoZWlnaHQ9IjE2MCIgdmlld0Jvg9IjAiMCIyNDAgMTYwIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxyZWN0IHdpZHRoPSIyNDAiIGhlaWdodD0iMTYwIiBmaWxsPSIjZjNmNGY2Ii8+PHRleHQgeD0iMTIwIiB5PSI4MCIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZmlsbD0iIzZiNzI4MCIgZm9udC1zaXplPSIxNCI+U2hvZSBJbWFnZTwvdGV4dD48L3N2Zz4=';
        },

        // Xử lý khi load hình ảnh thành công - SIMPLIFIED
        handleImageLoad(event) {
            console.log('Image loaded successfully:', event.target.src);
        },

        // Xử lý khi load hình ảnh thất bại - KHÔNG TỰ ĐỘNG RETRY
        handleImageError(event) {
            console.log('Image load failed for:', event.target.src);

            // Chỉ set placeholder SVG nếu chưa phải SVG
            if (!event.target.src.startsWith('data:image/svg+xml')) {
                console.log('Setting SVG placeholder');
                event.target.src =
                    'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjQwIiBoZWlnaHQ9IjE2MCIgdmlld0Jvg9IjAiMCIyNDAgMTYwIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxyZWN0IHdpZHRoPSIyNDAiIGhlaWdodD0iMTYwIiBmaWxsPSIjZjNmNGY2Ii8+PHRleHQgeD0iMTIwIiB5PSI4MCIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZmlsbD0iIzZiNzI4MCIgZm9udC1zaXplPSIxNCI+U2hvZSBJbWFnZTwvdGV4dD48L3N2Zz4=';
            }
        },

        // Test API connectivity - THÊM MỚI
        async testAPIConnectivity() {
            try {
                console.log('Testing API connectivity...');

                // Test hinh-anh endpoint
                const imageResponse = await axios.get('http://localhost:8080/hinh-anh');
                console.log('Images API success:', imageResponse.data.length, 'images found');

                // Test nếu có image nào
                if (imageResponse.data.length > 0) {
                    const firstImage = imageResponse.data[0];
                    console.log('First image:', firstImage);

                    // Test truy cập trực tiếp
                    if (firstImage.fullUrl) {
                        console.log('Testing direct image access:', firstImage.fullUrl);
                        const testImg = new Image();
                        testImg.onload = () => console.log('✅ Image loads successfully');
                        testImg.onerror = () => console.log('❌ Image access failed');
                        testImg.src = firstImage.fullUrl;
                    }
                }
            } catch (error) {
                console.error('API connectivity test failed:', error);
                if (error.response) {
                    console.error('Response status:', error.response.status);
                    console.error('Response data:', error.response.data);
                }
            }
        },

        formatPrice(price) {
            if (!price) return '0';
            return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.');
        },

        async fetchCategories() {
            try {
                this.loadingCategories = true;
                const response = await axios.get('http://localhost:8080/danh-muc');
                this.categories = response.data.filter((cat) => cat.trangThai === 1);
                console.log('Categories loaded:', this.categories.length);
            } catch (error) {
                console.error('Lỗi khi gọi API danh mục:', error);
                this.categories = [];
            } finally {
                this.loadingCategories = false;
            }
        },

        // PHẦN FETCHPRODUCTS ĐÃ SỬA HOÀN TOÀN
        async fetchProducts() {
            try {
                this.loading = true;

                // Lấy danh sách hình ảnh từ API - THÊM MỚI
                const [productsResponse, detailsResponse, imagesResponse] = await Promise.all([axios.get('http://localhost:8080/api/san-pham'), axios.get('http://localhost:8080/api/san-pham-chi-tiet'), axios.get('http://localhost:8080/hinh-anh')]);

                console.log('Products API Response:', productsResponse.data.length, 'products');
                console.log('Details API Response:', detailsResponse.data.length, 'details');
                console.log('Images API Response:', imagesResponse.data.length, 'images');

                if (!productsResponse.data || productsResponse.data.length === 0) {
                    console.warn('No products data received from API');
                    this.products = [];
                    return;
                }

                // Tạo map hình ảnh theo ID - THÊM MỚI
                const imageMap = new Map();
                imagesResponse.data.forEach((image) => {
                    imageMap.set(image.id, image.fullUrl || `http://localhost:8080${image.duongDan}`);
                });

                const firstDetailMap = new Map();
                const priceMap = new Map();
                const productImageMap = new Map();

                // Xử lý từng detail để debug và extract thông tin
                detailsResponse.data.forEach((detail, index) => {
                    if (detail.sanPham?.id) {
                        const productId = detail.sanPham.id;

                        // Map first detail ID
                        if (!firstDetailMap.has(productId)) {
                            firstDetailMap.set(productId, detail.id);
                        }

                        // Map price info
                        if (detail.giaBan && (!priceMap.has(productId) || detail.giaBan < priceMap.get(productId).giaBan)) {
                            priceMap.set(productId, {
                                giaBan: detail.giaBan,
                                giaGoc: detail.giaGoc
                            });
                        }

                        // XỬ LÝ HÌNH ẢNH THEO ENTITY MỚI - SỬA ĐỔI HOÀN TOÀN
                        if (!productImageMap.has(productId) && detail.hinhAnh) {
                            console.log(`Processing image for detail ${detail.id}:`, detail.hinhAnh);

                            let finalImageUrl = null;

                            if (typeof detail.hinhAnh === 'object' && detail.hinhAnh !== null) {
                                // Trường hợp API trả về object với id
                                if (detail.hinhAnh.id) {
                                    finalImageUrl = imageMap.get(detail.hinhAnh.id);
                                    console.log(`Found image by ID ${detail.hinhAnh.id}:`, finalImageUrl);
                                }
                                // Trường hợp API trả về object đầy đủ
                                else if (detail.hinhAnh.fullUrl) {
                                    finalImageUrl = detail.hinhAnh.fullUrl;
                                    console.log(`Using fullUrl:`, finalImageUrl);
                                } else if (detail.hinhAnh.duongDan) {
                                    const duongDan = detail.hinhAnh.duongDan;
                                    if (duongDan.startsWith('http')) {
                                        finalImageUrl = duongDan;
                                    } else if (duongDan.startsWith('/hinh-anh/')) {
                                        finalImageUrl = 'http://localhost:8080' + duongDan;
                                    } else {
                                        finalImageUrl = 'http://localhost:8080/hinh-anh/images/' + duongDan;
                                    }
                                    console.log(`Built URL from duongDan:`, finalImageUrl);
                                }
                            } else if (typeof detail.hinhAnh === 'number') {
                                // Trường hợp API trả về ID number trực tiếp
                                finalImageUrl = imageMap.get(detail.hinhAnh);
                                console.log(`Found image by number ID ${detail.hinhAnh}:`, finalImageUrl);
                            }

                            if (finalImageUrl) {
                                productImageMap.set(productId, finalImageUrl);
                                console.log(`Final image URL for product ${productId}:`, finalImageUrl);
                            } else {
                                console.log(`No valid image URL found for detail ${detail.id}`);
                            }
                        }
                    }
                });

                // Map sản phẩm với thông tin từ chi tiết
                this.products = productsResponse.data.map((p) => {
                    const priceInfo = priceMap.get(p.id) || { giaBan: 0, giaGoc: 0 };
                    const imageUrl = productImageMap.get(p.id);
                    const firstDetailId = firstDetailMap.get(p.id);

                    const product = {
                        id: p.id,
                        firstDetailId: firstDetailId,
                        imgUrl: imageUrl,
                        label: p.tenSanPham || 'Sản phẩm không tên',
                        price: priceInfo.giaBan,
                        originalPrice: priceInfo.giaGoc,
                        rating: 4.5 + Math.random() * 0.5,
                        brandId: p.thuongHieu?.id,
                        brandName: p.thuongHieu?.tenThuongHieu || '',
                        categoryId: p.danhMuc?.id,
                        categoryName: p.danhMuc?.tenDanhMuc || '',
                        materialId: p.chatLieu?.id,
                        materialName: p.chatLieu?.tenChatLieu || '',
                        soleId: p.deGiay?.id,
                        soleName: p.deGiay?.tenDeGiay || '',
                        maSanPham: p.maSanPham,
                        soLuong: p.soLuong || 0,
                        trangThai: p.trangThai
                    };

                    return product;
                });

                console.log('Total processed products:', this.products.length);
                console.log('Products with prices:', this.products.filter((p) => p.price > 0).length);
                console.log('Products with images:', this.products.filter((p) => p.imgUrl).length);

                // Log products có hình ảnh để debug
                const productsWithImages = this.products.filter((p) => p.imgUrl);
                console.log(
                    'Products with images:',
                    productsWithImages.map((p) => ({
                        id: p.id,
                        name: p.label,
                        imgUrl: p.imgUrl
                    }))
                );
            } catch (error) {
                console.error('Error fetching products:', error);
                console.error('Error details:', {
                    message: error.message,
                    response: error.response,
                    request: error.request
                });
                this.products = [];
            } finally {
                this.loading = false;
            }
        },

        getRandomProducts(excludeProductId = null, count = 4) {
            let availableProducts = this.products;

            if (excludeProductId) {
                availableProducts = this.products.filter((p) => p.id !== excludeProductId);
            }

            const shuffled = availableProducts.sort(() => 0.5 - Math.random());
            return shuffled.slice(0, count);
        }
    },

    mounted() {
        console.log('ProductList component mounted');

        // Test API connectivity - THÊM MỚI
        this.testAPIConnectivity();

        this.fetchCategories();
        this.fetchProducts();
    },

    beforeUnmount() {
        console.log('ProductList component unmounting');
    },

    expose() {
        return {
            getRandomProducts: this.getRandomProducts,
            products: this.products
        };
    }
};
</script>
<template>
    <div class="nike-complete-layout">
        <!-- Navigation Component -->
        <Nav />
        <section id="home" class="xl:padding-l wide:padding-r padding-b overflow-hidden">
            <Hero />
        </section>
        <!-- Main Content -->
        <main class="nike-layout">
            <div class="main-container">
                <!-- Sidebar Categories -->
                <aside class="categories-sidebar" :class="{ 'mobile-open': isMobileMenuOpen }">
                    <div class="sidebar-header">
                        <h3 class="sidebar-title">
                            <svg class="category-icon" viewBox="0 0 24 24" fill="currentColor">
                                <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z" />
                            </svg>
                            Danh Mục
                        </h3>
                        <button class="mobile-close-btn" @click="isMobileMenuOpen = false">
                            <svg viewBox="0 0 24 24" fill="currentColor">
                                <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z" />
                            </svg>
                        </button>
                    </div>

                    <div class="categories-list">
                        <div v-if="loadingCategories" class="category-skeleton" v-for="n in 5" :key="n">
                            <div class="skeleton-text"></div>
                        </div>

                        <div v-else>
                            <div class="category-item" :class="{ active: selectedCategory === null }" @click="selectCategory(null)">
                                <span class="category-name">Tất cả sản phẩm</span>
                                <span class="category-count">{{ totalProducts }}</span>
                            </div>

                            <div v-for="category in categories" :key="category.id" class="category-item" :class="{ active: selectedCategory === category.id }" @click="selectCategory(category.id)">
                                <span class="category-name">{{ category.tenDanhMuc }}</span>
                                <span class="category-arrow">
                                    <svg viewBox="0 0 24 24" fill="currentColor">
                                        <path d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6-1.41-1.41z" />
                                    </svg>
                                </span>
                            </div>
                        </div>
                    </div>

                    <div class="sidebar-footer">
                        <div class="filter-section">
                            <h4 class="filter-title">Bộ lọc nâng cao</h4>
                            <button class="filter-btn">
                                <svg viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M10 18h4v-2h-4v2zM3 6v2h18V6H3zm3 7h12v-2H6v2z" />
                                </svg>
                                <span>Lọc sản phẩm</span>
                            </button>
                        </div>
                    </div>
                </aside>

                <!-- Mobile Menu Toggle -->
                <button class="mobile-menu-toggle" @click="isMobileMenuOpen = true">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                        <path d="M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z" />
                    </svg>
                    <span>Danh mục</span>
                </button>

                <!-- Products Section -->
                <div class="products-container">
                    <div class="section-header">
                        <div class="section-badge">Sản phẩm nổi bật</div>
                        <h2 class="section-title">
                            <span class="highlight-gradient">Sản Phẩm</span>
                            {{ selectedCategoryName }}
                        </h2>
                        <p class="section-description">
                            {{ selectedCategory ? `Khám phá bộ sưu tập ${selectedCategoryName.toLowerCase()} chất lượng cao` : 'Trải nghiệm chất lượng và phong cách hàng đầu với các lựa chọn được tìm kiếm nhiều nhất' }}
                        </p>
                    </div>

                    <!-- Loading State -->
                    <div v-if="loading" class="products-grid">
                        <div v-for="n in 8" :key="n" class="product-card-skeleton">
                            <div class="skeleton-image"></div>
                            <div class="skeleton-content">
                                <div class="skeleton-line skeleton-rating"></div>
                                <div class="skeleton-line skeleton-title"></div>
                                <div class="skeleton-line skeleton-price"></div>
                                <div class="skeleton-button"></div>
                            </div>
                        </div>
                    </div>

                    <!-- Products Grid -->
                    <div v-else class="products-grid">
                        <div v-for="(product, index) in filteredProducts" :key="product.id" @click="goToProductDetail(product)" class="product-card" :style="{ animationDelay: `${index * 0.1}s` }">
                            <!-- Product Image Container - SIMPLIFIED -->
                            <div class="product-image-container">
                                <!-- LUÔN hiển thị hình ảnh với fallback -->
                                <div class="product-image-wrapper">
                                    <img :src="getImageUrl(product)" :alt="product.label" class="product-image" @error="handleImageError" @load="handleImageLoad" loading="lazy" />
                                </div>

                                <!-- Product Badge -->
                                <div class="product-badge">
                                    <span>Mới</span>
                                </div>

                                <!-- Hover Overlay -->
                                <div class="product-overlay">
                                    <div class="quick-view-btn">
                                        <svg viewBox="0 0 24 24" fill="currentColor">
                                            <path
                                                d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"
                                            />
                                        </svg>
                                    </div>
                                </div>
                            </div>

                            <!-- Product Info -->
                            <div class="product-info">
                                <!-- Rating -->
                                <div class="product-rating">
                                    <div class="stars">
                                        <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= Math.floor(product.rating) }"> ★ </span>
                                    </div>
                                    <span class="rating-value">({{ product.rating }})</span>
                                </div>

                                <!-- Product Name -->
                                <h3 class="product-name">{{ product.label }}</h3>

                                <!-- Product Price -->
                                <div class="price-container">
                                    <p class="product-price">₫{{ formatPrice(product.price) }}</p>
                                    <p class="price-label">Giá tốt nhất</p>
                                </div>

                                <!-- View Details Button -->
                                <button class="view-details-btn" @click.stop="goToProductDetail(product)">
                                    <span class="btn-content">
                                        <svg class="view-icon" viewBox="0 0 24 24" fill="currentColor">
                                            <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
                                        </svg>
                                        <span class="btn-text">Xem chi tiết</span>
                                    </span>
                                    <div class="btn-background"></div>
                                </button>
                            </div>
                        </div>
                    </div>

                    <!-- Empty State -->
                    <div v-if="!loading && filteredProducts.length === 0" class="empty-state">
                        <svg viewBox="0 0 24 24" class="empty-icon">
                            <path fill="currentColor" d="M19,2H5A3,3 0 0,0 2,5V19A3,3 0 0,0 5,22H19A3,3 0 0,0 22,19V5A3,3 0 0,0 19,2M19,19H5V5H19V19M13.96,12.29L11.21,15.83L9.25,13.47L6.5,17H17.5L13.96,12.29Z" />
                        </svg>
                        <p class="empty-text">Không có sản phẩm nào trong danh mục này</p>
                    </div>
                </div>
            </div>
        </main>

        <!-- Footer Component -->
        <section id="#" class="padding-x padding-t bg-black pb-8">
            <Footer />
        </section>
        <ScrollToggler />

        <!-- ChatBot Component -->
        <ChatBot />
    </div>
</template>
<style lang="scss" scoped>
.nike-complete-layout {
    font-family: 'Helvetica Neue', Arial, sans-serif;
    background-color: #f5f5f5;
    min-height: 100vh;
}

.main-container {
    display: flex;
    max-width: 1600px;
    margin: 0 auto;
    gap: 2rem;
    padding: 2rem;
    position: relative;
}

/* Categories Sidebar */
.categories-sidebar {
    width: 280px;
    background: white;
    border-radius: 20px;
    padding: 2rem;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    height: fit-content;
    position: sticky;
    top: 2rem;
    transition: all 0.3s ease;
}

.sidebar-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 2rem;
}

.sidebar-title {
    font-size: 1.5rem;
    font-weight: 800;
    color: #1a202c;
    display: flex;
    align-items: center;
    gap: 0.75rem;
}

.category-icon {
    width: 24px;
    height: 24px;
    color: #ff6452;
}

.mobile-close-btn {
    display: none;
    background: none;
    border: none;
    padding: 0.5rem;
    cursor: pointer;
    color: #64748b;

    svg {
        width: 24px;
        height: 24px;
    }
}

.categories-list {
    margin-bottom: 2rem;
}

.category-skeleton {
    margin-bottom: 1rem;

    .skeleton-text {
        height: 48px;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: shimmer 2s infinite;
        border-radius: 12px;
    }
}

.category-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 1rem 1.25rem;
    margin-bottom: 0.5rem;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;

    &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        width: 4px;
        height: 100%;
        background: linear-gradient(135deg, #ff6452, #ff8a80);
        transform: scaleY(0);
        transition: transform 0.3s ease;
    }

    &:hover {
        background-color: #f8fafc;
        transform: translateX(8px);

        .category-arrow {
            transform: translateX(4px);
        }
    }

    &.active {
        background: linear-gradient(135deg, #ff6452 0%, #ff8a80 100%);
        color: white;
        box-shadow: 0 8px 20px rgba(255, 100, 82, 0.3);

        &::before {
            transform: scaleY(1);
            background: white;
        }

        .category-count {
            background: rgba(255, 255, 255, 0.2);
            color: white;
        }
    }
}

.category-name {
    font-weight: 600;
    font-size: 1rem;
}

.category-count {
    background: #f1f5f9;
    padding: 0.25rem 0.75rem;
    border-radius: 20px;
    font-size: 0.875rem;
    font-weight: 600;
    color: #64748b;
}

.category-arrow {
    display: flex;
    align-items: center;
    transition: transform 0.3s ease;

    svg {
        width: 20px;
        height: 20px;
    }
}

.sidebar-footer {
    border-top: 1px solid #e2e8f0;
    padding-top: 1.5rem;
}

.filter-section {
    text-align: center;
}

.filter-title {
    font-size: 0.875rem;
    color: #64748b;
    margin-bottom: 1rem;
    font-weight: 600;
}

.filter-btn {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.75rem;
    padding: 0.875rem;
    background: #f8fafc;
    border: 2px solid #e2e8f0;
    border-radius: 12px;
    font-weight: 600;
    color: #475569;
    cursor: pointer;
    transition: all 0.3s ease;

    svg {
        width: 20px;
        height: 20px;
    }

    &:hover {
        background: #ff6452;
        border-color: #ff6452;
        color: white;
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba(255, 100, 82, 0.2);
    }
}

/* Mobile Menu Toggle */
.mobile-menu-toggle {
    display: none;
    position: fixed;
    bottom: 2rem;
    left: 2rem;
    z-index: 100;
    background: linear-gradient(135deg, #ff6452, #ff8a80);
    color: white;
    padding: 1rem 1.5rem;
    border-radius: 50px;
    border: none;
    box-shadow: 0 8px 25px rgba(255, 100, 82, 0.4);
    cursor: pointer;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    transition: all 0.3s ease;

    svg {
        width: 24px;
        height: 24px;
    }

    &:hover {
        transform: translateY(-2px);
        box-shadow: 0 12px 30px rgba(255, 100, 82, 0.5);
    }
}

/* Products Container - Adjusted */
.products-container {
    flex: 1;
    position: relative;
    z-index: 1;
}

.section-header {
    text-align: center;
    margin-bottom: 4rem;

    .section-badge {
        display: inline-block;
        background: linear-gradient(135deg, #ff6452, #ff8a80);
        color: white;
        padding: 0.5rem 1.5rem;
        border-radius: 25px;
        font-size: 0.9rem;
        font-weight: 600;
        margin-bottom: 1.5rem;
        box-shadow: 0 4px 15px rgba(255, 100, 82, 0.3);
    }
}
/* ===== ĐÃ THÊM CSS CHO CHATBOT ===== */
/* Đảm bảo chatbot hiển thị trên cùng */
:deep(.chatbot-container) {
    z-index: 9999 !important;
}

/* Đảm bảo không conflict với mobile-menu-toggle */
.mobile-menu-toggle {
    z-index: 9997; /* Thấp hơn chatbot */
}

/* Đảm bảo ScrollToggler không conflict */
:deep(.scroll-toggler) {
    z-index: 9998;
}

/* Animations */
@keyframes slideUp {
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.highlight-gradient {
    background: linear-gradient(135deg, #ff6452, #ff8a80, #ffd700);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}
.mobile-close-btn {
    display: block;
}

.mobile-menu-toggle {
    display: flex;
}

.products-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 1.5rem;
}

.section-title {
    font-size: 2.5rem;
}

/* ===== RESPONSIVE CHATBOT ===== */
:deep(.chatbot-container) {
    bottom: 1rem;
    right: 1rem;
}

:deep(.chat-window) {
    width: calc(100vw - 2rem);
    height: 70vh;
}

/* Điều chỉnh mobile-menu-toggle khi có chatbot */
.mobile-menu-toggle {
    bottom: 80px; /* Tránh chatbot */
}
.section-description {
    color: #64748b;
    font-size: 1.2rem;
    max-width: 700px;
    margin: 0 auto;
    line-height: 1.6;
    font-weight: 400;
}

/* Empty State */
.empty-state {
    text-align: center;
    padding: 4rem 2rem;

    .empty-icon {
        width: 120px;
        height: 120px;
        margin: 0 auto 2rem;
        color: #e2e8f0;
    }

    .empty-text {
        font-size: 1.25rem;
        color: #64748b;
        font-weight: 500;
    }
}

/* Loading Skeleton */
.product-card-skeleton {
    background: white;
    border-radius: 24px;
    overflow: hidden;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);

    .skeleton-image {
        height: 280px;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: shimmer 2s infinite;
    }

    .skeleton-content {
        padding: 2rem;

        .skeleton-line {
            height: 16px;
            background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
            background-size: 200% 100%;
            animation: shimmer 2s infinite;
            border-radius: 8px;
            margin-bottom: 1rem;

            &.skeleton-rating {
                width: 60%;
            }
            &.skeleton-title {
                width: 80%;
                height: 20px;
            }
            &.skeleton-price {
                width: 40%;
                height: 24px;
            }
        }

        .skeleton-button {
            height: 48px;
            background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
            background-size: 200% 100%;
            animation: shimmer 2s infinite;
            border-radius: 12px;
            margin-top: 1rem;
        }
    }
}

/* Products Grid - Adjusted for smaller width */
.products-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 2rem;
    margin-top: 3rem;
}

.product-card {
    background: white;
    border-radius: 24px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    position: relative;
    animation: slideUp 0.6s ease-out forwards;
    opacity: 0;
    transform: translateY(30px);

    &:hover {
        transform: translateY(-12px) scale(1.02);
        box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);

        .product-image {
            transform: scale(1.05);
        }

        .product-overlay {
            opacity: 1;
        }

        .view-details-btn {
            .btn-background {
                transform: scaleX(1);
            }
        }
    }
}

.product-image-container {
    position: relative;
    background: transparent;
    padding: 2.5rem;
    display: flex;
    justify-content: center;
    align-items: center;
    height: 280px;
    overflow: hidden;
}

.product-image-wrapper {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
}

.product-image {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
    filter: drop-shadow(0 15px 30px rgba(0, 0, 0, 0.1));
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.product-placeholder {
    display: flex;
    justify-content: center;
    align-items: center;

    .placeholder-icon {
        width: 120px;
        height: 80px;
        opacity: 0.6;
    }
}

.product-badge {
    position: absolute;
    top: 1.5rem;
    right: 1.5rem;
    background: linear-gradient(135deg, #ff6452, #ff8a80);
    color: white;
    padding: 0.4rem 1rem;
    border-radius: 20px;
    font-size: 0.8rem;
    font-weight: 700;
    box-shadow: 0 4px 15px rgba(255, 100, 82, 0.4);
    z-index: 2;
}

.product-overlay {
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, rgba(255, 100, 82, 0.9), rgba(255, 138, 128, 0.9));
    display: flex;
    justify-content: center;
    align-items: center;
    opacity: 0;
    transition: all 0.3s ease;

    .quick-view-btn {
        background: white;
        color: #ff6452;
        width: 60px;
        height: 60px;
        border-radius: 50%;
        display: flex;
        justify-content: center;
        align-items: center;
        box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
        transform: scale(0.8);
        transition: all 0.3s ease;
        cursor: pointer;

        svg {
            width: 24px;
            height: 24px;
        }

        &:hover {
            transform: scale(1);
        }
    }
}

.product-info {
    padding: 2rem;
}

.mobile-menu-toggle {
    bottom: 80px; /* Tránh chatbot */
    left: 1rem;
    padding: 0.875rem 1.25rem;
    font-size: 0.9rem;
}

.product-name {
    font-size: 1.3rem;
    font-weight: 700;
    margin-bottom: 1rem;
    color: #1a202c;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.price-container {
    margin-bottom: 1.5rem;

    .product-price {
        font-size: 1.8rem;
        font-weight: 900;
        background: linear-gradient(135deg, #ff6452, #ff8a80);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
        margin-bottom: 0.25rem;
    }

    .price-label {
        font-size: 0.8rem;
        color: #10b981;
        font-weight: 600;
        text-transform: uppercase;
        letter-spacing: 0.5px;
    }
}

.view-details-btn {
    position: relative;
    width: 100%;
    background: transparent;
    border: 2px solid #3B82F6;
    color: #3B82F6;
    padding: 1rem;
    border-radius: 16px;
    font-weight: 700;
    cursor: pointer;
    overflow: hidden;
    transition: all 0.3s ease;

    .btn-content {
        position: relative;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 0.75rem;
        z-index: 2;
        transition: all 0.3s ease;

        .view-icon {
            width: 18px;
            height: 18px;
            transition: all 0.3s ease;
        }

        .btn-text {
            font-size: 1rem;
        }
    }

    .btn-background {
        position: absolute;
        inset: 0;
        background: linear-gradient(135deg, #3B82F6, #60A5FA);
        transform: scaleX(0);
        transform-origin: left;
        transition: transform 0.3s ease;
        z-index: 1;
    }

    &:hover {
        color: white;
        border-color: #3B82F6;
        transform: translateY(-2px);
        box-shadow: 0 8px 25px rgba(59, 130, 246, 0.3);

        .view-icon {
            transform: scale(1.1);
        }
    }
}

/* Animations */
@keyframes slideUp {
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes shimmer {
    0% {
        background-position: -200% 0;
    }
    100% {
        background-position: 200% 0;
    }
}

/* Responsive Design */
@media (max-width: 1400px) {
    .products-grid {
        grid-template-columns: repeat(2, 1fr);
    }
}

@media (max-width: 1024px) {
    .main-container {
        padding: 1rem;
    }

    .categories-sidebar {
        position: fixed;
        left: -100%;
        top: 0;
        height: 100vh;
        z-index: 1000;
        border-radius: 0;
        width: 300px;
        max-width: 80vw;
        padding: 2rem;
        overflow-y: auto;

        &.mobile-open {
            left: 0;
            box-shadow: 2px 0 20px rgba(0, 0, 0, 0.2);
        }
    }

    .mobile-close-btn {
        display: block;
    }

    .mobile-menu-toggle {
        display: flex;
    }

    .products-grid {
        grid-template-columns: repeat(2, 1fr);
        gap: 1.5rem;
    }

    .section-title {
        font-size: 2.5rem;
    }
}

@media (max-width: 768px) {
    .nike-layout {
        padding-top: 60px;
    }

    .products-grid {
        grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
        gap: 1.5rem;
    }

    .section-title {
        font-size: 2.5rem;
    }

    .section-description {
        font-size: 1rem;
    }

    .product-info {
        padding: 1.5rem;
    }

    .product-name {
        font-size: 1.1rem;
    }

    .product-price {
        font-size: 1.5rem;
    }
}

@media (max-width: 480px) {
    .main-container {
        padding: 0.5rem;
    }

    .section-title {
        font-size: 2rem;
    }

    .products-grid {
        grid-template-columns: 1fr;
        gap: 1.5rem;
    }

    .product-info {
        padding: 1.5rem;
    }

    .mobile-menu-toggle {
        bottom: 1rem;
        left: 1rem;
        padding: 0.875rem 1.25rem;
        font-size: 0.9rem;
    }
}
</style>