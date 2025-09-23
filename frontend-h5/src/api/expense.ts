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
  create: (data: CreateExpenseReq) => http.post('/expense/create', data),
  createShares: (data: CreateSharesReq) => http.post('/expense/shares', data),
  confirm: (shareId: string, userId: string) => http.post('/expense/confirm', { shareId, userId }),
  paid: (shareId: string, userId: string) => http.post('/expense/paid', { shareId, userId }),
  activityExpenses: (activityId: string) => http.post('/expense/activityExpenses', { activityId }),
  expenseShares: (expenseId: string) => http.post('/expense/expenseShares', { expenseId }),
  userShares: (userId: string) => http.post('/expense/userShares', { userId }),
  deleteExpense: (expenseId: string, userId: string) => http.post('/expense/delete', { expenseId, userId }),
};

