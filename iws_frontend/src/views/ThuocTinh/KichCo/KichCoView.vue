<template>
    <div class="card">
        <Toast />
        <Toolbar class="mb-6">
            <template #start>
                <Button label="Thêm mới" icon="pi pi-plus" severity="secondary" class="mr-2" @click="openNew" />
                <Button label="Xóa" icon="pi pi-trash" severity="secondary" @click="confirmDeleteSelected" :disabled="!selectedKichCo || !selectedKichCo.length" />
            </template>
            <template #end>
                <Button label="Xuất CSV" icon="pi pi-upload" severity="secondary" @click="exportCSV" />
            </template>
        </Toolbar>

        <DataTable
            ref="dt"
            v-model:selection="selectedKichCo"
            :value="ListKichCo"
            dataKey="id"
            :paginator="true"
            :rows="10"
            :filters="filters"
            paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
            :rowsPerPageOptions="[5, 10, 25]"
            currentPageReportTemplate="Hiển thị {first} đến {last} của {totalRecords} kích cỡ"
        >
            <template #header>
                <div class="flex flex-wrap gap-2 items-center justify-between">
                    <h4 class="m-0">📋 Quản lý Kích Cỡ</h4>
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
            <Column field="maKichCo" header="Mã Kích Cỡ" sortable style="min-width: 12rem"></Column>
            <Column field="tenKichCo" header="Tên Kích Cỡ" sortable style="min-width: 16rem"></Column>
            <Column field="trangThai" header="Trạng Thái" sortable style="min-width: 12rem">
                <template #body="slotProps">
                    <Tag :value="slotProps.data.trangThai === 1 ? 'Hoạt động' : 'Ngừng hoạt động'" :severity="getStatusLabel(slotProps.data.trangThai)" />
                </template>
            </Column>
            <Column :exportable="false" style="width: 10rem">
                <template #body="slotProps">
                    <div class="flex justify-between gap-2">
                        <Button icon="pi pi-pencil" outlined rounded size="small" @click="editKichCo(slotProps.data)" />
                        <Button icon="pi pi-trash" outlined rounded severity="danger" size="small" @click="confirmDeleteKichCo(slotProps.data)" />
                        <Button icon="pi pi-refresh" outlined rounded severity="secondary" size="small" @click="changeStatus(slotProps.data)" />
                    </div>
                </template>
            </Column>
        </DataTable>

        <Dialog v-model:visible="kichCoDialog" :style="{ width: '450px' }" header="Chi Tiết Kích Cỡ" :modal="true">
            <div class="flex flex-col gap-6">
                <div>
                    <label for="maKichCo" class="block font-bold mb-3">Mã Kích Cỡ</label>
                    <InputText id="maKichCo" v-model.trim="kichCo.maKichCo" required="true" autofocus :invalid="submitted && !kichCo.maKichCo" fluid readonly="true"/>
                    <small v-if="submitted && !kichCo.maKichCo" class="text-red-500">Mã Kích Cỡ là bắt buộc.</small>
                </div>
                <div>
                    <label for="tenKichCo" class="block font-bold mb-3">Tên Kích Cỡ</label>
                    <InputText 
                        id="tenKichCo" 
                        v-model.trim="kichCo.tenKichCo" 
                        required="true" 
                        :invalid="submitted && (!kichCo.tenKichCo || isDuplicateName || !isValidNumber)" 
                        fluid 
                    />
                    <small v-if="submitted && !kichCo.tenKichCo" class="text-red-500">
                        Tên Kích Cỡ là bắt buộc.
                    </small>
                    <small v-else-if="submitted && !isValidNumber" class="text-red-500">
                        Tên Kích Cỡ phải là số.
                    </small>
                    <small v-else-if="submitted && isDuplicateName" class="text-red-500">
                        Tên Kích Cỡ đã tồn tại, vui lòng chọn tên khác.
                    </small>
                </div>

                <div>
                    <label for="trangThai" class="block font-bold mb-3">Trạng Thái</label>
                    <Select id="trangThai" v-model="kichCo.trangThai" :options="statuses" optionLabel="label" optionValue="value" placeholder="Chọn trạng thái" fluid />
                </div>
            </div>
            <template #footer>
                <Button label="Hủy" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Lưu" icon="pi pi-check" @click="confirmAddDialog = true" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteKichCoDialog" :style="{ width: '450px' }" header="Xác nhận" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span v-if="kichCo"
                    >Bạn có chắc muốn xóa kích cỡ <b>{{ kichCo.tenKichCo }}</b
                    >?</span
                >
            </div>
            <template #footer>
                <Button label="Không" icon="pi pi-times" text @click="deleteKichCoDialog = false" />
                <Button label="Có" icon="pi pi-check" @click="deleteKichCo" />
            </template>
        </Dialog>

         <Dialog v-model:visible="confirmAddDialog" header="Xác nhận" modal>
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl text-red-500" />
                <div>
                    <p v-if="kichCo" class="mb-2">
                        Bạn có chắc chắn muốn thực hiện hành động này?
                    </p>
                </div>
            </div>
            <template #footer>
                <Button label="Hủy bỏ" icon="pi pi-times" text @click="confirmAddDialog = false" :disabled="loading" />
                <Button label="Thực hiện" icon="pi pi-check" severity="success" @click="handleAddKichCoConfirm" :loading="loading" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteKichCosDialog" :style="{ width: '450px' }" header="Xác nhận" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span>Bạn có chắc muốn xóa các kích cỡ đã chọn?</span>
            </div>
            <template #footer>
                <Button label="Không" icon="pi pi-times" text @click="deleteKichCosDialog = false" />
                <Button label="Có" icon="pi pi-check" text @click="deleteSelectedKichCos" />
            </template>
        </Dialog>
    </div>
