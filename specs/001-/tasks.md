# Tasks: 羽毛球约球与消费记录系统

**Input**: Design documents from `/specs/001-/`
**Prerequisites**: plan.md, research.md, data-model.md, contracts/, quickstart.md

## 项目结构
```
qiaoYu/
├── backend/                    # Spring Boot 后端
├── frontend-admin/            # Vue3 后台管理系统
├── frontend-h5/               # Vue3 H5 移动端
├── miniprogram/               # 微信小程序
└── docs/                      # 项目文档
```

## Phase 3.1: 项目初始化
- [x] T001 创建项目根目录结构，包含backend、frontend-admin、frontend-h5、miniprogram四个子项目
- [x] T002 [P] 初始化Spring Boot后端项目 in backend/，配置Java 17 + Spring Boot 3.x + Spring Data Redis
- [x] T003 [P] 初始化Vue3后台管理系统脚手架 in frontend-admin/，使用Vue CLI + Element Plus
- [x] T004 [P] 初始化Vue3 H5移动端项目 in frontend-h5/，使用Vite + Vant
- [x] T005 [P] 初始化微信小程序项目 in miniprogram/，配置uni-app框架支持微信快捷登录
- [x] T006 [P] 配置后端代码规范工具：Checkstyle + SpotBugs in backend/
- [x] T007 [P] 配置前端代码规范工具：ESLint + Prettier in frontend-admin/
- [x] T008 [P] 配置H5代码规范工具：ESLint + Prettier in frontend-h5/
- [x] T009 配置Docker Compose开发环境，包含Redis服务

## Phase 3.2: 测试优先开发 ⚠️ 必须在实现前完成
**关键：这些测试必须编写并失败，然后才能开始任何实现**

### 合约测试 (基于API规范)
- [x] T010 [P] 用户注册接口合约测试 in backend/src/test/java/contract/AuthRegisterContractTest.java
- [x] T011 [P] 用户登录接口合约测试 in backend/src/test/java/contract/AuthLoginContractTest.java
- [x] T012 [P] 微信登录接口合约测试 in backend/src/test/java/contract/WechatLoginContractTest.java
- [x] T013 [P] 活动列表接口合约测试 in backend/src/test/java/contract/ActivityListContractTest.java
- [x] T014 [P] 创建活动接口合约测试 in backend/src/test/java/contract/ActivityCreateContractTest.java
- [x] T015 [P] 参加活动接口合约测试 in backend/src/test/java/contract/ActivityJoinContractTest.java
- [x] T016 [P] 费用记录接口合约测试 in backend/src/test/java/contract/ExpenseContractTest.java
- [x] T017 [P] 用户信息接口合约测试 in backend/src/test/java/contract/UserProfileContractTest.java

### 集成测试 (基于用户场景)
- [x] T018 [P] 用户注册登录流程集成测试 in backend/src/test/java/integration/UserRegistrationFlowTest.java
- [x] T019 [P] 微信授权登录流程集成测试 in backend/src/test/java/integration/WechatAuthFlowTest.java
- [x] T020 [P] 创建并参加约球活动集成测试 in backend/src/test/java/integration/BookingActivityFlowTest.java
- [x] T021 [P] 费用记录和分摊集成测试 in backend/src/test/java/integration/ExpenseManagementFlowTest.java
- [x] T022 [P] Redis数据一致性集成测试 in backend/src/test/java/integration/RedisDataConsistencyTest.java

## Phase 3.3: 核心数据模型实现 (仅在测试失败后)

### 数据模型 (基于data-model.md)
- [x] T023 [P] 用户实体模型 in backend/src/main/java/cn/badminton/model/User.java
- [x] T024 [P] 约球活动实体模型 in backend/src/main/java/cn/badminton/model/BookingActivity.java
- [x] T025 [P] 参与记录实体模型 in backend/src/main/java/cn/badminton/model/Participation.java
- [x] T026 [P] 费用记录实体模型 in backend/src/main/java/cn/badminton/model/ExpenseRecord.java
- [x] T027 [P] 费用分摊实体模型 in backend/src/main/java/cn/badminton/model/ExpenseShare.java

### Redis存储层
- [x] T028 [P] 用户Redis存储库 in backend/src/main/java/cn/badminton/repository/UserRepository.java
- [x] T029 [P] 活动Redis存储库 in backend/src/main/java/cn/badminton/repository/ActivityRepository.java
- [x] T030 [P] 费用Redis存储库 in backend/src/main/java/cn/badminton/repository/ExpenseRepository.java
- [x] T031 Redis配置类 in backend/src/main/java/cn/badminton/config/RedisConfig.java

