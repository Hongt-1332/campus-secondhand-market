import axios from 'axios'

const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 5000
})

// 请求拦截器
request.interceptors.request.use(config => {
  const userStr = localStorage.getItem('userInfo')
  if (userStr) {
    config.headers.loginUser = userStr
  }
  return config
})

// 响应拦截器（可选，统一处理返回）
request.interceptors.response.use(
  response => response.data,
  err => {
    return Promise.reject(err)
  }
)

export default request