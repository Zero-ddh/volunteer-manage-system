import request from '@/utils/request'

export const roleApi = {
  list: (params?: any) => request.get('/admin/role/list', { params }),
  create: (data: any) => request.post('/admin/role', data),
  update: (data: any) => request.put('/admin/role', data),
  remove: (id: number) => request.delete(`/admin/role/${id}`),
  bindMenus: (roleId: number, menuIds: number[]) => request.post(`/admin/role/${roleId}/menus`, menuIds),
  getRoleMenus: (roleId: number) => request.get(`/admin/role/${roleId}/menus`),
}


