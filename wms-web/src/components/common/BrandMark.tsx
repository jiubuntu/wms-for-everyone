import { Link } from "react-router-dom"
import { cn } from "@/lib/utils"

export function BrandMark({ className }: { className?: string }) {
  return (
    <Link to="/" className={cn("text-sm font-extrabold tracking-tight", className)}>
      모두의 WMS
    </Link>
  )
}
