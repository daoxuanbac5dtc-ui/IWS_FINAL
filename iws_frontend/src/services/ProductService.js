// services/ProductService.js
import axios from 'axios';

/**
 * ProductService - Service quản lý tất cả dữ liệu liên quan đến sản phẩm
 * Singleton pattern để đảm bảo data consistency across components
 */
class ProductService {
  constructor() {
    this.baseURL = 'http://localhost:8080';
    this.products = [];
    this.productDetails = [];
    this.categories = [];
    this.brands = [];
    this.materials = [];
    this.sizes = [];
    this.colors = [];
    this.soles = [];
    this.isLoaded = false;
    this.isLoading = false;
    this.loadPromise = null;
    
    // Cache cho performance
    this.cache = {
      productList: null,
      categoryMap: new Map(),
      brandMap: new Map(),
      lastUpdated: null
    };
  }

  /**
   * Khởi tạo dữ liệu - load tất cả data cần thiết
   * @returns {Promise}
   */
  async initialize() {
    if (this.isLoaded) return Promise.resolve();
    if (this.isLoading) return this.loadPromise;
    
    this.isLoading = true;
    this.loadPromise = this._loadAllData();
    
    try {
      await this.loadPromise;
      this.isLoaded = true;
      this.cache.lastUpdated = Date.now();
      console.log('✅ ProductService initialized successfully');
    } catch (error) {
      console.error('❌ ProductService initialization failed:', error);
      this.isLoaded = false;
      throw error;
    } finally {
      this.isLoading = false;
      this.loadPromise = null;
    }
  }

  /**
   * Load tất cả dữ liệu từ API
   * @private
   */
  async _loadAllData() {
    try {
      console.log('🔄 Loading all product data...');
      
      const [
        productsResponse,
        detailsResponse,
        categoriesResponse,
        brandsResponse,
        materialsResponse,
        sizesResponse,
        colorsResponse,
        solesResponse
      ] = await Promise.all([
        axios.get(`${this.baseURL}/api/san-pham`),
        axios.get(`${this.baseURL}/api/san-pham-chi-tiet`),
        axios.get(`${this.baseURL}/danh-muc`),
        axios.get(`${this.baseURL}/thuong-hieu`).catch(() => ({ data: [] })),
        axios.get(`${this.baseURL}/chat-lieu`).catch(() => ({ data: [] })),
        axios.get(`${this.baseURL}/kich-co`).catch(() => ({ data: [] })),
        axios.get(`${this.baseURL}/mau-sac`).catch(() => ({ data: [] })),
        axios.get(`${this.baseURL}/de-giay`).catch(() => ({ data: [] }))
      ]);

      // Lưu raw data
      this.products = productsResponse.data || [];
      this.productDetails = detailsResponse.data || [];
      this.categories = categoriesResponse.data?.filter(cat => cat.trangThai === 1) || [];
      this.brands = brandsResponse.data?.filter(brand => brand.trangThai === 1) || [];
      this.materials = materialsResponse.data?.filter(material => material.trangThai === 1) || [];
      this.sizes = sizesResponse.data?.filter(size => size.trangThai === 1) || [];
      this.colors = colorsResponse.data?.filter(color => color.trangThai === 1) || [];
      this.soles = solesResponse.data?.filter(sole => sole.trangThai === 1) || [];

      // Tạo maps cho quick lookup
      this._buildCacheMaps();

      console.log('📊 Data loaded:', {
        products: this.products.length,
        details: this.productDetails.length,
        categories: this.categories.length,
        brands: this.brands.length,
        materials: this.materials.length,
        sizes: this.sizes.length,
        colors: this.colors.length,
        soles: this.soles.length
      });

    } catch (error) {
      console.error('💥 Error loading data:', error);
      throw new Error(`Failed to load product data: ${error.message}`);
    }
  }

