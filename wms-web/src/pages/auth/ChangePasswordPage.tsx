import { useState } from "react"
import { isAxiosError } from "axios"
import { useNavigate } from "react-router-dom"
import { useAuth } from "@/contexts/AuthContext"
import { changePassword } from "@/features/users/api"
import { AuthLayout } from "@/components/common/AuthLayout"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import loginBg from "@/assets/login.png"

const PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/

export function ChangePasswordPage() {
  const { user, clearMustChangePassword } = useAuth()
  const navigate = useNavigate()

  const [currentPassword, setCurrentPassword] = useState("")
  const [newPassword, setNewPassword] = useState("")
  const [newPasswordConfirm, setNewPasswordConfirm] = useState("")
  const [error, setError] = useState("")
  const [isSubmitting, setIsSubmitting] = useState(false)

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
      await changePassword(currentPassword, newPassword)
      clearMustChangePassword()
      navigate(user?.role === "SYSTEM_ADMIN" ? "/admin" : "/app", { replace: true })
    } catch (err) {
      const message = isAxiosError(err) ? err.response?.data?.message : null
      setError(message ?? "비밀번호 변경에 실패했습니다.")
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <AuthLayout
      title="비밀번호 변경"
      description="발급받은 임시 비밀번호로는 계속 이용할 수 없습니다. 새 비밀번호를 설정해주세요."
      backgroundImage={loginBg}
    >
      <form onSubmit={handleSubmit} className="flex flex-col gap-3">
        <div className="flex flex-col gap-1.5">
          <label htmlFor="currentPassword" className="text-xs text-muted-foreground">
            임시 비밀번호
          </label>
          <Input
            id="currentPassword"
            type="password"
            required
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
          />
        </div>

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
