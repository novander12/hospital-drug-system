# 医院药品管理系统前端

这是一个使用 Vue 3 和 Element Plus 构建的医院药品管理系统前端界面。

## 功能特性

- 药品列表展示
- 药品搜索
- 新增/编辑药品
- 删除单个药品
- 批量删除药品

## 技术栈

- Vue 3 组合式 API
- Element Plus UI 框架
- Axios 用于 HTTP 请求
- Vite 作为构建工具

## 开发环境设置

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

## 后端 API

系统使用以下 API 端点：

- `GET /api/drugs` - 获取所有药品
- `GET /api/drugs/{id}` - 获取单个药品
- `POST /api/drugs` - 创建新药品
- `PUT /api/drugs/{id}` - 更新药品信息
- `DELETE /api/drugs/{id}` - 删除药品
- `DELETE /api/drugs/batch` - 批量删除药品

## 目录结构

```
src/
  main.js           # 应用入口文件
  DrugManagement.vue # 药品管理主组件
``` 