<template>
    <div class="card">
        <Toast />
        <Toolbar class="mb-6">
            <template #start>
                <Button label="Thêm mới" icon="pi pi-plus" severity="secondary" class="mr-2" @click="openNew" />
                <Button label="Xóa" icon="pi pi-trash" severity="danger" @click="confirmDeleteSelected" :disabled="!selectedHinhAnh || !selectedHinhAnh.length" />
            </template>
            <template #end>
                <Button label="Xuất CSV" icon="pi pi-upload" severity="secondary" @click="exportCSV" />
                <Button icon="pi pi-refresh" v-tooltip.left="'Làm mới dữ liệu'" @click="fetchData" />
            </template>
        </Toolbar>

        <DataTable
            ref="dt"
            v-model:selection="selectedHinhAnh"
            :value="ListHinhAnh"
            dataKey="id"
            :paginator="true"
            :rows="10"
            :filters="filters"
            :loading="loading"
            paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
            :rowsPerPageOptions="[5, 10, 25]"
            currentPageReportTemplate="Hiển thị {first} đến {last} của {totalRecords} hình ảnh"
            tableStyle="min-width: 70rem"
        >
            <template #header>
                <div class="flex flex-wrap items-center justify-between gap-2">
                    <h4 class="m-0">📋 Quản lý Hình Ảnh</h4>
                    <IconField>
                        <InputIcon>
                            <i class="pi pi-search" />
                        </InputIcon>
                        <InputText v-model="filters['global'].value" placeholder="Tìm kiếm..." />
                    </IconField>
                </div>
            </template>

            <Column selectionMode="multiple" style="width: 3rem" :exportable="false"></Column>
            <Column field="id" header="ID" sortable style="min-width: 8rem"></Column>
            <Column field="maHinhAnh" header="Mã Hình Ảnh" sortable style="min-width: 12rem"></Column>
            <Column field="tenHinhAnh" header="Tên File" sortable style="min-width: 16rem"></Column>

            <!-- Cột chi tiết sản phẩm -->
            <Column header="Chi Tiết Sản Phẩm" sortable style="min-width: 20rem">
                <template #body="slotProps">
                    <div v-if="slotProps.data.chiTietSanPham" class="text-sm leading-6">
                        <div class="mb-1 font-medium">{{ slotProps.data.chiTietSanPham?.sanPham?.tenSanPham || 'N/A' }}</div>
                        <div class="flex gap-4 text-xs text-gray-600">
                            <span><strong>Mã:</strong> {{ slotProps.data.chiTietSanPham?.maChiTiet || 'N/A' }}</span>
                            <span><strong>Màu:</strong> {{ slotProps.data.chiTietSanPham?.mauSac?.tenMauSac || 'N/A' }}</span>
                            <span><strong>Size:</strong> {{ slotProps.data.chiTietSanPham?.kichCo?.tenKichCo || 'N/A' }}</span>
                        </div>
                    </div>
                    <div v-else class="italic text-gray-400">Chưa liên kết</div>
                </template>
            </Column>

            <Column field="trangThai" header="Trạng Thái" sortable style="min-width: 12rem">
                <template #body="slotProps">
                    <Tag :value="slotProps.data.trangThai === 1 ? 'Đã tải' : 'Đang tải'" :severity="getStatusLabel(slotProps.data.trangThai)" />
                </template>
            </Column>

            <!-- Cột ngày tạo -->
            <Column header="Ngày tạo" sortable style="min-width: 12rem">
                <template #body="slotProps">
                    {{ formatDate(slotProps.data.ngayTao) }}
                </template>
            </Column>

            <Column :exportable="false" style="min-width: 12rem">
                <template #body="slotProps">
                    <div class="flex gap-2">
                        <Button icon="pi pi-eye" outlined rounded size="small" v-tooltip.top="'Xem'" @click="openImagePreview(slotProps.data)" />
                        <Button icon="pi pi-pencil" outlined rounded size="small" v-tooltip.top="'Sửa'" @click="editHinhAnh(slotProps.data)" />
                        <Button icon="pi pi-trash" outlined rounded severity="danger" size="small" v-tooltip.top="'Xóa'" @click="confirmDeleteHinhAnh(slotProps.data)" />
                        <Button icon="pi pi-refresh" outlined rounded severity="secondary" size="small" v-tooltip.top="'Đổi trạng thái'" @click="changeStatus(slotProps.data)" />
                    </div>
                </template>
            </Column>
        </DataTable>

        <!-- Dialog thêm/sửa hình ảnh -->
        <Dialog v-model:visible="hinhAnhDialog" :style="{ width: '600px' }" header="Chi tiết Hình Ảnh" :modal="true" class="p-fluid">
            <div class="flex flex-col gap-6">
                <div class="grid grid-cols-12 gap-4">
                    <div class="col-span-6">
                        <label for="maHinhAnh" class="mb-3 block font-bold">Mã Hình Ảnh *</label>
                        <InputText id="maHinhAnh" v-model.trim="hinhAnh.maHinhAnh" required="true" autofocus :invalid="submitted && !hinhAnh.maHinhAnh" fluid :disabled="!!hinhAnh.id" />
                        <small v-if="submitted && !hinhAnh.maHinhAnh" class="text-red-500">Mã Hình Ảnh là bắt buộc.</small>
                    </div>
                    <div class="col-span-6">
                        <label for="trangThai" class="mb-3 block font-bold">Trạng Thái</label>
                        <Select id="trangThai" v-model="hinhAnh.trangThai" :options="statuses" optionLabel="label" optionValue="value" placeholder="Chọn trạng thái" fluid />
                    </div>
                </div>

                <!-- Upload file -->
                <div>
                    <label class="mb-3 block font-bold">Upload Hình Ảnh *</label>
                    <FileUpload
                        mode="basic"
                        name="file"
                        :auto="false"
                        chooseLabel="Chọn file"
                        accept="image/*"
                        :maxFileSize="5000000"
                        @select="onFileSelect"
                        @clear="onFileClear"
                        :pt="{
                            root: { class: 'w-full' },
                            input: { class: 'w-full' },
                            basicButton: { class: 'w-full' }
                        }"
                    />
                    <small v-if="submitted && !selectedFile && !hinhAnh.tenHinhAnh" class="text-red-500">Vui lòng chọn file hình ảnh.</small>
                    <small v-else class="text-gray-500">Chọn file ảnh (JPG, PNG, GIF). Tối đa 5MB.</small>

                    <!-- Preview hình ảnh đã chọn -->
                    <div v-if="selectedFile" class="mt-4 rounded border p-4">
                        <div class="flex items-center gap-4">
                            <img :src="previewUrl" alt="Preview" class="h-20 w-20 rounded object-cover" />
                            <div class="flex-1">
                                <div class="font-medium">{{ selectedFile.name }}</div>
                                <div class="text-sm text-gray-500">{{ formatFileSize(selectedFile.size) }}</div>
                            </div>
                            <Button icon="pi pi-times" outlined rounded size="small" @click="clearSelectedFile" />
                        </div>
                    </div>

                    <!-- Hiển thị hình ảnh hiện tại khi edit -->
                    <div v-else-if="hinhAnh.id && hinhAnh.tenHinhAnh" class="mt-4 rounded border p-4">
                        <div class="mb-2 text-sm font-medium">Hình ảnh hiện tại:</div>
                        <div class="flex items-center gap-4">
                            <img :src="getImageUrl(hinhAnh.tenHinhAnh)" :alt="hinhAnh.tenHinhAnh" class="h-20 w-20 rounded object-cover" @error="handleImageError($event)" />
                            <div class="flex-1">
                                <div class="font-medium">{{ hinhAnh.tenHinhAnh }}</div>
                                <div class="text-sm text-gray-500">File hiện tại</div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Chọn chi tiết sản phẩm (không bắt buộc) -->
                <div>
                    <label for="chiTietSanPham" class="mb-3 block font-bold">Chi Tiết Sản Phẩm <span class="font-normal text-gray-500">(Không bắt buộc)</span></label>
                    <Select id="chiTietSanPham" v-model="hinhAnh.chiTietSanPham" :options="chiTietSanPhams" optionLabel="displayName" placeholder="Chọn chi tiết sản phẩm (có thể bỏ trống)" fluid filter showClear :loading="loadingChiTiet" />
                    <small class="text-gray-500">Có thể liên kết với chi tiết sản phẩm hoặc để trống.</small>
                </div>
            </div>

            <template #footer>
                <Button label="Hủy" icon="pi pi-times" text @click="hideDialog" :disabled="uploading" />
                <Button label="Lưu" icon="pi pi-check" @click="saveHinhAnh" :loading="uploading" />
            </template>
        </Dialog>

        <!-- Dialog xem trước hình ảnh -->
        <Dialog v-model:visible="imagePreviewDialog" :style="{ width: '800px' }" header="Xem trước hình ảnh" :modal="true">
            <div v-if="selectedImage" class="flex flex-col gap-4">
                <!-- Hình ảnh lớn - SỬA ĐỂ TRÁNH NHẤP NHÁY -->
                <div class="text-center">
                    <!-- Nếu có file hình ảnh -->
                    <div v-if="selectedImage.tenHinhAnh" class="image-container">
                        <img :src="getImageUrl(selectedImage.tenHinhAnh)" :alt="selectedImage.tenHinhAnh" class="preview-image" @error="handleImageError($event)" @load="onImageLoad" />
                    </div>
                    <!-- Nếu không có file hình ảnh - HIỂN THỊ CỐ ĐỊNH -->
                    <div v-else class="no-image-container">
                        <div class="no-image-placeholder">
                            <i class="pi pi-image no-image-icon"></i>
                            <div class="no-image-text">
                                <h6>Chưa có hình ảnh</h6>
                                <p>Hình ảnh chưa được upload</p>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Thông tin chi tiết -->
                <div class="grid grid-cols-2 gap-4 border-t pt-4 text-sm">
                    <div><strong>Mã hình ảnh:</strong> {{ selectedImage.maHinhAnh }}</div>
                    <div><strong>Tên file:</strong> {{ selectedImage.tenHinhAnh || 'Chưa có file' }}</div>
                    <div>
                        <strong>Trạng thái:</strong>
                        <Tag :value="selectedImage.trangThai === 1 ? 'Đã tải' : 'Đang tải'" :severity="selectedImage.trangThai === 1 ? 'success' : 'warning'" class="ml-2" />
                    </div>
                    <div><strong>Ngày tạo:</strong> {{ formatDate(selectedImage.ngayTao) }}</div>
                </div>

                <!-- Thông tin chi tiết sản phẩm -->
                <div v-if="selectedImage.chiTietSanPham" class="border-t pt-4">
                    <h6 class="mb-3 font-bold">Chi tiết sản phẩm liên kết:</h6>
                    <div class="grid grid-cols-2 gap-3 rounded bg-gray-50 p-4 text-sm">
                        <div><strong>Sản phẩm:</strong> {{ selectedImage.chiTietSanPham?.sanPham?.tenSanPham || 'N/A' }}</div>
                        <div><strong>Mã chi tiết:</strong> {{ selectedImage.chiTietSanPham?.maChiTiet || 'N/A' }}</div>
                        <div><strong>Màu sắc:</strong> {{ selectedImage.chiTietSanPham?.mauSac?.tenMauSac || 'N/A' }}</div>
                        <div><strong>Kích cỡ:</strong> {{ selectedImage.chiTietSanPham?.kichCo?.tenKichCo || 'N/A' }}</div>
                        <div><strong>Giá bán:</strong> {{ formatCurrency(selectedImage.chiTietSanPham?.giaBan) }}</div>
                        <div><strong>Số lượng:</strong> {{ selectedImage.chiTietSanPham?.soLuong || 0 }}</div>
                    </div>
                </div>
                <div v-else class="border-t pt-4">
                    <div class="text-center italic text-gray-500">Hình ảnh này chưa được liên kết với chi tiết sản phẩm nào.</div>
                </div>
            </div>

            <template #footer>
                <Button label="Đóng" icon="pi pi-times" @click="imagePreviewDialog = false" />
                <Button v-if="selectedImage?.tenHinhAnh" label="Tải xuống" icon="pi pi-download" @click="downloadImage(selectedImage)" />
                <Button label="Chỉnh sửa" icon="pi pi-pencil" @click="editFromPreview" />
            </template>
        </Dialog>

        <!-- Dialog xác nhận xóa -->
        <Dialog v-model:visible="deleteHinhAnhDialog" :style="{ width: '450px' }" header="Xác nhận" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl text-red-500" />
                <div>
                    <p v-if="hinhAnh">
                        Bạn có chắc muốn xóa hình ảnh <strong>{{ hinhAnh.tenHinhAnh || hinhAnh.maHinhAnh }}</strong
                        >?
                    </p>
                    <small class="text-gray-500">Hành động này không thể hoàn tác.</small>
                </div>
            </div>
            <template #footer>
                <Button label="Không" icon="pi pi-times" text @click="deleteHinhAnhDialog = false" />
                <Button label="Có" icon="pi pi-check" severity="danger" @click="deleteHinhAnh" />
            </template>
        </Dialog>

        <!-- Dialog xác nhận xóa nhiều -->
        <Dialog v-model:visible="deleteHinhAnhsDialog" :style="{ width: '450px' }" header="Xác nhận" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl text-red-500" />
                <div>
                    <p>
                        Bạn có chắc muốn xóa <strong>{{ selectedHinhAnh?.length || 0 }}</strong> hình ảnh đã chọn?
                    </p>
                    <small class="text-gray-500">Hành động này không thể hoàn tác.</small>
                </div>
            </div>
            <template #footer>
                <Button label="Không" icon="pi pi-times" text @click="deleteHinhAnhsDialog = false" />
                <Button label="Có" icon="pi pi-check" severity="danger" @click="deleteSelectedHinhAnhs" />
            </template>
        </Dialog>
    </div>
