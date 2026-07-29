import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import type { InventoryHealthStatus, InventoryStatusItem } from "@/features/dashboard/types"

const STATUS_LABEL: Record<InventoryHealthStatus, string> = {
  NORMAL: "정상",
  LOW: "부족",
  UNAVAILABLE: "가용없음",
}

const STATUS_VARIANT: Record<InventoryHealthStatus, "success" | "warning" | "destructive"> = {
  NORMAL: "success",
  LOW: "warning",
  UNAVAILABLE: "destructive",
}

export function InventoryStatusTable({ items }: { items: InventoryStatusItem[] }) {
  return (
    <Table>
      <TableHeader>
        <TableRow className="bg-muted/50 hover:bg-muted/50">
          <TableHead className="text-xs text-muted-foreground uppercase">상품</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">보유</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">가용</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">상태</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {items.map((item) => (
          <TableRow key={item.skuCode}>
            <TableCell>
              <p className="font-medium">{item.productName}</p>
              <p className="text-xs text-muted-foreground">{item.skuCode}</p>
            </TableCell>
            <TableCell>{item.quantity}</TableCell>
            <TableCell>{item.availableQuantity}</TableCell>
            <TableCell>
              <Badge variant={STATUS_VARIANT[item.status]}>{STATUS_LABEL[item.status]}</Badge>
            </TableCell>
          </TableRow>
        ))}
        {items.length === 0 && (
          <TableRow>
            <TableCell colSpan={4} className="py-6 text-center text-sm text-muted-foreground">
              보유 중인 재고가 없습니다.
            </TableCell>
          </TableRow>
        )}
      </TableBody>
    </Table>
  )
}
