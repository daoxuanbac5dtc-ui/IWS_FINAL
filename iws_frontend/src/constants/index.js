import { facebook, instagram, shieldTick as shieldTickIcon, support as supportIcon, truckFast as truckFastIcon, twitter } from '../assets/icons';
import { customer1, customer2 } from '../assets/images';

// Navigation Links - Phù hợp với router
export const navLinks = [
    { href: '/', label: 'Trang Chủ', route: 'user-home' },
    { href: '/gioithieu', label: 'Giới Thiệu', route: 'gioi-thieu' },
    { href: '/products', label: 'Sản Phẩm', route: 'products' },
    { href: '/track-order', label: 'Tra cứu đơn hàng', route: 'track-order' },
    { href: '/lienhe', label: 'Liên Hệ', route: 'lien-he' }
];

// Statistics cho Hero Section
export const statistics = [
    { value: '1K+', label: 'Thương hiệu' },
    { value: '250+', label: 'Cửa hàng' },
    { value: '250K+', label: 'Khách hàng' }
];

// Services/Offers
export const offers = [
    {
        iconUrl: truckFastIcon,
        label: 'Miễn phí vận chuyển',
        desc: 'Tận hưởng trải nghiệm mua sắm dễ dàng với dịch vụ vận chuyển miễn phí của chúng tôi.'
    },
    {
        iconUrl: shieldTickIcon,
        label: 'Thanh toán an toàn',
        desc: 'Trải nghiệm giao dịch không phải lo lắng với các tùy chọn thanh toán an toàn của chúng tôi.'
    },
    {
        iconUrl: supportIcon,
        label: 'Rất vui được giúp đỡ bạn',
        desc: 'Đội ngũ tận tâm của chúng tôi luôn sẵn sàng hỗ trợ bạn trong từng bước thực hiện.'
    }
];

// Customer Reviews
export const customers = [
    {
        profileImg: customer1,
        name: 'Nguyễn Văn Ngọc',
        rating: '4.5',
        comment: 'Sự chú ý đến từng chi tiết và chất lượng sản phẩm vượt quá mong đợi của tôi. Rất đáng để giới thiệu!'
    },
    {
        profileImg: customer2,
        name: 'Trần Đức Hiếu',
        rating: '4.8',
        comment: 'Sản phẩm không chỉ đáp ứng mà còn vượt quá mong đợi của tôi. Tôi chắc chắn sẽ là khách hàng quay lại!'
    }
];

// Social Media Links
export const socialMedia = [
    {
        src: facebook,
        alt: 'facebook logo',
        name: 'Facebook',
        url: 'https://facebook.com/yourstore'
    },
    {
        src: twitter,
        alt: 'twitter logo',
        name: 'Twitter',
        url: 'https://twitter.com/yourstore'
    },
    {
        src: instagram,
        alt: 'instagram logo',
        name: 'Instagram',
        url: 'https://instagram.com/yourstore'
    }
];

// Footer Links
export const footerLinks = [
    {
        title: 'Sản Phẩm',
        links: [
            { name: 'Air Force 1', link: '/products?brand=nike&model=air-force-1' },
            { name: 'Air Max 1', link: '/products?brand=nike&model=air-max-1' },
            { name: 'Air Jordan 1', link: '/products?brand=nike&model=air-jordan-1' },
            { name: 'Air Force 2', link: '/products?brand=nike&model=air-force-2' },
            { name: 'Nike Waffle Racer', link: '/products?brand=nike&model=waffle-racer' },
            { name: 'Nike Cortez', link: '/products?brand=nike&model=cortez' }
        ]
    },
    {
        title: 'Giúp đỡ',
        links: [
            { name: 'Về chúng tôi', link: '/gioithieu', route: 'gioi-thieu' },
            { name: 'FAQs', link: '/gioithieu#faq' },
            { name: 'Hướng dẫn mua hàng', link: '/gioithieu#huong-dan' },
            { name: 'Chính sách đổi trả', link: '/gioithieu#doi-tra' },
            { name: 'Chính sách bảo hành', link: '/gioithieu#bao-hanh' }
        ]
    },
    {
        title: 'Liên hệ',
        links: [
            { name: 'customer@nike.com', link: 'mailto:customer@nike.com', external: true },
            { name: '+92554862354', link: 'tel:+92554862354', external: true },
            { name: 'Tìm cửa hàng', link: '/lienhe#stores', route: 'lien-he' },
            { name: 'Hỗ trợ trực tuyến', link: '/lienhe#support', route: 'lien-he' }
        ]
    }
];

