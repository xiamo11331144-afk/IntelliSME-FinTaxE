<template>
  <div class="login-container">
    <!-- 卡片浮动层 -->
    <div class="login-card">
      <div class="card-header">
        <h2 class="card-title">{{ title }}</h2>
        <p class="card-subtitle">欢迎登录管理系统</p>
      </div>
      
      <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
        <!-- 账号输入框 -->
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            type="text"
            size="large"
            auto-complete="off"
            placeholder="请输入账号"
            clearable
          >
            <template #prefix><svg-icon icon-class="user" class="input-icon" /></template>
          </el-input>
        </el-form-item>

        <!-- 密码输入框 -->
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            size="large"
            auto-complete="off"
            placeholder="请输入密码"
            show-password
            @keyup.enter="handleLogin"
          >
            <template #prefix><svg-icon icon-class="password" class="input-icon" /></template>
          </el-input>
        </el-form-item>

        <!-- 验证码输入框 -->
        <el-form-item prop="code" v-if="captchaEnabled">
          <div class="captcha-wrapper">
            <el-input
              v-model="loginForm.code"
              size="large"
              auto-complete="off"
              placeholder="请输入验证码"
              @keyup.enter="handleLogin"
            >
              <template #prefix><svg-icon icon-class="validCode" class="input-icon" /></template>
            </el-input>
            <img :src="codeUrl" @click="getCode" class="captcha-img" title="点击刷新验证码"/>
          </div>
        </el-form-item>

        <!-- 记住密码复选框 -->
        <div class="form-footer">
          <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
          <router-link v-if="register" class="forgot-link" :to="'/register'">立即注册</router-link>
        </div>

        <!-- 登录按钮 -->
        <el-form-item style="width: 100%;">
          <el-button
            :loading="loading"
            size="large"
            type="primary"
            class="login-button"
            @click.prevent="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登 录 中...</span>
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 底部版权 -->
    <div class="login-footer">
      <span>Copyright © 2018-2025 ruoyi.vip All Rights Reserved.</span>
    </div>
  </div>
</template>

<script setup>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import useUserStore from '@/store/modules/user'

const title = import.meta.env.VITE_APP_TITLE
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

const loginForm = ref({
  username: "",
  password: "",
  rememberMe: false,
  code: "",
  uuid: ""
})

const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const codeUrl = ref("")
const loading = ref(false)
// 验证码开关
const captchaEnabled = ref(true)
// 注册开关
const register = ref(true)
const redirect = ref(undefined)

watch(route, (newRoute) => {
    redirect.value = newRoute.query && newRoute.query.redirect
}, { immediate: true })

function handleLogin() {
  proxy.$refs.loginRef.validate(valid => {
    if (valid) {
      loading.value = true
      // 勾选了需要记住密码设置在 cookie 中设置记住用户名和密码
      if (loginForm.value.rememberMe) {
        Cookies.set("username", loginForm.value.username, { expires: 30, secure: true, sameSite: 'strict' })
        Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30, secure: true, sameSite: 'strict' })
      } else {
        // 否则移除
        Cookies.remove("username")
        Cookies.remove("rememberMe")
      }
      // 调用action的登录方法
      userStore.login(loginForm.value).then(() => {
        const query = route.query
        const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
          if (cur !== "redirect") {
            acc[cur] = query[cur]
          }
          return acc
        }, {})
        router.push({ path: redirect.value || "/", query: otherQueryParams })
      }).catch(() => {
        loading.value = false
        // 重新获取验证码
        if (captchaEnabled.value) {
          getCode()
        }
      })
    }
  })
}

function getCode() {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img
      loginForm.value.uuid = res.uuid
    }
  })
}

function getCookie() {
  const username = Cookies.get("username")
  const rememberMe = Cookies.get("rememberMe")
  loginForm.value = {
    ...loginForm.value,
    username: username === undefined ? loginForm.value.username : username,
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
  }
}

getCode()
getCookie()
</script>

