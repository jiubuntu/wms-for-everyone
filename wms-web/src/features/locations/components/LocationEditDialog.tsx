import { useEffect, useState } from "react"
import { isAxiosError } from "axios"
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { useAllCommonCodes } from "@/features/common-codes/hooks"
import { useUpdateLocation } from "@/features/locations/hooks"
import type { LocationItem, LocationStatus } from "@/features/locations/types"

const FOLLOW_WAREHOUSE = "follow-warehouse"

interface LocationEditDialogProps {
  warehouseId: number
  open: boolean
  onOpenChange: (open: boolean) => void
  target: LocationItem | null
}

export function LocationEditDialog({
  warehouseId,
  open,
  onOpenChange,
  target,
}: LocationEditDialogProps) {
  const [storageTypeId, setStorageTypeId] = useState(FOLLOW_WAREHOUSE)
  const [status, setStatus] = useState<LocationStatus>("ACTIVE")
  const [error, setError] = useState("")

  const { data: storageTypes } = useAllCommonCodes("company", "STORAGE_TYPE")
  const updateMutation = useUpdateLocation(warehouseId)

  useEffect(() => {
    if (!open || !target) return
    setStorageTypeId(target.storageTypeId ? String(target.storageTypeId) : FOLLOW_WAREHOUSE)
    setStatus(target.status)
    setError("")
  }, [open, target])

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!target) return
    setError("")

    const input = {
      storageTypeId: storageTypeId === FOLLOW_WAREHOUSE ? null : Number(storageTypeId),
      status,
    }

    try {
      await updateMutation.mutateAsync({ id: target.id, input })
      onOpenChange(false)
    } catch (err) {
      const message = isAxiosError(err) ? err.response?.data?.message : null
      setError(message ?? "저장하지 못했습니다.")
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <DialogHeader>
            <DialogTitle>위치 수정 {target && `— ${target.code}`}</DialogTitle>
          </DialogHeader>

          <div className="flex flex-col gap-3">
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-medium">보관 유형</label>
              <Select value={storageTypeId} onValueChange={setStorageTypeId}>
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="보관 유형 선택" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={FOLLOW_WAREHOUSE}>창고 기본값 따름</SelectItem>
                  {storageTypes?.map((s) => (
                    <SelectItem key={s.id} value={String(s.id)}>
                      {s.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-medium">상태</label>
              <Select value={status} onValueChange={(v) => setStatus(v as LocationStatus)}>
                <SelectTrigger className="w-full">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ACTIVE">사용</SelectItem>
                  <SelectItem value="DISABLED">사용 중지</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {error && <p className="text-sm text-destructive">{error}</p>}
          </div>

          <DialogFooter>
            <Button type="submit" disabled={updateMutation.isPending}>
              {updateMutation.isPending ? "저장 중..." : "저장"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
