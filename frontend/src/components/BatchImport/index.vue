<template>
  <el-dialog
    :title="title"
    v-model="visible"
    width="680px"
    append-to-body
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <!-- 操作栏 -->
    <div class="import-toolbar">
      <el-button type="primary" plain icon="Download" @click="handleDownloadTemplate">
        下载模板
      </el-button>
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :show-file-list="false"
        :accept="acceptTypes"
        :on-change="handleFileChange"
        :limit="1"
      >
        <el-button type="success" plain icon="Upload">选择文件</el-button>
      </el-upload>
      <span v-if="fileName" class="file-name">{{ fileName }}</span>
    </div>

    <!-- 校验错误 -->
    <el-alert
      v-if="errors.length"
      type="error"
      :closable="false"
      show-icon
      class="import-errors"
    >
      <template #title>
        校验失败（{{ errors.length }} 条错误），以下为前 10 条：
      </template>
      <template #default>
        <ul class="error-list">
          <li v-for="(err, i) in errors.slice(0, 10)" :key="i">{{ err.message }}</li>
        </ul>
      </template>
    </el-alert>

    <!-- 数据预览 -->
    <el-table
      v-if="previewData.length"
      :data="previewData.slice(0, 20)"
      border
      size="small"
      max-height="300"
      class="import-preview"
    >
      <el-table-column
        v-for="col in previewColumns"
        :key="col.prop"
        :prop="col.prop"
        :label="col.label"
        min-width="100"
        show-overflow-tooltip
      />
    </el-table>

    <div v-if="previewData.length" class="import-summary">
      共解析 <b>{{ previewData.length }}</b> 条有效数据
      <span v-if="previewData.length > 20">（仅预览前 20 条）</span>
    </div>

    <div v-if="!previewData.length && !errors.length && fileName" class="import-empty">
      <el-empty description="未解析到有效数据" :image-size="60" />
    </div>

    <template #footer>
      <el-button @click="visible = false">取 消</el-button>
      <el-button
        type="primary"
        :disabled="!previewData.length"
        :loading="submitting"
        @click="handleConfirm"
      >
        确认导入（{{ previewData.length }} 条）
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup name="BatchImport">
import { parseExcel, downloadTemplate } from '@/utils/import'

const props = defineProps({
  /** 弹窗标题 */
  title: { type: String, default: '批量导入' },
  /**
   * 字段定义 Schema
   * { fieldName: { label, required, type?, example?, validator? } }
   */
  schema: { type: Object, required: true },
  /** 模板文件名（不含扩展名） */
  templateFilename: { type: String, default: '导入模板' },
  /** 上传接口函数 (data[] ) => Promise */
  uploadFn: { type: Function, required: true },
  /** 接受的文件类型 */
  acceptTypes: { type: String, default: '.xlsx,.xls,.csv' }
})

const emit = defineEmits(['success'])

const visible = ref(false)
const fileName = ref('')
const previewData = ref([])
const errors = ref([])
const submitting = ref(false)
const uploadRef = ref()

const previewColumns = computed(() =>
  Object.entries(props.schema).map(([prop, conf]) => ({
    prop,
    label: conf.label
  }))
)

function open() {
  visible.value = true
  resetState()
}

function resetState() {
  fileName.value = ''
  previewData.value = []
  errors.value = []
  submitting.value = false
}

function handleClose() {
  resetState()
}

function handleDownloadTemplate() {
  downloadTemplate(props.templateFilename, props.schema)
}

async function handleFileChange(uploadFile) {
  fileName.value = uploadFile.name
  errors.value = []
  previewData.value = []

  try {
    const { data, errors: errs } = await parseExcel(uploadFile.raw, props.schema)
    errors.value = errs
    previewData.value = data
  } catch (err) {
    errors.value = [{ message: err.message || '解析失败' }]
  }
}

async function handleConfirm() {
  if (!previewData.value.length) return
  submitting.value = true
  try {
    await props.uploadFn(previewData.value)
    ElMessage.success(`成功导入 ${previewData.value.length} 条数据`)
    visible.value = false
    emit('success')
  } catch (err) {
    ElMessage.error(err?.message || '导入失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>

<style lang="scss" scoped>
.import-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;

  .file-name {
    color: #606266;
    font-size: 13px;
    margin-left: 4px;
  }
}

.import-errors {
  margin-bottom: 14px;

  .error-list {
    margin: 0;
    padding-left: 16px;
    font-size: 13px;

    li {
      line-height: 1.8;
      color: #c0392b;
    }
  }
}

.import-preview {
  margin-bottom: 10px;
}

.import-summary {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}

.import-empty {
  padding: 20px 0;
}
</style>