import assert from 'node:assert/strict'
import { describe, it } from 'node:test'

import type { CurrentUser } from '../types/auth.ts'
import type { TicketSummary } from '../types/ticket.ts'
import {
  countTicketsByCategory,
  filterTickets,
  formatLabel,
  primaryRole,
  resolveWorkspace,
} from './dashboard.ts'

const user = (roles: CurrentUser['roles']): CurrentUser => ({
  id: '00000000-0000-0000-0000-000000000001',
  email: 'user@example.com',
  fullName: 'Test User',
  roles,
})

const ticket = (
  title: string,
  category: TicketSummary['category'],
  status: TicketSummary['status'] = 'OPEN',
): TicketSummary => ({
  id: title,
  referenceCode: `HD-${title.toUpperCase()}`,
  title,
  category,
  priority: 'MEDIUM',
  status,
  createdAt: '2026-09-21T00:00:00Z',
  updatedAt: '2026-09-21T00:00:00Z',
})

describe('dashboard role rules', () => {
  it('selects the highest privileged workspace and primary role', () => {
    assert.equal(resolveWorkspace(user(['EMPLOYEE'])), 'employee')
    assert.equal(resolveWorkspace(user(['EMPLOYEE', 'TECHNICIAN'])), 'technician')
    assert.equal(
      resolveWorkspace(user(['EMPLOYEE', 'TECHNICIAN', 'ADMINISTRATOR'])),
      'administrator',
    )
    assert.equal(primaryRole(['EMPLOYEE', 'TECHNICIAN']), 'TECHNICIAN')
  })

  it('formats machine labels for the interface', () => {
    assert.equal(formatLabel('IN_PROGRESS'), 'In Progress')
    assert.equal(formatLabel('administrator'), 'Administrator')
  })
})

describe('dashboard ticket analytics', () => {
  const tickets = [
    ticket('Broken laptop', 'HARDWARE'),
    ticket('Printer setup', 'HARDWARE', 'RESOLVED'),
    ticket('VPN unavailable', 'NETWORK'),
  ]

  it('searches across visible ticket fields without case sensitivity', () => {
    assert.deepEqual(filterTickets(tickets, 'vpn'), [tickets[2]])
    assert.deepEqual(filterTickets(tickets, 'resolved'), [tickets[1]])
    assert.equal(filterTickets(tickets, '  ').length, 3)
  })

  it('returns a stable category series including zero-count categories', () => {
    assert.deepEqual(countTicketsByCategory(tickets), [
      { category: 'HARDWARE', count: 2 },
      { category: 'SOFTWARE', count: 0 },
      { category: 'NETWORK', count: 1 },
      { category: 'ACCESS', count: 0 },
      { category: 'OTHER', count: 0 },
    ])
  })
})
