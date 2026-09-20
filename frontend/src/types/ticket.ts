export type TicketCategory =
  | 'HARDWARE'
  | 'SOFTWARE'
  | 'NETWORK'
  | 'ACCESS'
  | 'OTHER'

export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'

export type TicketStatus =
  | 'OPEN'
  | 'ASSIGNED'
  | 'IN_PROGRESS'
  | 'RESOLVED'
  | 'CLOSED'
  | 'CANCELLED'

export interface CreateTicketRequest {
  title: string
  description: string
  category: TicketCategory
  priority: TicketPriority
}

export interface TicketResponse {
  id: string
  referenceCode: string
  title: string
  description: string
  category: TicketCategory
  priority: TicketPriority
  status: TicketStatus
  requesterId: string
  createdAt: string
}

export interface TicketSummary {
  id: string
  referenceCode: string
  title: string
  category: TicketCategory
  priority: TicketPriority
  status: TicketStatus
  createdAt: string
  updatedAt: string
}