import { isAxiosError } from "axios"
import { Link } from "react-router-dom"
import { MapPin, Pencil, Trash2 } from "lucide-react"
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
import { useDeleteWarehouse } from "@/features/warehouses/hooks"
import type { WarehouseItem } from "@/features/warehouses/types"

interface WarehouseTableProps {
  items: WarehouseItem[]
  storageTypeNameById: Map<number, string>
  canManage: boolean
  onEdit: (item: WarehouseItem) => void
}

export function WarehouseTable({
  items,
  storageTypeNameById,
  canManage,
  onEdit,
}: WarehouseTableProps) {
  const deleteMutation = useDeleteWarehouse()

  async function handleDelete(item: WarehouseItem) {
    if (!window.confirm(`'${item.name}' 창고를 비활성화하시겠습니까?`)) return

    try {
      await deleteMutation.mutateAsync(item.id)
    } catch (err) {
      const message = isAxiosError(err) ? err.response?.data?.message : null
      window.alert(message ?? "재고가 있거나 담당자가 배정된 창고는 비활성화할 수 없습니다.")
    }
  }

  return (
    <Table>
      <TableHeader>
        <TableRow className="bg-muted/50 hover:bg-muted/50">
          <TableHead className="text-xs text-muted-foreground uppercase">창고명</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">보관 유형</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">주소</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">위치 수</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">상태</TableHead>
          <TableHead />
        </TableRow>
      </TableHeader>
      <TableBody>
        {items.map((item) => (
          <TableRow key={item.id}>
            <TableCell className="font-medium">{item.name}</TableCell>
            <TableCell>{storageTypeNameById.get(item.storageTypeId) ?? "-"}</TableCell>
            <TableCell>{item.address ?? "-"}</TableCell>
            <TableCell>{item.locationCount}</TableCell>
            <TableCell>
              <Badge variant={item.active ? "success" : "secondary"}>
                {item.active ? "사용" : "미사용"}
              </Badge>
            </TableCell>
            <TableCell className="text-right">
              <div className="flex justify-end gap-1.5">
                <Button
                  variant="ghost"
                  size="icon"
                  className="size-8 rounded-full bg-info/10 text-info hover:bg-info/20 hover:text-info"
                  asChild
                >
                  <Link to={`/app/warehouses/${item.id}`} aria-label="위치 관리">
                    <MapPin className="size-4" />
                  </Link>
                </Button>
                {canManage && (
                  <>
                    <Button
                      variant="ghost"
                      size="icon"
                      className="size-8 rounded-full bg-primary/10 text-primary hover:bg-primary/20 hover:text-primary"
                      onClick={() => onEdit(item)}
                      aria-label="수정"
                    >
                      <Pencil className="size-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      className="size-8 rounded-full bg-destructive/10 text-destructive hover:bg-destructive/20 hover:text-destructive"
                      onClick={() => handleDelete(item)}
                      aria-label="비활성화"
                    >
                      <Trash2 className="size-4" />
                    </Button>
                  </>
                )}
              </div>
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}
