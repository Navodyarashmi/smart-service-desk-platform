import type { ApiErrorResponse } from '../types/auth'

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

export async function apiRequest<T>(
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
  let responseData: T | ApiErrorResponse | null = null

  if (responseText) {
    try {
      responseData = JSON.parse(responseText) as T | ApiErrorResponse
    } catch {
      responseData = null
    }
  }

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