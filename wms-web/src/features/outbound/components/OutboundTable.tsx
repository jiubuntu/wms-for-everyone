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
import { OutboundStatusBadge } from "@/features/outbound/components/OutboundStatusBadge"
import type { OutboundListItem } from "@/features/outbound/types"

interface OutboundTableProps {
  items: OutboundListItem[]
}

export function OutboundTable({ items }: OutboundTableProps) {
  return (
    <Table>
      <TableHeader>
        <TableRow className="bg-muted/50 hover:bg-muted/50">
          <TableHead className="text-xs text-muted-foreground uppercase">고객사명</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">상태</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">상품 라인 수</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">등록일</TableHead>
          <TableHead />
        </TableRow>
      </TableHeader>
      <TableBody>
        {items.map((item) => (
          <TableRow key={item.id}>
            <TableCell className="font-medium">{item.customerName}</TableCell>
            <TableCell>
              <OutboundStatusBadge status={item.status} />
            </TableCell>
            <TableCell>{item.itemCount}</TableCell>
            <TableCell>{dayjs(item.createdAt).format("YYYY-MM-DD HH:mm")}</TableCell>
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
      </TableBody>
    </Table>
  )
}
