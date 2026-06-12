import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import axios from 'axios'
import api from '@/api'

vi.mock('axios', () => ({
  default: {
    create: vi.fn((config) => ({
      defaults: { baseURL: config?.baseURL },
      interceptors: {
        request: { use: vi.fn(), handlers: [] },
        response: { use: vi.fn(), handlers: [] }
      }
    }))
  }
}))

describe('API Module', () => {
  beforeEach(() => {
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

    // 获取请求拦截器 fulfilled 函数
    const handler = api.interceptors.request.use.mock.calls[0][0]
    const config = { headers: {} }
    const result = handler(config)

    expect(result.headers.Authorization).toBe('Bearer test-token-123')
  })

  // ==================== FA-03 请求拦截器 - 无token不附加Header ====================
  it('request interceptor does not add Authorization header when no token', () => {
    const handler = api.interceptors.request.use.mock.calls[0][0]
    const config = { headers: {} }
    const result = handler(config)

    expect(result.headers.Authorization).toBeUndefined()
  })

  // ==================== FA-04 响应拦截器 - 401时清除localStorage ====================
  it('response interceptor clears auth data on 401', async () => {
    localStorage.setItem('cinema_token', 'old-token')
    localStorage.setItem('cinema_username', 'testuser')
    localStorage.setItem('cinema_role', 'user')

    const errorHandler = api.interceptors.response.use.mock.calls[0][1]
    const err = { response: { status: 401 } }

    await expect(errorHandler(err)).rejects.toBe(err)

    expect(localStorage.getItem('cinema_token')).toBeNull()
    expect(localStorage.getItem('cinema_username')).toBeNull()
    expect(localStorage.getItem('cinema_role')).toBeNull()
  })

  // ==================== FA-05 响应拦截器 - 200响应正常通过 ====================
  it('response interceptor passes through successful response', () => {
    const successHandler = api.interceptors.response.use.mock.calls[0][0]
    const mockResponse = { status: 200, data: { success: true } }

    const result = successHandler(mockResponse)

    expect(result).toEqual(mockResponse)
    expect(localStorage.getItem('cinema_token')).toBeNull()
  })

  // ==================== FA-06 响应拦截器 - 非401错误保留token ====================
  it('response interceptor preserves auth data on non-401 errors', async () => {
    localStorage.setItem('cinema_token', 'valid-token')
    localStorage.setItem('cinema_username', 'testuser')

    const errorHandler = api.interceptors.response.use.mock.calls[0][1]
    const err = { response: { status: 500 } }

    await expect(errorHandler(err)).rejects.toBe(err)

    expect(localStorage.getItem('cinema_token')).toBe('valid-token')
  })
})
