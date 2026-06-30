<template>
  <div class="app-container">
    <el-card shadow="never" class="mb8">
      <template #header>
        <span>AI票据识别</span>
      </template>
      <el-form ref="recognizeRef" :model="recognizeForm" :rules="recognizeRules" label-width="110px">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="企业ID" prop="companyId">
              <el-input v-model.number="recognizeForm.companyId" placeholder="如 1001" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="文件类型" prop="fileType">
              <el-select v-model="recognizeForm.fileType" placeholder="可选" clearable>
                <el-option label="图片" value="jpg" />
                <el-option label="PDF" value="pdf" />
                <el-option label="文本" value="txt" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="上传票据文件" prop="fileUrl">
              <FileUpload v-model="recognizeForm.fileUrl" :limit="1" :file-size="20" :file-type="['jpg','jpeg','png','pdf','txt']" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="OCR文本">
              <el-input
                v-model="recognizeForm.rawText"
                type="textarea"
                :rows="6"
                placeholder="可直接粘贴OCR识别文本；与上传文件二选一即可"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button type="primary" icon="MagicStick" :loading="recognizing" @click="handleRecognize" v-hasPermi="['aifc:invoice:add']">AI识别并入库</el-button>
          <el-button icon="Refresh" @click="resetRecognize">清空</el-button>
        </el-form-item>
      </el-form>
    </el-card>

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
      <el-form-item label="发票代码" prop="invoiceCode">
        <el-input v-model="queryParams.invoiceCode" placeholder="发票代码" style="width: 150px" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="发票号码" prop="invoiceNo">
        <el-input v-model="queryParams.invoiceNo" placeholder="发票号码" style="width: 150px" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" style="width: 120px" clearable>
          <el-option label="有效" :value="1" />
          <el-option label="作废" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['aifc:invoice:remove']">批量删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="invoiceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="70" align="center">
        <template #default="scope">
          {{ (queryParams.pageNum - 1) * queryParams.pageSize + scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column label="企业ID" prop="companyId" width="90" align="center" />
      <el-table-column label="月份" prop="reportMonth" width="100" align="center" />
      <el-table-column label="发票代码" prop="invoiceCode" min-width="120" :show-overflow-tooltip="true" />
      <el-table-column label="发票号码" prop="invoiceNo" min-width="120" :show-overflow-tooltip="true" />
      <el-table-column label="金额" prop="amount" width="100" align="right" />
      <el-table-column label="税额" prop="taxAmount" width="100" align="right" />
      <el-table-column label="开票日期" prop="invoiceDate" width="120" align="center">
        <template #default="scope">{{ parseTime(scope.row.invoiceDate, '{y}-{m}-{d}') }}</template>
      </el-table-column>
      <el-table-column label="销售方" prop="sellerName" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="购买方" prop="buyerName" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="货物/服务" prop="goodsName" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="整数" width="70" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.isInteger === 1" type="warning">是</el-tag>
          <el-tag v-else type="info">否</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="连号" width="70" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.isSeries === 1" type="danger">是</el-tag>
          <el-tag v-else type="success">否</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 2" type="danger">作废</el-tag>
          <el-tag v-else type="success">有效</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Check" @click="openConfirm(scope.row)" v-hasPermi="['aifc:invoice:edit']">确认入账</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['aifc:invoice:remove']">删除</el-button>
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

    <el-dialog title="确认入账" v-model="open" width="500px" append-to-body>
      <el-form ref="confirmRef" :model="confirmForm" :rules="confirmRules" label-width="100px">
        <el-form-item label="会计科目" prop="subject">
          <el-input v-model="confirmForm.subject" placeholder="例如 管理费用" />
        </el-form-item>
        <el-form-item label="凭证类型" prop="entryType">
          <el-radio-group v-model="confirmForm.entryType">
            <el-radio :label="1">收入</el-radio>
            <el-radio :label="2">支出</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="entryRemark">
          <el-input v-model="confirmForm.entryRemark" type="textarea" :rows="3" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="confirming" @click="submitConfirm">确认</el-button>
          <el-button @click="open = false">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AifcInvoice">
import { listInvoice, recognizeInvoice, confirmInvoice, removeInvoice } from '@/api/aifc/invoice'
import { parseTime } from '@/utils/ruoyi'
import useCompanyStore from '@/store/modules/company'

const { proxy } = getCurrentInstance()
const companyStore = useCompanyStore()

const loading = ref(false)
const recognizing = ref(false)
const confirming = ref(false)
const showSearch = ref(true)
const total = ref(0)
const invoiceList = ref([])
const ids = ref([])
const multiple = ref(true)
const open = ref(false)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    companyId: undefined,
    reportMonth: undefined,
    invoiceCode: undefined,
    invoiceNo: undefined,
    status: undefined
  },
  recognizeForm: {
    companyId: undefined,
    fileUrl: '',
    fileType: '',
    rawText: ''
  },
  confirmForm: {
    id: undefined,
    subject: '管理费用',
    entryType: 2,
    entryRemark: '票据确认入账'
  },
  recognizeRules: {
    companyId: [{ required: true, message: '请输入企业ID', trigger: 'blur' }]
  },
  confirmRules: {
    subject: [{ required: true, message: '请输入会计科目', trigger: 'blur' }],
    entryType: [{ required: true, message: '请选择凭证类型', trigger: 'change' }]
  }
})

