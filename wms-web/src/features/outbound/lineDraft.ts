import type { AllocationType, OutboundItemLineInput } from "@/features/outbound/types"

export interface OutboundLineDraft {
  key: string
  productId: string
  unitId: string
  quantity: string
  allocationType: AllocationType
  manualQuantities: Record<string, string>
}

export function createEmptyLine(): OutboundLineDraft {
  return {
    key: crypto.randomUUID(),
    productId: "",
    unitId: "",
    quantity: "",
    allocationType: "FEFO",
    manualQuantities: {},
  }
}

export function fromKeyOf(locationId: number, lotNumber: string | null) {
  return `${locationId}::${lotNumber ?? ""}`
}

function parseFromKey(key: string): { locationId: number; lotNumber: string | null } {
  const [locationIdStr, lot] = key.split("::")
  return { locationId: Number(locationIdStr), lotNumber: lot ? lot : null }
}

export function manualTotal(line: OutboundLineDraft): number {
  return Object.values(line.manualQuantities).reduce((sum, v) => sum + (Number(v) || 0), 0)
}

export function isLineValid(line: OutboundLineDraft): boolean {
  if (!line.productId || !line.unitId || !line.quantity) return false
  const qty = Number(line.quantity)
  if (!(qty > 0)) return false
  if (line.allocationType === "MANUAL") {
    return manualTotal(line) === qty
  }
  return true
}

export function toLineInput(line: OutboundLineDraft): OutboundItemLineInput {
  const allocations =
    line.allocationType === "MANUAL"
      ? Object.entries(line.manualQuantities)
          .filter(([, qty]) => Number(qty) > 0)
          .map(([key, qty]) => {
            const { locationId, lotNumber } = parseFromKey(key)
            return { locationId, lotNumber, quantity: Number(qty) }
          })
      : []

  return {
    productId: Number(line.productId),
    unitId: Number(line.unitId),
    quantity: Number(line.quantity),
    allocationType: line.allocationType,
    allocations,
  }
}
