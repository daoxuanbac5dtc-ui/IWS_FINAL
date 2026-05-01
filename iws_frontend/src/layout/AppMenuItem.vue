<script setup>
import { useLayout } from '@/layout/composables/layout';
import { onBeforeMount, ref, watch } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();

const { layoutState, setActiveMenuItem, toggleMenu } = useLayout();

const props = defineProps({
    item: {
        type: Object,
        default: () => ({})
    },
    index: {
        type: Number,
        default: 0
    },
    root: {
        type: Boolean,
        default: true
    },
    parentItemKey: {
        type: String,
        default: null
    }
});

const isActiveMenu = ref(false);
const itemKey = ref(null);

function normalizeMenuPath(path) {
    if (typeof path !== 'string' || !path.length) {
        return path;
    }

    if (path.startsWith('/') || path.startsWith('#') || /^[a-z]+:/i.test(path)) {
        return path;
    }

    return `/${path}`;
}

function matchesRoute(path) {
    const normalizedPath = normalizeMenuPath(path);
    return typeof normalizedPath === 'string' && route.path === normalizedPath;
}

function itemMatchesCurrentRoute(item) {
    if (!item) {
        return false;
    }

    if (matchesRoute(item.to)) {
        return true;
    }

    return Array.isArray(item.items) && item.items.some((child) => itemMatchesCurrentRoute(child));
}

function syncActiveState(activeItem = layoutState.activeMenuItem) {
    const keyMatch = typeof activeItem === 'string' && (activeItem === itemKey.value || activeItem.startsWith(itemKey.value + '-'));
    isActiveMenu.value = itemMatchesCurrentRoute(props.item) || keyMatch;
}

onBeforeMount(() => {
    itemKey.value = props.parentItemKey ? props.parentItemKey + '-' + props.index : String(props.index);
    syncActiveState();
});

watch(
    () => layoutState.activeMenuItem,
    (newVal) => {
        syncActiveState(newVal);
    }
);

watch(
    () => route.path,
    () => {
        syncActiveState();
    }
);

function itemClick(event, item) {
    if (item.disabled) {
        event.preventDefault();
        return;
    }

    if ((item.to || item.url) && (layoutState.staticMenuMobileActive || layoutState.overlayMenuActive)) {
        toggleMenu();
    }

    if (item.command) {
        item.command({ originalEvent: event, item: item });
    }

    const foundItemKey = item.items ? (isActiveMenu.value ? props.parentItemKey : itemKey) : itemKey.value;

    setActiveMenuItem(foundItemKey);
}

function resolveRouteTarget(item) {
    return normalizeMenuPath(item?.to);
}

function checkActiveRoute(item) {
    return matchesRoute(item?.to);
}
</script>

<template>
    <li :class="{ 'layout-root-menuitem': root, 'active-menuitem': isActiveMenu }">
        <div v-if="root && item.visible !== false" class="layout-menuitem-root-text">{{ item.label }}</div>
        <a v-if="(!item.to || item.items) && item.visible !== false" :href="item.url" @click="itemClick($event, item, index)" :class="item.class" :target="item.target" tabindex="0">
            <i :class="item.icon" class="layout-menuitem-icon"></i>
            <span class="layout-menuitem-text">{{ item.label }}</span>
            <i class="pi pi-fw pi-angle-down layout-submenu-toggler" v-if="item.items"></i>
        </a>
        <router-link
            v-if="item.to && !item.items && item.visible !== false"
            @click="itemClick($event, item, index)"
            :class="[item.class, { 'active-route': checkActiveRoute(item) }]"
            tabindex="0"
            :to="resolveRouteTarget(item)"
        >
            <i :class="item.icon" class="layout-menuitem-icon"></i>
            <span class="layout-menuitem-text">{{ item.label }}</span>
            <i class="pi pi-fw pi-angle-down layout-submenu-toggler" v-if="item.items"></i>
        </router-link>
        <Transition v-if="item.items && item.visible !== false" name="layout-submenu">
            <ul v-show="root ? true : isActiveMenu" class="layout-submenu">
                <app-menu-item v-for="(child, i) in item.items" :key="child" :index="i" :item="child" :parentItemKey="itemKey" :root="false"></app-menu-item>
            </ul>
        </Transition>
    </li>
</template>

<style lang="scss" scoped></style>
