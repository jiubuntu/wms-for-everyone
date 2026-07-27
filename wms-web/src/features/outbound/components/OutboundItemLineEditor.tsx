import { useMemo } from "react"
import { Trash2 } from "lucide-react"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { useAllProducts } from "@/features/products/hooks"
import { useAllProductUnits } from "@/features/product-units/hooks"
import { useAvailableLocations } from "@/features/inventory/hooks"
import { fromKeyOf, manualTotal, type OutboundLineDraft } from "@/features/outbound/lineDraft"
import type { AllocationType } from "@/features/outbound/types"

interface OutboundItemLineEditorProps {
  warehouseId: number
  index: number
  value: OutboundLineDraft
  onChange: (value: OutboundLineDraft) => void
  onRemove: () => void
}

export function OutboundItemLineEditor({
  warehouseId,
  index,
  value,
  onChange,
  onRemove,
}: OutboundItemLineEditorProps) {
  const { data: products } = useAllProducts()
  const { data: units } = useAllProductUnits()
  const product = products?.find((p) => String(p.id) === value.productId)

  const { data: availableLocations } = useAvailableLocations(
    warehouseId,
    value.productId ? Number(value.productId) : null
  )

  const unitOptions = useMemo(() => {
    if (!product || !units) return []
    const unitById = new Map(units.map((u) => [u.id, u.name]))
    const options = [
      { id: product.baseUnitId, label: `기본 단위: ${unitById.get(product.baseUnitId) ?? "-"}` },
    ]
    if (product.subUnitId) {
      options.push({
        id: product.subUnitId,
        label: `보조 단위: ${unitById.get(product.subUnitId) ?? "-"}`,
      })
    }
    return options
  }, [product, units])

  function patch(partial: Partial<OutboundLineDraft>) {
    onChange({ ...value, ...partial })
  }

  function handleProductChange(productId: string) {
    onChange({ ...value, productId, unitId: "", manualQuantities: {} })
  }

  function handleManualQuantityChange(key: string, qty: string) {
    patch({ manualQuantities: { ...value.manualQuantities, [key]: qty } })
  }

  const total = manualTotal(value)

  return (
    <div className="flex flex-col gap-3 rounded-lg border border-border bg-card p-4">
      <div className="flex items-center justify-between">
        <span className="text-sm font-medium">상품 {index + 1}</span>
        <Button
          type="button"
          variant="ghost"
          size="icon"
          className="size-8 rounded-full bg-destructive/10 text-destructive hover:bg-destructive/20 hover:text-destructive"
          onClick={onRemove}
          aria-label="라인 삭제"
        >
          <Trash2 className="size-4" />
        </Button>
      </div>

      <div className="grid grid-cols-2 gap-3">
        <div className="flex flex-col gap-1.5">
          <label className="text-sm font-medium">상품</label>
          <Select value={value.productId} onValueChange={handleProductChange} required>
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
          <label className="text-sm font-medium">단위</label>
          <Select
            value={value.unitId}
            onValueChange={(v) => patch({ unitId: v })}
            required
            disabled={!product}
          >
            <SelectTrigger className="w-full">
              <SelectValue placeholder="단위 선택" />
            </SelectTrigger>
            <SelectContent>
              {unitOptions.map((opt) => (
                <SelectItem key={opt.id} value={String(opt.id)}>
                  {opt.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="flex flex-col gap-1.5">
          <label className="text-sm font-medium">수량</label>
          <Input
            type="number"
            min={1}
            value={value.quantity}
            onChange={(e) => patch({ quantity: e.target.value })}
            required
          />
        </div>

        <div className="flex flex-col gap-1.5">
          <label className="text-sm font-medium">할당 방식</label>
          <Select
            value={value.allocationType}
            onValueChange={(v) =>
              patch({ allocationType: v as AllocationType, manualQuantities: {} })
            }
          >
            <SelectTrigger className="w-full">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="FEFO">FEFO (자동, 유효기간 임박 순)</SelectItem>
              <SelectItem value="MANUAL">MANUAL (직접 선택)</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      {value.allocationType === "MANUAL" && (
        <div className="flex flex-col gap-2 rounded-[0.3rem] bg-muted/50 p-3">
          <p className="text-xs text-muted-foreground">
            위치별로 가져올 수량을 입력하세요. 합계가 요청 수량과 같아야 합니다. (배분 {total} / 필요{" "}
            {Number(value.quantity) || 0})
          </p>
          {!value.productId && (
            <p className="text-sm text-muted-foreground">상품을 먼저 선택하세요.</p>
          )}
          {value.productId && availableLocations?.length === 0 && (
            <p className="text-sm text-muted-foreground">가용재고가 없습니다.</p>
          )}
          {availableLocations?.map((loc) => {
            const key = fromKeyOf(loc.locationId, loc.lotNumber)
            return (
              <div key={key} className="flex items-center justify-between gap-3 text-sm">
                <span>
                  {loc.locationCode}
                  {loc.lotNumber && ` / ${loc.lotNumber}`}
                  <span className="ml-1.5 text-xs text-muted-foreground">
                    (가용 {loc.availableQuantity})
                  </span>
                </span>
                <Input
                  type="number"
                  min={0}
                  max={loc.availableQuantity}
                  className="w-24"
                  value={value.manualQuantities[key] ?? ""}
                  onChange={(e) => handleManualQuantityChange(key, e.target.value)}
                  placeholder="0"
                />
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
