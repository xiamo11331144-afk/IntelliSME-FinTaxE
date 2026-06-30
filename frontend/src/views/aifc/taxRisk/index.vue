<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch">
      <el-form-item label="月份" prop="reportMonth">
        <el-date-picker
          v-model="queryParams.reportMonth"
          type="month"
          value-format="YYYY-MM"
          placeholder="月份"
          style="width: 140px"
          clearable
        />
      </el-form-item>
      <el-form-item label="类型" prop="riskType">
        <el-input v-model="queryParams.riskType" placeholder="风险类型" style="width: 180px" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="等级" prop="riskLevel">
        <el-select v-model="queryParams.riskLevel" placeholder="风险等级" clearable style="width: 120px">
          <el-option label="高" value="high" />
          <el-option label="中" value="medium" />
          <el-option label="低" value="low" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="处理状态" clearable style="width: 130px">
          <el-option label="未处理" :value="0" />
          <el-option label="已处理" :value="1" />
          <el-option label="已忽略" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="MagicStick" :loading="scanning" :disabled="scanning" @click="handleScan" v-hasPermi="['aifc:taxRisk:add']">AI 深度扫描</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Lightning" :loading="scanning" :disabled="scanning" @click="handleQuickScan" v-hasPermi="['aifc:taxRisk:add']">快速扫描</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Check" :disabled="multiple" @click="openBatchDialog(1)" v-hasPermi="['aifc:taxRisk:edit']">批量处理</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="CircleClose" :disabled="multiple" @click="openBatchDialog(2)" v-hasPermi="['aifc:taxRisk:edit']">批量忽略</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="RefreshLeft" :disabled="multiple" @click="openBatchDialog(0)" v-hasPermi="['aifc:taxRisk:edit']">批量恢复未处理</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="riskList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="70" align="center">
        <template #default="scope">
          {{ (queryParams.pageNum - 1) * queryParams.pageSize + scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column label="企业ID" prop="companyId" width="90" align="center" />
      <el-table-column label="月份" prop="reportMonth" width="100" align="center" />
      <el-table-column label="风险类型" prop="riskType" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column label="风险等级" width="90" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.riskLevel === 'high'" type="danger">高</el-tag>
          <el-tag v-else-if="scope.row.riskLevel === 'medium'" type="warning">中</el-tag>
          <el-tag v-else type="info">低</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="风险内容" prop="riskContent" min-width="220" :show-overflow-tooltip="true" />
      <el-table-column label="整改建议" prop="aiSuggestion" min-width="220" :show-overflow-tooltip="true" />
      <el-table-column label="处理状态" width="110" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 1" type="success">已处理</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="info">已忽略</el-tag>
          <el-tag v-else type="danger">未处理</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="360" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            v-if="scope.row.status !== 1"
            link
            type="primary"
            icon="Check"
            @click="openSingleDialog(scope.row, 1)"
            v-hasPermi="['aifc:taxRisk:edit']"
          >处理</el-button>
          <el-button
            v-if="scope.row.status !== 2"
            link
            type="primary"
            icon="CircleClose"
            @click="openSingleDialog(scope.row, 2)"
            v-hasPermi="['aifc:taxRisk:edit']"
          >忽略</el-button>
          <el-button
            v-if="scope.row.status !== 0"
            link
            type="primary"
            icon="RefreshLeft"
            @click="openSingleDialog(scope.row, 0)"
            v-hasPermi="['aifc:taxRisk:edit']"
          >恢复未处理</el-button>
          <el-button link type="primary" icon="Document" @click="openRemarkDialog(scope.row)">备注</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="dialogTitle" v-model="openHandle" width="520px" append-to-body>
      <el-form ref="handleRef" :model="handleForm" label-width="120px">
        <el-form-item label="处理状态">
          <el-tag v-if="handleForm.status === 1" type="success">已处理</el-tag>
          <el-tag v-else-if="handleForm.status === 0" type="danger">未处理</el-tag>
          <el-tag v-else type="info">已忽略</el-tag>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="handleForm.handleRemark" type="textarea" :rows="4" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitHandle">确认</el-button>
          <el-button @click="openHandle = false">取消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="处理备注" v-model="openRemark" width="520px" append-to-body>
      <el-input v-model="remarkContent" type="textarea" :rows="6" readonly />
      <template #footer>
        <el-button @click="openRemark = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AifcTaxRisk">
import { listTaxRisk, scanTaxRisk, scanTaxRiskAuto, scanTaxRiskQuick, scanTaxRiskAutoQuick, handleTaxRisk, batchHandleTaxRisk } from '@/api/aifc/taxRisk'
import { exportTable } from '@/utils/export'
import useCompanyStore from '@/store/modules/company'

const { proxy } = getCurrentInstance()
const route = useRoute()
const companyStore = useCompanyStore()

const loading = ref(false)
const showSearch = ref(true)
const total = ref(0)
const riskList = ref([])
const ids = ref([])
const multiple = ref(true)

const scanning = ref(false)

const openHandle = ref(false)
const openRemark = ref(false)
const remarkContent = ref('')
const dialogTitle = ref('')
const batchMode = ref(false)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    companyId: undefined,
    reportMonth: '',
    riskType: undefined,
    riskLevel: undefined,
    status: undefined
  },
  handleForm: {
    id: undefined,
    status: 1,
    handleRemark: ''
  }
})

