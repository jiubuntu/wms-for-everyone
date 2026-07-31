import { Link } from "react-router-dom"
import { ChevronRight } from "lucide-react"

interface BreadcrumbItem {
  label: string
  to?: string
}

interface PageHeaderProps {
  title: string
  breadcrumb: BreadcrumbItem[]
}

export function PageHeader({ title, breadcrumb }: PageHeaderProps) {
  return (
    <div className="mb-6 flex flex-wrap items-center justify-between gap-2">
      <h1 className="text-2xl font-bold">{title}</h1>
      <nav className="flex items-center gap-1.5 text-sm text-muted-foreground">
        {breadcrumb.map((crumb, i) => (
          <span key={crumb.label} className="flex items-center gap-1.5">
            {i > 0 && <ChevronRight className="size-3.5" />}
            {crumb.to ? (
              <Link to={crumb.to} className="hover:text-foreground">
                {crumb.label}
              </Link>
            ) : (
              <span className="text-foreground">{crumb.label}</span>
            )}
          </span>
        ))}
      </nav>
    </div>
  )
}
