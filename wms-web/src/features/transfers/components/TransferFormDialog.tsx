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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { useAllCommonCodes } from "@/features/common-codes/hooks"
import { useAllProducts } from "@/features/products/hooks"
import { useAllLocations } from "@/features/locations/hooks"
import { useAvailableLocations } from "@/features/inventory/hooks"
import { useCreateTransfer } from "@/features/transfers/hooks"
import { createIdempotencyKey } from "@/lib/idempotency"

const NO_REASON = "none"

interface TransferFormDialogProps {
  warehouseId: number
  open: boolean
  onOpenChange: (open: boolean) => void
}

function fromKeyOf(locationId: number, lotNumber: string | null) {
  return `${locationId}::${lotNumber ?? ""}`
}

export function TransferFormDialog({ warehouseId, open, onOpenChange }: TransferFormDialogProps) {
  const [productId, setProductId] = useState("")
  const [fromKey, setFromKey] = useState("")
  const [toLocationId, setToLocationId] = useState("")
  const [quantity, setQuantity] = useState("")
  const [reasonId, setReasonId] = useState(NO_REASON)
  const [note, setNote] = useState("")
  const [error, setError] = useState("")
  const [idempotencyKey, setIdempotencyKey] = useState("")

  const { data: products } = useAllProducts()
  const { data: reasons } = useAllCommonCodes("company", "TRANSFER_REASON")
  const { data: availableLocations } = useAvailableLocations(
    warehouseId,
    productId ? Number(productId) : null
  )
  const { data: allLocations } = useAllLocations(warehouseId)

  const createMutation = useCreateTransfer(warehouseId)

  useEffect(() => {
    if (!open) return
    setProductId("")
    setFromKey("")
    setToLocationId("")
    setQuantity("")
    setReasonId(NO_REASON)
    setNote("")
    setError("")
    setIdempotencyKey(createIdempotencyKey())
  }, [open])

  useEffect(() => {
    setFromKey("")
    setQuantity("")
  }, [productId])

  const selectedFrom = availableLocations?.find(
    (loc) => fromKeyOf(loc.locationId, loc.lotNumber) === fromKey
  )
  const destinationOptions = (allLocations ?? []).filter(
    (loc) => loc.status === "ACTIVE" && loc.id !== selectedFrom?.locationId
  )

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError("")
    if (!selectedFrom) return

    const input = {
      productId: Number(productId),
      fromLocationId: selectedFrom.locationId,
      toLocationId: Number(toLocationId),
      lotNumber: selectedFrom.lotNumber,
      quantity: Number(quantity),
      reasonId: reasonId === NO_REASON ? null : Number(reasonId),
      note: note.trim() ? note : null,
    }

    try {
      await createMutation.mutateAsync({ input, idempotencyKey })
      onOpenChange(false)
    } catch (err) {
      const message = isAxiosError(err) ? err.response?.data?.message : null
      setError(message ?? "재고를 이동하지 못했습니다.")
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <DialogHeader>
            <DialogTitle>재고 이동</DialogTitle>
          </DialogHeader>

          <div className="flex flex-col gap-3">
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-medium">상품</label>
              <Select value={productId} onValueChange={setProductId} required>
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="상품 선택" />
                </SelectTrigger>
                <SelectContent>
                  {products?.map((p) => (
                    <SelectItem key={p.id} value={String(p.id)}>
                      {p.name} ({p.skuCode})
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-medium">출발 위치</label>
              <Select value={fromKey} onValueChange={setFromKey} required disabled={!productId}>
                <SelectTrigger className="w-full">
                  <SelectValue placeholder={productId ? "출발 위치 선택" : "상품을 먼저 선택하세요"} />
                </SelectTrigger>
                <SelectContent>
                  {availableLocations?.length === 0 && (
                    <p className="px-2 py-1.5 text-sm text-muted-foreground">가용재고가 없습니다.</p>
                  )}
                  {availableLocations?.map((loc) => (
                    <SelectItem
                      key={fromKeyOf(loc.locationId, loc.lotNumber)}
                      value={fromKeyOf(loc.locationId, loc.lotNumber)}
                    >
                      {loc.locationCode}
                      {loc.lotNumber && ` / ${loc.lotNumber}`} (가용 {loc.availableQuantity})
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-medium">도착 위치</label>
              <Select
                value={toLocationId}
                onValueChange={setToLocationId}
                required
                disabled={!selectedFrom}
              >
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="도착 위치 선택" />
                </SelectTrigger>
                <SelectContent>
                  {destinationOptions.map((loc) => (
                    <SelectItem key={loc.id} value={String(loc.id)}>
                      {loc.code}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="flex flex-col gap-1.5">
              <label htmlFor="transfer-quantity" className="text-sm font-medium">
                수량{selectedFrom && ` (최대 ${selectedFrom.availableQuantity})`}
              </label>
              <Input
                id="transfer-quantity"
                type="number"
                min={1}
                max={selectedFrom?.availableQuantity}
                value={quantity}
                onChange={(e) => setQuantity(e.target.value)}
                required
                disabled={!selectedFrom}
              />
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-medium">사유</label>
              <Select value={reasonId} onValueChange={setReasonId}>
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="사유 선택 (선택)" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={NO_REASON}>선택 안 함</SelectItem>
                  {reasons?.map((r) => (
                    <SelectItem key={r.id} value={String(r.id)}>
                      {r.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="flex flex-col gap-1.5">
              <label htmlFor="transfer-note" className="text-sm font-medium">
                비고
              </label>
              <Textarea
                id="transfer-note"
                value={note}
                onChange={(e) => setNote(e.target.value)}
                placeholder="선택 입력"
              />
            </div>

            {error && <p className="text-sm text-destructive">{error}</p>}
          </div>

          <DialogFooter>
            <Button type="submit" disabled={createMutation.isPending}>
              {createMutation.isPending ? "이동 중..." : "이동"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
