import { useState, type ComponentType } from "react"
import { Link, NavLink, useLocation } from "react-router-dom"
import { ChevronDown, LayoutDashboard, Users, type LucideProps } from "lucide-react"
import { cn } from "@/lib/utils"

type Icon = ComponentType<LucideProps>

interface NavLeaf {
  type: "item"
  label: string
  to: string
  icon: Icon
}

interface NavGroup {
  type: "group"
  label: string
  icon: Icon
  children: { label: string; to: string }[]
}

const NAV: (NavLeaf | NavGroup)[] = [
  { type: "item", label: "대시보드", to: "/admin", icon: LayoutDashboard },
  {
    type: "group",
    label: "사용자",
    icon: Users,
    children: [{ label: "기업 승인 관리", to: "/admin/companies" }],
  },
]

const SIDEBAR_SHADOW = "shadow-[0_0_15px_0_rgba(154,161,171,0.05)]"

export function AdminSidebar({ collapsed }: { collapsed: boolean }) {
  const location = useLocation()

  return (
    <aside
      className={cn(
        "fixed inset-y-0 left-0 z-20 flex flex-col border-r bg-card transition-[width] duration-200",
        SIDEBAR_SHADOW,
        collapsed ? "w-[80px]" : "w-[245px]"
      )}
    >
      <Link to="/admin" className="flex h-[70px] shrink-0 items-center gap-2 px-4">
        {!collapsed && (
          <span className="truncate text-sm font-extrabold tracking-tight">
            모두의 WMS - 관리자
          </span>
        )}
      </Link>

      <nav className="flex flex-1 flex-col gap-1 overflow-y-auto px-2.5 py-3">
        {NAV.map((entry) =>
          entry.type === "item" ? (
            <SidebarLink
              key={entry.to}
              to={entry.to}
              label={entry.label}
              icon={entry.icon}
              collapsed={collapsed}
              end
            />
          ) : (
            <SidebarGroup
              key={entry.label}
              group={entry}
              collapsed={collapsed}
              currentPath={location.pathname}
            />
          )
        )}
      </nav>
    </aside>
  )
}

function SidebarLink({
  to,
  label,
  icon: Icon,
  collapsed,
  end,
  sub,
}: {
  to: string
  label: string
  icon?: Icon
  collapsed: boolean
  end?: boolean
  sub?: boolean
}) {
  return (
    <NavLink
      to={to}
      end={end}
      title={collapsed ? label : undefined}
      className={({ isActive }) =>
        cn(
          "flex items-center gap-2.5 rounded-[0.3rem] px-[15px] py-2 text-sm text-muted-foreground transition-colors hover:bg-primary/10 hover:text-primary",
          collapsed && "justify-center px-0",
          sub && "py-1.5 pl-11 text-[0.875rem]",
          isActive && "font-medium text-primary"
        )
      }
    >
      {Icon && <Icon className="size-5 shrink-0" />}
      {!collapsed && <span className="truncate">{label}</span>}
    </NavLink>
  )
}

function SidebarGroup({
  group,
  collapsed,
  currentPath,
}: {
  group: NavGroup
  collapsed: boolean
  currentPath: string
}) {
  const hasActiveChild = group.children.some((child) => currentPath.startsWith(child.to))
  const [open, setOpen] = useState(hasActiveChild)
  const Icon = group.icon

  if (collapsed) {
    return (
      <div
        title={group.label}
        className={cn(
          "flex items-center justify-center rounded-[0.3rem] px-0 py-2 text-muted-foreground",
          hasActiveChild && "font-medium text-primary"
        )}
      >
        <Icon className="size-5 shrink-0" />
      </div>
    )
  }

  return (
    <div>
      <button
        type="button"
        onClick={() => setOpen((prev) => !prev)}
        className={cn(
          "flex w-full items-center gap-2.5 rounded-[0.3rem] px-[15px] py-2 text-sm text-muted-foreground transition-colors hover:bg-primary/10 hover:text-primary",
          hasActiveChild && "font-medium text-primary"
        )}
      >
        <Icon className="size-5 shrink-0" />
        <span className="flex-1 truncate text-left">{group.label}</span>
        <ChevronDown
          className={cn("size-3.5 shrink-0 transition-transform", open && "rotate-180")}
        />
      </button>

      {open && (
        <div className="mt-0.5 flex flex-col gap-0.5">
          {group.children.map((child) => (
            <SidebarLink key={child.to} to={child.to} label={child.label} collapsed={false} sub />
          ))}
        </div>
      )}
    </div>
  )
}
