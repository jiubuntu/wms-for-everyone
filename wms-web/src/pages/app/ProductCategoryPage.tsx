import { useEffect, useState } from "react"
import { Plus } from "lucide-react"
import { PageHeader } from "@/components/common/PageHeader"
import { SearchInput } from "@/components/common/SearchInput"
import { DataTablePagination } from "@/components/common/DataTablePagination"
import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { useCommonCodes } from "@/features/common-codes/hooks"
import { CommonCodeTable } from "@/features/common-codes/components/CommonCodeTable"
import { CommonCodeFormDialog } from "@/features/common-codes/components/CommonCodeFormDialog"
import type { CommonCodeItem } from "@/features/common-codes/types"

const PAGE_SIZE = 10

export function ProductCategoryPage() {
  const [page, setPage] = useState(1)
  const [search, setSearch] = useState("")
  const [formTarget, setFormTarget] = useState<CommonCodeItem | null>(null)
  const [isFormOpen, setIsFormOpen] = useState(false)

  useEffect(() => {
    setPage(1)
  }, [search])

  const { data, isLoading, isError } = useCommonCodes("company", "PRODUCT_CATEGORY", search, page, PAGE_SIZE)
  const items = data?.content ?? []

  function handleCreate() {
    setFormTarget(null)
    setIsFormOpen(true)
  }

  function handleEdit(item: CommonCodeItem) {
    setFormTarget(item)
    setIsFormOpen(true)
  }

  return (
    <div className="flex flex-col gap-4">
      <PageHeader
        title="상품 카테고리 관리"
        breadcrumb={[{ label: "홈", to: "/app" }, { label: "상품 카테고리 관리" }]}
      />

      <Card className="py-4">
        <CardContent>
          <SearchInput value={search} onChange={setSearch} placeholder="코드, 코드명 검색" />
        </CardContent>
      </Card>

      <div className="flex justify-end">
        <Button onClick={handleCreate}>
          <Plus className="size-4" />
          카테고리 등록
        </Button>
      </div>

      <Card className="gap-0 py-0">
        <CardContent className="p-0">
          <CommonCodeTable
            scope="company"
            items={items}
            onEdit={handleEdit}
            showScopeColumn
          />

          {!isLoading && !isError && items.length === 0 && (
            <p className="p-6 text-center text-sm text-muted-foreground">
              {search ? "검색 결과가 없습니다." : "등록된 카테고리가 없습니다."}
            </p>
          )}
        </CardContent>
      </Card>

      {isLoading && <p className="text-sm text-muted-foreground">불러오는 중...</p>}
      {isError && <p className="text-sm text-destructive">목록을 불러오지 못했습니다.</p>}

      {data && data.pageInfo.totalElements > 0 && (
        <DataTablePagination pageInfo={data.pageInfo} onPageChange={setPage} />
      )}

      <CommonCodeFormDialog
        scope="company"
        open={isFormOpen}
        onOpenChange={setIsFormOpen}
        groupCode="PRODUCT_CATEGORY"
        target={formTarget}
      />
    </div>
  )
}
