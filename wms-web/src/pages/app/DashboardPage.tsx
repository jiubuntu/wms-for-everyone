import { useState } from "react"
import { useAuth } from "@/contexts/AuthContext"
import { useWarehouseFilter } from "@/contexts/WarehouseFilterContext"
import { PageHeader } from "@/components/common/PageHeader"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { CompanyOverviewDashboard } from "@/features/dashboard/components/CompanyOverviewDashboard"
import { WarehouseScopeDashboard } from "@/features/dashboard/components/WarehouseScopeDashboard"

export function DashboardPage() {
  const { user } = useAuth()
  const { setWarehouseId } = useWarehouseFilter()
  const [tab, setTab] = useState("all")

  function handleSelectWarehouse(warehouseId: number) {
    setWarehouseId(warehouseId)
    setTab("one")
  }

  return (
    <div className="flex flex-col gap-4">
      <PageHeader title="대시보드" breadcrumb={[{ label: "홈" }, { label: "대시보드" }]} />

      {user?.role === "COMPANY_ADMIN" ? (
        <Tabs value={tab} onValueChange={setTab}>
          <TabsList variant="line" className="h-10! gap-4">
            <TabsTrigger value="all" className="px-1 text-base">
              전체 창고
            </TabsTrigger>
            <TabsTrigger value="one" className="px-1 text-base">
              창고별
            </TabsTrigger>
          </TabsList>
          <TabsContent value="all">
            <CompanyOverviewDashboard onSelectWarehouse={handleSelectWarehouse} />
          </TabsContent>
          <TabsContent value="one">
            <WarehouseScopeDashboard />
          </TabsContent>
        </Tabs>
      ) : (
        <WarehouseScopeDashboard />
      )}
    </div>
  )
}
