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
import { useCreateProductUnit, useUpdateProductUnit } from "@/features/product-units/hooks"
import type { ProductUnitItem } from "@/features/product-units/types"

interface ProductUnitFormDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  target: ProductUnitItem | null
}

export function ProductUnitFormDialog({ open, onOpenChange, target }: ProductUnitFormDialogProps) {
  const [name, setName] = useState("")
  const [error, setError] = useState("")

  const createMutation = useCreateProductUnit()
  const updateMutation = useUpdateProductUnit()
  const isSubmitting = createMutation.isPending || updateMutation.isPending

  useEffect(() => {
    if (!open) return
    setName(target?.name ?? "")
    setError("")
  }, [open, target])

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError("")

    try {
      if (target) {
        await updateMutation.mutateAsync({ id: target.id, input: { name } })
      } else {
        await createMutation.mutateAsync({ name })
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
            <DialogTitle>{target ? "단위 수정" : "단위 등록"}</DialogTitle>
          </DialogHeader>

          <div className="flex flex-col gap-3">
            <div className="flex flex-col gap-1.5">
              <label htmlFor="product-unit-name" className="text-sm font-medium">
                단위명
              </label>
              <Input
                id="product-unit-name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
                placeholder="예: 박스"
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
