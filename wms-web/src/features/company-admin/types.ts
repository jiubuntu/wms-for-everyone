export type CompanyStatus = "PENDING" | "ACTIVE" | "SUSPENDED"

export interface CompanyListItem {
  id: number
  name: string
  businessNumber: string
  status: CompanyStatus
  createdAt: string
}

export interface CompanyDetail extends CompanyListItem {
  businessLicenseFileUrl: string
}
