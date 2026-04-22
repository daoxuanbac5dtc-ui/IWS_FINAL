<template>
    <div class="card">
        <Toast />
        <Toolbar class="mb-6">
            <template #start>
                <Button label="Thêm mới" icon="pi pi-plus" severity="secondary" class="mr-2" @click="openNew" />
                <Button label="Xóa" icon="pi pi-trash" severity="secondary" @click="confirmDeleteSelected" :disabled="!selectedThuongHieu || !selectedThuongHieu.length" />
            </template>
            <template #end>
                <Button label="Xuất CSV" icon="pi pi-upload" severity="secondary" @click="exportCSV" />
            </template>
        </Toolbar>

        <DataTable
            ref="dt"
            v-model:selection="selectedThuongHieu"
            :value="ListThuongHieu"
            dataKey="id"
            :paginator="true"
            :rows="10"
            :filters="filters"
            paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
            :rowsPerPageOptions="[5, 10, 25]"
            currentPageReportTemplate="Hiển thị {first} đến {last} của {totalRecords} thương hiệu"
        >
            <template #header>
                <div class="flex flex-wrap gap-2 items-center justify-between">
                    <h4 class="m-0">📋 Quản lý Thương Hiệu</h4>
                    <IconField>
                        <InputIcon>
                            <i class="pi pi-search" />
                        </InputIcon>
                        <InputText v-model="filters['global'].value" placeholder="Tìm kiếm..." />
                    </IconField>
                </div>
            </template>

            <Column selectionMode="multiple" style="width: 3rem" :exportable="false"></Column>
            <!-- <Column field="id" header="ID" sortable style="min-width: 8rem"></Column> -->
             <Column field="STT" header="STT" sortable style="min-width: 8rem">
                <template #body="slotProps">
                    {{ getRowIndex(slotProps.index) }}
                </template>
            </Column>
            <Column field="maThuongHieu" header="Mã Thương Hiệu" sortable style="min-width: 12rem"></Column>
            <Column field="tenThuongHieu" header="Tên Thương Hiệu" sortable style="min-width: 16rem"></Column>
            <Column field="trangThai" header="Trạng Thái" sortable style="min-width: 12rem">
                <template #body="slotProps">
                    <Tag :value="slotProps.data.trangThai === 1 ? 'Hoạt động' : 'Ngừng hoạt động'" :severity="getStatusLabel(slotProps.data.trangThai)" />
                </template>
            </Column>
            <Column :exportable="false" style="width: 10rem">
                <template #body="slotProps">
                    <div class="flex justify-between gap-2">
                        <Button icon="pi pi-pencil" outlined rounded size="small" @click="editThuongHieu(slotProps.data)" />
                        <Button icon="pi pi-trash" outlined rounded severity="danger" size="small" @click="confirmDeleteThuongHieu(slotProps.data)" />
                        <Button icon="pi pi-refresh" outlined rounded severity="secondary" size="small" @click="changeStatus(slotProps.data)" />
                    </div>
                </template>
            </Column>
        </DataTable>

        <Dialog v-model:visible="thuongHieuDialog" :style="{ width: '450px' }" header="Chi Tiết Thương Hiệu" :modal="true">
            <div class="flex flex-col gap-6">
                <div>
                    <label for="maThuongHieu" class="block font-bold mb-3">Mã Thương Hiệu</label>
                    <InputText id="maThuongHieu" v-model.trim="thuongHieu.maThuongHieu" required="true" autofocus :invalid="submitted && !thuongHieu.maThuongHieu" fluid readonly="true" />
                    <small v-if="submitted && !thuongHieu.maThuongHieu" class="text-red-500">Mã Thương Hiệu là bắt buộc.</small>
                </div>
                <div>
                    <label for="tenThuongHieu" class="block font-bold mb-3">Tên Thương Hiệu</label>
                    <InputText id="tenThuongHieu" v-model.trim="thuongHieu.tenThuongHieu" required="true" :invalid="submitted && (!thuongHieu.tenThuongHieu || isDuplicateName)" fluid />
                    <small v-if="submitted && !thuongHieu.tenThuongHieu" class="text-red-500">Tên Thương Hiệu là bắt buộc.</small>
                </div>
                <div>
                    <label for="trangThai" class="block font-bold mb-3">Trạng Thái</label>
                    <Select id="trangThai" v-model="thuongHieu.trangThai" :options="statuses" optionLabel="label" optionValue="value" placeholder="Chọn trạng thái" fluid />
                </div>
            </div>
            <template #footer>
                <Button label="Hủy" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Lưu" icon="pi pi-check" @click="confirmAddDialog = true" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteThuongHieuDialog" :style="{ width: '450px' }" header="Xác nhận" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span v-if="thuongHieu"
                    >Bạn có chắc muốn xóa thương hiệu <b>{{ thuongHieu.tenThuongHieu }}</b
                    >?</span
                >
            </div>
            <template #footer>
                <Button label="Không" icon="pi pi-times" text @click="deleteThuongHieuDialog = false" />
                <Button label="Có" icon="pi pi-check" @click="deleteThuongHieu" />
            </template>
        </Dialog>

         <Dialog v-model:visible="confirmAddDialog" header="Xác nhận" modal>
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl text-red-500" />
                <div>
                    <p v-if="thuongHieu" class="mb-2">
                        Bạn có chắc chắn muốn thực hiện hành động này ?
                    </p>
                </div>
            </div>
            <template #footer>
                <Button label="Hủy bỏ" icon="pi pi-times" text @click="confirmAddDialog = false" :disabled="loading" />
                <Button label="Thực hiện" icon="pi pi-check" severity="success" @click="handleAddThuongHieuConfirm" :loading="loading" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteThuongHieusDialog" :style="{ width: '450px' }" header="Xác nhận" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span>Bạn có chắc muốn xóa các thương hiệu đã chọn?</span>
            </div>
            <template #footer>
                <Button label="Không" icon="pi pi-times" text @click="deleteThuongHieusDialog = false" />
                <Button label="Có" icon="pi pi-check" text @click="deleteSelectedThuongHieus" />
            </template>
        </Dialog>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useToast } from 'primevue/usetoast';
