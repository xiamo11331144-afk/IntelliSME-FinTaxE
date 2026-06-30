/**
 * SSE 流式请求封装
 * 使用 fetch + ReadableStream 实现，支持逐 chunk 回调
 *
 * @param {string} url - 请求地址
 * @param {object} body - POST body
 * @param {object} callbacks
 * @param {function} callbacks.onChunk - 收到一段文本时回调 (text)
 * @param {function} callbacks.onDone - 流结束时回调 (fullText)
 * @param {function} callbacks.onError - 出错时回调 (error)
 * @returns {{ abort: () => void }} 可中止的控制器
 */
export function streamChat(url, body, { onChunk, onDone, onError }) {
  const controller = new AbortController()
  const baseURL = import.meta.env.VITE_APP_BASE_API

  fetch(baseURL + url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + getToken(),
      'Accept': 'text/event-stream'
    },
    body: JSON.stringify(body),
    signal: controller.signal
  })
    .then(async response => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`)
      }

      // 检查是否为 SSE 流
      const contentType = response.headers.get('content-type') || ''
      if (!contentType.includes('text/event-stream') && !contentType.includes('text/plain')) {
        // 非流式响应，作为普通 JSON 读取
        const data = await response.json()
        const answer = data?.data?.answer || data?.answer || ''
        if (answer) onChunk(answer)
        onDone(answer, data)
        return
      }

      // 流式读取
      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let fullText = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        const chunk = decoder.decode(value, { stream: true })
        // 解析 SSE 格式: data: {...}\n\n
        const lines = chunk.split('\n')

        for (const line of lines) {
          if (line.startsWith('data:')) {
            const payload = line.slice(5).trim()
            if (payload === '[DONE]') continue

            try {
              const data = JSON.parse(payload)
              const text = data.content || data.text || data.delta?.content || ''
              if (text) {
                fullText += text
                onChunk(fullText)
              }
              if (data.sessionId) {
                fullText._sessionId = data.sessionId
              }
            } catch {
              // 非 JSON，当作纯文本 chunk
              if (payload) {
                fullText += payload
                onChunk(fullText)
              }
            }
          }
        }
      }

      onDone(fullText)
    })
    .catch(err => {
      if (err.name !== 'AbortError') {
        onError(err)
      }
    })

  return { abort: () => controller.abort() }
}

function getToken() {
  const match = document.cookie.match(/Admin-Token=([^;]+)/)
  return match ? match[1] : ''
}