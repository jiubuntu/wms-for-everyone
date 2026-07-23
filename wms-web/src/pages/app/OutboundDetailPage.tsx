import { useState } from "react"
import { useParams } from "react-router-dom"
import { isAxiosError } from "axios"
import dayjs from "dayjs"
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
import { useCancelOutbound, useCompleteOutbound, useOutbound } from "@/features/outbound/hooks"
import { OutboundStatusBadge } from "@/features/outbound/components/OutboundStatusBadge"
import { createIdempotencyKey } from "@/lib/idempotency"

export function OutboundDetailPage() {
  const { id } = useParams<{ id: string }>()
  const outboundId = Number(id)
  const { warehouseId } = useWarehouseFilter()
  const [error, setError] = useState("")

  const { data: outbound, isLoading, isError } = useOutbound(warehouseId, outboundId)
  const completeMutation = useCompleteOutbound(warehouseId)
  const cancelMutation = useCancelOutbound(warehouseId)

  const canAct = outbound?.status === "PENDING" || outbound?.status === "PICKING"

  async function handleComplete() {
    if (!window.confirm("출하를 확정하시겠습니까? 확정 후에는 되돌릴 수 없습니다.")) return
    setError("")
    try {
      await completeMutation.mutateAsync({ id: outboundId, idempotencyKey: createIdempotencyKey() })
    } catch (err) {
      const message = isAxiosError(err) ? err.response?.data?.message : null
      setError(message ?? "출하 확정에 실패했습니다.")
    }
  }

  async function handleCancel() {
    if (!window.confirm("이 출고건을 취소하시겠습니까?")) return
    setError("")
    try {
      await cancelMutation.mutateAsync({ id: outboundId, idempotencyKey: createIdempotencyKey() })
    } catch (err) {
      const message = isAxiosError(err) ? err.response?.data?.message : null
      setError(message ?? "출고 취소에 실패했습니다.")
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <PageHeader
        title="출고 상세"
        breadcrumb={[
          { label: "홈", to: "/app" },
          { label: "출고 관리", to: "/app/outbounds" },
          { label: "출고 상세" },
        ]}
      />

      {isLoading && <p className="text-sm text-muted-foreground">불러오는 중...</p>}
      {isError && <p className="text-sm text-destructive">출고 정보를 불러오지 못했습니다.</p>}

      {outbound && (
        <>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between">
              <CardTitle className="flex items-center gap-2 text-base">
                {outbound.customerName}
                <OutboundStatusBadge status={outbound.status} />
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
                    disabled={cancelMutation.isPending || completeMutation.isPending}
                  >
                    출하 확정
                  </Button>
                </div>
              )}
            </CardHeader>
            <CardContent className="flex flex-col gap-2 text-sm">
              <div className="flex justify-between border-b pb-2">
                <span className="text-muted-foreground">등록일</span>
                <span className="font-medium">{dayjs(outbound.createdAt).format("YYYY-MM-DD HH:mm")}</span>
              </div>
              <div className="flex justify-between border-b pb-2">
                <span className="text-muted-foreground">처리자</span>
                <span className="font-medium">{outbound.processedByName ?? "-"}</span>
              </div>
              <div className="flex justify-between pb-1">
                <span className="text-muted-foreground">비고</span>
                <span className="font-medium">{outbound.note ?? "-"}</span>
              </div>
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
                    <TableHead className="text-xs text-muted-foreground uppercase">할당 방식</TableHead>
                    <TableHead className="text-xs text-muted-foreground uppercase">
                      할당 위치 / LOT / 수량
                    </TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {outbound.items.map((item) => (
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
                        <Badge variant={item.allocationType === "FEFO" ? "default" : "secondary"}>
                          {item.allocationType}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <div className="flex flex-col gap-0.5">
                          {item.allocations.map((a, i) => (
                            <span key={i} className="text-sm">
                              {a.locationCode}
                              {a.lotNumber && ` / ${a.lotNumber}`} — {a.quantity}
                            </span>
                          ))}
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </>
      )}
    </div>
  )
}
