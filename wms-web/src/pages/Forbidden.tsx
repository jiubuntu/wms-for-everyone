import { Link } from "react-router-dom"
import { Button } from "@/components/ui/button"

export function Forbidden() {
  return (
    <div className="flex min-h-svh flex-col items-center justify-center gap-3 p-6 text-center">
      <p className="text-sm font-extrabold tracking-tight">모두의 WMS</p>
      <h1 className="text-2xl font-bold">접근 권한이 없습니다</h1>
      <p className="text-muted-foreground">이 화면에 접근할 수 있는 권한이 없습니다.</p>
      <Button asChild className="mt-2">
        <Link to="/">홈으로</Link>
      </Button>
    </div>
  )
}
