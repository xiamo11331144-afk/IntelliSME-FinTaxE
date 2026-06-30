/**
 * 将数据导出为 CSV 文件
 * @param {string} filename - 文件名（不含扩展名）
 * @param {string[]} headers - 列标题
 * @param {string[]} keys - 对应的数据字段名
 * @param {object[]} data - 数据数组
 */
export function exportCsv(filename, headers, keys, data) {
  const BOM = '\uFEFF'
  const rows = [headers.join(',')]

  data.forEach(item => {
    const cells = keys.map(key => {
      const val = item[key] ?? ''
      // CSV 转义：含逗号/引号/换行的字段用双引号包裹
      const str = String(val)
      if (str.includes(',') || str.includes('"') || str.includes('\n')) {
        return `"${str.replace(/"/g, '""')}"`
      }
      return str
    })
    rows.push(cells.join(','))
  })

  const csv = BOM + rows.join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })

  // 动态导入 file-saver（避免 SSR 问题）
  import('file-saver').then(({ saveAs }) => {
    saveAs(blob, `${filename}.csv`)
  })
}

/**
 * 通用表格导出快捷方法
 * @param {object} options
 * @param {string} options.filename
 * @param {Array<{label: string, prop: string}>} options.columns
 * @param {object[]} options.data
 */
export function exportTable({ filename, columns, data }) {
  const headers = columns.map(c => c.label)
  const keys = columns.map(c => c.prop)
  exportCsv(filename, headers, keys, data)
}