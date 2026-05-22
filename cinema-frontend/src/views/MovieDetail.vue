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

function normalizeDate(dateStr) {
  if (!dateStr) return dateStr
  const parts = dateStr.split('-')
  if (parts.length === 3) {
    return parts[0] + '-' + parts[1].padStart(2, '0') + '-' + parts[2].padStart(2, '0')
  }
  return dateStr
}

async function loadShowtimes(movieId) {
  try {
    const res = await api.get(`/showtimes?movieId=${movieId}`)
    if (res.data.success) {
      showtimeData.value = res.data.data || []
      const dSet = new Set()
      showtimeData.value.forEach(s => dSet.add(normalizeDate(s.showDate)))
      dates.value = [...dSet].sort()
      if (dates.value.length) currentDate.value = dates.value[0]
    }
  } catch (e) { console.error(e) }
}

function isShowtimePast(st) {
  if (!st || !st.showDate || !st.showTime) return false
  const dt = new Date(st.showDate + 'T' + st.showTime + ':00')
  return dt < new Date()
}

function selectShowtime(st) {
  if (isShowtimePast(st)) return
  cart.currentMovieId = movie.value.id
  cart.currentShowtimeId = st.id
  cart.currentHallId = st.hallId
  cart.reset()
  router.push(`/seats/${st.id}`)
}

function groupedShowtimes() {
  const filtered = showtimeData.value.filter(s => normalizeDate(s.showDate) === currentDate.value)
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

function formatDateSimple(d) {
  if (!d) return ''
  const parts = d.split('-')
  if (parts.length === 3) {
    return parts[1] + '/' + parts[2]
  }
  return d
}

function getDateLabel(d) {
  if (!d) return ''
  const parts = d.split('-')
  if (parts.length === 3) {
    const dt = new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]))
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    dt.setHours(0, 0, 0, 0)
    const diff = Math.floor((dt - today) / (1000 * 60 * 60 * 24))
    if (diff === 0) return '今天'
    if (diff === 1) return '明天'
    const w = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return w[dt.getDay()]
  }
  return d
}
</script>

<template>
  <div v-if="movie" class="detail-page">
    <!-- Top Banner -->
    <div class="top-banner">
      <div class="banner-bg" :style="{ background: `url(${movie.posterUrl}) center/cover` }"></div>
      <div class="banner-overlay"></div>
      <div class="banner-content">
        <div class="poster-card">
          <div class="poster-inner" :style="{ background: `url(${movie.posterUrl}) center/cover` }">
            <div v-if="movie.status === 'hot'" class="tag hot">热映</div>
            <div v-else-if="movie.status === 'coming'" class="tag coming">即将上映</div>
          </div>
        </div>
        <div class="info-card">
          <h1 class="movie-title">{{ movie.title }}</h1>
          <div class="rating-row">
            <div class="rating">
              <span class="star">★</span>
              <span class="score">{{ (movie.rating || 0).toFixed(1) }}</span>
            </div>
            <span class="rating-label">猫眼评分</span>
          </div>
          <div class="meta-tags">
            <span v-if="movie.genre" class="meta-tag">{{ movie.genre }}</span>
            <span v-if="movie.duration" class="meta-tag">{{ movie.duration }}分钟</span>
            <span v-if="movie.language" class="meta-tag">{{ movie.language }}</span>
            <span v-if="movie.country" class="meta-tag">{{ movie.country }}</span>
          </div>
          <div class="crew-info">
            <span class="crew-item">导演：{{ movie.director }}</span>
            <span class="crew-item">主演：{{ movie.cast }}</span>
          </div>
          <p class="synopsis">{{ movie.description }}</p>
        </div>
      </div>
    </div>

    <!-- Booking Panel -->
    <div class="booking-panel">
      <div class="panel-header">
        <h3 class="panel-title">购票</h3>
        <div class="date-selector">
          <div 
            v-for="d in dates" 
            :key="d" 
            :class="['date-chip', { active: d === currentDate }]"
            @click="currentDate = d"
          >
            <span class="chip-day">{{ getDateLabel(d) }}</span>
            <span class="chip-date">{{ formatDateSimple(d) }}</span>
          </div>
        </div>
      </div>

      <div class="hall-sections">
        <div v-for="(shows, hallName) in groupedShowtimes()" :key="hallName" class="hall-section">
          <div class="hall-header">
            <span class="hall-name">{{ hallName }}</span>
            <span class="hall-format">中文2D</span>
          </div>
          <div class="showtime-grid">
            <div
              v-for="s in shows.sort((a,b) => a.showTime.localeCompare(b.showTime))"
              :key="s.id"
              :class="['time-card', { 'time-card--past': isShowtimePast(s) }]"
              @click="selectShowtime(s)"
            >
              <div class="time-display">{{ s.showTime }}</div>
              <div class="price-display">
                <span class="currency">¥</span>
                <span class="amount">{{ s.priceStandard.toFixed(1) }}</span>
              </div>
              <div class="buy-btn">{{ isShowtimePast(s) ? '已过场' : '购票' }}</div>
            </div>
          </div>
        </div>

        <div v-if="!Object.keys(groupedShowtimes()).length" class="empty-state">
          <div class="empty-icon">🎬</div>
          <p>该日期暂无场次</p>
          <p class="empty-hint">请选择其他日期</p>
        </div>
      </div>
    </div>

    <!-- Reviews Section -->
    <div class="reviews-panel">
      <ReviewSection :movieId="movie.id" :showForm="auth.isLoggedIn && !auth.isAdmin" />
    </div>
  </div>
