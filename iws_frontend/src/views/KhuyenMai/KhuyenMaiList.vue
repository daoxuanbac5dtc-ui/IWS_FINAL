<template>
    <div class="card">
        <!-- Toolbar -->
<Toolbar class="mb-4">
    <template #start>
        <Button label="Thêm mới" icon="pi pi-plus" severity="secondary" class="mr-2" @click="openNew" />
        <Button label="Xóa" icon="pi pi-trash" severity="secondary" @click="confirmDeleteSelected" :disabled="!selectedKhuyenMai || !selectedKhuyenMai.length" />
    </template>
    <template #end>
        <Button label="Xuất CSV" icon="pi pi-upload" severity="secondary" @click="exportCSV" />
    </template>
</Toolbar>

<!-- 🆕 BỘ LỌC VÀ TÌM KIẾM ĐƯỢC TÍCH HỢP -->
<div class="mb-4 rounded-lg border bg-white p-4 shadow-sm">
    <div class="mb-3 flex items-center gap-2">
        <i class="pi pi-filter text-blue-600"></i>
        <h5 class="m-0 font-semibold">Tìm kiếm & Bộ lọc</h5>
    </div>
    <div class="grid grid-cols-12 gap-2">
        <!-- Tìm kiếm -->
        <div class="col-span-12 md:col-span-5">
            <label class="mb-2 block text-sm font-medium">Tìm kiếm</label>
            <IconField>
                <InputIcon>
                    <i class="pi pi-search" />
                </InputIcon>
                <InputText 
                    v-model="filters['global'].value" 
                    placeholder="Tìm theo tên, mã khuyến mãi..." 
                    fluid
                />
            </IconField>
        </div>

        <!-- Lọc theo trạng thái -->
        <div class="col-span-6 md:col-span-3">
            <label class="mb-2 block text-sm font-medium">Trạng thái</label>
            <Select 
                v-model="statusFilter" 
                :options="statusOptions" 
                optionLabel="label" 
                optionValue="value" 
                placeholder="Chọn trạng thái"
                showClear
                fluid
                @change="onStatusFilterChange"
            />
        </div>
        
    </div>
</div>

<!-- DataTable - CẬP NHẬT HEADER -->
<DataTable
    ref="dt"
    v-model:selection="selectedKhuyenMai"
    :value="filteredKhuyenMai"
    dataKey="id"
    :paginator="true"
    :rows="10"
    :filters="filters"
    paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
    :rowsPerPageOptions="[5, 10, 25]"
    currentPageReportTemplate="Hiển thị {first} đến {last} của {totalRecords} khuyến mãi"
    :loading="isLoading"
