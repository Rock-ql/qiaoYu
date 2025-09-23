// 仪表板API接口
// =======================================

import { request } from '@/utils/request'

/**
 * 统计数据接口
 */
export interface DashboardStatistics {
  totalRevenue: {
    value: number
    change: string
    changeType: 'increase' | 'decrease'
  }
  activeUsers: {
    value: number
    change: string
    changeType: 'increase' | 'decrease'
  }
  totalBookings: {
    value: number
    change: string
    changeType: 'increase' | 'decrease'
  }
  courtUsage: {
    value: number
    change: string
    changeType: 'increase' | 'decrease'
  }
}

/**
 * 仪表板API类
 */
export class DashboardApi {
  /**
   * 获取仪表板统计数据
   */
  async getStatistics(): Promise<DashboardStatistics> {
    const response = await request.get<DashboardStatistics>('/dashboard/statistics')
    return response.data
  }

  /**
   * 导出仪表板数据
   */
  async exportData(format: 'xlsx' | 'csv' = 'xlsx'): Promise<void> {
    await request.download('/dashboard/export', `dashboard_data.${format}`, {
      params: { format }
    })
  }
}

// 创建实例
export const dashboardApi = new DashboardApi()

// 导出默认实例
export default dashboardApi