<style lang='scss' scoped>
.login-container {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  
  // 渐变背景
  background: linear-gradient(135deg, #667eea 0%, #764ba2 25%, #f093fb 50%, #4facfe 75%, #00f2fe 100%);
  background-size: 400% 400%;
  animation: gradient 15s ease infinite;
  
  // 背景图片覆盖
  &::before {
    content: '';
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: url("../assets/images/login-background.jpg");
    background-size: cover;
    background-position: center;
    opacity: 0.3;
    z-index: 0;
    pointer-events: none;
  }
}

@keyframes gradient {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.login-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1), 
              0 0 1px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  animation: slideUp 0.6s ease-out;
  
  @media (max-width: 640px) {
    margin: 20px;
    max-width: none;
    padding: 30px 20px;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.card-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #1f2d3d;
  letter-spacing: -0.5px;
}

.card-subtitle {
  margin: 8px 0 0 0;
  font-size: 14px;
  color: #909399;
  font-weight: 400;
}

// 深色主题支持
:deep(.dark) {
  .login-container {
    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 25%, #0f3460 50%, #0a1e38 75%, #001a33 100%);
  }
  
  .login-card {
    background: rgba(30, 30, 30, 0.95);
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3), 
                0 0 1px rgba(0, 0, 0, 0.3);
  }
  
  .card-title {
    color: #ffffff;
  }
  
  .card-subtitle {
    color: #c0c4cc;
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 20px;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  :deep(.el-input) {
    --el-input-height: 44px;
    
    input {
      font-size: 14px;
      
      &::placeholder {
        color: #c0c4cc;
      }
    }
    
    &.is-focus {
      .el-input__wrapper {
        box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
      }
    }
  }
  
  :deep(.el-input__wrapper) {
    background: #f8f9fa;
    border: 1px solid #e4e7eb;
    border-radius: 8px;
    transition: all 0.3s ease;
    
    &:hover {
      background: #ffffff;
      border-color: #d0d0d0;
    }
  }
  
  :deep(.el-input__prefix) {
    display: flex;
    align-items: center;
  }
}

.input-icon {
  width: 16px;
  height: 16px;
  color: #909399;
  margin-right: 8px;
}

// 验证码包装类
.captcha-wrapper {
  display: flex;
  gap: 8px;
  align-items: center;
}

.captcha-img {
  width: 100px;
  height: 44px;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid #e4e7eb;
  background: #f8f9fa;
  transition: all 0.3s ease;
  
  &:hover {
    border-color: #409eff;
    transform: scale(1.02);
  }
}

// 表单底部
.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px 0 28px 0;
  font-size: 14px;
  
  :deep(.el-checkbox) {
    --el-checkbox-text-color: #606266;
    
    .el-checkbox__label {
      font-size: 14px;
      user-select: none;
    }
  }
}

.forgot-link {
  color: #409eff;
  text-decoration: none;
  transition: color 0.3s ease;
  
  &:hover {
    color: #66b1ff;
    text-decoration: underline;
  }
}

// 登录按钮
.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 1px;
  border-radius: 8px;
  transition: all 0.3s ease;
  
  :deep(&) {
    background: linear-gradient(90deg, #409eff 0%, #66b1ff 100%);
    border: none;
    
    &:hover {
      box-shadow: 0 4px 20px rgba(64, 158, 255, 0.4);
      transform: translateY(-2px);
    }
    
    &:active {
      transform: translateY(0);
    }
  }
}

// 底部版权
.login-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 50px;
  line-height: 50px;
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
  font-family: Arial, Helvetica, sans-serif;
  font-size: 12px;
  letter-spacing: 1px;
  z-index: 0;
  background: linear-gradient(180deg, transparent 0%, rgba(0, 0, 0, 0.1) 100%);
}

// 响应式设计
@media (max-width: 768px) {
  .login-container {
    padding: 20px;
  }
  
  .login-card {
    max-width: 100%;
  }
  
  .card-title {
    font-size: 24px;
  }
  
  .form-footer {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}

// 深色主题详细优化
:deep(.dark) {
  .login-form {
    :deep(.el-input__wrapper) {
      background: rgba(255, 255, 255, 0.05);
      border-color: rgba(255, 255, 255, 0.1);
      
      &:hover {
        background: rgba(255, 255, 255, 0.08);
        border-color: rgba(255, 255, 255, 0.15);
      }
    }
  }
  
  .captcha-img {
    background: rgba(255, 255, 255, 0.05);
    border-color: rgba(255, 255, 255, 0.1);
  }
  
  .input-icon {
    color: #c0c4cc;
  }
  
  .forgot-link {
    color: #85ce61;
    
    &:hover {
      color: #a6e969;
    }
  }
}
</style>
