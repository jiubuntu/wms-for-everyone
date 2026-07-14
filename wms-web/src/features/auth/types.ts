export type UserRole = "SYSTEM_ADMIN" | "COMPANY_ADMIN" | "WAREHOUSE_MANAGER" | "WORKER"

export interface LoginResult {
  accessToken: string
  userId: number
  email: string
  name: string
  role: UserRole
}

export interface AuthUser {
  userId: number
  companyId: number | null
  role: UserRole
  email?: string
  name?: string
}
