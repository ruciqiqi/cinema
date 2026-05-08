<template>
  <div class="star-rating" @mouseleave="hoverRating = 0">
    <span
      v-for="star in 10"
      :key="star"
      class="star"
      :class="{
        'star--active': star <= displayRating,
        'star--half': star === Math.ceil(displayRating) && displayRating % 1 !== 0
      }"
      @click="setRating(star)"
      @mouseenter="hoverRating = star"
    >
      {{ star <= displayRating ? '★' : '☆' }}
    </span>
    <span class="star-value">{{ modelValue }} / 10</span>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: Number,
    default: 0
    // 0-10
  }
})

const emit = defineEmits(['update:modelValue'])

const hoverRating = ref(0)

const displayRating = computed(() => {
  return hoverRating.value > 0 ? hoverRating.value : props.modelValue
})

function setRating(star) {
  emit('update:modelValue', star)
}
</script>

<style scoped>
.star-rating {
  display: flex;
  align-items: center;
  gap: 2px;
  user-select: none;
}

.star {
  font-size: 22px;
  color: var(--border);
  cursor: pointer;
  transition: color 0.15s ease, transform 0.1s ease;
}

.star:hover {
  transform: scale(1.15);
}

.star--active {
  color: var(--gold);
}

.star--half {
  color: var(--gold);
  opacity: 0.6;
}

.star-value {
  margin-left: 8px;
  font-size: 13px;
  color: var(--text3);
}
</style>
