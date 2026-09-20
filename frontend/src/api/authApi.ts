import type {
  ApiErrorResponse,
  CurrentUser,
  LoginRequest,
  LoginResponse,
  RegistrationRequest,
  RegistrationResponse,
} from '../types/auth'

const ACCESS_TOKEN_KEY = 'helphub.accessToken'

export class ApiRequestError extends Error {
  readonly status: number
  readonly fieldErrors: Record<string, string>

  constructor(
    message: string,
    status: number,
    fieldErrors: Record<string, string> = {},
  ) {
    super(message)
    this.name = 'ApiRequestError'
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

async function request<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const response = await fetch(path, {
    ...options,
    headers: {
      Accept: 'application/json',
      ...options.headers,
    },
  })

  const responseText = await response.text()
  const responseData = responseText
    ? (JSON.parse(responseText) as T | ApiErrorResponse)
    : null

  if (!response.ok) {
    const errorResponse = responseData as ApiErrorResponse | null

    throw new ApiRequestError(
      errorResponse?.message ?? 'Something went wrong. Please try again.',
      response.status,
      errorResponse?.fieldErrors ?? {},
    )
  }

  return responseData as T
}

export function registerUser(
  registration: RegistrationRequest,
): Promise<RegistrationResponse> {
  return request<RegistrationResponse>('/api/v1/auth/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(registration),
  })
}

export function loginUser(login: LoginRequest): Promise<LoginResponse> {
  return request<LoginResponse>('/api/v1/auth/login', {
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
  return request<CurrentUser>('/api/v1/users/me', {
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