</template>

<script setup>
import { FilterMatchMode } from '@primevue/core/api';
import axios from 'axios';
import { useToast } from 'primevue/usetoast';
import { onMounted, ref, computed } from 'vue';

const toast = useToast();
const dt = ref();
const ListKichCo = ref([]);
const kichCoDialog = ref(false);
const deleteKichCoDialog = ref(false);
const deleteKichCosDialog = ref(false);
const kichCo = ref({});
const selectedKichCo = ref();
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
    if (!kichCo.value.tenKichCo) return false;
    
    const trimmedName = kichCo.value.tenKichCo.trim().toLowerCase();
    
    return ListKichCo.value.some(item => {
        // Skip checking against itself when editing
        if (kichCo.value.id && item.id === kichCo.value.id) {
            return false;
        }
        return item.tenKichCo && item.tenKichCo.trim().toLowerCase() === trimmedName;
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
        const res = await axios.get('http://localhost:8080/kich-co');
        ListKichCo.value = res.data;
    } catch (error) {
        console.error('Error fetching data:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Không thể tải danh sách kích cỡ',
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
    return 'KC' + id;
}

function openNew() {
    kichCo.value = { 
        maKichCo: createId(),
        tenKichCo: '',
        trangThai: 1 
    };
    submitted.value = false;
    kichCoDialog.value = true;
}

function hideDialog() {
    kichCoDialog.value = false;
    submitted.value = false;
}

const isValidNumber = computed(() => {
    if (!kichCo.value.tenKichCo) return true; // Cho phép rỗng để hiển thị lỗi "bắt buộc"
    
    const trimmedName = kichCo.value.tenKichCo.trim();
    // Kiểm tra xem có phải là số không (bao gồm số thập phân)
    return !isNaN(trimmedName) && !isNaN(parseFloat(trimmedName)) && trimmedName !== '';
});

async function saveKichCo() {
    submitted.value = true;

    // Check if required fields are filled, name is valid number, and name is not duplicate
    if (kichCo.value.maKichCo?.trim() && 
        kichCo.value.tenKichCo?.trim() && 
        isValidNumber.value && 
        !isDuplicateName.value) {
        try {
            if (kichCo.value.id) {
                await axios.put(`http://localhost:8080/kich-co/${kichCo.value.id}`, kichCo.value);
                toast.add({
                    severity: 'success',
                    summary: 'Thành công',
                    detail: 'Cập nhật kích cỡ thành công',
                    life: 3000
                });
            } else {
                kichCo.value.maKichCo = kichCo.value.maKichCo || createId();
                await axios.post('http://localhost:8080/kich-co', kichCo.value);
                toast.add({
                    severity: 'success',
                    summary: 'Thành công',
                    detail: 'Tạo kích cỡ thành công',
                    life: 3000
                });
            }
            fetchData();
            kichCoDialog.value = false;
            kichCo.value = {};
        } catch (error) {
            console.error('Error saving kích cỡ:', error);
            toast.add({
                severity: 'error',
                summary: 'Lỗi',
                detail: error.response?.data?.message || 'Lưu kích cỡ thất bại',
                life: 3000
            });
        }
    } else {
        // Hiển thị thông báo lỗi cụ thể
        if (!isValidNumber.value) {
            toast.add({
                severity: 'error',
                summary: 'Lỗi',
                detail: 'Tên kích cỡ phải là số',
                life: 3000
            });
        } else if (isDuplicateName.value) {
            toast.add({
                severity: 'error',
                summary: 'Lỗi',
                detail: 'Tên kích cỡ đã tồn tại, vui lòng chọn tên khác',
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

function editKichCo(kc) {
    kichCo.value = { ...kc };
    kichCoDialog.value = true;
}

function confirmDeleteKichCo(kc) {
    kichCo.value = kc;
    deleteKichCoDialog.value = true;
}

async function deleteKichCo() {
    try {
        await axios.delete(`http://localhost:8080/kich-co/${kichCo.value.id}`);
        fetchData();
        deleteKichCoDialog.value = false;
        kichCo.value = {};
        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: 'Xóa kích cỡ thành công',
            life: 3000
        });
    } catch (error) {
        console.error('Error deleting kích cỡ:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: error.response?.data?.message || 'Xóa kích cỡ thất bại',
            life: 3000
        });
    }
}

function confirmDeleteSelected() {
    deleteKichCosDialog.value = true;
}

async function deleteSelectedKichCos() {
    try {
        for (const kc of selectedKichCo.value) {
            await axios.delete(`http://localhost:8080/kich-co/${kc.id}`);
        }
        fetchData();
        deleteKichCosDialog.value = false;
        selectedKichCo.value = null;
        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: 'Xóa các kích cỡ thành công',
            life: 3000
        });
    } catch (error) {
        console.error('Error deleting kích cỡ:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: error.response?.data?.message || 'Xóa các kích cỡ thất bại',
            life: 3000
        });
    }
}

async function changeStatus(kc) {
    try {
        const updatedKichCo = { ...kc, trangThai: kc.trangThai === 1 ? 0 : 1 };
        await axios.put(`http://localhost:8080/kich-co/${kc.id}`, updatedKichCo);
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

function exportCSV() {
    try {
        // If no data, show warning
        if (!ListKichCo.value || ListKichCo.value.length === 0) {
            toast.add({
                severity: 'warn',
                summary: 'Cảnh báo',
                detail: 'Không có dữ liệu để xuất',
                life: 3000
            });
            return;
        }

        // Create CSV headers with Vietnamese labels
        const headers = ['STT', 'Mã Kích Cỡ', 'Tên Kích Cỡ', 'Trạng Thái'];

        // Convert data to CSV format with STT
        const csvData = ListKichCo.value.map((item, index) => {
            return [
                index + 1, // STT
                // item.id || '',
                item.maKichCo || '',
                item.tenKichCo || '',
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
            const filename = `KichCo-${dateStr}.csv`;

            link.setAttribute('download', filename);
            link.style.visibility = 'hidden';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            
            // Show success message
            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: `Đã xuất ${ListKichCo.value.length} bản ghi ra file CSV`,
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
function handleAddKichCoConfirm() {
    saveKichCo();              // gọi API thêm kích cỡ
  confirmAddDialog.value = false; // tắt dialog confirm
}
function handleUpdateKichCoConfirm() {
  editKichCo();              // gọi API cập nhật kích cỡ
  confirmUpdateDialog.value = false; // tắt dialog confirm
}
}
</script>

<style scoped>
.card {
    border: none;
    box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}
</style>