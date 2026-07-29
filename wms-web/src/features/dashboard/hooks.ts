import { useQuery } from "@tanstack/react-query"
import { getCompanyOverviewDashboard, getWarehouseScopeDashboard } from "@/features/dashboard/api"

export function useWarehouseScopeDashboard(warehouseId: number | null) {
  return useQuery({
    queryKey: ["dashboard", "warehouse-scope", warehouseId],
    queryFn: () => getWarehouseScopeDashboard(warehouseId as number),
    enabled: warehouseId != null,
  })
}

export function useCompanyOverviewDashboard() {
  return useQuery({
    queryKey: ["dashboard", "company-overview"],
    queryFn: () => getCompanyOverviewDashboard(),
  })
}
