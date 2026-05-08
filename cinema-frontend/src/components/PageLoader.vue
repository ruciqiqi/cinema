<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const width = ref(0)
let timer = null

function start() {
  loading.value = true
  width.value = 0
  timer = setInterval(() => {
    if (width.value < 90) width.value += (90 - width.value) * 0.15 + 2
  }, 80)
}

function done() {
  width.value = 100
  clearInterval(timer)
  setTimeout(() => {
    loading.value = false
    width.value = 0
  }, 200)
}

onMounted(() => {
  router.beforeEach((to, from, next) => {
    if (from.name && to.name !== from.name) start()
    next()
  })
  router.afterEach(() => done())
})

onBeforeUnmount(() => { clearInterval(timer) })
</script>

<template>
  <div v-if="loading" class="loader-bar" :style="{ width: width + '%' }"></div>
</template>

<style scoped>
.loader-bar {
  position: fixed;
  top: 0;
  left: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--primary), var(--gold));
  z-index: 10001;
  transition: width .2s ease;
}
</style>
