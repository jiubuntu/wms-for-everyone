import { useState } from "react"
import { useParams } from "react-router-dom"
import { isAxiosError } from "axios"
import dayjs from "dayjs"
import { MapPin } from "lucide-react"
import { PageHeader } from "@/components/common/PageHeader"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { useWarehouseFilter } from "@/contexts/WarehouseFilterContext"
import { useCancelInbound, useCompleteInbound, useInbound } from "@/features/inbound/hooks"
import { InboundStatusBadge } from "@/features/inbound/components/InboundStatusBadge"
import { InboundLocationAssignDialog } from "@/features/inbound/components/InboundLocationAssignDialog"
import { createIdempotencyKey } from "@/lib/idempotency"

export function InboundDetailPage() {
  const { id } = useParams<{ id: string }>()
  const inboundId = Number(id)
  const { warehouseId } = useWarehouseFilter()
  const [error, setError] = useState("")
  const [assigningItemId, setAssigningItemId] = useState<number | null>(null)

  const { data: inbound, isLoading, isError } = useInbound(warehouseId, inboundId)
  const completeMutation = useCompleteInbound(warehouseId)
  const cancelMutation = useCancelInbound(warehouseId)

  const canAct = inbound?.status === "PENDING" || inbound?.status === "IN_PROGRESS"
  const canComplete =
    canAct &&
    (inbound?.items.length ?? 0) > 0 &&
    (inbound?.items.every(
      (item) => item.locations.reduce((sum, l) => sum + l.quantity, 0) === item.quantity
    ) ?? false)

  const assigningItem = inbound?.items.find((item) => item.id === assigningItemId) ?? null

  async function handleComplete() {
    if (!window.confirm("입고를 확정하시겠습니까? 확정 후에는 되돌릴 수 없습니다.")) return
    setError("")
    try {
      await completeMutation.mutateAsync({ id: inboundId, idempotencyKey: createIdempotencyKey() })
    } catch (err) {
      const message = isAxiosError(err) ? err.response?.data?.message : null
      setError(message ?? "입고 확정에 실패했습니다.")
    }
  }

  async function handleCancel() {
    if (!window.confirm("이 입고건을 취소하시겠습니까?")) return
    setError("")
    try {
      await cancelMutation.mutateAsync({ id: inboundId, idempotencyKey: createIdempotencyKey() })
    } catch (err) {
      const message = isAxiosError(err) ? err.response?.data?.message : null
      setError(message ?? "입고 취소에 실패했습니다.")
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <PageHeader
        title="입고 상세"
        breadcrumb={[
          { label: "홈", to: "/app" },
          { label: "입고 관리", to: "/app/inbounds" },
          { label: "입고 상세" },
        ]}
      />

      {isLoading && <p className="text-sm text-muted-foreground">불러오는 중...</p>}
      {isError && <p className="text-sm text-destructive">입고 정보를 불러오지 못했습니다.</p>}

      {inbound && (
        <>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between">
              <CardTitle className="flex items-center gap-2 text-base">
                {inbound.supplierName}
                <InboundStatusBadge status={inbound.status} />
              </CardTitle>
              {canAct && (
                <div className="flex gap-2">
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={handleCancel}
                    disabled={cancelMutation.isPending || completeMutation.isPending}
                  >
                    취소
                  </Button>
                  <Button
                    type="button"
                    size="sm"
                    onClick={handleComplete}
                    disabled={!canComplete || cancelMutation.isPending || completeMutation.isPending}
                  >
                    확정
                  </Button>
                </div>
              )}
            </CardHeader>
            <CardContent className="flex flex-col gap-2 text-sm">
              <div className="flex justify-between border-b pb-2">
                <span className="text-muted-foreground">등록일</span>
                <span className="font-medium">{dayjs(inbound.createdAt).format("YYYY-MM-DD HH:mm")}</span>
              </div>
              <div className="flex justify-between border-b pb-2">
                <span className="text-muted-foreground">처리자</span>
                <span className="font-medium">{inbound.processedByName ?? "-"}</span>
              </div>
              <div className="flex justify-between pb-1">
                <span className="text-muted-foreground">비고</span>
                <span className="font-medium">{inbound.note ?? "-"}</span>
              </div>
              {canAct && !canComplete && (
                <p className="text-xs text-muted-foreground">
                  모든 상품 라인의 위치 배치를 완료해야 확정할 수 있습니다.
                </p>
              )}
              {error && <p className="text-sm text-destructive">{error}</p>}
            </CardContent>
          </Card>

          <Card className="gap-0 py-0">
            <CardContent className="p-0">
              <Table>
                <TableHeader>
                  <TableRow className="bg-muted/50 hover:bg-muted/50">
                    <TableHead className="text-xs text-muted-foreground uppercase">상품</TableHead>
                    <TableHead className="text-xs text-muted-foreground uppercase">수량</TableHead>
                    <TableHead className="text-xs text-muted-foreground uppercase">LOT</TableHead>
                    <TableHead className="text-xs text-muted-foreground uppercase">배치 위치</TableHead>
                    <TableHead className="text-xs text-muted-foreground uppercase">배치 진행</TableHead>
                    <TableHead />
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {inbound.items.map((item) => {
                    const placed = item.locations.reduce((sum, l) => sum + l.quantity, 0)
                    const isFull = placed === item.quantity
                    return (
                      <TableRow key={item.id}>
                        <TableCell className="font-medium">
                          {item.productName}
                          <span className="ml-1.5 text-xs text-muted-foreground">
                            {item.productSkuCode}
                          </span>
                        </TableCell>
                        <TableCell>
                          {item.quantity} {item.unitName}
                        </TableCell>
                        <TableCell>
                          {item.lotNumber ? (
                            <div className="flex flex-col text-xs">
                              <span>{item.lotNumber}</span>
                              <span className="text-muted-foreground">
                                {item.manufactureDate} ~ {item.expiryDate}
                              </span>
                            </div>
                          ) : (
                            "-"
                          )}
                        </TableCell>
                        <TableCell>
                          <div className="flex flex-col gap-0.5">
                            {item.locations.length === 0 && (
                              <span className="text-sm text-muted-foreground">미배치</span>
                            )}
                            {item.locations.map((loc) => (
                              <span key={loc.locationId} className="text-sm">
                                {loc.locationCode} — {loc.quantity}
                              </span>
                            ))}
                          </div>
                        </TableCell>
                        <TableCell>
                          <Badge variant={isFull ? "success" : "warning"}>
                            {placed} / {item.quantity}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-right">
                          {canAct && (
                            <Button
                              type="button"
                              variant="ghost"
                              size="icon"
                              className="size-8 rounded-full bg-primary/10 text-primary hover:bg-primary/20 hover:text-primary"
                              onClick={() => setAssigningItemId(item.id)}
                              aria-label="위치 배치"
                            >
                              <MapPin className="size-4" />
                            </Button>
                          )}
                        </TableCell>
                      </TableRow>
                    )
                  })}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </>
      )}

      {inbound && assigningItem && warehouseId && (
        <InboundLocationAssignDialog
          warehouseId={warehouseId}
          inboundId={inbound.id}
          item={assigningItem}
          open={assigningItemId != null}
          onOpenChange={(open) => {
            if (!open) setAssigningItemId(null)
          }}
        />
      )}
    </div>
  )
}
