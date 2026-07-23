import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { approveCompany, getCompanyDetail, getPendingCompanies } from "@/features/company-admin/api"

export function usePendingCompanies(page: number, limit = 10) {
  return useQuery({
    queryKey: ["companies", "pending", page, limit],
    queryFn: () => getPendingCompanies(page, limit),
  })
}

export function useCompanyDetail(companyId: number) {
  return useQuery({
    queryKey: ["companies", companyId],
    queryFn: () => getCompanyDetail(companyId),
  })
}

export function useApproveCompany() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => approveCompany(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["companies"] })
    },
  })
}