  /**
   * Tạo cache maps cho performance
   * @private
   */
  _buildCacheMaps() {
    // Category map
    this.categories.forEach(cat => {
      this.cache.categoryMap.set(cat.id, cat);
    });

    // Brand map
    this.brands.forEach(brand => {
      this.cache.brandMap.set(brand.id, brand);
    });
  }

  /**
   * Lấy danh sách sản phẩm cho ProductList
   * @returns {Array} Danh sách sản phẩm đã được format
   */
  getProductsForList() {
    if (this.cache.productList) {
      return this.cache.productList;
    }

    // Tạo maps để xử lý data
    const firstDetailMap = new Map();
    const priceMap = new Map();
    const imageMap = new Map();
    const stockMap = new Map();
    const variantCountMap = new Map();
    
    // Xử lý từng product detail
    this.productDetails.forEach(detail => {
      if (!detail.sanPham?.id) return;
      
      const productId = detail.sanPham.id;
      
      // Lưu chi tiết đầu tiên
      if (!firstDetailMap.has(productId)) {
        firstDetailMap.set(productId, detail.id);
      }
      
      // Tính giá thấp nhất và cao nhất
      const currentPriceInfo = priceMap.get(productId) || { 
        minPrice: Infinity, 
        maxPrice: 0, 
        minOriginalPrice: Infinity,
        maxOriginalPrice: 0,
        bestPrice: null,
        bestOriginalPrice: null
      };
      
      if (detail.giaBan && detail.giaBan > 0) {
        if (detail.giaBan < currentPriceInfo.minPrice) {
          currentPriceInfo.minPrice = detail.giaBan;
          currentPriceInfo.bestPrice = detail.giaBan;
          currentPriceInfo.bestOriginalPrice = detail.giaGoc;
        }
        if (detail.giaBan > currentPriceInfo.maxPrice) {
          currentPriceInfo.maxPrice = detail.giaBan;
        }
      }
      
      priceMap.set(productId, currentPriceInfo);
      
      // Lưu hình ảnh đầu tiên
      if (!imageMap.has(productId) && detail.hinhAnh) {
        const imageUrl = this.processImageUrl(detail.hinhAnh);
        if (imageUrl) {
          imageMap.set(productId, imageUrl);
        }
      }
      
      // Tính tổng stock
      const currentStock = stockMap.get(productId) || 0;
      stockMap.set(productId, currentStock + (detail.soLuong || 0));
      
      // Đếm variants
      const currentVariants = variantCountMap.get(productId) || 0;
      variantCountMap.set(productId, currentVariants + 1);
    });
    
    // Format products
    const formattedProducts = this.products
      .filter(p => p.trangThai === 1) // Chỉ lấy sản phẩm active
      .map(product => {
        const priceInfo = priceMap.get(product.id) || { bestPrice: 0, bestOriginalPrice: 0 };
        const imageUrl = imageMap.get(product.id);
        const firstDetailId = firstDetailMap.get(product.id);
        const totalStock = stockMap.get(product.id) || 0;
        const variantCount = variantCountMap.get(product.id) || 0;
        
        return {
          id: product.id,
          firstDetailId: firstDetailId,
          imgUrl: imageUrl,
          label: product.tenSanPham || 'Sản phẩm không tên',
          price: priceInfo.bestPrice || 0,
          originalPrice: priceInfo.bestOriginalPrice || 0,
          minPrice: priceInfo.minPrice === Infinity ? 0 : priceInfo.minPrice,
          maxPrice: priceInfo.maxPrice || 0,
          rating: this._generateRating(), // Generate realistic rating
          totalStock: totalStock,
          variantCount: variantCount,
          isInStock: totalStock > 0,
          
          // Thông tin chi tiết
          brandId: product.thuongHieu?.id,
          brandName: product.thuongHieu?.tenThuongHieu || '',
          categoryId: product.danhMuc?.id,
          categoryName: product.danhMuc?.tenDanhMuc || '',
          materialId: product.chatLieu?.id,
          materialName: product.chatLieu?.tenChatLieu || '',
          soleId: product.deGiay?.id,
          soleName: product.deGiay?.loaiDe || '',
          
          // Meta data
          maSanPham: product.maSanPham,
          moTa: product.moTa,
          trangThai: product.trangThai,
          ngayTao: product.ngayTao,
          ngayCapNhat: product.ngayCapNhat
        };
      })
      .filter(p => p.price > 0 && p.firstDetailId) // Chỉ lấy sản phẩm có giá và có chi tiết
      .sort((a, b) => b.ngayCapNhat?.localeCompare(a.ngayCapNhat) || 0); // Sort theo ngày cập nhật mới nhất

    // Cache result
    this.cache.productList = formattedProducts;
    return formattedProducts;
  }

