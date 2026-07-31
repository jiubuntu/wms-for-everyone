import { useState } from "react"
import { Link } from "react-router-dom"
import { AuthLayout } from "@/components/common/AuthLayout"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import loginBg from "@/assets/login.png"

const MAX_FILE_SIZE = 10 * 1024 * 1024 // 10MB
const ALLOWED_FILE_TYPES = ["application/pdf", "image/jpeg", "image/png"]

const TERMS_TEXT = `제1조 (목적)
이 약관은 모두의 WMS(이하 "회사")가 제공하는 창고관리 서비스의 이용 조건 및 절차, 회사와 회원의 권리·의무 및 책임사항을 규정함을 목적으로 합니다.

제2조 (회원가입)
회원가입은 이용자가 이 약관에 동의하고 회사가 정한 절차에 따라 가입 신청을 완료함으로써 성립합니다. 기업관리자 계정은 시스템관리자의 승인을 거쳐 활성화됩니다.

※ 본 내용은 초안이며, 서비스 공개 전 법무 검토를 거쳐 확정됩니다.`

const PRIVACY_TEXT = `1. 수집 항목: 이메일, 비밀번호, 이름, 연락처, 기업명, 사업자등록번호, 사업자등록증
2. 수집 목적: 회원 식별 및 인증, 기업 가입 심사, 서비스 제공
3. 보유 기간: 회원 탈퇴 시까지 (관련 법령에 따른 보존 의무가 있는 경우 해당 기간 동안 보관)
4. 동의를 거부할 권리가 있으며, 거부 시 회원가입이 제한됩니다.

※ 본 내용은 초안이며, 서비스 공개 전 법무 검토를 거쳐 확정됩니다.`

const MARKETING_TEXT = `이벤트, 서비스 업데이트 등 마케팅 정보를 이메일로 수신하는 데 동의합니다. 동의하지 않아도 서비스 이용에는 제한이 없습니다.`

