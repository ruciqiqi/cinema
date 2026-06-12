import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import ToastNotification from '@/components/ToastNotification.vue'
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'

describe('ToastNotification.vue', () => {
  let host

  beforeEach(async () => {
    vi.useFakeTimers()
    host = document.createElement('div')
    document.body.appendChild(host)
  })

  afterEach(() => {
    vi.useRealTimers()
    if (host && host.parentNode) {
      host.remove()
    }
    // 清理 body 中残留的 toast-container
    document.querySelectorAll('.toast-container').forEach(el => el.remove())
  })

  // ==================== FC-09 初始状态无toast ====================
  it('starts with no toasts', () => {
    const wrapper = mount(ToastNotification, {
      attachTo: host
    })

    expect(wrapper.vm.toasts).toEqual([])
    expect(document.querySelectorAll('.toast-item').length).toBe(0)
  })

  // ==================== FC-10 添加 success toast ====================
  it('adds a success toast via addToast', async () => {
    const wrapper = mount(ToastNotification, {
      attachTo: host
    })

    wrapper.vm.addToast('操作成功', 'success')
    await nextTick()

    expect(wrapper.vm.toasts.length).toBe(1)
    expect(wrapper.vm.toasts[0].msg).toBe('操作成功')
    expect(wrapper.vm.toasts[0].type).toBe('success')
    const item = document.querySelector('.toast-item')
    expect(item).toBeTruthy()
    expect(item.classList.contains('success')).toBe(true)
  })

  // ==================== FC-11 添加 error toast ====================
  it('adds an error toast', async () => {
    const wrapper = mount(ToastNotification, {
      attachTo: host
    })

    wrapper.vm.addToast('出错了', 'error')
    await nextTick()

    expect(wrapper.vm.toasts.length).toBe(1)
    expect(wrapper.vm.toasts[0].type).toBe('error')
    expect(document.querySelector('.toast-item').classList.contains('error')).toBe(true)
  })

  // ==================== FC-12 添加 info toast ====================
  it('adds an info toast', async () => {
    const wrapper = mount(ToastNotification, {
      attachTo: host
    })

    wrapper.vm.addToast('提示信息', 'info')
    await nextTick()

    expect(wrapper.vm.toasts[0].type).toBe('info')
    expect(document.querySelector('.toast-item').classList.contains('info')).toBe(true)
  })

  // ==================== FC-13 添加多个toast ====================
  it('adds multiple toasts', async () => {
    const wrapper = mount(ToastNotification, {
      attachTo: host
    })

    wrapper.vm.addToast('第一个', 'success')
    wrapper.vm.addToast('第二个', 'error')
    wrapper.vm.addToast('第三个', 'info')
    await nextTick()

    expect(wrapper.vm.toasts.length).toBe(3)
    expect(document.querySelectorAll('.toast-item').length).toBe(3)
  })

  // ==================== FC-14 toast自动移除 ====================
  it('removes toast after 3 seconds', async () => {
    const wrapper = mount(ToastNotification, {
      attachTo: host
    })

    wrapper.vm.addToast('临时消息', 'success')
    await nextTick()
    expect(wrapper.vm.toasts.length).toBe(1)

    vi.advanceTimersByTime(3000)
    await nextTick()
    expect(wrapper.vm.toasts[0].leaving).toBe(true)

    vi.advanceTimersByTime(300)
    await nextTick()
    expect(wrapper.vm.toasts.length).toBe(0)
  })

  // ==================== FC-15 toast id唯一递增 ====================
  it('generates unique ids for each toast', async () => {
    const wrapper = mount(ToastNotification, {
      attachTo: host
    })

    wrapper.vm.addToast('A', 'success')
    wrapper.vm.addToast('B', 'success')
    wrapper.vm.addToast('C', 'success')
    await nextTick()

    const ids = wrapper.vm.toasts.map(t => t.id)
    expect(new Set(ids).size).toBe(3)
    expect(ids).toEqual(ids.sort())
  })

  // ==================== FC-16 expose addToast方法 ====================
  it('exposes addToast method via defineExpose', () => {
    const wrapper = mount(ToastNotification, {
      attachTo: host
    })

    expect(typeof wrapper.vm.addToast).toBe('function')
    expect(() => wrapper.vm.addToast('test', 'success')).not.toThrow()
  })

  // ==================== FC-17 toast显示消息内容 ====================
  it('displays correct message text', async () => {
    const wrapper = mount(ToastNotification, {
      attachTo: host
    })

    wrapper.vm.addToast('我的消息内容', 'success')
    await nextTick()

    expect(document.querySelector('.toast-msg').textContent).toBe('我的消息内容')
  })

  // ==================== FC-18 多个toast顺序正确 ====================
  it('maintains order of toasts by insertion', async () => {
    const wrapper = mount(ToastNotification, {
      attachTo: host
    })

    wrapper.vm.addToast('First', 'success')
    wrapper.vm.addToast('Second', 'success')
    await nextTick()

    const items = document.querySelectorAll('.toast-msg')
    expect(items[0].textContent).toBe('First')
    expect(items[1].textContent).toBe('Second')
  })
})
