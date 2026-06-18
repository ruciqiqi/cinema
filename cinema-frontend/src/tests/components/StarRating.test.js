import { mount } from '@vue/test-utils'
import StarRating from '@/components/StarRating.vue'
import { describe, it, expect } from 'vitest'

describe('StarRating.vue', () => {
  // ==================== FC-01 初始渲染 - 0分 ====================
  it('renders 10 stars with default 0 rating', () => {
    const wrapper = mount(StarRating, {
      props: { modelValue: 0 }
    })

    const stars = wrapper.findAll('.star')
    expect(stars.length).toBe(10)
    expect(wrapper.find('.star-rating').exists()).toBe(true)
    expect(wrapper.find('.star-value').text()).toContain('0 / 10')
  })

  // ==================== FC-02 渲染 - 高评分 ====================
  it('renders active stars for high rating', () => {
    const wrapper = mount(StarRating, {
      props: { modelValue: 8 }
    })

    const activeStars = wrapper.findAll('.star--active')
    expect(activeStars.length).toBe(8)
    expect(wrapper.find('.star-value').text()).toContain('8 / 10')
  })

  // ==================== FC-03 渲染 - 满分 ====================
  it('renders all 10 stars active for max rating', () => {
    const wrapper = mount(StarRating, {
      props: { modelValue: 10 }
    })

    const activeStars = wrapper.findAll('.star--active')
    expect(activeStars.length).toBe(10)
    expect(wrapper.find('.star-value').text()).toContain('10 / 10')
  })

  // ==================== FC-04 点击更新评分 ====================
  it('emits update event when a star is clicked', async () => {
    const wrapper = mount(StarRating, {
      props: { modelValue: 0 }
    })

    const stars = wrapper.findAll('.star')
    await stars[4].trigger('click') // 点击第5颗星

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')[0]).toEqual([5])
  })

  // ==================== FC-05 点击不同位置 ====================
  it('emits correct value when different star is clicked', async () => {
    const wrapper = mount(StarRating, {
      props: { modelValue: 3 }
    })

    const stars = wrapper.findAll('.star')
    await stars[9].trigger('click') // 点击第10颗星

    expect(wrapper.emitted('update:modelValue')[0]).toEqual([10])
  })

  // ==================== FC-06 hoverRating 高亮 ====================
  it('highlights stars on mouse enter', async () => {
    const wrapper = mount(StarRating, {
      props: { modelValue: 3 }
    })

    const stars = wrapper.findAll('.star')
    await stars[6].trigger('mouseenter') // 悬停在第7颗星

    // displayRating 使用 hoverRating
    expect(wrapper.vm.hoverRating).toBe(7)
  })

  // ==================== FC-07 鼠标离开重置 hoverRating ====================
  it('resets hover rating on mouse leave', async () => {
    const wrapper = mount(StarRating, {
      props: { modelValue: 5 }
    })

    const stars = wrapper.findAll('.star')
    await stars[3].trigger('mouseenter')
    expect(wrapper.vm.hoverRating).toBe(4)

    await wrapper.trigger('mouseleave')
    expect(wrapper.vm.hoverRating).toBe(0)
  })

  // ==================== FC-08 星号显示正确符号 ====================
  it('shows filled star character for active stars', () => {
    const wrapper = mount(StarRating, {
      props: { modelValue: 5 }
    })

    const stars = wrapper.findAll('.star')
    expect(stars[0].text()).toBe('★') // 第1颗星填充
    expect(stars[9].text()).toBe('☆') // 第10颗星空
  })
})
