// 表单验证工具类
// =======================================

import type { FormItemRule } from 'element-plus'

/**
 * 验证结果接口
 */
export interface ValidationResult {
  isValid: boolean
  message?: string
  field?: string
}

/**
 * 批量验证结果接口
 */
export interface BatchValidationResult {
  isValid: boolean
  errors: ValidationResult[]
  firstError?: string
}

/**
 * 验证器函数类型
 */
export type ValidatorFunction = (value: any, rule?: any, callback?: any) => boolean | string | Promise<boolean | string>

/**
 * 常用正则表达式
 */
export const REGEX_PATTERNS = {
  // 手机号（中国大陆）
  mobile: /^1[3-9]\d{9}$/,
  
  // 邮箱
  email: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
  
  // 身份证号（18位）
  idCard: /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/,
  
  // 密码（8-20位，包含字母和数字）
  password: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,20}$/,
  
  // 强密码（8-20位，包含大小写字母、数字和特殊字符）
  strongPassword: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,20}$/,
  
  // 用户名（4-20位字母数字下划线）
  username: /^[a-zA-Z0-9_]{4,20}$/,
  
  // 中文姓名
  chineseName: /^[\u4e00-\u9fa5]{2,6}$/,
  
  // 网址
  url: /^(https?:\/\/)?([\da-z\.-]+)\.([a-z\.]{2,6})([\/\w \.-]*)*\/?$/,
  
  // IP地址
  ip: /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/,
  
  // 港澳台手机号
  hkMobile: /^[5-9]\d{7}$/,
  taiwanMobile: /^09\d{8}$/,
  macauMobile: /^6\d{7}$/
}

/**
 * 基础验证器类
 */
export class BaseValidator {
  /**
   * 必填验证
   */
  static required(message = '此项为必填项'): FormItemRule {
    return {
      required: true,
      message,
      trigger: ['blur', 'change']
    }
  }

  /**
   * 长度验证
   */
  static length(min?: number, max?: number, message?: string): FormItemRule {
    return {
      min,
      max,
      message: message || `长度应在 ${min || 0} 到 ${max || '无限'} 个字符之间`,
      trigger: ['blur', 'change']
    }
  }

  /**
   * 正则验证
   */
  static pattern(pattern: RegExp, message: string): FormItemRule {
    return {
      pattern,
      message,
      trigger: ['blur', 'change']
    }
  }

  /**
   * 自定义验证器
   */
  static custom(validator: ValidatorFunction, message?: string): FormItemRule {
    return {
      validator: async (rule: any, value: any, callback: any) => {
        try {
          const result = await validator(value, rule, callback)
          if (result === true) {
            callback()
          } else if (typeof result === 'string') {
            callback(new Error(result))
          } else if (result === false) {
            callback(new Error(message || '验证失败'))
          }
        } catch (error) {
          callback(new Error(message || '验证出错'))
        }
      },
      trigger: ['blur', 'change']
    }
  }
}

/**
 * 常用验证器
 */
export class CommonValidators extends BaseValidator {
  /**
   * 手机号验证
   */
  static mobile(message = '请输入正确的手机号码'): FormItemRule {
    return this.pattern(REGEX_PATTERNS.mobile, message)
  }

  /**
   * 邮箱验证
   */
  static email(message = '请输入正确的邮箱地址'): FormItemRule {
    return this.pattern(REGEX_PATTERNS.email, message)
  }

  /**
   * 身份证验证
   */
  static idCard(message = '请输入正确的身份证号码'): FormItemRule {
    return this.custom((value: string) => {
      if (!value) return true
      
      if (!REGEX_PATTERNS.idCard.test(value)) {
        return false
      }
      
      // 校验码验证
      const weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
      const codes = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2']
      
      let sum = 0
      for (let i = 0; i < 17; i++) {
        sum += parseInt(value[i]) * weights[i]
      }
      
      const code = codes[sum % 11]
      return value[17].toUpperCase() === code
    }, message)
  }

  /**
   * 密码验证
   */
  static password(strong = false, message?: string): FormItemRule {
    const pattern = strong ? REGEX_PATTERNS.strongPassword : REGEX_PATTERNS.password
    const defaultMessage = strong 
      ? '密码必须包含大小写字母、数字和特殊字符，长度8-20位'
      : '密码必须包含字母和数字，长度8-20位'
    
    return this.pattern(pattern, message || defaultMessage)
  }

  /**
   * 确认密码验证
   */
  static confirmPassword(passwordField: string, message = '两次输入的密码不一致'): FormItemRule {
    return this.custom((value: string, rule: any, callback: any) => {
      const form = rule.form || {}
      const password = form[passwordField]
      
      if (!value) return true
      
      return value === password
    }, message)
  }

