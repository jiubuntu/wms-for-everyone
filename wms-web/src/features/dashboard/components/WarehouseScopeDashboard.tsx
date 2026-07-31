import { useWarehouseFilter } from "@/contexts/WarehouseFilterContext"
import { useWarehouseScopeDashboard } from "@/features/dashboard/hooks"
import { StatCardGroup, type StatCardGroupDef } from "@/features/dashboard/components/StatCardGroup"
import { DashboardPanelCard } from "@/features/dashboard/components/DashboardPanelCard"
import { OutboundQueueTable } from "@/features/dashboard/components/OutboundQueueTable"
import { InboundQueueTable } from "@/features/dashboard/components/InboundQueueTable"
import { ExpiryList } from "@/features/dashboard/components/ExpiryList"
import { InventoryStatusTable } from "@/features/dashboard/components/InventoryStatusTable"

export function WarehouseScopeDashboard() {
  const { warehouseId } = useWarehouseFilter()
  const { data, isLoading, isError } = useWarehouseScopeDashboard(warehouseId)

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
          value: data.stats.outboundPending,
          unit: "건",
          basis: ``,
          dotColor: "warning",
        },
        {
          key: "outboundPicking",
          label: "피킹중",
          value: data.stats.outboundPicking,
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
          value: data.stats.inboundPending,
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
          label: "유통기한 임박 재고",
          value: data.stats.expiringSoonCount,
          unit: "건",
          basis: "",
          dotColor: "destructive",
        },
      ],
    },
  ]

  return (
    <div className="flex flex-col gap-4">
      <StatCardGroup groups={groups} />

      <div className="grid gap-4 lg:grid-cols-2">
        <DashboardPanelCard title="출고 대기 목록">
          <OutboundQueueTable items={data.outboundQueue} />
        </DashboardPanelCard>
        <DashboardPanelCard title="입고 대기 목록" >
          <InboundQueueTable items={data.inboundQueue} />
        </DashboardPanelCard>
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        <DashboardPanelCard title="유통기한 임박 재고">
          <ExpiryList items={data.expiringInventory} />
        </DashboardPanelCard>
        <DashboardPanelCard title="재고 현황" >
          <InventoryStatusTable items={data.inventoryStatus} />
        </DashboardPanelCard>
      </div>
    </div>
  )
}
