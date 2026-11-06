import request from '@/utils/request'

export function apiGetProfile(username: string) {
  return request({
    url: '/user/profile',
    method: 'get',
    params: { username },
  })
}

export function apiUpdateProfile(data: any) {
  return request({
    url: '/user/profile',
    method: 'put',
    data,
  })
}

export function apiUploadAvatar(userId: number, file: File) {
  const formData = new FormData()
  formData.append('userId', String(userId))
  formData.append('file', file)
  return request({
    url: '/user/avatar',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function apiListSkills(userId: number) {
  return request({
    url: '/user/skills',
    method: 'get',
    params: { userId },
  })
}

export function apiAddSkill(data: any) {
  return request({
    url: '/user/skills',
    method: 'post',
    data,
  })
}

export function apiUpdateSkill(data: any) {
  return request({
    url: '/user/skills',
    method: 'put',
    data,
  })
}

export function apiDeleteSkill(id: number) {
  return request({
    url: `/user/skills/${id}`,
    method: 'delete',
  })
}

// 技能分类相关接口
export function apiGetSkillCategories() {
  return request({
    url: '/v1/skills/categories',
    method: 'get',
  })
}

export function apiGetSkillCategoryTree() {
  return request({
    url: '/v1/skills/categories/tree',
    method: 'get',
  })
}

// 用户技能相关接口
export function apiGetUserSkills(userId: number) {
  return request({
    url: `/v1/skills/user/${userId}`,
    method: 'get',
  })
}

export function apiAddUserSkill(data: any) {
  return request({
    url: '/v1/skills/user',
    method: 'post',
    data,
  })
}

export function apiUpdateUserSkill(id: number, data: any) {
  return request({
    url: `/v1/skills/user/${id}`,
    method: 'put',
    data,
  })
}

export function apiDeleteUserSkill(id: number) {
  return request({
    url: `/v1/skills/user/${id}`,
    method: 'delete',
  })
}

// 证书上传相关接口
export function apiUploadCertificate(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/v1/upload/certificate',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function apiDeleteCertificateFile(url: string) {
  return request({
    url: '/v1/upload/certificate',
    method: 'delete',
    params: { url },
  })
}

// 用户证书相关接口
export function apiGetUserCertificates(userId: number) {
  return request({
    url: `/v1/skills/certificate/user/${userId}`,
    method: 'get',
  })
}

export function apiAddCertificate(data: any) {
  return request({
    url: '/v1/skills/certificate',
    method: 'post',
    data,
  })
}

export function apiUpdateCertificate(id: number, data: any) {
  return request({
    url: `/v1/skills/certificate/${id}`,
    method: 'put',
    data,
  })
}

export function apiDeleteCertificate(id: number) {
  return request({
    url: `/v1/skills/certificate/${id}`,
    method: 'delete',
  })
}

export function apiAuditCertificate(id: number, data: any) {
  return request({
    url: `/v1/skills/certificate/${id}/audit`,
    method: 'put',
    data,
  })
}

export function apiGetPendingCertificates() {
  return request({
    url: '/v1/skills/certificate/pending',
    method: 'get',
  })
}

// 活动技能匹配相关接口
export function apiGetActivitySkillRequirements(activityId: number) {
  return request({
    url: `/v1/activities/${activityId}/skill-requirements`,
    method: 'get',
  })
}

export function apiSetActivitySkillRequirements(activityId: number, requirements: any[]) {
  return request({
    url: `/v1/activities/${activityId}/skill-requirements`,
    method: 'post',
    data: requirements,
  })
}

export function apiGetVolunteersBySkill(activityId: number, skillCategoryId?: number, skillName?: string) {
  return request({
    url: `/v1/activities/${activityId}/volunteers/by-skill`,
    method: 'get',
    params: { skillCategoryId, skillName },
  })
}

export function apiGetVolunteerMatchScore(activityId: number, userId: number) {
  return request({
    url: `/v1/activities/${activityId}/volunteers/${userId}/match-score`,
    method: 'get',
  })
}

export function apiGetVolunteerMatchDetails(activityId: number, userId: number) {
  return request({
    url: `/v1/activities/${activityId}/volunteers/${userId}/match-details`,
    method: 'get',
  })
}

export function apiGetRecommendedVolunteers(activityId: number, limit: number = 10) {
  return request({
    url: `/v1/activities/${activityId}/recommended-volunteers`,
    method: 'get',
    params: { limit },
  })
}

