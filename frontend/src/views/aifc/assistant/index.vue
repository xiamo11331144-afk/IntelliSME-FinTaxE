<template>
  <div class="app-container assistant-page">
    <el-card class="assistant-card" shadow="never">
      <template #header>
        <div class="header-row">
          <span class="title">AIFC AI 助手</span>
          <div class="header-actions">
            <el-button type="primary" plain @click="handleCreateSession">新建会话</el-button>
            <el-button type="danger" plain @click="handleClearAllSessions" :disabled="!sessions.length">清除所有会话</el-button>
          </div>
        </div>
      </template>

      <div class="assistant-layout">
        <aside class="session-panel">
          <div class="session-title">会话历史</div>
          <el-scrollbar class="session-scroll">
            <div
              v-for="item in sessions"
              :key="item.id"
              class="session-item"
              :class="{ active: activeSessionId === item.id }"
              @click="switchSession(item)"
            >
              <div class="session-item-content">
                <div class="session-item-title">{{ item.title || `会话 ${item.id}` }}</div>
                <div class="session-item-time">{{ formatTime(item.lastChatTime || item.updateTime || item.createTime) }}</div>
                <div class="session-item-preview">{{ item.lastMessage || '暂无消息' }}</div>
              </div>
              <el-icon class="session-item-delete" @click.stop="handleDeleteSession(item.id)">
                <Close />
              </el-icon>
            </div>
            <el-empty v-if="!sessions.length" description="暂无会话" :image-size="80" />
          </el-scrollbar>
        </aside>

        <section class="chat-panel">
          <el-form :model="form" label-width="110px" class="prompt-form">
            <el-form-item label="系统提示语">
              <el-input
                v-model="form.systemPrompt"
                type="textarea"
                :rows="2"
                placeholder="可选。留空则使用后端默认值。"
              />
            </el-form-item>
          </el-form>

          <div class="message-area" ref="messageAreaRef">
            <el-scrollbar ref="messageScrollRef" class="message-scroll">
              <div v-if="messages.length" class="message-list">
                <div
                  v-for="(item, index) in messages"
                  :key="item.id || index"
                  class="message-item"
                  :class="item.role"
                >
                  <div class="message-role">{{ item.role === 'user' ? '我' : '助手' }}</div>
                  <!-- 助手消息：Markdown 渲染 -->
                  <div
                    v-if="item.role === 'assistant'"
                    class="message-content md-body"
                    v-html="renderMarkdown(item.content)"
                  />
                  <!-- 用户消息：纯文本 -->
                  <div v-else class="message-content">{{ item.content }}</div>
                  <div class="message-time">{{ formatTime(item.createTime) }}</div>
                </div>
              </div>
              <el-empty v-else description="开始新的对话" :image-size="90" />
            </el-scrollbar>
          </div>

          <!-- AI 思考中提示 -->
          <div v-if="loading" class="thinking-tip">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>{{ streaming ? 'AI 正在回复...' : 'AI 正在思考...' }}</span>
          </div>

          <!-- 附件预览 -->
          <div v-if="attachments.length" class="attachment-bar">
            <div v-for="(file, i) in attachments" :key="i" class="attachment-tag">
              <el-icon><Document /></el-icon>
              <span class="attachment-name">{{ file.name }}</span>
              <el-icon class="attachment-remove" @click="removeAttachment(i)"><Close /></el-icon>
            </div>
          </div>

          <div class="composer">
            <el-input
              v-model="form.userPrompt"
              type="textarea"
              :rows="4"
              maxlength="3000"
              show-word-limit
              placeholder="输入您的问题，支持粘贴或拖拽附件"
              @keyup.ctrl.enter="sendChat"
              @paste="handlePaste"
              @drop.prevent="handleDrop"
            />
            <div class="composer-actions">
              <div class="composer-left">
                <span class="tip">Ctrl + Enter 发送</span>
                <el-upload
                  ref="fileUploadRef"
                  :auto-upload="false"
                  :show-file-list="false"
                  :on-change="handleFileSelect"
                  :multiple="true"
                  :accept="fileAccept"
                >
                  <el-button text type="primary" icon="Paperclip" size="small">附件</el-button>
                </el-upload>
              </div>
              <el-button type="primary" :loading="loading" @click="sendChat">发送</el-button>
            </div>
          </div>
        </section>
      </div>
    </el-card>
  </div>
