<template>
  <!-- 仪表板页面 -->
  <PageContainer
    title="仪表板"
    description="羽毛球馆运营数据总览"
    icon="Dashboard"
    :breadcrumbs="breadcrumbs"
    :loading="loading"
  >
    <!-- 数据概览卡片 -->
    <div class="dashboard-overview">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6" v-for="card in overviewCards" :key="card.key">
          <div class="overview-card" :class="`overview-card--${card.type}`">
            <div class="overview-card__icon">
              <el-icon>
                <component :is="card.icon" />
              </el-icon>
            </div>
            <div class="overview-card__content">
              <div class="overview-card__value">
                {{ formatNumber(card.value) }}
                <span v-if="card.unit" class="overview-card__unit">{{ card.unit }}</span>
              </div>
              <div class="overview-card__label">{{ card.label }}</div>
              <div class="overview-card__change" :class="card.changeType">
                <el-icon>
                  <component :is="card.changeType === 'increase' ? 'TrendCharts' : 'Histogram'" />
                </el-icon>
                {{ card.change }}
                <span class="change-text">较昨日</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 快捷操作 -->
    <div class="dashboard-shortcuts">
      <el-card shadow="hover">
        <template #header>
          <span class="card-title">快捷操作</span>
        </template>
        <el-row :gutter="16">
          <el-col 
            :xs="12" :sm="8" :md="6" :lg="4"
            v-for="shortcut in shortcuts" 
            :key="shortcut.key"
          >
            <div class="shortcut-item" @click="handleShortcut(shortcut)">
              <div class="shortcut-icon">
                <el-icon>
                  <component :is="shortcut.icon" />
                </el-icon>
              </div>
              <div class="shortcut-label">{{ shortcut.label }}</div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Dashboard, User, Calendar, TrendCharts, Histogram,
  Plus, Edit, Search, Setting, Download, Refresh
} from '@element-plus/icons-vue'
import PageContainer from '@/components/common/PageContainer.vue'
import { formatNumber } from '@/utils/format'

// 路由实例
const router = useRouter()

// 面包屑配置
const breadcrumbs = [
  { title: '首页', to: '/dashboard' }
]

// 响应式数据
const loading = ref(false)

// 概览卡片数据
const overviewCards = ref([
  {
    key: 'totalRevenue',
    label: '今日收入',
    value: 12580,
    unit: '元',
    change: '+12.5%',
    changeType: 'increase',
    icon: 'TrendCharts',
    type: 'success'
  },
  {
    key: 'activeUsers',
    label: '活跃用户',
    value: 1028,
    unit: '人',
    change: '+8.2%',
    changeType: 'increase',
    icon: 'User',
    type: 'primary'
  },
  {
    key: 'totalBookings',
    label: '今日预订',
    value: 156,
    unit: '场次',
    change: '+15.3%',
    changeType: 'increase',
    icon: 'Calendar',
    type: 'warning'
  },
  {
    key: 'courtUsage',
    label: '场地使用率',
    value: 78.5,
    unit: '%',
    change: '-2.1%',
    changeType: 'decrease',
    icon: 'Histogram',
    type: 'info'
  }
])

// 快捷操作配置
const shortcuts = [
  { key: 'newBooking', label: '新建预订', icon: 'Plus', route: '/reservations/create' },
  { key: 'manageUsers', label: '用户管理', icon: 'User', route: '/users' },
  { key: 'manageActivities', label: '活动管理', icon: 'Calendar', route: '/activities' },
  { key: 'manageCourts', label: '场地管理', icon: 'Setting', route: '/courts' },
  { key: 'viewReports', label: '报表查看', icon: 'TrendCharts', route: '/reports' },
  { key: 'exportData', label: '数据导出', icon: 'Download', action: 'export' }
]

// 方法
const loadDashboardData = async () => {
  loading.value = true
  try {
    // 模拟加载数据
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('数据加载完成')
  } catch (error) {
    console.error('加载仪表板数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 处理快捷操作
const handleShortcut = (shortcut: any) => {
  if (shortcut.route) {
    router.push(shortcut.route)
  } else if (shortcut.action === 'export') {
    ElMessage.success('开始导出数据...')
  }
}

// 生命周期
onMounted(() => {
  loadDashboardData()
})
</script>

<style lang="scss" scoped>
.dashboard-overview {
  margin-bottom: 24px;
}

.overview-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: var(--el-bg-color);
  border-radius: 12px;
  border: 1px solid var(--el-border-color-lighter);
  transition: all 0.3s;
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }
  
  &__icon {
    flex-shrink: 0;
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: var(--el-color-white);
  }
  
  &--success &__icon {
    background: linear-gradient(135deg, #00C853, #00E676);
  }
  
  &--primary &__icon {
    background: linear-gradient(135deg, #409EFF, #66B2FF);
  }
  
  &--warning &__icon {
    background: linear-gradient(135deg, #FF6D00, #FF8F00);
  }
  
  &--info &__icon {
    background: linear-gradient(135deg, #909399, #B4B7BF);
  }
  
  &__content {
    flex: 1;
  }
  
  &__value {
    font-size: 28px;
    font-weight: 600;
    color: var(--el-text-color-primary);
    line-height: 1;
    margin-bottom: 4px;
  }
  
  &__unit {
    font-size: 16px;
    font-weight: normal;
    color: var(--el-text-color-secondary);
  }
  
  &__label {
    font-size: 14px;
    color: var(--el-text-color-secondary);
    margin-bottom: 8px;
  }
  
  &__change {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    font-weight: 500;
    
    &.increase {
      color: var(--el-color-success);
    }
    
    &.decrease {
      color: var(--el-color-danger);
    }
    
    .change-text {
      color: var(--el-text-color-secondary);
      margin-left: 4px;
    }
  }
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.dashboard-shortcuts {
  .shortcut-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 20px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      background: var(--el-fill-color-light);
      transform: translateY(-2px);
    }
    
    .shortcut-icon {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: var(--el-color-primary-light-9);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      color: var(--el-color-primary);
    }
    
    .shortcut-label {
      font-size: 14px;
      color: var(--el-text-color-primary);
      text-align: center;
    }
  }
}

// 响应式调整
@media (max-width: 768px) {
  .overview-card {
    padding: 16px;
    
    &__value {
      font-size: 24px;
    }
    
    &__icon {
      width: 40px;
      height: 40px;
      font-size: 20px;
    }
  }
  
  .shortcut-item {
    padding: 16px 8px;
    
    .shortcut-icon {
      width: 40px;
      height: 40px;
      font-size: 20px;
    }
    
    .shortcut-label {
      font-size: 12px;
    }
  }
}
</style>