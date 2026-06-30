<template>
  <div class="app-container">
    <el-form :model="query" :inline="true">
      <el-form-item label="企业ID">
        <el-input v-model="query.companyId" placeholder="留空=全部企业" style="width: 160px" clearable />
      </el-form-item>
      <el-form-item label="周期">
        <el-select v-model="query.periodType" style="width: 130px">
          <el-option label="月" value="month" />
          <el-option label="季度" value="quarter" />
          <el-option label="年" value="year" />
        </el-select>
      </el-form-item>
      <el-form-item label="期间">
        <el-input v-model="query.period" placeholder="如 2026-03 / 2026-Q1 / 2026" style="width: 240px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">查询</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="16" class="mb12">
      <el-col :span="6"><el-card shadow="hover">收入：{{ formatMoney(summary.totalIncome) }}</el-card></el-col>
      <el-col :span="6"><el-card shadow="hover">成本：{{ formatMoney(summary.totalCost) }}</el-card></el-col>
      <el-col :span="6"><el-card shadow="hover">净利润：{{ formatMoney(summary.netProfit) }}</el-card></el-col>
      <el-col :span="6"><el-card shadow="hover">利润率：{{ formatPercent(summary.profitRate) }}</el-card></el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header>近6个月经营趋势</template>
          <div ref="chartRef" class="chart" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="AifcAnalysis">
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getAnalysisSummary, getAnalysisTrend } from '@/api/aifc/analysis'
import { formatMoney, formatPercent } from '@/utils/format'

const { proxy } = getCurrentInstance()
const chartRef = ref(null)
let chartInstance
let handleResize

const query = reactive({
  companyId: undefined,
  periodType: 'month',
  period: new Date().toISOString().slice(0, 7),
  months: 6
})

const summary = reactive({
  totalIncome: 0,
  totalCost: 0,
  netProfit: 0,
  profitRate: 0
})

function renderChart(rows) {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
    handleResize = () => chartInstance?.resize()
    window.addEventListener('resize', handleResize)
  }
  const months = rows.map(item => item.reportMonth)
  const income = rows.map(item => Number(item.totalIncome || 0))
  const profit = rows.map(item => Number(item.netProfit || 0))
  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['收入', '净利润'] },
    xAxis: { type: 'category', data: months },
    yAxis: { type: 'value' },
    series: [
      { name: '收入', type: 'bar', data: income },
      { name: '净利润', type: 'line', smooth: true, data: profit }
    ]
  })
}

async function loadData() {
  try {
    const [summaryRes, trendRes] = await Promise.all([
      getAnalysisSummary({
        companyId: query.companyId,
        periodType: query.periodType,
        period: query.period
      }),
      getAnalysisTrend({
        companyId: query.companyId,
        months: query.months
      })
    ])
    Object.assign(summary, summaryRes.data || {})
    renderChart(trendRes.data || [])
  } catch (e) {
    ElMessage.error('加载分析数据失败')
  }
}

onMounted(() => {
  loadData()
})

onBeforeUnmount(() => {
  if (handleResize) {
    window.removeEventListener('resize', handleResize)
  }
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style scoped>
.chart {
  height: 380px;
}

.mb12 {
  margin-bottom: 12px;
}
</style>