</template>

<script setup>
import { FilterMatchMode } from '@primevue/core/api';
import axios from 'axios';
import { useToast } from 'primevue/usetoast';
import { onMounted, ref } from 'vue';

const API_BASE_URL = `${import.meta.env.VITE_API_BASE_URL}`;

const toast = useToast();
const dt = ref();
const ListHinhAnh = ref([]);
const hinhAnhDialog = ref(false);
const deleteHinhAnhDialog = ref(false);
const deleteHinhAnhsDialog = ref(false);
const imagePreviewDialog = ref(false);
const hinhAnh = ref({});
const selectedHinhAnh = ref();
const selectedImage = ref({});
const submitted = ref(false);
const loading = ref(false);
const uploading = ref(false);
const loadingChiTiet = ref(false);

// File upload
const selectedFile = ref(null);
const previewUrl = ref('');

// Chi tiết sản phẩm
const chiTietSanPhams = ref([]);

const filters = ref({
    global: { value: null, matchMode: FilterMatchMode.CONTAINS }
});

const statuses = ref([
    { label: 'Đã tải', value: 1 },
    { label: 'Đang tải', value: 0 }
]);

onMounted(() => {
    fetchData();
    loadChiTietSanPhams();
});

// API Functions
async function fetchData() {
    try {
        loading.value = true;
        const res = await axios.get(`${API_BASE_URL}/hinh-anh`);
        ListHinhAnh.value = res.data;
    } catch (error) {
        console.error('Error fetching data:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Không thể tải danh sách hình ảnh',
            life: 3000
        });
    } finally {
        loading.value = false;
    }
}

