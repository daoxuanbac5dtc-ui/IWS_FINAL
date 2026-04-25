<template>
    <div class="container-fluid">
        <!-- Toast thông báo -->
        <div class="toast-container position-fixed end-0 top-0 p-3">
            <div v-for="toast in toasts" :key="toast.id" class="toast show" :class="getToastClass(toast.type)">
                <div class="toast-header">
                    <strong class="me-auto">{{ getToastTitle(toast.type) }}</strong>
                    <button type="button" class="btn-close" @click="removeToast(toast.id)"></button>
                </div>
                <div class="toast-body">{{ toast.message }}</div>
            </div>
        </div>

        <!-- Header -->
        <div class="row mb-4">
            <div class="col">
                <h2><i class="bi bi-box-seam me-2"></i>Quản lý sản phẩm</h2>
            </div>
        </div>

        <!-- Bộ lọc -->
        <div class="filter-section bg-light mb-4 rounded p-3">
            <div class="row g-3">
                <div class="col-md-3">
                    <label class="form-label">Tìm kiếm</label>
                    <input type="text" class="form-control" v-model="filters.search" @input="debouncedFilter" placeholder="Tên sản phẩm, mã sản phẩm..." />
                </div>
                <div class="col-md-2">
                    <label class="form-label">Danh mục</label>
                    <select class="form-select" v-model="filters.category" @change="applyFilters">
                        <option value="">Tất cả</option>
                        <option v-for="cat in danhMucs" :key="cat.id" :value="cat.id">
                            {{ cat.tenDanhMuc }}
                        </option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label">Thương hiệu</label>
                    <select class="form-select" v-model="filters.brand" @change="applyFilters">
                        <option value="">Tất cả</option>
                        <option v-for="brand in thuongHieus" :key="brand.id" :value="brand.id">
                            {{ brand.tenThuongHieu }}
                        </option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label">Trạng thái</label>
                    <select class="form-select" v-model="filters.status" @change="applyFilters">
                        <option value="">Tất cả</option>
                        <option value="1">Hoạt động</option>
                        <option value="0">Ngừng hoạt động</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label">Sắp xếp</label>
                    <select class="form-select" v-model="filters.sort" @change="applyFilters">
                        <option value="name_asc">Tên A-Z</option>
                        <option value="name_desc">Tên Z-A</option>
                        <option value="created_desc">Mới nhất</option>
                        <option value="created_asc">Cũ nhất</option>
                        <option value="quantity_desc">Số lượng cao</option>
                        <option value="quantity_asc">Số lượng thấp</option>
                    </select>
                </div>
                <div class="col-md-1 d-flex align-items-end">
                    <button class="btn btn-outline-primary w-100" @click="resetFilters">
                        <i class="bi bi-arrow-clockwise"></i>
                    </button>
                </div>
            </div>
        </div>

        <!-- Toolbar -->
        <div class="row mb-3">
            <div class="col-md-6">
                <button class="btn btn-primary me-2" @click="openProductModal()"><i class="bi bi-plus-lg me-1"></i>Thêm sản phẩm</button>
                <button class="btn btn-success me-2" @click="exportData()"><i class="bi bi-download me-1"></i>Xuất Excel</button>
                <button class="btn btn-info" @click="refreshData()"><i class="bi bi-arrow-clockwise me-1"></i>Làm mới</button>
            </div>
            <div class="col-md-6 text-end">
                <span class="text-muted"> Hiển thị {{ paginatedProducts.length }} / {{ filteredProducts.length }} sản phẩm </span>
            </div>
        </div>

        <!-- Loading Spinner -->
        <div v-if="loading" class="my-5 text-center">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Đang tải...</span>
            </div>
            <p class="mt-2">Đang tải dữ liệu...</p>
        </div>

        <!-- Danh sách sản phẩm -->
        <div v-else class="card">
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table-hover table">
                        <thead class="table-light">
                            <tr>
                                <th width="5%">#</th>
                                <th width="12%">Mã sản phẩm</th>
                                <th width="20%">Tên sản phẩm</th>
                                <th width="8%">Tổng SL</th>
                                <th width="8%">Chi tiết</th>
                                <th width="12%">Danh mục</th>
                                <th width="12%">Thương hiệu</th>
                                <th width="10%">Hình ảnh</th>
                                <th width="8%">Trạng thái</th>
                                <th width="10%">Ngày tạo</th>
                                <th width="5%">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(product, index) in paginatedProducts" :key="product.id">
                                <td>{{ (currentPage - 1) * itemsPerPage + index + 1 }}</td>
                                <td>
                                    <code class="text-primary">{{ product.maSanPham }}</code>
                                </td>
                                <td>
                                    <div class="fw-semibold">{{ product.tenSanPham }}</div>
                                    <div class="small text-muted">{{ product.material }} - {{ product.sole }}</div>
                                </td>
                                <td>
                                    <span class="badge" :class="getQuantityBadgeClass(getTotalQuantity(product))">
                                        {{ getTotalQuantity(product) }}
                                    </span>
                                </td>
                                <td>
                                    <span class="badge bg-info">{{ getDetailCount(product) }}</span>
                                </td>
                                <td>{{ product.category }}</td>
                                <td>{{ product.brand }}</td>
                                <td>
                                    <div v-if="getProductImages(product).length > 0" class="d-flex gap-1">
                                        <img
                                            v-for="(img, imgIndex) in getProductImages(product).slice(0, 2)"
                                            :key="imgIndex"
                                            :src="img.url"
                                            class="product-table-image"
                                            @click="showImageModal(img)"
                                            @error="handleImageError($event)"
                                            alt="Product image"
                                        />
                                        <div
                                            v-if="getProductImages(product).length > 2"
                                            class="product-table-image d-flex align-items-center justify-content-center bg-light text-muted small rounded border"
                                            @click="showProductDetail(product)"
                                            style="cursor: pointer"
                                        >
                                            +{{ getProductImages(product).length - 2 }}
                                        </div>
                                    </div>
                                    <span v-else class="text-muted small">Chưa có ảnh</span>
                                </td>
                                <td>
                                    <span class="badge" :class="getStatusBadgeClass(product.trangThai)">
                                        {{ product.trangThai === 1 ? 'Hoạt động' : 'Ngừng' }}
                                    </span>
                                </td>
                                <td>
                                    <div class="small">{{ product.createdAt }}</div>
                                </td>
                                <td>
                                    <div class="btn-group btn-group-sm" role="group">
                                        <button type="button" class="btn btn-outline-info" @click="showProductDetail(product)" title="Xem chi tiết">
                                            <i class="bi bi-eye"></i>
                                        </button>
                                        <button type="button" class="btn btn-outline-warning" @click="editProduct(product)" title="Chỉnh sửa">
                                            <i class="bi bi-pencil"></i>
                                        </button>
                                        <button type="button" class="btn btn-outline-danger" @click="confirmDeleteProduct(product)" title="Xóa">
                                            <i class="bi bi-trash"></i>
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>

                    <!-- Empty state -->
                    <div v-if="!paginatedProducts.length" class="py-5 text-center">
                        <i class="bi bi-box-seam display-1 text-muted mb-3"></i>
                        <h5 class="text-muted">Không tìm thấy sản phẩm nào</h5>
                        <p class="text-muted">Thử thay đổi bộ lọc hoặc thêm sản phẩm mới</p>
                        <button class="btn btn-primary" @click="openProductModal()"><i class="bi bi-plus-lg me-1"></i>Thêm sản phẩm đầu tiên</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Pagination -->
        <nav v-if="totalPages > 1" class="mt-4">
            <ul class="pagination justify-content-center">
                <li class="page-item" :class="{ disabled: currentPage === 1 }">
                    <button class="page-link" @click="changePage(currentPage - 1)">Trước</button>
                </li>
                <li v-for="page in getVisiblePages()" :key="page" class="page-item" :class="{ active: page === currentPage }">
                    <button class="page-link" @click="changePage(page)">{{ page }}</button>
                </li>
                <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                    <button class="page-link" @click="changePage(currentPage + 1)">Sau</button>
                </li>
            </ul>
        </nav>

        <!-- Modal thêm/sửa sản phẩm -->
        <div class="modal fade" ref="productModal" tabindex="-1">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">{{ isEditMode ? 'Sửa sản phẩm' : 'Thêm sản phẩm' }}</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <form @submit.prevent="saveProduct">
                            <div class="row g-3">
                                <div class="col-md-8">
                                    <label class="form-label">Tên sản phẩm <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" v-model="product.tenSanPham" :class="{ 'is-invalid': errors.tenSanPham }" required />
                                    <div v-if="errors.tenSanPham" class="invalid-feedback">
                                        {{ errors.tenSanPham }}
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label">Mã sản phẩm</label>
                                    <input type="text" class="form-control" v-model="product.maSanPham" readonly />
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Danh mục <span class="text-danger">*</span></label>
                                    <select class="form-select" v-model="product.danhMuc" :class="{ 'is-invalid': errors.danhMuc }" required>
                                        <option value="">Chọn danh mục</option>
                                        <option v-for="cat in danhMucs" :key="cat.id" :value="cat">
                                            {{ cat.tenDanhMuc }}
                                        </option>
                                    </select>
                                    <div v-if="errors.danhMuc" class="invalid-feedback">
                                        {{ errors.danhMuc }}
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Thương hiệu <span class="text-danger">*</span></label>
                                    <select class="form-select" v-model="product.thuongHieu" :class="{ 'is-invalid': errors.thuongHieu }" required>
                                        <option value="">Chọn thương hiệu</option>
                                        <option v-for="brand in thuongHieus" :key="brand.id" :value="brand">
                                            {{ brand.tenThuongHieu }}
                                        </option>
                                    </select>
                                    <div v-if="errors.thuongHieu" class="invalid-feedback">
                                        {{ errors.thuongHieu }}
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Chất liệu <span class="text-danger">*</span></label>
                                    <select class="form-select" v-model="product.chatLieu" :class="{ 'is-invalid': errors.chatLieu }" required>
                                        <option value="">Chọn chất liệu</option>
                                        <option v-for="material in chatLieus" :key="material.id" :value="material">
                                            {{ material.tenChatLieu }}
                                        </option>
                                    </select>
                                    <div v-if="errors.chatLieu" class="invalid-feedback">
                                        {{ errors.chatLieu }}
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Đế giày <span class="text-danger">*</span></label>
                                    <select class="form-select" v-model="product.deGiay" :class="{ 'is-invalid': errors.deGiay }" required>
                                        <option value="">Chọn đế giày</option>
                                        <option v-for="sole in deGiays" :key="sole.id" :value="sole">
                                            {{ sole.tenDeGiay }}
                                        </option>
                                    </select>
                                    <div v-if="errors.deGiay" class="invalid-feedback">
                                        {{ errors.deGiay }}
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Số lượng</label>
                                    <input type="number" class="form-control" v-model.number="product.soLuong" min="0" />
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Trạng thái</label>
                                    <select class="form-select" v-model="product.trangThai">
                                        <option :value="1">Hoạt động</option>
                                        <option :value="0">Ngừng hoạt động</option>
                                    </select>
                                </div>
                            </div>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                        <button type="button" class="btn btn-primary" @click="saveProduct" :disabled="saving">
                            <span v-if="saving" class="spinner-border spinner-border-sm me-2"></span>
                            {{ isEditMode ? 'Cập nhật' : 'Thêm mới' }}
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Modal chi tiết sản phẩm -->
        <div class="modal fade" ref="detailModal" tabindex="-1">
            <div class="modal-dialog modal-xl">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Chi tiết sản phẩm: {{ selectedProduct?.tenSanPham }}</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <div v-if="selectedProduct">
                            <!-- Thông tin cơ bản -->
                            <div class="row mb-4">
                                <div class="col-md-6">
                                    <div class="card">
                                        <div class="card-header">
                                            <h6 class="mb-0">Thông tin sản phẩm</h6>
                                        </div>
                                        <div class="card-body">
                                            <table class="table-borderless table-sm table">
                                                <tbody>
                                                    <tr>
                                                        <td class="text-muted" width="40%">Mã sản phẩm:</td>
                                                        <td>
                                                            <strong>{{ selectedProduct.maSanPham }}</strong>
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <td class="text-muted">Tên sản phẩm:</td>
                                                        <td>
                                                            <strong>{{ selectedProduct.tenSanPham }}</strong>
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <td class="text-muted">Danh mục:</td>
                                                        <td>{{ selectedProduct.category }}</td>
                                                    </tr>
                                                    <tr>
                                                        <td class="text-muted">Thương hiệu:</td>
                                                        <td>{{ selectedProduct.brand }}</td>
                                                    </tr>
                                                    <tr>
                                                        <td class="text-muted">Chất liệu:</td>
                                                        <td>{{ selectedProduct.material }}</td>
                                                    </tr>
                                                    <tr>
                                                        <td class="text-muted">Đế giày:</td>
                                                        <td>{{ selectedProduct.sole }}</td>
                                                    </tr>
                                                    <tr>
                                                        <td class="text-muted">Tổng số lượng:</td>
                                                        <td>
                                                            <span class="badge bg-info">{{ getTotalQuantity(selectedProduct) }}</span>
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <td class="text-muted">Trạng thái:</td>
                                                        <td>
                                                            <span class="badge" :class="getStatusBadgeClass(selectedProduct.trangThai)">
                                                                {{ selectedProduct.trangThai === 1 ? 'Hoạt động' : 'Ngừng hoạt động' }}
                                                            </span>
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <td class="text-muted">Ngày tạo:</td>
                                                        <td>{{ selectedProduct.createdAt }}</td>
                                                    </tr>
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="card">
                                        <div class="card-header">
                                            <h6 class="mb-0">Thống kê</h6>
                                        </div>
                                        <div class="card-body">
                                            <div class="row text-center">
                                                <div class="col-4">
                                                    <div class="rounded bg-primary bg-opacity-10 p-3">
                                                        <div class="h4 mb-1 text-primary">{{ getDetailCount(selectedProduct) }}</div>
                                                        <div class="small text-muted">Biến thể</div>
                                                    </div>
                                                </div>
                                                <div class="col-4">
                                                    <div class="bg-success rounded bg-opacity-10 p-3">
                                                        <div class="h4 text-success mb-1">{{ getUniqueColors(selectedProduct) }}</div>
                                                        <div class="small text-muted">Màu sắc</div>
                                                    </div>
                                                </div>
                                                <div class="col-4">
                                                    <div class="bg-warning rounded bg-opacity-10 p-3">
                                                        <div class="h4 text-warning mb-1">{{ getUniqueSizes(selectedProduct) }}</div>
                                                        <div class="small text-muted">Kích cỡ</div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Chi tiết sản phẩm -->
                            <div class="card">
                                <div class="card-header d-flex justify-content-between align-items-center">
                                    <h6 class="mb-0">Chi tiết sản phẩm</h6>
                                    <button class="btn btn-primary btn-sm" @click="openDetailModal(selectedProduct.id)"><i class="bi bi-plus-lg"></i> Thêm chi tiết</button>
                                </div>
                                <div class="card-body">
                                    <div v-if="selectedProduct.details && selectedProduct.details.length" class="table-responsive">
                                        <table class="table-hover table">
                                            <thead class="table-light">
                                                <tr>
                                                    <th>Mã chi tiết</th>
                                                    <th>Kích cỡ</th>
                                                    <th>Màu sắc</th>
                                                    <th>Số lượng</th>
                                                    <th>Giá gốc</th>
                                                    <th>Giá bán</th>
                                                    <th>Hình ảnh</th>
                                                    <th>Trạng thái</th>
                                                    <th>Thao tác</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <tr v-for="detail in selectedProduct.details" :key="detail.id">
                                                    <td>
                                                        <code>{{ detail.maChiTiet }}</code>
                                                    </td>
                                                    <td>{{ detail.size }}</td>
                                                    <td>{{ detail.color }}</td>
                                                    <td>
                                                        <span class="badge" :class="getQuantityBadgeClass(detail.soLuong)">
                                                            {{ detail.soLuong }}
                                                        </span>
                                                    </td>
                                                    <td>{{ formatCurrency(detail.giaGoc) }}</td>
                                                    <td>{{ formatCurrency(detail.giaBan) }}</td>
                                                    <td>
                                                        <div v-if="detail.images && detail.images.length" class="d-flex gap-1">
                                                            <img
                                                                v-for="(img, index) in detail.images.slice(0, 2)"
                                                                :key="index"
                                                                :src="img.url"
                                                                class="detail-image-thumb"
                                                                @click="showImageModal(img)"
                                                                @error="handleImageError($event)"
                                                                alt="Detail image"
                                                            />
                                                            <div v-if="detail.images.length > 2" class="detail-image-thumb d-flex align-items-center justify-content-center bg-light text-muted small">+{{ detail.images.length - 2 }}</div>
                                                        </div>
                                                        <span v-else class="text-muted small">Chưa có ảnh</span>
                                                    </td>
                                                    <td>
                                                        <span class="badge" :class="getStatusBadgeClass(detail.trangThai)">
                                                            {{ detail.trangThai === 1 ? 'Hoạt động' : 'Ngừng' }}
                                                        </span>
                                                    </td>
                                                    <td>
                                                        <div class="btn-group btn-group-sm">
                                                            <button type="button" class="btn btn-outline-warning" @click="editDetail(detail, selectedProduct.id)">
                                                                <i class="bi bi-pencil"></i>
                                                            </button>
                                                            <button type="button" class="btn btn-outline-danger" @click="confirmDeleteDetail(detail)">
                                                                <i class="bi bi-trash"></i>
                                                            </button>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                    <div v-else class="py-4 text-center">
                                        <i class="bi bi-box-seam display-3 text-muted mb-3"></i>
                                        <p class="text-muted">Chưa có chi tiết sản phẩm nào</p>
                                        <button class="btn btn-primary" @click="openDetailModal(selectedProduct.id)"><i class="bi bi-plus-lg"></i> Thêm chi tiết đầu tiên</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Modal xem ảnh -->
        <div class="modal fade" ref="imageModal" tabindex="-1">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Hình ảnh sản phẩm</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body text-center">
                        <img v-if="selectedImage" :src="selectedImage.url" class="img-fluid rounded" :alt="selectedImage.tenHinhAnh" />
                        <div v-if="selectedImage" class="mt-3">
                            <h6>{{ selectedImage.maHinhAnh }}</h6>
                            <p class="text-muted">{{ selectedImage.tenHinhAnh }}</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Modal xác nhận xóa -->
        <div class="modal fade" ref="deleteModal" tabindex="-1">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Xác nhận xóa</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <div class="d-flex align-items-center">
                            <i class="bi bi-exclamation-triangle-fill text-warning fs-1 me-3"></i>
                            <div>
                                <p class="mb-2">{{ deleteMessage }}</p>
                                <small class="text-muted">Hành động này không thể hoàn tác.</small>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                        <button type="button" class="btn btn-danger" @click="confirmDelete" :disabled="deleting">
                            <span v-if="deleting" class="spinner-border spinner-border-sm me-2"></span>
                            Xóa
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import axios from 'axios';
import { computed, onMounted, ref } from 'vue';

