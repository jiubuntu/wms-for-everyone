import { LayoutDashboard, Users } from "lucide-react"
import type { NavEntry } from "@/components/common/BackofficeSidebar"

export const ADMIN_NAV: NavEntry[] = [
  { type: "item", label: "대시보드", to: "/admin", icon: LayoutDashboard },
  {
    type: "group",
    label: "사용자",
    icon: Users,
    children: [{ label: "기업 승인 관리", to: "/admin/companies" }],
  },
]

export const APP_NAV: NavEntry[] = [{ type: "item", label: "대시보드", to: "/app", icon: LayoutDashboard }]
