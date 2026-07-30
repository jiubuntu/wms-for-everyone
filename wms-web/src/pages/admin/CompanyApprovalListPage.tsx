import { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import dayjs from "dayjs"
import { Eye } from "lucide-react"
import { usePendingCompanies } from "@/features/company-admin/hooks"
import { PageHeader } from "@/components/common/PageHeader"
import { SearchInput } from "@/components/common/SearchInput"
import { DataTablePagination } from "@/components/common/DataTablePagination"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"

const PAGE_SIZE = 10

export function CompanyApprovalListPage() {
  const [page, setPage] = useState(1)
  const [search, setSearch] = useState("")
  useEffect(() => {
    setPage(1)
  }, [search])

  const { data, isLoading, isError } = usePendingCompanies(search, page, PAGE_SIZE)
  const companies = data?.content ?? []

  return (
    <div className="flex flex-col gap-4">
      <PageHeader
        title="기업 승인 관리"
        breadcrumb={[{ label: "홈", to: "/admin" }, { label: "기업 승인 관리" }]}
      />

      <Card className="py-4">
        <CardContent>
          <SearchInput
            value={search}
            onChange={setSearch}
            placeholder="기업명, 사업자등록번호 검색"
          />
        </CardContent>
      </Card>

      <Card className="gap-0 py-0">
        <CardContent className="p-0">
          <Table>
            <TableHeader>
              <TableRow className="bg-muted/50 hover:bg-muted/50">
                <TableHead className="text-xs text-muted-foreground uppercase">기업명</TableHead>
                <TableHead className="text-xs text-muted-foreground uppercase">
                  사업자등록번호
                </TableHead>
                <TableHead className="text-xs text-muted-foreground uppercase">상태</TableHead>
                <TableHead className="text-xs text-muted-foreground uppercase">신청일</TableHead>
                <TableHead />
              </TableRow>
            </TableHeader>
            <TableBody>
              {companies.map((company) => (
                <TableRow key={company.id}>
                  <TableCell className="font-medium">{company.name}</TableCell>
                  <TableCell>{company.businessNumber}</TableCell>
                  <TableCell>
                    <Badge variant="warning">승인 대기</Badge>
                  </TableCell>
                  <TableCell>{dayjs(company.createdAt).format("YYYY-MM-DD HH:mm")}</TableCell>
                  <TableCell className="text-right">
                    <Button
                      variant="ghost"
                      size="icon"
                      className="size-8 rounded-full bg-primary/10 text-primary hover:bg-primary/20 hover:text-primary"
                      asChild
                    >
                      <Link to={`/admin/companies/${company.id}`} aria-label="상세보기">
                        <Eye className="size-4" />
                      </Link>
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>

          {!isLoading && !isError && companies.length === 0 && (
            <p className="p-6 text-center text-sm text-muted-foreground">
              {search
                ? "검색 결과가 없습니다."
                : "승인 대기 중인 기업이 없습니다."}
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