// Cấu hình API
const API_BASE_URL = 'http://localhost:8080';

// Reactive data
const products = ref([]);
const filteredProducts = ref([]);
const selectedProduct = ref(null);
const selectedImage = ref(null);
const loading = ref(false);
const saving = ref(false);
const deleting = ref(false);
const isEditMode = ref(false);
const toasts = ref([]);

// Form data
const product = ref({
    tenSanPham: '',
    maSanPham: '',
    soLuong: 0,
    trangThai: 1,
    danhMuc: null,
    thuongHieu: null,
    chatLieu: null,
    deGiay: null
});

const errors = ref({});

// Filter data
const filters = ref({
    search: '',
    category: '',
    brand: '',
    status: '',
    sort: 'name_asc'
});

// Pagination
const currentPage = ref(1);
const itemsPerPage = ref(10); // Giảm từ 12 xuống 10 cho table

// Master data
const danhMucs = ref([]);
const thuongHieus = ref([]);
const chatLieus = ref([]);
const deGiays = ref([]);
const kichCos = ref([]);
const mauSacs = ref([]);

// Delete confirmation
const deleteAction = ref(null);
const deleteMessage = ref('');

// Modal refs
const productModal = ref(null);
const detailModal = ref(null);
const imageModal = ref(null);
const deleteModal = ref(null);