async function loadChiTietSanPhams() {
    try {
        loadingChiTiet.value = true;
        const response = await axios.get(`${API_BASE_URL}/api/san-pham-chi-tiet`);
        chiTietSanPhams.value = response.data.map((item) => ({
            ...item,
            displayName: `${item.sanPham?.tenSanPham} - ${item.mauSac?.tenMauSac} - ${item.kichCo?.tenKichCo} (${item.maChiTiet})`
        }));
    } catch (error) {
        console.error('Lỗi khi tải chi tiết sản phẩm:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Không thể tải danh sách chi tiết sản phẩm',
            life: 3000
        });
    } finally {
        loadingChiTiet.value = false;
    }
}

// Utility Functions
function createId() {
    let id = '';
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    for (let i = 0; i < 8; i++) {
        id += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return 'HA' + id;
}

function getImageUrl(fileName) {
    return `${API_BASE_URL}/hinh-anh/images/${fileName}`;
}

function handleImageError(event) {
    event.target.src = '/images/placeholder.png';
    event.target.onerror = null;
}

function formatDate(date) {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function formatCurrency(value) {
    if (value) return value.toLocaleString('vi-VN', { style: 'currency', currency: 'VND' });
    return '0 ₫';
}

// Dialog Functions
function openNew() {
    hinhAnh.value = {
        maHinhAnh: createId(),
        tenHinhAnh: '',
        trangThai: 1,
        chiTietSanPham: null
    };
    submitted.value = false;
    clearSelectedFile();
    hinhAnhDialog.value = true;
}

function hideDialog() {
    hinhAnhDialog.value = false;
    submitted.value = false;
    clearSelectedFile();
}

function editHinhAnh(ha) {
    hinhAnh.value = { ...ha };
    clearSelectedFile();
    hinhAnhDialog.value = true;
}

function editFromPreview() {
    if (selectedImage.value) {
        editHinhAnh(selectedImage.value);
        imagePreviewDialog.value = false;
    }
}

// File Upload Functions
function onFileSelect(event) {
    selectedFile.value = event.files[0];
    if (selectedFile.value) {
        const reader = new FileReader();
        reader.onload = (e) => {
            previewUrl.value = e.target.result;
        };
        reader.readAsDataURL(selectedFile.value);

        // Auto generate filename
        const fileExtension = selectedFile.value.name.split('.').pop();
        const timestamp = Date.now();
        hinhAnh.value.tenHinhAnh = `${hinhAnh.value.maHinhAnh}_${timestamp}.${fileExtension}`;
    }
}

function onFileClear() {
    clearSelectedFile();
}

function clearSelectedFile() {
    selectedFile.value = null;
    previewUrl.value = '';
}

// Validation function
function validateForm() {
    const errors = [];

    // Kiểm tra mã hình ảnh
    if (!hinhAnh.value.maHinhAnh?.trim()) {
        errors.push('Mã hình ảnh là bắt buộc');
    }

    // Kiểm tra file
    if (!hinhAnh.value.id) {
        // Tạo mới: bắt buộc phải có file
        if (!selectedFile.value) {
            errors.push('Vui lòng chọn file hình ảnh');
        }
    } else {
        // Cập nhật: bắt buộc phải có tenHinhAnh hoặc file mới
        if (!hinhAnh.value.tenHinhAnh && !selectedFile.value) {
            errors.push('Vui lòng chọn file hình ảnh hoặc giữ file hiện tại');
        }
    }

    return errors;
}

// Save Function
async function saveHinhAnh() {
    submitted.value = true;

    // Validate form
    const validationErrors = validateForm();
    if (validationErrors.length > 0) {
        toast.add({
            severity: 'error',
            summary: 'Lỗi validation',
            detail: validationErrors.join(', '),
            life: 5000
        });
        return;
    }

    try {
        uploading.value = true;

        // Tạo FormData để upload file
        const formData = new FormData();

        // Thêm thông tin hình ảnh
        const hinhAnhData = {
            maHinhAnh: hinhAnh.value.maHinhAnh,
            tenHinhAnh: hinhAnh.value.tenHinhAnh,
            trangThai: hinhAnh.value.trangThai,
            chiTietSanPham: hinhAnh.value.chiTietSanPham || null // Cho phép null
        };

        formData.append('hinhAnh', JSON.stringify(hinhAnhData));

        // Thêm file nếu có
        if (selectedFile.value) {
            formData.append('file', selectedFile.value);
        }

        if (hinhAnh.value.id) {
            // Update
            await axios.put(`${API_BASE_URL}/hinh-anh/${hinhAnh.value.id}`, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });
            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: 'Cập nhật hình ảnh thành công',
                life: 3000
            });
        } else {
            // Create
            await axios.post(`${API_BASE_URL}/hinh-anh`, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });
            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: 'Tạo hình ảnh thành công',
                life: 3000
            });
        }

        await fetchData();
        hideDialog();
        hinhAnh.value = {};
    } catch (error) {
        console.error('Error saving hình ảnh:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: error.response?.data?.message || 'Lưu hình ảnh thất bại',
            life: 3000
        });
    } finally {
        uploading.value = false;
    }
}

