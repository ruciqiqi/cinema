import axios from 'axios'
import api from '@/api'
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'

vi.mock('axios')

describe('API Module', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  afterEach(() => {
    localStorage.clear()
  })

  // ==================== FA-01 基础URL正确 ====================
  it('creates axios instance with correct baseURL', () => {
    expect(axios.create).toHaveBeenCalled()
    const callArg = axios.create.mock.calls[0][0]
    expect(callArg.baseURL).toBe('/api')
  })

  // ==================== FA-02 请求拦截器 - 有token附加Header ====================
  it('request interceptor adds Authorization header when token exists', () => {
    localStorage.setItem('cinema_token', 'test-token-123')

    // 重新加载模块以获取新的拦截器
    vi.resetModules()
    const apiFresh = require('@/api').default

    // 模拟调用
    const config = { headers: {} }
    const handler = axios.create.mock.results[0].value.interceptors.request.handlers[0].fulfilled
    const result = handler(config)

    expect(result.headers.Authorization).toBe('Bearer test-token-123')
  })

  // ==================== FA-03 请求拦截器 - 无token不附加Header ====================
  it('request interceptor does not add Authorization header when no token', () => {
    const config = { headers: {} }
    const handler = api.interceptors.request.handlers[0].fulfilled
    const result = handler(config)

    expect(result.headers.Authorization).toBeUndefined()
  })

  // ==================== FA-04 响应拦截器 - 401时清除localStorage ====================
  it('response interceptor clears auth data on 401', () => {
    localStorage.setItem('cinema_token', 'old-token')
    localStorage.setItem('cinema_username', 'testuser')
    localStorage.setItem('cinema_role', 'user')

    const errorHandler = api.interceptors.response.handlers[0].rejected
    const err = { response: { status: 401 } }

    let rejected = false
    try {
      errorHandler(err)
    } catch (e) {
      rejected = true
    }

    expect(localStorage.getItem('cinema_token')).toBeNull()
    expect(localStorage.getItem('cinema_username')).toBeNull()
    expect(localStorage.getItem('cinema_role')).toBeNull()
    expect(rejected).toBe(true)
  })

  // ==================== FA-05 响应拦截器 - 200响应正常通过 ====================
  it('response interceptor passes through successful response', () => {
    const successHandler = api.interceptors.response.handlers[0].fulfilled
    const mockResponse = { status: 200, data: { success: true } }

    const result = successHandler(mockResponse)

    expect(result).toEqual(mockResponse)
    expect(localStorage.getItem('cinema_token')).toBeNull()
  })

  // ==================== FA-06 响应拦截器 - 非401错误保留token ====================
  it('response interceptor preserves auth data on non-401 errors', () => {
    localStorage.setItem('cinema_token', 'valid-token')
    localStorage.setItem('cinema_username', 'testuser')

    const errorHandler = api.interceptors.response.handlers[0].rejected
    const err = { response: { status: 500 } }

    let rejected = false
    try {
      errorHandler(err)
    } catch (e) {
      rejected = true
    }

    expect(localStorage.getItem('cinema_token')).toBe('valid-token')
    expect(rejected).toBe(true)
  })
})
