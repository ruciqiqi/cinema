import { mount } from '@vue/test-utils'
import MovieCard from '@/components/MovieCard.vue'
import { createRouter, createWebHistory } from 'vue-router'
import { describe, it, expect } from 'vitest'

describe('MovieCard.vue', () => {
  const movie = {
    id: 1,
    title: 'Test Movie',
    posterUrl: 'https://example.com/poster.jpg',
    genre: 'Action',
    duration: 120,
    rating: 8.5
  }

  it('renders movie information correctly', () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/movie/:id', name: 'MovieDetail', component: { template: '<div></div>' } }
      ]
    })

    const wrapper = mount(MovieCard, {
      props: { movie },
      global: {
        plugins: [router]
      }
    })

    expect(wrapper.find('.card-title').text()).toBe('Test Movie')
    expect(wrapper.find('.card-genre').text()).toBe('Action')
    expect(wrapper.find('.card-rating').text()).toBe('8.5')
    expect(wrapper.find('.card-meta span').text()).toBe('120分钟')
  })

  it('triggers navigation when clicked', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/movie/:id', name: 'MovieDetail', component: { template: '<div></div>' } }
      ]
    })

    const pushSpy = vi.spyOn(router, 'push')

    const wrapper = mount(MovieCard, {
      props: { movie },
      global: {
        plugins: [router]
      }
    })

    await wrapper.trigger('click')
    
    expect(pushSpy).toHaveBeenCalledWith('/movie/1')
  })
})