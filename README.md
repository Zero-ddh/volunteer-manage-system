# 志愿者活动服务平台 - 管理后台

## 项目简介

志愿者活动服务平台管理后台是一个基于Vue 3的现代化Web应用，面向管理员和活动组织者，提供完整的平台运营管理、活动管理、用户管理等核心功能。

## 技术栈

### 核心框架
- **Vue 3.5.22** - 渐进式JavaScript框架
- **TypeScript 5.9** - 类型安全的JavaScript超集
- **Vite 7.1.7** - 新一代前端构建工具

### UI组件库
- **Element Plus 2.11.5** - 基于Vue 3的桌面端组件库
- **@element-plus/icons-vue 2.3.2** - Element Plus图标库

### 状态管理 & 路由
- **Pinia 3.0.3** - Vue的官方状态管理库
- **Vue Router 4.5.1** - Vue.js官方路由管理器

### HTTP请求
- **Axios 1.12.2** - 基于Promise的HTTP客户端

### 开发工具
- **ESLint** - 代码质量检查工具
- **vue-tsc** - Vue TypeScript类型检查
- **vite-plugin-vue-devtools** - Vue开发者工具

## 项目结构

```
volunteer-admin/
├── public/                      # 静态资源目录
│   └── favicon.ico              # 网站图标
├── src/
│   ├── api/                     # API接口封装
│   │   ├── index.ts            # 基础axios配置
│   │   ├── menu.ts             # 菜单API
│   │   ├── role.ts             # 角色API
│   │   ├── user.ts             # 用户API
│   │   └── volunteer.ts        # 志愿者API
│   ├── assets/                  # 资源文件
│   │   ├── base.css            # 基础样式
│   │   ├── logo.svg            # 项目Logo
│   │   └── main.css            # 主样式
│   ├── components/              # 公共组件
│   │   ├── ActivitySkillRequirementManager.vue  # 活动技能要求管理
│   │   ├── CertificatePreview.vue               # 证书预览
│   │   ├── icons/              # 图标组件
│   │   └── Table/              # 表格组件
│   ├── router/                  # 路由配置
│   │   └── index.ts            # 路由定义
│   ├── stores/                  # Pinia状态管理
│   │   ├── auth.ts             # 认证状态
│   │   └── counter.ts          # 计数器示例
│   ├── utils/                   # 工具函数
│   │   ├── date.ts             # 日期工具
│   │   ├── organizerPermissions.ts  # 组织者权限
│   │   └── request.ts          # 请求工具
│   ├── views/                   # 页面组件
│   │   ├── Activity/           # 活动管理
│   │   ├── ActivitySummary/    # 活动总结
│   │   ├── Checkin/            # 签到管理
│   │   ├── Community/          # 社群管理
│   │   ├── Dashboard/          # 仪表盘
│   │   ├── Goods/              # 商品管理
│   │   ├── Home/               # 首页
│   │   ├── Insurance/          # 保险管理
│   │   ├── Layout/             # 布局组件
│   │   ├── Login/              # 登录页
│   │   ├── Menu/               # 菜单管理
│   │   ├── PointsExchange/     # 积分兑换
│   │   ├── PointsRecord/       # 积分记录
│   │   ├── Recruitment/        # 招募管理
│   │   ├── Registration/       # 报名管理
│   │   ├── Role/               # 角色管理
│   │   ├── Statistics/         # 数据统计
│   │   ├── System/             # 系统管理
│   │   ├── User/               # 用户管理
│   │   └── Volunteer/          # 志愿者管理
│   ├── App.vue                 # 根组件
│   └── main.ts                 # 应用入口
├── index.html                   # HTML模板
├── package.json                 # 项目依赖配置
├── tsconfig.json                # TypeScript配置
├── vite.config.ts               # Vite构建配置
└── README.md                    # 项目说明文档
```

## 功能模块

### 1. 系统管理
- 用户管理：用户列表、用户详情、用户状态管理
- 角色管理：角色定义、权限分配
- 菜单管理：菜单配置、权限控制

- 志愿者管理：志愿者列表、证书管理

### 2. 活动管理
- 活动列表：活动创建、编辑、发布、取消
- 活动审核：活动状态审核、内容审核
- 报名管理：报名人员审核、审批流程
- 签到管理：活动签到记录、签到统计

