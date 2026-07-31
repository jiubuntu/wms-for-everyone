import { useEffect, useState } from "react"
import { isAxiosError } from "axios"
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { useCreateCommonCode, useUpdateCommonCode } from "@/features/common-codes/hooks"
import type { CommonCodeGroup, CommonCodeItem, CommonCodeScope } from "@/features/common-codes/types"

interface CommonCodeFormDialogProps {
  scope: CommonCodeScope
  open: boolean
  onOpenChange: (open: boolean) => void
  groupCode: CommonCodeGroup
  target: CommonCodeItem | null
}

export function CommonCodeFormDialog({
  scope,
  open,
  onOpenChange,
  groupCode,
  target,
}: CommonCodeFormDialogProps) {
  const [code, setCode] = useState("")
  const [name, setName] = useState("")
  const [sortOrder, setSortOrder] = useState("0")
  const [error, setError] = useState("")

  const createMutation = useCreateCommonCode(scope)
  const updateMutation = useUpdateCommonCode(scope)
  const isSubmitting = createMutation.isPending || updateMutation.isPending

  useEffect(() => {
    if (!open) return
    setCode(target?.code ?? "")
    setName(target?.name ?? "")
    setSortOrder(String(target?.sortOrder ?? 0))
    setError("")
  }, [open, target])

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError("")

    try {
      if (target) {
        await updateMutation.mutateAsync({
          id: target.id,
          input: { name, sortOrder: Number(sortOrder) },
        })
      } else {
        await createMutation.mutateAsync({ groupCode, code, name, sortOrder: Number(sortOrder) })
      }
      onOpenChange(false)
    } catch (err) {
      const message = isAxiosError(err) ? err.response?.data?.message : null
      setError(message ?? "저장하지 못했습니다.")
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <DialogHeader>
            <DialogTitle>{target ? "코드 수정" : "코드 등록"}</DialogTitle>
          </DialogHeader>

          <div className="flex flex-col gap-3">
            <div className="flex flex-col gap-1.5">
              <label htmlFor="common-code-code" className="text-sm font-medium">
                코드
              </label>
              <Input
                id="common-code-code"
                value={code}
                onChange={(e) => setCode(e.target.value)}
                disabled={!!target}
                required
                placeholder="예: ELECTRONICS"
              />
            </div>

            <div className="flex flex-col gap-1.5">
              <label htmlFor="common-code-name" className="text-sm font-medium">
                코드명
              </label>
              <Input
                id="common-code-name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
                placeholder="예: 전자제품"
              />
            </div>

            <div className="flex flex-col gap-1.5">
              <label htmlFor="common-code-sort-order" className="text-sm font-medium">
                정렬 순서
              </label>
              <Input
                id="common-code-sort-order"
                type="number"
                value={sortOrder}
                onChange={(e) => setSortOrder(e.target.value)}
                required
              />
            </div>

            {error && <p className="text-sm text-destructive">{error}</p>}
          </div>

          <DialogFooter>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "저장 중..." : "저장"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
