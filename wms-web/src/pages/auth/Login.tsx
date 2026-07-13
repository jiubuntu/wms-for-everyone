import { useState } from "react"
import { Link } from "react-router-dom"
import { AuthLayout } from "@/components/common/AuthLayout"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import loginBg from "@/assets/login.png"

export function Login() {
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    // TODO: 백엔드 로그인 API 연동 (5단계 백엔드 개발 시점)
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

        <Button type="submit" className="mt-1">
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
