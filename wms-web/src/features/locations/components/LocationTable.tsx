import { isAxiosError } from "axios"
import { Pencil, Trash2 } from "lucide-react"
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
import { useDeleteLocation } from "@/features/locations/hooks"
import type { LocationItem } from "@/features/locations/types"

interface LocationTableProps {
  warehouseId: number
  items: LocationItem[]
  storageTypeNameById: Map<number, string>
  onEdit: (item: LocationItem) => void
}

export function LocationTable({
  warehouseId,
  items,
  storageTypeNameById,
  onEdit,
}: LocationTableProps) {
  const deleteMutation = useDeleteLocation(warehouseId)

  async function handleDelete(item: LocationItem) {
    if (!window.confirm(`'${item.code}' 위치를 삭제하시겠습니까?`)) return

    try {
      await deleteMutation.mutateAsync(item.id)
    } catch (err) {
      const message = isAxiosError(err) ? err.response?.data?.message : null
      window.alert(message ?? "재고가 남아있는 위치는 삭제할 수 없습니다.")
    }
  }

  return (
    <Table>
      <TableHeader>
        <TableRow className="bg-muted/50 hover:bg-muted/50">
          <TableHead className="text-xs text-muted-foreground uppercase">코드</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">보관 유형</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">상태</TableHead>
          <TableHead />
        </TableRow>
      </TableHeader>
      <TableBody>
        {items.map((item) => (
          <TableRow key={item.id}>
            <TableCell className="font-medium">{item.code}</TableCell>
            <TableCell>
              {item.storageTypeId
                ? (storageTypeNameById.get(item.storageTypeId) ?? "-")
                : "창고 기본값"}
            </TableCell>
            <TableCell>
              <Badge variant={item.status === "ACTIVE" ? "success" : "secondary"}>
                {item.status === "ACTIVE" ? "사용" : "사용 중지"}
              </Badge>
            </TableCell>
            <TableCell className="text-right">
              <div className="flex justify-end gap-1.5">
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
                  aria-label="삭제"
                >
                  <Trash2 className="size-4" />
                </Button>
              </div>
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}
