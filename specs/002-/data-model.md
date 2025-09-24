# Data Model: 后台管理页面数据模型

**范围**: 后台管理界面涉及的数据实体和UI状态模型

## 1. 用户界面状态模型

### 布局状态 (LayoutState)
```typescript
interface LayoutState {
  // 侧边栏状态
  sidebarCollapsed: boolean
  // 当前选中的菜单项
  activeMenu: string
  // 面包屑导航
  breadcrumb: BreadcrumbItem[]
  // 主题配置
  theme: ThemeConfig
}

interface BreadcrumbItem {
  title: string
  path?: string
}

interface ThemeConfig {
  primaryColor: string  // 默认: #00C853
  isDarkMode: boolean   // 默认: false
}
```

### 表格状态 (TableState)
```typescript
interface TableState<T> {
  // 数据列表
  data: T[]
  // 加载状态
  loading: boolean
  // 分页信息
  pagination: PaginationConfig
  // 选中的行
  selectedRows: T[]
  // 排序配置
  sort: SortConfig
  // 筛选条件
  filters: Record<string, any>
}

interface PaginationConfig {
  current: number       // 当前页码
  pageSize: number      // 每页条数
  total: number         // 总条数
  showSizeChanger: boolean
  showQuickJumper: boolean
}

interface SortConfig {
  field: string
  order: 'asc' | 'desc' | null
}
```

### 表单状态 (FormState)
```typescript
interface FormState<T> {
  // 表单数据
  model: T
  // 验证规则
  rules: ValidationRules<T>
  // 验证状态
  valid: boolean
  // 提交状态
  submitting: boolean
  // 错误信息
  errors: Record<string, string>
}

type ValidationRules<T> = {
  [K in keyof T]?: ValidationRule[]
}

interface ValidationRule {
  required?: boolean
  message: string
  trigger?: 'blur' | 'change'
  validator?: (value: any) => boolean
}
```

## 2. 业务数据模型

### 用户管理数据模型
```typescript
interface User {
  id: number
  nickname: string
  phone: string
  email?: string
  avatar?: string
  status: UserStatus
  role: UserRole
  createdAt: string
  updatedAt: string
  lastLoginAt?: string
}

enum UserStatus {
  ACTIVE = 1,
  DISABLED = 0,
  PENDING = 2
}

enum UserRole {
  ADMIN = 'admin',
  USER = 'user',
  MODERATOR = 'moderator'
}

// 用户管理页面的查询参数
interface UserQueryParams {
  keyword?: string      // 搜索关键词
  status?: UserStatus   // 状态筛选
  role?: UserRole      // 角色筛选
  dateRange?: [string, string]  // 注册时间范围
}
```

### 活动管理数据模型
```typescript
interface Activity {
  id: number
  title: string
  description?: string
  location: string
  startTime: string
  endTime: string
  maxParticipants: number
  currentParticipants: number
  fee: number
  status: ActivityStatus
  organizerId: number
  createdAt: string
  updatedAt: string
}

enum ActivityStatus {
  DRAFT = 0,
  PUBLISHED = 1,
  IN_PROGRESS = 2,
  COMPLETED = 3,
  CANCELLED = 4
}

interface ActivityQueryParams {
  keyword?: string
  status?: ActivityStatus
  dateRange?: [string, string]
  organizer?: number
}
```

### 费用数据模型
```typescript
interface Expense {
  id: number
  activityId: number
  userId: number
  amount: number
  type: ExpenseType
  description?: string
  status: ExpenseStatus
  paymentMethod?: string
  transactionId?: string
  createdAt: string
  updatedAt: string
}

enum ExpenseType {
  PARTICIPATION_FEE = 'participation',
  REFUND = 'refund',
  PENALTY = 'penalty'
}

enum ExpenseStatus {
  PENDING = 0,
  PAID = 1,
  FAILED = 2,
  REFUNDED = 3
}
```

## 3. API响应模型

### 通用响应格式
```typescript
interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

interface PaginatedResponse<T> {
  list: T[]
  total: number
  current: number
  pageSize: number
  pages: number
}

interface ErrorResponse {
  code: number
  message: string
  details?: string[]
  timestamp: number
}
```

