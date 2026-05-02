<template>
    <div class="card">
        <!-- Toolbar -->
        <Toolbar class="mb-6">
            <template #start>
                <Button label="Thêm tài khoản" icon="pi pi-plus" severity="secondary" class="mr-2" @click="openNew" />
                <Button 
                    label="Xóa đã chọn" 
                    icon="pi pi-trash" 
                    severity="secondary" 
                    @click="confirmDeleteSelected" 
                    :disabled="!selectedAccounts || !selectedAccounts.length" 
                />
            </template>
            <template #end>
                <Button 
                    label="Xuất CSV" 
                    icon="pi pi-upload" 
                    severity="secondary" p-button p-component p-button-icon-only p-button-danger p-button-outlined p-button-sm
                    @click="handleExportCSV" 
                    :loading="exporting" 
                />
            </template>
        </Toolbar>

        <!-- Enhanced Search and Filter Section -->
        <div class="search-filter-section mb-6 p-4 bg-gray-50 rounded-lg border">
            <!-- Global Search -->
            <div class="mb-4">
                <IconField>
                    <InputIcon>
                        <i class="pi pi-search" />
                    </InputIcon>
                    <InputText 
                        v-model="globalFilter" 
                        placeholder="Tìm kiếm tất cả thông tin tài khoản (email, mã TK, vai trò, trạng thái)..." 
                        class="w-full"
                        @input="debouncedSearch"
                    />
                </IconField>
            </div>

            <!-- Advanced Filters -->
            <div class="flex flex-wrap items-center gap-3">
                <Select 
                    v-model="roleFilter" 
                    :options="ROLE_FILTER_OPTIONS" 
                    optionLabel="label" 
                    optionValue="value" 
                    placeholder="Lọc vai trò" 
                    class="w-40"
                    @change="applyFilters"
                    showClear
                />

                <Select 
                    v-model="statusFilter" 
                    :options="STATUS_OPTIONS" 
                    optionLabel="label" 
                    optionValue="value" 
                    placeholder="Lọc trạng thái" 
                    class="w-40"
                    @change="applyFilters"
                    showClear
                />

                <DatePicker
                    v-model="dateFilters.startDate"
                    placeholder="Từ ngày"
                    dateFormat="dd/mm/yy"
                    @date-select="applyFilters"
                    @clear="applyFilters"
                    class="w-36"
                    showIcon
                    showClear
                    :maxDate="dateFilters.endDate || new Date()"
                />

                <DatePicker
                    v-model="dateFilters.endDate"
                    placeholder="Đến ngày"
                    dateFormat="dd/mm/yy"
                    @date-select="applyFilters"
                    @clear="applyFilters"
                    class="w-36"
                    showIcon
                    showClear
                    :minDate="dateFilters.startDate"
                    :maxDate="new Date()"
                />
                <Button
                    label="Reset"
                    icon="pi pi-refresh"
                    outlined
                    @click="resetFilters"
                />

                <div class="ml-auto flex items-center gap-2">
                    <Badge 
                        v-if="selectedAccounts && selectedAccounts.length" 
                        :value="`${selectedAccounts.length} đã chọn`" 
                        severity="info" 
                    />
                    <span class="text-sm text-gray-600">
                        Hiển thị {{ filteredAccounts.length }} / {{ accounts.length }} tài khoản
                    </span>
                </div>
            </div>
        </div>

        <!-- DataTable -->
        <DataTable
            ref="dt"
            v-model:selection="selectedAccounts"
            :value="filteredAccounts"
            dataKey="id"
            :paginator="true"
            :rows="PAGINATION_CONFIG.defaultRows"
            :paginatorTemplate="PAGINATION_CONFIG.paginatorTemplate"
            :rowsPerPageOptions="PAGINATION_CONFIG.rowsPerPageOptions"
            currentPageReportTemplate="Hiển thị {first} đến {last} của {totalRecords} tài khoản"
            :loading="isLoading"
            class="responsive-table"
        >
            <template #header>
                <div class="flex flex-wrap items-center justify-between gap-2">
                    <h4 class="m-0 text-xl font-bold text-gray-800">Quản Lý Tài Khoản</h4>
                    <div class="flex items-center gap-3">
                        <div class="stats-summary flex gap-4 text-sm">
                            <div class="flex items-center gap-1">
                                <div class="w-3 h-3 rounded bg-blue-500"></div>
                                <span>Tổng: {{ accounts.length }}</span>
                            </div>
                            <div class="flex items-center gap-1">
                                <div class="w-3 h-3 rounded bg-green-500"></div>
                                <span>Hoạt động: {{ getActiveAccountsCount() }}</span>
                            </div>
                            <div class="flex items-center gap-1">
                                <div class="w-3 h-3 rounded bg-red-500"></div>
                                <span>Khóa: {{ getInactiveAccountsCount() }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </template>

            <Column selectionMode="multiple" style="width: 3rem" :exportable="false"></Column>
            
            <Column field="id" header="ID" sortable style="width: 6rem">
                <template #body="slotProps">
                    <span class="font-bold text-primary">#{{ slotProps.data.id }}</span>
                </template>
            </Column>
            
            <Column field="maTaiKhoan" header="Mã TK" sortable style="width: 10rem">
                <template #body="slotProps">
                    <Tag :value="slotProps.data.maTaiKhoan" severity="info" />
                </template>
            </Column>
            
            <Column field="email" header="Email" sortable style="min-width: 20rem">
                <template #body="slotProps">
                    <div class="flex items-center">
                        <i class="pi pi-envelope text-muted mr-2"></i>
                        <span class="font-medium">{{ slotProps.data.email }}</span>
                    </div>
                </template>
            </Column>
            
            <Column field="vaiTro" header="Vai trò" sortable style="width: 12rem">
                <template #body="slotProps">
                    <Tag 
                        :value="getRoleLabel(slotProps.data.vaiTro)" 
                        :severity="getRoleSeverity(slotProps.data.vaiTro)"
                    >
                        <i :class="getRoleIcon(slotProps.data.vaiTro)" class="mr-1"></i>
                        {{ getRoleLabel(slotProps.data.vaiTro) }}
                    </Tag>
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
            
            <Column header="Thông tin liên kết" style="min-width: 15rem">
                <template #body="slotProps">
                    <div class="flex flex-col gap-1 text-sm">
                        <div v-if="slotProps.data.vaiTro === 'USER'" class="flex items-center gap-2">
                            <i class="pi pi-user text-blue-500"></i>
                            <span>KH: {{ getLinkedCustomerInfo(slotProps.data.id) }}</span>
                        </div>
                        <div v-else-if="slotProps.data.vaiTro === 'NHANVIEN'" class="flex items-center gap-2">
                            <i class="pi pi-users text-green-500"></i>
                            <span>NV: {{ getLinkedEmployeeInfo(slotProps.data.id) }}</span>
                        </div>
                        <div v-else class="flex items-center gap-2">
                            <i class="pi pi-crown text-yellow-500"></i>
                            <span>Admin hệ thống</span>
                        </div>
                        <div class="text-xs text-gray-500">
                            Tạo: {{ formatDate(slotProps.data.ngayTao) }}
                        </div>
                    </div>
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
                            outlined 
                            size="small" 
                            @click="viewAccount(slotProps.data)" 
                            title="Xem chi tiết" 
                        />
                        <Button 
                            icon="pi pi-pencil" 
                            outlined 
                            size="small" 
                            @click="editAccount(slotProps.data)" 
                            title="Sửa" 
                        />
                        <Button 
                            icon="pi pi-refresh" 
                            outlined 
                            severity="secondary" 
                            size="small" 
                            @click="handleChangeStatus(slotProps.data)" 
                            :title="slotProps.data.trangThai === 1 ? 'Ngưng hoạt động' : 'Kích hoạt'" 
                        />
                        <!-- <Button 
                            icon="pi pi-trash" 
                            outlined 
                            severity="danger" 
                            size="small" 
                            @click="confirmDeleteAccount(slotProps.data)" 
                            title="Xóa" 
                        /> -->
                    </div>
                </template>
            </Column>

            <template #empty>
                <div class="p-8 text-center">
                    <i class="pi pi-users text-gray-400 text-6xl mb-4"></i>
                    <h5 class="text-gray-600 mb-2">Không tìm thấy tài khoản</h5>
                    <p class="text-gray-500 mb-4">
                        {{ globalFilter ? 'Thử thay đổi từ khóa tìm kiếm hoặc thêm tài khoản mới.' : 'Thử thay đổi bộ lọc hoặc thêm tài khoản mới.' }}
                    </p>
                    <div class="flex gap-2 justify-center">
                        <Button
                            v-if="globalFilter"
                            label="Xóa tìm kiếm"
                            icon="pi pi-times"
                            outlined
                            @click="clearSearch"
                        />
                        <Button
                            label="Làm mới"
                            icon="pi pi-refresh"
                            outlined
                            @click="fetchData"
                        />
                        <Button
                            label="Thêm tài khoản"
                            icon="pi pi-plus"
                            @click="openNew"
                        />
                    </div>
                </div>
            </template>
        </DataTable>

        <!-- Add Account Dialog -->
        <Dialog v-model:visible="addDialog" :style="{ width: DIALOG_SIZES.EXTRA_LARGE }" header="Thêm tài khoản mới" :modal="true">
            <div class="flex flex-col gap-6">
                <!-- Chọn vai trò -->
                <div class="border-bottom pb-4">
                    <h5 class="mb-3 flex items-center gap-2">
                        <i class="pi pi-users"></i>
                        Chọn vai trò *
                    </h5>
                    <div class="mb-4">
                        <label for="vaiTro" class="mb-3 block font-bold">Vai trò *</label>
                        <Select
                            id="vaiTro"
                            v-model="newAccount.vaiTro"
                            :options="ROLE_OPTIONS_FOR_FORM"
                            optionLabel="label"
                            optionValue="value"
                            placeholder="Chọn vai trò"
                            :invalid="hasValidationError('vaiTro')"
                            fluid
                            @change="onRoleChange"
                        />
                        <small v-if="hasValidationError('vaiTro')" class="text-red-500">
                            {{ getValidationError('vaiTro') }}
                        </small>
                    </div>
                </div>

                <!-- Thông tin cá nhân (chỉ hiện khi không phải Admin) -->
                <div v-if="newAccount.vaiTro && newAccount.vaiTro !== 'ADMIN'" class="border-bottom pb-4">
                    <h5 class="mb-3 flex items-center gap-2">
                        <i class="pi pi-user"></i>
                        Thông tin {{ newAccount.vaiTro === 'USER' ? 'khách hàng' : 'nhân viên' }}
                    </h5>
                    
                    <!-- Thông tin cơ bản -->
                    <div class="grid grid-cols-2 gap-4 mb-4">
                        <div>
                            <label for="hoTen" class="mb-3 block font-bold">Họ và tên *</label>
                            <InputText 
                                id="hoTen" 
                                v-model.trim="personalInfo.hoTen" 
                                required="true" 
                                :invalid="hasValidationError('hoTen')" 
                                fluid 
                                placeholder="Nhập họ và tên"
                                ref="hoTenInput"
                                @input="clearFieldError('hoTen')"
                            />
                            <small v-if="hasValidationError('hoTen')" class="text-red-500">
                                {{ getValidationError('hoTen') }}
                            </small>
                        </div>
                        <div>
                            <label for="sdt" class="mb-3 block font-bold">Số điện thoại *</label>
                            <InputText 
                                id="sdt" 
                                v-model.trim="personalInfo.sdt" 
                                required="true" 
                                :invalid="hasValidationError('sdt')" 
                                fluid 
                                placeholder="Nhập số điện thoại"
                                @input="clearFieldError('sdt')"
                            />
                            <small v-if="hasValidationError('sdt')" class="text-red-500">
                                {{ getValidationError('sdt') }}
                            </small>
                        </div>
                    </div>
                    <div class="grid grid-cols-2 gap-4 mt-4">
     
<!-- Đoạn code cần sửa trong template (dòng 360-376) -->
<!-- <div class="grid grid-cols-2 gap-4 mt-4">
    <div>
        <label for="addNgaySinh" class="mb-3 block font-bold">Ngày sinh</label>
        <Calendar
            id="addNgaySinh"
            v-model="personalInfo.ngaySinh"
            dateFormat="dd/mm/yy"
            :maxDate="new Date()"
            showIcon
            showClear
            fluid
            placeholder="Chọn ngày sinh"
        />
        <small class="text-gray-500">Tùy chọn - để trống nếu không có</small>
    </div>
</div> -->
</div>



                    <div class="grid grid-cols-1 gap-4 mb-4">
                        <div>
                           <label for="personalEmail" class="mb-3 block font-bold">Email *</label>
    <InputText 
        id="personalEmail" 
        v-model.trim="personalInfo.email" 
        required="true" 
        :invalid="hasValidationError('email')" 
        fluid 
        placeholder="Nhập email"
        @input="syncEmailToAccount"
        @blur="validateEmailField"
    />
                            <small class="text-xs text-gray-500">
        Current: {{ personalInfo.email || '[empty]' }} | Account: {{ newAccount.email || '[empty]' }}
    </small>
    <small v-if="hasValidationError('email')" class="text-red-500">
        {{ getValidationError('email') }}
    </small>
                        </div>
                    </div>

                    <!-- Địa chỉ với API tích hợp -->
                    <div class="mt-4">
                        <label class="mb-3 block font-bold flex items-center gap-1">
                            <i class="pi pi-map-marker"></i>
                            Địa chỉ
                        </label>
                        
                        <!-- Tỉnh/Thành phố -->
                        <div class="grid grid-cols-2 gap-3 mb-3">
                            <div>
                                <label class="mb-2 block text-sm font-medium">Tỉnh/Thành phố</label>
                                <Select
                                    v-model="personalInfo.maTinh"
                                    :options="provinces"
                                    optionLabel="name"
                                    optionValue="code"
                                    placeholder="Chọn Tỉnh/TP"
                                    :loading="loadingProvinces"
                                    @change="onProvinceChange"
                                    fluid
                                    showClear
                                />
                            </div>
                            
                            <!-- Xã/Phường -->
                            <div>
                                <label class="mb-2 block text-sm font-medium">Xã/Phường</label>
                                <Select
                                    v-model="personalInfo.maPhuong"
                                    :options="wards"
                                    optionLabel="name"
                                    optionValue="code"
                                    placeholder="Chọn Xã/Phường"
                                    :disabled="!personalInfo.maTinh"
                                    :loading="loadingWards"
                                    @change="onWardChange"
                                    fluid
                                    showClear
                                />
                            </div>
                        </div>
                        
                        <!-- Địa chỉ chi tiết -->
                        <div class="mb-3">
                            <label class="mb-2 block text-sm font-medium">Địa chỉ chi tiết</label>
                            <InputText
                                v-model.trim="personalInfo.diaChiChiTiet"
                                placeholder="Số nhà, tên đường, ngõ..."
                                @input="updateFullAddress"
                                fluid
                            />
                        </div>
                        
                        <!-- Hiển thị địa chỉ đầy đủ -->
                        <div v-if="personalInfo.fullAddress" class="rounded bg-green-50 p-3 border border-green-200">
                            <label class="mb-1 block text-sm font-bold text-green-800 flex items-center gap-1">
                                <i class="pi pi-check-circle"></i>
                                Địa chỉ đầy đủ:
                            </label>
                            <p class="text-green-700 font-medium">{{ personalInfo.fullAddress }}</p>
                        </div>
                    </div>
                </div>

                <!-- Thông tin tài khoản -->
                <div class="border-bottom pb-4">
                    <h5 class="mb-3 flex items-center gap-2">
                        <i class="pi pi-lock"></i>
                        Thông tin đăng nhập
                    </h5>
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label for="maTaiKhoan" class="mb-3 block font-bold">Mã tài khoản</label>
                            <InputText 
                                id="maTaiKhoan" 
                                v-model="newAccount.maTaiKhoan" 
                                placeholder="Để trống để tự tạo" 
                                fluid 
                            />
                            <small class="text-muted">Nếu để trống, hệ thống sẽ tự động tạo mã</small>
                        </div>
                        <div>
                            <label for="email" class="mb-3 block font-bold">Email đăng nhập *</label>
                            <InputText 
                                id="email" 
                                v-model.trim="newAccount.email" 
                                required="true" 
                                :invalid="hasValidationError('accountEmail')" 
                                :readonly="newAccount.vaiTro !== 'ADMIN'"
                                fluid 
                                placeholder="Email để đăng nhập"
                                @input="clearFieldError('accountEmail')"
                            />
                            <small v-if="hasValidationError('accountEmail')" class="text-red-500">
                                {{ getValidationError('accountEmail') }}
                            </small>
                            <small v-if="newAccount.vaiTro !== 'ADMIN'" class="text-muted">
                                Email tự động lấy từ thông tin cá nhân
                            </small>
                        </div>
                    </div>
                    <div class="mt-4 grid grid-cols-2 gap-4">
                        <div>
                            <label for="matKhau" class="mb-3 block font-bold">Mật khẩu *</label>
                            <Password 
                                id="matKhau" 
                                v-model="newAccount.matKhau" 
                                :required="true" 
                                :invalid="hasValidationError('matKhau')" 
                                toggleMask 
                                fluid
                                placeholder="Nhập mật khẩu"
                                @input="clearFieldError('matKhau')"
                            >
                                <template #header>
                                    <h6>Nhập mật khẩu</h6>
                                </template>
                                <template #footer>
                                    <Divider />
                                    <p class="mt-2">Yêu cầu</p>
                                    <ul class="ml-2 mt-0 pl-2" style="line-height: 1.5">
                                        <li>Tối thiểu 6 ký tự</li>
                                        <li>Tối đa 50 ký tự</li>
                                    </ul>
                                </template>
                            </Password>
                            <small v-if="hasValidationError('matKhau')" class="text-red-500">
                                {{ getValidationError('matKhau') }}
                            </small>
                        </div>
                        <div>
                            <label for="trangThai" class="mb-3 block font-bold">Trạng thái *</label>
                            <Select 
                                id="trangThai" 
                                v-model="newAccount.trangThai" 
                                :options="STATUS_OPTIONS_FOR_FORM" 
                                optionLabel="label" 
                                optionValue="value" 
                                placeholder="Chọn trạng thái" 
                                :invalid="hasValidationError('trangThai')" 
                                fluid 
                            />
                            <small v-if="hasValidationError('trangThai')" class="text-red-500">
                                {{ getValidationError('trangThai') }}
                            </small>
                        </div>
                    </div>
                </div>

                <!-- Thông báo cho Admin -->
                <div v-if="newAccount.vaiTro === 'ADMIN'" class="rounded-lg bg-blue-50 p-4">
                    <div class="flex items-center gap-3">
                        <i class="pi pi-info-circle text-xl text-blue-600"></i>
                        <div>
                            <h6 class="mb-1 text-blue-700">Tài khoản Admin</h6>
                            <p class="mb-0 text-sm text-blue-600">
                                Tài khoản Admin chỉ cần thông tin đăng nhập cơ bản, không cần thông tin cá nhân và địa chỉ.
                            </p>
                        </div>
                    </div>
                </div>
            </div>
            <template #footer>
                <div class="flex justify-between items-center w-full">
                    <div class="flex gap-2">
                        <Button 
                            v-if="Object.keys(validationErrors).length > 0" 
                            label="Làm mới" 
                            icon="pi pi-refresh" 
                            text 
                            severity="secondary"
                            @click="refreshFormAfterError" 
                            :disabled="saving"
                            title="Xóa các lỗi validation để thử lại"
                        />
                    </div>
                    <div class="flex gap-2">
                        <Button label="Hủy" icon="pi pi-times" text @click="hideAddDialog" :disabled="saving" />
                        <Button label="Lưu" icon="pi pi-check" @click="handleSaveAccount" :loading="saving" />
                    </div>
                </div>
            </template>
        </Dialog>

        <!-- Edit Account Dialog - CHỈ EMAIL VÀ MẬT KHẨU -->
        <Dialog v-model:visible="editDialog" :style="{ width: DIALOG_SIZES.MEDIUM }" header="Cập nhật thông tin đăng nhập" :modal="true">
            <div class="flex flex-col gap-4">
                <!-- THÔNG BÁO GIỚI HẠN -->
                <div class="bg-amber-50 p-3 rounded border border-amber-200 mb-4">
                    <div class="flex items-center gap-2 text-amber-700">
                        <i class="pi pi-info-circle"></i>
                        <span class="font-semibold text-sm">Lưu ý:</span>
                    </div>
                    <p class="text-sm text-amber-600 mt-1 mb-0">
                        Chỉ có thể chỉnh sửa Email và Mật khẩu. Vai trò và trạng thái được quản lý bằng các chức năng khác.
                    </p>
                </div>

                <div>
                    <label for="editEmail" class="mb-3 block font-bold">Email đăng nhập *</label>
                    <InputText 
                        id="editEmail" 
                        v-model.trim="editAccountData.email" 
                        required="true" 
                        :invalid="hasValidationError('editEmail')" 
                        fluid 
                        placeholder="Nhập email mới"
                        @input="clearFieldError('editEmail')"
                    />
                    <small v-if="hasValidationError('editEmail')" class="text-red-500">
                        {{ getValidationError('editEmail') }}
                    </small>
                </div>
                
                <div>
                    <label for="editMatKhau" class="mb-3 block font-bold">Mật khẩu mới</label>
                    <Password 
                        id="editMatKhau" 
                        v-model="editAccountData.matKhau" 
                        placeholder="Để trống nếu không đổi" 
                        toggleMask 
                        fluid 
                        @input="clearFieldError('editMatKhau')"
                    />
                    <small v-if="hasValidationError('editMatKhau')" class="text-red-500">
                        {{ getValidationError('editMatKhau') }}
                    </small>
                    <small v-else class="text-muted">Để trống nếu không muốn thay đổi mật khẩu</small>
                </div>

                <!-- HIỂN THỊ THÔNG TIN KHÔNG ĐƯỢC SỬA -->
                <div class="bg-gray-50 p-3 rounded border">
                    <h6 class="mb-2 text-gray-700 font-semibold">Thông tin chỉ đọc:</h6>
                    <div class="grid grid-cols-2 gap-3 text-sm">
                        <div>
                            <strong>Vai trò:</strong>
                            <Tag 
                                :value="getRoleLabel(editAccountData.vaiTro)" 
                                :severity="getRoleSeverity(editAccountData.vaiTro)" 
                                class="ml-2"
                            />
                        </div>
                        <div>
                            <strong>Trạng thái:</strong>
                            <Tag
                                :value="getStatusLabel(editAccountData.trangThai)"
                                :severity="getStatusSeverity(editAccountData.trangThai)"
                                class="ml-2"
                            />
                        </div>
                    </div>
                    <small class="text-gray-500 mt-2 block">
                        Sử dụng nút "Đổi trạng thái" ở bảng chính để thay đổi trạng thái hoạt động.
                    </small>
                </div>
            </div>
            <template #footer>
                <Button label="Hủy" icon="pi pi-times" text @click="hideEditDialog" :disabled="saving" />
                <Button label="Cập nhật" icon="pi pi-check" @click="handleUpdateAccount" :loading="saving" />
            </template>
        </Dialog>

        <!-- View Account Dialog - HIỂN THỊ MẬT KHẨU -->
        <Dialog v-model:visible="viewDialog" :style="{ width: DIALOG_SIZES.LARGE }" :header="`Chi tiết tài khoản - ${viewingAccount?.email || 'N/A'}`" :modal="true">
            <div v-if="viewingAccount" class="flex flex-col gap-6">
                <!-- Thông tin tài khoản -->
                <div class="rounded-lg bg-blue-50 p-4 border border-blue-200">
                    <h6 class="mb-3 font-semibold text-blue-700 flex items-center gap-2">
                        <i class="pi pi-user"></i>
                        Thông tin tài khoản:
                    </h6>
                    <div class="grid grid-cols-2 gap-3 text-sm">
                        <div><strong>ID:</strong> #{{ viewingAccount.id }}</div>
                        <div><strong>Mã TK:</strong> {{ viewingAccount.maTaiKhoan }}</div>
                        <div><strong>Email:</strong> {{ viewingAccount.email }}</div>
                        <div>
                            <strong>Mật khẩu:</strong>
                            <span class="font-mono bg-gray-100 px-2 py-1 rounded text-xs">
                                {{ viewingAccount.matKhau || 'Không có' }}
                            </span>
                        </div>
                        <div>
                            <strong>Vai trò:</strong>
                            <Tag 
                                :value="getRoleLabel(viewingAccount.vaiTro)" 
                                :severity="getRoleSeverity(viewingAccount.vaiTro)" 
                                class="ml-2"
                            />
                        </div>
                        <div>
                            <strong>Trạng thái:</strong>
                            <Tag 
                                :value="getStatusLabel(viewingAccount.trangThai)" 
                                :severity="getStatusSeverity(viewingAccount.trangThai)" 
                                class="ml-2"
                            />
                        </div>
                        <div><strong>Ngày tạo:</strong> {{ formatDate(viewingAccount.ngayTao) }}</div>
                        <div><strong>Cập nhật:</strong> {{ formatDate(viewingAccount.ngayCapNhat) }}</div>
                    </div>
                </div>

                <!-- Thông tin liên kết -->
                <div class="rounded-lg bg-green-50 p-4 border border-green-200">
                    <h6 class="mb-3 font-semibold text-green-700 flex items-center gap-2">
                        <i class="pi pi-link"></i>
                        Thông tin liên kết:
                    </h6>
                    <div class="text-sm">
                        <div v-if="viewingAccount.vaiTro === 'USER'">
                            <strong>Khách hàng:</strong> {{ getLinkedCustomerInfo(viewingAccount.id) }}
                        </div>
                        <div v-else-if="viewingAccount.vaiTro === 'NHANVIEN'">
                            <strong>Nhân viên:</strong> {{ getLinkedEmployeeInfo(viewingAccount.id) }}
                        </div>
                        <div v-else>
                            <strong>Vai trò:</strong> Admin hệ thống (không liên kết)
                        </div>
                    </div>
                </div>

                <!-- Thông báo bảo mật -->
                <div class="rounded-lg bg-amber-50 p-4 border border-amber-200">
                    <div class="flex items-center gap-2 text-amber-700">
                        <i class="pi pi-exclamation-triangle"></i>
                        <span class="font-semibold">Lưu ý bảo mật:</span>
                    </div>
                    <p class="text-sm text-amber-600 mt-1 mb-0">
                        Mật khẩu hiển thị ở dạng đã mã hóa (hash). Để thay đổi mật khẩu, sử dụng chức năng "Sửa" và nhập mật khẩu mới.
                    </p>
                    <p class="text-xs text-amber-500 mt-2 mb-0">
                        ⚠️ Chỉ Admin mới có thể xem thông tin này. Hãy cẩn thận khi chia sẻ thông tin tài khoản.
                    </p>
                </div>
            </div>
            <template #footer>
                <Button label="Đóng" icon="pi pi-times" text @click="viewDialog = false" />
                <Button label="Sửa thông tin đăng nhập" icon="pi pi-pencil" @click="editFromView" />
            </template>
        </Dialog>

        <!-- Delete Dialogs -->
        <Dialog v-model:visible="deleteAccountDialog" :style="{ width: DIALOG_SIZES.SMALL }" header="Xác nhận xóa hoàn toàn" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl text-red-500" />
                <span v-if="selectedAccountForDelete">
                    Bạn có chắc chắn muốn XÓA HOÀN TOÀN tài khoản <b>{{ selectedAccountForDelete.email }}</b>?
                    <br><small class="text-red-600 font-semibold">⚠️ CẢNH BÁO: Tất cả dữ liệu liên quan sẽ bị XÓA HOÀN TOÀN!</small>
                    <br><small class="text-red-600 font-semibold">❌ Hành động này KHÔNG THỂ HOÀN TÁC!</small>
                </span>
            </div>
            <template #footer>
                <Button label="Hủy" icon="pi pi-times" text @click="deleteAccountDialog = false" :disabled="deleting" />
                <Button label="Xóa hoàn toàn" icon="pi pi-check" severity="danger" @click="handleDeleteAccount" :loading="deleting" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteAccountsDialog" :style="{ width: DIALOG_SIZES.SMALL }" header="Xác nhận xóa hoàn toàn" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl text-red-500" />
                <span>
                    Bạn có chắc chắn muốn XÓA HOÀN TOÀN {{ selectedAccounts?.length }} tài khoản đã chọn?
                    <br><small class="text-red-600 font-semibold">⚠️ CẢNH BÁO: Tất cả dữ liệu liên quan (khách hàng/nhân viên) cũng sẽ bị XÓA HOÀN TOÀN!</small>
                    <br><small class="text-red-600 font-semibold">❌ Hành động này KHÔNG THỂ HOÀN TÁC!</small>
                </span>
            </div>
            <template #footer>
                <Button label="Hủy" icon="pi pi-times" text @click="deleteAccountsDialog = false" :disabled="deleting" />
                <Button label="Xóa tất cả" icon="pi pi-check" severity="danger" @click="handleDeleteSelectedAccounts" :loading="deleting" />
            </template>
        </Dialog>

        <Toast />
    </div>
</template>

<script setup>
import axios from 'axios'
import { useConfirm } from 'primevue/useconfirm'
import { useToast } from 'primevue/usetoast'
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

// Constants
const STATUS_OPTIONS = [
    { label: 'Tất cả trạng thái', value: null },
    { label: 'Hoạt động', value: 1 },
    { label: 'Ngưng hoạt động', value: 0 }
]

const STATUS_OPTIONS_FOR_FORM = [
    { label: 'Hoạt động', value: 1 },
    { label: 'Ngưng hoạt động', value: 0 }
]

const ROLE_FILTER_OPTIONS = [
    { label: 'Tất cả vai trò', value: null },
    { label: 'Khách hàng', value: 'USER' },
    { label: 'Nhân viên', value: 'NHANVIEN' },
    { label: 'Admin', value: 'ADMIN' }
]

const ROLE_OPTIONS_FOR_FORM = [
    { label: 'Khách hàng', value: 'USER' },
    { label: 'Nhân viên', value: 'NHANVIEN' },
    { label: 'Admin', value: 'ADMIN' }
]

const PAGINATION_CONFIG = {
    defaultRows: 10,
    rowsPerPageOptions: [5, 10, 25, 50],
    paginatorTemplate: "FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
}

const DIALOG_SIZES = {
    SMALL: '450px',
    MEDIUM: '600px',
    LARGE: '800px',
    EXTRA_LARGE: '1000px'
}

// Composables
const router = useRouter()
const toast = useToast()
const confirm = useConfirm()

// Reactive State
const dt = ref()
const accounts = ref([])
const customers = ref([])
const employees = ref([])
const selectedAccounts = ref()
const isLoading = ref(false)
const addDialog = ref(false)
const editDialog = ref(false)
const viewDialog = ref(false)
const deleteAccountDialog = ref(false)
const deleteAccountsDialog = ref(false)

const hoTenInput = ref(null)

const newAccount = ref({})
const personalInfo = ref({
    hoTen: '',
    email: '',
    sdt: '',
    ngaySinh: null,
    maTinh: '',
    maPhuong: '',
    diaChiChiTiet: '',
    fullAddress: ''
})
const editAccountData = ref({})
const viewingAccount = ref(null)
const selectedAccountForDelete = ref(null)

const globalFilter = ref('')
const roleFilter = ref(null)
const statusFilter = ref(null)
const dateFilters = ref({
    startDate: null,
    endDate: null
})

const submitted = ref(false)
const saving = ref(false)
const deleting = ref(false)
const exporting = ref(false)
const validationErrors = ref({})

// Address data
const provinces = ref([])
const wards = ref([])
const loadingProvinces = ref(false)
const loadingWards = ref(false)

const lastCreatedAccountRole = ref('')

// User authentication
const isAdmin = ref(false)

// ===== UTILITY FUNCTIONS =====
const formatDate = (date) => {
    if (!date) return ''
    
    try {
        const dateObj = new Date(date)
        if (isNaN(dateObj.getTime())) {
            console.warn('Invalid date:', date)
            return 'Ngày không hợp lệ'
        }
        
        return dateObj.toLocaleDateString('vi-VN', {
            day: '2-digit',
            month: '2-digit', 
            year: 'numeric'
        })
    } catch (error) {
        console.error('Error formatting date:', error)
        return 'Lỗi định dạng ngày'
    }
}

watch(addDialog, async (val) => {
    if (val) {
        await nextTick()
        
        setTimeout(() => {
            try {
                if (document.activeElement && document.activeElement !== document.body) {
                    document.activeElement.blur()
                }
                
                if (hoTenInput.value && hoTenInput.value.$el) {
                    hoTenInput.value.$el.focus()
                    console.log('Auto-focused họ tên input')
                }
            } catch (error) {
                console.warn('Could not auto-focus input:', error)
            }
        }, 150)
    }
})

const getStatusLabel = (status) => {
    return status === 1 ? 'Hoạt động' : 'Ngưng hoạt động'
}

const getStatusSeverity = (status) => {
    return status === 1 ? 'success' : 'danger'
}

const getStatusIcon = (status) => {
    return status === 1 ? 'pi pi-check-circle' : 'pi pi-times-circle'
}

const getRoleLabel = (vaiTro) => {
    switch (vaiTro) {
        case 'USER': return 'Khách hàng'
        case 'NHANVIEN': return 'Nhân viên'
        case 'ADMIN': return 'Admin'
        default: return 'Không xác định'
    }
}

const getRoleSeverity = (vaiTro) => {
    switch (vaiTro) {
        case 'USER': return 'primary'
        case 'NHANVIEN': return 'success'
        case 'ADMIN': return 'warn'
        default: return 'secondary'
    }
}

const getRoleIcon = (vaiTro) => {
    switch (vaiTro) {
        case 'USER': return 'pi pi-user'
        case 'NHANVIEN': return 'pi pi-user-edit'
        case 'ADMIN': return 'pi pi-crown'
        default: return 'pi pi-question'
    }
}

const getActiveAccountsCount = () => {
    return accounts.value.filter(acc => acc.trangThai === 1).length
}

const getInactiveAccountsCount = () => {
    return accounts.value.filter(acc => acc.trangThai === 0).length
}

const getLinkedCustomerInfo = (accountId) => {
    const customer = customers.value.find(c => c.idTaiKhoan === accountId)
    return customer ? `${customer.hoTen} (ID: ${customer.id})` : 'Chưa liên kết'
}

const getLinkedEmployeeInfo = (accountId) => {
    const employee = employees.value.find(e => e.idTaiKhoan === accountId)
    return employee ? `${employee.hoTen} (ID: ${employee.id})` : 'Chưa liên kết'
}

// ===== COMPUTED =====
const filteredAccounts = computed(() => {
    let filtered = accounts.value || []

    if (globalFilter.value && globalFilter.value.trim()) {
        const searchTerm = globalFilter.value.toLowerCase().trim()
        filtered = filtered.filter(acc => 
            (acc.email && acc.email.toLowerCase().includes(searchTerm)) ||
            (acc.maTaiKhoan && acc.maTaiKhoan.toLowerCase().includes(searchTerm)) ||
            getRoleLabel(acc.vaiTro).toLowerCase().includes(searchTerm) ||
            getStatusLabel(acc.trangThai).toLowerCase().includes(searchTerm)
        )
    }

    if (roleFilter.value !== null && roleFilter.value !== undefined && roleFilter.value !== '') {
        filtered = filtered.filter(acc => acc.vaiTro === roleFilter.value)
    }

    if (statusFilter.value !== null && statusFilter.value !== undefined && statusFilter.value !== '') {
        const statusValue = parseInt(statusFilter.value)
        if (!isNaN(statusValue)) {
            filtered = filtered.filter(acc => acc.trangThai === statusValue)
        }
    }

    if (dateFilters.value.startDate || dateFilters.value.endDate) {
        filtered = filtered.filter(acc => {
            if (!acc.ngayTao) return false
            
            const accDate = new Date(acc.ngayTao)
            if (isNaN(accDate.getTime())) return false
            
            const accDateOnly = new Date(accDate.getFullYear(), accDate.getMonth(), accDate.getDate())
            
            if (dateFilters.value.startDate) {
                const startDateOnly = new Date(
                    dateFilters.value.startDate.getFullYear(), 
                    dateFilters.value.startDate.getMonth(), 
                    dateFilters.value.startDate.getDate()
                )
                if (accDateOnly < startDateOnly) return false
            }
            
            if (dateFilters.value.endDate) {
                const endDateOnly = new Date(
                    dateFilters.value.endDate.getFullYear(), 
                    dateFilters.value.endDate.getMonth(), 
                    dateFilters.value.endDate.getDate()
                )
                if (accDateOnly > endDateOnly) return false
            }
            
            return true
        })
    }

    return filtered
})

// Computed property để theo dõi quyền ADMIN
const adminPermission = computed(() => {
    return isAdmin.value
})

const hasValidationError = (field) => {
    return Boolean(validationErrors.value[field])
}

const getValidationError = (field) => {
    return validationErrors.value[field] || ''
}

// ===== DATA MANAGEMENT =====
const fetchData = async () => {
    isLoading.value = true
    try {
        const [accountsResponse, customersResponse, employeesResponse] = await Promise.all([
            axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/tai-khoan?size=1000`),
            axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/khach-hang/all`).catch(() => ({ data: [] })),
            axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/nhan-vien/all`).catch(() => ({ data: [] }))
        ])
        
        if (Array.isArray(accountsResponse.data)) {
            accounts.value = accountsResponse.data
        } else if (accountsResponse.data && Array.isArray(accountsResponse.data.data)) {
            accounts.value = accountsResponse.data.data
        } else {
            accounts.value = []
        }

        if (customersResponse.data) {
            if (Array.isArray(customersResponse.data)) {
                customers.value = customersResponse.data
            } else if (customersResponse.data.content) {
                customers.value = customersResponse.data.content
            } else {
                customers.value = []
            }
        } else {
            customers.value = []
        }

        if (employeesResponse.data) {
            if (Array.isArray(employeesResponse.data)) {
                employees.value = employeesResponse.data
            } else if (employeesResponse.data.content) {
                employees.value = employeesResponse.data.content
            } else {
                employees.value = []
            }
        } else {
            employees.value = []
        }
        
    } catch (error) {
        console.error('Error fetching data:', error)
        toast.add({
            severity: 'error',
            summary: 'Lỗi kết nối',
            detail: `Không thể tải dữ liệu: ${error.message}`,
            life: 5000
        })
        accounts.value = []
        customers.value = []
        employees.value = []
    } finally {
        isLoading.value = false
    }
}

// ===== DELETE FUNCTIONS =====
const hardDeleteRelatedEntity = async (accountId, role) => {
    try {
        console.log('🔄 hardDeleteRelatedEntity called with:', { accountId, role })
        
        if (role === 'USER') {
            const customer = customers.value.find(c => c.idTaiKhoan === accountId)
            console.log('🔍 Found customer:', customer)
            
            if (customer) {
                console.log('🔄 Deleting customer:', customer.id)
                await axios.delete(`http://localhost:8080/api/khach-hang/${customer.id}`, {
                    headers: { 'Content-Type': 'application/json' },
                    timeout: 10000
                })
                console.log('✅ Customer deleted successfully:', customer.id)
            } else {
                console.log('ℹ️ No customer found for account:', accountId)
            }
        } else if (role === 'NHANVIEN') {
            const employee = employees.value.find(e => e.idTaiKhoan === accountId)
            console.log('🔍 Found employee:', employee)
            
            if (employee) {
                console.log('🔄 Deleting employee:', employee.id)
                await axios.delete(`http://localhost:8080/api/nhan-vien/${employee.id}`, {
                    headers: { 'Content-Type': 'application/json' },
                    timeout: 10000
                })
                console.log('✅ Employee deleted successfully:', employee.id)
            } else {
                console.log('ℹ️ No employee found for account:', accountId)
            }
        } else {
            console.log('ℹ️ No related entity to delete for role:', role)
        }
        
        console.log('✅ hardDeleteRelatedEntity completed successfully')
        
    } catch (error) {
        console.error('❌ Error in hardDeleteRelatedEntity:', error)
        console.error('❌ Error details:', {
            message: error.message,
            response: error.response?.data,
            status: error.response?.status
        })
        throw new Error(`Không thể xóa ${role === 'USER' ? 'khách hàng' : 'nhân viên'} liên quan: ${error.response?.data?.message || error.message}`)
    }
}

