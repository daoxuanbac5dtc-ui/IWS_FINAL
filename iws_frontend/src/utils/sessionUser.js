export function getStoredUserInfo() {
    const rawUserInfo = localStorage.getItem('user_info');
    if (!rawUserInfo) return null;

    try {
        return JSON.parse(rawUserInfo);
    } catch (error) {
        console.warn('Failed to read user_info from localStorage:', error);
        return null;
    }
}

export function getUserDisplayName(user = {}) {
    return user?.hoTen || user?.tenKhachHang || user?.name || user?.tenNhanVien || '';
}

export function getUserEmail(user = {}) {
    return user?.taiKhoan?.email || user?.email || user?.khachHang?.email || '';
}

export function getUserPhone(user = {}) {
    return user?.sdt || user?.dienThoai || user?.phone || '';
}

export function getUserAccountId(user = {}) {
    return user?.taiKhoan?.id || user?.idTaiKhoan || user?.taiKhoanId || user?.id || null;
}

export function mergeUserInfo(...sources) {
    const merged = {};

    for (const source of sources) {
        if (source && typeof source === 'object') {
            Object.assign(merged, source);
        }
    }

    const displayName = getUserDisplayName(merged);
    const email = getUserEmail(merged);
    const phone = getUserPhone(merged);

    if (displayName) {
        merged.hoTen = displayName;
    }

    if (email) {
        merged.email = email;
    }

    if (phone) {
        merged.sdt = phone;
    }

    return merged;
}

export function hasBasicCachedUser(user = {}) {
    return Boolean(getUserAccountId(user) && getUserDisplayName(user) && getUserEmail(user));
}
