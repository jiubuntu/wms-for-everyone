import { useState, type ComponentType } from "react"
import { Link, NavLink, useLocation } from "react-router-dom"
import { ChevronDown, type LucideProps } from "lucide-react"
import { cn } from "@/lib/utils"
import { useAuth } from "@/contexts/AuthContext"
import type { UserRole } from "@/features/auth/types"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"

type Icon = ComponentType<LucideProps>

export interface NavLeaf {
  type: "item"
  label: string
  to: string
  icon: Icon
  roles?: UserRole[]
}

export interface NavGroup {
  type: "group"
  label: string
  icon: Icon
  children: { label: string; to: string; roles?: UserRole[] }[]
}

export type NavEntry = NavLeaf | NavGroup

const SIDEBAR_SHADOW = "shadow-[0_0_15px_0_rgba(154,161,171,0.05)]"

function isVisible(roles: UserRole[] | undefined, role: UserRole | undefined) {
  if (!roles) return true
  if (!role) return false
  return roles.includes(role)
}

export function BackofficeSidebar({
  brandLabel,
  homeTo,
  nav,
  collapsed,
}: {
  brandLabel: string
  homeTo: string
  nav: NavEntry[]
  collapsed: boolean
}) {
  const location = useLocation()
  const { user } = useAuth()

  const visibleNav = nav
    .filter((entry) => (entry.type === "item" ? isVisible(entry.roles, user?.role) : true))
    .map((entry) =>
      entry.type === "group"
        ? { ...entry, children: entry.children.filter((child) => isVisible(child.roles, user?.role)) }
        : entry
    )
    .filter((entry) => entry.type === "item" || entry.children.length > 0)

  return (
    <aside
      className={cn(
        "fixed inset-y-0 left-0 z-20 flex flex-col border-r bg-card transition-[width] duration-200",
        SIDEBAR_SHADOW,
        collapsed ? "w-[80px]" : "w-[245px]"
      )}
    >
      <Link to={homeTo} className="flex h-[70px] shrink-0 items-center gap-2 px-4">
        {!collapsed && (
          <span className="truncate text-sm font-extrabold tracking-tight">{brandLabel}</span>
        )}
      </Link>

      <nav className="flex flex-1 flex-col gap-1 overflow-y-auto px-2.5 py-3">
        {visibleNav.map((entry) =>
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
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <button
            type="button"
            title={group.label}
            className={cn(
              "flex w-full cursor-pointer items-center justify-center rounded-[0.3rem] px-0 py-2 text-muted-foreground transition-colors hover:bg-primary/10 hover:text-primary",
              hasActiveChild && "font-medium text-primary"
            )}
          >
            <Icon className="size-5 shrink-0" />
          </button>
        </DropdownMenuTrigger>
        <DropdownMenuContent side="right" align="start">
          <DropdownMenuLabel>{group.label}</DropdownMenuLabel>
          <DropdownMenuSeparator />
          {group.children.map((child) => (
            <DropdownMenuItem key={child.to} asChild>
              <Link to={child.to}>{child.label}</Link>
            </DropdownMenuItem>
          ))}
        </DropdownMenuContent>
      </DropdownMenu>
    )
  }

  return (
    <div>
      <button
        type="button"
        onClick={() => setOpen((prev) => !prev)}
        className={cn(
          "flex w-full cursor-pointer items-center gap-2.5 rounded-[0.3rem] px-[15px] py-2 text-sm text-muted-foreground transition-colors hover:bg-primary/10 hover:text-primary",
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
            <SidebarLink key={child.to} to={child.to} label={child.label} collapsed={false} sub end />
          ))}
        </div>
      )}
    </div>
  )
}
