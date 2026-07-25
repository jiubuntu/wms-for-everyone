export interface TransferItem {
  id: number
  warehouseId: number
  productId: number
  productSkuCode: string
  productName: string
  unitName: string
  fromLocationId: number
  fromLocationCode: string
  toLocationId: number
  toLocationCode: string
  lotNumber: string | null
  quantity: number
  reasonId: number | null
  note: string | null
  createdByName: string
  createdAt: string
}

export interface TransferCreateInput {
  productId: number
  fromLocationId: number
  toLocationId: number
  lotNumber: string | null
  quantity: number
  reasonId: number | null
  note: string | null
}
