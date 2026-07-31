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
import { Checkbox } from "@/components/ui/checkbox"
import { Button } from "@/components/ui/button"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { useAllCommonCodes } from "@/features/common-codes/hooks"
import { useAllProductUnits } from "@/features/product-units/hooks"
import { useCreateProduct, useUpdateProduct } from "@/features/products/hooks"
import type { ProductItem } from "@/features/products/types"

const NO_SUB_UNIT = "none"

interface ProductFormDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  target: ProductItem | null
}

export function ProductFormDialog({ open, onOpenChange, target }: ProductFormDialogProps) {
  const [skuCode, setSkuCode] = useState("")
  const [name, setName] = useState("")
  const [categoryId, setCategoryId] = useState("")
  const [storageTypeId, setStorageTypeId] = useState("")
  const [baseUnitId, setBaseUnitId] = useState("")
  const [subUnitId, setSubUnitId] = useState(NO_SUB_UNIT)
  const [unitConversionRate, setUnitConversionRate] = useState("")
  const [lotTracking, setLotTracking] = useState(false)
  const [description, setDescription] = useState("")
  const [error, setError] = useState("")

  const { data: categories } = useAllCommonCodes("company", "PRODUCT_CATEGORY")
  const { data: storageTypes } = useAllCommonCodes("company", "STORAGE_TYPE")
  const { data: units } = useAllProductUnits()

  const createMutation = useCreateProduct()
  const updateMutation = useUpdateProduct()
  const isSubmitting = createMutation.isPending || updateMutation.isPending

  useEffect(() => {
    if (!open) return
    setSkuCode(target?.skuCode ?? "")
    setName(target?.name ?? "")
    setCategoryId(target ? String(target.categoryId) : "")
    setStorageTypeId(target ? String(target.storageTypeId) : "")
    setBaseUnitId(target ? String(target.baseUnitId) : "")
    setSubUnitId(target?.subUnitId ? String(target.subUnitId) : NO_SUB_UNIT)
    setUnitConversionRate(target?.unitConversionRate ? String(target.unitConversionRate) : "")
    setLotTracking(target?.lotTracking ?? false)
    setDescription(target?.description ?? "")
    setError("")
  }, [open, target])

  function handleSubUnitChange(value: string) {
    setSubUnitId(value)
    if (value === NO_SUB_UNIT) setUnitConversionRate("")
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError("")

    const input = {
      name,
      categoryId: Number(categoryId),
      storageTypeId: Number(storageTypeId),
      baseUnitId: Number(baseUnitId),
      subUnitId: subUnitId === NO_SUB_UNIT ? null : Number(subUnitId),
      unitConversionRate:
        subUnitId === NO_SUB_UNIT ? null : Number(unitConversionRate),
      lotTracking,
      description: description.trim() ? description : null,
    }

    try {
      if (target) {
        await updateMutation.mutateAsync({ id: target.id, input })
      } else {
        await createMutation.mutateAsync({ skuCode, ...input })
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
            <DialogTitle>{target ? "상품 수정" : "상품 등록"}</DialogTitle>
          </DialogHeader>

          <div className="flex flex-col gap-3">
            <div className="flex flex-col gap-1.5">
              <label htmlFor="product-sku" className="text-sm font-medium">
                SKU 코드
              </label>
              <Input
                id="product-sku"
                value={skuCode}
                onChange={(e) => setSkuCode(e.target.value)}
                disabled={!!target}
                required
                placeholder="예: SKU-0001"
              />
            </div>

            <div className="flex flex-col gap-1.5">
              <label htmlFor="product-name" className="text-sm font-medium">
                상품명
              </label>
              <Input
                id="product-name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-medium">카테고리</label>
              <Select value={categoryId} onValueChange={setCategoryId} required>
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="카테고리 선택" />
                </SelectTrigger>
                <SelectContent>
                  {categories?.map((c) => (
                    <SelectItem key={c.id} value={String(c.id)}>
                      {c.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
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
              <label className="text-sm font-medium">기본 단위</label>
              <Select value={baseUnitId} onValueChange={setBaseUnitId} required>
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="기본 단위 선택" />
                </SelectTrigger>
                <SelectContent>
                  {units?.map((u) => (
                    <SelectItem key={u.id} value={String(u.id)}>
                      {u.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-medium">보조 단위</label>
              <Select value={subUnitId} onValueChange={handleSubUnitChange}>
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="보조 단위 선택" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={NO_SUB_UNIT}>없음</SelectItem>
                  {units
                    ?.filter((u) => String(u.id) !== baseUnitId)
                    .map((u) => (
                      <SelectItem key={u.id} value={String(u.id)}>
                        {u.name}
                      </SelectItem>
                    ))}
                </SelectContent>
              </Select>
            </div>

            {subUnitId !== NO_SUB_UNIT && (
              <div className="flex flex-col gap-1.5">
                <label htmlFor="product-conversion-rate" className="text-sm font-medium">
                  변환율 (보조 단위 1개 = 기본 단위 몇 개)
                </label>
                <Input
                  id="product-conversion-rate"
                  type="number"
                  step="0.0001"
                  value={unitConversionRate}
                  onChange={(e) => setUnitConversionRate(e.target.value)}
                  required
                  placeholder="예: 24"
                />
              </div>
            )}

            <label className="flex items-center gap-2 text-sm font-medium">
              <Checkbox
                checked={lotTracking}
                onCheckedChange={(checked) => setLotTracking(checked === true)}
              />
              LOT 추적
            </label>

            <div className="flex flex-col gap-1.5">
              <label htmlFor="product-description" className="text-sm font-medium">
                설명
              </label>
              <Textarea
                id="product-description"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
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
