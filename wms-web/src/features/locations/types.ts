export type LocationStatus = "ACTIVE" | "DISABLED"

export interface LocationItem {
  id: number
  warehouseId: number
  zone: string
  row: string
  col: string
  level: string
  code: string
  storageTypeId: number | null
  status: LocationStatus
  createdAt: string
}

export interface LocationBulkCreateInput {
  zone: string
  rowFrom: number
  rowTo: number
  colFrom: number
  colTo: number
  levelCount: number
  storageTypeId: number | null
}

export interface LocationUpdateInput {
  storageTypeId: number | null
  status: LocationStatus
}
