import { useMemo, useState } from "react"
import { Plus } from "lucide-react"
import { PageHeader } from "@/components/common/PageHeader"
import { SearchInput } from "@/components/common/SearchInput"
import { DataTablePagination } from "@/components/common/DataTablePagination"
import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { useProductUnits } from "@/features/product-units/hooks"
import { ProductUnitTable } from "@/features/product-units/components/ProductUnitTable"
import { ProductUnitFormDialog } from "@/features/product-units/components/ProductUnitFormDialog"
import type { ProductUnitItem } from "@/features/product-units/types"

const PAGE_SIZE = 10

export function ProductUnitPage() {
  const [page, setPage] = useState(1)
  const [search, setSearch] = useState("")
  const [formTarget, setFormTarget] = useState<ProductUnitItem | null>(null)
  const [isFormOpen, setIsFormOpen] = useState(false)

  const { data, isLoading, isError } = useProductUnits(page, PAGE_SIZE)

  const filteredItems = useMemo(() => {
    const keyword = search.trim().toLowerCase()
    if (!keyword) return data?.content ?? []
    return (data?.content ?? []).filter((item) => item.name.toLowerCase().includes(keyword))
  }, [data, search])

  function handleCreate() {
    setFormTarget(null)
    setIsFormOpen(true)
  }

  function handleEdit(item: ProductUnitItem) {
    setFormTarget(item)
    setIsFormOpen(true)
  }

  return (
    <div className="flex flex-col gap-4">
      <PageHeader
        title="상품 단위 관리"
        breadcrumb={[{ label: "홈", to: "/app" }, { label: "상품 단위 관리" }]}
      />

      <Card className="py-4">
        <CardContent>
          <SearchInput value={search} onChange={setSearch} placeholder="단위명 검색" />
        </CardContent>
      </Card>

      <div className="flex justify-end">
        <Button onClick={handleCreate}>
          <Plus className="size-4" />
          단위 등록
        </Button>
      </div>

      <Card className="gap-0 py-0">
        <CardContent className="p-0">
          <ProductUnitTable items={filteredItems} onEdit={handleEdit} />

          {!isLoading && !isError && filteredItems.length === 0 && (
            <p className="p-6 text-center text-sm text-muted-foreground">
              {search ? "검색 결과가 없습니다." : "등록된 단위가 없습니다."}
            </p>
          )}
        </CardContent>
      </Card>

      {isLoading && <p className="text-sm text-muted-foreground">불러오는 중...</p>}
      {isError && <p className="text-sm text-destructive">목록을 불러오지 못했습니다.</p>}

      {data && data.pageInfo.totalElements > 0 && (
        <DataTablePagination pageInfo={data.pageInfo} onPageChange={setPage} />
      )}

      <ProductUnitFormDialog open={isFormOpen} onOpenChange={setIsFormOpen} target={formTarget} />
    </div>
  )
}
