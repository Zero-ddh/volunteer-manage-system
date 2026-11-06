import request from '@/utils/request'

// 星级志愿者相关接口
export const starVolunteersApi = {
  // 获取星级志愿者列表
  getStarVolunteers: (params?: {
    name?: string
    starLevel?: number
    page?: number
    size?: number
  }) => request.get('/v1/star-volunteers/list', { params }),
  
  // 获取积分排行榜
  getPointsRanking: () => request.get('/v1/star-volunteers/ranking')
}

export default starVolunteersApi
