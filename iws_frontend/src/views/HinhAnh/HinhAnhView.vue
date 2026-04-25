<script setup>
import { FilterMatchMode } from '@primevue/core/api';
import axios from 'axios';
import { useToast } from 'primevue/usetoast';
import { onMounted, ref } from 'vue';

const toast = useToast();
const dt = ref();
const ListHinhAnh = ref([]);
const hinhAnhDialog = ref(false);
const deleteHinhAnhDialog = ref(false);
const deleteHinhAnhsDialog = ref(false);
const hinhAnh = ref({});
const selectedHinhAnh = ref();
const submitted = ref(false);
const filters = ref({
    global: { value: null, matchMode: FilterMatchMode.CONTAINS }
});
const statuses = ref([
    { label: 'Đã load', value: 1 },
    { label: 'Đang load', value: 0 }
]);

const confirmAddDialog = ref(false);


// CÁC REF CHO UPLOAD FILE
const fileInput = ref();
const selectedFile = ref(null);
const selectedFileName = ref('');
const imagePreview = ref('');
const uploading = ref(false);

// CÁC REF CHO PREVIEW HÌNH ẢNH
const imagePreviewDialog = ref(false);
const previewImageSrc = ref('');
const previewImageName = ref('');
const previewImagePath = ref('');

onMounted(() => {
    fetchData();
});

// Hàm tính toán số thứ tự với pagination
function getRowIndex(index) {
    // Lấy thông tin pagination từ DataTable
    const currentPage = dt.value ? dt.value.d_first / dt.value.d_rows : 0;
    const rowsPerPage = dt.value ? dt.value.d_rows : 10;
    return currentPage * rowsPerPage + index + 1;
}

async function fetchData() {
    try {
        const res = await axios.get('http://localhost:8080/hinh-anh');
        ListHinhAnh.value = res.data;
    } catch (error) {
        console.error('Error fetching data:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Không thể tải danh sách hình ảnh',
            life: 3000
        });
    }
}

// Hàm tự động gen mã hình ảnh
function createId() {
    let id = '';
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    for (let i = 0; i < 8; i++) {
        id += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return 'HA' + id;
}

function handleAddHinhAnhConfirm() {
  saveHinhAnh();              // gọi API thêm hình ảnh
  confirmAddDialog.value = false; // tắt dialog confirm
}
function handleUpdateHinhAnhConfirm() {
  editHinhAnh();              // gọi API cập nhật hình ảnh
  confirmUpdateDialog.value = false; // tắt dialog confirm
}

// HÀM openNew ĐƠN GIẢN
function openNew() {
    hinhAnh.value = {
        maHinhAnh: createId(),
        tenHinhAnh: '',
        trangThai: 1
    };

    clearFile();
    submitted.value = false;
    hinhAnhDialog.value = true;
}

function hideDialog() {
    hinhAnhDialog.value = false;
    submitted.value = false;
    clearFile();
}

// XỬ LÝ FILE
function handleFileSelect(event) {
    const file = event.target.files[0];
    if (!file) return;

    // Kiểm tra loại file
    if (!file.type.startsWith('image/')) {
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Vui lòng chọn file hình ảnh (JPG, PNG, GIF)',
            life: 3000
        });
        return;
    }

    // Kiểm tra kích thước file (5MB)
    if (file.size > 5 * 1024 * 1024) {
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'File không được vượt quá 5MB',
            life: 3000
        });
        return;
    }

    selectedFile.value = file;
    selectedFileName.value = file.name;

    // CHỈ TỰ ĐỘNG ĐIỀN TÊN FILE
    hinhAnh.value.tenHinhAnh = file.name;

    // Tạo preview
    const reader = new FileReader();
    reader.onload = (e) => {
        imagePreview.value = e.target.result;
    };
    reader.readAsDataURL(file);
}

function clearFile() {
    selectedFile.value = null;
    selectedFileName.value = '';
    imagePreview.value = '';
    hinhAnh.value.tenHinhAnh = '';
    if (fileInput.value) {
        fileInput.value.value = '';
    }
}

