import { Badge } from "@/components/ui/badge"
import type { OutboundStatus } from "@/features/outbound/types"

const STATUS_LABEL: Record<OutboundStatus, string> = {
  PENDING: "대기",
  PICKING: "피킹중",
  COMPLETED: "출하 완료",
  CANCELLED: "취소",
}

const STATUS_VARIANT: Record<OutboundStatus, "warning" | "default" | "success" | "secondary"> = {
  PENDING: "warning",
  PICKING: "default",
  COMPLETED: "success",
  CANCELLED: "secondary",
}

export function OutboundStatusBadge({ status }: { status: OutboundStatus }) {
  return <Badge variant={STATUS_VARIANT[status]}>{STATUS_LABEL[status]}</Badge>
}
