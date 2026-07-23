import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import {
  createProduct,
  deleteProduct,
  getProducts,
  updateProduct,
} from "@/features/products/api"
import type { ProductCreateInput, ProductUpdateInput } from "@/features/products/types"

export function useProducts(page: number, limit = 10) {
  return useQuery({
    queryKey: ["products", page, limit],
    queryFn: () => getProducts(page, limit),
  })
}

export function useCreateProduct() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (input: ProductCreateInput) => createProduct(input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["products"] })
    },
  })
}

export function useUpdateProduct() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, input }: { id: number; input: ProductUpdateInput }) =>
      updateProduct(id, input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["products"] })
    },
  })
}

export function useDeleteProduct() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => deleteProduct(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["products"] })
    },
  })
}