>
    <!-- ✅ HEADER ĐÃ ĐƯỢC ĐƠN GIẢN HÓA -->
    <template #header>
        <div class="flex items-center justify-between">
            <h4 class="m-0">🎫 Quản Lý Khuyến Mãi</h4>
        </div>
    </template>
            <Column selectionMode="multiple" style="width: 3rem" :exportable="false"></Column>
            <!-- <Column field="id" header="ID" sortable style="width: 8rem">
                <template #body="slotProps">
                    <span class="font-bold text-primary">#{{ slotProps.data.id }}</span>
                </template>
            </Column> -->
            <Column field="STT" header="STT" sortable style="min-width: 8rem">
                <template #body="slotProps">
                    {{ getRowIndex(slotProps.index) }}
                </template>
            </Column>
            <Column field="maKhuyenMai" header="Mã KM" sortable style="width: 10rem">
                <template #body="slotProps">
                    <Tag :value="slotProps.data.maKhuyenMai" severity="primary" />
                </template>
            </Column>
            <Column field="tenKhuyenMai" header="Tên Khuyến Mãi" sortable style="min-width: 16rem" />
            <Column field="giaTri" header="Giá Trị Giảm" sortable style="width: 12rem">
                <template #body="slotProps">
                    <span class="font-bold text-red-600">{{ formatPercentage(slotProps.data.giaTri) }}%</span>
                </template>
            </Column>
            <Column field="soLuongSanPham" header="Sản Phẩm" sortable style="width: 10rem">
                <template #body="slotProps">
                    <Tag :value="slotProps.data.soLuongSanPham || 0" severity="info">
                        <i class="pi pi-shopping-bag mr-1"></i>
                        {{ slotProps.data.soLuongSanPham || 0 }}
                    </Tag>
                </template>
            </Column>
            <Column field="ngayBatDau" header="Ngày Bắt Đầu" sortable style="width: 12rem">
                <template #body="slotProps">
                    {{ formatDateDisplay(slotProps.data.ngayBatDau) }}
                </template>
            </Column>
            <Column field="ngayKetThuc" header="Ngày Kết Thúc" sortable style="width: 12rem">
                <template #body="slotProps">
                    {{ formatDateDisplay(slotProps.data.ngayKetThuc) }}
                </template>
            </Column>
            <Column field="trangThai" header="Trạng Thái" sortable style="width: 12rem">
                <template #body="slotProps">
                    <Tag 
                        :value="getPromotionStatusText(slotProps.data)" 
                        :severity="getPromotionStatusSeverity(slotProps.data)"
                    >
                        <i :class="getPromotionStatusIcon(slotProps.data)" class="mr-1"></i>
                        {{ getPromotionStatusText(slotProps.data) }}
                    </Tag>
                </template>
            </Column>
            <Column :exportable="false" style="width: 18rem">
                <template #body="slotProps">
                    <div class="flex justify-between gap-1">
                        <Button icon="pi pi-eye" outlined rounded size="small" severity="info" @click="viewPromotionProducts(slotProps.data)" title="Xem sản phẩm" />
                        <Button 
                            icon="pi pi-plus" 
                            outlined 
                            rounded 
                            size="small" 
                            severity="warning" 
                            @click="openApplyProductsDialog(slotProps.data)" 
                            title="Áp dụng sản phẩm"
                            :disabled="getPromotionStatus(slotProps.data) !== 'active'"
                        />
                        <Button icon="pi pi-pencil" outlined rounded size="small" @click="editKhuyenMai(slotProps.data)" title="Chỉnh sửa" />
                        <Button icon="pi pi-trash" outlined rounded severity="danger" size="small" @click="confirmDeleteKhuyenMai(slotProps.data)" title="Xóa" />
                        <Button 
                            icon="pi pi-refresh" 
                            outlined 
                            rounded 
                            severity="secondary" 
                            size="small" 
                            @click="changeStatus(slotProps.data)" 
                            :title="slotProps.data.trangThai === 1 ? 'Hoạt động ' : 'Không hoạt động'"
                        />
                    </div>
                </template>
            </Column>
            
            <template #empty>
                <div class="p-5 text-center">
                    <i class="pi pi-tags text-muted mb-3 text-5xl"></i>
                    <h5 class="text-muted">Không tìm thấy khuyến mãi</h5>
                    <p class="text-muted">
                        {{ filters['global'].value || statusFilter ? 'Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm.' : 'Thêm khuyến mãi mới để bắt đầu.' }}
                    </p>
                    <Button 
                        v-if="filters['global'].value || statusFilter" 
                        label="Xóa bộ lọc" 
                        icon="pi pi-times" 
                        text 
                        @click="clearAllFilters" 
                        class="mt-3" 
                    />
                </div>
            </template>
        </DataTable>

        <!-- Add/Edit Promotion Dialog -->
        <Dialog v-model:visible="khuyenMaiDialog" :style="{ width: '500px' }" :header="khuyenMai.id ? 'Cập nhật khuyến mãi' : 'Thêm khuyến mãi'" :modal="true">
            <div class="flex flex-col gap-6">
                <!-- Mã Khuyến Mãi -->
                <div>
                    <label for="maKhuyenMai" class="mb-3 block font-bold">Mã Khuyến Mãi</label>
                    <InputText 
                        id="maKhuyenMai" 
                        v-model="khuyenMai.maKhuyenMai" 
                        fluid 
                        readonly="true" 
                        class="bg-gray-50" 
                    />
                    
                </div>
                <!-- Tên Khuyến Mãi -->
                <div>
                    <label for="tenKhuyenMai" class="mb-3 block font-bold">
                        Tên Khuyến Mãi <span class="text-red-500">*</span>
                    </label>
                    <InputText 
                        id="tenKhuyenMai" 
                        v-model.trim="khuyenMai.tenKhuyenMai" 
                        required="true" 
                        autofocus 
                        :invalid="submitted && (!khuyenMai.tenKhuyenMai || duplicateErrors.tenKhuyenMai)" 
                        fluid 
                        @blur="validateField('tenKhuyenMai')"
                        @input="() => { if (submitted) validateField('tenKhuyenMai') }"
                        placeholder="Nhập tên khuyến mãi..."
                    />
                    <small v-if="submitted && !khuyenMai.tenKhuyenMai" class="text-red-500">
                        Tên khuyến mãi là bắt buộc.
                    </small>
                    <small v-else-if="submitted && duplicateErrors.tenKhuyenMai" class="text-red-500">
                        {{ duplicateErrors.tenKhuyenMai }}
                    </small>
                </div>

                <!-- Giá Trị Giảm -->
                <div>
                    <label for="giaTri" class="mb-3 block font-bold">
                        Giá Trị Giảm (%) <span class="text-red-500">*</span>
                    </label>
                    <InputText 
                        id="giaTri" 
                        v-model="khuyenMai.giaTri" 
                        :min="0.1" 
                        :max="100" 
                        suffix="%" 
                        fluid 
                        :invalid="submitted && (!khuyenMai.giaTri || khuyenMai.giaTri <= 0 || khuyenMai.giaTri > 100)"
                        :minFractionDigits="0"
                        :maxFractionDigits="2"
                        placeholder="Nhập % giảm giá..."
                        @blur="validateField('giaTri')"
                        @input="validateField('giaTri')"
                        mode="decimal"
                        :useGrouping="false"
                    />
                    <small v-if="submitted && (khuyenMai.giaTri == null || khuyenMai.giaTri <= 0)" class="text-red-500">
                        Giá trị giảm phải lớn hơn 0.
                    </small>
                    <small v-else-if="submitted && khuyenMai.giaTri > 100" class="text-red-500">
                        Giá trị giảm không được vượt quá 100%.
                    </small>
                    <small v-else-if="submitted && isNaN(khuyenMai.giaTri)" class="text-red-500">
                        Giá trị giảm phải là số hợp lệ.
                    </small>
                </div>

                <!-- Ngày Bắt Đầu -->
                <div>
                    <label for="ngayBatDau" class="mb-3 block font-bold">
                        Ngày Bắt Đầu <span class="text-red-500">*</span>
                    </label>
                    <Calendar 
                        id="ngayBatDau" 
                        v-model="khuyenMai.ngayBatDau" 
                        dateFormat="dd/mm/yy" 
                        showTime 
                        hourFormat="24" 
                        fluid 
                        :invalid="submitted && !khuyenMai.ngayBatDau"
                        placeholder="Chọn ngày bắt đầu..."
                        @date-select="validateField('ngayBatDau')"
                    />
                    <small v-if="submitted && !khuyenMai.ngayBatDau" class="text-red-500">
                        Ngày bắt đầu là bắt buộc.
                    </small>
                </div>

                <!-- Ngày Kết Thúc -->
                <div>
                    <label for="ngayKetThuc" class="mb-3 block font-bold">
                        Ngày Kết Thúc <span class="text-red-500">*</span>
                    </label>
                    <Calendar 
                        id="ngayKetThuc" 
                        v-model="khuyenMai.ngayKetThuc" 
                        dateFormat="dd/mm/yy" 
                        showTime 
                        hourFormat="24" 
                        fluid 
                        :invalid="submitted && (!khuyenMai.ngayKetThuc || isEndDateBeforeStartDate)"
                        placeholder="Chọn ngày kết thúc..."
                        @date-select="validateField('ngayKetThuc')"
                    />
                    <small v-if="submitted && !khuyenMai.ngayKetThuc" class="text-red-500">
                        Ngày kết thúc là bắt buộc.
                    </small>
                    <small v-else-if="submitted && isEndDateBeforeStartDate" class="text-red-500">
                        Ngày kết thúc phải sau ngày bắt đầu.
                    </small>
                </div>

                <!-- Trạng Thái -->
                <div>
                    <label for="trangThai" class="mb-3 block font-bold">
                        Trạng Thái Hoạt Động <span class="text-red-500">*</span>
                    </label>
                    <Select 
                        id="trangThai" 
                        v-model="khuyenMai.trangThai" 
                        :options="statusOptionsForForm" 
                        optionLabel="label" 
                        optionValue="value" 
                        placeholder="Chọn trạng thái..." 
                        fluid 
                        :invalid="submitted && khuyenMai.trangThai == null"
                    />
                    <small v-if="submitted && khuyenMai.trangThai == null" class="text-red-500">
                        Trạng thái hoạt động là bắt buộc.
                    </small>
                    <small v-else class="text-gray-500">
                        Trạng thái này chỉ là bật/tắt khuyến mãi, không phụ thuộc vào thời gian
                    </small>
                </div>
            </div>
            
            <template #footer>
                <Button label="Hủy" icon="pi pi-times" text @click="hideDialog" />
                <Button 
                    label="Lưu" 
                    icon="pi pi-check" 
                    @click="confirmAddDialog = true" 
                                  />
            </template>
        </Dialog>

        <!-- Apply Products Dialog -->
        <Dialog v-model:visible="applyProductsDialog" :style="{ width: '95vw', height: '85vh' }" header="Áp Dụng Sản Phẩm Cho Khuyến Mãi" :modal="true" class="apply-products-dialog">
            <div class="flex h-full flex-col">
                <!-- Promotion info -->
                <div class="mb-4 rounded-lg bg-blue-50 p-4">
                    <h6 class="mb-2 font-bold text-blue-800"><i class="pi pi-tag mr-2"></i>Khuyến mãi được chọn:</h6>
                    <div class="flex flex-wrap items-center gap-4">
                        <Tag :value="selectedPromotionForApply?.maKhuyenMai" severity="secondary" />
                        <span class="font-medium">{{ selectedPromotionForApply?.tenKhuyenMai }}</span>
                        <span class="font-bold text-red-600"> Giảm {{ formatPercentage(selectedPromotionForApply?.giaTri) }}% </span>
                        <Tag :value="`${selectedProductsForApply?.length || 0} sản phẩm đã chọn`" severity="success" />
                        <Tag 
                            :value="getPromotionStatusText(selectedPromotionForApply)" 
                            :severity="getPromotionStatusSeverity(selectedPromotionForApply)"
                        />
                    </div>
                </div>

                <!-- Search and filter -->
                <div class="mb-4 flex flex-wrap items-center gap-4">
                    <IconField class="min-w-64 flex-1">
                        <InputIcon>
                            <i class="pi pi-search" />
                        </InputIcon>
                        <InputText v-model="productSearchKeyword" placeholder="Tìm kiếm theo tên, mã sản phẩm..." @input="debounceSearch" fluid />
                    </IconField>
                    <Button label="Tải lại" icon="pi pi-refresh" @click="loadAvailableProducts" severity="secondary" />
                    <Button label="Chọn tất cả" icon="pi pi-check-square" @click="selectAllProducts" severity="info" :disabled="!availableProducts.length" />
                    <Button label="Bỏ chọn" icon="pi pi-square" @click="clearSelectedProducts" text />
                </div>

                <!-- Products table -->
                <div class="flex-1 overflow-hidden">
                    <DataTable
                        v-model:selection="selectedProductsForApply"
                        :value="availableProducts"
                        dataKey="id"
                        :paginator="true"
                        :rows="15"
                        :loading="isLoadingProducts"
                        scrollable
                        scrollHeight="flex"
                        class="h-full"
                        :rowsPerPageOptions="[15, 25, 50, 100]"
                        paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                        currentPageReportTemplate="Hiển thị {first} đến {last} của {totalRecords} sản phẩm"
                        stripedRows
                    >
                        <Column selectionMode="multiple" style="width: 3rem" frozen></Column>
                        <Column field="maChiTiet" header="Mã Chi Tiết" style="width: 130px" frozen>
                            <template #body="slotProps">
                                <Tag :value="slotProps.data.maChiTiet" severity="warning" />
                            </template>
                        </Column>
                        <Column field="sanPham.tenSanPham" header="Tên Sản Phẩm" style="min-width: 250px">
                            <template #body="slotProps">
                                <div class="font-medium">{{ slotProps.data.sanPham?.tenSanPham }}</div>
                                <small class="text-gray-500">{{ slotProps.data.sanPham?.maSanPham }}</small>
                            </template>
                        </Column>
                        <Column field="sanPham.thuongHieu.tenThuongHieu" header="Thương Hiệu" style="width: 130px" />
                        <Column field="sanPham.danhMuc.tenDanhMuc" header="Danh Mục" style="width: 130px" />
                        <Column field="mauSac.tenMauSac" header="Màu Sắc" style="width: 120px">
                            <template #body="slotProps">
                                <div class="flex items-center gap-2">
                                    <span class="inline-block h-4 w-4 rounded-full border" :style="{ backgroundColor: slotProps.data.mauSac?.maMau || '#ccc' }"></span>
                                    <span>{{ slotProps.data.mauSac?.tenMauSac }}</span>
                                </div>
                            </template>
                        </Column>
                        <Column field="kichCo.tenKichCo" header="Size" style="width: 80px" sortable>
                            <template #body="slotProps">
                                <Tag :value="slotProps.data.kichCo?.tenKichCo" severity="info" />
                            </template>
                        </Column>
                        <Column field="soLuong" header="Số lượng" style="width: 100px" sortable>
                            <template #body="slotProps">
                                <Tag :value="slotProps.data.soLuong" :severity="slotProps.data.soLuong > 10 ? 'success' : slotProps.data.soLuong > 0 ? 'warning' : 'danger'" />
                            </template>
                        </Column>
                        <Column field="giaGoc" header="Giá Gốc" style="width: 130px" sortable>
                            <template #body="slotProps">
                                <span class="font-bold text-blue-600">{{ formatCurrency(slotProps.data.giaGoc) }}</span>
                            </template>
                        </Column>
                        <Column header="Giá Sau KM" style="width: 130px">
                            <template #body="slotProps">
                                <span class="font-bold text-red-600">
                                    {{ formatCurrency(calculateDiscountedPrice(slotProps.data.giaGoc, selectedPromotionForApply?.giaTri)) }}
                                </span>
                            </template>
                        </Column>
                        <Column header="Tiết Kiệm" style="width: 130px">
                            <template #body="slotProps">
                                <span class="font-bold text-green-600">
                                    {{ formatCurrency(calculateSavings(slotProps.data.giaGoc, selectedPromotionForApply?.giaTri)) }}
                                </span>
                            </template>
                        </Column>
                        <template #empty>
                            <div class="p-8 text-center">
                                <i class="pi pi-shopping-bag mb-4 text-6xl text-gray-400"></i>
                                <h5 class="mb-2 text-gray-600">Không có sản phẩm khả dụng</h5>
                                <p class="text-gray-500">
                                    {{ productSearchKeyword ? 'Không tìm thấy sản phẩm phù hợp' : 'Tất cả sản phẩm đã được áp dụng khuyến mãi hoặc hết hàng' }}
                                </p>
                                <Button v-if="productSearchKeyword" label="Xóa bộ lọc" icon="pi pi-times" text @click="clearSearch" class="mt-3" />
                            </div>
                        </template>
                    </DataTable>
                </div>
            </div>
            <template #footer>
                <div class="flex w-full items-center justify-between">
                    <span class="font-medium text-gray-600">
                        Đã chọn: <strong class="text-blue-600">{{ selectedProductsForApply?.length || 0 }}</strong> sản phẩm
                    </span>
                    <div class="flex gap-2">
                        <Button label="Hủy" icon="pi pi-times" text @click="applyProductsDialog = false" />
                        <Button 
                            label="Áp Dụng Khuyến Mãi" 
                            icon="pi pi-check" 
                            @click="applyPromotionToProducts" 
                            :disabled="!selectedProductsForApply || selectedProductsForApply.length === 0 || getPromotionStatus(selectedPromotionForApply) !== 'active'" 
                            :loading="isApplyingPromotion" 
                        />
                    </div>
                </div>
            </template>
        </Dialog>

        <!-- View Promotion Products Dialog -->
        <Dialog v-model:visible="viewProductsDialog" :style="{ width: '95vw', height: '85vh' }" header="Sản Phẩm Được Áp Dụng Khuyến Mãi" :modal="true">
            <div class="flex h-full flex-col">
                <!-- Promotion info -->
                <div class="mb-4 rounded-lg bg-green-50 p-4">
                    <h6 class="mb-2 font-bold text-green-800"><i class="pi pi-info-circle mr-2"></i>Thông tin khuyến mãi:</h6>
                    <div class="flex flex-wrap items-center gap-4">
                        <Tag :value="selectedPromotionForView?.maKhuyenMai" severity="secondary" />
                        <span class="font-medium">{{ selectedPromotionForView?.tenKhuyenMai }}</span>
                        <span class="font-bold text-red-600"> Giảm {{ formatPercentage(selectedPromotionForView?.giaTri) }}% </span>
                        <Tag :value="`${promotionProducts.length} sản phẩm`" severity="info" />
                        <Tag 
                            :value="getPromotionStatusText(selectedPromotionForView)" 
                            :severity="getPromotionStatusSeverity(selectedPromotionForView)"
                        />
                    </div>
                </div>

                <!-- Summary stats -->
                <div class="mb-4 grid grid-cols-4 gap-4">
                    <div class="rounded-lg bg-blue-50 p-3 text-center">
                        <div class="text-2xl font-bold text-blue-600">{{ promotionProducts.length }}</div>
                        <div class="text-sm text-blue-800">Sản phẩm</div>
                    </div>
                    <div class="rounded-lg bg-green-50 p-3 text-center">
                        <div class="text-2xl font-bold text-green-600">{{ formatCurrency(totalPromotionValue) }}</div>
                        <div class="text-sm text-green-800">Tổng tiết kiệm</div>
                    </div>
                    <div class="rounded-lg bg-orange-50 p-3 text-center">
                        <div class="text-2xl font-bold text-orange-600">{{ formatCurrency(totalOriginalValue) }}</div>
                        <div class="text-sm text-orange-800">Tổng giá gốc</div>
                    </div>
                    <div class="rounded-lg bg-purple-50 p-3 text-center">
                        <div class="text-2xl font-bold text-purple-600">{{ formatCurrency(totalDiscountedValue) }}</div>
                        <div class="text-sm text-purple-800">Tổng sau KM</div>
                    </div>
                </div>

                <!-- Products table -->
                <div class="flex-1 overflow-hidden">
                    <DataTable
                        :value="promotionProducts"
                        dataKey="id"
                        :paginator="true"
                        :rows="15"
                        :loading="isLoadingPromotionProducts"
                        scrollable
                        scrollHeight="flex"
                        class="h-full"
                        :rowsPerPageOptions="[15, 25, 50]"
                        paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                        currentPageReportTemplate="Hiển thị {first} đến {last} của {totalRecords} sản phẩm"
                        stripedRows
                    >
                        <Column field="chiTietSanPham.maChiTiet" header="Mã Chi Tiết" style="width: 130px" frozen>
                            <template #body="slotProps">
                                <Tag :value="slotProps.data.chiTietSanPham.maChiTiet" severity="warning" />
                            </template>
                        </Column>
                        <Column field="chiTietSanPham.sanPham.tenSanPham" header="Tên Sản Phẩm" style="min-width: 250px">
                            <template #body="slotProps">
                                <div class="font-medium">{{ slotProps.data.chiTietSanPham.sanPham?.tenSanPham }}</div>
                                <small class="text-gray-500">{{ slotProps.data.chiTietSanPham.sanPham?.maSanPham }}</small>
                            </template>
                        </Column>
                        <Column field="chiTietSanPham.sanPham.thuongHieu.tenThuongHieu" header="Thương Hiệu" style="width: 130px" />
                        <Column field="chiTietSanPham.mauSac.tenMauSac" header="Màu Sắc" style="width: 120px">
                            <template #body="slotProps">
                                <div class="flex items-center gap-2">
                                    <span>{{ slotProps.data.chiTietSanPham.mauSac?.tenMauSac }}</span>
                                </div>
                            </template>
                        </Column>
                        <Column field="chiTietSanPham.kichCo.tenKichCo" header="Size" style="width: 80px">
                            <template #body="slotProps">
                                <Tag :value="slotProps.data.chiTietSanPham.kichCo?.tenKichCo" severity="info" />
                            </template>
                        </Column>
                        <Column field="chiTietSanPham.soLuong" header="Số lượng" style="width: 100px">
                            <template #body="slotProps">
                                <Tag :value="slotProps.data.chiTietSanPham.soLuong" :severity="slotProps.data.chiTietSanPham.soLuong > 10 ? 'success' : slotProps.data.chiTietSanPham.soLuong > 0 ? 'warning' : 'danger'" />
                            </template>
                        </Column>
                        <Column field="chiTietSanPham.giaGoc" header="Giá Gốc" style="width: 130px">
                            <template #body="slotProps">
                                <span class="font-bold text-blue-600">{{ formatCurrency(slotProps.data.chiTietSanPham.giaGoc) }}</span>
                            </template>
                        </Column>
                        <Column field="chiTietSanPham.giaBan" header="Giá Sau KM" style="width: 130px">
                            <template #body="slotProps">
                                <span class="font-bold text-red-600">
                                    {{ formatCurrency(slotProps.data.chiTietSanPham.giaBan) }}
                                </span>
                            </template>
                        </Column>
                        <Column header="Tiết Kiệm" style="width: 130px">
                            <template #body="slotProps">
                                <span class="font-bold text-green-600">
                                    {{ formatCurrency(slotProps.data.chiTietSanPham.giaGoc - slotProps.data.chiTietSanPham.giaBan) }}
                                </span>
                            </template>
                        </Column>
                        <Column header="Ngày Áp Dụng" style="width: 140px">
                            <template #body="slotProps">
                                <span class="text-gray-600">{{ formatDateDisplay(slotProps.data.ngayTao) }}</span>
                            </template>
                        </Column>
                        <Column header="Thao Tác" style="width: 100px" frozen alignFrozen="right">
                            <template #body="slotProps">
                                <Button icon="pi pi-times" outlined rounded severity="danger" size="small" @click="confirmRemoveProductFromPromotion(slotProps.data)" title="Hủy áp dụng" />
                            </template>
                        </Column>
                        <template #empty>
                            <div class="p-8 text-center">
                                <i class="pi pi-shopping-bag mb-4 text-6xl text-gray-400"></i>
                                <h5 class="mb-2 text-gray-600">Chưa có sản phẩm nào được áp dụng</h5>
                                <p class="mb-4 text-gray-500">Khuyến mãi này chưa được áp dụng cho sản phẩm nào.</p>
                                <Button label="Áp Dụng Sản Phẩm Ngay" icon="pi pi-plus" @click="openApplyProductsFromView" :disabled="getPromotionStatus(selectedPromotionForView) !== 'active'" />
                            </div>
                        </template>
                    </DataTable>
                </div>
            </div>
            <template #footer>
                <div class="flex w-full items-center justify-between">
                    <div class="text-sm text-gray-600">
                        Tổng cộng <strong>{{ promotionProducts.length }}</strong> sản phẩm được áp dụng khuyến mãi
                    </div>
                    <div class="flex gap-2">
                        <Button label="Đóng" icon="pi pi-times" @click="viewProductsDialog = false" />
                        <Button 
                            label="Áp Dụng Thêm Sản Phẩm" 
                            icon="pi pi-plus" 
                            @click="openApplyProductsFromView" 
                            severity="secondary"
                            :disabled="getPromotionStatus(selectedPromotionForView) !== 'active'"
                        />
                    </div>
                </div>
            </template>
        </Dialog>

        <!-- Remove Product from Promotion Dialog -->
        <Dialog v-model:visible="removeProductDialog" :style="{ width: '450px' }" header="Xác nhận hủy áp dụng" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle text-3xl text-orange-500" />
                <div>
                    <p>Bạn có chắc chắn muốn hủy áp dụng khuyến mãi cho sản phẩm này?</p>
                    <div class="mt-3 rounded bg-gray-50 p-3">
                        <div><strong>Sản phẩm:</strong> {{ selectedProductForRemove?.chiTietSanPham?.sanPham?.tenSanPham }}</div>
                        <div><strong>Mã chi tiết:</strong> {{ selectedProductForRemove?.chiTietSanPham?.maChiTiet }}</div>
                        <div><strong>Giá sẽ trở về:</strong> {{ formatCurrency(selectedProductForRemove?.chiTietSanPham?.giaGoc) }}</div>
                    </div>
                </div>
            </div>
            <template #footer>
                <Button label="Không" icon="pi pi-times" text @click="removeProductDialog = false" />
                <Button label="Có, hủy áp dụng" icon="pi pi-check" severity="warning" @click="removeProductFromPromotion" />
            </template>
        </Dialog>

        <!-- Delete Promotion Dialog -->
        <Dialog v-model:visible="deleteKhuyenMaiDialog" :style="{ width: '450px' }" header="Xác nhận xóa" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle text-3xl text-red-500" />
                <div>
                    <span v-if="khuyenMai">
                        Bạn có chắc chắn muốn xóa khuyến mãi <strong>{{ khuyenMai.tenKhuyenMai }}</strong
                        >?
                    </span>
                </div>
            </div>
            <template #footer>
                <Button label="Không" icon="pi pi-times" text @click="deleteKhuyenMaiDialog = false" />
                <Button label="Có, xóa" icon="pi pi-check" severity="danger" @click="deleteKhuyenMai" />
            </template>
        </Dialog>

        <!-- Delete Selected Promotions Dialog -->
        <Dialog v-model:visible="deleteKhuyenMaisDialog" :style="{ width: '450px' }" header="Xác nhận xóa" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle text-3xl text-red-500" />
                <div>
                    <span
                        >Bạn có chắc chắn muốn xóa <strong>{{ selectedKhuyenMai?.length || 0 }}</strong> khuyến mãi đã chọn?</span
                    >
                    <div class="mt-3 rounded bg-red-50 p-3 text-red-700"><strong>Lưu ý:</strong> Tất cả sản phẩm được áp dụng các khuyến mãi này sẽ được khôi phục về giá gốc.</div>
                </div>
            </div>
            <template #footer>
                <Button label="Không" icon="pi pi-times" text @click="deleteKhuyenMaisDialog = false" />
                <Button label="Có, xóa tất cả" icon="pi pi-check" severity="danger" @click="deleteSelectedKhuyenMais" />
            </template>
        </Dialog>

        <Dialog v-model:visible="confirmAddDialog" header="Xác nhận" modal>
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl text-red-500" />
                <div>
                    <p v-if="khuyenMai" class="mb-2">
                        Bạn có chắc chắn muốn thực hiện hành động này ?
                    </p>
                </div>
            </div>
            <template #footer>
                <Button label="Hủy bỏ" icon="pi pi-times" text @click="confirmAddDialog = false" :disabled="loading" />
                <Button label="Thực hiện" icon="pi pi-check" severity="success" @click="handleAddKhuyenMaiConfirm" :loading="loading" />
            </template>
        </Dialog>
        <!-- Toast Notifications -->
        <Toast />
    </div>
