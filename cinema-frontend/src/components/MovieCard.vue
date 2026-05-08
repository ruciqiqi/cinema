<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  movie: {
    type: Object,
    required: true,
    /* expected shape:
     *   id, title, posterBg, posterUrl, genre, rating,
     *   duration, language, director, isHot
     */
  },
})

const router = useRouter()

const cardStyle = computed(() => {
  const bg = props.movie.posterUrl
    ? `url(${props.movie.posterUrl}) center/cover no-repeat`
    : `linear-gradient(135deg, ${props.movie.posterBg || '#1a1a3e'}, #0d0d20)`
  return { background: bg }
})

function goDetail() {
  router.push(`/movie/${props.movie.id}`)
}
</script>

<template>
  <div class="movie-card" @click="goDetail">
    <div class="card-poster" :style="cardStyle">
      <!-- fallback gradient handled by :style -->
      <div class="card-tags">
        <span v-if="movie.genre" class="card-genre">{{ movie.genre }}</span>
        <span v-if="movie.isHot" class="card-hot">HOT</span>
      </div>
      <div v-if="movie.rating" class="card-rating">{{ movie.rating }}</div>
      <div class="card-overlay">
        <h3 class="card-title">{{ movie.title }}</h3>
        <div class="card-meta">
          <span v-if="movie.duration">{{ movie.duration }}分钟</span>
          <span v-if="movie.language">{{ movie.language }}</span>
          <span v-if="movie.director">{{ movie.director }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.movie-card {
  cursor: pointer;
  border-radius: var(--radius);
  overflow: hidden;
  background: var(--surface);
  border: 1px solid transparent;
  transition: transform .35s cubic-bezier(.21, 1.02, .73, 1), box-shadow .35s, border-color .35s;
  box-shadow: 0 4px 20px rgba(0,0,0,.25);
}
.movie-card:hover {
  transform: translateY(-8px) scale(1.03);
  box-shadow: 0 20px 50px rgba(0,0,0,.5), 0 0 0 2px var(--primary-glow);
  border-color: rgba(255,94,94,.3);
}

.card-poster {
  position: relative;
  width: 100%;
  padding-top: 140%;
  overflow: hidden;
}
.card-poster::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(transparent 35%, rgba(0,0,0,.6) 70%, rgba(0,0,0,.9) 100%);
  z-index: 1;
  pointer-events: none;
}

.card-tags {
  position: absolute;
  top: 14px;
  left: 14px;
  display: flex;
  gap: 8px;
  z-index: 3;
}

.card-genre {
  padding: 5px 12px;
  border-radius: 6px;
  font-size: 11.5px;
  font-weight: 600;
  color: #fff;
  background: rgba(255,255,255,.12);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(255,255,255,.1);
}

.card-hot {
  padding: 5px 12px;
  border-radius: 6px;
  font-size: 11.5px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, var(--primary), #d63c3c);
  box-shadow: 0 2px 12px rgba(255,94,94,.5);
  animation: hot-pulse 1.5s ease-in-out infinite;
}
@keyframes hot-pulse {
  0%, 100% { box-shadow: 0 2px 12px rgba(255,94,94,.5); }
  50% { box-shadow: 0 4px 20px rgba(255,94,94,.8); }
}

.card-rating {
  position: absolute;
  top: 14px;
  right: 14px;
  z-index: 3;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--gold), #ff9f43);
  color: #1a1a2e;
  font-size: 15px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px var(--gold-glow);
}

.card-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 18px 16px;
  z-index: 2;
}

.card-title {
  font-size: 19px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 8px;
  text-shadow: 0 2px 10px rgba(0,0,0,.6);
  letter-spacing: .5px;
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  font-size: 11.5px;
  color: rgba(255,255,255,.75);
}
.card-meta span {
  padding: 3px 9px;
  border-radius: 4px;
  background: rgba(255,255,255,.08);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}
</style>