</template>

<style scoped>
.detail-page {
  background: #f5f5f5;
  min-height: 100vh;
}

/* Top Banner */
.top-banner {
  position: relative;
  min-height: 420px;
  overflow: hidden;
}
.banner-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  filter: blur(25px);
  transform: scale(1.05);
}
.banner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to bottom, rgba(0,0,0,0.7) 0%, rgba(0,0,0,0.4) 60%, rgba(245,245,245,1) 100%);
}
.banner-content {
  position: relative;
  z-index: 1;
  max-width: 1000px;
  margin: 0 auto;
  padding: 40px 24px;
  display: flex;
  gap: 36px;
}

/* Poster Card */
.poster-card {
  flex-shrink: 0;
  width: 180px;
  height: 252px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0,0,0,0.4);
}
.poster-inner {
  width: 100%;
  height: 100%;
  position: relative;
}
.tag {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 14px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
}
.tag.hot {
  background: linear-gradient(135deg, #e54847, #c83f3a);
}
.tag.coming {
  background: rgba(0,0,0,0.6);
}

/* Info Card */
.info-card {
  flex: 1;
  color: #fff;
  padding-top: 8px;
}
.movie-title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 16px;
  letter-spacing: 1px;
}
.rating-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.rating {
  display: flex;
  align-items: baseline;
  gap: 4px;
}
.star {
  font-size: 24px;
  color: #f9b418;
}
.score {
  font-size: 32px;
  font-weight: 700;
  color: #f9b418;
}
.rating-label {
  font-size: 14px;
  color: rgba(255,255,255,0.7);
}
.meta-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
}
.meta-tag {
  padding: 4px 12px;
  background: rgba(255,255,255,0.15);
  border-radius: 4px;
  font-size: 13px;
}
.crew-info {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  margin-bottom: 16px;
  font-size: 13px;
  color: rgba(255,255,255,0.8);
}
.crew-item {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.synopsis {
  font-size: 14px;
  line-height: 1.7;
  color: rgba(255,255,255,0.85);
  max-width: 500px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Booking Panel */
.booking-panel {
  background: #fff;
  margin: -20px 24px 24px;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.08);
  position: relative;
  z-index: 2;
}
.panel-header {
  margin-bottom: 20px;
}
.panel-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 16px;
}
.date-selector {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.date-chip {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 24px;
  background: #fafafa;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
}
.date-chip:hover {
  background: #f0f0f0;
}
.date-chip.active {
  background: linear-gradient(135deg, #e54847, #c83f3a);
  color: #fff;
  border-color: #e54847;
}
.chip-day {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}
.chip-date {
  font-size: 12px;
  opacity: 0.8;
}

/* Hall Sections */
.hall-sections {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.hall-section {
  padding: 20px;
  background: #fafafa;
  border-radius: 12px;
}
.hall-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}
.hall-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
}
.hall-format {
  font-size: 12px;
  color: #999;
  padding: 2px 8px;
  background: #fff;
  border-radius: 4px;
}
.showtime-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}
.time-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
}
.time-card:hover {
  border-color: #e54847;
  transform: translateY(-3px);
  box-shadow: 0 4px 16px rgba(229,72,71,0.15);
}
.time-card--past {
  opacity: 0.4;
  cursor: not-allowed;
  pointer-events: none;
}
.time-card--past .buy-btn {
  background: #999;
}
.time-display {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 8px;
}
.price-display {
  display: flex;
  justify-content: center;
  align-items: baseline;
  margin-bottom: 12px;
}
.currency {
  font-size: 14px;
  color: #e54847;
}
.amount {
  font-size: 22px;
  font-weight: 700;
  color: #e54847;
}
.buy-btn {
  display: inline-block;
  padding: 8px 28px;
  background: linear-gradient(135deg, #e54847, #c83f3a);
  color: #fff;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 48px 24px;
  background: #fafafa;
  border-radius: 12px;
}
.empty-icon {
  font-size: 56px;
  margin-bottom: 16px;
}
.empty-state p {
  font-size: 15px;
  color: #666;
  margin-bottom: 8px;
}
.empty-hint {
  font-size: 13px;
  color: #999 !important;
}

/* Reviews Panel */
.reviews-panel {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 24px 40px;
}

/* Responsive */
@media (max-width: 768px) {
  .banner-content {
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 32px 16px;
  }
  .poster-card {
    width: 150px;
    height: 210px;
  }
  .info-card {
    padding-top: 20px;
  }
  .movie-title {
    font-size: 26px;
  }
  .meta-tags {
    justify-content: center;
  }
  .crew-info {
    justify-content: center;
  }
  .booking-panel {
    margin: -10px 12px 16px;
    padding: 20px 16px;
  }
  .date-selector {
    justify-content: center;
  }
  .showtime-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  }
  .time-card {
    padding: 14px 12px;
  }
}
</style>
