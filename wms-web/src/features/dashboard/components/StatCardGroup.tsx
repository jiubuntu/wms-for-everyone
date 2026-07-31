import { Card, CardContent } from "@/components/ui/card"
import { cn } from "@/lib/utils"

type DotColor = "warning" | "primary" | "info" | "destructive"

const DOT_CLASS: Record<DotColor, string> = {
  warning: "bg-warning",
  primary: "bg-primary",
  info: "bg-info",
  destructive: "bg-destructive",
}

const SPAN_CLASS: Record<number, string> = {
  1: "col-span-1",
  2: "col-span-2",
  3: "col-span-3",
  4: "col-span-4",
}

export interface StatCardItem {
  key: string
  label: string
  value: number
  unit: string
  basis: string
  dotColor: DotColor
}

export interface StatCardGroupDef {
  title: string
  span: number
  items: StatCardItem[]
}

export function StatCardGroup({ groups }: { groups: StatCardGroupDef[] }) {
  return (
    <div className="flex flex-col gap-2">
      <div className="grid grid-cols-4 gap-4">
        {groups.map((group) => (
          <div
            key={group.title}
            className={cn(
              "border-b pb-1.5 text-xs font-bold tracking-wide text-muted-foreground uppercase",
              SPAN_CLASS[group.span]
            )}
          >
            {group.title}
          </div>
        ))}
      </div>
      <div className="grid grid-cols-4 gap-4">
        {groups.flatMap((group) =>
          group.items.map((item) => (
            <Card key={item.key} className="py-4">
              <CardContent className="flex flex-col gap-2">
                <div className="flex items-center gap-1.5">
                  <span className={cn("size-1.5 shrink-0 rounded-full", DOT_CLASS[item.dotColor])} />
                  <span className="text-xs font-semibold text-muted-foreground">{item.label}</span>
                </div>
                <div className="flex items-baseline gap-1.5">
                  <span className="text-2xl font-bold tracking-tight">{item.value.toLocaleString()}</span>
                  <span className="text-sm font-medium text-muted-foreground">{item.unit}</span>
                </div>
                <div className="border-t border-dashed pt-1.5 text-[11px] text-muted-foreground/70">
                  {item.basis}
                </div>
              </CardContent>
            </Card>
          ))
        )}
      </div>
    </div>
  )
}
