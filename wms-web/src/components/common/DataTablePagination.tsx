import type { ReactNode } from "react"
import { ChevronsLeft, ChevronsRight } from "lucide-react"
import type { PageInfo } from "@/lib/apiTypes"
import { cn } from "@/lib/utils"

interface DataTablePaginationProps {
  pageInfo: PageInfo
  onPageChange: (page: number) => void
}

export function DataTablePagination({ pageInfo, onPageChange }: DataTablePaginationProps) {
  const { page, totalPages } = pageInfo
  const pages = getPageNumbers(page, totalPages)

  return (
    <div className="flex justify-center">
      <div className="inline-flex items-center">
        <PageButton
          className="rounded-l-md"
          disabled={page <= 1}
          onClick={() => onPageChange(page - 1)}
          aria-label="이전 페이지"
        >
          <ChevronsLeft className="size-4" />
        </PageButton>

        {pages.map((p, i) =>
          p === "..." ? (
            <span
              key={`ellipsis-${i}`}
              className="-ml-px flex h-8 w-8 items-center justify-center border border-border text-sm text-muted-foreground"
            >
              …
            </span>
          ) : (
            <PageButton key={p} active={p === page} onClick={() => onPageChange(p)}>
              {p}
            </PageButton>
          )
        )}

        <PageButton
          className="rounded-r-md"
          disabled={page >= totalPages}
          onClick={() => onPageChange(page + 1)}
          aria-label="다음 페이지"
        >
          <ChevronsRight className="size-4" />
        </PageButton>
      </div>
    </div>
  )
}

function PageButton({
  children,
  active,
  disabled,
  onClick,
  className,
  "aria-label": ariaLabel,
}: {
  children: ReactNode
  active?: boolean
  disabled?: boolean
  onClick?: () => void
  className?: string
  "aria-label"?: string
}) {
  return (
    <button
      type="button"
      disabled={disabled}
      onClick={onClick}
      aria-label={ariaLabel}
      className={cn(
        "-ml-px flex h-8 w-8 items-center justify-center border border-border text-sm first:ml-0 disabled:pointer-events-none disabled:opacity-40",
        active
          ? "z-10 border-primary bg-primary text-primary-foreground"
          : "bg-card text-foreground hover:bg-muted",
        className
      )}
    >
      {children}
    </button>
  )
}

function getPageNumbers(current: number, total: number): (number | "...")[] {
  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => i + 1)
  }

  const pages: (number | "...")[] = [1]
  if (current > 3) pages.push("...")

  for (let p = Math.max(2, current - 1); p <= Math.min(total - 1, current + 1); p++) {
    pages.push(p)
  }

  if (current < total - 2) pages.push("...")
  pages.push(total)

  return pages
}
