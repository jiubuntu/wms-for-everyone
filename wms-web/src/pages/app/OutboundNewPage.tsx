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
import { useCreateOutbound } from "@/features/outbound/hooks"
import { OutboundItemLineEditor } from "@/features/outbound/components/OutboundItemLineEditor"
import {
  createEmptyLine,
  isLineValid,
  toLineInput,
  type OutboundLineDraft,
} from "@/features/outbound/lineDraft"
import { createIdempotencyKey } from "@/lib/idempotency"

export function OutboundNewPage() {
  const { warehouseId } = useWarehouseFilter()
  const navigate = useNavigate()

  const [customerName, setCustomerName] = useState("")
  const [note, setNote] = useState("")
  const [lines, setLines] = useState<OutboundLineDraft[]>([createEmptyLine()])
  const [error, setError] = useState("")
  const [idempotencyKey] = useState(() => createIdempotencyKey())

  const createMutation = useCreateOutbound(warehouseId)

  function updateLine(key: string, next: OutboundLineDraft) {
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
          customerName,
          note: note.trim() ? note : null,
          items: lines.map(toLineInput),
        },
        idempotencyKey,
      })
      navigate(`/app/outbounds/${result.id}`)
    } catch (err) {
      const message = isAxiosError(err) ? err.response?.data?.message : null
      setError(message ?? "출고를 등록하지 못했습니다.")
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <PageHeader
        title="출고 등록"
        breadcrumb={[
          { label: "홈", to: "/app" },
          { label: "출고 관리", to: "/app/outbounds" },
          { label: "출고 등록" },
        ]}
      />

      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <Card>
          <CardContent className="flex flex-col gap-3">
            <div className="flex flex-col gap-1.5">
              <label htmlFor="outbound-customer" className="text-sm font-medium">
                고객사명
              </label>
              <Input
                id="outbound-customer"
                value={customerName}
                onChange={(e) => setCustomerName(e.target.value)}
                required
              />
            </div>
            <div className="flex flex-col gap-1.5">
              <label htmlFor="outbound-note" className="text-sm font-medium">
                비고
              </label>
              <Textarea
                id="outbound-note"
                value={note}
                onChange={(e) => setNote(e.target.value)}
                placeholder="선택 입력"
              />
            </div>
          </CardContent>
        </Card>

        <div className="flex flex-col gap-3">
          {warehouseId &&
            lines.map((line, index) => (
              <OutboundItemLineEditor
                key={line.key}
                warehouseId={warehouseId}
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
            {createMutation.isPending ? "등록 중..." : "출고 등록"}
          </Button>
        </div>
      </form>
    </div>
  )
}
