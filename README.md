# EzClaim 管理后台

Next.js 14 构建的 EzClaim 管理端，提供管理员登录、标签管理、报销单审批以及审计事件查询等能力。后端接口基于项目根目录的 `api.json` (OpenAPI 3.1)。

## 功能亮点

- 🔐 管理员登录，使用 JWT 存储于 HttpOnly Cookie
- 🏷️ 标签管理：创建、查看、删除标签
- 💼 报销单管理：支持搜索、筛选、排序，状态流转遵守服务端规则
- 📊 实时概览：按状态统计报销单数量
- 🛠️ 审计事件查询：多条件过滤、分页展示、详细 JSON 查看

## 快速开始

```bash
npm install
npm run dev
```

默认后端地址为 `http://localhost:8080`，如需修改可在启动前设置：

```bash
export API_BASE_URL="http://your-backend:8080"
```

或在 `.env.local` 中声明 `API_BASE_URL`/`NEXT_PUBLIC_API_BASE_URL`。

## 运行前提

- Node.js 18+
- 后端服务（默认端口 8080，可通过 `api.json` 查看接口定义）
- Demo 账户：`admin / ezclaim-password` 或 `reader / reader-pass`

## 脚本

| 命令         | 说明                 |
| ------------ | -------------------- |
| `npm run dev`   | 开发模式 (`localhost:3000`) |
| `npm run build` | 生产构建            |
| `npm run start` | 启动生产环境服务器  |
| `npm run lint`  | 执行 ESLint 检查    |

## 目录结构

```
app/                 # App Router 路由
  (auth)/login       # 登录页
  (dashboard)/       # 受保护的业务页面
components/          # UI 组件与布局
lib/                 # API 客户端、配置、工具函数
middleware.ts        # 保护路由的中间件
```

## 认证机制

- 登录后通过服务端调用 `/api/auth/login` 获取 JWT
- Token 以 HttpOnly Cookie (`ezclaim_token`) 形式存储
- `middleware.ts` 拦截未登录访问并重定向至 `/login`

## 注意事项

- 所有与后端交互的操作使用 Next.js Server Actions，并在成功后 `router.refresh()` + `revalidatePath`
- 报销单状态流转遵循后端 `ClaimService` 的约束：
  - `SUBMITTED → APPROVED/REJECTED`
  - `APPROVED → PAID/REJECTED`
- `Audit Events` 页面采用 GET 参数驱动，可直接分享链接复现查询条件

欢迎根据业务需要扩展更多管理能力，例如报销单详情编辑、批量操作等。
