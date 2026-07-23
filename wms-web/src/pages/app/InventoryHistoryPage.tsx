import { useMemo, useState } from "react"
import { PageHeader } from "@/components/common/PageHeader"
import { SearchInput } from "@/components/common/SearchInput"
import { DataTablePagination } from "@/components/common/DataTablePagination"
import { Card, CardContent } from "@/components/ui/card"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { useWarehouseFilter } from "@/contexts/WarehouseFilterContext"
import { useInventoryHistory } from "@/features/inventory/hooks"
import { InventoryHistoryTable } from "@/features/inventory/components/InventoryHistoryTable"
import type { InventoryHistoryTargetType } from "@/features/inventory/types"

const PAGE_SIZE = 10

const ALL_TYPES = "all"

export function InventoryHistoryPage() {
  const { warehouseId } = useWarehouseFilter()

  const [page, setPage] = useState(1)
  const [search, setSearch] = useState("")
  const [targetType, setTargetType] = useState<InventoryHistoryTargetType | typeof ALL_TYPES>(
    ALL_TYPES
  )

  const { data, isLoading, isError } = useInventoryHistory(warehouseId, page, PAGE_SIZE)

  const filteredItems = useMemo(() => {
    const keyword = search.trim().toLowerCase()
    return (data?.content ?? []).filter((item) => {
      const matchesKeyword =
        !keyword ||
        item.productName.toLowerCase().includes(keyword) ||
        item.productSkuCode.toLowerCase().includes(keyword) ||
        item.locationCode.toLowerCase().includes(keyword)
      const matchesType = targetType === ALL_TYPES || item.targetType === targetType
      return matchesKeyword && matchesType
    })
  }, [data, search, targetType])

  return (
    <div className="flex flex-col gap-4">
      <PageHeader title="재고 이력" breadcrumb={[{ label: "홈", to: "/app" }, { label: "재고 이력" }]} />

      <Card className="py-4">
        <CardContent className="flex flex-wrap items-center gap-3">
          <SearchInput value={search} onChange={setSearch} placeholder="상품명, SKU, 위치 코드 검색" />
          <Select
            value={targetType}
            onValueChange={(v) => setTargetType(v as InventoryHistoryTargetType | typeof ALL_TYPES)}
          >
            <SelectTrigger className="w-[140px]">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={ALL_TYPES}>전체 유형</SelectItem>
              <SelectItem value="INBOUND">입고</SelectItem>
              <SelectItem value="OUTBOUND">출고</SelectItem>
              <SelectItem value="TRANSFER">이동</SelectItem>
              <SelectItem value="ADJUSTMENT">조정</SelectItem>
            </SelectContent>
          </Select>
        </CardContent>
      </Card>

      <Card className="gap-0 py-0">
        <CardContent className="p-0">
          <InventoryHistoryTable items={filteredItems} />

          {!isLoading && !isError && filteredItems.length === 0 && (
            <p className="p-6 text-center text-sm text-muted-foreground">
              {search || targetType !== ALL_TYPES ? "검색 결과가 없습니다." : "재고 변동 이력이 없습니다."}
            </p>
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
