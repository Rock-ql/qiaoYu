import { defineStore } from 'pinia';

export interface UserInfo { id: string; phone: string; nickname: string; avatar?: string }

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: (localStorage.getItem('token') || '') as string,
    user: (localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')!) : null) as UserInfo | null,
  }),
  actions: {
    setAuth(token: string, user: UserInfo) {
      this.token = token;
      this.user = user;
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
    },
    logout() {
      this.token = '';
      this.user = null;
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    },
    isAuthed() { return !!this.token; },
  },
});