// Delete Functions
function confirmDeleteHinhAnh(ha) {
    hinhAnh.value = ha;
    deleteHinhAnhDialog.value = true;
}

async function deleteHinhAnh() {
    try {
        await axios.delete(`${API_BASE_URL}/hinh-anh/${hinhAnh.value.id}`);
        await fetchData();
        deleteHinhAnhDialog.value = false;
        hinhAnh.value = {};
        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: 'Xóa hình ảnh thành công',
            life: 3000
        });
    } catch (error) {
        console.error('Error deleting hình ảnh:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: error.response?.data?.message || 'Xóa hình ảnh thất bại',
            life: 3000
        });
    }
}

function confirmDeleteSelected() {
    deleteHinhAnhsDialog.value = true;
}

async function deleteSelectedHinhAnhs() {
    try {
        for (const ha of selectedHinhAnh.value) {
            await axios.delete(`${API_BASE_URL}/hinh-anh/${ha.id}`);
        }
        await fetchData();
        deleteHinhAnhsDialog.value = false;
        selectedHinhAnh.value = null;
        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: 'Xóa các hình ảnh thành công',
            life: 3000
        });
    } catch (error) {
        console.error('Error deleting hình ảnh:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: error.response?.data?.message || 'Xóa các hình ảnh thất bại',
            life: 3000
        });
    }
}

