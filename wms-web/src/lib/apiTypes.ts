export interface ApiCommonResponse<T> {
  httpCode: number
  message: string
  data: T
}

export interface PageInfo {
  page: number
  size: number
  totalElements: number
  totalPages: number
  hasNext: boolean
}

export interface ApiPageResponse<T> {
  content: T[]
  pageInfo: PageInfo
}