</template>

<script setup>
import { FilterMatchMode } from '@primevue/core/api';
import axios from 'axios';
import { InputText } from 'primevue';
import { useToast } from 'primevue/usetoast';
import { computed, onMounted, ref } from 'vue';

const toast = useToast();
const dt = ref();
const khuyenMais = ref([]);
const khuyenMaiDialog = ref(false);
const deleteKhuyenMaiDialog = ref(false);
const deleteKhuyenMaisDialog = ref(false);
const applyProductsDialog = ref(false);
const viewProductsDialog = ref(false);
const removeProductDialog = ref(false);
const khuyenMai = ref({});
const selectedKhuyenMai = ref();
const filters = ref({
    global: { value: null, matchMode: FilterMatchMode.CONTAINS }
});
const statusFilter = ref('');
const submitted = ref(false);
const isLoading = ref(false);
const isSaving = ref(false);
const confirmAddDialog = ref(false);

// Thêm ref để lưu trữ lỗi trùng lặp
const duplicateErrors = ref({
    tenKhuyenMai: ''
});

// Apply products dialog
const availableProducts = ref([]);
const selectedProductsForApply = ref([]);
const selectedPromotionForApply = ref(null);
const productSearchKeyword = ref('');
const isLoadingProducts = ref(false);
const isApplyingPromotion = ref(false);

