<template>
  <!-- 页面容器组件 -->
  <div class="page-container" :class="containerClass">
    <!-- 页面头部 -->
    <div v-if="showHeader" class="page-container__header">
      <div class="page-header">
        <!-- 返回按钮 -->
        <el-button
          v-if="showBack"
          :icon="ArrowLeft"
          type="primary"
          text
          @click="handleBack"
        >
          返回
        </el-button>

        <!-- 页面标题区域 -->
        <div class="page-header__title-area">
          <div class="page-title">
            <el-icon v-if="icon" class="page-title__icon">
              <component :is="icon" />
            </el-icon>
            <h1 class="page-title__text">{{ title }}</h1>
            <el-tag v-if="subtitle" type="info" effect="plain" class="page-title__subtitle">
              {{ subtitle }}
            </el-tag>
          </div>
          <div v-if="description" class="page-description">
            {{ description }}
          </div>
        </div>

        <!-- 页面头部操作区域 -->
        <div v-if="$slots['header-extra']" class="page-header__extra">
          <slot name="header-extra" />
        </div>
      </div>

      <!-- 面包屑导航 -->
      <div v-if="showBreadcrumb && breadcrumbs.length > 0" class="page-breadcrumb">
        <el-breadcrumb :separator="breadcrumbSeparator">
          <el-breadcrumb-item
            v-for="(item, index) in breadcrumbs"
            :key="index"
            :to="item.to"
          >
            <el-icon v-if="item.icon">
              <component :is="item.icon" />
            </el-icon>
            {{ item.title }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <!-- 标签页 -->
      <div v-if="showTabs && tabs.length > 0" class="page-tabs">
        <el-tabs
          v-model="activeTab"
          :type="tabType"
          :closable="tabClosable"
          :addable="tabAddable"
          @tab-click="handleTabClick"
          @tab-remove="handleTabRemove"
          @tab-add="handleTabAdd"
        >
          <el-tab-pane
            v-for="tab in tabs"
            :key="tab.name"
            :label="tab.label"
            :name="tab.name"
            :disabled="tab.disabled"
            :closable="tab.closable"
          >
            <template v-if="tab.icon" #label>
              <el-icon style="margin-right: 4px;">
                <component :is="tab.icon" />
              </el-icon>
              {{ tab.label }}
            </template>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 页面内容区域 -->
    <div class="page-container__content" :class="contentClass">
      <!-- 加载状态 -->
      <div v-if="loading" class="page-loading">
        <el-skeleton :rows="skeletonRows" animated />
      </div>

      <!-- 错误状态 -->
      <div v-else-if="error" class="page-error">
        <el-result
          :icon="errorIcon"
          :title="errorTitle"
          :sub-title="errorDescription"
        >
          <template #extra>
            <el-button type="primary" @click="handleRetry">
              重试
            </el-button>
            <el-button @click="handleBack">
              返回
            </el-button>
          </template>
        </el-result>
      </div>

      <!-- 空状态 -->
      <div v-else-if="empty" class="page-empty">
        <el-empty
          :image="emptyImage"
          :image-size="emptyImageSize"
          :description="emptyDescription"
        >
          <template v-if="$slots['empty-extra']" #default>
            <slot name="empty-extra" />
          </template>
        </el-empty>
      </div>

      <!-- 正常内容 -->
      <div v-else class="page-content">
        <slot name="default" />
      </div>
    </div>

    <!-- 页面底部 -->
    <div v-if="$slots.footer" class="page-container__footer">
      <slot name="footer" />
    </div>

    <!-- 固定操作栏 -->
    <div v-if="$slots['fixed-actions']" class="page-container__fixed-actions">
      <div class="fixed-actions-wrapper">
        <slot name="fixed-actions" />
      </div>
    </div>

    <!-- 回到顶部 -->
    <el-backtop
      v-if="showBackTop"
      :target="backTopTarget"
      :visibility-height="backTopHeight"
      :right="backTopRight"
      :bottom="backTopBottom"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'

/**
 * 面包屑项目接口
 */
export interface BreadcrumbItem {
  title: string
  to?: string | { name: string; params?: any; query?: any }
  icon?: string
}

/**
 * 标签页接口
 */
export interface TabItem {
  name: string
  label: string
  icon?: string
  disabled?: boolean
  closable?: boolean
}

/**
 * 组件Props
 */
interface Props {
  // 页面标题
  title?: string
  subtitle?: string
  description?: string
  icon?: string
  
  // 页面配置
  showHeader?: boolean
  showBack?: boolean
  showBreadcrumb?: boolean
  showTabs?: boolean
  showBackTop?: boolean
  
  // 面包屑配置
  breadcrumbs?: BreadcrumbItem[]
  breadcrumbSeparator?: string
  
  // 标签页配置
  tabs?: TabItem[]
  activeTab?: string
  tabType?: 'line' | 'card' | 'border-card'
  tabClosable?: boolean
  tabAddable?: boolean
  
  // 页面状态
  loading?: boolean
  error?: boolean
  empty?: boolean
  
  // 错误状态配置
  errorIcon?: string
  errorTitle?: string
  errorDescription?: string
  
  // 空状态配置
  emptyImage?: string
  emptyImageSize?: number
  emptyDescription?: string
  
  // 样式配置
  containerClass?: string
  contentClass?: string
  fluid?: boolean
  padding?: boolean
  
  // 加载配置
  skeletonRows?: number
  
  // 回到顶部配置
  backTopTarget?: string
  backTopHeight?: number
  backTopRight?: number
  backTopBottom?: number
}

const props = withDefaults(defineProps<Props>(), {
  showHeader: true,
  showBack: false,
  showBreadcrumb: true,
  showTabs: false,
  showBackTop: true,
  breadcrumbs: () => [],
  breadcrumbSeparator: '/',
  tabs: () => [],
  tabType: 'line',
  tabClosable: false,
  tabAddable: false,
  loading: false,
  error: false,
  empty: false,
  errorIcon: 'error',
  errorTitle: '页面出错了',
  errorDescription: '请稍后重试',
  emptyDescription: '暂无数据',
  emptyImageSize: 200,
  fluid: false,
  padding: true,
  skeletonRows: 8,
  backTopHeight: 400,
  backTopRight: 40,
  backTopBottom: 40
})

/**
 * 组件Emits
 */
interface Emits {
  'update:activeTab': [tab: string]
  'tab-click': [tab: any, event: Event]
  'tab-remove': [name: string]
  'tab-add': []
  'back': []
  'retry': []
}

const emit = defineEmits<Emits>()

// 路由实例
const router = useRouter()

// 响应式数据
const activeTab = computed({
  get: () => props.activeTab || '',
  set: (value: string) => {
    emit('update:activeTab', value)
  }
})

// 计算属性
const containerClass = computed(() => {
  const classes = []
  
  if (props.fluid) {
    classes.push('page-container--fluid')
  }
  
  if (!props.padding) {
    classes.push('page-container--no-padding')
  }
  
  if (props.containerClass) {
    classes.push(props.containerClass)
  }
  
  return classes.join(' ')
})

const contentClass = computed(() => {
  const classes = ['page-content-wrapper']
  
  if (props.contentClass) {
    classes.push(props.contentClass)
  }
  
  return classes.join(' ')
})

// 事件处理
const handleBack = () => {
  emit('back')
  if (!emit('back')) {
    router.back()
  }
}

const handleTabClick = (tab: any, event: Event) => {
  emit('tab-click', tab, event)
}

const handleTabRemove = (name: string) => {
  emit('tab-remove', name)
}

const handleTabAdd = () => {
  emit('tab-add')
}

const handleRetry = () => {
  emit('retry')
}

// 滚动到顶部
const scrollToTop = () => {
  const target = props.backTopTarget ? document.querySelector(props.backTopTarget) : window
  if (target) {
    target.scrollTo({
      top: 0,
      behavior: 'smooth'
    })
  }
}

// 暴露方法
defineExpose({
  scrollToTop
})
</script>

<style lang="scss" scoped>
.page-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--el-bg-color-page);

  &--fluid {
    .page-container__content {
      max-width: none;
    }
  }

  &--no-padding {
    .page-content {
      padding: 0;
    }
  }

  &__header {
    background: var(--el-bg-color);
    border-bottom: 1px solid var(--el-border-color-lighter);
    position: sticky;
    top: 0;
    z-index: 100;
  }

  &__content {
    flex: 1;
    display: flex;
    flex-direction: column;
    max-width: 1200px;
    margin: 0 auto;
    width: 100%;
  }

  &__footer {
    background: var(--el-bg-color);
    border-top: 1px solid var(--el-border-color-lighter);
    padding: 16px;
  }

  &__fixed-actions {
    position: fixed;
    bottom: 24px;
    right: 24px;
    z-index: 1000;

    .fixed-actions-wrapper {
      display: flex;
      flex-direction: column;
      gap: 8px;
      align-items: flex-end;
    }
  }
}

