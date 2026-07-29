import { Link } from "react-router-dom"
import dayjs from "dayjs"
import { Eye } from "lucide-react"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { InboundStatusBadge } from "@/features/inbound/components/InboundStatusBadge"
import type { InboundQueueItem } from "@/features/dashboard/types"

function formatCreatedAt(createdAt: string): string {
  const date = dayjs(createdAt)
  return date.isSame(dayjs(), "day") ? `${date.format("M/D")} (오늘)` : date.format("M/D")
}

export function InboundQueueTable({ items }: { items: InboundQueueItem[] }) {
  return (
    <Table>
      <TableHeader>
        <TableRow className="bg-muted/50 hover:bg-muted/50">
          <TableHead className="text-xs text-muted-foreground uppercase">공급처</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">품목수</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">등록일</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">상태</TableHead>
          <TableHead />
        </TableRow>
      </TableHeader>
      <TableBody>
        {items.map((item) => (
          <TableRow key={item.id}>
            <TableCell className="font-medium">{item.supplierName}</TableCell>
            <TableCell>{item.itemCount}</TableCell>
            <TableCell>{formatCreatedAt(item.createdAt)}</TableCell>
            <TableCell>
              <InboundStatusBadge status={item.status} />
            </TableCell>
            <TableCell className="text-right">
              <Button
                variant="ghost"
                size="icon"
                className="size-8 rounded-full bg-primary/10 text-primary hover:bg-primary/20 hover:text-primary"
                asChild
              >
                <Link to={`/app/inbounds/${item.id}`} aria-label="상세보기">
                  <Eye className="size-4" />
                </Link>
              </Button>
            </TableCell>
          </TableRow>
        ))}
        {items.length === 0 && (
          <TableRow>
            <TableCell colSpan={5} className="py-6 text-center text-sm text-muted-foreground">
              처리 대기 중인 입고가 없습니다.
            </TableCell>
          </TableRow>
        )}
      </TableBody>
    </Table>
  )
}
