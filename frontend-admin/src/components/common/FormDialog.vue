<template>
  <!-- 表单对话框组件 -->
  <CommonDialog
    v-model="visible"
    :title="title"
    :width="width"
    :loading="loading"
    :confirm-loading="confirmLoading"
    :confirm-disabled="confirmDisabled"
    :show-cancel="showCancel"
    :show-confirm="showConfirm"
    :cancel-text="cancelText"
    :confirm-text="confirmText"
    dialog-class="dialog-form"
    @confirm="handleConfirm"
    @cancel="handleCancel"
    @closed="handleClosed"
  >
    <!-- 表单内容 -->
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-width="labelWidth"
      :label-position="labelPosition"
      :size="size"
      :validate-on-rule-change="validateOnRuleChange"
      :hide-required-asterisk="hideRequiredAsterisk"
      @validate="handleFormValidate"
    >
      <template v-for="field in formFields" :key="field.prop">
        <el-form-item
          :label="field.label"
          :prop="field.prop"
          :required="field.required"
          :rules="field.rules"
          :error="field.error"
          :show-message="field.showMessage !== false"
          :inline-message="field.inlineMessage"
          :size="field.size || size"
        >
          <!-- 输入框 -->
          <el-input
            v-if="field.type === 'input'"
            v-model="formData[field.prop]"
            :type="field.inputType || 'text'"
            :placeholder="field.placeholder || `请输入${field.label}`"
            :clearable="field.clearable !== false"
            :disabled="field.disabled"
            :readonly="field.readonly"
            :maxlength="field.maxlength"
            :minlength="field.minlength"
            :show-word-limit="field.showWordLimit"
            :prefix-icon="field.prefixIcon"
            :suffix-icon="field.suffixIcon"
          />

          <!-- 文本域 -->
          <el-input
            v-else-if="field.type === 'textarea'"
            v-model="formData[field.prop]"
            type="textarea"
            :placeholder="field.placeholder || `请输入${field.label}`"
            :rows="field.rows || 4"
            :autosize="field.autosize"
            :disabled="field.disabled"
            :readonly="field.readonly"
            :maxlength="field.maxlength"
            :show-word-limit="field.showWordLimit"
          />

          <!-- 密码输入框 -->
          <el-input
            v-else-if="field.type === 'password'"
            v-model="formData[field.prop]"
            type="password"
            :placeholder="field.placeholder || `请输入${field.label}`"
            :show-password="field.showPassword !== false"
            :clearable="field.clearable !== false"
            :disabled="field.disabled"
            :maxlength="field.maxlength"
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
            :controls-position="field.controlsPosition"
            style="width: 100%"
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
            :allow-create="field.allowCreate"
            :default-first-option="field.defaultFirstOption"
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
            :separator="field.separator"
            :show-all-levels="field.showAllLevels !== false"
            :collapse-tags="field.collapseTags"
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
            :editable="field.editable"
            :format="field.format"
            :value-format="field.valueFormat"
            :start-placeholder="field.startPlaceholder"
            :end-placeholder="field.endPlaceholder"
            style="width: 100%"
          />

          <!-- 时间选择器 -->
          <el-time-picker
            v-else-if="field.type === 'time'"
            v-model="formData[field.prop]"
            :placeholder="field.placeholder || `请选择${field.label}`"
            :clearable="field.clearable !== false"
            :disabled="field.disabled"
            :editable="field.editable"
            :format="field.format"
            :value-format="field.valueFormat"
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
            :active-color="field.activeColor"
            :inactive-color="field.inactiveColor"
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
            :min="field.min"
            :max="field.max"
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

          <!-- 评分 -->
          <el-rate
            v-else-if="field.type === 'rate'"
            v-model="formData[field.prop]"
            :disabled="field.disabled"
            :max="field.max || 5"
            :allow-half="field.allowHalf"
            :low-threshold="field.lowThreshold"
            :high-threshold="field.highThreshold"
            :colors="field.colors"
            :void-color="field.voidColor"
            :disabled-void-color="field.disabledVoidColor"
            :icon-classes="field.iconClasses"
            :void-icon-class="field.voidIconClass"
            :disabled-void-icon-class="field.disabledVoidIconClass"
            :show-text="field.showText"
            :show-score="field.showScore"
            :text-color="field.textColor"
            :texts="field.texts"
            :score-template="field.scoreTemplate"
          />

          <!-- 滑块 -->
          <el-slider
            v-else-if="field.type === 'slider'"
            v-model="formData[field.prop]"
            :disabled="field.disabled"
            :min="field.min || 0"
            :max="field.max || 100"
            :step="field.step || 1"
            :show-input="field.showInput"
            :show-input-controls="field.showInputControls"
            :show-stops="field.showStops"
            :show-tooltip="field.showTooltip !== false"
            :format-tooltip="field.formatTooltip"
            :range="field.range"
            :vertical="field.vertical"
            :height="field.height"
            :marks="field.marks"
          />

          <!-- 文件上传 -->
          <el-upload
            v-else-if="field.type === 'upload'"
            v-model:file-list="formData[field.prop]"
            :action="field.action"
            :headers="field.headers"
            :method="field.method || 'post'"
            :data="field.data"
            :name="field.name || 'file'"
            :with-credentials="field.withCredentials"
            :multiple="field.multiple"
            :accept="field.accept"
            :on-preview="field.onPreview"
            :on-remove="field.onRemove"
            :on-success="field.onSuccess"
            :on-error="field.onError"
            :on-progress="field.onProgress"
            :on-change="field.onChange"
            :before-upload="field.beforeUpload"
            :before-remove="field.beforeRemove"
            :list-type="field.listType || 'text'"
            :auto-upload="field.autoUpload !== false"
            :disabled="field.disabled"
            :limit="field.limit"
            :on-exceed="field.onExceed"
            :file-list="field.fileList"
            :drag="field.drag"
            :show-file-list="field.showFileList !== false"
          >
            <slot :name="`upload-${field.prop}`">
              <el-button v-if="!field.drag" size="small" type="primary">
                {{ field.uploadText || '点击上传' }}
              </el-button>
              <div v-else>
                <el-icon class="el-icon--upload">
                  <UploadFilled />
                </el-icon>
                <div class="el-upload__text">
                  {{ field.uploadText || '将文件拖到此处，或点击上传' }}
                </div>
              </div>
            </slot>
          </el-upload>

          <!-- 自定义插槽 -->
          <slot
            v-else-if="field.type === 'slot'"
            :name="field.prop"
            :field="field"
            :value="formData[field.prop]"
            :form-data="formData"
          />

          <!-- 提示信息 -->
          <div
            v-if="field.tip"
            class="form-field-tip"
          >
            {{ field.tip }}
          </div>
        </el-form-item>
      </template>

      <!-- 额外内容插槽 -->
      <slot name="extra" :form-data="formData" />
    </el-form>
  </CommonDialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, reactive, nextTick } from 'vue'
