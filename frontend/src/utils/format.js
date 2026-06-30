/**
 * 金额格式化（千分位 + 两位小数）
 * @param {number|string} value
 * @returns {string}
 */
export function formatMoney(value) {
  const n = Number(value || 0)
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/**
 * 百分比格式化（小数 → 百分比字符串）
 * @param {number|string} value  0~1 之间的小数
 * @returns {string}
 */
export function formatPercent(value) {
  const n = Number(value || 0) * 100
  return `${n.toFixed(2)}%`
}