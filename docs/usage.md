# 使用说明

## 后端
- 运行：`cd backend && ./mvnw spring-boot:run`
- 健康检查：`GET /actuator/health`
- Redis：默认 `localhost:6379`

## 管理后台（admin）
- 启动：`cd frontend-admin && npm run dev`
- 登录：使用注册后账号，或在登录页直接输入测试账号后调用后端登录

## H5
- 启动：`cd frontend-h5 && npm run dev`
- 功能：登录/活动列表与详情/活动创建/费用记录/个人中心

## 小程序
- 以 uni-app 方式导入 `miniprogram`，使用微信开发者工具预览
- 已启用分包与预加载，提升首屏速度

