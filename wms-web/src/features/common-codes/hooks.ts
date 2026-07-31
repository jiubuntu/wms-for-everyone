import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import {
  createCommonCode,
  deleteCommonCode,
  getAllCommonCodes,
  getCommonCodes,
  updateCommonCode,
} from "@/features/common-codes/api"
import type {
  CommonCodeCreateInput,
  CommonCodeGroup,
  CommonCodeScope,
  CommonCodeUpdateInput,
} from "@/features/common-codes/types"

export function useCommonCodes(
  scope: CommonCodeScope,
  groupCode: CommonCodeGroup,
  keyword: string,
  page: number,
  limit = 10
) {
  return useQuery({
    queryKey: ["common-codes", scope, groupCode, keyword, page, limit],
    queryFn: () => getCommonCodes(scope, groupCode, keyword, page, limit),
  })
}

export function useAllCommonCodes(scope: CommonCodeScope, groupCode: CommonCodeGroup) {
  return useQuery({
    queryKey: ["common-codes", scope, groupCode, "all"],
    queryFn: () => getAllCommonCodes(scope, groupCode),
  })
}

export function useCreateCommonCode(scope: CommonCodeScope) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (input: CommonCodeCreateInput) => createCommonCode(scope, input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["common-codes", scope] })
    },
  })
}

export function useUpdateCommonCode(scope: CommonCodeScope) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, input }: { id: number; input: CommonCodeUpdateInput }) =>
      updateCommonCode(scope, id, input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["common-codes", scope] })
    },
  })
}

export function useDeleteCommonCode(scope: CommonCodeScope) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => deleteCommonCode(scope, id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["common-codes", scope] })
    },
  })
}
