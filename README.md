# 志愿者活动服务平台 - 用户端

## 项目简介

志愿者活动服务平台用户端是一个面向志愿者和普通用户的Web应用，提供活动浏览、报名、服务记录查询、社群互动等完整的志愿者服务功能。

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

### 文档处理
- **jsPDF 3.0.3** - PDF文档生成库
- **html2canvas 1.4.1** - HTML转Canvas工具

### 开发工具
- **ESLint** - 代码质量检查工具
- **vue-tsc** - Vue TypeScript类型检查
- **vite-plugin-vue-devtools** - Vue开发者工具

## 项目结构

```
volunteer-web/
├── public/                      # 静态资源目录
│   └── favicon.ico              # 网站图标
├── src/
│   ├── api/                     # API接口封装
│   │   ├── index.ts            # 基础axios配置
│   │   ├── auth.ts             # 认证API
│   │   ├── goods.ts            # 商品API
│   │   ├── insurance.ts        # 保险API
│   │   ├── message.ts          # 消息API
│   │   ├── safety.ts           # 安全相关API
│   │   ├── starVolunteers.ts   # 明星志愿者API
│   │   └── user.ts             # 用户API
│   ├── assets/                  # 资源文件
│   │   ├── base.css            # 基础样式
│   │   ├── logo.svg            # 项目Logo
│   │   └── main.css            # 主样式
│   ├── components/              # 公共组件
│   │   ├── ActivitySkillRequirements.vue  # 活动技能要求
│   │   ├── CertificatePreview.vue         # 证书预览
│   │   ├── Dialog/             # 对话框组件
│   │   ├── Form/               # 表单组件
│   │   ├── icons/              # 图标组件
│   │   ├── Layout/             # 布局组件
│   │   ├── SkillManagement.vue  # 技能管理
│   │   ├── SkillMatch.vue      # 技能匹配
│   │   ├── Table/              # 表格组件
│   │   └── VolunteerFilter.vue # 志愿者筛选
│   ├── router/                  # 路由配置
│   │   └── index.ts            # 路由定义
│   ├── stores/                  # Pinia状态管理
│   │   ├── auth.ts             # 认证状态
│   │   ├── activity.ts         # 活动状态
│   │   └── counter.ts          # 计数器示例
│   ├── utils/                   # 工具函数
│   │   └── request.ts          # 请求工具
│   ├── views/                   # 页面组件
│   │   ├── Activity/           # 活动相关页面
│   │   ├── Auth/               # 认证页面（登录、注册）
│   │   ├── Certificate/        # 证书管理
│   │   ├── Community/          # 社群功能
│   │   ├── Insurance/          # 保险服务
│   │   ├── Profile/            # 个人中心
│   │   ├── Recruitment/        # 招募信息
│   │   └── ...                 # 其他功能页面
│   ├── App.vue                 # 根组件
│   └── main.ts                 # 应用入口
├── index.html                   # HTML模板
├── package.json                 # 项目依赖配置
├── tsconfig.json                # TypeScript配置
├── vite.config.ts               # Vite构建配置
└── README.md                    # 项目说明文档
```

## 功能模块

### 1. 用户认证
- 用户注册：用户名/手机号注册
- 用户登录：账号密码登录
- 游客模式：无需登录即可浏览活动

### 2. 活动管理
- 活动列表：浏览所有活动、按分类筛选
- 活动详情：查看活动详细信息、报名要求
- 活动报名：一键报名、报名状态查看
- 我的活动：已报名活动、历史活动记录
- 活动签到：扫码签到、位置签到

### 3. 个人中心
- 个人信息：资料编辑上传
- 技能管理：技能添加、证书上传
- 服务记录：服务时长统计、服务证书
- 积分管理：积分查询、积分商城
- 安全设置：紧急联系人、安全保障、保险购买
- 我的活动、签到记录、我的社群、招募申请、消息中心等模块

### 4. 智能推荐
- 活动匹配：基于技能、历史记录的推荐
- 技能匹配：活动技能要求匹配
- 个性化推荐：AI智能推荐适配活动

