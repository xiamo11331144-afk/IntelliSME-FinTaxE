<template>
  <div class="navbar">
    <hamburger id="hamburger-container" :is-active="appStore.sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />
    <breadcrumb id="breadcrumb-container" class="breadcrumb-container" />

    <div class="right-menu">
      <template v-if="appStore.device !== 'mobile'">
        <!-- 企业切换器 -->
        <el-dropdown trigger="click" class="right-menu-item hover-effect company-switch" @command="handleCompanySwitch">
          <div class="company-wrapper">
            <el-icon><OfficeBuilding /></el-icon>
            <span class="company-name">{{ companyStore.currentName }}</span>
            <el-icon class="company-arrow"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item :command="null" :class="{ 'is-active': !companyStore.companyId }">全部企业</el-dropdown-item>
              <el-dropdown-item
                v-for="c in companyStore.list"
                :key="c.id || c.companyId"
                :command="c.id || c.companyId"
                :class="{ 'is-active': companyStore.companyId === (c.id || c.companyId) }"
              >{{ c.name || c.companyName || `企业 ${c.id || c.companyId}` }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <header-search id="header-search" class="right-menu-item" />

        <screenfull id="screenfull" class="right-menu-item hover-effect" />

        <el-tooltip content="主题模式" effect="dark" placement="bottom">
          <div class="right-menu-item hover-effect theme-switch-wrapper" @click="toggleTheme">
            <svg-icon v-if="settingsStore.isDark" icon-class="sunny" />
            <svg-icon v-if="!settingsStore.isDark" icon-class="moon" />
          </div>
        </el-tooltip>

        <el-tooltip content="布局大小" effect="dark" placement="bottom">
          <size-select id="size-select" class="right-menu-item hover-effect" />
        </el-tooltip>
      </template>

      <el-dropdown @command="handleCommand" class="avatar-container right-menu-item hover-effect" trigger="hover">
        <div class="avatar-wrapper">
          <img :src="userStore.avatar" class="user-avatar" />
          <span class="user-nickname"> {{ userStore.name || userStore.nickName }} </span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item divided command="logout">
              <span>退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <div class="right-menu-item hover-effect setting" @click="setLayout" v-if="settingsStore.showSettings">
        <svg-icon icon-class="more-up" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ElMessageBox } from 'element-plus'
import Breadcrumb from '@/components/Breadcrumb'
import Hamburger from '@/components/Hamburger'
import Screenfull from '@/components/Screenfull'
import SizeSelect from '@/components/SizeSelect'
import HeaderSearch from '@/components/HeaderSearch'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'
import useSettingsStore from '@/store/modules/settings'
import useCompanyStore from '@/store/modules/company'
import { OfficeBuilding, ArrowDown } from '@element-plus/icons-vue'

const appStore = useAppStore()
const userStore = useUserStore()
const settingsStore = useSettingsStore()
const companyStore = useCompanyStore()

onMounted(() => {
  companyStore.loadCompanies()
})

function handleCompanySwitch(companyId) {
  companyStore.setCompanyId(companyId)
}

function toggleSideBar() {
  appStore.toggleSideBar()
}

function handleCommand(command) {
  switch (command) {
    case "setLayout":
      setLayout()
      break
    case "logout":
      logout()
      break
    default:
      break
  }
}

function logout() {
  ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logOut().then(() => {
      location.href = '/index'
    })
  }).catch(() => { })
}

const emits = defineEmits(['setLayout'])
function setLayout() {
  emits('setLayout')
}

function toggleTheme() {
  settingsStore.toggleTheme()
}
</script>

<style lang='scss' scoped>
.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  background: var(--navbar-bg);
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);

  .hamburger-container {
    line-height: 46px;
    height: 100%;
    float: left;
    cursor: pointer;
    transition: background 0.3s;
    -webkit-tap-highlight-color: transparent;

    &:hover {
      background: rgba(0, 0, 0, 0.025);
    }
  }

  .breadcrumb-container {
    float: left;
  }

  .topmenu-container {
    position: absolute;
    left: 50px;
  }

  .errLog-container {
    display: inline-block;
    vertical-align: top;
  }

  .right-menu {
    float: right;
    height: 100%;
    line-height: 50px;
    display: flex;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-block;
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: #5a5e66;
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background 0.3s;

        &:hover {
          background: rgba(0, 0, 0, 0.025);
        }
      }

      &.theme-switch-wrapper {
        display: flex;
        align-items: center;

        svg {
          transition: transform 0.3s;
          
          &:hover {
            transform: scale(1.15);
          }
        }
      }
    }

    .company-switch {
      display: inline-flex;
      align-items: center;

      .company-wrapper {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        color: #5a5e66;
        line-height: normal;

        .company-name {
          max-width: 120px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .company-arrow {
          font-size: 12px;
        }
      }
    }

    .avatar-container {
      margin-right: 0px;
      padding-right: 10px;
      display: inline-flex;
      align-items: center;

      .avatar-wrapper {
        display: flex;
        align-items: center;
        gap: 6px;

        .user-avatar {
          cursor: pointer;
          width: 30px;
          height: 30px;
          border-radius: 50%;
        }

        .user-nickname {
          font-size: 14px;
          font-weight: bold;
          color: #5a5e66;
        }

        i {
          cursor: pointer;
          font-size: 12px;
        }
      }
    }
  }
}
</style>
