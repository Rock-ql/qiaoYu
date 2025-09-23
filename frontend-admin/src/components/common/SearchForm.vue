<template>
  <!-- 通用搜索表单组件 -->
  <div class="search-form">
    <el-form
      ref="formRef"
      :model="formData"
      :label-width="labelWidth"
      :label-position="labelPosition"
      :size="size"
      class="search-form__form"
    >
      <el-row :gutter="gutter">
        <template v-for="field in visibleFields" :key="field.prop">
          <el-col 
            :xs="field.responsive?.xs || defaultColSpan.xs"
            :sm="field.responsive?.sm || defaultColSpan.sm"
            :md="field.responsive?.md || defaultColSpan.md"
            :lg="field.responsive?.lg || defaultColSpan.lg"
            :xl="field.responsive?.xl || defaultColSpan.xl"
          >
            <el-form-item :label="field.label" :prop="field.prop">
              <!-- 输入框 -->
              <el-input
                v-if="field.type === 'input'"
                v-model="formData[field.prop]"
                :placeholder="field.placeholder || `请输入${field.label}`"
                :clearable="field.clearable !== false"
                :disabled="field.disabled"
                @keyup.enter="handleSearch"
              />

              <!-- 选择器 -->
              <el-select
                v-else-if="field.type === 'select'"
                v-model="formData[field.prop]"
                :placeholder="field.placeholder || `请选择${field.label}`"
                :clearable="field.clearable !== false"
                :disabled="field.disabled"
                :multiple="field.multiple"
                :filterable="field.filterable"
                :remote="field.remote"
                :remote-method="field.remoteMethod"
                :loading="field.loading"
                style="width: 100%"
              >
                <el-option
                  v-for="option in field.options"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                  :disabled="option.disabled"
                />
              </el-select>

              <!-- 级联选择器 -->
              <el-cascader
                v-else-if="field.type === 'cascader'"
                v-model="formData[field.prop]"
                :options="field.options"
                :props="field.cascaderProps"
                :placeholder="field.placeholder || `请选择${field.label}`"
                :clearable="field.clearable !== false"
                :disabled="field.disabled"
                :filterable="field.filterable"
                style="width: 100%"
              />

              <!-- 日期选择器 -->
              <el-date-picker
                v-else-if="field.type === 'date'"
                v-model="formData[field.prop]"
                :type="field.dateType || 'date'"
                :placeholder="field.placeholder || `请选择${field.label}`"
                :clearable="field.clearable !== false"
                :disabled="field.disabled"
                :format="field.format"
                :value-format="field.valueFormat"
                style="width: 100%"
              />

              <!-- 日期范围选择器 -->
              <el-date-picker
                v-else-if="field.type === 'daterange'"
                v-model="formData[field.prop]"
                type="daterange"
                :start-placeholder="field.startPlaceholder || '开始日期'"
                :end-placeholder="field.endPlaceholder || '结束日期'"
                :clearable="field.clearable !== false"
                :disabled="field.disabled"
                :format="field.format"
                :value-format="field.valueFormat"
                style="width: 100%"
              />

              <!-- 数字输入框 -->
              <el-input-number
                v-else-if="field.type === 'number'"
                v-model="formData[field.prop]"
                :placeholder="field.placeholder || `请输入${field.label}`"
                :min="field.min"
                :max="field.max"
                :step="field.step"
                :precision="field.precision"
                :disabled="field.disabled"
                :controls="field.controls !== false"
                style="width: 100%"
              />

              <!-- 开关 -->
              <el-switch
                v-else-if="field.type === 'switch'"
                v-model="formData[field.prop]"
                :disabled="field.disabled"
                :active-text="field.activeText"
                :inactive-text="field.inactiveText"
                :active-value="field.activeValue"
                :inactive-value="field.inactiveValue"
              />

              <!-- 单选框组 -->
              <el-radio-group
                v-else-if="field.type === 'radio'"
                v-model="formData[field.prop]"
                :disabled="field.disabled"
              >
                <el-radio
                  v-for="option in field.options"
                  :key="option.value"
                  :label="option.value"
                  :disabled="option.disabled"
                >
                  {{ option.label }}
                </el-radio>
              </el-radio-group>

              <!-- 复选框组 -->
              <el-checkbox-group
                v-else-if="field.type === 'checkbox'"
                v-model="formData[field.prop]"
                :disabled="field.disabled"
              >
                <el-checkbox
                  v-for="option in field.options"
                  :key="option.value"
                  :label="option.value"
                  :disabled="option.disabled"
                >
                  {{ option.label }}
                </el-checkbox>
              </el-checkbox-group>

              <!-- 自定义插槽 -->
              <slot
                v-else-if="field.type === 'slot'"
                :name="field.prop"
                :field="field"
                :value="formData[field.prop]"
                :form-data="formData"
              />
            </el-form-item>
          </el-col>
        </template>

        <!-- 操作按钮区域 -->
        <el-col 
          :xs="actionColSpan.xs"
          :sm="actionColSpan.sm"
          :md="actionColSpan.md"
          :lg="actionColSpan.lg"
          :xl="actionColSpan.xl"
        >
          <el-form-item>
            <div class="search-form__actions">
              <el-button
                type="primary"
                :icon="Search"
                :loading="loading"
                @click="handleSearch"
              >
                搜索
              </el-button>
              <el-button
                :icon="Refresh"
                @click="handleReset"
              >
                重置
              </el-button>
              <el-button
                v-if="showExpandToggle"
                type="primary"
                link
                @click="toggleExpand"
              >
                <template v-if="expanded">
                  收起 <el-icon><ArrowUp /></el-icon>
                </template>
                <template v-else>
                  展开 <el-icon><ArrowDown /></el-icon>
                </template>
              </el-button>
            </div>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, reactive } from 'vue'
