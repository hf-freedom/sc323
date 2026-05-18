import axios from 'axios'
import { Message } from 'element-ui'

axios.defaults.baseURL = 'http://localhost:8004'
axios.defaults.timeout = 10000

axios.interceptors.request.use(
  config => {
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

axios.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response) {
      Message.error('服务器错误: ' + error.response.status)
    } else if (error.request) {
      Message.error('请求失败，请检查网络连接')
    } else {
      Message.error('请求错误: ' + error.message)
    }
    return Promise.reject(error)
  }
)

export default axios