import { ElForm, ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import CommonDialog from './CommonDialog.vue'
import type { FormItemRule } from 'element-plus'

/**
 * 字段选项接口
 */
export interface FieldOption {
  label: string
  value: any
  disabled?: boolean
}

/**
 * 表单字段配置接口
 */
export interface FormField {
  prop: string
  label: string
  type: 'input' | 'textarea' | 'password' | 'number' | 'select' | 'cascader' | 'date' | 'time' | 'switch' | 'radio' | 'checkbox' | 'rate' | 'slider' | 'upload' | 'slot'
  defaultValue?: any
  placeholder?: string
  required?: boolean
  disabled?: boolean
  readonly?: boolean
  rules?: FormItemRule[]
  error?: string
  showMessage?: boolean
  inlineMessage?: boolean
  size?: 'large' | 'default' | 'small'
  tip?: string
  
  // input/textarea 相关
  inputType?: string
  maxlength?: number
  minlength?: number
  showWordLimit?: boolean
  prefixIcon?: string
  suffixIcon?: string
  rows?: number
  autosize?: boolean | { minRows: number; maxRows: number }
  clearable?: boolean
  
  // password 相关
  showPassword?: boolean
  
  // number 相关
  min?: number
  max?: number
  step?: number
  precision?: number
  controls?: boolean
  controlsPosition?: 'right' | ''
  
  // select 相关
  options?: FieldOption[]
  multiple?: boolean
  filterable?: boolean
  remote?: boolean
  remoteMethod?: (query: string) => void
  loading?: boolean
  allowCreate?: boolean
  defaultFirstOption?: boolean
  
  // cascader 相关
  cascaderProps?: any
  separator?: string
  showAllLevels?: boolean
  collapseTags?: boolean
  
  // date/time 相关
  dateType?: string
  editable?: boolean
  format?: string
  valueFormat?: string
  startPlaceholder?: string
  endPlaceholder?: string
  
  // switch 相关
  activeText?: string
  inactiveText?: string
  activeValue?: any
  inactiveValue?: any
  activeColor?: string
  inactiveColor?: string
  
  // rate 相关
  allowHalf?: boolean
  lowThreshold?: number
  highThreshold?: number
  colors?: string[]
  voidColor?: string
  disabledVoidColor?: string
  iconClasses?: string[]
  voidIconClass?: string
  disabledVoidIconClass?: string
  showText?: boolean
  showScore?: boolean
  textColor?: string
  texts?: string[]
  scoreTemplate?: string
  
  // slider 相关
  showInput?: boolean
  showInputControls?: boolean
  showStops?: boolean
  showTooltip?: boolean
  formatTooltip?: (value: number) => string
  range?: boolean
  vertical?: boolean
  height?: string
  marks?: Record<number, string>
  
  // upload 相关
  action?: string
  headers?: Record<string, any>
  method?: string
  data?: Record<string, any>
  name?: string
  withCredentials?: boolean
  accept?: string
  onPreview?: (file: any) => void
  onRemove?: (file: any, fileList: any[]) => void
  onSuccess?: (response: any, file: any, fileList: any[]) => void
  onError?: (err: any, file: any, fileList: any[]) => void
  onProgress?: (event: any, file: any, fileList: any[]) => void
  onChange?: (file: any, fileList: any[]) => void
  beforeUpload?: (file: any) => boolean | Promise<boolean>
  beforeRemove?: (file: any, fileList: any[]) => boolean | Promise<boolean>
  listType?: 'text' | 'picture' | 'picture-card'
  autoUpload?: boolean
  limit?: number
  onExceed?: (files: any[], fileList: any[]) => void
  fileList?: any[]
  drag?: boolean
  showFileList?: boolean
  uploadText?: string
}

/**
 * 组件Props
 */
interface Props {
  // 显示控制
  modelValue: boolean
  
  // 对话框配置
  title?: string
  width?: string | number
  
  // 表单配置
  fields: FormField[]
  data?: Record<string, any>
  rules?: Record<string, FormItemRule[]>
  labelWidth?: string | number
  labelPosition?: 'left' | 'right' | 'top'
  size?: 'large' | 'default' | 'small'
  validateOnRuleChange?: boolean
  hideRequiredAsterisk?: boolean
  
  // 按钮配置
  showCancel?: boolean
  showConfirm?: boolean
  cancelText?: string
  confirmText?: string
  
  // 状态
  loading?: boolean
  confirmLoading?: boolean
  
  // 行为配置
  resetOnClose?: boolean
  validateOnConfirm?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  width: '600px',
  data: () => ({}),
  rules: () => ({}),
  labelWidth: '100px',
  labelPosition: 'right',
  size: 'default',
  validateOnRuleChange: true,
  hideRequiredAsterisk: false,
  showCancel: true,
  showConfirm: true,
  cancelText: '取消',
  confirmText: '确定',
  loading: false,
  confirmLoading: false,
  resetOnClose: true,
  validateOnConfirm: true
})