  /**
   * Lấy chi tiết sản phẩm theo ID
   * @param {number|string} detailId ID của product detail
   * @returns {Promise<Object>} Chi tiết sản phẩm đã được xử lý
   */
  async getProductDetail(detailId) {
    try {
      console.log('🔍 Getting product detail:', detailId);
      
      // Tìm trong cache trước
      let currentDetail = this.productDetails.find(d => d.id == detailId);
      
      // Nếu không có trong cache, gọi API
      if (!currentDetail) {
        console.log('📡 Fetching detail from API...');
        const response = await axios.get(`${this.baseURL}/api/san-pham-chi-tiet/${detailId}`);
        currentDetail = response.data;
        
        if (!currentDetail) {
          throw new Error('Product detail not found');
        }
        
        // Thêm vào cache
        this.productDetails.push(currentDetail);
      }
      
      return await this._processProductDetail(currentDetail);
      
    } catch (error) {
      console.error('💥 Error getting product detail:', error);
      throw new Error(`Failed to get product detail: ${error.message}`);
    }
  }

  /**
   * Xử lý chi tiết sản phẩm
   * @param {Object} currentDetail Chi tiết sản phẩm hiện tại
   * @returns {Object} Dữ liệu đã được xử lý
   * @private
   */
  async _processProductDetail(currentDetail) {
    // Lấy tất cả variants của cùng sản phẩm
    const relatedDetails = this.productDetails.filter(d => 
      d.sanPham?.id === currentDetail.sanPham?.id
    );

    // Lấy danh sách màu sắc có sẵn
    const availableColors = [...new Map(
      relatedDetails
        .filter(d => d.mauSac && d.soLuong > 0)
        .map(d => [d.mauSac.id, {
          id: d.mauSac.id,
          name: d.mauSac.tenMauSac,
          hex: this.getColorHex(d.mauSac.tenMauSac)
        }])
    ).values()];
    
    // Lấy danh sách kích cỡ có sẵn
    const availableSizes = [...new Map(
      relatedDetails
        .filter(d => d.kichCo && d.soLuong > 0)
        .map(d => [d.kichCo.id, {
          id: d.kichCo.id,
          name: d.kichCo.tenKichCo,
          size: parseFloat(d.kichCo.tenKichCo) || d.kichCo.tenKichCo
        }])
    ).values()].sort((a, b) => {
      // Sort sizes numerically if possible
      const aNum = parseFloat(a.name);
      const bNum = parseFloat(b.name);
      if (!isNaN(aNum) && !isNaN(bNum)) {
        return aNum - bNum;
      }
      return a.name.localeCompare(b.name);
    });

    // Tạo map hình ảnh theo màu sắc
    const imagesByColor = new Map();
    const allImages = [];
    
    relatedDetails.forEach(detail => {
      if (detail.hinhAnh) {
        const imageUrl = this.processImageUrl(detail.hinhAnh);
        if (imageUrl) {
          allImages.push(imageUrl);
          
          if (detail.mauSac) {
            const colorId = detail.mauSac.id;
            if (!imagesByColor.has(colorId)) {
              imagesByColor.set(colorId, []);
            }
            imagesByColor.get(colorId).push(imageUrl);
          }
        }
      }
    });

    // Tạo variant map (màu + size -> chi tiết)
    const variantMap = new Map();
    const stockMap = new Map(); // colorId-sizeId -> stock
    
    relatedDetails.forEach(detail => {
      if (detail.mauSac && detail.kichCo) {
        const key = `${detail.mauSac.id}-${detail.kichCo.id}`;
        variantMap.set(key, detail);
        stockMap.set(key, detail.soLuong || 0);
      }
    });

    // Tính thống kê
    const totalStock = relatedDetails.reduce((sum, d) => sum + (d.soLuong || 0), 0);
    const priceRange = this._calculatePriceRange(relatedDetails);
    const averageRating = this._generateRating();

    return {
      // Dữ liệu cơ bản
      product: currentDetail,
      currentProduct: currentDetail,
      allProductDetails: relatedDetails,
      
      // Options
      availableColors,
      availableSizes,
      
      // Images
      imagesByColor,
      allImages: [...new Set(allImages)],
      
      // Variants
      variantMap,
      stockMap,
      
      // Selections (mặc định)
      selectedColor: currentDetail.mauSac?.id || (availableColors[0]?.id),
      selectedSize: currentDetail.kichCo?.id || (availableSizes[0]?.id),
      
      // Statistics
      totalStock,
      priceRange,
      averageRating,
      totalVariants: relatedDetails.length,
      
      // Related products hint
      categoryId: currentDetail.sanPham?.danhMuc?.id,
      brandId: currentDetail.sanPham?.thuongHieu?.id
    };
  }

