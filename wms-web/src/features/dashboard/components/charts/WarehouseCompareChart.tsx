import { Bar, BarChart, CartesianGrid, XAxis, YAxis } from "recharts"
import {
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
  type ChartConfig,
} from "@/components/ui/chart"

const chartConfig = {
  outboundPending: { label: "출고 등록대기", color: "var(--chart-1)" },
  outboundPicking: { label: "출고 피킹중", color: "var(--chart-2)" },
  inboundPending: { label: "입고 예정", color: "var(--chart-3)" },
} satisfies ChartConfig

interface WarehouseCompareChartProps {
  data: {
    warehouseName: string
    outboundPending: number
    outboundPicking: number
    inboundPending: number
  }[]
}

export function WarehouseCompareChart({ data }: WarehouseCompareChartProps) {
  return (
    <ChartContainer config={chartConfig} className="aspect-auto h-64 w-full">
      <BarChart data={data} barGap={4}>
        <CartesianGrid vertical={false} />
        <XAxis dataKey="warehouseName" tickLine={false} axisLine={false} tickMargin={8} fontSize={12} />
        <YAxis tickLine={false} axisLine={false} tickMargin={8} fontSize={12} width={28} allowDecimals={false} />
        <ChartTooltip content={<ChartTooltipContent />} />
        <ChartLegend content={<ChartLegendContent />} />
        <Bar dataKey="outboundPending" fill="var(--color-outboundPending)" radius={4} />
        <Bar dataKey="outboundPicking" fill="var(--color-outboundPicking)" radius={4} />
        <Bar dataKey="inboundPending" fill="var(--color-inboundPending)" radius={4} />
      </BarChart>
    </ChartContainer>
  )
}
