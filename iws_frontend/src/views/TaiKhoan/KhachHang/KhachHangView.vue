<template>
    <div class="card">
        <!-- Header Actions -->
        <div class="card-header">
            <div class="flex justify-between items-center">
                <div>
                    <h3 class="text-2xl font-bold text-gray-900">Quản Lý Khách Hàng</h3>
                    <p class="text-gray-600 mt-1">Quản lý thông tin khách hàng và địa chỉ</p>
                </div>
                <div class="flex gap-2">
                    <Button
                        label="Xuất Excel"
                        icon="pi pi-file-excel"
                        severity="secondary"
                        @click="exportToExcel"
                        :loading="exporting"
                    />
                </div>
            </div>
        </div>

        <!-- Enhanced Search Section -->
        <div class="search-section">
            <!-- Main Global Search -->
            <div class="grid grid-cols-1 gap-4 mb-4">
                <div class="relative">
                    <IconField>
                        <InputIcon>
                            <i class="pi pi-search" />
                        </InputIcon>
                        <InputText
                            v-model="globalSearch"
                            placeholder="Tìm kiếm tất cả thông tin khách hàng (tên, email, SĐT, mã KH, tài khoản)..."
                            @input="debouncedGlobalSearch"
                            class="w-full text-lg py-3"
                        />
                    </IconField>
                    <Button
                        v-if="globalSearch"
                        icon="pi pi-times"
                        class="absolute right-2 top-1/2 transform -translate-y-1/2"
                        text
                        rounded
                        size="small"
                        @click="clearGlobalSearch"
                        title="Xóa tìm kiếm"
                    />
                </div>
            </div>

            <!-- Advanced Filters -->
            <div class="flex flex-wrap gap-2 items-center mb-4">
                <Dropdown
                    v-model="advancedFilters.trangThai"
                    :options="statusOptions"
                    optionLabel="label"
                    optionValue="value"
                    placeholder="Trạng thái"
                    @change="applyAdvancedFilters"
                    showClear
                />
                <Calendar
                    v-model="advancedFilters.startDate"
                    placeholder="Từ ngày"
                    dateFormat="dd/mm/yy"
                    @date-select="applyAdvancedFilters"
                    showIcon
                    showClear
                />
                <Calendar
                    v-model="advancedFilters.endDate"
                    placeholder="Đến ngày"
                    dateFormat="dd/mm/yy"
                    @date-select="applyAdvancedFilters"
                    showIcon
                    showClear
                />
                <Button
                    label="Xóa bộ lọc"
                    icon="pi pi-filter-slash"
                    outlined
                    @click="resetAdvancedFilters"
                />
                
                <div class="ml-auto flex gap-2">
                    <Badge 
                        v-if="selectedCustomers.length > 0" 
                        :value="`${selectedCustomers.length} đã chọn`" 
                        severity="info" 
                    />
                    <Button
                        label="Thay đổi trạng thái"
                        icon="pi pi-refresh"
                        severity="warning"
                        :loading="batchChangingStatus"
                        @click="confirmBatchStatusChange"
                        :disabled="batchChangingStatus || !selectedCustomers || !selectedCustomers.length"
                    />
                </div>
            </div>

            <!-- Search Results Info -->
            <div class="flex justify-between items-center mb-4">
                <div class="flex items-center gap-2 text-sm text-gray-600">
                    <i class="pi pi-info-circle"></i>
                    <span v-if="isLoading">Đang tìm kiếm...</span>
                    <span v-else>
                        Hiển thị {{ customers.length }} / {{ totalRecords }} khách hàng
                        <span v-if="globalSearch" class="ml-2 px-2 py-1 bg-blue-100 text-blue-800 rounded text-xs">
                            Kết quả cho: "{{ globalSearch }}"
                        </span>
                    </span>
                </div>
            </div>
        </div>

        <!-- Data Table -->
        <DataTable
            ref="dt"
            v-model:selection="selectedCustomers"
            :value="customers"
            dataKey="id"
            :paginator="true"
            :rows="pagination.size"
            :totalRecords="pagination.totalElements"
            :loading="isLoading"
            :lazy="true"
            @page="onPageChange"
            @sort="onSort"
            paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
            :rowsPerPageOptions="[10, 25, 50, 100]"
            currentPageReportTemplate="Hiển thị {first} đến {last} của {totalRecords} khách hàng"
            class="responsive-table"
        >
            <Column selectionMode="multiple" :exportable="false" style="width: 3rem"></Column>
            
            <Column field="id" header="ID" sortable style="width: 6rem">
                <template #body="slotProps">
                    <span class="font-bold text-primary">#{{ slotProps.data.id }}</span>
                </template>
            </Column>

            <Column field="maKhachHang" header="Mã KH" sortable style="width: 10rem">
                <template #body="slotProps">
                    <Tag :value="slotProps.data.maKhachHang || 'Chưa có'" severity="info" />
                </template>
            </Column>

            <Column field="hoTen" header="Thông tin khách hàng" sortable style="min-width: 18rem">
                <template #body="slotProps">
                    <div class="flex items-center gap-3">
                        <Avatar
                            :label="getInitials(slotProps.data.hoTen)"
                            class="text-white"
                            style="background-color: #3b82f6"
                            size="large"
                        />
                        <div class="flex flex-col">
                            <span class="font-semibold text-gray-900">{{ slotProps.data.hoTen }}</span>
                            <span class="text-sm text-gray-500">{{ slotProps.data.email }}</span>
                            <span class="text-sm text-gray-500" v-if="slotProps.data.sdt">
                                <i class="pi pi-phone mr-1"></i>{{ slotProps.data.sdt }}
                            </span>
                        </div>
                    </div>
                </template>
            </Column>

            <!-- FIXED: Địa chỉ từ API -->
            <Column header="Địa chỉ" style="min-width: 20rem">
                <template #body="slotProps">
                    <div class="address-display">
                        <span v-if="slotProps.data.diaChiMacDinh && slotProps.data.diaChiMacDinh.diaChiDayDu">
                            {{ slotProps.data.diaChiMacDinh.diaChiDayDu }}
                        </span>
                        <span v-else-if="slotProps.data.danhSachDiaChi && slotProps.data.danhSachDiaChi.length > 0 && slotProps.data.danhSachDiaChi[0].diaChiDayDu">
                            {{ slotProps.data.danhSachDiaChi[0].diaChiDayDu }}
                        </span>
                        <span v-else class="text-muted text-sm">
                            <i class="pi pi-map-marker mr-1"></i>
                            Chưa có địa chỉ
                        </span>
                    </div>
                </template>
            </Column>

            <Column header="Tài khoản & Ví điểm" style="min-width: 12rem">
                <template #body="slotProps">
                    <div class="flex flex-col gap-1">
                        <div class="flex items-center gap-2">
                            <i class="pi pi-user text-blue-500"></i>
                            <span class="text-sm">
                                ID: {{ slotProps.data.idTaiKhoan || 'Chưa liên kết' }}
                            </span>
                        </div>
                        <div class="flex items-center gap-2">
                            <i class="pi pi-star text-yellow-500"></i>
                            <span class="text-sm font-semibold">
                                {{ slotProps.data.idViDiem || 'Chưa có ví' }}
                            </span>
                        </div>
                    </div>
                </template>
            </Column>

            <Column field="trangThai" header="Trạng thái" sortable style="width: 12rem">
                <template #body="slotProps">
                    <Tag
                        :value="getStatusLabel(slotProps.data.trangThai)"
                        :severity="getStatusSeverity(slotProps.data.trangThai)"
                    >
                        <i :class="getStatusIcon(slotProps.data.trangThai)" class="mr-1"></i>
                        {{ getStatusLabel(slotProps.data.trangThai) }}
                    </Tag>
                </template>
            </Column>

            <Column field="ngayTao" header="Ngày tạo" sortable style="width: 10rem">
                <template #body="slotProps">
                    <span class="text-sm">{{ formatDate(slotProps.data.ngayTao) }}</span>
                </template>
            </Column>

            <Column :exportable="false" style="width: 16rem">
                <template #body="slotProps">
                    <div class="flex gap-1">
                        <Button
                            icon="pi pi-eye"
                            size="small"
                            outlined
                            @click="viewCustomer(slotProps.data)"
                            title="Xem chi tiết"
                        />
                        <Button
                            v-if="canEditCustomer"
                            icon="pi pi-pencil"
                            size="small"
                            outlined
                            severity="success"
                            @click="editCustomer(slotProps.data)"
                            title="Chỉnh sửa"
                        />
                        <Button
                            v-if="canEditCustomer"
                            icon="pi pi-refresh"
                            size="small"
                            outlined
                            severity="secondary"
                            @click="changeStatus(slotProps.data)"
                            :title="slotProps.data.trangThai === 1 ? 'Vô hiệu hóa' : 'Kích hoạt'"
                        />
                    </div>
                </template>
            </Column>

            <template #empty>
                <div class="text-center py-8">
                    <i class="pi pi-users text-gray-400 text-6xl mb-4"></i>
                    <h5 class="text-gray-600 mb-2">Không tìm thấy khách hàng</h5>
                    <p class="text-gray-500 mb-4">
                        {{ globalSearch ? 'Thử thay đổi từ khóa tìm kiếm hoặc kiểm tra lại dữ liệu.' : 'Thử thay đổi bộ lọc hoặc kiểm tra lại dữ liệu.' }}
                    </p>
                    <div class="flex gap-2 justify-center">
                        <Button
                            v-if="globalSearch"
                            label="Xóa tìm kiếm"
                            icon="pi pi-times"
                            outlined
                            @click="clearGlobalSearch"
                        />
                        <Button
                            label="Làm mới"
                            icon="pi pi-refresh"
                            outlined
                            @click="fetchData"
                        />
                    </div>
                </div>
            </template>
        </DataTable>

        <!-- Customer View Dialog -->
        <Dialog v-model:visible="viewDialog" :style="{ width: '800px' }" :header="`Chi tiết khách hàng - ${viewingCustomer?.hoTen || 'N/A'}`" :modal="true">
            <div v-if="viewingCustomer" class="flex flex-col gap-4">
                <!-- Thông tin cơ bản -->
                                <div class="rounded-lg bg-blue-50 p-4 border border-blue-200">
                    <h6 class="mb-3 font-semibold text-blue-700 flex items-center">
                        <i class="pi pi-user mr-2"></i>
                        Thông tin khách hàng
                    </h6>
                    <div class="grid grid-cols-2 gap-4 text-sm">
                        <div><strong>ID:</strong> #{{ viewingCustomer.id }}</div>
                        <div><strong>Mã KH:</strong> {{ viewingCustomer.maKhachHang || 'Chưa có' }}</div>
                        <div><strong>Họ tên:</strong> {{ viewingCustomer.hoTen }}</div>
                        <div><strong>Email:</strong> {{ viewingCustomer.email }}</div>
                        <div><strong>SĐT:</strong> {{ viewingCustomer.sdt }}</div>
                        <div>
                            <strong>Trạng thái:</strong>
                            <Tag 
                                :value="getStatusLabel(viewingCustomer.trangThai)" 
                                :severity="getStatusSeverity(viewingCustomer.trangThai)" 
                                class="ml-2"
                            />
                        </div>
                    </div>
                </div>

                <!-- Thông tin tài khoản -->
                <div class="rounded-lg bg-green-50 p-4 border border-green-200">
                    <h6 class="mb-3 font-semibold text-green-700 flex items-center">
                        <i class="pi pi-id-card mr-2"></i>
                        Tài khoản liên kết
                    </h6>
                    <div v-if="viewingCustomer.idTaiKhoan" class="text-sm">
                        <div><strong>ID Tài khoản:</strong> #{{ viewingCustomer.idTaiKhoan }}</div>
                        <div><strong>Email đăng nhập:</strong> {{ viewingCustomer.email }}</div>
                    </div>
                    <div v-else class="text-orange-600">
                        Chưa liên kết với tài khoản nào
                    </div>
                </div>

                <!-- FIXED: Địa chỉ từ API -->
                <div v-if="viewingCustomer.danhSachDiaChi && viewingCustomer.danhSachDiaChi.length > 0" class="rounded-lg bg-indigo-50 p-4 border border-indigo-200">
                    <h6 class="mb-3 font-semibold text-indigo-700 flex items-center">
                        <i class="pi pi-map-marker mr-2"></i>
                        Địa chỉ ({{ viewingCustomer.danhSachDiaChi.length }})
                    </h6>
                    <div class="space-y-2">
                        <div v-for="(diaChi, index) in viewingCustomer.danhSachDiaChi" 
                             :key="index" 
                             class="border rounded p-3 bg-white" 
                             :class="{ 'border-green-500 bg-green-50': diaChi.isDefault }">
                            <div class="flex justify-between items-start">
                                <div>
                                    <p class="font-semibold">{{ diaChi.diaChiDayDu || formatAddressFromInfo(diaChi) }}</p>
                                    <div class="text-xs text-gray-600 mt-1">
                                        <span>Tỉnh: {{ diaChi.tenTinh || 'N/A' }}</span> | 
                                        <span>Phường/Xã: {{ diaChi.tenPhuong || 'N/A' }}</span>
                                    </div>
                                </div>
                                <Tag v-if="diaChi.isDefault" value="Mặc định" severity="success" />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <template #footer>
                <Button label="Đóng" icon="pi pi-times" text @click="viewDialog = false" />
                <Button label="Chỉnh sửa" icon="pi pi-pencil" @click="editFromView" />
            </template>
        </Dialog>

        <!-- Customer Edit Dialog -->
        <Dialog v-model:visible="customerDialog" :style="{ width: '1000px' }" header="Chỉnh sửa thông tin khách hàng" :modal="true">
            <div v-if="customer.id" class="flex flex-col gap-6">
                <!-- Thông tin cơ bản -->
                <div class="border-bottom pb-4">
                    <h6 class="mb-3 font-semibold flex items-center gap-2">
                        <i class="pi pi-user"></i>
                        Thông tin cơ bản
                    </h6>
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label for="editHoTen" class="mb-3 block font-bold text-red-600">Họ Tên *</label>
                            <InputText 
                                id="editHoTen" 
                                v-model.trim="customer.hoTen" 
                                required="true" 
                                :invalid="submitted && !customer.hoTen" 
                                fluid 
                            />
                            <small v-if="submitted && !customer.hoTen" class="text-red-500">
                                Họ tên là bắt buộc
                            </small>
                        </div>
                        <div>
                            <label for="editSdt" class="mb-3 block font-bold text-red-600">Số điện thoại *</label>
                            <InputText 
                                id="editSdt" 
                                v-model="customer.sdt" 
                                required="true" 
                                :invalid="submitted && (!customer.sdt || !isValidPhone(customer.sdt))" 
                                fluid 
                            />
                            <small v-if="submitted && !customer.sdt" class="text-red-500">
                                Số điện thoại là bắt buộc
                            </small>
                            <small v-if="submitted && customer.sdt && !isValidPhone(customer.sdt)" class="text-red-500">
                                Số điện thoại không hợp lệ
                            </small>
                        </div>
                    </div>
                    
                    <div class="mt-4">
                        <div>
                            <label for="editTrangThai" class="mb-3 block font-bold text-red-600">Trạng thái *</label>
                            <Dropdown 
                                id="editTrangThai" 
                                v-model="customer.trangThai" 
                                :options="statusOptionsForForm" 
                                optionLabel="label" 
                                optionValue="value" 
                                placeholder="Chọn trạng thái" 
                                :invalid="submitted && customer.trangThai === undefined" 
                                fluid 
                            />
                            <small v-if="submitted && customer.trangThai === undefined" class="text-red-500">
                                Trạng thái là bắt buộc
                            </small>
                        </div>
                    </div>
                </div>

                <!-- FIXED: Quản lý địa chỉ với API Việt Nam -->
                <div class="bg-indigo-50 p-4 rounded-lg border border-indigo-200">
                    <div class="flex justify-between items-center mb-3">
                        <h6 class="font-semibold text-indigo-700 flex items-center gap-2">
                            <i class="pi pi-map-marker"></i>
                            Quản lý địa chỉ (API Việt Nam)
                        </h6>
                        <Button
                            label="Thêm địa chỉ"
                            icon="pi pi-plus"
                            size="small"
                            @click="addNewAddress"
                            class="p-button-sm"
                        />
                    </div>

                    <!-- Thông báo hướng dẫn -->
                    <div class="mb-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
                        <div class="flex items-start gap-2">
                            <i class="pi pi-info-circle text-blue-600 mt-1"></i>
                            <div class="text-sm text-blue-800">
                                <p class="font-medium mb-1">Hướng dẫn:</p>
                                <p>• Chọn đầy đủ <strong>Tỉnh/Thành phố</strong> và <strong>Phường/Xã</strong> để địa chỉ được lưu</p>
                                <p>• Địa chỉ chưa hoàn chỉnh sẽ không được lưu vào hệ thống</p>
                                <p>• Cần có ít nhất một địa chỉ hoàn chỉnh để lưu thông tin khách hàng</p>
                            </div>
                        </div>
                    </div>

                    <div v-if="customer.danhSachDiaChi && customer.danhSachDiaChi.length > 0" class="space-y-3">
                        <div v-for="(diaChi, index) in customer.danhSachDiaChi" 
                             :key="index" 
                             :data-address-index="index"
                             class="border rounded-lg p-4 bg-white" 
                             :class="{ 'border-green-500 bg-green-50': diaChi.isDefault }">
                            
                            <div class="flex justify-between items-start mb-3">
                                <h6 class="font-semibold flex items-center gap-2">
                                    <i class="pi pi-home"></i>
                                    Địa chỉ {{ index + 1 }}
                                    <Tag 
                                        v-if="!diaChi.tenTinh || !diaChi.tenPhuong" 
                                        value="Chưa hoàn chỉnh" 
                                        severity="warning" 
                                        class="ml-2"
                                    />
                                    <Tag 
                                        v-else 
                                        value="Hoàn chỉnh" 
                                        severity="success" 
                                        class="ml-2"
                                    />
                                </h6>
                                <div class="flex gap-2">
                                    <Button
                                        icon="pi pi-eye"
                                        size="small"
                                        outlined
                                        @click="viewAddressDetail(diaChi, index)"
                                        title="Xem chi tiết địa chỉ"
                                    />
                                    <Button
                                        v-if="!diaChi.isDefault"
                                        label="Đặt mặc định"
                                        icon="pi pi-star"
                                        size="small"
                                        outlined
                                        @click="setDefaultAddress(index)"
                                        :disabled="!diaChi.tenTinh || !diaChi.tenPhuong"
                                    />
                                    <Tag v-else value="Mặc định" severity="success" />
                                    <Button
                                        icon="pi pi-trash"
                                        size="small"
                                        severity="danger"
                                        outlined
                                        @click="removeAddress(index)"
                                        :disabled="customer.danhSachDiaChi.length === 1"
                                        title="Xóa địa chỉ"
                                    />
                                </div>
                            </div>

                            <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
                                <div>
                                    <label class="block text-sm font-medium mb-1">Tỉnh/Thành phố</label>
                                    <Dropdown
                                        :model-value="diaChi.maTinh"
                                        :options="provinces"
                                        optionLabel="name"
                                        optionValue="code"
                                        placeholder="Chọn tỉnh/thành phố"
                                        fluid
                                        :loading="loadingProvinces"
                                        @change="(event) => onAddressProvinceChange(event.value, index)"
                                        showClear
                                    />
                                </div>
                                <div>
                                    <label class="block text-sm font-medium mb-1">Phường/Xã</label>
                                    <Dropdown
                                        :model-value="diaChi.maPhuong"
                                        :options="diaChi.availableWards || []"
                                        optionLabel="name"
                                        optionValue="code"
                                        placeholder="Chọn phường/xã"
                                        fluid
                                        :loading="loadingWards"
                                        :disabled="!diaChi.maTinh"
                                        @change="(event) => onAddressWardChange(event.value, index)"
                                        showClear
                                    />
                                    <small v-if="!diaChi.maTinh" class="text-gray-500">
                                        Vui lòng chọn tỉnh/thành phố trước
                                    </small>
                                </div>
                                <div class="md:col-span-2">
                                    <label class="block text-sm font-medium mb-1">Địa chỉ chi tiết</label>
                                    <InputText
                                        v-model="diaChi.diaChiChiTiet"
                                        placeholder="Số nhà, tên đường..."
                                        fluid
                                        @input="updateAddressFullText(index)"
                                    />
                                </div>
                            </div>

                            <!-- Địa chỉ đầy đủ preview -->
                            <div class="mt-3 p-2 bg-gray-50 rounded">
                                <small class="text-gray-600">Địa chỉ đầy đủ:</small>
                                <p class="font-medium text-gray-800 mt-1">
                                    {{ formatFullAddressEdit(diaChi) }}
                                </p>
                            </div>
                        </div>
                    </div>

                    <div v-else class="text-center text-indigo-600 py-6">
                        <i class="pi pi-map-marker text-3xl mb-2"></i>
                        <p class="font-medium">Chưa có địa chỉ</p>
                        <p class="text-sm">Nhấn "Thêm địa chỉ" để thêm địa chỉ mới</p>
                    </div>
                </div>
            </div>

            <template #footer>
                <Button label="Hủy" icon="pi pi-times" text @click="hideDialog" :disabled="saving" />
                <Button 
                    label="Lưu thay đổi" 
                    icon="pi pi-check" 
                    @click="saveCustomer" 
                    :loading="saving" 
                />
            </template>
        </Dialog>

        <!-- Address Detail Dialog -->
        <Dialog v-model:visible="addressDetailDialog" :style="{ width: '600px' }" header="Chi tiết địa chỉ" :modal="true">
            <div v-if="viewingAddress" class="space-y-4">
                <div class="grid grid-cols-2 gap-4">
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">Tỉnh/Thành phố</label>
                        <div class="p-2 bg-gray-50 rounded border">
                            {{ viewingAddress.tenTinh || 'Chưa chọn' }}
                        </div>
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">Phường/Xã</label>
                        <div class="p-2 bg-gray-50 rounded border">
                            {{ viewingAddress.tenPhuong || 'Chưa chọn' }}
                        </div>
                    </div>
                    <div class="col-span-2">
                        <label class="block text-sm font-medium text-gray-700 mb-1">Địa chỉ chi tiết</label>
                        <div class="p-2 bg-gray-50 rounded border">
                            {{ viewingAddress.diaChiChiTiet || 'Chưa nhập' }}
                        </div>
                    </div>
                    <div class="col-span-2">
                        <label class="block text-sm font-medium text-gray-700 mb-1">Địa chỉ đầy đủ</label>
                        <div class="p-2 bg-blue-50 rounded border border-blue-200">
                            <strong>{{ formatFullAddressEdit(viewingAddress) }}</strong>
                        </div>
                    </div>
                </div>
                
                <div class="flex items-center gap-4 pt-4 border-t">
                    <div class="flex items-center gap-2">
                        <i class="pi pi-info-circle text-blue-600"></i>
                        <span class="text-sm text-gray-600">
                            Trạng thái: 
                            <Tag 
                                :value="viewingAddress.trangThai === 1 ? 'Hoạt động' : 'Tạm khóa'" 
                                :severity="viewingAddress.trangThai === 1 ? 'success' : 'danger'" 
                            />
                        </span>
                    </div>
                    <div class="flex items-center gap-2">
                        <i class="pi pi-star text-yellow-600"></i>
                        <span class="text-sm text-gray-600">
                            Mặc định: 
                            <Tag 
                                :value="viewingAddress.isDefault ? 'Có' : 'Không'" 
                                :severity="viewingAddress.isDefault ? 'success' : 'secondary'" 
                            />
                        </span>
                    </div>
                </div>
            </div>
            <template #footer>
                <Button label="Đóng" icon="pi pi-times" @click="addressDetailDialog = false" />
                <Button 
                    label="Chỉnh sửa" 
                    icon="pi pi-pencil" 
                    @click="editAddressFromDetail" 
                    :disabled="!canEditCustomer"
                />
            </template>
        </Dialog>

        <!-- Address List Dialog -->
        <Dialog v-model:visible="addressListDialog" :style="{ width: '700px' }" header="Danh sách địa chỉ" :modal="true">
            <div v-if="selectedCustomerAddresses">
                <div v-if="selectedCustomerAddresses.danhSachDiaChi && selectedCustomerAddresses.danhSachDiaChi.length > 0" class="space-y-3">
                    <div v-for="(diaChi, index) in selectedCustomerAddresses.danhSachDiaChi" 
                         :key="index" 
                         class="border rounded p-4" 
                         :class="{ 'border-green-500 bg-green-50': diaChi.isDefault }">
                        <div class="flex justify-between items-start mb-3">
                            <h6 class="font-semibold">Địa chỉ {{ index + 1 }}</h6>
                            <Tag v-if="diaChi.isDefault" value="Mặc định" severity="success" />
                        </div>
                        <div class="text-sm space-y-1">
                            <p class="font-medium">{{ diaChi.diaChiDayDu || formatAddressFromInfo(diaChi) }}</p>
                            <div class="text-gray-600">
                                <div v-if="diaChi.diaChiChiTiet">Chi tiết: {{ diaChi.diaChiChiTiet }}</div>
                                <div>Phường/Xã: {{ diaChi.tenPhuong || 'N/A' }}</div>
                                <div>Tỉnh/TP: {{ diaChi.tenTinh || 'N/A' }}</div>
                            </div>
                        </div>
                    </div>
                </div>
                <div v-else class="text-center text-muted py-8">
                    <i class="pi pi-map-marker text-4xl mb-3"></i>
                    <h6>Chưa có địa chỉ</h6>
                </div>
            </div>
            <template #footer>
                <Button label="Đóng" icon="pi pi-times" @click="addressListDialog = false" />
            </template>
        </Dialog>
    </div>
