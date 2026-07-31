import { api } from "@/lib/axios"
import type { ApiCommonResponse, ApiPageResponse } from "@/lib/apiTypes"
import type {
  CommonCodeCreateInput,
  CommonCodeGroup,
  CommonCodeItem,
  CommonCodeScope,
  CommonCodeUpdateInput,
} from "@/features/common-codes/types"

function basePath(scope: CommonCodeScope): string {
  return scope === "admin" ? "/admin/common-code" : "/common-code"
}

export async function getCommonCodes(
  scope: CommonCodeScope,
  groupCode: CommonCodeGroup,
  keyword: string,
  page: number,
  limit: number
): Promise<ApiPageResponse<CommonCodeItem>> {
  const res = await api.get<ApiCommonResponse<ApiPageResponse<CommonCodeItem>>>(
    `${basePath(scope)}/list`,
    { params: { groupCode, keyword: keyword || undefined, page, limit } }
  )
  return res.data.data
}

export async function getAllCommonCodes(
  scope: CommonCodeScope,
  groupCode: CommonCodeGroup
): Promise<CommonCodeItem[]> {
  const res = await api.get<ApiCommonResponse<CommonCodeItem[]>>(`${basePath(scope)}/all`, {
    params: { groupCode },
  })
  return res.data.data
}

export async function createCommonCode(
  scope: CommonCodeScope,
  input: CommonCodeCreateInput
): Promise<CommonCodeItem> {
  const res = await api.post<ApiCommonResponse<CommonCodeItem>>(`${basePath(scope)}/create`, input)
  return res.data.data
}

export async function updateCommonCode(
  scope: CommonCodeScope,
  id: number,
  input: CommonCodeUpdateInput
): Promise<CommonCodeItem> {
  const res = await api.post<ApiCommonResponse<CommonCodeItem>>(
    `${basePath(scope)}/${id}/update`,
    input
  )
  return res.data.data
}

export async function deleteCommonCode(scope: CommonCodeScope, id: number): Promise<void> {
  await api.post(`${basePath(scope)}/delete`, { id })
}