## Phase 3.4: 业务服务层实现

### 核心业务服务
- [x] T032 [P] 用户管理服务 in backend/src/main/java/cn/badminton/service/UserService.java
- [x] T033 [P] 认证服务(包含微信登录) in backend/src/main/java/cn/badminton/service/AuthService.java
- [x] T034 [P] 活动管理服务 in backend/src/main/java/cn/badminton/service/ActivityService.java
- [x] T035 [P] 费用管理服务 in backend/src/main/java/cn/badminton/service/ExpenseService.java
- [x] T036 [P] 微信API集成服务 in backend/src/main/java/cn/badminton/service/WechatService.java

### DTO和验证
- [x] T037 [P] 请求响应DTO类 in backend/src/main/java/cn/badminton/dto/
- [x] T038 [P] 数据验证器 in backend/src/main/java/cn/badminton/validator/
- [x] T039 统一响应结果类 in backend/src/main/java/cn/badminton/common/Result.java

## Phase 3.5: API控制器实现

### 认证相关接口
- [x] T040 用户注册登录控制器 in backend/src/main/java/cn/badminton/controller/AuthController.java
- [x] T041 微信授权登录端点 (在AuthController中添加/auth/wechat路径)

### 业务功能接口
- [x] T042 活动管理控制器 in backend/src/main/java/cn/badminton/controller/ActivityController.java
- [x] T043 费用管理控制器 in backend/src/main/java/cn/badminton/controller/ExpenseController.java
- [x] T044 用户信息控制器 in backend/src/main/java/cn/badminton/controller/UserController.java

## Phase 3.6: 中间件和安全配置

### 安全和认证
- [x] T045 JWT认证配置 in backend/src/main/java/cn/badminton/config/SecurityConfig.java
- [x] T046 JWT工具类 in backend/src/main/java/cn/badminton/util/JwtUtil.java
- [x] T047 认证过滤器 in backend/src/main/java/cn/badminton/filter/JwtAuthenticationFilter.java

### 系统配置
- [x] T048 全局异常处理器 in backend/src/main/java/cn/badminton/exception/GlobalExceptionHandler.java
- [x] T049 CORS和安全头配置 (在SecurityConfig中配置)
- [x] T050 请求响应日志拦截器 in backend/src/main/java/cn/badminton/interceptor/LoggingInterceptor.java

## Phase 3.7: 前端管理后台实现

### 后台管理基础框架
- [x] T051 [P] 后台登录页面 in frontend-admin/src/views/Login.vue
- [x] T052 [P] 后台主框架布局 in frontend-admin/src/layouts/BasicLayout.vue  
- [x] T053 [P] 路由配置和权限控制 in frontend-admin/src/router/index.ts
- [x] T054 [P] 状态管理配置 in frontend-admin/src/stores/

### 业务管理页面
- [ ] T055 [P] 用户管理页面 in frontend-admin/src/views/user/UserManagement.vue
- [ ] T056 [P] 活动管理页面 in frontend-admin/src/views/activity/ActivityManagement.vue
- [ ] T057 [P] 费用统计页面 in frontend-admin/src/views/expense/ExpenseStatistics.vue
- [ ] T058 [P] 系统设置页面 in frontend-admin/src/views/system/Settings.vue

## Phase 3.8: H5移动端实现

### H5基础框架
- [x] T059 [P] H5登录注册页面 in frontend-h5/src/views/auth/Login.vue
- [x] T060 [P] H5主页和导航 in frontend-h5/src/views/Home.vue
- [x] T061 [P] H5路由和状态管理配置 in frontend-h5/src/router/index.ts

### H5业务页面
- [x] T062 [P] 活动列表页面 in frontend-h5/src/views/activity/ActivityList.vue
- [x] T063 [P] 活动详情页面 in frontend-h5/src/views/activity/ActivityDetail.vue
- [x] T064 [P] 创建活动页面 in frontend-h5/src/views/activity/CreateActivity.vue
- [x] T065 [P] 费用记录页面 in frontend-h5/src/views/expense/ExpenseRecord.vue
- [x] T066 [P] 个人中心页面 in frontend-h5/src/views/user/Profile.vue

## Phase 3.9: 微信小程序实现

### 小程序基础配置
- [ ] T067 [P] 小程序基础配置和页面结构 in miniprogram/app.json
- [ ] T068 [P] 微信授权登录页面 in miniprogram/pages/auth/login.vue
- [ ] T069 [P] 小程序主页标签页 in miniprogram/pages/index/index.vue

