import {
  Bell,
  CheckCircle2,
  ChevronRight,
  CircleUserRound,
  Clock3,
  Headphones,
  LayoutDashboard,
  ListTodo,
  LoaderCircle,
  LogOut,
  Plus,
  Search,
  TicketCheck,
  UsersRound,
} from 'lucide-react'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import {
  clearAccessToken,
  getAccessToken,
  getCurrentUser,
} from '../api/authApi'
import type { CurrentUser } from '../types/auth'

const statistics = [
  {
    label: 'Open tickets',
    value: '08',
    detail: '2 need attention',
    icon: TicketCheck,
    tone: 'blue',
  },
  {
    label: 'In progress',
    value: '05',
    detail: 'Across 3 categories',
    icon: Clock3,
    tone: 'violet',
  },
  {
    label: 'Resolved',
    value: '24',
    detail: 'This month',
    icon: CheckCircle2,
    tone: 'green',
  },
]

const recentTickets = [
  {
    id: 'HD-1042',
    title: 'Unable to connect to office Wi-Fi',
    category: 'Network',
    status: 'In progress',
    priority: 'High',
  },
  {
    id: 'HD-1038',
    title: 'Request access to inventory dashboard',
    category: 'Access',
    status: 'Open',
    priority: 'Medium',
  },
  {
    id: 'HD-1031',
    title: 'Printer on second floor is unavailable',
    category: 'Hardware',
    status: 'Resolved',
    priority: 'Low',
  },
]

export function DashboardPage() {
  const navigate = useNavigate()
  const [user, setUser] = useState<CurrentUser | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const accessToken = getAccessToken()

    if (!accessToken) {
      navigate('/login', { replace: true })
      return
    }

    getCurrentUser(accessToken)
      .then(setUser)
      .catch(() => {
        clearAccessToken()
        navigate('/login', { replace: true })
      })
      .finally(() => setLoading(false))
  }, [navigate])

  const signOut = () => {
    clearAccessToken()
    navigate('/login', { replace: true })
  }

  if (loading || !user) {
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
            <span className="nav-item__count">8</span>
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

      <main className="dashboard-main">
        <header className="topbar">
          <div className="topbar__search">
            <Search size={19} />
            <input
              type="search"
              placeholder="Search tickets, people, or categories"
              aria-label="Search"
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
                <small>{user.roles.join(' · ')}</small>
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
                Here is what is happening with your support requests today.
              </p>
            </div>

            <button className="primary-button primary-button--compact">
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
                <h2>Recent tickets</h2>
                <p>Follow your latest requests and updates.</p>
              </div>
              <button className="text-button" type="button">
                View all
                <ChevronRight size={17} />
              </button>
            </header>

            <div className="ticket-table">
              {recentTickets.map((ticket) => (
                <article className="ticket-row" key={ticket.id}>
                  <div className="ticket-row__identity">
                    <span className="ticket-row__icon">
                      <TicketCheck size={19} />
                    </span>
                    <div>
                      <strong>{ticket.title}</strong>
                      <span>
                        {ticket.id} · {ticket.category}
                      </span>
                    </div>
                  </div>

                  <span
                    className={`priority priority--${ticket.priority.toLowerCase()}`}
                  >
                    {ticket.priority}
                  </span>

                  <span
                    className={`status status--${ticket.status
                      .toLowerCase()
                      .replace(' ', '-')}`}
                  >
                    {ticket.status}
                  </span>

                  <button
                    className="row-action"
                    type="button"
                    aria-label={`Open ${ticket.id}`}
                  >
                    <ChevronRight size={19} />
                  </button>
                </article>
              ))}
            </div>
          </section>
        </div>
      </main>
    </div>
  )
}