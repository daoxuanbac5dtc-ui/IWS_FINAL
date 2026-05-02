<script>
import ChatBot from '@/components/ChatBotAndReview/ChatBot.vue';
import Nav from '@/components/user/Nav.vue';
import ScrollToggler from '@/components/user/ScrollToggler.vue';
import Footer from '@/views/user/Footer.vue';
import { createProductImageLookup, createSvgPlaceholder, resolveProductImageUrl } from '@/utils/productMedia';
import axios from 'axios';
import HeroSection from '../HeroSection.vue';

const productPlaceholderImage = createSvgPlaceholder({ width: 240, height: 160, label: 'Shoe Image' });

export default {
    name: 'ProductList',

    components: {
        Nav,
        Footer,
        HeroSection,
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
            isMobileMenuOpen: false,
            isAdvancedFilterOpen: false,
            selectedPriceRange: 'all',
            selectedSort: 'default',
            priceRanges: [
                { value: 'all', label: 'Tất cả giá', min: null, max: null },
                { value: 'under1500', label: 'Dưới 1.500.000', min: 0, max: 1499999 },
                { value: '1500to2000', label: '1.500.000 - 2.000.000', min: 1500000, max: 2000000 },
                { value: '2000to2500', label: '2.000.000 - 2.500.000', min: 2000001, max: 2500000 },
                { value: 'over2500', label: 'Trên 2.500.000', min: 2500001, max: null }
            ],
            sortOptions: [
                { value: 'default', label: 'Mặc định' },
                { value: 'priceAsc', label: 'Giá thấp đến cao' },
                { value: 'priceDesc', label: 'Giá cao đến thấp' },
                { value: 'nameAsc', label: 'Tên A-Z' },
                { value: 'ratingDesc', label: 'Đánh giá cao' }
            ]
        };
    },

    computed: {
        filteredProducts() {
            let result = this.selectedCategory
                ? this.products.filter((product) => String(product.categoryId) === String(this.selectedCategory))
                : [...this.products];

            const priceRange = this.priceRanges.find((range) => range.value === this.selectedPriceRange);
            if (priceRange && priceRange.value !== 'all') {
                result = result.filter((product) => {
                    const price = Number(product.price) || 0;
                    const matchesMin = priceRange.min === null || price >= priceRange.min;
                    const matchesMax = priceRange.max === null || price <= priceRange.max;
                    return matchesMin && matchesMax;
                });
            }

            switch (this.selectedSort) {
                case 'priceAsc':
                    return [...result].sort((a, b) => (a.price || 0) - (b.price || 0));
                case 'priceDesc':
                    return [...result].sort((a, b) => (b.price || 0) - (a.price || 0));
                case 'nameAsc':
                    return [...result].sort((a, b) => a.label.localeCompare(b.label, 'vi'));
                case 'ratingDesc':
                    return [...result].sort((a, b) => Number(b.rating || 0) - Number(a.rating || 0));
                default:
                    return result;
            }
        },

        selectedCategoryName() {
            if (!this.selectedCategory) {
                return 'Phổ Biến';
            }
            const category = this.categories.find((cat) => String(cat.id) === String(this.selectedCategory));
            return category ? category.tenDanhMuc : 'Phổ Biến';
        },

        totalProducts() {
            return this.products.length;
        },

        productCountByCategory() {
            return this.products.reduce((counts, product) => {
                if (product.categoryId !== undefined && product.categoryId !== null) {
                    const categoryKey = String(product.categoryId);
                    counts.set(categoryKey, (counts.get(categoryKey) || 0) + 1);
                }
                return counts;
            }, new Map());
        },

        activeFilterCount() {
            let count = 0;
            if (this.selectedPriceRange !== 'all') count += 1;
            if (this.selectedSort !== 'default') count += 1;
            return count;
        }
    },

    methods: {
        selectCategory(categoryId) {
            this.selectedCategory = categoryId;
            this.isMobileMenuOpen = false;
        },

        scrollToProducts() {
            if (this.$refs.productsSection) {
                this.$refs.productsSection.scrollIntoView({ behavior: 'smooth' });
            }
        },

        toggleAdvancedFilter() {
            this.isAdvancedFilterOpen = !this.isAdvancedFilterOpen;
        },

        setPriceRange(value) {
            this.selectedPriceRange = value;
        },

        setSort(value) {
            this.selectedSort = value;
        },

        resetAdvancedFilters() {
            this.selectedPriceRange = 'all';
            this.selectedSort = 'default';
        },

        goToProductDetail(product) {
            console.log('Navigating to product detail:', product);
            if (product.firstDetailId) {
                this.$router.push({
                    path: `/product/${product.firstDetailId}`,
                    hash: '#product-info'
                });
            } else {
                console.warn('No firstDetailId found for product:', product);
                if (this.$toast) {
                    this.$toast.error('Không thể xem chi tiết sản phẩm này');
                } else {
                    alert('Không thể xem chi tiết sản phẩm này');
                }
            }
        },

        getImageUrl(product) {
            const imageUrl = resolveProductImageUrl(product.imgUrl);
            if (imageUrl) {
                return imageUrl;
            }
            return productPlaceholderImage;
        },

        getCategoryProductCount(categoryId) {
            return this.productCountByCategory.get(String(categoryId)) || 0;
        },

        resolveDetailImage(detail, imageLookup) {
            const image = detail?.hinhAnh;
            if (!image) return null;

            if (typeof image === 'object') {
                const imageById = image.id ? imageLookup.findById(image.id) : null;
                return imageById || resolveProductImageUrl(image);
            }

            if (typeof image === 'number' || typeof image === 'string') {
                return imageLookup.findById(image) || resolveProductImageUrl(image);
            }

            return null;
        },

        handleImageLoad(event) {
            // console.log('Image loaded successfully:', event.target.src);
        },

        handleImageError(event) {
            console.log('Image load failed for:', event.target.src);
            if (!event.target.src.startsWith('data:image/svg+xml')) {
                event.target.src = productPlaceholderImage;
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
            } catch (error) {
                console.error('Lỗi khi gọi API danh mục:', error);
                this.categories = [];
            } finally {
                this.loadingCategories = false;
            }
        },

        async fetchProducts() {
            try {
                this.loading = true;

                const [productsResponse, detailsResponse, imagesResponse] = await Promise.all([
                    axios.get('http://localhost:8080/api/san-pham'),
                    axios.get('http://localhost:8080/api/san-pham-chi-tiet'),
                    axios.get('http://localhost:8080/hinh-anh')
                ]);

                if (!productsResponse.data || productsResponse.data.length === 0) {
                    this.products = [];
                    return;
                }

                const imageLookup = createProductImageLookup(imagesResponse.data);

                const firstDetailMap = new Map();
                const priceMap = new Map();
                const productImageMap = new Map();

                detailsResponse.data.forEach((detail) => {
                    const rawProductId = (detail.sanPham && typeof detail.sanPham === 'object') 
                        ? detail.sanPham.id 
                        : detail.sanPham;
                    
                    if (rawProductId) {
                        const productId = String(rawProductId);

                        if (!firstDetailMap.has(productId)) {
                            firstDetailMap.set(productId, detail.id);
                        }

                        const detailGiaBan = detail.giaBan || 0;
                        if (detailGiaBan > 0) {
                            if (!priceMap.has(productId) || detailGiaBan < priceMap.get(productId).giaBan) {
                                priceMap.set(productId, {
                                    giaBan: detailGiaBan,
                                    giaGoc: detail.giaGoc || detailGiaBan
                                });
                            }
                        }

                        if (!productImageMap.has(productId) && detail.hinhAnh) {
                            const finalImageUrl = this.resolveDetailImage(detail, imageLookup);

                            if (finalImageUrl) {
                                productImageMap.set(productId, finalImageUrl);
                            }
                        }
                    }
                });

                this.products = productsResponse.data
                    .filter((p) => p.trangThai === 1)
                    .map((p) => {
                        const normalizedId = String(p.id);
                        const priceInfo = priceMap.get(normalizedId) || { giaBan: 0, giaGoc: 0 };

                        let imageUrl = productImageMap.get(normalizedId) || imageLookup.findForProduct(p);
                        if (!imageUrl && p.hinhAnh) {
                            imageUrl = resolveProductImageUrl(p.hinhAnh);
                        }

                        return {
                            id: p.id,
                            firstDetailId: firstDetailMap.get(normalizedId),
                            imgUrl: imageUrl,
                            label: p.tenSanPham || 'Sản phẩm không tên',
                            price: priceInfo.giaBan,
                            originalPrice: priceInfo.giaGoc,
                            rating: (4.5 + Math.random() * 0.5).toFixed(1),
                            brandId: p.thuongHieu?.id,
                            brandName: p.thuongHieu?.tenThuongHieu || '',
                            categoryId: p.danhMuc?.id,
                            categoryName: p.danhMuc?.tenDanhMuc || '',
                            maSanPham: p.maSanPham,
                            soLuong: p.soLuong || 0,
                            trangThai: p.trangThai
                        };
                    })
                    .filter((product) => product.firstDetailId && product.price > 0);

            } catch (error) {
                console.error('Error fetching products:', error);
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
            const shuffled = [...availableProducts].sort(() => 0.5 - Math.random());
            return shuffled.slice(0, count);
        }
    },

    mounted() {
        this.fetchCategories();
        this.fetchProducts();
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
        <Nav />
        <HeroSection />
        <main id="products" class="nike-layout">
            <div class="main-container">
                <div v-if="isMobileMenuOpen" class="sidebar-backdrop" @click="isMobileMenuOpen = false"></div>
                <aside id="product-categories" class="categories-sidebar" :class="{ 'mobile-open': isMobileMenuOpen }">
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

                            <div
                                v-for="category in categories"
                                :key="category.id"
                                class="category-item"
                                :class="{
                                    active: selectedCategory === category.id,
                                    empty: getCategoryProductCount(category.id) === 0
                                }"
                                @click="selectCategory(category.id)"
                            >
                                <span class="category-name">{{ category.tenDanhMuc }}</span>
                                <span class="category-count">{{ getCategoryProductCount(category.id) }}</span>
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
                            <button
                                class="filter-btn"
                                :class="{ active: isAdvancedFilterOpen || activeFilterCount > 0 }"
                                type="button"
                                @click="toggleAdvancedFilter"
                                :aria-expanded="isAdvancedFilterOpen"
                                aria-controls="advanced-filter-panel"
                            >
                                <svg viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M4 7h10.2a2 2 0 0 0 3.6 0H20a1 1 0 1 0 0-2h-2.2a2 2 0 0 0-3.6 0H4a1 1 0 1 0 0 2Zm0 6h2.2a2 2 0 0 0 3.6 0H20a1 1 0 1 0 0-2H9.8a2 2 0 0 0-3.6 0H4a1 1 0 1 0 0 2Zm0 6h10.2a2 2 0 0 0 3.6 0H20a1 1 0 1 0 0-2h-2.2a2 2 0 0 0-3.6 0H4a1 1 0 1 0 0 2Z" />
                                </svg>
                                <span>Lọc sản phẩm</span>
                                <span v-if="activeFilterCount" class="filter-count">{{ activeFilterCount }}</span>
                            </button>

                            <div id="advanced-filter-panel" v-if="isAdvancedFilterOpen" class="advanced-filter-panel">
                                <div class="filter-group">
                                    <div class="filter-group-title">Khoảng giá</div>
                                    <button
                                        v-for="range in priceRanges"
                                        :key="range.value"
                                        type="button"
                                        class="filter-option"
                                        :class="{ selected: selectedPriceRange === range.value }"
                                        @click="setPriceRange(range.value)"
                                    >
                                        <span>{{ range.label }}</span>
                                        <svg v-if="selectedPriceRange === range.value" viewBox="0 0 24 24" fill="currentColor">
                                            <path d="M9 16.2 4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4z" />
                                        </svg>
                                    </button>
                                </div>

                                <div class="filter-group">
                                    <div class="filter-group-title">Sắp xếp</div>
                                    <button
                                        v-for="sort in sortOptions"
                                        :key="sort.value"
                                        type="button"
                                        class="filter-option"
                                        :class="{ selected: selectedSort === sort.value }"
                                        @click="setSort(sort.value)"
                                    >
                                        <span>{{ sort.label }}</span>
                                        <svg v-if="selectedSort === sort.value" viewBox="0 0 24 24" fill="currentColor">
                                            <path d="M9 16.2 4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4z" />
                                        </svg>
                                    </button>
                                </div>

                                <button class="clear-filter-btn" type="button" @click="resetAdvancedFilters" :disabled="activeFilterCount === 0">
                                    Xóa bộ lọc
                                </button>
                            </div>
                        </div>
                    </div>
                </aside>

                <button class="mobile-menu-toggle" @click="isMobileMenuOpen = true" type="button" :aria-expanded="isMobileMenuOpen" aria-controls="product-categories">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                        <path d="M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z" />
                    </svg>
                    <span>Danh mục</span>
                </button>

                <div class="products-container" ref="productsSection">
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

                    <div v-else class="products-grid">
                        <div v-for="(product, index) in filteredProducts" :key="product.id" @click="goToProductDetail(product)" class="product-card" :style="{ animationDelay: `${index * 0.1}s` }">
                            <div class="product-image-container">
                                <div class="product-image-wrapper">
                                    <img :src="getImageUrl(product)" :alt="product.label" class="product-image" @error="handleImageError" @load="handleImageLoad" loading="lazy" />
                                </div>
                                <div class="product-badge">
                                    <span>Mới</span>
                                </div>
                                <div class="product-overlay">
                                    <div class="quick-view-btn">
                                        <svg viewBox="0 0 24 24" fill="currentColor">
                                            <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
                                        </svg>
                                    </div>
                                </div>
                            </div>

                            <div class="product-info">
                                <div class="product-rating">
                                    <div class="stars">
                                        <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= Math.floor(product.rating) }"> ★ </span>
                                    </div>
                                    <span class="rating-value">({{ product.rating }})</span>
                                </div>
                                <h3 class="product-name">{{ product.label }}</h3>
                                <div class="price-container">
                                    <p class="product-price">₫{{ formatPrice(product.price) }}</p>
                                    <p class="price-label">Giá tốt nhất</p>
                                </div>
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

                    <div v-if="!loading && filteredProducts.length === 0" class="empty-state">
                        <svg viewBox="0 0 24 24" class="empty-icon">
                            <path fill="currentColor" d="M19,2H5A3,3 0 0,0 2,5V19A3,3 0 0,0 5,22H19A3,3 0 0,0 22,19V5A3,3 0 0,0 19,2M19,19H5V5H19V19M13.96,12.29L11.21,15.83L9.25,13.47L6.5,17H17.5L13.96,12.29Z" />
                        </svg>
                        <p class="empty-text">
                            {{ selectedCategory ? `Chưa có sản phẩm trong danh mục ${selectedCategoryName}` : 'Chưa có sản phẩm phù hợp với bộ lọc hiện tại' }}
                        </p>
                    </div>
                </div>
            </div>
        </main>
        <section id="footer-section" class="bg-black px-8 pb-8 pt-12 sm:px-16 sm:pt-24">
            <Footer />
        </section>
        <ScrollToggler />
        <ChatBot />
    </div>