// UPLOAD FILE
async function uploadFile(file) {
    try {
        const formData = new FormData();
        formData.append('file', file);

        const response = await axios.post('http://localhost:8080/hinh-anh/upload', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });

        return response.data.path;
    } catch (error) {
        console.error('Error uploading file:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Upload file thất bại',
            life: 3000
        });
        return null;
    }
}

// SAVE ĐƠN GIẢN HÓA
async function saveHinhAnh() {
    submitted.value = true;

    // KIỂM TRA CƠ BẢN
    if (!hinhAnh.value.maHinhAnh?.trim()) {
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Mã hình ảnh là bắt buộc',
            life: 3000
        });
        return;
    }

    if (!hinhAnh.value.tenHinhAnh?.trim()) {
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Tên hình ảnh là bắt buộc',
            life: 3000
        });
        return;
    }

    // NẾU LÀ THÊM MỚI, BẮT BUỘC PHẢI CÓ FILE
    if (!hinhAnh.value.id && !selectedFile.value) {
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Vui lòng chọn file hình ảnh',
            life: 3000
        });
        return;
    }

    try {
        uploading.value = true;

        // NẾU CÓ FILE MỚI, UPLOAD TRƯỚC
        if (selectedFile.value) {
            const uploadedPath = await uploadFile(selectedFile.value);
            if (uploadedPath) {
                hinhAnh.value.duongDan = uploadedPath;
            } else {
                toast.add({
                    severity: 'error',
                    summary: 'Lỗi',
                    detail: 'Upload file thất bại',
                    life: 3000
                });
                return;
            }
        }

        if (hinhAnh.value.id) {
            // CẬP NHẬT
            await axios.put(`http://localhost:8080/hinh-anh/${hinhAnh.value.id}`, hinhAnh.value);
            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: 'Cập nhật hình ảnh thành công',
                life: 3000
            });
        } else {
            // THÊM MỚI
            await axios.post('http://localhost:8080/hinh-anh', hinhAnh.value);
            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: 'Tạo hình ảnh thành công',
                life: 3000
            });
        }

        fetchData();
        hinhAnhDialog.value = false;
        hinhAnh.value = {};
        clearFile();
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

// EDIT ĐƠN GIẢN
function editHinhAnh(ha) {
    hinhAnh.value = { ...ha };

    // Reset file upload khi edit
    selectedFile.value = null;
    selectedFileName.value = '';
    imagePreview.value = '';

    // Hiển thị hình ảnh hiện có nếu có
    if (ha.duongDan) {
        imagePreview.value = `http://localhost:8080${ha.duongDan}`;
        selectedFileName.value = ha.tenHinhAnh;
    }

    hinhAnhDialog.value = true;
}

function confirmDeleteHinhAnh(ha) {
    hinhAnh.value = ha;
    deleteHinhAnhDialog.value = true;
}

