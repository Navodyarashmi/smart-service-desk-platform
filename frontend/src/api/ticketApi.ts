import type {
  CreateTicketRequest,
  TicketDetails,
  TicketActivity,
  TicketAttachment,
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

export function getTicketActivity(accessToken: string, ticketId: string): Promise<TicketActivity[]> {
  return apiRequest<TicketActivity[]>(`/api/v1/tickets/${ticketId}/activity`, {
    headers: authorizationHeader(accessToken),
  })
}

export function addTicketComment(accessToken: string, ticketId: string, message: string, internalNote: boolean): Promise<TicketActivity> {
  return apiRequest<TicketActivity>(`/api/v1/tickets/${ticketId}/activity`, {
    method: 'POST',
    headers: { ...authorizationHeader(accessToken), 'Content-Type': 'application/json' },
    body: JSON.stringify({ message, internalNote }),
  })
}

export function getTicketAttachments(
  accessToken: string,
  ticketId: string,
): Promise<TicketAttachment[]> {
  return apiRequest<TicketAttachment[]>(`/api/v1/tickets/${ticketId}/attachments`, {
    headers: authorizationHeader(accessToken),
  })
}

export function uploadTicketAttachment(
  accessToken: string,
  ticketId: string,
  file: File,
): Promise<TicketAttachment> {
  const body = new FormData()
  body.append('file', file)
  return apiRequest<TicketAttachment>(`/api/v1/tickets/${ticketId}/attachments`, {
    method: 'POST',
    headers: authorizationHeader(accessToken),
    body,
  })
}

export async function downloadTicketAttachment(
  accessToken: string,
  ticketId: string,
  attachment: TicketAttachment,
): Promise<void> {
  const response = await fetch(
    `/api/v1/tickets/${ticketId}/attachments/${attachment.id}/content`,
    { headers: authorizationHeader(accessToken) },
  )
  if (!response.ok) throw new Error('Attachment download failed.')
  const objectUrl = URL.createObjectURL(await response.blob())
  const link = document.createElement('a')
  link.href = objectUrl
  link.download = attachment.filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(objectUrl)
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
