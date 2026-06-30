# IntelliSME-FinTaxE

中小企业 AI 智能财税助手 — 集成税务风险扫描、AI 对话、发票管理、经营分析于一体的财务管理平台。

## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | Vue 3.4 + Vite 5.3 + Element Plus 2.7 + Pinia 2.1 + ECharts |
| 后端 | Spring Boot 2.7 + MyBatis + MySQL 8 + Redis + JWT |
| AI | 通义千问 (DashScope API) — SSE 流式对话 + 税务风险智能分析 |
| 存储 | 阿里云 OSS（文件/发票图片上传） |

## 核心功能

- **AI 智能助手** — SSE 流式对话，前端 Markdown 实时渲染，代码块一键复制，支持文件上传
- **税务风险扫描** — 7 维度自动检测（缺票/税负/发票异常/公私混淆/零申报/库存/工资），支持 AI 深度分析与规则引擎快速扫描双模式
- **老板看板** — 收入/成本/利润卡片 + 最新风险预警表格，点击风险可跳转详情
- **发票管理** — OCR 识别 + 手动录入，入账确认带二次弹窗
- **薪资 / 库存 / 月报管理** — Excel 批量导入、CSV/Excel 导出、分页查询
- **经营分析** — 多维度趋势图表，支持月/季/年汇总
- **全局企业切换** — Navbar 下拉切换企业，所有页面数据自动联动
- **权限体系** — 基于 RuoYi RBAC 动态路由，支持老板/会计/出纳多角色

## 项目结构

```
AIFC/                          # 项目根目录
├── aifc-admin/                # 后端启动模块 (Controller、配置)
├── aifc-system/               # 业务模块 (Domain、Service、Mapper)
│   └── src/main/resources/mapper/biz/   # MyBatis XML
├── aifc-common/               # 公共工具 (BaseEntity、JWT、异常处理)
├── aifc-framework/            # 框架配置 (安全、拦截器)
├── sql/
│   └── aifc.sql               # 数据库初始化脚本（表结构 + 菜单 + 演示数据）
├── pom.xml                    # Maven 父 POM
└── frontend/                  # 前端 (Vue 3)
    ├── src/
    │   ├── api/aifc/          # 业务 API
    │   ├── views/aifc/        # 业务页面 (8 个模块)
    │   ├── store/modules/     # Pinia 状态 (user / company / permission)
    │   ├── utils/
    │   │   ├── sse.js         # SSE 流式请求
    │   │   ├── markdown.js    # Markdown 渲染
    │   │   ├── export.js      # Excel/CSV 导出
    │   │   └── import.js      # Excel 导入
    │   └── components/BatchImport  # 通用批量导入组件
    ├── .env.development       # 开发环境变量
    ├── .env.production        # 生产环境变量
    ├── .env.staging           # 预发布环境变量
    └── vite.config.js         # Vite 配置 (代理 → 8082)
```

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis
- Node.js 18+

### 1. 数据库初始化

```sql
CREATE DATABASE aifc DEFAULT CHARSET utf8mb4;
USE aifc;
SOURCE sql/aifc.sql;
```

### 2. 后端配置

编辑 `aifc-admin/src/main/resources/application.yml`，替换以下占位符：

```yaml
token:
  secret: your-jwt-secret-key-at-least-32-chars

aifc:
  ai:
    api-key: your-dashscope-api-key    # 通义千问 API Key

aliyun:
  oss:
    accessKeyId: your-access-key-id
    accessKeySecret: your-access-key-secret
```

编辑 `application-druid.yml`：

```yaml
master:
  username: root
  password: your-mysql-password
```

### 3. 启动后端

IDEA 打开 `AIFC` 项目，运行 `AifcApplication`，默认端口 `8082`。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev        # 开发模式，默认端口 80
```

Vite 代理已配置 `/dev-api` → `http://localhost:8082`。

## 架构说明

```
┌─────────────┐     ┌──────────────┐     ┌───────────────┐
│  Vue 3 前端  │────▶│  Spring Boot │────▶│  MySQL + Redis │
│  Port 80    │     │  Port 8082   │     │  Port 3306/6379│
└──────┬──────┘     └──────┬───────┘     └───────────────┘
       │                   │
       │  SSE Stream       │  HTTP
       │                   ▼
       └──────────▶  DashScope AI API
                     (通义千问)
```

- 前端通过 Vite 代理转发请求到后端
- AI 助手使用 SSE 实现流式响应
- 税务风险扫描优先调用 AI 深度分析，失败自动降级为规则引擎
- 企业数据通过 `sys_user_company` 关联表实现多租户隔离

## 演示账号

| 角色 | 用户名 | 密码 |
|---|---|---|
| 管理员 | admin | admin123 |

> 首次使用需执行 `sql/aifc.sql` 初始化数据库（包含表结构、菜单权限和演示数据）。

## 许可证

MIT