// View products dialog
const promotionProducts = ref([]);
const selectedPromotionForView = ref(null);
const isLoadingPromotionProducts = ref(false);
const selectedProductForRemove = ref(null);

// Search debounce
let searchTimeout = null;

const statusOptions = ref([
    // { label: 'Tất cả trạng thái', value: '' },
    { label: 'Chưa diễn ra', value: 'pending' },
    { label: 'Đang diễn ra', value: 'active' },
    { label: 'Đã hết hạn', value: 'expired' },
    { label: 'Không hoạt động', value: 'disabled' }
]);

const statusOptionsForForm = ref([
    { label: 'Hoạt động', value: 1 },
    { label: 'Không hoạt động', value: 0 }
]);

onMounted(() => {
    fetchData();
});

// ===== DUPLICATE CHECK FUNCTIONS =====
// Hàm kiểm tra trùng lặp
function checkDuplicate() {
    // Reset lỗi trước khi kiểm tra
    duplicateErrors.value = {
        tenKhuyenMai: ''
    };

    if (!khuyenMai.value.tenKhuyenMai) {
        return;
    }

    // Chỉ kiểm tra trùng tên khuyến mãi
    if (khuyenMai.value.tenKhuyenMai) {
        const existingTen = khuyenMais.value.find(item => 
            item.tenKhuyenMai.toLowerCase().trim() === khuyenMai.value.tenKhuyenMai.toLowerCase().trim() && 
            item.id !== khuyenMai.value.id
        );
        if (existingTen) {
            duplicateErrors.value.tenKhuyenMai = 'Tên khuyến mãi đã tồn tại';
        }
    }
}

