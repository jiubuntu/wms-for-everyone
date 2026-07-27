import type { InboundItemLineInput } from "@/features/inbound/types"

export interface InboundLineDraft {
  key: string
  productId: string
  unitId: string
  quantity: string
  lotTracking: boolean
  lotNumber: string
  manufactureDate: string
  expiryDate: string
}

export function createEmptyLine(): InboundLineDraft {
  return {
    key: crypto.randomUUID(),
    productId: "",
    unitId: "",
    quantity: "",
    lotTracking: false,
    lotNumber: "",
    manufactureDate: "",
    expiryDate: "",
  }
}

export function isLineValid(line: InboundLineDraft): boolean {
  if (!line.productId || !line.unitId || !line.quantity) return false
  const qty = Number(line.quantity)
  if (!(qty > 0)) return false
  if (line.lotTracking) {
    return Boolean(line.lotNumber && line.manufactureDate && line.expiryDate)
  }
  return true
}

export function toLineInput(line: InboundLineDraft): InboundItemLineInput {
  return {
    productId: Number(line.productId),
    unitId: Number(line.unitId),
    quantity: Number(line.quantity),
    lotNumber: line.lotTracking && line.lotNumber ? line.lotNumber : null,
    manufactureDate: line.lotTracking && line.manufactureDate ? line.manufactureDate : null,
    expiryDate: line.lotTracking && line.expiryDate ? line.expiryDate : null,
  }
}
