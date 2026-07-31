import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import {
  adjustInventory,
  getAvailableLocations,
  getInventory,
  getInventoryHistory,
} from "@/features/inventory/api"
import type { InventoryAdjustInput, InventoryHistoryTargetType } from "@/features/inventory/types"

export function useInventory(warehouseId: number | null, keyword: string, page: number, limit = 10) {
  return useQuery({
    queryKey: ["inventory", warehouseId, keyword, page, limit],
    queryFn: () => getInventory(warehouseId, keyword, page, limit),
    refetchInterval: 5000,
  })
}

export function useAvailableLocations(warehouseId: number | null, productId: number | null) {
  return useQuery({
    queryKey: ["inventory", "available-locations", warehouseId, productId],
    queryFn: () => getAvailableLocations(warehouseId as number, productId as number),
    enabled: warehouseId != null && productId != null,
  })
}

export function useInventoryHistory(
  warehouseId: number | null,
  keyword: string,
  targetType: InventoryHistoryTargetType | undefined,
  page: number,
  limit = 10
) {
  return useQuery({
    queryKey: ["inventory", "history", warehouseId, keyword, targetType, page, limit],
    queryFn: () => getInventoryHistory(warehouseId, keyword, targetType, page, limit),
  })
}

export function useAdjustInventory() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({
      id,
      input,
      idempotencyKey,
    }: {
      id: number
      input: InventoryAdjustInput
      idempotencyKey: string
    }) => adjustInventory(id, input, idempotencyKey),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["inventory"] })
    },
  })
}
