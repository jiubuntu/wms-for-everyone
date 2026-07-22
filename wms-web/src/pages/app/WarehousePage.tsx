import { useMemo, useState } from "react"
import { Plus } from "lucide-react"
import { PageHeader } from "@/components/common/PageHeader"
import { SearchInput } from "@/components/common/SearchInput"
import { DataTablePagination } from "@/components/common/DataTablePagination"
import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { useAuth } from "@/contexts/AuthContext"
import { useAllCommonCodes } from "@/features/common-codes/hooks"
import { useWarehouses } from "@/features/warehouses/hooks"
import { WarehouseTable } from "@/features/warehouses/components/WarehouseTable"
import { WarehouseFormDialog } from "@/features/warehouses/components/WarehouseFormDialog"
import type { WarehouseItem } from "@/features/warehouses/types"

const PAGE_SIZE = 10

export function WarehousePage() {
  const { user } = useAuth()
  const canManage = user?.role === "COMPANY_ADMIN"

  const [page, setPage] = useState(1)
  const [search, setSearch] = useState("")
  const [formTarget, setFormTarget] = useState<WarehouseItem | null>(null)
  const [isFormOpen, setIsFormOpen] = useState(false)

  const { data, isLoading, isError } = useWarehouses(page, PAGE_SIZE)
  const { data: storageTypes } = useAllCommonCodes("company", "STORAGE_TYPE")

  const storageTypeNameById = useMemo(
    () => new Map(storageTypes?.map((s) => [s.id, s.name]) ?? []),
    [storageTypes]
  )

  const filteredItems = useMemo(() => {
    const keyword = search.trim().toLowerCase()
    if (!keyword) return data?.content ?? []
    return (data?.content ?? []).filter((item) => item.name.toLowerCase().includes(keyword))
  }, [data, search])

  function handleCreate() {
    setFormTarget(null)
    setIsFormOpen(true)
  }

  function handleEdit(item: WarehouseItem) {
    setFormTarget(item)
    setIsFormOpen(true)
  }

  return (
    <div className="flex flex-col gap-4">
      <PageHeader title="창고 관리" breadcrumb={[{ label: "홈", to: "/app" }, { label: "창고 관리" }]} />

      <Card className="py-4">
        <CardContent>
          <SearchInput value={search} onChange={setSearch} placeholder="창고명 검색" />
        </CardContent>
      </Card>

      {canManage && (
        <div className="flex justify-end">
          <Button onClick={handleCreate}>
            <Plus className="size-4" />
            창고 등록
          </Button>
        </div>
      )}

      <Card className="gap-0 py-0">
        <CardContent className="p-0">
          <WarehouseTable
            items={filteredItems}
            storageTypeNameById={storageTypeNameById}
            canManage={canManage}
            onEdit={handleEdit}
          />

          {!isLoading && !isError && filteredItems.length === 0 && (
            <p className="p-6 text-center text-sm text-muted-foreground">
              {search ? "검색 결과가 없습니다." : "등록된 창고가 없습니다."}
            </p>
          )}
        </CardContent>
      </Card>

      {isLoading && <p className="text-sm text-muted-foreground">불러오는 중...</p>}
      {isError && <p className="text-sm text-destructive">목록을 불러오지 못했습니다.</p>}

      {data && data.pageInfo.totalElements > 0 && (
        <DataTablePagination pageInfo={data.pageInfo} onPageChange={setPage} />
      )}

      {canManage && (
        <WarehouseFormDialog open={isFormOpen} onOpenChange={setIsFormOpen} target={formTarget} />
      )}
    </div>
  )
}
