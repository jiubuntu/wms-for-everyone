export type InboundStatus = "PENDING" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED"

export interface InboundListItem {
  id: number
  warehouseId: number
  supplierName: string
  status: InboundStatus
  itemCount: number
  createdAt: string
}

export interface InboundItemLocationDetail {
  locationId: number
  locationCode: string
  quantity: number
}

export interface InboundItemDetail {
  id: number
  productId: number
  productSkuCode: string
  productName: string
  unitId: number
  unitName: string
  quantity: number
  lotNumber: string | null
  manufactureDate: string | null
  expiryDate: string | null
  locations: InboundItemLocationDetail[]
}

export interface InboundDetail {
  id: number
  warehouseId: number
  supplierName: string
  status: InboundStatus
  note: string | null
  processedByName: string | null
  items: InboundItemDetail[]
  createdAt: string
}

export interface InboundItemLineInput {
  productId: number
  unitId: number
  quantity: number
  lotNumber: string | null
  manufactureDate: string | null
  expiryDate: string | null
}

export interface InboundCreateInput {
  supplierName: string
  note: string | null
  items: InboundItemLineInput[]
}

export interface InboundLocationInput {
  locationId: number
  quantity: number
}
