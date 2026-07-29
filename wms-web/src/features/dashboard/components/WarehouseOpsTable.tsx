import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import type { WarehouseOpsRow, WarehouseOpsStatus } from "@/features/dashboard/types"

const STATUS_LABEL: Record<WarehouseOpsStatus, string> = {
  NORMAL: "정상",
  WATCH: "관찰",
  ALERT: "주의",
}

const STATUS_VARIANT: Record<WarehouseOpsStatus, "success" | "warning" | "destructive"> = {
  NORMAL: "success",
  WATCH: "warning",
  ALERT: "destructive",
}

interface WarehouseOpsTableProps {
  rows: WarehouseOpsRow[]
  onSelectWarehouse: (warehouseId: number) => void
}

export function WarehouseOpsTable({ rows, onSelectWarehouse }: WarehouseOpsTableProps) {
  return (
    <Table>
      <TableHeader>
        <TableRow className="bg-muted/50 hover:bg-muted/50">
          <TableHead className="text-xs text-muted-foreground uppercase">창고</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">출고대기</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">피킹중</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">입고예정</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">유통기한임박</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">상태</TableHead>
          <TableHead />
        </TableRow>
      </TableHeader>
      <TableBody>
        {rows.map((row) => (
          <TableRow key={row.warehouseId}>
            <TableCell>
              <p className="font-medium">{row.warehouseName}</p>
              <p className="text-xs text-muted-foreground">{row.warehouseNote}</p>
            </TableCell>
            <TableCell>{row.outboundPending}</TableCell>
            <TableCell>{row.outboundPicking}</TableCell>
            <TableCell>{row.inboundPending}</TableCell>
            <TableCell>{row.expiringSoonCount}</TableCell>
            <TableCell>
              <Badge variant={STATUS_VARIANT[row.status]}>{STATUS_LABEL[row.status]}</Badge>
            </TableCell>
            <TableCell className="text-right">
              <Button variant="outline" size="sm" onClick={() => onSelectWarehouse(row.warehouseId)}>
                보기
              </Button>
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}