</template>

<script setup name="AifcAssistant">
import { nextTick, ref, reactive, onMounted, onBeforeUnmount, getCurrentInstance } from 'vue'
import { Close, Loading, Document } from '@element-plus/icons-vue'
import { chatAssistant, chatAssistantStream, listAssistantSessions, createAssistantSession, listAssistantMessages, deleteAssistantSession, clearAllAssistantSessions } from '@/api/aifc/assistant'
import { renderMarkdown, getCodeBlockText } from '@/utils/markdown'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const streaming = ref(false)
const sessions = ref([])
const messages = ref([])
const activeSessionId = ref(null)
const messageScrollRef = ref()
const messageAreaRef = ref()
const fileUploadRef = ref()
const attachments = ref([])
let currentStream = null

const fileAccept = '.txt,.pdf,.doc,.docx,.xls,.xlsx,.csv,.json,.md,.png,.jpg,.jpeg,.gif,.webp'
const maxFileSize = 10 * 1024 * 1024 // 10MB

const form = reactive({
  systemPrompt: '你是一个专业的财务和税务顾问。请用简洁的中文回复。',
  userPrompt: ''
})

onMounted(async () => {
  await loadSessions()
  // 事件委托：代码块复制按钮
  messageAreaRef.value?.addEventListener('click', handleCopyClick)
})

onBeforeUnmount(() => {
  currentStream?.abort()
  messageAreaRef.value?.removeEventListener('click', handleCopyClick)
})

// ── 代码块复制 ──

function handleCopyClick(e) {
  const btn = e.target.closest('.md-copy-btn')
  if (!btn) return

  const idx = Number(btn.dataset.codeIdx)
  if (Number.isNaN(idx)) return

  // 从 messages 中找到最近一条助手消息的原始文本
  const lastAssistant = [...messages.value].reverse().find(m => m.role === 'assistant')
  if (!lastAssistant) return

  const code = getCodeBlockText(lastAssistant.content, idx)
  if (!code) return

  const decoded = decodeEntities(code)
  navigator.clipboard.writeText(decoded).then(() => {
    const original = btn.textContent
    btn.textContent = '已复制!'
    btn.classList.add('copied')
    setTimeout(() => {
      btn.textContent = original
      btn.classList.remove('copied')
    }, 2000)
  }).catch(() => {
    ElMessage.warning('复制失败，请手动选择复制')
  })
}

function decodeEntities(str) {
  const el = document.createElement('textarea')
  el.innerHTML = str
  return el.value
}

// ── 会话管理 ──

async function loadSessions(keepCurrent = true) {
  const current = keepCurrent ? activeSessionId.value : null
  const res = await listAssistantSessions()
  sessions.value = Array.isArray(res.data) ? res.data : []
  if (!sessions.value.length) {
    activeSessionId.value = null
    messages.value = []
    return
  }

  if (current && sessions.value.some((it) => it.id === current)) {
    activeSessionId.value = current
  } else {
    activeSessionId.value = sessions.value[0].id
  }
  await loadMessages(activeSessionId.value)
}

async function loadMessages(sessionId) {
  if (!sessionId) {
    messages.value = []
    return
  }
  const res = await listAssistantMessages(sessionId)
  messages.value = Array.isArray(res.data) ? res.data : []
  await scrollToBottom()
}

async function switchSession(item) {
  if (!item?.id || item.id === activeSessionId.value) {
    return
  }
  activeSessionId.value = item.id
  form.userPrompt = ''
  form.systemPrompt = item.systemPrompt || form.systemPrompt
  attachments.value = []
  await loadMessages(item.id)
}

