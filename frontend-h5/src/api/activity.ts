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
  create: (data: CreateActivityReq) => http.post('/activity/create', data),
  join: (data: JoinActivityReq) => http.post('/activity/join', data),
  leave: (data: JoinActivityReq) => http.post('/activity/leave', data),
  cancel: (data: JoinActivityReq) => http.post('/activity/cancel', data),
  start: (data: JoinActivityReq) => http.post('/activity/start', data),
  complete: (data: JoinActivityReq) => http.post('/activity/complete', data),
  detail: (activityId: string) => http.post('/activity/detail', { activityId }),
  available: () => http.post('/activity/available', {}),
  byStatus: (status: number) => http.post('/activity/byStatus', { status }),
  byOrganizer: (userId: string) => http.post('/activity/byOrganizer', { userId }),
  byTimeRange: (startTime: string, endTime: string) => http.post('/activity/byTimeRange', { startTime, endTime }),
};

