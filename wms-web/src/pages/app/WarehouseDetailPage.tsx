import { useEffect, useMemo, useState } from "react"
import { useParams } from "react-router-dom"
import { Plus } from "lucide-react"
import { PageHeader } from "@/components/common/PageHeader"
import { SearchInput } from "@/components/common/SearchInput"
import { DataTablePagination } from "@/components/common/DataTablePagination"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { useAuth } from "@/contexts/AuthContext"
import { useAllCommonCodes } from "@/features/common-codes/hooks"
import { useWarehouse } from "@/features/warehouses/hooks"
import { WarehouseFormDialog } from "@/features/warehouses/components/WarehouseFormDialog"
import { useLocations } from "@/features/locations/hooks"
import { LocationTable } from "@/features/locations/components/LocationTable"
import { LocationBulkCreateDialog } from "@/features/locations/components/LocationBulkCreateDialog"
import { LocationEditDialog } from "@/features/locations/components/LocationEditDialog"
import type { LocationItem } from "@/features/locations/types"

const PAGE_SIZE = 10

export function WarehouseDetailPage() {
  const { id } = useParams<{ id: string }>()
  const warehouseId = Number(id)
  const { user } = useAuth()
  const canManageWarehouse = user?.role === "COMPANY_ADMIN"

  const [page, setPage] = useState(1)
  const [search, setSearch] = useState("")
  const [isWarehouseFormOpen, setIsWarehouseFormOpen] = useState(false)
  const [isBulkCreateOpen, setIsBulkCreateOpen] = useState(false)
  const [editTarget, setEditTarget] = useState<LocationItem | null>(null)
  const [isEditOpen, setIsEditOpen] = useState(false)

  useEffect(() => {
    setPage(1)
  }, [search])

  const { data: warehouse, isLoading: isWarehouseLoading, isError: isWarehouseError } =
    useWarehouse(warehouseId)
  const { data: storageTypes } = useAllCommonCodes("company", "STORAGE_TYPE")
  const { data, isLoading, isError } = useLocations(warehouseId, search, page, PAGE_SIZE)
  const items = data?.content ?? []

  const storageTypeNameById = useMemo(
    () => new Map(storageTypes?.map((s) => [s.id, s.name]) ?? []),
    [storageTypes]
  )

  function handleEditLocation(item: LocationItem) {
    setEditTarget(item)
    setIsEditOpen(true)
  }

  return (
    <div className="flex flex-col gap-4">
      <PageHeader
        title={warehouse?.name ?? "창고 상세"}
        breadcrumb={[
          { label: "홈", to: "/app" },
          { label: "창고 관리", to: "/app/warehouses" },
          { label: warehouse?.name ?? "" },
        ]}
      />

      {isWarehouseLoading && <p className="text-sm text-muted-foreground">불러오는 중...</p>}
      {isWarehouseError && (
        <p className="text-sm text-destructive">창고 정보를 불러오지 못했습니다.</p>
      )}

      {warehouse && (
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle className="flex items-center gap-2 text-base">
              {warehouse.name}
              <Badge variant={warehouse.active ? "success" : "secondary"}>
                {warehouse.active ? "사용" : "미사용"}
              </Badge>
            </CardTitle>
            {canManageWarehouse && (
              <Button variant="outline" size="sm" onClick={() => setIsWarehouseFormOpen(true)}>
                수정
              </Button>
            )}
          </CardHeader>
          <CardContent className="flex flex-col gap-2 text-sm">
            <div className="flex justify-between pb-1">
              <span className="text-muted-foreground">주소</span>
              <span className="font-medium">{warehouse.address ?? "-"}</span>
            </div>
          </CardContent>
        </Card>
      )}

      <Card className="py-4">
        <CardContent>
          <SearchInput value={search} onChange={setSearch} placeholder="위치 코드 검색" />
        </CardContent>
      </Card>

      <div className="flex justify-end">
        <Button onClick={() => setIsBulkCreateOpen(true)}>
          <Plus className="size-4" />
          위치 일괄 생성
        </Button>
      </div>

      <Card className="gap-0 py-0">
        <CardContent className="p-0">
          <LocationTable
            warehouseId={warehouseId}
            items={items}
            storageTypeNameById={storageTypeNameById}
            onEdit={handleEditLocation}
          />

          {!isLoading && !isError && items.length === 0 && (
            <p className="p-6 text-center text-sm text-muted-foreground">
              {search ? "검색 결과가 없습니다." : "등록된 위치가 없습니다."}
            </p>
          )}
        </CardContent>
      </Card>

      {isLoading && <p className="text-sm text-muted-foreground">불러오는 중...</p>}
      {isError && <p className="text-sm text-destructive">목록을 불러오지 못했습니다.</p>}

      {data && data.pageInfo.totalElements > 0 && (
        <DataTablePagination pageInfo={data.pageInfo} onPageChange={setPage} />
      )}

      {canManageWarehouse && warehouse && (
        <WarehouseFormDialog
          open={isWarehouseFormOpen}
          onOpenChange={setIsWarehouseFormOpen}
          target={warehouse}
        />
      )}

      <LocationBulkCreateDialog
        warehouseId={warehouseId}
        open={isBulkCreateOpen}
        onOpenChange={setIsBulkCreateOpen}
      />

      <LocationEditDialog
        warehouseId={warehouseId}
        open={isEditOpen}
        onOpenChange={setIsEditOpen}
        target={editTarget}
      />
    </div>
  )
}
