import request from '@/utils/request'

export function getAnalysisSummary(params) {
  return request({
    url: '/aifc/analysis/summary',
    method: 'get',
    params
  })
}

export function getAnalysisTrend(params) {
  return request({
    url: '/aifc/analysis/trend',
    method: 'get',
    params
  })
}

