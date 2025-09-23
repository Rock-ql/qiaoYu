# Repository Guidelines

## 项目结构与模块组织
- `backend/`：Spring Boot 3 + Maven，源码在 `src/main/java`，测试在 `src/test/java`，资源在 `src/main/resources`。
- `frontend-admin/`：Vue 3 + Element Plus（Vite）。
- `frontend-h5/`：Vue 3 + Vant（Vite）。
- `miniprogram/`：小程序/跨端资源（见 `manifest.json`、`pages.json`）。
- `specs/`：规格与计划文档；`docs/`：补充文档。
- `docker-compose.yml`：本地 Redis 与后端编排。

## 构建、测试与本地开发
- 后端：`cd backend`
  - 构建与检查：`./mvnw clean verify`（含 Checkstyle/SpotBugs/测试）
  - 运行：`./mvnw spring-boot:run`
  - 测试：`./mvnw test`
- 前端（两端一致，示例以 admin）：`cd frontend-admin`
  - 安装：`npm install`
  - 开发：`npm run dev`；构建：`npm run build`；预览：`npm run preview`
  - 质量：`npm run lint`，`npm run format`，单测：`npm run test:unit`
- Docker：`docker compose up -d`（启动 Redis 与后端，端口 6379/8080）

## 代码风格与命名约定
- Java：遵循 `backend/checkstyle.xml` 与 SpotBugs；包名小写，类名 PascalCase，方法/变量 camelCase；4 空格缩进。
- TS/Vue：ESLint + Prettier（2 空格缩进）；组件文件 PascalCase（如 `UserCard.vue`），工具与普通文件 kebab-case；类型/接口使用 PascalCase。

## 测试规范
- 后端：JUnit 5（`@SpringBootTest`）；测试放 `src/test/java`，类名以 `*Tests` 结尾；可用 Testcontainers 进行 Redis 集成测试。
- 前端：Vitest；测试文件以 `*.spec.ts` 命名，靠近被测代码或 `tests/` 目录；CI 需通过单测与 Lint。

## 提交与 Pull Request
- Commit：推荐 Conventional Commits + 中文描述（如：`feat(backend): 新增登录接口`）。
- PR：包含变更说明、影响范围、测试要点；前端附关键截图；关联 `specs/` 中的相关文档。

## 安全与配置提示
- 要求：JDK 17、Node.js 20+；默认端口：后端 8080、前端 5173。
- 后端环境：`SPRING_DATA_REDIS_HOST/PORT`；前端使用 Vite 环境变量（`VITE_*`）。

## 代理协作（可选）
- 使用分阶段流程：`/spec → /plan → /do`；在 `/do` 前仅修改文档，不改动源码。