// Computed
const paginatedProducts = computed(() => {
    const start = (currentPage.value - 1) * itemsPerPage.value;
    const end = start + itemsPerPage.value;
    return filteredProducts.value.slice(start, end);
});

const totalPages = computed(() => {
    return Math.ceil(filteredProducts.value.length / itemsPerPage.value);
});

// Debounced filter function
let filterTimeout = null;
const debouncedFilter = () => {
    clearTimeout(filterTimeout);
    filterTimeout = setTimeout(() => {
        applyFilters();
    }, 300);
};

// Bootstrap Modal instances
let bsProductModal = null;
let bsDetailModal = null;
let bsImageModal = null;
let bsDeleteModal = null;

// Lifecycle
onMounted(async () => {
    // Initialize Bootstrap modals
    const { Modal } = await import('bootstrap');

    bsProductModal = new Modal(productModal.value);
    bsDetailModal = new Modal(detailModal.value);
    bsImageModal = new Modal(imageModal.value);
    bsDeleteModal = new Modal(deleteModal.value);

    // Load initial data
    await loadInitialData();
});

// Methods
async function loadInitialData() {
    loading.value = true;
    try {
        await Promise.all([loadProducts(), loadDanhMucs(), loadThuongHieus(), loadChatLieus(), loadDeGiays(), loadKichCos(), loadMauSacs()]);
        applyFilters();
    } catch (error) {
        console.error('Lỗi khi tải dữ liệu:', error);
        showToast('Lỗi khi tải dữ liệu', 'error');
    } finally {
        loading.value = false;
    }
}

