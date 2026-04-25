<template>
    <div class="card">
        <Toast />
        <Toolbar class="mb-6">
            <template #start>
                <Button label="Thêm mới" icon="pi pi-plus" severity="secondary" class="mr-2" @click="openNew" />
                <Button label="Xóa" icon="pi pi-trash" severity="secondary" @click="confirmDeleteSelected" :disabled="!selectedMauSac || !selectedMauSac.length" />
            </template>
            <template #end>
                <Button label="Xuất CSV" icon="pi pi-upload" severity="secondary" @click="exportCSV" />
            </template>
        </Toolbar>

        <DataTable
            ref="dt"
            v-model:selection="selectedMauSac"
            :value="ListMauSac"
            dataKey="id"
            :paginator="true"
            :rows="10"
            :filters="filters"
            paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
            :rowsPerPageOptions="[5, 10, 25]"
            currentPageReportTemplate="Hiển thị {first} đến {last} của {totalRecords} màu sắc"
        >
            <template #header>
                <div class="flex flex-wrap gap-2 items-center justify-between">
                    <h4 class="m-0">📋 Quản lý Màu Sắc</h4>
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
            <Column field="maMauSac" header="Mã Màu Sắc" sortable style="min-width: 12rem"></Column>
            <Column field="tenMauSac" header="Tên Màu Sắc" sortable style="min-width: 16rem"></Column>
            <Column field="trangThai" header="Trạng Thái" sortable style="min-width: 12rem">
                <template #body="slotProps">
                    <Tag :value="slotProps.data.trangThai === 1 ? 'Hoạt động' : 'Ngừng hoạt động'" :severity="getStatusLabel(slotProps.data.trangThai)" />
                </template>
            </Column>
            <Column :exportable="false" style="width: 10rem">
                <template #body="slotProps">
                    <div class="flex justify-between gap-2">
                        <Button icon="pi pi-pencil" outlined rounded size="small" @click="editMauSac(slotProps.data)" />
                        <Button icon="pi pi-trash" outlined rounded severity="danger" size="small" @click="confirmDeleteMauSac(slotProps.data)" />
                        <Button icon="pi pi-refresh" outlined rounded severity="secondary" size="small" @click="changeStatus(slotProps.data)" />
                    </div>
                </template>
            </Column>
        </DataTable>

        <Dialog v-model:visible="mauSacDialog" :style="{ width: '450px' }" header="Chi Tiết Màu Sắc" :modal="true">
            <div class="flex flex-col gap-6">
                <div>
                    <label for="maMauSac" class="block font-bold mb-3">Mã Màu Sắc</label>
                    <InputText id="maMauSac" v-model.trim="mauSac.maMauSac" required="true" autofocus :invalid="submitted && !mauSac.maMauSac" fluid readonly="true" />
                    <small v-if="submitted && !mauSac.maMauSac" class="text-red-500">Mã Màu Sắc là bắt buộc.</small>
                </div>
                <div>
                    <label for="tenMauSac" class="block font-bold mb-3">Tên Màu Sắc</label>
                    <InputText id="tenMauSac" v-model.trim="mauSac.tenMauSac" required="true" :invalid="submitted && (!mauSac.tenMauSac || isDuplicateName)" fluid />
                    <small v-if="submitted && !mauSac.tenMauSac" class="text-red-500">Tên Màu Sắc là bắt buộc.</small>
                </div>
                <div>
                    <label for="trangThai" class="block font-bold mb-3">Trạng Thái</label>
                    <Select id="trangThai" v-model="mauSac.trangThai" :options="statuses" optionLabel="label" optionValue="value" placeholder="Chọn trạng thái" fluid />
                </div>
            </div>
            <template #footer>
                <Button label="Hủy" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Lưu" icon="pi pi-check" @click="confirmAddDialog = true" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteMauSacDialog" :style="{ width: '450px' }" header="Xác nhận" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span v-if="mauSac"
                    >Bạn có chắc muốn xóa màu sắc <b>{{ mauSac.tenMauSac }}</b
                    >?</span
                >
            </div>
            <template #footer>
                <Button label="Không" icon="pi pi-times" text @click="deleteMauSacDialog = false" />
                <Button label="Có" icon="pi pi-check" @click="deleteMauSac" />
            </template>
        </Dialog>

         <Dialog v-model:visible="confirmAddDialog" header="Xác nhận" modal>
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl text-red-500" />
                <div>
                    <p v-if="mauSac" class="mb-2">
                        Bạn có chắc chắn muốn thực hiện hành động này ?
                    </p>
                </div>
            </div>
            <template #footer>
                <Button label="Hủy bỏ" icon="pi pi-times" text @click="confirmAddDialog = false" :disabled="loading" />
                <Button label="Thực hiện" icon="pi pi-check" severity="success" @click="handleAddMauSacConfirm" :loading="loading" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteMauSacsDialog" :style="{ width: '450px' }" header="Xác nhận" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span>Bạn có chắc muốn xóa các màu sắc đã chọn?</span>
            </div>
            <template #footer>
                <Button label="Không" icon="pi pi-times" text @click="deleteMauSacsDialog = false" />
                <Button label="Có" icon="pi pi-check" text @click="deleteSelectedMauSacs" />
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
const ListMauSac = ref([]);
const mauSacDialog = ref(false);
const deleteMauSacDialog = ref(false);
const deleteMauSacsDialog = ref(false);
const mauSac = ref({});
const selectedMauSac = ref();
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
    if (!mauSac.value.tenMauSac) return false;
    
    const trimmedName = mauSac.value.tenMauSac.trim().toLowerCase();
    
    return ListMauSac.value.some(item => {
        // Skip checking against itself when editing
        if (mauSac.value.id && item.id === mauSac.value.id) {
            return false;
        }
        return item.tenMauSac && item.tenMauSac.trim().toLowerCase() === trimmedName;
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

function handleAddMauSacConfirm() {
  saveMauSac();              // gọi API thêm màu sắc
  confirmAddDialog.value = false; // tắt dialog confirm
}
function handleUpdateMauSacConfirm() {
  editMauSac();              // gọi API cập nhật màu sắc
  confirmUpdateDialog.value = false; // tắt dialog confirm
}

async function fetchData() {
    try {
        const res = await axios.get('http://localhost:8080/mau-sac');
        ListMauSac.value = res.data;
    } catch (error) {
        console.error('Error fetching data:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Không thể tải danh sách màu sắc',
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
    return 'MS' + id;
}

function openNew() {
    mauSac.value = { 
        maMauSac: createId(),
        tenMauSac: '',
        trangThai: 1 
    };
    submitted.value = false;
    mauSacDialog.value = true;
}

function hideDialog() {
    mauSacDialog.value = false;
    submitted.value = false;
}

async function saveMauSac() {
    submitted.value = true;

    // Check if required fields are filled and name is not duplicate
    if (mauSac.value.maMauSac?.trim() && mauSac.value.tenMauSac?.trim() && !isDuplicateName.value) {
        try {
            if (mauSac.value.id) {
                await axios.put(`http://localhost:8080/mau-sac/${mauSac.value.id}`, mauSac.value);
                toast.add({
                    severity: 'success',
                    summary: 'Thành công',
                    detail: 'Cập nhật màu sắc thành công',
                    life: 3000
                });
            } else {
                mauSac.value.maMauSac = mauSac.value.maMauSac || createId();
                await axios.post('http://localhost:8080/mau-sac', mauSac.value);
                toast.add({
                    severity: 'success',
                    summary: 'Thành công',
                    detail: 'Tạo màu sắc thành công',
                    life: 3000
                });
            }
            fetchData();
            mauSacDialog.value = false;
            mauSac.value = {};
        } catch (error) {
            console.error('Error saving màu sắc:', error);
            toast.add({
                severity: 'error',
                summary: 'Lỗi',
                detail: error.response?.data?.message || 'Lưu màu sắc thất bại',
                life: 3000
            });
        }
    } else {
        if (isDuplicateName.value) {
            toast.add({
                severity: 'error',
                summary: 'Lỗi',
                detail: 'Tên Màu Sắc đã tồn tại, vui lòng chọn tên khác',
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

function editMauSac(ms) {
    mauSac.value = { ...ms };
    mauSacDialog.value = true;
}

function confirmDeleteMauSac(ms) {
    mauSac.value = ms;
    deleteMauSacDialog.value = true;
}

async function deleteMauSac() {
    try {
        await axios.delete(`http://localhost:8080/mau-sac/${mauSac.value.id}`);
        fetchData();
        deleteMauSacDialog.value = false;
        mauSac.value = {};
        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: 'Xóa màu sắc thành công',
            life: 3000
        });
    } catch (error) {
        console.error('Error deleting màu sắc:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: error.response?.data?.message || 'Xóa màu sắc thất bại',
            life: 3000
        });
    }
}

function confirmDeleteSelected() {
    deleteMauSacsDialog.value = true;
}

async function deleteSelectedMauSacs() {
    try {
        for (const ms of selectedMauSac.value) {
            await axios.delete(`http://localhost:8080/mau-sac/${ms.id}`);
        }
        fetchData();
        deleteMauSacsDialog.value = false;
        selectedMauSac.value = null;
        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: 'Xóa các màu sắc thành công',
            life: 3000
        });
    } catch (error) {
        console.error('Error deleting màu sắc:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: error.response?.data?.message || 'Xóa các màu sắc thất bại',
            life: 3000
        });
    }
}

async function changeStatus(ms) {
    try {
        const updatedMauSac = { ...ms, trangThai: ms.trangThai === 1 ? 0 : 1 };
        await axios.put(`http://localhost:8080/mau-sac/${ms.id}`, updatedMauSac);
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
        if (!ListMauSac.value || ListMauSac.value.length === 0) {
            toast.add({
                severity: 'warn',
                summary: 'Cảnh báo',
                detail: 'Không có dữ liệu để xuất',
                life: 3000
            });
            return;
        }

        // Create CSV headers with Vietnamese labels
        const headers = ['STT', 'Mã Màu Sắc', 'Tên Màu Sắc', 'Trạng Thái'];

        // Convert data to CSV format
        const csvData = ListMauSac.value.map((item , index) => {
            return [
                item.id || '',
                index + 1,
                item.maMauSac || '',
                item.tenMauSac || '',
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
            const filename = `MauSac-${dateStr}.csv`;

            link.setAttribute('download', filename);
            link.style.visibility = 'hidden';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            
            // Show success message
            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: `Đã xuất ${ListMauSac.value.length} bản ghi ra file CSV`,
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