/**
 * 组件Emits
 */
interface Emits {
  'update:modelValue': [value: boolean]
  'confirm': [data: Record<string, any>]
  'cancel': []
  'validate': [prop: string, isValid: boolean, message: string]
}

const emit = defineEmits<Emits>()

// 模板引用
const formRef = ref<InstanceType<typeof ElForm>>()

// 响应式数据
const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => {
    emit('update:modelValue', value)
  }
})

const formData = reactive<Record<string, any>>({})
const confirmDisabled = ref(false)

// 计算属性
const formFields = computed(() => props.fields)
const formRules = computed(() => {
  const rules: Record<string, FormItemRule[]> = { ...props.rules }
  
  // 合并字段规则
  props.fields.forEach(field => {
    if (field.rules) {
      rules[field.prop] = [...(rules[field.prop] || []), ...field.rules]
    }
  })
  
  return rules
})

// 初始化表单数据
const initFormData = () => {
  // 清空现有数据
  Object.keys(formData).forEach(key => {
    delete formData[key]
  })
  
  // 设置默认值
  props.fields.forEach(field => {
    if (props.data && props.data.hasOwnProperty(field.prop)) {
      formData[field.prop] = props.data[field.prop]
    } else if (field.defaultValue !== undefined) {
      formData[field.prop] = field.defaultValue
    } else {
      // 根据字段类型设置默认值
      switch (field.type) {
        case 'select':
          formData[field.prop] = field.multiple ? [] : ''
          break
        case 'cascader':
        case 'checkbox':
          formData[field.prop] = []
          break
        case 'number':
          formData[field.prop] = undefined
          break
        case 'switch':
          formData[field.prop] = field.inactiveValue !== undefined ? field.inactiveValue : false
          break
        case 'rate':
          formData[field.prop] = 0
          break
        case 'slider':
          formData[field.prop] = field.range ? [field.min || 0, field.max || 100] : field.min || 0
          break
        case 'upload':
          formData[field.prop] = []
          break
        default:
          formData[field.prop] = ''
      }
    }
  })
}