const softDeleteRelatedEntity = async (accountId, role) => {
    try {
        if (role === 'USER') {
            const customer = customers.value.find(c => c.idTaiKhoan === accountId)
            if (customer) {
                await axios.patch(`http://localhost:8080/api/khach-hang/${customer.id}/soft-delete`, {
                    trangThai: 0,
                    isDeleted: true
                }, {
                    headers: { 'Content-Type': 'application/json' },
                    timeout: 10000
                })
                console.log('Đã xóa mềm khách hàng:', customer.id)
            }
        } else if (role === 'NHANVIEN') {
            const employee = employees.value.find(e => e.idTaiKhoan === accountId)
            if (employee) {
                await axios.patch(`http://localhost:8080/api/nhan-vien/${employee.id}/soft-delete`, {
                    trangThai: 0,
                    isDeleted: true
                }, {
                    headers: { 'Content-Type': 'application/json' },
                    timeout: 10000
                })
                console.log('Đã xóa mềm nhân viên:', employee.id)
            }
        }
    } catch (error) {
        console.warn('Không thể xóa mềm entity liên quan:', error.response?.data?.message || error.message)
        
        // Fallback: Sử dụng API status cũ
        try {
            if (role === 'USER') {
                const customer = customers.value.find(c => c.idTaiKhoan === accountId)
                if (customer) {
                    await axios.patch(`http://localhost:8080/api/khach-hang/${customer.id}/status`, {
                        trangThai: 0
                    }, {
                        headers: { 'Content-Type': 'application/json' },
                        timeout: 10000
                    })
                    console.log('Fallback: Đã vô hiệu hóa khách hàng')
                }
            } else if (role === 'NHANVIEN') {
                const employee = employees.value.find(e => e.idTaiKhoan === accountId)
                if (employee) {
                    await axios.patch(`http://localhost:8080/api/nhan-vien/${employee.id}/status`, {
                        trangThai: 0
                    }, {
                        headers: { 'Content-Type': 'application/json' },
                        timeout: 10000
                    })
                    console.log('Fallback: Đã cho nhân viên nghỉ việc')
                }
            }
        } catch (fallbackError) {
            console.error('Fallback cũng thất bại:', fallbackError)
        }
    }
}

