import { useMemo, useState } from "react"
import { PageHeader } from "@/components/common/PageHeader"
import { SearchInput } from "@/components/common/SearchInput"
import { DataTablePagination } from "@/components/common/DataTablePagination"
import { Card, CardContent } from "@/components/ui/card"
import { useWarehouseFilter } from "@/contexts/WarehouseFilterContext"
import { useInventory } from "@/features/inventory/hooks"
import { useFlashingRows } from "@/features/inventory/useFlashingRows"
import { InventoryTable } from "@/features/inventory/components/InventoryTable"
import { InventoryAdjustDialog } from "@/features/inventory/components/InventoryAdjustDialog"
import type { InventoryItem } from "@/features/inventory/types"

const PAGE_SIZE = 10

export function InventoryPage() {
  const { warehouseId } = useWarehouseFilter()

  const [page, setPage] = useState(1)
  const [search, setSearch] = useState("")
  const [adjustTarget, setAdjustTarget] = useState<InventoryItem | null>(null)
  const [isAdjustOpen, setIsAdjustOpen] = useState(false)

  const { data, isLoading, isError } = useInventory(warehouseId, page, PAGE_SIZE)
  const flashingIds = useFlashingRows(data?.content ?? [], `${warehouseId ?? "none"}-${page}`)

  const filteredItems = useMemo(() => {
    const keyword = search.trim().toLowerCase()
    if (!keyword) return data?.content ?? []
    return (data?.content ?? []).filter(
      (item) =>
        item.productName.toLowerCase().includes(keyword) ||
        item.productSkuCode.toLowerCase().includes(keyword) ||
        item.locationCode.toLowerCase().includes(keyword)
    )
  }, [data, search])

  function handleAdjust(item: InventoryItem) {
    setAdjustTarget(item)
    setIsAdjustOpen(true)
  }

  return (
    <div className="flex flex-col gap-4">
      <PageHeader title="재고 현황" breadcrumb={[{ label: "홈", to: "/app" }, { label: "재고 현황" }]} />

      <Card className="py-4">
        <CardContent>
          <SearchInput value={search} onChange={setSearch} placeholder="상품명, SKU, 위치 코드 검색" />
        </CardContent>
      </Card>

      <Card className="gap-0 py-0">
        <CardContent className="p-0">
          <InventoryTable items={filteredItems} onAdjust={handleAdjust} flashingIds={flashingIds} />

          {!isLoading && !isError && filteredItems.length === 0 && (
            <p className="p-6 text-center text-sm text-muted-foreground">
              {search ? "검색 결과가 없습니다." : "보유 중인 재고가 없습니다."}
            </p>
          )}
        </CardContent>
      </Card>

      {isLoading && <p className="text-sm text-muted-foreground">불러오는 중...</p>}
      {isError && <p className="text-sm text-destructive">목록을 불러오지 못했습니다.</p>}

      {data && data.pageInfo.totalElements > 0 && (
        <DataTablePagination pageInfo={data.pageInfo} onPageChange={setPage} />
      )}

      <InventoryAdjustDialog open={isAdjustOpen} onOpenChange={setIsAdjustOpen} target={adjustTarget} />
    </div>
  )
}
