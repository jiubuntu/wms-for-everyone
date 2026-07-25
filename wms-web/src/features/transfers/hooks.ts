import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { createTransfer, getTransfers } from "@/features/transfers/api"
import type { TransferCreateInput } from "@/features/transfers/types"

export function useTransfers(warehouseId: number | null, page: number, limit = 10) {
  return useQuery({
    queryKey: ["transfers", warehouseId, page, limit],
    queryFn: () => getTransfers(warehouseId as number, page, limit),
    enabled: warehouseId != null,
  })
}

export function useCreateTransfer(warehouseId: number | null) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({
      input,
      idempotencyKey,
    }: {
      input: TransferCreateInput
      idempotencyKey: string
    }) => createTransfer(warehouseId as number, input, idempotencyKey),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["transfers", warehouseId] })
      queryClient.invalidateQueries({ queryKey: ["inventory"] })
    },
  })
}
