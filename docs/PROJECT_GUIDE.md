# 项目说明与部署指南

## 一、项目简介
羽毛球约球与消费记录系统，支持：
- 约球活动：创建/参加/退出/取消/开始/完成，按状态/时间/发起人查询
- 费用管理：记录费用、平均/自定义分摊、确认与标记已支付
- 账号体系：注册/登录（手机号+密码）、微信授权登录（示例）、用户资料管理
- 统一返回：所有接口返回 `Result{ code,message,data }`
- 安全：JWT 鉴权（`Authorization: Bearer <token>`）、CORS、统一异常JSON

## 二、技术栈与结构
- 后端：Java 17、Spring Boot 3、Spring Security、Redis（Lettuce/Pool）、springdoc-openapi
- 前端：
  - 管理后台（admin）：Vue3 + Vite + Element Plus + Pinia + Vitest
  - H5：Vue3 + Vite + Vant + Pinia + Vitest
  - 小程序：uni-app（示例骨架，分包/预加载/分享）
- 目录：
  - `backend/` 后端服务，端口 8080
  - `frontend-admin/` 管理端，开发端口 5173
  - `frontend-h5/` H5 端，开发端口 5173
  - `miniprogram/` 小程序代码（微信开发者工具导入）
  - `docs/` 使用、E2E、OpenAPI、性能文档

## 三、接口文档
- Swagger UI：`http://localhost:8080/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`
- OpenAPI YAML：`docs/openapi.yaml`

## 四、环境准备
- 必备：JDK 17、Node.js ≥ 20、Redis 7（默认 `localhost:6379`）
- 可选：Docker / Docker Compose（用于镜像构建与部署）

## 五、本地快速启动（推荐）
1) 启动 Redis（本地或容器）
2) 后端服务
```
make backend-run
```
3) 管理端 / H5 任一
```
make admin-dev     # 或 make h5-dev
```
4) 访问
- 管理端/H5：启动命令行输出的本地 URL
- 后端健康：`GET http://localhost:8080/actuator/health`
- Swagger：`http://localhost:8080/swagger-ui.html`

> 或者一次性构建：`make all-build`

## 六、端到端验证（示例）
文档：`docs/e2e.md`
- 注册：`POST /api/auth/register` → 登录获取 token
- 携带 `Authorization: Bearer <token>`
- 创建活动：`POST /api/activity/create`
- 查询可参加活动：`POST /api/activity/available`

## 七、配置与环境变量
- Redis：
  - `SPRING_DATA_REDIS_HOST`（默认 `localhost`）
  - `SPRING_DATA_REDIS_PORT`（默认 `6379`）
- JWT：
  - `jwt.secret`（生产务必设置为≥32字节安全随机串）
  - `jwt.expireMinutes`（默认 43200，30天）
- CORS：
  - `app.security.cors.allowed-origins`（默认 `*`，生产建议限定域名）

## 八、生产部署
- 方式A：Docker 镜像（已内置 `backend/Dockerfile`）
```
# 本地构建
cd backend && docker build -t badminton-backend:0.1.0 .
# 运行（按需覆盖环境变量）
docker run -d --name badminton-backend \
  -e SPRING_DATA_REDIS_HOST=redis \
  -e SPRING_DATA_REDIS_PORT=6379 \
  -e jwt.secret="<secure-32-bytes>" \
  -p 8080:8080 badminton-backend:0.1.0
```
- 方式B：Compose（根目录 `docker-compose.yml`）
```
docker compose up -d
```
- 方式C：CI 发布（GHCR 示例）
  - 打 tag：`git tag v0.1.0 && git push --tags`
  - Actions 自动构建并推送镜像 `ghcr.io/<owner>/badminton-backend`（见 `.github/workflows/release.yml`）

前端生产部署：
```
# 管理端
cd frontend-admin && npm ci || npm install && npm run build
# H5
cd frontend-h5 && npm ci || npm install && npm run build
# 将 dist/ 部署至静态服务器（Nginx/OSS/静态托管均可）
```

## 九、常见问题
- 401/403：检查是否携带 Bearer Token，或账号权限
- Redis 连接失败：确认服务可达、主机/端口/密码是否正确
- JWT 秘钥不安全：生产环境设置 `jwt.secret` ≥ 32 字节

## 十、更多资料
- 使用说明：`docs/usage.md`
- 性能建议：`docs/perf.md`
- OpenAPI：`docs/openapi.yaml`
- 任务清单与进度：`specs/001-/tasks.md`

