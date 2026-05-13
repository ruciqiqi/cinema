import { createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'

describe('Auth Store', () => {
  let authStore

  beforeEach(() => {
    const pinia = createPinia()
    authStore = useAuthStore(pinia)
    vi.clearAllMocks()
  })

  afterEach(() => {
    localStorage.clear()
  })

  it('initializes with empty state', () => {
    expect(authStore.username).toBe('')
    expect(authStore.token).toBe('')
    expect(authStore.role).toBe('')
    expect(authStore.isLoggedIn).toBe(false)
    expect(authStore.isAdmin).toBe(false)
  })

  it('sets session successfully', () => {
    const mockSession = {
      token: 'test-token-123',
      username: 'testuser',
      role: 'USER'
    }

    authStore.setSession(mockSession)

    expect(authStore.username).toBe('testuser')
    expect(authStore.token).toBe('test-token-123')
    expect(authStore.role).toBe('USER')
    expect(authStore.isLoggedIn).toBe(true)
    expect(authStore.isAdmin).toBe(false)
    expect(localStorage.getItem('cinema_token')).toBe('test-token-123')
    expect(localStorage.getItem('cinema_username')).toBe('testuser')
    expect(localStorage.getItem('cinema_role')).toBe('USER')
  })

  it('logs out successfully', () => {
    const mockSession = {
      token: 'test-token-123',
      username: 'testuser',
      role: 'USER'
    }

    authStore.setSession(mockSession)
    authStore.logout()

    expect(authStore.username).toBe('')
    expect(authStore.token).toBe('')
    expect(authStore.role).toBe('')
    expect(authStore.isLoggedIn).toBe(false)
    expect(localStorage.getItem('cinema_token')).toBeNull()
    expect(localStorage.getItem('cinema_username')).toBeNull()
    expect(localStorage.getItem('cinema_role')).toBeNull()
  })

  it('checks if user is admin', () => {
    authStore.setSession({ token: 'token', username: 'admin', role: 'admin' })
    expect(authStore.isAdmin).toBe(true)

    authStore.logout()
    authStore.setSession({ token: 'token', username: 'user', role: 'USER' })
    expect(authStore.isAdmin).toBe(false)
  })
})