import type {
  CreateTicketRequest,
  TicketResponse,
  TicketSummary,
} from '../types/ticket'
import { apiRequest } from './apiClient'

export function getTickets(
  accessToken: string,
): Promise<TicketSummary[]> {
  return apiRequest<TicketSummary[]>('/api/v1/tickets', {
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  })
}

export function createTicket(
  accessToken: string,
  ticket: CreateTicketRequest,
): Promise<TicketResponse> {
  return apiRequest<TicketResponse>('/api/v1/tickets', {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${accessToken}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(ticket),
  })
}