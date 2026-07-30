import { useEffect, useState } from "react"
import { Plus } from "lucide-react"
import { PageHeader } from "@/components/common/PageHeader"
import { SearchInput } from "@/components/common/SearchInput"
import { DataTablePagination } from "@/components/common/DataTablePagination"
import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { useCommonCodes } from "@/features/common-codes/hooks"
import { CommonCodeTable } from "@/features/common-codes/components/CommonCodeTable"
import { CommonCodeFormDialog } from "@/features/common-codes/components/CommonCodeFormDialog"
import type { CommonCodeGroup, CommonCodeItem } from "@/features/common-codes/types"

const PAGE_SIZE = 10

const GROUP_TABS: { value: CommonCodeGroup; label: string }[] = [
  { value: "PRODUCT_CATEGORY", label: "상품 카테고리" },
  { value: "STORAGE_TYPE", label: "보관 유형" },
  { value: "TRANSFER_REASON", label: "재고 이동 사유" },
]

export function CommonCodePage() {
  const [groupCode, setGroupCode] = useState<CommonCodeGroup>("PRODUCT_CATEGORY")
  const [page, setPage] = useState(1)
  const [search, setSearch] = useState("")
  const [formTarget, setFormTarget] = useState<CommonCodeItem | null>(null)
  const [isFormOpen, setIsFormOpen] = useState(false)

  useEffect(() => {
    setPage(1)
  }, [search])

  const { data, isLoading, isError } = useCommonCodes("admin", groupCode, search, page, PAGE_SIZE)
  const items = data?.content ?? []

  function handleGroupChange(value: string) {
    setGroupCode(value as CommonCodeGroup)
    setPage(1)
    setSearch("")
  }

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
        title="공통 코드 관리"
        breadcrumb={[{ label: "홈", to: "/admin" }, { label: "공통 코드 관리" }]}
      />

      <Tabs value={groupCode} onValueChange={handleGroupChange}>
        <TabsList>
          {GROUP_TABS.map((tab) => (
            <TabsTrigger key={tab.value} value={tab.value}>
              {tab.label}
            </TabsTrigger>
          ))}
        </TabsList>
      </Tabs>

      <Card className="py-4">
        <CardContent>
          <SearchInput value={search} onChange={setSearch} placeholder="코드, 코드명 검색" />
        </CardContent>
      </Card>

      <div className="flex justify-end">
        <Button onClick={handleCreate}>
          <Plus className="size-4" />
          코드 등록
        </Button>
      </div>

      <Card className="gap-0 py-0">
        <CardContent className="p-0">
          <CommonCodeTable scope="admin" items={items} onEdit={handleEdit} />

          {!isLoading && !isError && items.length === 0 && (
            <p className="p-6 text-center text-sm text-muted-foreground">
              {search ? "검색 결과가 없습니다." : "등록된 코드가 없습니다."}
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
        scope="admin"
        open={isFormOpen}
        onOpenChange={setIsFormOpen}
        groupCode={groupCode}
        target={formTarget}
      />
    </div>
  )
}
