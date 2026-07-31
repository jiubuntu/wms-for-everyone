import { useEffect, useState } from "react"
import { isAxiosError } from "axios"
import { Plus, Trash2 } from "lucide-react"
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
import { useAllLocations } from "@/features/locations/hooks"
import { useReplaceInboundItemLocations } from "@/features/inbound/hooks"
import type { InboundItemDetail } from "@/features/inbound/types"

interface LocationRow {
  key: string
  locationId: string
  quantity: string
}

interface InboundLocationAssignDialogProps {
  warehouseId: number
  inboundId: number
  item: InboundItemDetail
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function InboundLocationAssignDialog({
  warehouseId,
  inboundId,
  item,
  open,
  onOpenChange,
}: InboundLocationAssignDialogProps) {
  const [rows, setRows] = useState<LocationRow[]>([])
  const [error, setError] = useState("")

  const { data: locations } = useAllLocations(warehouseId)
  const replaceMutation = useReplaceInboundItemLocations(warehouseId, inboundId)

  useEffect(() => {
    if (!open) return
    setError("")
    setRows(
      item.locations.length > 0
        ? item.locations.map((loc) => ({
            key: crypto.randomUUID(),
            locationId: String(loc.locationId),
            quantity: String(loc.quantity),
          }))
        : [{ key: crypto.randomUUID(), locationId: "", quantity: "" }]
    )
  }, [open, item.locations])

  function updateRow(key: string, partial: Partial<LocationRow>) {
    setRows((prev) => prev.map((r) => (r.key === key ? { ...r, ...partial } : r)))
  }

  function addRow() {
    setRows((prev) => [...prev, { key: crypto.randomUUID(), locationId: "", quantity: "" }])
  }

  function removeRow(key: string) {
    setRows((prev) => prev.filter((r) => r.key !== key))
  }

  const total = rows.reduce((sum, r) => sum + (Number(r.quantity) || 0), 0)
  const rowsFilled = rows.length > 0 && rows.every((r) => r.locationId && Number(r.quantity) > 0)
  const noDuplicateLocations = new Set(rows.map((r) => r.locationId)).size === rows.length
  const canSave = rowsFilled && noDuplicateLocations && total === item.quantity

  async function handleSave() {
    setError("")
    try {
      await replaceMutation.mutateAsync({
        itemId: item.id,
        locations: rows.map((r) => ({
          locationId: Number(r.locationId),
          quantity: Number(r.quantity),
        })),
      })
      onOpenChange(false)
    } catch (err) {
      const message = isAxiosError(err) ? err.response?.data?.message : null
      setError(message ?? "위치 배치를 저장하지 못했습니다.")
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>
            위치 배치 — {item.productName} ({total} / {item.quantity})
          </DialogTitle>
        </DialogHeader>

        <div className="flex flex-col gap-2">
          {rows.map((row) => (
            <div key={row.key} className="flex items-center gap-2">
              <Select
                value={row.locationId}
                onValueChange={(v) => updateRow(row.key, { locationId: v })}
              >
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="위치 선택" />
                </SelectTrigger>
                <SelectContent>
                  {locations
                    ?.filter((loc) => loc.status === "ACTIVE")
                    .map((loc) => (
                      <SelectItem key={loc.id} value={String(loc.id)}>
                        {loc.code}
                      </SelectItem>
                    ))}
                </SelectContent>
              </Select>
              <Input
                type="number"
                min={1}
                className="w-24"
                value={row.quantity}
                onChange={(e) => updateRow(row.key, { quantity: e.target.value })}
                placeholder="수량"
              />
              <Button
                type="button"
                variant="ghost"
                size="icon"
                className="size-8 rounded-full bg-destructive/10 text-destructive hover:bg-destructive/20 hover:text-destructive"
                onClick={() => removeRow(row.key)}
                aria-label="위치 삭제"
                disabled={rows.length === 1}
              >
                <Trash2 className="size-4" />
              </Button>
            </div>
          ))}

          <Button type="button" variant="outline" onClick={addRow} className="w-fit">
            <Plus className="size-4" />
            위치 추가
          </Button>

          <p className="text-xs text-muted-foreground">
            배치 합계가 상품 라인 수량과 정확히 같아야 저장할 수 있습니다.
          </p>

          {error && <p className="text-sm text-destructive">{error}</p>}
        </div>

        <DialogFooter>
          <Button
            type="button"
            onClick={handleSave}
            disabled={!canSave || replaceMutation.isPending}
          >
            {replaceMutation.isPending ? "저장 중..." : "저장"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