async function loadProducts() {
    try {
        const response = await axios.get(`${API_BASE_URL}/api/san-pham`);
        products.value = response.data.map((item) => ({
            ...item,
            category: item.danhMuc?.tenDanhMuc || 'Chưa phân loại',
            brand: item.thuongHieu?.tenThuongHieu || 'Không có',
            material: item.chatLieu?.tenChatLieu || 'Không có',
            sole: item.deGiay?.tenDeGiay || 'Không có',
            createdAt: item.ngayTao ? new Date(item.ngayTao).toLocaleDateString('vi-VN') : 'N/A'
        }));

        // Load chi tiết cho mỗi sản phẩm
        for (const product of products.value) {
            await loadProductDetails(product.id);
        }
    } catch (error) {
        console.error('Lỗi khi tải sản phẩm:', error);
        throw error;
    }
}

async function loadProductDetails(productId) {
    try {
        const response = await axios.get(`${API_BASE_URL}/api/san-pham-chi-tiet/san-pham/${productId}`);
        const details = response.data;

        const product = products.value.find((p) => p.id === productId);
        if (product) {
            product.details = details.map((detail) => ({
                ...detail,
                size: detail.kichCo?.tenKichCo || 'N/A',
                color: detail.mauSac?.tenMauSac || 'N/A',
                createdAt: detail.ngayTao ? new Date(detail.ngayTao).toLocaleDateString('vi-VN') : 'N/A'
            }));

            // Tính tổng số lượng
            product.totalQuantity = details.reduce((sum, detail) => sum + (detail.soLuong || 0), 0);

            // Load hình ảnh cho từng chi tiết
            for (const detail of product.details) {
                await loadDetailImages(detail);
            }
        }
    } catch (error) {
        console.error(`Lỗi khi tải chi tiết sản phẩm ${productId}:`, error);
    }
}