const { queryParams, handleForm } = toRefs(data)

function getList() {
  loading.value = true
  listTaxRisk(queryParams.value).then(res => {
    riskList.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  queryParams.value.pageNum = 1
  queryParams.value.pageSize = 10
  queryParams.value.companyId = undefined
  getList()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  multiple.value = selection.length === 0
}

function handleScan() {
  const hasCompany = !!queryParams.value.companyId
  const hasMonth = !!queryParams.value.reportMonth
  if (hasCompany !== hasMonth) {
    proxy.$modal.msgWarning('单企业扫描请同时填写企业ID和月份；留空则执行全部企业自动扫描')
    return
  }
  const scanDesc = hasCompany
    ? `企业 ${queryParams.value.companyId}（${queryParams.value.reportMonth}）`
    : '全部企业'
  proxy.$modal.confirm(`确认对 ${scanDesc} 执行 AI 深度扫描？（较慢，含智能分析建议）`, 'AI 深度扫描').then(() => {
    scanning.value = true
    const action = hasCompany
      ? scanTaxRisk(queryParams.value.companyId, queryParams.value.reportMonth)
      : scanTaxRiskAuto()
    return action
  }).then(res => {
    proxy.$modal.msgSuccess('AI 扫描完成，本次新增风险：' + (res.scanInserted || 0))
    getList()
  }).catch(err => {
    if (err === 'cancel') return
    proxy.$modal.msgError('扫描失败，请稍后重试')
  }).finally(() => {
    scanning.value = false
  })
}

function handleQuickScan() {
  const hasCompany = !!queryParams.value.companyId
  const hasMonth = !!queryParams.value.reportMonth
  if (hasCompany !== hasMonth) {
    proxy.$modal.msgWarning('单企业扫描请同时填写企业ID和月份；留空则执行全部企业快速扫描')
    return
  }
  const scanDesc = hasCompany
    ? `企业 ${queryParams.value.companyId}（${queryParams.value.reportMonth}）`
    : '全部企业'
  proxy.$modal.confirm(`确认对 ${scanDesc} 执行快速扫描？（仅规则引擎，速度快）`, '快速扫描').then(() => {
    scanning.value = true
    const action = hasCompany
      ? scanTaxRiskQuick(queryParams.value.companyId, queryParams.value.reportMonth)
      : scanTaxRiskAutoQuick()
    return action
  }).then(res => {
    proxy.$modal.msgSuccess('快速扫描完成，本次新增风险：' + (res.scanInserted || 0))
    getList()
  }).catch(err => {
    if (err === 'cancel') return
    proxy.$modal.msgError('扫描失败，请稍后重试')
  }).finally(() => {
    scanning.value = false
  })
}

function openSingleDialog(row, status) {
  batchMode.value = false
  dialogTitle.value = status === 1 ? '处理风险' : (status === 2 ? '忽略风险' : '恢复为未处理')
  handleForm.value.id = row.id
  handleForm.value.status = status
  handleForm.value.handleRemark = ''
  openHandle.value = true
}

function openBatchDialog(status) {
  if (ids.value.length === 0) {
    proxy.$modal.msgWarning('请至少选择一条数据')
    return
  }
  batchMode.value = true
  dialogTitle.value = status === 1 ? '批量处理风险' : (status === 2 ? '批量忽略风险' : '批量恢复未处理')
  handleForm.value.status = status
  handleForm.value.handleRemark = ''
  openHandle.value = true
}

function submitHandle() {
  if (batchMode.value) {
    batchHandleTaxRisk({
      ids: ids.value,
      status: handleForm.value.status,
      handleRemark: handleForm.value.handleRemark
    }).then(() => {
      proxy.$modal.msgSuccess('批量操作成功')
      openHandle.value = false
      getList()
    }).catch(() => {
      proxy.$modal.msgError('批量操作失败，请稍后重试')
    })
    return
  }
  handleTaxRisk(handleForm.value.id, {
    status: handleForm.value.status,
    handleRemark: handleForm.value.handleRemark
  }).then(() => {
    proxy.$modal.msgSuccess('操作成功')
    openHandle.value = false
    getList()
  }).catch(() => {
    proxy.$modal.msgError('操作失败，请稍后重试')
  })
}

function openRemarkDialog(row) {
  remarkContent.value = row.handleRemark || '暂无备注'
  openRemark.value = true
}

function handleExport() {
  exportTable({
    filename: '税务风险',
    columns: [
      { label: '企业ID', prop: 'companyId' },
      { label: '月份', prop: 'reportMonth' },
      { label: '风险类型', prop: 'riskType' },
      { label: '风险等级', prop: 'riskLevel' },
      { label: '风险内容', prop: 'riskContent' },
      { label: '整改建议', prop: 'aiSuggestion' },
      { label: '处理状态', prop: 'status' }
    ],
    data: riskList.value
  })
}

// 从 dashboard 跳转过来时，读取查询参数
if (route.query.companyId) {
  queryParams.value.companyId = route.query.companyId
}
if (route.query.reportMonth) {
  queryParams.value.reportMonth = route.query.reportMonth
}

// 同步全局企业选择（仅在 route.query 无 companyId 时生效）
if (!route.query.companyId && companyStore.companyId) {
  queryParams.value.companyId = companyStore.companyId
}
watch(() => companyStore.companyId, (val) => {
  queryParams.value.companyId = val
  handleQuery()
})

getList()
</script>
