import type { AdminUser, AdminUserUpdate } from '../types/auth'
import { apiRequest } from './apiClient'

function authorizationHeader(accessToken: string) {
  return { Authorization: `Bearer ${accessToken}` }
}

export function getAdminUsers(accessToken: string): Promise<AdminUser[]> {
  return apiRequest<AdminUser[]>('/api/v1/admin/users', {
    headers: authorizationHeader(accessToken),
  })
}

export function updateAdminUser(
  accessToken: string,
  userId: string,
  update: AdminUserUpdate,
): Promise<AdminUser> {
  return apiRequest<AdminUser>(`/api/v1/admin/users/${userId}`, {
    method: 'PATCH',
    headers: {
      ...authorizationHeader(accessToken),
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(update),
  })
}
