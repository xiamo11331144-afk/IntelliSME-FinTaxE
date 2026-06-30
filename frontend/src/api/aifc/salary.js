import request from '@/utils/request'

export function listSalary(query) {
  return request({
    url: '/aifc/salary/list',
    method: 'get',
    params: query
  })
}

export function addSalary(data) {
  return request({
    url: '/aifc/salary',
    method: 'post',
    data
  })
}

export function updateSalary(data) {
  return request({
    url: '/aifc/salary',
    method: 'put',
    data
  })
}

export function removeSalary(ids) {
  return request({
    url: '/aifc/salary/' + ids,
    method: 'delete'
  })
}

export function batchAddSalary(data) {
  return request({
    url: '/aifc/salary/batch',
    method: 'post',
    data
  })
}