  /**
   * Lấy sản phẩm tương tự
   * @param {number} excludeProductId ID sản phẩm cần loại trừ
   * @param {number} categoryId ID danh mục (ưu tiên)
   * @param {number} count Số lượng sản phẩm cần lấy
   * @returns {Array} Danh sách sản phẩm tương tự
   */
  getSimilarProducts(excludeProductId, categoryId = null, count = 4) {
    const productList = this.getProductsForList();
    let availableProducts = productList.filter(p => 
      p.id !== excludeProductId && p.isInStock
    );
    
    if (categoryId) {
      // Lấy sản phẩm cùng danh mục trước
      const sameCategoryProducts = availableProducts.filter(p => p.categoryId === categoryId);
      
      if (sameCategoryProducts.length >= count) {
        return this._shuffleArray(sameCategoryProducts).slice(0, count);
      } else {
        // Không đủ sản phẩm cùng danh mục, lấy thêm từ danh mục khác
        const otherProducts = availableProducts.filter(p => p.categoryId !== categoryId);
        const combined = [...sameCategoryProducts, ...otherProducts];
        return this._shuffleArray(combined).slice(0, count);
      }
    }
    
    // Không có categoryId, random từ tất cả
    return this._shuffleArray(availableProducts).slice(0, count);
  }

  /**
   * Lấy sản phẩm phổ biến
   * @param {number} count Số lượng sản phẩm
   * @returns {Array} Danh sách sản phẩm phổ biến
   */
  getPopularProducts(count = 4) {
    const productList = this.getProductsForList();
    
    // Filter valid products
    const validProducts = productList.filter(p => 
      p.isInStock && p.price > 0 && p.imgUrl
    );
    
    // Sort by criteria that make products "popular"
    const popularProducts = validProducts.sort((a, b) => {
      // Criteria: higher rating, more variants, newer products
      const scoreA = (a.rating * 0.4) + (a.variantCount * 0.3) + (a.totalStock > 50 ? 0.3 : 0);
      const scoreB = (b.rating * 0.4) + (b.variantCount * 0.3) + (b.totalStock > 50 ? 0.3 : 0);
      return scoreB - scoreA;
    });
    
    // Add some randomness to avoid always showing the same products
    const topProducts = popularProducts.slice(0, Math.min(count * 2, popularProducts.length));
    return this._shuffleArray(topProducts).slice(0, count);
  }

