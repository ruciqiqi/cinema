<script setup>
import { ref, shallowRef, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'
import HeroCarousel from '../components/HeroCarousel.vue'
import MovieCard from '../components/MovieCard.vue'

const router = useRouter()
const toast = inject('toast')
const movies = shallowRef([])
const comingMovies = shallowRef([])
const allMovies = shallowRef([])
const currentGenre = ref('all')
const currentDate = ref('all')
const genres = ref([])
const showDates = ref([])
const showtimeMap = ref({})
const searchKeyword = ref('')
const announcements = ref([])
const heroMovies = shallowRef([])

onMounted(async () => {
  try {
    const [movieRes, annRes, showRes] = await Promise.all([
      api.get('/movies'),
      api.get('/announcements'),
      api.get('/showtimes')
    ])
    if (movieRes.data.success) {
      const all = movieRes.data.data
      const showing = all.filter(m => m.status !== 'coming')
      comingMovies.value = all.filter(m => m.status === 'coming')
      allMovies.value = showing
      movies.value = showing
      heroMovies.value = showing.filter(m => m.isHot || parseFloat(m.rating) >= 9.0)
      if (!heroMovies.value.length) heroMovies.value = showing.slice(0, 5)
      extractGenres(showing)
    }
    if (annRes.data.success) announcements.value = annRes.data.data
    if (showRes.data.success) {
      buildShowtimeMap(showRes.data.data || [])
    }
    filterMovies()
  } catch (e) { console.error(e) }
})

function buildShowtimeMap(showtimes) {
  const map = {}
  const dates = new Set()
  showtimes.forEach(s => {
    if (!map[s.showDate]) map[s.showDate] = new Set()
    map[s.showDate].add(s.movieId)
    dates.add(s.showDate)
  })
  showtimeMap.value = map
  showDates.value = [...dates].sort()
}

function formatDateLabel(d) {
  if (!d) return d
  const parts = d.split('-')
  if (parts.length === 3) {
    const dt = new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]))
    const today = new Date()
    const diff = Math.floor((dt - today) / (1000 * 60 * 60 * 24))
    if (diff === 0) return '今天'
    if (diff === 1) return '明天'
    if (diff === 2) return '后天'
    return parts[1] + '月' + parts[2] + '日'
  }
  return d
}

function extractGenres(list) {
  const gSet = new Set()
  list.forEach(m => {
    if (m.genre) m.genre.split('/').forEach(g => gSet.add(g.trim()))
  })
  genres.value = [...gSet].sort()
}

function filterMovies() {
  let filtered = allMovies.value
  if (currentGenre.value !== 'all') {
    filtered = filtered.filter(m => m.genre && m.genre.includes(currentGenre.value))
  }
  if (currentDate.value !== 'all') {
    const dateMovieIds = showtimeMap.value[currentDate.value]
    if (dateMovieIds) {
      filtered = filtered.filter(m => dateMovieIds.has(m.id))
    } else {
      filtered = []
    }
  }
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(m => m.title.toLowerCase().includes(kw))
  }
  movies.value = [...filtered]
}

function setGenre(genre) {
  currentGenre.value = genre
  filterMovies()
}

function setDate(date) {
  currentDate.value = date
  filterMovies()
}

function search() { filterMovies() }
function goDetail(id) { router.push(`/movie/${id}`) }
</script>

<template>
  <!-- Hero Carousel -->
  <HeroCarousel v-if="heroMovies.length" :movies="heroMovies" @select="goDetail" />

  <!-- Announcements -->
  <div v-if="announcements.length" class="announce-bar">
    <div class="marquee">
      <span v-for="a in announcements" :key="a.id">
        【{{ a.title }}】{{ a.content }} &nbsp;&nbsp;|&nbsp;&nbsp;
      </span>
    </div>
  </div>

  <!-- Date Filter -->
  <div v-if="showDates.length" class="filter-bar">
    <span class="filter-label">上映时间：</span>
    <button :class="['filter-tag', { active: currentDate === 'all' }]" @click="setDate('all')">全部</button>
    <button v-for="d in showDates" :key="d" :class="['filter-tag', { active: currentDate === d }]" @click="setDate(d)">{{ formatDateLabel(d) }}</button>
  </div>

  <!-- Genre Filter -->
  <div class="filter-bar">
    <span class="filter-label">类型筛选：</span>
    <button :class="['filter-tag', { active: currentGenre === 'all' }]" @click="setGenre('all')">全部</button>
    <button v-for="g in genres" :key="g" :class="['filter-tag', { active: currentGenre === g }]" @click="setGenre(g)">{{ g }}</button>
  </div>

  <!-- Search -->
  <div class="search-bar">
    <input v-model="searchKeyword" placeholder="搜索影片名称..." @keyup.enter="search">
    <button class="btn btn-primary" @click="search">搜索</button>
  </div>

  <!-- Movie Grid -->
  <div class="movie-grid">
    <MovieCard v-for="m in movies" :key="m.id" :movie="m" @click="goDetail(m.id)" />
  </div>
  <p v-if="!movies.length" style="color:var(--text3);text-align:center;padding:40px;">暂无影片</p>

  <!-- Coming Soon -->
  <template v-if="comingMovies.length">
    <div class="section-divider">
      <h2 class="section-title">即将上映</h2>
      <span class="section-sub">{{ comingMovies.length }} 部影片即将上映</span>
    </div>
    <div class="movie-grid coming-grid">
      <MovieCard v-for="m in comingMovies" :key="m.id" :movie="m" @click="goDetail(m.id)" />
    </div>
  </template>
</template>

<style scoped>
.announce-bar { background: var(--surface); border: 1px solid var(--border); border-radius: 8px; padding: 10px 16px; margin-bottom: 16px; overflow: hidden; white-space: nowrap; }
.marquee { display: inline-block; animation: marquee 20s linear infinite; font-size: 13px; color: var(--text2); }
@keyframes marquee { from { transform: translateX(100%); } to { transform: translateX(-100%); } }
.filter-bar { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; }
.filter-label { color: var(--text3); font-size: 14px; }
.filter-tag { padding: 6px 16px; border: 1px solid var(--border); border-radius: 16px; cursor: pointer; font-size: 13px; background: var(--surface); color: var(--text2); transition: all .2s; }
.filter-tag:hover, .filter-tag.active { background: var(--primary); border-color: var(--primary); color: #fff; }
.search-bar { display: flex; gap: 12px; margin-bottom: 20px; }
.search-bar input { flex: 1; }
.movie-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 20px; }
.coming-grid { opacity: .85; }
.section-divider { display: flex; align-items: baseline; gap: 16px; margin: 40px 0 20px; padding-top: 24px; border-top: 1px solid var(--border); }
.section-title { font-size: 22px; font-weight: 700; color: var(--text); }
.section-sub { font-size: 13px; color: var(--text3); }
@media(max-width:768px) { .movie-grid { grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 12px; } }
</style>
