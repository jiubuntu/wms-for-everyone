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
import { useDeleteProduct } from "@/features/products/hooks"
import type { ProductItem } from "@/features/products/types"

interface ProductTableProps {
  items: ProductItem[]
  categoryNameById: Map<number, string>
  storageTypeNameById: Map<number, string>
  unitNameById: Map<number, string>
  onEdit: (item: ProductItem) => void
}

export function ProductTable({
  items,
  categoryNameById,
  storageTypeNameById,
  unitNameById,
  onEdit,
}: ProductTableProps) {
  const deleteMutation = useDeleteProduct()

  async function handleDelete(item: ProductItem) {
    if (!window.confirm(`'${item.name}' 상품을 삭제하시겠습니까?`)) return

    try {
      await deleteMutation.mutateAsync(item.id)
    } catch (err) {
      const message = isAxiosError(err) ? err.response?.data?.message : null
      window.alert(message ?? "재고가 남아있는 상품은 삭제할 수 없습니다.")
    }
  }

  return (
    <Table>
      <TableHeader>
        <TableRow className="bg-muted/50 hover:bg-muted/50">
          <TableHead className="text-xs text-muted-foreground uppercase">SKU 코드</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">상품명</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">카테고리</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">보관 유형</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">기본 단위</TableHead>
          <TableHead />
        </TableRow>
      </TableHeader>
      <TableBody>
        {items.map((item) => (
          <TableRow key={item.id}>
            <TableCell className="font-medium">{item.skuCode}</TableCell>
            <TableCell>{item.name}</TableCell>
            <TableCell>{categoryNameById.get(item.categoryId) ?? "-"}</TableCell>
            <TableCell>{storageTypeNameById.get(item.storageTypeId) ?? "-"}</TableCell>
            <TableCell>{unitNameById.get(item.baseUnitId) ?? "-"}</TableCell>
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
