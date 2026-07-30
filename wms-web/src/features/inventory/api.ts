import { api } from "@/lib/axios"
import type { ApiCommonResponse, ApiPageResponse } from "@/lib/apiTypes"
import type {
  AvailableLocationItem,
  InventoryAdjustInput,
  InventoryHistoryItem,
  InventoryHistoryTargetType,
  InventoryItem,
} from "@/features/inventory/types"

export async function getInventory(
  warehouseId: number | null,
  keyword: string,
  page: number,
  limit: number
): Promise<ApiPageResponse<InventoryItem>> {
  const res = await api.get<ApiCommonResponse<ApiPageResponse<InventoryItem>>>("/inventory/list", {
    params: { warehouseId: warehouseId ?? undefined, keyword: keyword || undefined, page, limit },
  })
  return res.data.data
}

export async function adjustInventory(
  id: number,
  input: InventoryAdjustInput,
  idempotencyKey: string
): Promise<InventoryItem> {
  const res = await api.post<ApiCommonResponse<InventoryItem>>(`/inventory/${id}/adjust`, input, {
    headers: { "Idempotency-Key": idempotencyKey },
  })
  return res.data.data
}

export async function getAvailableLocations(
  warehouseId: number,
  productId: number
): Promise<AvailableLocationItem[]> {
  const res = await api.get<ApiCommonResponse<AvailableLocationItem[]>>(
    "/inventory/available-locations",
    { params: { warehouseId, productId } }
  )
  return res.data.data
}

export async function getInventoryHistory(
  warehouseId: number | null,
  keyword: string,
  targetType: InventoryHistoryTargetType | undefined,
  page: number,
  limit: number
): Promise<ApiPageResponse<InventoryHistoryItem>> {
  const res = await api.get<ApiCommonResponse<ApiPageResponse<InventoryHistoryItem>>>(
    "/inventory/history",
    { params: { warehouseId: warehouseId ?? undefined, keyword: keyword || undefined, targetType, page, limit } }
  )
  return res.data.data
}
