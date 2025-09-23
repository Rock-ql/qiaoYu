import http from './http';

export interface CreateActivityReq {
  organizerId: string;
  title: string;
  venue: string;
  startTime: string;
  endTime: string;
  maxPlayers: number;
  description?: string;
  address?: string;
}

export interface JoinActivityReq {
  activityId: string; userId: string; remark?: string
}

export const activityApi = {
  create: (data: CreateActivityReq) => http.post('/activity/create', data) as unknown as Promise<any>,
  join: (data: JoinActivityReq) => http.post('/activity/join', data) as unknown as Promise<any>,
  leave: (data: JoinActivityReq) => http.post('/activity/leave', data) as unknown as Promise<any>,
  cancel: (data: JoinActivityReq) => http.post('/activity/cancel', data) as unknown as Promise<any>,
  start: (data: JoinActivityReq) => http.post('/activity/start', data) as unknown as Promise<any>,
  complete: (data: JoinActivityReq) => http.post('/activity/complete', data) as unknown as Promise<any>,
  detail: (activityId: string) => http.post('/activity/detail', { activityId }) as unknown as Promise<any>,
  available: () => http.post('/activity/available', {}) as unknown as Promise<any[]>,
  byStatus: (status: number) => http.post('/activity/byStatus', { status }) as unknown as Promise<any[]>,
  byOrganizer: (userId: string) => http.post('/activity/byOrganizer', { userId }) as unknown as Promise<any[]>,
  byTimeRange: (startTime: string, endTime: string) => http.post('/activity/byTimeRange', { startTime, endTime }) as unknown as Promise<any[]>,
};