// 监听数据变化
watch(() => props.data, () => {
  initFormData()
}, { deep: true, immediate: true })

watch(() => props.fields, () => {
  initFormData()
}, { deep: true })

// 事件处理
const handleConfirm = async () => {
  if (props.validateOnConfirm) {
    try {
      await formRef.value?.validate()
      emit('confirm', { ...formData })
    } catch (error) {
      ElMessage.error('请检查表单输入')
    }
  } else {
    emit('confirm', { ...formData })
  }
}

const handleCancel = () => {
  emit('cancel')
}

const handleClosed = () => {
  if (props.resetOnClose) {
    formRef.value?.resetFields()
    nextTick(() => {
      initFormData()
    })
  }
}

const handleFormValidate = (prop: string, isValid: boolean, message: string) => {
  emit('validate', prop, isValid, message)
  
  // 更新确认按钮状态
  nextTick(async () => {
    try {
      await formRef.value?.validate()
      confirmDisabled.value = false
    } catch {
      confirmDisabled.value = true
    }
  })
}

// 公共方法
const validate = async (): Promise<boolean> => {
  try {
    await formRef.value?.validate()
    return true
  } catch {
    return false
  }
}

const validateField = async (prop: string): Promise<boolean> => {
  try {
    await formRef.value?.validateField(prop)
    return true
  } catch {
    return false
  }
}

const resetFields = () => {
  formRef.value?.resetFields()
}

const clearValidate = (props?: string | string[]) => {
  formRef.value?.clearValidate(props)
}

const setFieldValue = (prop: string, value: any) => {
  formData[prop] = value
}

const getFieldValue = (prop: string) => {
  return formData[prop]
}

const getFormData = () => {
  return { ...formData }
}

const setFormData = (data: Record<string, any>) => {
  Object.keys(formData).forEach(key => {
    if (data.hasOwnProperty(key)) {
      formData[key] = data[key]
    }
  })
}

// 暴露方法
defineExpose({
  validate,
  validateField,
  resetFields,
  clearValidate,
  setFieldValue,
  getFieldValue,
  getFormData,
  setFormData,
  formRef
})
</script>

<style lang="scss" scoped>
.form-field-tip {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}

// 表单样式调整
:deep(.el-form) {
  .el-form-item {
    margin-bottom: 20px;

    .el-form-item__label {
      font-weight: 500;
      color: var(--el-text-color-regular);
    }

    .el-form-item__content {
      .el-input,
      .el-select,
      .el-cascader,
      .el-date-editor,
      .el-time-picker {
        width: 100%;
      }

      .el-textarea {
        .el-textarea__inner {
          resize: vertical;
        }
      }

      .el-upload {
        width: 100%;
      }
    }
  }
}

// 响应式调整
@media (max-width: 768px) {
  :deep(.el-form) {
    .el-form-item {
      margin-bottom: 16px;

      .el-form-item__label {
        padding-bottom: 4px;
      }
    }
  }
}
</style>