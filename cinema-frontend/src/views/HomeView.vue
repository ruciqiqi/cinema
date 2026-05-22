<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '../api'
import MovieCard from '../components/MovieCard.vue'

const router = useRouter()
const route = useRoute()

const movies = ref([])
const comingMovies = ref([])
const allMovies = ref([])
const currentGenre = ref('all')
const currentDate = ref('all')
const genres = ref([])
const showDates = ref([])
const showtimeMap = ref({})
const allShowtimes = ref([])
const searchKeyword = ref('')
const activeTab = ref('showing')

// Carousel state
const currentSlide = ref(0)
const slideCount = ref(0)
let autoSlideInterval = null

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
      slideCount.value = allMovies.value.length > 0 ? Math.min(allMovies.value.length, 5) : 0
      extractGenres(allMovies.value)
      startAutoSlide()
    }
    if (showRes.data.success) {
      allShowtimes.value = showRes.data.data || []
      buildShowtimeMap()
    }
    // Apply search keyword from route query after data loaded
    if (route.query.search) {
      searchKeyword.value = route.query.search
      filterMovies()
    }
  } catch (e) {
    console.error(e)
  }
})

// Watch for search keyword from AppHeader
watch(() => route.query.search, (newVal) => {
  searchKeyword.value = newVal || ''
  filterMovies()
})

function startAutoSlide() {
  autoSlideInterval = setInterval(() => {
    goToNextSlide()
  }, 4000)
}

function stopAutoSlide() {
  if (autoSlideInterval) {
    clearInterval(autoSlideInterval)
  }
}

function goToSlide(index) {
  currentSlide.value = index
}

function goToPrevSlide() {
  currentSlide.value = (currentSlide.value - 1 + slideCount.value) % slideCount.value
}

function goToNextSlide() {
  currentSlide.value = (currentSlide.value + 1) % slideCount.value
}

function goToCarouselDetail(id) {
  stopAutoSlide()
  router.push('/movie/' + id)
}

function normalizeDate(dateStr) {
  if (!dateStr) return dateStr
  const parts = dateStr.split('-')
  if (parts.length === 3) {
    return parts[0] + '-' + parts[1].padStart(2, '0') + '-' + parts[2].padStart(2, '0')
  }
  return dateStr
}

function buildShowtimeMap() {
  const showtimes = allShowtimes.value
  const map = {}
  const dates = new Set()
  showtimes.forEach(s => {
    const date = normalizeDate(s.showDate)
    if (!map[date]) map[date] = new Set()
    map[date].add(s.movieId)
    dates.add(date)
  })
  showtimeMap.value = map
  showDates.value = [...dates].sort()
}

function formatDateLabel(dateStr) {
  if (!dateStr) return dateStr
  const d = normalizeDate(dateStr)
  const parts = d.split('-')
  if (parts.length === 3) {
    const dt = new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]))
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    const diff = Math.floor((dt - today) / (1000 * 60 * 60 * 24))
    if (diff === 0) return '今天'
    if (diff === 1) return '明天'
    if (diff === 2) return '后天'
    return parts[1] + '月' + parts[2] + '日'
  }
  return d
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
    <!-- Carousel -->
    <div class="carousel-wrapper" v-if="allMovies.length > 0">
      <div 
        class="carousel-container"
        @mouseenter="stopAutoSlide"
        @mouseleave="startAutoSlide"
      >
        <div 
          class="carousel-slides"
          :style="{ transform: `translateX(-${currentSlide * 100}%)` }"
        >
          <div 
            v-for="(movie, index) in allMovies.slice(0, 5)" 
            :key="movie.id"
            class="carousel-slide"
          >
            <div 
              class="carousel-image"
              :style="{ backgroundImage: `url(${movie.posterUrl})` }"
              @click="goToCarouselDetail(movie.id)"
            >
              <div class="carousel-overlay"></div>
              <div class="carousel-content">
                <div class="carousel-poster">
                  <div class="poster-thumb" :style="{ backgroundImage: `url(${movie.posterUrl})` }"></div>
                </div>
                <div class="carousel-info">
                  <h2 class="carousel-title">{{ movie.title }}</h2>
                  <div class="carousel-meta">
                    <span class="rating">
                      <span class="star">★</span>
                      <span class="score">{{ (movie.rating || 0).toFixed(1) }}</span>
                    </span>
                    <span class="genre" v-if="movie.genre">{{ movie.genre }}</span>
                    <span class="duration" v-if="movie.duration">{{ movie.duration }}分钟</span>
                  </div>
                  <p class="carousel-desc">{{ movie.description }}</p>
                  <button class="buy-ticket-btn" @click.stop="goToCarouselDetail(movie.id)">
                    立即购票
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Navigation arrows -->
        <button class="carousel-arrow left" @click="goToPrevSlide">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="15 18 9 12 15 6"></polyline>
          </svg>
        </button>
        <button class="carousel-arrow right" @click="goToNextSlide">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9 18 15 12 9 6"></polyline>
          </svg>
        </button>
        
        <!-- Indicators -->
        <div class="carousel-indicators">
          <button
            v-for="(_, index) in allMovies.slice(0, 5)"
            :key="index"
            :class="['indicator', { active: index === currentSlide }]"
            @click="goToSlide(index)"
          ></button>
        </div>
      </div>
    </div>

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

