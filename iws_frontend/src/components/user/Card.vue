<script setup>
import { defineProps, defineEmits } from "vue";

defineProps({
  imgUrl: {
    type: String,
    required: true
  },
  isActive: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(["change-hero-img"]);

const onClick = (imgUrl) => {
  emit("change-hero-img", imgUrl);
};
</script>

<template>
  <button
    class="shoe-card"
    :class="{ 'shoe-card--active': isActive }"
    type="button"
    @click="onClick(imgUrl)"
    aria-label="Chọn hình sản phẩm"
  >
    <span class="shoe-card__glow"></span>
    <img class="shoe-card__image" :src="imgUrl" alt="Shoes collection" />
  </button>
</template>

<style lang="scss" scoped>
.shoe-card {
  position: relative;
  z-index: 10;
  width: 138px;
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 2px solid rgba(255, 255, 255, 0.78);
  border-radius: 18px;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.52), rgba(255, 255, 255, 0.12)),
    url('@/assets/images/thumbnail-background.svg') center / cover no-repeat,
    #dfe3fb;
  box-shadow: 0 16px 34px rgba(30, 41, 59, 0.16);
  cursor: pointer;
  transition:
    border-color 0.25s ease,
    box-shadow 0.25s ease,
    transform 0.25s ease;
}

.shoe-card::after {
  content: '';
  position: absolute;
  inset: 10px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.28);
  opacity: 0;
  transition: opacity 0.25s ease;
}

.shoe-card:hover {
  transform: translateY(-6px);
  border-color: rgba(255, 100, 82, 0.75);
  box-shadow: 0 22px 42px rgba(30, 41, 59, 0.2);
}

.shoe-card:hover::after,
.shoe-card--active::after {
  opacity: 1;
}

.shoe-card--active {
  border-color: #ff6452;
  box-shadow:
    0 18px 38px rgba(255, 100, 82, 0.22),
    0 0 0 4px rgba(255, 100, 82, 0.12);
}

.shoe-card__glow {
  position: absolute;
  width: 82%;
  height: 36%;
  bottom: 18px;
  border-radius: 999px;
  background: radial-gradient(circle, rgba(255, 100, 82, 0.18), transparent 68%);
  filter: blur(10px);
}

.shoe-card__image {
  position: relative;
  z-index: 2;
  width: 92%;
  height: 92%;
  object-fit: contain;
  mix-blend-mode: multiply;
  filter: drop-shadow(0 14px 18px rgba(15, 23, 42, 0.18));
  transform: scale(1.08) rotate(-7deg);
  transition: transform 0.25s ease;
}

.shoe-card:hover .shoe-card__image,
.shoe-card--active .shoe-card__image {
  transform: scale(1.18) rotate(-7deg);
}

@media (max-width: 768px) {
  .shoe-card {
    width: 116px;
    border-radius: 16px;
  }
}
</style>
