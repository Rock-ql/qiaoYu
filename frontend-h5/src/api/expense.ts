import http from './http';

export interface CreateExpenseReq {
  activityId: string;
  payerId: string;
  title: string;
  totalAmount: string | number;
  description?: string;
  shareType?: number;
}

export interface CreateSharesReq {
  expenseId: string;
  participantIds: string[];
  customAmounts?: Record<string, string | number>;
}

export const expenseApi = {
  create: (data: CreateExpenseReq) => http.post('/expense/create', data) as unknown as Promise<any>,
  createShares: (data: CreateSharesReq) => http.post('/expense/shares', data) as unknown as Promise<any[]>,
  confirm: (shareId: string, userId: string) => http.post('/expense/confirm', { shareId, userId }) as unknown as Promise<any>,
  paid: (shareId: string, userId: string) => http.post('/expense/paid', { shareId, userId }) as unknown as Promise<any>,
  activityExpenses: (activityId: string) => http.post('/expense/activityExpenses', { activityId }) as unknown as Promise<any[]>,
  expenseShares: (expenseId: string) => http.post('/expense/expenseShares', { expenseId }) as unknown as Promise<any[]>,
  userShares: (userId: string) => http.post('/expense/userShares', { userId }) as unknown as Promise<any[]>,
  deleteExpense: (expenseId: string, userId: string) => http.post('/expense/delete', { expenseId, userId }) as unknown as Promise<any>,
};
