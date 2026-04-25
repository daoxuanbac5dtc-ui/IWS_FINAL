<script setup>
import { ref, onMounted } from 'vue';
import { useToast } from 'primevue/usetoast';
import axios from 'axios';

// Cấu hình API base URL
const API_BASE_URL = 'http://localhost:8080';

// State
const toast = useToast();
const dt = ref();
const images = ref([]);
const imageDialog = ref(false);
const deleteImageDialog = ref(false);
const deleteImagesDialog = ref(false);
const image = ref({});
const selectedImages = ref([]);
const loading = ref(false);
const submitted = ref(false);
const chiTietSanPhams = ref([]);
const galleriaResponsiveOptions = ref([
    { breakpoint: '1024px', numVisible: 5 },
    { breakpoint: '960px', numVisible: 4 },
    { breakpoint: '768px', numVisible: 3 },
    { breakpoint: '560px', numVisible: 1 }
]);
const statuses = ref([
    { label: 'HOẠT ĐỘNG', value: 1 },
    { label: 'NGỪNG HOẠT ĐỘNG', value: 0 }
]);
const uploadedFile = ref(null);

// Load dữ liệu khi component mount
onMounted(async () => {
    await Promise.all([loadImages(), loadChiTietSanPhams()]);
});

// API calls
async function loadImages() {
    try {
        loading.value = true;
        console.log('Gọi API:', `${API_BASE_URL}/api/hinh-anh`);
        const response = await axios.get(`${API_BASE_URL}/api/hinh-anh`, { timeout: 5000 });
        console.log('Phản hồi API:', response.data);
        images.value = response.data.map((item) => ({
            ...item,
            url: `${API_BASE_URL}/images/${item.tenHinhAnh}`,
            chiTietSanPhamName: item.chiTietSanPham?.maChiTiet || 'N/A',
            createdAt: item.ngayTao ? new Date(item.ngayTao).toLocaleDateString('vi-VN') : 'N/A',
            updatedAt: item.ngayCapNhat ? new Date(item.ngayCapNhat).toLocaleDateString('vi-VN') : 'N/A'
        }));
        if (images.value.length === 0) {
            toast.add({ severity: 'info', summary: 'Thông báo', detail: 'Không có hình ảnh nào', life: 3000 });
        }
    } catch (error) {
        console.error('Lỗi khi tải hình ảnh:', error);
        toast.add({ severity: 'error', summary: 'Lỗi', detail: `Không thể tải danh sách hình ảnh: ${error.message}`, life: 3000 });
    } finally {
        loading.value = false;
    }
}

async function loadChiTietSanPhams() {
    try {
        const response = await axios.get(`${API_BASE_URL}/san-pham-chi-tiet`, { timeout: 5000 });
        chiTietSanPhams.value = response.data;
    } catch (error) {
        console.error('Lỗi khi tải chi tiết sản phẩm:', error);
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải danh sách chi tiết sản phẩm', life: 3000 });
    }
}

