<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch">
      <el-form-item label="月份"><el-date-picker v-model="queryParams.reportMonth" type="month" value-format="YYYY-MM" style="width:140px" clearable /></el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['aifc:inventory:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport">导出</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Upload" @click="handleImport">批量导入</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="inventoryList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="企业ID" prop="companyId" width="90" align="center" />
      <el-table-column label="月份" prop="reportMonth" width="100" align="center" />
      <el-table-column label="账面金额" prop="bookAmount" width="120" align="right" />
      <el-table-column label="盘点金额" prop="realAmount" width="120" align="right" />
      <el-table-column label="差异金额" prop="diffAmount" width="120" align="right" />
      <el-table-column label="差异率(%)" prop="diffRate" width="110" align="right" />
      <el-table-column label="备注" prop="remark" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="操作" width="160" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['aifc:inventory:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['aifc:inventory:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="620px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="企业ID" prop="companyId"><el-input v-model.number="form.companyId" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="月份" prop="reportMonth"><el-date-picker v-model="form.reportMonth" type="month" value-format="YYYY-MM" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="账面金额" prop="bookAmount"><el-input-number v-model="form.bookAmount" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="盘点金额" prop="realAmount"><el-input-number v-model="form.realAmount" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <BatchImport
      ref="batchImportRef"
      title="批量导入库存"
      templateFilename="库存导入模板"
      :schema="importSchema"
      :uploadFn="handleBatchUpload"
      @success="getList"
    />
  </div>
</template>

<script setup name="AifcInventory">
import { listInventory, addInventory, updateInventory, removeInventory, batchAddInventory } from '@/api/aifc/inventory'
import { exportTable } from '@/utils/export'
import BatchImport from '@/components/BatchImport/index.vue'
import useCompanyStore from '@/store/modules/company'

const { proxy } = getCurrentInstance()
const companyStore = useCompanyStore()
const loading = ref(false)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const inventoryList = ref([])
const title = ref('')
const open = ref(false)
const batchImportRef = ref()

const importSchema = {
  companyId:   { label: '企业ID',    required: true, type: 'number', example: '1' },
  reportMonth: { label: '月份',      required: true, type: 'month',  example: '2025-03' },
  bookAmount:  { label: '账面金额',  required: true, type: 'number', example: '50000' },
  realAmount:  { label: '盘点金额',  required: true, type: 'number', example: '49500' },
  remark:      { label: '备注',      required: false, example: '' }
}

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, companyId: undefined, reportMonth: undefined },
  form: {},
  rules: {
    companyId: [{ required: true, message: '请输入企业ID', trigger: 'blur' }],
    reportMonth: [{ required: true, message: '请选择月份', trigger: 'change' }],
    bookAmount: [{ required: true, message: '请输入账面金额', trigger: 'blur' }],
    realAmount: [{ required: true, message: '请输入盘点金额', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listInventory(queryParams.value).then(res => {
    inventoryList.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
  }).catch(() => (loading.value = false))
}

function reset() {
  form.value = { id: undefined, companyId: undefined, reportMonth: undefined, bookAmount: 0, realAmount: 0, remark: '' }
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
  title.value = '新增库存'
}

function handleUpdate(row) {
  reset()
  const item = row || inventoryList.value.find(i => i.id === ids.value[0])
  if (!item) return
  form.value = { ...item }
  open.value = true
  title.value = '修改库存'
}

function submitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (!valid) return
    const req = form.value.id ? updateInventory(form.value) : addInventory(form.value)
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
  proxy.$modal.confirm('确认删除选中数据吗？').then(() => removeInventory(deleteIds.join(','))).then(() => {
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
    filename: '库存数据',
    columns: [
      { label: '企业ID', prop: 'companyId' },
      { label: '月份', prop: 'reportMonth' },
      { label: '账面金额', prop: 'bookAmount' },
      { label: '盘点金额', prop: 'realAmount' },
      { label: '差异金额', prop: 'diffAmount' },
      { label: '差异率(%)', prop: 'diffRate' },
      { label: '备注', prop: 'remark' }
    ],
    data: inventoryList.value
  })
}

function handleImport() {
  batchImportRef.value?.open()
}

async function handleBatchUpload(data) {
  await batchAddInventory(data)
}

// 同步全局企业选择
queryParams.value.companyId = companyStore.companyId
watch(() => companyStore.companyId, (val) => {
  queryParams.value.companyId = val
  handleQuery()
})

getList()
</script>
