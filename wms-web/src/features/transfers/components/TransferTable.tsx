import dayjs from "dayjs"
import { ArrowRight } from "lucide-react"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import type { TransferItem } from "@/features/transfers/types"

interface TransferTableProps {
  items: TransferItem[]
}

export function TransferTable({ items }: TransferTableProps) {
  return (
    <Table>
      <TableHeader>
        <TableRow className="bg-muted/50 hover:bg-muted/50">
          <TableHead className="text-xs text-muted-foreground uppercase">일시</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">상품</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">위치</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">수량</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">처리자</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {items.map((item) => (
          <TableRow key={item.id}>
            <TableCell>{dayjs(item.createdAt).format("YYYY-MM-DD HH:mm")}</TableCell>
            <TableCell className="font-medium">
              {item.productName}
              <span className="ml-1.5 text-xs text-muted-foreground">{item.productSkuCode}</span>
            </TableCell>
            <TableCell>
              <span className="inline-flex items-center gap-1.5">
                {item.fromLocationCode}
                <ArrowRight className="size-3.5 text-muted-foreground" />
                {item.toLocationCode}
              </span>
              {item.lotNumber && (
                <span className="ml-1.5 text-xs text-muted-foreground">{item.lotNumber}</span>
              )}
            </TableCell>
            <TableCell>
              {item.quantity} {item.unitName}
            </TableCell>
            <TableCell>{item.createdByName}</TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}
