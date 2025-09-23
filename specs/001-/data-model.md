# 羽毛球约球与消费记录系统数据模型

## 核心实体设计

### 1. 用户(User)

**Redis存储结构**: `badminton:user:{user_id}` (Hash)

```json
{
  "id": "string",           // 用户唯一标识
  "phone": "string",        // 手机号(登录凭证)
  "nickname": "string",     // 用户昵称
  "avatar": "string",       // 头像URL
  "status": "number",       // 状态: 1-正常 2-禁用
  "createdAt": "timestamp", // 创建时间
  "updatedAt": "timestamp", // 更新时间
  "totalActivities": "number", // 参与活动总数
  "totalExpense": "number"  // 总消费金额
}
```

**关联数据**:
- `badminton:user:activities:{user_id}` (Set) - 用户参与的活动ID列表
- `badminton:user:friends:{user_id}` (Set) - 用户好友ID列表

### 2. 约球活动(BookingActivity)

**Redis存储结构**: `badminton:activity:{activity_id}` (Hash)

```json
{
  "id": "string",           // 活动唯一标识
  "title": "string",        // 活动标题
  "organizer": "string",    // 发起人用户ID
  "venue": "string",        // 场地名称
  "address": "string",      // 详细地址
  "startTime": "timestamp", // 开始时间
  "endTime": "timestamp",   // 结束时间
  "maxPlayers": "number",   // 最大人数
  "currentPlayers": "number", // 当前人数
  "fee": "number",          // 预估费用
  "description": "string",  // 活动描述
  "status": "number",       // 状态: 1-待确认 2-进行中 3-已完成 4-已取消
  "createdAt": "timestamp", // 创建时间
  "updatedAt": "timestamp"  // 更新时间
}
```

**关联数据**:
- `badminton:activity:participants:{activity_id}` (Set) - 参与者用户ID列表
- `badminton:activity:expenses:{activity_id}` (Set) - 活动费用记录ID列表

### 3. 参与记录(Participation)

**Redis存储结构**: `badminton:participation:{participation_id}` (Hash)

```json
{
  "id": "string",           // 参与记录ID
  "activityId": "string",   // 约球活动ID
  "userId": "string",       // 参与者用户ID
  "joinTime": "timestamp",  // 加入时间
  "status": "number",       // 状态: 1-已确认 2-已取消
  "isOrganizer": "boolean"  // 是否为发起人
}
```

### 4. 费用记录(ExpenseRecord)

**Redis存储结构**: `badminton:expense:{expense_id}` (Hash)

```json
{
  "id": "string",           // 费用记录ID
  "activityId": "string",   // 关联活动ID
  "payerId": "string",      // 支付人用户ID
  "type": "string",         // 费用类型: venue-场地费 food-餐饮费 transport-交通费 other-其他
  "description": "string",  // 费用描述
  "totalAmount": "number",  // 总金额
  "splitMethod": "string",  // 分摊方式: equal-平均分摊 custom-自定义分摊
  "createdAt": "timestamp", // 创建时间
  "updatedAt": "timestamp"  // 更新时间
}
```

**关联数据**:
- `badminton:expense:shares:{expense_id}` (Set) - 费用分摊记录ID列表

### 5. 费用分摊(ExpenseShare)

**Redis存储结构**: `badminton:share:{share_id}` (Hash)

```json
{
  "id": "string",           // 分摊记录ID
  "expenseId": "string",    // 费用记录ID
  "userId": "string",       // 分摊用户ID
  "amount": "number",       // 应付金额
  "status": "number",       // 状态: 1-待结算 2-已结算
  "settledAt": "timestamp"  // 结算时间
}
```

## 数据关系映射

### 用户 -> 活动关系
```
badminton:user:activities:{user_id} = {activity_id1, activity_id2, ...}
```

### 活动 -> 参与者关系
```
badminton:activity:participants:{activity_id} = {user_id1, user_id2, ...}
```

### 活动 -> 费用关系
```
badminton:activity:expenses:{activity_id} = {expense_id1, expense_id2, ...}
```

### 费用 -> 分摊关系
```
badminton:expense:shares:{expense_id} = {share_id1, share_id2, ...}
```

## 索引设计

### 按时间查询活动
```
badminton:activities:by_date:{date} = {activity_id1, activity_id2, ...} (Set)
```

### 按场地查询活动
```
badminton:activities:by_venue:{venue_hash} = {activity_id1, activity_id2, ...} (Set)
```

### 用户欠款统计
```
badminton:user:debts:{user_id} = {share_id1, share_id2, ...} (Set)
```

## 数据验证规则

### 用户数据验证
- phone: 11位中国手机号格式
- nickname: 2-20个字符，不能包含特殊字符
- avatar: 有效的URL格式

### 活动数据验证
- title: 5-50个字符
- startTime: 必须晚于当前时间
- endTime: 必须晚于startTime
- maxPlayers: 2-20人
- fee: 非负数，最多2位小数

### 费用数据验证
- totalAmount: 大于0，最多2位小数
- type: 枚举值验证
- splitMethod: 枚举值验证

## 状态转换规则

### 活动状态转换
```
待确认(1) -> 进行中(2) -> 已完成(3)
待确认(1) -> 已取消(4)
进行中(2) -> 已取消(4)
```

### 费用分摊状态转换
```
待结算(1) -> 已结算(2)
```

## Redis数据过期策略

### TTL设置
- 已取消活动: 30天后删除
- 已完成活动: 1年后删除
- 用户会话: 7天过期
- 验证码: 5分钟过期

### 内存优化
- 使用Hash压缩编码
- 设置合理的过期时间
- 定期清理无效数据

## 备份与恢复

### 定期备份
- 每日RDB快照备份
- AOF文件实时备份
- 关键数据异地备份

### 数据恢复策略
- RDB + AOF混合恢复
- 分级数据恢复优先级
- 数据一致性检查机制