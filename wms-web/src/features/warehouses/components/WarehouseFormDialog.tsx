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
import { useCreateWarehouse, useUpdateWarehouse } from "@/features/warehouses/hooks"
import type { WarehouseItem } from "@/features/warehouses/types"

interface WarehouseFormDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  target: WarehouseItem | null
}

export function WarehouseFormDialog({ open, onOpenChange, target }: WarehouseFormDialogProps) {
  const [name, setName] = useState("")
  const [address, setAddress] = useState("")
  const [error, setError] = useState("")

  const createMutation = useCreateWarehouse()
  const updateMutation = useUpdateWarehouse()
  const isSubmitting = createMutation.isPending || updateMutation.isPending

  useEffect(() => {
    if (!open) return
    setName(target?.name ?? "")
    setAddress(target?.address ?? "")
    setError("")
  }, [open, target])

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError("")

    const input = {
      name,
      address: address.trim() ? address : null,
    }

    try {
      if (target) {
        await updateMutation.mutateAsync({ id: target.id, input })
      } else {
        await createMutation.mutateAsync(input)
      }
      onOpenChange(false)
    } catch (err) {
      const message = isAxiosError(err) ? err.response?.data?.message : null
      setError(message ?? "저장하지 못했습니다.")
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <DialogHeader>
            <DialogTitle>{target ? "창고 수정" : "창고 등록"}</DialogTitle>
          </DialogHeader>

          <div className="flex flex-col gap-3">
            <div className="flex flex-col gap-1.5">
              <label htmlFor="warehouse-name" className="text-sm font-medium">
                창고명
              </label>
              <Input
                id="warehouse-name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
            </div>

            <div className="flex flex-col gap-1.5">
              <label htmlFor="warehouse-address" className="text-sm font-medium">
                주소
              </label>
              <Input
                id="warehouse-address"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                placeholder="선택 입력"
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
