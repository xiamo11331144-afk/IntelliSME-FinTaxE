<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch">
      <el-form-item label="月份" prop="reportMonth">
        <el-date-picker v-model="queryParams.reportMonth" type="month" value-format="YYYY-MM" placeholder="月份" style="width: 140px" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['aifc:monthReport:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="monthReportList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="企业ID" prop="companyId" width="90" align="center" />
      <el-table-column label="月份" prop="reportMonth" width="100" align="center" />
      <el-table-column label="总收入" prop="totalIncome" width="110" align="right" />
      <el-table-column label="总成本" prop="totalCost" width="110" align="right" />
      <el-table-column label="税额" prop="taxPaid" width="100" align="right" />
      <el-table-column label="有票成本" prop="invoiceAmount" width="110" align="right" />
      <el-table-column label="工资总额" prop="salaryTotal" width="110" align="right" />
      <el-table-column label="员工数" prop="employeeCount" width="90" align="center" />
      <el-table-column label="库存金额" prop="inventoryAmount" width="110" align="right" />
      <el-table-column label="备注" prop="remark" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="操作" width="160" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['aifc:monthReport:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['aifc:monthReport:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="720px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="企业ID" prop="companyId">
              <el-input v-model.number="form.companyId" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="月份" prop="reportMonth">
              <el-date-picker v-model="form.reportMonth" type="month" value-format="YYYY-MM" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="总收入"><el-input-number v-model="form.totalIncome" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="总成本"><el-input-number v-model="form.totalCost" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="税额"><el-input-number v-model="form.taxPaid" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="有票成本"><el-input-number v-model="form.invoiceAmount" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="工资总额"><el-input-number v-model="form.salaryTotal" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="员工数"><el-input-number v-model="form.employeeCount" :min="0" :precision="0" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="库存金额"><el-input-number v-model="form.inventoryAmount" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AifcMonthReport">
import { listMonthReport, addMonthReport, updateMonthReport, removeMonthReport } from '@/api/aifc/monthReport'
import { exportTable } from '@/utils/export'
import useCompanyStore from '@/store/modules/company'

const { proxy } = getCurrentInstance()
const companyStore = useCompanyStore()
const loading = ref(false)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const monthReportList = ref([])
const title = ref('')
const open = ref(false)

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, companyId: undefined, reportMonth: undefined },
  form: {},
  rules: {
    companyId: [{ required: true, message: '请输入企业ID', trigger: 'blur' }],
    reportMonth: [{ required: true, message: '请选择月份', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listMonthReport(queryParams.value).then(res => {
    monthReportList.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
  }).catch(() => (loading.value = false))
}

function reset() {
  form.value = { id: undefined, companyId: undefined, reportMonth: undefined, totalIncome: 0, totalCost: 0, taxPaid: 0, invoiceAmount: 0, salaryTotal: 0, employeeCount: 0, inventoryAmount: 0, remark: '' }
  proxy.resetForm('formRef')
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增月报'
}

function handleUpdate(row) {
  reset()
  const item = row || monthReportList.value.find(i => i.id === ids.value[0])
  if (!item) return
  form.value = { ...item }
  open.value = true
  title.value = '修改月报'
}

function submitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (!valid) return
    const req = form.value.id ? updateMonthReport(form.value) : addMonthReport(form.value)
    req.then(() => {
      proxy.$modal.msgSuccess(form.value.id ? '修改成功' : '新增成功')
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  const deleteIds = row?.id ? [row.id] : ids.value
  if (!deleteIds.length) {
    proxy.$modal.msgWarning('请先选择数据')
    return
  }
  proxy.$modal.confirm('确认删除选中数据吗？').then(() => removeMonthReport(deleteIds.join(','))).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getList()
  }).catch(() => {})
}

function cancel() {
  open.value = false
  reset()
}

function handleExport() {
  exportTable({
    filename: '月报数据',
    columns: [
      { label: '企业ID', prop: 'companyId' },
      { label: '月份', prop: 'reportMonth' },
      { label: '总收入', prop: 'totalIncome' },
      { label: '总成本', prop: 'totalCost' },
      { label: '税额', prop: 'taxPaid' },
      { label: '有票成本', prop: 'invoiceAmount' },
      { label: '工资总额', prop: 'salaryTotal' },
      { label: '员工数', prop: 'employeeCount' },
      { label: '库存金额', prop: 'inventoryAmount' },
      { label: '备注', prop: 'remark' }
    ],
    data: monthReportList.value
  })
}

// 同步全局企业选择
queryParams.value.companyId = companyStore.companyId
watch(() => companyStore.companyId, (val) => {
  queryParams.value.companyId = val
  handleQuery()
})

getList()
</script>
