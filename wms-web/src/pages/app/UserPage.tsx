import { useEffect, useState } from "react"
import { isAxiosError } from "axios"
import { Plus } from "lucide-react"
import { PageHeader } from "@/components/common/PageHeader"
import { SearchInput } from "@/components/common/SearchInput"
import { DataTablePagination } from "@/components/common/DataTablePagination"
import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { useAuth } from "@/contexts/AuthContext"
import { useUsers, useWithdrawUser } from "@/features/users/hooks"
import { UserTable } from "@/features/users/components/UserTable"
import { UserIssueDialog } from "@/features/users/components/UserIssueDialog"
import type { UserListItem } from "@/features/users/types"

const PAGE_SIZE = 10

export function UserPage() {
  const { user } = useAuth()
  const [page, setPage] = useState(1)
  const [search, setSearch] = useState("")
  const [isIssueOpen, setIsIssueOpen] = useState(false)
  const [error, setError] = useState("")

  useEffect(() => {
    setPage(1)
  }, [search])

  const { data, isLoading, isError } = useUsers(search, page, PAGE_SIZE)
  const items = data?.content ?? []
  const withdrawMutation = useWithdrawUser()

  // 창고관리자는 담당 창고 작업자만 탈퇴 가능 — 다른 창고관리자는 기업관리자만 처리 가능 (docs/domains/users.md 비활성화 정책)
  function canWithdraw(item: UserListItem) {
    if (item.status === "INACTIVE") return false
    if (user?.role === "WAREHOUSE_MANAGER") return item.role === "WORKER"
    return true
  }

  async function handleWithdraw(item: UserListItem) {
    if (!window.confirm(`${item.name}(${item.email}) 계정을 탈퇴 처리하시겠습니까?`)) return
    setError("")
    try {
      await withdrawMutation.mutateAsync(item.id)
    } catch (err) {
      const message = isAxiosError(err) ? err.response?.data?.message : null
      setError(message ?? "탈퇴 처리에 실패했습니다.")
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <PageHeader title="직원 관리" breadcrumb={[{ label: "홈", to: "/app" }, { label: "직원 관리" }]} />

      <Card className="py-4">
        <CardContent>
          <SearchInput value={search} onChange={setSearch} placeholder="이메일, 이름 검색" />
        </CardContent>
      </Card>

      <div className="flex justify-end">
        <Button onClick={() => setIsIssueOpen(true)}>
          <Plus className="size-4" />
          직원 등록
        </Button>
      </div>

      <Card className="gap-0 py-0">
        <CardContent className="p-0">
          <UserTable items={items} onWithdraw={handleWithdraw} canWithdraw={canWithdraw} />

          {!isLoading && !isError && items.length === 0 && (
            <p className="p-6 text-center text-sm text-muted-foreground">
              {search ? "검색 결과가 없습니다." : "등록된 직원이 없습니다."}
            </p>
          )}
        </CardContent>
      </Card>

      {isLoading && <p className="text-sm text-muted-foreground">불러오는 중...</p>}
      {isError && <p className="text-sm text-destructive">목록을 불러오지 못했습니다.</p>}
      {error && <p className="text-sm text-destructive">{error}</p>}

      {data && data.pageInfo.totalElements > 0 && (
        <DataTablePagination pageInfo={data.pageInfo} onPageChange={setPage} />
      )}

      <UserIssueDialog open={isIssueOpen} onOpenChange={setIsIssueOpen} />
    </div>
  )
}
