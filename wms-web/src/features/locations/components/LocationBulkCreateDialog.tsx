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
import { useBulkCreateLocations } from "@/features/locations/hooks"

const FOLLOW_WAREHOUSE = "follow-warehouse"

interface LocationBulkCreateDialogProps {
  warehouseId: number
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function LocationBulkCreateDialog({
  warehouseId,
  open,
  onOpenChange,
}: LocationBulkCreateDialogProps) {
  const [zone, setZone] = useState("")
  const [rowFrom, setRowFrom] = useState("1")
  const [rowTo, setRowTo] = useState("")
  const [colFrom, setColFrom] = useState("1")
  const [colTo, setColTo] = useState("")
  const [levelCount, setLevelCount] = useState("")
  const [storageTypeId, setStorageTypeId] = useState(FOLLOW_WAREHOUSE)
  const [error, setError] = useState("")

  const { data: storageTypes } = useAllCommonCodes("company", "STORAGE_TYPE")
  const bulkCreateMutation = useBulkCreateLocations(warehouseId)

  useEffect(() => {
    if (!open) return
    setZone("")
    setRowFrom("1")
    setRowTo("")
    setColFrom("1")
    setColTo("")
    setLevelCount("")
    setStorageTypeId(FOLLOW_WAREHOUSE)
    setError("")
  }, [open])

  const rowCount = Number(rowTo) - Number(rowFrom) + 1
  const colCount = Number(colTo) - Number(colFrom) + 1
  const previewCount =
    rowCount > 0 && colCount > 0 && Number(levelCount) > 0
      ? rowCount * colCount * Number(levelCount)
      : 0

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError("")

    const input = {
      zone,
      rowFrom: Number(rowFrom),
      rowTo: Number(rowTo),
      colFrom: Number(colFrom),
      colTo: Number(colTo),
      levelCount: Number(levelCount),
      storageTypeId: storageTypeId === FOLLOW_WAREHOUSE ? null : Number(storageTypeId),
    }

    try {
      await bulkCreateMutation.mutateAsync(input)
      onOpenChange(false)
    } catch (err) {
      const message = isAxiosError(err) ? err.response?.data?.message : null
      setError(message ?? "위치를 생성하지 못했습니다.")
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <DialogHeader>
            <DialogTitle>위치 일괄 생성</DialogTitle>
          </DialogHeader>

          <div className="flex flex-col gap-3">
            <div className="flex flex-col gap-1.5">
              <label htmlFor="location-zone" className="text-sm font-medium">
                구역
              </label>
              <Input
                id="location-zone"
                value={zone}
                onChange={(e) => setZone(e.target.value)}
                required
                placeholder="예: A"
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="flex flex-col gap-1.5">
                <label htmlFor="location-row-from" className="text-sm font-medium">
                  행 시작
                </label>
                <Input
                  id="location-row-from"
                  type="number"
                  min={1}
                  value={rowFrom}
                  onChange={(e) => setRowFrom(e.target.value)}
                  required
                />
              </div>
              <div className="flex flex-col gap-1.5">
                <label htmlFor="location-row-to" className="text-sm font-medium">
                  행 끝
                </label>
                <Input
                  id="location-row-to"
                  type="number"
                  min={1}
                  value={rowTo}
                  onChange={(e) => setRowTo(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="flex flex-col gap-1.5">
                <label htmlFor="location-col-from" className="text-sm font-medium">
                  열 시작
                </label>
                <Input
                  id="location-col-from"
                  type="number"
                  min={1}
                  value={colFrom}
                  onChange={(e) => setColFrom(e.target.value)}
                  required
                />
              </div>
              <div className="flex flex-col gap-1.5">
                <label htmlFor="location-col-to" className="text-sm font-medium">
                  열 끝
                </label>
                <Input
                  id="location-col-to"
                  type="number"
                  min={1}
                  value={colTo}
                  onChange={(e) => setColTo(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="flex flex-col gap-1.5">
              <label htmlFor="location-level-count" className="text-sm font-medium">
                단 수
              </label>
              <Input
                id="location-level-count"
                type="number"
                min={1}
                value={levelCount}
                onChange={(e) => setLevelCount(e.target.value)}
                required
                placeholder="예: 3"
              />
            </div>

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

            {previewCount > 0 && (
              <p className="text-sm text-muted-foreground">
                총 <span className="font-medium text-foreground">{previewCount}개</span>의 위치가
                생성됩니다.
              </p>
            )}

            {error && <p className="text-sm text-destructive">{error}</p>}
          </div>

          <DialogFooter>
            <Button type="submit" disabled={bulkCreateMutation.isPending}>
              {bulkCreateMutation.isPending ? "생성 중..." : "생성"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
