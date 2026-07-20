export type CommonCodeGroup = "PRODUCT_CATEGORY" | "STORAGE_TYPE" | "TRANSFER_REASON"

export type CommonCodeScope = "admin" | "company"

export interface CommonCodeItem {
  id: number
  companyId: number | null
  groupCode: CommonCodeGroup
  code: string
  name: string
  sortOrder: number
  createdAt: string
}

export interface CommonCodeCreateInput {
  groupCode: CommonCodeGroup
  code: string
  name: string
  sortOrder: number
}

export interface CommonCodeUpdateInput {
  name: string
  sortOrder: number
}
