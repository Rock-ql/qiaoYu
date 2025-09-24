<template>
  <!-- 通用对话框组件 -->
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    :fullscreen="fullscreen"
    :top="top"
    :modal="modal"
    :modal-class="modalClass"
    :lock-scroll="lockScroll"
    :open-delay="openDelay"
    :close-delay="closeDelay"
    :close-on-click-modal="closeOnClickModal"
    :close-on-press-escape="closeOnPressEscape"
    :show-close="showClose"
    :before-close="handleBeforeClose"
    :destroy-on-close="destroyOnClose"
    :append-to-body="appendToBody"
    :draggable="draggable"
    :overflow="overflow"
    :center="center"
    :align-center="alignCenter"
    :class="dialogClass"
  >
    <!-- 自定义标题 -->
    <template v-if="$slots.title" #title>
      <slot name="title" />
    </template>

    <!-- 对话框内容 -->
    <div v-loading="loading" class="common-dialog__content">
      <slot name="default" />
    </div>

    <!-- 对话框底部 -->
    <template #footer>
      <slot name="footer">
        <div class="common-dialog__footer">
          <el-button
            v-if="showCancel"
            :size="buttonSize"
            @click="handleCancel"
          >
            {{ cancelText }}
          </el-button>
          <el-button
            v-if="showConfirm"
            type="primary"
            :size="buttonSize"
            :loading="confirmLoading"
            :disabled="confirmDisabled"
            @click="handleConfirm"
          >
            {{ confirmText }}
          </el-button>
        </div>
      </slot>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

/**
 * 组件Props
 */
interface Props {
  // 显示控制
  modelValue: boolean
  
  // 基本配置
  title?: string
  width?: string | number
  fullscreen?: boolean
  top?: string
  
  // 行为配置
  modal?: boolean
  modalClass?: string
  lockScroll?: boolean
  openDelay?: number
  closeDelay?: number
  closeOnClickModal?: boolean
  closeOnPressEscape?: boolean
  showClose?: boolean
  destroyOnClose?: boolean
  appendToBody?: boolean
  draggable?: boolean
  overflow?: boolean
  center?: boolean
  alignCenter?: boolean
  
  // 样式配置
  dialogClass?: string
  
  // 内容配置
  loading?: boolean
  
  // 按钮配置
  showCancel?: boolean
  showConfirm?: boolean
  cancelText?: string
  confirmText?: string
  buttonSize?: 'large' | 'default' | 'small'
  confirmLoading?: boolean
  confirmDisabled?: boolean
  
  // 关闭前回调
  beforeClose?: (done: () => void) => void
}

const props = withDefaults(defineProps<Props>(), {
  width: '50%',
  fullscreen: false,
  top: '15vh',
  modal: true,
  lockScroll: true,
  openDelay: 0,
  closeDelay: 0,
  closeOnClickModal: true,
  closeOnPressEscape: true,
  showClose: true,
  destroyOnClose: false,
  appendToBody: false,
  draggable: false,
  overflow: false,
  center: false,
  alignCenter: true,
  loading: false,
  showCancel: true,
  showConfirm: true,
  cancelText: '取消',
  confirmText: '确定',
  buttonSize: 'default',
  confirmLoading: false,
  confirmDisabled: false
})

/**
 * 组件Emits
 */
interface Emits {
  'update:modelValue': [value: boolean]
  'open': []
  'opened': []
  'close': []
  'closed': []
  'cancel': []
  'confirm': []
  'before-close': [done: () => void]
}

const emit = defineEmits<Emits>()

// 响应式数据
const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => {
    emit('update:modelValue', value)
  }
})

// 监听显示状态变化
watch(visible, (newVal, oldVal) => {
  if (newVal && !oldVal) {
    emit('open')
    // 打开后的回调需要等待动画完成
    setTimeout(() => emit('opened'), 300)
  } else if (!newVal && oldVal) {
    emit('close')
    // 关闭后的回调需要等待动画完成
    setTimeout(() => emit('closed'), 300)
  }
})

// 事件处理
const handleCancel = () => {
  emit('cancel')
  visible.value = false
}

const handleConfirm = () => {
  emit('confirm')
}