### 3. 社群管理
- 社群列表：社群创建、管理
- 成员管理：成员列表、成员状态管理


### 4. 招募管理
- 招募列表：招募信息发布、编辑
- 报名管理：报名者筛选、录用管理
- 录用通知：自动发送录用通知（消息通知）

### 5. 积分与激励
- 积分规则：积分规则配置
- 积分记录：积分明细查询
- 商品管理：积分商品管理
- 兑换管理：兑换记录管理

### 6. 保险管理
- 保险产品：产品配置、价格管理
- 订单管理：订单查询、状态管理

### 7. 数据统计
- 活动统计：活动数据统计
- 志愿者统计：服务时长、参与度分析

## 环境要求

- **Node.js**: ^20.19.0 || >=22.12.0
- **npm**: 9.0.0 或更高版本
- **后端服务**: 需要在本地8080端口运行

## 安装与启动

### 1. 安装依赖

```bash
npm install
```

### 2. 开发环境运行

```bash
npm run dev
```

访问地址: http://localhost:3001

### 3. 生产环境构建

```bash
# 类型检查并构建
npm run build

# 仅构建（不含类型检查）
npm run build-only
```

### 4. 构建产物预览

```bash
npm run preview
```

### 5. 代码检查

```bash
npm run lint
```

## 开发配置

### 代理配置

项目在 `vite.config.ts` 中配置了开发代理：

```typescript
server: {
  port: 3001,                    // 前端开发服务端口
  host: '0.0.0.0',
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // 后端服务地址
      changeOrigin: true,
      secure: false,
      rewrite: (path) => path.replace(/^\/api/, '/api')
    }
  }
}
```

### 环境变量

可在项目根目录创建 `.env` 文件配置环境变量：

```bash
# 后端API地址
VITE_API_BASE_URL=http://localhost:8080/api
```

## 主要功能说明

### 权限控制

系统采用RBAC（基于角色的访问控制）模型：
- **超级管理员**：拥有所有权限
- **活动管理员**：负责活动审核和管理
- **用户管理员**：负责用户和志愿者管理
- **活动组织者**：可发布和管理自己的活动

### 认证机制

- 使用JWT Token进行身份认证
- Token存储在localStorage中
- 请求自动携带Token
- Token过期自动跳转登录页

### 状态管理

使用Pinia管理应用状态：
- `auth.ts`: 用户认证状态（登录信息、权限等）
- 其他模块状态可根据需要添加

### 请求拦截

已配置axios请求/响应拦截器：
- 请求拦截：自动添加Token
- 响应拦截：统一错误处理、Token刷新

## 注意事项

### 1. 开发环境配置
- 确保后端服务已启动在8080端口
- 确保MySQL数据库已运行并包含完整数据
- 首次运行需先执行 `npm install` 安装依赖

### 2. 代码规范
- 使用TypeScript进行开发，确保类型安全
- 遵循Vue 3 Composition API规范
- 使用ESLint进行代码检查
- 组件命名采用PascalCase
- 文件命名采用kebab-case

### 3. 性能优化
- 使用Vite的自动代码分割
- 按需导入Element Plus组件
- 大列表使用虚拟滚动
- 图片使用懒加载

### 4. 浏览器兼容性
- Chrome/Edge（推荐）
- Firefox
- Safari
- 不支持IE浏览器

### 5. 常见问题

**Q: 启动后无法访问后端接口？**
A: 检查后端服务是否在8080端口运行，检查防火墙设置

**Q: npm install 失败？**
A: 尝试清除缓存：`npm cache clean --force`，或使用yarn: `yarn install`

**Q: 构建后页面空白？**
A: 检查路由配置的base路径，确认资源路径配置正确

**Q: Element Plus组件样式丢失？**
A: 确保在main.ts中正确引入了Element Plus样式

## 浏览器支持

推荐使用以下浏览器：
- Chrome（最新版本）
- Edge（最新版本）
- Firefox（最新版本）
- Safari（最新版本）

## 贡献指南

1. Fork本项目
2. 创建功能分支: `git checkout -b feature/YourFeature`
3. 提交更改: `git commit -m 'Add some feature'`
4. 推送到分支: `git push origin feature/YourFeature`
5. 提交Pull Request

## 许可证

本项目采用 MIT 许可证

## 联系方式

如有问题或建议，请联系开发团队
