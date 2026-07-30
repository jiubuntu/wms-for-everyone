import { api } from "@/lib/axios"
import type { ApiCommonResponse, ApiPageResponse } from "@/lib/apiTypes"
import type {
  WarehouseCreateInput,
  WarehouseItem,
  WarehouseUpdateInput,
} from "@/features/warehouses/types"

export async function getWarehouses(
  keyword: string,
  page: number,
  limit: number
): Promise<ApiPageResponse<WarehouseItem>> {
  const res = await api.get<ApiCommonResponse<ApiPageResponse<WarehouseItem>>>("/warehouse/list", {
    params: { keyword: keyword || undefined, page, limit },
  })
  return res.data.data
}

export async function getAllWarehouses(): Promise<WarehouseItem[]> {
  const res = await api.get<ApiCommonResponse<WarehouseItem[]>>("/warehouse/all")
  return res.data.data
}

export async function getWarehouse(id: number): Promise<WarehouseItem> {
  const res = await api.get<ApiCommonResponse<WarehouseItem>>(`/warehouse/${id}`)
  return res.data.data
}

export async function createWarehouse(input: WarehouseCreateInput): Promise<WarehouseItem> {
  const res = await api.post<ApiCommonResponse<WarehouseItem>>("/warehouse/create", input)
  return res.data.data
}

export async function updateWarehouse(
  id: number,
  input: WarehouseUpdateInput
): Promise<WarehouseItem> {
  const res = await api.post<ApiCommonResponse<WarehouseItem>>(`/warehouse/${id}/update`, input)
  return res.data.data
}

export async function deleteWarehouse(id: number): Promise<void> {
  await api.post("/warehouse/delete", { id })
}
