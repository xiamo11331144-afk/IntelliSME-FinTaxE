<template>
  <div class="app-container home">
    <!-- 顶部欢迎区域 -->
    <div class="welcome-section">
      <el-row :gutter="20">
        <el-col :sm="24" :lg="14">
          <div class="welcome-card">
            <div class="welcome-header">
              <svg-icon icon-class="dashboard" class="welcome-icon" />
              <div>
                <h1>欢迎使用 AIFC</h1>
                <p class="welcome-subtitle">AI赋能的财务分析系统</p>
              </div>
            </div>
            <p class="welcome-intro">
              AIFC 是一套专业的企业财务智能分析平台，为您提供全面的财务数据管理、智能风险预警、经营分析和决策支持。通过人工智能技术的赋能，让您的财务管理更智能、更高效。
            </p>
            <div class="feature-tags">
              <el-tag type="success">AI驱动</el-tag>
              <el-tag type="info">实时分析</el-tag>
              <el-tag type="warning">风险预警</el-tag>
              <el-tag type="danger">数据安全</el-tag>
            </div>
          </div>
        </el-col>
        <el-col :sm="24" :lg="10">
          <div class="stat-card">
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="stat-item">
                  <div class="stat-value">1.0.0</div>
                  <div class="stat-label">系统版本</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="stat-item">
                  <div class="stat-value">7</div>
                  <div class="stat-label">核心模块</div>
                </div>
              </el-col>
            </el-row>
            <el-divider />
            <p style="text-align: center; color: #909399; font-size: 12px; margin: 0;">
              <el-tag type="success">&checkmark; 免费开源</el-tag>
            </p>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-divider />

    <!-- 核心功能模块 -->
    <div class="modules-section">
      <h2 style="margin-bottom: 20px; text-align: center;">核心功能模块</h2>
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="module-card" @click="navigateTo('/aifc/invoice')">
            <template #header>
              <div class="card-header">
                <svg-icon icon-class="shopping" class="module-icon" />
                <span>票据管理</span>
              </div>
            </template>
            <p class="module-desc">智能发票识别、票据管理和电子档案存储</p>
            <el-progress :percentage="100" color="#f56c6c" />
            <div class="module-action">→ 进入</div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="module-card" @click="navigateTo('/aifc/tax-risk')">
            <template #header>
              <div class="card-header">
                <svg-icon icon-class="warning" class="module-icon" />
                <span>风险预警</span>
              </div>
            </template>
            <p class="module-desc">实时税务风险扫描和预警提示</p>
            <el-progress :percentage="95" color="#e6a23c" />
            <div class="module-action">→ 进入</div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="module-card" @click="navigateTo('/aifc/analysis')">
            <template #header>
              <div class="card-header">
                <svg-icon icon-class="chart" class="module-icon" />
                <span>经营分析</span>
              </div>
            </template>
            <p class="module-desc">多维度数据分析和趋势预测</p>
            <el-progress :percentage="100" color="#409eff" />
            <div class="module-action">→ 进入</div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" style="margin-top: 16px;">
        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="module-card" @click="navigateTo('/aifc/month-report')">
            <template #header>
              <div class="card-header">
                <svg-icon icon-class="date" class="module-icon" />
                <span>月报管理</span>
              </div>
            </template>
            <p class="module-desc">自动生成财务月报和经营总结</p>
            <el-progress :percentage="88" color="#67c23a" />
            <div class="module-action">→ 进入</div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="module-card" @click="navigateTo('/aifc/salary')">
            <template #header>
              <div class="card-header">
                <svg-icon icon-class="money" class="module-icon" />
                <span>工资管理</span>
              </div>
            </template>
            <p class="module-desc">员工薪资核算和薪酬分析</p>
            <el-progress :percentage="92" color="#909399" />
            <div class="module-action">→ 进入</div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="module-card" @click="navigateTo('/aifc/inventory')">
            <template #header>
              <div class="card-header">
                <svg-icon icon-class="list" class="module-icon" />
                <span>库存管理</span>
              </div>
            </template>
            <p class="module-desc">实时库存监控和成本分析</p>
            <el-progress :percentage="85" color="#00bfff" />
            <div class="module-action">→ 进入</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-divider />

    <!-- 系统特性和技术栈 -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="feature-card">
          <template v-slot:header>
            <div class="clearfix">
              <span><svg-icon icon-class="star" style="margin-right: 8px;" /> 核心特性</span>
            </div>
          </template>
          <div class="feature-list">
            <p><i class="el-icon-success"></i> AI智能分析</p>
            <p><i class="el-icon-success"></i> 实时数据处理</p>
            <p><i class="el-icon-success"></i> 风险自动预警</p>
            <p><i class="el-icon-success"></i> 多维度报表</p>
            <p><i class="el-icon-success"></i> 权限精细控制</p>
            <p><i class="el-icon-success"></i> 数据深度加密</p>
            <p><i class="el-icon-success"></i> 高效性能</p>
            <p><i class="el-icon-success"></i> 易于扩展</p>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="feature-card">
          <template v-slot:header>
            <div class="clearfix">
              <span><svg-icon icon-class="tool" style="margin-right: 8px;" /> 后端技术栈</span>
            </div>
          </template>
          <div class="tech-stack">
            <div class="tech-item">
              <span class="tech-name">SpringBoot</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">Spring Security</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">JWT 认证</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">MyBatis ORM</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">MySQL 数据库</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">Redis 缓存</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="feature-card">
          <template v-slot:header>
            <div class="clearfix">
              <span><svg-icon icon-class="component" style="margin-right: 8px;" /> 前端技术栈</span>
            </div>
          </template>
          <div class="tech-stack">
            <div class="tech-item">
              <span class="tech-name">Vue 3</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">Element Plus</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">Pinia 状态管理</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">Axios 请求</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">ECharts 图表</span>
            </div>
            <div class="tech-item">
              <span class="tech-name">Vite 构建</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Index">
