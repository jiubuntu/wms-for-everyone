import { useAuth } from "@/contexts/AuthContext"
import { PageHeader } from "@/components/common/PageHeader"
import { Card, CardContent } from "@/components/ui/card"

export function DashboardPage() {
  const { user } = useAuth()

  return (
    <div className="flex flex-col gap-4">
      <PageHeader title="대시보드" breadcrumb={[{ label: "홈" }, { label: "대시보드" }]} />

      <Card>
        <CardContent>
          <p className="text-sm text-muted-foreground">
            {user?.name ?? user?.email}님, 모두의 WMS에 오신 것을 환영합니다.
          </p>
        </CardContent>
      </Card>
    </div>
  )
}