async function handleCreateSession() {
  const res = await createAssistantSession({
    title: 'New Chat',
    systemPrompt: form.systemPrompt
  })
  const sessionId = res?.data?.id
  await loadSessions(false)
  if (sessionId) {
    activeSessionId.value = sessionId
    await loadMessages(sessionId)
  }
}

async function handleDeleteSession(sessionId) {
  try {
    await proxy.$modal.confirm('是否要删除该会话？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteAssistantSession(sessionId)
    proxy.$modal.msgSuccess('会话已删除')
    await loadSessions(false)
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    proxy.$modal.msgError(error?.message || '删除失败，请稍后重试')
  }
}

async function handleClearAllSessions() {
  try {
    await proxy.$modal.confirm('此操作将删除所有会话，是否继续？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await clearAllAssistantSessions()
    proxy.$modal.msgSuccess('所有会话已清除')
    await loadSessions(false)
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    proxy.$modal.msgError(error?.message || '清除失败，请稍后重试')
  }
}

// ── 文件附件 ──

function handleFileSelect(uploadFile) {
  addFile(uploadFile.raw)
}

function handlePaste(e) {
  const items = e.clipboardData?.items
  if (!items) return
  for (const item of items) {
    if (item.kind === 'file') {
      e.preventDefault()
      addFile(item.getAsFile())
    }
  }
}

function handleDrop(e) {
  const files = e.dataTransfer?.files
  if (!files) return
  for (const file of files) {
    addFile(file)
  }
}

function addFile(file) {
  if (!file) return
  if (file.size > maxFileSize) {
    ElMessage.warning(`文件「${file.name}」超过 10MB 限制`)
    return
  }
  attachments.value.push(file)
}

function removeAttachment(index) {
  attachments.value.splice(index, 1)
}

// ── 发送消息 ──

async function sendChat() {
  const question = (form.userPrompt || '').trim()
  if (!question && !attachments.value.length) {
    proxy.$modal.msgWarning('请输入问题')
    return
  }
  if (loading.value) return

  loading.value = true
  streaming.value = false
  const optimisticIndex = messages.value.length

  try {
    // 构建用户消息文本（附件信息附加到文本尾部）
    let displayText = question
    if (attachments.value.length) {
      const fileList = attachments.value.map(f => `[附件: ${f.name}]`).join(' ')
      displayText = question ? `${question}\n${fileList}` : fileList
    }

    messages.value.push({ role: 'user', content: displayText, createTime: new Date() })

    // 构建请求参数
    const payload = {
      sessionId: activeSessionId.value,
      systemPrompt: form.systemPrompt,
      userPrompt: question
    }

    // 将附件文件名信息附加到 userPrompt（后端可据此处理）
    if (attachments.value.length) {
      payload.attachments = attachments.value.map(f => ({
        name: f.name,
        size: f.size,
        type: f.type
      }))
    }

    const sentAttachments = [...attachments.value]
    form.userPrompt = ''
    attachments.value = []
    await scrollToBottom()

    // 尝试 SSE 流式输出
    const fullText = await new Promise((resolve, reject) => {
      let resolved = false
      let assistantIdx = -1

      currentStream = chatAssistantStream(payload, {
        onChunk(text) {
          if (!streaming.value) {
            streaming.value = true
            assistantIdx = messages.value.length
            messages.value.push({ role: 'assistant', content: text, createTime: new Date() })
          } else {
            messages.value[assistantIdx].content = text
          }
          scrollToBottom()
        },
        onDone(text, data) {
          resolved = true
          const sessionId = data?.data?.sessionId || text?._sessionId
          if (sessionId) activeSessionId.value = sessionId
          resolve(text)
        },
        onError(err) {
          if (!resolved) {
            resolved = true
            reject(err)
          }
        }
      })
    })

    if (!fullText && !streaming.value) {
      const res = await chatAssistant(payload)
      const answer = res?.data?.answer || ''
      if (!answer) {
        messages.value.splice(optimisticIndex)
        proxy.$modal.msgWarning(res?.msg || '助手返回空响应')
        return
      }
      activeSessionId.value = res?.data?.sessionId || activeSessionId.value
      messages.value.push({ role: 'assistant', content: answer, createTime: new Date() })
    }

    await scrollToBottom()
    await loadSessions(true)
  } catch (error) {
    messages.value.splice(optimisticIndex)
    proxy.$modal.msgError(error?.message || '发送失败，请稍后重试')
  } finally {
    loading.value = false
    streaming.value = false
    currentStream = null
  }
}

// ── 工具函数 ──

function formatTime(v) {
  if (!v) return ''
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return `${v}`
  const y = d.getFullYear()
  const m = `${d.getMonth() + 1}`.padStart(2, '0')
  const day = `${d.getDate()}`.padStart(2, '0')
  const h = `${d.getHours()}`.padStart(2, '0')
  const mm = `${d.getMinutes()}`.padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${mm}`
}

async function scrollToBottom() {
  await nextTick()
  const wrap = messageScrollRef.value?.wrapRef
  if (wrap) {
    wrap.scrollTop = wrap.scrollHeight
  }
}
</script>

<style lang="scss" scoped>
.assistant-page {
  .assistant-card {
    min-height: calc(100vh - 140px);
  }

  .header-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .header-actions {
    display: flex;
    gap: 8px;
  }

  .title {
    font-weight: 600;
    color: #1f2d3d;
  }

  .assistant-layout {
    display: grid;
    grid-template-columns: 280px 1fr;
    gap: 14px;
    min-height: calc(100vh - 240px);
  }

  .session-panel {
    border: 1px solid #e6ebf5;
    border-radius: 10px;
    background: #f8fafd;
    overflow: hidden;
  }

  .session-title {
    padding: 12px 14px;
    font-size: 14px;
    font-weight: 600;
    border-bottom: 1px solid #e7ecf6;
  }

  .session-scroll {
    height: calc(100vh - 320px);
  }

  .session-item {
    border-bottom: 1px solid #eef1f6;
    padding: 12px 14px;
    cursor: pointer;
    transition: background-color 0.2s;
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 8px;
  }

  .session-item:hover {
    background: #eef5ff;
  }

  .session-item.active {
    background: #e6f2ff;
  }

  .session-item-content {
    flex: 1;
    min-width: 0;
  }

  .session-item-delete {
    font-size: 16px;
    color: #909399;
    cursor: pointer;
    flex-shrink: 0;
    transition: color 0.2s;
  }

  .session-item:hover .session-item-delete {
    color: #f56c6c;
  }

  .session-item-title {
    font-weight: 600;
    color: #303133;
    margin-bottom: 4px;
  }

  .session-item-time {
    color: #909399;
    font-size: 12px;
    margin-bottom: 4px;
  }

  .session-item-preview {
    color: #606266;
    font-size: 12px;
    line-height: 1.4;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .chat-panel {
    border: 1px solid #e6ebf5;
    border-radius: 10px;
    display: flex;
    flex-direction: column;
    padding: 12px;
    background: #fff;
  }

  .prompt-form {
    margin-bottom: 6px;
  }

  .message-area {
    flex: 1;
    border: 1px dashed #dbe5f2;
    border-radius: 10px;
    padding: 12px;
    margin-bottom: 12px;
    min-height: 280px;
    background: linear-gradient(180deg, #f8fbff 0%, #ffffff 70%);
  }

  .message-scroll {
    height: calc(100vh - 560px);
    min-height: 200px;
  }

  .message-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .message-item {
    max-width: 85%;
    border-radius: 10px;
    padding: 10px 12px;
    line-height: 1.6;
    word-break: break-word;
  }

  .message-item.user {
    align-self: flex-end;
    background: #e6f2ff;
    border: 1px solid #d2e7ff;
  }

  .message-item.assistant {
    align-self: flex-start;
    background: #f6f8fb;
    border: 1px solid #e7ecf3;
  }

  .message-role {
    font-size: 12px;
    color: #909399;
    margin-bottom: 4px;
  }

  .message-content {
    white-space: pre-wrap;
    color: #303133;
  }

  .message-time {
    margin-top: 4px;
    font-size: 12px;
    color: #a8abb2;
  }

  .composer {
    border-top: 1px solid #edf1f6;
    padding-top: 10px;
  }

  .thinking-tip {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px 0 10px;
    font-size: 13px;
    color: #909399;
  }

  .attachment-bar {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 6px 0 10px;
  }

  .attachment-tag {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    background: #f0f5ff;
    border: 1px solid #d6e4ff;
    border-radius: 6px;
    font-size: 12px;
    color: #409eff;

    .attachment-name {
      max-width: 180px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .attachment-remove {
      cursor: pointer;
      color: #909399;
      margin-left: 2px;
      transition: color 0.2s;

      &:hover {
        color: #f56c6c;
      }
    }
  }

  .composer-actions {
    margin-top: 8px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .composer-left {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .tip {
    color: #909399;
    font-size: 12px;
  }

  /* Markdown 渲染样式 */
  .md-body {
    white-space: normal;

    :deep(h1), :deep(h2), :deep(h3), :deep(h4) {
      margin: 8px 0 4px;
      font-weight: 600;
      color: #1f2d3d;
    }
    :deep(h1) { font-size: 18px; }
    :deep(h2) { font-size: 16px; }
    :deep(h3) { font-size: 15px; }
    :deep(h4) { font-size: 14px; }

    :deep(strong) { font-weight: 600; }
    :deep(em) { font-style: italic; }

    :deep(.md-code-block) {
      background: #1e1e1e;
      color: #d4d4d4;
      border-radius: 8px;
      overflow: hidden;
      margin: 8px 0;

      code {
        display: block;
        padding: 12px 14px;
        overflow-x: auto;
        font-size: 13px;
        line-height: 1.6;
      }
    }

    :deep(.md-code-header) {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 6px 14px;
      background: #2d2d2d;
      border-bottom: 1px solid #3e3e3e;

      .md-code-lang {
        font-size: 12px;
        color: #a0a0a0;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }

      .md-copy-btn {
        background: transparent;
        border: 1px solid #555;
        color: #ccc;
        font-size: 12px;
        padding: 2px 10px;
        border-radius: 4px;
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          background: #409eff;
          border-color: #409eff;
          color: #fff;
        }

        &.copied {
          background: #67c23a;
          border-color: #67c23a;
          color: #fff;
        }
      }
    }

    :deep(.md-inline-code) {
      background: #f0f0f0;
      color: #c7254e;
      padding: 1px 4px;
      border-radius: 3px;
      font-size: 13px;
    }

    :deep(.md-table) {
      border-collapse: collapse;
      margin: 6px 0;
      width: 100%;
      font-size: 13px;
    }
    :deep(.md-table th),
    :deep(.md-table td) {
      border: 1px solid #dfe2e5;
      padding: 6px 10px;
      text-align: left;
    }
    :deep(.md-table th) {
      background: #f6f8fa;
      font-weight: 600;
    }

    :deep(ol), :deep(ul) {
      padding-left: 20px;
      margin: 4px 0;
    }
    :deep(li) { margin: 2px 0; }

    :deep(a) {
      color: #409eff;
      text-decoration: none;
      &:hover { text-decoration: underline; }
    }

    :deep(.md-hr) {
      border: none;
      border-top: 1px solid #e4e7ed;
      margin: 8px 0;
    }
  }
}

@media (max-width: 960px) {
  .assistant-page .assistant-layout {
    grid-template-columns: 1fr;
  }

  .assistant-page .session-scroll {
    height: 220px;
  }
}
</style>