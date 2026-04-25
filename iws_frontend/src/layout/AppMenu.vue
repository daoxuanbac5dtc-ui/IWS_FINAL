<script setup>
import { ref } from 'vue';

import AppMenuItem from './AppMenuItem.vue';
const model = ref([
    {
        label: 'Test',
        items: [
            {
                 label: 'Dashboard', icon: 'pi pi-fw pi-chart-line', to: '/dashboard'
            },
            {
                label: 'Bán Hàng Tại Quầy',
                icon: 'pi pi-fw pi-shopping-cart',
                to: '/ban-hang'
            },
            {
                label: 'Quản lý sản phẩm',
                icon: 'pi pi-fw pi-box',
                items: [
                    {
                        label: 'Sản Phẩm',
                        icon: 'pi pi-fw pi-shopping-bag',
                        to: '/san-pham'
                    },
                    {
                        label: 'Chất liệu',
                        icon: 'pi pi-fw pi-th-large',
                        to: '/chat-lieu'
                    },
                    {
                        label: 'Thương hiệu',
                        icon: 'pi pi-fw pi-star',
                        to: '/thuong-hieu'
                    },
                    {
                        label: 'Màu sắc',
                        icon: 'pi pi-fw pi-palette',
                        to: '/mau-sac'
                    },
                    {
                        label: 'Kích cỡ',
                        icon: 'pi pi-fw pi-expand',
                        to: '/kich-co'
                    },
                    {
                        label: 'Danh mục',
                        icon: 'pi pi-fw pi-list',
                        to: '/danh-muc'
                    },
                    {
                        label: 'Đế giày',
                        icon: 'pi pi-fw pi-shield',
                        to: 'de-giay'
                    },
                    {
                        label: 'Hình ảnh',
                        icon: 'pi pi-fw pi-image',
                        to: 'hinh-anh'
                    }
                ]
            },
            {
                label: 'Quản lý tài khoản',
                icon: 'pi pi-fw pi-users',
                items: [
                    {
                        label: 'Khách Hàng',
                        icon: 'pi pi-fw pi-user-plus',
                        to: '/khach-hang'
                    },
                    {
                        label: 'Nhân viên',
                        icon: 'pi pi-fw pi-id-card',
                        to: '/nhan-vien'
                    },
                    {
                        label: 'Tài khoản',
                        icon: 'pi pi-fw pi-user-edit',
                        to: '/tai-khoan'
                    }
                ]
            },
            {
                label: 'Quản lý khuyến mãi',
                icon: 'pi pi-fw pi-tags',
                items: [
                    {
                        label: 'Khuyến mãi',
                        icon: 'pi pi-fw pi-percentage',
                        to: '/khuyen-mai'
                    },
                    {
                        label: 'Voucher',
                        icon: 'pi pi-fw pi-ticket',
                        to: '/voucher'
                    }
                ]
            },
            {
                label: 'Hóa đơn',
                icon: 'pi pi-fw pi-receipt',
                to: '/hoa-don'
            },
            {
                label: 'Trả hàng',
                icon: 'pi pi-fw pi-undo',
                to: '/tra-hang'
            }
        ]
    }
]);
</script>

<template>
    <ul class="layout-menu">
        <template v-for="(item, i) in model" :key="item">
            <app-menu-item v-if="!item.separator" :item="item" :index="i"></app-menu-item>
            <li v-if="item.separator" class="menu-separator"></li>
        </template>
    </ul>
</template>

<style lang="scss" scoped></style>
