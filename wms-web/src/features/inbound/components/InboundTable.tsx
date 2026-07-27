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
import type { InboundListItem } from "@/features/inbound/types"

interface InboundTableProps {
  items: InboundListItem[]
}

export function InboundTable({ items }: InboundTableProps) {
  return (
    <Table>
      <TableHeader>
        <TableRow className="bg-muted/50 hover:bg-muted/50">
          <TableHead className="text-xs text-muted-foreground uppercase">공급업체명</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">상태</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">상품 라인 수</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">등록일</TableHead>
          <TableHead />
        </TableRow>
      </TableHeader>
      <TableBody>
        {items.map((item) => (
          <TableRow key={item.id}>
            <TableCell className="font-medium">{item.supplierName}</TableCell>
            <TableCell>
              <InboundStatusBadge status={item.status} />
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
                <Link to={`/app/inbounds/${item.id}`} aria-label="상세보기">
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
