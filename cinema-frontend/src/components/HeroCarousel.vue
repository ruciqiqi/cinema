<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  movies: { type: Array, required: true },
})

const router = useRouter()
const current = ref(0)
const isPaused = ref(false)
let timer = null

const total = computed(() => props.movies.length)

function goTo(idx) {
  current.value = ((idx % total.value) + total.value) % total.value
}

function next() { goTo(current.value + 1) }
function prev() { goTo(current.value - 1) }

function startAuto() {
  stopAuto()
  if (total.value <= 1) return
  timer = setInterval(() => {
    if (!isPaused.value) next()
  }, 4000)
}

function stopAuto() {
  if (timer) { clearInterval(timer); timer = null }
}

onMounted(startAuto)
onBeforeUnmount(stopAuto)

function goMovie(id) {
  router.push(`/movie/${id}`)
}
</script>

<template>
  <div
    v-if="movies.length"
    class="hero-carousel"
    @mouseenter="isPaused = true"
    @mouseleave="isPaused = false"
  >
    <!-- slides -->
    <div class="hero-track">
      <TransitionGroup name="carousel">
        <div
          v-for="(m, i) in movies"
          :key="m.id"
          v-show="i === current"
          class="hero-slide"
          :style="{
            background: m.posterUrl
              ? `url(${m.posterUrl}) center/cover no-repeat, linear-gradient(135deg, ${m.posterBg || '#1a1a3e'}, #0d0d20)`
              : `linear-gradient(135deg, ${m.posterBg || '#1a1a3e'}, #0d0d20)`,
          }"
        >
          <div class="hero-gradient" />
          <div class="hero-content">
            <h2 class="hero-title">{{ m.title }}</h2>
            <div class="hero-badges">
              <span v-if="m.genre" class="hero-badge">{{ m.genre }}</span>
              <span v-if="m.rating" class="hero-badge hero-badge-rating">{{ m.rating }}</span>
              <span v-if="m.duration" class="hero-badge">{{ m.duration }}分钟</span>
            </div>
            <p v-if="m.description" class="hero-desc">{{ m.description }}</p>
            <button class="hero-btn" @click="goMovie(m.id)">立即购票</button>
          </div>
        </div>
      </TransitionGroup>
    </div>

    <!-- arrows -->
    <button v-if="total > 1" class="hero-arrow hero-arrow-left" @click="prev">&#8249;</button>
    <button v-if="total > 1" class="hero-arrow hero-arrow-right" @click="next">&#8250;</button>

    <!-- dots -->
    <div v-if="total > 1" class="hero-dots">
      <button
        v-for="(m, i) in movies"
        :key="'dot' + m.id"
        :class="['hero-dot', { active: i === current }]"
        @click="goTo(i)"
      />
    </div>
  </div>
</template>

<style scoped>
.hero-carousel {
  position: relative;
  width: 100%;
  border-radius: var(--radius);
  overflow: hidden;
  background: var(--surface);
  box-shadow: 0 8px 32px rgba(0,0,0,.4);
}

.hero-track {
  position: relative;
  width: 100%;
  padding-top: 40%; /* 2.5:1 aspect */
}

.hero-slide {
  position: absolute;
  inset: 0;
  transition: opacity .6s ease;
}

.hero-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    90deg,
    rgba(0,0,0,.7) 0%,
    rgba(0,0,0,.35) 40%,
    rgba(0,0,0,.15) 70%,
    rgba(0,0,0,.4) 100%
  );
}

.hero-content {
  position: absolute;
  left: 8%;
  top: 50%;
  transform: translateY(-50%);
  max-width: 480px;
  z-index: 2;
}

.hero-title {
  font-size: 42px;
  font-weight: 800;
  color: #fff;
  margin: 0 0 16px;
  text-shadow: 0 4px 20px rgba(0,0,0,.5);
  letter-spacing: 1px;
  line-height: 1.2;
}

.hero-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 14px;
}

.hero-badge {
  padding: 5px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  background: rgba(255,255,255,.15);
  backdrop-filter: blur(6px);
}
.hero-badge-rating {
  background: rgba(255, 215, 0, .85);
  color: #333;
  font-weight: 700;
}

.hero-desc {
  font-size: 15px;
  color: rgba(255,255,255,.75);
  line-height: 1.6;
  margin-bottom: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hero-btn {
  padding: 12px 36px;
  border: none;
  border-radius: 28px;
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, var(--primary), var(--primary2));
  cursor: pointer;
  transition: all .25s;
  box-shadow: 0 4px 18px rgba(255,107,107,.5);
}
.hero-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(255,107,107,.6);
}

/* arrows */
.hero-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 5;
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 50%;
  background: rgba(0,0,0,.45);
  backdrop-filter: blur(8px);
  color: #fff;
  font-size: 28px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all .2s;
}
.hero-arrow:hover { background: rgba(0,0,0,.7); }
.hero-arrow-left  { left: 16px; }
.hero-arrow-right { right: 16px; }

/* dots */
.hero-dots {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 10px;
  z-index: 5;
}
.hero-dot {
  width: 10px;
  height: 10px;
  border: none;
  border-radius: 50%;
  background: rgba(255,255,255,.35);
  cursor: pointer;
  transition: all .25s;
}
.hero-dot.active {
  background: var(--primary);
  box-shadow: 0 0 8px rgba(255,107,107,.6);
  transform: scale(1.25);
}

/* responsive */
@media (max-width: 768px) {
  .hero-track { padding-top: 56%; }
  .hero-content { left: 5%; max-width: 85%; }
  .hero-title { font-size: 24px; }
  .hero-btn { padding: 10px 24px; font-size: 14px; }
  .hero-arrow { width: 32px; height: 32px; font-size: 20px; }
}
</style>
