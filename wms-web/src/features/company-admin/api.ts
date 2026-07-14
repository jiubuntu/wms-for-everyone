import { api } from "@/lib/axios"
import type { ApiCommonResponse, ApiPageResponse } from "@/lib/apiTypes"
import type { CompanyDetail, CompanyListItem, CompanyStatus } from "@/features/company-admin/types"

export async function getPendingCompanies(
  page: number,
  limit: number
): Promise<ApiPageResponse<CompanyListItem>> {
  const res = await api.get<ApiCommonResponse<ApiPageResponse<CompanyListItem>>>(
    "/admin/company/list",
    { params: { page, limit } }
  )
  return res.data.data
}

export async function getCompanyDetail(companyId: number): Promise<CompanyDetail> {
  const res = await api.get<ApiCommonResponse<CompanyDetail>>("/admin/company", {
    params: { companyId },
  })
  return res.data.data
}

export async function approveCompany(id: number): Promise<{ id: number; status: CompanyStatus }> {
  const res = await api.post<ApiCommonResponse<{ id: number; status: CompanyStatus }>>(
    `/admin/company/${id}/approve`
  )
  return res.data.data
}
