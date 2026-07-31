import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import {
  createWarehouse,
  deleteWarehouse,
  getAllWarehouses,
  getWarehouse,
  getWarehouses,
  updateWarehouse,
} from "@/features/warehouses/api"
import type { WarehouseCreateInput, WarehouseUpdateInput } from "@/features/warehouses/types"

export function useWarehouses(keyword: string, page: number, limit = 10) {
  return useQuery({
    queryKey: ["warehouses", keyword, page, limit],
    queryFn: () => getWarehouses(keyword, page, limit),
  })
}

export function useAllWarehouses() {
  return useQuery({
    queryKey: ["warehouses", "all"],
    queryFn: () => getAllWarehouses(),
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