// Hàm kiểm tra validation trước khi lưu
function validateKhuyenMaiDuplicates() {
    // Kiểm tra trùng lặp
    checkDuplicate();
    
    // Kiểm tra có lỗi trùng lặp không
    const hasDuplicateError = duplicateErrors.value.tenKhuyenMai;
    
    return !hasDuplicateError;
}

// Helper functions for formatting and calculations
function formatPercentage(value) {
    if (value == null || value === undefined || isNaN(value)) {
        return '0';
    }
    return Number(value).toFixed(1);
}

function calculateDiscountedPrice(originalPrice, discountPercent) {
    if (!originalPrice || !discountPercent) return originalPrice || 0;
    return originalPrice * (1 - discountPercent / 100);
}

function calculateSavings(originalPrice, discountPercent) {
    if (!originalPrice || !discountPercent) return 0;
    return originalPrice * (discountPercent / 100);
}

// Helper functions for promotion status based on dates
function getPromotionStatus(promotion) {
    if (!promotion) return 'disabled';
    
    // If manually disabled
    if (promotion.trangThai === 0) return 'disabled';
    
    const now = new Date();
    const startDate = new Date(promotion.ngayBatDau);
    const endDate = new Date(promotion.ngayKetThuc);
    
    if (now < startDate) {
        return 'pending'; // Chưa diễn ra
    } else if (now >= startDate && now <= endDate) {
        return 'active'; // Đang diễn ra
    } else {
        return 'expired'; // Đã hết hạn
    }
}

function getPromotionStatusText(promotion) {
    const status = getPromotionStatus(promotion);
    switch (status) {
        case 'pending':
            return 'Chưa diễn ra';
        case 'active':
            return 'Đang diễn ra';
        case 'expired':
            return 'Đã hết hạn';
        case 'disabled':
            return 'Không hoạt động';
        default:
            return 'Không xác định';
    }
}

function handleAddKhuyenMaiConfirm() {
  saveKhuyenMai();              // gọi API thêm khuyến mãi
  confirmAddDialog.value = false; // tắt dialog confirm
}
function handleUpdateKhuyenMaiConfirm() {
  editKhuyenMai();              // gọi API cập nhật khuyến mãi
  confirmUpdateDialog.value = false; // tắt dialog confirm
}

