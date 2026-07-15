import { useState } from "react"
import { isAxiosError } from "axios"
import { Link, useNavigate } from "react-router-dom"
import { useAuth } from "@/contexts/AuthContext"
import { AuthLayout } from "@/components/common/AuthLayout"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import loginBg from "@/assets/login.png"

export function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()

  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState("")
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError("")
    setIsSubmitting(true)

    try {
      const user = await login(email, password)
      navigate(user.role === "SYSTEM_ADMIN" ? "/admin" : "/")
    } catch (err) {
      const message = isAxiosError(err) ? err.response?.data?.message : null
      setError(message ?? "로그인에 실패했습니다.")
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <AuthLayout title="로그인" backgroundImage={loginBg}>
      <form onSubmit={handleSubmit} className="flex flex-col gap-3">
        <div className="flex flex-col gap-1.5">
          <label htmlFor="email" className="text-xs text-muted-foreground">
            이메일
          </label>
          <Input
            id="email"
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>

        <div className="flex flex-col gap-1.5">
          <label htmlFor="password" className="text-xs text-muted-foreground">
            비밀번호
          </label>
          <Input
            id="password"
            type="password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>

        {error && <p className="text-xs text-destructive">{error}</p>}

        <Button type="submit" className="mt-1" disabled={isSubmitting}>
          로그인
        </Button>

        <div className="mt-1 flex justify-between text-xs text-muted-foreground">
          <Link to="/reset-password" className="hover:text-foreground">
            비밀번호 찾기
          </Link>
          <Link to="/signup" className="hover:text-foreground">
            회원가입
          </Link>
        </div>
      </form>
    </AuthLayout>
  )
}
