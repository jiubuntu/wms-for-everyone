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
import type { InboundLineDraft } from "@/features/inbound/lineDraft"

interface InboundItemLineEditorProps {
  index: number
  value: InboundLineDraft
  onChange: (value: InboundLineDraft) => void
  onRemove: () => void
}

export function InboundItemLineEditor({
  index,
  value,
  onChange,
  onRemove,
}: InboundItemLineEditorProps) {
  const { data: products } = useAllProducts()
  const { data: units } = useAllProductUnits()
  const product = products?.find((p) => String(p.id) === value.productId)

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

  function patch(partial: Partial<InboundLineDraft>) {
    onChange({ ...value, ...partial })
  }

  function handleProductChange(productId: string) {
    const selected = products?.find((p) => String(p.id) === productId)
    onChange({
      ...value,
      productId,
      unitId: "",
      lotTracking: selected?.lotTracking ?? false,
      lotNumber: "",
      manufactureDate: "",
      expiryDate: "",
    })
  }

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
      </div>

      {value.lotTracking && (
        <div className="grid grid-cols-3 gap-3 rounded-[0.3rem] bg-muted/50 p-3">
          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium">로트번호</label>
            <Input
              value={value.lotNumber}
              onChange={(e) => patch({ lotNumber: e.target.value })}
              required
            />
          </div>
          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium">제조일자</label>
            <Input
              type="date"
              value={value.manufactureDate}
              onChange={(e) => patch({ manufactureDate: e.target.value })}
              required
            />
          </div>
          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium">유통기한</label>
            <Input
              type="date"
              value={value.expiryDate}
              onChange={(e) => patch({ expiryDate: e.target.value })}
              required
            />
          </div>
        </div>
      )}
    </div>
  )
}
