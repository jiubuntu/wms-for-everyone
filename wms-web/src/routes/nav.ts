import { Database, LayoutDashboard, Users } from "lucide-react"
import type { NavEntry } from "@/components/common/BackofficeSidebar"

export const ADMIN_NAV: NavEntry[] = [
  { type: "item", label: "대시보드", to: "/admin", icon: LayoutDashboard },
  {
    type: "group",
    label: "마스터 데이터",
    icon: Database,
    children: [{ label: "공통 코드 관리", to: "/admin/common-codes" }],
  },
  {
    type: "group",
    label: "사용자",
    icon: Users,
    children: [{ label: "기업 승인 관리", to: "/admin/companies" }],
  },
]

export const APP_NAV: NavEntry[] = [
  { type: "item", label: "대시보드", to: "/app", icon: LayoutDashboard },
  {
    type: "group",
    label: "마스터 데이터",
    icon: Database,
    children: [
      { label: "상품 카테고리 관리", to: "/app/product-categories", roles: ["COMPANY_ADMIN"] },
      {
        label: "상품 단위 관리",
        to: "/app/product-units",
        roles: ["COMPANY_ADMIN", "WAREHOUSE_MANAGER"],
      },
    ],
  },
]
