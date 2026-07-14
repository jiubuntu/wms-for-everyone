import { useState } from "react"
import { Link } from "react-router-dom"
import dayjs from "dayjs"
import { usePendingCompanies } from "@/features/company-admin/hooks"
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

const PAGE_SIZE = 20

export function CompanyApprovalListPage() {
  const [page, setPage] = useState(1)
  const { data, isLoading, isError } = usePendingCompanies(page, PAGE_SIZE)

  return (
    <div className="flex max-w-4xl flex-col gap-4">
      <div>
        <h1 className="text-2xl font-bold">기업 승인 관리</h1>
        <p className="text-base text-muted-foreground">
          가입 승인을 기다리고 있는 기업 목록입니다.
        </p>
      </div>

      <div className="rounded-lg border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>기업명</TableHead>
              <TableHead>사업자등록번호</TableHead>
              <TableHead>상태</TableHead>
              <TableHead>신청일</TableHead>
              <TableHead />
            </TableRow>
          </TableHeader>
          <TableBody>
            {data?.content.map((company) => (
              <TableRow key={company.id}>
                <TableCell className="font-medium">{company.name}</TableCell>
                <TableCell>{company.businessNumber}</TableCell>
                <TableCell>
                  <Badge>승인 대기</Badge>
                </TableCell>
                <TableCell>{dayjs(company.createdAt).format("YYYY-MM-DD HH:mm")}</TableCell>
                <TableCell>
                  <Button variant="outline" size="sm" asChild>
                    <Link to={`/admin/companies/${company.id}`}>상세보기</Link>
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>

        {!isLoading && !isError && data?.content.length === 0 && (
          <p className="p-6 text-center text-sm text-muted-foreground">
            승인 대기 중인 기업이 없습니다.
          </p>
        )}
      </div>

      {isLoading && <p className="text-sm text-muted-foreground">불러오는 중...</p>}
      {isError && <p className="text-sm text-destructive">목록을 불러오지 못했습니다.</p>}

      {data && data.pageInfo.totalPages > 1 && (
        <div className="flex items-center justify-center gap-2">
          <Button
            variant="outline"
            size="sm"
            disabled={page <= 1}
            onClick={() => setPage((p) => p - 1)}
          >
            이전
          </Button>
          <span className="text-sm text-muted-foreground">
            {data.pageInfo.page} / {data.pageInfo.totalPages}
          </span>
          <Button
            variant="outline"
            size="sm"
            disabled={!data.pageInfo.hasNext}
            onClick={() => setPage((p) => p + 1)}
          >
            다음
          </Button>
        </div>
      )}
    </div>
  )
}
