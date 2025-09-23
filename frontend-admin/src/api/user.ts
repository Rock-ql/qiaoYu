import http from './http';

export interface UpdateProfileReq { userId: string; nickname: string; avatar?: string }
export interface UpdateAvatarReq { userId: string; avatar?: string }

export const userApi = {
  detail: (userId: string) => http.post('/user/detail', { userId }) as unknown as Promise<any>,
  list: () => http.post('/user/list', {}) as unknown as Promise<any[]>,
  updateProfile: (data: UpdateProfileReq) => http.post('/user/updateProfile', data) as unknown as Promise<any>,
  updateAvatar: (data: UpdateAvatarReq) => http.post('/user/updateAvatar', data) as unknown as Promise<any>,
  disable: (userId: string) => http.post('/user/disable', { userId }) as unknown as Promise<any>,
  enable: (userId: string) => http.post('/user/enable', { userId }) as unknown as Promise<any>,
};