</template>

<style lang="scss" scoped>
.nike-complete-layout {
    font-family: 'Helvetica Neue', Arial, sans-serif;
    background-color: #f5f5f5;
    min-height: 100vh;
}

#products {
    scroll-margin-top: 96px;
}

.main-container {
    display: flex;
    max-width: 1600px;
    margin: 0 auto;
    gap: 2rem;
    padding: 2rem;
    position: relative;
}

.categories-sidebar {
    width: 280px;
    background: white;
    border-radius: 20px;
    padding: 2rem;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    height: fit-content;
    position: sticky;
    top: 2rem;
    z-index: 10;
}

.sidebar-backdrop {
    display: none;
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
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    border: none;
    border-radius: 999px;
    background: #f8fafc;
    color: #64748b;
    cursor: pointer;
    transition: all 0.2s ease;
}

.mobile-close-btn:hover {
    background: #ffefed;
    color: #ff6452;
}

.mobile-close-btn svg {
    width: 22px;
    height: 22px;
}

.category-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.75rem;
    padding: 1rem 1.25rem;
    margin-bottom: 0.5rem;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
        background-color: #f8fafc;
        transform: translateX(8px);
    }

    &.active {
        background: linear-gradient(135deg, #ff6452 0%, #ff8a80 100%);
        color: white;
        box-shadow: 0 8px 20px rgba(255, 100, 82, 0.3);
    }
}

