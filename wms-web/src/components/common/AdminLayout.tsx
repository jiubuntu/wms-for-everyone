import { useState } from "react"
import { Outlet } from "react-router-dom"
import { cn } from "@/lib/utils"
import { AdminSidebar } from "@/components/common/AdminSidebar"
import { AdminHeader } from "@/components/common/AdminHeader"

export function AdminLayout() {
  const [collapsed, setCollapsed] = useState(false)

  return (
    <div className="admin-theme flex min-h-svh bg-background text-foreground">
      <AdminSidebar collapsed={collapsed} />

      <div className={cn("flex flex-1 flex-col", collapsed ? "pl-[80px]" : "pl-[245px]")}>
        <AdminHeader onToggleSidebar={() => setCollapsed((prev) => !prev)} />
        <main className="flex-1 p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
