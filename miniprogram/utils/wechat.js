// 简易微信/uni请求封装
const baseURL = (process && process.env && process.env.VITE_API_BASE) || '/api'

function request(method, url, data) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: baseURL + url,
      method,
      data,
      header: buildHeaders(),
      success: (res) => {
        const body = res.data
        if (body && typeof body.code === 'number' && body.code !== 0) {
          reject(new Error(body.message || '请求失败'))
        } else {
          resolve(body && body.data !== undefined ? body.data : body)
        }
      },
      fail: (err) => reject(err),
    })
  })
}

function buildHeaders() {
  const token = uni.getStorageSync('token')
  const headers = { 'Content-Type': 'application/json' }
  if (token) headers['Authorization'] = 'Bearer ' + token
  return headers
}

export default {
  get: (url, params) => request('GET', url, params),
  post: (url, data) => request('POST', url, data),
}