.category-name {
    font-weight: 600;
    min-width: 0;
    flex: 1;
}

.category-count {
    background: #f1f5f9;
    padding: 0.25rem 0.75rem;
    border-radius: 20px;
    font-size: 0.875rem;
    color: #64748b;
    flex: 0 0 auto;
}

.active .category-count {
    background: rgba(255, 255, 255, 0.2);
    color: white;
}

.category-item.empty:not(.active) {
    color: #94a3b8;
}

.category-item.empty:not(.active) .category-count {
    background: #f8fafc;
    color: #94a3b8;
}

.category-arrow {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: #94a3b8;
    flex: 0 0 auto;
}

.category-arrow svg {
    width: 18px;
    height: 18px;
}

.sidebar-footer {
    margin-top: 1.5rem;
    padding-top: 1.5rem;
    border-top: 1px solid #e5e7eb;
}

.filter-section {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
    position: relative;
}

.filter-title {
    color: #475569;
    font-size: 1rem;
    font-weight: 700;
}

.filter-btn {
    width: 100%;
    min-height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.625rem;
    padding: 0 1rem;
    border: 1px solid #e2e8f0;
    border-radius: 14px;
    background: #f8fafc;
    color: #1f2937;
    font-weight: 800;
    cursor: pointer;
    transition: all 0.2s ease;
}

