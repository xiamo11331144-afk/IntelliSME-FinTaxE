import request from '@/utils/request'

export function listInvoice(query) {
  return request({
    url: '/aifc/invoice/list',
    method: 'get',
    params: query
  })
}

export function addInvoice(data) {
  return request({
    url: '/aifc/invoice',
    method: 'post',
    data
  })
}

export function recognizeInvoice(data) {
  return request({
    url: '/aifc/invoice/recognize',
    method: 'post',
    data
  })
}

export function confirmInvoice(id, data) {
  return request({
    url: '/aifc/invoice/' + id + '/confirm',
    method: 'post',
    data
  })
}

export function removeInvoice(ids) {
  return request({
    url: '/aifc/invoice/' + ids,
    method: 'delete'
  })
}