async function deleteHinhAnh() {
    try {
        await axios.delete(`http://localhost:8080/hinh-anh/${hinhAnh.value.id}`);
        fetchData();
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
            await axios.delete(`http://localhost:8080/hinh-anh/${ha.id}`);
        }
        fetchData();
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

async function changeStatus(ha) {
    try {
        const updatedHinhAnh = { ...ha, trangThai: ha.trangThai === 1 ? 0 : 1 };
        await axios.put(`http://localhost:8080/hinh-anh/${ha.id}`, updatedHinhAnh);
        fetchData();
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

// XEM HÌNH ẢNH FULL SIZE
function previewImage(imageData) {
    previewImageSrc.value = `http://localhost:8080${imageData.duongDan}`;
    previewImageName.value = imageData.tenHinhAnh;
    previewImagePath.value = imageData.duongDan;
    imagePreviewDialog.value = true;
}

function handleImageError(event) {
    event.target.src = '/images/placeholder.png';
    event.target.onerror = null;
}

function getStatusLabel(status) {
    return status === 1 ? 'success' : 'danger';
}

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

        const headers = ['STT', 'Mã Hình Ảnh', 'Tên File', 'Trạng Thái'];

        const csvData = ListHinhAnh.value.map((item, index) => {
            return [
                // item.id || '',
                index + 1,
                item.maHinhAnh || '',
                item.tenHinhAnh || '',
                item.trangThai === 1 ? 'Đã load' : 'Đang load'
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

<template>
    <div class="card">
        <Toast />
        <Toolbar class="mb-6">
            <template #start>
                <Button label="Thêm mới" icon="pi pi-plus" severity="secondary" class="mr-2" @click="openNew" />
                <Button label="Xóa" icon="pi pi-trash" severity="secondary" @click="confirmDeleteSelected" :disabled="!selectedHinhAnh || !selectedHinhAnh.length" />
            </template>
            <template #end>
                <Button label="Xuất CSV" icon="pi pi-upload" severity="secondary" @click="exportCSV" />
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
            paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
            :rowsPerPageOptions="[5, 10, 25]"
            currentPageReportTemplate="Hiển thị {first} đến {last} của {totalRecords} hình ảnh"
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
            <!-- <Column field="id" header="ID" sortable style="min-width: 6rem"></Column> -->

            <Column field="STT" header="STT" sortable style="min-width: 8rem">
                <template #body="slotProps">
                    {{ getRowIndex(slotProps.index) }}
                </template>
            </Column>
            <!-- CỘT HÌNH ẢNH -->
            <Column header="Hình ảnh" style="min-width: 11rem">
                <template #body="slotProps">
                    <div class="justify flex">
                        <img
                            :src="`http://localhost:8080${slotProps.data.duongDan}`"
                            :alt="slotProps.data.tenHinhAnh"
                            class="h-20 w-20 cursor-pointer rounded border object-cover shadow-sm transition-transform hover:scale-105"
                            @click="previewImage(slotProps.data)"
                            @error="handleImageError($event)"
                        />
                    </div>
                </template>
            </Column>
            <Column field="maHinhAnh" header="Mã Hình Ảnh" sortable style="min-width: 12rem"></Column>
            <Column field="tenHinhAnh" header="Tên File" sortable style="min-width: 20rem"></Column>
            <!-- <Column field="trangThai" header="Trạng Thái" sortable style="min-width: 12rem">
                <template #body="slotProps">
                    <Tag :value="slotProps.data.trangThai === 1 ? 'Đã load' : 'Đang load'" :severity="getStatusLabel(slotProps.data.trangThai)" />
                </template>
            </Column> -->
            <Column :exportable="false" style="width: 12rem">
                <template #body="slotProps">
                    <div class="flex justify-center gap-2">
                        <Button icon="pi pi-pencil" outlined rounded size="small" @click="editHinhAnh(slotProps.data)" v-tooltip.top="'Chỉnh sửa'" />
                        <Button icon="pi pi-trash" outlined rounded severity="danger" size="small" @click="confirmDeleteHinhAnh(slotProps.data)" v-tooltip.top="'Xóa'" />
                        <!-- <Button icon="pi pi-refresh" outlined rounded severity="secondary" size="small" @click="changeStatus(slotProps.data)" v-tooltip.top="'Đổi trạng thái'" /> -->
                    </div>
                </template>
            </Column>
        </DataTable>

        <!-- DIALOG THÊM/SỬA HÌNH ẢNH ĐƠN GIẢN -->
        <Dialog v-model:visible="hinhAnhDialog" :style="{ width: '500px' }" header="Chi tiết Hình Ảnh" :modal="true">
            <div class="flex flex-col gap-6">
                <div>
                    <label for="maHinhAnh" class="mb-3 block font-bold">Mã Hình Ảnh</label>
                    <InputText id="maHinhAnh" v-model.trim="hinhAnh.maHinhAnh" required="true" autofocus :invalid="submitted && !hinhAnh.maHinhAnh" fluid readonly="true" />
                    <small v-if="submitted && !hinhAnh.maHinhAnh" class="text-red-500">Mã Hình Ảnh là bắt buộc.</small>
                </div>

                <!-- PHẦN UPLOAD FILE -->
                <div>
                    <label class="mb-3 block font-bold">Chọn hình ảnh</label>
                    <div class="cursor-pointer rounded-lg border-2 border-dashed border-gray-300 p-6 text-center transition-colors hover:border-blue-400" @click="$refs.fileInput.click()">
                        <input type="file" ref="fileInput" @change="handleFileSelect" accept="image/*" class="hidden" />

                        <!-- Hiển thị hình ảnh preview -->
                        <div v-if="imagePreview" class="mb-4">
                            <img :src="imagePreview" alt="Preview" class="mx-auto max-h-48 max-w-full rounded border shadow-sm" />
                            <p class="mt-2 text-sm text-gray-600">{{ selectedFileName }}</p>
                        </div>

                        <!-- Nút chọn file -->
                        <div v-else class="mb-4">
                            <i class="pi pi-cloud-upload mb-4 text-4xl text-gray-400"></i>
                            <p class="text-gray-600">Nhấn để chọn hình ảnh</p>
                            <p class="text-sm text-gray-400">JPG, PNG, GIF (Tối đa 5MB)</p>
                        </div>

                        <div class="flex justify-center gap-2" @click.stop>
                            <Button label="Chọn file" icon="pi pi-upload" @click="$refs.fileInput.click()" severity="secondary" />
                            <Button v-if="imagePreview" label="Xóa" icon="pi pi-times" @click="clearFile" severity="danger" outlined />
                        </div>
                    </div>
                </div>

                <!-- <div>
                    <label for="trangThai" class="mb-3 block font-bold">Trạng Thái</label>
                    <Select id="trangThai" v-model="hinhAnh.trangThai" :options="statuses" optionLabel="label" optionValue="value" placeholder="Chọn trạng thái" fluid />
                </div> -->
            </div>
            <template #footer>
                <Button label="Hủy" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Lưu" icon="pi pi-check" @click="confirmAddDialog = true" :loading="uploading" />
            </template>
        </Dialog>

        <!-- DIALOG XEM HÌNH ẢNH FULL SIZE -->
        <Dialog v-model:visible="imagePreviewDialog" :style="{ width: '800px' }" header="Xem hình ảnh" :modal="true">
            <div class="text-center">
                <img :src="previewImageSrc" :alt="previewImageName" class="max-h-96 max-w-full rounded object-contain shadow" />
                <div class="mt-4 text-sm text-gray-600">
                    <p><strong>Tên file:</strong> {{ previewImageName }}</p>
                    <p><strong>Đường dẫn:</strong> {{ previewImagePath }}</p>
                </div>
            </div>
            <template #footer>
                <Button label="Đóng" icon="pi pi-times" @click="imagePreviewDialog = false" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteHinhAnhDialog" :style="{ width: '450px' }" header="Xác nhận" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span v-if="hinhAnh"
                    >Bạn có chắc muốn xóa hình ảnh <b>{{ hinhAnh.tenHinhAnh }}</b
                    >?</span
                >
            </div>
            <template #footer>
                <Button label="Không" icon="pi pi-times" text @click="deleteHinhAnhDialog = false" />
                <Button label="Có" icon="pi pi-check" @click="deleteHinhAnh" />
            </template>
        </Dialog>

        <Dialog v-model:visible="confirmAddDialog" header="Xác nhận" modal>
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl text-red-500" />
                <div>
                    <p v-if="hinhAnh" class="mb-2">
                        Bạn có chắc chắn muốn thực hiện hành động này ?
                    </p>
                </div>
            </div>
            <template #footer>
                <Button label="Hủy bỏ" icon="pi pi-times" text @click="confirmAddDialog = false" :disabled="loading" />
                <Button label="Thực hiện" icon="pi pi-check" severity="success" @click="handleAddHinhAnhConfirm" :loading="loading" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteHinhAnhsDialog" :style="{ width: '450px' }" header="Xác nhận" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span>Bạn có chắc muốn xóa các hình ảnh đã chọn?</span>
            </div>
            <template #footer>
                <Button label="Không" icon="pi pi-times" text @click="deleteHinhAnhsDialog = false" />
                <Button label="Có" icon="pi pi-check" text @click="deleteSelectedHinhAnhs" />
            </template>
        </Dialog>
    </div>
</template>

<style scoped>
.card {
    border: none;
    box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}
</style>