// Preview Functions
function openImagePreview(hinhAnh) {
    selectedImage.value = hinhAnh;
    imagePreviewDialog.value = true;
}

function onImageLoad() {
    // Có thể thêm logic khi ảnh load thành công
}

function downloadImage(hinhAnh) {
    if (!hinhAnh.tenHinhAnh) {
        toast.add({
            severity: 'warn',
            summary: 'Cảnh báo',
            detail: 'Không có file để tải xuống',
            life: 3000
        });
        return;
    }

    const link = document.createElement('a');
    link.href = getImageUrl(hinhAnh.tenHinhAnh);
    link.download = hinhAnh.tenHinhAnh;
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

// Status Functions
async function changeStatus(ha) {
    try {
        const updatedHinhAnh = { ...ha, trangThai: ha.trangThai === 1 ? 0 : 1 };
        await axios.put(`${API_BASE_URL}/hinh-anh/${ha.id}`, updatedHinhAnh);
        await fetchData();
        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: 'Cập nhật trạng thái thành công',
            life: 3000
        });
    } catch (error) {
        console.error('Error changing status:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: error.response?.data?.message || 'Cập nhật trạng thái thất bại',
            life: 3000
        });
    }
}

function getStatusLabel(status) {
    return status === 1 ? 'success' : 'danger';
}