import { FilterMatchMode } from '@primevue/core/api';
import axios from 'axios';

const toast = useToast();
const dt = ref();
const ListThuongHieu = ref([]);
const thuongHieuDialog = ref(false);
const deleteThuongHieuDialog = ref(false);
const deleteThuongHieusDialog = ref(false);
const thuongHieu = ref({});
const selectedThuongHieu = ref();
const submitted = ref(false);
const filters = ref({
    global: { value: null, matchMode: FilterMatchMode.CONTAINS }
});
const statuses = ref([
    { label: 'Hoạt động', value: 1 },
    { label: 'Ngừng hoạt động', value: 0 }
]);

const confirmAddDialog = ref(false);

// Computed property to check for duplicate names
const isDuplicateName = computed(() => {
    if (!thuongHieu.value.tenThuongHieu) return false;
    
    const trimmedName = thuongHieu.value.tenThuongHieu.trim().toLowerCase();
    
    return ListThuongHieu.value.some(item => {
        // Skip checking against itself when editing
        if (thuongHieu.value.id && item.id === thuongHieu.value.id) {
            return false;
        }
        return item.tenThuongHieu && item.tenThuongHieu.trim().toLowerCase() === trimmedName;
    });
});

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
        const res = await axios.get('http://localhost:8080/thuong-hieu');
        ListThuongHieu.value = res.data;
    } catch (error) {
        console.error('Error fetching data:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Không thể tải danh sách thương hiệu',
            life: 3000
        });
    }
}

function createId() {
    let id = '';
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    for (let i = 0; i < 8; i++) {
        id += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return 'TH' + id;
}

function openNew() {
    thuongHieu.value = { 
        maThuongHieu: createId(),
        tenThuongHieu: '',
        trangThai: 1 
    };
    submitted.value = false;
    thuongHieuDialog.value = true;
}
function handleAddThuongHieuConfirm() {
  saveThuongHieu();              // gọi API thêm sản phẩm
  confirmAddDialog.value = false; // tắt dialog confirm
}


function hideDialog() {
    thuongHieuDialog.value = false;
    submitted.value = false;
}

async function saveThuongHieu() {
    submitted.value = true;

    // Check if required fields are filled and name is not duplicate
    if (thuongHieu.value.maThuongHieu?.trim() && thuongHieu.value.tenThuongHieu?.trim() && !isDuplicateName.value) {
        try {
            if (thuongHieu.value.id) {
                // Cập nhật thương hiệu
                await axios.put(`http://localhost:8080/thuong-hieu/${thuongHieu.value.id}`, thuongHieu.value);
                toast.add({
                    severity: 'success',
                    summary: 'Thành công',
                    detail: 'Cập nhật thương hiệu thành công',
                    life: 3000
                });
            } else {
                // Tạo mới thương hiệu
                // ✅ Sửa: Đảm bảo có mã thương hiệu trước khi gửi
                thuongHieu.value.maThuongHieu = thuongHieu.value.maThuongHieu || createId();
                
                await axios.post('http://localhost:8080/thuong-hieu', thuongHieu.value);
                toast.add({
                    severity: 'success',
                    summary: 'Thành công',
                    detail: 'Tạo thương hiệu thành công',
                    life: 3000
                });
            }
            
            fetchData();
            thuongHieuDialog.value = false;
            thuongHieu.value = {};
        } catch (error) {
            console.error('Error saving thương hiệu:', error);
            
            // ✅ Hiển thị lỗi chi tiết hơn
            let errorMessage = 'Lưu thương hiệu thất bại';
            
            if (error.response?.data?.message) {
                errorMessage = error.response.data.message;
            } else if (error.response?.data) {
                errorMessage = error.response.data;
            } else if (error.message) {
                errorMessage = error.message;
            }
            
            toast.add({
                severity: 'error',
                summary: 'Lỗi',
                detail: errorMessage,
                life: 5000
            });
        }
    } else {
        if (isDuplicateName.value) {
            toast.add({
                severity: 'error',
                summary: 'Lỗi',
                detail: 'Tên Thương Hiệu đã tồn tại, vui lòng chọn tên khác',
                life: 3000
            });
        } else {
            toast.add({
                severity: 'error',
                summary: 'Lỗi',
                detail: 'Vui lòng nhập đầy đủ thông tin bắt buộc',
                life: 3000
            });
        }
    }
}

function editThuongHieu(th) {
    thuongHieu.value = { ...th };
    thuongHieuDialog.value = true;
}

function confirmDeleteThuongHieu(th) {
    thuongHieu.value = th;
    deleteThuongHieuDialog.value = true;
}

async function deleteThuongHieu() {
    try {
        await axios.delete(`http://localhost:8080/thuong-hieu/${thuongHieu.value.id}`);
        fetchData();
        deleteThuongHieuDialog.value = false;
        thuongHieu.value = {};
        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: 'Xóa thương hiệu thành công',
            life: 3000
        });
    } catch (error) {
        console.error('Error deleting thương hiệu:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: error.response?.data?.message || 'Xóa thương hiệu thất bại',
            life: 3000
        });
    }
}

