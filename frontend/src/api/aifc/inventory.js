import request from '@/utils/request'

export function listInventory(query) {
  return request({
    url: '/aifc/inventory/list',
    method: 'get',
    params: query
  })
}

export function addInventory(data) {
  return request({
    url: '/aifc/inventory',
    method: 'post',
    data
  })
}

export function updateInventory(data) {
  return request({
    url: '/aifc/inventory',
    method: 'put',
    data
  })
}

export function removeInventory(ids) {
  return request({
    url: '/aifc/inventory/' + ids,
    method: 'delete'
  })
}

export function batchAddInventory(data) {
  return request({
    url: '/aifc/inventory/batch',
    method: 'post',
    data
  })
}
