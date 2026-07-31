import { useState } from "react"
import { Outlet } from "react-router-dom"
import { cn } from "@/lib/utils"
import { BackofficeSidebar, type NavEntry } from "@/components/common/BackofficeSidebar"
import { BackofficeHeader } from "@/components/common/BackofficeHeader"

export function BackofficeLayout({
  brandLabel,
  homeTo,
  nav,
  showWarehouseSelector,
}: {
  brandLabel: string
  homeTo: string
  nav: NavEntry[]
  showWarehouseSelector?: boolean
}) {
  const [collapsed, setCollapsed] = useState(false)

  return (
    <div className="admin-theme flex min-h-svh bg-background text-foreground">
      <BackofficeSidebar brandLabel={brandLabel} homeTo={homeTo} nav={nav} collapsed={collapsed} />

      <div className={cn("flex flex-1 flex-col", collapsed ? "pl-[80px]" : "pl-[245px]")}>
        <BackofficeHeader
          onToggleSidebar={() => setCollapsed((prev) => !prev)}
          showWarehouseSelector={showWarehouseSelector}
        />
        <main className="flex-1 p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
