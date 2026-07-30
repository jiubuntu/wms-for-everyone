import { api } from "@/lib/axios"
import type { ApiCommonResponse, ApiPageResponse } from "@/lib/apiTypes"
import type {
  ProductCreateInput,
  ProductItem,
  ProductUpdateInput,
} from "@/features/products/types"

export async function getProducts(
  keyword: string,
  page: number,
  limit: number
): Promise<ApiPageResponse<ProductItem>> {
  const res = await api.get<ApiCommonResponse<ApiPageResponse<ProductItem>>>("/product/list", {
    params: { keyword: keyword || undefined, page, limit },
  })
  return res.data.data
}

export async function getAllProducts(): Promise<ProductItem[]> {
  const res = await api.get<ApiCommonResponse<ProductItem[]>>("/product/all")
  return res.data.data
}

export async function createProduct(input: ProductCreateInput): Promise<ProductItem> {
  const res = await api.post<ApiCommonResponse<ProductItem>>("/product/create", input)
  return res.data.data
}

export async function updateProduct(
  id: number,
  input: ProductUpdateInput
): Promise<ProductItem> {
  const res = await api.post<ApiCommonResponse<ProductItem>>(`/product/${id}/update`, input)
  return res.data.data
}

export async function deleteProduct(id: number): Promise<void> {
  await api.post("/product/delete", { id })
}
