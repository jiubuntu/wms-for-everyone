import { useState } from "react"
import { Link } from "react-router-dom"
import { Plus } from "lucide-react"
import { PageHeader } from "@/components/common/PageHeader"
import { DataTablePagination } from "@/components/common/DataTablePagination"
import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { useWarehouseFilter } from "@/contexts/WarehouseFilterContext"
import { useOutbounds } from "@/features/outbound/hooks"
import { OutboundTable } from "@/features/outbound/components/OutboundTable"

const PAGE_SIZE = 10

export function OutboundPage() {
  const { warehouseId } = useWarehouseFilter()
  const [page, setPage] = useState(1)

  const { data, isLoading, isError } = useOutbounds(warehouseId, page, PAGE_SIZE)

  return (
    <div className="flex flex-col gap-4">
      <PageHeader title="출고 관리" breadcrumb={[{ label: "홈", to: "/app" }, { label: "출고 관리" }]} />

      <div className="flex justify-end">
        {warehouseId ? (
          <Button asChild>
            <Link to="/app/outbounds/new">
              <Plus className="size-4" />
              출고 등록
            </Link>
          </Button>
        ) : (
          <Button disabled>
            <Plus className="size-4" />
            출고 등록
          </Button>
        )}
      </div>

      <Card className="gap-0 py-0">
        <CardContent className="p-0">
          <OutboundTable items={data?.content ?? []} />

          {!isLoading && !isError && (data?.content.length ?? 0) === 0 && (
            <p className="p-6 text-center text-sm text-muted-foreground">등록된 출고가 없습니다.</p>
          )}
        </CardContent>
      </Card>

      {isLoading && <p className="text-sm text-muted-foreground">불러오는 중...</p>}
      {isError && <p className="text-sm text-destructive">목록을 불러오지 못했습니다.</p>}

      {data && data.pageInfo.totalElements > 0 && (
        <DataTablePagination pageInfo={data.pageInfo} onPageChange={setPage} />
      )}
    </div>
  )
}
