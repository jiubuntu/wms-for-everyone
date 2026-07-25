import { createContext, useContext, useEffect, useState, type ReactNode } from "react"
import { useAllWarehouses } from "@/features/warehouses/hooks"
import type { WarehouseItem } from "@/features/warehouses/types"

interface WarehouseFilterContextValue {
  warehouseId: number | null
  setWarehouseId: (id: number) => void
  warehouses: WarehouseItem[]
  isLoading: boolean
}

const WarehouseFilterContext = createContext<WarehouseFilterContextValue | null>(null)

export function WarehouseFilterProvider({ children }: { children: ReactNode }) {
  const { data, isLoading } = useAllWarehouses()
  const warehouses = data ?? []

  const [warehouseId, setWarehouseId] = useState<number | null>(null)

  useEffect(() => {
    if (warehouseId !== null) return
    if (data && data.length > 0) setWarehouseId(data[0].id)
  }, [data, warehouseId])

  return (
    <WarehouseFilterContext.Provider value={{ warehouseId, setWarehouseId, warehouses, isLoading }}>
      {children}
    </WarehouseFilterContext.Provider>
  )
}

export function useWarehouseFilter() {
  const context = useContext(WarehouseFilterContext)
  if (!context) {
    throw new Error("useWarehouseFilter must be used within WarehouseFilterProvider")
  }
  return context
}