function confirmDeleteSelected() {
    deleteThuongHieusDialog.value = true;
}

async function deleteSelectedThuongHieus() {
    try {
        for (const th of selectedThuongHieu.value) {
            await axios.delete(`http://localhost:8080/thuong-hieu/${th.id}`);
        }
        fetchData();
        deleteThuongHieusDialog.value = false;
        selectedThuongHieu.value = null;
        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: 'Xóa các thương hiệu thành công',
            life: 3000
        });
    } catch (error) {
        console.error('Error deleting thương hiệu:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: error.response?.data?.message || 'Xóa các thương hiệu thất bại',
            life: 3000
        });
    }
}

async function changeStatus(th) {
    try {
        const updatedThuongHieu = { ...th, trangThai: th.trangThai === 1 ? 0 : 1 };
        await axios.put(`http://localhost:8080/thuong-hieu/${th.id}`, updatedThuongHieu);
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

function getStatusLabel(status) {
    return status === 1 ? 'success' : 'danger';
}

// Xuat theo dang CSV
function exportCSV() {
    try {
        // If no data, show warning
        if (!ListThuongHieu.value || ListThuongHieu.value.length === 0) {
            toast.add({
                severity: 'warn',
                summary: 'Cảnh báo',
                detail: 'Không có dữ liệu để xuất',
                life: 3000
            });
            return;
        }

        // Create CSV headers with Vietnamese labels
        const headers = ['STT', 'Mã Thương Hiệu', 'Tên Thương Hiệu', 'Trạng Thái'];

        // Convert data to CSV format
        const csvData = ListThuongHieu.value.map((item , index) => {
            return [
                // item.id || '',
                index + 1,
                item.maThuongHieu || '',
                item.tenThuongHieu || '',
                item.trangThai === 1 ? 'Hoạt động' : 'Ngừng hoạt động'
            ];
        });

        // Combine headers and data
        const csvContent = [headers, ...csvData]
            .map(row => row.map(field => {
                // Handle fields that might contain commas or quotes
                const stringField = String(field);
                if (stringField.includes(',') || stringField.includes('"') || stringField.includes('\n')) {
                    return `"${stringField.replace(/"/g, '""')}"`;
                }
                return stringField;
            }).join(','))
            .join('\n');

        // Add BOM for proper UTF-8 encoding in Excel
        const BOM = '\uFEFF';
        const csvWithBOM = BOM + csvContent;

        // Create and download file
        const blob = new Blob([csvWithBOM], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        
        if (link.download !== undefined) {
            const url = URL.createObjectURL(blob);
            link.setAttribute('href', url);
            
            // Generate filename with current date
            const now = new Date();
            const dateStr = now.toISOString().split('T')[0]; // YYYY-MM-DD format
            const filename = `ThuongHieu-${dateStr}.csv`;

            link.setAttribute('download', filename);
            link.style.visibility = 'hidden';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            
            // Show success message
            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: `Đã xuất ${ListThuongHieu.value.length} bản ghi ra file CSV`,
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
.card {
    border: none;
    box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}
</style>