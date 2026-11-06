import request from '@/utils/request'

export interface Message {
  id: number
  userId: number
  messageType: number
  title: string
  content: string
  relatedId?: number
  relatedType?: string
  isRead: number
  readTime?: string
  createdTime: string
  updatedTime: string
}

export interface MessageListParams {
  messageType?: number
  isRead?: number
  pageNo?: number
  pageSize?: number
}

export const messageApi = {
  // 获取消息列表
  getList(params: MessageListParams) {
    return request.get('/v1/messages/list', { params })
  },

  // 标记消息为已读
  markAsRead(id: number) {
    return request.put(`/v1/messages/${id}/read`)
  },

  // 获取未读消息数量
  getUnreadCount() {
    return request.get('/v1/messages/unread-count')
  },

  // 批量标记为已读
  markAllAsRead(messageType?: number) {
    return request.put('/v1/messages/mark-all-read', null, {
      params: messageType ? { messageType } : {}
    })
  }
}
