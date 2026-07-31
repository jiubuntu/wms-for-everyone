import { createContext, useContext, useEffect, useState, type ReactNode } from "react"
import { useAllWarehouses } from "@/features/warehouses/hooks"
import type { WarehouseItem } from "@/features/warehouses/types"
import { useAuth } from "@/contexts/AuthContext"

interface WarehouseFilterContextValue {
  warehouseId: number | null
  setWarehouseId: (id: number) => void
  warehouses: WarehouseItem[]
  isLoading: boolean
  /** 창고관리자·작업자는 본인 소속 창고로 고정되어 선택을 바꿀 수 없다 */
  isLocked: boolean
}

const WarehouseFilterContext = createContext<WarehouseFilterContextValue | null>(null)

export function WarehouseFilterProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth()
  const { data, isLoading } = useAllWarehouses()
  const warehouses = data ?? []

  const isLocked = user?.role === "WAREHOUSE_MANAGER" || user?.role === "WORKER"

  const [selectedWarehouseId, setSelectedWarehouseId] = useState<number | null>(null)

  useEffect(() => {
    if (isLocked) return
    if (selectedWarehouseId !== null) return
    if (data && data.length > 0) setSelectedWarehouseId(data[0].id)
  }, [data, selectedWarehouseId, isLocked])

  function setWarehouseId(id: number) {
    if (isLocked) return
    setSelectedWarehouseId(id)
  }

  const warehouseId = isLocked ? (user?.warehouseId ?? null) : selectedWarehouseId

  return (
    <WarehouseFilterContext.Provider
      value={{ warehouseId, setWarehouseId, warehouses, isLoading, isLocked }}
    >
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
