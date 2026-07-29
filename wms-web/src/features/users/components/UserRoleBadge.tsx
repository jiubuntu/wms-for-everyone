import { Badge } from "@/components/ui/badge"
import type { IssuableUserRole } from "@/features/users/types"

const ROLE_LABEL: Record<IssuableUserRole, string> = {
  WAREHOUSE_MANAGER: "창고관리자",
  WORKER: "작업자",
}

export function UserRoleBadge({ role }: { role: IssuableUserRole }) {
  return <Badge variant="secondary">{ROLE_LABEL[role]}</Badge>
}