// Export Function
function exportCSV() {
    try {
        if (!ListHinhAnh.value || ListHinhAnh.value.length === 0) {
            toast.add({
                severity: 'warn',
                summary: 'Cảnh báo',
                detail: 'Không có dữ liệu để xuất',
                life: 3000
            });
            return;
        }

        const headers = ['ID', 'Mã Hình Ảnh', 'Tên File', 'Trạng Thái', 'Sản Phẩm', 'Mã Chi Tiết', 'Màu Sắc', 'Kích Cỡ', 'Ngày Tạo'];

        const csvData = ListHinhAnh.value.map((item) => {
            return [
                item.id || '',
                item.maHinhAnh || '',
                item.tenHinhAnh || '',
                item.trangThai === 1 ? 'Đã tải' : 'Đang tải',
                item.chiTietSanPham?.sanPham?.tenSanPham || '',
                item.chiTietSanPham?.maChiTiet || '',
                item.chiTietSanPham?.mauSac?.tenMauSac || '',
                item.chiTietSanPham?.kichCo?.tenKichCo || '',
                formatDate(item.ngayTao)
            ];
        });

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
            const filename = `HinhAnh-${dateStr}.csv`;

            link.setAttribute('download', filename);
            link.style.visibility = 'hidden';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);

            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: `Đã xuất ${ListHinhAnh.value.length} bản ghi ra file CSV`,
                life: 3000
            });
        }
    } catch (error) {
        console.error('Error exporting CSV:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Xuất CSV thất bại',
            life: 3000
        });
    }
}
</script>

<style scoped>
/* CSS để tránh nhấp nháy */
.image-container {
    position: relative;
    display: inline-block;
    max-width: 100%;
    max-height: 400px;
}

.preview-image {
    max-width: 100%;
    max-height: 400px;
    width: auto;
    height: auto;
    object-fit: contain;
    border-radius: 8px;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
    /* TẮT TẤT CẢ ANIMATION */
    animation: none !important;
    transition: none !important;
    transform: none !important;
}

.preview-image:hover {
    transform: none !important;
    animation: none !important;
    transition: none !important;
}

/* Container cho trường hợp không có ảnh */
.no-image-container {
    width: 100%;
    height: 300px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f8f9fa;
    border: 2px dashed #dee2e6;
    border-radius: 8px;
    /* TẮT TẤT CẢ ANIMATION */
    animation: none !important;
    transition: none !important;
}

.no-image-placeholder {
    text-align: center;
    color: #6c757d;
    /* TẮT ANIMATION */
    animation: none !important;
    transition: none !important;
}

.no-image-icon {
    font-size: 4rem;
    color: #adb5bd;
    margin-bottom: 1rem;
    /* TẮT ANIMATION */
    animation: none !important;
    transition: none !important;
}

