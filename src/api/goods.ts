import request from '@/utils/request'

// 商品相关接口
export const goodsApi = {
  // 获取商品列表
  getGoodsList: () => request.get('/v1/goods/list'),
  
  // 兑换商品（带收货信息）
  exchangeGoods: (data: { goodsId: number, receiverName: string, receiverPhone: string, receiverAddress: string }) => 
    request.post('/v1/goods/exchange', data),
  
  // 获取用户兑换记录（支持分页）
  getExchangeRecords: (params?: { page?: number, size?: number }) => request.get('/v1/goods/user/exchange-records', { params }),
  
  // 获取用户总积分
  getUserTotalPoints: () => request.get('/v1/goods/user/total-points'),
  
  // 确认收货
  confirmReceipt: (exchangeId: number) => request.put(`/v1/goods/user/exchange-records/${exchangeId}/confirm-receipt`)
}

export default goodsApi
