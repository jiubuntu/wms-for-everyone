import { useNavigate } from "react-router-dom"
import { Bell, ChevronDown, LogOut, Menu, User } from "lucide-react"
import { useAuth } from "@/contexts/AuthContext"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"

const MOCK_NOTIFICATIONS: { id: number; text: string }[] = []

export function BackofficeHeader({ onToggleSidebar }: { onToggleSidebar: () => void }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    navigate("/login")
  }

  const initial = (user?.name ?? user?.email)?.slice(0, 1).toUpperCase()

  return (
    <header className="sticky top-0 z-10 flex h-[70px] shrink-0 items-center justify-between bg-card px-4 shadow-[0_0_15px_0_rgba(154,161,171,0.05)]">
      <Button variant="ghost" size="icon" onClick={onToggleSidebar} aria-label="사이드바 접기/펼치기">
        <Menu className="size-4.5" />
      </Button>

      <div className="flex items-center gap-3">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon" className="relative" aria-label="알림">
              <Bell className="size-4.5" />
              {MOCK_NOTIFICATIONS.length > 0 && (
                <span className="absolute top-1.5 right-1.5 size-1.5 rounded-full bg-destructive" />
              )}
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-72">
            <DropdownMenuLabel>알림</DropdownMenuLabel>
            <DropdownMenuSeparator />
            {MOCK_NOTIFICATIONS.length === 0 ? (
              <p className="px-2 py-3 text-center text-sm text-muted-foreground">새로운 알림이 없습니다.</p>
            ) : (
              MOCK_NOTIFICATIONS.map((notification) => (
                <DropdownMenuItem key={notification.id} className="whitespace-normal">
                  {notification.text}
                </DropdownMenuItem>
              ))
            )}
          </DropdownMenuContent>
        </DropdownMenu>

        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" className="gap-2 px-2">
              <span className="grid size-7 shrink-0 place-items-center rounded-full bg-primary/10 text-xs font-semibold text-primary">
                {initial ?? <User className="size-3.5" />}
              </span>
              <span className="text-sm font-medium">{user?.name ?? user?.email ?? "관리자"}</span>
              <ChevronDown className="size-3.5 text-muted-foreground" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuLabel>{user?.email ?? "관리자"}</DropdownMenuLabel>
            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={handleLogout}>
              <LogOut className="size-4" />
              로그아웃
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  )
}