### 小程序业务页面
- [ ] T070 [P] 活动列表页面 in miniprogram/pages/activity/list.vue
- [ ] T071 [P] 活动详情页面 in miniprogram/pages/activity/detail.vue
- [ ] T072 [P] 发起约球页面 in miniprogram/pages/activity/create.vue
- [ ] T073 [P] 费用分摊页面 in miniprogram/pages/expense/share.vue
- [ ] T074 [P] 个人中心页面 in miniprogram/pages/user/profile.vue

### 小程序特殊功能
- [ ] T075 微信API调用封装 in miniprogram/utils/wechat.js
- [ ] T076 小程序分享功能配置 (在各页面添加分享配置)

## Phase 3.10: 系统集成和优化

### API集成
- [x] T077 [P] 后台管理系统API集成 in frontend-admin/src/api/
- [x] T078 [P] H5移动端API集成 in frontend-h5/src/api/
- [ ] T079 [P] 小程序API集成 in miniprogram/api/

### 性能优化
- [ ] T080 [P] Redis连接池优化和监控 in backend/src/main/java/cn/badminton/config/
- [ ] T081 [P] 前端构建优化和代码分割 in frontend-*/vite.config.ts
- [ ] T082 [P] 小程序性能优化配置 in miniprogram/

## Phase 3.11: 测试和文档完善

### 单元测试
- [ ] T083 [P] 业务服务单元测试 in backend/src/test/java/unit/service/
- [ ] T084 [P] 工具类单元测试 in backend/src/test/java/unit/util/
- [ ] T085 [P] 前端组件单元测试 in frontend-*/src/tests/

### 系统测试
- [ ] T086 端到端测试：完整约球流程 (执行quickstart.md场景验证)
- [ ] T087 性能测试：API响应时间<200ms验证
- [ ] T088 [P] 文档更新：API文档和用户手册 in docs/

## 依赖关系

### 关键依赖
- 项目初始化 (T001-T009) → 所有其他任务
- 测试编写 (T010-T022) → 实现任务 (T023-T076)
- 数据模型 (T023-T027) → 存储层 (T028-T031) → 服务层 (T032-T039)
- 服务层完成 → API控制器 (T040-T044)
- 后端API完成 → 前端集成 (T077-T079)
- 所有实现完成 → 系统集成和测试 (T080-T088)

### 并行执行组
```bash
# 项目初始化并行组
Task: "初始化Spring Boot后端项目 in backend/"
Task: "初始化Vue3后台管理系统脚手架 in frontend-admin/"  
Task: "初始化Vue3 H5移动端项目 in frontend-h5/"
Task: "初始化微信小程序项目 in miniprogram/"

# 合约测试并行组
Task: "用户注册接口合约测试 in backend/src/test/java/contract/AuthRegisterContractTest.java"
Task: "用户登录接口合约测试 in backend/src/test/java/contract/AuthLoginContractTest.java"
Task: "微信登录接口合约测试 in backend/src/test/java/contract/WechatLoginContractTest.java"
Task: "活动列表接口合约测试 in backend/src/test/java/contract/ActivityListContractTest.java"

# 数据模型并行组
Task: "用户实体模型 in backend/src/main/java/cn/badminton/model/User.java"
Task: "约球活动实体模型 in backend/src/main/java/cn/badminton/model/BookingActivity.java"
Task: "费用记录实体模型 in backend/src/main/java/cn/badminton/model/ExpenseRecord.java"
```

## 特殊说明

### 微信小程序登录集成
- T068、T075需要配置微信小程序AppID和AppSecret
- 实现微信授权登录、获取用户信息、静默登录
- 后端T033、T036需要集成微信API验证登录凭证

### 脚手架使用要求
- 所有前端项目必须基于开源脚手架初始化，不从零开始
- 后台管理使用Vue CLI + Element Plus企业级模板
- H5使用Vite + Vant移动端模板
- 小程序使用uni-app官方模板

### 复杂度控制
- 无支付功能：费用记录仅用于记账，不涉及真实支付
- 无消息队列：通知功能使用轮询或WebSocket实现
- 单Redis存储：简化数据架构，所有数据存储在Redis

## 验证清单

- [x] 所有API接口都有对应的合约测试
- [x] 所有数据模型都有对应的实体和存储任务
- [x] 所有测试任务都在实现任务之前
- [x] 并行任务操作不同文件，无冲突
- [x] 每个任务都指定了具体的文件路径
- [x] 微信小程序快捷登录需求已包含
- [x] 后台管理脚手架需求已包含

**总任务数**: 88个任务
**预计完成时间**: 4-6周 (2-3人开发团队)
**关键里程碑**: T022(测试完成) → T050(后端完成) → T079(前端完成) → T088(系统完成)
