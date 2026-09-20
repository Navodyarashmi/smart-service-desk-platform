import type { CurrentUser, Role } from '../types/auth.ts'
import type { TicketCategory, TicketSummary } from '../types/ticket.ts'

export type Workspace = 'employee' | 'technician' | 'administrator'

const TICKET_CATEGORIES: TicketCategory[] = [
  'HARDWARE',
  'SOFTWARE',
  'NETWORK',
  'ACCESS',
  'OTHER',
]

export function formatLabel(value: string): string {
  return value
    .toLowerCase()
    .replaceAll('_', ' ')
    .replace(/\b\w/g, (character) => character.toUpperCase())
}

export function resolveWorkspace(user: CurrentUser): Workspace {
  if (user.roles.includes('ADMINISTRATOR')) return 'administrator'
  if (user.roles.includes('TECHNICIAN')) return 'technician'
  return 'employee'
}

export function primaryRole(roles: Role[]): Role {
  if (roles.includes('ADMINISTRATOR')) return 'ADMINISTRATOR'
  if (roles.includes('TECHNICIAN')) return 'TECHNICIAN'
  return 'EMPLOYEE'
}

export function filterTickets(
  tickets: TicketSummary[],
  searchQuery: string,
): TicketSummary[] {
  const query = searchQuery.trim().toLowerCase()
  if (!query) return tickets
  return tickets.filter((ticket) =>
    [
      ticket.referenceCode,
      ticket.title,
      ticket.category,
      ticket.priority,
      ticket.status,
    ].some((value) => value.toLowerCase().includes(query)),
  )
}

export function countTicketsByCategory(tickets: TicketSummary[]) {
  return TICKET_CATEGORIES.map((category) => ({
    category,
    count: tickets.filter((ticket) => ticket.category === category).length,
  }))
}
