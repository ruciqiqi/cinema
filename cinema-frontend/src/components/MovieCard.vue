<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  movie: {
    type: Object,
    required: true
  }
})

const router = useRouter()

function goDetail() {
  router.push('/movie/' + props.movie.id)
}

const isComingSoon = computed(() => props.movie.status === 'coming')

function getPosterStyle() {
  if (props.movie.posterUrl) {
    return `url(${props.movie.posterUrl}) center/cover no-repeat`
  }
  return `linear-gradient(135deg, ${props.movie.posterBg || '#e0e0e0'}, ${props.movie.posterBg || '#d0d0d0'})`
}
</script>

<template>
  <div class="movie-card" @click="goDetail">
    <div class="card-poster">
      <div class="poster-image" :style="{ background: getPosterStyle() }">
        <div v-if="movie.isHot" class="hot-badge">热映</div>
        <div v-else-if="isComingSoon" class="coming-badge">即将上映</div>
        <div v-if="movie.rating && parseFloat(movie.rating) > 0" class="rating-badge">
          <span class="rating-value">{{ movie.rating }}</span>
        </div>
      </div>
    </div>
    <div class="card-info">
      <h3 class="card-title">{{ movie.title }}</h3>
      <div class="card-attrs" v-if="movie.genre || movie.duration">
        <span v-if="movie.genre" class="attr-item">{{ movie.genre }}</span>
        <span v-if="movie.duration" class="attr-item">{{ movie.duration }}分钟</span>
      </div>
      <div class="card-action">
        <button v-if="!isComingSoon" class="buy-btn">购票</button>
        <button v-else class="wish-btn">想看</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.movie-card {
  cursor: pointer;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.2s ease;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.movie-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
}

.card-poster {
  position: relative;
  width: 100%;
  padding-top: 140%;
  overflow: hidden;
}
.poster-image {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.hot-badge,
.coming-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 4px 8px;
  border-radius: 2px;
  font-size: 12px;
  font-weight: 600;
}
.hot-badge {
  background: var(--primary);
  color: #fff;
}
.coming-badge {
  background: #f5f5f5;
  color: var(--text2);
}

.rating-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 4px 6px;
  background: rgba(0,0,0,0.65);
  border-radius: 4px;
}
.rating-value {
  font-size: 14px;
  font-weight: 700;
  color: var(--maoyan-yellow);
}

.card-info {
  padding: 10px 12px 14px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-attrs {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.attr-item {
  font-size: 12px;
  color: var(--text3);
  background: #f5f5f5;
  padding: 3px 8px;
  border-radius: 2px;
}

.card-action {
  margin-top: 8px;
}
.buy-btn,
.wish-btn {
  width: 100%;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
}
.buy-btn {
  background: var(--primary);
  color: #fff;
}
.buy-btn:hover {
  background: var(--primary2);
}
.wish-btn {
  background: #f5f5f5;
  color: var(--text2);
}
.wish-btn:hover {
  background: #eee;
}
</style>
