# 志愿者活动服务平台 - 后端服务

## 项目简介

志愿者活动服务平台后端是基于Spring Boot开发的RESTful API服务，提供用户认证、活动管理、行政管理、数据统计等完整的业务功能，支持志愿者活动服务平台的运营需求。

## 技术栈

### 核心框架
- **Spring Boot 2.7.18** - Java企业级应用开发框架
- **Spring Security** - 安全认证授权框架
- **Spring MVC** - Web MVC框架
- **MyBatis Plus 3.5.3.1** - MyBatis增强工具

### 数据库
- **MySQL 8.0** - 关系型数据库
- **HikariCP** - 数据库连接池

### 认证授权
- **JWT (jjwt 0.11.5)** - JSON Web Token认证
- **Spring Security** - 安全框架

### 工具库
- **Lombok** - 简化Java代码开发
- **Jackson** - JSON序列化/反序列化
- **EasyExcel 3.3.2** - Excel处理工具
- **iText 7.2.5** - PDF生成工具
- **ZXing 3.5.1** - 二维码生成

### 缓存
- **Caffeine** - 高性能本地缓存
- **Spring Cache** - 缓存抽象

### 其他
- **Spring AOP** - 面向切面编程
- **Spring Validation** - 参数校验

## 项目结构

```
volunteer-service-backend/
├── src/
│   ├── main/
│   │   ├── java/com/volunteer/
│   │   │   ├── config/              # 配置类
│   │   │   │   ├── MybatisPlusConfig.java    # MyBatis配置
│   │   │   │   ├── SecurityConfig.java       # 安全配置
│   │   │   │   ├── WebConfig.java           # Web配置
│   │   │   │   └── WebMvcConfig.java        # MVC配置
│   │   │   ├── controller/          # 控制器层
│   │   │   │   ├── ActivityController.java        # 活动管理
│   │   │   │   ├── AdminUserController.java       # 用户管理
│   │   │   │   ├── AuthController.java            # 认证登录
│   │   │   │   ├── CommunityController.java       # 社群管理
│   │   │   │   ├── RecruitmentController.java     # 招募管理
│   │   │   │   ├── RegistrationController.java    # 报名管理
│   │   │   │   ├── StatisticsController.java      # 数据统计
│   │   │   │   └── ...                           # 其他控制器
│   │   │   ├── service/             # 服务层
│   │   │   │   ├── ActivityService.java           # 活动服务
│   │   │   │   ├── AuthService.java               # 认证服务
│   │   │   │   ├── CommunityService.java          # 社群服务
│   │   │   │   └── ...                           # 其他服务
│   │   │   ├── mapper/              # 数据访问层
│   │   │   │   ├── ActivityMapper.java
│   │   │   │   ├── UserMapper.java
│   │   │   │   └── ...                           # 其他Mapper
│   │   │   ├── entity/              # 实体类
│   │   │   │   ├── Activity.java                  # 活动实体
│   │   │   │   ├── SysUser.java                   # 用户实体
│   │   │   │   ├── VolunteerUser.java            # 志愿者实体
│   │   │   │   └── ...                           # 其他实体
│   │   │   ├── dto/                 # 数据传输对象
│   │   │   ├── vo/                  # 视图对象
│   │   │   ├── exception/           # 异常处理
│   │   │   │   ├── BusinessException.java        # 业务异常
│   │   │   │   └── GlobalExceptionHandler.java   # 全局异常处理
│   │   │   ├── interceptor/         # 拦截器
│   │   │   │   └── JwtAuthenticationInterceptor.java  # JWT认证拦截器
│   │   │   ├── utils/               # 工具类
│   │   │   │   └── JwtUtils.java                 # JWT工具
│   │   │   └── VolunteerServiceApplication.java  # 启动类
│   │   └── resources/
│   │       ├── mapper/              # MyBatis XML映射文件
│   │       ├── application.yml      # 主配置文件
│   │       └── application-dev.yml  # 开发环境配置
│   └── test/                        # 测试代码
├── logs/                            # 日志目录
├── uploads/                         # 文件上传目录
├── target/                          # 编译输出目录
├── pom.xml                          # Maven项目配置
├── start.bat                        # Windows启动脚本
└── README.md                        # 项目说明文档
```

## 功能模块

### 1. 用户认证模块
- 用户注册、登录、登出
- JWT Token生成和验证
- 密码加密和重置
- 用户角色权限控制

### 2. 用户管理模块
- 用户信息CRUD操作
- 志愿者信息管理
- 用户状态管理
- 头像上传和管理

### 3. 活动管理模块
- 活动创建、编辑、发布、取消
- 活动分类管理
- 活动审核流程
- 活动状态管理
- 活动封面图片上传

### 4. 报名管理模块
- 活动报名申请
- 报名审核流程
- 报名状态查询

### 5. 签到管理模块
- 签到记录管理
- 二维码签到验证
- 位置签到验证
- 签到状态统计

### 6. 社群管理模块
- 社群创建和管理
- 社群成员管理

### 7. 招募管理模块
- 招募信息发布
- 招募申请管理
- 录用筛选流程
- 录用通知发送

### 8. 技能管理模块
- 技能分类管理
- 用户技能管理
- 证书管理
- 技能匹配推荐

### 9. 积分与激励模块
- 积分规则配置
- 积分记录管理
- 积分商城商品管理
- 积分兑换功能

### 10. 保险管理模块
- 保险产品管理
- 保险订单管理
- 保单查询

