import { createContext, useContext, useEffect, useState, type ReactNode } from "react"
import { login as loginApi, logout as logoutApi, refresh as refreshApi } from "@/features/auth/api"
import type { AuthUser, UserRole } from "@/features/auth/types"
import { setAccessToken, setUnauthorizedHandler } from "@/lib/axios"

interface AccessTokenPayload {
  sub: string
  companyId: number | null
  warehouseId: number | null
  role: UserRole
}

function decodeAccessToken(token: string): AccessTokenPayload {
  const payloadSegment = token.split(".")[1]
  const base64 = payloadSegment.replace(/-/g, "+").replace(/_/g, "/")
  return JSON.parse(atob(base64)) as AccessTokenPayload
}

interface AuthContextValue {
  user: AuthUser | null
  isLoading: boolean
  login: (email: string, password: string) => Promise<AuthUser>
  logout: () => Promise<void>
  clearMustChangePassword: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    setUnauthorizedHandler(() => setUser(null))

    refreshApi()
      .then(({ accessToken }) => {
        setAccessToken(accessToken)
        const payload = decodeAccessToken(accessToken)
        // mustChangePassword는 JWT claim에 없어 새로고침 복원 시엔 항상 false로 취급한다 (알려진 제약, docs/request/user.md 참고)
        setUser({
          userId: Number(payload.sub),
          companyId: payload.companyId,
          warehouseId: payload.warehouseId,
          role: payload.role,
          mustChangePassword: false,
        })
      })
      .catch(() => {
        setAccessToken(null)
        setUser(null)
      })
      .finally(() => setIsLoading(false))
  }, [])

  async function login(email: string, password: string) {
    const result = await loginApi(email, password)
    setAccessToken(result.accessToken)
    const payload = decodeAccessToken(result.accessToken)
    const loggedInUser: AuthUser = {
      userId: result.userId,
      companyId: payload.companyId,
      warehouseId: payload.warehouseId,
      role: result.role,
      email: result.email,
      name: result.name,
      mustChangePassword: result.mustChangePassword,
    }
    setUser(loggedInUser)
    return loggedInUser
  }

  async function logout() {
    await logoutApi().catch(() => {})
    setAccessToken(null)
    setUser(null)
  }

  function clearMustChangePassword() {
    setUser((prev) => (prev ? { ...prev, mustChangePassword: false } : prev))
  }

  return (
    <AuthContext.Provider value={{ user, isLoading, login, logout, clearMustChangePassword }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider")
  }
  return context
}
