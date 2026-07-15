import type { ReactNode } from "react"
import { Navigate, useLocation } from "react-router-dom"
import { useAuth } from "@/contexts/AuthContext"

export function GuestRoute({ children }: { children: ReactNode }) {
  const { user, isLoading } = useAuth()
  const location = useLocation()

  if (isLoading) {
    return null
  }

  if (user) {
    const home = user.role === "SYSTEM_ADMIN" ? "/admin" : "/"
    if (location.pathname !== home) {
      return <Navigate to={home} replace />
    }
  }

  return <>{children}</>
}