.filter-btn svg {
    width: 20px;
    height: 20px;
    flex: 0 0 auto;
    color: #ff6452;
}

.filter-btn:hover,
.filter-btn.active {
    border-color: #ff6452;
    background: #fff3f1;
    color: #ff6452;
    box-shadow: 0 10px 24px rgba(255, 100, 82, 0.14);
}

.filter-count {
    min-width: 22px;
    height: 22px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 999px;
    background: #ff6452;
    color: #ffffff;
    font-size: 0.75rem;
    font-weight: 900;
}

.advanced-filter-panel {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    position: absolute;
    left: 0;
    right: 0;
    bottom: calc(100% + 0.75rem);
    z-index: 30;
    max-height: min(30rem, calc(100vh - 10rem));
    overflow-y: auto;
    padding: 1rem;
    border: 1px solid #e5e7eb;
    border-radius: 16px;
    background: #ffffff;
    box-shadow: 0 14px 30px rgba(15, 23, 42, 0.08);
    transform-origin: bottom center;
}

.filter-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.filter-group-title {
    color: #1f2937;
    font-size: 0.875rem;
    font-weight: 800;
}

.filter-option {
    min-height: 38px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.75rem;
    padding: 0.625rem 0.75rem;
    border: 1px solid transparent;
    border-radius: 10px;
    background: #f8fafc;
    color: #475569;
    font-size: 0.9rem;
    font-weight: 700;
    text-align: left;
    cursor: pointer;
    transition: all 0.2s ease;
}

