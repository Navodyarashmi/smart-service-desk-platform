export type Role = 'EMPLOYEE' | 'TECHNICIAN' | 'ADMINISTRATOR'

export interface RegistrationRequest {
  email: string
  password: string
  fullName: string
}

export interface RegistrationResponse {
  id: string
  email: string
  fullName: string
  role: Role
}

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  tokenType: 'Bearer'
  expiresAt: string
  userId: string
  email: string
  fullName: string
  roles: Role[]
}

export interface CurrentUser {
  id: string
  email: string
  fullName: string
  roles: Role[]
}

export interface ApiErrorResponse {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
  fieldErrors: Record<string, string>
}
