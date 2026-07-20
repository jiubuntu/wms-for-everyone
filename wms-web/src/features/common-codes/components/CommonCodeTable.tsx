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
import { useDeleteCommonCode } from "@/features/common-codes/hooks"
import type { CommonCodeItem, CommonCodeScope } from "@/features/common-codes/types"

interface CommonCodeTableProps {
  scope: CommonCodeScope
  items: CommonCodeItem[]
  onEdit: (item: CommonCodeItem) => void
  showScopeColumn?: boolean
}

export function CommonCodeTable({
  scope,
  items,
  onEdit,
  showScopeColumn = false,
}: CommonCodeTableProps) {
  const deleteMutation = useDeleteCommonCode(scope)

  async function handleDelete(item: CommonCodeItem) {
    if (!window.confirm(`'${item.name}' 코드를 삭제하시겠습니까?`)) return

    try {
      await deleteMutation.mutateAsync(item.id)
    } catch (err) {
      const message = isAxiosError(err) ? err.response?.data?.message : null
      window.alert(message ?? "사용 중인 코드는 삭제할 수 없습니다.")
    }
  }

  return (
    <Table>
      <TableHeader>
        <TableRow className="bg-muted/50 hover:bg-muted/50">
          <TableHead className="text-xs text-muted-foreground uppercase">코드</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">코드명</TableHead>
          <TableHead className="text-xs text-muted-foreground uppercase">정렬 순서</TableHead>
          {showScopeColumn && (
            <TableHead className="text-xs text-muted-foreground uppercase">구분</TableHead>
          )}
          <TableHead />
        </TableRow>
      </TableHeader>
      <TableBody>
        {items.map((item) => {
          const readOnly = showScopeColumn && item.companyId === null

          return (
            <TableRow key={item.id}>
              <TableCell className="font-medium">{item.code}</TableCell>
              <TableCell>{item.name}</TableCell>
              <TableCell>{item.sortOrder}</TableCell>
              {showScopeColumn && (
                <TableCell>
                  <Badge variant={item.companyId === null ? "secondary" : "default"}>
                    {item.companyId === null ? "시스템 기본" : "자사 커스텀"}
                  </Badge>
                </TableCell>
              )}
              <TableCell className="text-right">
                {!readOnly && (
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
                )}
              </TableCell>
            </TableRow>
          )
        })}
      </TableBody>
    </Table>
  )
}
