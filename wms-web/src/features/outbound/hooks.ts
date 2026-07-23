import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import {
  cancelOutbound,
  completeOutbound,
  createOutbound,
  getOutbound,
  getOutbounds,
} from "@/features/outbound/api"
import type { OutboundCreateInput } from "@/features/outbound/types"

export function useOutbounds(warehouseId: number | null, page: number, limit = 10) {
  return useQuery({
    queryKey: ["outbounds", warehouseId, page, limit],
    queryFn: () => getOutbounds(warehouseId as number, page, limit),
    enabled: warehouseId != null,
  })
}

export function useOutbound(warehouseId: number | null, id: number) {
  return useQuery({
    queryKey: ["outbounds", warehouseId, id],
    queryFn: () => getOutbound(warehouseId as number, id),
    enabled: warehouseId != null && Number.isFinite(id),
  })
}

export function useCreateOutbound(warehouseId: number | null) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({
      input,
      idempotencyKey,
    }: {
      input: OutboundCreateInput
      idempotencyKey: string
    }) => createOutbound(warehouseId as number, input, idempotencyKey),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["outbounds", warehouseId] })
      queryClient.invalidateQueries({ queryKey: ["inventory"] })
    },
  })
}

export function useCompleteOutbound(warehouseId: number | null) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, idempotencyKey }: { id: number; idempotencyKey: string }) =>
      completeOutbound(warehouseId as number, id, idempotencyKey),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ["outbounds", warehouseId] })
      queryClient.invalidateQueries({ queryKey: ["outbounds", warehouseId, variables.id] })
      queryClient.invalidateQueries({ queryKey: ["inventory"] })
    },
  })
}

export function useCancelOutbound(warehouseId: number | null) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, idempotencyKey }: { id: number; idempotencyKey: string }) =>
      cancelOutbound(warehouseId as number, id, idempotencyKey),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ["outbounds", warehouseId] })
      queryClient.invalidateQueries({ queryKey: ["outbounds", warehouseId, variables.id] })
      queryClient.invalidateQueries({ queryKey: ["inventory"] })
    },
  })
}