### 仪表板数据模型
```typescript
interface DashboardStats {
  userStats: {
    total: number
    activeToday: number
    newThisMonth: number
    growthRate: number
  }
  activityStats: {
    total: number
    inProgress: number
    completedThisMonth: number
    participationRate: number
  }
  revenueStats: {
    totalRevenue: number
    monthlyRevenue: number
    growthRate: number
    refundAmount: number
  }
  systemStats: {
    systemHealth: 'good' | 'warning' | 'error'
    apiResponseTime: number
    errorRate: number
    activeConnections: number
  }
}
```

## 4. 状态变化与生命周期

### 表格数据生命周期
```
1. 初始状态: loading=true, data=[]
2. 加载数据: 调用API获取数据
3. 数据就绪: loading=false, data=apiData
4. 用户交互: 排序/筛选/分页
5. 重新加载: loading=true, 更新查询参数
6. 数据更新: loading=false, data=newData
```

### 表单处理生命周期
```
1. 初始化: model=defaultValues, valid=false
2. 用户输入: 实时验证，更新errors
3. 提交前验证: 全字段验证
4. 提交中: submitting=true
5. 提交完成: submitting=false, 处理结果
6. 成功: 关闭表单/刷新列表
7. 失败: 显示错误信息
```

## 5. 数据验证规则

### 用户数据验证
```typescript
const userValidationRules: ValidationRules<User> = {
  nickname: [
    { required: true, message: '请输入用户昵称', trigger: 'blur' },
    { validator: (value) => value.length >= 2 && value.length <= 20, message: '昵称长度为2-20字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { validator: (value) => /^1[3-9]\d{9}$/.test(value), message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { validator: (value) => !value || /^\w+@\w+\.\w+$/.test(value), message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}
```

### 活动数据验证
```typescript
const activityValidationRules: ValidationRules<Activity> = {
  title: [
    { required: true, message: '请输入活动标题', trigger: 'blur' },
    { validator: (value) => value.length <= 100, message: '标题长度不能超过100字符', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入活动地点', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    { validator: (value, model) => new Date(value) > new Date(model.startTime), message: '结束时间必须晚于开始时间', trigger: 'change' }
  ],
  maxParticipants: [
    { required: true, message: '请输入最大参与人数', trigger: 'blur' },
    { validator: (value) => value > 0 && value <= 1000, message: '参与人数范围为1-1000', trigger: 'blur' }
  ]
}
```

## 6. 本地存储模型

### 用户偏好设置
```typescript
interface UserPreferences {
  // 主题设置
  theme: 'light' | 'dark' | 'auto'
  // 语言设置
  locale: 'zh-CN' | 'en-US'
  // 表格显示设置
  tableSettings: {
    pageSize: number
    density: 'large' | 'medium' | 'small'
  }
  // 侧边栏设置
  sidebarCollapsed: boolean
}

// 存储键名常量
const STORAGE_KEYS = {
  USER_PREFERENCES: 'badminton_admin_preferences',
  AUTH_TOKEN: 'badminton_admin_token',
  LAST_VISITED_PAGE: 'badminton_admin_last_page'
} as const
```

## 关系图

```
DashboardStats
    ├── userStats (关联 User)
    ├── activityStats (关联 Activity)  
    ├── revenueStats (关联 Expense)
    └── systemStats

User 1:N Activity (organizerId)
Activity 1:N Expense (activityId)
User 1:N Expense (userId)

UI State Models:
    ├── LayoutState (全局布局)
    ├── TableState<T> (表格通用)
    ├── FormState<T> (表单通用)
    └── UserPreferences (用户设置)
```

## 注意事项

1. **类型安全**: 所有数据模型都使用TypeScript严格类型
2. **状态一致性**: UI状态与后端数据保持同步
3. **验证机制**: 前端验证与后端验证规则保持一致
4. **错误处理**: 统一的错误状态模型和处理机制
5. **性能考虑**: 大数据量时的分页和虚拟滚动支持