import { ElForm, ElMessage } from 'element-plus'
import { Search, Refresh, ArrowUp, ArrowDown } from '@element-plus/icons-vue'

/**
 * 字段选项接口
 */
export interface FieldOption {
  label: string
  value: any
  disabled?: boolean
}

/**
 * 响应式配置接口
 */
export interface ResponsiveConfig {
  xs?: number
  sm?: number
  md?: number
  lg?: number
  xl?: number
}

/**
 * 搜索字段配置接口
 */
export interface SearchField {
  prop: string
  label: string
  type: 'input' | 'select' | 'cascader' | 'date' | 'daterange' | 'number' | 'switch' | 'radio' | 'checkbox' | 'slot'
  placeholder?: string
  defaultValue?: any
  clearable?: boolean
  disabled?: boolean
  hidden?: boolean
  responsive?: ResponsiveConfig
  
  // select 相关
  options?: FieldOption[]
  multiple?: boolean
  filterable?: boolean
  remote?: boolean
  remoteMethod?: (query: string) => void
  loading?: boolean
  
  // cascader 相关
  cascaderProps?: any
  
  // date 相关
  dateType?: 'year' | 'month' | 'date' | 'dates' | 'datetime' | 'week' | 'datetimerange' | 'daterange' | 'monthrange'
  format?: string
  valueFormat?: string
  startPlaceholder?: string
  endPlaceholder?: string
  
  // number 相关
  min?: number
  max?: number
  step?: number
  precision?: number
  controls?: boolean
  
  // switch 相关
  activeText?: string
  inactiveText?: string
  activeValue?: any
  inactiveValue?: any
}

/**
 * 组件Props
 */
interface Props {
  // 字段配置
  fields: SearchField[]
  
  // 表单配置
  labelWidth?: string | number
  labelPosition?: 'left' | 'right' | 'top'
  size?: 'large' | 'default' | 'small'
  gutter?: number
  
  // 响应式布局
  defaultColSpan?: ResponsiveConfig
  actionColSpan?: ResponsiveConfig
  
  // 展开收起功能
  collapsible?: boolean
  defaultExpanded?: boolean
  maxVisibleRows?: number
  
  // 其他
  loading?: boolean
  autoSearch?: boolean
  searchOnMount?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  labelWidth: '80px',
  labelPosition: 'right',
  size: 'default',
  gutter: 16,
  defaultColSpan: () => ({ xs: 24, sm: 12, md: 8, lg: 6, xl: 6 }),
  actionColSpan: () => ({ xs: 24, sm: 12, md: 8, lg: 6, xl: 6 }),
  collapsible: false,
  defaultExpanded: true,
  maxVisibleRows: 1,
  loading: false,
  autoSearch: true,
  searchOnMount: false
})

/**
 * 组件Emits
 */
interface Emits {
  'search': [params: Record<string, any>]
  'reset': []
  'field-change': [field: string, value: any, formData: Record<string, any>]
}

const emit = defineEmits<Emits>()

// 模板引用
const formRef = ref<InstanceType<typeof ElForm>>()

// 响应式数据
const formData = reactive<Record<string, any>>({})
const expanded = ref(props.defaultExpanded)

// 计算属性
const showExpandToggle = computed(() => {
  return props.collapsible && props.fields.length > getMaxVisibleFields()
})

const visibleFields = computed(() => {
  if (!props.collapsible || expanded.value) {
    return props.fields.filter(field => !field.hidden)
  }
  
  const maxFields = getMaxVisibleFields()
  return props.fields.filter(field => !field.hidden).slice(0, maxFields)
})

