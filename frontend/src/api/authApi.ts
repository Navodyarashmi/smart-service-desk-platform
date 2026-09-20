import type {
  CurrentUser,
  LoginRequest,
  LoginResponse,
  RegistrationRequest,
  RegistrationResponse,
} from '../types/auth'
import { apiRequest } from './apiClient'

export { ApiRequestError } from './apiClient'

const ACCESS_TOKEN_KEY = 'helphub.accessToken'

export function registerUser(
  registration: RegistrationRequest,
): Promise<RegistrationResponse> {
  return apiRequest<RegistrationResponse>('/api/v1/auth/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(registration),
  })
}

export function loginUser(login: LoginRequest): Promise<LoginResponse> {
  return apiRequest<LoginResponse>('/api/v1/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(login),
  })
}

export function getCurrentUser(
  accessToken: string,
): Promise<CurrentUser> {
  return apiRequest<CurrentUser>('/api/v1/users/me', {
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  })
}

export function saveAccessToken(accessToken: string): void {
  sessionStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
}

export function getAccessToken(): string | null {
  return sessionStorage.getItem(ACCESS_TOKEN_KEY)
}

export function clearAccessToken(): void {
  sessionStorage.removeItem(ACCESS_TOKEN_KEY)
}