### 5. 社群功能
- 社群列表：浏览所有社群
- 加入社群：申请加入感兴趣的社群

### 6. 招募信息
- 招募列表：浏览招募信息
- 申请招募：提交申请
- 申请状态：查看申请进度、录用通知

### 7. 积分商城
- 商品浏览：积分兑换商品
- 兑换记录：历史兑换记录查询

### 8. 保险服务
- 保险购买：志愿者保险产品购买
- 订单管理：保单查询

### 9. 星级志愿者
- 排行榜：服务时长排行

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

访问地址: http://localhost:3000

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
  port: 3000,                    // 前端开发服务端口
  host: '0.0.0.0',
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // 后端服务地址
      changeOrigin: true,
      secure: false
    },
    '/uploads': {
      target: 'http://localhost:8080',  // 文件上传服务地址
      changeOrigin: true,
      secure: false
    }
  }
}
```

### 环境变量

可在项目根目录创建 `.env` 文件配置环境变量：

```bash
# 后端API地址
VITE_API_BASE_URL=http://localhost:8080/api

# 文件服务地址
VITE_UPLOAD_BASE_URL=http://localhost:8080/api/uploads
```

## 主要功能说明

### 用户权限

系统支持两种用户模式：
- **游客模式**：无需登录，可浏览活动、查看详情
- **注册用户**：登录后可使用完整功能，包括报名、个人信息管理等

### 活动报名流程

1. 浏览活动列表
2. 查看活动详情和要求
3. 点击"立即报名"
4. 填写报名信息
5. 等待组织者审核
6. 接收审核通知
7. 审核通过后参与活动

### 签到机制

- **二维码签到**：活动组织者生成签到码
- **位置签到**：基于GPS位置的签到验证
- **双重验证**：确保服务真实性

### 服务时长统计

- 自动记录活动参与时长
- 生成服务证书（PDF格式）
- 支持导出和打印
- 对接官方志愿平台完成时长认证

### 证书生成

使用jsPDF和html2canvas技术：
- 活动完成后自动生成电子证书
- 支持自定义证书样式
- 证书可下载保存或添加至个人档案

### 智能匹配

- **技能匹配**：根据用户技能匹配活动需求
- **历史推荐**：基于用户参与历史推荐相似活动
- **地理推荐**：优先推荐附近的活动
- **时间推荐**：根据用户可用时间推荐

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
- 活动列表使用分页加载
- 图片使用懒加载优化
- 长列表使用虚拟滚动

### 4. 移动端适配
- 支持响应式设计
- 在移动设备上自动适配布局
- 触摸事件优化
- 提供移动端专用操作体验

### 5. 文件上传

- 图片上传：头像、活动封面、证书等
- 支持格式：JPG、PNG、PDF等
- 文件大小限制：单文件最大10MB
- 自动压缩和格式转换

### 6. 常见问题

**Q: 无法加载图片或文件？**
A: 检查后端uploads目录是否存在，检查文件上传服务是否正常

**Q: 证书下载失败？**
A: 确保安装了jsPDF和html2canvas依赖，检查浏览器控制台错误

**Q: 定位功能不工作？**
A: 检查浏览器定位权限，确保使用HTTPS或localhost环境

**Q: 报名后审核状态不更新？**
A: 检查websocket连接是否正常，或手动刷新页面

## 浏览器支持

推荐使用以下浏览器：
- Chrome（最新版本）- 推荐
- Edge（最新版本）
- Firefox（最新版本）
- Safari（最新版本）
- 移动端浏览器（iOS Safari、Chrome Mobile）

## 贡献指南

1. Fork本项目
2. 创建功能分支: `git checkout -b feature/YourFeature`
3. 提交更改: `git commit日本語 -m 'Add some feature'`
4. 推送到分支: `git push origin feature/YourFeature`
5. 提交Pull Request

## 许可证

本项目采用 MIT 许可证

## 联系方式

如有问题或建议，请联系开发团队
