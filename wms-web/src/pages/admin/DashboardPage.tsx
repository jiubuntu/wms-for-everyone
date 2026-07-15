import { Link } from "react-router-dom"
import dayjs from "dayjs"
import { Building2, FileClock, ShieldCheck, Users } from "lucide-react"
import { PageHeader } from "@/components/common/PageHeader"
import { Badge } from "@/components/ui/badge"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"

const STAT_CARDS = [
  { label: "전체 가입 기업", value: "128", icon: Building2 },
  { label: "승인 대기 기업", value: "6", icon: FileClock, to: "/admin/companies" },
  { label: "전체 사용자", value: "842", icon: Users },
  { label: "오늘 신규 가입 신청", value: "3", icon: ShieldCheck },
]

const RECENT_SIGNUPS = [
  { id: 1, name: "모두의커머스", businessNumber: "123-45-67890", createdAt: "2026-07-14T09:12:00", status: "PENDING" as const },
  { id: 2, name: "다온물류", businessNumber: "234-56-78901", createdAt: "2026-07-13T15:40:00", status: "PENDING" as const },
  { id: 3, name: "하늘유통", businessNumber: "345-67-89012", createdAt: "2026-07-12T11:05:00", status: "ACTIVE" as const },
  { id: 4, name: "온새로운", businessNumber: "456-78-90123", createdAt: "2026-07-11T08:30:00", status: "ACTIVE" as const },
  { id: 5, name: "그린박스", businessNumber: "567-89-01234", createdAt: "2026-07-10T14:20:00", status: "ACTIVE" as const },
]

const STATUS_LABEL: Record<string, string> = {
  PENDING: "승인 대기",
  ACTIVE: "승인 완료",
}

export function DashboardPage() {
  return (
    <div className="flex flex-col gap-6">
      <PageHeader title="대시보드" breadcrumb={[{ label: "홈" }, { label: "대시보드" }]} />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {STAT_CARDS.map((stat) => {
          const Icon = stat.icon
          const content = (
            <Card key={stat.label} className="gap-2 py-5">
              <CardContent className="flex items-center justify-between px-5">
                <div>
                  <p className="text-xs text-muted-foreground">{stat.label}</p>
                  <p className="mt-1.5 text-2xl font-bold">{stat.value}</p>
                </div>
                <span className="grid size-10 shrink-0 place-items-center rounded-lg bg-primary/10 text-primary">
                  <Icon className="size-5" />
                </span>
              </CardContent>
            </Card>
          )
          return stat.to ? (
            <Link key={stat.label} to={stat.to} className="block">
              {content}
            </Link>
          ) : (
            content
          )
        })}
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-base">월별 기업 가입 추이</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex aspect-[2/1] items-center justify-center rounded-lg border border-dashed bg-muted/40 text-xs text-muted-foreground">
              차트 자리 (추후 확장)
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle className="text-base">역할별 사용자 분포</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex aspect-[2/1] items-center justify-center rounded-lg border border-dashed bg-muted/40 text-xs text-muted-foreground">
              차트 자리 (추후 확장)
            </div>
          </CardContent>
        </Card>
      </div>

      <Card className="py-0">
        <CardHeader className="pt-5">
          <CardTitle className="text-base">최근 가입 신청</CardTitle>
        </CardHeader>
        <CardContent className="px-0 pb-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>기업명</TableHead>
                <TableHead>사업자등록번호</TableHead>
                <TableHead>신청일</TableHead>
                <TableHead>상태</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {RECENT_SIGNUPS.map((company) => (
                <TableRow key={company.id}>
                  <TableCell className="font-medium">{company.name}</TableCell>
                  <TableCell>{company.businessNumber}</TableCell>
                  <TableCell>{dayjs(company.createdAt).format("YYYY-MM-DD HH:mm")}</TableCell>
                  <TableCell>
                    <Badge variant={company.status === "PENDING" ? "warning" : "success"}>
                      {STATUS_LABEL[company.status]}
                    </Badge>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  )
}
