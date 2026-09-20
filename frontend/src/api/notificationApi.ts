import type { NotificationItem } from '../types/ticket'
import { apiRequest } from './apiClient'

function headers(accessToken: string) { return { Authorization: `Bearer ${accessToken}` } }

export function getNotifications(accessToken: string): Promise<NotificationItem[]> {
  return apiRequest<NotificationItem[]>('/api/v1/notifications', { headers: headers(accessToken) })
}

export function markNotificationRead(accessToken: string, id: string): Promise<NotificationItem> {
  return apiRequest<NotificationItem>(`/api/v1/notifications/${id}/read`, { method: 'PATCH', headers: headers(accessToken) })
}
