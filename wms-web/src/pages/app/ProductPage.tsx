import { useMemo, useState } from "react"
import { Plus } from "lucide-react"
import { PageHeader } from "@/components/common/PageHeader"
import { SearchInput } from "@/components/common/SearchInput"
import { DataTablePagination } from "@/components/common/DataTablePagination"
import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { useAllCommonCodes } from "@/features/common-codes/hooks"
import { useAllProductUnits } from "@/features/product-units/hooks"
import { useProducts } from "@/features/products/hooks"
import { ProductTable } from "@/features/products/components/ProductTable"
import { ProductFormDialog } from "@/features/products/components/ProductFormDialog"
import type { ProductItem } from "@/features/products/types"

const PAGE_SIZE = 10

export function ProductPage() {
  const [page, setPage] = useState(1)
  const [search, setSearch] = useState("")
  const [formTarget, setFormTarget] = useState<ProductItem | null>(null)
  const [isFormOpen, setIsFormOpen] = useState(false)

  const { data, isLoading, isError } = useProducts(page, PAGE_SIZE)
  const { data: categories } = useAllCommonCodes("company", "PRODUCT_CATEGORY")
  const { data: storageTypes } = useAllCommonCodes("company", "STORAGE_TYPE")
  const { data: units } = useAllProductUnits()

  const categoryNameById = useMemo(
    () => new Map(categories?.map((c) => [c.id, c.name]) ?? []),
    [categories]
  )
  const storageTypeNameById = useMemo(
    () => new Map(storageTypes?.map((s) => [s.id, s.name]) ?? []),
    [storageTypes]
  )
  const unitNameById = useMemo(() => new Map(units?.map((u) => [u.id, u.name]) ?? []), [units])

  const filteredItems = useMemo(() => {
    const keyword = search.trim().toLowerCase()
    if (!keyword) return data?.content ?? []
    return (data?.content ?? []).filter(
      (item) =>
        item.skuCode.toLowerCase().includes(keyword) || item.name.toLowerCase().includes(keyword)
    )
  }, [data, search])

  function handleCreate() {
    setFormTarget(null)
    setIsFormOpen(true)
  }

  function handleEdit(item: ProductItem) {
    setFormTarget(item)
    setIsFormOpen(true)
  }

  return (
    <div className="flex flex-col gap-4">
      <PageHeader title="상품 관리" breadcrumb={[{ label: "홈", to: "/app" }, { label: "상품 관리" }]} />

      <Card className="py-4">
        <CardContent>
          <SearchInput value={search} onChange={setSearch} placeholder="SKU, 상품명 검색" />
        </CardContent>
      </Card>

      <div className="flex justify-end">
        <Button onClick={handleCreate}>
          <Plus className="size-4" />
          상품 등록
        </Button>
      </div>

      <Card className="gap-0 py-0">
        <CardContent className="p-0">
          <ProductTable
            items={filteredItems}
            categoryNameById={categoryNameById}
            storageTypeNameById={storageTypeNameById}
            unitNameById={unitNameById}
            onEdit={handleEdit}
          />

          {!isLoading && !isError && filteredItems.length === 0 && (
            <p className="p-6 text-center text-sm text-muted-foreground">
              {search ? "검색 결과가 없습니다." : "등록된 상품이 없습니다."}
            </p>
          )}
        </CardContent>
      </Card>

      {isLoading && <p className="text-sm text-muted-foreground">불러오는 중...</p>}
      {isError && <p className="text-sm text-destructive">목록을 불러오지 못했습니다.</p>}

      {data && data.pageInfo.totalElements > 0 && (
        <DataTablePagination pageInfo={data.pageInfo} onPageChange={setPage} />
      )}

      <ProductFormDialog open={isFormOpen} onOpenChange={setIsFormOpen} target={formTarget} />
    </div>
  )
}
