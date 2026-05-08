import axios from 'axios'

const api = axios.create({ baseURL: '/api' })

api.interceptors.request.use(config => {
  const token = localStorage.getItem('cinema_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  res => res,
  err => {
    if (err.response && err.response.status === 401) {
      localStorage.removeItem('cinema_token')
      localStorage.removeItem('cinema_username')
      localStorage.removeItem('cinema_role')
    }
    return Promise.reject(err)
  }
)

export default api
