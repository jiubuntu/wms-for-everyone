import axios, { AxiosError, type InternalAxiosRequestConfig } from "axios"
import type { ApiCommonResponse } from "@/lib/apiTypes"

interface RetryableRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean
}

let accessToken: string | null = null
let onUnauthorized: (() => void) | null = null

export function setAccessToken(token: string | null) {
  accessToken = token
}

export function setUnauthorizedHandler(handler: () => void) {
  onUnauthorized = handler
}

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true,
})

api.interceptors.request.use((config) => {
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  return config
})

// 이 경로들은 401이 "액세스 토큰 만료"가 아니라 그 자체로 의미 있는 응답(로그인 실패, 재설정 링크 만료 등)이라
// 자동 리프레시-재시도 대상에서 제외한다. 포함시키면 리프레시 실패 에러가 원래 에러를 덮어써버린다.
const AUTH_RETRY_EXCLUDED_PATHS = ["/auth/refresh", "/auth/login", "/auth/password/reset-confirm"]

let refreshPromise: Promise<string> | null = null

async function refreshAccessToken(): Promise<string> {
  if (!refreshPromise) {
    refreshPromise = api
      .post<ApiCommonResponse<{ accessToken: string }>>("/auth/refresh")
      .then((res) => {
        const token = res.data.data.accessToken
        setAccessToken(token)
        return token
      })
      .finally(() => {
        refreshPromise = null
      })
  }
  return refreshPromise
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as RetryableRequestConfig | undefined

    if (
      error.response?.status === 401 &&
      original &&
      !original._retry &&
      !AUTH_RETRY_EXCLUDED_PATHS.includes(original.url ?? "")
    ) {
      original._retry = true
      try {
        const token = await refreshAccessToken()
        original.headers.Authorization = `Bearer ${token}`
        return api(original)
      } catch (refreshError) {
        setAccessToken(null)
        onUnauthorized?.()
        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  }
)
