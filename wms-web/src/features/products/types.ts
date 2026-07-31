export interface ProductItem {
  id: number
  companyId: number
  skuCode: string
  name: string
  categoryId: number
  storageTypeId: number
  baseUnitId: number
  subUnitId: number | null
  unitConversionRate: number | null
  lotTracking: boolean
  description: string | null
  createdAt: string
}

export interface ProductCreateInput {
  skuCode: string
  name: string
  categoryId: number
  storageTypeId: number
  baseUnitId: number
  subUnitId: number | null
  unitConversionRate: number | null
  lotTracking: boolean
  description: string | null
}

export type ProductUpdateInput = Omit<ProductCreateInput, "skuCode">