const { proxy } = getCurrentInstance()

function navigateTo(path) {
  proxy.$router.push(path)
}
</script>

<style scoped lang="scss">
.home {
  padding: 20px;
  background-color: var(--el-bg-color, #f5f7fa);
}

/* 欢迎区域 */
.welcome-section {
  margin-bottom: 20px;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);

  .welcome-header {
    display: flex;
    align-items: flex-start;
    margin-bottom: 20px;

    .welcome-icon {
      font-size: 40px;
      margin-right: 16px;
    }

    h1 {
      margin: 0;
      font-size: 28px;
      font-weight: bold;
    }
  }

  .welcome-subtitle {
    margin: 0;
    font-size: 14px;
    opacity: 0.9;
  }

  .welcome-intro {
    margin: 15px 0;
    line-height: 1.6;
    opacity: 0.95;
  }

  .feature-tags {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;

    :deep(.el-tag) {
      background-color: rgba(255, 255, 255, 0.2);
      border-color: rgba(255, 255, 255, 0.3);
      color: white;
    }
  }
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

  .stat-item {
    text-align: center;
    padding: 12px 0;

    .stat-value {
      font-size: 24px;
      font-weight: bold;
      color: #409eff;
      line-height: 1;
    }

    .stat-label {
      font-size: 12px;
      color: #909399;
      margin-top: 8px;
    }
  }
}

/* 模块卡片 */
.modules-section {
  margin: 20px 0;

  h2 {
    font-size: 20px;
    color: #303133;
  }
}

.module-card {
  transition: all 0.3s ease;
  cursor: pointer;
  border-radius: 8px;
  height: 100%;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);

    .module-action {
      transform: translateX(4px);
    }
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;

    .module-icon {
      font-size: 20px;
      color: #409eff;
    }
  }

  .module-desc {
    color: #606266;
    font-size: 12px;
    margin: 12px 0;
    line-height: 1.5;
    min-height: 24px;
  }

  .module-action {
    text-align: center;
    color: #409eff;
    font-weight: bold;
    margin-top: 12px;
    transition: all 0.2s ease;
  }

  :deep(.el-progress) {
    margin: 12px 0;
  }
}

/* 特性卡片 */
.feature-card {
  border-radius: 8px;
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  }

  :deep(.el-card__header) {
    border-bottom: 1px solid var(--el-border-color-light, #ebeef5);
    padding: 16px 20px;
  }

  :deep(.el-card__body) {
    padding: 20px;
  }
}

.feature-list {
  p {
    margin: 8px 0;
    font-size: 13px;
    color: #606266;
    display: flex;
    align-items: center;

    i {
      color: #67c23a;
      margin-right: 8px;
      font-size: 14px;
    }
  }
}

.tech-stack {
  display: flex;
  flex-direction: column;
  gap: 8px;

  .tech-item {
    display: inline-block;
    padding: 6px 12px;
    background-color: var(--el-fill-color-light, #f5f7fa);
    border-radius: 4px;
    border-left: 3px solid #409eff;
    transition: all 0.2s ease;

    &:hover {
      background-color: #409eff;
      color: white;
    }

    .tech-name {
      font-size: 12px;
      font-weight: 500;
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .home {
    padding: 12px;
  }

  .welcome-card {
    padding: 20px !important;

    .welcome-header {
      flex-direction: column;

      .welcome-icon {
        margin-right: 0;
        margin-bottom: 12px;
      }

      h1 {
        font-size: 22px;
      }
    }
  }

  .module-card {
    .card-header {
      flex-wrap: wrap;
    }
  }
}
</style>