function getPromotionStatusSeverity(promotion) {
    const status = getPromotionStatus(promotion);
    switch (status) {
        case 'pending':
            return 'info';
        case 'active':
            return 'success';
        case 'expired':
            return 'danger';
        case 'disabled':
            return 'secondary';
        default:
            return 'secondary';
    }
}

function getPromotionStatusIcon(promotion) {
    const status = getPromotionStatus(promotion);
    switch (status) {
        case 'pending':
            return 'pi pi-clock';
        case 'active':
            return 'pi pi-check-circle';
        case 'expired':
            return 'pi pi-times-circle';
        case 'disabled':
            return 'pi pi-ban';
        default:
            return 'pi pi-question-circle';
    }
}

// Computed properties
const filteredKhuyenMai = computed(() => {
    let filtered = khuyenMais.value;

    // Lọc theo trạng thái - chỉ lọc khi có giá trị
    if (statusFilter.value && statusFilter.value !== '') {
        filtered = filtered.filter((km) => {
            const status = getPromotionStatus(km);
            return status === statusFilter.value;
        });
    }

    return filtered;
});

// ✅ THÊM FUNCTION MỚI
function onStatusFilterChange() {
    // Function này có thể để trống hoặc thêm logic xử lý nếu cần
    console.log('Status filter changed to:', statusFilter.value);
}

function clearAllFilters() {
    statusFilter.value = null; // Đặt về null thay vì ''
    filters.value.global.value = null;
    
    toast.add({
        severity: 'info',
        summary: 'Thông báo',
        detail: 'Đã xóa tất cả bộ lọc',
        life: 2000
    });
}

const isEndDateBeforeStartDate = computed(() => {
    if (!khuyenMai.value.ngayBatDau || !khuyenMai.value.ngayKetThuc) return false;
    return new Date(khuyenMai.value.ngayKetThuc) <= new Date(khuyenMai.value.ngayBatDau);
});

// Calculate totals based on giaGoc and giaBan
const totalOriginalValue = computed(() => {
    return promotionProducts.value.reduce((sum, item) => {
        return sum + (item.chiTietSanPham?.giaGoc || 0);
    }, 0);
});

const totalDiscountedValue = computed(() => {
    return promotionProducts.value.reduce((sum, item) => {
        return sum + (item.chiTietSanPham?.giaBan || 0);
    }, 0);
});

const totalPromotionValue = computed(() => {
    return totalOriginalValue.value - totalDiscountedValue.value;
});

// Hàm tính toán số thứ tự với pagination
function getRowIndex(index) {
    // Lấy thông tin pagination từ DataTable
    const currentPage = dt.value ? dt.value.d_first / dt.value.d_rows : 0;
    const rowsPerPage = dt.value ? dt.value.d_rows : 10;
    return currentPage * rowsPerPage + index + 1;
}
// Main data fetching
async function fetchData() {
    isLoading.value = true;
    try {
        // Kiểm tra và reset giá cho các khuyến mãi không active trước
        await checkAndResetAllInactivePrices();
        
        const res = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/khuyen-mai`);
        khuyenMais.value = res.data;
    } catch (error) {
        console.error('Error fetching data:', error.response?.data || error.message);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: error.response?.data?.message || 'Có lỗi xảy ra khi tải dữ liệu',
            life: 3000
        });
    } finally {
        isLoading.value = false;
    }
}

// Apply products functions
async function openApplyProductsDialog(promotion) {
    const status = getPromotionStatus(promotion);
    if (status !== 'active') {
        let message = '';
        switch (status) {
            case 'pending':
                message = 'Khuyến mãi chưa bắt đầu, không thể áp dụng cho sản phẩm';
                break;
            case 'expired':
                message = 'Khuyến mãi đã hết hạn, không thể áp dụng cho sản phẩm';
                break;
            case 'disabled':
                message = 'Khuyến mãi không hoạt động, không thể áp dụng cho sản phẩm';
                break;
            default:
                message = 'Khuyến mãi không khả dụng, không thể áp dụng cho sản phẩm';
        }
        toast.add({
            severity: 'warn',
            summary: 'Cảnh báo',
            detail: message,
            life: 3000
        });
        return;
    }
    
    selectedPromotionForApply.value = promotion;
    applyProductsDialog.value = true;
    await loadAvailableProducts();
}

async function loadAvailableProducts() {
    isLoadingProducts.value = true;
    try {
        const res = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/khuyen-mai-chi-tiet/products/available`);
        const data = Array.isArray(res.data) ? res.data : [];
        availableProducts.value = data.filter((p) => p?.sanPham?.id != null);
        selectedProductsForApply.value = [];
        productSearchKeyword.value = '';
    } catch (error) {
        console.error('Error loading products:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Không thể tải danh sách sản phẩm',
            life: 3000
        });
    } finally {
        isLoadingProducts.value = false;
    }
}

function debounceSearch() {
    if (searchTimeout) {
        clearTimeout(searchTimeout);
    }
    searchTimeout = setTimeout(() => {
        searchProducts();
    }, 500);
}

async function searchProducts() {
    if (!productSearchKeyword.value.trim()) {
        await loadAvailableProducts();
        return;
    }

    isLoadingProducts.value = true;
    try {
        const res = await axios.get(`http://localhost:8080/khuyen-mai-chi-tiet/products/search?keyword=${productSearchKeyword.value}`);
        const data = Array.isArray(res.data) ? res.data : [];
        availableProducts.value = data.filter((product) => {
            const hasProduct = product?.sanPham?.id != null;
            return hasProduct && product.trangThai === 1 && product.giaGoc > 0;
        });
    } catch (error) {
        console.error('Error searching products:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Không thể tìm kiếm sản phẩm',
            life: 3000
        });
    } finally {
        isLoadingProducts.value = false;
    }
}

function clearSearch() {
    productSearchKeyword.value = '';
    loadAvailableProducts();
}

function selectAllProducts() {
    selectedProductsForApply.value = [...availableProducts.value];
}

function clearSelectedProducts() {
    selectedProductsForApply.value = [];
}

async function applyPromotionToProducts() {
    if (!selectedProductsForApply.value || selectedProductsForApply.value.length === 0) {
        toast.add({
            severity: 'warn',
            summary: 'Cảnh báo',
            detail: 'Vui lòng chọn ít nhất một sản phẩm',
            life: 3000
        });
        return;
    }

    // Check if promotion is still valid
    const status = getPromotionStatus(selectedPromotionForApply.value);
    if (status !== 'active') {
        let message = '';
        switch (status) {
            case 'pending':
                message = 'Khuyến mãi chưa bắt đầu, không thể áp dụng cho sản phẩm';
                break;
            case 'expired':
                message = 'Khuyến mãi đã hết hạn, không thể áp dụng cho sản phẩm';
                break;
            case 'disabled':
                message = 'Khuyến mãi không hoạt động, không thể áp dụng cho sản phẩm';
                break;
            default:
                message = 'Khuyến mãi không khả dụng, không thể áp dụng cho sản phẩm';
        }
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: message,
            life: 3000
        });
        return;
    }

    isApplyingPromotion.value = true;
    try {
        const request = {
            khuyenMaiId: selectedPromotionForApply.value.id,
            chiTietSanPhamIds: selectedProductsForApply.value.map((p) => p.id)
        };

        await axios.post(`${import.meta.env.VITE_API_BASE_URL}/khuyen-mai-chi-tiet/apply`, request);

        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: `Đã áp dụng khuyến mãi cho ${selectedProductsForApply.value.length} sản phẩm. Giá bán đã được cập nhật tự động.`,
            life: 5000
        });

        applyProductsDialog.value = false;
        await fetchData(); // Refresh promotion list to update product count
    } catch (error) {
        console.error('Error applying promotion:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: error.response?.data?.message || 'Có lỗi xảy ra khi áp dụng khuyến mãi',
            life: 3000
        });
    } finally {
        isApplyingPromotion.value = false;
    }
}

