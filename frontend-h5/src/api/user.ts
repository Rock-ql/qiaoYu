import http from './http';

export interface UpdateProfileReq { userId: string; nickname: string; avatar?: string }
export interface UpdateAvatarReq { userId: string; avatar?: string }

export const userApi = {
  detail: (userId: string) => http.post('/user/detail', { userId }),
  list: () => http.post('/user/list', {}),
  updateProfile: (data: UpdateProfileReq) => http.post('/user/updateProfile', data),
  updateAvatar: (data: UpdateAvatarReq) => http.post('/user/updateAvatar', data),
  disable: (userId: string) => http.post('/user/disable', { userId }),
  enable: (userId: string) => http.post('/user/enable', { userId }),
};