</template>

<script setup>
import axios from 'axios'
import { useConfirm } from 'primevue/useconfirm'
import { useToast } from 'primevue/usetoast'
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

// Composables
const router = useRouter()
const toast = useToast()
const confirm = useConfirm()

// Reactive State
const dt = ref()
const customers = ref([])
const selectedCustomers = ref([])
const isLoading = ref(false)
const saving = ref(false)
const batchChangingStatus = ref(false)
const exporting = ref(false)
const submitted = ref(false)
const totalRecords = ref(0)

// Dialog States
const viewDialog = ref(false)
const customerDialog = ref(false)
const addressListDialog = ref(false)
const addressDetailDialog = ref(false)

// Form Data
const customer = ref({})
const viewingCustomer = ref(null)
const selectedCustomerAddresses = ref(null)
const viewingAddress = ref(null)
const viewingAddressIndex = ref(-1)

// Address Data - FIXED TO USE API
const provinces = ref([])
const wards = ref([])
const loadingProvinces = ref(false)
const loadingWards = ref(false)

// Search States
const globalSearch = ref('')
const advancedFilters = ref({
    trangThai: null,
    startDate: null,
    endDate: null
})

const pagination = ref({
    page: 0,
    size: 10,
    sortField: 'id',
    sortOrder: -1,
    totalElements: 0,
    totalPages: 0
})

