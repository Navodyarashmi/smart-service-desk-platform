import {
  AlertTriangle,
  Bell,
  CheckCircle2,
  ChevronRight,
  CircleUserRound,
  Clock3,
  Headphones,
  Inbox,
  LayoutDashboard,
  ListTodo,
  LoaderCircle,
  LogOut,
  Plus,
  Search,
  TicketCheck,
  UsersRound,
} from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import { ApiRequestError } from '../api/apiClient'
import {
  clearAccessToken,
  getAccessToken,
  getCurrentUser,
} from '../api/authApi'
import { getTickets } from '../api/ticketApi'
import { CreateTicketModal } from '../components/CreateTicketModal'
import { TicketDetailsModal } from '../components/TicketDetailsModal'
import type { CurrentUser } from '../types/auth'
import type {
  TicketDetails,
  TicketResponse,
  TicketStatus,
  TicketSummary,
} from '../types/ticket'

function formatLabel(value: string): string {
  return value
    .toLowerCase()
    .replaceAll('_', ' ')
    .replace(/\b\w/g, (character) => character.toUpperCase())
}

function formatTicketDate(value: string): string {
  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  }).format(new Date(value))
}

export function DashboardPage() {
  const navigate = useNavigate()

  const [user, setUser] = useState<CurrentUser | null>(null)
  const [tickets, setTickets] = useState<TicketSummary[]>([])
  const [accessToken] = useState(() => getAccessToken() ?? '')
  const [searchQuery, setSearchQuery] = useState('')
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [ticketModalOpen, setTicketModalOpen] = useState(false)
  const [selectedTicketId, setSelectedTicketId] = useState<string | null>(
  null,
)

  useEffect(() => {
    if (!accessToken) {
  navigate('/login', { replace: true })
  return
}

Promise.all([
  getCurrentUser(accessToken),
  getTickets(accessToken),
])
      .then(([currentUser, currentTickets]) => {
        setUser(currentUser)
        setTickets(currentTickets)
      })
      .catch((error: unknown) => {
        if (error instanceof ApiRequestError && error.status === 401) {
          clearAccessToken()
          navigate('/login', { replace: true })
          return
        }

        setLoadError(
          'We could not load your workspace. Check the backend and try again.',
        )
      })
      .finally(() => setLoading(false))
  }, [accessToken, navigate])

  const filteredTickets = useMemo(() => {
    const normalizedSearch = searchQuery.trim().toLowerCase()

    if (!normalizedSearch) {
      return tickets
    }

    return tickets.filter((ticket) =>
      [
        ticket.referenceCode,
        ticket.title,
        ticket.category,
        ticket.priority,
        ticket.status,
      ].some((value) => value.toLowerCase().includes(normalizedSearch)),
    )
  }, [searchQuery, tickets])

  const ticketCounts = useMemo(() => {
    const countStatuses = (...statuses: TicketStatus[]) =>
      tickets.filter((ticket) => statuses.includes(ticket.status)).length

    return {
      open: countStatuses('OPEN', 'ASSIGNED'),
      inProgress: countStatuses('IN_PROGRESS'),
      resolved: countStatuses('RESOLVED', 'CLOSED'),
    }
  }, [tickets])

  const statistics = [
    {
      label: 'Open tickets',
      value: String(ticketCounts.open).padStart(2, '0'),
      detail: `${tickets.length} total requests`,
      icon: TicketCheck,
      tone: 'blue',
    },
    {
      label: 'In progress',
      value: String(ticketCounts.inProgress).padStart(2, '0'),
      detail: 'Currently being handled',
      icon: Clock3,
      tone: 'violet',
    },
    {
      label: 'Resolved',
      value: String(ticketCounts.resolved).padStart(2, '0'),
      detail: 'Successfully completed',
      icon: CheckCircle2,
      tone: 'green',
    },
  ]

  const signOut = () => {
    clearAccessToken()
    navigate('/login', { replace: true })
  }

  const handleTicketCreated = (ticket: TicketResponse) => {
    const summary: TicketSummary = {
      id: ticket.id,
      referenceCode: ticket.referenceCode,
      title: ticket.title,
      category: ticket.category,
      priority: ticket.priority,
      status: ticket.status,
      createdAt: ticket.createdAt,
      updatedAt: ticket.createdAt,
    }

    setTickets((currentTickets) => [summary, ...currentTickets])
  }

  const handleTicketChanged = (ticket: TicketDetails) => {
    setTickets((currentTickets) =>
      currentTickets.map((currentTicket) =>
        currentTicket.id === ticket.id
          ? {
              id: ticket.id,
              referenceCode: ticket.referenceCode,
              title: ticket.title,
              category: ticket.category,
              priority: ticket.priority,
              status: ticket.status,
              createdAt: ticket.createdAt,
              updatedAt: ticket.updatedAt,
            }
          : currentTicket,
      ),
    )
  }

  if (loading) {
    return (
      <main className="loading-screen">
        <span className="brand__mark">
          <Headphones size={24} />
        </span>
        <LoaderCircle className="spin" size={24} />
        <p>Preparing your workspace…</p>
      </main>
    )
  }

  if (loadError || !user) {
    return (
      <main className="loading-screen">
        <span className="error-state__icon">
          <AlertTriangle size={24} />
        </span>
        <strong>Workspace unavailable</strong>
        <p>{loadError ?? 'Your workspace could not be loaded.'}</p>
        <button
          className="secondary-button"
          type="button"
          onClick={() => window.location.reload()}
        >
          Try again
        </button>
      </main>
    )
  }

  const firstName = user.fullName.split(' ')[0]
  const initials = user.fullName
    .split(' ')
    .map((part) => part[0])
    .slice(0, 2)
    .join('')
    .toUpperCase()

  return (
    <div className="dashboard-shell">
      <aside className="sidebar">
        <div className="brand sidebar__brand">
          <span className="brand__mark">
            <Headphones size={22} />
          </span>
          <span className="brand__name">HelpHub</span>
        </div>

        <nav className="sidebar__nav" aria-label="Main navigation">
          <a className="nav-item nav-item--active" href="#overview">
            <LayoutDashboard size={19} />
            Overview
          </a>

          <a className="nav-item" href="#tickets">
            <ListTodo size={19} />
            My tickets
            <span className="nav-item__count">{tickets.length}</span>
          </a>

          <a className="nav-item" href="#team">
            <UsersRound size={19} />
            Team
          </a>
        </nav>

        <div className="sidebar__support">
          <span className="sidebar__support-icon">
            <Headphones size={20} />
          </span>
          <strong>Need urgent help?</strong>
          <p>Contact the service desk directly.</p>
          <button type="button">View contacts</button>
        </div>

        <button className="sign-out-button" type="button" onClick={signOut}>
          <LogOut size={18} />
          Sign out
        </button>
      </aside>

      <main className="dashboard-main" id="overview">
        <header className="topbar">
          <div className="topbar__search">
            <Search size={19} />
            <input
              type="search"
              value={searchQuery}
              onChange={(event) => setSearchQuery(event.target.value)}
              placeholder="Search your tickets"
              aria-label="Search tickets"
            />
          </div>

          <div className="topbar__actions">
            <button
              className="icon-button"
              type="button"
              aria-label="Notifications"
            >
              <Bell size={20} />
              <span className="notification-dot" />
            </button>

            <div className="user-menu">
              <span className="user-avatar">{initials}</span>

              <span className="user-menu__details">
                <strong>{user.fullName}</strong>
                <small>{user.roles.map(formatLabel).join(' · ')}</small>
              </span>

              <CircleUserRound size={19} />
            </div>
          </div>
        </header>

        <div className="dashboard-content">
          <section className="welcome-row">
            <div>
              <p className="dashboard-eyebrow">Employee workspace</p>
              <h1>Good to see you, {firstName}.</h1>
              <p>
                Track your requests and reach the right support team.
              </p>
            </div>

            <button
              className="primary-button primary-button--compact"
              type="button"
              onClick={() => setTicketModalOpen(true)}
            >
              <Plus size={19} />
              Create ticket
            </button>
          </section>

          <section className="stats-grid" aria-label="Ticket statistics">
            {statistics.map(({ label, value, detail, icon: Icon, tone }) => (
              <article className="stat-card" key={label}>
                <span className={`stat-card__icon stat-card__icon--${tone}`}>
                  <Icon size={21} />
                </span>
                <div className="stat-card__value">{value}</div>
                <h2>{label}</h2>
                <p>{detail}</p>
              </article>
            ))}
          </section>

          <section className="ticket-panel" id="tickets">
            <header className="ticket-panel__header">
              <div>
                <h2>Your tickets</h2>
                <p>Live requests loaded securely from the service desk.</p>
              </div>

              <span className="ticket-result-count">
                {filteredTickets.length}{' '}
                {filteredTickets.length === 1 ? 'ticket' : 'tickets'}
              </span>
            </header>

            {filteredTickets.length === 0 ? (
              <div className="empty-tickets">
                <span className="empty-tickets__icon">
                  <Inbox size={25} />
                </span>
                <h3>
                  {searchQuery
                    ? 'No matching tickets'
                    : 'No tickets created yet'}
                </h3>
                <p>
                  {searchQuery
                    ? 'Try another search term.'
                    : 'Create your first request and track it here.'}
                </p>

                {!searchQuery && (
                  <button
                    className="text-button"
                    type="button"
                    onClick={() => setTicketModalOpen(true)}
                  >
                    Create your first ticket
                    <ChevronRight size={17} />
                  </button>
                )}
              </div>
            ) : (
              <div className="ticket-table">
                {filteredTickets.map((ticket) => (
                  <article className="ticket-row" key={ticket.id}>
                    <div className="ticket-row__identity">
                      <span className="ticket-row__icon">
                        <TicketCheck size={19} />
                      </span>

                      <div>
                        <strong>{ticket.title}</strong>
                        <span>
                          {ticket.referenceCode} ·{' '}
                          {formatLabel(ticket.category)} ·{' '}
                          {formatTicketDate(ticket.createdAt)}
                        </span>
                      </div>
                    </div>

                    <span
                      className={`priority priority--${ticket.priority.toLowerCase()}`}
                    >
                      {formatLabel(ticket.priority)}
                    </span>

                    <span
                      className={`status status--${ticket.status
                        .toLowerCase()
                        .replaceAll('_', '-')}`}
                    >
                      {formatLabel(ticket.status)}
                    </span>

                    <button
                        className="row-action"
                        type="button"
                        onClick={() => setSelectedTicketId(ticket.id)}
                        aria-label={`Open ${ticket.referenceCode}`}
                    >
                      <ChevronRight size={19} />
                    </button>
                  </article>
                ))}
              </div>
            )}
          </section>
        </div>
      </main>

      <CreateTicketModal
        open={ticketModalOpen}
        accessToken={accessToken}
        onClose={() => setTicketModalOpen(false)}
        onCreated={handleTicketCreated}
      />
        {selectedTicketId && (
        <TicketDetailsModal
          key={selectedTicketId}
          ticketId={selectedTicketId}
          accessToken={accessToken}
          onClose={() => setSelectedTicketId(null)}
          onChanged={handleTicketChanged}
        />
      )}
    </div>
  )
}