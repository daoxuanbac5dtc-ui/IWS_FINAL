import { dashboardAPI } from '@/services/dashboardAPI';
import { computed, ref } from 'vue';

export function useDashboardAPI() {
    // Reactive state
    const dashboardData = ref(null);
    const loading = ref(false);
    const error = ref(null);
    const lastUpdated = ref(null);
    const apiStatus = ref('checking');

    // Computed
    const isLoading = computed(() => loading.value);
    const hasError = computed(() => error.value !== null);
    const hasData = computed(() => dashboardData.value !== null);

    // Enhanced fetch dashboard data with connection testing
    const fetchDashboardData = async (period = '30days') => {
        try {
            loading.value = true;
            error.value = null;

            console.log('🔄 Starting dashboard data fetch...');

            // First, test the connection
            const isHealthy = await dashboardAPI.healthCheck();
            if (!isHealthy) {
                throw new Error('Backend health check failed');
            }

            // If healthy, fetch the data
            const data = await dashboardAPI.getDashboardStats(period);

            // Success!
            dashboardData.value = data;
            lastUpdated.value = new Date();
            apiStatus.value = 'connected';

            console.log('✅ Dashboard data loaded successfully!');
        } catch (err) {
            console.error('❌ Dashboard data fetch failed:', err);

            error.value = err.message || 'Failed to load dashboard data';
            apiStatus.value = 'error';

            // Fallback to mock data if no data exists
            if (!dashboardData.value) {
                console.log('📝 Using fallback mock data');
                dashboardData.value = getMockData();
            }
        } finally {
            loading.value = false;
        }
    };

    const fetchDashboardDataByRange = async (startDate, endDate) => {
        try {
            loading.value = true;
            error.value = null;

            const data = await dashboardAPI.getDashboardStatsByRange(startDate, endDate);
            dashboardData.value = data;
            lastUpdated.value = new Date();
            apiStatus.value = 'connected';
        } catch (err) {
            error.value = err.message || 'Failed to load dashboard data by range';
            apiStatus.value = 'error';
            throw err;
        } finally {
            loading.value = false;
        }
    };

    const checkApiHealth = async () => {
        try {
            const isHealthy = await dashboardAPI.healthCheck();
            apiStatus.value = isHealthy ? 'connected' : 'disconnected';
            return isHealthy;
        } catch (err) {
            apiStatus.value = 'disconnected';
            return false;
        }
    };

    // Test all endpoints
    const runDiagnostics = async () => {
        console.log('🔬 Running API diagnostics...');

        const results = {
            health: false,
            test: false,
            stats: false
        };

        try {
            // Test health endpoint
            results.health = await dashboardAPI.healthCheck();
            console.log('Health check:', results.health ? '✅' : '❌');

            // Test connection endpoint
            results.test = await dashboardAPI.testConnection();
            console.log('Test endpoint:', results.test ? '✅' : '❌');

            // Test stats endpoint
            try {
                await dashboardAPI.getDashboardStats('7days');
                results.stats = true;
                console.log('Stats endpoint: ✅');
            } catch (e) {
                results.stats = false;
                console.log('Stats endpoint: ❌', e.message);
            }
        } catch (error) {
            console.error('Diagnostics failed:', error);
        }

        console.log('🔬 Diagnostics complete:', results);
        return results;
    };

    const getMockData = () => ({
        totalStats: {
            totalRevenue: 125000000,
            totalOrders: 1250,
            totalCustomers: 890,
            totalProducts: 156,
            avgOrderValue: 100000,
            conversionRate: 3.2,
            returnRate: 2.1,
            customerRetention: 68.5
        },
        revenueData: [
            { date: '2024-01-01', revenue: 8500000, orders: 85, customers: 65 },
            { date: '2024-01-08', revenue: 12000000, orders: 120, customers: 95 },
            { date: '2024-01-15', revenue: 15500000, orders: 155, customers: 118 },
            { date: '2024-01-22', revenue: 18200000, orders: 182, customers: 142 },
            { date: '2024-01-29', revenue: 22100000, orders: 221, customers: 168 }
        ],
        topProducts: [
            { productName: 'Nike Air Force 1', brandName: 'Nike', soldQuantity: 245, revenue: 49000000, stock: 45 },
            { productName: 'Adidas Ultraboost', brandName: 'Adidas', soldQuantity: 198, revenue: 39600000, stock: 32 },
            { productName: 'Converse Chuck Taylor', brandName: 'Converse', soldQuantity: 156, revenue: 23400000, stock: 67 },
            { productName: 'Vans Old Skool', brandName: 'Vans', soldQuantity: 134, revenue: 20100000, stock: 28 },
            { productName: 'Puma Suede Classic', brandName: 'Puma', soldQuantity: 112, revenue: 16800000, stock: 54 }
        ],
        brandAnalysis: [
            { brandName: 'Nike', percentage: 35, revenue: 43750000, productCount: 25 },
            { brandName: 'Adidas', percentage: 28, revenue: 35000000, productCount: 20 },
            { brandName: 'Converse', percentage: 15, revenue: 18750000, productCount: 12 },
            { brandName: 'Vans', percentage: 12, revenue: 15000000, productCount: 10 },
            { brandName: 'Puma', percentage: 10, revenue: 12500000, productCount: 8 }
        ],
        orderStatus: [
            { status: 'Chờ xác nhận', count: 45, percentage: 18 },
            { status: 'Đã xác nhận', count: 89, percentage: 36 },
            { status: 'Đang giao', count: 67, percentage: 27 },
            { status: 'Hoàn thành', count: 234, percentage: 94 },
            { status: 'Đã hủy', count: 12, percentage: 5 }
        ],
        customerGrowth: [
            { month: 'T7', newCustomers: 45, returningCustomers: 78 },
            { month: 'T8', newCustomers: 52, returningCustomers: 89 },
            { month: 'T9', newCustomers: 48, returningCustomers: 94 },
            { month: 'T10', newCustomers: 61, returningCustomers: 102 },
            { month: 'T11', newCustomers: 58, returningCustomers: 115 },
            { month: 'T12', newCustomers: 72, returningCustomers: 128 }
        ],
        sizeAnalysis: [
            { size: '38', count: 45 },
            { size: '39', count: 78 },
            { size: '40', count: 125 },
            { size: '41', count: 156 },
            { size: '42', count: 134 },
            { size: '43', count: 98 },
            { size: '44', count: 67 },
            { size: '45', count: 34 }
        ]
    });

    return {
        dashboardData,
        loading,
        error,
        lastUpdated,
        apiStatus,
        isLoading,
        hasError,
        hasData,
        fetchDashboardData,
        fetchDashboardDataByRange,
        checkApiHealth,
        runDiagnostics
    };
}
