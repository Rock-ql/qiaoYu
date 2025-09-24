// 数据格式化工具类
// =======================================

import dayjs from 'dayjs'

/**
 * 日期格式化
 */
export function formatDate(date: string | number | Date, format = 'YYYY-MM-DD HH:mm:ss'): string {
  if (!date) return '-'
  return dayjs(date).format(format)
}

/**
 * 相对时间格式化
 */
export function formatRelativeTime(date: string | number | Date): string {
  if (!date) return '-'
  return dayjs(date).fromNow()
}

/**
 * 货币格式化
 */
export function formatCurrency(amount: number | string, currency = '¥', decimals = 2): string {
  if (amount === null || amount === undefined || amount === '') return '-'
  
  const num = Number(amount)
  if (isNaN(num)) return '-'
  
  return `${currency}${num.toFixed(decimals).replace(/\B(?=(\d{3})+(?!\d))/g, ',')}`
}

/**
 * 数字格式化（千分位）
 */
export function formatNumber(num: number | string, decimals = 0): string {
  if (num === null || num === undefined || num === '') return '-'
  
  const number = Number(num)
  if (isNaN(number)) return '-'
  
  return number.toFixed(decimals).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/**
 * 百分比格式化
 */
export function formatPercent(num: number | string, decimals = 2): string {
  if (num === null || num === undefined || num === '') return '-'
  
  const number = Number(num)
  if (isNaN(number)) return '-'
  
  return `${(number * 100).toFixed(decimals)}%`
}

/**
 * 文件大小格式化
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  if (!bytes || bytes < 0) return '-'
  
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`
}

/**
 * 手机号格式化（隐藏中间4位）
 */
export function formatPhone(phone: string): string {
  if (!phone) return '-'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 身份证号格式化（隐藏中间位）
 */
export function formatIdCard(idCard: string): string {
  if (!idCard) return '-'
  return idCard.replace(/(\d{6})\d{8}(\d{4})/, '$1********$2')
}

/**
 * 银行卡号格式化
 */
export function formatBankCard(cardNo: string): string {
  if (!cardNo) return '-'
  return cardNo.replace(/(\d{4})\d*(\d{4})/, '$1 **** **** $2')
}

/**
 * 时长格式化（秒转换为时分秒）
 */
export function formatDuration(seconds: number): string {
  if (!seconds || seconds < 0) return '-'
  
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  
  if (hours > 0) {
    return `${hours}时${minutes}分${secs}秒`
  } else if (minutes > 0) {
    return `${minutes}分${secs}秒`
  } else {
    return `${secs}秒`
  }
}

/**
 * 字符串截断
 */
export function truncateString(str: string, length = 20, suffix = '...'): string {
  if (!str) return '-'
  if (str.length <= length) return str
  return str.substring(0, length) + suffix
}

/**
 * 状态格式化
 */
export function formatStatus(
  status: string | number, 
  statusMap: Record<string | number, { text: string; type?: string }>
): { text: string; type: string } {
  const config = statusMap[status]
  return {
    text: config?.text || String(status),
    type: config?.type || 'info'
  }
}

/**
 * 地址格式化
 */
export function formatAddress(province?: string, city?: string, district?: string, detail?: string): string {
  const parts = [province, city, district, detail].filter(Boolean)
  return parts.length > 0 ? parts.join(' ') : '-'
}

/**
 * 性别格式化
 */
export function formatGender(gender: string | number): string {
  const genderMap: Record<string | number, string> = {
    0: '未知',
    1: '男',
    2: '女',
    male: '男',
    female: '女',
    unknown: '未知'
  }
  return genderMap[gender] || '未知'
}

/**
 * 年龄格式化
 */
export function formatAge(birthDate: string | Date): string {
  if (!birthDate) return '-'
  
  const birth = dayjs(birthDate)
  const now = dayjs()
  const years = now.diff(birth, 'year')
  
  if (years > 0) {
    return `${years}岁`
  } else {
    const months = now.diff(birth, 'month')
    return months > 0 ? `${months}个月` : '不足1个月'
  }
}

/**
 * 羽毛球等级格式化
 */
export function formatBadmintonLevel(level: number): string {
  if (!level || level < 1 || level > 10) return '未定级'
  
  const levelNames = [
    '', '入门', '初级', '初中级', '中级', '中高级', 
    '高级', '准专业', '专业', '职业', '国际级'
  ]
  
  return `${level}级(${levelNames[level]})`
}

/**
 * 羽毛球活动状态格式化
 */
export function formatActivityStatus(status: string): { text: string; type: string } {
  const statusMap: Record<string, { text: string; type: string }> = {
    draft: { text: '草稿', type: 'info' },
    published: { text: '已发布', type: 'success' },
    ongoing: { text: '进行中', type: 'warning' },
    completed: { text: '已完成', type: 'success' },
    cancelled: { text: '已取消', type: 'danger' },
    full: { text: '已满员', type: 'warning' }
  }
  
  return statusMap[status] || { text: status, type: 'info' }
}

/**
 * 支付状态格式化
 */
export function formatPaymentStatus(status: string): { text: string; type: string } {
  const statusMap: Record<string, { text: string; type: string }> = {
    pending: { text: '待支付', type: 'warning' },
    paid: { text: '已支付', type: 'success' },
    refunded: { text: '已退款', type: 'info' },
    failed: { text: '支付失败', type: 'danger' },
    cancelled: { text: '已取消', type: 'info' }
  }
  
  return statusMap[status] || { text: status, type: 'info' }
}

/**
 * 用户状态格式化
 */
export function formatUserStatus(status: string): { text: string; type: string } {
  const statusMap: Record<string, { text: string; type: string }> = {
    active: { text: '正常', type: 'success' },
    inactive: { text: '未激活', type: 'warning' },
    locked: { text: '已锁定', type: 'danger' },
    banned: { text: '已封禁', type: 'danger' }
  }
  
  return statusMap[status] || { text: status, type: 'info' }
}

/**
 * 场地状态格式化
 */
export function formatCourtStatus(status: string): { text: string; type: string } {
  const statusMap: Record<string, { text: string; type: string }> = {
    available: { text: '可用', type: 'success' },
    occupied: { text: '占用中', type: 'warning' },
    maintenance: { text: '维护中', type: 'danger' },
    disabled: { text: '已停用', type: 'info' }
  }
  
  return statusMap[status] || { text: status, type: 'info' }
}

/**
 * 羽毛球拍类型格式化
 */
export function formatRacketType(type: string): string {
  const typeMap: Record<string, string> = {
    attack: '进攻型',
    defense: '防守型',
    balanced: '平衡型',
    speed: '速度型',
    control: '控制型'
  }
  
  return typeMap[type] || type
}

/**
 * 比赛结果格式化
 */
export function formatMatchResult(result: { player1Score: number; player2Score: number }): string {
  if (!result) return '-'
  return `${result.player1Score} : ${result.player2Score}`
}

// 导出所有格式化函数
export default {
  formatDate,
  formatRelativeTime,
  formatCurrency,
  formatNumber,
  formatPercent,
  formatFileSize,
  formatPhone,
  formatIdCard,
  formatBankCard,
  formatDuration,
  truncateString,
  formatStatus,
  formatAddress,
  formatGender,
  formatAge,
  formatBadmintonLevel,
  formatActivityStatus,
  formatPaymentStatus,
  formatUserStatus,
  formatCourtStatus,
  formatRacketType,
  formatMatchResult
}