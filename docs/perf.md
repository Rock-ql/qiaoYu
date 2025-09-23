# 性能测试（示例）

- 登录接口：ab -n 200 -c 20 -p login.json -T application/json http://localhost:8080/api/auth/login
- 活动列表：ab -n 500 -c 50 -p empty.json -T application/json http://localhost:8080/api/activity/available

建议：
- 观察 P95/P99 与失败率
- 后端打开 `management.metrics`，配合 JDK Flight Recorder/VisualVM 观察资源占用

