/**
 * 日期时间格式化工具函数
 */

/**
 * 格式化日期时间
 * @param date 日期对象或时间戳
 * @param format 格式化模式，默认为 'YYYY-MM-DD HH:mm:ss'
 * @returns 格式化后的日期字符串
 */
export function formatDateTime(date: Date | string | number, format: string = 'YYYY-MM-DD HH:mm:ss'): string {
  if (!date) return ''
  
  let d: Date
  if (typeof date === 'string') {
    d = new Date(date)
  } else if (typeof date === 'number') {
    d = new Date(date)
  } else {
    d = date
  }
  
  if (isNaN(d.getTime())) return ''
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化日期
 * @param date 日期对象或时间戳
 * @returns 格式化后的日期字符串 YYYY-MM-DD
 */
export function formatDate(date: Date | string | number): string {
  return formatDateTime(date, 'YYYY-MM-DD')
}

/**
 * 格式化时间
 * @param date 日期对象或时间戳
 * @returns 格式化后的时间字符串 HH:mm:ss
 */
export function formatTime(date: Date | string | number): string {
  return formatDateTime(date, 'HH:mm:ss')
}

/**
 * 获取相对时间描述
 * @param date 日期对象或时间戳
 * @returns 相对时间描述
 */
export function getRelativeTime(date: Date | string | number): string {
  if (!date) return ''
  
  let d: Date
  if (typeof date === 'string') {
    d = new Date(date)
  } else if (typeof date === 'number') {
    d = new Date(date)
  } else {
    d = date
  }
  
  if (isNaN(d.getTime())) return ''
  
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  const week = 7 * day
  const month = 30 * day
  const year = 365 * day
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < week) {
    return `${Math.floor(diff / day)}天前`
  } else if (diff < month) {
    return `${Math.floor(diff / week)}周前`
  } else if (diff < year) {
    return `${Math.floor(diff / month)}个月前`
  } else {
    return `${Math.floor(diff / year)}年前`
  }
}

/**
 * 判断是否为今天
 * @param date 日期对象或时间戳
 * @returns 是否为今天
 */
export function isToday(date: Date | string | number): boolean {
  if (!date) return false
  
  let d: Date
  if (typeof date === 'string') {
    d = new Date(date)
  } else if (typeof date === 'number') {
    d = new Date(date)
  } else {
    d = date
  }
  
  if (isNaN(d.getTime())) return false
  
  const today = new Date()
  return d.getFullYear() === today.getFullYear() &&
         d.getMonth() === today.getMonth() &&
         d.getDate() === today.getDate()
}

/**
 * 判断是否为昨天
 * @param date 日期对象或时间戳
 * @returns 是否为昨天
 */
export function isYesterday(date: Date | string | number): boolean {
  if (!date) return false
  
  let d: Date
  if (typeof date === 'string') {
    d = new Date(date)
  } else if (typeof date === 'number') {
    d = new Date(date)
  } else {
    d = date
  }
  
  if (isNaN(d.getTime())) return false
  
  const yesterday = new Date()
  yesterday.setDate(yesterday.getDate() - 1)
  
  return d.getFullYear() === yesterday.getFullYear() &&
         d.getMonth() === yesterday.getMonth() &&
         d.getDate() === yesterday.getDate()
}
