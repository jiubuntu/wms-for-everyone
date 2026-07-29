import type { ReactNode } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { cn } from "@/lib/utils"

interface DashboardPanelCardProps {
  title: string
  caption?: string
  children: ReactNode
  bodyClassName?: string
}

export function DashboardPanelCard({ title, caption, children, bodyClassName }: DashboardPanelCardProps) {
  return (
    <Card className="gap-0 py-0">
      <CardHeader className="flex-row items-baseline justify-between gap-2 border-b border-dashed py-4">
        <CardTitle className="text-sm">{title}</CardTitle>
        {caption && <p className="text-[11px] text-muted-foreground">{caption}</p>}
      </CardHeader>
      <CardContent className={cn("p-0", bodyClassName)}>{children}</CardContent>
    </Card>
  )
}
