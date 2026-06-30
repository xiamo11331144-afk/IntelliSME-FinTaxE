import request from '@/utils/request'

export function listCompanies() {
  return request({
    url: '/aifc/company/list',
    method: 'get',
    params: { pageSize: 500 }
  })
}