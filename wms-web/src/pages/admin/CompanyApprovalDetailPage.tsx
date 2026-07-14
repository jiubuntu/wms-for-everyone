import { useNavigate, useParams } from "react-router-dom"
import dayjs from "dayjs"
import { useApproveCompany, useCompanyDetail } from "@/features/company-admin/hooks"
import type { CompanyStatus } from "@/features/company-admin/types"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"

const STATUS_LABEL: Record<CompanyStatus, string> = {
  PENDING: "승인 대기",
  ACTIVE: "승인 완료",
  SUSPENDED: "정지",
}

export function CompanyApprovalDetailPage() {
  const { id } = useParams<{ id: string }>()
  const companyId = Number(id)
  const navigate = useNavigate()

  const { data: company, isLoading, isError } = useCompanyDetail(companyId)
  const approveMutation = useApproveCompany()

  async function handleApprove() {
    if (!window.confirm("이 기업을 승인하시겠습니까?")) return
    await approveMutation.mutateAsync(companyId)
    navigate("/admin/companies")
  }

  if (isLoading) {
    return <p className="text-sm text-muted-foreground">불러오는 중...</p>
  }

  if (isError || !company) {
    return <p className="text-sm text-destructive">기업 정보를 불러오지 못했습니다.</p>
  }

  return (
    <div className="flex max-w-2xl flex-col gap-4">
      <h1 className="text-2xl font-bold">기업 상세</h1>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-lg">
            {company.name}
            <Badge>{STATUS_LABEL[company.status]}</Badge>
          </CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-3 text-sm">
          <DetailRow label="사업자등록번호" value={company.businessNumber} />
          <DetailRow label="신청일" value={dayjs(company.createdAt).format("YYYY-MM-DD HH:mm")} />
          <div className="flex flex-col gap-1.5 border-b pb-2">
            <span className="text-xs text-muted-foreground">사업자등록증</span>
            <a
              href={company.businessLicenseFileUrl}
              target="_blank"
              rel="noreferrer"
              className="text-primary hover:underline"
            >
              첨부파일 열기
            </a>
          </div>
        </CardContent>
      </Card>

      {company.status === "PENDING" && (
        <Button onClick={handleApprove} disabled={approveMutation.isPending} className="w-fit">
          승인
        </Button>
      )}
    </div>
  )
}

function DetailRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between border-b pb-2">
      <span className="text-muted-foreground">{label}</span>
      <span className="font-medium">{value}</span>
    </div>
  )
}
