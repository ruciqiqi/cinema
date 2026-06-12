import { mount } from '@vue/test-utils'
import ToastNotification from '@/components/ToastNotification.vue'
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'

describe('ToastNotification.vue', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  // ==================== FC-09 初始状态无toast ====================
  it('starts with no toasts', () => {
    const wrapper = mount(ToastNotification, {
      global: { stubs: { teleport: { template: '<div><slot></slot></div>' } } }
    })

    expect(wrapper.vm.toasts).toEqual([])
    expect(wrapper.findAll('.toast-item').length).toBe(0)
  })

  // ==================== FC-10 添加 success toast ====================
  it('adds a success toast via addToast', () => {
    const wrapper = mount(ToastNotification, {
      global: { stubs: { teleport: { template: '<div><slot></slot></div>' } } }
    )

    wrapper.vm.addToast('操作成功', 'success')

    expect(wrapper.vm.toasts.length).toBe(1)
    expect(wrapper.vm.toasts[0].msg).toBe('操作成功')
    expect(wrapper.vm.toasts[0].type).toBe('success')
    expect(wrapper.find('.toast-item').exists()).toBe(true)
    expect(wrapper.find('.toast-item').classes()).toContain('success')
  })

  // ==================== FC-11 添加 error toast ====================
  it('adds an error toast', () => {
    const wrapper = mount(ToastNotification, {
      global: { stubs: { teleport: { template: '<div><slot></slot></div>' } } }
    )

    wrapper.vm.addToast('出错了', 'error')

    expect(wrapper.vm.toasts.length).toBe(1)
    expect(wrapper.vm.toasts[0].type).toBe('error')
    expect(wrapper.find('.toast-item').classes()).toContain('error')
  })

  // ==================== FC-12 添加 info toast ====================
  it('adds an info toast', () => {
    const wrapper = mount(ToastNotification, {
      global: { stubs: { teleport: { template: '<div><slot></slot></div>' } } }
    )

    wrapper.vm.addToast('提示信息', 'info')

    expect(wrapper.vm.toasts[0].type).toBe('info')
    expect(wrapper.find('.toast-item').classes()).toContain('info')
  })

  // ==================== FC-13 添加多个toast ====================
  it('adds multiple toasts', () => {
    const wrapper = mount(ToastNotification, {
      global: { stubs: { teleport: { template: '<div><slot></slot></div>' } } }
    )

    wrapper.vm.addToast('第一个', 'success')
    wrapper.vm.addToast('第二个', 'error')
    wrapper.vm.addToast('第三个', 'info')

    expect(wrapper.vm.toasts.length).toBe(3)
    expect(wrapper.findAll('.toast-item').length).toBe(3)
  })

  // ==================== FC-14 toast自动移除 ====================
  it('removes toast after 3 seconds', () => {
    const wrapper = mount(ToastNotification, {
      global: { stubs: { teleport: { template: '<div><slot></slot></div>' } } }
    })

    wrapper.vm.addToast('临时消息', 'success')
    expect(wrapper.vm.toasts.length).toBe(1)

    // 3秒后开始标记为离开
    vi.advanceTimersByTime(3000)
    expect(wrapper.vm.toasts[0].leaving).toBe(true)

    // 再过300ms后完全移除
    vi.advanceTimersByTime(300)
    expect(wrapper.vm.toasts.length).toBe(0)
  })

  // ==================== FC-15 toast id唯一递增 ====================
  it('generates unique ids for each toast', () => {
    const wrapper = mount(ToastNotification, {
      global: { stubs: { teleport: { template: '<div><slot></slot></div>' } } }
    )

    wrapper.vm.addToast('A', 'success')
    wrapper.vm.addToast('B', 'success')
    wrapper.vm.addToast('C', 'success')

    const ids = wrapper.vm.toasts.map(t => t.id)
    expect(new Set(ids).size).toBe(3) // 无重复
    expect(ids).toEqual(ids.sort()) // 递增
  })

  // ==================== FC-16 expose addToast方法 ====================
  it('exposes addToast method via defineExpose', () => {
    const wrapper = mount(ToastNotification, {
      global: { stubs: { teleport: { template: '<div><slot></slot></div>' } } }
    )

    expect(typeof wrapper.vm.addToast).toBe('function')
    expect(() => wrapper.vm.addToast('test', 'success')).not.toThrow()
  })

  // ==================== FC-17 toast显示消息内容 ====================
  it('displays correct message text', () => {
    const wrapper = mount(ToastNotification, {
      global: { stubs: { teleport: { template: '<div><slot></slot></div>' } } }
    )

    wrapper.vm.addToast('我的消息内容', 'success')

    expect(wrapper.find('.toast-msg').text()).toBe('我的消息内容')
  })

  // ==================== FC-18 多个toast顺序正确 ====================
  it('maintains order of toasts by insertion', () => {
    const wrapper = mount(ToastNotification, {
      global: { stubs: { teleport: { template: '<div><slot></slot></div>' } } }
    )

    wrapper.vm.addToast('First', 'success')
    wrapper.vm.addToast('Second', 'success')

    const items = wrapper.findAll('.toast-msg')
    expect(items[0].text()).toBe('First')
    expect(items[1].text()).toBe('Second')
  })
})
