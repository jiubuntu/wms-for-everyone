import { Link } from "react-router-dom"
import { Button } from "@/components/ui/button"
import { cn } from "@/lib/utils"
import introBg from "@/assets/intro.png"

const FEATURES = [
  {
    title: "입고·출고 자동 기록",
    description: "확정 처리 시점에 재고와 이력이 자동으로 반영됩니다.",
  },
  {
    title: "위치·LOT 단위 재고",
    description: "구역-행-열-단, 필요하면 LOT 번호까지 정확하게 추적합니다.",
  },
  {
    title: "멀티 창고 지원",
    description: "여러 창고를 하나의 화면에서 관리하고, 창고 간 이동 내역도 기록됩니다.",
  },
  {
    title: "역할 기반 권한",
    description: "기업관리자부터 작업자까지, 딱 필요한 만큼만 접근합니다.",
  },
]

export function Landing() {
  return (
    <div className="flex min-h-svh flex-col">
      <header className="flex items-center justify-between border-b px-6 py-3 sm:px-10">
        <span className="text-sm font-extrabold tracking-tight">모두의 WMS</span>
        <nav className="flex items-center gap-2">
          <Button variant="ghost" asChild>
            <Link to="/login">로그인</Link>
          </Button>
          <Button asChild>
            <Link to="/signup">무료로 시작하기</Link>
          </Button>
        </nav>
      </header>

      <main
        className="relative flex flex-1 flex-col items-center justify-center bg-cover bg-center px-6 py-28 text-center sm:px-10"
        style={{ backgroundImage: `url(${introBg})` }}
      >
        <div className="absolute inset-0 bg-background/55" />
        <div className="relative z-10">
          <p className="mb-4 font-mono text-xs tracking-wider text-muted-foreground uppercase">
            소규모 사업체를 위한 창고관리 솔루션
          </p>
          <h1 className="mb-8 text-4xl font-extrabold text-balance sm:text-5xl">
            입고부터 재고까지
            <br />한 곳에서 정확하게 관리
          </h1>
          <Button size="lg" asChild>
            <Link to="/signup">무료로 시작하기</Link>
          </Button>
        </div>
      </main>

      <section className="border-t">
        {/*<p className="pt-14 text-center font-mono text-xs tracking-wider text-muted-foreground uppercase">*/}
        {/*  핵심 기능*/}
        {/*</p>*/}
        {FEATURES.map((feature, i) => (
          <FeatureSection key={feature.title} index={i} {...feature} />
        ))}
      </section>
    </div>
  )
}

function FeatureSection({
  index,
  title,
  description,
}: {
  index: number
  title: string
  description: string
}) {
  const reversed = index % 2 === 1

  return (
    <div
      className={cn(
        "mx-auto flex max-w-4xl flex-col items-center gap-8 px-6 py-16 sm:px-10 md:min-h-[60vh] md:flex-row md:gap-16",
        reversed && "md:flex-row-reverse"
      )}
    >
      <div className="flex aspect-video w-full flex-1 items-center justify-center border border-dashed bg-muted/40 text-xs text-muted-foreground">
        이미지 자리 (추후 제작)
      </div>
      <div className="flex-1 text-center md:text-left">
        <p className="mb-2 font-mono text-xs text-muted-foreground">0{index + 1}</p>
        <h2 className="mb-3 text-2xl font-extrabold text-balance sm:text-3xl">{title}</h2>
        <p className="text-muted-foreground">{description}</p>
      </div>
    </div>
  )
}
