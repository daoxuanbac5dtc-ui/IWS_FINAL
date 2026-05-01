<template>
    <button
      class="scroll-toggler"
      @click="scrollToBottom"
      v-smooth-scroll
      type="button"
      :aria-label="to ? 'Cuộn lên đầu trang' : 'Cuộn xuống cuối trang'"
    >
      <img
        :src="arrowRight"
        :class="classRotate"
        class="transition duration-500"
        width="30"
        alt=""
      />
    </button>
  </template>

  <script setup>
  import { arrowRight } from "@/assets/icons";
  import { onMounted, onUnmounted, ref, watch } from "vue";
  const to = ref(false);
  const classRotate = ref("rotate-90");
  const scrollToBottom = () => {
    window.scrollTo({
      top: to.value ? 0 : document.body.scrollHeight,
      behavior: "smooth",
    });
    to.value = !to.value;
  };

  watch(to, () => {
    classRotate.value = to.value == true ? "reverse-rotate" : "rotate-90";
  });

  const handleScroll = () => {
    if (window.scrollY == 0) {
      to.value = false;
    } else {
      to.value = true;
    }
  };

  onMounted(() => {
    window.addEventListener("scroll", handleScroll, { passive: true });
    handleScroll();
  });

  onUnmounted(() => {
    window.removeEventListener("scroll", handleScroll);
  });
  </script>

  <style lang="scss" scoped>
  .scroll-toggler {
    position: fixed;
    right: 1.5rem;
    bottom: 6rem;
    z-index: 1090;
    width: 56px;
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px solid #ff6452;
    border-radius: 999px;
    background: #ff6452;
    color: #ffffff;
    cursor: pointer;
    box-shadow: 0 12px 28px rgba(255, 100, 82, 0.32);
    transition: transform 0.2s ease, box-shadow 0.2s ease;
  }

  .scroll-toggler:hover {
    transform: translateY(-2px);
    box-shadow: 0 16px 34px rgba(255, 100, 82, 0.38);
  }

  .scroll-toggler img {
    width: 30px;
    height: 30px;
  }

  @media (max-width: 480px) {
    .scroll-toggler {
      right: 1rem;
      bottom: 5.25rem;
      width: 50px;
      height: 50px;
    }

    .scroll-toggler img {
      width: 27px;
      height: 27px;
    }
  }
  </style>
