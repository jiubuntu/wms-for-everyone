import { api } from "@/lib/axios"
import type { ApiCommonResponse } from "@/lib/apiTypes"
import type {
  CompanyOverviewData,
  WarehouseScopeDashboardData,
} from "@/features/dashboard/types"

export async function getWarehouseScopeDashboard(
  warehouseId: number
): Promise<WarehouseScopeDashboardData> {
  const res = await api.get<ApiCommonResponse<WarehouseScopeDashboardData>>(
    `/warehouse/${warehouseId}/dashboard/summary`
  )
  return res.data.data
}

export async function getCompanyOverviewDashboard(): Promise<CompanyOverviewData> {
  const res = await api.get<ApiCommonResponse<CompanyOverviewData>>("/company/dashboard/summary")
  return res.data.data
}