  /**
   * Tìm kiếm sản phẩm
   * @param {string} query Từ khóa tìm kiếm
   * @param {Object} filters Bộ lọc
   * @returns {Array} Kết quả tìm kiếm
   */
  searchProducts(query, filters = {}) {
    let products = this.getProductsForList();
    
    // Text search
    if (query && query.trim()) {
      const searchTerm = query.toLowerCase().trim();
      products = products.filter(p => 
        p.label.toLowerCase().includes(searchTerm) ||
        p.brandName.toLowerCase().includes(searchTerm) ||
        p.categoryName.toLowerCase().includes(searchTerm) ||
        p.maSanPham?.toLowerCase().includes(searchTerm)
      );
    }
    
    // Category filter
    if (filters.categoryId) {
      products = products.filter(p => p.categoryId === filters.categoryId);
    }
    
    // Brand filter
    if (filters.brandId) {
      products = products.filter(p => p.brandId === filters.brandId);
    }
    
    // Price range filter
    if (filters.minPrice || filters.maxPrice) {
      products = products.filter(p => {
        const price = p.price;
        const minOk = !filters.minPrice || price >= filters.minPrice;
        const maxOk = !filters.maxPrice || price <= filters.maxPrice;
        return minOk && maxOk;
      });
    }
    
    // In stock filter
    if (filters.inStockOnly) {
      products = products.filter(p => p.isInStock);
    }
    
    // Sort
    if (filters.sortBy) {
      products = this._sortProducts(products, filters.sortBy);
    }
    
    return products;
  }

  /**
   * Lấy sản phẩm theo danh mục
   * @param {number} categoryId ID danh mục
   * @returns {Array} Danh sách sản phẩm
   */
  getProductsByCategory(categoryId) {
    if (!categoryId) return this.getProductsForList();
    
    return this.getProductsForList().filter(p => p.categoryId === categoryId);
  }

  /**
   * Lấy danh sách danh mục
   * @returns {Array} Danh sách danh mục
   */
  getCategories() {
    return this.categories.map(cat => ({
      ...cat,
      productCount: this.getProductsByCategory(cat.id).length
    }));
  }

  /**
   * Lấy danh sách thương hiệu
   * @returns {Array} Danh sách thương hiệu
   */
  getBrands() {
    return this.brands;
  }

  /**
   * Lấy thống kê tổng quan
   * @returns {Object} Thống kê
   */
  getStatistics() {
    const products = this.getProductsForList();
    
    return {
      totalProducts: products.length,
      inStockProducts: products.filter(p => p.isInStock).length,
      totalCategories: this.categories.length,
      totalBrands: this.brands.length,
      averagePrice: products.reduce((sum, p) => sum + p.price, 0) / products.length,
      priceRange: {
        min: Math.min(...products.map(p => p.price)),
        max: Math.max(...products.map(p => p.price))
      }
    };
  }

  // ==================== UTILITY METHODS ====================

  /**
   * Xử lý URL hình ảnh
   * @param {Object|string} imageData Dữ liệu hình ảnh
   * @returns {string} URL hình ảnh đã xử lý
   */
  processImageUrl(imageData) {
    let imageUrl = '';
    
    if (typeof imageData === 'object' && imageData !== null) {
      imageUrl = imageData.duongDan || 
                 imageData.url || 
                 imageData.path || 
                 imageData.link || 
                 imageData.src || 
                 imageData.href || '';
    } else if (typeof imageData === 'string') {
      imageUrl = imageData;
    }
    
    if (!imageUrl) return null;
    
    // Handle relative URLs
    if (imageUrl && !imageUrl.startsWith('http')) {
      imageUrl = this.baseURL + (imageUrl.startsWith('/') ? '' : '/') + imageUrl;
    }
    
    return imageUrl;
  }

  /**
   * Định dạng giá tiền
   * @param {number} price Giá
   * @returns {string} Giá đã định dạng
   */
  formatPrice(price) {
    if (!price || price === 0) return '0';
    return Math.round(price).toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  }

