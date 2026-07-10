import { useState } from "react"
import { Link } from "react-router-dom"
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
    <div
      className="relative flex min-h-svh items-center justify-center overflow-hidden bg-cover bg-center p-6"
      style={{ backgroundImage: `url(${loginBg})` }}
    >
      <div className="absolute inset-0 bg-black/50" />

      <form
        onSubmit={handleSubmit}
        className="relative z-10 flex w-full max-w-xs flex-col gap-3 border bg-background p-8 shadow-lg"
      >
        <h1 className="mb-1 text-center text-sm font-extrabold">모두의 WMS</h1>

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
    </div>
  )
}
