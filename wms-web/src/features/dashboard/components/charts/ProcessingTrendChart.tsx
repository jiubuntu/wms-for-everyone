import { CartesianGrid, Line, LineChart, XAxis, YAxis } from "recharts"
import {
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
  type ChartConfig,
} from "@/components/ui/chart"
import type { ProcessingTrendPoint } from "@/features/dashboard/types"

const chartConfig = {
  outboundCount: { label: "출고 처리 건수", color: "var(--chart-1)" },
  inboundCount: { label: "입고 처리 건수", color: "var(--chart-3)" },
} satisfies ChartConfig

export function ProcessingTrendChart({ data }: { data: ProcessingTrendPoint[] }) {
  return (
    <ChartContainer config={chartConfig} className="aspect-auto h-64 w-full">
      <LineChart data={data}>
        <CartesianGrid vertical={false} />
        <XAxis dataKey="label" tickLine={false} axisLine={false} tickMargin={8} fontSize={12} interval={2} />
        <YAxis tickLine={false} axisLine={false} tickMargin={8} fontSize={12} width={28} allowDecimals={false} />
        <ChartTooltip content={<ChartTooltipContent indicator="line" />} />
        <ChartLegend content={<ChartLegendContent />} />
        <Line dataKey="outboundCount" type="monotone" stroke="var(--color-outboundCount)" strokeWidth={2} dot={false} />
        <Line dataKey="inboundCount" type="monotone" stroke="var(--color-inboundCount)" strokeWidth={2} dot={false} />
      </LineChart>
    </ChartContainer>
  )
}