const handleDeleteAccount = async () => {
    deleting.value = true
    try {
        console.log('🗑️ handleDeleteAccount called')
        console.log('📋 Account to delete:', selectedAccountForDelete.value)
        
        if (!selectedAccountForDelete.value) {
            console.error('❌ No account selected for deletion')
            toast.add({
                severity: 'error',
                summary: 'Lỗi',
                detail: 'Không có tài khoản nào được chọn để xóa',
                life: 3000
            })
            return
        }
        
        const accountToDelete = selectedAccountForDelete.value
        
        console.log('🔄 Starting deletion process for account:', accountToDelete.email)
        
        // Xóa hoàn toàn entity liên quan trước
        console.log('🔄 Deleting related entity...')
        await hardDeleteRelatedEntity(accountToDelete.id, accountToDelete.vaiTro)
        console.log('✅ Related entity deleted successfully')
        
        // Xóa hoàn toàn tài khoản
        console.log('🔄 Deleting account...')
        const response = await axios.delete(
            `http://localhost:8080/api/tai-khoan/${accountToDelete.id}`,
            {
                timeout: 30000,
                headers: {
                    'Content-Type': 'application/json'
                }
            }
        )
        
        console.log('📡 Delete response:', response.status, response.data)
        
        if (response.status === 200 || response.status === 204) {
            const entityName = accountToDelete.vaiTro === 'USER' ? 'khách hàng' : 
                             accountToDelete.vaiTro === 'NHANVIEN' ? 'nhân viên' : 'admin'
            
            console.log('✅ Account deleted successfully')
            
            toast.add({
                severity: 'success',
                summary: 'Xóa thành công',
                detail: `Đã xóa hoàn toàn tài khoản và ${entityName} liên quan khỏi hệ thống`,
                life: 4000
            })
            
            deleteAccountDialog.value = false
            selectedAccountForDelete.value = null
            await fetchData()
        }
        
    } catch (error) {
        console.error('❌ Error in handleDeleteAccount:', error)
        console.error('❌ Error details:', {
            message: error.message,
            response: error.response?.data,
            status: error.response?.status
        })
        handleDeleteError(error)
    } finally {
        deleting.value = false
    }
}


