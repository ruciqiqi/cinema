<script setup>
import { provide, ref } from 'vue'
import AppHeader from './components/AppHeader.vue'
import ToastNotification from './components/ToastNotification.vue'
import PageLoader from './components/PageLoader.vue'

const toastRef = ref(null)
provide('toast', (msg, type) => {
  toastRef.value?.addToast(msg, type)
})
</script>

<template>
  <PageLoader />
  <AppHeader />
  <main class="main">
    <router-view />
  </main>
  <ToastNotification ref="toastRef" />
</template>
