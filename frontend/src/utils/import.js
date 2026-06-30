import * as XLSX from 'xlsx'

/**
 * 从 Excel/CSV 文件解析数据
 * @param {File} file - 上传的文件对象
 * @param {object} schema - 字段定义 { field: { label, required, type?, validator? } }
 * @returns {Promise<{ data: object[], errors: object[] }>}
 */
export function parseExcel(file, schema) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()

    reader.onload = (e) => {
      try {
        const workbook = XLSX.read(e.target.result, { type: 'array' })
        const sheet = workbook.Sheets[workbook.SheetNames[0]]
        const rawRows = XLSX.utils.sheet_to_json(sheet, { defval: '' })

        const headerMap = buildHeaderMap(schema)
        const data = []
        const errors = []

        rawRows.forEach((raw, idx) => {
          const row = {}
          const rowNum = idx + 2 // Excel 行号（第 1 行是表头）

          // 按列标题映射字段
          for (const [header, value] of Object.entries(raw)) {
            const field = headerMap[header.trim()]
            if (field) {
              row[field] = value
            }
          }

          // 校验
          const rowErrors = validateRow(row, schema, rowNum)
          if (rowErrors.length) {
            errors.push(...rowErrors)
          } else {
            data.push(row)
          }
        })

        resolve({ data, errors })
      } catch (err) {
        reject(new Error('文件解析失败：' + err.message))
      }
    }

    reader.onerror = () => reject(new Error('文件读取失败'))
    reader.readAsArrayBuffer(file)
  })
}

/**
 * 生成并下载导入模板
 * @param {string} filename - 模板文件名（不含扩展名）
 * @param {object} schema - 字段定义
 */
export function downloadTemplate(filename, schema) {
  const headers = Object.values(schema).map((s) => s.label)
  const exampleRow = Object.values(schema).map((s) => s.example ?? '')

  const ws = XLSX.utils.aoa_to_sheet([headers, exampleRow])

  // 自动列宽
  ws['!cols'] = headers.map((h, i) => {
    const w = Math.max(h.length * 2, String(exampleRow[i]).length * 1.5, 10)
    return { wch: Math.min(w, 30) }
  })

  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, 'Sheet1')
  XLSX.writeFile(wb, `${filename}.xlsx`)
}

// ── 内部辅助 ──

/**
 * 构建「列标题 → 字段名」映射，同时支持原生字段名
 */
function buildHeaderMap(schema) {
  const map = {}
  for (const [field, conf] of Object.entries(schema)) {
    if (conf.label) map[conf.label] = field
    map[field] = field // 兼容英文表头
  }
  return map
}

/**
 * 校验单行数据
 */
function validateRow(row, schema, rowNum) {
  const errors = []

  for (const [field, conf] of Object.entries(schema)) {
    const value = row[field]

    // 必填校验
    if (conf.required && (value === undefined || value === null || value === '')) {
      errors.push({ row: rowNum, field, message: `第 ${rowNum} 行「${conf.label}」不能为空` })
      continue
    }

    if (value === undefined || value === null || value === '') continue

    // 类型转换与校验
    if (conf.type === 'number') {
      const num = Number(value)
      if (Number.isNaN(num)) {
        errors.push({ row: rowNum, field, message: `第 ${rowNum} 行「${conf.label}」必须为数字` })
      } else {
        row[field] = num
      }
    }

    if (conf.type === 'date' || conf.type === 'month') {
      // Excel 日期可能是序列号或字符串
      if (typeof value === 'number') {
        const d = XLSX.SSF.parse_date_code(value)
        row[field] = `${d.y}-${String(d.m).padStart(2, '0')}`
      }
      // 字符串格式校验
      if (conf.type === 'month' && !/^\d{4}-\d{2}$/.test(row[field])) {
        errors.push({ row: rowNum, field, message: `第 ${rowNum} 行「${conf.label}」格式应为 YYYY-MM` })
      }
    }

    // 自定义校验
    if (conf.validator) {
      const msg = conf.validator(row[field], row)
      if (msg) {
        errors.push({ row: rowNum, field, message: `第 ${rowNum} 行${msg}` })
      }
    }
  }

  return errors
}