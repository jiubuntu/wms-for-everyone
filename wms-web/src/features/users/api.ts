import { api } from "@/lib/axios"
import type { ApiCommonResponse, ApiPageResponse } from "@/lib/apiTypes"
import type { UserCreateInput, UserIssueResult, UserListItem } from "@/features/users/types"

export async function getUsers(page: number, limit: number): Promise<ApiPageResponse<UserListItem>> {
  const res = await api.get<ApiCommonResponse<ApiPageResponse<UserListItem>>>("/user/list", {
    params: { page, limit },
  })
  return res.data.data
}

export async function createUser(input: UserCreateInput): Promise<UserIssueResult> {
  const res = await api.post<ApiCommonResponse<UserIssueResult>>("/user/create", input)
  return res.data.data
}

export async function withdrawUser(id: number): Promise<void> {
  await api.post(`/user/${id}/withdraw`)
}

export async function changePassword(currentPassword: string, newPassword: string): Promise<void> {
  await api.post("/user/change-password", { currentPassword, newPassword })
}
