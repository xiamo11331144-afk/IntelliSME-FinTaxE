import request from '@/utils/request'

export function listTaxRisk(query) {
  return request({
    url: '/aifc/taxRisk/list',
    method: 'get',
    params: query
  })
}

export function getTaxRisk(id) {
  return request({
    url: '/aifc/taxRisk/' + id,
    method: 'get'
  })
}

export function scanTaxRisk(companyId, reportMonth) {
  return request({
    url: '/aifc/taxRisk/scan',
    method: 'post',
    params: { companyId, reportMonth }
  })
}

export function scanTaxRiskAuto() {
  return request({
    url: '/aifc/taxRisk/scan/auto',
    method: 'post'
  })
}

export function scanTaxRiskQuick(companyId, reportMonth) {
  return request({
    url: '/aifc/taxRisk/scan/quick',
    method: 'post',
    params: { companyId, reportMonth }
  })
}

export function scanTaxRiskAutoQuick() {
  return request({
    url: '/aifc/taxRisk/scan/auto/quick',
    method: 'post'
  })
}

export function handleTaxRisk(id, data) {
  return request({
    url: '/aifc/taxRisk/' + id + '/handle',
    method: 'post',
    data
  })
}

export function batchHandleTaxRisk(data) {
  return request({
    url: '/aifc/taxRisk/handle/batch',
    method: 'post',
    data
  })
}
