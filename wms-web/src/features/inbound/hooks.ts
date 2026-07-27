import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import {
  cancelInbound,
  completeInbound,
  createInbound,
  getInbound,
  getInbounds,
  replaceInboundItemLocations,
} from "@/features/inbound/api"
import type { InboundCreateInput, InboundLocationInput } from "@/features/inbound/types"

export function useInbounds(warehouseId: number | null, page: number, limit = 10) {
  return useQuery({
    queryKey: ["inbounds", warehouseId, page, limit],
    queryFn: () => getInbounds(warehouseId as number, page, limit),
    enabled: warehouseId != null,
  })
}

export function useInbound(warehouseId: number | null, id: number) {
  return useQuery({
    queryKey: ["inbounds", warehouseId, id],
    queryFn: () => getInbound(warehouseId as number, id),
    enabled: warehouseId != null && Number.isFinite(id),
  })
}

export function useCreateInbound(warehouseId: number | null) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({
      input,
      idempotencyKey,
    }: {
      input: InboundCreateInput
      idempotencyKey: string
    }) => createInbound(warehouseId as number, input, idempotencyKey),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["inbounds", warehouseId] })
    },
  })
}

export function useReplaceInboundItemLocations(warehouseId: number | null, inboundId: number) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ itemId, locations }: { itemId: number; locations: InboundLocationInput[] }) =>
      replaceInboundItemLocations(warehouseId as number, inboundId, itemId, locations),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["inbounds", warehouseId] })
      queryClient.invalidateQueries({ queryKey: ["inbounds", warehouseId, inboundId] })
    },
  })
}

export function useCompleteInbound(warehouseId: number | null) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, idempotencyKey }: { id: number; idempotencyKey: string }) =>
      completeInbound(warehouseId as number, id, idempotencyKey),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ["inbounds", warehouseId] })
      queryClient.invalidateQueries({ queryKey: ["inbounds", warehouseId, variables.id] })
      queryClient.invalidateQueries({ queryKey: ["inventory"] })
    },
  })
}

export function useCancelInbound(warehouseId: number | null) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, idempotencyKey }: { id: number; idempotencyKey: string }) =>
      cancelInbound(warehouseId as number, id, idempotencyKey),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ["inbounds", warehouseId] })
      queryClient.invalidateQueries({ queryKey: ["inbounds", warehouseId, variables.id] })
    },
  })
}