.no-image-text h6 {
    margin: 0.5rem 0;
    font-weight: 600;
    color: #495057;
}

.no-image-text p {
    margin: 0;
    font-size: 0.875rem;
    color: #6c757d;
}

/* Tắt tất cả animations cho toàn bộ dialog */
.p-dialog * {
    animation: none !important;
    transition: none !important;
}

/* Tắt hover effects */
.p-dialog *:hover {
    transform: none !important;
    animation: none !important;
    transition: none !important;
}

/* Tắt loading animations */
.p-dialog .pi-spin {
    animation: none !important;
}

/* Card styles vẫn giữ nguyên */
.card {
    border: none;
    box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
    background: var(--surface-card);
    padding: 2rem;
    border-radius: 10px;
    margin-bottom: 1rem;
}

.p-fileupload-basic {
    display: flex;
    flex-direction: column;
}

.p-fileupload-basic .p-button {
    margin-right: 0.5rem;
}

/* Loading overlay */
.loading-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.8);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
}

/* Custom tooltip styles */
.p-tooltip {
    font-size: 0.875rem;
}

/* Responsive adjustments */
@media (max-width: 768px) {
    .card {
        padding: 1rem;
    }

    .grid.grid-cols-12 > div {
        grid-column: span 12;
    }

    .flex.gap-2 {
        flex-wrap: wrap;
    }

    .no-image-container {
        height: 200px;
    }

    .no-image-icon {
        font-size: 3rem;
    }
}

/* Dark mode support */
.p-dark .card {
    background: var(--surface-card);
    border-color: var(--surface-border);
}

.p-dark .no-image-container {
    background: var(--surface-section);
    border-color: var(--surface-border);
}

/* File upload area styling */
.p-fileupload .p-fileupload-buttonbar {
    background: transparent;
    border: none;
    padding: 0;
}

.p-fileupload .p-fileupload-content {
    border: none;
    padding: 0;
}

/* Enhanced button styling */
.p-button.p-button-outlined {
    background: transparent;
}

.p-button.p-button-outlined:hover {
    background: var(--primary-color);
    color: var(--primary-color-text);
}

/* Table enhancements */
.p-datatable .p-datatable-tbody > tr > td {
    padding: 0.75rem;
}

.p-datatable .p-datatable-thead > tr > th {
    padding: 0.75rem;
    font-weight: 600;
}

/* Dialog enhancements */
.p-dialog .p-dialog-header {
    padding: 1.5rem 1.5rem 1rem 1.5rem;
}

.p-dialog .p-dialog-content {
    padding: 0 1.5rem 1rem 1.5rem;
}

.p-dialog .p-dialog-footer {
    padding: 1rem 1.5rem 1.5rem 1.5rem;
}

/* Tag enhancements */
.p-tag {
    font-size: 0.75rem;
    font-weight: 600;
}

/* Input field enhancements */
.p-inputtext:focus {
    box-shadow:
        0 0 0 2px var(--primary-color-text),
        0 0 0 4px var(--primary-color);
}

/* Select dropdown enhancements */
.p-select:focus {
    box-shadow:
        0 0 0 2px var(--primary-color-text),
        0 0 0 4px var(--primary-color);
}

/* Toast enhancements */
.p-toast .p-toast-message {
    border-radius: 8px;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

/* Loading spinner */
.p-progress-spinner {
    width: 2rem;
    height: 2rem;
}

/* Custom scrollbar */
.p-datatable .p-datatable-wrapper::-webkit-scrollbar {
    height: 8px;
}

.p-datatable .p-datatable-wrapper::-webkit-scrollbar-track {
    background: var(--surface-ground);
}

.p-datatable .p-datatable-wrapper::-webkit-scrollbar-thumb {
    background: var(--surface-border);
    border-radius: 4px;
}

.p-datatable .p-datatable-wrapper::-webkit-scrollbar-thumb:hover {
    background: var(--surface-border-hover);
}

/* Validation error styling */
.text-red-500 {
    color: #ef4444;
    font-size: 0.875rem;
    margin-top: 0.25rem;
    display: block;
}

/* Required field indicator */
.font-bold:has(+ .text-red-500)::after {
    content: ' *';
    color: #ef4444;
}

/* File upload validation styling */
.p-fileupload.p-invalid .p-button {
    border-color: #ef4444;
}

/* Optional field styling */
.text-gray-500.font-normal {
    font-weight: 400;
    font-size: 0.875rem;
}
</style>

