import request from '@/utils/request'
import { streamChat } from '@/utils/sse'

export function chatAssistant(data) {
  return request({
    url: '/aifc/ai/chat',
    method: 'post',
    data,
    timeout: 60000
  })
}

/**
 * 流式对话（SSE），后端不支持时自动回退到普通请求
 * @returns {{ abort: () => void }}
 */
export function chatAssistantStream(data, callbacks) {
  return streamChat('/aifc/ai/chat', data, callbacks)
}

export function listAssistantSessions() {
  return request({
    url: '/aifc/ai/sessions',
    method: 'get'
  })
}

export function createAssistantSession(data) {
  return request({
    url: '/aifc/ai/sessions',
    method: 'post',
    data
  })
}

export function listAssistantMessages(sessionId) {
  return request({
    url: `/aifc/ai/sessions/${sessionId}/messages`,
    method: 'get'
  })
}

export function deleteAssistantSession(sessionId) {
  return request({
    url: `/aifc/ai/sessions/${sessionId}`,
    method: 'delete'
  })
}

export function clearAllAssistantSessions() {
  return request({
    url: '/aifc/ai/sessions/clear-all',
    method: 'delete'
  })
}