// Utility functions
function createId() {
    let id = '';
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    for (let i = 0; i = 8; i++) {
        id += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return 'HA' + id;
}

function getStatusLabel(status) {
    return status === 1 ? 'success' : 'danger';
}

// Dialog functions
function openNew() {
    image.value = {
        maHinhAnh: createId(),
        tenHinhAnh: '',
        trangThai: 1,
        chiTietSanPham: null
    };
    uploadedFile.value = null;
    submitted.value = false;
    imageDialog.value = true;
}

function editImage(img) {
    image.value = {
        id: img.id,
        maHinhAnh: img.maHinhAnh,
        tenHinhAnh: img.tenHinhAnh,
        trangThai: img.trangThai,
        chiTietSanPham: img.chiTietSanPham
    };
    uploadedFile.value = null;
    submitted.value = false;
    imageDialog.value = true;
}

function hideDialog() {
    imageDialog.value = false;
    submitted.value = false;
    image.value = {};
    uploadedFile.value = null;
}

async function saveImage() {
    submitted.value = true;
    if (!image.value.maHinhAnh?.trim() || !image.value.chiTietSanPham || (!uploadedFile.value && !image.value.tenHinhAnh)) {
        toast.add({ severity: 'warn', summary: 'Cảnh báo', detail: 'Vui lòng điền đầy đủ thông tin và chọn file ảnh', life: 3000 });
        return;
    }

    try {
        loading.value = true;
        const formData = new FormData();
        formData.append('maHinhAnh', image.value.maHinhAnh);
        formData.append('trangThai', image.value.trangThai.toString());
        formData.append('idCtsp', image.value.chiTietSanPham.id.toString());
        if (uploadedFile.value) {
            formData.append('file', uploadedFile.value);
        } else {
            formData.append('tenHinhAnh', image.value.tenHinhAnh);
        }

        if (image.value.id) {
            await axios.put(`${API_BASE_URL}/api/hinh-anh/update/${image.value.id}`, formData, {
                headers: { 'Content-Type': 'multipart/form-data' },
                timeout: 10000
            });
            toast.add({ severity: 'success', summary: 'Thành công', detail: `Đã cập nhật hình ảnh "${image.value.maHinhAnh}"`, life: 3000 });
        } else {
            await axios.post(`${API_BASE_URL}/api/hinh-anh/save`, formData, {
                headers: { 'Content-Type': 'multipart/form-data' },
                timeout: 10000
            });
            toast.add({ severity: 'success', summary: 'Thành công', detail: `Đã thêm hình ảnh "${image.value.maHinhAnh}"`, life: 3000 });
        }

        await loadImages();
        hideDialog();
    } catch (error) {
        console.error('Lỗi khi lưu hình ảnh:', error);
        toast.add({ severity: 'error', summary: 'Lỗi', detail: `Không thể lưu hình ảnh: ${error.response?.data?.message || error.message}`, life: 3000 });
    } finally {
        loading.value = false;
    }
}

function confirmDeleteImage(img) {
    image.value = img;
    deleteImageDialog.value = true;
}

async function deleteImage() {
    try {
        loading.value = true;
        await axios.delete(`${API_BASE_URL}/api/hinh-anh/delete/${image.value.id}`, { timeout: 5000 });
        toast.add({ severity: 'success', summary: 'Thành công', detail: `Đã xóa hình ảnh "${image.value.maHinhAnh}"`, life: 3000 });
        await loadImages();
        deleteImageDialog.value = false;
        image.value = {};
    } catch (error) {
        console.error('Lỗi khi xóa hình ảnh:', error);
        toast.add({ severity: 'error', summary: 'Lỗi', detail: `Không thể xóa hình ảnh: ${error.response?.data?.message || error.message}`, life: 3000 });
    } finally {
        loading.value = false;
    }
}

function confirmDeleteSelected() {
    deleteImagesDialog.value = true;
}

async function deleteSelectedImages() {
    try {
        loading.value = true;
        const deletePromises = selectedImages.value.map((img) => axios.delete(`${API_BASE_URL}/api/hinh-anh/delete/${img.id}`, { timeout: 5000 }));
        await Promise.all(deletePromises);
        toast.add({ severity: 'success', summary: 'Thành công', detail: `Đã xóa ${selectedImages.value.length} hình ảnh`, life: 3000 });
        await loadImages();
        deleteImagesDialog.value = false;
        selectedImages.value = [];
    } catch (error) {
        console.error('Lỗi khi xóa nhiều hình ảnh:', error);
        toast.add({ severity: 'error', summary: 'Lỗi', detail: `Không thể xóa hình ảnh: ${error.response?.data?.message || error.message}`, life: 3000 });
    } finally {
        loading.value = false;
    }
}

function onFileSelect(event) {
    uploadedFile.value = event.files[0];
    image.value.tenHinhAnh = uploadedFile.value.name;
}

function exportCSV() {
    dt.value.exportCSV();
}
</script>

<template>
    <div>
        <Toast />
        <div class="card">
            <Toolbar class="mb-6">
                <template #start>
                    <div class="flex gap-2">
                        <Button label="Thêm mới" icon="pi pi-plus" severity="secondary" @click="openNew" :loading="loading" />
                        <Button label="Xóa đã chọn" icon="pi pi-trash" severity="danger" @click="confirmDeleteSelected" :disabled="!selectedImages || !selectedImages.length" :loading="loading" />
                    </div>
                </template>
                <template #end>
                    <Button label="Xuất CSV" icon="pi pi-download" severity="secondary" @click="exportCSV" />
                </template>
            </Toolbar>

            <DataTable
                ref="dt"
                v-model:selection="selectedImages"
                :value="images"
                dataKey="id"
                :paginator="true"
                :rows="10"
                :loading="loading"
                paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageReport RowsPerPageDropdown"
                :rowsPerPageOptions="[5, 10, 25]"
                currentPageReportTemplate="Hiển thị {first} đến {last} trong tổng số {totalRecords} hình ảnh"
                tableStyle="min-width: 60rem"
            >
                <template #header>
                    <div class="flex flex-wrap gap-2 items-center justify-between">
                        <h4 class="m-0">Quản lý Hình ảnh</h4>
                        <IconField>
                            <InputIcon>
                                <i class="pi pi-search" />
                            </InputIcon>
                            <InputText placeholder="Tìm kiếm hình ảnh..." />
                        </IconField>
                    </div>
                </template>

                <Column selectionMode="multiple" style="width: 3rem" :exportable="false"></Column>
                <Column field="maHinhAnh" header="Mã hình ảnh" sortable style="min-width: 10rem"></Column>
                <Column field="tenHinhAnh" header="Tên file" sortable style="min-width: 12rem"></Column>
                <Column header="Ảnh" style="min-width: 10rem">
                    <template #body="slotProps">
                        <Image :src="slotProps.data.url" alt="Hình ảnh" width="100" preview />
                    </template>
                </Column>
                <Column field="chiTietSanPhamName" header="Chi tiết sản phẩm" sortable style="min-width: 12rem"></Column>
                <Column field="trangThai" header="Trạng thái" sortable style="min-width: 10rem">
                    <template #body="slotProps">
                        <Tag :value="slotProps.data.trangThai === 1 ? 'HOẠT ĐỘNG' : 'NGỪNG'" :severity="getStatusLabel(slotProps.data.trangThai)" />
                    </template>
                </Column>
                <Column field="createdAt" header="Ngày tạo" sortable style="min-width: 10rem"></Column>
                <Column field="updatedAt" header="Ngày cập nhật" sortable style="min-width: 10rem"></Column>
                <Column :exportable="false" style="min-width: 8rem">
                    <template #body="slotProps">
                        <Button icon="pi pi-pencil" outlined rounded class="mr-2" @click="editImage(slotProps.data)" v-tooltip.top="'Chỉnh sửa'" :disabled="loading" />
                        <Button icon="pi pi-trash" outlined rounded severity="danger" @click="confirmDeleteImage(slotProps.data)" v-tooltip.top="'Xóa'" :disabled="loading" />
                    </template>
                </Column>
            </DataTable>
        </div>

        <!-- Dialog thêm/sửa hình ảnh -->
        <Dialog v-model:visible="imageDialog" :style="{ width: '600px' }" header="Chi tiết Hình ảnh" :modal="true" class="p-fluid">
            <div class="flex flex-col gap-6">
                <div class="grid grid-cols-12 gap-4">
                    <div class="col-span-6">
                        <label for="maHinhAnh" class="block font-bold mb-3">Mã hình ảnh *</label>
                        <InputText id="maHinhAnh" v-model.trim="image.maHinhAnh" required="true" autofocus :invalid="submitted && !image.maHinhAnh" fluid placeholder="Nhập mã hình ảnh" :disabled="!!image.id" />
                        <small v-if="submitted && !image.maHinhAnh" class="text-red-500">Mã hình ảnh là bắt buộc.</small>
                    </div>
                    <div class="col-span-6">
                        <label for="trangThai" class="block font-bold mb-3">Trạng thái *</label>
                        <Select id="trangThai" v-model="image.trangThai" :options="statuses" optionLabel="label" optionValue="value" placeholder="Chọn trạng thái" fluid :invalid="submitted && image.trangThai == null" />
                        <small v-if="submitted && image.trangThai == null" class="text-red-500">Trạng thái là bắt buộc.</small>
                    </div>
                </div>

                <div class="grid grid-cols-12 gap-4">
                    <div class="col-span-12">
                        <label for="chiTietSanPham" class="block font-bold mb-3">Chi tiết sản phẩm *</label>
                        <Select id="chiTietSanPham" v-model="image.chiTietSanPham" :options="chiTietSanPhams" optionLabel="maChiTiet" placeholder="Chọn chi tiết sản phẩm" fluid :invalid="submitted && !image.chiTietSanPham" />
                        <small v-if="submitted && !image.chiTietSanPham" class="text-red-500">Chi tiết sản phẩm là bắt buộc.</small>
                    </div>
                </div>

                <div class="grid grid-cols-12 gap-4">
                    <div class="col-span-12">
                        <label for="file" class="block font-bold mb-3">File ảnh {{ image.id ? '(Tùy chọn)' : '*' }}</label>
                        <FileUpload
                            mode="basic"
                            accept="image/*"
                            :maxFileSize="5000000"
                            @select="onFileSelect"
                            :auto="false"
                            chooseLabel="Chọn file"
                            :invalid="submitted && !image.tenHinhAnh && !uploadedFile"
                        />
                        <small v-if="submitted && !image.tenHinhAnh && !uploadedFile" class="text-red-500">File ảnh là bắt buộc khi thêm mới.</small>
                        <div v-if="image.tenHinhAnh && image.id" class="mt-2">
                            <Image :src="`${API_BASE_URL}/images/${image.tenHinhAnh}`" alt="Hình ảnh hiện tại" width="100" preview />
                        </div>
                    </div>
                </div>
            </div>

            <template #footer>
                <Button label="Hủy bỏ" icon="pi pi-times" text @click="hideDialog" :disabled="loading" />
                <Button label="Lưu lại" icon="pi pi-check" @click="saveImage" :loading="loading || submitted" />
            </template>
        </Dialog>

        <!-- Dialog xác nhận xóa hình ảnh -->
        <Dialog v-model:visible="deleteImageDialog" :style="{ width: '450px' }" header="Xác nhận xóa" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl text-red-500" />
                <div>
                    <p v-if="image" class="mb-2">
                        Bạn có chắc chắn muốn xóa hình ảnh <strong>{{ image.maHinhAnh }}</strong>?
                    </p>
                    <small class="text-gray-500">Hành động này không thể hoàn tác.</small>
                </div>
            </div>
            <template #footer>
                <Button label="Hủy bỏ" icon="pi pi-times" text @click="deleteImageDialog = false" :disabled="loading" />
                <Button label="Xóa" icon="pi pi-trash" severity="danger" @click="deleteImage" :loading="loading" />
            </template>
        </Dialog>

        <!-- Dialog xác nhận xóa nhiều hình ảnh -->
        <Dialog v-model:visible="deleteImagesDialog" :style="{ width: '450px' }" header="Xác nhận xóa nhiều" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl text-red-500" />
                <div>
                    <p>
                        Bạn có chắc chắn muốn xóa <strong>{{ selectedImages?.length || 0 }}</strong> hình ảnh đã chọn?
                    </p>
                    <small class="text-gray-500">Hành động này không thể hoàn tác.</small>
                </div>
            </div>
            <template #footer>
                <Button label="Hủy bỏ" icon="pi pi-times" text @click="deleteImagesDialog = false" :disabled="loading" />
                <Button label="Xóa tất cả" icon="pi pi-trash" severity="danger" @click="deleteSelectedImages" :loading="loading" />
            </template>
        </Dialog>
    </div>
</template>

<style scoped>
.card {
    background: var(--surface-card);
    padding: 2rem;
    border-radius: 10px;
    margin-bottom: 1rem;
}
</style>