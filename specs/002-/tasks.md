# Tasks: 后台管理页面样式优化与完善

**Input**: Design documents from `/specs/002-/`
**Prerequisites**: plan.md, research.md, data-model.md, contracts/admin-api.yaml, quickstart.md

## Tech Stack Summary
- **Frontend**: Vue 3.x + TypeScript + Element Plus + Vue Router + Axios
- **Backend**: Java 17 + Spring Boot 3.x + MyBatis Plus  
- **Database**: MySQL 8.0 + Redis 7.0
- **Testing**: Vitest (前端), JUnit 5 (后端)
- **Structure**: Web app (frontend-admin/ + backend/ 分离架构)

## Path Conventions
- **Frontend**: `frontend-admin/src/`
- **Backend**: `backend/src/main/java/`
- **Tests**: `frontend-admin/tests/`, `backend/src/test/java/`

## Phase 3.1: 项目设置与依赖配置

- [x] T001 检查并完善前端项目依赖，确保Element Plus、Vue Router等已正确安装配置
- [x] T002 [P] 配置前端TypeScript编译选项和ESLint规则，支持Vue 3.x语法
- [x] T003 [P] 配置前端项目的样式预处理器(SCSS)和主题变量
- [x] T004 创建后台管理的路由配置文件 `frontend-admin/src/router/admin.ts`

## Phase 3.2: 测试先行 (TDD) ⚠️ 必须在实现前完成

**关键要求**: 这些测试必须编写完成并处于失败状态，然后才能开始实现代码

### API合约测试 [P]
- [x] T005 [P] Dashboard API合约测试 `frontend-admin/tests/api/dashboard.test.ts`
- [x] T006 [P] Users API合约测试 `frontend-admin/tests/api/users.test.ts`  
- [x] T007 [P] Activities API合约测试 `frontend-admin/tests/api/activities.test.ts`
- [x] T008 [P] Expenses API合约测试 `frontend-admin/tests/api/expenses.test.ts`

### 组件集成测试 [P]
- [x] T009 [P] 登录页面集成测试 `frontend-admin/tests/views/Login.test.ts`
- [x] T010 [P] 用户管理页面集成测试 `frontend-admin/tests/views/UserManagement.test.ts`
- [ ] T011 [P] 活动管理页面集成测试 `frontend-admin/tests/views/ActivityManagement.test.ts`
- [ ] T012 [P] 费用统计页面集成测试 `frontend-admin/tests/views/ExpenseStatistics.test.ts`

## Phase 3.3: 核心UI组件与状态管理 (测试失败后才开始)

### 全局状态和工具类 [P]
- [ ] T013 [P] 创建主题配置store `frontend-admin/src/stores/theme.ts`
- [ ] T014 [P] 创建布局状态store `frontend-admin/src/stores/layout.ts`
- [ ] T015 [P] 创建用户认证store `frontend-admin/src/stores/auth.ts`
- [ ] T016 [P] 创建API请求工具类 `frontend-admin/src/utils/request.ts`
- [ ] T017 [P] 创建表单验证工具 `frontend-admin/src/utils/validation.ts`

### 通用组件开发 [P]
- [ ] T018 [P] 开发通用数据表格组件 `frontend-admin/src/components/DataTable.vue`
- [ ] T019 [P] 开发通用搜索表单组件 `frontend-admin/src/components/SearchForm.vue`
- [ ] T020 [P] 开发通用对话框组件 `frontend-admin/src/components/CommonDialog.vue`
- [ ] T021 [P] 开发加载状态组件 `frontend-admin/src/components/LoadingState.vue`

### 布局框架组件
- [ ] T022 优化BasicLayout布局组件 `frontend-admin/src/layouts/BasicLayout.vue`
- [ ] T023 创建侧边导航菜单组件 `frontend-admin/src/components/SideMenu.vue`
- [ ] T024 创建顶部导航栏组件 `frontend-admin/src/components/TopNavbar.vue`
- [ ] T025 创建面包屑导航组件 `frontend-admin/src/components/Breadcrumb.vue`

## Phase 3.4: 业务页面实现

### 仪表板页面
- [ ] T026 [P] 创建仪表板数据API服务 `frontend-admin/src/api/dashboard.ts`
- [ ] T027 重新设计首页仪表板 `frontend-admin/src/views/HomeView.vue`
- [ ] T028 创建统计卡片组件 `frontend-admin/src/components/StatsCard.vue`
- [ ] T029 集成图表库并创建数据可视化组件 `frontend-admin/src/components/Charts.vue`

### 用户管理页面
- [ ] T030 [P] 创建用户API服务 `frontend-admin/src/api/user.ts`
- [ ] T031 重新设计用户管理页面 `frontend-admin/src/views/user/UserManagement.vue`
- [ ] T032 创建用户编辑表单组件 `frontend-admin/src/components/UserForm.vue`
- [ ] T033 实现用户搜索和筛选功能

### 活动管理页面
- [ ] T034 [P] 创建活动API服务 `frontend-admin/src/api/activity.ts`
- [ ] T035 重新设计活动管理页面 `frontend-admin/src/views/activity/ActivityManagement.vue`
- [ ] T036 创建活动编辑表单组件 `frontend-admin/src/components/ActivityForm.vue`
- [ ] T037 实现活动状态管理和批量操作