// Options
const statusOptions = ref([
    { label: 'Tất cả trạng thái', value: null },
    { label: 'Hoạt động', value: 1 },
    { label: 'Tạm khóa', value: 0 }
])

const statusOptionsForForm = ref([
    { label: 'Hoạt động', value: 1 },
    { label: 'Tạm khóa', value: 0 }
])

// ===== UTILITY FUNCTIONS =====
const formatDate = (date) => {
    if (!date) return ''
    return new Date(date).toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    })
}

const getInitials = (name) => {
    if (!name) return 'KH'
    return name
        .split(' ')
        .map(word => word.charAt(0))
        .join('')
        .toUpperCase()
        .slice(0, 2)
}

const getStatusLabel = (status) => {
    return status === 1 ? 'Hoạt động' : 'Tạm khóa'
}

const getStatusSeverity = (status) => {
    return status === 1 ? 'success' : 'danger'
}

const getStatusIcon = (status) => {
    return status === 1 ? 'pi pi-check-circle' : 'pi pi-times-circle'
}

const getDefaultAddress = (customer) => {
    if (customer.diaChiMacDinh && customer.diaChiMacDinh.diaChiDayDu) {
        return truncateAddress(customer.diaChiMacDinh.diaChiDayDu)
    }
    if (customer.danhSachDiaChi && customer.danhSachDiaChi.length > 0) {
        const firstAddress = customer.danhSachDiaChi[0]
        return truncateAddress(firstAddress.diaChiDayDu || formatAddressFromInfo(firstAddress))
    }
    return null
}

