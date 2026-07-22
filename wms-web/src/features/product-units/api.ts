import { api } from "@/lib/axios"
import type { ApiCommonResponse, ApiPageResponse } from "@/lib/apiTypes"
import type {
  ProductUnitCreateInput,
  ProductUnitItem,
  ProductUnitUpdateInput,
} from "@/features/product-units/types"

export async function getProductUnits(
  page: number,
  limit: number
): Promise<ApiPageResponse<ProductUnitItem>> {
  const res = await api.get<ApiCommonResponse<ApiPageResponse<ProductUnitItem>>>(
    "/product-unit/list",
    { params: { page, limit } }
  )
  return res.data.data
}

export async function getAllProductUnits(): Promise<ProductUnitItem[]> {
  const res = await api.get<ApiCommonResponse<ProductUnitItem[]>>("/product-unit/all")
  return res.data.data
}

export async function createProductUnit(input: ProductUnitCreateInput): Promise<ProductUnitItem> {
  const res = await api.post<ApiCommonResponse<ProductUnitItem>>("/product-unit/create", input)
  return res.data.data
}

export async function updateProductUnit(
  id: number,
  input: ProductUnitUpdateInput
): Promise<ProductUnitItem> {
  const res = await api.post<ApiCommonResponse<ProductUnitItem>>(
    `/product-unit/${id}/update`,
    input
  )
  return res.data.data
}

export async function deleteProductUnit(id: number): Promise<void> {
  await api.post("/product-unit/delete", { id })
}
