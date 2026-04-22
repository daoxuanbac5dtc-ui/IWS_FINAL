import axios from 'axios';

// API Configuration
const API_BASE_URL = 'http://localhost:8080/api/dashboard';

console.log('🔧 API Configuration:');
console.log('- Base URL:', API_BASE_URL);
console.log('- Frontend URL:', window.location.origin);

// Create axios instance with optimized config
const api = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json'
    },
    withCredentials: false // Important for CORS
});

// Request interceptor
api.interceptors.request.use(
    (config) => {
        console.log(`🚀 Making request: ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`);
        return config;
    },
    (error) => {
        console.error('❌ Request setup error:', error);
        return Promise.reject(error);
    }
);

// Response interceptor with detailed error info
api.interceptors.response.use(
    (response) => {
        console.log(`✅ API Success: ${response.status} - ${response.config.url}`);
        console.log('Response data:', response.data);
        return response;
    },
    (error) => {
        console.error('❌ API Error occurred:');
        console.error('- Error message:', error.message);
        console.error('- Error code:', error.code);

        if (error.response) {
            // Server responded with error
            console.error('- Response status:', error.response.status);
            console.error('- Response data:', error.response.data);
        } else if (error.request) {
            // Request made but no response
            console.error('- No response received');
            console.error('- Request details:', error.request);
        }

        return Promise.reject(error);
    }
);

export const dashboardAPI = {
    // Enhanced health check
    async healthCheck() {
        try {
            console.log('🏥 Checking API health...');
            const response = await api.get('/health');
            console.log('✅ Health check passed:', response.data);
            return true;
        } catch (error) {
            console.error('❌ Health check failed:', error.message);
            return false;
        }
    },

    // Enhanced get dashboard stats
    async getDashboardStats(period = '30days') {
        try {
            console.log(`📊 Fetching dashboard stats for period: ${period}`);

            const response = await api.get('/stats', {
                params: { period },
                timeout: 15000 // Increase timeout for this request
            });

            console.log('✅ Dashboard stats received successfully');
            return response.data;
        } catch (error) {
            console.error('❌ Failed to fetch dashboard stats:', error);

            // Create detailed error message
            let errorMessage = 'Unknown error occurred';

            if (error.code === 'ECONNREFUSED') {
                errorMessage = 'Cannot connect to backend server. Please ensure Spring Boot is running on port 8080.';
            } else if (error.code === 'ENOTFOUND') {
                errorMessage = 'Backend server not found. Check if localhost:8080 is accessible.';
            } else if (error.code === 'ECONNABORTED') {
                errorMessage = 'Request timeout. Backend server is taking too long to respond.';
            } else if (error.response) {
                errorMessage = `Server error: ${error.response.status} ${error.response.statusText}`;
            } else if (error.request) {
                errorMessage = 'No response from server. Check network connection and CORS configuration.';
            }

            throw new Error(errorMessage);
        }
    },

    // Enhanced get dashboard stats by range
    async getDashboardStatsByRange(startDate, endDate) {
        try {
            console.log(`📅 Fetching dashboard stats for range: ${startDate} to ${endDate}`);

            const response = await api.get('/stats/range', {
                params: { startDate, endDate }
            });

            console.log('✅ Dashboard stats by range received successfully');
            return response.data;
        } catch (error) {
            console.error('❌ Failed to fetch dashboard stats by range:', error);
            throw new Error(`Error fetching stats by date range: ${error.message}`);
        }
    },

    // Test endpoint
    async testConnection() {
        try {
            console.log('🧪 Testing connection...');
            const response = await api.get('/test');
            console.log('✅ Test connection successful:', response.data);
            return true;
        } catch (error) {
            console.error('❌ Test connection failed:', error);
            return false;
        }
    }
};
