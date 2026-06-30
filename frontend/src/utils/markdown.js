/**
 * 轻量级 Markdown → HTML 转换器
 * 支持：标题、粗体、斜体、行内代码、代码块（含复制按钮）、链接、列表、表格、换行
 */
export function renderMarkdown(text) {
  if (!text) return ''

  // 先提取代码块，避免内部被其他规则误处理
  const codeBlocks = []
  let html = text.replace(/```(\w*)\n([\s\S]*?)```/g, (_, lang, code) => {
    const idx = codeBlocks.length
    codeBlocks.push({ lang, code: code.replace(/\n$/, '') })
    return `%%CODE_BLOCK_${idx}%%`
  })

  html = escapeHtml(html)

  // 恢复代码块（escape 已在提取前跳过）
  html = html.replace(/%%CODE_BLOCK_(\d+)%%/g, (_, idx) => {
    const { lang, code } = codeBlocks[Number(idx)]
    const escapedCode = escapeHtml(code)
    // data-code 存储原始文本供复制按钮使用（已 escape，JS 端需 decodeEntities）
    const copyLabel = lang ? `${lang} · 复制` : '复制'
    return `<pre class="md-code-block"><div class="md-code-header"><span class="md-code-lang">${lang || 'code'}</span><button class="md-copy-btn" data-code-idx="${idx}">${copyLabel}</button></div><code>${escapedCode}</code></pre>`
  })

  // 行内代码
  html = html.replace(/`([^`]+)`/g, '<code class="md-inline-code">$1</code>')

  // 表格
  html = html.replace(/^(\|.+)\n(\|[-: |]+)\n((?:\|.+\n?)*)/gm, (_, header, sep, body) => {
    const heads = header.split('|').filter(c => c.trim()).map(c => `<th>${c.trim()}</th>`).join('')
    const rows = body.trim().split('\n').map(row => {
      const cells = row.split('|').filter(c => c.trim()).map(c => `<td>${c.trim()}</td>`).join('')
      return `<tr>${cells}</tr>`
    }).join('')
    return `<table class="md-table"><thead><tr>${heads}</tr></thead><tbody>${rows}</tbody></table>`
  })

  // 标题 (h1-h4)
  html = html.replace(/^#### (.+)$/gm, '<h4>$1</h4>')
  html = html.replace(/^### (.+)$/gm, '<h3>$1</h3>')
  html = html.replace(/^## (.+)$/gm, '<h2>$1</h2>')
  html = html.replace(/^# (.+)$/gm, '<h1>$1</h1>')

  // 粗体 + 斜体
  html = html.replace(/\*\*\*(.+?)\*\*\*/g, '<strong><em>$1</em></strong>')
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/\*(.+?)\*/g, '<em>$1</em>')

  // 有序列表
  html = html.replace(/^\d+\. (.+)$/gm, '<li>$1</li>')
  html = html.replace(/((?:<li>.*<\/li>\n?)+)/g, '<ol>$1</ol>')

  // 无序列表
  html = html.replace(/^[-*] (.+)$/gm, '<li>$1</li>')
  html = html.replace(/(?!<\/ol>)((?:<li>.*<\/li>\n?)+)(?!<\/ol>)/g, '<ul>$1</ul>')

  // 链接
  html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener">$1</a>')

  // 分割线
  html = html.replace(/^---+$/gm, '<hr class="md-hr" />')

  // 换行
  html = html.replace(/\n/g, '<br>')

  // 清理多余的 <br> 紧跟块级标签
  html = html.replace(/(<\/(?:h[1-6]|pre|table|ol|ul|hr)>)<br>/g, '$1')
  html = html.replace(/(<br>)(<(?:h[1-6]|pre|table|ol|ul|hr))/g, '$2')

  return html
}

/**
 * 获取代码块原始文本（供复制按钮使用）
 * @param {string} text - 原始 Markdown 文本
 * @param {number} idx - 代码块索引
 * @returns {string}
 */
export function getCodeBlockText(text, idx) {
  const blocks = []
  text.replace(/```(\w*)\n([\s\S]*?)```/g, (_, lang, code) => {
    blocks.push(code.replace(/\n$/, ''))
    return ''
  })
  return blocks[idx] || ''
}

function escapeHtml(str) {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
}