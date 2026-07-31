import { Cell, Label, Pie, PieChart } from "recharts"
import {
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
  type ChartConfig,
} from "@/components/ui/chart"

interface StageDonutSlice {
  key: string
  label: string
  value: number
  color: string
}

interface StageDonutChartProps {
  data: StageDonutSlice[]
  centerCaption: string
}

export function StageDonutChart({ data, centerCaption }: StageDonutChartProps) {
  const total = data.reduce((sum, d) => sum + d.value, 0)
  const chartConfig = Object.fromEntries(
    data.map((d) => [d.key, { label: d.label, color: d.color }])
  ) satisfies ChartConfig

  return (
    <ChartContainer config={chartConfig} className="mx-auto aspect-square h-56">
      <PieChart>
        <ChartTooltip content={<ChartTooltipContent hideLabel nameKey="key" />} />
        <Pie
          data={data}
          dataKey="value"
          nameKey="key"
          innerRadius={60}
          outerRadius={85}
          strokeWidth={3}
          stroke="var(--card)"
        >
          {data.map((entry) => (
            <Cell key={entry.key} fill={entry.color} />
          ))}
          <Label
            content={({ viewBox }) => {
              if (viewBox && "cx" in viewBox && "cy" in viewBox) {
                return (
                  <text x={viewBox.cx} y={viewBox.cy} textAnchor="middle" dominantBaseline="middle">
                    <tspan x={viewBox.cx} y={viewBox.cy} className="fill-foreground text-2xl font-bold">
                      {total}
                    </tspan>
                    <tspan x={viewBox.cx} y={(viewBox.cy ?? 0) + 18} className="fill-muted-foreground text-xs">
                      {centerCaption}
                    </tspan>
                  </text>
                )
              }
              return null
            }}
          />
        </Pie>
        <ChartLegend content={<ChartLegendContent nameKey="key" />} />
      </PieChart>
    </ChartContainer>
  )
}