const getAddressCount = (customer) => {
    return customer.danhSachDiaChi ? customer.danhSachDiaChi.length : 0
}

const formatAddressFromInfo = (address) => {
    if (!address) return 'Chưa có địa chỉ'
    
    const parts = [
        address.diaChiChiTiet,
        address.tenPhuong,
        address.tenTinh
    ].filter(part => part && part.trim() !== '')
    
    return parts.length > 0 ? parts.join(', ') : 'Chưa có địa chỉ'
}

const truncateAddress = (address) => {
    if (!address) return ''
    return address.length > 50 ? address.substring(0, 50) + '...' : address
}

const formatFullAddressEdit = (address) => {
    if (!address) return 'Chưa có địa chỉ'
    
    const parts = [
        address.diaChiChiTiet,
        address.tenPhuong,
        address.tenTinh
    ].filter(part => part && part.trim() !== '')
    
    return parts.length > 0 ? parts.join(', ') : 'Chưa có địa chỉ'
}

const isValidPhone = (phone) => /^0\d{9,10}$/.test(phone)

// ===== SEARCH FUNCTIONS =====
const clearGlobalSearch = () => {
    globalSearch.value = ''
    pagination.value.page = 0
    fetchData()
}

const resetAdvancedFilters = () => {
    advancedFilters.value = {
        trangThai: null,
        startDate: null,
        endDate: null
    }
    applyAdvancedFilters()
}

const applyAdvancedFilters = () => {
    pagination.value.page = 0
    fetchData()
}

// Debounced global search
const debounce = (func, wait) => {
    let timeout
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout)
            func(...args)
        }
        clearTimeout(timeout)
        timeout = setTimeout(later, wait)
    }
}

const debouncedGlobalSearch = debounce(() => {
    pagination.value.page = 0
    fetchData()
}, 500)

// ===== ADDRESS MANAGEMENT - FIXED TO USE API =====
const updateCustomerAddressesIntelligently = async (customerId, newAddresses) => {
    try {
        console.log('🧠 Updating addresses intelligently for customer:', customerId)
        
        // Lấy địa chỉ hiện tại từ API
        const currentAddresses = await fetchCustomerAddresses(customerId)
        console.log('📋 Current addresses from API:', currentAddresses)
        console.log('📋 New addresses to save:', newAddresses)
        
        // Logic mới: Xử lý từng địa chỉ một cách thông minh
        console.log('🔍 Analyzing address changes...')
        
        // Tạo map để theo dõi địa chỉ đã xử lý
        const processedAddresses = new Set()
        
        // Xử lý từng địa chỉ mới
        for (let i = 0; i < newAddresses.length; i++) {
            const newAddr = newAddresses[i]
            console.log(`📋 Processing new address ${i + 1}:`, newAddr.diaChiChiTiet)
            
            // Tìm địa chỉ hiện tại có cùng vị trí trong danh sách (index-based matching)
            let existingAddr = null
            if (i < currentAddresses.length) {
                existingAddr = currentAddresses[i]
                console.log(`🔍 Found existing address at position ${i}:`, existingAddr.diaChiChiTiet)
            }
            
            if (existingAddr) {
                // So sánh chi tiết để xem có thay đổi không
                const hasLocationChange = (
                    existingAddr.maTinh !== newAddr.maTinh ||
                    existingAddr.maPhuong !== newAddr.maPhuong ||
                    existingAddr.tenTinh !== newAddr.tenTinh ||
                    existingAddr.tenPhuong !== newAddr.tenPhuong
                )
                
                const hasDetailChange = (
                    existingAddr.diaChiChiTiet !== newAddr.diaChiChiTiet ||
                    existingAddr.isDefault !== newAddr.isDefault
                )
                
                if (hasLocationChange || hasDetailChange) {
                    console.log('✏️ Updating address:', existingAddr.id, 'Changes:', { hasLocationChange, hasDetailChange })
                    await updateCustomerAddress(existingAddr.id, newAddr)
                    console.log('✅ Updated address:', existingAddr.id)
                } else {
                    console.log('⏭️ No changes for address:', existingAddr.id)
                }
                
                processedAddresses.add(existingAddr.id)
            } else {
                // Địa chỉ mới, thêm vào
                console.log('➕ Adding new address:', newAddr.diaChiChiTiet)
                await addAddressToCustomer(customerId, newAddr)
            }
        }
        
        // Xóa địa chỉ không còn cần (những địa chỉ không được xử lý)
        const addressesToDelete = currentAddresses.filter(current => !processedAddresses.has(current.id))
        
        for (const addrToDelete of addressesToDelete) {
            if (addrToDelete.isDefault) {
                console.log('⚠️ Skipping deletion of default address:', addrToDelete.id, addrToDelete.diaChiChiTiet)
                continue
            }
            console.log('🗑️ Deleting unused address:', addrToDelete.id, addrToDelete.diaChiChiTiet)
            await deleteCustomerAddress(addrToDelete.id, customerId)
            console.log('✅ Deleted address:', addrToDelete.id)
        }
        
        console.log('✅ Intelligent address update completed')
        
    } catch (error) {
        console.error('❌ Error in intelligent address update:', error)
        throw error
    }
}

