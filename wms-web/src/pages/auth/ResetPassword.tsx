import { useState } from "react"
import { isAxiosError } from "axios"
import { Link, useSearchParams } from "react-router-dom"
import { confirmPasswordReset, requestPasswordReset } from "@/features/auth/api"
import { AuthLayout } from "@/components/common/AuthLayout"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import loginBg from "@/assets/login.png"

const PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/

export function ResetPassword() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get("token")

  return token ? <ConfirmStep token={token} /> : <RequestStep />
}

function RequestStep() {
  const [email, setEmail] = useState("")
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isSent, setIsSent] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setIsSubmitting(true)

    try {
      await requestPasswordReset(email)
    } catch {
      // 계정 존재 여부를 노출하지 않기 위해 실패해도 동일한 안내로 처리한다
    } finally {
      setIsSubmitting(false)
      setIsSent(true)
    }
  }

  if (isSent) {
    return (
      <AuthLayout title="비밀번호 찾기" backgroundImage={loginBg}>
        <p className="text-center text-sm text-muted-foreground">
          입력하신 이메일로 재설정 링크를 보내드렸습니다. 메일함을 확인해 주세요.
        </p>
      </AuthLayout>
    )
  }

  return (
    <AuthLayout
      title="비밀번호 찾기"
      description="가입한 이메일로 재설정 링크를 보내드립니다."
      backgroundImage={loginBg}
    >
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

        <Button type="submit" className="mt-1" disabled={isSubmitting}>
          재설정 링크 보내기
        </Button>

        <div className="mt-1 text-center text-xs text-muted-foreground">
          <Link to="/login" className="hover:text-foreground">
            로그인으로 돌아가기
          </Link>
        </div>
      </form>
    </AuthLayout>
  )
}

function ConfirmStep({ token }: { token: string }) {
  const [newPassword, setNewPassword] = useState("")
  const [newPasswordConfirm, setNewPasswordConfirm] = useState("")
  const [error, setError] = useState("")
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDone, setIsDone] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError("")

    if (!PASSWORD_PATTERN.test(newPassword)) {
      setError("비밀번호는 영문, 숫자, 특수문자를 모두 포함해 8자 이상이어야 합니다.")
      return
    }
    if (newPassword !== newPasswordConfirm) {
      setError("비밀번호가 일치하지 않습니다.")
      return
    }

    setIsSubmitting(true)
    try {
      await confirmPasswordReset({ token, newPassword, newPasswordConfirm })
      setIsDone(true)
    } catch (err) {
      const message = isAxiosError(err) ? err.response?.data?.message : null
      setError(message ?? "비밀번호 재설정에 실패했습니다.")
    } finally {
      setIsSubmitting(false)
    }
  }

  if (isDone) {
    return (
      <AuthLayout title="새 비밀번호 설정" backgroundImage={loginBg}>
        <div className="flex flex-col items-center gap-3">
          <p className="text-center text-sm text-muted-foreground">
            비밀번호가 변경되었습니다. 다시 로그인해 주세요.
          </p>
          <Button asChild className="w-full">
            <Link to="/login">로그인하러 가기</Link>
          </Button>
        </div>
      </AuthLayout>
    )
  }

  return (
    <AuthLayout
      title="새 비밀번호 설정"
      description="영문, 숫자, 특수문자를 포함해 8자 이상 입력해 주세요."
      backgroundImage={loginBg}
    >
      <form onSubmit={handleSubmit} className="flex flex-col gap-3">
        <div className="flex flex-col gap-1.5">
          <label htmlFor="newPassword" className="text-xs text-muted-foreground">
            새 비밀번호
          </label>
          <Input
            id="newPassword"
            type="password"
            required
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
          />
        </div>

        <div className="flex flex-col gap-1.5">
          <label htmlFor="newPasswordConfirm" className="text-xs text-muted-foreground">
            새 비밀번호 확인
          </label>
          <Input
            id="newPasswordConfirm"
            type="password"
            required
            value={newPasswordConfirm}
            onChange={(e) => setNewPasswordConfirm(e.target.value)}
          />
        </div>

        {error && <p className="text-xs text-destructive">{error}</p>}

        <Button type="submit" className="mt-1" disabled={isSubmitting}>
          비밀번호 변경
        </Button>
      </form>
    </AuthLayout>
  )
}
