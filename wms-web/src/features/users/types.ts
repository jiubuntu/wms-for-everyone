import type { UserRole } from "@/features/auth/types"

export type IssuableUserRole = Extract<UserRole, "WAREHOUSE_MANAGER" | "WORKER">
export type UserStatus = "ACTIVE" | "LOCKED" | "INACTIVE"

export interface UserListItem {
  id: number
  email: string
  name: string
  phone: string
  role: IssuableUserRole
  status: UserStatus
  warehouseId: number
  warehouseName: string
  createdAt: string
}

export interface UserCreateInput {
  email: string
  name: string
  phone: string
  role: IssuableUserRole
  warehouseId: number
}

export interface UserIssueResult {
  id: number
  email: string
  name: string
  role: IssuableUserRole
  status: UserStatus
  warehouseId: number
  temporaryPassword: string
}