const handleDeleteError = (error) => {
    if (error.response) {
        const { status, data } = error.response
        let errorMessage = 'Không thể xóa tài khoản'
        let errorDetail = ''
        
        switch (status) {
            case 409:
                errorMessage = 'Xung đột dữ liệu'
                errorDetail = 'Tài khoản có dữ liệu liên quan không thể xóa.'
                break
            case 500:
                errorMessage = 'Lỗi hệ thống'
                errorDetail = data?.message || 'Có lỗi ràng buộc dữ liệu.'
                break
            case 400:
                errorMessage = 'Yêu cầu không hợp lệ'
                errorDetail = data?.message || 'Tài khoản không thể xóa do vi phạm quy tắc nghiệp vụ.'
                break
            default:
                errorDetail = data?.message || error.message || 'Lỗi không xác định'
        }
        
        toast.add({
            severity: 'error',
            summary: errorMessage,
            detail: errorDetail,
            life: 5000
        })
        
    } else {
        handleApiError(error, 'Không thể xóa tài khoản')
    }
}

const handleHardDeleteSafely = async (accountToDelete) => {
    try {
        // Hủy liên kết entity trước khi xóa cứng
        if (accountToDelete.vaiTro === 'USER') {
            const relatedCustomer = customers.value.find(c => c.idTaiKhoan === accountToDelete.id)
            if (relatedCustomer) {
                try {
                    await axios.put(`http://localhost:8080/api/khach-hang/${relatedCustomer.id}`, {
                        ...relatedCustomer,
                        idTaiKhoan: null
                    }, {
                        headers: { 'Content-Type': 'application/json' }
                    })
                    console.log('Đã hủy liên kết khách hàng')
                } catch (unlinkError) {
                    console.warn('Không thể hủy liên kết khách hàng:', unlinkError)
                    throw new Error('Không thể hủy liên kết dữ liệu. Hãy thử lại sau.')
                }
            }
        } else if (accountToDelete.vaiTro === 'NHANVIEN') {
            const relatedEmployee = employees.value.find(e => e.idTaiKhoan === accountToDelete.id)
            if (relatedEmployee) {
                try {
                    await axios.put(`http://localhost:8080/api/nhan-vien/${relatedEmployee.id}`, {
                        ...relatedEmployee,
                        idTaiKhoan: null
                    }, {
                        headers: { 'Content-Type': 'application/json' }
                    })
                    console.log('Đã hủy liên kết nhân viên')
                } catch (unlinkError) {
                    console.warn('Không thể hủy liên kết nhân viên:', unlinkError)
                    throw new Error('Không thể hủy liên kết dữ liệu. Hãy thử lại sau.')
                }
            }
        }
        
        // Xóa cứng tài khoản
        const response = await axios.delete(
            `http://localhost:8080/api/tai-khoan/${accountToDelete.id}`,
            {
                timeout: 30000,
                headers: {
                    'Content-Type': 'application/json'
                }
            }
        )
        
        if (response.status === 200 || response.status === 204) {
            const entityName = accountToDelete.vaiTro === 'USER' ? 'khách hàng' : 
                             accountToDelete.vaiTro === 'NHANVIEN' ? 'nhân viên' : 'admin'
            
            toast.add({
                severity: 'success',
                summary: 'Xóa thành công',
                detail: `Đã xóa hoàn toàn tài khoản và ${entityName} liên quan`,
                life: 3000
            })
            
            deleteAccountDialog.value = false
            selectedAccountForDelete.value = null
            await fetchData()
        }
    } catch (error) {
        throw error
    }
}


