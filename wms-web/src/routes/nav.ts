import { Boxes, Database, LayoutDashboard, Package, Users, Warehouse } from "lucide-react"
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
    label: "상품",
    icon: Package,
    children: [
      {
        label: "상품 관리",
        to: "/app/products",
        roles: ["COMPANY_ADMIN", "WAREHOUSE_MANAGER"],
      },
      {
        label: "상품 카테고리 관리",
        to: "/app/product-categories",
        roles: ["COMPANY_ADMIN", "WAREHOUSE_MANAGER"],
      },
      {
        label: "상품 단위 관리",
        to: "/app/product-units",
        roles: ["COMPANY_ADMIN", "WAREHOUSE_MANAGER"],
      },
    ],
  },
  {
    type: "group",
    label: "창고",
    icon: Warehouse,
    children: [
      {
        label: "창고 관리",
        to: "/app/warehouses",
        roles: ["COMPANY_ADMIN", "WAREHOUSE_MANAGER"],
      },
    ],
  },
  {
    type: "group",
    label: "재고",
    icon: Boxes,
    children: [
      {
        label: "재고 현황",
        to: "/app/inventory",
        roles: ["COMPANY_ADMIN", "WAREHOUSE_MANAGER", "WORKER"],
      },
      {
        label: "재고 이동",
        to: "/app/inventory/transfers",
        roles: ["COMPANY_ADMIN", "WAREHOUSE_MANAGER", "WORKER"],
      },
      {
        label: "재고 이력",
        to: "/app/inventory/history",
        roles: ["COMPANY_ADMIN", "WAREHOUSE_MANAGER", "WORKER"],
      },
    ],
  },
]
