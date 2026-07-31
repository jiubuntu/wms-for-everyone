import { Link } from "react-router-dom"
import { Check } from "lucide-react"
import { Button } from "@/components/ui/button"
import { BrandMark } from "@/components/common/BrandMark"
import { cn } from "@/lib/utils"
import introBg from "@/assets/intro.png"
import inboundOutboundImg from "@/assets/dashboard-3.png"
import inventoryImg from "@/assets/inventory-1.png"
import multiWarehouseImg from "@/assets/dashboard-4.png"
import roleImg from "@/assets/user-1.png"

const FEATURES = [
  {
    title: "창고 현황 확인",
    description: "창고별 재고와 입출고 데이터를 통합하여 효율적으로 관리할 수 있습니다.",
    points: ["창고별 입출고 대기 비교", "금일 입출고 처리 현황", "최근 14일 처리 건수 추이"],
    image: multiWarehouseImg,
  },
  {
    title: "정확한 재고관리",
    description: "LOT과 유효기간까지 검색 한 번으로 재고현황을 파악할 수 있습니다.",
    points: ["구역-행-열-단 위치 코드", "LOT 유효기간 추적", "실재고 예약재고 가용재고 구분"],
    image: inventoryImg,
  },
  {
    title: "실시간 대시보드",
    description:
      "출고, 입고, 유통기한 임박 재고까지 실시간으로 관리할 수 있습니다.",
    points: [],
    image: inboundOutboundImg,
  },
  {
    title: "효율적인 직원관리",
    description: "직원 계정을 직접 발급하고 관리할 수 있습니다.",
    points: ["역할별 계정 발급", "담당 창고 소속 관리"],
    image: roleImg,
  },
]

export function Landing() {
  return (
    <div className="flex min-h-svh flex-col">
      <header className="flex items-center justify-between border-b px-6 py-3 sm:px-10">
        <BrandMark />
        <nav className="flex items-center gap-2 text-[15px] font-medium">
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
        <div className="relative z-10 flex flex-col items-center">
          <h1 className="mb-5 text-4xl font-extrabold text-balance sm:text-5xl">
            입고부터 출고까지
            <br />하나의 WMS로 관리하세요
          </h1>
          <p className="mb-8 max-w-xl text-base text-muted-foreground sm:text-lg">
            창고 운영의 전 과정을 통합 관리하는 클라우드 기반 WMS SaaS 플랫폼
          </p>
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
  points,
  image,
}: {
  index: number
  title: string
  description: string
  points: string[]
  image: string
}) {
  const reversed = index % 2 === 1

  return (
    <div
      className={cn(
        "mx-auto flex max-w-6xl flex-col items-center gap-10 px-6 py-20 sm:px-10 md:min-h-[65vh] md:flex-row md:gap-20",
        reversed && "md:flex-row-reverse"
      )}
    >
      <div className="group relative w-full md:flex-[1.15]">
        <div
          className={cn(
            "absolute -inset-6 -z-10 rounded-[2rem] bg-linear-to-br from-primary/30 via-primary/10 to-transparent opacity-70 blur-2xl transition-opacity duration-300 group-hover:opacity-100",
            reversed && "from-primary/10 via-primary/30"
          )}
        />
        <div className="overflow-hidden rounded-xl border bg-card shadow-xl transition-all duration-300 group-hover:-translate-y-1.5 group-hover:shadow-2xl">
          <div className="flex items-center gap-1.5 border-b bg-muted/60 px-3.5 py-2.5">
            <span className="size-2.5 rounded-full bg-destructive/40" />
            <span className="size-2.5 rounded-full bg-warning/50" />
            <span className="size-2.5 rounded-full bg-success/50" />
          </div>
          <img
            src={image}
            alt={`${title} 화면 예시`}
            className="aspect-video w-full object-cover object-top"
          />
        </div>
      </div>
      <div className="flex-1 text-center md:text-left">
        <h2 className="mb-4 text-3xl font-bold text-balance sm:text-4xl">{title}</h2>
        <p className="text-lg text-muted-foreground sm:text-xl">{description}</p>
        {points.length > 0 && (
          <ul className="mx-auto mt-5 flex w-fit flex-col gap-2.5 text-left md:mx-0">
            {points.map((point) => (
              <li key={point} className="flex items-start gap-2.5 text-base text-muted-foreground sm:text-lg">
                <Check className="mt-1 size-5 shrink-0 text-primary" />
                <span>{point}</span>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}
