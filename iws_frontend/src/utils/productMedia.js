const API_BASE_URL = import.meta.env?.VITE_API_URL || `${import.meta.env.VITE_API_BASE_URL}`;
const FRONTEND_PUBLIC_PREFIXES = ['/placeholder', '/favicon', '/assets/'];
const BACKEND_DIRECT_PREFIXES = ['/uploads/', '/product-images/', '/static/', '/voucher/images/', '/return-images/'];

const escapeSvgText = (value = '') =>
    String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');

export const createSvgPlaceholder = ({ width = 240, height = 160, label = 'Shoe Image' } = {}) => {
    const safeLabel = escapeSvgText(label);
    const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}"><rect width="${width}" height="${height}" fill="#f3f4f6"/><text x="50%" y="50%" fill="#6b7280" font-family="Arial, sans-serif" font-size="14" text-anchor="middle" dominant-baseline="middle">${safeLabel}</text></svg>`;
    return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`;
};

export const resolveProductImageUrl = (image) => {
    const rawValue =
        typeof image === 'string'
            ? image
            : image?.fullUrl || image?.urlHinhAnh || image?.duongDan || image?.url || image?.path || image?.src || '';

    if (!rawValue || typeof rawValue !== 'string') {
        return null;
    }

    const trimmedValue = rawValue.trim();
    if (!trimmedValue || trimmedValue === 'null' || trimmedValue === 'undefined' || trimmedValue.includes('null')) {
        return null;
    }

    if (trimmedValue.startsWith('data:') || trimmedValue.startsWith('blob:')) {
        return trimmedValue;
    }

    if (trimmedValue.startsWith('http://') || trimmedValue.startsWith('https://')) {
        if (trimmedValue.startsWith(`${API_BASE_URL}/images/`)) {
            const cleanPath = trimmedValue.slice(`${API_BASE_URL}/images/`.length);
            return `${API_BASE_URL}/hinh-anh/images/${cleanPath}`;
        }
        return trimmedValue;
    }

    if (FRONTEND_PUBLIC_PREFIXES.some((prefix) => trimmedValue.startsWith(prefix))) {
        return trimmedValue;
    }

    if (BACKEND_DIRECT_PREFIXES.some((prefix) => trimmedValue.startsWith(prefix))) {
        return `${API_BASE_URL}${trimmedValue}`;
    }

    if (BACKEND_DIRECT_PREFIXES.some((prefix) => trimmedValue.startsWith(prefix.replace(/^\//, '')))) {
        return `${API_BASE_URL}/${trimmedValue}`;
    }

    const cleanPath = trimmedValue
        .replace(/^\/+/, '')
        .replace(/^hinh-anh\/images\//, '')
        .replace(/^images\//, '');

    return `${API_BASE_URL}/hinh-anh/images/${cleanPath}`;
};

const normalizeProductText = (value = '') =>
    String(value)
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/đ/g, 'd')
        .replace(/Đ/g, 'D')
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, ' ')
        .trim();

const getCodeSuffix = (value = '') => {
    const match = String(value).match(/(\d+)$/);
    return match ? match[1] : '';
};

const getProductText = (product = {}) =>
    normalizeProductText(
        [
            product.tenSanPham,
            product.name,
            product.label,
            product.thuongHieu?.tenThuongHieu,
            product.brand,
            product.brandName
        ]
            .filter(Boolean)
            .join(' ')
    );

const getImageText = (image = {}) =>
    normalizeProductText([image.tenHinhAnh, image.duongDan, image.path, image.url].filter(Boolean).join(' '));

export const createProductImageLookup = (images = []) => {
    const byId = new Map();
    const byCodeSuffix = new Map();
    const searchableImages = [];

    images.forEach((image) => {
        const imageUrl = resolveProductImageUrl(image);
        if (!imageUrl) return;

        if (image?.id !== undefined && image?.id !== null) {
            byId.set(String(image.id), imageUrl);
        }

        const normalizedText = getImageText(image);
        const tokens = new Set(normalizedText.split(' ').filter((token) => token.length > 1));
        const searchableImage = { imageUrl, normalizedText, tokens };
        searchableImages.push(searchableImage);

        const codeSuffix = getCodeSuffix(image?.maHinhAnh);
        if (codeSuffix && !byCodeSuffix.has(codeSuffix)) {
            byCodeSuffix.set(codeSuffix, searchableImage);
        }
    });

    const findById = (id) => {
        if (id === undefined || id === null) return null;
        return byId.get(String(id)) || null;
    };

    const findForProduct = (product = {}) => {
        const directImageUrl = resolveProductImageUrl(product.hinhAnh || product.imgUrl || product.imgURL);
        if (directImageUrl) {
            return directImageUrl;
        }

        const productText = getProductText(product);
        const productCodeSuffix = getCodeSuffix(product.maSanPham || product.code || product.ma);

        if (!productText) {
            return byCodeSuffix.get(productCodeSuffix)?.imageUrl || null;
        }

        const productTokens = productText.split(' ').filter((token) => token.length > 1);
        if (!productTokens.length) {
            return byCodeSuffix.get(productCodeSuffix)?.imageUrl || null;
        }

        let bestMatch = null;
        let bestScore = 0;

        searchableImages.forEach((image) => {
            let score = productTokens.reduce((total, token) => total + (image.tokens.has(token) ? 1 : 0), 0);

            if (image.normalizedText.includes(productText) || productText.includes(image.normalizedText)) {
                score += 3;
            }

            if (score > bestScore) {
                bestScore = score;
                bestMatch = image.imageUrl;
            }
        });

        if (bestScore >= Math.min(2, productTokens.length)) {
            return bestMatch;
        }

        return byCodeSuffix.get(productCodeSuffix)?.imageUrl || null;
    };

    return {
        findById,
        findForProduct
    };
};

