export interface WarehouseItem {
  id: number
  companyId: number
  name: string
  address: string | null
  locationCount: number
  active: boolean
  createdAt: string
}

export interface WarehouseCreateInput {
  name: string
  address: string | null
}

export type WarehouseUpdateInput = WarehouseCreateInput
