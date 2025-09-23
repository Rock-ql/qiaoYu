# 羽毛球约球与消费记录系统快速开始指南

## 环境准备

### 开发环境要求
- **Java**: OpenJDK 17+
- **Node.js**: 18.0+
- **Redis**: 7.0+
- **Git**: 2.30+

### 推荐IDE
- **后端**: IntelliJ IDEA 2024.1+
- **前端**: VS Code + Volar插件

## 快速启动

### 1. 克隆项目

```bash
git clone <repository-url>
cd qiaoYu
```

### 2. 启动Redis

```bash
# 使用Docker启动Redis
docker run -d --name badminton-redis \
  -p 6379:6379 \
  -v redis-data:/data \
  redis:7-alpine redis-server --appendonly yes

# 或使用本地Redis
redis-server --appendonly yes
```

### 3. 后端启动

```bash
cd backend

# 安装依赖并启动
./mvnw spring-boot:run

# 或使用IDE启动main方法
# cn.badminton.Application
```

**验证后端启动**:
```bash
curl http://localhost:8080/api/health
# 预期返回: {"status": "UP"}
```

### 4. 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 访问 http://localhost:5173
```

## 核心功能验证

### 场景1: 用户注册和登录

```bash
# 1. 用户注册
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138001",
    "nickname": "小明",
    "password": "123456"
  }'

# 2. 用户登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138001", 
    "password": "123456"
  }'

# 保存返回的token用于后续请求
export TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 场景2: 创建约球活动

```bash
# 创建约球活动
curl -X POST http://localhost:8080/api/activities \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "周末羽毛球约球",
    "venue": "体育中心羽毛球馆",
    "address": "北京市朝阳区体育中心路1号",
    "startTime": "2025-09-30T09:00:00",
    "endTime": "2025-09-30T11:00:00",
    "maxPlayers": 4,
    "fee": 50.00,
    "description": "技术水平不限，重在参与！"
  }'

# 获取活动列表
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/activities?page=1&size=10"
```

### 场景3: 参加活动

```bash
# 注册第二个用户
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138002",
    "nickname": "小红",
    "password": "123456"
  }'

# 第二个用户登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138002",
    "password": "123456"
  }'

export TOKEN2="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# 参加活动 (需要从上一步获取activity_id)
curl -X POST http://localhost:8080/api/activities/{activity_id}/join \
  -H "Authorization: Bearer $TOKEN2"
```

### 场景4: 记录费用

```bash
# 活动发起人记录费用
curl -X POST http://localhost:8080/api/activities/{activity_id}/expenses \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "venue",
    "description": "场地费用",
    "totalAmount": 100.00,
    "splitMethod": "equal",
    "participants": [
      {"userId": "user1_id"},
      {"userId": "user2_id"}
    ]
  }'

# 查看费用分摊
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/activities/{activity_id}/expenses"
```

## 前端界面验证

### 1. 访问应用首页
打开浏览器访问: `http://localhost:5173`

### 2. 用户注册流程
1. 点击"注册"按钮
2. 填写手机号、昵称、密码
3. 点击"注册"完成账户创建

### 3. 创建约球活动
1. 登录后点击"发起约球"
2. 填写活动信息：
   - 活动标题：周末羽毛球
   - 场地：体育中心
   - 时间：选择未来时间
   - 人数：4人
   - 费用：50元
3. 点击"发布活动"

### 4. 查看活动列表
- 首页显示所有活动
- 可以按时间、状态筛选
- 点击活动查看详情

### 5. 费用记录
1. 进入已完成的活动详情
2. 点击"记录费用"
3. 添加不同类型费用：
   - 场地费：100元
   - 餐饮费：80元
4. 选择参与人员
5. 系统自动计算分摊金额

## 数据验证

### Redis数据查看

```bash
# 连接Redis
redis-cli

# 查看用户数据
HGETALL badminton:user:user1_id

# 查看活动数据  
HGETALL badminton:activity:activity1_id

# 查看用户参与的活动
SMEMBERS badminton:user:activities:user1_id

# 查看活动参与者
SMEMBERS badminton:activity:participants:activity1_id

# 查看费用记录
HGETALL badminton:expense:expense1_id
```

### 日志查看

```bash
# 后端日志
tail -f backend/logs/application.log

# 前端开发日志
# 浏览器控制台查看
```

## 性能验证

### API响应时间测试

```bash
# 安装Apache Bench
apt-get install apache2-utils

# 测试登录接口性能
ab -n 1000 -c 10 -T 'application/json' -p login.json \
  http://localhost:8080/api/auth/login

# 测试活动列表接口
ab -n 1000 -c 10 -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/activities
```

### 内存使用监控

```bash
# 查看Redis内存使用
redis-cli info memory

# 查看Java应用内存
jcmd <pid> VM.system_properties | grep heap
```

## 常见问题排查

### 后端启动失败
1. **检查Java版本**: `java -version`
2. **检查Redis连接**: `redis-cli ping`  
3. **查看启动日志**: `tail -f logs/application.log`

### 前端启动失败
1. **检查Node版本**: `node -v`
2. **清除依赖**: `rm -rf node_modules && npm install`
3. **检查端口占用**: `lsof -i :5173`

### 接口调用失败
1. **检查网络连接**: `curl -I http://localhost:8080/api/health`
2. **验证Token**: JWT token是否有效
3. **检查请求格式**: Content-Type和请求体格式

### Redis数据异常
1. **检查Redis状态**: `redis-cli ping`
2. **查看Redis日志**: `redis-cli client list`  
3. **重启Redis**: `docker restart badminton-redis`

## 下一步

✅ 完成快速启动验证后，可以进行：

1. **功能扩展**: 添加新的业务功能
2. **界面优化**: 改进用户体验设计
3. **性能调优**: 优化数据库查询和缓存策略
4. **部署上线**: 配置生产环境部署

更多详细文档请参考：
- [API文档](./contracts/api-specification.yaml)
- [数据模型](./data-model.md)
- [技术研究](./research.md)