// Function để reset giá bán về giá gốc cho các sản phẩm của khuyến mãi không active
async function resetPricesForInactivePromotion(promotionId) {
    try {
        await axios.put(`http://localhost:8080/khuyen-mai-chi-tiet/${promotionId}/reset-prices`);
    } catch (error) {
        console.error('Error resetting prices:', error);
    }
}

// Function để kiểm tra và reset giá cho tất cả khuyến mãi không active  
async function checkAndResetAllInactivePrices() {
    try {
        await axios.put(`${import.meta.env.VITE_API_BASE_URL}/khuyen-mai-chi-tiet/reset-all-inactive-prices`);
    } catch (error) {
        console.error('Error resetting all inactive prices:', error);
    }
}

// View products functions
async function viewPromotionProducts(promotion) {
    selectedPromotionForView.value = promotion;
    
    // Kiểm tra trạng thái khuyến mãi và reset giá nếu cần
    const status = getPromotionStatus(promotion);
    if (status !== 'active') {
        await resetPricesForInactivePromotion(promotion.id);
        await fetchData(); // Refresh dữ liệu khuyến mãi
    }
    
    viewProductsDialog.value = true;
    await loadPromotionProducts(promotion.id);
}

async function loadPromotionProducts(promotionId) {
    isLoadingPromotionProducts.value = true;
    try {
        const res = await axios.get(`http://localhost:8080/khuyen-mai-chi-tiet/${promotionId}`);
        const raw = Array.isArray(res.data) ? res.data : [];
        promotionProducts.value = raw.filter((item) => item?.chiTietSanPham?.sanPham?.id != null);
        
        // Kiểm tra trạng thái khuyến mãi và đảm bảo giá bán được hiển thị đúng
        const promotion = selectedPromotionForView.value;
        const status = getPromotionStatus(promotion);
        
        if (status !== 'active') {
            // Nếu khuyến mãi không active, đảm bảo giá bán = giá gốc trong hiển thị
            promotionProducts.value = promotionProducts.value.map(item => ({
                ...item,
                chiTietSanPham: {
                    ...item.chiTietSanPham,
                    giaBan: item.chiTietSanPham.giaGoc
                }
            }));
        }
    } catch (error) {
        console.error('Error loading promotion products:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Không thể tải danh sách sản phẩm của khuyến mãi',
            life: 3000
        })
    } finally {
        isLoadingPromotionProducts.value = false;
    }
}

function confirmRemoveProductFromPromotion(promotionDetail) {
    selectedProductForRemove.value = promotionDetail;
    removeProductDialog.value = true;
}

async function removeProductFromPromotion() {
    try {
        await axios.delete(`http://localhost:8080/khuyen-mai-chi-tiet/${selectedProductForRemove.value.khuyenMai.id}/product/${selectedProductForRemove.value.chiTietSanPham.id}`);

        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: 'Đã hủy áp dụng khuyến mãi cho sản phẩm. Giá đã được khôi phục về giá gốc.',
            life: 3000
        });

        removeProductDialog.value = false;
        await loadPromotionProducts(selectedPromotionForView.value.id);
        await fetchData(); // Refresh promotion list
    } catch (error) {
        console.error('Error removing product from promotion:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Có lỗi xảy ra khi hủy áp dụng khuyến mãi',
            life: 3000
        });
    }
}

function openApplyProductsFromView() {
    const status = getPromotionStatus(selectedPromotionForView.value);
    if (status !== 'active') {
        let message = '';
        switch (status) {
            case 'pending':
                message = 'Khuyến mãi chưa bắt đầu, không thể áp dụng thêm sản phẩm';
                break;
            case 'expired':
                message = 'Khuyến mãi đã hết hạn, không thể áp dụng thêm sản phẩm';
                break;
            case 'disabled':
                message = 'Khuyến mãi không hoạt động, không thể áp dụng thêm sản phẩm';
                break;
            default:
                message = 'Khuyến mãi không khả dụng, không thể áp dụng thêm sản phẩm';
        }
        toast.add({
            severity: 'warn',
            summary: 'Cảnh báo',
            detail: message,
            life: 3000
        });
        return;
    }
    
    viewProductsDialog.value = false;
    openApplyProductsDialog(selectedPromotionForView.value);
}

