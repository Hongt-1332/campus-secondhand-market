import request from '../utils/request'

// 查询商品列表
export const getProductList = (name) => {
  return request.get('/product/list', { params: { name } })
}

// 查询详情
export const getProductDetail = (id) => {
  return request.get(`/product/${id}`)
}

// 新增商品
export const addProduct = (data) => {
  return request.post('/product', data)
}

// 修改商品
export const updateProduct = (data) => {
  return request.put('/product', data)
}

// 删除商品
export const deleteProduct = (id) => {
  return request.delete(`/product/${id}`)
}