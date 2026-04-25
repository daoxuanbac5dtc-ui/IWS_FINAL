// Password hashing utility
// Note: In production, this should be done on the backend for security
// This is a temporary solution for development

/**
 * Simple password hashing (for development only)
 * In production, use bcrypt or similar on the backend
 */
export const hashPassword = (password) => {
    if (!password) return null;
    
    // Simple hash for development (NOT SECURE for production)
    // In production, this should be handled by the backend
    let hash = 0;
    for (let i = 0; i < password.length; i++) {
        const char = password.charCodeAt(i);
        hash = ((hash << 5) - hash) + char;
        hash = hash & hash; // Convert to 32bit integer
    }
    
    // Add some salt-like behavior
    const salt = 'sneaker_store_salt_2024';
    const saltedPassword = password + salt;
    
    let saltedHash = 0;
    for (let i = 0; i < saltedPassword.length; i++) {
        const char = saltedPassword.charCodeAt(i);
        saltedHash = ((saltedHash << 5) - saltedHash) + char;
        saltedHash = saltedHash & saltedHash;
    }
    
    return Math.abs(saltedHash).toString(16);
};

/**
 * Check if password needs to be hashed
 */
export const shouldHashPassword = (password) => {
    // Check if password looks like it's already hashed (hex string)
    return password && !/^[0-9a-f]+$/i.test(password);
};

/**
 * Prepare password for API call
 */
export const preparePasswordForAPI = (password) => {
    if (!password) return null;
    
    // If password looks like plain text, we'll let backend handle hashing
    // Backend should hash the password before storing
    return password;
};

