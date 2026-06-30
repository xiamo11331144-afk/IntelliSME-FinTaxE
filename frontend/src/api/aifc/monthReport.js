import request from '@/utils/request'

export function listMonthReport(query) {
  return request({
    url: '/aifc/monthReport/list',
    method: 'get',
    params: query
  })
}

export function getMonthReport(id) {
  return request({
    url: '/aifc/monthReport/' + id,
    method: 'get'
  })
}

export function addMonthReport(data) {
  return request({
    url: '/aifc/monthReport',
    method: 'post',
    data
  })
}

export function updateMonthReport(data) {
  return request({
    url: '/aifc/monthReport',
    method: 'put',
    data
  })
}

export function removeMonthReport(ids) {
  return request({
    url: '/aifc/monthReport/' + ids,
    method: 'delete'
  })
}
