import { api } from "@/lib/axios"
import type { ApiCommonResponse, ApiPageResponse } from "@/lib/apiTypes"
import type {
  LocationBulkCreateInput,
  LocationItem,
  LocationUpdateInput,
} from "@/features/locations/types"

export async function getLocations(
  warehouseId: number,
  page: number,
  limit: number
): Promise<ApiPageResponse<LocationItem>> {
  const res = await api.get<ApiCommonResponse<ApiPageResponse<LocationItem>>>(
    `/warehouse/${warehouseId}/location/list`,
    { params: { page, limit } }
  )
  return res.data.data
}

export async function getAllLocations(warehouseId: number): Promise<LocationItem[]> {
  const res = await api.get<ApiCommonResponse<LocationItem[]>>(
    `/warehouse/${warehouseId}/location/all`
  )
  return res.data.data
}

export async function bulkCreateLocations(
  warehouseId: number,
  input: LocationBulkCreateInput
): Promise<LocationItem[]> {
  const res = await api.post<ApiCommonResponse<LocationItem[]>>(
    `/warehouse/${warehouseId}/location/bulk-create`,
    input
  )
  return res.data.data
}

export async function updateLocation(
  warehouseId: number,
  id: number,
  input: LocationUpdateInput
): Promise<LocationItem> {
  const res = await api.post<ApiCommonResponse<LocationItem>>(
    `/warehouse/${warehouseId}/location/${id}/update`,
    input
  )
  return res.data.data
}

export async function deleteLocation(warehouseId: number, id: number): Promise<void> {
  await api.post(`/warehouse/${warehouseId}/location/delete`, { id })
}
