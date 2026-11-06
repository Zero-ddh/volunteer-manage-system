import request from '@/utils/request'

export const adminUserApi = {
  // 用户管理
  list: (params?: any) => request.get('/admin/users', { params }),
  detail: (id: number) => request.get(`/admin/users/${id}`),
  create: (data: any) => request.post('/admin/users', data),
  update: (data: any) => request.put('/admin/users', data),
  delete: (id: number) => request.delete(`/admin/users/${id}`),
  updateStatus: (id: number, status: number) => request.put(`/admin/users/${id}/status`, null, { params: { status } }),
  resetPassword: (id: number, password: string) => request.put(`/admin/users/${id}/password`, null, { params: { password } }),
  assignRoles: (id: number, roleIds: number[]) => request.put(`/admin/users/${id}/roles`, roleIds),
  statistics: () => request.get('/admin/users/statistics'),
  
  // 用户角色绑定
  getUserRoles: (userId: number) => request.get(`/admin/user/${userId}/roles`),
  setUserRoles: (userId: number, roleIds: number[]) => request.post(`/admin/user/${userId}/roles`, roleIds),
}


