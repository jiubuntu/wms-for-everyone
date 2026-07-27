import { useEffect, useRef, useState } from "react"
import type { InventoryItem } from "@/features/inventory/types"

const FLASH_DURATION_MS = 60_000

interface RowSnapshot {
  quantity: number
  reservedQuantity: number
}

export function useFlashingRows(items: InventoryItem[], resetKey: string) {
  const [flashingIds, setFlashingIds] = useState<Set<number>>(new Set())
  const prevItemsRef = useRef<Map<number, RowSnapshot> | null>(null)
  const prevResetKeyRef = useRef(resetKey)
  const timersRef = useRef<Map<number, ReturnType<typeof setTimeout>>>(new Map())

  useEffect(() => {
    const currentMap = new Map<number, RowSnapshot>(
      items.map((item) => [item.id, { quantity: item.quantity, reservedQuantity: item.reservedQuantity }])
    )

    const keyChanged = prevResetKeyRef.current !== resetKey
    prevResetKeyRef.current = resetKey

    if (!keyChanged && prevItemsRef.current) {
      const prevMap = prevItemsRef.current

      for (const [id, snapshot] of currentMap) {
        const prev = prevMap.get(id)
        if (!prev) continue
        if (prev.quantity === snapshot.quantity && prev.reservedQuantity === snapshot.reservedQuantity) continue

        const existingTimer = timersRef.current.get(id)
        if (existingTimer) clearTimeout(existingTimer)

        setFlashingIds((prevSet) => {
          if (prevSet.has(id)) return prevSet
          return new Set(prevSet).add(id)
        })

        const timer = setTimeout(() => {
          timersRef.current.delete(id)
          setFlashingIds((prevSet) => {
            if (!prevSet.has(id)) return prevSet
            const next = new Set(prevSet)
            next.delete(id)
            return next
          })
        }, FLASH_DURATION_MS)
        timersRef.current.set(id, timer)
      }
    }

    prevItemsRef.current = currentMap
  }, [items, resetKey])

  useEffect(() => {
    const timers = timersRef.current
    return () => {
      timers.forEach((timer) => clearTimeout(timer))
      timers.clear()
    }
  }, [])

  return flashingIds
}