// 页面头部样式
.page-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 24px;

  &__title-area {
    flex: 1;
  }

  &__extra {
    display: flex;
    gap: 8px;
    align-items: center;
  }
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;

  &__icon {
    font-size: 24px;
    color: var(--el-color-primary);
  }

  &__text {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  &__subtitle {
    margin-left: 8px;
  }
}

.page-description {
  color: var(--el-text-color-secondary);
  line-height: 1.6;
  margin-top: 4px;
}

// 面包屑样式
.page-breadcrumb {
  padding: 0 24px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);

  :deep(.el-breadcrumb__item) {
    .el-breadcrumb__inner {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}

// 标签页样式
.page-tabs {
  padding: 0 24px;

  :deep(.el-tabs__header) {
    margin: 0;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }
}

// 页面内容样式
.page-content-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.page-content {
  flex: 1;
  padding: 24px;
}

.page-loading,
.page-error,
.page-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
}

.page-loading {
  :deep(.el-skeleton) {
    width: 100%;
    max-width: 800px;
  }
}

// 响应式调整
@media (max-width: 768px) {
  .page-container {
    &__content {
      max-width: none;
      margin: 0;
    }

    &__fixed-actions {
      right: 16px;
      bottom: 16px;
    }
  }

  .page-header {
    flex-direction: column;
    gap: 12px;
    padding: 16px;

    &__extra {
      width: 100%;
      justify-content: flex-end;
    }
  }

  .page-title {
    &__text {
      font-size: 20px;
    }
  }

  .page-breadcrumb {
    padding: 0 16px 12px;
  }

  .page-tabs {
    padding: 0 16px;
  }

  .page-content {
    padding: 16px;
  }

  .page-loading,
  .page-error,
  .page-empty {
    padding: 32px 16px;
  }
}

@media (max-width: 576px) {
  .page-header {
    padding: 12px;
  }

  .page-title {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;

    &__text {
      font-size: 18px;
    }

    &__subtitle {
      margin-left: 0;
    }
  }

  .page-breadcrumb {
    padding: 0 12px 8px;
  }

  .page-tabs {
    padding: 0 12px;
  }

  .page-content {
    padding: 12px;
  }
}

// 暗色主题适配
.dark {
  .page-container {
    background: var(--el-bg-color-page);

    &__header,
    &__footer {
      background: var(--el-bg-color);
      border-color: var(--el-border-color);
    }
  }

  .page-breadcrumb {
    border-color: var(--el-border-color);
  }
}
</style>