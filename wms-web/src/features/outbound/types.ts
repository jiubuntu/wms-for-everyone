export type OutboundStatus = "PENDING" | "PICKING" | "COMPLETED" | "CANCELLED"
export type AllocationType = "FEFO" | "MANUAL"

export interface OutboundListItem {
  id: number
  warehouseId: number
  customerName: string
  status: OutboundStatus
  itemCount: number
  createdAt: string
}

export interface OutboundAllocationDetail {
  locationCode: string
  lotNumber: string | null
  quantity: number
}

export interface OutboundItemDetail {
  id: number
  productId: number
  productSkuCode: string
  productName: string
  unitId: number
  unitName: string
  quantity: number
  allocationType: AllocationType
  allocations: OutboundAllocationDetail[]
}

export interface OutboundDetail {
  id: number
  warehouseId: number
  customerName: string
  status: OutboundStatus
  note: string | null
  processedByName: string | null
  items: OutboundItemDetail[]
  createdAt: string
}

export interface OutboundItemAllocationInput {
  locationId: number
  lotNumber: string | null
  quantity: number
}

export interface OutboundItemLineInput {
  productId: number
  unitId: number
  quantity: number
  allocationType: AllocationType
  allocations: OutboundItemAllocationInput[]
}

export interface OutboundCreateInput {
  customerName: string
  note: string | null
  items: OutboundItemLineInput[]
}
