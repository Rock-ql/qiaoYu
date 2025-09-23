import http from './http';

export interface RegisterReq { phone: string; nickname: string; password: string }
export interface LoginReq { phone: string; password: string }
export interface WechatReq { code: string; encryptedData?: string; iv?: string }

export const authApi = {
  register: (data: RegisterReq) => http.post('/auth/register', data),
  login: (data: LoginReq) => http.post('/auth/login', data),
  wechat: (data: WechatReq) => http.post('/auth/wechat', data),
};