const fetchProvinces = async () => {
    if (provinces.value.length > 0) return
    
    loadingProvinces.value = true
    try {
        console.log('🌍 Fetching provinces from Vietnam API...')
        const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/vietnam-address/provinces`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
            }
        })
        
        if (response.data && response.data.success && response.data.data) {
            provinces.value = response.data.data.map(item => ({
                code: item.code.toString(),
                name: item.name,
                codename: item.codename
            }))
            console.log('✅ Loaded provinces from API:', provinces.value.length)
        } else {
            provinces.value = [
                { code: '1', name: 'Hà Nội', codename: 'ha_noi' },
                { code: '79', name: 'TP. Hồ Chí Minh', codename: 'ho_chi_minh' },
                { code: '48', name: 'Đà Nẵng', codename: 'da_nang' }
            ]
        }
    } catch (error) {
        console.error('Error loading provinces:', error)
        provinces.value = [
            { code: '1', name: 'Hà Nội', codename: 'ha_noi' },
            { code: '79', name: 'TP. Hồ Chí Minh', codename: 'ho_chi_minh' },
            { code: '48', name: 'Đà Nẵng', codename: 'da_nang' }
        ]
    } finally {
        loadingProvinces.value = false
    }
}

const fetchWards = async (provinceCode) => {
    if (!provinceCode) {
        wards.value = []
        return
    }
    
    loadingWards.value = true
    try {
        console.log('🏘️ Fetching wards for province:', provinceCode)
        const response = await axios.get(`http://localhost:8080/api/vietnam-address/wards/${provinceCode}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
            }
        })
        
        if (response.data && response.data.success && response.data.data) {
            wards.value = response.data.data.map(item => ({
                code: item.code.toString(),
                name: item.name,
                codename: item.codename
            }))
            console.log('✅ Loaded wards from API:', wards.value.length)
        } else {
            wards.value = [
                { code: '1', name: 'Phường/Xã 1', codename: 'phuong_xa_1' },
                { code: '2', name: 'Phường/Xã 2', codename: 'phuong_xa_2' },
                { code: '3', name: 'Phường/Xã 3', codename: 'phuong_xa_3' }
            ]
        }
    } catch (error) {
        console.error('Error loading wards:', error)
        wards.value = [
            { code: '1', name: 'Phường/Xã 1', codename: 'phuong_xa_1' },
            { code: '2', name: 'Phường/Xã 2', codename: 'phuong_xa_2' }
        ]
    } finally {
        loadingWards.value = false
    }
}

// ===== CUSTOMER ADDRESS API FUNCTIONS =====
const fetchCustomerAddresses = async (customerId) => {
    try {
        console.log('🏠 Fetching addresses for customer:', customerId)
        
        // Tìm customer để lấy idTaiKhoan
        const customerData = customers.value.find(c => c.id === customerId)
        if (!customerData || !customerData.idTaiKhoan) {
            console.log('⚠️ No account found for customer:', customerId)
            return []
        }
        
        const response = await axios.get(`http://localhost:8080/api/dia-chi/tai-khoan/${customerData.idTaiKhoan}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
            },
            timeout: 10000
        })
        
        if (response.data && Array.isArray(response.data)) {
            console.log('✅ Addresses fetched from API:', response.data)
            return response.data
        } else {
            console.log('⚠️ No addresses found for customer:', customerId)
            return []
        }
        
    } catch (error) {
        console.error('❌ Error fetching customer addresses:', error)
        return []
    }
}

const addAddressToCustomer = async (customerId, addressData) => {
    try {
        console.log('➕ Adding address for customer:', customerId, addressData)
        
        // Tìm customer để lấy idTaiKhoan
        const customerData = customers.value.find(c => c.id === customerId)
        if (!customerData || !customerData.idTaiKhoan) {
            throw new Error('Không tìm thấy tài khoản của khách hàng')
        }
        
        const payload = {
            idTaiKhoan: customerData.idTaiKhoan,
            diaChiChiTiet: addressData.diaChiChiTiet,
            tenPhuong: addressData.tenPhuong,
            tenTinh: addressData.tenTinh,
            maPhuong: addressData.maPhuong,
            maTinh: addressData.maTinh,
            trangThai: 1
        }
        
        const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/api/dia-chi`, payload, {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
            },
            timeout: 10000
        })
        
        if (response.data && response.data.data) {
            console.log('✅ Address added successfully:', response.data.data)
            return response.data.data
        } else {
            throw new Error('API response không hợp lệ')
        }
        
    } catch (error) {
        console.error('❌ Error adding address:', error)
        throw error
    }
}

const updateCustomerAddress = async (addressId, addressData) => {
    try {
        console.log('✏️ Updating address:', addressId, addressData)
        
        // Tìm customer để lấy idTaiKhoan
        const customerData = customers.value.find(c => c.id === customer.value.id)
        if (!customerData || !customerData.idTaiKhoan) {
            throw new Error('Không tìm thấy tài khoản của khách hàng')
        }
        
        const payload = {
            idTaiKhoan: customerData.idTaiKhoan,
            diaChiChiTiet: addressData.diaChiChiTiet,
            tenPhuong: addressData.tenPhuong,
            tenTinh: addressData.tenTinh,
            maPhuong: addressData.maPhuong,
            maTinh: addressData.maTinh,
            trangThai: 1
        }
        
        const response = await axios.put(`http://localhost:8080/api/dia-chi/${addressId}`, payload, {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
            },
            timeout: 10000
        })
        
        if (response.data && response.data.data) {
            console.log('✅ Address updated successfully:', response.data.data)
            return response.data.data
        } else {
            throw new Error('API response không hợp lệ')
        }
        
    } catch (error) {
        console.error('❌ Error updating address:', error)
        throw error
    }
}

const deleteCustomerAddress = async (addressId, customerId) => {
    try {
        console.log('🗑️ Deleting address:', addressId, 'for customer:', customerId)
        
        // Tìm khách hàng để lấy idTaiKhoan
        const customer = customers.value.find(cus => cus.id === customerId)
        if (!customer || !customer.idTaiKhoan) {
            throw new Error('Không tìm thấy tài khoản của khách hàng')
        }
        
        // Kiểm tra xem đây có phải địa chỉ local không
        if (addressId && addressId.toString().startsWith('local_')) {
            console.log('🔄 Deleting local address (no API call needed)')
            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: 'Đã xóa địa chỉ tạm thời',
                life: 3000
            })
            return true
        }
        
        try {
            const response = await axios.delete(`http://localhost:8080/api/dia-chi/${addressId}`, {
                timeout: 5000
            })
            
            if (response.data && response.data.success) {
                console.log('✅ Address deleted successfully via API')
                toast.add({
                    severity: 'success',
                    summary: 'Thành công',
                    detail: 'Đã xóa địa chỉ',
                    life: 3000
                })
                return true
            }
            
            throw new Error('API response không hợp lệ')
        } catch (apiError) {
            console.warn('⚠️ Address API not available, treating as local deletion:', apiError.response?.status)
            
            toast.add({
                severity: 'warn',
                summary: 'Xóa tạm thời',
                detail: 'Địa chỉ được xóa tạm thời (API chưa sẵn sàng)',
                life: 3000
            })
            
            return true
        }
    } catch (error) {
        console.error('❌ Error deleting address:', error)
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: `Không thể xóa địa chỉ: ${error.response?.data?.message || error.message}`,
            life: 5000
        })
        throw error
    }
}

