<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'
import MovieCard from '../components/MovieCard.vue'

const router = useRouter()

const movies = ref([])
const comingMovies = ref([])
const allMovies = ref([])
const currentGenre = ref('all')
const currentDate = ref('all')
const genres = ref([])
const showDates = ref([])
const showtimeMap = ref({})
const searchKeyword = ref('')
const activeTab = ref('showing') // 'showing' or 'coming'

onMounted(async () => {
  try {
    const [movieRes, showRes] = await Promise.all([
      api.get('/movies'),
      api.get('/showtimes')
    ])
    if (movieRes.data.success) {
      const all = movieRes.data.data
      allMovies.value = all.filter(m => m.status !== 'coming')
      movies.value = allMovies.value
      comingMovies.value = all.filter(m => m.status === 'coming')
      extractGenres(allMovies.value)
    }
    if (showRes.data.success) {
      buildShowtimeMap(showRes.data.data || [])
    }
  } catch (e) {
    console.error(e)
  }
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

function formatDateLabel(dateStr) {
  if (!dateStr) return dateStr
  const parts = dateStr.split('-')
  if (parts.length === 3) {
    const dt = new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]))
    const today = new Date()
    const diff = Math.floor((dt - today) / (1000 * 60 * 60 * 24))
    if (diff === 0) return '今天'
    if (diff === 1) return '明天'
    if (diff === 2) return '后天'
    return parts[1] + '月' + parts[2] + '日'
  }
  return dateStr
}

function extractGenres(list) {
  const genreSet = new Set()
  list.forEach(m => {
    if (m.genre) {
      m.genre.split('/').forEach(g => genreSet.add(g.trim()))
    }
  })
  genres.value = [...genreSet].sort()
}

function filterMovies() {
  let filtered = activeTab.value === 'showing' ? allMovies.value : comingMovies.value
  
  if (currentGenre.value !== 'all') {
    filtered = filtered.filter(m => m.genre && m.genre.includes(currentGenre.value))
  }
  
  if (currentDate.value !== 'all' && activeTab.value === 'showing') {
    const dateMovieIds = showtimeMap.value[currentDate.value]
    if (dateMovieIds) {
      filtered = filtered.filter(m => dateMovieIds.has(m.id))
    } else {
      filtered = []
    }
  }
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(m => m.title.toLowerCase().includes(keyword))
  }
  
  movies.value = filtered
}

function setGenre(genre) {
  currentGenre.value = genre
  filterMovies()
}

function setDate(date) {
  currentDate.value = date
  filterMovies()
}

function setTab(tab) {
  activeTab.value = tab
  filterMovies()
}

function goDetail(id) {
  router.push('/movie/' + id)
}
</script>

<template>
  <div class="home-page">
    <div class="main-content">
      <!-- Tabs -->
      <div class="tabs-bar">
        <button 
          :class="['tab-btn', { active: activeTab === 'showing' }]"
          @click="setTab('showing')"
        >
          正在热映
          <span class="tab-count" v-if="allMovies.length">({{ allMovies.length }})</span>
        </button>
        <button 
          :class="['tab-btn', { active: activeTab === 'coming' }]"
          @click="setTab('coming')"
        >
          即将上映
          <span class="tab-count" v-if="comingMovies.length">({{ comingMovies.length }})</span>
        </button>
      </div>

      <!-- Filters -->
      <div class="filters-wrapper" v-if="activeTab === 'showing'">
        <div class="filter-section">
          <span class="filter-label">日期：</span>
          <button 
            :class="['filter-tag', { active: currentDate === 'all' }]"
            @click="setDate('all')"
          >
            全部
          </button>
          <button 
            v-for="date in showDates"
            :key="date"
            :class="['filter-tag', { active: currentDate === date }]"
            @click="setDate(date)"
          >
            {{ formatDateLabel(date) }}
          </button>
        </div>
      </div>

      <div class="filter-section">
        <span class="filter-label">类型：</span>
        <button 
          :class="['filter-tag', { active: currentGenre === 'all' }]"
          @click="setGenre('all')"
        >
          全部
        </button>
        <button 
          v-for="g in genres"
          :key="g"
          :class="['filter-tag', { active: currentGenre === g }]"
          @click="setGenre(g)"
        >
          {{ g }}
        </button>
      </div>

      <!-- Movies Grid -->
      <div v-if="movies.length" class="movies-grid">
        <MovieCard 
          v-for="m in movies"
          :key="m.id"
          :movie="m"
          @click="goDetail(m.id)"
        />
      </div>
      <div v-else class="empty-state">
        暂无符合条件的影片
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  background: #f5f5f5;
  min-height: calc(100vh - 70px);
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px;
}

/* Tabs */
.tabs-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  border-bottom: 1px solid var(--border);
  padding-bottom: 12px;
}
.tab-btn {
  background: none;
  border: none;
  padding: 10px 20px;
  font-size: 16px;
  font-weight: 500;
  color: var(--text2);
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.15s;
  position: relative;
}
.tab-btn:hover {
  color: var(--primary);
}
.tab-btn.active {
  color: var(--text);
  font-weight: 600;
}
.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -13px;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 3px;
  background: var(--primary);
  border-radius: 2px;
}
.tab-count {
  margin-left: 6px;
  color: var(--text3);
  font-size: 14px;
}

/* Filters */
.filters-wrapper {
  margin-bottom: 16px;
}
.filter-section {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.filter-label {
  color: var(--text);
  font-size: 14px;
  font-weight: 500;
  margin-right: 8px;
}
.filter-tag {
  padding: 6px 16px;
  border: none;
  border-radius: 16px;
  background: #f5f5f5;
  color: var(--text2);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
}
.filter-tag:hover {
  background: #eee;
  color: var(--text);
}
.filter-tag.active {
  background: var(--primary);
  color: #fff;
}

/* Movies Grid */
.movies-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 20px;
  margin-top: 24px;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text3);
  font-size: 14px;
}

/* Responsive */
@media (max-width: 768px) {
  .main-content {
    padding: 16px 12px;
  }
  .movies-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 12px;
  }
}
</style>
