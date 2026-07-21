import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import {
  createProductUnit,
  deleteProductUnit,
  getProductUnits,
  updateProductUnit,
} from "@/features/product-units/api"
import type {
  ProductUnitCreateInput,
  ProductUnitUpdateInput,
} from "@/features/product-units/types"

export function useProductUnits(page: number, limit = 20) {
  return useQuery({
    queryKey: ["product-units", page, limit],
    queryFn: () => getProductUnits(page, limit),
  })
}

export function useCreateProductUnit() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (input: ProductUnitCreateInput) => createProductUnit(input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["product-units"] })
    },
  })
}

export function useUpdateProductUnit() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, input }: { id: number; input: ProductUnitUpdateInput }) =>
      updateProductUnit(id, input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["product-units"] })
    },
  })
}

export function useDeleteProductUnit() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => deleteProductUnit(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["product-units"] })
    },
  })
}