// For editing addresses with API data
const onAddressProvinceChange = async (provinceCode, addressIndex) => {
    if (!customer.value.danhSachDiaChi[addressIndex]) return
    
    // Clear ward selection
    customer.value.danhSachDiaChi[addressIndex].maPhuong = ''
    customer.value.danhSachDiaChi[addressIndex].tenPhuong = ''
    
    // Set province info
    const selectedProvince = provinces.value.find(p => p.code === provinceCode)
    if (selectedProvince) {
        customer.value.danhSachDiaChi[addressIndex].tenTinh = selectedProvince.name
        customer.value.danhSachDiaChi[addressIndex].maTinh = provinceCode
    }
    
    // Load wards for this province
    await fetchWardsForAddress(provinceCode, addressIndex)
    updateAddressFullText(addressIndex)
}

const onAddressWardChange = (wardCode, addressIndex) => {
    if (!customer.value.danhSachDiaChi[addressIndex]) return
    
    const availableWards = customer.value.danhSachDiaChi[addressIndex].availableWards || []
    const selectedWard = availableWards.find(w => w.code === wardCode)
    if (selectedWard) {
        customer.value.danhSachDiaChi[addressIndex].tenPhuong = selectedWard.name
        customer.value.danhSachDiaChi[addressIndex].maPhuong = wardCode
    }
    updateAddressFullText(addressIndex)
}

const fetchWardsForAddress = async (provinceCode, addressIndex) => {
    if (!provinceCode || !customer.value.danhSachDiaChi || !customer.value.danhSachDiaChi[addressIndex]) return
    
    try {
        const response = await axios.get(`http://localhost:8080/api/vietnam-address/wards/${provinceCode}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
            }
        })
        
        let wardsData = []
        if (response.data && response.data.success && response.data.data) {
            wardsData = response.data.data.map(item => ({
                code: item.code.toString(),
                name: item.name,
                codename: item.codename
            }))
        } else {
            wardsData = [
                { code: '1', name: 'Phường/Xã 1', codename: 'phuong_xa_1' },
                { code: '2', name: 'Phường/Xã 2', codename: 'phuong_xa_2' }
            ]
        }
        
        customer.value.danhSachDiaChi[addressIndex].availableWards = wardsData
    } catch (error) {
        console.error('❌ Error fetching wards for address:', error)
        customer.value.danhSachDiaChi[addressIndex].availableWards = [
            { code: '1', name: 'Phường/Xã 1', codename: 'phuong_xa_1' }
        ]
    }
}

const updateAddressFullText = (index) => {
    if (!customer.value.danhSachDiaChi || !customer.value.danhSachDiaChi[index]) return
    
    const address = customer.value.danhSachDiaChi[index]
    const parts = [
        address.diaChiChiTiet,
        address.tenPhuong,
        address.tenTinh
    ].filter(part => part && part.trim() !== '')
    
    address.diaChiDayDu = parts.join(', ')
}

// Address management functions
const addNewAddress = () => {
    if (!customer.value.danhSachDiaChi) {
        customer.value.danhSachDiaChi = []
    }
    
    const newAddress = {
        diaChiChiTiet: '',
        tenPhuong: '',
        tenTinh: '',
        maPhuong: '',
        maTinh: '',
        diaChiDayDu: '',
        availableWards: [],
        isDefault: customer.value.danhSachDiaChi.length === 0,
        trangThai: 1
    }
    
    customer.value.danhSachDiaChi.push(newAddress)
    
    toast.add({
        severity: 'info',
        summary: 'Thêm địa chỉ',
        detail: 'Đã thêm địa chỉ mới. Vui lòng chọn đầy đủ tỉnh/thành phố và phường/xã.',
        life: 3000
    })
}

const removeAddress = (index) => {
    if (!canEditCustomer.value) {
        toast.add({
            severity: 'warn',
            summary: 'Không có quyền',
            detail: 'Chỉ tài khoản ADMIN mới có thể xoá địa chỉ khách hàng',
            life: 3000
        })
        return
    }
    
    if (!customer.value.danhSachDiaChi || customer.value.danhSachDiaChi.length <= 1) {
        toast.add({
            severity: 'warn',
            summary: 'Không thể xóa',
            detail: 'Khách hàng phải có ít nhất một địa chỉ',
            life: 3000
        })
        return
    }
    
    const addressToRemove = customer.value.danhSachDiaChi[index]
    const isRemovedDefault = addressToRemove.isDefault
    
    // Xác nhận trước khi xóa
    confirm.require({
        message: `Bạn có chắc chắn muốn xóa địa chỉ "${addressToRemove.diaChiDayDu || formatFullAddressEdit(addressToRemove)}"?`,
        header: 'Xác nhận xóa địa chỉ',
        icon: 'pi pi-exclamation-triangle',
        rejectClass: 'p-button-secondary p-button-outlined',
        rejectLabel: 'Hủy',
        acceptLabel: 'Xóa',
        accept: () => {
            customer.value.danhSachDiaChi.splice(index, 1)
            
            // Nếu xóa địa chỉ mặc định, tự động đặt địa chỉ đầu tiên còn lại làm mặc định
            if (isRemovedDefault && customer.value.danhSachDiaChi.length > 0) {
                customer.value.danhSachDiaChi.forEach((addr, i) => addr.isDefault = (i === 0))
            }
            
            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: 'Đã xóa địa chỉ thành công',
                life: 3000
            })
        }
    })
}


const setDefaultAddress = (index) => {
    if (!customer.value.danhSachDiaChi) return
    
    const addressToSetDefault = customer.value.danhSachDiaChi[index]
    
    // Kiểm tra địa chỉ có hoàn chỉnh không
    if (!addressToSetDefault.tenTinh || !addressToSetDefault.tenPhuong) {
        toast.add({
            severity: 'warn',
            summary: 'Không thể đặt mặc định',
            detail: 'Chỉ có thể đặt địa chỉ hoàn chỉnh làm mặc định',
            life: 3000
        })
        return
    }
    
    // Bỏ mặc định cho tất cả địa chỉ
    customer.value.danhSachDiaChi.forEach(addr => addr.isDefault = false)
    
    // Đặt địa chỉ này làm mặc định
    customer.value.danhSachDiaChi[index].isDefault = true
    
    toast.add({
        severity: 'success',
        summary: 'Thành công',
        detail: 'Đã đặt địa chỉ làm mặc định',
        life: 3000
    })
}

// ===== MAIN API FUNCTION =====
const fetchData = async () => {
    isLoading.value = true
    try {
        const params = {
            page: pagination.value.page,
            size: pagination.value.size,
            sortBy: pagination.value.sortField || 'id',
            sortDir: pagination.value.sortOrder === 1 ? 'asc' : 'desc'
        }

        let endpoint = `${import.meta.env.VITE_API_BASE_URL}/api/khach-hang`

        // Add search parameter if exists
        if (globalSearch.value && globalSearch.value.trim()) {
            params.search = globalSearch.value.trim()
        }

        // Advanced filters
        if (advancedFilters.value.trangThai !== null && advancedFilters.value.trangThai !== undefined) {
            params.trangThai = advancedFilters.value.trangThai
        }
        if (advancedFilters.value.startDate) {
            params.startDate = advancedFilters.value.startDate.toISOString().split('T')[0]
        }
        if (advancedFilters.value.endDate) {
            params.endDate = advancedFilters.value.endDate.toISOString().split('T')[0]
        }

        const response = await axios.get(endpoint, { 
            params,
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
            }
        })

        if (response.data) {
            if (response.data.content && Array.isArray(response.data.content)) {
                customers.value = response.data.content
                pagination.value.totalElements = response.data.totalElements || 0
                pagination.value.totalPages = response.data.totalPages || 0
                totalRecords.value = response.data.totalElements || 0
            } else if (Array.isArray(response.data)) {
                customers.value = response.data
                pagination.value.totalElements = response.data.length
                totalRecords.value = response.data.length
            } else {
                customers.value = []
                pagination.value.totalElements = 0
                totalRecords.value = 0
            }
        }
    } catch (error) {
        console.error('Error fetching customers:', error)
        handleApiError(error, 'Không thể tải danh sách khách hàng')
        customers.value = []
        totalRecords.value = 0
    } finally {
        isLoading.value = false
    }
}

// ===== CRUD OPERATIONS =====
const saveCustomer = async () => {
    submitted.value = true
    saving.value = true
    
    try {
        if (!customer.value.id) {
            toast.add({
                severity: 'warn',
                summary: 'Cảnh báo',
                detail: 'Không thể lưu khách hàng không tồn tại.',
                life: 3000
            })
            return
        }

        const validationErrors = validateCustomerData()
        if (validationErrors.length > 0) {
            toast.add({
                severity: 'warn',
                summary: 'Dữ liệu không hợp lệ',
                detail: validationErrors[0],
                life: 3000
            })
            return
        }

        // Xử lý địa chỉ - CHỈ LẤY ĐỊA CHỈ HOÀN CHỈNH (có đầy đủ tỉnh và phường)
        const processedAddresses = customer.value.danhSachDiaChi?.filter(addr => {
            // Chỉ lấy địa chỉ đã chọn đầy đủ tỉnh và phường
            return addr.tenTinh && addr.tenTinh.trim() !== '' && 
                   addr.tenPhuong && addr.tenPhuong.trim() !== ''
        }).map(addr => ({
            diaChiChiTiet: addr.diaChiChiTiet?.trim() || '',
            tenPhuong: addr.tenPhuong.trim(),
            tenTinh: addr.tenTinh.trim(),
            diaChiDayDu: formatFullAddressEdit(addr),
            isDefault: addr.isDefault || false,
            maPhuong: addr.maPhuong || null,
            maTinh: addr.maTinh || null,
            trangThai: 1
        })) || []

        // Kiểm tra có địa chỉ hoàn chỉnh không
        if (processedAddresses.length === 0) {
            toast.add({
                severity: 'warn',
                summary: 'Thiếu thông tin địa chỉ',
                detail: 'Vui lòng chọn đầy đủ tỉnh/thành phố và phường/xã cho ít nhất một địa chỉ',
                life: 4000
            })
            return
        }

        // Đảm bảo có địa chỉ mặc định
        if (processedAddresses.length > 0) {
            const hasDefault = processedAddresses.some(addr => addr.isDefault)
            if (!hasDefault) {
                processedAddresses[0].isDefault = true
            }
        }

        const customerData = {
            hoTen: customer.value.hoTen.trim(),
            sdt: customer.value.sdt.trim(),
            trangThai: customer.value.trangThai
        }
        
        // Chỉ thêm ngaySinh nếu có
        if (customer.value.ngaySinh) {
            customerData.ngaySinh = customer.value.ngaySinh.toISOString().split('T')[0]
        }

        console.log('📤 Sending customer data:', customerData)

        await axios.put(`http://localhost:8080/api/khach-hang/${customer.value.id}`, customerData, {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
            }
        })
        
        // Cập nhật địa chỉ thông minh
        console.log('🏠 Updating addresses intelligently:', processedAddresses)
        await updateCustomerAddressesIntelligently(customer.value.id, processedAddresses)
        console.log('✅ Addresses updated successfully')
        
        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: 'Cập nhật thông tin khách hàng và địa chỉ thành công',
            life: 3000
        })

        await fetchData()
        hideDialog()
    } catch (error) {
        console.error('❌ Error saving customer:', error)
        console.error('❌ Error response:', error.response?.data)
        console.error('❌ Error status:', error.response?.status)
        console.error('❌ Error headers:', error.response?.headers)
        
        if (error.response?.data?.errors) {
            console.error('❌ Validation errors:', error.response.data.errors)
        }
        
        handleApiError(error, 'Không thể cập nhật thông tin khách hàng')
    } finally {
        saving.value = false
    }
}
const formatDateOnly = (date) => {
    if (!date) return 'Chưa có'
    return new Date(date).toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    })
}
const changeStatus = async (customerData) => {
    if (!canEditCustomer.value) {
        toast.add({
            severity: 'warn',
            summary: 'Không có quyền',
            detail: 'Chỉ tài khoản ADMIN mới có thể thay đổi trạng thái khách hàng',
            life: 3000
        })
        return
    }

    try {
        const newStatus = customerData.trangThai === 1 ? 0 : 1
        
        await axios.patch(`http://localhost:8080/api/khach-hang/${customerData.id}/status`, {
            trangThai: newStatus
        }, {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
            }
        })

        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: `Đã ${newStatus === 1 ? 'kích hoạt' : 'tạm khóa'} khách hàng ${customerData.hoTen} và đồng bộ tài khoản`,
            life: 3000
        })

        await fetchData()
    } catch (error) {
        console.error('Error changing status:', error)
        handleApiError(error, 'Không thể thay đổi trạng thái')
    }
}
const confirmBatchStatusChange = () => {
    if (batchChangingStatus.value) return

    if (!canEditCustomer.value) {
        toast.add({
            severity: 'warn',
            summary: 'Không có quyền',
            detail: 'Chỉ tài khoản ADMIN mới có thể thay đổi trạng thái hàng loạt',
            life: 3000
        })
        return
    }

    if (!selectedCustomers.value || !selectedCustomers.value.length) return

    confirm.require({
        message: `Bạn có muốn thay đổi trạng thái của ${selectedCustomers.value.length} khách hàng đã chọn?`,
        header: 'Xác nhận thay đổi trạng thái',
        icon: 'pi pi-question-circle',
        rejectClass: 'p-button-secondary p-button-outlined',
        rejectLabel: 'Hủy',
        acceptLabel: 'Thực hiện',
        accept: () => batchChangeStatus()
    })
}
const getCurrentUser = () => {
    try {
        const userInfo = localStorage.getItem('user_info')
        if (userInfo) {
            return JSON.parse(userInfo)
        }
        const userData = localStorage.getItem('user')
        if (userData) {
            return JSON.parse(userData)
        }
        const sessionUser = sessionStorage.getItem('currentUser')
        if (sessionUser) {
            return JSON.parse(sessionUser)
        }
        return null
    } catch (error) {
        console.error('Error getting user:', error)
        return null
    }
}
const isAdmin = computed(() => {
    const user = getCurrentUser()
    const result = user?.vaiTro === 'ADMIN' || user?.role === 'ADMIN' || user?.vai_tro === 'ADMIN'
    console.log('Admin check for customer management:', { user, result })
    return result
})

