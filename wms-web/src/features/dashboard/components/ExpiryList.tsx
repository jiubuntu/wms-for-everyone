import { Badge } from "@/components/ui/badge"
import type { ExpiringInventoryItem } from "@/features/dashboard/types"

export function ExpiryList({ items }: { items: ExpiringInventoryItem[] }) {
  if (items.length === 0) {
    return <p className="p-6 text-center text-sm text-muted-foreground">유통기한 임박 재고가 없습니다.</p>
  }

  return (
    <div className="flex flex-col">
      {items.map((item) => (
        <div
          key={`${item.lotNumber}-${item.locationCode}`}
          className="flex items-center justify-between gap-3 border-t px-4 py-2.5 first:border-t-0"
        >
          <div className="min-w-0">
            <p className="truncate text-sm font-medium">
              {item.productName} · {item.lotNumber}
            </p>
            <p className="text-xs text-muted-foreground">
              {item.locationCode} · {item.quantity}개
            </p>
          </div>
          <Badge variant={item.daysLeft <= 3 ? "destructive" : "warning"}>D-{item.daysLeft}</Badge>
        </div>
      ))}
    </div>
  )
}
