import request from '@/utils/request'

export const menuApi = {
  list: (params?: any) => request.get('/admin/menu/list', { params }),
  create: (data: any) => request.post('/admin/menu', data),
  update: (data: any) => request.put('/admin/menu', data),
  remove: (id: number) => request.delete(`/admin/menu/${id}`),
}


