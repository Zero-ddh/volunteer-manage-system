import request from '@/utils/request'

export function apiRegister(data: { 
  username: string; 
  password: string; 
  realName?: string; 
  phone?: string; 
  email?: string;
  idCard?: string;
  gender?: number;
  age?: number;
  address?: string;
  introduction?: string;
}) {
  return request({
    url: '/auth/register',
    method: 'post',
    data,
  })
}

export function apiLogin(data: { username: string; password: string }) {
  return request({
    url: '/auth/login',
    method: 'post',
    data,
  })
}

export function apiLogout() {
  return request({
    url: '/auth/logout',
    method: 'post',
  })
}