const confirmDeleteAccount = (account) => {
    try {
        console.log('🗑️ confirmDeleteAccount called with:', account)
        
        if (!account || !account.id) {
            console.error('❌ Invalid account data:', account)
            toast.add({
                severity: 'error',
                summary: 'Lỗi',
                detail: 'Dữ liệu tài khoản không hợp lệ',
                life: 3000
            })
            return
        }
        
    selectedAccountForDelete.value = account
    
        let warningMessage = `Bạn có chắc chắn muốn XÓA HOÀN TOÀN tài khoản "${account.email}"?`
        let hasRelatedData = false
        let relatedEntityName = ''
        
        if (account.vaiTro === 'USER') {
            const relatedCustomer = customers.value.find(c => c.idTaiKhoan === account.id)
            if (relatedCustomer) {
                hasRelatedData = true
                relatedEntityName = `khách hàng "${relatedCustomer.hoTen}"`
                warningMessage += `\n\n⚠️ CẢNH BÁO: Tài khoản này liên kết với ${relatedEntityName}.`
            }
        } else if (account.vaiTro === 'NHANVIEN') {
            const relatedEmployee = employees.value.find(e => e.idTaiKhoan === account.id)
            if (relatedEmployee) {
                hasRelatedData = true
                relatedEntityName = `nhân viên "${relatedEmployee.hoTen}"`
                warningMessage += `\n\n⚠️ CẢNH BÁO: Tài khoản này liên kết với ${relatedEntityName}.`
            }
        }
        
        if (hasRelatedData) {
            warningMessage += `\n\n🗑️ Dữ liệu liên quan (${relatedEntityName}) cũng sẽ bị XÓA HOÀN TOÀN khỏi hệ thống.`
            warningMessage += `\n\n❌ Hành động này KHÔNG THỂ HOÀN TÁC!`
        } else {
            warningMessage += `\n\n❌ Hành động này sẽ XÓA HOÀN TOÀN tài khoản khỏi hệ thống!`
        }
        
        console.log('🔍 About to show confirm dialog with message:', warningMessage)
        
        if (!confirm || typeof confirm.require !== 'function') {
            console.error('❌ Confirm service not available:', confirm)
            console.log('🔄 Falling back to manual dialog...')
            
            // Fallback: Sử dụng dialog thông thường
            deleteAccountDialog.value = true
            return
        }
        
        confirm.require({
            message: warningMessage,
            header: 'Xác nhận xóa hoàn toàn',
            icon: 'pi pi-exclamation-triangle',
            rejectClass: 'p-button-secondary p-button-outlined',
            rejectLabel: 'Hủy',
            acceptLabel: hasRelatedData ? 'Xóa tất cả' : 'Xóa tài khoản',
            acceptClass: 'p-button-danger',
            accept: () => {
                console.log('✅ User confirmed deletion')
                handleDeleteAccount()
            },
            reject: () => {
                console.log('❌ User cancelled deletion')
            }
        })
        
        console.log('✅ Confirm dialog should be shown')
        
    } catch (error) {
        console.error('❌ Error in confirmDeleteAccount:', error)
        toast.add({
            severity: 'error',
            summary: 'Lỗi hệ thống',
            detail: `Không thể hiển thị dialog xác nhận: ${error.message}`,
            life: 5000
        })
    }
}

const handleDeleteSelectedAccounts = async () => {
    deleting.value = true
    
    try {
        
        const totalAccounts = selectedAccounts.value.length
        let successCount = 0
        let failedAccounts = []
        
        for (const account of selectedAccounts.value) {
            try {
                // Xóa hoàn toàn entity liên quan trước
                await hardDeleteRelatedEntity(account.id, account.vaiTro)
                
                // Xóa hoàn toàn tài khoản
                await axios.delete(`http://localhost:8080/api/tai-khoan/${account.id}`, {
                    headers: { 'Content-Type': 'application/json' },
                    timeout: 10000
                })
                successCount++
            } catch (error) {
                failedAccounts.push({
                    account: account,
                    error: error.response?.data?.message || error.message
                })
            }
        }
        
        if (successCount > 0) {
            toast.add({
                severity: successCount === totalAccounts ? 'success' : 'warn',
                summary: 'Hoàn thành',
                detail: `Đã xóa hoàn toàn ${successCount}/${totalAccounts} tài khoản và dữ liệu liên quan. ${failedAccounts.length > 0 ? failedAccounts.length + ' tài khoản không thể xóa.' : ''}`,
                life: 5000
            })
        } else {
            toast.add({
                severity: 'error',
                summary: 'Thất bại',
                detail: 'Không thể xóa tài khoản nào.',
                life: 5000
            })
        }
        
        deleteAccountsDialog.value = false
        selectedAccounts.value = null
        await fetchData()
        
    } catch (error) {
        console.error('Lỗi xóa nhiều tài khoản:', error)
        toast.add({
            severity: 'error',
            summary: 'Lỗi hệ thống',
            detail: 'Có lỗi xảy ra khi xử lý hàng loạt',
            life: 5000
        })
    } finally {
        deleting.value = false
    }
}

const confirmDeleteSelected = () => {
    // Kiểm tra quyền ADMIN
    if (!isAdmin.value) {
        toast.add({
            severity: 'warn',
            summary: 'Không có quyền',
            detail: 'Chỉ tài khoản ADMIN mới có thể xóa tài khoản',
            life: 3000
        })
        return
    }
    
    deleteAccountsDialog.value = true
}

