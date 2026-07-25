import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import {
  bulkCreateLocations,
  deleteLocation,
  getAllLocations,
  getLocations,
  updateLocation,
} from "@/features/locations/api"
import type { LocationBulkCreateInput, LocationUpdateInput } from "@/features/locations/types"

export function useLocations(warehouseId: number, page: number, limit = 10) {
  return useQuery({
    queryKey: ["locations", warehouseId, page, limit],
    queryFn: () => getLocations(warehouseId, page, limit),
    enabled: Number.isFinite(warehouseId),
  })
}

export function useAllLocations(warehouseId: number | null) {
  return useQuery({
    queryKey: ["locations", warehouseId, "all"],
    queryFn: () => getAllLocations(warehouseId as number),
    enabled: warehouseId != null,
  })
}

export function useBulkCreateLocations(warehouseId: number) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (input: LocationBulkCreateInput) => bulkCreateLocations(warehouseId, input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["locations", warehouseId] })
      queryClient.invalidateQueries({ queryKey: ["warehouses"] })
    },
  })
}

export function useUpdateLocation(warehouseId: number) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, input }: { id: number; input: LocationUpdateInput }) =>
      updateLocation(warehouseId, id, input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["locations", warehouseId] })
    },
  })
}

export function useDeleteLocation(warehouseId: number) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => deleteLocation(warehouseId, id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["locations", warehouseId] })
      queryClient.invalidateQueries({ queryKey: ["warehouses"] })
    },
  })
}
