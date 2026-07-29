import { useEffect, useState } from "react"
import { isAxiosError } from "axios"
import { Copy } from "lucide-react"
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { useAuth } from "@/contexts/AuthContext"
import { useAllWarehouses } from "@/features/warehouses/hooks"
import { useCreateUser } from "@/features/users/hooks"
import type { IssuableUserRole, UserIssueResult } from "@/features/users/types"

interface UserIssueDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
}

const ROLE_OPTIONS: { value: IssuableUserRole; label: string }[] = [
  { value: "WAREHOUSE_MANAGER", label: "창고관리자" },
  { value: "WORKER", label: "작업자" },
]

export function UserIssueDialog({ open, onOpenChange }: UserIssueDialogProps) {
  const { user } = useAuth()
  const roleOptions =
    user?.role === "COMPANY_ADMIN" ? ROLE_OPTIONS : ROLE_OPTIONS.filter((o) => o.value === "WORKER")

  const [step, setStep] = useState<"form" | "result">("form")
  const [email, setEmail] = useState("")
  const [name, setName] = useState("")
  const [phone, setPhone] = useState("")
  const [role, setRole] = useState<IssuableUserRole | "">("")
  const [warehouseId, setWarehouseId] = useState("")
  const [error, setError] = useState("")
  const [result, setResult] = useState<UserIssueResult | null>(null)
  const [isCopied, setIsCopied] = useState(false)

  const { data: warehouses } = useAllWarehouses()
  const createMutation = useCreateUser()

  useEffect(() => {
    if (!open) return
    setStep("form")
    setEmail("")
    setName("")
    setPhone("")
    setRole(user?.role === "COMPANY_ADMIN" ? "" : "WORKER")
    setWarehouseId("")
    setError("")
    setResult(null)
    setIsCopied(false)
  }, [open, user?.role])

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError("")
    if (!role) return

    try {
      const issued = await createMutation.mutateAsync({
        email,
        name,
        phone,
        role,
        warehouseId: Number(warehouseId),
      })
      setResult(issued)
      setStep("result")
    } catch (err) {
      const message = isAxiosError(err) ? err.response?.data?.message : null
      setError(message ?? "계정을 발급하지 못했습니다.")
    }
  }

  async function handleCopy() {
    if (!result) return
    await navigator.clipboard.writeText(result.temporaryPassword)
    setIsCopied(true)
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg">
        {step === "form" ? (
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <DialogHeader>
              <DialogTitle>직원 계정 발급</DialogTitle>
            </DialogHeader>

            <div className="flex flex-col gap-3">
              <div className="flex flex-col gap-1.5">
                <label htmlFor="user-email" className="text-sm font-medium">
                  이메일
                </label>
                <Input
                  id="user-email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>

              <div className="flex flex-col gap-1.5">
                <label htmlFor="user-name" className="text-sm font-medium">
                  이름
                </label>
                <Input id="user-name" value={name} onChange={(e) => setName(e.target.value)} required />
              </div>

              <div className="flex flex-col gap-1.5">
                <label htmlFor="user-phone" className="text-sm font-medium">
                  연락처
                </label>
                <Input
                  id="user-phone"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  required
                />
              </div>

              <div className="flex flex-col gap-1.5">
                <label className="text-sm font-medium">역할</label>
                <Select value={role} onValueChange={(v) => setRole(v as IssuableUserRole)} required>
                  <SelectTrigger className="w-full">
                    <SelectValue placeholder="역할 선택" />
                  </SelectTrigger>
                  <SelectContent>
                    {roleOptions.map((o) => (
                      <SelectItem key={o.value} value={o.value}>
                        {o.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="flex flex-col gap-1.5">
                <label className="text-sm font-medium">소속 창고</label>
                <Select value={warehouseId} onValueChange={setWarehouseId} required>
                  <SelectTrigger className="w-full">
                    <SelectValue placeholder="창고 선택" />
                  </SelectTrigger>
                  <SelectContent>
                    {warehouses?.map((w) => (
                      <SelectItem key={w.id} value={String(w.id)}>
                        {w.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              {error && <p className="text-sm text-destructive">{error}</p>}
            </div>

            <DialogFooter>
              <Button type="submit" disabled={createMutation.isPending}>
                {createMutation.isPending ? "발급 중..." : "발급"}
              </Button>
            </DialogFooter>
          </form>
        ) : (
          result && (
            <div className="flex flex-col gap-4">
              <DialogHeader>
                <DialogTitle>발급 완료</DialogTitle>
              </DialogHeader>

              <div className="flex flex-col gap-3">
                <div className="flex flex-col gap-1.5">
                  <span className="text-sm text-muted-foreground">이메일</span>
                  <span className="text-sm font-medium">{result.email}</span>
                </div>

                <div className="flex flex-col gap-1.5">
                  <span className="text-sm text-muted-foreground">임시 비밀번호</span>
                  <div className="flex items-center gap-2">
                    <Input value={result.temporaryPassword} readOnly className="font-mono" />
                    <Button
                      type="button"
                      variant="outline"
                      size="icon"
                      onClick={handleCopy}
                      aria-label="비밀번호 복사"
                    >
                      <Copy className="size-4" />
                    </Button>
                  </div>
                  {isCopied && <span className="text-xs text-success">복사했습니다.</span>}
                </div>

                <p className="text-xs text-warning">
                  이 비밀번호는 지금만 확인할 수 있고 다시 표시되지 않습니다. 잊지 말고 전달해주세요.
                </p>
              </div>

              <DialogFooter>
                <Button type="button" onClick={() => onOpenChange(false)}>
                  확인
                </Button>
              </DialogFooter>
            </div>
          )
        )}
      </DialogContent>
    </Dialog>
  )
}
