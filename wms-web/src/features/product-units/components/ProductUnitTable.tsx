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
import { Button } from "@/components/ui/button"
import { useDeleteProductUnit } from "@/features/product-units/hooks"
import type { ProductUnitItem } from "@/features/product-units/types"

interface ProductUnitTableProps {
  items: ProductUnitItem[]
  onEdit: (item: ProductUnitItem) => void
}

export function ProductUnitTable({ items, onEdit }: ProductUnitTableProps) {
  const deleteMutation = useDeleteProductUnit()

  async function handleDelete(item: ProductUnitItem) {
    if (!window.confirm(`'${item.name}' 단위를 삭제하시겠습니까?`)) return

    try {
      await deleteMutation.mutateAsync(item.id)
    } catch (err) {
      const message = isAxiosError(err) ? err.response?.data?.message : null
      window.alert(message ?? "사용 중인 단위는 삭제할 수 없습니다.")
    }
  }

  return (
    <Table>
      <TableHeader>
        <TableRow className="bg-muted/50 hover:bg-muted/50">
          <TableHead className="text-xs text-muted-foreground uppercase">단위명</TableHead>
          <TableHead />
        </TableRow>
      </TableHeader>
      <TableBody>
        {items.map((item) => (
          <TableRow key={item.id}>
            <TableCell className="font-medium">{item.name}</TableCell>
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