const currentUserRole = computed(() => {
    const user = getCurrentUser()
    return user?.vaiTro || user?.role || user?.vai_tro || null
})

const canEditCustomer = computed(() => {
    return currentUserRole.value === 'ADMIN'
})

const canViewOnly = computed(() => {
    // USER/NHANVIEN chỉ được xem
    return ['USER', 'NHANVIEN'].includes(currentUserRole.value)
})
const batchChangeStatus = async () => {
    if (batchChangingStatus.value) return

    const selectedSnapshot = [...selectedCustomers.value]
    if (!selectedSnapshot.length) return

    batchChangingStatus.value = true

    try {
    const promises = selectedSnapshot.map(customer =>
            axios.patch(`http://localhost:8080/api/khach-hang/${customer.id}/status`, { 
                trangThai: customer.trangThai === 1 ? 0 : 1 
            }, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
                }
            })
        )
        
        const results = await Promise.allSettled(promises)
        const failedResults = results.filter(result => result.status === 'rejected')

        if (failedResults.length > 0) {
            const firstError = failedResults[0].reason
            console.error('Batch status change failures:', failedResults)
            handleApiError(
                firstError,
                `Đã đổi ${selectedSnapshot.length - failedResults.length}/${selectedSnapshot.length} khách hàng. Một số khách hàng đổi trạng thái thất bại.`
            )
        } else {
            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: `Đã thay đổi trạng thái ${selectedSnapshot.length} khách hàng`,
                life: 3000
            })
        }
        
        selectedCustomers.value = []
        await fetchData()
    } catch (error) {
        console.error('Error batch changing status:', error)
        handleApiError(error, 'Không thể thay đổi trạng thái hàng loạt')
    } finally {
        batchChangingStatus.value = false
    }
}

