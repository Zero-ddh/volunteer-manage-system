import request from '@/utils/request'

export const volunteerApi = {
  // 志愿者管理
  list: (params?: any) => request.post('/admin/volunteers/list', params),
  detail: (id: number) => request.get(`/admin/volunteers/detail/${id}`),
  update: (data: any) => request.put('/admin/volunteers/update', data),
  updateStatus: (id: number, status: number) => request.put(`/admin/volunteers/status/${id}`, null, { params: { status } }),
  batchOperate: (data: any) => request.post('/admin/volunteers/batch', data),
  delete: (id: number) => request.delete(`/admin/volunteers/delete/${id}`),
  statistics: () => request.get('/admin/volunteers/statistics'),
  // 手动评级志愿者
  rate: (id: number, starLevel: number) => request.put(`/admin/volunteers/rate/${id}`, null, { params: { starLevel } }),
  // 更新志愿者评级
  updateRating: (data: any) => request.put('/admin/volunteers/rating', data),
  // 获取志愿者的紧急联系人信息
  getEmergencyContacts: (userId: number) => request.get(`/admin/volunteers/${userId}/emergency-contacts`),
}
