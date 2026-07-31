import { Badge } from "@/components/ui/badge"
import type { UserStatus } from "@/features/users/types"

const STATUS_LABEL: Record<UserStatus, string> = {
  ACTIVE: "활성",
  LOCKED: "잠김",
  INACTIVE: "비활성",
}

const STATUS_VARIANT: Record<UserStatus, "success" | "destructive" | "secondary"> = {
  ACTIVE: "success",
  LOCKED: "destructive",
  INACTIVE: "secondary",
}

export function UserStatusBadge({ status }: { status: UserStatus }) {
  return <Badge variant={STATUS_VARIANT[status]}>{STATUS_LABEL[status]}</Badge>
}