async function loadDetailImages(detail) {
    try {
        const response = await axios.get(`${API_BASE_URL}/hinh-anh/chi-tiet-san-pham/${detail.id}`);
        const images = response.data;

        detail.images = images.map((img) => ({
            ...img,
            url: createImageUrl(img.duongDan)
        }));
    } catch (error) {
        console.error(`Lỗi khi tải hình ảnh cho chi tiết ${detail.id}:`, error);
        detail.images = [];
    }
}

function createImageUrl(duongDan) {
    if (!duongDan) return '/images/placeholder.png';

    let cleanPath = duongDan;
    if (cleanPath.startsWith('/hinh-anh/images/')) {
        cleanPath = cleanPath.replace('/hinh-anh/images/', '');
    } else if (cleanPath.startsWith('/images/')) {
        cleanPath = cleanPath.replace('/images/', '');
    }

    return `${API_BASE_URL}/hinh-anh/images/${cleanPath}`;
}

async function loadDanhMucs() {
    try {
        const response = await axios.get(`${API_BASE_URL}/danh-muc`);
        danhMucs.value = response.data;
    } catch (error) {
        console.error('Lỗi khi tải danh mục:', error);
    }
}

async function loadThuongHieus() {
    try {
        const response = await axios.get(`${API_BASE_URL}/thuong-hieu`);
        thuongHieus.value = response.data;
    } catch (error) {
        console.error('Lỗi khi tải thương hiệu:', error);
    }
}

async function loadChatLieus() {
    try {
        const response = await axios.get(`${API_BASE_URL}/chat-lieu`);
        chatLieus.value = response.data;
    } catch (error) {
        console.error('Lỗi khi tải chất liệu:', error);
    }
}

async function loadDeGiays() {
    try {
        const response = await axios.get(`${API_BASE_URL}/de-giay`);
        deGiays.value = response.data;
    } catch (error) {
        console.error('Lỗi khi tải đế giày:', error);
    }
}

async function loadKichCos() {
    try {
        const response = await axios.get(`${API_BASE_URL}/kich-co`);
        kichCos.value = response.data;
    } catch (error) {
        console.error('Lỗi khi tải kích cỡ:', error);
    }
}

async function loadMauSacs() {
    try {
        const response = await axios.get(`${API_BASE_URL}/mau-sac`);
        mauSacs.value = response.data;
    } catch (error) {
        console.error('Lỗi khi tải màu sắc:', error);
    }
}

function applyFilters() {
    const { search, category, brand, status, sort } = filters.value;

    // Filter products
    filteredProducts.value = products.value.filter((product) => {
        const matchSearch = !search || product.tenSanPham.toLowerCase().includes(search.toLowerCase()) || product.maSanPham.toLowerCase().includes(search.toLowerCase());

        const matchCategory = !category || (product.danhMuc && product.danhMuc.id == category);

        const matchBrand = !brand || (product.thuongHieu && product.thuongHieu.id == brand);

        const matchStatus = status === '' || product.trangThai == status;

        return matchSearch && matchCategory && matchBrand && matchStatus;
    });

    // Sort products
    filteredProducts.value.sort((a, b) => {
        switch (sort) {
            case 'name_asc':
                return a.tenSanPham.localeCompare(b.tenSanPham);
            case 'name_desc':
                return b.tenSanPham.localeCompare(a.tenSanPham);
            case 'created_desc':
                return new Date(b.ngayTao || 0) - new Date(a.ngayTao || 0);
            case 'created_asc':
                return new Date(a.ngayTao || 0) - new Date(b.ngayTao || 0);
            case 'quantity_desc':
                return (b.totalQuantity || 0) - (a.totalQuantity || 0);
            case 'quantity_asc':
                return (a.totalQuantity || 0) - (b.totalQuantity || 0);
            default:
                return 0;
        }
    });

    // Reset to first page
    currentPage.value = 1;
}

