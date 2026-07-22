import axios, { AxiosError } from 'axios'

export interface ApiResult<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 8000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('oa_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResult<unknown>
    if (body && typeof body.code === 'number') {
      if (body.code !== 200) {
        throw new Error(body.message || '请求失败')
      }
      return body.data
    }
    return response.data
  },
  (error: AxiosError<ApiResult<unknown>>) => {
    const message = error.response?.data?.message || error.message || '网络异常'
    throw new Error(message)
  }
)

export async function withFallback<T>(request: Promise<T>, fallback: T): Promise<{ data: T; mocked: boolean }> {
  try {
    return { data: await request, mocked: false }
  } catch {
    return { data: fallback, mocked: true }
  }
}
