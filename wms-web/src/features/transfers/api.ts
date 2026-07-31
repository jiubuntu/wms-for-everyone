import { api } from "@/lib/axios"
import type { ApiCommonResponse, ApiPageResponse } from "@/lib/apiTypes"
import type { TransferCreateInput, TransferItem } from "@/features/transfers/types"

export async function getTransfers(
  warehouseId: number,
  page: number,
  limit: number
): Promise<ApiPageResponse<TransferItem>> {
  const res = await api.get<ApiCommonResponse<ApiPageResponse<TransferItem>>>(
    `/warehouse/${warehouseId}/transfer/list`,
    { params: { page, limit } }
  )
  return res.data.data
}

export async function createTransfer(
  warehouseId: number,
  input: TransferCreateInput,
  idempotencyKey: string
): Promise<TransferItem> {
  const res = await api.post<ApiCommonResponse<TransferItem>>(
    `/warehouse/${warehouseId}/transfer/create`,
    input,
    { headers: { "Idempotency-Key": idempotencyKey } }
  )
  return res.data.data
}