function resetFilters() {
    filters.value = {
        search: '',
        category: '',
        brand: '',
        status: '',
        sort: 'name_asc'
    };
    applyFilters();
}

function changePage(page) {
    if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page;
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }
}

function getVisiblePages() {
    const total = totalPages.value;
    const current = currentPage.value;
    const visible = [];

    if (total <= 7) {
        for (let i = 1; i <= total; i++) {
            visible.push(i);
        }
    } else {
        if (current <= 4) {
            for (let i = 1; i <= 5; i++) {
                visible.push(i);
            }
            visible.push('...', total);
        } else if (current >= total - 3) {
            visible.push(1, '...');
            for (let i = total - 4; i <= total; i++) {
                visible.push(i);
            }
        } else {
            visible.push(1, '...');
            for (let i = current - 1; i <= current + 1; i++) {
                visible.push(i);
            }
            visible.push('...', total);
        }
    }

    return visible.filter((page) => page !== '...' || visible.indexOf(page) === visible.lastIndexOf(page));
}

// Product CRUD operations
function openProductModal(editProduct = null) {
    if (editProduct) {
        isEditMode.value = true;
        product.value = {
            id: editProduct.id,
            tenSanPham: editProduct.tenSanPham,
            maSanPham: editProduct.maSanPham,
            soLuong: editProduct.soLuong || 0,
            trangThai: editProduct.trangThai,
            danhMuc: editProduct.danhMuc,
            thuongHieu: editProduct.thuongHieu,
            chatLieu: editProduct.chatLieu,
            deGiay: editProduct.deGiay
        };
    } else {
        isEditMode.value = false;
        product.value = {
            tenSanPham: '',
            maSanPham: createProductId(),
            soLuong: 0,
            trangThai: 1,
            danhMuc: null,
            thuongHieu: null,
            chatLieu: null,
            deGiay: null
        };
    }

    errors.value = {};
    bsProductModal.show();
}

function editProduct(prod) {
    openProductModal(prod);
}

async function saveProduct() {
    if (!validateProduct()) return;

    saving.value = true;
    try {
        const productData = {
            tenSanPham: product.value.tenSanPham,
            maSanPham: product.value.maSanPham,
            soLuong: Math.max(0, product.value.soLuong || 0),
            trangThai: product.value.trangThai,
            danhMuc: product.value.danhMuc,
            thuongHieu: product.value.thuongHieu,
            chatLieu: product.value.chatLieu,
            deGiay: product.value.deGiay
        };

        if (product.value.id) {
            await axios.put(`${API_BASE_URL}/api/san-pham/update/${product.value.id}`, productData);
            showToast(`Đã cập nhật sản phẩm "${product.value.tenSanPham}"`, 'success');
        } else {
            await axios.post(`${API_BASE_URL}/api/san-pham/save`, productData);
            showToast(`Đã thêm sản phẩm "${product.value.tenSanPham}"`, 'success');
        }

        await loadProducts();
        applyFilters();
        bsProductModal.hide();
    } catch (error) {
        console.error('Lỗi khi lưu sản phẩm:', error);
        showToast(`Không thể lưu sản phẩm: ${error.response?.data?.message || error.message}`, 'error');
    } finally {
        saving.value = false;
    }
}

function validateProduct() {
    errors.value = {};

    if (!product.value.tenSanPham?.trim()) {
        errors.value.tenSanPham = 'Tên sản phẩm là bắt buộc';
    }

    if (!product.value.danhMuc) {
        errors.value.danhMuc = 'Danh mục là bắt buộc';
    }

    if (!product.value.thuongHieu) {
        errors.value.thuongHieu = 'Thương hiệu là bắt buộc';
    }

    if (!product.value.chatLieu) {
        errors.value.chatLieu = 'Chất liệu là bắt buộc';
    }

    if (!product.value.deGiay) {
        errors.value.deGiay = 'Đế giày là bắt buộc';
    }

    return Object.keys(errors.value).length === 0;
}

function confirmDeleteProduct(prod) {
    deleteAction.value = () => deleteProduct(prod);
    deleteMessage.value = `Bạn có chắc chắn muốn xóa sản phẩm "${prod.tenSanPham}"?`;
    bsDeleteModal.show();
}

async function deleteProduct(prod) {
    deleting.value = true;
    try {
        await axios.delete(`${API_BASE_URL}/api/san-pham/delete/${prod.id}`);
        showToast(`Đã xóa sản phẩm "${prod.tenSanPham}"`, 'success');
        await loadProducts();
        applyFilters();
        bsDeleteModal.hide();
    } catch (error) {
        console.error('Lỗi khi xóa sản phẩm:', error);
        showToast(`Không thể xóa sản phẩm: ${error.response?.data?.message || error.message}`, 'error');
    } finally {
        deleting.value = false;
    }
}

function confirmDelete() {
    if (deleteAction.value) {
        deleteAction.value();
    }
}

// Product detail operations
function showProductDetail(prod) {
    selectedProduct.value = prod;
    bsDetailModal.show();
}

function openDetailModal(productId) {
    // Implementation for opening detail creation modal
    showToast('Chức năng thêm chi tiết sản phẩm đang phát triển', 'info');
}

