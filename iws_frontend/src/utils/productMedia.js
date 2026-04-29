const API_BASE_URL = 'http://localhost:8080';

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
            : image?.fullUrl || image?.duongDan || image?.url || image?.path || image?.src || '';

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

    const cleanPath = trimmedValue
        .replace(/^\/+/, '')
        .replace(/^hinh-anh\/images\//, '')
        .replace(/^images\//, '');

    return `${API_BASE_URL}/hinh-anh/images/${cleanPath}`;
};
