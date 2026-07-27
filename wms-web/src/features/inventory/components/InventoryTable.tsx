import { SlidersHorizontal } from "lucide-react"
import { cn } from "@/lib/utils"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import type { InventoryItem } from "@/features/inventory/types"

interface InventoryTableProps {
  items: InventoryItem[]
  onAdjust: (item: InventoryItem) => void
  flashingIds?: Set<number>
}

export function InventoryTable({ items, onAdjust, flashingIds }: InventoryTableProps) {
  return (
    <Table>
      <TableHeader>
        <TableRow className="bg-muted/50 hover:bg-muted/50">
          <TableHead className="text-xs text-muted-foreground uppercase">상품</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">위치</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">LOT / 유효기간</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">실재고</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">예약재고</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">가용재고</TableHead>
          <TableHead />
        </TableRow>
      </TableHeader>
      <TableBody>
        {items.map((item) => (
          <TableRow
            key={item.id}
            className={cn(flashingIds?.has(item.id) && "animate-row-flash")}
          >
            <TableCell className="font-medium">
              {item.productName}
              <span className="ml-1.5 text-xs text-muted-foreground">{item.productSkuCode}</span>
            </TableCell>
            <TableCell>{item.locationCode}</TableCell>
            <TableCell>
              {item.lotNumber ? (
                <>
                  {item.lotNumber}
                  {item.expiryDate && (
                    <span className="ml-1.5 text-xs text-muted-foreground">{item.expiryDate}</span>
                  )}
                </>
              ) : (
                "-"
              )}
            </TableCell>
            <TableCell>
              {item.quantity} {item.unitName}
            </TableCell>
            <TableCell>
              {item.reservedQuantity} {item.unitName}
            </TableCell>
            <TableCell className="font-medium">
              {item.quantity - item.reservedQuantity} {item.unitName}
            </TableCell>
            <TableCell className="text-right">
              <Button
                variant="ghost"
                size="icon"
                className="size-8 rounded-full bg-primary/10 text-primary hover:bg-primary/20 hover:text-primary"
                onClick={() => onAdjust(item)}
                aria-label="재고 조정"
              >
                <SlidersHorizontal className="size-4" />
              </Button>
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}