// Product Categories - Dựa trên API endpoint
export const productCategories = [
    { id: 1, name: 'Giày Thể Thao', slug: 'giay-the-thao', icon: 'running' },
    { id: 2, name: 'Giày Sneaker', slug: 'giay-sneaker', icon: 'sneaker' },
    { id: 3, name: 'Giày Chạy Bộ', slug: 'giay-chay-bo', icon: 'run' },
    { id: 4, name: 'Giày Bóng Rổ', slug: 'giay-bong-ro', icon: 'basketball' },
    { id: 5, name: 'Giày Lifestyle', slug: 'giay-lifestyle', icon: 'lifestyle' }
];

// Brands - Thương hiệu
export const brands = [
    { id: 1, name: 'Nike', slug: 'nike', logo: '/brands/nike.png' },
    { id: 2, name: 'Adidas', slug: 'adidas', logo: '/brands/adidas.png' },
    { id: 3, name: 'Puma', slug: 'puma', logo: '/brands/puma.png' },
    { id: 4, name: 'New Balance', slug: 'new-balance', logo: '/brands/nb.png' },
    { id: 5, name: 'Converse', slug: 'converse', logo: '/brands/converse.png' }
];

// Size Chart - Bảng kích cỡ
export const sizeChart = {
    men: [
        { vn: 38, us: 6, eu: 39, cm: 24 },
        { vn: 39, us: 7, eu: 40, cm: 25 },
        { vn: 40, us: 8, eu: 41, cm: 26 },
        { vn: 41, us: 9, eu: 42, cm: 27 },
        { vn: 42, us: 10, eu: 43, cm: 28 },
        { vn: 43, us: 11, eu: 44, cm: 29 },
        { vn: 44, us: 12, eu: 45, cm: 30 }
    ],
    women: [
        { vn: 35, us: 5, eu: 36, cm: 22 },
        { vn: 36, us: 6, eu: 37, cm: 23 },
        { vn: 37, us: 7, eu: 38, cm: 24 },
        { vn: 38, us: 8, eu: 39, cm: 25 },
        { vn: 39, us: 9, eu: 40, cm: 26 },
        { vn: 40, us: 10, eu: 41, cm: 27 }
    ]
};

// Colors - Màu sắc phổ biến
export const colors = [
    { id: 1, name: 'Đen', hex: '#000000', slug: 'den' },
    { id: 2, name: 'Trắng', hex: '#FFFFFF', slug: 'trang' },
    { id: 3, name: 'Xám', hex: '#808080', slug: 'xam' },
    { id: 4, name: 'Đỏ', hex: '#FF0000', slug: 'do' },
    { id: 5, name: 'Xanh dương', hex: '#0000FF', slug: 'xanh-duong' },
    { id: 6, name: 'Xanh lá', hex: '#00FF00', slug: 'xanh-la' },
    { id: 7, name: 'Vàng', hex: '#FFFF00', slug: 'vang' },
    { id: 8, name: 'Cam', hex: '#FFA500', slug: 'cam' },
    { id: 9, name: 'Tím', hex: '#800080', slug: 'tim' },
    { id: 10, name: 'Hồng', hex: '#FFC0CB', slug: 'hong' }
];

// Price Ranges - Khoảng giá
export const priceRanges = [
    { id: 1, label: 'Dưới 1 triệu', min: 0, max: 1000000 },
    { id: 2, label: '1 - 2 triệu', min: 1000000, max: 2000000 },
    { id: 3, label: '2 - 3 triệu', min: 2000000, max: 3000000 },
    { id: 4, label: '3 - 5 triệu', min: 3000000, max: 5000000 },
    { id: 5, label: 'Trên 5 triệu', min: 5000000, max: null }
];

