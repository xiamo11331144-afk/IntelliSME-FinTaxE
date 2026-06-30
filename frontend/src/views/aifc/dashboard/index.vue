<template>
  <div class="app-container">
    <el-form :inline="true">
      <el-form-item label="月份">
        <el-date-picker v-model="reportMonth" type="month" value-format="YYYY-MM" style="width: 140px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">刷新</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="16" class="mb12">
      <el-col :span="6"><el-card shadow="hover">本月收入：{{ formatMoney(summary.totalIncome) }}</el-card></el-col>
      <el-col :span="6"><el-card shadow="hover">本月成本：{{ formatMoney(summary.totalCost) }}</el-card></el-col>
      <el-col :span="6"><el-card shadow="hover">本月净利润：{{ formatMoney(summary.netProfit) }}</el-card></el-col>
      <el-col :span="6"><el-card shadow="hover">未处理风险：{{ unhandledCount }}</el-card></el-col>
    </el-row>

    <el-card shadow="never">
      <template #header>最新风险预警</template>
      <el-table :data="riskRows" v-loading="loading" @row-click="goToRisk" style="cursor: pointer">
        <el-table-column label="类型" prop="riskType" min-width="220" />
        <el-table-column label="等级" width="90">
          <template #default="scope">
            <el-tag v-if="scope.row.riskLevel === 'high'" type="danger">高</el-tag>
            <el-tag v-else-if="scope.row.riskLevel === 'medium'" type="warning">中</el-tag>
            <el-tag v-else type="info">低</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="内容" prop="riskContent" min-width="280" :show-overflow-tooltip="true" />
        <el-table-column label="建议" prop="aiSuggestion" min-width="280" :show-overflow-tooltip="true" />
      </el-table>
      <pagination v-show="riskTotal > 0" :total="riskTotal" v-model:page="riskPageNum" v-model:limit="riskPageSize" @pagination="loadRisks" />
    </el-card>
  </div>
</template>

<script setup name="AifcDashboard">
import { getAnalysisSummary } from '@/api/aifc/analysis'
import { listTaxRisk } from '@/api/aifc/taxRisk'
import { formatMoney } from '@/utils/format'
import { ElMessage } from 'element-plus'
import useCompanyStore from '@/store/modules/company'

const router = useRouter()
const companyStore = useCompanyStore()
const loading = ref(false)
const reportMonth = ref(new Date().toISOString().slice(0, 7))
const riskRows = ref([])
const riskTotal = ref(0)
const riskPageNum = ref(1)
const riskPageSize = ref(10)
const unhandledCount = ref(0)
const summary = reactive({
  totalIncome: 0,
  totalCost: 0,
  netProfit: 0
})

async function loadRisks() {
  try {
    const r = await listTaxRisk({
      pageNum: riskPageNum.value,
      pageSize: riskPageSize.value,
      companyId: companyStore.companyId
    })
    riskRows.value = r.rows || []
    riskTotal.value = r.total || 0
    // 未处理数用全部数据统计（不依赖分页）
    unhandledCount.value = r.rows ? r.rows.filter(item => item.status === 0).length : 0
  } catch {
    riskRows.value = []
    riskTotal.value = 0
  }
}

async function loadData() {
  loading.value = true
  try {
    const [s] = await Promise.all([
      getAnalysisSummary({
        companyId: companyStore.companyId,
        periodType: 'month',
        period: reportMonth.value
      }),
      loadRisks()
    ])
    Object.assign(summary, s.data || {})
  } catch (e) {
    ElMessage.error('加载仪表盘数据失败')
  } finally {
    loading.value = false
  }
}

function goToRisk(row) {
  router.push({
    path: '/aifc/taxRisk',
    query: { companyId: row.companyId, reportMonth: row.reportMonth }
  })
}

onMounted(() => {
  loadData()
  // 全局企业切换时自动刷新
  watch(() => companyStore.companyId, () => loadData())
})
</script>

<style scoped>
.mb12 {
  margin-bottom: 12px;
}
</style>