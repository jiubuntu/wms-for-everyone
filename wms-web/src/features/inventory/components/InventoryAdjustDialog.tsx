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
import { Textarea } from "@/components/ui/textarea"
import { Button } from "@/components/ui/button"
import { useAdjustInventory } from "@/features/inventory/hooks"
import { createIdempotencyKey } from "@/lib/idempotency"
import type { InventoryItem } from "@/features/inventory/types"

interface InventoryAdjustDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  target: InventoryItem | null
}

export function InventoryAdjustDialog({ open, onOpenChange, target }: InventoryAdjustDialogProps) {
  const [quantity, setQuantity] = useState("")
  const [reason, setReason] = useState("")
  const [error, setError] = useState("")
  const [idempotencyKey, setIdempotencyKey] = useState("")

  const adjustMutation = useAdjustInventory()

  useEffect(() => {
    if (!open || !target) return
    setQuantity(String(target.quantity))
    setReason("")
    setError("")
    setIdempotencyKey(createIdempotencyKey())
  }, [open, target])

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!target) return
    setError("")

    try {
      await adjustMutation.mutateAsync({
        id: target.id,
        input: { quantity: Number(quantity), reason },
        idempotencyKey,
      })
      onOpenChange(false)
    } catch (err) {
      const message = isAxiosError(err) ? err.response?.data?.message : null
      setError(message ?? "재고를 조정하지 못했습니다.")
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <DialogHeader>
            <DialogTitle>재고 조정 {target && `— ${target.productName}`}</DialogTitle>
          </DialogHeader>

          {target && (
            <div className="flex flex-col gap-3">
              <div className="flex justify-between border-b pb-2 text-sm">
                <span className="text-muted-foreground">현재 위치 / LOT</span>
                <span className="font-medium">
                  {target.locationCode}
                  {target.lotNumber && ` / ${target.lotNumber}`}
                </span>
              </div>
              <div className="flex justify-between border-b pb-2 text-sm">
                <span className="text-muted-foreground">현재 실재고</span>
                <span className="font-medium">
                  {target.quantity} {target.unitName}
                </span>
              </div>

              <div className="flex flex-col gap-1.5">
                <label htmlFor="adjust-quantity" className="text-sm font-medium">
                  실사 수량 (조정 후 실재고)
                </label>
                <Input
                  id="adjust-quantity"
                  type="number"
                  min={0}
                  value={quantity}
                  onChange={(e) => setQuantity(e.target.value)}
                  required
                />
              </div>

              <div className="flex flex-col gap-1.5">
                <label htmlFor="adjust-reason" className="text-sm font-medium">
                  조정 사유
                </label>
                <Textarea
                  id="adjust-reason"
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  required
                  placeholder="예: 실사 결과 수량 불일치"
                />
              </div>

              {error && <p className="text-sm text-destructive">{error}</p>}
            </div>
          )}

          <DialogFooter>
            <Button type="submit" disabled={adjustMutation.isPending}>
              {adjustMutation.isPending ? "저장 중..." : "저장"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
