import { Warehouse } from "lucide-react"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { useWarehouseFilter } from "@/contexts/WarehouseFilterContext"

export function WarehouseSelector() {
  const { warehouseId, setWarehouseId, warehouses, isLoading } = useWarehouseFilter()

  if (isLoading || warehouses.length <= 1) return null

  return (
    <Select
      value={warehouseId ? String(warehouseId) : undefined}
      onValueChange={(value) => setWarehouseId(Number(value))}
    >
      <SelectTrigger className="w-[180px] gap-2 border-0 bg-muted" aria-label="작업 창고 선택">
        <Warehouse className="size-4 text-muted-foreground" />
        <SelectValue placeholder="창고 선택" />
      </SelectTrigger>
      <SelectContent>
        {warehouses.map((w) => (
          <SelectItem key={w.id} value={String(w.id)}>
            {w.name}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  )
}