// Sort Options - Tùy chọn sắp xếp
export const sortOptions = [
    { value: 'newest', label: 'Mới nhất' },
    { value: 'price-asc', label: 'Giá: Thấp đến Cao' },
    { value: 'price-desc', label: 'Giá: Cao đến Thấp' },
    { value: 'name-asc', label: 'Tên: A-Z' },
    { value: 'name-desc', label: 'Tên: Z-A' },
    { value: 'popular', label: 'Phổ biến nhất' },
    { value: 'rating', label: 'Đánh giá cao nhất' }
];

// Order Status - Trạng thái đơn hàng
export const orderStatus = {
    PENDING: { value: 'PENDING', label: 'Chờ xác nhận', color: 'warning' },
    CONFIRMED: { value: 'CONFIRMED', label: 'Đã xác nhận', color: 'info' },
    PROCESSING: { value: 'PROCESSING', label: 'Đang xử lý', color: 'info' },
    SHIPPING: { value: 'SHIPPING', label: 'Đang giao hàng', color: 'primary' },
    DELIVERED: { value: 'DELIVERED', label: 'Đã giao hàng', color: 'success' },
    CANCELLED: { value: 'CANCELLED', label: 'Đã hủy', color: 'danger' },
    RETURNED: { value: 'RETURNED', label: 'Đã trả hàng', color: 'secondary' }
};

// Payment Methods - Phương thức thanh toán
export const paymentMethods = [
    { id: 'cod', name: 'Thanh toán khi nhận hàng (COD)', icon: 'cash' },
    { id: 'bank', name: 'Chuyển khoản ngân hàng', icon: 'bank' },
    { id: 'vnpay', name: 'VNPAY', icon: 'vnpay' },
    { id: 'momo', name: 'Ví MoMo', icon: 'momo' },
    { id: 'zalopay', name: 'ZaloPay', icon: 'zalopay' }
];

// API Endpoints
export const API_ENDPOINTS = {
    BASE_URL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',

    // Auth
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    LOGOUT: '/auth/logout',
    REFRESH_TOKEN: '/auth/refresh',

    // Products
    PRODUCTS: '/san-pham',
    PRODUCT_DETAILS: '/san-pham-chi-tiet',

    // Categories & Attributes
    CATEGORIES: '/danh-muc',
    BRANDS: '/thuong-hieu',
    COLORS: '/mau-sac',
    SIZES: '/kich-co',
    MATERIALS: '/chat-lieu',
    SOLES: '/de-giay',

    // Orders
    ORDERS: '/hoa-don',
    ORDER_DETAILS: '/hoa-don-chi-tiet',

    // Cart
    CART: '/gio-hang',
    CART_DETAILS: '/gio-hang-chi-tiet',

    // Users
    CUSTOMERS: '/khach-hang',
    STAFF: '/nhan-vien',

    // Promotions
    VOUCHERS: '/voucher',
    PROMOTIONS: '/khuyen-mai',

    // Others
    IMAGES: '/hinh-anh',
    NOTIFICATIONS: '/thong-bao'
};

// Validation Rules
export const VALIDATION_RULES = {
    email: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
    phone: /^(0[3|5|7|8|9])+([0-9]{8})$/,
    password: {
        minLength: 6,
        pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{6,}$/,
        message: 'Mật khẩu phải có ít nhất 6 ký tự, bao gồm chữ và số'
    },
    username: {
        minLength: 3,
        maxLength: 20,
        pattern: /^[a-zA-Z0-9_]+$/,
        message: 'Tên đăng nhập chỉ được chứa chữ cái, số và dấu gạch dưới'
    }
};

// Format utilities
export const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
};

export const formatDate = (date) => {
    return new Date(date).toLocaleDateString('vi-VN');
};

export const formatDateTime = (date) => {
    return new Date(date).toLocaleString('vi-VN');
};
