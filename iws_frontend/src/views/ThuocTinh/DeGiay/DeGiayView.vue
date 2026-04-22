<template>
    <div class="card">
        <Toast />
        <Toolbar class="mb-6">
            <template #start>
                <Button label="Thêm mới" icon="pi pi-plus" severity="secondary" class="mr-2" @click="openNew" />
                <Button label="Xóa" icon="pi pi-trash" severity="secondary" @click="confirmDeleteSelected" :disabled="!selectedDeGiay || !selectedDeGiay.length" />
            </template>
            <template #end>
                <Button label="Xuất CSV" icon="pi pi-upload" severity="secondary" @click="exportCSV" />
            </template>
        </Toolbar>

        <DataTable
            ref="dt"
            v-model:selection="selectedDeGiay"
            :value="ListDeGiay"
            dataKey="id"
            :paginator="true"
            :rows="10"
            :filters="filters"
            paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
            :rowsPerPageOptions="[5, 10, 25]"
            currentPageReportTemplate="Hiển thị {first} đến {last} của {totalRecords} đế giày"
        >
            <template #header>
                <div class="flex flex-wrap gap-2 items-center justify-between">
                    <h4 class="m-0">📋 Quản lý Đế Giày</h4>
                    <IconField>
                        <InputIcon>
                            <i class="pi pi-search" />
                        </InputIcon>
                        <InputText v-model="filters['global'].value" placeholder="Tìm kiếm..." />
                    </IconField>
                </div>
            </template>

            <Column selectionMode="multiple" style="width: 3rem" :exportable="false"></Column>
            <!-- Cột STT với template để tính toán số thứ tự -->
            <Column field="STT" header="STT" sortable style="min-width: 8rem">
                <template #body="slotProps">
                    {{ getRowIndex(slotProps.index) }}
                </template>
            </Column>
            <Column field="maDeGiay" header="Mã Đế Giày" sortable style="min-width: 12rem"></Column>
            <Column field="tenDeGiay" header="Tên Đế Giày" sortable style="min-width: 16rem"></Column>
            <Column field="trangThai" header="Trạng Thái" sortable style="min-width: 12rem">
                <template #body="slotProps">
                    <Tag :value="slotProps.data.trangThai === 1 ? 'Hoạt động' : 'Ngừng hoạt động'" :severity="getStatusLabel(slotProps.data.trangThai)" />
                </template>
            </Column>
            <Column :exportable="false" style="width: 10rem">
                <template #body="slotProps">
                    <div class="flex justify-between gap-2">
                        <Button icon="pi pi-pencil" outlined rounded size="small" @click="editDeGiay(slotProps.data)" />
                        <Button icon="pi pi-trash" outlined rounded severity="danger" size="small" @click="confirmDeleteDeGiay(slotProps.data)" />
                        <Button icon="pi pi-refresh" outlined rounded severity="secondary" size="small" @click="changeStatus(slotProps.data)" />
                    </div>
                </template>
            </Column>
        </DataTable>

        <Dialog v-model:visible="deGiayDialog" :style="{ width: '450px' }" header="Chi Tiết Đế Giày" :modal="true">
            <div class="flex flex-col gap-6">
                <div>
                    <label for="maDeGiay" class="block font-bold mb-3">Mã Đế Giày</label>
                    <InputText id="maDeGiay" v-model.trim="deGiay.maDeGiay" required="true" autofocus :invalid="submitted && !deGiay.maDeGiay" fluid readonly="true" />
                    <small v-if="submitted && !deGiay.maDeGiay" class="text-red-500">Mã Đế Giày là bắt buộc.</small>
                </div>
                <div>
                    <label for="tenDeGiay" class="block font-bold mb-3">Tên Đế Giày</label>
                    <InputText id="tenDeGiay" v-model.trim="deGiay.tenDeGiay" required="true" :invalid="submitted && (!deGiay.tenDeGiay || isDuplicateName)" fluid />
                    <small v-if="submitted && !deGiay.tenDeGiay" class="text-red-500">Tên Đế Giày là bắt buộc.</small>
                </div>
                <div>
                    <label for="trangThai" class="block font-bold mb-3">Trạng Thái</label>
                    <Select id="trangThai" v-model="deGiay.trangThai" :options="statuses" optionLabel="label" optionValue="value" placeholder="Chọn trạng thái" fluid />
                </div>
            </div>
            <template #footer>
                <Button label="Hủy" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Lưu" icon="pi pi-check" @click="confirmAddDialog = true" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteDeGiayDialog" :style="{ width: '450px' }" header="Xác nhận" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span v-if="deGiay"
                    >Bạn có chắc muốn xóa đế giày <b>{{ deGiay.tenDeGiay }}</b
                    >?</span
                >
            </div>
            <template #footer>
                <Button label="Không" icon="pi pi-times" text @click="deleteDeGiayDialog = false" />
                <Button label="Có" icon="pi pi-check" @click="deleteDeGiay" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteDeGiaysDialog" :style="{ width: '450px' }" header="Xác nhận" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span>Bạn có chắc muốn xóa các đế giày đã chọn?</span>
            </div>
            <template #footer>
                <Button label="Không" icon="pi pi-times" text @click="deleteDeGiaysDialog = false" />
                <Button label="Có" icon="pi pi-check" text @click="deleteSelectedDeGiays" />
            </template>
        </Dialog>
         <Dialog v-model:visible="confirmAddDialog" header="Xác nhận" modal>
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl text-red-500" />
                <div>
                    <p v-if="deGiay" class="mb-2">
                        Bạn có chắc chắn muốn thực hiện hành động này ?
                    </p>
                </div>
            </div>
            <template #footer>
                <Button label="Hủy bỏ" icon="pi pi-times" text @click="confirmAddDialog = false" :disabled="loading" />
                <Button label="Thực hiện" icon="pi pi-check" severity="success" @click="handleAddDeGiayConfirm" :loading="loading" />
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
const ListDeGiay = ref([]);
const deGiayDialog = ref(false);
const deleteDeGiayDialog = ref(false);
const deleteDeGiaysDialog = ref(false);
const deGiay = ref({});
const selectedDeGiay = ref();
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
    if (!deGiay.value.tenDeGiay) return false;
    
    const trimmedName = deGiay.value.tenDeGiay.trim().toLowerCase();
    
    return ListDeGiay.value.some(item => {
        // Skip checking against itself when editing
        if (deGiay.value.id && item.id === deGiay.value.id) {
            return false;
        }
        return item.tenDeGiay && item.tenDeGiay.trim().toLowerCase() === trimmedName;
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
        const res = await axios.get('http://localhost:8080/de-giay');
        ListDeGiay.value = res.data;
    } catch (error) {
        console.error('Error fetching data:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Không thể tải danh sách đế giày',
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
    return 'DG' + id;
}

function openNew() {
    deGiay.value = { 
        maDeGiay: createId(),
        tenDeGiay: '',
        trangThai: 1 
    };
    submitted.value = false;
    deGiayDialog.value = true;
}

function hideDialog() {
    deGiayDialog.value = false;
    submitted.value = false;
}

async function saveDeGiay() {
    submitted.value = true;

    // Check if required fields are filled and name is not duplicate
    if (deGiay.value.maDeGiay?.trim() && deGiay.value.tenDeGiay?.trim() && !isDuplicateName.value) {
        try {
            if (deGiay.value.id) {
                await axios.put(`http://localhost:8080/de-giay/${deGiay.value.id}`, deGiay.value);
                toast.add({
                    severity: 'success',
                    summary: 'Thành công',
                    detail: 'Cập nhật đế giày thành công',
                    life: 3000
                });
            } else {
                deGiay.value.maDeGiay = deGiay.value.maDeGiay || createId();
                await axios.post('http://localhost:8080/de-giay', deGiay.value);
                toast.add({
                    severity: 'success',
                    summary: 'Thành công',
                    detail: 'Tạo đế giày thành công',
                    life: 3000
                });
            }
            fetchData();
            deGiayDialog.value = false;
            deGiay.value = {};
        } catch (error) {
            console.error('Error saving đế giày:', error);
            toast.add({
                severity: 'error',
                summary: 'Lỗi',
                detail: error.response?.data?.message || 'Lưu đế giày thất bại',
                life: 3000
            });
        }
    } else {
        if (isDuplicateName.value) {
            toast.add({
                severity: 'error',
                summary: 'Lỗi',
                detail: 'Tên Đế Giày đã tồn tại, vui lòng chọn tên khác',
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

function editDeGiay(dg) {
    deGiay.value = { ...dg };
    deGiayDialog.value = true;
}

function confirmDeleteDeGiay(dg) {
    deGiay.value = dg;
    deleteDeGiayDialog.value = true;
}

async function deleteDeGiay() {
    try {
        await axios.delete(`http://localhost:8080/de-giay/${deGiay.value.id}`);
        fetchData();
        deleteDeGiayDialog.value = false;
        deGiay.value = {};
        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: 'Xóa đế giày thành công',
            life: 3000
        });
    } catch (error) {
        console.error('Error deleting đế giày:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: error.response?.data?.message || 'Xóa đế giày thất bại',
            life: 3000
        });
    }
}

function confirmDeleteSelected() {
    deleteDeGiaysDialog.value = true;
}

async function deleteSelectedDeGiays() {
    try {
        for (const dg of selectedDeGiay.value) {
            await axios.delete(`http://localhost:8080/de-giay/${dg.id}`);
        }
        fetchData();
        deleteDeGiaysDialog.value = false;
        selectedDeGiay.value = null;
        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: 'Xóa các đế giày thành công',
            life: 3000
        });
    } catch (error) {
        console.error('Error deleting đế giày:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: error.response?.data?.message || 'Xóa các đế giày thất bại',
            life: 3000
        });
    }
}

async function changeStatus(dg) {
    try {
        const updatedDeGiay = { ...dg, trangThai: dg.trangThai === 1 ? 0 : 1 };
        await axios.put(`http://localhost:8080/de-giay/${dg.id}`, updatedDeGiay);
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

function handleAddDeGiayConfirm() {
  saveDeGiay();              // gọi API thêm sản phẩm
  confirmAddDialog.value = false; // tắt dialog confirm
}
function handleUpdateDeGiayConfirm() {
  editDeGiay();              // gọi API cập nhật sản phẩm
  confirmUpdateDialog.value = false; // tắt dialog confirm
}

function exportCSV() {
    try {
        // If no data, show warning
        if (!ListDeGiay.value || ListDeGiay.value.length === 0) {
            toast.add({
                severity: 'warn',
                summary: 'Cảnh báo',
                detail: 'Không có dữ liệu để xuất',
                life: 3000
            });
            return;
        }

        // Create CSV headers with Vietnamese labels
        const headers = ['STT', 'Mã Đế Giày', 'Tên Đế Giày', 'Trạng Thái'];

        // Convert data to CSV format with STT
        const csvData = ListDeGiay.value.map((item, index) => {
            return [
                index + 1, // STT
                // item.id || '',
                item.maDeGiay || '',
                item.tenDeGiay || '',
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
            const filename = `DeGiay-${dateStr}.csv`;

            link.setAttribute('download', filename);
            link.style.visibility = 'hidden';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            
            // Show success message
            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: `Đã xuất ${ListDeGiay.value.length} bản ghi ra file CSV`,
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