import { defineStore } from 'pinia'
import { authApi } from '@/api/services'
import { mockUser } from '@/api/mock'
import type { LoginUser } from '@/api/types'

const TOKEN_KEY = 'oa_token'
const USER_KEY = 'oa_user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: JSON.parse(localStorage.getItem(USER_KEY) || 'null') as LoginUser | null,
    demoMode: localStorage.getItem('oa_demo_mode') === '1'
  }),
  getters: {
    isAuthed: (state) => Boolean(state.token),
    displayName: (state) => state.user?.realName || state.user?.username || '未登录'
  },
  actions: {
    async login(username: string, password: string) {
      const user = await authApi.login({ username, password })
      this.setSession(user, false)
    },
    useDemo() {
      this.setSession(mockUser, true)
    },
    setSession(user: LoginUser, demo: boolean) {
      this.user = user
      this.token = user.accessToken
      this.demoMode = demo
      localStorage.setItem(TOKEN_KEY, user.accessToken)
      localStorage.setItem(USER_KEY, JSON.stringify(user))
      localStorage.setItem('oa_demo_mode', demo ? '1' : '0')
    },
    logout() {
      this.token = ''
      this.user = null
      this.demoMode = false
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
      localStorage.removeItem('oa_demo_mode')
    }
  }
})
