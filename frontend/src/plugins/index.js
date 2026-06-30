import tab from './tab'
import modal from './modal'

export default function installPlugins(app){
  // 页签操作
  app.config.globalProperties.$tab = tab
  // 模态框对象
  app.config.globalProperties.$modal = modal
}