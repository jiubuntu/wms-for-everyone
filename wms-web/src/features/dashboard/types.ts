// docs/request/dashboard.md 기준으로 구현된 백엔드 응답 형태에 맞춘 타입입니다.
// 아직 OpenAPI codegen 대상이 아니라 수기로 작성/유지합니다.

import type { OutboundStatus } from "@/features/outbound/types"
import type { InboundStatus } from "@/features/inbound/types"

export interface WarehouseScopeStats {
  outboundPending: number
  outboundPicking: number
  outboundCompletedToday: number
  inboundPending: number
  expiringSoonCount: number
}

export interface OutboundQueueItem {
  id: number
  customerName: string
  itemCount: number
  waitingMinutes: number
  status: OutboundStatus
}

export interface InboundQueueItem {
  id: number
  supplierName: string
  itemCount: number
  createdAt: string
  status: InboundStatus
}

export interface ExpiringInventoryItem {
  productName: string
  lotNumber: string
  locationCode: string
  quantity: number
  daysLeft: number
}

export type InventoryHealthStatus = "NORMAL" | "LOW" | "UNAVAILABLE"

export interface InventoryStatusItem {
  productName: string
  skuCode: string
  quantity: number
  availableQuantity: number
  status: InventoryHealthStatus
}

export interface WarehouseScopeDashboardData {
  stats: WarehouseScopeStats
  outboundQueue: OutboundQueueItem[]
  inboundQueue: InboundQueueItem[]
  expiringInventory: ExpiringInventoryItem[]
  inventoryStatus: InventoryStatusItem[]
}

export type WarehouseOpsStatus = "NORMAL" | "WATCH" | "ALERT"

export interface WarehouseOpsRow {
  warehouseId: number
  warehouseName: string
  warehouseNote: string
  outboundPending: number
  outboundPicking: number
  inboundPending: number
  expiringSoonCount: number
  status: WarehouseOpsStatus
}

export interface ProcessingTrendPoint {
  label: string
  outboundCount: number
  inboundCount: number
}

export interface StageBreakdown {
  pending: number
  inProgress: number
  completed: number
  cancelled: number
}

export interface CompanyStats {
  outboundPending: number
  outboundPicking: number
  inboundPending: number
  expiringSoonCount: number
}

export interface CompanyOverviewData {
  productSkuCount: number
  companyStats: CompanyStats
  warehouseOps: WarehouseOpsRow[]
  processingTrend: ProcessingTrendPoint[]
  todayOutboundStatus: StageBreakdown
  todayInboundStatus: StageBreakdown
}
