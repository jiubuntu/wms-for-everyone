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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { useAllCommonCodes } from "@/features/common-codes/hooks"
import { useCreateWarehouse, useUpdateWarehouse } from "@/features/warehouses/hooks"
import type { WarehouseItem } from "@/features/warehouses/types"

interface WarehouseFormDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  target: WarehouseItem | null
}

export function WarehouseFormDialog({ open, onOpenChange, target }: WarehouseFormDialogProps) {
  const [name, setName] = useState("")
  const [storageTypeId, setStorageTypeId] = useState("")
  const [address, setAddress] = useState("")
  const [error, setError] = useState("")

  const { data: storageTypes } = useAllCommonCodes("company", "STORAGE_TYPE")

  const createMutation = useCreateWarehouse()
  const updateMutation = useUpdateWarehouse()
  const isSubmitting = createMutation.isPending || updateMutation.isPending

  useEffect(() => {
    if (!open) return
    setName(target?.name ?? "")
    setStorageTypeId(target ? String(target.storageTypeId) : "")
    setAddress(target?.address ?? "")
    setError("")
  }, [open, target])

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError("")

    const input = {
      name,
      storageTypeId: Number(storageTypeId),
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
              <label className="text-sm font-medium">보관 유형</label>
              <Select value={storageTypeId} onValueChange={setStorageTypeId} required>
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="보관 유형 선택" />
                </SelectTrigger>
                <SelectContent>
                  {storageTypes?.map((s) => (
                    <SelectItem key={s.id} value={String(s.id)}>
                      {s.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
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
