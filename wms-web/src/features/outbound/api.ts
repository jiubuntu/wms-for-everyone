import { api } from "@/lib/axios"
import type { ApiCommonResponse, ApiPageResponse } from "@/lib/apiTypes"
import type {
  OutboundCreateInput,
  OutboundDetail,
  OutboundListItem,
} from "@/features/outbound/types"

export async function getOutbounds(
  warehouseId: number,
  page: number,
  limit: number
): Promise<ApiPageResponse<OutboundListItem>> {
  const res = await api.get<ApiCommonResponse<ApiPageResponse<OutboundListItem>>>(
    `/warehouse/${warehouseId}/outbound/list`,
    { params: { page, limit } }
  )
  return res.data.data
}

export async function getOutbound(warehouseId: number, id: number): Promise<OutboundDetail> {
  const res = await api.get<ApiCommonResponse<OutboundDetail>>(
    `/warehouse/${warehouseId}/outbound/${id}`
  )
  return res.data.data
}

export async function createOutbound(
  warehouseId: number,
  input: OutboundCreateInput,
  idempotencyKey: string
): Promise<OutboundDetail> {
  const res = await api.post<ApiCommonResponse<OutboundDetail>>(
    `/warehouse/${warehouseId}/outbound/create`,
    input,
    { headers: { "Idempotency-Key": idempotencyKey } }
  )
  return res.data.data
}

export async function completeOutbound(
  warehouseId: number,
  id: number,
  idempotencyKey: string
): Promise<OutboundDetail> {
  const res = await api.post<ApiCommonResponse<OutboundDetail>>(
    `/warehouse/${warehouseId}/outbound/${id}/complete`,
    {},
    { headers: { "Idempotency-Key": idempotencyKey } }
  )
  return res.data.data
}

export async function cancelOutbound(
  warehouseId: number,
  id: number,
  idempotencyKey: string
): Promise<OutboundDetail> {
  const res = await api.post<ApiCommonResponse<OutboundDetail>>(
    `/warehouse/${warehouseId}/outbound/${id}/cancel`,
    {},
    { headers: { "Idempotency-Key": idempotencyKey } }
  )
  return res.data.data
}
