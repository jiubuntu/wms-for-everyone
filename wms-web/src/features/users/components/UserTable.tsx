import { UserX } from "lucide-react"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { UserRoleBadge } from "@/features/users/components/UserRoleBadge"
import { UserStatusBadge } from "@/features/users/components/UserStatusBadge"
import type { UserListItem } from "@/features/users/types"

interface UserTableProps {
  items: UserListItem[]
  onWithdraw: (item: UserListItem) => void
  canWithdraw: (item: UserListItem) => boolean
}

export function UserTable({ items, onWithdraw, canWithdraw }: UserTableProps) {
  return (
    <Table>
      <TableHeader>
        <TableRow className="bg-muted/50 hover:bg-muted/50">
          <TableHead className="text-xs text-muted-foreground uppercase">이메일</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">이름</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">연락처</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">역할</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">소속 창고</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">상태</TableHead>
          <TableHead />
        </TableRow>
      </TableHeader>
      <TableBody>
        {items.map((item) => (
          <TableRow key={item.id}>
            <TableCell className="font-medium">{item.email}</TableCell>
            <TableCell>{item.name}</TableCell>
            <TableCell>{item.phone}</TableCell>
            <TableCell>
              <UserRoleBadge role={item.role} />
            </TableCell>
            <TableCell>{item.warehouseName}</TableCell>
            <TableCell>
              <UserStatusBadge status={item.status} />
            </TableCell>
            <TableCell className="text-right">
              {canWithdraw(item) && (
                <Button
                  variant="ghost"
                  size="icon"
                  className="size-8 rounded-full bg-destructive/10 text-destructive hover:bg-destructive/20 hover:text-destructive"
                  onClick={() => onWithdraw(item)}
                  aria-label="탈퇴"
                >
                  <UserX className="size-4" />
                </Button>
              )}
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}