const handleBeforeClose = (done: () => void) => {
  if (props.beforeClose) {
    props.beforeClose(done)
  } else {
    emit('before-close', done)
    done()
  }
}

// 公共方法
const open = () => {
  visible.value = true
}

const close = () => {
  visible.value = false
}

// 暴露方法
defineExpose({
  open,
  close
})
</script>

<script lang="ts">
export default {
  name: 'CommonDialog'
}
</script>

<style lang="scss" scoped>
.common-dialog {
  &__content {
    min-height: 100px;
    max-height: 60vh;
    overflow-y: auto;
    padding: 0 4px; // 为滚动条留出空间
  }

  &__footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    padding-top: 16px;
    border-top: 1px solid var(--el-border-color-lighter);
  }
}

// 全局样式调整
:deep(.el-dialog) {
  border-radius: 8px;
  overflow: hidden;

  .el-dialog__header {
    background: var(--el-bg-color-page);
    border-bottom: 1px solid var(--el-border-color-lighter);
    padding: 16px 20px;
    margin: 0;

    .el-dialog__title {
      font-size: 16px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }

    .el-dialog__headerbtn {
      top: 50%;
      transform: translateY(-50%);
      
      .el-dialog__close {
        color: var(--el-text-color-regular);
        font-size: 16px;
        
        &:hover {
          color: var(--el-color-primary);
        }
      }
    }
  }

  .el-dialog__body {
    padding: 20px;
    color: var(--el-text-color-primary);
    line-height: 1.6;
  }

  .el-dialog__footer {
    padding: 0 20px 20px;
    text-align: right;
  }
}

// 响应式调整
@media (max-width: 768px) {
  :deep(.el-dialog) {
    width: 95% !important;
    margin: 5vh auto !important;
    max-height: 90vh;

    .el-dialog__body {
      padding: 16px;
    }

    .el-dialog__footer {
      padding: 0 16px 16px;
    }
  }

  .common-dialog {
    &__content {
      max-height: 50vh;
    }

    &__footer {
      flex-direction: column-reverse;
      
      .el-button {
        width: 100%;
        margin: 0 0 8px 0;
      }
    }
  }
}

@media (max-width: 576px) {
  :deep(.el-dialog) {
    width: 100% !important;
    height: 100% !important;
    margin: 0 !important;
    border-radius: 0;
    
    .el-dialog__header {
      padding: 12px 16px;
    }

    .el-dialog__body {
      padding: 12px;
    }

    .el-dialog__footer {
      padding: 0 12px 12px;
    }
  }

  .common-dialog {
    &__content {
      max-height: calc(100vh - 160px);
    }
  }
}

// 自定义对话框类型样式
:deep(.el-dialog.dialog-info) {
  .el-dialog__header {
    background: var(--el-color-info-light-9);
    border-bottom-color: var(--el-color-info-light-7);
  }
}

:deep(.el-dialog.dialog-success) {
  .el-dialog__header {
    background: var(--el-color-success-light-9);
    border-bottom-color: var(--el-color-success-light-7);
  }
}

:deep(.el-dialog.dialog-warning) {
  .el-dialog__header {
    background: var(--el-color-warning-light-9);
    border-bottom-color: var(--el-color-warning-light-7);
  }
}

:deep(.el-dialog.dialog-danger) {
  .el-dialog__header {
    background: var(--el-color-danger-light-9);
    border-bottom-color: var(--el-color-danger-light-7);
  }
}

// 大尺寸对话框
:deep(.el-dialog.dialog-large) {
  width: 80% !important;
}

// 小尺寸对话框
:deep(.el-dialog.dialog-small) {
  width: 30% !important;
}

// 表单对话框
:deep(.el-dialog.dialog-form) {
  .el-dialog__body {
    padding: 24px;
  }
}

// 确认对话框
:deep(.el-dialog.dialog-confirm) {
  .el-dialog__body {
    text-align: center;
    padding: 32px 24px;
    
    .el-icon {
      font-size: 48px;
      margin-bottom: 16px;
      
      &.icon-warning {
        color: var(--el-color-warning);
      }
      
      &.icon-danger {
        color: var(--el-color-danger);
      }
      
      &.icon-info {
        color: var(--el-color-info);
      }
    }
  }
}
</style>