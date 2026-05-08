<script setup>
import { ref, onMounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'
import api from '../api'
import ReviewSection from '../components/ReviewSection.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()
const toast = inject('toast')
const movie = ref(null)
const showtimeData = ref([])
const currentDate = ref('')
const dates = ref([])

onMounted(async () => {
  const movieId = route.params.id
  try {
    const res = await api.get(`/movies/${movieId}`)
    if (res.data.success) movie.value = res.data.data
  } catch (e) { console.error(e) }
  loadShowtimes(movieId)
})

async function loadShowtimes(movieId) {
  try {
    const res = await api.get(`/showtimes?movieId=${movieId}`)
    if (res.data.success) {
      showtimeData.value = res.data.data || []
      const dSet = new Set()
      showtimeData.value.forEach(s => dSet.add(s.showDate))
      dates.value = [...dSet].sort()
      if (dates.value.length) currentDate.value = dates.value[0]
    }
  } catch (e) { console.error(e) }
}

function selectShowtime(st) {
  cart.currentMovieId = movie.value.id
  cart.currentShowtimeId = st.id
  cart.currentHallId = st.hallId
  cart.reset()
  router.push(`/seats/${st.id}`)
}

function groupedShowtimes() {
  const filtered = showtimeData.value.filter(s => s.showDate === currentDate.value)
  const groups = {}
  filtered.forEach(s => {
    const key = s.hallName
    if (!groups[key]) groups[key] = []
    groups[key].push(s)
  })
  return groups
}

function formatDate(d) {
  if (!d) return ''
  const parts = d.split('-')
  if (parts.length === 3) {
    const dt = new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]))
    const w = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return parts[1] + '月' + parts[2] + '日 ' + w[dt.getDay()]
  }
  return d
}
</script>

<template>
  <div v-if="movie">
    <button class="btn btn-outline" @click="$router.push('/')" style="margin-bottom:16px;">&larr; 返回列表</button>
    <!-- Poster -->
    <div class="detail-poster" :style="{ background: movie.posterBg || '#1a1a2e' }">
      <img v-if="movie.posterUrl" :src="movie.posterUrl" :alt="movie.title" class="detail-poster-img" @error="e => e.target.style.display='none'">
      <div class="detail-poster-overlay"></div>
      <span class="detail-poster-title">{{ movie.title }}</span>
    </div>
    <!-- Info -->
    <div class="detail-info">
      <h2>{{ movie.title }} <span style="color:var(--gold);">★ {{ (movie.rating || 0).toFixed(1) }}</span></h2>
      <div class="meta-row">
        <span>类型: {{ movie.genre }}</span>
        <span>时长: {{ movie.duration }}分钟</span>
        <span>语言: {{ movie.language }}</span>
        <span>国家: {{ movie.country }}</span>
        <span>上映: {{ movie.releaseDate }}</span>
      </div>
      <div class="meta-row"><span>导演: {{ movie.director }}</span><span>主演: {{ movie.cast }}</span></div>
      <p class="desc">{{ movie.description }}</p>
    </div>

    <!-- Showtimes -->
    <div class="showtime-section">
      <h3>选择场次</h3>
      <div class="date-tabs" v-if="dates.length">
        <span v-for="d in dates" :key="d" :class="['date-tab', { active: d === currentDate }]" @click="currentDate = d">{{ formatDate(d) }}</span>
      </div>
      <div v-for="(shows, hallName) in groupedShowtimes()" :key="hallName" class="showtime-group">
        <h4>{{ hallName }}</h4>
        <div class="showtime-cards">
          <div v-for="s in shows.sort((a,b) => a.showTime.localeCompare(b.showTime))" :key="s.id" class="showtime-card" @click="selectShowtime(s)">
            <div class="time">{{ s.showTime }}</div>
            <div class="hall">{{ s.hallName }}</div>
            <div class="price">&yen;{{ s.priceStandard.toFixed(1) }} / VIP &yen;{{ s.priceVip.toFixed(1) }}</div>
          </div>
        </div>
      </div>
      <p v-if="!Object.keys(groupedShowtimes()).length" style="color:var(--text3);text-align:center;padding:20px;">该日期暂无场次</p>
    </div>

    <!-- Reviews -->
    <ReviewSection :movieId="movie.id" :showForm="auth.isLoggedIn && !auth.isAdmin" />
  </div>
</template>

<style scoped>
.detail-poster { height: 300px; display: flex; align-items: flex-end; padding: 30px; border-radius: var(--radius) var(--radius) 0 0; position: relative; overflow: hidden; }
.detail-poster-img { position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: cover; z-index: 0; }
.detail-poster-overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: linear-gradient(to top, rgba(0,0,0,.85) 0%, rgba(0,0,0,.2) 60%); z-index: 1; }
.detail-poster-title { font-size: 36px; font-weight: 700; color: #fff; text-shadow: 0 2px 10px rgba(0,0,0,.5); position: relative; z-index: 2; }
.detail-info { background: var(--surface); padding: 24px; border-radius: 0 0 var(--radius) var(--radius); border: 1px solid var(--border); border-top: 0; }
.detail-info h2 { font-size: 24px; margin-bottom: 8px; }
.meta-row { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 10px; font-size: 13px; color: var(--text2); }
.meta-row span { background: var(--surface2); padding: 4px 10px; border-radius: 4px; }
.desc { font-size: 14px; color: var(--text3); line-height: 1.8; }
.showtime-section { margin-top: 24px; }
.showtime-section h3 { font-size: 18px; margin-bottom: 14px; color: var(--primary); }
.date-tabs { display: flex; gap: 8px; margin-bottom: 14px; flex-wrap: wrap; }
.date-tab { padding: 8px 20px; border: 1px solid var(--border); border-radius: 20px; cursor: pointer; font-size: 14px; background: var(--surface); color: var(--text2); transition: all .2s; }
.date-tab.active { background: var(--primary); border-color: var(--primary); color: #fff; }
.date-tab:hover:not(.active) { border-color: var(--primary); color: var(--primary); }
.showtime-group { margin-bottom: 16px; }
.showtime-group h4 { font-size: 14px; color: var(--text); margin-bottom: 8px; }
.showtime-cards { display: flex; gap: 10px; flex-wrap: wrap; }
.showtime-card { background: var(--surface2); border: 1px solid var(--border); border-radius: 8px; padding: 12px 16px; cursor: pointer; transition: all .2s; min-width: 140px; }
.showtime-card:hover { border-color: var(--primary); transform: translateY(-2px); }
.showtime-card .time { font-size: 20px; font-weight: 700; color: var(--primary); }
.showtime-card .hall { font-size: 12px; color: var(--text3); }
.showtime-card .price { font-size: 11px; color: var(--gold); margin-top: 3px; }
</style>