  /**
   * Lấy mã màu hex từ tên màu
   * @param {string} colorName Tên màu
   * @returns {string} Mã màu hex
   */
  getColorHex(colorName) {
    const colorMap = {
      'Trắng': '#ffffff',
      'Đen': '#000000',
      'Đỏ': '#ff0000',
      'Xanh Dương': '#0000ff',
      'Xanh Navy': '#001f3f',
      'Xanh Lá': '#008000',
      'Vàng': '#ffff00',
      'Cam': '#ffa500',
      'Hồng': '#ff69b4',
      'Tím': '#800080',
      'Nâu': '#8b4513',
      'Xám': '#808080',
      'Bạc': '#c0c0c0',
      'Vàng Gold': '#ffd700',
      'Be': '#f5f5dc',
      'Xanh Lam': '#87ceeb',
      'Xanh Mint': '#98ff98',
      'Hồng Phấn': '#ffb6c1',
      'Tím Lavender': '#e6e6fa'
    };
    
    return colorMap[colorName] || '#cccccc';
  }

  /**
   * Làm mới dữ liệu
   * @returns {Promise}
   */
  async refresh() {
    console.log('🔄 Refreshing ProductService data...');
    this.isLoaded = false;
    this.cache.productList = null;
    this.cache.categoryMap.clear();
    this.cache.brandMap.clear();
    await this.initialize();
  }

  /**
   * Kiểm tra trạng thái service
   * @returns {Object} Trạng thái
   */
  getStatus() {
    return {
      isLoaded: this.isLoaded,
      isLoading: this.isLoading,
      lastUpdated: this.cache.lastUpdated,
      dataCount: {
        products: this.products.length,
        details: this.productDetails.length,
        categories: this.categories.length
      }
    };
  }

  // ==================== PRIVATE METHODS ====================

  /**
   * Generate realistic rating
   * @returns {number} Rating từ 3.5 đến 5.0
   * @private
   */
  _generateRating() {
    return Math.round((3.5 + Math.random() * 1.5) * 10) / 10;
  }

  /**
   * Tính khoảng giá
   * @param {Array} details Danh sách chi tiết
   * @returns {Object} Khoảng giá
   * @private
   */
  _calculatePriceRange(details) {
    const prices = details
      .filter(d => d.giaBan && d.giaBan > 0)
      .map(d => d.giaBan);
    
    if (prices.length === 0) {
      return { min: 0, max: 0 };
    }
    
    return {
      min: Math.min(...prices),
      max: Math.max(...prices)
    };
  }

  /**
   * Shuffle array
   * @param {Array} array Mảng cần shuffle
   * @returns {Array} Mảng đã shuffle
   * @private
   */
  _shuffleArray(array) {
    const shuffled = [...array];
    for (let i = shuffled.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
    }
    return shuffled;
  }

  /**
   * Sắp xếp sản phẩm
   * @param {Array} products Danh sách sản phẩm
   * @param {string} sortBy Tiêu chí sắp xếp
   * @returns {Array} Danh sách đã sắp xếp
   * @private
   */
  _sortProducts(products, sortBy) {
    switch (sortBy) {
      case 'price-asc':
        return products.sort((a, b) => a.price - b.price);
      case 'price-desc':
        return products.sort((a, b) => b.price - a.price);
      case 'name-asc':
        return products.sort((a, b) => a.label.localeCompare(b.label));
      case 'name-desc':
        return products.sort((a, b) => b.label.localeCompare(a.label));
      case 'rating-desc':
        return products.sort((a, b) => b.rating - a.rating);
      case 'newest':
        return products.sort((a, b) => 
          new Date(b.ngayCapNhat || b.ngayTao) - new Date(a.ngayCapNhat || a.ngayTao)
        );
      case 'popular':
        return products.sort((a, b) => 
          (b.rating * b.variantCount) - (a.rating * a.variantCount)
        );
      default:
        return products;
    }
  }
}

// Export singleton instance
const productService = new ProductService();

// Auto-initialize khi import (optional)
// productService.initialize().catch(console.error);

export default productService;