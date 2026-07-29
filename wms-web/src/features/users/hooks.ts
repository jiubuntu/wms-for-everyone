import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { createUser, getUsers, withdrawUser } from "@/features/users/api"
import type { UserCreateInput } from "@/features/users/types"

export function useUsers(page: number, limit = 10) {
  return useQuery({
    queryKey: ["users", page, limit],
    queryFn: () => getUsers(page, limit),
  })
}

export function useCreateUser() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (input: UserCreateInput) => createUser(input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] })
    },
  })
}

export function useWithdrawUser() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => withdrawUser(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] })
    },
  })
}