function editDetail(detail, productId) {
    // Implementation for editing detail
    showToast('Chức năng sửa chi tiết sản phẩm đang phát triển', 'info');
}

function confirmDeleteDetail(detail) {
    deleteAction.value = () => deleteDetail(detail);
    deleteMessage.value = `Bạn có chắc chắn muốn xóa chi tiết "${detail.maChiTiet}"?`;
    bsDeleteModal.show();
}

async function deleteDetail(detail) {
    deleting.value = true;
    try {
        await axios.delete(`${API_BASE_URL}/api/san-pham-chi-tiet/delete/${detail.id}`);
        showToast(`Đã xóa chi tiết "${detail.maChiTiet}"`, 'success');

        // Reload product details
        if (selectedProduct.value) {
            await loadProductDetails(selectedProduct.value.id);
            // Update selected product
            selectedProduct.value = products.value.find((p) => p.id === selectedProduct.value.id);
        }

        bsDeleteModal.hide();
    } catch (error) {
        console.error('Lỗi khi xóa chi tiết:', error);
        showToast(`Không thể xóa chi tiết: ${error.response?.data?.message || error.message}`, 'error');
    } finally {
        deleting.value = false;
    }
}

// Image operations
function showImageModal(image) {
    selectedImage.value = image;
    bsImageModal.show();
}

function handleImageError(event) {
    if (event.target.src.includes('placeholder.png')) {
        return;
    }
    event.target.onerror = null;
    event.target.src = '/images/placeholder.png';
}

// Utility functions
function createProductId() {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let id = '';
    for (let i = 0; i < 8; i++) {
        id += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return 'SP' + id;
}

function formatCurrency(value) {
    if (!value) return '0 ₫';
    return value.toLocaleString('vi-VN', { style: 'currency', currency: 'VND' });
}

function getTotalQuantity(product) {
    if (!product.details) return product.soLuong || 0;
    return product.details.reduce((sum, detail) => sum + (detail.soLuong || 0), 0);
}

function getDetailCount(product) {
    return product.details ? product.details.length : 0;
}

function getUniqueColors(product) {
    if (!product.details) return 0;
    const colors = new Set(product.details.map((d) => d.color));
    return colors.size;
}

function getUniqueSizes(product) {
    if (!product.details) return 0;
    const sizes = new Set(product.details.map((d) => d.size));
    return sizes.size;
}

function getProductImages(product) {
    if (!product.details) return [];
    const images = [];
    product.details.forEach((detail) => {
        if (detail.images) {
            images.push(...detail.images);
        }
    });
    return images;
}

// Badge classes
function getStatusBadgeClass(status) {
    return status === 1 ? 'bg-success' : 'bg-danger';
}

function getQuantityBadgeClass(quantity) {
    if (quantity > 50) return 'bg-success';
    if (quantity > 10) return 'bg-warning';
    return 'bg-danger';
}

// Toast system
function showToast(message, type = 'info') {
    const toast = {
        id: Date.now(),
        message,
        type
    };
    toasts.value.push(toast);

    setTimeout(() => {
        removeToast(toast.id);
    }, 5000);
}

function removeToast(id) {
    const index = toasts.value.findIndex((t) => t.id === id);
    if (index > -1) {
        toasts.value.splice(index, 1);
    }
}

function getToastClass(type) {
    const classes = {
        success: 'text-bg-success',
        error: 'text-bg-danger',
        warning: 'text-bg-warning',
        info: 'text-bg-info'
    };
    return classes[type] || classes.info;
}

function getToastTitle(type) {
    const titles = {
        success: 'Thành công',
        error: 'Lỗi',
        warning: 'Cảnh báo',
        info: 'Thông báo'
    };
    return titles[type] || titles.info;
}

// Export and refresh
async function exportData() {
    try {
        if (!filteredProducts.value.length) {
            showToast('Không có dữ liệu để xuất', 'warning');
            return;
        }

        const headers = ['ID', 'Mã SP', 'Tên sản phẩm', 'Tổng SL', 'Danh mục', 'Thương hiệu', 'Chất liệu', 'Đế giày', 'Trạng thái', 'Ngày tạo'];

        const csvData = filteredProducts.value.map((item) => [
            item.id || '',
            item.maSanPham || '',
            item.tenSanPham || '',
            getTotalQuantity(item),
            item.category || '',
            item.brand || '',
            item.material || '',
            item.sole || '',
            item.trangThai === 1 ? 'Hoạt động' : 'Ngừng hoạt động',
            item.createdAt || ''
        ]);

        const csvContent = [headers, ...csvData]
            .map((row) =>
                row
                    .map((field) => {
                        const stringField = String(field);
                        if (stringField.includes(',') || stringField.includes('"') || stringField.includes('\n')) {
                            return `"${stringField.replace(/"/g, '""')}"`;
                        }
                        return stringField;
                    })
                    .join(',')
            )
            .join('\n');

        const BOM = '\uFEFF';
        const csvWithBOM = BOM + csvContent;
        const blob = new Blob([csvWithBOM], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');

        if (link.download !== undefined) {
            const url = URL.createObjectURL(blob);
            link.setAttribute('href', url);

            const now = new Date();
            const dateStr = now.toISOString().split('T')[0];
            const filename = `SanPham-${dateStr}.csv`;

            link.setAttribute('download', filename);
            link.style.visibility = 'hidden';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);

            showToast(`Đã xuất ${filteredProducts.value.length} sản phẩm ra file CSV`, 'success');
        }
    } catch (error) {
        console.error('Error exporting CSV:', error);
        showToast('Xuất CSV thất bại', 'error');
    }
}

async function refreshData() {
    await loadInitialData();
    showToast('Dữ liệu đã được cập nhật', 'info');
}
</script>

<style scoped>
.filter-section {
    background: #f8f9fa;
    border: 1px solid #e9ecef;
}

.product-card {
    transition:
        transform 0.2s ease,
        box-shadow 0.2s ease;
    border: 1px solid #e9ecef;
}

.product-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.product-table-image {
    width: 32px;
    height: 32px;
    object-fit: cover;
    border-radius: 4px;
    border: 1px solid #dee2e6;
    cursor: pointer;
    transition: border-color 0.2s ease;
}

.product-table-image:hover {
    border-color: #0d6efd;
}

.table th {
    background-color: #f8f9fa;
    border-top: none;
    font-weight: 600;
    font-size: 0.875rem;
    vertical-align: middle;
}

.table td {
    vertical-align: middle;
    padding: 0.75rem 0.5rem;
}

.table tbody tr:hover {
    background-color: rgba(13, 110, 253, 0.04);
}

.btn-group-sm .btn {
    padding: 0.2rem 0.4rem;
    font-size: 0.75rem;
    border-radius: 3px;
}

/* Bootstrap Icons specific styles */
.bi {
    font-family: 'bootstrap-icons' !important;
    font-style: normal;
    font-weight: normal;
    font-variant: normal;
    text-transform: none;
    line-height: 1;
    vertical-align: -0.125em;
}

.btn i.bi {
    font-size: 0.875rem;
}

.btn-group-sm .btn i.bi {
    font-size: 0.75rem;
}

code {
    font-size: 0.8rem;
    padding: 0.2rem 0.4rem;
    background-color: rgba(13, 110, 253, 0.1);
    border-radius: 3px;
}

/* Tooltip styles */
[v-tooltip] {
    position: relative;
}

.detail-image-thumb {
    width: 30px;
    height: 30px;
    object-fit: cover;
    border-radius: 4px;
    border: 1px solid #dee2e6;
    cursor: pointer;
}

/* Responsive table improvements */
@media (max-width: 992px) {
    .table-responsive {
        font-size: 0.8rem;
    }

    .btn-group-sm .btn {
        padding: 0.15rem 0.3rem;
    }

    .product-table-image {
        width: 24px;
        height: 24px;
    }
}

.card-header {
    background-color: #f8f9fa;
    border-bottom: 1px solid #e9ecef;
}

.toast-container {
    z-index: 1080;
}

.btn-group-sm > .btn {
    padding: 0.25rem 0.5rem;
    font-size: 0.775rem;
}

.pagination {
    margin-bottom: 0;
}

.page-link {
    color: #6c757d;
    border-color: #dee2e6;
}

.page-item.active .page-link {
    background-color: #0d6efd;
    border-color: #0d6efd;
}

.spinner-border-sm {
    width: 1rem;
    height: 1rem;
}

.text-bg-success {
    color: #fff !important;
    background-color: #198754 !important;
}

.text-bg-danger {
    color: #fff !important;
    background-color: #dc3545 !important;
}

.text-bg-warning {
    color: #000 !important;
    background-color: #ffc107 !important;
}

.text-bg-info {
    color: #fff !important;
    background-color: #0dcaf0 !important;
}

@media (max-width: 768px) {
    .col-lg-4 {
        margin-bottom: 1rem;
    }

    .table-responsive {
        font-size: 0.875rem;
    }

    .btn-group {
        flex-direction: column;
    }

    .btn-group .btn {
        margin-bottom: 0.25rem;
    }
}

/* Custom scrollbar */
.table-responsive::-webkit-scrollbar {
    height: 8px;
}

.table-responsive::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
}

