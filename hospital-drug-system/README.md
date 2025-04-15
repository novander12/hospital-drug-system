# 医院药品管理系统

这是一个基于Spring Boot和Vue3开发的医院药品管理系统，用于管理药品信息。

## 技术栈

### 后端
- Spring Boot 2.7.5
- Spring Data JPA
- H2数据库
- Hibernate

### 前端
- Vue 3 (组合式API)
- Element Plus
- Axios
- Vite

## 功能特性

- 用户登录认证
- 药品CRUD操作
- 批量删除药品
- 用户管理（未实现）

## 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           └── hospital/
│   │               ├── config/         # 配置类
│   │               ├── controller/     # 控制器
│   │               ├── model/          # 实体类
│   │               └── repository/     # 数据访问层
│   │               
│   └── resources/
│       ├── static/                     # 前端资源
│       │   ├── js/                     # Vue组件和脚本
│       │   │   ├── App.vue             # 主应用组件
│       │   │   ├── DrugManagement.vue  # 药品管理组件
│       │   │   ├── LoginPage.vue       # 登录页面组件
│       │   │   └── main.js             # 入口JS文件
│       │   ├── index.html              # HTML入口
│       │   ├── package.json            # NPM配置
│       │   └── vite.config.js          # Vite配置
│       └── application.properties      # 应用配置
```

## 运行项目

### 后端
```bash
# 使用Maven运行
mvn spring-boot:run
```

### 前端(开发模式)
```bash
# 进入前端目录
cd src/main/resources/static

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

## 默认用户

系统初始化时会创建以下用户：

- 管理员账户: admin / admin123
- 普通用户: user / user123

## API端点

### 认证
- `POST /api/auth/login` - 用户登录

### 药品管理
- `GET /api/drugs` - 获取所有药品
- `GET /api/drugs/{id}` - 获取单个药品
- `POST /api/drugs` - 创建新药品
- `PUT /api/drugs/{id}` - 更新药品
- `DELETE /api/drugs/{id}` - 删除药品
- `DELETE /api/drugs/batch` - 批量删除药品 