### 11. 数据统计模块
- 活动数据统计
- 用户数据统计
- 志愿者服务时长统计
- 综合数据报表

### 12. 系统管理模块
- 角色管理（RBAC）
- 菜单管理
- 权限控制

## 环境要求

- **JDK**: 1.8 或更高版本
- **Maven**: 3.6.0 或更高版本
- **MySQL**: 8.0 或更高版本
- **操作系统**: Windows/Linux/MacOS

## 安装与启动

### 1. 数据库准备

1. 创建数据库：

```sql
CREATE DATABASE volunteer_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：

```bash

# 导入表结构和测试数据
mysql -u root -p volunteer_db < database/02.表结构和测试数据初始化volunteer_db.sql
```

### 2. 配置文件修改

编辑 `src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/volunteer_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root  # 修改为你的数据库用户名
    password: 123456  # 修改为你的数据库密码
```

### 3. 编译项目

```bash
# 清理并编译项目
mvn clean compile

# 或者打包项目
mvn clean package
```

### 4. 启动项目

**方式一：使用Maven启动**

```bash
mvn spring-boot:run
```

**方式二：使用Java命令启动**

```bash
# 先打包
mvn clean package

# 启动jar包
java -jar target/volunteer-service-backend-1.0.0.jar
```

**方式三：使用启动脚本**

Windows:
```bash
start.bat
```

### 5. 验证启动

访问以下URL验证服务是否启动成功：
- 健康检查: http://localhost:8080/api/health
- Swagger文档: http://localhost:8080/api/swagger-ui.html (如果配置了)

## 配置说明

### 数据库配置

在 `application.yml` 中配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/volunteer_db
    username: root
    password: 123456
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
```

### JWT配置

```yaml
jwt:
  secret: your-jwt-secret-key
  expiration: 86400000  # 24小时，单位毫秒
  header: Authorization
  prefix: Bearer
```

### 文件上传配置

```yaml
file:
  upload:
    path: uploads  # 上传文件存储路径
    url-prefix: /uploads
    base-url: http://localhost:8080/api/uploads
    max-size: 10485760  # 10MB
```

### 日志配置

```yaml
logging:
  level:
    com.volunteer: debug
    org.springframework.security: debug
  file:
    name: logs/volunteer-service.log
```

## API接口说明

### 基础路径

所有API接口的基础路径为：`http://localhost:8080/api`

### 认证接口

- `POST /auth/login` - 用户登录
- `POST /auth/register` - 用户注册
- `POST /auth/logout` - 用户登出

### 活动接口

- `GET /activity/list` - 获取活动列表
- `GET /activity/{id}` - 获取活动详情
- `POST /activity/create` - 创建活动
- `PUT /activity/update` - 更新活动
- `DELETE /activity/{id}` - 删除活动

### 报名接口

- `POST /registration/register` - 活动报名
- `GET /registration/my-registrations` - 我的报名
- `PUT /registration/cancel/{id}` - 取消报名

更多接口详见API文档或Swagger配置。

## 注意事项

### 1. 数据库配置

- 确保MySQL服务已启动
- 确保数据库已创建并执行了初始化脚本
- 用户名密码配置正确
- 时区设置为Asia/Shanghai

### 2. 端口占用

- 默认端口为8080
- 如需修改，在 `application.yml` 中配置：

```yaml
server:
  port: 8081  # 修改为你想要的端口
```

### 3. 文件上传

- 确保 `uploads` 目录存在且有写权限
- 文件上传限制为10MB
- 支持的文件类型：图片、PDF等

### 4. JWT密钥

- 生产环境请修改JWT密钥
- 密钥长度建议至少32个字符
- 密钥泄露会带来安全风险

### 5. 日志管理

- 日志文件存储在 `logs` 目录
- 建议定期清理历史日志
- 生产环境建议配置日志轮转

### 6. 常见问题

**Q: 数据库连接失败？**
A: 检查MySQL服务是否启动，数据库名、用户名、密码是否正确

**Q: 端口8080被占用？**
A: Powershell中执行 `netstat -ano | findstr :8080` 查找占用进程，或修改端口配置

**Q: JWT认证失败？**
A: 检查请求头是否携带正确的Token，格式为 `Bearer {token}`

**Q: 文件上传失败？**
A: 检查uploads目录权限，确保有写权限

**Q: MyBatis报错？**
A: 检查mapper XML文件路径，确保mapper-locations配置正确

### 7. 性能优化

- 使用HikariCP连接池提升性能
- 启用Caffeine缓存减少数据库查询
- 数据库添加适当索引
- 大列表查询使用分页

### 8. 安全建议

- 生产环境修改JWT密钥
- 配置HTTPS协议
- 加强SQL注入防护
- 添加请求频率限制
- 配置CORS跨域策略

## 部署说明

### 开发环境部署

使用IDE（如IntelliJ IDEA）：
1. 导入Maven项目
2. 配置JDK版本
3. 配置数据库连接
4. 运行 `VolunteerServiceApplication`

### 生产环境部署

1. 打包项目：

```bash
mvn clean package -DskipTests
```

2. 上传jar包到服务器

3. 创建启动脚本

4. 使用nohup或systemd运行：

```bash
nohup java -jar volunteer-service-backend-wide-jar-with-dependencies.jar > logs/console.log 2>&1 &
```

5. 配置Nginx反向代理（可选）
## 联系方式

如有问题或建议，请联系开发团队