// 获取最大可见字段数
const getMaxVisibleFields = () => {
  const colSpan = props.defaultColSpan.lg || 6
  const fieldsPerRow = 24 / colSpan
  return Math.max(1, fieldsPerRow * props.maxVisibleRows - 1) // 减1为操作按钮留位置
}

// 初始化表单数据
const initFormData = () => {
  props.fields.forEach(field => {
    if (field.defaultValue !== undefined) {
      formData[field.prop] = field.defaultValue
    } else {
      // 根据字段类型设置默认值
      switch (field.type) {
        case 'select':
          formData[field.prop] = field.multiple ? [] : ''
          break
        case 'cascader':
          formData[field.prop] = []
          break
        case 'daterange':
          formData[field.prop] = []
          break
        case 'checkbox':
          formData[field.prop] = []
          break
        case 'number':
          formData[field.prop] = undefined
          break
        case 'switch':
          formData[field.prop] = field.inactiveValue !== undefined ? field.inactiveValue : false
          break
        default:
          formData[field.prop] = ''
      }
    }
  })
}

// 监听字段变化
watch(formData, (newData, oldData) => {
  for (const [key, value] of Object.entries(newData)) {
    if (oldData && oldData[key] !== value) {
      emit('field-change', key, value, { ...formData })
      
      // 自动搜索
      if (props.autoSearch) {
        handleSearch()
      }
    }
  }
}, { deep: true })

// 事件处理
const handleSearch = () => {
  const params = getSearchParams()
  emit('search', params)
}

const handleReset = () => {
  formRef.value?.resetFields()
  initFormData()
  emit('reset')
  
  if (props.autoSearch) {
    handleSearch()
  }
}

const toggleExpand = () => {
  expanded.value = !expanded.value
}

// 获取搜索参数
const getSearchParams = () => {
  const params: Record<string, any> = {}
  
  for (const [key, value] of Object.entries(formData)) {
    if (value !== '' && value !== null && value !== undefined) {
      // 处理数组类型（如日期范围、多选等）
      if (Array.isArray(value) && value.length === 0) {
        continue
      }
      params[key] = value
    }
  }
  
  return params
}

// 设置表单数据
const setFormData = (data: Record<string, any>) => {
  Object.keys(formData).forEach(key => {
    if (data.hasOwnProperty(key)) {
      formData[key] = data[key]
    }
  })
}

// 获取表单数据
const getFormData = () => {
  return { ...formData }
}

// 清空表单
const clearForm = () => {
  formRef.value?.resetFields()
  initFormData()
}

// 验证表单
const validateForm = async (): Promise<boolean> => {
  try {
    await formRef.value?.validate()
    return true
  } catch {
    return false
  }
}

// 获取字段值
const getFieldValue = (field: string) => {
  return formData[field]
}

// 设置字段值
const setFieldValue = (field: string, value: any) => {
  formData[field] = value
}

// 初始化
initFormData()

// 组件挂载后自动搜索
if (props.searchOnMount) {
  nextTick(() => {
    handleSearch()
  })
}

// 暴露方法
defineExpose({
  getSearchParams,
  setFormData,
  getFormData,
  clearForm,
  validateForm,
  getFieldValue,
  setFieldValue,
  formRef
})
</script>

<style lang="scss" scoped>
.search-form {
  &__form {
    background: var(--el-bg-color-page);
    padding: 16px;
    border-radius: 8px;
    margin-bottom: 16px;
  }

  &__actions {
    display: flex;
    gap: 8px;
    justify-content: flex-end;
    width: 100%;

    .el-button {
      min-width: 80px;
    }
  }

  // 响应式调整
  @media (max-width: 768px) {
    &__actions {
      justify-content: center;
      
      .el-button {
        flex: 1;
        min-width: auto;
      }
    }
  }

  @media (max-width: 576px) {
    &__form {
      padding: 12px;
    }

    &__actions {
      flex-direction: column;
      
      .el-button {
        width: 100%;
      }
    }
  }
}

// 表单项样式调整
:deep(.el-form-item) {
  margin-bottom: 16px;

  .el-form-item__label {
    font-weight: 500;
    color: var(--el-text-color-regular);
  }

  .el-form-item__content {
    .el-input,
    .el-select,
    .el-cascader,
    .el-date-editor {
      width: 100%;
    }
  }
}

// 展开收起按钮样式
:deep(.el-button--text) {
  padding: 0;
  margin-left: 8px;
  
  .el-icon {
    margin-left: 4px;
  }
}
</style>