### 费用统计页面
- [ ] T038 [P] 创建费用API服务 `frontend-admin/src/api/expense.ts`
- [ ] T039 重新设计费用统计页面 `frontend-admin/src/views/expense/ExpenseStatistics.vue`
- [ ] T040 创建费用图表组件 `frontend-admin/src/components/ExpenseCharts.vue`
- [ ] T041 实现费用数据导出功能

### 登录页面优化
- [ ] T042 重新设计登录页面样式 `frontend-admin/src/views/Login.vue`
- [ ] T043 优化登录表单验证和用户体验

## Phase 3.5: 样式系统与主题

### 设计系统实现
- [ ] T044 [P] 创建主题样式变量文件 `frontend-admin/src/styles/variables.scss`
- [ ] T045 [P] 创建通用样式工具类 `frontend-admin/src/styles/utils.scss`
- [ ] T046 [P] 创建羽毛球主题色彩方案 `frontend-admin/src/styles/theme.scss`
- [ ] T047 [P] 创建响应式断点混入 `frontend-admin/src/styles/mixins.scss`

### 组件样式优化
- [ ] T048 优化全局App.vue样式，整合主题系统
- [ ] T049 实现深色模式切换功能 `frontend-admin/src/components/ThemeToggle.vue`
- [ ] T050 优化所有页面的移动端响应式适配

## Phase 3.6: 集成与连通性

### 后端API集成
- [ ] T051 验证并测试所有API端点的连通性
- [ ] T052 实现统一的错误处理和用户提示机制
- [ ] T053 实现请求拦截器，处理认证token和全局loading
- [ ] T054 实现响应拦截器，处理错误和数据格式统一

### 权限和路由守卫
- [ ] T055 实现路由权限守卫 `frontend-admin/src/router/guards.ts`
- [ ] T056 实现菜单权限控制功能
- [ ] T057 创建无权限访问页面 `frontend-admin/src/views/403.vue`

## Phase 3.7: 性能优化与完善

### 性能优化 [P]
- [ ] T058 [P] 实现路由级别的懒加载优化
- [ ] T059 [P] 优化静态资源打包和压缩配置
- [ ] T060 [P] 实现大数据量表格的虚拟滚动
- [ ] T061 [P] 添加页面loading骨架屏

### 单元测试和文档 [P]
- [ ] T062 [P] 为核心组件添加单元测试覆盖
- [ ] T063 [P] 为工具函数添加单元测试
- [ ] T064 [P] 创建组件使用文档和样式指南
- [ ] T065 [P] 更新项目README.md文档

### 验收测试
- [ ] T066 执行quickstart.md中的所有验收测试场景
- [ ] T067 修复测试中发现的bug和用户体验问题
- [ ] T068 性能测试：确保页面加载时间<2秒，API响应<200ms
- [ ] T069 兼容性测试：验证主流浏览器和设备的表现

## Dependencies 依赖关系

### 阻塞关系
- **Setup完成后才能开始**: T001-T004 → T005-T012 (测试编写)
- **测试失败后才能实现**: T005-T012 → T013+ (实现代码)
- **基础组件先于业务组件**: T013-T025 → T026+ (业务页面)
- **API服务先于页面**: T026,T030,T034,T038 → 对应的页面实现任务
- **样式系统支撑所有组件**: T044-T047 应尽早完成
- **集成测试在实现后**: T051-T057 → T066-T069 (验收测试)

### 并行执行组
**不同文件的任务可以并行执行，相同组件的优化任务需要串行**

## Parallel Execution Examples 并行执行示例

### 合约测试阶段 (T005-T008)
```bash
# 可以同时启动的API测试任务:
Task: "Dashboard API合约测试 frontend-admin/tests/api/dashboard.test.ts"
Task: "Users API合约测试 frontend-admin/tests/api/users.test.ts" 
Task: "Activities API合约测试 frontend-admin/tests/api/activities.test.ts"
Task: "Expenses API合约测试 frontend-admin/tests/api/expenses.test.ts"
```

### 状态管理和工具类开发 (T013-T017)
```bash
# 可以同时开发的独立模块:
Task: "创建主题配置store frontend-admin/src/stores/theme.ts"
Task: "创建布局状态store frontend-admin/src/stores/layout.ts"
Task: "创建用户认证store frontend-admin/src/stores/auth.ts"
Task: "创建API请求工具类 frontend-admin/src/utils/request.ts"
```

### 通用组件开发 (T018-T021)
```bash
# 可以同时开发的独立组件:
Task: "开发通用数据表格组件 frontend-admin/src/components/DataTable.vue"
Task: "开发通用搜索表单组件 frontend-admin/src/components/SearchForm.vue"
Task: "开发通用对话框组件 frontend-admin/src/components/CommonDialog.vue"
Task: "开发加载状态组件 frontend-admin/src/components/LoadingState.vue"
```

## Validation Checklist 验收检查清单

- [x] 所有API合约都有对应的测试任务
- [x] 所有数据实体都有对应的处理任务
- [x] 所有测试任务都在实现任务之前
- [x] 并行任务确实相互独立
- [x] 每个任务都指定了确切的文件路径
- [x] 没有[P]任务修改相同的文件

## Notes 注意事项

- **[P]标记的任务**: 不同文件，无依赖关系，可以并行执行
- **测试驱动**: 必须先写测试，确保测试失败，然后再实现功能
- **渐进提交**: 每完成一个任务就提交一次，保持版本历史清晰
- **样式一致性**: 所有组件都应遵循羽毛球主题设计规范
- **性能第一**: 在实现过程中始终关注页面加载速度和交互响应