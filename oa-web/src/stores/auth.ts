import { defineStore } from 'pinia'
import { authApi } from '@/api/services'
import type { LoginUser } from '@/api/types'

const TOKEN_KEY = 'oa_token'
const USER_KEY = 'oa_user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: JSON.parse(localStorage.getItem(USER_KEY) || 'null') as LoginUser | null
  }),
  getters: {
    isAuthed: (state) => Boolean(state.token),
    displayName: (state) => state.user?.realName || state.user?.username || '未登录'
  },
  actions: {
    async login(username: string, password: string) {
      const user = await authApi.login({ username, password })
      this.setSession(user)
    },
    setSession(user: LoginUser) {
      this.user = user
      this.token = user.accessToken
      localStorage.setItem(TOKEN_KEY, user.accessToken)
      localStorage.setItem(USER_KEY, JSON.stringify(user))
    },
    async logout() {
      try {
        if (this.token) {
          await authApi.logout()
        }
      } finally {
        this.clearSession()
      }
    },
    clearSession() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    }
  }
})