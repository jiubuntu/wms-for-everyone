import { useState } from "react"
import { Plus } from "lucide-react"
import { PageHeader } from "@/components/common/PageHeader"
import { DataTablePagination } from "@/components/common/DataTablePagination"
import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { useWarehouseFilter } from "@/contexts/WarehouseFilterContext"
import { useTransfers } from "@/features/transfers/hooks"
import { TransferTable } from "@/features/transfers/components/TransferTable"
import { TransferFormDialog } from "@/features/transfers/components/TransferFormDialog"

const PAGE_SIZE = 10

export function TransferPage() {
  const { warehouseId } = useWarehouseFilter()

  const [page, setPage] = useState(1)
  const [isFormOpen, setIsFormOpen] = useState(false)

  const { data, isLoading, isError } = useTransfers(warehouseId, page, PAGE_SIZE)

  return (
    <div className="flex flex-col gap-4">
      <PageHeader title="재고 이동" breadcrumb={[{ label: "홈", to: "/app" }, { label: "재고 이동" }]} />

      <div className="flex justify-end">
        <Button onClick={() => setIsFormOpen(true)} disabled={!warehouseId}>
          <Plus className="size-4" />
          재고 이동
        </Button>
      </div>

      <Card className="gap-0 py-0">
        <CardContent className="p-0">
          <TransferTable items={data?.content ?? []} />

          {!isLoading && !isError && (data?.content.length ?? 0) === 0 && (
            <p className="p-6 text-center text-sm text-muted-foreground">이동 이력이 없습니다.</p>
          )}
        </CardContent>
      </Card>

      {isLoading && <p className="text-sm text-muted-foreground">불러오는 중...</p>}
      {isError && <p className="text-sm text-destructive">목록을 불러오지 못했습니다.</p>}

      {data && data.pageInfo.totalElements > 0 && (
        <DataTablePagination pageInfo={data.pageInfo} onPageChange={setPage} />
      )}

      {warehouseId && (
        <TransferFormDialog warehouseId={warehouseId} open={isFormOpen} onOpenChange={setIsFormOpen} />
      )}
    </div>
  )
}
