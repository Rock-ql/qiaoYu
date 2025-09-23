# 羽毛球约球与消费记录系统

一个面向小团体的约球与AA记账应用，支持活动约球、费用分摊、用户管理，并提供管理后台、H5 与小程序端入口。后端采用 Spring Boot 3 + Redis，接口统一使用 JWT 鉴权与 Result 返回。

## 功能概览
- 活动：创建/参加/退出/取消/开始/完成；按状态/时间/发起人查询
- 费用：记录费用、平均/自定义分摊、确认与标记已支付
- 账号：注册/登录、微信授权登录（示例）、用户资料管理
- 安全：JWT 鉴权、CORS、统一异常 JSON

## 目录结构
- `backend/`：Spring Boot 后端（:8080）
- `frontend-admin/`：管理后台（Vue3 + Element Plus）
- `frontend-h5/`：H5 端（Vue3 + Vant）
- `miniprogram/`：小程序（uni-app，含分包/预加载/分享配置）
- `docs/`：使用指南、E2E、OpenAPI、性能
- `deploy/`：部署模版（Compose、K8s、Nginx）

## 快速开始
前置：JDK17、Node.js≥20、Redis7（默认 localhost:6379）

- 启动后端：`make backend-run`
- 启动前端：`make admin-dev` 或 `make h5-dev`
- 健康检查：`GET http://localhost:8080/actuator/health`
- Swagger UI：`http://localhost:8080/swagger-ui.html`

更多细节见：`docs/PROJECT_GUIDE.md`

## 接口文档
- Swagger UI：`/swagger-ui.html`，OpenAPI：`/v3/api-docs`
- OpenAPI YAML：`docs/openapi.yaml`

## 构建与测试
- 一键构建：`make all-build`
- 前端单测：在各前端目录 `npm run test:unit`

## 部署
- Docker：根目录 `docker-compose.yml`
- 生产模版：`deploy/compose/docker-compose.prod.yml`
- K8s：`deploy/k8s/`（Deployment/Service/Ingress）
- Nginx：`deploy/nginx/nginx.conf`（静态托管与 /api 反向代理）

环境变量（关键）
- `SPRING_DATA_REDIS_HOST`、`SPRING_DATA_REDIS_PORT`
- `jwt.secret`（生产请使用 ≥32 字节安全随机串）
- `app.security.cors.allowed-origins`（生产建议限定域名）

## 文档速览
- 使用说明：`docs/usage.md`
- 端到端验证：`docs/e2e.md`
- 性能建议：`docs/perf.md`

