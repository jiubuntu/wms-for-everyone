export type InventoryHistoryTargetType = "INBOUND" | "OUTBOUND" | "TRANSFER" | "ADJUSTMENT"

export interface InventoryItem {
  id: number
  warehouseId: number
  locationId: number
  locationCode: string
  productId: number
  productSkuCode: string
  productName: string
  unitName: string
  lotNumber: string | null
  manufactureDate: string | null
  expiryDate: string | null
  quantity: number
  reservedQuantity: number
}

export interface InventoryAdjustInput {
  quantity: number
  reason: string
}

export interface AvailableLocationItem {
  locationId: number
  locationCode: string
  lotNumber: string | null
  expiryDate: string | null
  availableQuantity: number
}

export interface InventoryHistoryItem {
  id: number
  warehouseId: number
  locationCode: string
  productSkuCode: string
  productName: string
  lotNumber: string | null
  quantityChange: number
  quantityAfter: number
  targetType: InventoryHistoryTargetType
  targetId: number | null
  reason: string | null
  createdAt: string
  createdByName: string
}
