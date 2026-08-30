import { defineStore } from 'pinia'
import { ref } from 'vue'
import { userApi } from '@/api/user'
import type { UserProfile } from '@/types'

/**
 * 用户状态管理
 */
export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const profile = ref<UserProfile | null>(
    localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')!) : null
  )

  // 是否已登录
  const isLoggedIn = ref(!!token.value)

  /** 设置登录状态 */
  function setLogin(newToken: string, newProfile: UserProfile) {
    token.value = newToken
    profile.value = newProfile
    isLoggedIn.value = true

    localStorage.setItem('token', newToken)
    localStorage.setItem('user', JSON.stringify(newProfile))
  }

  /** 退出登录 */
  function logout() {
    token.value = ''
    profile.value = null
    isLoggedIn.value = false

    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  /** 获取用户信息（刷新 profile） */
  async function fetchProfile() {
    try {
      const data = await userApi.getProfile()
      profile.value = data
      localStorage.setItem('user', JSON.stringify(data))
    } catch {
      // 失败时清除登录状态
      logout()
    }
  }

  return {
    token,
    profile,
    isLoggedIn,
    setLogin,
    logout,
    fetchProfile,
  }
})