// ===== PAGINATION HANDLERS =====
const onPageChange = (event) => {
    pagination.value.page = event.page
    pagination.value.size = event.rows
    fetchData()
}

const onSort = (event) => {
    pagination.value.sortField = event.sortField
    pagination.value.sortOrder = event.sortOrder
    fetchData()
}

// ===== DIALOG FUNCTIONS =====
const viewCustomer = (customerData) => {
    viewingCustomer.value = { ...customerData }
    viewDialog.value = true
}

const editCustomer = (customerData) => {
    if (!canEditCustomer.value) {
        toast.add({
            severity: 'warn',
            summary: 'Không có quyền',
            detail: 'Chỉ tài khoản ADMIN mới có thể chỉnh sửa thông tin khách hàng',
            life: 3000
        })
        return
    }

    customer.value = { 
        ...customerData,
        originalMaKhachHang: customerData.maKhachHang,
        ngaySinh: customerData.ngaySinh ? new Date(customerData.ngaySinh) : null,
        danhSachDiaChi: customerData.danhSachDiaChi ? 
            customerData.danhSachDiaChi.map(addr => ({
                ...addr,
                availableWards: []
            })) : []
    }
    
    if (!customer.value.danhSachDiaChi || customer.value.danhSachDiaChi.length === 0) {
        customer.value.danhSachDiaChi = [{
            diaChiChiTiet: '',
            tenPhuong: '',
            tenTinh: '',
            maPhuong: '',
            maTinh: '',
            availableWards: [],
            isDefault: true
        }]
    } else {
        customer.value.danhSachDiaChi.forEach((addr, index) => {
            if (addr.maTinh) {
                fetchWardsForAddress(addr.maTinh, index)
            }
        })
    }
    
    submitted.value = false
    customerDialog.value = true
    fetchProvinces()
}
const editFromView = () => {
    editCustomer(viewingCustomer.value)
    viewDialog.value = false
}

const hideDialog = () => {
    customerDialog.value = false
    customer.value = {}
    submitted.value = false
}

const viewAllAddresses = (customerData) => {
    selectedCustomerAddresses.value = customerData
    addressListDialog.value = true
}

const viewAddressDetail = (address, index) => {
    viewingAddress.value = { ...address }
    viewingAddressIndex.value = index
    addressDetailDialog.value = true
}

const editAddressFromDetail = () => {
    addressDetailDialog.value = false
    // Focus vào địa chỉ đang xem trong form edit
    if (viewingAddressIndex.value >= 0) {
        // Scroll to address section
        setTimeout(() => {
            const addressElement = document.querySelector(`[data-address-index="${viewingAddressIndex.value}"]`)
            if (addressElement) {
                addressElement.scrollIntoView({ behavior: 'smooth', block: 'center' })
            }
        }, 100)
    }
}

// ===== EXPORT FUNCTIONS =====
const exportToExcel = async () => {
    exporting.value = true
    try {
        const headers = [
            'ID', 'Mã Khách Hàng', 'Họ Tên', 'Email', 'SĐT', 
            'Địa Chỉ', 'Trạng Thái', 'ID Tài Khoản', 'Ngày Tạo'
        ]

        const data = customers.value.map(customer => [
            customer.id,
            customer.maKhachHang || '',
            customer.hoTen || '',
            customer.email || '',
            customer.sdt || '',
            getDefaultAddress(customer) || 'Chưa có địa chỉ',
            getStatusLabel(customer.trangThai),
            customer.idTaiKhoan || 'Chưa liên kết',
            formatDate(customer.ngayTao)
        ])

        downloadExcel(headers, data, 'Danh_sach_khach_hang')
        
        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: `Đã xuất ${customers.value.length} khách hàng`,
            life: 3000
        })
    } catch (error) {
        console.error('Error exporting:', error)
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Không thể xuất file Excel',
            life: 3000
        })
    } finally {
        exporting.value = false
    }
}

const downloadExcel = (headers, data, filename) => {
    const csvContent = [headers, ...data]
        .map(row => row.map(field => `"${String(field).replace(/"/g, '""')}"`).join(','))
        .join('\n')

    const BOM = '\uFEFF'
    const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    
    link.setAttribute('href', url)
    link.setAttribute('download', `${filename}_${new Date().toISOString().split('T')[0]}.csv`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
}
const validateCustomerData = () => {
    const errors = []
    
    // SỬA: Ngăn sửa mã khách hàng - chỉ kiểm tra nếu có thay đổi
    if (customer.value.id && customer.value.originalMaKhachHang && 
        customer.value.maKhachHang && 
        customer.value.maKhachHang !== customer.value.originalMaKhachHang) {
        errors.push('Không thể thay đổi mã khách hàng đã được tạo')
    }
    
    // Validate họ tên
    if (!customer.value.hoTen || !customer.value.hoTen.trim()) {
        errors.push('Họ tên không được để trống')
    } else if (customer.value.hoTen.length > 100) {
        errors.push('Họ tên không được quá 100 ký tự')
    }
    
    // Validate số điện thoại
    if (!customer.value.sdt || !customer.value.sdt.trim()) {
        errors.push('Số điện thoại không được để trống')
    } else if (!isValidPhone(customer.value.sdt)) {
        errors.push('Số điện thoại không hợp lệ')
    }
    
    // Validate trạng thái
    if (customer.value.trangThai === undefined || customer.value.trangThai === null) {
        errors.push('Trạng thái không được để trống')
    }
    
    // Validate ngày sinh nếu có
    if (customer.value.ngaySinh) {
        const today = new Date()
        const birthDate = new Date(customer.value.ngaySinh)
        if (birthDate > today) {
            errors.push('Ngày sinh không thể lớn hơn ngày hiện tại')
        }
    }
    
    return errors
}// ===== ERROR HANDLING =====
const handleApiError = (error, defaultMessage) => {
    let errorMessage = defaultMessage
    
    if (error.response) {
        const { status, data } = error.response
        switch (status) {
            case 400:
                if (data.errors && typeof data.errors === 'object') {
                    const errorList = Object.values(data.errors).join(', ')
                    errorMessage = `Dữ liệu không hợp lệ: ${errorList}`
                } else {
                    errorMessage = data.message || data.error || 'Dữ liệu không hợp lệ'
                }
                break
            case 404:
                errorMessage = 'Không tìm thấy dữ liệu'
                break
            case 409:
                errorMessage = data.message || 'Email đã tồn tại trong hệ thống'
                break
            case 500:
                errorMessage = data.message || defaultMessage || 'Lỗi server nội bộ'
                break
            default:
                errorMessage = data.message || data.error || defaultMessage
        }
    } else if (error.code === 'ECONNREFUSED') {
        errorMessage = 'Không thể kết nối đến server'
    }

    toast.add({
        severity: 'error',
        summary: 'Lỗi',
        detail: errorMessage,
        life: 5000
    })
}

// ===== LIFECYCLE =====
onMounted(() => {
    fetchData()
})

// ===== WATCHERS =====
watch(() => pagination.value.size, () => {
    pagination.value.page = 0
    fetchData()
})
</script>

<style scoped>
.card {
    @apply bg-white rounded-lg shadow-sm border border-gray-200;
}

.card-header {
    @apply p-6 border-b border-gray-200;
}

.search-section {
    @apply p-6 bg-gray-50 border-b border-gray-200;
}

.responsive-table {
    @apply bg-white;
}

.address-display {
    @apply text-sm;
}

.text-muted {
    color: #6c757d;
}

.text-blue-600 {
    color: #2563eb;
}

.text-xs {
    font-size: 0.75rem;
}

.font-semibold {
    font-weight: 600;
}

.border-bottom {
    border-bottom: 1px solid #dee2e6;
}

:deep(.p-datatable) {
    @apply border-0;
}

:deep(.p-datatable-header) {
    @apply bg-gray-50 border-b border-gray-200;
}

:deep(.p-datatable-tbody tr) {
    @apply hover:bg-gray-50 transition-colors;
}

:deep(.p-paginator) {
    @apply bg-white border-t border-gray-200;
}

@media (max-width: 768px) {
    .search-section .grid {
        @apply grid-cols-1 gap-2;
    }
    
    :deep(.p-datatable-responsive-demo .p-datatable-tbody tr td) {
        @apply text-sm;
    }
}
</style>