const { queryParams, recognizeForm, confirmForm, recognizeRules, confirmRules } = toRefs(data)

function getList() {
  loading.value = true
  listInvoice(queryParams.value).then(res => {
    invoiceList.value = res.rows || []
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
  getList()
}

function resetRecognize() {
  proxy.resetForm('recognizeRef')
  recognizeForm.value.fileUrl = ''
  recognizeForm.value.fileType = ''
  recognizeForm.value.rawText = ''
}

function handleRecognize() {
  proxy.$refs.recognizeRef.validate(valid => {
    if (!valid) {
      return
    }
    if (!recognizeForm.value.fileUrl && !recognizeForm.value.rawText) {
      proxy.$modal.msgWarning('请上传票据文件或粘贴OCR文本，至少填写一项')
      return
    }
    recognizing.value = true
    recognizeInvoice(recognizeForm.value).then(res => {
      const data = res.data || {}
      proxy.$modal.msgSuccess('识别完成，已生成草稿，单号：' + (data.invoiceNo || '-'))
      resetRecognize()
      getList()
    }).finally(() => {
      recognizing.value = false
    })
  })
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  multiple.value = selection.length === 0
}

function openConfirm(row) {
  confirmForm.value.id = row.id
  confirmForm.value.subject = '管理费用'
  confirmForm.value.entryType = 2
  confirmForm.value.entryRemark = '票据确认入账'
  open.value = true
}

function submitConfirm() {
  proxy.$refs.confirmRef.validate(valid => {
    if (!valid) {
      return
    }
    confirming.value = true
    confirmInvoice(confirmForm.value.id, {
      subject: confirmForm.value.subject,
      entryType: confirmForm.value.entryType,
      entryRemark: confirmForm.value.entryRemark
    }).then(() => {
      proxy.$modal.msgSuccess('入账成功')
      open.value = false
      getList()
    }).finally(() => {
      confirming.value = false
    })
  })
}

function handleDelete(row) {
  const deleteIds = row?.id ? [row.id] : ids.value
  if (!deleteIds || deleteIds.length === 0) {
    proxy.$modal.msgWarning('请先选择要删除的票据')
    return
  }
  proxy.$modal.confirm('确认删除选中的票据数据吗？').then(() => {
    return removeInvoice(deleteIds.join(','))
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getList()
  }).catch(() => {})
}

// 同步全局企业选择
queryParams.value.companyId = companyStore.companyId
watch(() => companyStore.companyId, (val) => {
  queryParams.value.companyId = val
  handleQuery()
})

getList()
</script>