export function Signup() {
  const [email, setEmail] = useState("")
  const [name, setName] = useState("")
  const [password, setPassword] = useState("")
  const [passwordConfirm, setPasswordConfirm] = useState("")
  const [phone, setPhone] = useState("")
  const [companyName, setCompanyName] = useState("")
  const [businessNumber, setBusinessNumber] = useState("")
  const [businessLicenseFile, setBusinessLicenseFile] = useState<File | null>(null)
  const [fileError, setFileError] = useState("")
  const [error, setError] = useState("")

  const [agreeTerms, setAgreeTerms] = useState(false)
  const [agreePrivacy, setAgreePrivacy] = useState(false)
  const [agreeMarketing, setAgreeMarketing] = useState(false)

  const passwordMismatch = passwordConfirm.length > 0 && password !== passwordConfirm

  function handleAgreeAll(checked: boolean) {
    setAgreeTerms(checked)
    setAgreePrivacy(checked)
    setAgreeMarketing(checked)
  }

  function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0] ?? null
    if (!file) {
      setBusinessLicenseFile(null)
      setFileError("")
      return
    }
    if (!ALLOWED_FILE_TYPES.includes(file.type)) {
      setBusinessLicenseFile(null)
      setFileError("PDF, JPG, PNG 파일만 업로드할 수 있습니다.")
      return
    }
    if (file.size >= MAX_FILE_SIZE) {
      setBusinessLicenseFile(null)
      setFileError("파일 용량은 10MB 미만이어야 합니다.")
      return
    }
    setBusinessLicenseFile(file)
    setFileError("")
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError("")

    if (password !== passwordConfirm) {
      setError("비밀번호가 일치하지 않습니다.")
      return
    }
    if (!businessLicenseFile) {
      setError("사업자등록증을 첨부해주세요.")
      return
    }
    if (!agreeTerms || !agreePrivacy) {
      setError("필수 약관에 동의해주세요.")
      return
    }

    // TODO: 백엔드 회원가입 API 연동 (5단계 백엔드 개발 시점)
  }

  return (
    <AuthLayout title="회원가입" className="max-w-lg" backgroundImage={loginBg}>
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        {/*<p className="rounded bg-muted/50 p-3 text-xs text-muted-foreground">*/}
        {/*  회원가입 시 기업관리자로 등록됩니다. 가입 신청 후 시스템관리자 승인이 완료되어야 로그인할 수 있습니다.*/}
        {/*</p>*/}
        <div className="grid grid-cols-2 gap-3">
          <Field label="이메일" htmlFor="email">
            <Input id="email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
          </Field>
          <Field label="이름" htmlFor="name">
            <Input id="name" required value={name} onChange={(e) => setName(e.target.value)} />
          </Field>
          <Field label="비밀번호" htmlFor="password">
            <Input
              id="password"
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </Field>
          <Field label="비밀번호 확인" htmlFor="passwordConfirm">
            <Input
              id="passwordConfirm"
              type="password"
              required
              value={passwordConfirm}
              onChange={(e) => setPasswordConfirm(e.target.value)}
              aria-invalid={passwordMismatch}
            />
          </Field>
          <Field label="연락처" htmlFor="phone">
            <Input id="phone" required value={phone} onChange={(e) => setPhone(e.target.value)} />
          </Field>
          <Field label="기업명" htmlFor="companyName">
            <Input id="companyName" required value={companyName} onChange={(e) => setCompanyName(e.target.value)} />
          </Field>
          <div className="col-span-2">
            <Field label="사업자등록번호" htmlFor="businessNumber">
              <Input
                id="businessNumber"
                required
                value={businessNumber}
                onChange={(e) => setBusinessNumber(e.target.value)}
              />
            </Field>
          </div>
        </div>

        {passwordMismatch && (
          <p className="-mt-2 text-xs text-destructive">비밀번호가 일치하지 않습니다.</p>
        )}

        <div className="flex flex-col gap-1.5">
          <label htmlFor="businessLicense" className="text-xs text-muted-foreground">
            사업자등록증 (필수)
          </label>
          <label
            htmlFor="businessLicense"
            className="flex cursor-pointer flex-col items-center gap-1 border border-dashed p-5 text-center text-xs text-muted-foreground hover:bg-muted/50"
          >
            <span className="font-medium text-foreground">
              {businessLicenseFile ? businessLicenseFile.name : "파일을 클릭하여 업로드"}
            </span>
            <span>PDF · JPG · PNG, 10MB 미만</span>
          </label>
          <input
            id="businessLicense"
            type="file"
            required
            accept=".pdf,.jpg,.jpeg,.png"
            className="sr-only"
            onChange={handleFileChange}
          />
          {fileError && <p className="text-xs text-destructive">{fileError}</p>}
        </div>

        <div className="flex flex-col gap-2.5 border-t pt-4">
          <label className="flex items-center gap-2 text-xs font-semibold">
            <input
              type="checkbox"
              checked={agreeTerms && agreePrivacy && agreeMarketing}
              onChange={(e) => handleAgreeAll(e.target.checked)}
              className="size-3.5"
            />
            전체 동의
          </label>

          <AgreementItem
            required
            label="이용약관 동의"
            checked={agreeTerms}
            onChange={setAgreeTerms}
            detail={TERMS_TEXT}
          />
          <AgreementItem
            required
            label="개인정보 수집·이용 동의"
            checked={agreePrivacy}
            onChange={setAgreePrivacy}
            detail={PRIVACY_TEXT}
          />
          <AgreementItem
            label="마케팅 정보 수신 동의"
            checked={agreeMarketing}
            onChange={setAgreeMarketing}
            detail={MARKETING_TEXT}
          />
        </div>

        {error && <p className="text-xs text-destructive">{error}</p>}

        <Button type="submit" className="mt-1">
          가입 신청
        </Button>

        <p className="text-center text-xs text-muted-foreground">
          이미 계정이 있으신가요?{" "}
          <Link to="/login" className="text-foreground hover:underline">
            로그인
          </Link>
        </p>
      </form>
    </AuthLayout>
  )
}

function Field({
  label,
  htmlFor,
  children,
}: {
  label: string
  htmlFor: string
  children: React.ReactNode
}) {
  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={htmlFor} className="text-xs text-muted-foreground">
        {label}
      </label>
      {children}
    </div>
  )
}

function AgreementItem({
  label,
  required,
  checked,
  onChange,
  detail,
}: {
  label: string
  required?: boolean
  checked: boolean
  onChange: (checked: boolean) => void
  detail: string
}) {
  return (
    <div className="text-xs">
      <label className="flex items-center gap-2">
        <input
          type="checkbox"
          checked={checked}
          onChange={(e) => onChange(e.target.checked)}
          className="size-3.5"
        />
        <span>
          <span className={required ? "font-medium" : "text-muted-foreground"}>
            {required ? "[필수]" : "[선택]"}
          </span>{" "}
          {label}
        </span>
      </label>
      <div className="mt-1.5 ml-5.5 max-h-24 overflow-y-auto rounded border bg-muted/30 p-3 text-[11px] leading-relaxed whitespace-pre-line text-muted-foreground">
        {detail}
      </div>
    </div>
  )
}