// ===== STATUS CHANGE FUNCTION =====
const handleChangeStatus = async (account) => {
    try {
        const newStatus = account.trangThai === 1 ? 0 : 1
        const statusText = newStatus === 1 ? 'kích hoạt' : 'ngưng hoạt động'
        
        console.log(`🔄 Changing status for account ${account.email} to ${statusText}`)
        
        const response = await axios.patch(
            `http://localhost:8080/api/tai-khoan/${account.id}/trang-thai`,
            { trangThai: newStatus },
            {
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                timeout: 10000
            }
        )
        
        if (response.status === 200) {
            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: `Đã ${statusText} tài khoản ${account.email}`,
                life: 3000
            })
            
            await fetchData()
        }
        
    } catch (error) {
        console.error('❌ Error changing status:', error)
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: `Không thể thay đổi trạng thái tài khoản: ${error.response?.data?.message || error.message}`,
            life: 5000
        })
    }
}

// ===== UTILITY FUNCTIONS =====

// ===== ADDRESS MANAGEMENT =====
const fetchProvinces = async () => {
    if (provinces.value.length > 0) return
    
    try {
        loadingProvinces.value = true
        const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/vietnam-address/provinces`)
        
        if (response.data && response.data.success && response.data.data) {
            provinces.value = response.data.data.map(item => ({
                code: item.code.toString(),
                name: item.name,
                codename: item.codename
            }))
        } else {
            provinces.value = [
                { code: '1', name: 'Hà Nội', codename: 'ha_noi' },
                { code: '79', name: 'TP. Hồ Chí Minh', codename: 'ho_chi_minh' },
                { code: '48', name: 'Đà Nẵng', codename: 'da_nang' }
            ]
        }
    } catch (error) {
        console.error('Error fetching provinces:', error)
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
    
    try {
        loadingWards.value = true
        const response = await axios.get(`http://localhost:8080/api/vietnam-address/wards/${provinceCode}`)
        
        if (response.data && response.data.success && response.data.data) {
            wards.value = response.data.data.map(item => ({
                code: item.code.toString(),
                name: item.name,
                codename: item.codename
            }))
        } else {
            wards.value = [
                { code: '1', name: 'Phường/Xã 1', codename: 'phuong_xa_1' },
                { code: '2', name: 'Phường/Xã 2', codename: 'phuong_xa_2' }
            ]
        }
    } catch (error) {
        wards.value = [
            { code: '1', name: 'Phường/Xã 1', codename: 'phuong_xa_1' },
            { code: '2', name: 'Phường/Xã 2', codename: 'phuong_xa_2' }
        ]
    } finally {
        loadingWards.value = false
    }
}

const onProvinceChange = async () => {
    personalInfo.value.maPhuong = ''
    wards.value = []
    
    if (personalInfo.value.maTinh) {
        await fetchWards(personalInfo.value.maTinh)
    }
    updateFullAddress()
}

const onWardChange = () => {
    updateFullAddress()
}

const updateFullAddress = () => {
    const provinceName = provinces.value.find(p => p.code === personalInfo.value.maTinh)?.name || ''
    const wardName = wards.value.find(w => w.code === personalInfo.value.maPhuong)?.name || ''
    
    const addressParts = [
        personalInfo.value.diaChiChiTiet,
        wardName,
        provinceName
    ].filter(part => part && part.trim() !== '')
    
    personalInfo.value.fullAddress = addressParts.join(', ')
}

// ===== SEARCH AND FILTER =====
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

const debouncedSearch = debounce(() => {
    // Search is handled by computed filteredAccounts
}, 300)

const applyFilters = () => {
    if (dateFilters.value.startDate && dateFilters.value.endDate) {
        if (dateFilters.value.startDate > dateFilters.value.endDate) {
            toast.add({
                severity: 'warn',
                summary: 'Lỗi ngày tháng',
                detail: 'Ngày bắt đầu không thể lớn hơn ngày kết thúc',
                life: 3000
            })
            const temp = dateFilters.value.startDate
            dateFilters.value.startDate = dateFilters.value.endDate
            dateFilters.value.endDate = temp
        }
    }
}

const resetFilters = () => {
    globalFilter.value = ''
    roleFilter.value = null
    statusFilter.value = null
    dateFilters.value = {
        startDate: null,
        endDate: null
    }
}

const clearSearch = () => {
    globalFilter.value = ''
}

// ===== DIALOG MANAGEMENT =====
const openNew = () => {
    resetForms()
    addDialog.value = true
    fetchProvinces()
}

const resetForms = () => {
    newAccount.value = {
        maTaiKhoan: '',
        email: '',
        matKhau: '',
        vaiTro: undefined,
        trangThai: 1
    }
    
    personalInfo.value = {
        hoTen: '',
        email: '',
        sdt: '',
        ngaySinh: null,
        maTinh: '',
        maPhuong: '',
        diaChiChiTiet: '',
        fullAddress: ''
    }
    
    wards.value = []
    submitted.value = false
    validationErrors.value = {}
}

const refreshFormAfterError = () => {
    validationErrors.value = {}
    submitted.value = false
}

const onRoleChange = () => {
    personalInfo.value = {
        hoTen: '',
        email: '',
        sdt: '',
        ngaySinh: null,
        maTinh: '',
        maPhuong: '',
        diaChiChiTiet: '',
        fullAddress: ''
    }
    
    wards.value = []
    validationErrors.value = {}
    
    if (newAccount.value.vaiTro === 'ADMIN') {
        newAccount.value.email = ''
    } else {
        if (personalInfo.value.email?.trim()) {
            newAccount.value.email = personalInfo.value.email.trim()
        }
    }
}

const syncEmailToAccount = () => {
    const personalEmail = personalInfo.value.email?.trim() || ''
    
    if (newAccount.value.vaiTro !== 'ADMIN' && personalEmail) {
        newAccount.value.email = personalEmail
    }
    
    if (validationErrors.value.email) {
        delete validationErrors.value.email
    }
    if (validationErrors.value.accountEmail) {
        delete validationErrors.value.accountEmail
    }
    
    if (personalEmail && newAccount.value.vaiTro !== 'ADMIN') {
        nextTick(() => {
            const emailRegex = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/
            if (!emailRegex.test(personalEmail)) {
                validationErrors.value.email = 'Email không hợp lệ'
            }
        })
    }
}

const clearFieldError = (fieldName) => {
    if (validationErrors.value[fieldName]) {
        delete validationErrors.value[fieldName]
    }
}

const hideAddDialog = () => {
    addDialog.value = false
    resetForms()
}

const hideEditDialog = () => {
    editDialog.value = false
    submitted.value = false
    validationErrors.value = {}
}

const viewAccount = (account) => {
    viewingAccount.value = { ...account }
    viewDialog.value = true
}

const editAccount = (account) => {
    editAccountData.value = {
        id: account.id,
        email: account.email,
        matKhau: '',
        vaiTro: account.vaiTro,
        trangThai: account.trangThai
    }
    submitted.value = false
    validationErrors.value = {}
    editDialog.value = true
}

const editFromView = () => {
    editAccount(viewingAccount.value)
    viewDialog.value = false
}

// ===== VALIDATION =====
const checkEmailExists = (email, excludeId = null) => {
    return accounts.value.some(account => 
        account.email.toLowerCase() === email.toLowerCase() && account.id !== excludeId
    )
}

const checkPhoneExists = (phone) => {
    const customerPhones = customers.value.map(c => c.sdt?.replace(/\s/g, '') || '')
    const employeePhones = employees.value.map(e => e.sdt?.replace(/\s/g, '') || '')
    const allPhones = [...customerPhones, ...employeePhones]
    
    return allPhones.includes(phone)
}

const normalizeName = (name) => {
    if (!name || typeof name !== 'string') return '';
    
    let normalized = name.normalize('NFC').trim();
    normalized = normalized.replace(/[^\p{L}\s]/gu, '');
    normalized = normalized.replace(/\s+/g, ' ').trim();
    
    if (normalized.length < 2) {
        return normalized;
    }
    
    const words = normalized.split(/\s+/).filter(word => word.length > 0);
    if (words.length < 2) {
        console.warn('Họ tên phải có ít nhất 2 từ:', normalized);
    }
    
    return normalized;
}

// Debounce validation để tránh validate quá nhiều lần
let validationTimeout = null
const debouncedValidate = (callback) => {
    if (validationTimeout) {
        clearTimeout(validationTimeout)
    }
    validationTimeout = setTimeout(callback, 100)
}

const validateForm = () => {
    validationErrors.value = {}
    
    // Kiểm tra vai trò trước (đơn giản nhất)
    if (!newAccount.value.vaiTro) {
        validationErrors.value.vaiTro = 'Vui lòng chọn vai trò'
        return false
    }
    
    // Kiểm tra trạng thái (đơn giản)
    if (newAccount.value.trangThai === undefined || newAccount.value.trangThai === null) {
        validationErrors.value.trangThai = 'Vui lòng chọn trạng thái'
        return false
    }
    
    // Kiểm tra mật khẩu (đơn giản)
    const password = newAccount.value.matKhau?.trim() || ''
    if (!password) {
        validationErrors.value.matKhau = 'Mật khẩu không được để trống'
        return false
    } else if (password.length < 6) {
        validationErrors.value.matKhau = 'Mật khẩu phải có ít nhất 6 ký tự'
        return false
    } else if (password.length > 50) {
        validationErrors.value.matKhau = 'Mật khẩu không được quá 50 ký tự'
        return false
    }
    
    // Xử lý email
    let accountEmail = newAccount.value.email?.trim() || ''
    
    if (newAccount.value.vaiTro !== 'ADMIN' && personalInfo.value.email?.trim()) {
        accountEmail = personalInfo.value.email.trim()
        newAccount.value.email = accountEmail
    }
    
    if (!accountEmail) {
        validationErrors.value.accountEmail = 'Email không được để trống'
        validationErrors.value.email = 'Email không được để trống'
        return false
    } else {
        // Sử dụng regex đơn giản hơn
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        if (!emailRegex.test(accountEmail)) {
            validationErrors.value.accountEmail = 'Email không hợp lệ'
            validationErrors.value.email = 'Email không hợp lệ'
            return false
        } else if (checkEmailExists(accountEmail)) {
            validationErrors.value.accountEmail = 'Email đã tồn tại trong hệ thống'
            validationErrors.value.email = 'Email đã tồn tại trong hệ thống'
            return false
        }
    }
    
    // Kiểm tra thông tin cá nhân
    if (newAccount.value.vaiTro !== 'ADMIN') {
        const hoTen = personalInfo.value.hoTen?.trim() || ''
        if (!hoTen) {
            validationErrors.value.hoTen = 'Họ tên không được để trống'
            return false
        } else if (hoTen.length > 100) {
            validationErrors.value.hoTen = 'Họ tên không được quá 100 ký tự'
            return false
        } else {
            // Sử dụng regex đơn giản hơn cho tên tiếng Việt
            const nameRegex = /^[a-zA-ZÀ-ỹ\s]+$/u
            if (!nameRegex.test(hoTen)) {
                validationErrors.value.hoTen = 'Họ tên chỉ được chứa chữ cái và khoảng trắng'
                return false
            }
        }
        
        const sdt = personalInfo.value.sdt?.trim() || ''
        if (!sdt) {
            validationErrors.value.sdt = 'Số điện thoại không được để trống'
            return false
        } else {
            const phoneRegex = /^0\d{9,10}$/
            if (!phoneRegex.test(sdt)) {
                validationErrors.value.sdt = 'Số điện thoại không hợp lệ'
                return false
            }
        }
    }
    
    return true
}

