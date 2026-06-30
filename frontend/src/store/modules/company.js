import { listCompanies } from '@/api/aifc/company'

const STORAGE_KEY = 'aifc_company_id'

const useCompanyStore = defineStore('company', {
  state: () => ({
    /** 当前选中企业 ID，null 表示全部 */
    companyId: loadFromStorage(),
    /** 企业列表 [{ id, name, ... }] */
    list: [],
    /** 加载状态 */
    loading: false
  }),

  getters: {
    currentName(state) {
      if (!state.companyId) return '全部企业'
      const item = state.list.find(c => c.id === state.companyId || c.companyId === state.companyId)
      return item?.name || item?.companyName || `企业 ${state.companyId}`
    }
  },

  actions: {
    /** 切换当前企业 */
    setCompanyId(id) {
      this.companyId = id || null
      if (id) {
        localStorage.setItem(STORAGE_KEY, String(id))
      } else {
        localStorage.removeItem(STORAGE_KEY)
      }
    },

    /** 加载企业列表（首次调用后会缓存） */
    async loadCompanies() {
      if (this.list.length) return this.list
      this.loading = true
      try {
        const res = await listCompanies()
        this.list = Array.isArray(res.rows) ? res.rows : Array.isArray(res.data) ? res.data : []
      } catch {
        this.list = []
      } finally {
        this.loading = false
      }
      return this.list
    }
  }
})

function loadFromStorage() {
  const val = localStorage.getItem(STORAGE_KEY)
  return val ? Number(val) : null
}

export default useCompanyStore