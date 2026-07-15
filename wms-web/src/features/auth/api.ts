import { api } from "@/lib/axios"
import type { ApiCommonResponse } from "@/lib/apiTypes"
import type { LoginResult } from "@/features/auth/types"

export async function login(email: string, password: string): Promise<LoginResult> {
  const res = await api.post<ApiCommonResponse<LoginResult>>("/auth/login", { email, password })
  return res.data.data
}

export async function refresh(): Promise<{ accessToken: string }> {
  const res = await api.post<ApiCommonResponse<{ accessToken: string }>>("/auth/refresh")
  return res.data.data
}

export async function logout(): Promise<void> {
  await api.post("/auth/logout")
}

export async function requestPasswordReset(email: string): Promise<void> {
  await api.post("/auth/password/reset-request", { email })
}

export async function confirmPasswordReset(params: {
  token: string
  newPassword: string
  newPasswordConfirm: string
}): Promise<void> {
  await api.post("/auth/password/reset-confirm", params)
}
