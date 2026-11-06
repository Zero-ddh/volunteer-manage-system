import request from '@/utils/request'

// 获取保险产品列表
export function getInsuranceProducts() {
  return request({ url: '/v1/insurance/products', method: 'get' })
}

// 创建保险订单
export function createInsuranceOrder(data: { productId: number; paymentMethod?: string }) {
  return request({ url: '/v1/insurance/order', method: 'post', data })
}

// 模拟支付（100%成功）
export function mockPayment(data: { orderNo: string; paymentMethod?: string }) {
  return request({ url: '/v1/insurance/payment/mock', method: 'post', data })
}

// 获取用户保险订单（支持分页和搜索）
export function getUserInsuranceOrders(params?: {
  pageNo?: number;
  pageSize?: number;
  orderNo?: string;
  productName?: string;
}) {
  return request({ 
    url: '/v1/insurance/orders', 
    method: 'get',
    params: params || {}
  })
}

// 获取订单详情
export function getInsuranceOrderDetail(orderNo: string) {
  return request({ url: `/v1/insurance/order/${orderNo}`, method: 'get' })
}

