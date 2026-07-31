import dayjs from "dayjs"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import type { InventoryHistoryItem, InventoryHistoryTargetType } from "@/features/inventory/types"

const TARGET_TYPE_LABEL: Record<InventoryHistoryTargetType, string> = {
  INBOUND: "입고",
  OUTBOUND: "출고",
  TRANSFER: "이동",
  ADJUSTMENT: "조정",
}

const TARGET_TYPE_VARIANT: Record<
  InventoryHistoryTargetType,
  "success" | "destructive" | "default" | "warning"
> = {
  INBOUND: "success",
  OUTBOUND: "destructive",
  TRANSFER: "default",
  ADJUSTMENT: "warning",
}

interface InventoryHistoryTableProps {
  items: InventoryHistoryItem[]
}

export function InventoryHistoryTable({ items }: InventoryHistoryTableProps) {
  return (
    <Table>
      <TableHeader>
        <TableRow className="bg-muted/50 hover:bg-muted/50">
          <TableHead className="text-xs text-muted-foreground uppercase">발생일시</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">유형</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">상품</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">위치 / LOT</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">변동량</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">변동 후 수량</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">처리자</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {items.map((item) => (
          <TableRow key={item.id}>
            <TableCell>{dayjs(item.createdAt).format("YYYY-MM-DD HH:mm")}</TableCell>
            <TableCell>
              <Badge variant={TARGET_TYPE_VARIANT[item.targetType]}>
                {TARGET_TYPE_LABEL[item.targetType]}
              </Badge>
            </TableCell>
            <TableCell className="font-medium">
              {item.productName}
              <span className="ml-1.5 text-xs text-muted-foreground">{item.productSkuCode}</span>
            </TableCell>
            <TableCell>
              {item.locationCode}
              {item.lotNumber && (
                <span className="ml-1.5 text-xs text-muted-foreground">{item.lotNumber}</span>
              )}
            </TableCell>
            <TableCell className={item.quantityChange >= 0 ? "text-success" : "text-destructive"}>
              {item.quantityChange >= 0 ? `+${item.quantityChange}` : item.quantityChange}
            </TableCell>
            <TableCell>{item.quantityAfter}</TableCell>
            <TableCell>{item.createdByName}</TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}
