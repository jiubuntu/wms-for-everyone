export interface ProductUnitItem {
  id: number
  companyId: number
  name: string
  createdAt: string
}

export interface ProductUnitCreateInput {
  name: string
}

export interface ProductUnitUpdateInput {
  name: string
}
