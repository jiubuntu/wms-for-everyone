import { api } from "@/lib/axios"
import type { ApiCommonResponse, ApiPageResponse } from "@/lib/apiTypes"
import type {
  InboundCreateInput,
  InboundDetail,
  InboundListItem,
  InboundLocationInput,
} from "@/features/inbound/types"

export async function getInbounds(
  warehouseId: number,
  page: number,
  limit: number
): Promise<ApiPageResponse<InboundListItem>> {
  const res = await api.get<ApiCommonResponse<ApiPageResponse<InboundListItem>>>(
    `/warehouse/${warehouseId}/inbound/list`,
    { params: { page, limit } }
  )
  return res.data.data
}

export async function getInbound(warehouseId: number, id: number): Promise<InboundDetail> {
  const res = await api.get<ApiCommonResponse<InboundDetail>>(
    `/warehouse/${warehouseId}/inbound/${id}`
  )
  return res.data.data
}

export async function createInbound(
  warehouseId: number,
  input: InboundCreateInput,
  idempotencyKey: string
): Promise<InboundDetail> {
  const res = await api.post<ApiCommonResponse<InboundDetail>>(
    `/warehouse/${warehouseId}/inbound/create`,
    input,
    { headers: { "Idempotency-Key": idempotencyKey } }
  )
  return res.data.data
}

export async function replaceInboundItemLocations(
  warehouseId: number,
  inboundId: number,
  itemId: number,
  locations: InboundLocationInput[]
): Promise<InboundDetail> {
  const res = await api.post<ApiCommonResponse<InboundDetail>>(
    `/warehouse/${warehouseId}/inbound/${inboundId}/items/${itemId}/locations`,
    { locations }
  )
  return res.data.data
}

export async function completeInbound(
  warehouseId: number,
  id: number,
  idempotencyKey: string
): Promise<InboundDetail> {
  const res = await api.post<ApiCommonResponse<InboundDetail>>(
    `/warehouse/${warehouseId}/inbound/${id}/complete`,
    {},
    { headers: { "Idempotency-Key": idempotencyKey } }
  )
  return res.data.data
}

export async function cancelInbound(
  warehouseId: number,
  id: number,
  idempotencyKey: string
): Promise<InboundDetail> {
  const res = await api.post<ApiCommonResponse<InboundDetail>>(
    `/warehouse/${warehouseId}/inbound/${id}/cancel`,
    {},
    { headers: { "Idempotency-Key": idempotencyKey } }
  )
  return res.data.data
}