  /**
   * 用户名验证
   */
  static username(message = '用户名应为4-20位字母、数字或下划线'): FormItemRule {
    return this.pattern(REGEX_PATTERNS.username, message)
  }

  /**
   * 中文姓名验证
   */
  static chineseName(message = '请输入正确的中文姓名'): FormItemRule {
    return this.pattern(REGEX_PATTERNS.chineseName, message)
  }

  /**
   * 网址验证
   */
  static url(message = '请输入正确的网址'): FormItemRule {
    return this.pattern(REGEX_PATTERNS.url, message)
  }

  /**
   * IP地址验证
   */
  static ip(message = '请输入正确的IP地址'): FormItemRule {
    return this.pattern(REGEX_PATTERNS.ip, message)
  }

  /**
   * 数字验证
   */
  static number(min?: number, max?: number, message?: string): FormItemRule {
    return this.custom((value: any) => {
      if (value === '' || value === null || value === undefined) return true
      
      const num = Number(value)
      if (isNaN(num)) return false
      
      if (min !== undefined && num < min) return false
      if (max !== undefined && num > max) return false
      
      return true
    }, message || `请输入${min !== undefined ? `${min}到` : ''}${max !== undefined ? max : '有效'}的数字`)
  }

  /**
   * 整数验证
   */
  static integer(min?: number, max?: number, message?: string): FormItemRule {
    return this.custom((value: any) => {
      if (value === '' || value === null || value === undefined) return true
      
      const num = Number(value)
      if (isNaN(num) || !Number.isInteger(num)) return false
      
      if (min !== undefined && num < min) return false
      if (max !== undefined && num > max) return false
      
      return true
    }, message || `请输入${min !== undefined ? `${min}到` : ''}${max !== undefined ? max : '有效'}的整数`)
  }

  /**
   * 价格验证（保留两位小数）
   */
  static price(min = 0, max?: number, message?: string): FormItemRule {
    return this.custom((value: any) => {
      if (value === '' || value === null || value === undefined) return true
      
      const num = Number(value)
      if (isNaN(num)) return false
      
      // 检查小数位数
      const decimalPlaces = (value.toString().split('.')[1] || '').length
      if (decimalPlaces > 2) return false
      
      if (num < min) return false
      if (max !== undefined && num > max) return false
      
      return true
    }, message || `请输入${min}到${max || '有效'}的价格（最多保留两位小数）`)
  }
}

/**
 * 业务验证器
 */
export class BusinessValidators extends CommonValidators {
  /**
   * 羽毛球场地编号验证
   */
  static courtNumber(message = '场地编号应为1-99的整数'): FormItemRule {
    return this.integer(1, 99, message)
  }

  /**
   * 活动时长验证（分钟）
   */
  static activityDuration(message = '活动时长应为15-480分钟'): FormItemRule {
    return this.custom((value: any) => {
      if (!value) return true
      
      const num = Number(value)
      if (isNaN(num) || !Number.isInteger(num)) return false
      
      // 最少15分钟，最多8小时
      if (num < 15 || num > 480) return false
      
      // 必须是15的倍数
      if (num % 15 !== 0) return false
      
      return true
    }, message)
  }

  /**
   * 活动费用验证
   */
  static activityPrice(message = '活动费用应为0-9999.99元'): FormItemRule {
    return this.price(0, 9999.99, message)
  }

  /**
   * 参与人数验证
   */
  static participantCount(min = 1, max = 50, message?: string): FormItemRule {
    return this.integer(min, max, message || `参与人数应为${min}-${max}人`)
  }

  /**
   * 等级验证（1-10级）
   */
  static skillLevel(message = '技能等级应为1-10级'): FormItemRule {
    return this.integer(1, 10, message)
  }

  /**
   * 日期范围验证
   */
  static dateRange(minDays = 0, maxDays = 365, message?: string): FormItemRule {
    return this.custom((value: any[]) => {
      if (!Array.isArray(value) || value.length !== 2) return false
      
      const [start, end] = value
      if (!start || !end) return false
      
      const startDate = new Date(start)
      const endDate = new Date(end)
      const now = new Date()
      
      // 检查日期有效性
      if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) return false
      
      // 结束日期不能早于开始日期
      if (endDate < startDate) return false
      
      // 开始日期不能早于今天
      if (minDays === 0) {
        const today = new Date()
        today.setHours(0, 0, 0, 0)
        if (startDate < today) return false
      }
      
      // 检查日期范围
      const diffDays = Math.ceil((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24))
      if (diffDays < minDays || diffDays > maxDays) return false
      
      return true
    }, message || `日期范围应在${minDays}-${maxDays}天之间`)
  }
}

/**
 * 表单验证工具类
 */
