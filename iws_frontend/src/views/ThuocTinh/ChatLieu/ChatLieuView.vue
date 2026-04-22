<template>
    <div class="card">
        <Toast />
        <Toolbar class="mb-6">
            <template #start>
                <Button label="Thêm mới" icon="pi pi-plus" severity="secondary" class="mr-2" @click="openNew" />
                <Button label="Xóa" icon="pi pi-trash" severity="secondary" @click="confirmDeleteSelected" :disabled="!selectedChatLieu || !selectedChatLieu.length" />
            </template>
            <template #end>
                <Button label="Xuất CSV" icon="pi pi-upload" severity="secondary" @click="exportCSV" />
            </template>
        </Toolbar>

        <DataTable
            ref="dt"
            v-model:selection="selectedChatLieu"
            :value="ListChatLieu"
            dataKey="id"
            :paginator="true"
            :rows="10"
            :filters="filters"
            paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
            :rowsPerPageOptions="[5, 10, 25]"
            currentPageReportTemplate="Hiển thị {first} đến {last} của {totalRecords} chất liệu"
        >
            <template #header>
                <div class="flex flex-wrap gap-2 items-center justify-between">
                    <h4 class="m-0">📋 Quản lý Chất Liệu</h4>
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
            <Column field="maChatLieu" header="Mã Chất Liệu" sortable style="min-width: 12rem"></Column>
            <Column field="tenChatLieu" header="Tên Chất Liệu" sortable style="min-width: 16rem"></Column>
            <Column field="trangThai" header="Trạng Thái" sortable style="min-width: 12rem">
                <template #body="slotProps">
                    <Tag :value="slotProps.data.trangThai === 1 ? 'Hoạt động' : 'Ngừng hoạt động'" :severity="getStatusLabel(slotProps.data.trangThai)" />
                </template>
            </Column>
            <Column :exportable="false" style="width: 10rem">
                <template #body="slotProps">
                    <div class="flex justify-between gap-2">
                        <Button icon="pi pi-pencil" outlined rounded size="small" @click="editChatLieu(slotProps.data)" />
                        <Button icon="pi pi-trash" outlined rounded severity="danger" size="small" @click="confirmDeleteChatLieu(slotProps.data)" />
                        <Button icon="pi pi-refresh" outlined rounded severity="secondary" size="small" @click="changeStatus(slotProps.data)" />
                    </div>
                </template>
            </Column>
        </DataTable>

        <Dialog v-model:visible="chatLieuDialog" :style="{ width: '450px' }" header="Chi Tiết Chất Liệu" :modal="true">
            <div class="flex flex-col gap-6">
                <div>
                    <label for="maChatLieu" class="block font-bold mb-3">Mã Chất Liệu</label>
                    <InputText id="maChatLieu" v-model.trim="chatLieu.maChatLieu" required="true" autofocus :invalid="submitted && !chatLieu.maChatLieu" fluid readonly="true"/>
                    <small v-if="submitted && !chatLieu.maChatLieu" class="text-red-500">Mã Chất Liệu là bắt buộc.</small>
                </div>
                <div>
                    <label for="tenChatLieu" class="block font-bold mb-3">Tên Chất Liệu</label>
                    <InputText id="tenChatLieu" v-model.trim="chatLieu.tenChatLieu" required="true" :invalid="submitted && (!chatLieu.tenChatLieu || isDuplicateName)" fluid />
                    <small v-if="submitted && !chatLieu.tenChatLieu" class="text-red-500">Tên Chất Liệu là bắt buộc.</small>
                </div>
                <div>
                    <label for="trangThai" class="block font-bold mb-3">Trạng Thái</label>
                    <Select id="trangThai" v-model="chatLieu.trangThai" :options="statuses" optionLabel="label" optionValue="value" placeholder="Chọn trạng thái" fluid />
                </div>
            </div>
            <template #footer>
                <Button label="Hủy" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Lưu" icon="pi pi-check" @click="confirmAddDialog = true" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteChatLieuDialog" :style="{ width: '450px' }" header="Xác nhận" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span v-if="chatLieu"
                    >Bạn có chắc muốn xóa chất liệu <b>{{ chatLieu.tenChatLieu }}</b
                    >?</span
                >
            </div>
            <template #footer>
                <Button label="Không" icon="pi pi-times" text @click="deleteChatLieuDialog = false" />
                <Button label="Có" icon="pi pi-check" @click="deleteChatLieu" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteChatLieusDialog" :style="{ width: '450px' }" header="Xác nhận" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span>Bạn có chắc muốn xóa các chất liệu đã chọn?</span>
            </div>
            <template #footer>
                <Button label="Không" icon="pi pi-times" text @click="deleteChatLieusDialog = false" />
                <Button label="Có" icon="pi pi-check" text @click="deleteSelectedChatLieus" />
            </template>
        </Dialog>

        <Dialog v-model:visible="confirmAddDialog" header="Xác nhận" modal>
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl text-red-500" />
                <div>
                    <p v-if="chatLieu" class="mb-2">
                        Bạn có chắc chắn muốn thực hiện hành động này ?
                    </p>
                </div>
            </div>
            <template #footer>
                <Button label="Hủy bỏ" icon="pi pi-times" text @click="confirmAddDialog = false" :disabled="loading" />
                <Button label="Thực hiện" icon="pi pi-check" severity="success" @click="handleAddChatLieuConfirm" :loading="loading" />
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
const ListChatLieu = ref([]);
const chatLieuDialog = ref(false);
const deleteChatLieuDialog = ref(false);
const deleteChatLieusDialog = ref(false);
const chatLieu = ref({});
const selectedChatLieu = ref();
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
    if (!chatLieu.value.tenChatLieu) return false;
    
    const trimmedName = chatLieu.value.tenChatLieu.trim().toLowerCase();
    
    return ListChatLieu.value.some(item => {
        // Skip checking against itself when editing
        if (chatLieu.value.id && item.id === chatLieu.value.id) {
            return false;
        }
        return item.tenChatLieu && item.tenChatLieu.trim().toLowerCase() === trimmedName;
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
        const res = await axios.get('http://localhost:8080/chat-lieu');
        ListChatLieu.value = res.data;
    } catch (error) {
        console.error('Error fetching data:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Không thể tải danh sách chất liệu',
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
    return 'CL' + id;
}

function handleAddChatLieuConfirm() {
  saveChatLieu();              // gọi API thêm sản phẩm
  confirmAddDialog.value = false; // tắt dialog confirm
}
function handleUpdateChatLieuConfirm() {
  editChatLieu();              // gọi API cập nhật sản phẩm
  confirmUpdateDialog.value = false; // tắt dialog confirm
}
function openNew() {
    chatLieu.value = { 
        maChatLieu: createId(),
        tenChatLieu: '',
        trangThai: 1 
    };
    submitted.value = false;
    chatLieuDialog.value = true;
}

function hideDialog() {
    chatLieuDialog.value = false;
    submitted.value = false;
}

async function saveChatLieu() {
    submitted.value = true;

    // Check if required fields are filled and name is not duplicate
    if (chatLieu.value.maChatLieu?.trim() && chatLieu.value.tenChatLieu?.trim() && !isDuplicateName.value) {
        try {
            if (chatLieu.value.id) {
                await axios.put(`http://localhost:8080/chat-lieu/${chatLieu.value.id}`, chatLieu.value);
                toast.add({
                    severity: 'success',
                    summary: 'Thành công',
                    detail: 'Cập nhật chất liệu thành công',
                    life: 3000
                });
            } else {
                chatLieu.value.maChatLieu = chatLieu.value.maChatLieu || createId();
                await axios.post('http://localhost:8080/chat-lieu', chatLieu.value);
                toast.add({
                    severity: 'success',
                    summary: 'Thành công',
                    detail: 'Tạo chất liệu thành công',
                    life: 3000
                });
            }
            fetchData();
            chatLieuDialog.value = false;
            chatLieu.value = {};
        } catch (error) {
            console.error('Error saving chất liệu:', error);
            toast.add({
                severity: 'error',
                summary: 'Lỗi',
                detail: error.response?.data?.message || 'Lưu chất liệu thất bại',
                life: 3000
            });
        }
    } else {
        if (isDuplicateName.value) {
            toast.add({
                severity: 'error',
                summary: 'Lỗi',
                detail: 'Tên Chất Liệu đã tồn tại, vui lòng chọn tên khác',
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

function editChatLieu(cl) {
    chatLieu.value = { ...cl };
    chatLieuDialog.value = true;
}

function confirmDeleteChatLieu(cl) {
    chatLieu.value = cl;
    deleteChatLieuDialog.value = true;
}

async function deleteChatLieu() {
    try {
        await axios.delete(`http://localhost:8080/chat-lieu/${chatLieu.value.id}`);
        fetchData();
        deleteChatLieuDialog.value = false;
        chatLieu.value = {};
        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: 'Xóa chất liệu thành công',
            life: 3000
        });
    } catch (error) {
        console.error('Error deleting chất liệu:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: error.response?.data?.message || 'Xóa chất liệu thất bại',
            life: 3000
        });
    }
}

function confirmDeleteSelected() {
    deleteChatLieusDialog.value = true;
}

async function deleteSelectedChatLieus() {
    try {
        for (const cl of selectedChatLieu.value) {
            await axios.delete(`http://localhost:8080/chat-lieu/${cl.id}`);
        }
        fetchData();
        deleteChatLieusDialog.value = false;
        selectedChatLieu.value = null;
        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: 'Xóa các chất liệu thành công',
            life: 3000
        });
    } catch (error) {
        console.error('Error deleting chất liệu:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: error.response?.data?.message || 'Xóa các chất liệu thất bại',
            life: 3000
        });
    }
}

async function changeStatus(cl) {
    try {
        const updatedChatLieu = { ...cl, trangThai: cl.trangThai === 1 ? 0 : 1 };
        await axios.put(`http://localhost:8080/chat-lieu/${cl.id}`, updatedChatLieu);
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
        if (!ListChatLieu.value || ListChatLieu.value.length === 0) {
            toast.add({
                severity: 'warn',
                summary: 'Cảnh báo',
                detail: 'Không có dữ liệu để xuất',
                life: 3000
            });
            return;
        }

        // Create CSV headers with Vietnamese labels
        const headers = ['STT', 'Mã Chất Liệu', 'Tên Chất Liệu', 'Trạng Thái'];
        
        // Convert data to CSV format with STT
        const csvData = ListChatLieu.value.map((item, index) => {
            return [
                index + 1, // STT
                // item.id || '',
                item.maChatLieu || '',
                item.tenChatLieu || '',
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
            const filename = `ChatLieu-${dateStr}.csv`;

            link.setAttribute('download', filename);
            link.style.visibility = 'hidden';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            
            // Show success message
            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: `Đã xuất ${ListChatLieu.value.length} bản ghi ra file CSV`,
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