.filter-option svg {
    width: 16px;
    height: 16px;
    color: #ff6452;
}

.filter-option:hover,
.filter-option.selected {
    border-color: #ffd2cc;
    background: #fff3f1;
    color: #ff6452;
}

.clear-filter-btn {
    min-height: 40px;
    border: none;
    border-radius: 10px;
    background: #1f2937;
    color: #ffffff;
    font-weight: 800;
    cursor: pointer;
    transition: all 0.2s ease;
}

.clear-filter-btn:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 10px 20px rgba(15, 23, 42, 0.16);
}

.clear-filter-btn:disabled {
    cursor: not-allowed;
    opacity: 0.42;
}

.products-container {
    flex: 1;
}

.products-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 2rem;
}

.product-card {
    background: white;
    border-radius: 20px;
    overflow: hidden;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
    transition: all 0.4s ease;
    cursor: pointer;

    &:hover {
        transform: translateY(-10px);
        box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
    }
}

.product-image-container {
    position: relative;
    height: 240px;
    background: #f8f9fa;
    display: flex;
    align-items: center;
    justify-content: center;
}

.product-image-wrapper {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
}

.product-image {
    width: 100%;
    height: 100%;
    max-width: 80%;
    max-height: 80%;
    object-fit: contain;
    transition: transform 0.5s ease;
}

.product-card:hover .product-image {
    transform: scale(1.1) rotate(-5deg);
}

.product-badge {
    position: absolute;
    top: 15px;
    right: 15px;
    background: #ff6452;
    color: white;
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 0.75rem;
    font-weight: 700;
}

