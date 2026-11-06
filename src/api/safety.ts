import request from '@/utils/request'

// 紧急联系人
export function listEmergencyContacts() {
  return request({ url: '/v1/safety/emergency-contact', method: 'get' })
}

export function createEmergencyContact(data: any) {
  return request({ url: '/v1/safety/emergency-contact', method: 'post', data })
}

export function updateEmergencyContact(id: number, data: any) {
  return request({ url: `/v1/safety/emergency-contact/${id}`, method: 'put', data })
}

export function deleteEmergencyContact(id: number) {
  return request({ url: `/v1/safety/emergency-contact/${id}`, method: 'delete' })
}

export function setPrimaryEmergencyContact(id: number) {
  return request({ url: `/v1/safety/emergency-contact/${id}/primary`, method: 'put' })
}

// 紧急上报 - 已屏蔽
// export function createEmergencyReport(data: any) {
//   return request({ url: '/v1/safety/emergency-report', method: 'post', data })
// }

// export function getEmergencyReport(id: number) {
//   return request({ url: `/v1/safety/emergency-report/${id}`, method: 'get' })
// }

// export function listMyEmergencyReports(params: { pageNo: number; pageSize: number }) {
//   return request({ url: '/v1/safety/emergency-report/list', method: 'get', params })
// }


