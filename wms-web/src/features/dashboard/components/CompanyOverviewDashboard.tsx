import { useCompanyOverviewDashboard } from "@/features/dashboard/hooks"
import { StatCardGroup, type StatCardGroupDef } from "@/features/dashboard/components/StatCardGroup"
import { DashboardPanelCard } from "@/features/dashboard/components/DashboardPanelCard"
import { WarehouseCompareChart } from "@/features/dashboard/components/charts/WarehouseCompareChart"
import { ProcessingTrendChart } from "@/features/dashboard/components/charts/ProcessingTrendChart"
import { StageDonutChart } from "@/features/dashboard/components/charts/StageDonutChart"
import { WarehouseOpsTable } from "@/features/dashboard/components/WarehouseOpsTable"

interface CompanyOverviewDashboardProps {
  onSelectWarehouse: (warehouseId: number) => void
}

export function CompanyOverviewDashboard({ onSelectWarehouse }: CompanyOverviewDashboardProps) {
  const { data, isLoading, isError } = useCompanyOverviewDashboard()

  if (isLoading) {
    return <p className="text-sm text-muted-foreground">불러오는 중...</p>
  }

  if (isError || !data) {
    return <p className="text-sm text-destructive">대시보드를 불러오지 못했습니다.</p>
  }

  const groups: StatCardGroupDef[] = [
    {
      title: "",
      span: 2,
      items: [
        {
          key: "outboundPending",
          label: "등록대기",
          value: data.companyStats.outboundPending,
          unit: "건",
          basis: "",
          dotColor: "warning",
        },
        {
          key: "outboundPicking",
          label: "피킹중",
          value: data.companyStats.outboundPicking,
          unit: "건",
          basis: "",
          dotColor: "primary",
        },
      ],
    },
    {
      title: "",
      span: 1,
      items: [
        {
          key: "inboundPending",
          label: "입고 대기",
          value: data.companyStats.inboundPending,
          unit: "건",
          basis: "",
          dotColor: "info",
        },
      ],
    },
    {
      title: "",
      span: 1,
      items: [
        {
          key: "expiringSoon",
          label: "유통기한 임박",
          value: data.companyStats.expiringSoonCount,
          unit: "건",
          basis: "",
          dotColor: "destructive",
        },
      ],
    },
  ]

  const barData = data.warehouseOps.map((row) => ({
    warehouseName: row.warehouseName,
    outboundPending: row.outboundPending,
    outboundPicking: row.outboundPicking,
    inboundPending: row.inboundPending,
  }))

  const outboundDonutData = [
    { key: "pending", label: "대기", value: data.todayOutboundStatus.pending, color: "var(--chart-1)" },
    { key: "inProgress", label: "피킹중", value: data.todayOutboundStatus.inProgress, color: "var(--chart-2)" },
    { key: "completed", label: "출하 완료", value: data.todayOutboundStatus.completed, color: "var(--chart-4)" },
    { key: "cancelled", label: "취소", value: data.todayOutboundStatus.cancelled, color: "var(--chart-5)" },
  ]
  const inboundDonutData = [
    { key: "pending", label: "대기", value: data.todayInboundStatus.pending, color: "var(--chart-1)" },
    { key: "inProgress", label: "위치배치중", value: data.todayInboundStatus.inProgress, color: "var(--chart-2)" },
    { key: "completed", label: "입고 완료", value: data.todayInboundStatus.completed, color: "var(--chart-4)" },
    { key: "cancelled", label: "취소", value: data.todayInboundStatus.cancelled, color: "var(--chart-5)" },
  ]
  return (
    <div className="flex flex-col gap-4">


      <StatCardGroup groups={groups} />

      <div className="grid gap-4 lg:grid-cols-2">
        <DashboardPanelCard
          title="창고별 입출고 대기 현황"
          bodyClassName="px-4 pb-4"
        >
          <WarehouseCompareChart data={barData} />
        </DashboardPanelCard>
        <DashboardPanelCard
          title="최근 14일 입고/출고 처리 건수 추이"
          bodyClassName="px-4 pb-4"
        >
          <ProcessingTrendChart data={data.processingTrend} />
        </DashboardPanelCard>
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        <DashboardPanelCard
          title="금일 출고 처리 현황"
          bodyClassName="px-4 pb-4"
        >
          <StageDonutChart data={outboundDonutData} centerCaption="건" />
        </DashboardPanelCard>
        <DashboardPanelCard
          title="금일 입고 처리 현황"

          bodyClassName="px-4 pb-4"
        >
          <StageDonutChart data={inboundDonutData} centerCaption="건" />
        </DashboardPanelCard>
      </div>

      <DashboardPanelCard
        title="창고별 운영 현황"
      >
        <WarehouseOpsTable rows={data.warehouseOps} onSelectWarehouse={onSelectWarehouse} />
      </DashboardPanelCard>
    </div>
  )
}