.product-info {
    padding: 1.5rem;
}

.product-name {
    font-size: 1.1rem;
    font-weight: 700;
    margin: 0.5rem 0;
    color: #1a202c;
}

.product-price {
    font-size: 1.25rem;
    font-weight: 800;
    color: #ff6452;
}

.price-label {
    font-size: 0.75rem;
    color: #64748b;
    font-weight: 600;
    text-transform: uppercase;
}

.view-details-btn {
    width: 100%;
    margin-top: 1rem;
    padding: 0.75rem;
    border-radius: 12px;
    border: 2px solid #ff6452;
    background: transparent;
    color: #ff6452;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;

    &:hover {
        background: #ff6452;
        color: white;
    }
}

.btn-content {
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 0.25rem;
    text-align: center;
    line-height: 1.2;
}

.view-icon {
    width: 18px;
    height: 18px;
    display: block;
    margin: 0 auto;
    flex: 0 0 auto;
}

.section-header {
    text-align: center;
    margin-bottom: 3rem;
}

.section-badge {
    background: #ffefed;
    color: #ff6452;
    padding: 4px 16px;
    border-radius: 20px;
    font-weight: 700;
    display: inline-block;
    margin-bottom: 1rem;
}

.section-title {
    font-size: 2.5rem;
    font-weight: 800;
}

.highlight-gradient {
    color: #ff6452;
}

.mobile-menu-toggle {
    display: none;
}

@media (max-width: 1024px) {
    .main-container {
        flex-direction: column;
        padding: 1rem;
    }

    .sidebar-backdrop {
        display: block;
        position: fixed;
        inset: 0;
        z-index: 1250;
        background: rgba(15, 23, 42, 0.42);
        backdrop-filter: blur(2px);
    }

    .categories-sidebar {
        position: fixed;
        top: 0;
        left: 0;
        z-index: 1300;
        width: min(86vw, 340px);
        height: 100dvh;
        overflow-y: auto;
        border-radius: 0 20px 20px 0;
        transform: translateX(-105%);
        transition: transform 0.28s ease;
        box-shadow: 20px 0 45px rgba(15, 23, 42, 0.18);
    }

    .categories-sidebar.mobile-open {
        transform: translateX(0);
    }

    .mobile-close-btn {
        display: inline-flex;
    }

    .mobile-menu-toggle {
        position: fixed;
        right: 1.5rem;
        bottom: 11rem;
        z-index: 1050;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 0.5rem;
        min-height: 52px;
        padding: 0 1rem;
        border: 1px solid rgba(255, 255, 255, 0.55);
        border-radius: 999px;
        background: linear-gradient(135deg, #ff6452, #ff8a80);
        color: #ffffff;
        font-weight: 800;
        box-shadow: 0 12px 28px rgba(255, 100, 82, 0.32);
        cursor: pointer;
        transition: transform 0.2s ease, box-shadow 0.2s ease;
    }

    .mobile-menu-toggle:hover {
        transform: translateY(-2px);
        box-shadow: 0 16px 34px rgba(255, 100, 82, 0.38);
    }

    .mobile-menu-toggle svg {
        width: 20px;
        height: 20px;
    }

    .advanced-filter-panel {
        position: static;
        max-height: none;
        overflow-y: visible;
    }
}

@media (max-width: 480px) {
    .mobile-menu-toggle {
        right: 1rem;
        bottom: 10rem;
        min-height: 48px;
        padding: 0 0.875rem;
        font-size: 0.875rem;
    }
}

/* Skeleton Styles */
.product-card-skeleton {
    background: white;
    border-radius: 20px;
    height: 400px;
    overflow: hidden;
}

.skeleton-image {
    height: 240px;
    background: #eee;
}

.skeleton-content {
    padding: 1.5rem;
}

.skeleton-line {
    height: 15px;
    background: #eee;
    margin-bottom: 10px;
    border-radius: 4px;
}

.skeleton-title { width: 80%; }
.skeleton-price { width: 40%; }
.skeleton-button { height: 40px; background: #eee; border-radius: 12px; margin-top: 10px; }

@keyframes shimmer {
    0% { opacity: 0.5; }
    50% { opacity: 1; }
    100% { opacity: 0.5; }
}

.product-card-skeleton * {
    animation: shimmer 1.5s infinite;
}
</style>
