import type {
  CreateTicketRequest,
  TicketDetails,
  TicketResponse,
  TicketSummary,
  UpdateTicketRequest,
} from '../types/ticket'
import { apiRequest } from './apiClient'

function authorizationHeader(accessToken: string) {
  return {
    Authorization: `Bearer ${accessToken}`,
  }
}

export function getTickets(
  accessToken: string,
): Promise<TicketSummary[]> {
  return apiRequest<TicketSummary[]>('/api/v1/tickets', {
    headers: authorizationHeader(accessToken),
  })
}

export function getStaffTickets(
  accessToken: string,
): Promise<TicketSummary[]> {
  return apiRequest<TicketSummary[]>('/api/v1/staff/tickets', {
    headers: authorizationHeader(accessToken),
  })
}

export function getTicket(
  accessToken: string,
  ticketId: string,
): Promise<TicketDetails> {
  return apiRequest<TicketDetails>(`/api/v1/tickets/${ticketId}`, {
    headers: authorizationHeader(accessToken),
  })
}

export function getStaffTicket(
  accessToken: string,
  ticketId: string,
): Promise<TicketDetails> {
  return apiRequest<TicketDetails>(`/api/v1/staff/tickets/${ticketId}`, {
    headers: authorizationHeader(accessToken),
  })
}

export function claimTicket(
  accessToken: string,
  ticketId: string,
): Promise<TicketDetails> {
  return apiRequest<TicketDetails>(
    `/api/v1/staff/tickets/${ticketId}/claim`,
    {
      method: 'POST',
      headers: authorizationHeader(accessToken),
    },
  )
}

export function changeTicketStatus(
  accessToken: string,
  ticketId: string,
  status: 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED',
): Promise<TicketDetails> {
  return apiRequest<TicketDetails>(
    `/api/v1/staff/tickets/${ticketId}/status`,
    {
      method: 'PATCH',
      headers: {
        ...authorizationHeader(accessToken),
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ status }),
    },
  )
}

export function createTicket(
  accessToken: string,
  ticket: CreateTicketRequest,
): Promise<TicketResponse> {
  return apiRequest<TicketResponse>('/api/v1/tickets', {
    method: 'POST',
    headers: {
      ...authorizationHeader(accessToken),
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(ticket),
  })
}

export function updateTicket(
  accessToken: string,
  ticketId: string,
  ticket: UpdateTicketRequest,
): Promise<TicketDetails> {
  return apiRequest<TicketDetails>(`/api/v1/tickets/${ticketId}`, {
    method: 'PATCH',
    headers: {
      ...authorizationHeader(accessToken),
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(ticket),
  })
}

export function cancelTicket(
  accessToken: string,
  ticketId: string,
): Promise<TicketDetails> {
  return apiRequest<TicketDetails>(
    `/api/v1/tickets/${ticketId}/cancel`,
    {
      method: 'POST',
      headers: authorizationHeader(accessToken),
    },
  )
}
