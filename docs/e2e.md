# 端到端测试（E2E）

- 环境：本机 Redis 6379，后端 `./mvnw spring-boot:run`
- 场景：注册 → 登录 → 创建活动 → 查询可参加活动

## API 序列
1. POST `/api/auth/register` { phone, nickname, password }
2. POST `/api/auth/login` { phone, password } → 拿到 token
3. POST `/api/activity/create` { organizerId, title, venue, startTime, endTime, maxPlayers }
4. POST `/api/activity/available` {}

可用工具：curl / httpie / Postman。若使用 curl，请在 step2 后设置请求头 `Authorization: Bearer {token}`。