export class FormValidator {
  /**
   * 验证单个值
   */
  static async validateValue(value: any, rules: FormItemRule[]): Promise<ValidationResult> {
    for (const rule of rules) {
      try {
        if (rule.required && this.isEmpty(value)) {
          return {
            isValid: false,
            message: rule.message || '此项为必填项'
          }
        }

        if (!this.isEmpty(value)) {
          if (rule.min !== undefined || rule.max !== undefined) {
            const len = String(value).length
            if (rule.min !== undefined && len < rule.min) {
              return {
                isValid: false,
                message: rule.message || `长度不能少于${rule.min}个字符`
              }
            }
            if (rule.max !== undefined && len > rule.max) {
              return {
                isValid: false,
                message: rule.message || `长度不能超过${rule.max}个字符`
              }
            }
          }

          if (rule.pattern && !rule.pattern.test(String(value))) {
            return {
              isValid: false,
              message: rule.message || '格式不正确'
            }
          }

          if (rule.validator) {
            const result = await new Promise<boolean | string>((resolve) => {
              rule.validator!(rule, value, (error?: Error) => {
                if (error) {
                  resolve(error.message)
                } else {
                  resolve(true)
                }
              })
            })

            if (result !== true) {
              return {
                isValid: false,
                message: typeof result === 'string' ? result : rule.message || '验证失败'
              }
            }
          }
        }
      } catch (error) {
        return {
          isValid: false,
          message: rule.message || '验证出错'
        }
      }
    }

    return { isValid: true }
  }

  /**
   * 批量验证表单
   */
  static async validateForm(
    formData: Record<string, any>, 
    rules: Record<string, FormItemRule[]>
  ): Promise<BatchValidationResult> {
    const errors: ValidationResult[] = []

    for (const [field, fieldRules] of Object.entries(rules)) {
      const value = formData[field]
      const result = await this.validateValue(value, fieldRules)
      
      if (!result.isValid) {
        errors.push({
          ...result,
          field
        })
      }
    }

    return {
      isValid: errors.length === 0,
      errors,
      firstError: errors[0]?.message
    }
  }

  /**
   * 检查值是否为空
   */
  static isEmpty(value: any): boolean {
    if (value === null || value === undefined) return true
    if (typeof value === 'string' && value.trim() === '') return true
    if (Array.isArray(value) && value.length === 0) return true
    return false
  }

  /**
   * 清理表单数据（移除空值）
   */
  static cleanFormData(data: Record<string, any>): Record<string, any> {
    const cleaned: Record<string, any> = {}
    
    for (const [key, value] of Object.entries(data)) {
      if (!this.isEmpty(value)) {
        cleaned[key] = value
      }
    }
    
    return cleaned
  }

  /**
   * 获取字段的第一个错误信息
   */
  static getFieldError(field: string, errors: ValidationResult[]): string | undefined {
    return errors.find(error => error.field === field)?.message
  }

  /**
   * 检查字段是否有错误
   */
  static hasFieldError(field: string, errors: ValidationResult[]): boolean {
    return errors.some(error => error.field === field)
  }
}

/**
 * 常用验证规则组合
 */
export const ValidationRules = {
  // 必填文本
  requiredText: (message?: string) => [
    CommonValidators.required(message)
  ],

  // 用户名
  username: () => [
    CommonValidators.required('请输入用户名'),
    CommonValidators.username()
  ],

  // 邮箱
  email: (required = true) => [
    ...(required ? [CommonValidators.required('请输入邮箱')] : []),
    CommonValidators.email()
  ],

  // 手机号
  mobile: (required = true) => [
    ...(required ? [CommonValidators.required('请输入手机号')] : []),
    CommonValidators.mobile()
  ],

  // 密码
  password: (strong = false) => [
    CommonValidators.required('请输入密码'),
    CommonValidators.password(strong)
  ],

  // 确认密码
  confirmPassword: (passwordField: string) => [
    CommonValidators.required('请再次输入密码'),
    CommonValidators.confirmPassword(passwordField)
  ],

  // 中文姓名
  chineseName: () => [
    CommonValidators.required('请输入姓名'),
    CommonValidators.chineseName()
  ],

  // 活动标题
  activityTitle: () => [
    CommonValidators.required('请输入活动标题'),
    CommonValidators.length(2, 50, '活动标题长度应在2-50个字符之间')
  ],

  // 活动描述
  activityDescription: () => [
    CommonValidators.length(0, 500, '活动描述不能超过500个字符')
  ],

  // 场地编号
  courtNumber: () => [
    CommonValidators.required('请选择场地'),
    BusinessValidators.courtNumber()
  ],

  // 活动费用
  activityPrice: () => [
    CommonValidators.required('请输入活动费用'),
    BusinessValidators.activityPrice()
  ],

  // 参与人数
  participantCount: () => [
    CommonValidators.required('请输入参与人数'),
    BusinessValidators.participantCount()
  ]
}

// 导出默认实例
export default FormValidator