// Validation cho email field riêng biệt
const validateEmailField = (email) => {
    if (!email?.trim()) {
        return 'Email không được để trống'
    }
    
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(email.trim())) {
        return 'Email không hợp lệ'
    }
    
    if (checkEmailExists(email.trim())) {
        return 'Email đã tồn tại trong hệ thống'
    }
    
    return null
}

// Validation cho các field khác
const validateOtherFields = () => {
    if (newAccount.value.vaiTro && newAccount.value.vaiTro !== 'ADMIN') {
        const rawHoTen = personalInfo.value.hoTen || ''
        const normalizedHoTen = normalizeName(rawHoTen)
        
        if (!normalizedHoTen) {
            validationErrors.value.hoTen = 'Họ tên không được để trống'
            return false
        } else if (normalizedHoTen.length < 2) {
            validationErrors.value.hoTen = 'Họ tên phải có ít nhất 2 ký tự'
            return false
        } else if (normalizedHoTen.length > 100) {
            validationErrors.value.hoTen = 'Họ tên không được quá 100 ký tự'
            return false
        } else {
            const vietnameseRegex = /^[a-zA-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠƯĂẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỀỂẾỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪỬỮỰỲỴÝỶỸàáâãèéêìíòóôõùúăđĩũơưăạảấầẩẫậắằẳẵặẹẻẽềếểễệỉịọỏốồổỗộớờởỡợụủứừửữựỳỵýỷỹ\s]+$/u
            if (!vietnameseRegex.test(normalizedHoTen)) {
                validationErrors.value.hoTen = 'Họ tên chỉ chứa chữ cái và khoảng trắng, hỗ trợ tiếng Việt'
                return false
            }
        }

        const personalEmail = personalInfo.value.email?.trim() || ''
        if (!personalEmail) {
            validationErrors.value.email = 'Email không được để trống'
            return false
        } else {
            const emailRegex = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/
            if (!emailRegex.test(personalEmail)) {
                validationErrors.value.email = 'Email không hợp lệ'
                return false
            }
        }

        const phone = personalInfo.value.sdt?.replace(/\s/g, '') || ''
        if (!phone) {
            validationErrors.value.sdt = 'Số điện thoại không được để trống'
            return false
        } else {
            const phoneRegex = /^(03|05|07|08|09|02)\d{8}$/
            if (!phoneRegex.test(phone)) {
                validationErrors.value.sdt = 'Số điện thoại không hợp lệ (10 số, bắt đầu bằng 03/05/07/08/09/02)'
                return false
            } else if (checkPhoneExists(phone)) {
                validationErrors.value.sdt = 'Số điện thoại đã tồn tại trong hệ thống'
                return false
            }
        }

        if (personalInfo.value.ngaySinh) {
            const today = new Date()
            const birthDate = new Date(personalInfo.value.ngaySinh)
            if (birthDate > today) {
                validationErrors.value.ngaySinh = 'Ngày sinh không thể lớn hơn ngày hiện tại'
                return false
            }
            const age = Math.floor((today - birthDate) / (365.25 * 24 * 60 * 60 * 1000))
            if (age < 16) {
                validationErrors.value.ngaySinh = 'Người dùng phải ít nhất 16 tuổi'
                return false
            }
        }

        const hasAddressData = personalInfo.value.maTinh || 
                              personalInfo.value.maPhuong || 
                              personalInfo.value.diaChiChiTiet?.trim()
        
        if (hasAddressData) {
            if (!personalInfo.value.maTinh) {
                validationErrors.value.diaChi = 'Vui lòng chọn tỉnh/thành phố'
                return false
            }
            if (!personalInfo.value.maPhuong) {
                validationErrors.value.diaChi = 'Vui lòng chọn phường/xã'
                return false
            }
            if (!personalInfo.value.diaChiChiTiet?.trim()) {
                validationErrors.value.diaChiChiTiet = 'Vui lòng nhập địa chỉ chi tiết'
                return false
            }
        }
    }
    
    return Object.keys(validationErrors.value).length === 0
}


// Kiểm tra backend health trước khi tạo tài khoản
const checkBackendHealth = async () => {
    try {
        const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/tai-khoan`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('auth_token')}` },
            timeout: 5000
        })
        return true
    } catch (error) {
        console.error('❌ Backend health check failed:', error)
        return false
    }
}

