import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import {
  createWarehouse,
  deleteWarehouse,
  getWarehouse,
  getWarehouses,
  updateWarehouse,
} from "@/features/warehouses/api"
import type { WarehouseCreateInput, WarehouseUpdateInput } from "@/features/warehouses/types"

export function useWarehouses(page: number, limit = 10) {
  return useQuery({
    queryKey: ["warehouses", page, limit],
    queryFn: () => getWarehouses(page, limit),
  })
}

export function useWarehouse(id: number) {
  return useQuery({
    queryKey: ["warehouses", id],
    queryFn: () => getWarehouse(id),
    enabled: Number.isFinite(id),
  })
}

export function useCreateWarehouse() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (input: WarehouseCreateInput) => createWarehouse(input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["warehouses"] })
    },
  })
}

export function useUpdateWarehouse() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, input }: { id: number; input: WarehouseUpdateInput }) =>
      updateWarehouse(id, input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["warehouses"] })
    },
  })
}

export function useDeleteWarehouse() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => deleteWarehouse(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["warehouses"] })
    },
  })
}