// Date and currency formatting
function formatDateDisplay(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatDateForBackend(date) {
    if (!date) return null;
    const dateObj = date instanceof Date ? date : new Date(date);

    // Format as "yyyy-MM-dd HH:mm:ss"
    const year = dateObj.getFullYear();
    const month = String(dateObj.getMonth() + 1).padStart(2, '0');
    const day = String(dateObj.getDate()).padStart(2, '0');
    const hours = String(dateObj.getHours()).padStart(2, '0');
    const minutes = String(dateObj.getMinutes()).padStart(2, '0');
    const seconds = String(dateObj.getSeconds()).padStart(2, '0');

    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

// CRUD operations
function openNew() {
    khuyenMai.value = {
        tenKhuyenMai: '',
        maKhuyenMai: createId(),
        giaTri: null,
        ngayBatDau: null,
        ngayKetThuc: null,
        trangThai: 1
    };
    submitted.value = false;
    // Reset lỗi trùng lặp
    duplicateErrors.value = {
        tenKhuyenMai: ''
    };
    khuyenMaiDialog.value = true;
}

function hideDialog() {
    khuyenMaiDialog.value = false;
    submitted.value = false;
    // Reset lỗi trùng lặp
    duplicateErrors.value = {
        tenKhuyenMai: ''
    };
}

// Thêm hàm validation chi tiết
function validateKhuyenMaiForm() {
    const errors = [];
    
    // Kiểm tra tên khuyến mãi
    if (!khuyenMai.value.tenKhuyenMai || !khuyenMai.value.tenKhuyenMai.trim()) {
        errors.push('Tên khuyến mãi là bắt buộc');
    }
    
    // Kiểm tra giá trị giảm - cập nhật validation
    if (khuyenMai.value.giaTri == null || khuyenMai.value.giaTri === '') {
        errors.push('Giá trị giảm là bắt buộc');
    } else if (isNaN(khuyenMai.value.giaTri)) {
        errors.push('Giá trị giảm phải là số hợp lệ');
    } else if (Number(khuyenMai.value.giaTri) <= 0) {
        errors.push('Giá trị giảm phải lớn hơn 0');
    } else if (Number(khuyenMai.value.giaTri) > 100) {
        errors.push('Giá trị giảm không được vượt quá 100%');
    }
    
    // Kiểm tra ngày bắt đầu
    if (!khuyenMai.value.ngayBatDau) {
        errors.push('Ngày bắt đầu là bắt buộc');
    }
    
    // Kiểm tra ngày kết thúc
    if (!khuyenMai.value.ngayKetThuc) {
        errors.push('Ngày kết thúc là bắt buộc');
    }
    
    // Kiểm tra ngày kết thúc phải sau ngày bắt đầu
    if (khuyenMai.value.ngayBatDau && khuyenMai.value.ngayKetThuc && isEndDateBeforeStartDate.value) {
        errors.push('Ngày kết thúc phải sau ngày bắt đầu');
    }
    
    // Kiểm tra trạng thái
    if (khuyenMai.value.trangThai == null) {
        errors.push('Trạng thái hoạt động là bắt buộc');
    }
    
    return errors;
}

// Cập nhật hàm saveKhuyenMai
async function saveKhuyenMai() {
    submitted.value = true;

    // Kiểm tra validation form
    const formErrors = validateKhuyenMaiForm();
    
    // Kiểm tra trùng lặp
    if (!validateKhuyenMaiDuplicates()) {
        formErrors.push('Tên khuyến mãi đã tồn tại');
    }

    // Nếu có lỗi, hiển thị thông báo chi tiết
    if (formErrors.length > 0) {
        const errorMessage = formErrors.length === 1 
            ? formErrors[0] 
            : `Vui lòng kiểm tra lại:\n• ${formErrors.join('\n• ')}`;
            
        toast.add({
            severity: 'warn',
            summary: 'Vui lòng kiểm tra thông tin',
            detail: errorMessage,
            life: 5000
        });
        return;
    }

    // Nếu không có lỗi, tiến hành lưu
    isSaving.value = true;
    try {
        const requestData = {
            ...khuyenMai.value,
            maKhuyenMai: khuyenMai.value.maKhuyenMai || createId(),
            ngayBatDau: formatDateForBackend(khuyenMai.value.ngayBatDau),
            ngayKetThuc: formatDateForBackend(khuyenMai.value.ngayKetThuc)
        };

        if (khuyenMai.value.id) {
            await axios.put(`http://localhost:8080/khuyen-mai/${khuyenMai.value.id}`, requestData);
            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: 'Cập nhật khuyến mãi thành công. Giá sản phẩm đã được tính lại tự động.',
                life: 3000
            });
        } else {
            await axios.post(`${import.meta.env.VITE_API_BASE_URL}/khuyen-mai`, requestData);
            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: 'Thêm khuyến mãi thành công',
                life: 3000
            });
        }
        await fetchData();
        khuyenMaiDialog.value = false;
        khuyenMai.value = {};
    } catch (error) {
        console.error('Error saving promotion:', error.response?.data || error.message);
        
        let errorMessage = 'Có lỗi xảy ra khi lưu khuyến mãi';
        if (error.response?.data?.message) {
            errorMessage = error.response.data.message;
        } else if (error.response?.status === 409) {
            errorMessage = 'Dữ liệu bị trùng lặp';
        }
        
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: errorMessage,
            life: 3000
        });
    } finally {
        isSaving.value = false;
    }
}

// Thêm validation real-time cho các trường input
function validateField(fieldName) {
    switch (fieldName) {
        case 'tenKhuyenMai':
            if (khuyenMai.value.tenKhuyenMai && khuyenMai.value.tenKhuyenMai.trim()) {
                checkDuplicate();
            }
            break;
        case 'giaTri':
            // Validation được xử lý real-time bởi InputNumber
            // Có thể thêm validation custom nếu cần
            if (submitted.value) {
                // Force re-validate khi user đang trong trạng thái đã submit
                const errors = validateKhuyenMaiForm();
            }
            break;
        case 'ngayBatDau':
        case 'ngayKetThuc':
            // Validation cho ngày sẽ được xử lý trong computed property
            break;
    }
}

function editKhuyenMai(km) {
    khuyenMai.value = { ...km };

    if (khuyenMai.value.ngayBatDau) {
        khuyenMai.value.ngayBatDau = new Date(khuyenMai.value.ngayBatDau);
    }
    if (khuyenMai.value.ngayKetThuc) {
        khuyenMai.value.ngayKetThuc = new Date(khuyenMai.value.ngayKetThuc);
    }
    
    // Reset lỗi trùng lặp khi edit
    duplicateErrors.value = {
        tenKhuyenMai: ''
    };
    
    khuyenMaiDialog.value = true;
}

function confirmDeleteKhuyenMai(km) {
    khuyenMai.value = km;
    deleteKhuyenMaiDialog.value = true;
}

async function deleteKhuyenMai() {
    try {
        await axios.delete(`http://localhost:8080/khuyen-mai/${khuyenMai.value.id}`);
        await fetchData();
        deleteKhuyenMaiDialog.value = false;
        khuyenMai.value = {};
        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: 'Xóa khuyến mãi thành công.',
            life: 3000
        });
    } catch (error) {
        console.error('Error deleting promotion:', error.response?.data || error.message);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: error.response?.data?.message || 'Có lỗi xảy ra khi xóa khuyến mãi',
            life: 3000
        });
    }
}

async function changeStatus(km) {
    try {
        await axios.put(`http://localhost:8080/khuyen-mai/${km.id}/change-status`);
        await fetchData();
        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: `Đã ${km.trangThai === 1 ? 'kích hoạt' : 'ngừng hoạt động'} khuyến mãi`,
            life: 3000
        });
    } catch (error) {
        console.error('Error changing status:', error.response?.data || error.message);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: error.response?.data?.message || 'Có lỗi xảy ra khi thay đổi trạng thái',
            life: 3000
        });
    }
}

function confirmDeleteSelected() {
    deleteKhuyenMaisDialog.value = true;
}

async function deleteSelectedKhuyenMais() {
    try {
        for (const km of selectedKhuyenMai.value) {
            await axios.delete(`http://localhost:8080/khuyen-mai/${km.id}`);
        }
        await fetchData();
        deleteKhuyenMaisDialog.value = false;
        selectedKhuyenMai.value = null;
        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: 'Xóa các khuyến mãi thành công.',
            life: 3000
        });
    } catch (error) {
        console.error('Error deleting promotions:', error.response?.data || error.message);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: error.response?.data?.message || 'Có lỗi xảy ra khi xóa khuyến mãi',
            life: 3000
        });
    }
}

function exportCSV() {
    try {
        if (!khuyenMais.value || khuyenMais.value.length === 0) {
            toast.add({
                severity: 'warn',
                summary: 'Cảnh báo',
                detail: 'Không có dữ liệu để xuất',
                life: 3000
            });
            return;
        }

        const headers = ['STT', 'Mã Khuyến Mãi', 'Tên Khuyến Mãi', 'Giá Trị Giảm (%)', 'Ngày Bắt Đầu', 'Ngày Kết Thúc', 'Trạng Thái', 'Số Lượng Sản Phẩm'];

        const csvData = khuyenMais.value.map((item , index) => {
            return [
                // item.id || '',
                index + 1 ,
                item.maKhuyenMai || '',
                item.tenKhuyenMai || '',
                formatPercentage(item.giaTri) + '%',
                formatDateDisplay(item.ngayBatDau),
                formatDateDisplay(item.ngayKetThuc),
                getPromotionStatusText(item),
                item.soLuongSanPham || 0
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
            const filename = `KhuyenMai-${dateStr}.csv`;

            link.setAttribute('download', filename);
            link.style.visibility = 'hidden';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);

            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: `Đã xuất ${khuyenMais.value.length} bản ghi ra file CSV`,
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

function createId() {
    let id = '';
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    for (let i = 0; i < 8; i++) {
        id += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return 'KM' + id;
}
</script>
