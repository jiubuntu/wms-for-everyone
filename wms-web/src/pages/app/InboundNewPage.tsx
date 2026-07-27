import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { isAxiosError } from "axios"
import { Plus } from "lucide-react"
import { PageHeader } from "@/components/common/PageHeader"
import { Card, CardContent } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Button } from "@/components/ui/button"
import { useWarehouseFilter } from "@/contexts/WarehouseFilterContext"
import { useCreateInbound } from "@/features/inbound/hooks"
import { InboundItemLineEditor } from "@/features/inbound/components/InboundItemLineEditor"
import {
  createEmptyLine,
  isLineValid,
  toLineInput,
  type InboundLineDraft,
} from "@/features/inbound/lineDraft"
import { createIdempotencyKey } from "@/lib/idempotency"

export function InboundNewPage() {
  const { warehouseId } = useWarehouseFilter()
  const navigate = useNavigate()

  const [supplierName, setSupplierName] = useState("")
  const [note, setNote] = useState("")
  const [lines, setLines] = useState<InboundLineDraft[]>([createEmptyLine()])
  const [error, setError] = useState("")
  const [idempotencyKey] = useState(() => createIdempotencyKey())

  const createMutation = useCreateInbound(warehouseId)

  function updateLine(key: string, next: InboundLineDraft) {
    setLines((prev) => prev.map((l) => (l.key === key ? next : l)))
  }

  function removeLine(key: string) {
    setLines((prev) => prev.filter((l) => l.key !== key))
  }

  function addLine() {
    setLines((prev) => [...prev, createEmptyLine()])
  }

  const allLinesValid = lines.length > 0 && lines.every(isLineValid)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError("")
    if (!warehouseId) return

    try {
      const result = await createMutation.mutateAsync({
        input: {
          supplierName,
          note: note.trim() ? note : null,
          items: lines.map(toLineInput),
        },
        idempotencyKey,
      })
      navigate(`/app/inbounds/${result.id}`)
    } catch (err) {
      const message = isAxiosError(err) ? err.response?.data?.message : null
      setError(message ?? "입고를 등록하지 못했습니다.")
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <PageHeader
        title="입고 등록"
        breadcrumb={[
          { label: "홈", to: "/app" },
          { label: "입고 관리", to: "/app/inbounds" },
          { label: "입고 등록" },
        ]}
      />

      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <Card>
          <CardContent className="flex flex-col gap-3">
            <div className="flex flex-col gap-1.5">
              <label htmlFor="inbound-supplier" className="text-sm font-medium">
                공급업체명
              </label>
              <Input
                id="inbound-supplier"
                value={supplierName}
                onChange={(e) => setSupplierName(e.target.value)}
                required
              />
            </div>
            <div className="flex flex-col gap-1.5">
              <label htmlFor="inbound-note" className="text-sm font-medium">
                비고
              </label>
              <Textarea
                id="inbound-note"
                value={note}
                onChange={(e) => setNote(e.target.value)}
                placeholder="선택 입력"
              />
            </div>
          </CardContent>
        </Card>

        <div className="flex flex-col gap-3">
          {lines.map((line, index) => (
            <InboundItemLineEditor
              key={line.key}
              index={index}
              value={line}
              onChange={(next) => updateLine(line.key, next)}
              onRemove={() => removeLine(line.key)}
            />
          ))}

          <Button type="button" variant="outline" onClick={addLine} className="w-fit">
            <Plus className="size-4" />
            상품 추가
          </Button>
        </div>

        {error && <p className="text-sm text-destructive">{error}</p>}

        <div className="flex justify-end">
          <Button type="submit" disabled={!allLinesValid || createMutation.isPending || !warehouseId}>
            {createMutation.isPending ? "등록 중..." : "입고 등록"}
          </Button>
        </div>
      </form>
    </div>
  )
}
