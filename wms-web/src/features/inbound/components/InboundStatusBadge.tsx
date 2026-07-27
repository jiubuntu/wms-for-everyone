import { Badge } from "@/components/ui/badge"
import type { InboundStatus } from "@/features/inbound/types"

const STATUS_LABEL: Record<InboundStatus, string> = {
  PENDING: "대기",
  IN_PROGRESS: "위치배치중",
  COMPLETED: "입고 완료",
  CANCELLED: "취소",
}

const STATUS_VARIANT: Record<InboundStatus, "warning" | "default" | "success" | "secondary"> = {
  PENDING: "warning",
  IN_PROGRESS: "default",
  COMPLETED: "success",
  CANCELLED: "secondary",
}

export function InboundStatusBadge({ status }: { status: InboundStatus }) {
  return <Badge variant={STATUS_VARIANT[status]}>{STATUS_LABEL[status]}</Badge>
}