/* Carousel */
.carousel-wrapper {
  position: relative;
  background: #1a1a1a;
  overflow: hidden;
}

.carousel-container {
  position: relative;
  max-width: 100%;
  height: 420px;
}

.carousel-slides {
  display: flex;
  width: 100%;
  height: 100%;
  transition: transform 0.5s ease;
}

.carousel-slide {
  flex: 0 0 100%;
  height: 100%;
}

.carousel-image {
  position: relative;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center 20%;
  cursor: pointer;
}

.carousel-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    to right,
    rgba(0,0,0,0.85) 0%,
    rgba(0,0,0,0.4) 50%,
    rgba(0,0,0,0.1) 100%
  );
}

.carousel-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 40px 24px;
  display: flex;
  align-items: flex-end;
  gap: 32px;
  max-width: 1200px;
  margin: 0 auto;
}

.carousel-poster {
  flex-shrink: 0;
}

.poster-thumb {
  width: 140px;
  height: 200px;
  border-radius: 8px;
  background-size: cover;
  background-position: center;
  box-shadow: 0 8px 32px rgba(0,0,0,0.4);
}

.carousel-info {
  flex: 1;
  color: #fff;
}

.carousel-title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 16px;
  letter-spacing: 1px;
}

.carousel-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.rating {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.star {
  font-size: 18px;
  color: #f9b418;
}

.score {
  font-size: 26px;
  font-weight: 700;
  color: #f9b418;
}

.genre,
.duration {
  padding: 4px 12px;
  background: rgba(255,255,255,0.15);
  border-radius: 4px;
  font-size: 13px;
}

.carousel-desc {
  font-size: 14px;
  line-height: 1.7;
  color: rgba(255,255,255,0.85);
  max-width: 500px;
  margin-bottom: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.buy-ticket-btn {
  display: inline-block;
  padding: 12px 32px;
  background: linear-gradient(135deg, #e54847, #c83f3a);
  color: #fff;
  border: none;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 16px rgba(229,72,71,0.3);
}

.buy-ticket-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(229,72,71,0.4);
}

/* Carousel arrows */
.carousel-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(255,255,255,0.15);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  transition: all 0.2s ease;
  backdrop-filter: blur(4px);
}

.carousel-arrow:hover {
  background: rgba(229,72,71,0.8);
}

.carousel-arrow.left {
  left: 24px;
}

.carousel-arrow.right {
  right: 24px;
}

.carousel-arrow svg {
  width: 24px;
  height: 24px;
}

/* Carousel indicators */
.carousel-indicators {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 10px;
}

.indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255,255,255,0.4);
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.indicator:hover {
  background: rgba(255,255,255,0.7);
}

.indicator.active {
  width: 32px;
  border-radius: 5px;
  background: #e54847;
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
  .carousel-container {
    height: 360px;
  }
  
  .carousel-content {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 20px;
    padding: 24px 16px;
  }
  
  .poster-thumb {
    width: 110px;
    height: 160px;
  }
  
  .carousel-title {
    font-size: 24px;
  }
  
  .carousel-meta {
    justify-content: center;
  }
  
  .carousel-desc {
    max-width: 100%;
    -webkit-line-clamp: 1;
  }
  
  .main-content {
    padding: 16px 12px;
  }
  .movies-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 12px;
  }
}
</style>
