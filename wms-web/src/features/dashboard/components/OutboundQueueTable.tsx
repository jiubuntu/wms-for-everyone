import { Link } from "react-router-dom"
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
import { OutboundStatusBadge } from "@/features/outbound/components/OutboundStatusBadge"
import type { OutboundQueueItem } from "@/features/dashboard/types"

function formatWaitingTime(minutes: number): string {
  if (minutes < 60) return `${minutes}분`
  const hours = Math.floor(minutes / 60)
  const rest = minutes % 60
  return rest === 0 ? `${hours}시간` : `${hours}시간 ${rest}분`
}

export function OutboundQueueTable({ items }: { items: OutboundQueueItem[] }) {
  return (
    <Table>
      <TableHeader>
        <TableRow className="bg-muted/50 hover:bg-muted/50">
          <TableHead className="text-xs text-muted-foreground uppercase">거래처</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">품목수</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">대기시간</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">상태</TableHead>
          <TableHead />
        </TableRow>
      </TableHeader>
      <TableBody>
        {items.map((item) => (
          <TableRow key={item.id}>
            <TableCell className="font-medium">{item.customerName}</TableCell>
            <TableCell>{item.itemCount}</TableCell>
            <TableCell>{formatWaitingTime(item.waitingMinutes)}</TableCell>
            <TableCell>
              <OutboundStatusBadge status={item.status} />
            </TableCell>
            <TableCell className="text-right">
              <Button
                variant="ghost"
                size="icon"
                className="size-8 rounded-full bg-primary/10 text-primary hover:bg-primary/20 hover:text-primary"
                asChild
              >
                <Link to={`/app/outbounds/${item.id}`} aria-label="상세보기">
                  <Eye className="size-4" />
                </Link>
              </Button>
            </TableCell>
          </TableRow>
        ))}
        {items.length === 0 && (
          <TableRow>
            <TableCell colSpan={5} className="py-6 text-center text-sm text-muted-foreground">
              처리 대기 중인 출고가 없습니다.
            </TableCell>
          </TableRow>
        )}
      </TableBody>
    </Table>
  )
}