const handleSaveAccount = async () => {
    submitted.value = true
    saving.value = true
    
    try {
        // Validation
        const isValid = await validateForm()
        if (!isValid) {
            toast.add({
                severity: 'warn',
                summary: 'Dữ liệu không hợp lệ',
                detail: 'Vui lòng kiểm tra các lỗi được đánh dấu màu đỏ',
                life: 4000
            })
            return
        }
        
        const accountData = {
            email: newAccount.value.email?.trim() || '',
            matKhau: newAccount.value.matKhau?.trim() || '',
            vaiTro: newAccount.value.vaiTro,
            trangThai: newAccount.value.trangThai || 1
        }
        
        if (newAccount.value.maTaiKhoan && newAccount.value.maTaiKhoan.trim()) {
            accountData.maTaiKhoan = newAccount.value.maTaiKhoan.trim()
        }

        if (newAccount.value.vaiTro && newAccount.value.vaiTro !== 'ADMIN') {
            const normalizedHoTen = normalizeName(personalInfo.value.hoTen);
            if (!normalizedHoTen) {
                throw new Error('Họ tên không hợp lệ sau normalize');
            }
            accountData.hoTen = normalizedHoTen;
            accountData.sdt = personalInfo.value.sdt?.replace(/\s/g, '') || '';
            accountData.email = personalInfo.value.email?.trim() || newAccount.value.email?.trim() || '';
            
            if (personalInfo.value.ngaySinh) {
                accountData.ngaySinh = personalInfo.value.ngaySinh.toISOString().split('T')[0];
            }

            const hasCompleteAddress = personalInfo.value.maTinh && 
                                     personalInfo.value.maPhuong && 
                                     personalInfo.value.diaChiChiTiet?.trim();
            
            if (hasCompleteAddress) {
                accountData.maTinh = personalInfo.value.maTinh;
                accountData.tenTinh = provinces.value.find(p => p.code === personalInfo.value.maTinh)?.name || '';
                accountData.maPhuong = personalInfo.value.maPhuong;
                accountData.tenPhuong = wards.value.find(w => w.code === personalInfo.value.maPhuong)?.name || '';
                accountData.diaChiChiTiet = personalInfo.value.diaChiChiTiet.trim();
            }
        }

        lastCreatedAccountRole.value = newAccount.value.vaiTro;
        
        console.log('🚀 Creating account with data:', accountData)
        console.log('🔐 Auth token present:', !!localStorage.getItem('auth_token'))
        console.log('🌐 API URL: http://localhost:8080/api/tai-khoan')
        
        const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/api/tai-khoan`, accountData, {
            headers: { 
                'Content-Type': 'application/json',
                'Accept': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
            },
            timeout: 15000
        })
        
        if (response.status === 201 || response.status === 200) {
            handleSuccessResponse(response)
            await fetchData()
            hideAddDialog()
            
            setTimeout(() => {
                switch(lastCreatedAccountRole.value) {
                    case 'USER':
                        toast.add({
                            severity: 'info',
                            summary: 'Chuyển trang',
                            detail: 'Đang chuyển đến trang quản lý khách hàng...',
                            life: 2000
                        })
                        setTimeout(() => router.push('/khach-hang'), 2000)
                        break
                    case 'NHANVIEN':
                        toast.add({
                            severity: 'info',
                            summary: 'Chuyển trang',
                            detail: 'Đang chuyển đến trang quản lý nhân viên...',
                            life: 2000
                        })
                        setTimeout(() => router.push('/nhan-vien'), 2000)
                        break
                    case 'ADMIN':
                        toast.add({
                            severity: 'success',
                            summary: 'Thành công',
                            detail: 'Tài khoản Admin đã được tạo thành công',
                            life: 3000
                        })
                        break
                }
            }, 1000)
        }
        
    } catch (error) {
        console.error('❌ Error creating account:', error)
        console.error('❌ Error details:', {
            message: error.message,
            response: error.response?.data,
            status: error.response?.status,
            config: {
                url: error.config?.url,
                method: error.config?.method,
                headers: error.config?.headers
            }
        })

        // Gọi handler chung để hiển thị toast thân thiện và gán lỗi field
        handleApiError(error, 'Không thể tạo tài khoản')

        // Chuẩn bị thông tin cho confirm dialog (chỉ hiển thị khi không phải lỗi trùng lặp dữ liệu)
        let errorMessage = 'Không thể tạo tài khoản'
        let errorDetail = ''

        const respData = error.response?.data
        if (respData?.message) {
            errorDetail = respData.message
        } else if (respData?.errors) {
            const errors = Object.values(respData.errors)
            errorDetail = errors.join('\n')
        } else if (error.message) {
            errorDetail = error.message
        } else {
            errorDetail = 'Lỗi không xác định từ server'
        }

        // Kiểm tra backend health
        if (error.code === 'ECONNREFUSED' || error.message.includes('Network Error')) {
            errorMessage = 'Không thể kết nối đến server'
            errorDetail = 'Vui lòng kiểm tra kết nối mạng và đảm bảo backend đang chạy'
        } else if (error.response?.status === 401) {
            errorMessage = 'Không có quyền truy cập'
            errorDetail = 'Token xác thực không hợp lệ hoặc đã hết hạn'
        } else if (error.response?.status === 409) {
            errorMessage = 'Dữ liệu bị trùng lặp'
            errorDetail = respData?.message || 'Email hoặc số điện thoại đã tồn tại trong hệ thống'
        } else if (error.response?.status === 500) {
            errorMessage = 'Lỗi server'
            errorDetail = respData?.message || 'Có lỗi xảy ra ở phía server. Vui lòng thử lại sau'
        }

        // Nếu là lỗi trùng email/số điện thoại thì KHÔNG bật confirm (đã hiển thị toast + gán lỗi field)
        const msg = (respData?.message || '').toLowerCase()
        const isDuplicateCase = [400, 409].includes(error.response?.status || 0) || (
            error.response?.status === 500 && (
                msg.includes('đã tồn tại') || msg.includes('duplicate') || msg.includes('unique') || msg.includes('trùng') || msg.includes('constraint') || msg.includes('phone') || msg.includes('sdt') || msg.includes('điện thoại') || msg.includes('email')
            )
        )

        if (!isDuplicateCase) {
            confirm.require({
                message: `❌ ${errorMessage}\n\n📋 Chi tiết lỗi:\n${errorDetail}`,
                header: 'Lỗi tạo tài khoản',
                icon: 'pi pi-exclamation-triangle',
                rejectClass: 'p-button-secondary p-button-outlined',
                rejectLabel: 'Đóng',
                acceptLabel: 'Thử lại',
                acceptClass: 'p-button-warning',
                accept: () => {
                    // Thử lại tạo tài khoản
                    handleSaveAccount();
                },
                reject: () => {
                    // Đóng dialog và reset state
                    hideAddDialog();
                }
            })
        }
    } finally {
        saving.value = false
    }
}

// ===== SUCCESS HANDLING =====
const handleSuccessResponse = (response) => {
    if (response.data && response.data.data) {
        const result = response.data.data
        
        let successDetail = 'Tài khoản đã được tạo thành công'
        
        if (result.taiKhoan) {
            successDetail += `. Mã tài khoản: ${result.taiKhoan.maTaiKhoan}`
        }
        
        if (result.khachHang) {
            successDetail += `. Mã khách hàng: ${result.khachHang.maKhachHang}`
        }
        
        if (result.nhanVien) {
            successDetail += `. Mã nhân viên: ${result.nhanVien.maNhanVien}`
        }
        
        if (result.warning) {
            toast.add({
                severity: 'warn',
                summary: 'Cảnh báo',
                detail: result.warning,
                life: 4000
            })
        }
        
        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: successDetail,
            life: 5000
        })
    } else {
        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: `Tài khoản ${getRoleLabel(newAccount.value.vaiTro).toLowerCase()} đã được tạo thành công`,
            life: 4000
        })
    }
}

// ===== API ERROR HANDLING =====
const handleApiError = (error, defaultMessage) => {
    let errorMessage = defaultMessage
    let errorDetail = ''
    let severity = 'error'
    
    if (error.response) {
        const { status, data } = error.response
        
        switch (status) {
            case 400:
                errorMessage = 'Dữ liệu không hợp lệ'
                severity = 'warn'
                
                if (data.errors && typeof data.errors === 'object') {
                    Object.keys(data.errors).forEach(field => {
                        validationErrors.value[field] = data.errors[field]
                    })
                    errorDetail = 'Vui lòng sửa các lỗi được đánh dấu màu đỏ'
                } else if (data.message) {
                    errorDetail = data.message
                    
                    if (data.message.includes('Email đã tồn tại')) {
                        validationErrors.value.email = 'Email đã tồn tại'
                        validationErrors.value.accountEmail = 'Email đã tồn tại'
                    } else if (data.message.includes('Số điện thoại đã tồn tại')) {
                        validationErrors.value.sdt = 'Số điện thoại đã tồn tại'
                    }
                }
                break
                
            case 409:
                errorMessage = 'Dữ liệu bị trùng lặp'
                severity = 'warn'
                errorDetail = data.message || 'Email hoặc số điện thoại đã tồn tại trong hệ thống'
                
                if (data.message && data.message.includes('email')) {
                    validationErrors.value.email = 'Email đã tồn tại'
                    validationErrors.value.accountEmail = 'Email đã tồn tại'
                }
                if (data.message && data.message.includes('phone')) {
                    validationErrors.value.sdt = 'Số điện thoại đã tồn tại'
                }
                break
                
            case 422:
                errorMessage = 'Dữ liệu không thể xử lý'
                severity = 'warn'
                errorDetail = data.message || 'Dữ liệu không đúng định dạng yêu cầu'
                break
                
            case 500:
                errorMessage = 'Lỗi hệ thống'
                errorDetail = data.message || 'Có lỗi xảy ra ở phía server. Vui lòng thử lại sau.'
                
                {
                    const msg = (data.message || '').toLowerCase()
                    // Nếu backend trả 500 nhưng có dấu hiệu trùng lặp/constraint, chuyển sang cảnh báo và gán lỗi field cụ thể
                    if (msg.includes('constraint') || msg.includes('duplicate') || msg.includes('unique') || msg.includes('đã tồn tại') || msg.includes('trùng')) {
                        severity = 'warn'
                        if (msg.includes('email')) {
                            validationErrors.value.email = 'Email đã tồn tại'
                            validationErrors.value.accountEmail = 'Email đã tồn tại'
                            errorDetail = 'Email đã tồn tại trong hệ thống'
                        }
                        if (msg.includes('phone') || msg.includes('sdt') || msg.includes('điện thoại')) {
                            validationErrors.value.sdt = 'Số điện thoại đã tồn tại'
                            errorDetail = 'Số điện thoại đã tồn tại trong hệ thống'
                        }
                        if (!validationErrors.value.email && !validationErrors.value.sdt) {
                            errorDetail = 'Vi phạm ràng buộc dữ liệu. Email hoặc số điện thoại có thể đã tồn tại.'
                        }
                    }
                }
                break
                
            default:
            errorDetail = data?.message || error.message || 'Lỗi không xác định từ máy chủ'
            {
                const msg = (data?.message || '').toLowerCase()
                if (msg.includes('email')) {
                    validationErrors.value.email = validationErrors.value.email || 'Email không hợp lệ hoặc đã tồn tại'
                    validationErrors.value.accountEmail = validationErrors.value.accountEmail || 'Email không hợp lệ hoặc đã tồn tại'
                }
                if (msg.includes('phone') || msg.includes('sdt') || msg.includes('điện thoại')) {
                    validationErrors.value.sdt = validationErrors.value.sdt || 'Số điện thoại không hợp lệ hoặc đã tồn tại'
                }
            }
        }
    } else if (error.code === 'ECONNREFUSED') {
        errorMessage = 'Lỗi kết nối'
        errorDetail = 'Không thể kết nối đến máy chủ'
        severity = 'warn'
    } else if (error.message.includes('timeout')) {
        errorMessage = 'Hết thời gian chờ'
        errorDetail = 'Yêu cầu mất quá nhiều thời gian. Vui lòng thử lại.'
        severity = 'warn'
    }

    toast.add({
        severity: severity,
        summary: errorMessage,
        detail: errorDetail,
        life: severity === 'error' ? 8000 : 6000
    })
}

// ===== UPDATE ACCOUNT METHOD =====
const handleUpdateAccount = async () => {
    submitted.value = true
    saving.value = true
    
    try {
        validationErrors.value = {}
        
        if (!editAccountData.value.email?.trim()) {
            validationErrors.value.editEmail = 'Email không được để trống'
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(editAccountData.value.email)) {
            validationErrors.value.editEmail = 'Email không hợp lệ'
        } else if (checkEmailExists(editAccountData.value.email, editAccountData.value.id)) {
            validationErrors.value.editEmail = 'Email đã tồn tại'
        }
        
        if (editAccountData.value.matKhau && editAccountData.value.matKhau.trim()) {
            if (editAccountData.value.matKhau.length < 6) {
                validationErrors.value.editMatKhau = 'Mật khẩu phải có ít nhất 6 ký tự'
            } else if (editAccountData.value.matKhau.length > 50) {
                validationErrors.value.editMatKhau = 'Mật khẩu không được quá 50 ký tự'
            }
        }
        
        if (Object.keys(validationErrors.value).length > 0) {
            return
        }
        
        const updateData = {
            email: editAccountData.value.email.trim()
        }
        
        if (editAccountData.value.matKhau && editAccountData.value.matKhau.trim()) {
            updateData.matKhau = editAccountData.value.matKhau.trim()
        }
        
        const response = await axios.put(
            `http://localhost:8080/api/tai-khoan/${editAccountData.value.id}`, 
            updateData,
            {
                headers: { 
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                timeout: 10000
            }
        )
        
        if (response.status === 200) {
            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: 'Thông tin đăng nhập đã được cập nhật',
                life: 3000
            })
            
            hideEditDialog()
            await fetchData()
        }
        
    } catch (error) {
        console.error('Update error:', error)
        handleApiError(error, 'Không thể cập nhật thông tin đăng nhập')
    } finally {
        saving.value = false
    }
}

// ===== EXPORT FUNCTION =====
const handleExportCSV = () => {
    exporting.value = true
    try {
        if (dt.value) {
            dt.value.exportCSV()
            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: `Đã xuất ${filteredAccounts.value.length} tài khoản`,
                life: 3000
            })
        }
    } catch (error) {
        console.error('Error exporting CSV:', error)
        toast.add({
            severity: 'error',
            summary: 'Lỗi xuất file',
            detail: 'Không thể xuất file CSV',
            life: 3000
        })
    } finally {
        exporting.value = false
    }
}

// ===== LIFECYCLE =====
onMounted(() => {
    // Kiểm tra quyền ADMIN từ localStorage
    checkAdminPermission()
    fetchData()
})

// Kiểm tra quyền ADMIN
const checkAdminPermission = () => {
    try {
        // Thử cả hai key có thể có trong localStorage
        let userInfo = localStorage.getItem('userInfo') || localStorage.getItem('user_info')
        if (userInfo) {
            const user = JSON.parse(userInfo)
            isAdmin.value = user.vaiTro === 'ADMIN'
            console.log('🔐 Admin permission checked:', { 
                userRole: user.vaiTro, 
                isAdmin: isAdmin.value,
                userEmail: user.email
            })
        } else {
            isAdmin.value = false
            console.log('🔐 No user info found in localStorage, setting isAdmin to false')
        }
    } catch (error) {
        console.error('❌ Error checking admin permission:', error)
        isAdmin.value = false
    }
}
</script>
<style scoped>
.card {
    border: none;
    box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}

.search-filter-section {
    background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.border-bottom {
    border-bottom: 1px solid #dee2e6;
}

.text-muted {
    color: #6c757d;
}

.text-sm {
    font-size: 0.875rem;
}

.font-bold {
    font-weight: 700;
}

.font-semibold {    
    font-weight: 600;
}

.font-medium {
    font-weight: 500;
}

.stats-summary {
    background: rgba(255, 255, 255, 0.8);
    padding: 0.5rem 1rem;
    border-radius: 0.5rem;
    border: 1px solid #e2e8f0;
}

.responsive-table {
    border-radius: 0.5rem;
    overflow: hidden;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* Table Styling */
:deep(.p-datatable) {
    border: none;
}

:deep(.p-datatable-header) {
    background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
    border-bottom: 2px solid #e2e8f0;
    padding: 1.5rem;
}

:deep(.p-datatable-tbody tr) {
    transition: all 0.2s ease;
}

:deep(.p-datatable-tbody tr:hover) {
    background-color: #f8fafc;
    transform: translateY(-1px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

:deep(.p-datatable-tbody tr.p-datatable-row-selected) {
    background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
    border-left: 4px solid #3b82f6;
}

:deep(.p-paginator) {
    background: #f8fafc;
    border-top: 1px solid #e2e8f0;
}
</style>