.table-responsive::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 4px;
}

.table-responsive::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
}

/* Loading animation */
@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.product-card {
    animation: fadeIn 0.3s ease-out;
}

/* Badge improvements */
.badge {
    font-size: 0.75rem;
    font-weight: 500;
}

/* Form improvements */
.form-label {
    font-weight: 600;
    color: #495057;
    margin-bottom: 0.5rem;
}

.form-control:focus,
.form-select:focus {
    border-color: #86b7fe;
    box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
}

.is-invalid {
    border-color: #dc3545;
}

.invalid-feedback {
    display: block;
    font-size: 0.875rem;
}

/* Modal improvements */
.modal-header {
    background-color: #f8f9fa;
    border-bottom: 1px solid #e9ecef;
}

.modal-footer {
    background-color: #f8f9fa;
    border-top: 1px solid #e9ecef;
}

/* Button improvements */
.btn {
    font-weight: 500;
    border-radius: 6px;
    transition: all 0.15s ease-in-out;
}

.btn:hover {
    transform: translateY(-1px);
}

.btn-outline-primary:hover,
.btn-outline-success:hover,
.btn-outline-info:hover,
.btn-outline-warning:hover,
.btn-outline-danger:hover {
    transform: translateY(-1px);
}

/* Card improvements */
.card {
    border-radius: 8px;
    overflow: hidden;
}

.card-body {
    padding: 1.25rem;
}

.card-footer {
    padding: 0.75rem 1.25rem;
    background-color: rgba(0, 0, 0, 0.03);
}

/* Statistics cards */
.bg-primary.bg-opacity-10 {
    background-color: rgba(13, 110, 253, 0.1) !important;
}

.bg-success.bg-opacity-10 {
    background-color: rgba(25, 135, 84, 0.1) !important;
}

.bg-warning.bg-opacity-10 {
    background-color: rgba(255, 193, 7, 0.1) !important;
}
</style>
