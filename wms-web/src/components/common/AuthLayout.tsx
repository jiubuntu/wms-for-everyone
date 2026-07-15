import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { BrandMark } from "@/components/common/BrandMark"
import { cn } from "@/lib/utils"

interface AuthLayoutProps {
  title: string
  description?: string
  children: React.ReactNode
  className?: string
  backgroundImage?: string
}

export function AuthLayout({
  title,
  description,
  children,
  className,
  backgroundImage,
}: AuthLayoutProps) {
  return (
    <div
      className={cn(
        "relative flex min-h-svh flex-col items-center justify-center gap-6 bg-cover bg-center p-6",
        !backgroundImage && "bg-background"
      )}
      style={backgroundImage ? { backgroundImage: `url(${backgroundImage})` } : undefined}
    >
      {backgroundImage && <div className="absolute inset-0 bg-background/70" />}

      <BrandMark className="relative z-10" />
      <Card className={cn("relative z-10 w-full max-w-xs bg-background", className)}>
        <CardHeader>
          <CardTitle className="text-center text-lg">{title}</CardTitle>
          {description && (
            <CardDescription className="text-center">{description}</CardDescription>
          )}
        </CardHeader>
        <CardContent>{children}</CardContent>
      </Card>
    </div>
  )
}
